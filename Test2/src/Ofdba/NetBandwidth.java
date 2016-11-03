package Ofdba;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetBandwidth {
	public int mx[][];
	public int size = 0;

	public int maxSize = 0;
	private Map<Integer, String> _indToName = new HashMap<>();
	private Map<String, Integer> _nameToInd = new HashMap<>();
	
	public NetBandwidth() {
	}

	public NetBandwidth(NetBandwidth other) {
		init(other.maxSize);
		for (int indI=0; indI<this.maxSize; ++indI) {
			String nodeA = other._indToName.get(indI);
			for (int indJ=0; indJ<this.maxSize; ++indJ) {
				int bw = other.mx[indI][indJ];
				if (bw == 0) {
					continue;
				}
				String nodeB = other._indToName.get(indJ);
				setLink(nodeA, nodeB, bw);
			}
		}
	}
	
	public void init(int maxSize) {
		mx = new int[maxSize][maxSize];
		this.size = 0;
		this.maxSize=maxSize;
		_indToName.clear();
		_nameToInd.clear();
		for (int indI=0; indI<this.size; ++indI) {
			for (int indJ=0; indJ<this.size; ++indJ) {
				mx[indI][indJ] = 0;
			}
		}
	}
	public String getName(int ind) {
		String name = _indToName.get(ind);
		if (name == null) {
			throw new RuntimeException(
					"Element with index " + ind + " is not added");			
		} 
		return name;
	}
	public Integer getIndex(String name) {
		Integer ind = _nameToInd.get(name);
		if (ind == null) {
			throw new RuntimeException(
					"Element with name " + name+ " is not added");			
		} 
		return ind;
	}

	public void setLink(String nodeA, String nodeB, int bw) {
		Integer nodeAInd = _getIndex(nodeA);
		Integer nodeBInd = _getIndex(nodeB);
		mx[nodeAInd][nodeBInd] = bw; 
	}

	public void setBothLinks(String nodeA, String nodeB, int bw) {
		Integer nodeAInd = _getIndex(nodeA);
		Integer nodeBInd = _getIndex(nodeB);
		mx[nodeAInd][nodeBInd] = bw; 
		mx[nodeBInd][nodeAInd] = bw; 
	}

	//
	private void updateBwMap(
			Map<String, Integer> bwMap,
			List<String> path, int bw) {
		for (int ind = 1; ind<path.size(); ++ind) {
			String linkId = path.get(ind-1) + "-" + path.get(ind);
			int newBw = bw;
			if (!bwMap.containsKey(linkId)) {
				int fromInd = getIndex(path.get(ind-1));
				int toInd = getIndex(path.get(ind));
				bwMap.put(linkId, mx[fromInd][toInd]);
			}
			bwMap.put(linkId, bwMap.get(linkId)-newBw);
		}
	}
	
	public boolean isEnoughBw(
			List<String> path, int bw) {
		if (path.size() <= 1) {
			return false;
		}
		Map<String, Integer> newBw = new HashMap<>();
		updateBwMap(newBw, path, bw);
//		updateBwMap(newBw, pathDl, bwDl);
		for (Integer bwPerLink: newBw.values() ) {
			if (bwPerLink < 0) {
				return false;
			}
		}
		return true;
	}
	
	public void setPath(List<String> path, int bw) {
		for (int ind = 1; ind<path.size(); ++ind) {
			int indFrom = getIndex(path.get(ind-1));
			int indTo = getIndex(path.get(ind));
			mx[indFrom ][indTo] -= bw;
		}
	}
	public void unsetPath(List<String> path, int bw) {
		for (int ind = 1; ind<path.size(); ++ind) {
			int indFrom = getIndex(path.get(ind-1));
			int indTo = getIndex(path.get(ind));
			mx[indFrom ][indTo] += bw;
		}
	}
	
	private Integer _getIndex(String node) {
		Integer nodeInd = _nameToInd.get(node);
		if (nodeInd == null) {
			nodeInd = size;
			_nameToInd.put(node, nodeInd);
			_indToName.put(nodeInd, node);
			size+=1;
			if (size > maxSize) {
				throw new RuntimeException(
						"Failed to add link into matrix");
			}
		}
		return nodeInd; 
	}
	
	public String toString() {
		String result="-";
		for (int indJ = 0; indJ<size; indJ+=1) {
			result += _indToName.get(indJ) + "\t";
		}
		result+="\n";
		for (int indI = 0; indI<size; indI+=1) {
			result += _indToName.get(indI) + "\t";
			for (int indJ = 0; indJ<size; indJ+=1) {
				result += mx[indI][indJ] + "\t";
			}
			result+="\n";
		}
		return result;
	}
	
	public long getTotalBw() {
		long totBw = 0;
		for (int indI=0; indI<this.size; ++indI) {
			for (int indJ=0; indJ<this.size; ++indJ) {
				totBw += mx[indI][indJ];
			}
		}
		return totBw;
	}

}
