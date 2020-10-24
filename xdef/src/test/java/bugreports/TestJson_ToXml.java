package bugreports;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.impl.compile.CompileJsonXdef;
import org.xdef.impl.xml.KNamespace;
import org.xdef.json.JNull;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;

/** Conversion of JSON to XML (X-definition form,or W3C form).
 * @author Vaclav Trojan
 */
class TestJson_ToXml extends TestJson_Util{
	/** Prefix of JSON namespace. */
	public String _jsPrefix = XDConstants.JSON_NS_PREFIX;
	/** JSON namespace. */
	public String _jsNamespace = XDConstants.JSON_NS_URI_XD;
	/** Document used to create X-definition. */
	private Document _doc;
	/** Stack of namespace URI. */
	private final KNamespace _ns = new KNamespace();

	/** Create instance of JsonToXml. */
	TestJson_ToXml() {super();}

////////////////////////////////////////////////////////////////////////////////
// Common private methods
////////////////////////////////////////////////////////////////////////////////

	/** Check if the object from attribute is an JSON object.
	 * @param val object to be checked.
	 * @return true if it is valid JSON object.
	 */
	private static boolean isJsonObject(final Object val) {
		return val == null || val instanceof List || val instanceof Map
			|| val instanceof Number || val instanceof Boolean
			|| val instanceof String || val instanceof JNull;
	}

	/** Create string with JSON simple value representation.
	 * @param val JSON simple value.
	 * @param isAttr true if is value of Attribute or false if of an Element.
	 * @return created string.
	 */
	private static String genSimpleValueToXmlString(final Object val,
		final boolean isAttr) {
		return val == null ? "null"
			: val instanceof String ? jstringToXML((String) val, isAttr)
			: val.toString();
	}

	/** Create and append new element and push context.
	 * @param namespace name space URI.
	 * @param tagname tag name of element.
	 * @return created element.
	 */
	private Element createElement(final String namespace,
		final String tagname) {
		String u;
		String prefix = getNamePrefix(tagname);
		u = namespace == null ?	_ns.getNamespaceURI(prefix) : namespace;
		Element e = u != null ? _doc.createElementNS(u, tagname)
			: _doc.createElement(replaceColonInXMLName(tagname));
		if (u != null && _ns.getNamespaceURI(prefix) == null) {
			_ns.setPrefix(prefix, u);
		}
		return e;
	}

	/** Append to node the element with JSON name space.
	 * @param n node where to append new element.
	 * @param name local name of element.
	 * @return created element.
	 */
	private Element appendJSONElem(final Node n, final String name) {
		return appendElem(n, _jsNamespace, _jsPrefix + ":" + name);
	}

////////////////////////////////////////////////////////////////////////////////
// Conversion to XML according X-definition recomendation
////////////////////////////////////////////////////////////////////////////////

	/** Check if the argument is a simple value. Simple value is null,
	 * number, boolean, string or JValue with object which is simple value.
	 * @param val Object to be tested.
	 * @return true if the argument is a simple value.
	 */
	private static boolean isSimpleValue(final Object val) {
		Object o;
		return val == null || val instanceof Number || val instanceof Boolean
			|| val instanceof String || val instanceof CompileJsonXdef.JValue
			&& ((o=((CompileJsonXdef.JValue) val).getValue()) == null || o instanceof Number
				|| o instanceof Boolean || o instanceof String);
	}

	/** Get prefix of XML name.
	 * @param s XML name.
	 * @return prefix of XML name.
	 */
	private static String getNamePrefix(final String s) {
		int i = s.indexOf(':');
		return (i >= 0) ? s.substring(0, i) : "";
	}

	/** Replace colon in XML name with "_x3a_".
	 * @param s raw XML name.
	 * @return name with colon replaced by "_x3a_".
	 */
	private static String replaceColonInXMLName(final String s) {
		int i = s.indexOf(':');
		return i >= 0 ? s.substring(0, i) + "_x3a_" + s.substring(i + 1) : s;
	}

	/** Generate XML form of string,
	 * @param val Object with value.
	 * @return XML form of string from the argument val,
	 */

	/** Append text with a value to element.
	 * @param e element where to add value.
	 * @param val value to be added as text.
	 */
	private void addValueAsText(final Element e, final Object val) {
		e.appendChild(_doc.createTextNode(genSimpleValueToXmlString(val, false)));
	}

	/** Set attribute to element,
	 * @param e element where to set attribute.
	 * @param namespace namespace URI of attribute.
	 * @param name name of attribute {if there is colon and namespace is null
	 * then replace colon with "_x3a_"}
	 * @param val string with value of attribute.
	 */
	private static void setAttr(final Element e,
		final String namespace,
		final String name,
		final String val) {
		if (namespace == null) {
			e.setAttribute(replaceColonInXMLName(name), val);
		} else {
			e.setAttributeNS(namespace, name, val);
		}
	}

	private void setAttrs(final Element e, final Map<String, String> attrs) {
		for (Map.Entry<String, String> entry: attrs.entrySet()) {
			String n = entry.getKey();
			String v = entry.getValue();
			String u = _ns.getNamespaceURI(getNamePrefix(n));
			e.setAttributeNS(u, n, v);
		}
	}

