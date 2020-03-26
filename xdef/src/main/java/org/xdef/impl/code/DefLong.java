package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueType;
import java.math.BigInteger;

/** The class DefLong implements the internal object with integer values.
 * @author Vaclav Trojan
 */
public final class DefLong extends XDValueAbstract {

	/** The value associated with this item. */
	private final long _value;
	private final boolean _isNull;

	/** Creates a new instance of DefInteger as null.
	 */
	public DefLong() {_isNull = true; _value = 0;}

	/** Creates a new instance of DefInteger
	 * @param value The integer value.
	 */
	public DefLong(final long value) {_value = value; _isNull = false;}

	/** Creates a new instance of DefInteger
	 * @param value The string with integer value.
	 */
	public DefLong(final String value) {
		_value = Long.parseLong(value);
		_isNull = false;
	}

////////////////////////////////////////////////////////////////////////////////
//  Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return isNull() ? null : _value;}
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_INT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.INT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return stringValue();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return isNull() ? "" : String.valueOf(_value);}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefLong(_value);}
	@Override
	public int hashCode() {return (int) _value;}
	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ? equals((XDValue) arg) :  false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return <tt>true</tt> if argument is same type as this XDValue and the
	 * value of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull()) {
			return false;
		}
		return _value == arg.longValue();
	}
	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return If both objects are comparable then returns -1, 0, or a 1
	 * as this XDValue object is less than, equal to, or greater than the
	 * specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) {
		return _value==arg.longValue() ? 0 : _value < arg.longValue() ? -1 : 1;
	}
	@Override
	public byte byteValue() {return (byte) _value;}
	@Override
	public short shortValue() {return (short) _value;}
	@Override
	public int intValue() {return (int) _value;}
	@Override
	public long longValue() {return _value;}
	@Override
	public float floatValue() {return _value;}
	@Override
	public double doubleValue() {return _value;}
	@Override
	public BigDecimal decimalValue() {
		return isNull() ? null : new BigDecimal(_value);}
	@Override
	public BigInteger integerValue() {
		return isNull() ? null : BigInteger.valueOf(_value);
	}
	@Override
	public boolean booleanValue() {return isNull() ? false : _value != 0;}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _isNull;}
}