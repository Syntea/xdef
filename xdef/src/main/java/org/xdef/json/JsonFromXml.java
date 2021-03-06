package org.xdef.json;

import org.xdef.XDConstants;
import org.xdef.sys.StringParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Test X-definition transformation XML -> JSONL
 * @author Vaclav Trojan
 */
class JsonFromXml extends JsonUtil {

	private JsonFromXml() {super();}

	private static String sourceToJstring(final String s)  {
		StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '\\') { // escaped characters
				if (++i >= s.length()) {
					return s; // missing escape char (error)
				}
				switch (ch = s.charAt(i)) {
					case '"':
						ch = '"';
						break;
					case '\\':
						ch = '\\';
						break;
					case '/':
						ch = '/';
						break;
					case 'b':
						ch = '\b';
						break;
					case 'f':
						ch = '\f';
						break;
					case 'n':
						ch = '\n';
						break;
					case 'r':
						ch = '\r';
						break;
					case 't':
						ch = '\t';
						break;
					case 'u':
						try {
							ch = (char) Short.parseShort(
								s.substring(i+1, i+5), 16);
							i += 4;
							break;
						} catch (Exception ex) {
							return s; // incorrect UTF-8 char (error)
						}
					default: return s; // illegal escape char (error)
				}
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	/** Parse JList (the string which begins with '[').
	 * @param ar the array where results are stored.
	 * @param p String parser with the string.
	 */
	private static void parseJList(final List<Object> ar, final StringParser p){
		for (;;) {
			p.skipSpaces();
			if (p.isChar('[')) {
				List<Object> ar1 = new ArrayList<Object>();
				parseJList(ar1, p);
				ar.add(ar1);
				continue;
			}
			if (p.isChar(']')) {
				break;
			}
			int pos = p.getIndex();
			char ch;
			int i;
			if ((i=p.isOneOfTokens(new String[]{"null","true","false"}))>=0
				&& (p.eos() || (ch = p.getCurrentChar()) <= ' '
					|| ch == ']' || ch == ',')) {
					ar.add(i == 0 ? null : i==1);
			} else {
				p.setIndex(pos);
				if (p.isSignedInteger()
					&& (p.eos() || (ch = p.getCurrentChar()) <= ' '
					|| ch == ']' || ch == ',')) {
					try {
						ar.add(Long.parseLong(
							p.getBufferPart(pos, p.getIndex())));
					} catch (Exception ex) {
						ar.add(new BigDecimal(
							p.getBufferPart(pos, p.getIndex())));
					}
				} else {
					p.setIndex(pos);
					if (p.isSignedFloat()
						&& (p.eos() || (ch = p.getCurrentChar()) <= ' '
						|| ch == ']' || ch == ',')) {
						String s = p.getBufferPart(pos, p.getIndex());
						if (s.indexOf('.') > 0 || s.indexOf('e') > 0
							|| s.indexOf('E') > 0) {
							ar.add(new BigDecimal(
								p.getBufferPart(pos, p.getIndex())));
						} else {
							try {
								ar.add(Long.parseLong(s));
							} catch (Exception ex) {
								ar.add(new BigInteger(
									p.getBufferPart(pos, p.getIndex())));
							}
						}
					} else {
						p.setIndex(pos);
						if (p.isChar('"')) {
							for(;;) {
								if (p.isChar('\\')) {
									if (p.eos()) {
										throw new RuntimeException("JList err");
									}
									p.nextChar();
								} else if (p.isChar('"')) {
									String s = p.getBufferPart(pos+1,
										p.getIndex() - 1);
									ar.add(sourceToJstring(s));
									break;
								} else {
									p.nextChar();
								}
								if (p.eos()) {
									throw new RuntimeException("JList err");
								}
							}
						} else {
							for(;;) {
								if (p.isChar('\\')) {
									if (p.eos()) {
										throw new RuntimeException("JList err");
									}
									p.nextChar();
								} else if ((ch = p.getCurrentChar()) == ' '
									|| ch == ',' || ch == ']' || ch == '[') {
									String s =
										p.getBufferPart(pos, p.getIndex());
									ar.add(xmlToJValue(s));
									break;
								}
								if (p.eos()) {
									throw new RuntimeException("JList err");
								}
								p.nextChar();
							}
						}
					}
				}
			}
			p.skipSpaces();
			if (p.isChar(']')) {
				break;
			}
			if (!p.isChar(',')) {
				throw new RuntimeException("JList err:\n"
					+ p.getParsedBufferPart() + "..."
					+ p.getUnparsedBufferPart());
			}
		}
	}

	/** Get JSON value from string in XML.
	 * @param s string with JSON simple value source
	 * @return object with JSOM value
	 */
	private static Object xmlToJValue(final String s) {
		if (s.isEmpty()) {
			return "";
		} else if (s.charAt(0) == '[') {
			ArrayList<Object> ar = new ArrayList<Object>();
			StringParser p = new StringParser(s);
			p.setIndex(1);
			parseJList(ar, p);
			return ar;
		} else if ("null".equals(s)) {
			return null;
		} else if ("true".equals(s)) {
			return Boolean.TRUE;
		} else if ("false".equals(s)) {
			return Boolean.FALSE;
		}
		int len = s.length();
		char ch = s.charAt(0);
		if (ch == '"' && s.charAt(len-1) == '"') {
			return (len == 1) ? "\"" : sourceToJstring(s.substring(1, len-1));
		}
		int i = 0;
		if (ch == '-' && len > 0) {
			ch = s.charAt(1);
			i = 1;
		}
		if (ch == '0' && i + 1 < len && s.charAt(i+1) >= '0'
			&& s.charAt(i+1) <= '9') {
			return s; //redundant leading zero, => JSON string
		}
		if (ch >= '0' && ch <= '9') { // not redundant leading zero
			try {
				return Long.parseLong(s);
			} catch (Exception ex) {}
			try {
				return new BigDecimal(s);
			} catch (Exception ex) {}
		}
		return s; // JSON String
	}

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
				&& XDConstants.JSON_NS_URI_XD.equals(el.getNamespaceURI()))) {
				String attName = xmlToJsonName(name);
				Object val = xmlToJValue(n.getNodeValue());
				result.put(attName, val);
			}
		}
		return result;
	}

	/** Create JSON object (array, map, or primitive value).
	 * @param elem element from XDConstants.JSON_NS_URI_XD name space with
	 * JSON array, map, or primitive value
	 * @return JSON array, map, or primitive value.
	 */
	private Object fromXmlW3C(final Element elem) {
		String localName = elem.getLocalName();
		if (J_ARRAY.equals(localName)) {
			return createArrayW3C(elem);
		} else if (J_MAP.equals(localName)) {
			return createMapW3C(elem);
		} else if (J_ITEM.equals(elem.getLocalName())) {
			if (elem.hasAttribute(J_VALUEATTR)) {
				return xmlToJValue(elem.getAttribute(J_VALUEATTR));
			}
			return xmlToJValue(((Element) elem).getTextContent());
		} else if (J_BOOLEAN.equals(elem.getLocalName())) {
			return ("true".equals(elem.getTextContent().trim()));
		} else if (J_NULL.equals(elem.getLocalName())) {
			return null;
		} else if (J_NUMBER.equals(elem.getLocalName())) {
			return new BigDecimal(elem.getTextContent().trim());
		} else if (J_STRING.equals(elem.getLocalName())) {
			return xmlToJValue(elem.getTextContent());
		}
		throw new RuntimeException(
			"Unsupported JSON W3C element: " + elem.getLocalName());
	}