	/** Create and append new element and push context.
	 * @param n node to which new element will be appended.
	 * @param namespace name space URI.
	 * @param tagname tag name of element.
	 * @return created element.
	 */
	private Element appendElem(final Node n,
		final String namespace,
		final String tagname) {
		Element e = createElement(namespace, tagname);
		_ns.pushContext();
		n.appendChild(e);
		return e;
	}

	/** Create child element with text value to node.
	 * @param node node where to create element.
	 * @param val value which will be represented as value of created element.
	 */
	private void addValueToNodeXD(final Node node, final Object val) {
		Element e = (Element) node;
		Node n = e.getLastChild();
		String t = genSimpleValueToXmlString(val, false);
		if (n == null || n.getNodeType() != Node.TEXT_NODE) {
			e.appendChild(_doc.createTextNode(t));
		} else {
			String s = n.getNodeValue();
			n.setNodeValue(s + " " + t);
		}
	}

	/** Append array of JSON values to node.
	 * @param array list with array of values.
	 * @param parent node where to append array.
	 */
	private void arrayToNodeXD(final List array, final Node parent,
		final boolean genArrayElement) {
		if (array.size() == 2 && array.get(0) instanceof Map
			&& isSimpleValue(array.get(1))) { // map and value
			Element e = mapToXmlXD((Map) array.get(0), parent);
			addValueAsText(e, array.get(1));
			return;
		}
		boolean notAllSimple = false;
		for (Object x: array) {
			if (!isSimpleValue(x)) {
				notAllSimple = true;
				break;
			}
		}
		Element e;
		if (genArrayElement | notAllSimple
			|| parent.getNodeType() != Node.ELEMENT_NODE) {
			e = appendJSONElem(parent, J_ARRAY);
			if (array.isEmpty()) {
				_ns.popContext();
				return;
			}
		} else {
			e = (Element) parent;
			if (array.isEmpty()) {
				return;
			}
		}
		for (Object x: array) {
			if (x == null) {
				addValueToNodeXD(e, null);
			} else if (x instanceof Map) {
				mapToXmlXD((Map) x, e);
			} else if (x instanceof List) {
				arrayToNodeXD((List)x,  e, notAllSimple);
			} else {
				addValueToNodeXD(e, x);
			}
		}
		if (e != parent) {
			_ns.popContext();
		}
	}

	/** Create named item,
	 * @param rawName raw name (JSON string).
	 * @param val Object to be created as named item.
	 * @param parent parent node where the item will be appended.
	 * @return created element.
	 */
	private Element namedItemToXmlXD(final String rawName,
		final Object val,
		final Node parent) {
		String name = toXmlName(rawName);
		String namespace = _ns.getNamespaceURI(getNamePrefix(name));
		if (val == null) {
			Element e = appendElem(parent, namespace, name);
			_ns.popContext();
			return e;
		} else if (val instanceof Map) {
			Map map = (Map) val;
			if (map.isEmpty()) {
				Element e = appendElem(parent, namespace, name);
				appendJSONElem(e, J_MAP);
				_ns.popContext();
				_ns.popContext(); // appended element js:map
				return e;
			}
			String prefix = getNamePrefix(name);
			String uri = (String) map.get(
				prefix.length() > 0 ? "xmlns:" + prefix : "xmlns");
			if (uri == null) {
				uri = namespace;
			}
			Map<String, String> attrs = new LinkedHashMap<String, String>();
			Map<String, Object> items = new LinkedHashMap<String, Object>();
			for (Object key: map.keySet()) {
				Object o = map.get(key);
				String name1 = toXmlName((String) key);
				if (o == null) {
					attrs.put(name1, "null");
				} else if (isSimpleValue(o)) {
					String s;
					if ("xmlns".equals(name1)) {
						_ns.setPrefix("", s = o.toString());
					} else if (name1.startsWith("xmlns:")) {
						_ns.setPrefix(name1.substring(6), s = o.toString());
					} else {
						s = genSimpleValueToXmlString(o, true);
					}
					attrs.put(name1, s);
				} else {
					items.put(name1, o);
				}
			}
			Element e;
			if (items.isEmpty()) {
				e = appendElem(parent, uri, name);
				setAttrs(e, attrs);
			} else {
				Iterator<Map.Entry<String,Object>> it = items.entrySet().iterator();
				e = appendElem(parent, uri, name);
				setAttrs(e, attrs);
				if (items.size() == 1) {
					Map.Entry<String, Object> entry = it.next();
					String n = entry.getKey();
					Object o = entry.getValue();
					namedItemToXmlXD(n, o, e);
				} else {
					Element ee = appendJSONElem(e, J_MAP);
					while (it.hasNext()) {
						Map.Entry<String, Object> entry = it.next();
						String n = entry.getKey();
						Object o = entry.getValue();
						namedItemToXmlXD(n, o, ee);
					}
					_ns.popContext();
				}
			}
			_ns.popContext();
			return e;
		} else if (val instanceof List) {
			Element e = appendElem(parent, namespace, name);
			arrayToNodeXD((List) val, e, true);
			_ns.popContext();
			return e;
		} else {
			Element e = appendElem(parent, namespace, name);
			addValueAsText(e, val);
			_ns.popContext();
			return e;
		}
	}

