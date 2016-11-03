package TopoGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class RanCliParams {
	public enum FlowType {
		TO_WAN,
		TO_AP
	};
	public enum LatencyReq {
		REAL_TIME,
		NORMAL,
		BEST_EFFORTS
	};
	public enum BwReq {
		SMALL(1),
		MEDIUM(2),
		HIGH(3);
		int bw;
		BwReq(int bw) {
			this.bw = bw;
		}
	};

	private final RanNetParams netParam;

	
	public class ClientClass {
		public int classId;
		public final FlowType flowTp;
		public final LatencyReq latencyReq;
		public final BwReq bwReq;
		public final int ulWeight;
		public final int dlWeight;

		public ClientClass(
				FlowType flowTp,
				LatencyReq latencyReq,
				BwReq bwReq,
				int ulWeight,
				int dlWeight) {;
		    this.classId = -1;
		    this.flowTp = flowTp;
		    this.latencyReq = latencyReq;
		    this.bwReq = bwReq;
		    this.ulWeight = ulWeight;
		    this.dlWeight = dlWeight;			
		}
		
		void setClassId(int classId) {
			this.classId = classId;
		}
		
		public int getUlBw() {
			return this.ulWeight * bwReq.bw;
		}
		public int getDlBw() {
			return this.dlWeight * bwReq.bw;
		}
		public int getHopCount() {
			int minHop = flowTp == FlowType.TO_AP 
					? netParam.expApToApHopCnt() 
					: netParam.expApToWanHopCnt();
			int maxHop = flowTp == FlowType.TO_AP 
					? netParam.worstApToApHopCnt() 
					: netParam.worstApToWanHopCnt();
			switch (latencyReq) {
			case REAL_TIME:
				return minHop;
			case NORMAL:
				return (maxHop+minHop)/2;
			case BEST_EFFORTS:
				return maxHop;
			default:
				throw new RuntimeException("Internal error");
			}
		}
		public double getAr() {
			switch (latencyReq) {
			case REAL_TIME:
				return 0.1;
			case NORMAL:
				return 0.5;
			case BEST_EFFORTS:
				return 0.999;
			default:
				throw new RuntimeException("Internal error");
			}
		}
	}
	
	// Params starting point
	private List<ClientClass> clientClasses = new ArrayList<>();
	private RandomIntSeq _randClientClassIdGen = new RandomIntSeq();
	private Long  setFixedMaxClients = null;
	
	public RanCliParams(RanNetParams netParam) {
		this.netParam = netParam;
	}
	
	public int add(ClientClass clientClass, int classWeight) {
		int classId = clientClasses.size();
		clientClass.classId = classId;
		clientClasses.add(clientClass);
		for (int ind=0; ind<classWeight; ++ind) {
			_randClientClassIdGen.add(classId);
		}
		return classId;
	}
	
	public ClientClass genClientClass() {
		ClientClass cliClass = clientClasses.get(_randClientClassIdGen.get());
		return cliClass;
	}
	
	public void setMaxClientCount(long setFixedMaxClients) {
		this.setFixedMaxClients  = setFixedMaxClients;
	}

	public void unsetMaxClientCount(long setFixedMaxClients) {
		this.setFixedMaxClients  = null;
	}

	public long getMaxClientCount() {
		if (this.setFixedMaxClients  != null) {
			return this.setFixedMaxClients;
		}
		long totalClientsPerSeq = 0;
		long bwPerSequence = 0;
		for (Integer cliClassId : _randClientClassIdGen.getSequnce()) {
			totalClientsPerSeq+=2;
			ClientClass cliClass = clientClasses.get(cliClassId);
			bwPerSequence+= cliClass.dlWeight + cliClass.ulWeight;
		}
		//netParam.getMaxApBw()
		return (2*netParam.getMaxApBw() / bwPerSequence) * totalClientsPerSeq;
	}
}
