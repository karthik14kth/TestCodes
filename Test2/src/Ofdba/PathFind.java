package Ofdba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathFind {
	public interface PathCostFunc {
		double get(
				int fromInd, int toInd,
				double currentPathCost,
				int[][] bwMatrix,
				int hopCnt,
				ClientFlow client,
				boolean isUplink );
	}
	
	static public class PathCostBasic implements PathCostFunc {
		public double get(
				int fromInd, int toInd,
				double currentPathCost,
				int[][] bwMatrix,
				int hopCnt,
				ClientFlow client,
				boolean isUplink ) {

			if (bwMatrix[fromInd][toInd] == 0) {
				return Double.POSITIVE_INFINITY;
			}
			Double newPathCost = currentPathCost 
					+ 1.0/bwMatrix[fromInd][toInd];
			return newPathCost;
		}
	}

	static public class PathCostMpQos implements PathCostFunc {
		public double get(
				int fromInd, int toInd,
				double currentPathCost,
				int[][] bwMatrix,
				int hopCnt,
				ClientFlow client,
				boolean isUplink ) {

			if (bwMatrix[fromInd][toInd] < client.getBw(isUplink)
					&& hopCnt>client.maxHopCnt) {
				return Double.POSITIVE_INFINITY;
			}
			Double newPathCost = 1.0/bwMatrix[fromInd][toInd];
			if (newPathCost > currentPathCost) {
				newPathCost /= (Math.pow(client.getAr(), hopCnt) );
			} else {
				newPathCost = currentPathCost / client.getAr();
			}
			return newPathCost;
		}
	}
	static private int getMinPathcostInd(
			Map<Integer, Double> pathCost,
			Set<Integer> visitedNodes,
			Set<Integer> noTraverseInd) {
		int minInd = -1;
		Double minPc = Double.POSITIVE_INFINITY;
		for (Map.Entry<Integer, Double> indPathcost: pathCost.entrySet()) {
			if (!visitedNodes.contains(indPathcost.getKey())
					&& !noTraverseInd.contains(indPathcost.getKey())
					&& minPc > indPathcost.getValue()) {
				minPc = indPathcost.getValue();
				minInd = indPathcost.getKey();
			}
		}
		return minInd;
	}
	static public List<String> dijextraPathFind(
			PathCostFunc pcFunc, 
			NetBandwidth netBw,
			Set<String> noTraverseNodes,
			ClientFlow client,
			boolean isUplink ) {
		// Maps mxIndex fromNode to to Node to encode path
		Set<Integer> visitedNodes = new HashSet<>();
		Set<Integer> noTraverseInd= new HashSet<>();

		Map<Integer, Double> pathCosts = new HashMap<>();
		Map<Integer, Integer> hopCount= new HashMap<>();

		// Iversed path, to to from
		Map<Integer, Integer> path = new HashMap<>();
		
		String src = client.getSrc(isUplink);
		String dst = client.getDst(isUplink);
		int srcIndex = netBw.getIndex(src);
		int dstIndex = netBw.getIndex(dst);
		
		for (String nodeId : noTraverseNodes) {
			if (nodeId == src || nodeId == dst) {
				continue;
			}
			noTraverseInd.add(
					netBw.getIndex(nodeId));
		}
		
		for (int ind=0; ind<netBw.size; ++ind) {
			pathCosts.put(ind, Double.POSITIVE_INFINITY);
		}
		pathCosts.put(srcIndex, 0.0);
		hopCount.put(srcIndex, 0);
		for (int iter=0; iter<netBw.size-1; ++iter) {
			int minPcNodeInd  = getMinPathcostInd(
					pathCosts, 
					visitedNodes, 
					noTraverseInd);
			if (minPcNodeInd<0) {
				//No more node to try
				break;
			}
			visitedNodes.add(minPcNodeInd);
			for (int nextHopInd = 0; 
					nextHopInd < netBw.size; 
					++nextHopInd) {
				if (visitedNodes.contains(nextHopInd)) {
					continue;
				}
				double newPathCost = pcFunc.get(
						minPcNodeInd, nextHopInd,
						pathCosts.get(minPcNodeInd),
						netBw.mx, hopCount.get(minPcNodeInd),
						client, isUplink);
				if (newPathCost != Double.POSITIVE_INFINITY 
						&& newPathCost < pathCosts.get(nextHopInd)) {
					pathCosts.put(nextHopInd, newPathCost);
					hopCount.put(nextHopInd, hopCount.get(minPcNodeInd)+1);
					path.put(nextHopInd, minPcNodeInd);
				}
			}
		}
		List<String> result = new ArrayList<>();
		Integer prevKey = dstIndex;
		result.add(0, netBw.getName(prevKey));
		while(path.containsKey(prevKey)) {
			prevKey  = path.get(prevKey);
			if (prevKey  == null) {
				break;
			}
			result.add(0, netBw.getName(prevKey));
		}
		if (result.size()>0 && !result.get(0).contentEquals(src)) {
			result.clear();
		}
		return result;
	}

}
