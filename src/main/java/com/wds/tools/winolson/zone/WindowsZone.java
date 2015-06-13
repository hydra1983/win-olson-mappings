package com.wds.tools.winolson.zone;

import static com.wds.tools.winolson.utils.Consts.DEFAULT_TERRITORY;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.wds.tools.winolson.utils.Strings;

public class WindowsZone {
	public static WindowsZone create(String windowsZoneInfo) {
		return new WindowsZone(windowsZoneInfo);
	}

	private WindowsZone(String windowsZoneInfo) {
		this.offset = parseOffset(windowsZoneInfo);
		this.name = parseName(windowsZoneInfo);
	}

	private final Long offset;

	public Long getOffset() {
		return offset;
	}

	private final String name;

	public String getDisplayName() {
		return name;
	}

	private OlsonZone defaultOlsonZone;

	public OlsonZone getDefaultOlsonZone() {
		return defaultOlsonZone;
	}

	private final List<OlsonZone> olsonZones = Lists.newArrayList();

	public List<OlsonZone> getOlsonZones() {
		return olsonZones;
	}

	public String getId() {
		return getDefaultOlsonZone().getWindowsZoneId();
	}

	public void addOlsonZone(OlsonZone olsonZone) {
		olsonZones.add(olsonZone);
		if (olsonZone.getLocation().equals(DEFAULT_TERRITORY)) {
			if (this.defaultOlsonZone == null) {
				this.defaultOlsonZone = olsonZone;
			} else {
				throw new RuntimeException(
						Strings.substitute(
								"Windows zone '{0}' can only have one default olson zone. The adding one is '{1}'",
								getDisplayName(), olsonZone.getId()));
			}
		}
	}

	private String parseName(String groupInfo) {
		String name = null;
		Pattern p = Pattern.compile("\\(UTC[^\\)]*\\)(.*)");
		Matcher m = p.matcher(groupInfo);
		if (m.find()) {
			name = m.group(1);
		}

		if (name != null) {
			name = name.trim();
		}

		return name;
	}

	private Long parseOffset(String groupInfo) {
		Long offset = 0L;
		Pattern p = Pattern.compile("\\(UTC([^\\)]*)\\).*");
		Matcher m = p.matcher(groupInfo);
		if (m.find()) {
			String offsetValue = m.group(1);
			if (offsetValue != null) {
				offsetValue = offsetValue.trim();
			}

			if (!offsetValue.isEmpty()) {
				String sign = offsetValue.substring(0, 1);
				offsetValue = offsetValue.substring(1);
				String[] values = offsetValue.split(":");
				String hrs = values[0];
				String mins = values[1];
				Long hrsL = Long.parseLong(hrs);
				Long minsL = Long.parseLong(mins);
				offset += hrsL * 3600 * 1000;
				offset += minsL * 60 * 1000;

				if (sign.equals("-")) {
					offset *= -1;
				}
			}
		}
		return offset;
	}
}
