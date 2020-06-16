package org.xdef.impl.code;

import java.util.ArrayList;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.XDContainer;
import org.xdef.XDUniqueSetItem;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueID;
import org.xdef.impl.compile.CompileBase;
import java.util.HashMap;
import java.util.Map;
import org.xdef.sys.ReportWriter;
import org.xdef.XDValueType;
import java.util.HashSet;
import java.util.Set;

/** Provides unique set used for validation methods ID, IDREF, CHECKID etc.
 * The item of the set an array of CodeParseItems. To each item is assigned
 * an object, containing list of not yet resolved references and the other
 * values used for different purposes (e.g. if the item was referred,
 * assigned user value etc.)
 * @author Vaclav Trojan
 */
public final class CodeUniqueset extends XDValueAbstract {

	/** Name of this uniqueSet. */
	private final String _name;
	/** Map of unique items ("table"). */
	private final Map<UniquesetKey, UniquesetValue> _map;
	/** Set of markers. */
	private final Set<Object> _markers = new HashSet<Object>();
	/** Array with parse items. */
	private final ParseItem[] _parseItems;
	/** Index of actual key item of multiple key. */
	private int _keyIndex;

	/** Creates a new instance of CodeUniqueSet.
	 * @param parseItems CodeUniquesetKey array from which the new instance
	 * will be created.
	 * @param name name of unique set object.
	 */
	public CodeUniqueset(final ParseItem[] parseItems,
		final String name) {
		_map = new HashMap<UniquesetKey, UniquesetValue>();
		_parseItems = new ParseItem[parseItems.length];
		for(int i = 0; i < parseItems.length; i++) {
			_parseItems[i] = (ParseItem) parseItems[i].cloneItem();
		}
		_name = name;
	}

	/** Set index of multiple key  (0 if key is simple).
	 * @param keyIndex multiple key index (0 if key is simple).
	 */
	public final void setKeyIndex(final int keyIndex) {_keyIndex = keyIndex;}

	/** Check if id already exists in the map.
	 * @return true if id exists, otherwise return false.
	 */
	public final boolean hasId() {
		UniquesetKey key = getKeyValue();
		UniquesetValue usv = _map.get(key);
		if (usv == null) {
			return false;
		}
		usv.mark(_markers);
		return true;
	}

	/** Check if key already exists in the map.
	 * @return null if id exists, otherwise return ArrayReporter with pending
	 * reports.
	 */
	public final ArrayReporter chkId() {
		UniquesetKey key = getKeyValue();
		UniquesetValue usv = _map.get(key);
		if (usv == null) {
			usv = new UniquesetValue(); //empty report list
			usv.mark(_markers);
			_map.put(key, usv);
			return usv._references;
		}
		usv.mark(_markers);
		return usv._references.isEmpty() ? null : usv._references;
	}

	/** Sets value of parsed value to the map of objects.
	 * If value already exists in the map return report. Otherwise
	 * clear the list of pending unresolved references and return null.
	 * @return error report or null.
	 */
	public final Report setId() {
		UniquesetKey key = getKeyValue();
		if (key._missing != null && !key._missing.isEmpty()) {//not complete?
			String s = "";
			for (int i : key._missing) {
				if (!s.isEmpty()) {
					s += ", ";
				}
				s += '"' + _parseItems[i]._name + '"';
			}
			//UniqueSet &{0}{"}{"} item is not complete&{1}{, missing }
			return Report.error(XDEF.XDEF521, _name, s);
		}
		UniquesetValue usv = _map.get(key);
		if (usv == null) { // item not exists
			_map.put(key, new UniquesetValue());
			return null;
		}
		if (usv._references.isEmpty()) {
			//Value must be unique&{0}{: }
			return Report.error(XDEF.XDEF523, (_name!=null ? _name+" ":"")+key);
		}
		usv._references.clear();
		return null;
	}

	/** Get array of keys.
	 * @return array of keys.
	 */
	public final ParseItem[] getParsedItems() {return _parseItems;}

	/** Get address of parsing method for key item with given index.
	 * @param i index of key in multiple key array.
	 * @return parse item of key in multiple key array.
	 */
	public final ParseItem getParseKeyItem(final int i) {return _parseItems[i];}

