package Verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Ui.GsSupport;
import Ofdba.ClientFlow;
import Ofdba.NetBandwidth;
import Ofdba.Network;
import Ofdba.Network.PathFindType;
import TopoGen.RanNetwork;
import TopoGen.RanCliParams;

public class BasicTest {
    public static void main(String[] args) {
    	// Functions below can be used to study algorithm
    	// uncomment single function to check 
    	
    	// 1. Graph visualization. 
    	// Can be used for RAN topology configuration
    	//testGraphGen();
    	
    	// 2. Clients generation. Check number of clients 
    	// generated based on client class
    	//testClientsGen();  	
    	
    	// 3. Test to verify clients distribution around specific APs.
    	//testClientsDistGen();

    	// 4. Algorithm fundamental.
    	//testAlgoritmBasicIdea();

    	// 5. Test funding path for user defined flows
    	// visualazing flows in the topology
    	testSimplePathFind();
    	
    	// 6. Test comparing KPIs in case of normal and modified
    	// dijextra algorithms
    	//testNetworkAllTraffic();

    	// 7. Same as 6, but only AP-WAN flows are considered
    	//testNetworkWanTraffic2();
    	
    	// Irrelevant: scenario does not demonstrate any valubale behavior.
    	//testNetworkAllTrafficMobility();

    	// Irrelevant: image does not bring any value
    	//testPathFindShowAllFlowsMpQosFlat();
    }
    
