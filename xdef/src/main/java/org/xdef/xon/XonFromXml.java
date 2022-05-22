package org.xdef.xon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import static org.xdef.xon.XonNames.I_ARRAY;
import static org.xdef.xon.XonNames.I_ITEM;
import static org.xdef.xon.XonNames.I_MAP;
import static org.xdef.xon.XonNames.I_VALUEATTR;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_ITEM;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALUEATTR;

/** Converter XML -> XON/JSON.
 * @author Vaclav Trojan
 */
class XonFromXml extends XonUtils implements XonNames {

////////////////////////////////////////////////////////////////////////////////
// Keywords of names of XON/JSON types
////////////////////////////////////////////////////////////////////////////////
	/** XON/JSON string item. */
	private final static String J_STRING = "string";
	/** XON/JSON number item. */
	private final static String J_NUMBER = "number";
	/** XON/JSON boolean item. */
	private final static String J_BOOLEAN = "boolean";
	/** XON/JSON null item. */
	private final static String J_NULL = "null";

	private XonFromXml() {super();}

	/** Create list of elements and texts from child nodes of element.
	 * @param el element from which the list is created.
	 * @return list elements and texts created from child nodes of element.
	 */
	private static List<Object> getElementChildList(final Element el) {
		List<Object> result = new ArrayList<Object>();
		Node n = el.getFirstChild();
		while (n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				result.add(n);
				n = n.getNextSibling();
			} else if (n.getNodeType() == Node.TEXT_NODE
				|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
				StringBuilder sb = new StringBuilder();
				String s;
				for (;;) {
					s = n.getNodeValue();
					if (s != null) {
						sb.append(s);
					}
					while ((n = n.getNextSibling()) != null
						&& n.getNodeType() != Node.TEXT_NODE
						&& n.getNodeType() != Node.TEXT_NODE
						&& n.getNodeType() == Node.CDATA_SECTION_NODE) {}
					if (n == null || n.getNodeType() == Node.ELEMENT_NODE) {
						break;
					}
				}
				if (!(s = sb.toString().trim()).isEmpty()) {
					result.add(s);
				}
			}
		}
		return result;
	}

	/** Read attributes of element to map.
	 * @param el element with attributes.
	 * @return created map from attributes of element.
	 */
	private Map<String, Object> getElementAttributes(final Element el) {
		String name = el.getTagName();
		int ndx = name.indexOf(':');
		String prefix = ndx > 0 ? ':' + name.substring(0, ndx) : "";
		String xmlnsName = "xmlns" + prefix;
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		NamedNodeMap nnm = el.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Node n = nnm.item(i);
			name = n.getNodeName();
			if (!(xmlnsName.equals(name = n.getNodeName())
				&& XDConstants.XON_NS_URI_XD.equals(el.getNamespaceURI()))) {
				String attName = XonTools.xmlToJName(name);
				Object val = XonTools.xmlToJValue(n.getNodeValue());
				result.put(attName, val);
			}
		}
		return result;
	}

	/** Create XON/JSON object (array, map, or primitive value).
	 * @param elem element from XDConstants.XON_NS_URI_XD name space with
	 * XON/JSON array, map, or primitive value.
	 * @return XON/JSON array, map, or primitive value.
	 */
	private Object fromXmlW3C(final Element elem) {
		String localName = elem.getLocalName();
		if (X_ARRAY.equals(localName)) {
			return createArrayW3C(elem);
		} else if (X_MAP.equals(localName)) {
			return createMapW3C(elem);
		} else if (X_ITEM.equals(elem.getLocalName())) {
			if (elem.hasAttribute(X_VALUEATTR)) {
				return XonTools.xmlToJValue(elem.getAttribute(X_VALUEATTR));
			}
			String s = elem.getTextContent();
			return s == null || s.isEmpty() ? null : XonTools.xmlToJValue(s);
		} else if (J_BOOLEAN.equals(elem.getLocalName())) {
			return ("true".equals(elem.getTextContent().trim()));
		} else if (J_NULL.equals(elem.getLocalName())) {
			return null;
		} else if (J_NUMBER.equals(elem.getLocalName())) {
			return new BigDecimal(elem.getTextContent().trim());
		} else if (J_STRING.equals(elem.getLocalName())) {
			return XonTools.xmlToJValue(elem.getTextContent());
		}
		throw new RuntimeException(
			"Unsupported XON/JSON W3C element: " + elem.getLocalName());
	}

