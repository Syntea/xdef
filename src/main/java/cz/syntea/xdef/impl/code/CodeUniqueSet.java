/*
 * File: CodeUniqueset.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.XDUniqueset;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import cz.syntea.xdef.impl.compile.CompileBase;
import java.util.HashMap;
import java.util.Map;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.XDValueType;
import java.util.HashSet;
import java.util.Set;

/** Provides unique set used for validation methods ID, IDREF, CHECKID etc.
 * The item of the set an array of CodeParseItems. To each item is assigned
 * an object, containing list of not yet resolved references and the other
 * values used for different purposes (e.g. if the item was referred,
 * assigned user value etc.)
 * @author Vaclav Trojan
 */
public final class CodeUniqueset extends XDValueAbstract implements XDUniqueset{

	/** Map of values. */
	private final Map<Object, UniquesetItem> _map;
	/** Set of markers. */
	private final Set<Object> _markers = new HashSet<Object>();
	/** Create "null" object. */
	private final CodeUniquesetParseItem[] _parseItems;
	/** Name of this uniqueSet. */
	private final String _name;
	/** Type of this object (UNIQUESET_VALUE or UNIQUESET_M_VALUE). */
	private final short _type;
	/** Index of actual key item of multiple key. */
	private int _keyIndex;

	/** Creates a new instance of CodeUniqueset.
	 * @param parseItems CodeUniquesetKey array from which the new instance
	 * will be created.
	 * @param name name of UniqueSet.
	 */
	public CodeUniqueset(final CodeUniquesetParseItem[] parseItems,
		final String name) {
		_map = new HashMap<Object, UniquesetItem>();
		_type = parseItems.length > 1
			? CompileBase.UNIQUESET_M_VALUE : CompileBase.UNIQUESET_VALUE;
		_parseItems = parseItems;
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
		UniquesetItem uso = _map.get(getKeyValue());
		if (uso == null) {
			return false;
		}
		uso.mark(_markers);
		return true;
	}

	/** Check if key already exists in the map.
	 * @return null if id exists, otherwise return ArrayReporter with pending
	 * reports.
	 */
	public final ArrayReporter chkId() {
		CodeUniquesetKey key = getKeyValue();
		UniquesetItem uso = _map.get(key);
		if (uso == null) {
			uso = new UniquesetItem(key); //empty report list
			uso.mark(_markers);
			_map.put(key, uso);
			return uso._references;
		}
		uso.mark(_markers);
		return uso._references.isEmpty() ? null : uso._references;
	}

	/** Sets value of parsed value to the map of objects.
	 * If value already exists in the map return report. Otherwise
	 * clear the list of pending unresolved references and return null.
	 * @return error report or null.
	 */
	public final Report setId() {
		CodeUniquesetKey keyValue = getKeyValue();
		UniquesetItem uso = _map.get(keyValue);
		if (uso == null) {
			_map.put(keyValue, new UniquesetItem(keyValue));
			return null;
		}
		if (uso._references.isEmpty()) {
			//Value must be unique&{0}{: }
			return Report.error(XDEF.XDEF523, (_name!=null ? _name+" " : "")
				+ keyValue);
		}
		uso._references.clear();
		return null;
	}

	/** Get array of keys.
	 * @return array of keys.
	 */
	public final CodeUniquesetParseItem[] getParsedItems() {return _parseItems;}

	/** Get address of parsing method for key item with given index.
	 * @param i index of key in multiple key array.
	 * @return parse item of key in multiple key array.
	 */
	public final CodeUniquesetParseItem getParseKeyItem(final int i) {
		return _parseItems[i];
	}

