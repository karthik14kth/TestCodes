package TopoGen;

public class RanNetParams {
	//AP: Access Point
	//TR: Transport Router
	//GW: Gateway
	//           {GW}    {GW}  : 2 GW per TR/ trPerGw TRs 
	//             | /  \ |
	//         - {TR} -  {TR} - :  2TR per site
	//        /   |       |   \
	//{ site }   {  site   }  { site }
	//          {Ap - Ap - Ap} : mesh, 

	public int apCnt= 0;
	public int apPerSiteCnt = 4;
	public int trPerGwCnt = 4; //alwas x2
	public int apToTrLinkBw = 0;
	public int ringToRadialBwRatio = 1;

	// DO NOT CHANGE
	private final int trPerSiteCnt = 2; 
	private final int gwPerTrCnt = 2; 
		
	public RanNetParams(){
	}
	
	//NOdes number
	public int getSitesCnt() {
		return (int) Math.ceil((double)apCnt / apPerSiteCnt);
	}
	public int getTrCnt() {
		int siteCnt = getSitesCnt();
		return siteCnt >= trPerSiteCnt ? siteCnt : trPerSiteCnt;
	}

	public int getGwCnt() {
		int gwCnt = (int) Math.ceil((double)getTrCnt() *gwPerTrCnt / trPerGwCnt);
		return  gwCnt >= gwPerTrCnt ? gwCnt : gwPerTrCnt;
	}
	
	public int getNodeCnt() {
		return apCnt+getTrCnt()+getGwCnt()+1/*wan*/;
	}

	//Lincs per node
	public int getApToTrMxLinksCnt() {
		return apPerSiteCnt;
	}
	public int getTrToApMxLinksCnt() {
		return apPerSiteCnt * trPerSiteCnt;
	}
	public int getTrToGwMxLinksCnt() {
		return gwPerTrCnt;
	}
	public int getGwToTrMxLinksCnt() {
		return trPerGwCnt;
	}

	//Bandwidth
	public int getTrToGwLinkBw() {
		return apToTrLinkBw * getTrToApMxLinksCnt()/gwPerTrCnt;
	}
	public int getTrToTrLinkBw() {
		return ringToRadialBwRatio * getTrToGwLinkBw() * gwPerTrCnt / 2;
	}
	public int getGwToWanLinkBw() {
		return getTrToGwLinkBw() * trPerGwCnt;
	}
	public int getGwToGwLinkBw() {
		return ringToRadialBwRatio * getGwToWanLinkBw() / 2;
	}

	public int getTotLinkBw() {
		//Bidirectional bandwidth on all access points
		int totApLinkBw = getMaxApBw();
				
		//Unidirection bandwidth on transport nodes
		int totBw = getTrCnt() * getTrToGwMxLinksCnt()*getTrToGwLinkBw();
		if (getTrCnt() > 2) {
			totBw += getTrCnt() * getTrToTrLinkBw();
		} else {
			totBw += (getTrCnt() - 1) * getTrToTrLinkBw();
		}
		
		//Links GW to to GW:
		if (getGwCnt() > 2) {
			totBw += getGwCnt() * getGwToGwLinkBw();
		} else {
			totBw += (getGwCnt() - 1) * getGwToGwLinkBw();
		}
		totBw += getGwCnt() * getGwToWanLinkBw();
		totBw = totBw * 2 + totApLinkBw;
		return totBw ;
	}
	
	public int getMaxApBw() {
		return apCnt * (apPerSiteCnt-1 + trPerSiteCnt*2) * apToTrLinkBw;
	}
	
	//Graph length
	public int expApToWanHopCnt() {
		return 3;
	}
	public int worstApToWanHopCnt() {
		return apPerSiteCnt + getTrCnt() + 1;
	}
	public int expApToApHopCnt() {
		return 4 + getGwCnt()/2;
	}
	public int worstApToApHopCnt() {
		return apPerSiteCnt + 2 + getTrCnt();
	}

	public String toString() {
		return String.format(
				"Nodes cnt: {ap: %d, site: %d, tr: %d, gw: %d, tot: %d}\n"
				+ "Links cnt: {ap-tr:%d, tr-ap:%d, tr-gw:%d, gw-tr:%d}\n"
				+ "Links BW: {ap-tr:%d, tr-tr:%d. tr-gw:%d, gw-gw:%d, gw-wan:%d}\n"
				+ "Hop Cnt: {exp AP-WAN:%d, worst AP-WAN:%d. exp AP-AP:%d, worst AP-AP:%d}",
				apCnt, getSitesCnt(), getTrCnt(), getGwCnt(), getNodeCnt(),
				trPerSiteCnt, getTrToApMxLinksCnt(), getTrToGwMxLinksCnt(), getGwToTrMxLinksCnt(),
				apToTrLinkBw, getTrToTrLinkBw(), getTrToGwLinkBw(), getGwToGwLinkBw(), getGwToWanLinkBw(),
				expApToWanHopCnt(), worstApToWanHopCnt(), expApToApHopCnt(), worstApToApHopCnt());
	}
}