////////////////////////////////////////////////////////////////////////////////

	/** Create XON/JSON array from array element.
	 * @param elem array element from XDConstants.XON_NS_URI_XD name space.
	 * @return created XON/JSON array.
	 */
	private List<Object> createArrayW3C(final Element elem) {
		List<Object> result = new ArrayList<Object>();
		Node n = elem.getFirstChild();
		while(n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				result.add(fromXmlW3C((Element) n));
			}
			n = n.getNextSibling();
		}
		return result;
	}

	/** Create XON/JSON object (map) from map element.
	 * @param elem map element from XDConstants.XON_NS_URI_XD name space.
	 * @return created XON/JSON object (map).
	 */
	private Map<String, Object> createMapW3C(final Element elem) {
		Map<String,Object> result = new LinkedHashMap<String,Object>();
		Node n = elem.getFirstChild();
		while(n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				String key = e.getNodeName();
				if (XDConstants.XON_NS_URI_W.equals(e.getNamespaceURI())
					&& (key.endsWith(X_ARRAY) || key.endsWith(X_MAP)
						|| key.endsWith(X_ITEM) || key.endsWith(J_NULL))) {
					key = XonTools.xmlToJName(e.getAttribute(X_KEYATTR));
					result.put(key, fromXmlW3C(e));
				} else {
					Object val;
					if (!e.hasAttribute(X_VALUEATTR)) {
						val = null;
					} else {
						val = XonTools.xmlToJValue(e.getAttribute(X_VALUEATTR));
					}
					result.put(XonTools.xmlToJName(key),val);
				}
			}
			n = n.getNextSibling();
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Add string with a simple value or with the list of simple values.
	 * to the array from the argument.
	 * <UL>
	 * <li> [] -> empty array.
	 * <li> [ x ... ,x] -> empty with simple values.
	 * <li> other -> simple values
	 * </UL>
	 * @param array array where to add items.
	 * @param s string with values.
	 */
	private void addSimpleValue(final List<Object> array, String s) {
		Object o = XonTools.xmlToJValue(s);
		if (o instanceof List) {
			for (Object x: (List) o) {
				array.add(x);
			}
		} else {
			array.add(o);
		}
	}

	/** Create XON/JSON object form element (XD form).
	 * @param elem the element form which object will be created.
	 * @return created XON/JSON object.
	 */
	private Object fromXmlXD(final Element elem) {
		String name = XonTools.xmlToJName(elem.getNodeName());
		Map<String, Object> attrs = getElementAttributes(elem);
		List<Object> childNodes = getElementChildList(elem);
		// result object
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		List<Object> array = new ArrayList<Object>();
		String nsURI = elem.getNamespaceURI(); // nasmespace URI of element
		String localName = nsURI==null ? elem.getNodeName():elem.getLocalName();
		if (XDConstants.XON_NS_URI_XD.equals(nsURI)) {
			if (X_ITEM.equals(localName)) {
				String s;
				if (elem.hasAttribute(X_VALUEATTR)) {
					s = elem.getAttribute(X_VALUEATTR);
				} else {
					s = elem.getTextContent();
					if (s != null) {
						s = s.trim();
					}
				}
				return XonTools.xmlToJValue(s);
			} else if (X_MAP.equals(localName)) {
				map.putAll(attrs);
				for (Object o: childNodes) {
					if (o instanceof Element) {
						Element el = (Element) o;
						name = XonTools.xmlToJName(el.getNodeName());
						o = fromXmlXD(el);
						if (o instanceof Map) {
							Map m = (Map) o;
							if (m.size() == 1) {
								Map.Entry e = (Map.Entry)
									m.entrySet().iterator().next();
								Object val = e.getValue();
								if (val instanceof List || val instanceof Map) {
									map.put(name, val);
								} else {
									map.put((String) e.getKey(), val);
								}
							} else {
								map.put(name, o);
							}
						} else if (o instanceof List) {
							map.put(name, o);
						} else {
							map.put(name, o);
						}
					} else {
						String s = (String) o;
						if (!s.isEmpty() // if not comment
							|| !s.startsWith("/*") || !s.endsWith("*/")) {
							map.put(name, XonTools.xmlToJValue(s));
							throw new RuntimeException(
								"Text is not allowed in XON/JSON map element: "
								+ s);
						}
					}
				}
				return map;
			} else if (X_ARRAY.equals(localName)) {
				if (!attrs.isEmpty()) {
					array.add(attrs);
				}
				for (Object o: childNodes) {
					if (o instanceof Element) {
						array.add(fromXmlXD((Element) o));
					} else {
						addSimpleValue(array, (String) o);
					}
				}
				return array;
			} else if (J_NULL.equals(localName)) {
				return null;
			} else if (J_STRING.equals(localName)
				|| J_NUMBER.equals(localName)
				|| J_BOOLEAN.equals(localName)) {
				if (elem.hasAttribute(X_VALUEATTR)) {
					return XonTools.xmlToJValue(elem.getAttribute(X_VALUEATTR));
				}
				String s = elem.getTextContent();
				return XonTools.xmlToJValue(s);
			}
			throw new RuntimeException(
				"Unknown element from XON/JSON namespace: " + name);
		}
		if (childNodes.isEmpty()) {
			if (attrs.isEmpty()) {
				array.add(attrs);
				map.put(name, array);
			} else {
				map.put(name, attrs);
			}
			return map;
		}
		int len = childNodes.size();
		if (len == 1) {
			Object o = childNodes.get(0);
			if (o instanceof String) {
				String s = (String) o;
				boolean genMap = true;
				if (elem.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
					// root element creates map if it has not "xmlns" attribute
					for (Object x: attrs.keySet()) {
						if (!((String) x).startsWith("xmlns")) {
							genMap = false;
							break;
						}
					}
					if (genMap) {
						attrs.put(name, XonTools.xmlToJValue(s));
						return attrs;
					}
				}
				if (!attrs.isEmpty()) {
					array.add(attrs);
				}
				if (XDConstants.XDEF41_NS_URI.equals(nsURI)
					|| XDConstants.XDEF40_NS_URI.equals(nsURI)
					|| XDConstants.XDEF32_NS_URI.equals(nsURI)
					|| XDConstants.XDEF31_NS_URI.equals(nsURI)) {
					array.add(s); //don't convert text of xd:xon/jsaon elements!
				} else {
					addSimpleValue(array, s);
				}
				map.put(name, array);
				return map;
			} else {
				o = fromXmlXD((Element) o);
				if (o instanceof Map) {
					Map m = (Map) o;
					Map<String, Object> mm = null;
					for (Object x: m.entrySet()) {
						Map.Entry entry = (Map.Entry) x;
						Object val = entry.getValue();
						String key = (String) entry.getKey();
						if (m.size() == 1 && val instanceof Map) {
							attrs.put(key, val);
							map.put(name, attrs);
							return map;
						}
						if (val instanceof List
							&& ((List) val).size() == 1
							&& ((List) val).get(0) instanceof Map
							&& ((Map)((List)val).get(0)).isEmpty()){
							mm = new LinkedHashMap<String, Object>();
							List<Object> empty = new ArrayList<Object>();
							empty.add(new LinkedHashMap<String, Object>());
							mm.put(key, empty);
							array.add(attrs);
							array.add(mm);
							map.put(name, array);
							return map;
						} else {
							if (attrs.containsKey(key)) {
								if (mm == null) {
									mm = new LinkedHashMap<String, Object>();
								}
								mm.put(key, val);
							} else {
								attrs.put(key, val);
							}
						}
					}
					if (mm == null) {
						map.put(name, attrs);
					} else {
						array.add(attrs);
						array.add(mm);
						map.put(name, array);
					}
					return map;
				} else if (o instanceof List) {
					map.put(name, o);
					return map;
				}
			}
		}
		for (int i = 0; i < len; i++) {
			Object o = childNodes.get(i);
			if (o instanceof String) {
				addSimpleValue(array, (String) o);
			} else {
				o = fromXmlXD((Element) o);
				array.add(o);
			}
		}
		if (!attrs.isEmpty()) {
			array.add(0, attrs);
		}
		map.put(name, array);
		return map;
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
				Attr attr = e.getAttributeNode(I_VALUEATTR);
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
		if (I_ARRAY.equals(name)) { // array
			return createArrayX(elem);
		} else if (I_MAP.equals(name)) { // map
			return createMapX(elem);
		} else if (I_ITEM.equals(name)) { // item
			Attr attr = elem.getAttributeNode(I_VALUEATTR);
			if (attr != null) {
				return XonTools.xmlToJValue(attr.getNodeValue());
			}
		}
		throw new RuntimeException(
			"Unsupported XON internal form element: " + elem.getTagName());
	}

////////////////////////////////////////////////////////////////////////////////

	/** Create XON/JSON object (map, array or primitive value) from an element.
	 * @param node XML node with XON/JSON data.
	 * @return created XON/JSON object (map, array or primitive value).
	 */
	final static Object toXon(final Node node) {
		Element elem = node.getNodeType() == Node.DOCUMENT_NODE
			? ((Document) node).getDocumentElement() : (Element) node;
		String name = elem.getTagName();
		String uri = elem.getNamespaceURI();
		if ((uri == null || uri.isEmpty())
			&& (I_ARRAY.equals(name)||I_MAP.equals(name)||I_ITEM.equals(name))){
			return fromXmlX(elem); // internal rormat
		} else {
			XonFromXml x = new XonFromXml();
			return (XDConstants.XON_NS_URI_W.equals(uri))
				? x.fromXmlW3C(elem)// W3C format
				: x.fromXmlXD(elem); // XD format
		}
	}
}