	/** Create value of unique set key object.
	 * @return new value of unique set key object.
	 */
	private UniquesetKey getKeyValue() {
		return new UniquesetKey(_parseItems);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDUniqueset interface.
////////////////////////////////////////////////////////////////////////////////

	/** Get name of this uniqueSet.
	 * @return name of this uniqueSet.
	 */
	public final String getName() {return _name;}

	/** Write error reports to reporter and clear map.
	 * @param reporter report writer.
	 * @return true if reporter was empty.
	 */
	public final boolean checkAndClear(final ReportWriter reporter) {
		boolean result = true;
		for (UniquesetValue usv: _map.values()) {
			if (reporter != null) {
				Report rep;
				while((rep = usv._references.getReport()) != null) {
					reporter.putReport(rep);
					result = false;
				}
			}
			usv._references.clear();
		}
		_map.clear();
		for (int i = 0; i < _parseItems.length; i++) {
			_parseItems[i]._itemValue = null;
		}
		return result;
	}

	/** Get address of parsing method.
	 * @return the address of code.
	 */
	public final int getParseMethod() {
		return _keyIndex!=-1 ? _parseItems[_keyIndex].getParseMethodAddr() : -1;
	}

	/** Get key part index of the actual item.
	 * @return actual key index.
	 */
	public final int getKeyItemIndex() {return _keyIndex;}

	/** Set object as marker.
	 * @param marker Object which is used for markers.
	 * @return true if the marker is new in the set of makers.
	 */
	public final boolean setMarker(final Object marker) {
		return _markers.add(marker);
	}

	/** Check if all item are marked with given object in this unique set.
	 * @param marker Object used as marker.
	 * @return list unmarked keys in this unique set or return the empty string.
	 */
	public final String checkNotMarked(final Object marker) {
		String s = "";
		if (_markers.contains(marker)) {
			for (Map.Entry<UniquesetKey, UniquesetValue> e:_map.entrySet()){
				UniquesetValue usv = e.getValue();
				if (!usv.checkAndRemoveMarked(marker)) {
					if (!s.isEmpty()) {
						s += ", ";
					}
					s += e.getKey().toString();
				}
			}
		}
		return s;
	}

	/** Set named named value assigned to to the actual unique set item.
	 * If the key not exists this method returns false, otherwise it returns
	 * true.
	 * @param name name of value.
	 * @param val value to be set.
	 * @return true if value was assigned to the item of unique set.
	 */
	public final boolean setNamedValue(final String name, final XDValue val) {
		UniquesetValue usv = _map.get(getKeyValue());
		if (usv == null) {
			return false;
		}
		usv.setValue(name, val);
		return true;
	}

	/** Get named named value assigned to the actual unique set item.
	 * If the key not exists this method returns null.
	 * @param name name of value.
	 * @return saved value.
	 */
	public final XDValue getNamedValue(final String name) {
		UniquesetValue usv = _map.get(getKeyValue());
		XDValue result = usv == null ? null : usv.getValue(name);
		return result == null ? DefNull.NULL_VALUE : result;
	}

	/** Get printable form of actual value of the key.
	 * @return printable form of actual value of the key.
	 */
	public final String printActualKey() {return getKeyValue().toString();}

	/** Get Container created from actual uniqueSet item.
	 * @return the Container created from actual uniqueSet item.
	 */
	public DefContainer getUniqueSetItemAsContainer() {
		UniquesetKey key = getKeyValue();
		UniquesetValue val = _map.get(key);
		if (val == null) {
			return null;
		}
		return usiToContainer(new UniquesetItem(this, key, val));
	}

	/** Create Container from uniqueSet item.
	 * @param x value of UniquesetItem to be converted.
	 * @return Container created from uniqueSet item.
	 */
	private DefContainer usiToContainer(final UniquesetItem x) {
		DefContainer item = new DefContainer();
		for (int i = 0; x._key != null && i < x._key._items.length; i++) {
			item.setXDNamedItem(_parseItems[i]._name, x._key._items[i]);
		}
		if (x._val._assignedValues != null) {
			DefContainer values = new DefContainer();
			for (Map.Entry<String,XDValue>y :
				x._val._assignedValues.entrySet()){
				values.setXDNamedItem(y.getKey(), y.getValue());
			}
			if (!values.isEmpty()) {
				item.addXDItem(values);
			}
		}
		return item;
	}

	/** Get key items from the table.
	 * @return the Container with key items of the table.
	 */
	public final XDContainer getKeyItems() {
		DefContainer result = new DefContainer();
		for (Map.Entry<UniquesetKey, UniquesetValue> x: _map.entrySet()) {
			result.addXDItem(usiToContainer(
				new UniquesetItem(this, x.getKey(), x.getValue())));
		}
		return result;
	}

	/** Get size of the uniqueSet table.
	 * @return size of the table.
	 */
	public final int size() {return _map.size();}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get type id of this object.
	 * @return The type id of this object.
	 */
	public final short getItemId() {return CompileBase.UNIQUESET_M_VALUE;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public final XDValueType getItemType() {return  XDValueType.OBJECT;}

	@Override
	public XDValue cloneItem() {return new CodeUniqueset(_parseItems, _name);}

	@Override
	public String toString() {
		String result = "UNIQUESET: " + _name;
		UniquesetKey key = getKeyValue();
		UniquesetValue usv = _map.get(key);
		result += ", actual key: ";
		if (_parseItems.length > 1) {
			result += "keys:";
			for (int i = 0; i < _parseItems.length; i++) {
				ParseItem keyItem = _parseItems[i];
				result += (i > 0 ? "," : "") + keyItem.toString();
			}
		} else {
			result += "key" + _parseItems[0];
		}
		if (usv != null && usv._assignedValues != null
			&& !usv._assignedValues.isEmpty()) {
			result += " (values: ";
			for (String x: usv._assignedValues.keySet()) {
				result += x + "=" + usv._assignedValues.get(key) + ";";
			}
			result += ")";
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used in this implementation of XDUniqueset.
////////////////////////////////////////////////////////////////////////////////

	/** This class is used for multiple keys in map of key values. */
	private static final class UniquesetKey {

		/** Values of parse items. */
		private final XDValue[] _items;
		/** List of indexes of incomplete items. */
		private ArrayList<Integer> _missing;

		/** Construct UniquesetKey object.
		 * @param keys array with unique set parse items.
		 */
		private UniquesetKey(final ParseItem[] keys) {
			XDValue[] items = new XDValue[keys.length];
			for (int i = 0; i < items.length; i++) {
				XDValue x = keys[i].getParsedObject();
				if (x != null) {
					items[i] = (x instanceof DefParseResult)
						 ? ((DefParseResult) x).getParsedValue() : x;
				} else {
					if (keys[i].isOptional()) {
						items[i] = DefNull.NULL_VALUE;
					} else {
						items[i] = null;
						// add to list of incomplete items
						if (_missing == null) {
							 _missing = new ArrayList<Integer>();
						}
						_missing.add(i);
					}
				}
			}
			_items = items;
		}

		@Override

		/** Get printable form of value of this key.
		 * @return printable form of actual value of this key.
		 */
		public String toString() {
			String result = "";
			for (XDValue x: _items) {
				result += result.isEmpty()? x : (", " + x);
			}
			return result;
		}

		////////////////////////////////////////////////////////////////////////
		// Methods hasCode and equals MUST be implemented here to be able
		// to use these items in HashMap.
		////////////////////////////////////////////////////////////////////////

		@Override
		public int hashCode() {
			int result = _items.length;
			for (int i = 0; i < _items.length; i++) {
				if (_items[i] != null) {
					result = result*3 + _items[i].hashCode();
				}
			}
			return result;
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof UniquesetKey) {
				XDValue[] x = ((UniquesetKey) o)._items;
				for (int i = 0; i < _items.length; i++) {
					if (_items[i] == null || !_items[i].equals(x[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	private static final class UniquesetValue {
		/** List of unresolved references. */
		private final ArrayReporter _references = new ArrayReporter();
		/** Set of markers of references. */
		private Set<Object> _referenced;
		/** Assigned named values to this item. */
		private Map<String, XDValue> _assignedValues;

		/** Mark this item with markers.
		 * @param markers Set with markers.
		 */
		private void mark(final Set<Object> markers) {
			if (!markers.isEmpty()) {
				if (_referenced == null) {
					_referenced = new HashSet<Object>();
				}
				for (Object m : markers) {
					_referenced.add(m);
				}
			}
		}

		/** Check if this item is marked; if yes, remove the marker.
		 * @param marker marker to be checked.
		 * @return true if the item was marked.
		 */
		private boolean checkAndRemoveMarked(final Object marker) {
			return (_referenced == null) ? false : _referenced.remove(marker);
		}

		/** Set named value to this item.
		 * @param name name of value.
		 * @param val value to be set.
		 */
		private void setValue(final String name, final XDValue val) {
			if (_assignedValues == null) {
				_assignedValues = new HashMap<String, XDValue>();
			}
			_assignedValues.put(name, val);
		}

		/** Set named value from this item.
		 * @param name name of value.
		 * @return the value or null.
		 */
		private XDValue getValue(final String name) {
			return _assignedValues ==null ? null : _assignedValues.get(name);
		}
	}

	/** Implements item of unique set (row of table). */
	public static final class UniquesetItem
		extends XDValueAbstract implements XDUniqueSetItem {
		/** uniqueSet object. */
		private final CodeUniqueset _uniqueSet;
		/** Key of uniqueSet item. */
		private final UniquesetKey _key;
		/* Value of uniqueSet item. */
		private final UniquesetValue _val;

		/** Constructor of UniquesetItem.
		 * @param key unique set key.
		 */
		private UniquesetItem(final CodeUniqueset uniqueSet,
			final UniquesetKey key,
			final UniquesetValue val) {
			_uniqueSet = uniqueSet;
			_key = key;
			_val = val;
		}

		@Override
		public String toString() {return _key.toString();}

		@Override
		public int hashCode() {return _key.hashCode();}

		@Override
		public boolean equals(final Object o) {
			return (o instanceof UniquesetItem)
				? ((UniquesetItem) o)._key.equals(_key)
				: (o instanceof UniquesetKey)
				? ((UniquesetKey) o).equals(_key) : false;
		}

		////////////////////////////////////////////////////////////////////////
		// Implementation of XDValue
		////////////////////////////////////////////////////////////////////////

		@Override
		public short getItemId() {return XD_UNIQUESET_ITEM;}

		@Override
		public XDValueType getItemType() {return XDValueType.UNIQUESET_ITEM;}

		////////////////////////////////////////////////////////////////////////
		// Implementation of XDUniqueSetItem
		////////////////////////////////////////////////////////////////////////

		@Override
		/** Get name of uniqueSet table.
		 * @return name of uniqueSet table.
		 */
		public final String getTableName() {return _uniqueSet._name;}

		@Override
		/** Get names of key parts.
		 * @return array with names of key parts.
		 */
		public final String[] getKeyPartNames() {
			String[] result = new String[_uniqueSet._parseItems.length];
			for (int i=0; i < _uniqueSet._parseItems.length; i++) {
				ParseItem x = _uniqueSet._parseItems[i];
				result[i] = x._name;
			}
			return result;
		}

		@Override
		/** Get values of key parts.
		 * @return array with values of key parts.
		 */
		public final XDValue[] getKeyParts() {
			XDValue[] result = new XDValue[_uniqueSet._parseItems.length];
			for (int i=0; i < _uniqueSet._parseItems.length; i++) {
				ParseItem x = _uniqueSet._parseItems[i];
				result[i] = x._itemValue;
			}
			return result;
		}

		@Override
		/** Get value of a key part.
		 * @param name the name of key part.
		 * @return value of key part.
		 */
		public final XDValue getKeyPart(final String name) {
			for (int i=0; i < _uniqueSet._parseItems.length; i++) {
				ParseItem x = _uniqueSet._parseItems[i];
					if (name.equals(x._name)) {
						return x._itemValue;
					}
			}
			throw new RuntimeException("Kye part " + name + " not exists");
		}

		@Override
		/** Get value of an assigned value.
		 * @param name the name of assigned value.
		 * @return assigned value.
		 */
		public final XDValue getValue(final String name) {
			return _val.getValue(name);
		}
	}

	/** Implements uniqueSet parse item. */
	public static final class ParseItem extends XDValueAbstract {

		/** Address of check method. */
		private final int _parseMethodAddr;
		/** Type of parsed object. */
		private final short _itemType;
		/** True if this key is optional, false if it is required. */
		private final boolean _optional;
		/** Key name. */
		private final String _name;
		/** Reference name to declared type or null. */
		private final String _refName;
		/** Resulting value of parsing. */
		private XDValue _itemValue;
		/** Index of key item. */
		private final int _itemIndex;

		/** Creates a new null instance of UniquesetParseItem. */
		ParseItem() {this(null, null, -1, -1, XDValueID.XD_OBJECT, false);}

		/** Creates a new instance of UniquesetParseItem (must be public
		 * because of XDReader).
		 * @param name name of parse item or null;
		 * @param chkAddr address of code of the method.
		 * @param refName name of type;
		 * @param itemIndex index of this key part
		 * @param parsedType type of id.
		 * @param optional if true this key value is required or return
		 * false if it is optional
		 */
		public ParseItem(final String name,
			final String refName,
			final int chkAddr,
			final int itemIndex,
			final short parsedType,
			final boolean optional) {
			_name = name;
			_refName = refName;
			_parseMethodAddr = chkAddr;
			_itemIndex = itemIndex;
			_itemType = parsedType;
			_optional = optional;
			// _itemValue = null; // java mekes it
		}

		////////////////////////////////////////////////////////////////////////
		// Implementation of XDUniquesetParseItem interface
		////////////////////////////////////////////////////////////////////////

		/** Get address of parsing method.
		 * @return the address of code.
		 */
		public final int getParseMethodAddr() {return _parseMethodAddr;}

		/** Get parsed type.
		 * @return the type id.
		 */
		public final short getParsedType() {return _itemType;}

		/** Get parsed type.
		 * @return the type id.
		 */
		public final String getParseName() {return _name;}

		/** Get reference name to declared type.
		 * @return reference name to declared type or null.
		 */
		public final String getDeclaredTypeName() {return _refName;}

		/** Set parsed object (used in XDCodeProcessor).
		 * @param val the value of parsed object.
		 */
		public final void setParsedObject(XDValue val) {_itemValue = val;}

		/** Check if this item is optional or required.
		 * @return true if this item is required.
		 */
		public final boolean isOptional() {return _optional;}

		////////////////////////////////////////////////////////////////////////
		// Implementation of XDValue interface
		////////////////////////////////////////////////////////////////////////

		@Override
		public final short getItemId() {return CompileBase.PARSEITEM_VALUE;}

		@Override
		/** Get ID of the type of value
		 * @return enumeration item of this type.
		 */
		public XDValueType getItemType() {return XDValueType.OBJECT;}

		@Override
		public String toString() {
			return "[" + _itemIndex + "]" + (_name == null ? "null"
				: ((!_name.isEmpty() ? ":" +_name : "") + "=" + _itemValue));
		}

		@Override
		public final String stringValue() {return _itemValue.stringValue();}

		@Override
		public final XDValue cloneItem() {
			return new ParseItem(_name,
				_refName, _parseMethodAddr, _itemIndex, _itemType, _optional);
		}

		@Override
		public final boolean isNull() {return _parseMethodAddr == -1;}

		@Override
		public int hashCode() {return _name.hashCode();}

		@Override
		public boolean equals(final Object arg) {
			 return arg != null && arg instanceof XDValue
				 ? equals((XDValue) arg) : false;
		}

		@Override
		public final boolean equals(final XDValue arg) {
			return arg == null ? false
				: arg.getItemId() != CompileBase.PARSEITEM_VALUE ? false
				: _name != null ? _name.equals(((ParseItem)arg)._name)
				: ((ParseItem)arg)._name == null;
		}

		////////////////////////////////////////////////////////////////////////
		// Methods used in CodeUniqueset.
		////////////////////////////////////////////////////////////////////////

		/** Get index of actual uniqueSet parse item.
		 * @return parse item index.
		 */
		final int getItemIndex() {return _itemIndex;}

		/** Get object of actual uniqueSet parse item.
		 * @return value of parsed object or <tt>null</tt>.
		 */
		final XDValue getParsedObject() {return _itemValue;}
	}
}