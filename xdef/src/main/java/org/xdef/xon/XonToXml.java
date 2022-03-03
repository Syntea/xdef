package org.xdef.xon;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.impl.xml.KNamespace;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_ITEM;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALUEATTR;
import static org.xdef.xon.XonTools.genXMLValue;
import static org.xdef.xon.XonTools.isSimpleValue;
import static org.xdef.xon.XonTools.jstringToXML;
import static org.xdef.xon.XonTools.replaceColonInXMLName;
import static org.xdef.xon.XonTools.toXmlName;

/** Conversion of XON/JSON to XML
 * @author Vaclav Trojan
 */
class XonToXml extends XonTools implements XonNames {
	/** Prefix of XON/JSON namespace. */
	private String _xPrefix;
	/** XON/JSON namespace. */
	private String _xNamespace;
	/** Stack of namespace URI. */
	KNamespace _ns;
	/** Document used to create X-definition. */
	Document _doc;

	private XonToXml() {super();}

	/** Create element with given name and XON/JSON namespace and prefix.
	 * @param name name of item.
	 * @return element with given name and XON/JSON namespace and prefix..
	 */
	private Element genJElement(final String name) {
		if (_xPrefix.isEmpty()) {
			return _doc.createElementNS(_xNamespace, name);
		} else {
			return _doc.createElementNS(_xNamespace, _xPrefix + ':' + name);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// XON/JSON to XML (X-detinition format)
////////////////////////////////////////////////////////////////////////////////

	/** Create and append new element and push context.
	 * @param n node to which new element will be appended.
	 * @param namespace name space URI.
	 * @param tagname tag name of element.
	 * @return created element.
	 */
	private Element addElem(final Node n,
		final String namespace,
		final String tagname) {
		Element e = namespace != null
			? _doc.createElementNS(namespace, tagname)
			: _doc.createElement(replaceColonInXMLName(tagname));
		_ns.pushContext();
		n.appendChild(e);
		return e;
	}

	/** Append to node the element with XON/JSON name space.
	 * @param n node where to append new element.
	 * @param name local name of element.
	 * @return created element.
	 */
	private Element addXonElem(final Node n, final String name) {
		Element e = genJElement(name);
		_ns.pushContext();
		_ns.setPrefix(_xPrefix, _xNamespace);
		n.appendChild(e);
		return e;
	}

	/** Get prefix of XML name.
	 * @param s XML name.
	 * @return prefix of XML name.
	 */
	private static String getNamePrefix(final String s) {
		int i = s.indexOf(':');
		return (i >= 0) ? s.substring(0, i) : "";
	}

	/** Set attribute to element,
	 * @param e element where to set attribute.
	 * @param name name of attribute {if there is colon and namespace is null
	 * then replace colon with "_x3a_"}
	 * @param s string with value of attribute.
	 */
	private void setAttr(final Element e,
		final String name,
		final Object v) {
		String u;
		String s;
		if (name.startsWith("xmlns")) {
			s = v == null ?
				XMLConstants.NULL_NS_URI : jstringToXML(v, 1);
			_ns.setPrefix("xmlns".equals(name) ? "" : name.substring(6), s);
			u = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else {
			s = jstringToXML(v, 1);
			u = getNamePrefix(name);
			u = u.isEmpty() ? null : _ns.getNamespaceURI(u);
		}
		e.setAttributeNS(u, name, s);
	}

	/** Append text with a value to element.
	 * @param e element where to add value.
	 * @param val value to be added as text.
	 */
	private void addValueAsText(final Element e, final Object val) {
		e.appendChild(_doc.createTextNode(jstringToXML(val, 0)));
	}

	/** Add items from array to element starting with given position.
	 * @param elem the element where to add items.
	 * @param array the array
	 * @param pos starting position.
	 */
	private void addArrayItems(final Element elem,
		final List array,
		final int pos) {
		int len = array.size();
		for (int i = pos; i < len; i++) {
			Object x = array.get(i);
			if (x instanceof Map) {
				Map m = (Map) x;
				Map.Entry en;
				if (m.size() == 1 && isSimpleValue((en=(Map.Entry) m.entrySet()
						.iterator().next()).getValue())) {
					Element e = addXonElem(elem, X_MAP);
					setAttr(e, toXmlName((String)en.getKey()), en.getValue());
					_ns.popContext();
				} else {
					addMapToXmlXD(m, elem, false);
				}
			} else {
				String text;
				if (x instanceof List) {
					List list = (List) x;
					text = genTextFromItem(list, 0);
					if (text == null) {
						Element ee = addXonElem(elem, X_ARRAY);
						addArrayItems(ee, list, 0);
						_ns.popContext();
						continue;
					}
				} else { // simpleValue or simple array
					if (i + 1 == len
						|| genTextFromItem(array.get(i+1), 2) == null) {
						addValueAsText((Element) elem, x);
						continue;
					} else {
						text = jstringToXML(x, 2);
					}
				}
				StringBuilder sb = new StringBuilder("[ ").append(text);
				for (i = i + 1; i < len; i++) {
					x = array.get(i);
					text = genTextFromItem(x, 2);
					if (text == null) {
						i--;
						break;
					}
					sb.append(", ").append(text);
				}
				sb.append(" ]");
				elem.appendChild(_doc.createTextNode(sb.toString()));
			}
		}
	}

	/** If all items of the array are simple values or arrays with simple values
	 * return the string with the array. Otherwise, return null.
	 * @param list the array to be converted.
	 * @param mode flag if to generate it to an attribute, text node or JList.
	 * @return the string with array representation or null.
	 */
	private String genTextFromItem(final Object val, final int mode) {
		if (isSimpleValue(val)) {
			return jstringToXML(val, mode);
		} else if (val instanceof List) {
			List list = (List) val;
			if (list.isEmpty()) {
				return "[]";  // empty array
			}
			StringBuilder sb = new StringBuilder("[ ");
			for (Object x: list) {
				if (isSimpleValue(x)) {
					if (sb.length() > 2) {
						sb.append(", "); // separator
					}
					if (x == null) {
						sb.append("null");
					} else if (x instanceof String) {
						sb.append(jstringToXML(x, 2));
					} else {
						sb.append(x.toString());
					}
				} else if (x instanceof List) {
					String s = genTextFromItem((List) x, 2);
					if (s == null) {
						return null; // not converible as string
					}
					if (sb.length() > 2) {
						sb.append(", "); // separator
					}
					sb.append(s);
				} else {
					return null;  // not converible as string
				}
			}
			return sb + " ]";
		}
		return null;
	}

	/** Add the element created from an array.
	 * @param array array from which element will be created.
	 * @param elem element to be added.
	 */
	private void addElementFromArray(final List array, final Element elem) {
		Element e;
		if (array.isEmpty()) {
			elem.appendChild(_doc.createTextNode("[]")); // empty array
			return;
		}
		int len = array.size();
		if (array.get(0) instanceof Map) {
			Map m = (Map) array.get(0);
			if (len == 1) {
				if (m.isEmpty()) {
					//this is special case: the map is not interporeted as attrs
					// so it will be an empty element
					return;
				}
				e = addXonElem(elem, X_ARRAY);
				addArrayItems(e, array, 0);
				_ns.popContext();
				return;
			}
			Map mm = null;
			if (len > 1 && array.get(1) instanceof Map) {
				if ((mm = (Map) array.get(1)).size() != 1) {
					addArrayItems(elem, array, 0);
					return;
				}
				if (len == 2) {
					Object x = mm.values().iterator().next();
					if (isSimpleValue(x)) {
						addMapToXmlXD(m, elem, false);
						addMapToXmlXD(mm, elem, true);
						return;
					}
					if (x instanceof List) {
						Map y = null;
						if (!((List) x).isEmpty()
							&& ((List) x).get(0) instanceof Map
							&& (y = (Map) ((List) x).get(0)).size() != 1) {
							if (!y.isEmpty()) {
								boolean allSimple = true;
								for (Object z: y.values()) {
									if (!isSimpleValue(z)) {
										allSimple = false;
									}
								}
								if (allSimple) {
									addArrayItems(elem, array, 0);
									return;
								}
							}
						}
					}
				}
			}
			boolean genMap = false;
			for (Object x: m.entrySet()) {
				Map.Entry xe = (Map.Entry) x;
				String key = (String) xe.getKey();
				Object val = xe.getValue();
				if (isSimpleValue(val)) {
					String ns;
					String s;
					if ("xmlns".equals(key)||key.startsWith("xmlns:")) {
						ns = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
						s = val == null ? XMLConstants.NULL_NS_URI
							: jstringToXML(val, 1);
						_ns.setPrefix("xmlns".equals(key)
							? "" : key.substring(6), s);
						elem.setAttributeNS(ns, key, s);
					} else {
						key = toXmlName(key);
						s = jstringToXML(val, 1);
						if (getNamePrefix(key).isEmpty()) {
							elem.setAttribute(key, s);
						} else {
							String namespace =
							   _ns.getNamespaceURI(getNamePrefix(key));
							ns = getElementNamespace(m, key, namespace);
							elem.setAttributeNS(ns, key, s);
						}
					}
				} else {
					genMap = true;
				}
			}
			Element ee = elem;
			// if in the map are not simple items, so generate the map element
			// with those items
			if (genMap) {
				if (m.size() != 1) {
					ee = addXonElem(elem, X_MAP);
				}
				for (Object x: m.entrySet()) {
					Map.Entry xe = (Map.Entry) x;
					String key = (String) xe.getKey();
					Object val = xe.getValue();
					if (!isSimpleValue(val)) {
						namedItemToXmlXD(toXmlName(key), val, ee);
					}
				}
			}
			if (ee != elem) {
				_ns.popContext();
			}
			if (len == 1) {
				return;
			}
			if (array.size() >= 1 && (array.get(1) instanceof Map)) {
				mm = (Map) array.get(1);
				if (mm.size() == 1) { // it is element
					if (array.size() == 1) { // no oter items of array
						// create element and return
						Map.Entry entry =
							(Map.Entry) m.entrySet().iterator().next();
						String name = toXmlName((String) entry.getKey());
						String namespace =
							_ns.getNamespaceURI(getNamePrefix(name));
						e = addElem(elem, namespace, name);
						_ns.popContext();
						return;
					} else {
						//add to element this map and remaining items from array
						addArrayItems(elem, array, 1);
					}
				} else {
					if (m.isEmpty()) {
						// if map is empty nothing was generated yet, so add map
						addXonElem(elem, X_MAP);
						_ns.popContext();
					}
					for (Object x: mm.entrySet()) {
						Map.Entry xe = (Map.Entry) x;
						String key = (String) xe.getKey();
						Object val = xe.getValue();
						namedItemToXmlXD(key, val, elem);
					}
					if (len > 1) {
						// add remaining items
						addArrayItems(elem, array, 1);
					}
					return;
				}
				return;
			} else {
				addArrayItems(elem, array, 1);
				return;
			}
		}
		if (array.size() <= 1) {
			String text = genTextFromItem(array, 0);
			if (text != null) {
				elem.appendChild(_doc.createTextNode(text));
				return;
			}
		}
		addArrayItems(elem, array, 0);
	}

	/** Get namespace of element which will be created.
	 * @param map the map with attributes.
	 * @param name name of element
	 * @param namespace actual namespace from namespace context.
	 * @return the actual namespace.
	 */
	private String getElementNamespace(final Map map,
		final String name,
		final String namespace) {
		String prefix = getNamePrefix(name);
		String uri =
			(String) map.get(prefix.length() > 0 ? "xmlns:" + prefix : "xmlns");
		return (uri == null) ? namespace : uri;
	}

	/** Create named item,
	 * @param name name (XON/JSON name converted to XML name).
	 * @param val Object to be created as named item.
	 * @param parent parent node where the item will be appended.
	 * @return created element.
	 */
	private Element namedItemToXmlXD(final String name,
		final Object val,
		final Node parent) {
		String namespace = _ns.getNamespaceURI(getNamePrefix(name));
		Element e;
		if (val instanceof Map) {
			Map map = (Map) val;
			if (map.isEmpty()) {
				e = addElem(parent, namespace, name);
				addXonElem(e, X_MAP);
				_ns.popContext();
				_ns.popContext(); // appended element js:map
				return e;
			}
			namespace = getElementNamespace(map, name, namespace);
			e = addElem(parent, namespace, name);
			int numAttrs = 0;
			// first set xmlns attributes with namespaces
			for (Object x: map.entrySet()) {
				Map.Entry entry = (Map.Entry) x;
				String key = (String) entry.getKey();
				Object o = entry.getValue();
				if (isSimpleValue(o)) {
					numAttrs++;
					if (key.startsWith("xmlns")) {
						setAttr(e, key, o);
					}
				}
			}
			if (numAttrs > 0) { // set attrs
				// set other attributes
				for (Object x: map.entrySet()) {
					Map.Entry entry = (Map.Entry) x;
					String key = (String) entry.getKey();
					Object v = entry.getValue();
					if (!key.startsWith("xmlns") && isSimpleValue(v)) {
						setAttr(e, toXmlName(key), v);
					}
				}
			}
			if (numAttrs < map.size()) {
				Element ee = (map.size() - numAttrs > 1)
					? addXonElem(e, X_MAP) : e;
				for (Object x: map.entrySet()) {
					Map.Entry entry = (Map.Entry) x;
					String key = (String) entry.getKey();
					Object o = entry.getValue();
					if (!isSimpleValue(o)) {
						namedItemToXmlXD(toXmlName(key), o, ee);
					}
				}
				if (e != ee) {
					_ns.popContext();
				}
			}
			_ns.popContext();
			return e;
		} else if (val instanceof List) {
			List array = (List) val;
			namespace = _ns.getNamespaceURI(getNamePrefix(name));
			if (!array.isEmpty() && array.get(0) instanceof Map) {
				// if the first item is map and there is xmlns item
				// in the map set this namespace for the element
				namespace = getElementNamespace((Map) array.get(0),
					name, namespace);
			}
			e = addElem(parent, namespace, name);
			if (val instanceof List && ((List) val).size() == 1
				&& isSimpleValue(((List) val).get(0))) {
				addValueAsText(e, ((List) val).get(0));
			} else {
				addElementFromArray(array, e);
			}
			_ns.popContext();
			return e;
		} else {
			e = addXonElem(parent, X_MAP);
			setAttr(e, name, val);
			_ns.popContext();
			return e;
		}
	}

	/** Append map with XON/JSON tuples to node.
	 * @param map map with XON/JSON tuples.
	 * @param forceMap if true the Map element is generated.
	 * @param parent node where to append map.
	 */
	private Element addMapToXmlXD(final Map map,
		final Node parent,
		final boolean forceMap) {
		int size = map.size();
		if (size == 0) {
			Element e = addXonElem(parent, X_MAP);
			_ns.popContext();
			return e;
		} else if (!forceMap && size == 1) {
			String key = (String) map.keySet().iterator().next();
			Object o = map.get(key);
			if (!key.startsWith("xmlns") || !isSimpleValue(o)) {
				key = toXmlName(key);
			}
			return namedItemToXmlXD(key, o, parent);
		} else {
			Element e = addXonElem(parent, X_MAP);
			boolean allXmlns = true;
			for (Object x: map.entrySet()) {
				Map.Entry entry = (Map.Entry) x;
				String name = (String) entry.getKey();
				Object y = entry.getValue();
				if (isSimpleValue(y) && name.startsWith("xmlns")) {
					setAttr(e, name, y); // set only xmlns attributes
				} else {
					allXmlns = false;
				}
			}
			if (!allXmlns) {
				for (Object x: map.entrySet()) {
					Map.Entry entry = (Map.Entry) x;
					Object y = entry.getValue();
					String name = (String) entry.getKey();
					if (isSimpleValue(y)) {
						if (!name.startsWith("xmlns")) {
							// other attributes
							setAttr(e, toXmlName(name), y);
						}
					} else {
						namedItemToXmlXD(toXmlName(name), y, e);
					}
				}
			}
			_ns.popContext();
			return e;
		}
	}

	/** Creates root element from map.
	 * @param map the map from which the root element is created.
	 */
	private void createRootElementFromMap(final Map map) {
		String name = null;
		String text = null;
		int numXmlns = 0;
		for (Object x: map.entrySet()) {
			Map.Entry en = (Map.Entry) x;
			String key = (String) en.getKey();
			Object val = en.getValue();
			if (key.startsWith("xmlns")) {
				numXmlns++;
			} else {
				if (name != null) { // more simple elements
					addMapToXmlXD(map, _doc, false); //create normally
					return;
				}
				name = key;
				text = genTextFromItem(val, 0);
				if (text == null) { // not simple text, create normally
					addMapToXmlXD(map, _doc, false);
					return;
				}
			}
		}
		if (name == null) {
			addXonElem(_doc, X_MAP); // empty map
		} else {
			name = toXmlName(name);
			String namespace = _ns.getNamespaceURI(getNamePrefix(name));
			namespace = getElementNamespace(map, name, namespace);
			Element e = addElem(_doc, namespace, name);
			if (numXmlns > 0) {
				for (Object x: map.entrySet()) {
					Map.Entry en = (Map.Entry) x;
					String key = (String) en.getKey();
					if (key.startsWith("xmlns")) {
						setAttr(e, key, jstringToXML(en.getValue(), 1));
					}
				}
			}
			e.appendChild(_doc.createTextNode(text));
		}
		_ns.popContext();
	}

	/** Create element created from XON/JSON (X-definition mode).
	 * @param xon XON/JSON object.
	 * @return created element.
	 */
	final static Element toXmlXD(final Object xon) {
		XonToXml x = new XonToXml();
		x._xPrefix = XDConstants.XON_NS_PREFIX;
		x._xNamespace = XDConstants.XON_NS_URI_XD;
		x._doc = KXmlUtils.newDocument();
		x._ns = new KNamespace();
		if (xon instanceof Map) {
			x.createRootElementFromMap((Map) xon);
		} else if (xon instanceof List) {
			Element elem = x.addXonElem(x._doc, X_ARRAY);
			x.addArrayItems(elem, (List) xon, 0);
			x._ns.popContext();
		} else if (isSimpleValue(xon)) {
			Element e = x.addXonElem(x._doc, X_ITEM);
			x.addValueAsText(e, xon);
			x._ns.popContext();
		} else {
			 //Not XON/JSON object&{0}
			throw new SRuntimeException(JSON.JSON011, xon.getClass());
		}
		return x._doc.getDocumentElement();
	}

////////////////////////////////////////////////////////////////////////////////
// XON/JSON to XML ("W" format)
////////////////////////////////////////////////////////////////////////////////

	/** Create element in "W" format from an object.
	 * @param val value.
	 * @param parent parent node where to add created element.
	 * @return element with value in "W" format.
	 */
	private Element genValueW(final Object val, final Node parent) {
		Element e;
		if (val == null) {
			e = genJElement(X_ITEM);
			e.setAttribute(X_VALUEATTR, "null");
		} else if (val instanceof Map) {
			e = genMapW((Map) val);
		} else if (val instanceof List) {
			e = genArrayW((List) val);
		} else {
			e = genJElement(X_ITEM);
			e.setAttribute(X_VALUEATTR, genXMLValue(val));
		}
		parent.appendChild(e);
		return e;
	}

	/** Create element in "W" format from array.
	 * @param array array to be created.
	 * @return element with array in "W" format.
	 */
	private Element genArrayW(final List array) {
		Element e = genJElement(X_ARRAY);
		for (Object val: array) {
			genValueW(val, e);
		}
		return e;
	}

	/** Create element with map in "W" format.
	 * @param map map to be created.
	 * @return element with map in "W" format..
	 */
	private Element genMapW(final Map map) {
		Element e = genJElement(X_MAP);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Map.Entry) it.next();
			Element ee = genValueW(en.getValue(), e);
			Object o = en.getKey();
			// NOTE in YAML it may be a byte array, otherwise it is String
			String key = o instanceof byte[]? new String((byte[])o) : (String)o;
			// convert key to XML name
			ee.setAttribute(X_KEYATTR, toXmlName(key));
		}
		return e;
	}

	/** Create XML in "W" format from XON/JSON object.
	 * @param xon object with XON/JSON data.
	 * @return XML element in "W" format created from JSON/XON data.
	 */
	final static Element toXmlW(final Object xon) {
		XonToXml x = new XonToXml();
		x._xNamespace = XDConstants.XON_NS_URI_W;
		x._xPrefix = "";
		return x.genValueW(xon, x._doc = KXmlUtils.newDocument());
	}
}