package TopoGen;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import Ofdba.ClientFlow;
import Ofdba.Network;
import Ofdba.NetBandwidth;
import TopoGen.RanNetParams;

public class RanNetwork extends Network{
	private Map<String, List<String>> _classNodes = new HashMap<>();
	private List<List<String>> _sites = new ArrayList<>();
	private RandomIntSeq _siteIndexGen = new RandomIntSeq();
	
	private RanNetParams _netPar;
	private RanCliParams _cliPar;
	
	public enum ClientDist {
		// Clients are equally distributed accross all sites.
		FLAT,
		// 70 % of clients are distributed accross 30% of sites.
		WORKING_HOURS,
		// 80 % of clients are distributed accross 10% of sites.
		PUBLIC_EVEN;
	};

	public RanNetwork(RanNetParams par, RanCliParams cliPar) {
		_netPar=par;
		_cliPar = cliPar;
		init(genTopo());
		reGenClients(ClientDist.FLAT);
	}
	
	private String getNextNodeInRing(List<String> nodesRing, int ind) {
		int nextInd = ind + 1;
		nextInd  %= nodesRing.size();
		return nodesRing.get(nextInd);
	}
	
	private NetBandwidth genTopo(){
		NetBandwidth topo = new NetBandwidth();
		topo.init(_netPar.getNodeCnt());
		
		//Generating GW and TR nodes name
		ArrayList<String> gwNodes = new ArrayList<>();
		for (int ind=0; ind < _netPar.getGwCnt(); ++ind) {
			gwNodes.add(RanNodes.GW.prefix + ind );
		}
		_classNodes.put(RanNodes.GW.prefix, gwNodes);
		ArrayList<String> trNodes = new ArrayList<>();
		for (int ind=0; ind < _netPar.getTrCnt(); ++ind) {
			trNodes.add(RanNodes.TR.prefix + ind );
		}
		_classNodes.put(RanNodes.TR.prefix, trNodes);
		
		insertNoTraverseNode(RanNodes.WAN.prefix);
		//Binding GW nodes to WAN and to each other
		for (int ind=0; ind<_netPar.getGwCnt(); ++ind ) {
			String gwNode = gwNodes.get(ind);
			topo.setBothLinks(
					RanNodes.WAN.prefix,
					gwNode, _netPar.getGwToWanLinkBw());
			String gwNodeNext = getNextNodeInRing(gwNodes, ind);
			topo.setBothLinks(
					gwNodeNext, gwNode, 
					_netPar.getGwToGwLinkBw());
		}
		
		int gwIndex = 0;
		int spareGwConnectors = _netPar.trPerGwCnt/2;
		for (int ind=0; ind<_netPar.getTrCnt(); ++ind ) {
			String trNode = trNodes.get(ind);
			String trNodeNext = getNextNodeInRing(trNodes, ind);
			topo.setBothLinks(
					trNode , trNodeNext, 
					_netPar.getTrToTrLinkBw());
			if (spareGwConnectors == 0) {
				gwIndex += 1;
				if (gwIndex == _netPar.getGwCnt()) {
					throw new RuntimeException(
							"Unbalanced network : \n" + _netPar + "\n"
							+ "\ntrPerGwCnt is too low");
				}
				spareGwConnectors = _netPar.trPerGwCnt/2;
			}
			spareGwConnectors-=1;
			String gw1 = gwNodes.get(gwIndex);
			String gw2 = getNextNodeInRing(gwNodes, gwIndex);
			//System.out.println("+ bind" + trNode + " " + gw1);

			topo.setBothLinks(
					trNode, gw1, 
					_netPar.getTrToGwLinkBw());
			topo.setBothLinks(
					trNode, gw2, 
					_netPar.getTrToGwLinkBw());
		}
		
		//Adding site with meshed topologu
		List<List<String>> sites = new ArrayList<>();
		for (int indT=0; indT<_netPar.getSitesCnt();indT+=1){
			List<String> siteNodes = new ArrayList<>();
			siteNodes.add(trNodes.get(indT));
			siteNodes.add(getNextNodeInRing(trNodes, indT));
			sites.add(siteNodes);
			_sites.add(new ArrayList<String>());
		}
		
		ArrayList<String> apNodes = new ArrayList<>();
		for (int indAp = 0; indAp < _netPar.apCnt; ++ indAp) {
			int siteInd = indAp %_netPar.getSitesCnt();
			String apName = RanNodes.AP.prefix  + siteInd  + "_" 
					+  (sites.get(siteInd).size()-2);
			sites.get(siteInd).add(apName);
			_sites.get(siteInd).add(apName);
			apNodes.add(apName);
		}
		_classNodes.put(RanNodes.AP.prefix, apNodes);
		
		for (List<String> siteNodes : sites) {
			for (int indI=0; indI<siteNodes.size(); ++indI){
				for (int indJ=indI+1; indJ<siteNodes.size(); ++indJ) {
					if (RanNodes.classifyNode(siteNodes.get(indI)) == RanNodes.TR
							&& RanNodes.classifyNode(siteNodes.get(indJ)) == RanNodes.TR) {
						continue;
					}
					topo.setBothLinks(
							siteNodes.get(indI),
							siteNodes.get(indJ),
							_netPar.apToTrLinkBw);
				}
			}
		}
		return topo;
	}
	
