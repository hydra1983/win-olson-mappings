package com.wds.tools.winzonesgen.zone;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Zones {
	private static final String ELEM_WINDOWS_ZONES = "windowsZones";
	private static final String ELEM_MAP_TIME_ZONES = "mapTimezones";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_OTHER = "other";
	private static final String ATTR_TERRITORY = "territory";
	private static final String UNMAPPABLE = "Unmappable";

	public static List<ZoneGroup> parse(File file) throws SAXException,
			IOException, ParserConfigurationException {
		List<ZoneGroup> zoneGroups = Lists.newArrayList();

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		docBuilderFactory.setIgnoringComments(false);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(file);
		Element root = doc.getDocumentElement();
		NodeList rootNodes = root.getChildNodes();
		NodeList windowsZonesNodes = findChildNodes(rootNodes,
				ELEM_WINDOWS_ZONES);
		NodeList mapTimezonesNodes = findChildNodes(windowsZonesNodes,
				ELEM_MAP_TIME_ZONES);

		if (mapTimezonesNodes != null) {
			Map<String, List<Node>> nodeListMap = Maps.newHashMap();
			List<String> nodeKeyList = Lists.newArrayList();
			List<Node> currentNodeList = null;
			for (int i = 0; i < mapTimezonesNodes.getLength(); i++) {
				Node node = mapTimezonesNodes.item(i);
				if (node.getNodeType() == Element.COMMENT_NODE) {
					String value = node.getNodeValue();
					if (!value.trim().equals(UNMAPPABLE)) {
						if (!nodeListMap.containsKey(value)) {
							nodeListMap.put(value, Lists.newArrayList());
							nodeKeyList.add(value);
						}

						currentNodeList = nodeListMap.get(value);
					}

				} else if (currentNodeList != null) {
					currentNodeList.add(node);
				}
			}

			for (String key : nodeKeyList) {
				ZoneGroup group = ZoneGroup.create(key);
				zoneGroups.add(group);

				List<Node> nodeList = nodeListMap.get(key);
				for (Node node : nodeList) {
					NamedNodeMap attrs = node.getAttributes();
					if (attrs != null) {
						for (int i = 0; i < attrs.getLength(); i++) {
							Node attr = attrs.item(i);
							String value = null;
							String other = null;
							String territory = null;
							if (attr.getNodeName().equals(ATTR_TYPE)) {
								value = attr.getNodeValue();
							} else if (attr.getNodeName().equals(ATTR_OTHER)) {
								other = attr.getNodeValue();
							} else if (attr.getNodeName()
									.equals(ATTR_TERRITORY)) {
								territory = attr.getNodeValue();
							}

							if (value != null && !value.isEmpty()) {
								String[] types = value.split(" ");
								for (String type : types) {
									if (!type.isEmpty()) {
										group.add(Zone.create(type, territory,
												other));
									}
								}
							}
						}
					}
				}
			}
		}

		return zoneGroups;
	}

	private static NodeList findChildNodes(NodeList parentNodes,
			String parentNodeName) {
		NodeList result = null;
		if (parentNodes != null) {
			for (int i = 0; i < parentNodes.getLength(); i++) {
				Node node = parentNodes.item(i);
				if (node.getNodeName().equals(parentNodeName)) {
					result = node.getChildNodes();
					break;
				}
			}
		}
		return result;
	}

}
