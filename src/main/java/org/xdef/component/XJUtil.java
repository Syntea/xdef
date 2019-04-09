package org.xdef.component;

import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.JSONUtil;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides utilities for JSON to XDEF conversion.
 * @author Vaclav Trojan
 */
public class XJUtil extends JSONUtil {

	private XJUtil() {super();}

////////////////////////////////////////////////////////////////////////////////
// JSON to XDEF
////////////////////////////////////////////////////////////////////////////////
	/** Get Document from argument. If argument is null then create Document.
	 * @param n node.
	 * @return Document object.
	 */
	private static Document getDoc(final Node n) {
		return (n instanceof Document) ? (Document) n : n.getOwnerDocument();
	}

	/** Get prefix of XML name.
	 * @param s XML name.
	 * @return prefix of XML name.
	 */
	private static String getNamePrefix(final String s) {
		final int i;
		return (i = s.indexOf(':')) >= 0 ? s.substring(0, i) : "";
	}

	/** Generate  value to XML string,
	 * @param val Object with value.
	 * @return string representing the value.
	 */
	private static String genSimpleValueToXD(final Object val) {
		if (val == null) {
			return "eq(\"null\")";
		} else if (val instanceof String) {
			String s;
			if ((s = (String) val).length() == 0) {
				return "jstring()";
			} else {
				boolean addQuot = "null".equals(s) || "true".equals(s)
					|| "false".equals(s) || s.indexOf(' ') > 0
					|| s.indexOf('\t') > 0 || s.indexOf('\n') > 0
					|| s.indexOf('\r') > 0 || s.indexOf('\f') > 0;
				if (s.indexOf('\"') > 0) {
					s = SUtils.modifyString(s, "\"", "\"\"");
					addQuot = true;
				}
				if (s.indexOf('\\') > 0) {
					s = SUtils.modifyString(s, "\"", "\\\\");
					addQuot = true;
				}
				if (!addQuot) {
					final char ch = s.charAt(0);
					if (ch == '-' || ch >= '0' && ch <= '9') {
						StringParser p = new StringParser(s);
						p.isChar('-');
//						addQuot |= p.isFloat() || p.isInteger();
					}
				}
				return "jstring()";
			}
		} else {
			return val instanceof Number ? "jnumber()" : "boolean()";
		}
	}

	/** Append text with a value to element.
	 * @param e element where to add value.
	 * @param val value to be added as text.
	 */
	private static void addValueAsTextXD(final Element e, final Object val) {
		e.appendChild(getDoc(e).createTextNode(genSimpleValueToXD(val)));
	}

	/** Create child element with text value to node.
	 * @param node node where to create element.
	 * @param val value which will be represented as value of created element.
	 */
	private void addValueToXD(final Node node, final Object val) {
		String name;
		if (val == null) {
			name = J_NULL;
		} else if (val instanceof String) {
			name = J_STRING;
		} else if (val instanceof Number) {
			name = J_NUMBER;
		} else if (val instanceof Boolean) {
			name = J_BOOLEAN;
		} else {
			throw new RuntimeException("Unknown object: " + val);
		}
		if (val != null) {
			addValueAsTextXD(appendJSONElem(node, name), val);
		} else {
			appendJSONElem(node, name);
		}
		popContext();
	}

