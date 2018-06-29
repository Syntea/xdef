/*
 * File: DefDecimal.java
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

import cz.syntea.xdef.sys.SIllegalArgumentException;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import java.math.BigDecimal;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

/** Implements the internal object with BigDecimal values.
 * @author Vaclav Trojan
 */
public final class DefDecimal extends XDValueAbstract {

	/** The value associated with this item. */
	private final BigDecimal _value;

	/** Creates a new instance of DefInteger.*/
	public DefDecimal() {_value = null;}

	/** Creates a new instance of DefInteger
	 * @param value The integer value.
	 */
	public DefDecimal(final BigDecimal value) {_value = value;}

	/** Creates a new instance of DefInteger
	 * @param value The string with integer value.
	 */
	public DefDecimal(final String value) {_value = new BigDecimal(value);}

	/** Creates a new instance of DefInteger
	 * @param value The integer value.
	 */
	public DefDecimal(final long value) {_value = new BigDecimal(value);}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _value;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_DECIMAL;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.DECIMAL;}

	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _value == null;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		return _value == null ? "null" : String.valueOf(_value);
	}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {
		return _value == null ? new DefDecimal() :
			new DefDecimal(new BigDecimal(_value.toString()));
	}

	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}

	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof XDValue) {
			return equals((XDValue) arg);
		}
		return false;
	}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull()) {
			return false;
		}
		return _value.equals(arg.decimalValue());
	}

	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return If both objects are comparable then returns -1, 0, or a 1
	 * as this XDValue object is less than, equal to, or greater than the
	 * specified object.
	 * @throws SIllegalArgumentException if objects are not comparable.
	 */
	public int compareTo(XDValue arg) throws SIllegalArgumentException {
		return _value.compareTo(arg.decimalValue());
	}

	@Override
	public byte byteValue() {return _value.byteValue();}
	@Override
	public short shortValue() {return _value.shortValue();}
	@Override
	public int intValue() { return _value.intValue();}
	@Override
	public long longValue() {return _value.longValue();}
	@Override
	public float floatValue() {return _value.floatValue();}
	@Override
	public double doubleValue() {return _value.doubleValue();}
	@Override
	public BigDecimal decimalValue() {return _value;}
	@Override
	public boolean booleanValue() {return false;}

}