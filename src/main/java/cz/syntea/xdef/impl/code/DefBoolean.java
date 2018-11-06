package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.sys.SIllegalArgumentException;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import java.math.BigDecimal;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

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
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value ? "true" : "false";}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return String.valueOf(_value);}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefBoolean(_value);}

	@Override
	public int hashCode() {return _value ? 1 : 3;}

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
		if (arg.getItemId() != XDValueID.XD_BOOLEAN) {
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
	public int intValue() {return _value ? 1 : 0;}
	@Override
	public long longValue() {return _value ? 1 : 0;}
	@Override
	public float floatValue() {return _value ? 1 : 0;}
	@Override
	public double doubleValue() {return _value ? 1 : 0;}
	@Override
	public BigDecimal decimalValue() {return new BigDecimal(_value ? 1 : 0);}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _isNull;}

}