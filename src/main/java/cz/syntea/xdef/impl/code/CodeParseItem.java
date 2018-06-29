/*
 * File: CodeParseItem.java
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

import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import cz.syntea.xdef.impl.compile.CompileBase;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

/** Implements items of map of id's.
 * @author Vaclav Trojan
 */
public class CodeParseItem extends XDValueAbstract {

	/** Address of check method. */
	private final int _parseMethodAddr;
	/** Type of parsed object. */
	private final short _itemType;
	/** True if this key is optional. */
	private final boolean _optional;
	/** Key name. */
	private final String _name;
	/** Resulting value of parsing. */
	private XDValue _itemValue;
	/** Index of key item. */
	private final int _itemIndex;
	/** Pointer to CodeUniqueSet.*/
	private CodeUniqueSet _uniqueset;

	/** Creates a new null instance of DefUniqueItem. */
	public CodeParseItem() {
		_itemType = XDValueID.XD_OBJECT;
		_parseMethodAddr = -1;
		_name = null;
		_optional = false;
		_itemIndex = -1;
	}

	/** Creates a new instance of DefUniqueItem.
	 * @param name name of parse item or null;
	 * @param chkAddr address of code of the method.
	 * @param itemIndex index of this key part
	 * @param parsedType type of id.
	 * @param optional if true this key value is not required
	 */
	public CodeParseItem(final String name,
		final int chkAddr,
		final int itemIndex,
		final short parsedType,
		final boolean optional) {
		_parseMethodAddr = chkAddr;
		_itemType = parsedType;
		_optional = optional;
		_name = name == null ? null : name.intern();
		_itemIndex = itemIndex;
		// _itemValue = null; // java mekes it
	}

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

	/** Get item index.
	 * @return item index.
	 */
	public final int getItemIndex() {return _itemIndex;}

	/** Get parsed object.
	 * @return value of parsed object or <tt>null</tt>.
	 */
	final XDValue getParsedObject() {return _itemValue;}

	/** Set parsed object.
	 * @param parsedValue the value of parsed object.
	 */
	public final void setParsedObject(XDValue parsedValue) {
		_itemValue = parsedValue;
	}

	final void setUniqueSet(final CodeUniqueSet set) {_uniqueset = set;}

	public final boolean isOptional() {return _optional;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public final short getItemId() {return CompileBase.PARSEITEM_VALUE;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.OBJECT;}
	@Override
	public String toString() {
		return "[" + _itemIndex + "]"
			+ (_name != null ? ":"+_name : "") + "=" + _itemValue;}
	@Override
	public final String stringValue() {return _itemValue.stringValue();}
	@Override
	public final XDValue cloneItem() {
		return new CodeParseItem(_name,
			_parseMethodAddr,_itemIndex,_itemType,_optional);
	}
	@Override
	public final boolean isNull() {return _parseMethodAddr==-1;}
	@Override
	public int hashCode() {
		return 3*_parseMethodAddr + _name==null?0:_name.hashCode();
	}
	@Override
	public boolean equals(final Object arg) {
		 return arg instanceof CodeParseItem ? eq((CodeParseItem) arg) : false;
	}
	@Override
	public final boolean equals(final XDValue arg) {
		return arg.getItemId() == CompileBase.PARSEITEM_VALUE ?
			false : eq((CodeParseItem)arg);
	}

	/** Check whether some other CodeParseItem object is "equal to" this one.
	 * @param arg other CodeParseItem object to which is to be compared.
	 * @return always <tt>false</tt>.
	 */
	private boolean eq(final CodeParseItem x) {
		return _parseMethodAddr == _parseMethodAddr &&
			_name == null ? x._name == null : _name.equals(x._name);
	}

}