package org.xdef.impl.code;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDContainer;
import org.xdef.XDGPSPosition;
import org.xdef.XDNamedValue;
import org.xdef.XDPrice;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueID;
import static org.xdef.XDValueID.XD_ATTR;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_REPORT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_TEXT;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.CONTAINER;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;

/** The class DefContainer implements item with org.w3c.dom.NodeList value.
 * @author Vaclav Trojan
 */
public final class DefContainer extends XDValueAbstract implements XDContainer, XDValueID {
	/** The NodeList as value of this item. */
	private XDValue[] _array;
	/** The NodeList as value of this item. */
	private XDNamedValue[] _map;

	/** Creates a new empty instance of DefContainer */
	public DefContainer() {_array = new XDValue[0];}

	/** Creates a new instance of DefContainer.
	 * @param value Array of values.
	 */
	public DefContainer(final XDValue[] value) {init(value);}

	/** Creates a new instance of DefContainer.
	 * @param value the value.
	 */
	public DefContainer(final XDValue value) {initContainer(value);}

	/** Creates a new instance of DefContainer from the value of context
	 * @param value The source XDValue array.
	 * @param first The index of the first item.
	 * @param last The index of the last item.
	 */
	public DefContainer(final XDValue[] value, final int first, final int last){
		int length = last + 1 - first;
		if (length == 0) {
			_array = new XDValue[0];
		} else if (length > 1) {
			XDValue[] v = new XDValue[length];
			System.arraycopy(value, first, v, 0, length);
			init(v);
		} else { //length == 1
			XDValue v = value[first];
			if (v.getItemId() == XD_CONTAINER) {
				setValuesFromContext((XDContainer) v);
			} else {
				if (v.getItemId() == XD_NAMEDVALUE) {
					_map = new XDNamedValue[]{(XDNamedValue) v};
					_array = null;
				} else {
					_array = new XDValue[]{v};
				}
			}
		}
	}

	/** Creates a new instance of DefContainer
	 * @param nodeList The NodeList object to be set as value of this item.
	 */
	public DefContainer(final NodeList nodeList) {setNodeListValue(nodeList);}

