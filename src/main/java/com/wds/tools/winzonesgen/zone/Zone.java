package com.wds.tools.winzonesgen.zone;

public class Zone {

	public static Zone create(String type, String territory, String other) {
		return new Zone(type, territory, other);
	}

	private Zone(String type, String territory, String other) {
		this.type = type;
		this.territory = territory;
		this.other = other;
	}

	private String type;

	public String getType() {
		return type;
	}

	private String territory;

	public String getTerritory() {
		return territory;
	}

	private String other;

	public String getOther() {
		return other;
	}
}
