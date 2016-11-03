package Ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map;


import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import Ofdba.ClientFlow;
import Ofdba.NetBandwidth;
import TopoGen.RanNodes;

public class GsSupport {

	public Graph graph;
	
	private Map<String, Node> addedNodes = new HashMap<>();
	private Map<String, Edge> addedLinks = new HashMap<>();
	
	public GsSupport(String graphName, String cssFileName) {
		System.setProperty("org.graphstream.ui.renderer", 
						   "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graph =  new MultiGraph(graphName);
    	graph.addAttribute("ui.stylesheet", readFile(cssFileName));
	}
	// Links are always assumed symmetric. 
	public void addLink(String nodeA, String nodeB, int linkBw) {
		checkAddNode(nodeA);
		checkAddNode(nodeB);
		
		Boolean isDirectEdge = nodeA.compareTo(nodeB) < 0;
		String firstNode = isDirectEdge ? nodeA : nodeB;
		String secondNode = isDirectEdge ? nodeB : nodeA;
		String name = firstNode +"_" + secondNode;
		
		Edge edge = addedLinks.get(name);
		String strNameBw = firstNode + ":" + linkBw;
		String strBw = Integer.toString(linkBw);
		if (edge == null) {
			edge = graph.addEdge(name, firstNode , secondNode);
			RanNodes linkType = classifyLink(nodeA, nodeB);
			String linkCss = "link_"+linkType.cssClass;
			edge.addAttribute("ui.class", linkCss );
			edge.addAttribute("ui.label", isDirectEdge ? strNameBw : strBw);
			addedLinks.put(name, edge);
		} else {
			String clabel = edge.getAttribute("ui.label");
			edge.addAttribute("ui.label", isDirectEdge ? strNameBw + "/" + clabel 
					: clabel  + "/" + strBw);
		}
	}
	

	public void addClientFlow(String id, List<String> pathUl, 
			List<String> pathDl, String colorBw, String colorLine) {
	
		String nodeStyle =  "text-mode: normal; \n"
		    + "size: 10px, 10px;\n"
		    + "text-alignment: under;\n"
		    + "stroke-mode: plain;\n"
		    + "fill-color:" + colorBw + ";\n"
		    + "stroke-color:" + colorLine + ";\n"; 

		String edgeStyle = "text-mode: hidden;\n"
				+ "size: 2;\n"
				+ "fill-color:" + colorLine + ";\n"
			    + "stroke-color:" + colorLine + ";\n"; 

		
		String idSrc = id+"_src";
		Node nodeSrc = graph.addNode(idSrc);
		nodeSrc.addAttribute("ui.style", nodeStyle);
		nodeSrc.addAttribute("ui.label", idSrc);

		String idDst = id+"_dst";
		Node nodeDst = graph.addNode(idDst);
		nodeDst.addAttribute("ui.style", nodeStyle);
		nodeDst.addAttribute("ui.label", idDst);

		List<String> flowPath = new ArrayList<>();
		flowPath.add(idSrc);
		flowPath.addAll(pathUl);
		flowPath.add(idDst);
		flowPath.addAll(pathDl);
		flowPath.add(idSrc);
		
		for (int ind=1; ind<flowPath.size(); ++ind) {
			String prevNode = flowPath.get(ind-1);
			String cNode = flowPath.get(ind);
			String name = prevNode + "_" + cNode;
			Edge edge = graph.addEdge(id+name, prevNode, cNode, true);
			edge.addAttribute("ui.label", name);	
			edge.addAttribute("ui.style", edgeStyle);

		}
	}

	private static String[][] colorLoop= {
		{"#bf5b17", "#ffff99"},
		{"#f0027f", "#ffff99"},
		{"#386cb0", "#ffff99"},
		{"#fdc086", "#ffff99"},
		{"#beaed4", "#ffff99"},
		{"#7fc97f", "#ffff99"},
	};
	static int colorInd = 0;
	public void addClientFlows(String prefix, List<ClientFlow> clients) {
		for (int ind = 0; ind<clients.size(); ++ind) {
			ClientFlow client=clients.get(ind);
			if (client.isConnected) {
				colorInd += 1;
				colorInd %= colorLoop.length;
				String id = prefix+"c" + ind;
				addClientFlow(id, client.pathUl,
						client.pathDl,
						colorLoop[colorInd][1],
						colorLoop[colorInd][0]);
			}
		}
	}
	public void addClientFlows(List<ClientFlow> clients) {
		addClientFlows("",clients);
	}	
	String genFlowHash(List<String> pathUl, 
			List<String> pathDl) {
		String hash = "";
		for (String node : pathUl) {
			hash+=node;
		}
		for (String node : pathDl) {
			hash+=node;
		}
		return hash;
	}

	public void addClientFlowsStat(List<ClientFlow> clients) {
		int colorInd = 0;
		Map<String, Integer> cliStat = new HashMap<>();
		Map<String, ClientFlow> statPaths = new HashMap<>();

		for (int ind = 0; ind<clients.size(); ++ind) {
			ClientFlow client=clients.get(ind);
			if (client.isConnected) {
				String genPathHash = genFlowHash(
						client.pathUl, client.pathDl);
				if (!cliStat.containsKey(genPathHash )) {
					cliStat.put(genPathHash , 0);
					statPaths.put(genPathHash, client);
				}
				cliStat.put(genPathHash ,
						cliStat.get(genPathHash)+1);				
			}
		}
		
		int ind=0;
   	    for (Map.Entry<String, Integer> entry : cliStat.entrySet()) {
   	    	ind+=1;
			colorInd += 1;
			colorInd %= colorLoop.length;
			ClientFlow client=statPaths.get(entry.getKey());
			String id = "C" + ind + "_" + entry.getValue();
			addClientFlow(id, client.pathUl,
					client.pathDl,
					colorLoop[colorInd][1],
					colorLoop[colorInd][0]);

   	    }

	}
	
	
	public void addBwMatrix(NetBandwidth net) {
		for (int indI=0; indI<net.size; ++indI) {
			String nodeA = net.getName(indI);
			for (int indJ=0; indJ<net.size; ++indJ) {
				if (net.mx[indI][indJ]==0){
					continue;
				}
				String nodeB = net.getName(indJ);
				addLink(nodeA, nodeB, net.mx[indI][indJ]);
			}
		}
	}
	
	private void checkAddNode(String nodeId) {
		if (addedNodes.containsKey(nodeId)) {
			return;
		}
		Node node = graph.addNode(nodeId);
		node.addAttribute("ui.class", RanNodes.classifyNode(nodeId).cssClass);
		node.addAttribute("ui.label", nodeId);
		addedNodes.put(nodeId, node);
	}
	
	private RanNodes classifyLink(String nodeA, String nodeB) {
		RanNodes nodeTypeA = RanNodes.classifyNode(nodeA);
		RanNodes nodeTypeB = RanNodes.classifyNode(nodeB);
		if (RanNodes.GW.isOneMatches(nodeTypeA, nodeTypeB)
				&& !RanNodes.TR.isOneMatches(nodeTypeA, nodeTypeB)) {
			return RanNodes.GW;
		} else if (RanNodes.TR.isOneMatches(nodeTypeA, nodeTypeB)
				&& !RanNodes.AP.isOneMatches(nodeTypeA, nodeTypeB)) {
			return RanNodes.TR;
		} else {
			return RanNodes.AP;
		}
	}
	
	private String readFile(String fileName) {
		String content = "";
		try {
			content = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return content;
	}
	
}
