package Ofdba;

import java.util.List;

//Strucutre representing unidrection flow
public class ClientFlow {	
	final public String src;
	final public String dst;
	// SRC->DST
	final public int reqBandwidthUl;
	// SRC<-DST
	final public int reqBandwidthDl;
	final public int maxHopCnt;
	final public int wortsHopCnt;
	final public int clientClass;
	final public double routeAspectRatio;

	public boolean isConnected = false;
	public List<String> pathUl;
	public List<String> pathDl;
	
	public ClientFlow(String src, String dst, 
				  int reqBandwidthUl, int reqBandwidthDl,
				  int maxHopCnt, int clientClass,
				  int wortsHopCnt,
				  double routeAspectRatio) {
		this.src = src;
		this.dst = dst;
		this.reqBandwidthUl = reqBandwidthUl;
		this.reqBandwidthDl = reqBandwidthDl;
		this.maxHopCnt = maxHopCnt;
		this.wortsHopCnt = wortsHopCnt;
		this.clientClass = clientClass;
		this.routeAspectRatio = routeAspectRatio;
	}
	
	public double getAr() {
//		return (double)(this.maxHopCnt) / (this.wortsHopCnt + 1);
		return this.routeAspectRatio ;
	}
	public void dropConnection() {
		isConnected = false;
		pathUl = null;
		pathDl = null;
	}
	
	public String getSrc(boolean isUlPath) {
		return isUlPath ? this.src : this.dst;
	}
	public String getDst(boolean isUlPath) {
		return isUlPath ? this.dst : this.src;
	}
	public int getBw(boolean isUlPath) {
		return isUlPath ? this.reqBandwidthUl
				: this.reqBandwidthDl;
	}
	public void setPath(boolean isUlPath, List<String> path) {
		if (isUlPath ) {
			pathUl = path;
		} else {
			pathDl = path;
		}
	}
	
	
	public boolean isValidPath(List<String> path, boolean isUlPath) {
		String nodeSrc = getSrc(isUlPath);
		String nodeDst = getDst(isUlPath);
		if (path.size() > 1 
				&& path.get(0).equals(nodeSrc)
				&& path.get(path.size()-1).equals(nodeDst)
				&& path.size()-1 <= this.maxHopCnt) {
			return true;
		}
		return false;
	}
	
	public void setConnection() {
		isConnected = true;
	}
}