	private String getRandomAp(List<String> apList, Random rnd, int seed) {
		int randInd = Math.abs(rnd.nextInt(seed)) % apList.size();
		return apList.get(randInd);
	}
	
	private void setFlatSiteSelection() {
		_siteIndexGen.clear();
		for (int ind=0; ind<_sites.size(); ++ind) {
			_siteIndexGen.add(ind);
		}
	}

	private void setSpecificSiteSelection(double sitesAmmount, 
			double clientsAmmount) {
		_siteIndexGen.clear();
		int clientsCnt = (int)Math.floor(clientsAmmount * 100);
		int otherClientsCnt = 100 - clientsCnt;
		int sitesCnt = (int) Math.floor(sitesAmmount * _sites.size() );
		if (sitesCnt == 0 ){
			sitesCnt = 1;
		}
		for (int ind=0; ind <_sites.size(); ++ind) {
			if (ind < sitesCnt) {
				for (int indCli=0; indCli<clientsCnt; ++indCli) {
					_siteIndexGen.add(ind);
				}
			} else {
				for (int indCli=0; indCli<otherClientsCnt; ++indCli) {
					_siteIndexGen.add(ind);
				}				
			}
		}
	}
	
	public void reGenClients(ClientDist clientDist) {
		if (_cliPar== null ) {
			return;
		}
		removeClients();
		_siteIndexGen.clear();
		switch (clientDist) {
		case FLAT:
			setFlatSiteSelection();
			break;
		case WORKING_HOURS:
			setSpecificSiteSelection(0.3, 0.6);
			break;
		case PUBLIC_EVEN:
			setSpecificSiteSelection(0.05, 0.9);
			break;
		default:
			throw new RuntimeException("Missconfig");
		}
			
		Random rnd = ThreadLocalRandom.current();
		for (int cliInd = 0; cliInd < _cliPar.getMaxClientCount(); ++cliInd ) {
			List<String> fromApList = _sites.get(_siteIndexGen.get());
			List<String> apList = _classNodes.get(RanNodes.AP.prefix);
			String srcApNode =  getRandomAp(fromApList, rnd, cliInd+1);
			String dstApNode =  getRandomAp(apList, rnd, cliInd+2);
			int seed = 3;
		    while(srcApNode.equals(dstApNode)) {
		    	dstApNode =  getRandomAp(apList, rnd, seed);
		    	seed+=1;
		    }
    		RanCliParams.ClientClass cliClass = _cliPar.genClientClass();
    		String dstNode = cliClass.flowTp == RanCliParams.FlowType.TO_WAN
    				? RanNodes.WAN.prefix : dstApNode ;
    		
    		pushClient(new ClientFlow(
	   				srcApNode, dstNode, 
    				cliClass.ulWeight, cliClass.dlWeight,
    				cliClass.getHopCount(), 
    				cliClass.classId,
    				_netPar.worstApToApHopCnt(),
    				cliClass.getAr()));
		}
	}
}