	/** Create value of unique set key object.
	 * @return new value of unique set key object.
	 */
	private CodeUniquesetKey getKeyValue() {
		return new CodeUniquesetKey(_parseItems);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDUniqueset interface.
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get name of this uniqueSet.
	 * @return name of this uniqueSet.
	 */
	public final String getName() {return _name;}

	@Override
	/** Write error reports to reporter and clear map.
	 * @param reporter report writer.
	 * @return true if reporter was empty.
	 */
	public final boolean checkAndClear(final ReportWriter reporter) {
		if (_map == null) {
			return true;
		}
		boolean result = true;
		for (UniquesetItem a: _map.values()) {
			if (reporter != null) {
				Report rep;
				while((rep = a._references.getReport()) != null) {
					reporter.putReport(rep);
					result = false;
				}
			}
			a._references.clear();
		}
		_map.clear();
		return result;
	}

	@Override
	/** Get address of parsing method.
	 * @return the address of code.
	 */
	public final int getParseMethod() {
		return _keyIndex!=-1 ? _parseItems[_keyIndex].getParseMethodAddr() : -1;
	}

	@Override
	/** Get key part index of the actual item.
	 * @return actual key index.
	 */
	public final int getKeyItemIndex() {return _keyIndex;}

	@Override
	/** Set object as marker.
	 * @param marker Object which is used for markers.
	 * @return true if the marker is new in the set of makers.
	 */
	public final boolean setMarker(final Object marker) {
		return _markers.add(marker);
	}

	@Override
	/** Check if all item are marked with given object in this unique set.
	 * @param marker Object used as marker.
	 * @return list unmarked keys in this unique set or return the empty string.
	 */
	public final String checkNotMarked(final Object marker) {
		String s = "";
		if (_markers.contains(marker)) {
			for (UniquesetItem uso : _map.values()) {
				if (!uso.checkAndRemoveMarked(marker)) {
				if (!s.isEmpty()) {
						s += "; ";
					}
					s += uso._key.toString();
				}
			}
		}
		return s;
	}

	@Override
	/** Set named named value assigned to to the actual unique set item.
	 * If the key not exists this method does nothing.
	 * @param name name of value.
	 * @param value value to be set.
	 */
	public final void setNamedValue(final String name, final XDValue value) {
		UniquesetItem uso = _map.get(getKeyValue());
		if (uso != null) {
			uso.setValue(name, value);
		}
	}

	@Override
	/** Get named named value assigned to the actual unique set item.
	 * If the key not exists this method returns null.
	 * @param name name of value.
	 * @return saved value.
	 */
	public final XDValue getNamedValue(final String name) {
		UniquesetItem uso = _map.get(getKeyValue());
		return uso == null ? null : uso.getValue(name);
	}

	@Override
	/** Get printable form of actual value of the key.
	 * @return printable form of actual value of the key.
	 */
	public final String printActualKey() {return getKeyValue().printKey();}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get type id of this object.
	 * @return The type id of this object.
	 */
	public final short getItemId() {return _type;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public final XDValueType getItemType() {return  XDValueType.OBJECT;}

	@Override
	public String toString() {
		String result = (_type == CompileBase.UNIQUESET_VALUE
			? "UNIQUESET: " : "UNIQUESET_M: ") + _name
			+ ", size=" + _map.size() + ", ";
		if (_parseItems.length > 1) {
			result += "keys:";
			for (int i = 0; i < _parseItems.length; i++) {
				CodeUniquesetParseItem keyItem = _parseItems[i];
				result += (i > 0 ? "," : "") + keyItem.toString();
			}
		} else {
			result += "key" + _parseItems[0];
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////
// Private classes used in this implementation of XDUniqueset.
////////////////////////////////////////////////////////////////////////////////

	/** This class is used for multiple keys in map of key values. */
	private static final class CodeUniquesetKey {

		/** Values of parse items. */
		private final XDValue[] _items;

		/** Construct  CodeUniquesetKey object.
		 * @param keys array with unique set parse items.
		 */
		private CodeUniquesetKey(final CodeUniquesetParseItem[] keys) {
			XDValue[] items = new XDValue[keys.length];
			for (int i = 0; i < items.length; i++) {
				XDValue x = keys[i].getParsedObject();
				if (x != null) {
					if (x instanceof DefParseResult) {
						DefParseResult d = (DefParseResult) x;
						items[i] = d.getParsedValue();
					} else {
						items[i] = x;
					}
					if (keys[i].isOptional()) {
						keys[i].setParsedObject(null);
					}
				} else {
					items[i] = DefNull.genNullValue(XD_ANY);
				}
			}
			_items = items;
		}

		/** Get printable form of value of this key.
		 * @return printable form of actual value of this key.
		 */
		private String printKey() {
			String result = "";
			for (XDValue x: _items) {
				result += result.isEmpty()? x : (", " + x);
			}
			return result;
		}

		@Override
		public String toString() {return printKey();}

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
			if (o instanceof CodeUniquesetKey) {
				XDValue[] x = ((CodeUniquesetKey) o)._items;
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

	/** Implements the item of unique set. */
	private static final class UniquesetItem {
		/** Key of unique set. */
		private final CodeUniquesetKey _key;
		/** List of unresolved references. */
		private final ArrayReporter _references = new ArrayReporter();
		/** Set of markers of references. */
		private Set<Object> _referenced;
		/** Assigned named values to this item. */
		private Map<String, XDValue> _assignedValues;
//
//		/** Constructor of UniquesetItem.
//		 * @param key unique set key.
//		 * @param assignedValues Map with assigned named values to this item.
//		 */
//		private UniquesetItem(final CodeUniquesetKey key,
//			final Map<String, XDValue> assignedValues) {
//			_key = key;
//			_assignedValues = assignedValues;
//		}

		/** Constructor of UniquesetItem.
		 * @param key unique set key.
		 */
		private UniquesetItem(CodeUniquesetKey key) {_key = key;}

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
		 * @param value value to be set.
		 */
		private void setValue(final String name, final XDValue value) {
			if (_assignedValues == null) {
				_assignedValues = new HashMap<String, XDValue>();
			}
			_assignedValues.put(name, value);
		}

		/** Set named value from this item.
		 * @param name name of value.
		 * @return the value or null.
		 */
		private XDValue getValue(final String name) {
			return (_assignedValues==null) ? null : _assignedValues.get(name);
		}
	}
}