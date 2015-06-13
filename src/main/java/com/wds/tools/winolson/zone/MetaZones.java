package com.wds.tools.winolson.zone;

import static com.wds.tools.winolson.utils.Consts.ATTR_FROM;
import static com.wds.tools.winolson.utils.Consts.ATTR_MZONE;
import static com.wds.tools.winolson.utils.Consts.ATTR_OTHER;
import static com.wds.tools.winolson.utils.Consts.ATTR_TERRITORY;
import static com.wds.tools.winolson.utils.Consts.ATTR_TO;
import static com.wds.tools.winolson.utils.Consts.ATTR_TYPE;
import static com.wds.tools.winolson.utils.Consts.DEFAULT_TERRITORY;
import static com.wds.tools.winolson.utils.Consts.ELEM_MAP_TIMEZONES;
import static com.wds.tools.winolson.utils.Consts.ELEM_MATAZONE_INFO;
import static com.wds.tools.winolson.utils.Consts.ELEM_META_ZONES;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wds.tools.winolson.utils.Strings;
import com.wds.tools.winolson.utils.Xmls;

public class MetaZones {
	private static final Logger LOG = LoggerFactory
			.getLogger(WinOlsonZones.class);

	private static final Set<String> convertableJavaZoneIds = Sets
			.newHashSet(TimeZone.getAvailableIDs());

	public static MetaZones parse(Document metaZonesDoc) {
		Element root = metaZonesDoc.getDocumentElement();
		Node metaZonesNode = Xmls.findChildNode(root, ELEM_META_ZONES);
		NodeList timezoneNodes = Xmls.findChildNodeChildren(metaZonesNode,
				ELEM_MATAZONE_INFO);
		NodeList mapZoneNodes = Xmls.findChildNodeChildren(metaZonesNode,
				ELEM_MAP_TIMEZONES);
		return new MetaZones(createTimezones(timezoneNodes),
				createMapZones(mapZoneNodes));
	}

	private static List<MapZoneGroup> createMapZones(NodeList mapZoneNodes) {
		List<MapZoneGroup> mapZoneGroups = Lists.newArrayList();

		Map<String, List<Node>> mapZoneNodeListMap = Maps.newHashMap();
		List<String> mapZoneNodeKeyList = Lists.newArrayList();

		for (int mapZoneNodeIndex = 0; mapZoneNodeIndex < mapZoneNodes
				.getLength(); mapZoneNodeIndex++) {
			Node mapZoneNode = mapZoneNodes.item(mapZoneNodeIndex);
			if (mapZoneNode.getNodeType() == Element.ELEMENT_NODE) {
				NamedNodeMap mapZoneNodeAttrs = mapZoneNode.getAttributes();
				if (mapZoneNodeAttrs != null
						&& mapZoneNodeAttrs.getLength() > 0) {
					String other = mapZoneNodeAttrs.getNamedItem(ATTR_OTHER)
							.getNodeValue();
					List<Node> currentMapZoneNodeList = null;
					if (mapZoneNodeListMap.containsKey(other)) {
						currentMapZoneNodeList = mapZoneNodeListMap.get(other);
					} else {
						currentMapZoneNodeList = Lists.newArrayList();
						mapZoneNodeListMap.put(other, currentMapZoneNodeList);
						mapZoneNodeKeyList.add(other);
					}
					currentMapZoneNodeList.add(mapZoneNode);
				}
			}
		}

		for (String mapZoneNodeKey : mapZoneNodeKeyList) {
			MapZoneGroup mapZoneGroup = new MapZoneGroup(mapZoneNodeKey);
			mapZoneGroups.add(mapZoneGroup);

			List<Node> mapZoneNodeList = mapZoneNodeListMap.get(mapZoneNodeKey);
			for (Node mapZoneNode : mapZoneNodeList) {
				NamedNodeMap mapZoneNodeAttrs = mapZoneNode.getAttributes();
				if (mapZoneNodeAttrs != null) {
					String type = null;
					String other = null;
					String territory = null;

					for (int mapZoneNodeAttrIndex = 0; mapZoneNodeAttrIndex < mapZoneNodeAttrs
							.getLength(); mapZoneNodeAttrIndex++) {
						Node mapZoneNodeAttr = mapZoneNodeAttrs
								.item(mapZoneNodeAttrIndex);
						if (mapZoneNodeAttr.getNodeName().equals(ATTR_TYPE)) {
							type = mapZoneNodeAttr.getNodeValue();
						} else if (mapZoneNodeAttr.getNodeName().equals(
								ATTR_OTHER)) {
							other = mapZoneNodeAttr.getNodeValue();
						} else if (mapZoneNodeAttr.getNodeName().equals(
								ATTR_TERRITORY)) {
							territory = mapZoneNodeAttr.getNodeValue();
						}
					}

					mapZoneGroup
							.addMapZone(new MapZone(other, territory, type));
				}
			}
		}

		return mapZoneGroups;
	}

