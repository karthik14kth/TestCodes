package Ofdba;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import Ofdba.ClientFlow;
import Ofdba.PathFind.PathCostBasic;

public class Network {
	public NetBandwidth spareBw;
	public NetBandwidth topology;
	public Set<String> noTraverseNode = new HashSet<>();

	// All clients flow added into network
	// no matter added or not
	public List<ClientFlow> clients= new ArrayList<>();
	
	public Map<Integer, Integer> serviceDenyalCliCntPerClass = new HashMap<>();
	public Map<Integer, Integer> connectedCliCntPerClass = new HashMap<>();
	public Map<Integer, Integer> totalCliCntPerClass = new HashMap<>();
	
	public enum PathFindType {
		DIJEXTRA_BASE,
		DIJEXTRA_SPAREBW,
		DIJEXTRA_QOS_MP,
		DRY_RUN;
	};
	
	public Network() {
	}
	
	public void init(NetBandwidth topology) {
		this.topology = topology;
		reset();	
	}
	
	public void reset() {
		spareBw = new NetBandwidth(this.topology);
		serviceDenyalCliCntPerClass.clear();
		connectedCliCntPerClass.clear();
		totalCliCntPerClass.clear();
		for (ClientFlow client : clients) {
			client.dropConnection();
		}
	}
	
	public int pushClient(ClientFlow client) {
		int clientId = clients.size();
		clients.add(client);
		return clientId;
	}
	
	private void incClassCounter(Map<Integer, Integer> classCntMap, int classId) {
		if (!classCntMap.containsKey(classId)) {
			serviceDenyalCliCntPerClass.put(classId, 0);
			connectedCliCntPerClass.put(classId, 0);
			totalCliCntPerClass.put(classId, 0);
		}
		classCntMap.put(classId, classCntMap.get(classId)+1);
	}
	
	private boolean setClientFlow(PathFindType pathFindTp,
			ClientFlow client, boolean isUplink) {
	
		PathFind.PathCostFunc pcFnc = null;
		NetBandwidth bw = null; 		
		switch(pathFindTp) {
		case DIJEXTRA_BASE:
			pcFnc = new PathFind.PathCostBasic();
			bw = topology;
			break;
		case DIJEXTRA_SPAREBW:
			pcFnc = new PathFind.PathCostBasic();
			bw = spareBw;
			break;
		case DIJEXTRA_QOS_MP:
			pcFnc = new PathFind.PathCostMpQos();
			bw = spareBw;
			break;
			
		default:
			throw new RuntimeException("New algorithm not registred");
		}
		
		List<String> path = PathFind.dijextraPathFind(
				pcFnc, bw, noTraverseNode, client, isUplink);
	
		if (!client.isValidPath(path, isUplink)
				|| !spareBw.isEnoughBw(
						path, client.getBw(isUplink))) {
			return false;
		}
		client.setPath(isUplink, path);
		spareBw.setPath(path, client.getBw(isUplink));
		return true;
	}

	private void unsetClientFlows(ClientFlow client) {
		if (client.pathUl != null) {
			spareBw.unsetPath(
					client.pathUl, 
					client.reqBandwidthUl);
		}
		if (client.pathDl != null) {
			spareBw.unsetPath(
					client.pathDl, 
					client.reqBandwidthDl);
		}
	}
	
	
	public void connectAllClients(PathFindType pathFindTp) {
		long start = System.currentTimeMillis();
		long lastCheck = start;
		int progress = 0;
		for (ClientFlow client : clients) {
			
			if (pathFindTp != PathFindType.DRY_RUN
					&& setClientFlow(pathFindTp, client, false)
					&& setClientFlow(pathFindTp, client, true)) {
				client.setConnection();
			} else {
				unsetClientFlows(client);
			}
			
			if (client.isConnected) {
				incClassCounter(connectedCliCntPerClass, client.clientClass);
			} else {
				incClassCounter(serviceDenyalCliCntPerClass, client.clientClass);
			}
			incClassCounter(totalCliCntPerClass, client.clientClass);
			
			long check = System.currentTimeMillis();
			progress+=1;
			if ((check - lastCheck)/1000 > 5) {
				lastCheck = check;
				double rate=1000.0*progress/(check - start);
		    	System.out.println("Procesing rate: " + rate + " per sec\n"
		    			+ " forecast: " + (double)(clients.size()-progress)/rate);
			}
		}
	}
	
	public void removeClients() {
		clients.clear();
		reset();
	}
	
	public void insertNoTraverseNode(String node) {
		noTraverseNode.add(node);
	}
	
	public double getUtilization() {
		return 1.0- (double)(spareBw.getTotalBw())/topology.getTotalBw();
	}
	
	public String simStatToStr() {
		String result = "id\tok\tnok\ttot\n";
		int okTot = 0;
		int nokTot = 0;
		int tot = 0;
		for (Integer classId : totalCliCntPerClass.keySet()) {
			result += classId + "\t" 
					+ connectedCliCntPerClass.get(classId) + "\t"
					+ serviceDenyalCliCntPerClass.get(classId) + "\t"
					+ totalCliCntPerClass.get(classId) + "\n";
			okTot+=connectedCliCntPerClass.get(classId);
			nokTot += serviceDenyalCliCntPerClass.get(classId);
			tot += totalCliCntPerClass.get(classId);
		}
		result += "tot" + "\t" 
				+ okTot + "\t"
				+ nokTot + "\t"
				+ tot + "\n";
		result += "utilization: " + getUtilization();
		return result;
	}
}