	/** Append map with JSON tuples to node.
	 * @param map map with JSON tuples.
	 * @param parent node where to append map.
	 */
	private Element mapToXmlXD(final Map map, final Node parent) {
		int size = map.size();
		if (size == 0) {
			Element e = appendJSONElem(parent, J_MAP);
			_ns.popContext();
			return e;
		} else if (size == 1) {
			String key = (String) map.keySet().iterator().next();
			return namedItemToXmlXD(key, map.get(key), parent);
		} else {
			Element e = appendJSONElem(parent, J_MAP);
			for (Object key: map.keySet()) {
				Object val = map.get(key);
				if (isSimpleValue(val)) {
					String name = toXmlName((String) key);
					String namespace = _ns.getNamespaceURI(getNamePrefix(name));
					setAttr(e, namespace, name, genSimpleValueToXmlString(val, true));
				} else {
					namedItemToXmlXD((String) key, val, e);
				}
			}
			_ns.popContext();
			return e;
		}
	}

	/** Convert object with JSON data to XML element.
	 * @param json object with JSON data.
	 * @return XML element.
	 */
	final static Element toXmlXD(final Object json) {
		if (isJsonObject(json)) {
			TestJson_ToXml x = new TestJson_ToXml();
			x._jsNamespace = XDConstants.JSON_NS_URI_XD;
			x._jsPrefix = "js";
			x._doc = KXmlUtils.newDocument();
			if (json == null || json instanceof String || json instanceof Boolean
				|| json instanceof Number) {
				x.genValueW3C(json, x._doc); // same as W3C, but with XD namespace!
			} else if (json instanceof Map) {
				x.mapToXmlXD((Map) json, x._doc);
			} else if (json instanceof List) {
				x.arrayToNodeXD((List) json, x._doc, true);
			}
			return x._doc.getDocumentElement();
		}
		throw new SRuntimeException(JSON.JSON011, json);//Not JSON object&{0}
	}

////////////////////////////////////////////////////////////////////////////////
// Conversion to XML according W3C recomendation
////////////////////////////////////////////////////////////////////////////////

	/** JSON map. */
	public static final String J_MAP = "map";
	/** JSON array. */
	public static final String J_ARRAY = "array";
	/** JSON any item with JSON value (XDEF mode), */
	public static final String J_ITEM = "item";
	/** JSON value attribute name. */
	public static final String J_VALUEATTR = "value";
	/** JSON map key attribute name. */
	public static final String J_KEYATTR = "key";


	/** Create element with the namespace  */
	private Element genJElement(final String name) {
		return _jsPrefix.isEmpty() ? _doc.createElementNS(_jsNamespace, name)
			: _doc.createElementNS(_jsNamespace, _jsPrefix + ':' + name);
	}

	/** Create element with JSON array in the W3C format.
	 * @param array the array to be used for creation.
	 * @return created element with map.
	 */
	private Element genArrayW3C(final List array) {
		Element e = genJElement(J_ARRAY);
		for (Object val: array) {
			genValueW3C(val, e);
		}
		return e;
	}

	/** Create element with JSON map in the W3C format.
	 * @param map the map to be used for creation.
	 * @return created element with map.
	 */
	private Element genMapW3C(final Map map) {
		Element e = genJElement(J_MAP);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Element ee = genValueW3C(entry.getValue(), e);
			String key = jstringToSource((String) entry.getKey());
			ee.setAttribute(J_KEYATTR, key);
		}
		return e;
	}

	/** Create element with value in W3C format.
	 * @param val the value to be used for creation.
	 * @param parent parent node where the element should be added.
	 * @return created element.
	 */
	private Element genValueW3C(final Object val, final Node parent) {
		Element e;
		if (val == null) {
			e = genJElement(J_ITEM);
			e.setAttribute(J_VALUEATTR, genSimpleValueToXmlString(val, true));
		} else if (val instanceof Map) {
			Map m = (Map) val;
			e = genMapW3C((Map) val);
		} else if (val instanceof List) {
			e = genArrayW3C((List) val);
		} else {
			e = genJElement(J_ITEM);
			e.setAttribute(J_VALUEATTR, genSimpleValueToXmlString(val, true));
		}
		parent.appendChild(e);
		return e;
	}

	/** Create XML from JSON object according to W3C recommendation.
	 * @param json object with JSON data.
	 * @return XML element created from JSON data.
	 */
	final static Element toXmlW3C(final Object json) {
		if (isJsonObject(json)) {
			TestJson_ToXml x = new TestJson_ToXml();
			x._jsNamespace = XDConstants.JSON_NS_URI_W3C;
			x._jsPrefix = "";
			return x.genValueW3C(json, x._doc = KXmlUtils.newDocument());
		}
		throw new SRuntimeException(JSON.JSON011, json);//Not JSON object&{0}
	}
}