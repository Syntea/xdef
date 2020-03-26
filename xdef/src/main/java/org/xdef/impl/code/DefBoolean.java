package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueID;
import org.xdef.XDValueType;

/** The class DefBoolean implements the internal object with boolean value.
 * @author Vaclav Trojan
 */
public final class DefBoolean extends XDValueAbstract {

	/** The boolean value of item. */
	private final boolean _value;
	private final boolean _isNull;

	/** Creates a new instance of DefBoolean as null.*/
	public DefBoolean() {_isNull = true; _value = false;}

	/** Creates a new instance of DefBoolean
	 * @param value The initial value of object.
	 */
	public DefBoolean(final boolean value) {_value = value; _isNull = false;}

	/** Creates a new instance of DefBoolean
	 * @param value The string with initial value of object ("true" or "false").
	 */
	public DefBoolean(final String value) {
		_value = Boolean.getBoolean(value);
		_isNull = false;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {
		return _isNull? null : _value ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_BOOLEAN;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.BOOLEAN;}

	@Override
	/** Return DefBoolean object as boolean.
	 * @return the DefBoolean object as boolean.
	 */
	public boolean booleanValue() {return _value;}
	@Override
	public byte byteValue() {return _value ? (byte) 1 : (byte) 0;}
	@Override
	public short shortValue() {return _value ? (short) 1 : (short) 0;}
	@Override
	public int intValue() {return _value ? 1 : 0;}
	@Override
	public long longValue() {return _value ? 1 : 0;}
	@Override
	public float floatValue() {return _value ? 1 : 0;}
	@Override
	public double doubleValue() {return _value ? 1 : 0;}
	@Override
	public BigDecimal decimalValue() {
		return isNull() ? null : _value ? BigDecimal.ONE : BigDecimal.ZERO;
	}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return isNull() ? "":_value ? "true":"false";}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return isNull()? null: String.valueOf(_value);}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefBoolean(_value);}

	@Override
	public int hashCode() {return _value ? 1 : 3;}

	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ?  equals((XDValue) arg) : false;
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
		return _value == arg.booleanValue();
	}

	@Override
	/** Compares this object with the other DefBoolean object.
	 * @param arg other DefBoolean object to which is to be compared.
	 * @return returns 0 if this object is equal to the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (arg.getItemId() == XDValueID.XD_BOOLEAN) {
			if (_value == arg.booleanValue()) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _isNull;}
}