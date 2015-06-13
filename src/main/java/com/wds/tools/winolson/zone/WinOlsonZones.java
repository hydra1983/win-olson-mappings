package com.wds.tools.winolson.zone;

import static com.wds.tools.winolson.utils.Consts.ATTR_OTHER;
import static com.wds.tools.winolson.utils.Consts.ATTR_TERRITORY;
import static com.wds.tools.winolson.utils.Consts.ATTR_TYPE;
import static com.wds.tools.winolson.utils.Consts.ELEM_MAP_TIMEZONES;
import static com.wds.tools.winolson.utils.Consts.ELEM_WINDOWS_ZONES;
import static com.wds.tools.winolson.utils.Consts.UNMAPPABLE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wds.tools.winolson.utils.Strings;
import com.wds.tools.winolson.utils.Xmls;

public class WinOlsonZones {

	public static WinOlsonZones parse(File windowsZonesFile, File metaZonesFile)
			throws SAXException, IOException, ParserConfigurationException {
		Assert.notNull(windowsZonesFile, "Require windowsZonesFile");
		Assert.notNull(metaZonesFile, "Require metaZonesFile");

		List<WindowsZone> windowsZones = doParse(
				createDocBuilder().parse(windowsZonesFile), createDocBuilder()
						.parse(metaZonesFile));

		return new WinOlsonZones(windowsZones);
	}

	public static WinOlsonZones parse(String windowsZonesUrl, String metaZonesUrl)
			throws SAXException, IOException, ParserConfigurationException {
		Assert.hasLength(windowsZonesUrl, "Require windowsZonesUrl");
		Assert.hasLength(metaZonesUrl, "Require metaZonesUrl");

		List<WindowsZone> windowsZones = doParse(
				createDocBuilder().parse(windowsZonesUrl), createDocBuilder()
						.parse(metaZonesUrl));

		return new WinOlsonZones(windowsZones);
	}

	private static DocumentBuilder createDocBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		docBuilderFactory.setIgnoringComments(false);
		return docBuilderFactory.newDocumentBuilder();
	}

	private static List<WindowsZone> doParse(Document windowsZonesDoc,
			Document metaZonesDoc) {
		List<WindowsZone> windowsZones = Lists.newArrayList();

		Element root = windowsZonesDoc.getDocumentElement();
		Node windowsZonesNode = Xmls.findChildNode(root, ELEM_WINDOWS_ZONES);
		NodeList mapZoneNodes = Xmls.findChildNodeChildren(windowsZonesNode,
				ELEM_MAP_TIMEZONES);

		Map<String, List<Node>> mapZoneNodeListMap = Maps.newHashMap();
		List<String> mapZoneNodeKeyList = Lists.newArrayList();
		List<Node> currentMapZoneNodeList = null;
		List<String> unmappableNodeList = Lists.newArrayList();

		MetaZones metaZones = MetaZones.parse(metaZonesDoc);

		for (int mapZoneNodeIndex = 0; mapZoneNodeIndex < mapZoneNodes
				.getLength(); mapZoneNodeIndex++) {
			Node mapZoneNode = mapZoneNodes.item(mapZoneNodeIndex);
			if (mapZoneNode.getNodeType() == Element.COMMENT_NODE) {
				String value = mapZoneNode.getNodeValue();
				if (!value.trim().equals(UNMAPPABLE)) {
					if (!mapZoneNodeListMap.containsKey(value)) {
						mapZoneNodeListMap.put(value, Lists.newArrayList());
						mapZoneNodeKeyList.add(value);
					}

					currentMapZoneNodeList = mapZoneNodeListMap.get(value);
				} else if (mapZoneNodeKeyList.size() > 1) {
					unmappableNodeList.add(mapZoneNodeKeyList
							.get(mapZoneNodeKeyList.size() - 1));
				}
			} else if (currentMapZoneNodeList != null) {
				currentMapZoneNodeList.add(mapZoneNode);
			}
		}

		for (String key : unmappableNodeList) {
			mapZoneNodeKeyList.remove(key);
			mapZoneNodeListMap.remove(key);
		}

		for (String key : mapZoneNodeKeyList) {
			WindowsZone windowsZone = WindowsZone.create(key);
			windowsZones.add(windowsZone);

			List<Node> mapZoneNodeList = mapZoneNodeListMap.get(key);
			for (Node mapZoneNode : mapZoneNodeList) {
				NamedNodeMap mapZoneNodeAttrs = mapZoneNode.getAttributes();
				if (mapZoneNodeAttrs != null) {
					String value = null;
					String other = null;
					String territory = null;
					String[] types = null;

					for (int mapZoneNodeAttrIndex = 0; mapZoneNodeAttrIndex < mapZoneNodeAttrs
							.getLength(); mapZoneNodeAttrIndex++) {
						Node mapZoneNodeAttr = mapZoneNodeAttrs
								.item(mapZoneNodeAttrIndex);
						if (mapZoneNodeAttr.getNodeName().equals(ATTR_TYPE)) {
							value = mapZoneNodeAttr.getNodeValue();
						} else if (mapZoneNodeAttr.getNodeName().equals(
								ATTR_OTHER)) {
							other = mapZoneNodeAttr.getNodeValue();
						} else if (mapZoneNodeAttr.getNodeName().equals(
								ATTR_TERRITORY)) {
							territory = mapZoneNodeAttr.getNodeValue();
						}

						if (value != null && !value.isEmpty()) {
							types = value.split(" ");
						}
					}

					if (types != null && types.length > 0) {
						for (String type : types) {
							if (!type.isEmpty()) {
								windowsZone.addOlsonZone(OlsonZone.create(
										windowsZone,
										metaZones.getTimeZone(type), type,
										territory, other));
							}
						}
					} else {
						throw new RuntimeException(
								Strings.substitute("No type found for {0}",
										mapZoneNode.toString()));
					}
				}
			}
		}

		return windowsZones;
	}

	public WinOlsonZones(List<WindowsZone> windowsZones) {
		this.windowsZones = windowsZones;
	}

	private final List<WindowsZone> windowsZones;

	public List<WindowsZone> list() {
		return windowsZones;
	}

}
