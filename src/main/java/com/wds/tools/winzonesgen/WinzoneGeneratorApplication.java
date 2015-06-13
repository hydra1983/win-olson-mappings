package com.wds.tools.winzonesgen;

import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wds.tools.winzonesgen.utils.Strings;
import com.wds.tools.winzonesgen.zone.Zone;
import com.wds.tools.winzonesgen.zone.ZoneGroup;
import com.wds.tools.winzonesgen.zone.Zones;

public class WinzoneGeneratorApplication {
	private static final Logger LOG = LoggerFactory
			.getLogger(WinzoneGeneratorApplication.class);

	public static void main(String[] args) throws Exception {
		List<ZoneGroup> groups = Zones
				.parse("http://unicode.org/repos/cldr/trunk/common/supplemental/windowsZones.xml");
		long autoId = 0l;
		for (ZoneGroup group : groups) {
			autoId++;
			Zone firstZone = group.getFirstZone();

			if (firstZone == null) {
				if (LOG.isErrorEnabled()) {
					LOG.error("no first zone found");
				}
			} else {
				TimeZone timeZone = TimeZone.getTimeZone(firstZone.getType());
				if (timeZone == null) {
					if (LOG.isErrorEnabled()) {
						LOG.error(Strings.substitute(
								"Cannot get time zone for zone '{0}'",
								firstZone.getType()));
					}
				} else {
					System.out
							.println(Strings
									.substitute(
											"INSERT INTO `time_zone` (`id`, `zone_id`, `zone_name`, `zone_offset`) VALUES ('{0}','{1}', '{2}', {3})",
											autoId, timeZone.getID(),
											group.getName(), group.getOffset()));
				}
			}
		}
	}
}