////////////////////////////////////////////////////////////////////////////////

	/** Create JSON array from array element.
	 * @param elem array element from XDConstants.JSON_NS_URI_XD name space.
	 * @return created JSON array.
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

	/** Create JSON object (map) from map element.
	 * @param elem map element from XDConstants.JSON_NS_URI_XD name space.
	 * @return created JSON object (map).
	 */
	private Map<String, Object> createMapW3C(final Element elem) {
		Map<String,Object> result = new LinkedHashMap<String,Object>();
		Node n = elem.getFirstChild();
		while(n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				result.put((String) sourceToJstring(e.getAttribute(J_KEYATTR)),
					fromXmlW3C(e));
			}
			n = n.getNextSibling();
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Add string with a simple value or with the list of simple values.
	 * to the array from the argument.
	 * <UL>
	 * <li> [] -> empty array.</li>
	 * <li> [ x ... ,x] -> empty with simple values.
	 * <li> other -> simple values
	 * </UL>
	 * @param array array where to add items.
	 * @param s string with values.
	 */
	private void addSimpleValue(final List<Object> array, String s) {
		Object o = xmlToJValue(s);
		if (o instanceof List) {
			for (Object x: (List) o) {
				array.add(x);
			}
		} else {
			array.add(o);
		}
	}

	/** Create JSON object form element (XD form).
	 * @param elem the element form which object will be created.
	 * @return created JSON object.
	 */
	private Object fromXmlXD(final Element elem) {
		String name = xmlToJsonName(elem.getNodeName());
		Map<String, Object> attrs = getElementAttributes(elem);
		List<Object> childNodes = getElementChildList(elem);
		// result object
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		List<Object> array = new ArrayList<Object>();
		String nsURI = elem.getNamespaceURI(); // nasmespace URI of element
		String localName = nsURI==null ? elem.getNodeName():elem.getLocalName();
		if (XDConstants.JSON_NS_URI_XD.equals(nsURI)) {
			if (J_ITEM.equals(localName)) {
				if (elem.hasAttribute(J_VALUEATTR)) {
					return xmlToJValue(elem.getAttribute(J_VALUEATTR));
				}
				String s = elem.getTextContent();
				if (s != null) {
					s = s.trim();
				}
				return xmlToJValue(s);
			} else if (J_MAP.equals(localName)) {
				map.putAll(attrs);
				for (Object o: childNodes) {
					if (o instanceof Element) {
						Element el = (Element) o;
						name = xmlToJsonName(el.getNodeName());
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
							map.put(name, xmlToJValue(s));
							throw new RuntimeException(
								"Text is not allowed in JSON map element: "+s);
						}
					}
				}
				return map;
			} else if (J_ARRAY.equals(localName)) {
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
				if (elem.hasAttribute(J_VALUEATTR)) {
					return xmlToJValue(elem.getAttribute(J_VALUEATTR));
				}
				String s = elem.getTextContent();
				return xmlToJValue(s);
			}
			throw new RuntimeException(
				"Unknown element from JSON namespace: " + name);
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
				if (!attrs.isEmpty()) {
					array.add(attrs);
				}
				if (XDConstants.XDEF40_NS_URI.equals(nsURI)
					&& "json".equals(localName)) {
					array.add(s);
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
							&&((List) val).size() == 1
							&&((List) val).get(0) instanceof Map
							&&((Map)((List)val).get(0)).isEmpty()){
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

	/** Create JSON object (map, array or primitive value) from element.
	 * @param node XML node with JSON data.
	 * @return created JSON object (map, array or primitive value).
	 */
	final static Object toJson(final Node node) {
		Element elem = node.getNodeType() == Node.DOCUMENT_NODE
			? ((Document) node).getDocumentElement() : (Element) node;
		JsonFromXml x =new JsonFromXml();
		if (XDConstants.JSON_NS_URI_W3C.equals(elem.getNamespaceURI())) {
			return x.fromXmlW3C(elem); // W3C form
		}
		return x.fromXmlXD(elem);
	}
}