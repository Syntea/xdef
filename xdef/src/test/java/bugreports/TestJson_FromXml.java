package bugreports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDConstants;
import org.xdef.json.JNull;
import org.xdef.msg.XDEF;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;

/** Conversion of JSON to XML (X-definition form,or W3C form).
 * @author Vaclav Trojan
 */
class TestJson_FromXml extends TestJson_ToXml {
	// Names of elements created from JSON primitive values in strict W3C format

	/** JSON null item. */
	public static final String J_NULL = "null";
	/** JSON string item. */
	public static final String J_STRING = "string";
	/** JSON number item. */
	public static final String J_NUMBER = "number";
	/** JSON boolean item. */
	public static final String J_BOOLEAN = "boolean";

	////////////////////////////////////////////////////////////////////////////

	/** Document used to create X-definition. */
	private boolean _isW3C;

	private TestJson_FromXml() {super();}

	/** Create JSON Map with attributes from element.
	 * @param e element with attributes.
	 * @return JSON Map with attributes.
	 */
	private Map<String, Object> genAttrs(final Element e) {
		if (e.hasAttributes()) {
			Map<String,Object> attrs = new LinkedHashMap<String,Object>();
			NamedNodeMap nnm = e.getAttributes();
			for (int i = nnm.getLength() - 1; i >= 0; i--) {
				Node n = nnm.item(i);
				if (!_isW3C && !_jsNamespace.equals(n.getNamespaceURI())) {
					attrs.put(toJsonName(n.getNodeName()),
						getJValue(n.getNodeValue()));
				}
			}
			return attrs;
		}
		return null;
	}

