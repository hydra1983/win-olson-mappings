package com.wds.tools.winolson.utils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Xmls {
	public static Node findChildNode(Node parentNode, String childNodeName) {
		Node result = null;
		if (parentNode != null) {
			NodeList nodes = parentNode.getChildNodes();
			for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
				Node node = nodes.item(nodeIndex);
				if (node.getNodeName().equals(childNodeName)) {
					if (result == null) {
						result = node;
					} else {
						throw new RuntimeException(
								Strings.substitute(
										"Find multiple nodes with name '{0}' in node with name '{1}'",
										childNodeName, parentNode.getNodeName()));
					}

				}
			}
		}
		return result;
	}

	public static NodeList findChildNodeChildren(Node parentNode,
			String childNodeName) {
		NodeList result = null;
		if (parentNode != null) {
			NodeList nodes = parentNode.getChildNodes();
			for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
				Node node = nodes.item(nodeIndex);
				if (node.getNodeName().equals(childNodeName)) {
					if (result == null) {
						result = node.getChildNodes();
					} else {
						throw new RuntimeException(
								Strings.substitute(
										"Find multiple nodes with name '{0}' in node with name '{1}'",
										childNodeName, parentNode.getNodeName()));
					}

				}
			}
		}
		return result;
	}
}
