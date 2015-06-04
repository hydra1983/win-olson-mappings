package com.wds.tools.winzonesgen;

import java.io.File;
import java.util.List;

import com.wds.tools.winzonesgen.utils.Strings;
import com.wds.tools.winzonesgen.zone.ZoneGroup;
import com.wds.tools.winzonesgen.zone.Zones;

public class WinzoneGeneratorApplication {
	public static void main(String[] args) throws Exception {
		List<ZoneGroup> groups = Zones.parse(new File("windowsZones.xml"));
		long autoId = 0l;
		for (ZoneGroup group : groups) {
			autoId++;
			System.out
					.println(Strings
							.substitute(
									"INSERT INTO `time_zone` (`id`, `zone_id`, `zone_offset`) VALUES ('{0}','{1}', {2})",
									autoId, group.getName(), group.getOffset()));
		}
	}
}
