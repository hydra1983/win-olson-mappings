package com.wds.tools.winzonesgen.zone;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wds.tools.winzonesgen.utils.Strings;

public class Zones {
	private static final Logger LOG = LoggerFactory.getLogger(Zones.class);

	static final String ELEM_WINDOWS_ZONES = "windowsZones";
	static final String ELEM_MAP_TIME_ZONES = "mapTimezones";
	static final String ATTR_TYPE = "type";
	static final String ATTR_OTHER = "other";
	static final String ATTR_TERRITORY = "territory";
	static final String UNMAPPABLE = "Unmappable";
	static final String FIRST_ZONE = "001";

	public static List<ZoneGroup> parse(File file) throws SAXException,
			IOException, ParserConfigurationException {
		return doParse(createDocBuilder().parse(file));
	}

	public static List<ZoneGroup> parse(String url) throws SAXException,
			IOException, ParserConfigurationException {
		return doParse(createDocBuilder().parse(url));
	}

	private static DocumentBuilder createDocBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		docBuilderFactory.setIgnoringComments(false);
		return docBuilderFactory.newDocumentBuilder();
	}

	private static List<ZoneGroup> doParse(Document doc) {
		List<ZoneGroup> zoneGroups = Lists.newArrayList();
		Element root = doc.getDocumentElement();
		NodeList rootNodes = root.getChildNodes();
		NodeList windowsZonesNodes = findChildNodes(rootNodes,
				ELEM_WINDOWS_ZONES);
		NodeList mapTimezonesNodes = findChildNodes(windowsZonesNodes,
				ELEM_MAP_TIME_ZONES);

		List<String> unmappableNodeList = Lists.newArrayList();

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
					} else if (nodeKeyList.size() > 1) {
						unmappableNodeList.add(nodeKeyList.get(nodeKeyList
								.size() - 1));
					}
				} else if (currentNodeList != null) {
					currentNodeList.add(node);
				}
			}

			for (String key : unmappableNodeList) {
				nodeKeyList.remove(key);
				nodeListMap.remove(key);
			}

			for (String key : nodeKeyList) {
				ZoneGroup group = ZoneGroup.create(key);
				zoneGroups.add(group);

				List<Node> nodeList = nodeListMap.get(key);
				for (Node node : nodeList) {
					NamedNodeMap attrs = node.getAttributes();
					if (attrs != null) {
						String value = null;
						String other = null;
						String territory = null;
						String[] types = null;

						for (int i = 0; i < attrs.getLength(); i++) {
							Node attr = attrs.item(i);
							if (attr.getNodeName().equals(ATTR_TYPE)) {
								value = attr.getNodeValue();
							} else if (attr.getNodeName().equals(ATTR_OTHER)) {
								other = attr.getNodeValue();
							} else if (attr.getNodeName()
									.equals(ATTR_TERRITORY)) {
								territory = attr.getNodeValue();
							}

							if (value != null && !value.isEmpty()) {
								types = value.split(" ");
							}
						}

						if (types != null && types.length > 0) {
							for (String type : types) {
								if (!type.isEmpty()) {
									group.add(Zone.create(type, territory,
											other));
								}
							}
						} else {
							if (LOG.isErrorEnabled()) {
								LOG.error(Strings.substitute(
										"No type found for {0}",
										node.toString()));
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