    public static void testClientsGen() {
    	TopoGen.RanNetParams netParam = new TopoGen.RanNetParams();
    	netParam.apCnt = 4024;
    	netParam.apToTrLinkBw = 10;
    	netParam.apPerSiteCnt = 6;
    	netParam.trPerGwCnt = 8;
		System.out.println("Net params:" + netParam);
		
    	RanCliParams cliParams = new RanCliParams(netParam);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.SMALL,
    			1,3), 5);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			1,3), 2);
    	int lastClassId = cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.HIGH,
    			1,3), 1);

	    System.out.println("Total clients cnt: " 
	    		+ cliParams.getMaxClientCount()); 

	    RanNetwork net = new RanNetwork(netParam, null);
    	for (int ind=0; ind<cliParams.getMaxClientCount(); ++ind) {
    		RanCliParams.ClientClass cliClass = cliParams.genClientClass();
    		net.pushClient(new ClientFlow(
    				"XXX", "wan", 
    				cliClass.ulWeight, cliClass.dlWeight, 
    				cliClass.getHopCount(), 
    				cliClass.classId, 
    				netParam.worstApToApHopCnt(), 1));
    		/*        	System.out.format("id: %d, ul: %d, dl: %d, hopCnt : %d\n",
        			cliClass.classId, cliClass.getUlBw(), cliClass.getDlBw(),
        			cliClass.getHopCount());    		*/
    	}
    	net.connectAllClients(Network.PathFindType.DRY_RUN);
    	
    	for (int classId = 0; classId <= lastClassId; ++classId) {
    	    System.out.println("ClassID: " + classId + " clientsCnt: " 
    	    		+ net.totalCliCntPerClass.get(classId));
    	}
    }

    public static void testClientsDistGen() {
    	TopoGen.RanNetParams netParam = new TopoGen.RanNetParams();
    	netParam.apCnt = 16;
    	netParam.apToTrLinkBw = 10;
    	netParam.apPerSiteCnt = 4;
    	netParam.trPerGwCnt = 4;
		System.out.println("Net params:" + netParam);
		
    	RanCliParams cliParams = new RanCliParams(netParam);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.SMALL,
    			1,3), 5);

	    System.out.println("Total clients cnt: " 
	    		+ cliParams.getMaxClientCount()); 

	    RanNetwork net = new RanNetwork(netParam, cliParams);
	    net.reGenClients(RanNetwork.ClientDist.PUBLIC_EVEN);
    	net.connectAllClients(Network.PathFindType.DRY_RUN);
   	    System.out.println("ClassID: " + 0 + " clientsCnt: " 
    	    		+ net.totalCliCntPerClass.get(0));
   	    
   	    Map<String, Integer> clientsPerSource = new HashMap<>();
   	    for (ClientFlow client : net.clients) {
   	    	if (!clientsPerSource.containsKey(client.src)) {
   	    		clientsPerSource.put(client.src, 0);
   	    	}
   	    	clientsPerSource.put(client.src, 
   	    			clientsPerSource.get(client.src)+1);
   	    }
   	    
   	    System.out.println("CLients per node ");
   	    for (Map.Entry<String, Integer> entry : clientsPerSource.entrySet()) {
   	     System.out.println(entry.getKey() + ":" + entry.getValue());
   	    }
    }

    public static void testGraphGen() {
    	GsSupport gss = new GsSupport("Test Graph", "graph-cfg.css");
    	TopoGen.RanNetParams ranParam = new TopoGen.RanNetParams();
   	
    	ranParam.apCnt=1;
    	ranParam.apToTrLinkBw = 10;
		System.out.println("Micro ran:" + ranParam);
    	ranParam.apCnt=16;
		System.out.println("16AP:" + ranParam);
		
    	ranParam.apCnt=300;
		System.out.println("300AP:" + ranParam);

    	ranParam.apCnt=1000;
    	ranParam.apPerSiteCnt = 4;
    	ranParam.trPerGwCnt = 4;
		System.out.println("1000AP:" + ranParam);

    	ranParam.apCnt=1000;
    	ranParam.apPerSiteCnt = 4;
    	ranParam.trPerGwCnt = 8;
		System.out.println("1000AP:" + ranParam);
		System.out.println("-----------------");

		
    	ranParam.apCnt=16;
    	ranParam.apPerSiteCnt = 4;
    	ranParam.trPerGwCnt = 4;
		System.out.println("C grapth:" + ranParam);
		RanNetwork net1 = new RanNetwork(ranParam, null);
		System.out.println("Expected total BW:" + ranParam.getTotLinkBw());
		System.out.println("Actual BW:" + net1.topology.getTotalBw());
		System.out.println("Delta BW:" 
				+ (ranParam.getTotLinkBw() - net1.topology.getTotalBw()));
		
    	ranParam.apCnt=44;
    	ranParam.apPerSiteCnt = 4;
    	ranParam.trPerGwCnt = 8;
		System.out.println("C grapth:" + ranParam);
		RanNetwork net = new RanNetwork(ranParam, null);
		
		
		gss.addBwMatrix(net.spareBw);
		//System.out.println("Matrix:" + net.topology);
		gss.graph.display();
	}
    
    public static void testNetworkAllTraffic() {
    	TopoGen.RanNetParams netParam= new TopoGen.RanNetParams();

    	netParam.apCnt=100;
    	netParam.apPerSiteCnt = 4;
    	netParam.trPerGwCnt = 8;
    	netParam.apToTrLinkBw = 10;
    	netParam.ringToRadialBwRatio = 1;
    	
		System.out.println("C grapth:" + netParam);

    	RanCliParams cliParams = new RanCliParams(netParam);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.SMALL,
    			1,3), 15);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			1,3), 6);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.HIGH,
    			1,3), 3);

    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_AP,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.MEDIUM,
    			1,1), 5);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_AP,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			1,1), 2);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_AP,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.HIGH,
    			1,1), 1);

	    System.out.println("Total clients cnt: " 
	    		+ cliParams.getMaxClientCount()); 

		RanNetwork net = new RanNetwork(netParam, cliParams);

    	net.connectAllClients(Network.PathFindType.DIJEXTRA_BASE);
    	System.out.println("Base algoritm metrics :\n" 
    			+ net.simStatToStr());
    	net.reset();
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_SPAREBW );
    	System.out.println("Algoritm with BW consideration:\n" 
    			+ net.simStatToStr());
    	net.reset();
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_QOS_MP);
    	System.out.println("MP and QoS algorithm :\n" 
    			+ net.simStatToStr());

    }

    
    public static void testNetworkAllTrafficMobility() {
    	TopoGen.RanNetParams netParam= new TopoGen.RanNetParams();

    	netParam.apCnt=100;
    	netParam.apPerSiteCnt = 4;
    	netParam.trPerGwCnt = 8;
    	netParam.apToTrLinkBw = 10;
    	netParam.ringToRadialBwRatio = 1;
    	
		System.out.println("C grapth:" + netParam);

    	RanCliParams cliParams = new RanCliParams(netParam);
    	cliParams.setMaxClientCount(650);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.SMALL,
    			1,3), 15);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			1,3), 6);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.HIGH,
    			1,3), 3);

    	System.out.println("Total clients cnt: " 
	    		+ cliParams.getMaxClientCount()); 

		RanNetwork net = new RanNetwork(netParam, cliParams);
		net.reGenClients(RanNetwork.ClientDist.FLAT);

    	net.connectAllClients(Network.PathFindType.DIJEXTRA_BASE);
    	System.out.println("Base algoritm metrics :\n" 
    			+ net.simStatToStr());
    	net.reset();
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_SPAREBW );
    	System.out.println("Algoritm with BW consideration:\n" 
    			+ net.simStatToStr());
    	net.reset();
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_QOS_MP);
    	System.out.println("MP and QoS algorithm :\n" 
    			+ net.simStatToStr());
    }

    
    public static void testNetworkWanTraffic() {
    	TopoGen.RanNetParams netParam= new TopoGen.RanNetParams();

    	netParam.apCnt=150;
    	netParam.apPerSiteCnt = 4;
    	netParam.trPerGwCnt = 8;
    	netParam.apToTrLinkBw = 10;
    	netParam.ringToRadialBwRatio = 1;
    	
		System.out.println("C grapth:" + netParam);

    	RanCliParams cliParams = new RanCliParams(netParam);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.SMALL,
    			1,3), 15);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			1,3), 6);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.HIGH,
    			1,3), 3);

	    System.out.println("Total clients cnt: " 
	    		+ cliParams.getMaxClientCount()); 

		RanNetwork net = new RanNetwork(netParam, cliParams);

    	net.connectAllClients(Network.PathFindType.DIJEXTRA_BASE);
    	System.out.println("Base algoritm metrics :\n" 
    			+ net.simStatToStr());
    	net.reset();
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_SPAREBW );
    	System.out.println("Algoritm with BW consideration:\n" 
    			+ net.simStatToStr());
    	net.reset();
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_QOS_MP);
    	System.out.println("MP and QoS algorithm :\n" 
    			+ net.simStatToStr());

    }
    public static void testNetworkWanTraffic2() {
    	TopoGen.RanNetParams netParam= new TopoGen.RanNetParams();

    	netParam.apCnt=150;
    	netParam.apPerSiteCnt = 4;
    	netParam.trPerGwCnt = 8;
    	netParam.apToTrLinkBw = 10;
    	netParam.ringToRadialBwRatio = 1;
    	
		System.out.println("C grapth:" + netParam);

    	RanCliParams cliParams = new RanCliParams(netParam);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.NORMAL,
    			RanCliParams.BwReq.SMALL,
    			1,3), 15);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			1,3), 6);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.HIGH,
    			1,3), 3);

	    System.out.println("Total clients cnt: " 
	    		+ cliParams.getMaxClientCount()); 

		RanNetwork net = new RanNetwork(netParam, cliParams);

    
    	net.connectAllClients(Network.PathFindType.DIJEXTRA_QOS_MP);
    	System.out.println("MP and QoS algorithm :\n" 
    			+ net.simStatToStr());

    }


    public static void testAlgoritmBasicIdea() {
    	Network net = new Network();
    	NetBandwidth topo = new NetBandwidth();
    	topo.init(5);
    	topo.setBothLinks("A", "B", 10);
    	topo.setBothLinks("A", "C", 9);		
    	topo.setBothLinks("B", "D", 20);
    	topo.setBothLinks("D", "E", 40);
    	topo.setBothLinks("C", "E", 40);
    	net.init(topo);

    	GsSupport gss = new GsSupport("Test Graph", "graph-cfg.css");
		gss.addBwMatrix(net.topology);
   	
    	// Normal algorithm
		net.pushClient(new ClientFlow(
				"A", "E", 
				1, 1, 4, 0, 5, 1));
		net.connectAllClients(PathFindType.DIJEXTRA_BASE);
		gss.addClientFlows("base_", net.clients);
		net.removeClients();

    	// Algorithm based on spare bandwidth
		net.pushClient(new ClientFlow(
				"A", "E", 
				1, 1, 4, 0, 5, 1));
		net.connectAllClients(PathFindType.DIJEXTRA_SPAREBW);
		gss.addClientFlows("spare_", net.clients);
		net.removeClients();
		
		// Modified algorithm
		net.pushClient(new ClientFlow(
				"A", "E", 
				1, 1, 4, 0, 5, 0.99));
		net.connectAllClients(PathFindType.DIJEXTRA_QOS_MP);
		gss.addClientFlows("mod_Ar=0.99 ", net.clients);
		net.removeClients();
		net.pushClient(new ClientFlow(
				"A", "E", 
				1, 1, 4, 0, 5, 0.1));
		net.connectAllClients(PathFindType.DIJEXTRA_QOS_MP);
		gss.addClientFlows("mod_Ar=0.1 ", net.clients);
		net.removeClients();
		

		//gss.addClientFlows(net.clients);
		gss.graph.display();
    }
	
    
    public static void testSimplePathFind() {
    	TopoGen.RanNetParams netParam= new TopoGen.RanNetParams();

    	netParam.apCnt=8;
    	netParam.apPerSiteCnt = 4;
    	netParam.trPerGwCnt = 8;
    	netParam.apToTrLinkBw = 10;
    	
		System.out.println("C grapth:" + netParam);

		RanNetwork net = new RanNetwork(netParam, null);
		
		net.pushClient(new ClientFlow(
				"ap0_0", "wan", 
				3, 1, 5,0, netParam.worstApToApHopCnt(), 0.1));
		net.pushClient(new ClientFlow(
				"ap0_0", "wan", 
				8, 1, 5, 1, netParam.worstApToApHopCnt(), 0.1));

		net.pushClient(new ClientFlow(
				"ap0_0", "wan", 
				2, 1, 4, 0, netParam.worstApToApHopCnt(), 0.1));
		net.pushClient(new ClientFlow(
				"ap0_0", "wan", 
				2, 1, 2, 1, netParam.worstApToApHopCnt(), 0.1));

		net.pushClient(new ClientFlow(
				"ap0_1", "ap1_1", 
				2, 1, 2, 0, netParam.worstApToApHopCnt(), 0.1));

		net.pushClient(new ClientFlow(
				"ap1_2", "wan", 
				10, 10, 5, 0, netParam.worstApToApHopCnt(), 0.1));
		
		net.connectAllClients(Network.PathFindType.DIJEXTRA_BASE);

    	System.out.println("Connection stat:\n" 
    			+ net.simStatToStr());
    	
    	System.out.println("Topology:\n" 
    			+ net.topology);

    	GsSupport gss = new GsSupport("Test Graph", "graph-cfg.css");
		gss.addBwMatrix(net.spareBw);
		gss.addClientFlows(net.clients);
		gss.graph.display();
    }
    
    
    public static void testPathFindShowAllFlowsMpQosFlat() {
    	TopoGen.RanNetParams netParam= new TopoGen.RanNetParams();

    	netParam.apCnt=16;
    	netParam.apPerSiteCnt = 2;
    	netParam.trPerGwCnt = 2;
    	netParam.apToTrLinkBw = 10;
    	
		System.out.println("C grapth:" + netParam);

		RanCliParams cliParams = new RanCliParams(netParam);
		
		cliParams.setMaxClientCount(10);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.REAL_TIME,
    			RanCliParams.BwReq.SMALL,
    			9,9), 1);
    	cliParams.add(
    			cliParams.new ClientClass(
    			RanCliParams.FlowType.TO_WAN,
    			RanCliParams.LatencyReq.BEST_EFFORTS,
    			RanCliParams.BwReq.SMALL,
    			9,9), 1);
		System.out.println("Max clients count:" + cliParams.getMaxClientCount());

    	RanNetwork net = new RanNetwork(netParam, cliParams);
    	net.reGenClients(RanNetwork.ClientDist.PUBLIC_EVEN);
		net.connectAllClients(Network.PathFindType.DIJEXTRA_QOS_MP);
		
    	GsSupport gss = new GsSupport("Test Graph", "graph-cfg.css");
		gss.addBwMatrix(net.spareBw);
		gss.addClientFlowsStat(net.clients);
		gss.graph.display();
    }
}