	private static List<Timezone> createTimezones(NodeList timezoneNodes) {
		List<Timezone> timezones = Lists.newArrayList();
		for (int timeZoneIndex = 0; timeZoneIndex < timezoneNodes.getLength(); timeZoneIndex++) {
			Node timezoneNode = timezoneNodes.item(timeZoneIndex);
			if (timezoneNode.getNodeType() == Element.ELEMENT_NODE) {
				NamedNodeMap timezoneNodeAttrs = timezoneNode.getAttributes();
				if (timezoneNodeAttrs != null
						&& timezoneNodeAttrs.getLength() > 0) {
					String type = null;

					for (int timezoneNodeAttrIndex = 0; timezoneNodeAttrIndex < timezoneNodeAttrs
							.getLength(); timezoneNodeAttrIndex++) {
						Node timezoneNodeAttr = timezoneNodeAttrs
								.item(timezoneNodeAttrIndex);
						if (timezoneNodeAttr.getNodeName().equals(ATTR_TYPE)) {
							type = timezoneNodeAttr.getNodeValue();
						}
					}

					List<UsesMetazone> usesMetazones = parseUsesMetazones(timezoneNode);
					timezones.add(new Timezone(type, usesMetazones));
				}
			}
		}
		return timezones;
	}

	private static List<UsesMetazone> parseUsesMetazones(Node timezoneNode) {
		List<UsesMetazone> usesMetazones = Lists.newArrayList();

		NodeList usesMetazoneNodes = timezoneNode.getChildNodes();
		if (usesMetazoneNodes != null && usesMetazoneNodes.getLength() > 0) {
			for (int usesMetazoneIndex = 0; usesMetazoneIndex < usesMetazoneNodes
					.getLength(); usesMetazoneIndex++) {
				Node useMetazone = usesMetazoneNodes.item(usesMetazoneIndex);
				if (useMetazone.getNodeType() == Element.ELEMENT_NODE) {
					NamedNodeMap useMetazoneNodeAttrs = useMetazone
							.getAttributes();

					if (useMetazoneNodeAttrs != null
							&& useMetazoneNodeAttrs.getLength() > 0) {
						String from = null;
						String to = null;
						String mzone = null;

						for (int useMetazoneNodeAttrIndex = 0; useMetazoneNodeAttrIndex < useMetazoneNodeAttrs
								.getLength(); useMetazoneNodeAttrIndex++) {
							Node useMetazoneNodeAttr = useMetazoneNodeAttrs
									.item(useMetazoneNodeAttrIndex);
							if (useMetazoneNodeAttr.getNodeName().equals(
									ATTR_FROM)) {
								from = useMetazoneNodeAttr.getNodeValue();
							} else if (useMetazoneNodeAttr.getNodeName()
									.equals(ATTR_TO)) {
								to = useMetazoneNodeAttr.getNodeValue();
							} else if (useMetazoneNodeAttr.getNodeName()
									.equals(ATTR_MZONE)) {
								mzone = useMetazoneNodeAttr.getNodeValue();
							}
						}

						usesMetazones.add(new UsesMetazone(from, to, mzone));
					}
				}
			}
		}
		return usesMetazones;
	}

