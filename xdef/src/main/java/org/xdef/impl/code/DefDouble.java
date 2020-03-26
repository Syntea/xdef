package org.xdef.impl.code;

import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import java.math.BigInteger;

/** Implements the internal object with float values.
 * @author Vaclav Trojan
 */
public final class DefDouble extends XDValueAbstract {

	/** The value associated with this item. */
	private final double _value;
	private final boolean _isNull;

	/** Creates a new instance of DefFloat as null.*/
	public DefDouble() {_isNull = true; _value = Double.NaN;}

	/** Creates a new instance of DefFloat from double value.
	 * @param value The value to be assigned to the object.
	 */
	public DefDouble(final double value) {_value = value; _isNull = false;}

	/** Creates a new instance of DefFloat from source string.
	 * @param source The string with float number to be assigned to the object.
	 */
	public DefDouble(final String source) {
		_value = "NaN".equals(source) ? Double.NaN :
			"INF".equals(source) ? Double.POSITIVE_INFINITY :
			"-INF".equals(source) ? Double.NEGATIVE_INFINITY :
			Double.parseDouble(source);
		 _isNull = false;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
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
	public short getItemId() {return XDValueID.XD_FLOAT;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.FLOAT;}

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
	public String stringValue() {
		return isNull() ? "" :
			_value == Double.NEGATIVE_INFINITY ? "-INF" :
			_value == Double.POSITIVE_INFINITY ? "INF" :
			_value == Double.NaN ? "NaN" : String.valueOf(_value);
	}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefDouble(_value);}

	@Override
	public int hashCode() {
		long bits = Double.doubleToLongBits(_value);
		return (int)(bits ^ (bits >>> 32));
	}

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
		return Double.isNaN(_value)
			? Double.isNaN(arg.doubleValue()) :  _value == arg.doubleValue();
	}
	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return If both objects are comparable then returns -1, 0, or a 1
	 * as this XDValue object is less than, equal to, or greater than the
	 * specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		return Double.compare(_value, arg.doubleValue());
	}
	@Override
	public byte byteValue() {return (byte) _value;}
	@Override
	public short shortValue() {return (short) _value;}
	@Override
	public int intValue() {return (int) _value;}
	@Override
	public long longValue() {return (long) _value;}
	@Override
	public float floatValue() {return (float) _value;}
	@Override
	public double doubleValue() {return _value;}
	@Override
	public BigDecimal decimalValue() {
		return isNull() ? null : new BigDecimal(_value);
	}
	@Override
	public BigInteger integerValue() {
		return isNull() ? null : decimalValue().toBigInteger();
	}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _isNull;}

}