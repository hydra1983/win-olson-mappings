package com.wds.tools.winolson.zone;

import java.util.TimeZone;

public class OlsonZone {
	public static OlsonZone create(WindowsZone windowsZone, TimeZone timezone,
			String type, String territory, String other) {
		return new OlsonZone(windowsZone, timezone, type, territory, other);
	}

	private OlsonZone(WindowsZone windowsZone, TimeZone timezone, String type,
			String territory, String other) {
		this.windowsZone = windowsZone;
		this.timezone = timezone;
		this.type = type;
		this.territory = territory;
		this.other = other;
	}

	private final WindowsZone windowsZone;

	public WindowsZone getWindowsZone() {
		return windowsZone;
	}

	private final String type;

	public String getId() {
		return type;
	}

	private final String other;

	String getWindowsZoneId() {
		return other;
	}

	private final String territory;

	public String getLocation() {
		return territory;
	}

	private final TimeZone timezone;

	public TimeZone getTimezone() {
		return timezone;
	}
}
