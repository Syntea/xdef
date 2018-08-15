/*
 * File: CodeUniqueSet.java
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

/** Provides unique set of id's.
 * @author Vaclav Trojan
 */
public final class CodeUniqueSet extends XDValueAbstract {

	/** Map of values. */
	private final Map<Object, ArrayReporter> _map;
	/** Create "null" object. */
	private final CodeParseItem[] _keys;
	/** Type of this object (UNIQUESET_VALUE or UNIQUESET_M_VALUE). */
	private final short _type;
	/** Index of actual key. */
	private int _keyIndex;
	/** Name of this uniqueSet. */
	final String _name;

	/** Creates a new instance of DefUniqueSet.
	 * @param keys array it key items.
	 * @param name name of UniqueSet.
	 */
	private CodeUniqueSet(final CodeParseItem[] keys, final String name) {
		_keys = keys.length == 0 ? null : keys;
		_map = keys.length == 0 ? null : new HashMap<Object, ArrayReporter>();
		_type = keys.length > 1
			? CompileBase.UNIQUESET_M_VALUE : CompileBase.UNIQUESET_VALUE;
		for (CodeParseItem parseItem: keys) {
			parseItem.setUniqueSet(this);
		}
		_name = name;
	}

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
	public XDValueType getItemType() {return  XDValueType.OBJECT;}

////////////////////////////////////////////////////////////////////////////////

	/** Creates a new instance of CodeUniqueSet.
	 * @param keys Key array from which the new instance will be created.
	 * @param name name of UniqueSet.
	 * @return new instance of CodeUniqueSet.
	 */
	public final static CodeUniqueSet newInstance(final CodeParseItem[] keys,
		final String name) {
		CodeParseItem[] k = new CodeParseItem[keys.length];
		for (int i = 0; i < k.length; i++) {
			CodeParseItem key = keys[i];
			k[i] = (CodeParseItem) key.cloneItem();
		}
		return new CodeUniqueSet(k, name);
	}

	final boolean hasMultipleKey() {return _keys.length > 1;}

	/** Sets value of parsed value to the map of objects.
	 * If value already exists in the map return report. Otherwise
	 * clear the list of pending unresolved references and return null.
	 * @return error report or null.
	 */
	public final Report setId() {
		Object o = getKeyValues();
		ArrayReporter a = _map.get(o);
		if (a == null) {
			_map.put(o, new ArrayReporter());
			return null;
		}
		if (a.isEmpty()) {
			//Value must be unique&{0}{: }
			return Report.error(XDEF.XDEF523, (_name!=null ? _name+" " : "")+o);
		}
		a.clear();
		return null;
	}

	/** Check if id already exists in the map.
	 * @return true if id exists, otherwise return false.
	 */
	public boolean hasId() {
		return _map.get(getKeyValues()) != null;
	}

	/** Check if id already exists in the map.
	 * @return null if id exists, otherwise return ArrayReporter with pending
	 * reports.
	 */
	public ArrayReporter chkId() {
		Object o = getKeyValues();
		ArrayReporter a = _map.get(o);
		if (a != null && a.isEmpty()) {
			return null;
		} //id exists
		if (a == null) {
			a = new ArrayReporter(); //empty report list
			_map.put(o, a);
		}
		return a;
	}

	/** Write error reports to reporter and clear map.
	 * @param reporter report writer.
	 * @return true if reporter was empty.
	 */
	public boolean checkAndClear(ReportWriter reporter) {
		if (_map == null) {
			return true;
		}
		boolean result = true;
		for (ArrayReporter a: _map.values()) {
			if (reporter != null) {
				Report rep;
				while((rep = a.getReport()) != null) {
					reporter.putReport(rep);
					result = false;
				}
			}
			a.clear();
		}
		_map.clear();
		return result;
	}

	/** Check if actual complex key already exists in the map.
	 * @return null if id exists, otherwise return ArrayReporter with pending
	 * reports.
	 */
	public final Object getKeyValues() {
		return _keys.length==1?_keys[0].getParsedObject():new KeyValues(_keys);
	}

	/** Get address of parsing method.
	 * @return the address of code.
	 */
	public final int getParseMethod() {
		return _keyIndex != -1 ? _keys[_keyIndex].getParseMethodAddr() : -1;
	}

	/** Set key index.
	 * @param keyIndex the key index.
	 */
	public final void setKeyIndex(final int keyIndex) {_keyIndex = keyIndex;}

	/** get key index.
	 * @return actual key index.
	 */
	public final int getKeyIndex() {return _keyIndex;}

	/** Get array of keys.
	 * @return array of keys.
	 */
	public final CodeParseItem[] getParsedKeys() {return _keys;}

	/** Get address of parsing method for key with given index.
	 * @param index index of key in complex key array.
	 * @return parse item of key in complex key array.
	 */
	public final CodeParseItem getParseKeyItem(final int index) {
		return _keys[index];
	}

	public final String getName() {return _name;}

	@Override
	public String toString() {
		String result = (_type == CompileBase.UNIQUESET_VALUE
			? "UNIQUESET: " : "UNIQUESET_M: ") + _name
			+ ", size=" + _map.size() + ", ";
		if (_keys.length > 1) {
			result += "keys:";
			for (int i = 0; i < _keys.length; i++) {
				CodeParseItem keyItem = _keys[i];
				result += (i > 0 ? "," : "") + keyItem.toString();
			}
		} else {
			result += "key" + _keys[0];
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////

	/** This class is used for complex keys in map of key values. */
	private final static class KeyValues {
		private final XDValue[] _items;
		private KeyValues(CodeParseItem[] keys) {
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
		public boolean equals(Object o) {
			if (o instanceof KeyValues) {
				XDValue[] x = ((KeyValues) o)._items;
				for (int i = 0; i < _items.length; i++) {
					if (_items[i] == null || !_items[i].equals(x[i])) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			String result = "";
			for (XDValue x: _items) {
				if (result.length() > 0) {
					result += ",";
				}
				result += x;
			}
			return result;
		}
	}

}