package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.NAMEDVALUE;

/** Implementation of XDNamedValue (key, value)
 * @author Vaclav Trojan
 */
public final class DefNamedValue extends XDValueAbstract implements XDNamedValue {
	private final String _name;
	private XDValue _value;

	public DefNamedValue() {_name=null;setValue(null);}

	public DefNamedValue(String key, XDValue value) {_name=key;setValue(value);}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDNamedValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get ID of the type of value (org.xdef.XDValueTypes.NAMED_VALUE).
	 * @return item type (org.xdef.XDValueTypes.NAMED_VALUE).
	 */
	@Override
	public short getItemId() {return XD_NAMEDVALUE;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return NAMEDVALUE;}

	/** Get key name of pair.
	 * @return key name of pair.
	 */
	@Override
	public String getName() {return _name;}

	/** Get value of pair.
	 * @return value of pair.
	 */
	@Override
	public XDValue getValue() {return _value;}

	/** Set value of pair.
	 * @param newValue new value of pair.
	 * @return original value of pair.
	 */
	@Override
	public final XDValue setValue(final XDValue newValue) {
		XDValue result = _value;
		_value = newValue == null ? new DefNull() : newValue;
		return result;
	}

///////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}

	@Override
	public final XDValue cloneItem() {return new DefNamedValue(_name,_value==null? null: _value.cloneItem());}

	@Override
	public boolean equals(final Object arg) {return arg instanceof XDValue ? equals((XDValue) arg) : false;}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if result value is the same type and if it is equal* to the result value of the argument;
	 * otherwise return false.
	 */
	@Override
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_NAMEDVALUE) {
			return false;
		}
		return compareTo(arg) == 0;
	}

	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return 0, 1, -1.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	@Override
	public int compareTo(final XDValue arg) {
		if (arg.getItemId() != XD_NAMEDVALUE) {
			if (_value == null) {
				throw new SIllegalArgumentException(SYS.SYS085); //Incomparable arguments
			}
			return _value.compareTo(arg);
		}
		XDValue xv = ((XDNamedValue) arg).getValue();
		if (_value == null) {
			if (xv == null) {
				return 0;
			}
			throw new SIllegalArgumentException(SYS.SYS085); //Incomparable arguments
		}
		return _value.compareTo(xv);
	}

	@Override
	public String toString() {return "%" + _name + "=" + _value;}

	@Override
	public byte byteValue() {return _value == null ? 0 : ((XDValue)_value).byteValue();}

	@Override
	public short shortValue() {return _value == null ? 0 : ((XDValue)_value).shortValue();}

	@Override
	public int intValue() {return _value == null ? 0 : ((XDValue)_value).intValue();}

	@Override
	public long longValue() {return _value == null ? 0 : ((XDValue)_value).longValue();}

	@Override
	public float floatValue() {return _value == null ? 0 : ((XDValue)_value).floatValue();}

	@Override
	public double doubleValue() {return _value == null ? 0 : ((XDValue)_value).doubleValue();}

	@Override
	public BigDecimal decimalValue() {return _value == null ? null : ((XDValue)_value).decimalValue();}

	@Override
	public String stringValue() {return _value == null ? null : ((XDValue)_value).stringValue();}

	@Override
	public SDatetime datetimeValue() {return _value == null ? null : ((XDValue)_value).datetimeValue();}

	@Override
	public SDuration durationValue() {return _value == null ? null : ((XDValue)_value).durationValue();}
}