	/** Create JSON map from element.
	 * @param e element from which the map will be created.
	 * @return JSON map created from NodeList.
	 */
	private Map<String,Object> createMap(final Element e) {
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		// process attributes
		NamedNodeMap nnm = e.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Node n = nnm.item(i);
			String u = n.getNamespaceURI();
			String name = n.getNodeName();
			if (!_isW3C && !_jsNamespace.equals(u)
				&& !((name.startsWith("xmlns:") || "xmlns".equals(name))
				&& _jsNamespace.equals(n.getNodeValue()))) {
				map.put(toJsonName(name), getJValue(n.getNodeValue()));
			}
		}
		// process child nodes
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element ee = (Element) n;
				Object o = createItem(ee);
				if (_isW3C) {
					String key = ee.getAttribute(J_KEYATTR);
					map.put(TestJson_Util.sourceToJstring(key), o);
					continue;
				}
				if (o instanceof Map) { // map created from element
					String k = toJsonName(ee.getNodeName());
					map.put(k, ((Map) o).get(k));
				} else if (o instanceof List && ((List) o).size() >= 1 &&
					((List) o).get(0) instanceof Map) {
					Iterator<?> x =
						((Map)((List)o).get(0)).entrySet().iterator();
					while (x.hasNext()) {
						Map.Entry<?, ?> y = (Map.Entry) x.next();
						List<?> list = (List) o;
						if (list.size() == 1) {
							map.put(y.getKey().toString(), y.getValue());
						} else {
							List<Object> z= new ArrayList<Object>();
							for (int k = 1; k <  list.size(); k++) {
								z.add(list.get(k));
							}
							map.put(y.getKey().toString(), z);
						}
					}
				} else {
					map.put(toJsonName(n.getNodeName()), o);
				}
			} else if (n.getNodeType() == Node.TEXT_NODE
				&& n.getNodeValue() != null
				&& !n.getNodeValue().trim().isEmpty()) {
				//Test node not allowed in this XML representation of JSOM map.
				throw new SRuntimeException(JSON.JSON016);
			}
		}
		return map;
	}

	/** Create JSON item from XML node.
	 * @param node XML node.
	 * @return JSON item
	 */
	private Object createItem(final Node node) {
		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE: {
				Element e = (Element) node;
				if (_jsNamespace.equals(e.getNamespaceURI())) {
					String name = e.getLocalName();
					if (J_ARRAY.equals(name)) {
						return createArray(e);
					}
					if (J_MAP.equals(name)) {
						return createMap(e);
					}
					if (J_NULL.equals(name)) {
						return JNull.JNULL;
					} else if (J_STRING.equals(name)
						|| J_NUMBER.equals(name)
						|| J_BOOLEAN.equals(name)) {
						/*xx*/
						if (e.hasAttribute(J_VALUEATTR)) {
							return getJValue(e.getAttribute(J_VALUEATTR));
						}
						/*xx*/
						String s = ((Element) node).getTextContent();
						return getJValue(s);
					} else if (J_ITEM.equals(name)) {
						/*xx*/
						if (e.hasAttribute(J_VALUEATTR)) {
							return getJValue(e.getAttribute(J_VALUEATTR));
						}
						/*xx*/
						String s = ((Element) node).getTextContent();
						return getJValue(s);
						/*xx*/
					}
					// Illegal JSON XML model &{0}
					throw new SRuntimeException(XDEF.XDEF313, node.getNodeName());
				} else {
					return getJsonObject((Element) node);
				}
			}
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				String s = node.getNodeValue();
				if (s == null) {
					s = "";
				}
				StringBuilder sb = new StringBuilder(s);
				Node n = node.getNextSibling();
				while (n != null && (n.getNodeType()== Node.TEXT_NODE
					|| n.getNodeType()== Node.CDATA_SECTION_NODE)) {
					s = n.getNodeValue();
					if (s != null) {
						sb.append(s);
					}
					n = n.getNextSibling();
				}
				List<Object> list = new ArrayList<Object>();
				parseArrayList(sb.toString(), 0, list);
				if (list.size() == 1) {
					return list.get(0);
				}
				return list;
		}
		// Illegal JSON XML model &{0}
		throw new SRuntimeException(XDEF.XDEF313, node.getNodeName());
	}

	/** Skip spaces in the string starting given position,
	 * @param s string to be investigated.
	 * @param pos starting position.
	 * @return position to string after spaces.
	 */
	private int skipSpaces(final String s, final int pos) {
		int ndx = pos;
		int len = s.length();
		while(ndx < len) {
			if (s.charAt(ndx) <= ' ') {
				ndx++;
			} else break;
		}
		return ndx;
	}

	/** Skip given token in the the string starting given position,
	 * @param s string to be investigated.
	 * @param pos starting position.
	 * @param token token to be sipped.
	 * @return position to string after the token or the unchanged argument pos.
	 */
	private static int skipToken(String s, final int pos, final String token) {
		if (s.startsWith(token, pos)) {
			int result = pos + token.length();
			int len = s.length();
			if (result == len || result < len && s.charAt(result) <= ' ') {
				return result;
			}
		}
		return pos;
	}

	/** Parse the string with jlist format.
	 * @param jlist string with source jlist.
	 * @param pos position where to start parsing.
	 * @param list List object where the parsed items are stored.
	 * @return position after the last parsed item.
	 */
	private int parseArrayList(final String jlist,
		final int pos,
		final List<Object> list) {
		int ndx = pos;
		int len = jlist.length();
		while ((ndx = skipSpaces(jlist, ndx)) < len) {
			int start = ndx;
			if ((ndx = skipToken(jlist, start, "null")) > start) {
				list.add(JNull.JNULL);
			} else if ((ndx = skipToken(jlist, start, "true")) > start) {
				list.add(Boolean.TRUE);
			} else if ((ndx = skipToken(jlist, start, "false")) > start) {
				list.add(Boolean.FALSE);
			} else {
				char ch;
				try {
					ch = jlist.charAt(ndx);
				} catch (RuntimeException ex) {
					throw ex;
				}
				boolean parsed = false;
				if (ch == '"') {
					while (++ndx < len) {
						ch = jlist.charAt(ndx);
						if (ch == '\\') {
							if (++ndx >= len) {
								break;
							}
						} else if (ch == '"') {
							list.add(TestJson_Util.getJValue(
								jlist.substring(start, ++ndx)));
							parsed = true;
							break;
						}
					}
					if (parsed) {
						continue;
					}
				}
				ndx = start;
				boolean wasDot = false;
				boolean wasE = false;
				if (ch == '-' || (ch >= '0' && ch <= '9')) {
					if (ch == '-') {
						ndx++;
					}
					while (ndx < len) {
						ch = jlist.charAt(ndx);
						if ((ch >= '0' && ch <= '9')) {
							parsed = true;
							ndx++;
						} else if (parsed && !wasDot
							&& ch == '.' && ndx + 1 < len) {
							ch = jlist.charAt(++ndx);
							wasDot = true;
							parsed = false;
						} else if (ndx + 1 < len && parsed && !wasE
							&& (ch=='e'||ch=='E')) {
							ch = jlist.charAt(++ndx);
							if (ndx + 1 < len && (ch == '+' || ch == '-')) {
								ch = jlist.charAt(++ndx);
							}
							wasE = true;
							parsed = false;
						} else {
							break;
						}
					}
					if (parsed && (ndx >= len || jlist.charAt(ndx) <= ' ')) {
						if (wasE || wasDot) {
							list.add(new BigDecimal(
								jlist.substring(start, ndx)));
						} else {
							list.add(new BigInteger(
								jlist.substring(start, ndx)));
						}
						continue;
					}
				}
				ndx = start;
				while (ndx < len && (ch = jlist.charAt(ndx)) > ' ') {
					ndx++;
				}
				if (ndx > start) {
					list.add(jlist.substring(start, ndx));
				}
			}
		}
		return ndx;
	}

	/** Create JSON array from element.
	 * @param e element from which the array will be created.
	 * @return JSON array with values created from the argument e.
	 */
	private List<Object> createArray(final Element e) {
		NodeList nl = e.getChildNodes();
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.TEXT_NODE
				|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
				String s = n.getNodeValue();
				if (s == null) {
					s = "";
				}
				while (i+1 < nl.getLength()
					&& ((n = nl.item(i+1)).getNodeType() == Node.TEXT_NODE
					|| n.getNodeType() == Node.CDATA_SECTION_NODE)) {
					s += n.getNodeValue();
					i++;
				}
				parseArrayList(s, 0, list);
			} else if (n.getNodeType() == Node.ELEMENT_NODE) {
				list.add(createItem(n));
			}
		}
		return list;
	}

	/** Convert XML element to object with JSON data.
	 * @param e XML element.
	 * @return object with JSON data created form element e.
	 */
	private Object getJsonObject(final Element e) {
		if (_jsNamespace.equals(e.getNamespaceURI())) {
			if (J_ARRAY.equals(e.getLocalName())) {
				return createArray(e);
			}
			if (J_MAP.equals(e.getLocalName())) {
				return createMap(e);
			} else if (J_ITEM.equals(e.getLocalName())) {
				Object o = createItem(e);
				return o != null && o instanceof JNull ? null : o;
			}
			//Incorrect XML node name with JSON namespace &{0}{: }
			throw new SRuntimeException(JSON.JSON013, e.getNodeName());
		}
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String key = toJsonName(e.getNodeName());
		int numOfItems = 0;
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.TEXT_NODE) {
				if (n.getNodeValue().trim().isEmpty()) {
					continue;
				}
				numOfItems++;
			} else if (n.getNodeType() == Node.ELEMENT_NODE) {
				numOfItems++;
			}
		}
		Map<String, Object> attrs = genAttrs(e);
		if (numOfItems == 0) {
			map.put(key, attrs);
			return map;
		} else {
			if (attrs != null) {
				map.put(key, attrs);
			}
			List<Object> list = new ArrayList<Object>();
			int i = 0;
			int len = nl.getLength();
			for (; i < len; i++) {
				Element ee;
				Node n = nl.item(i);
				if (n.getNodeType() == Node.TEXT_NODE
					|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
					String s = n.getNodeValue();
					if (s == null) {
						s = "";
					}
					while (i+1 < nl.getLength()
						&& ((n = nl.item(i+1)).getNodeType() == Node.TEXT_NODE
						|| n.getNodeType() == Node.CDATA_SECTION_NODE)) {
						s += n.getNodeValue();
						i++;
					}
					if (!(s = s.trim()).isEmpty()) {
						parseArrayList(s, 0, list);
					}
				} else if (n.getNodeType() == Node.ELEMENT_NODE) {
					ee = (Element) n;
					if (_jsNamespace.equals(ee.getNamespaceURI())) {
						Object o = getJsonObject(ee);
						map.put(key, o);
					} else {
						String key1 = toJsonName(ee.getNodeName());
						Object o = getJsonObject(ee);
						if (o instanceof Map) {
							Map m = (Map) o;
							if (attrs == null) {
								attrs = map;
								map.put(key, o);
							} else {
								for (Object x: m.entrySet()){
									Map.Entry entry = (Map.Entry) x;
									attrs.put((String) entry.getKey(),
										entry.getValue());
								}
							}
						} else if (o instanceof List) {
							map.put(key1, o);
						}
					}
				}
			}
			if (len == 1 && i == 1 && !map.containsKey(key)) {
				// no js:array and no child nodes (just an element)
				if (list.size() == 1) {
					map.put(key, list.get(0));
				} else {
					map.put(key, list);
				}
			} else if (list.size() > 0) {
				list.add(0, map);
				return list;
			}
			return map;
		}
	}

	/** Convert XML node to JSON object.
	 * @param node XML element or document with JSON data.
	 * @return JSON object created form argument node.
	 */
	public final static Object toJson(final Node node) {
		Element e = node instanceof Document
			? ((Document) node).getDocumentElement() : (Element) node;
		TestJson_FromXml x = new TestJson_FromXml();
		if (XDConstants.JSON_NS_URI_W3C.equals(e.getNamespaceURI())) {
			x._jsNamespace = XDConstants.JSON_NS_URI_W3C;
			x._isW3C = true;
		} else {
			x._jsNamespace = XDConstants.JSON_NS_URI_XD;
			x._isW3C = false;
		}
		return x.getJsonObject(e);
	}
}