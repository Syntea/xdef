package org.xdef.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.xml.KXmlUtils;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import org.xdef.xon.XonTools;
import static org.xdef.xon.XonTools.genXMLValue;
import static org.xdef.xon.XonTools.toXmlName;
import org.xdef.xon.XonUtils;
import static org.xdef.xon.XonUtils.parseXON;
import static org.xdef.xon.XonNames.X_VALUE;

/** XON and XML conversions (internal format)
 * @author Vaclav Trojan
 */
public class XonXml_X {
////////////////////////////////////////////////////////////////////////////////
// names used in XON internal format (Experimental version)
////////////////////////////////////////////////////////////////////////////////
	/** URI of XON/JSON/INI XML internal conversion*/
	public static final String XON_NS_URI_X = "http://www.xdef.org/xon/4.0/x";

	/** Create internal XML format of XML from an XON object.
	 * @param val value.
	 * @param parent parent node where to add created element.
	 * @param doc owner document.
	 * @return element with value internal format.
	 */
	private static Element genValueX(final Object val,
		final Node parent,
		final Document doc) {
		Element e;
		if (val instanceof Map) {
			e = genMapX((Map) val, doc);
		} else if (val instanceof List) {
			e = genArrayX((List) val, doc);
		} else {
			e = doc.createElementNS(XON_NS_URI_X, X_VALUE);
			e.setAttribute(X_VALATTR, genXMLValue(val));
		}
		parent.appendChild(e);
		return e;
	}

	/** Create XML from array (internal form).
	 * @param array array to be created.
	 * @param doc owner document.
	 * @return element with array (internal form).
	 */
	private static Element genArrayX(final List array, final Document doc) {
		Element e = doc.createElementNS(XON_NS_URI_X, X_ARRAY);
		for (Object val: array) {
			genValueX(val, e, doc);
		}
		return e;
	}

	/** Create XML from map (internal form).
	 * @param map map to be created.
	 * @param doc owner document.
	 * @return element with map (internal form).
	 */
	private static Element genMapX(final Map map, final Document doc) {
		Element e = e = doc.createElementNS(XON_NS_URI_X, X_MAP);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Map.Entry) it.next();
			Object o = en.getKey(); // name
			// NOTE in YAML it may be a byte array, otherwise it is String
			// convert key to XML name
			String key = o instanceof byte[]? new String((byte[])o) : (String)o;
			key = toXmlName(key);
			Element item = doc.createElement(key);
			e.appendChild(item);
			o = en.getValue();
			if (o instanceof List) {
				item.appendChild(genArrayX((List) o, doc));
			} else if (o instanceof Map) {
				item.appendChild(genMapX((Map) o, doc));
			} else {
				item.setAttribute(X_VALATTR, genXMLValue(o));
			}
		}
		return e;
	}

	/** Create XML from XON object (internal form).
	 * @param xon object with XON data.
	 * @return XML element created from XON data (internal form).
	 */
	private static Element toXmlX(final Object xon) {
		Document doc = KXmlUtils.newDocument();
		return genValueX(xon, doc, doc);
	}

	/** Create XML from XON object (internal form).
	 * @param xon path to XON source data.
	 * @return XML element created from XON data.
	 */
	public final static Element xonToXmlX(final String xon) {
		return toXmlX(XonUtils.parseXON(xon));
	}

	/** Create XML from XON object (internal form).
	 * @param xon file with XON source data.
	 * @return XML element created from XON data.
	 */
	public final static Element xonToXmlX(final File xon) {
		return toXmlX(XonUtils.parseXON(xon));
	}

	/** Create XML from XON object (internal form).
	 * @param xon URL where is XON source data.
	 * @return XML element created from XON data.
	 */
	public final static Element xonToXmlX(final URL xon) {
		return toXmlX(parseXON(xon));
	}

	/** Create XML from XON object (internal form).
	 * @param xon Input stream where is XON source data.
	 * @return XML element created from XON data.
	 */
	public final static Element xonToXmlX(final InputStream xon) {
		return toXmlX(XonUtils.parseXON(xon));
	}

	/** Create XML from XON object (internal form).
	 * @param xon XON object.
	 * @return XML element created from XON data.
	 */
	public final static Element xonToXmlX(final Object xon) {
		return toXmlX(xon);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Create XON array from array element (internal from).
	 * @param elem internal form of array element .
	 * @return created XON array.
	 */
	private static List<Object> createArrayX(final Element elem) {
		List<Object> result = new ArrayList<Object>();
		Node n = elem.getFirstChild();
		while(n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				result.add(fromXmlX((Element) n));
			}
			n = n.getNextSibling();
		}
		return result;
	}

	/** Create XON map from map element (internal form).
	 * @param elem internal form of map element.
	 * @return created XON object (map).
	 */
	private static Map<String, Object> createMapX(final Element elem) {
		Map<String,Object> result = new LinkedHashMap<String,Object>();
		Node n = elem.getFirstChild();
		while(n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				String key = e.getNodeName();
				key = XonTools.xmlToJName(key);
				Attr attr = e.getAttributeNode(X_VALATTR);
				if (attr != null) { // item with simple value
					result.put(key, XonTools.xmlToJValue(attr.getValue()));
				} else { // array or map
					Node nn = e.getFirstChild();
					while (nn != null) {
						if (nn.getNodeType() == Node.ELEMENT_NODE) {
							result.put(key, fromXmlX((Element) nn));
							break;
						}
						nn = nn.getNextSibling();
					}
					if (nn == null) {
						throw new RuntimeException(
							"Missing child element (internal form)");
					}
				}
			}
			n = n.getNextSibling();
		}
		return result;
	}

	/** Create XON object from element with array,map,or value (internal form).
	 * @param elem element with XON object in internal form.
	 * @return XON object.
	 */
	private static Object fromXmlX(final Element elem) {
		String name = elem.getNodeName();
		if (X_ARRAY.equals(name)) { // array
			return createArrayX(elem);
		} else if (X_MAP.equals(name)) { // map
			return createMapX(elem);
		} else if (X_VALUE.equals(name)) { // item
			Attr attr = elem.getAttributeNode(X_VALATTR);
			if (attr != null) {
				return XonTools.xmlToJValue(attr.getNodeValue());
			}
		}
		throw new RuntimeException(
			"Unsupported XON internal form element: " + elem.getTagName());
	}

	/** Create XON/JSON object (map, array or primitive value) from an element.
	 * @param node XML node with XON/JSON data.
	 * @return created XON/JSON object (map, array or primitive value).
	 */
	public final static Object toXon(final Node node) {
		Element elem = node.getNodeType() == Node.DOCUMENT_NODE
			? ((Document) node).getDocumentElement() : (Element) node;
		String name = elem.getTagName();
		String uri = elem.getNamespaceURI();
		return XON_NS_URI_X.equals(uri)
			? fromXmlX(elem) // X format
			: XonUtils.xmlToXon(elem); // W or XD formats
	}

}
