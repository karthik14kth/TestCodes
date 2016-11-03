package TopoGen;

public enum RanNodes {
	AP("ap", "access point"),
	TR("t", "transport router"),
	GW("gw", "gate way"),
	WAN("wan", "external network"),
	UE("ue", "user equipment"),
	NONE("_", "unknown");
	
	public final String name;
	public final String prefix;
	public final String cssClass;

	private RanNodes(String prefix, String name) {
		this.name = name;
		this.prefix = prefix;
		this.cssClass = prefix;
	}
	public static RanNodes classifyNode(String nodeId) {
		for (RanNodes nodeTp : RanNodes.values() ) {
			if (nodeId.startsWith(nodeTp.prefix)) {
				return nodeTp;
			}
		}
		return NONE;
	}
	
	public Boolean isOneMatches(RanNodes nodeA, RanNodes nodeB) {
		return this == nodeA || this == nodeB;
	}
	public Boolean isBothMache(RanNodes nodeA, RanNodes nodeB) {
		return this == nodeA && this == nodeB;
	}
}