	/** Creates a new instance of DefContainer
	 * @param obj The object from which the Container will be created.
	 */
	public DefContainer(final Object obj) {
		if (obj == null) {
			_array = null;
		} else if (obj instanceof XDValue) {
			initContainer((XDValue) obj);
		} else if (obj instanceof XDValue[]) {
			_array = (XDValue[]) obj;
		} else if (obj instanceof XDValue[]) {
			XDValue[] x = (XDValue[]) obj;
			_array = new XDValue[x.length];
			System.arraycopy(x, 0, _array, 0, x.length);
		} else if (obj instanceof String) {
			_array = new XDValue[]{new DefString((String) obj)};
		} else if (obj instanceof Element) {
			_array = new XDValue[]{new DefElement((Element) obj)};
		} else if (obj instanceof Attr) {
			_array = new XDValue[]{new DefAttr((Attr) obj)};
		} else if (obj instanceof CharacterData) {
			_array = new XDValue[] {
				new DefString(((CharacterData) obj).getData())};
		} else if (obj instanceof Properties) {
			_array = new XDValue[0];
			Properties props = (Properties) obj;
			int n = props.size();
			_map = new XDNamedValue[n];
			for (Map.Entry<Object,Object> e: props.entrySet()) {
				_map[--n] = new DefNamedValue((String) e.getKey(),
					new DefString((String) e.getValue()));
			}
		} else if (obj instanceof ArrayList) {
			setArrayListValue(obj);
		} else if (obj instanceof NodeList) {
			setNodeListValue((NodeList) obj);
		} else if (obj instanceof Long || obj instanceof Integer
			|| obj instanceof Short || obj instanceof Byte) {
			_array = new XDValue[] {
				new DefLong(((Number) obj).longValue())};
		} else if (obj instanceof CharacterData) {
			_array = new XDValue[] {
				new DefString(((CharacterData) obj).getData())};
		} else if (obj instanceof Boolean) {
			_array = new XDValue[] {new DefBoolean(((Boolean) obj))};
		} else if (obj instanceof Float || obj instanceof Double) {
			_array = new XDValue[] {
				new DefDouble(((Number) obj).doubleValue())};
		} else if (obj instanceof String[]) {
			String[] ss = (String[]) obj;
			_array = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_array[i] = new DefString(ss[i]);
			}
		} else if (obj instanceof Boolean[]) {
			Boolean[] ss = (Boolean[]) obj;
			_array = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_array[i] = new DefBoolean(ss[i]);
			}
		} else if (obj instanceof Integer[]) {
			Integer[] ss = (Integer[]) obj;
			_array = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_array[i] = new DefLong(ss[i]);
			}
		} else if (obj instanceof Long[]) {
			Long[] ss = (Long[]) obj;
			_array = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_array[i] = new DefLong(ss[i]);
			}
		} else if (obj instanceof Float[]) {
			Float[] ss = (Float[]) obj;
			_array = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_array[i] = new DefDouble(ss[i].doubleValue());
			}
		} else if (obj instanceof Double[]) {
			Double[] ss = (Double[]) obj;
			_array = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_array[i] = new DefDouble(ss[i]);
			}
		} else if (obj instanceof org.xdef.sys.Price) {
			_array = new XDValue[] {new XDPrice((org.xdef.sys.Price) obj)};
		} else if (obj instanceof org.xdef.sys.GPSPosition) {
			_array = new XDValue[] {
				new XDGPSPosition((org.xdef.sys.GPSPosition) obj)};
		} else if (obj instanceof java.net.URI) {
			_array = new XDValue[] {new DefURI((java.net.URI) obj)};
		} else {
			_array = new XDValue[]{new DefString("")};
			//Illegal argument in method: &{0}
			throw new SIllegalArgumentException(SYS.SYS084,
				"new " + obj.getClass() + "()");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//map interface
	////////////////////////////////////////////////////////////////////////////

	/** Set named item to the table of named items.
	 * @param name the name of item.
	 * @param val the value of item.
	 * @return if the named item not exists in the table of named items then
	 * return <i>null</i> or return the value which was replaced.
	 */
	@Override
	public final XDValue setXDNamedItem(final String name, final XDValue val) {
		XDNamedValue item = new DefNamedValue(name, val);
		int len = getXDNamedItemsNumber();
		if (len == 0) {
			_map = new XDNamedValue[]{item};
			return null;
		}
		for (int i = 0; i < len; i++) {
			if (name.equals(_map[i].getName())) {
				XDValue old = _map[i].getValue();
				_map[i] = new DefNamedValue(name, val);
				return old;
			}
		}
		XDNamedValue[] old = _map;
		int i = _map.length;
		_map = new XDNamedValue[i + 1];
		System.arraycopy(old, 0, _map, 0, i);
		_map[i] = new DefNamedValue(name, val);
		return null;
	}

	/** Check if named item exists in the table of named items.
	 * @param name the name of named item.
	 * @return <i>true</i> if and only if named item exists in the table.
	 */
	@Override
	public final boolean hasXDNamedItem(final String name) {return namedItemIndex(name)>=0;}

	/** Get named item from the table of named items.
	 * @param name the name of named item.
	 * @return if item not exists in table return <i>null</i> or
	 * return the named item from the table of named items.
	 */
	@Override
	public final XDNamedValue getXDNamedItem(final String name) {
		int i = namedItemIndex(name);
		return i >= 0 ? _map[i] : null;
	}

	/** Get value of named item from the table of named items.
	 * @param name the name of named item.
	 * @return if item not exists the return <i>null</i> or
	 * return the named item.
	 */
	@Override
	public final XDValue getXDNamedItemValue(final String name) {
		int i = namedItemIndex(name);
		return i >= 0 ? _map[i].getValue() : null;
	}

	/** Get value of named item from the table of named items as string.
	 * @param name the name of named item.
	 * @return if item not exists in table return <i>null</i> or
	 * return the value of named item as string.
	 */
	@Override
	public final String getXDNamedItemAsString(final String name) {
		XDValue val = getXDNamedItemValue(name);
		return val == null ? null : val.stringValue();
	}

	/** Remove named item from the table of named items.
	 * @param name the name of named item.
	 * @return the removed named item or <i>null</i>.
	 */
	@Override
	public final XDValue removeXDNamedItem(final String name) {
		int i = namedItemIndex(name);
		if (i < 0) {
			return null;
		}
		XDNamedValue[] old = _map;
		int newLength = old.length - 1;
		_map = new XDNamedValue[newLength];
		if (i > 0) {
			System.arraycopy(old, 0, _map, 0, i);
		}
		if (i < newLength) {
			System.arraycopy(old, i + 1, _map, i, newLength - i);
		}
		return old[i];
	}

	/** Get number of named items in the table of named items.
	 * @return The number of items.
	 */
	@Override
	public final int getXDNamedItemsNumber() {return _map == null ? 0 : _map.length;}

	/** Get array with named items in the table.
	 * @return array with named items.
	 */
	@Override
	public final XDNamedValue[] getXDNamedItems() {return _map != null ? _map : new XDNamedValue[0];}

	/** Get name of ith named item.
	 * @param index index of item.
	 * @return name of item.
	 */
	@Override
	public final String getXDNamedItemName(final int index) {
		return _map == null || index >= _map.length ? null : _map[index].getName();
	}

	/** Set named item to the table of named items.
	 * @param item the named item.
	 * @return if the named item not exists then return <i>null</i> or return
	 * the named item value which was replaced in the table of named items.
	 */
	@Override
	public final XDValue setXDNamedItem(final XDNamedValue item) {
		String key = item.getName();
		int i = namedItemIndex(key);
		if (i >= 0) {
			XDValue old = _map[i].getValue();
			_map[i] = item;
			return old;
		}
		if (_map == null || _map.length == 0) {
			_map = new XDNamedValue[]{item};
		} else {
			XDNamedValue[] old = _map;
			i = _map.length;
			_map = new XDNamedValue[i + 1];
			System.arraycopy(old, 0, _map, 0, i);
			_map[i] = item;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	// Private methods
	////////////////////////////////////////////////////////////////////////////

	private void initContainer(final XDValue value) {
		switch (value.getItemId()) {
			case XD_CONTAINER: setValuesFromContext((XDContainer) value); break;
			case XD_NAMEDVALUE: _map = new XDNamedValue[]{(XDNamedValue) value}; _array = null; break;
			default: _array = new XDValue[]{value};
		}
	}

	private void setValuesFromContext(final XDContainer d) {
		int len = d.getXDItemsNumber();
		if (len > 0) {
			_array = new XDValue[len];
			System.arraycopy(d.getXDItems(), 0, _array, 0, _array.length);
		} else {
			_array = len == 0 ? new XDValue[0] : null;
		}
		len = d.getXDNamedItemsNumber();
		if (len > 0) {
			_map = new XDNamedValue[len];
			System.arraycopy(d.getXDNamedItems(), 0, _map, 0, _map.length);
		}
	}

	private void init(final XDValue[] value) {
		if (value != null) {
			int numOfNamedItems = 0;
			for (XDValue xv: value) {
				if (xv.getItemId() == XD_NAMEDVALUE) {
					numOfNamedItems++;
					setXDNamedItem((XDNamedValue) xv);
				}
			}
			int len = value.length;
			int len1 = len - numOfNamedItems;
			_array = new XDValue[len1];
			if (len1 > 0 ) {
				if (numOfNamedItems == 0) {
					System.arraycopy(value, 0, _array, 0, len1);
				} else {
					for (int i = 0, j = 0; i < len; i++) {
						if (value[i].getItemId() != XD_NAMEDVALUE) {
							_array[j++] = value[i];
						}
					}
				}
			}
		} else {
			_array = null;
		}
	}

	private int namedItemIndex(final String name) {
		if (_map != null) {
			for (int i = 0; i < _map.length; i++) {
				if (name.equals(_map[i].getName())) {
					return i;
				}
			}
		}
		return -1;
	}

	private void setNodeListValue(final NodeList nodeList) {
		List<XDValue> ar = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			switch (node.getNodeType()) {
				case Node.ELEMENT_NODE: ar.add(new DefElement((Element) node)); continue;
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE: ar.add(new DefString(node.getNodeValue()));
			}
		}
		ar.toArray(_array = new  XDValue[ar.size()]);
	}

	private void setArrayListValue(final Object obj) {
		List<XDValue> ar = new ArrayList<>();
		for (int i = 0, size = ((List) obj).size(); i < size; i++) {
			Object item = ((List) obj).get(i);
			if (item instanceof Node) {
				Node n = (Node) item;
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					ar.add(new DefElement((Element) n));
				} else {
					ar.add(new DefString(n.getNodeValue()));
				}
			} else if (item instanceof String) {
				ar.add(new DefString((String) item));
			} else if (item instanceof Integer) {
				ar.add(new DefLong(((Integer) item).longValue()));
			} else if (item instanceof Long) {
				ar.add(new DefLong(((Long) item)));
			} else if (item instanceof Float) {
				ar.add(new DefDouble(((Float) item).doubleValue()));
			} else if (item instanceof Double) {
				ar.add(new DefDouble(((Double) item)));
			}
		}
		ar.toArray(_array = new  XDValue[ar.size()]);
	}

	/** Create element.
	 * @param nsMap map of namespaces.
	 * @param nsUri namespace URI.
	 * @param xmlName name of element.
	 * @return created element.
	 */
	private Element toElement(Map<String, String> nsMap,
		final String nsUri,
		final String xmlName) {
		String name = xmlName;
		String uri = nsUri;
		DefContainer c = this;
		if (name == null || name.length() == 0) {
			XDValue v;
			if (getXDNamedItemsNumber() == 1 &&	getXDItemsNumber() <= 1
				&& StringParser.chkXMLName(_map[0].getName(), (byte) 10)
				&& ((v = _map[0].getValue()) == null || v.isNull()
				|| v.getItemId() == XD_CONTAINER)) {
				c = v == null || v.isNull() ? new DefContainer()
					: (DefContainer) _map[0].getValue();
				name = _map[0].getName();
				if (getXDItemsNumber() == 0) {
					int ndx = name.indexOf(':');
					String prefix = ndx > 0 ? ":" + name.substring(0, ndx) : "";
					uri = c. hasXDNamedItem("xmlns"+prefix) ?
						c.getXDNamedItem("xmlns"+prefix).getValue().toString()
						: null;
					if (uri != null) {
						nsMap.put(ndx > 0 ? name.substring(0, ndx) : "", uri);
					} else {
						uri = nsMap.get(ndx > 0 ? name.substring(0, ndx) : "");
					}
				} else {
					return KXmlUtils.newDocument(
						null, name, null).getDocumentElement();
				}
			} else if (getXDItemsNumber() == 1 &&
				getXDItem(0).getItemId() == XD_ELEMENT) {
				return getXDItem(0).getElement();
			} else {
				name = "_";
			}
		}
		Element el = KXmlUtils.newDocument(uri,name,null).getDocumentElement();
		nsMap = new LinkedHashMap<>(nsMap);
		c.setAttrs(nsMap, el);
		c.setChildNodes(nsMap, el);
		return el;
	}

	/** Set attributes from element to this container.
	 * @param nsMap map with namespace URIs.
	 * @param e element with attributes.
	 */
	private void setAttrs(final Map<String,String> nsMap, final Element e) {
		if (_map != null) {
			for (XDNamedValue _map1 : _map) {
				XDValue val = _map1.getValue();
				//we nead to get null from STRING_VALUE
				if (val != null && !val.isNull()) {
					String t = _map1.getName();
					if (val.getItemId() == XD_CONTAINER) {
						XDContainer x = (XDContainer) val;
						e.appendChild(e.getOwnerDocument()
							.importNode(x.toElement(null, t),
								true));
					} else {
						String s = val.getItemId() == XD_STRING ?
							val.stringValue() : val.toString();
						e.setAttribute(t, s);
						if (t.startsWith("xmlns")) {
							if (t.length() == 5) {
								nsMap.put("", s);
							} else {
								nsMap.put(t.substring(6), s);
							}
						}
					}
				}
			}
		}
	}

	/** Add child nodes from element to this container.
	 * @param ns map with namespace URIs.
	 * @param e element with child nodes.
	 */
	private void setChildNodes(final Map<String, String> ns, final Element e) {
		if (_array != null && _array.length > 0) {
			Document doc = e.getOwnerDocument();
			for (XDValue item: _array) {
				if (item != null) {
					switch (item.getItemId()) {
						case XD_ELEMENT:
							Element ee = item.getElement();
							if (ee != null) {
								e.appendChild(doc.importNode(ee, true));
							}
							break;
						case XD_CONTAINER:
							{
								DefContainer x = ((DefContainer) item);
								e.appendChild(doc.importNode(
									x.toElement(ns, null, null), true));
							}
							break;
						case XD_NAMEDVALUE:
							{
								DefNamedValue x = (DefNamedValue) item;
								e.setAttribute(x.getName(), x.getValue().toString());
							}
							break;
						default:
							String s = item.stringValue();
							if (s != null && s.length() > 0) {
								e.appendChild(doc.createTextNode(s));
							}
					}
				}
			}
		}
	}

	/** Sorts this Container.
	 * @param key String with XPath expression or null (if null or empty string
	 * then for org.w3c.Node items it is used as a key the text value of
	 * an item). For items other then  org.w3c.Node objects this parameter is
	 * ignored.
	 * @param asc if true Container will be sorted ascendant, else descendant.
	 */
	private void sort1(final String key, final boolean asc) {
		int len;
		if ((len = _array != null ? _array.length : 0) <= 1) {
			return;
		}
		Object[] keys = new Object[len];
		for (int i = len - 1; i >= 0; i--) {
			switch(_array[i].getItemId()) {
				case XD_ELEMENT: {
					Element el = _array[i].getElement();
					if (el == null) {
						keys[i] = "";
					} else {
						if (key == null || key.length() == 0) {
							keys[i] = KXmlUtils.getTextValue(el);
						} else {
							DefContainer dc =
								new DefContainer(KXpathExpr.evaluate(el, key));
							if (dc.getXDItemsNumber() != 1) {
								keys[i] = "";
							} else {
								XDValue dv = dc.getXDItem(0);
								switch (dv.getItemId()) {
									case XD_ELEMENT:
									case XD_ATTR:
									case XD_TEXT:
									case XD_STRING: keys[i] = dc.getXDItem(0).stringValue(); break;
									case XD_DOUBLE:
									case XD_LONG:
									case XD_DECIMAL:
									case XD_BOOLEAN:
									case XD_DATETIME:
									case XD_REPORT: keys[i] = dv; break;
//									XD_BYTES, XX_ELEMENT, XX_TEXT,
//									XD_CONTAINER, XD_OBJECT, XD_INPUT,
//									XD_OUPUT, XD_EXCEPTION, XMLNODE_VALUE,
//									BYTE_VALUE, CHAR_VALUE ...
									default: keys[i] = "";
								}
							}
						}
					}
					break;
				}
				case XD_LONG:
					keys[i] = _array[i].longValue();
					break;
				case XD_BOOLEAN:
					keys[i] = _array[i].booleanValue() ? 1 : 0;
					break;
				default:
					keys[i] = _array[i].stringValue();
			} // case
		}
		sort2(0, _array.length - 1, keys, asc);
	}

	/** Partial recursive sort.
	 * @param low low index.
	 * @param high high index.
	 * @param keys array of keys to be sorted.
	 * @param asc if true sort ascending, otherwise descending.
	 */
	private void sort2(final int low,
		final int high,
		final Object[] keys,
		final boolean asc) {
		if (low >= high) {
			return;
		}
		Object first = keys[low];
		int i = low - 1;
		int j = high + 1;
		if (first instanceof String) {
			String x = (String) first;
			if (asc) {
				while (i < j) {
					i++;
					while (x.compareTo((String) keys[i]) > 0) {
						i++;
					}
					j--;
					while (x.compareTo((String) keys[j]) < 0) {
						j--;
					}
					if (i < j) { //swap item[i] and item[j]
						Object key = keys[i];
						keys[i] = keys[j];
						keys[j] = key;
						XDValue item = _array[i];
						_array[i] = _array[j];
						_array[j] = item;
					}
				}
			} else {
				while (i < j) {
					i++;
					while (x.compareTo((String) keys[i]) < 0) {
						i++;
					}
					j--;
					while (x.compareTo((String) keys[j]) > 0) {
						j--;
					}
					if (i < j) { //swap item[i] and item[j]
						Object key = keys[i];
						keys[i] = keys[j];
						keys[j] = key;
						XDValue item = _array[i];
						_array[i] = _array[j];
						_array[j] = item;
					}
				}
			}
		} else if (first instanceof Long) {
			Long x = (Long) first;
			if (asc) {
				while (i < j) {
					i++;
					while (x.compareTo((Long) keys[i]) > 0) {
						i++;
					}
					j--;
					while (x.compareTo((Long) keys[j]) < 0) {
						j--;
					}
					if (i < j) { //swap item[i] and item[j]
						Object key = keys[i];
						keys[i] = keys[j];
						keys[j] = key;
						XDValue item = _array[i];
						_array[i] = _array[j];
						_array[j] = item;
					}
				}
			} else {
				while (i < j) {
					i++;
					while (x.compareTo((Long) keys[i]) < 0) {
						i++;
					}
					j--;
					while (x.compareTo((Long) keys[j]) > 0) {
						j--;
					}
					if (i < j) { //swap item[i] and item[j]
						Object key = keys[i];
						keys[i] = keys[j];
						keys[j] = key;
						XDValue item = _array[i];
						_array[i] = _array[j];
						_array[j] = item;
					}
				}
			}
		} else if (first instanceof Double) {
			Double x = (Double) first;
			if (asc) {
				while (i < j) {
					i++;
					while (x.compareTo((Double) keys[i]) > 0) {
						i++;
					}
					j--;
					while (x.compareTo((Double) keys[j]) < 0) {
						j--;
					}
					if (i < j) { //swap item[i] and item[j]
						Object key = keys[i];
						keys[i] = keys[j];
						keys[j] = key;
						XDValue item = _array[i];
						_array[i] = _array[j];
						_array[j] = item;
					}
				}
			} else {
				while (i < j) {
					i++;
					while (x.compareTo((Double) keys[i]) < 0) {
						i++;
					}
					j--;
					while (x.compareTo((Double) keys[j]) > 0) {
						j--;
					}
					if (i < j) { //swap item[i] and item[j]
						Object key = keys[i];
						keys[i] = keys[j];
						keys[j] = key;
						XDValue item = _array[i];
						_array[i] = _array[j];
						_array[j] = item;
					}
				}
			}
		}
		sort2(low, j, keys, asc);
		sort2(j + 1, high, keys, asc);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Get the item from sequence at given index.
	 * @param index index of item.
	 * @return item at given index or return <i>null</i>.
	 */
	@Override
	public final XDValue getXDItem(final int index) {
		return index < 0 || _array == null || index >= _array.length ? null : _array[index];
	}

	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	@Override
	public final void addXDItem(final XDValue value) {
		if (_array == null) {
			_array = new XDValue[] {value};
		} else {
			insertXDItemBefore(_array.length, value);
		}
	}

	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	@Override
	public final void addXDItem(final String value) {
		XDValue x = new DefString(value);
		if (_array == null) {
			_array = new XDValue[] {x};
		} else {
			insertXDItemBefore(_array.length, x);
		}
	}

	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	@Override
	public final void addXDItem(final Element value) {
		XDValue x = new DefElement(value);
		if (_array == null) {
			_array = new XDValue[] {x};
		} else {
			insertXDItemBefore(_array.length, x);
		}
	}

	/** Set item at position given by index.
	 * @param index index of item item. If index is out of range of items this* method does nothing.
	 * @param value of item.
	 * @return original value or null;
	 */
	@Override
	public XDValue replaceXDItem(final int index, final XDValue value) {
		if (_array != null && index >= 0 && index < _array.length) {
			XDValue result = _array[index];
			_array[index] = value;
			return result;
		}
		return null;
	}

	/** Insert item before given index to the sequence.
	 * @param index index of required item.
	 * @param value item to be inserted.
	 */
	@Override
	public final void insertXDItemBefore(final int index, final XDValue value) {
		if (index < 0) {
			return;
		}
		if (_array == null) {
			if (index > 0) {
				return;
			}
			_array = new XDValue[] {value};
			return;
		}
		int ndx;
		if ((ndx = index) > _array.length) {
			ndx = _array.length;
		}
		int len = _array.length + 1;
		XDValue[] old = _array;
		_array = new XDValue[len];
		if (len == 1) {
			_array[0] = value;
			return;
		}
		if (ndx > 0) {
			System.arraycopy(old, 0, _array, 0, ndx);
		}
		if (ndx < len - 1) {
			System.arraycopy(old, ndx, _array, ndx + 1, len - ndx - 1);
		}
		_array[ndx] = value;
	}

	/** Remove item from the sequence at given index.
	 * @param index the index of item in the sequence which will be removed.
	 * @return removed value or null.
	 */
	@Override
	public final XDValue removeXDItem(final int index) {
		if (index < 0 || _array == null || index >= _array.length) {
			return null;
		}
		int len = _array.length - 1;
		XDValue result = _array[index];
		if (len == 0) {
			_array = new XDValue[0];
		} else {
			XDValue[] old = _array;
			_array = new XDValue[len];
			if (index > 0) {
				System.arraycopy(old, 0, _array, 0, index);
			}
			if (index < len) {
				System.arraycopy(old, index + 1, _array, index, len - index);
			}
		}
		return result;
	}

	/** Get number of items in the sequence.
	 * @return number of items in the sequence.
	 */
	@Override
	public final int getXDItemsNumber() {return _array != null ? _array.length : -1;}

	/** Get array of all items in the sequence.
	 * @return array of all items in the sequence.
	 */
	@Override
	public final XDValue[] getXDItems() {return _array;}

	/** Create new XDContainer with all elements from context.
	 * @return The new XDContainer with elements.
	 */
	@Override
	public final XDContainer getXDElements() {
		if (_array == null) {
			return new DefContainer();
		}
		List<XDValue> ar = new ArrayList<>();
		for (XDValue x: _array) {
			if (x.getItemId() == XD_ELEMENT) {
				ar.add(x);
			} else if (x.getItemId() == XD_CONTAINER) {
				ar.add(new DefElement(((XDContainer) x).toElement(null, "")));
			}
		}
		DefContainer result = new DefContainer();
		result._array = new XDValue[ar.size()];
		ar.toArray(result._array);
		return result;
	}

	/** Get the n-th element from Container or null.
	 * @param n The index of element.
	 * @return the n-th element from Container or null..
	 */
	@Override
	public final Element getXDElement(final int n) {
		if (_array == null) {
			return null;
		}
		if (n >= 0 && n < _array.length) {
			switch (_array[n].getItemId()) {
				case XD_ELEMENT: return _array[n].getElement();
				case XD_CONTAINER: return ((XDContainer)_array[n]).toElement(null, null);
			}
			Document doc = KXmlUtils.newDocument(null, "_", null);
			Element el = doc.getDocumentElement();
			setAttrs(new LinkedHashMap<>(), el);
			XDValue val = _array[n];
			String s = val == null || val.isNull() ? null :
				val.getItemId() == XD_STRING ?
				val.stringValue() : val.toString();
			if (s != null) {
				el.appendChild(doc.createTextNode(s));
			}
			return el;
		}
		return null;
	}

	/** Get all elements with given name from this Container.
	 * @param name name of element.
	 * @return new Container with elements.
	 */
	@Override
	public final XDContainer getXDElements(final String name) {return getXDElementsNS(null, name);}

	/** Get all elements with given name and namespace from Container.
	 * @param nsURI NameSpace URI.
	 * @param localName local name of element.
	 * @return new Container with all elements with given name and namespace.
	 */
	@Override
	public final XDContainer getXDElementsNS(final String nsURI, final String localName) {
		if (_array == null) {
			return new DefContainer();
		}
		List<XDValue> ar = new ArrayList<>();
		for (XDValue _array1 : _array) {
			if (_array1.getItemId() == XD_ELEMENT) {
				Element el = _array1.getElement();
				if (nsURI == null) {
					if (el.getNamespaceURI() != null) {
						continue;
					}
				} else if (!nsURI.equals(el.getNamespaceURI())) {
					continue;
				}
				String s = el.getNodeName();
				int n = s.indexOf(':');
				if (n >= 0) {
					s = s.substring(n + 1);
				}
				if (localName.equals(s)) {
					ar.add(_array1);
				}
			}
		}
		DefContainer result = new DefContainer();
		result._array = new XDValue[ar.size()];
		ar.toArray(result._array);
		return result;
	}

	/** Get all text nodes concatenated as a string.
	 * @return The string with all text nodes.
	 */
	@Override
	public final String getXDText() {
		StringBuilder sb = new StringBuilder();
		if (_array == null) {
			return null;
		}
		boolean wasItem = false;
		for (XDValue x: _array) {
			if (x.getItemId() == XD_STRING || x.getItemId() == XD_ATTR
//				|| x.getItemId() == XD_BOOLEAN || x.getItemId() == XD_LONG ||
//				|| x.getItemId() == XD_DOUBLE || x.getItemId() == XD_DATETIME ||
				|| x.getItemId() == XD_TEXT) {
				String s = x.stringValue();
				wasItem = true;
				if (s != null && s.length() > 0) {
					sb.append(s);
				}
			}

		}
		return wasItem ? sb.toString() : null;
	}

	/** Get string from n-th item from this Container. If the node does not
	 * exist or if it is not text then return the empty string.
	 * @param n The index of item.
	 * @return string from n-th item.
	 */
	@Override
	public final String getXDTextItem(final int n) {
		return n >= 0 && _array != null && n < _array.length &&
			(_array[n].getItemId() == XD_STRING || _array[n].getItemId() == XD_TEXT ||
			_array[n].getItemId() == XD_ATTR) ? _array[n].stringValue() : "";
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public final short getItemId() {return XD_CONTAINER;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return CONTAINER;}

	/** Check if the object is <i>null</i>.
	 * @return <i>true</i> if the object is <i>null</i> otherwise returns
	 * <i>false</i>.
	 */
	@Override
	public boolean isNull() { return _array == null && _map == null;}

	/** Check if the object is empty.
	 * @return <i>true</i> if the object is empty; otherwise returns
	 * <i>false</i>.
	 */
	@Override
	public boolean isEmpty() {
		return (_array == null || _array.length == 0) && (_map == null || _map.length == 0);
	}

	/** Get value as printable string. */
	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
/**
		if (_map != null) {
			for (XDNamedValue x: _map) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(x.toString());
			}
		}
		if (_array != null) {
			for (XDValue _array1 : _array) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(_array1.toString());
			}
		}
/**/
		if (_map != null) {
			for (XDNamedValue x: _map) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(x.toString());
			}
		}
		if (_array != null) {
			if (_array.length == 1) {
				return sb.toString() + _array[0];
			}
			for (XDValue _array1 : _array) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				String s = _array1.toString();
				if (s.contains(" ") || s.contains("'")) {
					s = s.replaceAll("'", "''");
					s = '\'' + s + '\'';
				}
				sb.append(s);
			}
		}
/**/
		return sb.toString();
	}

	/** Get boolean value of this object.
	 * @return boolean value of this object or <i>false</i>.
	 */
	@Override
	public final boolean booleanValue() {
		if (!isEmpty()) {
			if (_map == null && _array != null) {
				XDValue x;
				if (_array.length==1 && (x = _array[0])!=null) {
					if (x.isNull()) {
						return false;
					}
					switch (x.getItemId()) {
						case XD_BOOLEAN:
							return x.booleanValue();
						case XD_DOUBLE:
						case XD_LONG:
						case XD_DECIMAL:
							return x.longValue() != 0;
						case XD_STRING:
							return !x.stringValue().isEmpty();
						default:
							return true;
					}
				}
				for (XDValue v: _array) {
					if (v != null && !v.isNull()) {
						return true;
					}
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public final long longValue() {
		return (_array != null && _array.length == 1 && _array[0] != null)
			? _array[0].longValue() : 0;
	}

	@Override
	public final double doubleValue() {
		return (_array != null && _array.length == 1 && _array[0] != null)
			? _array[0].doubleValue() : Double.NaN;
	}

	@Override
	public BigDecimal decimalValue() {
		return (_array != null && _array.length == 1 && _array[0] != null)
			? _array[0].decimalValue() : null;
	}

	/** Get string value of this object.
	 * @return string value of this object.
	 */
	@Override
	public final String stringValue() {
		return toString();
	}

	/** Create element from this Container.
	 * @param nsUri of created element.
	 * @param xmlName name of created element.
	 * @return element created from this Container.
	 */
	@Override
	public final Element toElement(final String nsUri, final String xmlName) {
		return toElement(new LinkedHashMap<>(), nsUri, xmlName);
	}

	/** Get associated object.
	 * @return the associated object or null.
	 */
	@Override
	public Object getObject() {return isNull() ? null : this;}

	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	@Override
	public final XDValue cloneItem() {
		DefContainer result = new DefContainer();
		if (_array != null) {
			result._array = new XDValue[_array.length];
			for (int i = 0; i < _array.length; i++) {
				XDValue x = _array[i];
				if (x != null) {
					result._array[i] = _array[i].cloneItem();
				}
			}
		}
		if (_map != null) {
			result._map = new XDNamedValue[_map.length];
			for (int i = 0; i < _map.length; i++) {
				XDNamedValue x = _map[i];
				if (x != null) {
					result._map[i] = (XDNamedValue) _map[i].cloneItem();
				}
			}
		}
		return result;
	}

	/** Sorts this Container.
	 * If an item is an org.w3c.Node object then as a key it is used
	 * the text value of an item).
	 * @param asc if true Container will be sorted ascendant, else descendant.
	 * @return this Container sorted.
	 */
	@Override
	public final XDContainer sortXD(final boolean asc) {return sortXD(null, asc);}

	/** Sorts this Container.
	 * @param key String with XPath expression or null (if null or empty string
	 * then for org.w3c.Node items it is used as a key the text value of
	 * an item). For items other then  org.w3c.Node objects this parameter is
	 * ignored.
	 * @param asc if true Container will be sorted ascendant, else descendant.
	 * @return this Container sorted.
	 */
	@Override
	public final XDContainer sortXD(final String key, final boolean asc) {
		DefContainer dc = new DefContainer(this);
		sort1(key, asc);
		return dc;
	}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	@Override
	public final boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_CONTAINER) {
			return false;
		}
		XDContainer x = (XDContainer) arg;
		int len;
		if ((len = getXDNamedItemsNumber()) != x.getXDNamedItemsNumber()) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			XDNamedValue ni = _map[i];
			XDValue v = x.getXDNamedItemValue(ni.getName());
			if (!ni.getValue().equals(v)) {
				return false;
			}
		}
		if ((len = getXDItemsNumber()) != x.getXDItemsNumber()) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			XDValue v = getXDItem(i);
			if (v == null) {
				if (x.getXDItem(i) != null) {
					return false;
				}
			} else if (!v.equals(x.getXDItem(i))) {
				return false;
			}
		}
		return true;
	}
}