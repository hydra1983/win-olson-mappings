package com.wds.tools.winolson;

import com.wds.tools.winolson.utils.Strings;
import com.wds.tools.winolson.zone.WindowsZone;
import com.wds.tools.winolson.zone.WinOlsonZones;

public class WinOlsonApplication {
	private static final String WINDOWS_ZONES_URL = "http://unicode.org/repos/cldr/trunk/common/supplemental/windowsZones.xml";
	private static final String META_ZONES_URL = "http://www.unicode.org/repos/cldr/trunk/common/supplemental/metaZones.xml";

	public static void main(String[] args) throws Exception {
		WinOlsonZones winOlsonZones = WinOlsonZones.parse(WINDOWS_ZONES_URL,
				META_ZONES_URL);
		long autoId = 0l;
		for (WindowsZone windowsZone : winOlsonZones.list()) {
			autoId++;
			System.out
					.println(Strings
							.substitute(
									"INSERT INTO `time_zone` (`id`, `zone_id`, `zone_name`) VALUES ('{0}','{1}', '{2}')",
									autoId, windowsZone.getDefaultOlsonZone()
											.getId(), windowsZone
											.getDisplayName()));
		}
	}
}