	public MetaZones(List<Timezone> timezones, List<MapZoneGroup> mapZoneGroups) {
		this.timezoneMap = createTimezoneMap(timezones);
		this.mapZoneGroupMap = createMapZoneGroupMap(mapZoneGroups);
	}

	private Map<String, Timezone> createTimezoneMap(List<Timezone> timezones) {
		Map<String, Timezone> timezoneMap = Maps.newHashMap();
		for (Timezone timezone : timezones) {
			timezoneMap.put(timezone.getType(), timezone);
		}
		return timezoneMap;
	}

	private Map<String, MapZoneGroup> createMapZoneGroupMap(
			List<MapZoneGroup> mapZoneGroups) {
		Map<String, MapZoneGroup> mapZoneGroupMap = Maps.newHashMap();
		for (MapZoneGroup mapZoneGroup : mapZoneGroups) {
			mapZoneGroupMap.put(mapZoneGroup.getName(), mapZoneGroup);
		}
		return mapZoneGroupMap;
	}

	private final Map<String, Timezone> timezoneMap;
	private final Map<String, MapZoneGroup> mapZoneGroupMap;

	public TimeZone getTimeZone(String olsonZoneId) {
		if (convertableJavaZoneIds.contains(olsonZoneId)) {
			return TimeZone.getTimeZone(olsonZoneId);
		} else {
			String newOlsonZoneId = findHistoricalOlsonZoneId(olsonZoneId);
			if (newOlsonZoneId != null
					&& convertableJavaZoneIds.contains(newOlsonZoneId)) {
				return TimeZone.getTimeZone(newOlsonZoneId);
			} else {
				if (LOG.isErrorEnabled()) {
					LOG.error(Strings.substitute(
							"Cannot find time zone for olson zone with id {0}",
							olsonZoneId));
				}
			}

			return null;
		}
	}

	private String findHistoricalOlsonZoneId(String olsonZoneId) {
		Timezone timezone = timezoneMap.get(olsonZoneId);

		if (timezone != null) {
			UsesMetazone lastUsesMetazone = timezone.getLastUsesMetazone();
			String mzone = lastUsesMetazone.getMzone();
			MapZoneGroup mapZoneGroup = mapZoneGroupMap.get(mzone);
			MapZone mapZone = mapZoneGroup.getDefaultMapZone();
			return mapZone.getId();
		} else {
			throw new RuntimeException(
					Strings.substitute(
							"Cannot find historical olson zone id for olson zone with id {0}",
							olsonZoneId));
		}
	}

	private static class Timezone {
		public Timezone(String type, List<UsesMetazone> usesMetazones) {
			this.type = type;
			this.usesMetazones = usesMetazones;
		}

		private final List<UsesMetazone> usesMetazones;

		public UsesMetazone getLastUsesMetazone() {
			return usesMetazones.get(usesMetazones.size() - 1);
		}

		private final String type;

		public String getType() {
			return type;
		}
	}

	private static class UsesMetazone {
		public UsesMetazone(String from, String to, String mzone) {
			this.mzone = mzone;
		}

		private final String mzone;

		public String getMzone() {
			return mzone;
		}
	}

	private static class MapZoneGroup {
		public MapZoneGroup(String name) {
			this.name = name;
		}

		private final String name;

		public String getName() {
			return name;
		}

		private MapZone defaultMapZone;

		public MapZone getDefaultMapZone() {
			return defaultMapZone;
		}

		private final List<MapZone> mapZones = Lists.newArrayList();

		public void addMapZone(MapZone mapZone) {
			mapZones.add(mapZone);
			if (mapZone.getTerritory().equals(DEFAULT_TERRITORY)) {
				if (this.defaultMapZone == null) {
					this.defaultMapZone = mapZone;
				} else {
					throw new RuntimeException(
							Strings.substitute(
									"MapZoneGroup '{0}' can only have one default map zone. The adding one is '{1}'",
									getName(), mapZone.getId()));
				}
			}
		}
	}

	private static class MapZone {
		public MapZone(String other, String territory, String type) {
			this.territory = territory;
			this.type = type;
		}

		private final String territory;

		public String getTerritory() {
			return territory;
		}

		private final String type;

		public String getId() {
			return type;
		}
	}
}