	/** Create named item,
	 * @param rawName raw name (JSON string).
	 * @param val Object to be created as named item.
	 * @param parent parent node where the item will be appended.
	 * @return created element.
	 */
	private Element namedItemToXD(final String rawName,
		final Object val,
		final Node parent) {
		final String name = toXmlName(rawName);
		final String namespace = _ns.getNamespaceURI(getNamePrefix(name));
		if (val == null) {
			return appendElem(parent, namespace, name);
		} else if (val instanceof Map) {
			final Map<?, ?> map = (Map) val;
			if (map.isEmpty()) {
				final Element e = appendElem(parent, namespace, name);
				appendJSONElem(e, J_MAP);
				popContext(); // appended element js:map
				return e;
			}
			String u = getNamePrefix(name);
			u = u.length() > 0 ? "xmlns:" + u : "xmlns";
			u = (String) map.get(u);
			if (u == null) {
				u = namespace;
			}
			final Element e = appendElem(parent, u, name);
			for (Object key: map.keySet()) { // xmlns elements
				String name1 = (String) key;
				final Object o = map.get(key);
				if (o == null || o instanceof String) {
					name1 = toXmlName(name1);
					final String s = o == null ? "" : o.toString();
					if ("xmlns".equals(name1)) {
						e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							"xmlns", s);
						_ns.setPrefix("", s);
					} else if (name1.startsWith("xmlns:")) {
						e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							name1, s);
						_ns.setPrefix(name1.substring(6), s);
					}
				}
			}
			Element ee = null;
			for (Object key: map.keySet()) {
				final String name1 = toXmlName(key.toString());
				final Object o = map.get(key);
				if (o != null && (o instanceof Map || o instanceof List)) {
					if (ee == null) {
						ee = appendJSONElem(e, J_EXTMAP);
					}
					namedItemToXD(name1, o, ee);
					popContext();
				} else {
					if (!name1.startsWith("xmlns")) {
						final String uri1 =
							_ns.getNamespaceURI(getNamePrefix(name1));
						if (isSimpleValue(o)) { // we set it as attribute
							String s;
							if (o == null) {
								s = "eq(\"null\")";
							} else if (o instanceof String) {
								s = (String) o;
								if (s.length() == 0) {
									s = "empty()";
								} else {
									StringParser p = new StringParser(s);
									if ((p.isToken("null") || p.isToken("true")
										|| p.isToken("false")||p.isSignedFloat()
										|| p.isSignedInteger()) && p.eos()) {
										s = genSimpleValueToXD(o);
									} else {
										s = "string()";
									}
								}
							} else {
								s = genSimpleValueToXD(o);
							}
							setAttr(e, uri1, name1, s);
						} else {
							if (map.size() == 1) {
								if (o == null) {
									setAttr(e, uri1, name1, "eq(\"null\")");
								} else { // string
									String s = (String) o;
									if ("".equals(s)) {
										setAttr(e, uri1, name1, "empty()");
									} else {
										s = genSimpleValueToXML(s);
										if (s.charAt(0) == '"') {
											// we set it as named item
											if (ee == null) {
												ee = appendJSONElem(e,J_EXTMAP);
											}
											namedItemToXD(name1, val, ee);
											popContext();
										} else { // just attribute
											s = genSimpleValueToXD(val);
											setAttr(e, uri1, name1, s);
										}
									}
								}
								break;
							}
							if (ee == null) {
								ee = appendJSONElem(e, J_EXTMAP);
							}
							namedItemToXD(name1, o, ee);
							popContext();
						}
					}
				}
			}
			if (ee != null) {
				final NodeList nl = ee.getChildNodes();
				if (nl.getLength() > 1) {
					Element eee = getDoc(ee).createElementNS(XDEF31_NS_URI,
						XDEF_NS_PREFIX + ":mixed");
					for (int i = nl.getLength()-1; i >=0 ; i--) {
						final Node n = nl.item(i);
						eee.appendChild(n.cloneNode(true));
						ee.removeChild(n);
					}
					ee.appendChild(eee);
				}
				popContext();
			}
			return e;
		} else if (val instanceof List) {
			final Element e = appendElem(parent, namespace, name);
			listToXD((List) val, e);
			return e;
		} else {
			final Element e = appendElem(parent, namespace, name);
			addValueAsTextXD(e, val);
			return e;
		}
	}

	private void listToXD(final List<?> list, final Node parent) {
		final Element e = appendJSONElem(parent, J_ARRAY);
		for (Object x: list) {
			if (x == null) {
				addValueToXD(e, null);
			} else if (x instanceof Map) {
				mapToXD((Map) x, e);
			} else if (x instanceof List) {
				listToXD((List)x,  e);
			} else {
				addValueToXD(e, x);
			}
		}
		popContext();
	}

	private void mapToXD(final Map<?, ?> map, final Node parent) {
		final int size = map.size();
		if (size == 0) {
			appendJSONElem(parent, J_MAP);
		} else if (size == 1) {
			final String key = (String) map.keySet().iterator().next();
			namedItemToXD(key, map.get(key), parent);
		} else {
			final Element el = appendJSONElem(parent, J_MAP);
			for (Object key: map.keySet()) {
				namedItemToXD(key.toString(), map.get(key), el);
				popContext();
			}
		}
		popContext();
	}

	private void json2xd(final Object o, final Node parent) {
		if (o != null) {
			if (o instanceof Map) {
				mapToXD((Map) o, parent);
				return;
			}
			if (o instanceof List) {
				listToXD((List) o, parent);
				return;
			}
		}
		throw new SRuntimeException(JSON.JSON011, o); // Not JSON object&{0}
	}

	/** Convert object with JSON data to XML element.
	 * @param json object with JSON data.
	 * @return XML element with X-definition.
	 */
	public final static Element jsonToXDef(final Object json) {
		try {
			final DocumentBuilderFactory df =
				DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = df.newDocumentBuilder();
			final Document doc = db.newDocument();
			final XJUtil jsp = new XJUtil();
			final Element xdef =
				jsp.appendElem(doc, XDEF31_NS_URI, XDEF_NS_PREFIX + ":def");
			xdef.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:" + JSON_NS_PREFIX, JSON_NS_URI);
			jsp._ns.setPrefix(JSON_NS_PREFIX, JSON_NS_URI);
			jsp._ns.setPrefix(XDEF_NS_PREFIX, XDEF_NS_PREFIX);
			jsp.json2xd(json, xdef);
			xdef.setAttribute("root",
				KXmlUtils.firstElementChild(xdef).getNodeName());
			jsp.popContext();
			if (jsp._n != 0) {
				// Internal error&{0}{: }
				throw new SRuntimeException(SYS.SYS066,
					"Namespace nesting:"+jsp._n);
			}
			return xdef;
		} catch (ParserConfigurationException ex) {
			return null;
		}
	}
}