package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.LinkedHashMap;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDContainer;
import org.xdef.XDValueID;
import org.xdef.XDValueType;

/** The class DefContainer implements item with org.w3c.dom.NodeList value.
 * @author Vaclav Trojan
 */
public final class DefContainer extends XDValueAbstract
	implements XDContainer, XDValueID {
	/** The NodeList as value of this item. */
	private XDValue[] _value;
	/** The NodeList as value of this item. */
	private XDNamedValue[] _map;

	/** Creates a new empty instance of DefContext */
	public DefContainer() {_value = new XDValue[0];}

	/** Creates a new instance of DefContext.
	 * @param value Array of values.
	 */
	public DefContainer(final XDValue[] value) {init(value);}

	/** Creates a new instance of DefContext.
	 * @param value the value.
	 */
	public DefContainer(final XDValue value) {initContext(value);}

	private void initContext(final XDValue value) {
		switch (value.getItemId()) {
			case XD_CONTAINER: {
				setValuesFromContext((XDContainer) value);
				break;
			}
			case XD_NAMEDVALUE:
				_map = new XDNamedValue[]{(XDNamedValue) value};
				_value = new XDValue[0];
				break;
			default:
				_value = new XDValue[]{value};
				break;
		}
	}

	/** Creates a new instance of DefContext from the value of context
	 * @param value The source XDValue array.
	 * @param first The index of the first item.
	 * @param last The index of the last item.
	 */
	public DefContainer(final XDValue[] value, final int first, final int last) {
		int length = last + 1 - first;
		if (length == 0) {
			_value = new XDValue[0];
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
					_value = new XDValue[0];
				} else {
					_value = new XDValue[]{v};
				}
			}
		}
	}

	/** Creates a new instance of DefContext
	 * @param nodeList The NodeList object to be set as value of this item.
	 */
	public DefContainer(final NodeList nodeList) {setNodeListValue(nodeList);}

	/** Creates a new instance of DefContext
	 * @param obj The object from which context will be created.
	 */
	public DefContainer(final Object obj) {
		if (obj == null) {
			_value = null;
		} else if (obj instanceof XDValue) {
			initContext((XDValue) obj);
		} else if (obj instanceof XDValue[]) {
			_value = (XDValue[]) obj;
		} else if (obj instanceof XDValue[]) {
			XDValue[] x = (XDValue[]) obj;
			_value = new XDValue[x.length];
			System.arraycopy(x, 0, _value, 0, x.length);
		} else if (obj instanceof String) {
			_value = new XDValue[]{new DefString((String) obj)};
		} else if (obj instanceof Element) {
			_value = new XDValue[]{new DefElement((Element) obj)};
		} else if (obj instanceof Attr) {
			_value = new XDValue[]{new DefAttr((Attr) obj)};
		} else if (obj instanceof CharacterData) {
			_value = new XDValue[] {
				new DefString(((CharacterData) obj).getData())};
		} else if (obj instanceof Properties) {
			_value = new XDValue[0];
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
			_value = new XDValue[] {
				new DefLong(((Number) obj).longValue())};
		} else if (obj instanceof Boolean) {
			_value = new XDValue[] {	new DefBoolean(((Boolean) obj))};
		} else if (obj instanceof Float || obj instanceof Double) {
			_value = new XDValue[]{
				new DefDouble(((Number) obj).doubleValue())};
		} else if (obj instanceof String[]) {
			String[] ss = (String[]) obj;
			_value = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_value[i] = new DefString(ss[i]);
			}
		} else if (obj instanceof Boolean[]) {
			Boolean[] ss = (Boolean[]) obj;
			_value = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_value[i] = new DefBoolean(ss[i]);
			}
		} else if (obj instanceof Integer[]) {
			Integer[] ss = (Integer[]) obj;
			_value = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_value[i] = new DefLong(ss[i]);
			}
		} else if (obj instanceof Long[]) {
			Long[] ss = (Long[]) obj;
			_value = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_value[i] = new DefLong(ss[i]);
			}
		} else if (obj instanceof Float[]) {
			Float[] ss = (Float[]) obj;
			_value = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_value[i] = new DefDouble(ss[i].doubleValue());
			}
		} else if (obj instanceof Double[]) {
			Double[] ss = (Double[]) obj;
			_value = new XDValue[ss.length];
			for (int i = 0; i < ss.length; i++) {
				_value[i] = new DefDouble(ss[i]);
			}
		} else {
			_value = new XDValue[]{new DefString("")};
			//Illegal argument in method: &{0}
			throw new SIllegalArgumentException(SYS.SYS084,
				"new " + obj.getClass() + "()");
		}
	}

	private void setValuesFromContext(final XDContainer d) {
		int len = d.getXDItemsNumber();
		if (len > 0) {
			_value = new XDValue[len];
			System.arraycopy(d.getXDItems(), 0, _value, 0, _value.length);
		} else {
			_value = new XDValue[0];
		}
		len = d.getXDNamedItemsNumber();
		if (len > 0) {
			_map = new XDNamedValue[len];
			System.arraycopy(d.getXDNamedItems(), 0, _map, 0, _map.length);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//map interface
	////////////////////////////////////////////////////////////////////////////

	@Override
	/** Set named item to the table of named items.
	 * @param name the name of item.
	 * @param val the value of item.
	 * @return if the named item not exists in the table of named items then
	 * return <tt>null</tt> or return the value which was replaced.
	 */
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

	@Override
	/** Check if named item exists in the table of named items.
	 * @param name the name of named item.
	 * @return <tt>true</tt> if and only if named item exists in the table.
	 */
	public final boolean hasXDNamedItem(final String name) {
		return namedItemIndex(name)>=0;
	}

	@Override
	/** Get named item from the table of named items.
	 * @param name the name of named item.
	 * @return if item not exists in table return <tt>null</tt> or
	 * return the named item from the table of named items.
	 */
	public final XDNamedValue getXDNamedItem(final String name) {
		int i = namedItemIndex(name);
		return i >= 0 ? _map[i] : null;
	}

	@Override
	/** Get value of named item from the table of named items.
	 * @param name the name of named item.
	 * @return if item not exists the return <tt>null</tt> or
	 * return the named item.
	 */
	public final XDValue getXDNamedItemValue(final String name) {
		int i = namedItemIndex(name);
		return i >= 0 ? _map[i].getValue() : null;
	}

	@Override
	/** Get value of named item from the table of named items as string.
	 * @param name the name of named item.
	 * @return if item not exists in table return <tt>null</tt> or
	 * return the value of named item as string.
	 */
	public final String getXDNamedItemAsString(final String name) {
		XDValue val = getXDNamedItemValue(name);
		return val == null ? null : val.stringValue();
	}

	@Override
	/** Remove named item from the table of named items.
	 * @param name the name of named item.
	 * @return the removed named item or <tt>null</tt>.
	 */
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

	@Override
	/** Get number of named items in the table of named items.
	 * @return The number of items.
	 */
	public final int getXDNamedItemsNumber() {
		return _map == null ? 0 : _map.length;
	}

	@Override
	/** Get array with named items in the table.
	 * @return array with named items.
	 */
	public final XDNamedValue[] getXDNamedItems() {
		return _map != null ? _map : new XDNamedValue[0];
	}

	@Override
	/** Get name of i-th named item.
	 * @param index index of item.
	 * @return name of item.
	 */
	public final String getXDNamedItemName(final int index) {
		return _map == null || index >= _map.length ?
			null : _map[index].getName();
	}

	@Override
	/** Set named item to the table of named items.
	 * @param item the named item.
	 * @return if the named item not exists then return <tt>null</tt> or return
	 * the named item value which was replaced in the table of named items.
	 */
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
			_value = new XDValue[len1];
			if (len1 > 0 ) {
				if (numOfNamedItems == 0) {
					System.arraycopy(value, 0, _value, 0, len1);
				} else {
					for (int i = 0, j = 0; i < len; i++) {
						if (value[i].getItemId() != XD_NAMEDVALUE) {
							_value[j++] = value[i];
						}
					}
				}
			}
		} else {
			_value = new XDValue[0];
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
		ArrayList<XDValue> ar = new ArrayList<XDValue>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			switch (node.getNodeType()) {
				case Node.ELEMENT_NODE:
					ar.add(new DefElement((Element) node));
					continue;
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
					ar.add(new DefString(node.getNodeValue()));
			}
		}
		_value = new  XDValue[ar.size()];
		ar.toArray(_value);
	}

	private void setArrayListValue(final Object obj) {
		ArrayList<XDValue> ar = new ArrayList<XDValue>();
		for (int i = 0, size = ((ArrayList)obj).size(); i < size; i++) {
			Object item = ((ArrayList)obj).get(i);
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
		_value = new  XDValue[ar.size()];
		ar.toArray(_value);
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get the item from sequence at given index.
	 * @param index index of item.
	 * @return item at given index or return <tt>null</tt>.
	 */
	public final XDValue getXDItem(final int index) {
		return index < 0 || _value == null || index >= _value.length ?
			null : _value[index];
	}

	@Override
	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	public final void addXDItem(final XDValue value) {
		if (_value == null) {
			_value = new XDValue[] {value};
		} else {
			insertXDItemBefore(_value.length, value);
		}
	}

	@Override
	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	public final void addXDItem(final String value) {
		XDValue x = new DefString(value);
		if (_value == null) {
			_value = new XDValue[] {x};
		} else {
			insertXDItemBefore(_value.length, x);
		}
	}

	@Override
	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	public final void addXDItem(final Element value) {
		XDValue x = new DefElement(value);
		if (_value == null) {
			_value = new XDValue[] {x};
		} else {
			insertXDItemBefore(_value.length, x);
		}
	}

	@Override
	/** Set item at position given by index.
	 * @param index index of item item. If index is out of range of items this
	 * method does nothing.
	 * @param value of item.
	 * @return original value or null;
	 */
	public XDValue replaceXDItem(final int index, final XDValue value) {
		if (_value != null && index >= 0 && index < _value.length) {
			XDValue result = _value[index];
			_value[index] = value;
			return result;
		}
		return null;
	}

	@Override
	/** Insert item before given index to the sequence.
	 * @param index index of required item.
	 * @param value item to be inserted.
	 */
	public final void insertXDItemBefore(final int index, final XDValue value) {
		if (index < 0) {
			return;
		}
		if (_value == null) {
			if (index > 0) {
				return;
			}
			_value = new XDValue[0];
		}
		int ndx;
		if ((ndx = index) > _value.length) {
			ndx = _value.length;
		}
		int len = _value.length + 1;
		XDValue[] old = _value;
		_value = new XDValue[len];
		if (len == 1) {
			_value[0] = value;
			return;
		}
		if (ndx > 0) {
			System.arraycopy(old, 0, _value, 0, ndx);
		}
		if (ndx < len - 1) {
			System.arraycopy(old, ndx, _value, ndx + 1, len - ndx - 1);
		}
		_value[ndx] = value;
	}

	@Override
	/** Remove item from the sequence at given index.
	 * @param index the index of item in the sequence which will be removed.
	 * @return removed value or null.
	 */
	public final XDValue removeXDItem(final int index) {
		if (index < 0 || _value == null || index >= _value.length) {
			return null;
		}
		int len = _value.length - 1;
		XDValue result = _value[index];
		if (len == 0) {
			_value = new XDValue[0];
		} else {
			XDValue[] old = _value;
			_value = new XDValue[len];
			if (index > 0) {
				System.arraycopy(old, 0, _value, 0, index);
			}
			if (index < len) {
				System.arraycopy(old, index + 1, _value, index, len - index);
			}
		}
		return result;
	}

	@Override
	/** Get number of items in the sequence.
	 * @return number of items in the sequence.
	 */
	public final int getXDItemsNumber() {
		return _value != null ? _value.length : -1;
	}

	@Override
	/** Get array of all items in the sequence.
	 * @return array of all items in the sequence.
	 */
	public final XDValue[] getXDItems() {return _value;}

	@Override
	/** Create new XDContext with all elements from context.
	 * @return The new XDContext with elements.
	 */
	public final XDContainer getXDElements() {
		if (_value == null) {
			return new DefContainer();
		}
		ArrayList<XDValue> ar = new ArrayList<XDValue>();
		for (int i = 0; i < _value.length; i++) {
			if (_value[i].getItemId() == XD_ELEMENT) {
				ar.add(_value[i]);
			} else if (_value[i].getItemId() == XD_CONTAINER) {
				ar.add(new DefElement(
					((XDContainer)_value[i]).toElement(null, "")));
			}
		}
		DefContainer result = new DefContainer();
		result._value = new XDValue[ar.size()];
		ar.toArray(result._value);
		return result;
	}

	@Override
	/** Get the n-th element from context or null.
	 * @param n The index of element.
	 * @return the n-th element from context or null..
	 */
	public final Element getXDElement(final int n) {
		if (_value == null) {
			return null;
		}
		if (n >= 0 && n < _value.length) {
			switch (_value[n].getItemId()) {
				case XD_ELEMENT:
					return _value[n].getElement();
				case XD_CONTAINER:
					return ((XDContainer)_value[n]).toElement(null, null);
			}
			Document doc = KXmlUtils.newDocument(null, "_", null);
			Element el = doc.getDocumentElement();
			setAttrs(new LinkedHashMap<String, String>(), el);
			XDValue val = _value[n];
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

	@Override
	/** Get all elements with given name from context.
	 * @param name The name of element.
	 * @return The new context with elements.
	 */
	public final XDContainer getXDElements(final String name) {
		return getXDElementsNS(null, name);
	}

	@Override
	/** Get all elements with given name and NameSpace from context.
	 * @param nsURI NameSpace URI.
	 * @param localName local name of element.
	 * @return The new context with all elements with given name and NameSpace.
	 */
	public final XDContainer getXDElementsNS(final String nsURI,
		final String localName) {
		if (_value == null) {
			return new DefContainer();
		}
		ArrayList<XDValue> ar = new ArrayList<XDValue>();
		for (int i = 0; i < _value.length; i++) {
			if (_value[i].getItemId() == XD_ELEMENT) {
				Element el = _value[i].getElement();
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
					ar.add(_value[i]);
				}
			}
		}
		DefContainer result = new DefContainer();
		result._value = new XDValue[ar.size()];
		ar.toArray(result._value);
		return result;
	}

	@Override
	/** Get all text nodes concatenated as a string.
	 * @return The string with all text nodes.
	 */
	public final String getXDText() {
		StringBuilder sb = new StringBuilder();
		if (_value == null) {
			return null;
		}
		boolean wasItem = false;
		for (int i = 0; i < _value.length; i++) {
			if (_value[i].getItemId() == XD_STRING ||
				_value[i].getItemId() == XD_ATTR ||
//				_value[i].getItemId() == XD_BOOLEAN ||
//				_value[i].getItemId() == XD_INT ||
//				_value[i].getItemId() == XD_FLOAT ||
//				_value[i].getItemId() == XD_DATETIME ||
				_value[i].getItemId() == XD_TEXT) {
				String s = (_value[i]).stringValue();
				wasItem = true;
				if (s != null && s.length() > 0) {
					sb.append(s);
				}
			}
		}
		return wasItem ? sb.toString() : null;
	}

	@Override
	/** Get string from n-th item from this context. If the node does not
	 * exist or if it is not text then return the empty string.
	 * @param n The index of item.
	 * @return The string.
	 */
	public final String getXDTextItem(final int n) {
		return n >= 0 && _value != null && n < _value.length &&
			(_value[n].getItemId() == XD_STRING ||
			_value[n].getItemId() == XD_TEXT ||
			_value[n].getItemId() == XD_ATTR) ?
			_value[n].stringValue() : "";
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public final short getItemId() {return XD_CONTAINER;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.CONTAINER;}

	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return _value == null;}

	@Override
	/** Check if the object is empty.
	 * @return <tt>true</tt> if the object is empty; otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isEmpty() {
		return (_value == null || _value.length == 0) &&
			(_map == null || _map.length == 0);
	}

	@Override
	/** Get value as printable string. */
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		if (_map != null) {
			for (XDNamedValue x: _map) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(x.toString());
			}
		}
		if (_value != null) {
			for (int i = 0; i < _value.length; i++) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(_value[i].toString());
			}
		}
		return sb.toString();
	}

	@Override
	/** Get boolean value of this object.
	 * @return boolean value of this object or <tt>false</tt>.
	 */
	public final boolean booleanValue() {
		if (!isEmpty()) {
			if (_map == null && _value != null) {
				XDValue x;
				if (_value.length==1 && (x = _value[0])!=null) {
					if (x.isNull()) {
						return false;
					}
					switch (x.getItemId()) {
						case XD_BOOLEAN:
							return x.booleanValue();
						case XD_FLOAT:
						case XD_INT:
						case XD_DECIMAL:
							return x.longValue() != 0;
						case XD_STRING:
							return !x.stringValue().isEmpty();
						default:
							return true;
					}
				}
				for (XDValue v: _value) {
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
	/** Get boolean value of this object.
	 * @return boolean value of this object or <tt>false</tt>.
	 */
	public final long longValue() {
		return (_value != null && _value.length == 1 && _value[0] != null)
			? _value[0].longValue() : 0;
	}
	@Override
	/** Get boolean value of this object.
	 * @return boolean value of this object or <tt>false</tt>.
	 */
	public final double doubleValue() {
		return (_value != null && _value.length == 1 && _value[0] != null)
			? _value[0].doubleValue() : Double.NaN;
	}
	@Override
	/** Get boolean value of this object.
	 * @return boolean value of this object or <tt>false</tt>.
	 */
	public BigDecimal decimalValue() {
		return (_value != null && _value.length == 1 && _value[0] != null)
			? _value[0].decimalValue() : null;
	}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public final String stringValue() {
		return toString();
	}

	@Override
	/** Create element from context.
	 * @param nsUri of created element.
	 * @param xmlName name of created element.
	 * @return element created from this context.
	 */
	public final Element toElement(final String nsUri, final String xmlName) {
		return toElement(new LinkedHashMap<String, String>(), nsUri, xmlName);
	}

	public final Element toElement(
		LinkedHashMap<String, String> ns,
		final String nsUri, final String xmlName) {
		String n = xmlName;
		String u = nsUri;
		DefContainer c = this;
		if (n == null || n.length() == 0) {
			XDValue v;
			if (getXDNamedItemsNumber() == 1 &&	getXDItemsNumber() <= 1
				&& StringParser.chkXMLName(_map[0].getName(), (byte) 10)
				&& ((v = _map[0].getValue()) == null || v.isNull()
				|| v.getItemId() == XD_CONTAINER)) {
				c = v == null || v.isNull() ? new DefContainer()
					: (DefContainer) _map[0].getValue();
				n = _map[0].getName();
				if (getXDItemsNumber() == 0) {
					int ndx = n.indexOf(':');
					String prefix = ndx > 0 ? ":" + n.substring(0, ndx) : "";
					u = c. hasXDNamedItem("xmlns"+prefix) ?
						c.getXDNamedItem("xmlns"+prefix).getValue().toString()
						: null;
					if (u != null) {
						ns.put(ndx > 0 ? n.substring(0, ndx) : "", u);
					} else {
						u = ns.get(ndx > 0 ? n.substring(0, ndx) : "");
					}
				} else {
					return KXmlUtils.newDocument(null,
						n, null).getDocumentElement();
				}
			} else if (getXDItemsNumber() == 1 &&
				getXDItem(0).getItemId() == XD_ELEMENT) {
				return getXDItem(0).getElement();
			} else {
				n = "_";
			}
		}
		Element el = KXmlUtils.newDocument(u, n, null).getDocumentElement();
		ns = new LinkedHashMap<String, String>(ns);
		c.setAttrs(ns, el);
		c.setChildNodes(ns, el);
		return el;
	}

	private void setAttrs(final LinkedHashMap<String, String> ns,final Element el) {
		if (_map != null) {
			for (int i = 0; i < _map.length; i++) {
				XDValue val = _map[i].getValue();
				//we nead to get null from STRING_VALUE
				if (val != null && !val.isNull()) {
					String t = _map[i].getName();
					if (val.getItemId() == XD_CONTAINER) {
						XDContainer x = (XDContainer) val;
						Element el1 = x.toElement(null, t);
						el.appendChild(el.getOwnerDocument().importNode(el1, true));
					} else {
						String s = val.getItemId() == XD_STRING ?
						val.stringValue() : val.toString();
						el.setAttribute(t, s);
						if (t.startsWith("xmlns")) {
							if (t.length() == 5) {
								ns.put("", s);
							} else {
								ns.put(t.substring(6), s);
							}
						}
					}
				}
			}
		}
	}

	private void setChildNodes(final LinkedHashMap<String,String> ns,
		final Element el) {
		if (_value != null && _value.length > 0) {
			Document doc = el.getOwnerDocument();
			for (int i = 0; i < _value.length; i++) {
				XDValue item = _value[i];
				if (item != null) {
					if (item.getItemId() == XD_ELEMENT) {
						Element e = item.getElement();
						if (e != null) {
							el.appendChild(doc.importNode(e, true));
						}
					} else if (item.getItemId() == XD_CONTAINER) {
						DefContainer x = ((DefContainer) item);
						Element e = x.toElement(ns, null, null);
						el.appendChild(doc.importNode(e, true));
					} else if (item.getItemId() == XD_NAMEDVALUE) {
						DefNamedValue x = (DefNamedValue) item;
						el.setAttribute(x.getName(), x.getValue().toString());
					} else {
						String s = item.stringValue();
						if (s != null && s.length() > 0) {
							el.appendChild(doc.createTextNode(s));
						}
					}
				}
			}
		}
	}

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return isNull() ? null : this;}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public final XDValue cloneItem() {
		DefContainer result = new DefContainer();
		if (_value != null) {
			result._value = new XDValue[_value.length];
			for (int i = 0; i < _value.length; i++) {
				XDValue x = _value[i];
				if (x != null) {
					result._value[i] = _value[i].cloneItem();
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

	@Override
	/** Sorts this context.
	 * If an item is an org.w3c.Node object then as a key it is used
	 * the text value of an item).
	 * @param asc if true context will be sorted ascendant, else descendant.
	 * @return this context sorted.
	 */
	public final XDContainer sortXD(final boolean asc) {
		return sortXD(null, asc);
	}

	@Override
	/** Sorts this context.
	 * @param key String with xpath expression or null (if null or empty string
	 * then for org.w3c.Node items it is used as a key the text value of
	 * an item). For items other then  org.w3c.Node objects this parameter is
	 * ignored.
	 * @param asc if true context will be sorted ascendant, else descendant.
	 * @return this context sorted.
	 */
	public final XDContainer sortXD(final String key, final boolean asc) {
		DefContainer dc = new DefContainer(this);
		sort1(key, asc);
		return dc;
	}

	/** Sorts this context.
	 * @param key String with xpath expression or null (if null or empty string
	 * then for org.w3c.Node items it is used as a key the text value of
	 * an item). For items other then  org.w3c.Node objects this parameter is
	 * ignored.
	 * @param asc if true context will be sorted ascendant, else descendant.
	 */
	private void sort1(final String key, final boolean asc) {
		int len;
		if ((len = _value != null ? _value.length : 0) <= 1) {
			return;
		}
		Object[] keys = new Object[len];
		for (int i = len - 1; i >= 0; i--) {
			Object mykey;
			short type;
			if ((type = _value[i].getItemId()) == XD_ELEMENT){
				Element el = _value[i].getElement();
				if (el == null) {
					mykey = "";
				} else {
					if (key == null || key.length() == 0) {
						mykey = KXmlUtils.getTextValue(el);
					} else {
						DefContainer dc =
							new DefContainer(KXpathExpr.evaluate(el, key));
						if (dc.getXDItemsNumber() != 1) {
							mykey = "";
						} else {
							XDValue dv = dc.getXDItem(0);
							switch (dv.getItemId()) {
								case XD_ELEMENT:
								case XD_ATTR:
								case XD_TEXT:
								case XD_STRING:
									mykey =	dc.getXDItem(0).stringValue();
									break;
								case XD_FLOAT:
								case XD_INT:
								case XD_DECIMAL:
								case XD_BOOLEAN:
								case XD_DATETIME:
								case XD_REPORT:
									mykey = dv;
									break;
								default:
//									XD_BYTES, XX_ELEMENT, XX_TEXT,
//									XD_CONTAINER, XD_OBJECT, XD_INPUT,
//									XD_OUPUT, XD_EXCEPTION, XMLNODE_VALUE,
//									BYTE_VALUE, CHAR_VALUE ...
									mykey =	"";
							}
						}
					}
				}
			} else if (type == XD_ATTR) {
				mykey = _value[i].stringValue();
			} else if (type == XD_TEXT) {
				mykey = _value[i].stringValue();
			} else if (type == XD_STRING) {
				mykey = _value[i].stringValue();
			} else if (type == XD_INT) {
				mykey = _value[i].longValue();
			} else if (type == XD_BOOLEAN) {
				mykey = _value[i].booleanValue() ? 1 : 0;
			} else {
				mykey = _value[i].toString();
			}
			keys[i] = mykey;
		}
		sort2(0, _value.length - 1, keys, asc);
	}

	private void sort2(int low,
		int high,
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
						XDValue item = _value[i];
						_value[i] = _value[j];
						_value[j] = item;
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
						XDValue item = _value[i];
						_value[i] = _value[j];
						_value[j] = item;
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
						XDValue item = _value[i];
						_value[i] = _value[j];
						_value[j] = item;
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
						XDValue item = _value[i];
						_value[i] = _value[j];
						_value[j] = item;
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
						XDValue item = _value[i];
						_value[i] = _value[j];
						_value[j] = item;
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
						XDValue item = _value[i];
						_value[i] = _value[j];
						_value[j] = item;
					}
				}
			}
		}
		sort2(low, j, keys, asc);
		sort2(j + 1, high, keys, asc);
	}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public final boolean equals(final XDValue arg) {
		if (arg.getItemId() != XD_CONTAINER) {
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