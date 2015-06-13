package com.wds.tools.winzonesgen.zone;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.wds.tools.winzonesgen.utils.Strings;

public class ZoneGroup {
	private static final Logger LOG = LoggerFactory.getLogger(ZoneGroup.class);

	public static ZoneGroup create(String groupInfo) {
		return new ZoneGroup(groupInfo);
	}

	private ZoneGroup(String groupInfo) {
		this.offset = parseOffset(groupInfo);
		this.name = parseName(groupInfo);
	}

	private final Long offset;

	public Long getOffset() {
		return offset;
	}

	private final String name;

	public String getName() {
		return name;
	}

	private Zone firstZone;

	public Zone getFirstZone() {
		return firstZone;
	}

	private final List<Zone> zones = Lists.newArrayList();

	public List<Zone> getZones() {
		return zones;
	}

	public void add(Zone zone) {
		zones.add(zone);
		if (zone.getTerritory().equals(Zones.FIRST_ZONE)) {
			if (this.firstZone == null) {
				this.firstZone = zone;
			} else {
				if (LOG.isErrorEnabled()) {
					LOG.error(Strings
							.substitute(
									"Group '{0}' can only have one first zone. The adding one is '{1}'",
									getName(), zone.getType()));
				}
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
