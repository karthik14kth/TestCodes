package TopoGen;

public class RanParams {
	//AP: Access Point
	//TR: Transport Router
	//GW: Gateway
	
	private int totalApCount = 0;
	private int apPerSite = 4;
	private int trPerGw = 4;
	private int sitesCnt = 0;
	private int trCnt = 0;


	
	public RanParams(int totalApCount){
		this.totalApCount = totalApCount;
		update();
	}
	
	private void update() {
		
	}

}
