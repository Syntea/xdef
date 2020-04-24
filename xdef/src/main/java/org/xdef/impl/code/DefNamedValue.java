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

/** Implementation of XDNamedValue (key, value)
 * @author Vaclav Trojan
 */
public final class DefNamedValue extends XDValueAbstract
	implements XDNamedValue {

	private final String _name;
	private XDValue _value;

	public DefNamedValue() {_name=null;setValue(null);}

	public DefNamedValue(String key, XDValue value) {_name=key;setValue(value);}

	@Override
	/** Get ID of the type of value (org.xdef.XDValueTypes.NAMED_VALUE).
	 * @return item type (org.xdef.XDValueTypes.NAMED_VALUE).
	 */
	public short getItemId() {return XD_NAMEDVALUE;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.NAMEDVALUE;}

	@Override
	/** Get key name of pair.
	 * @return key name of pair.
	 */
	public String getName() {return _name;}

	@Override
	/** Get value of pair.
	 * @return value of pair.
	 */
	public XDValue getValue() {return _value;}

	@Override
	/** Set value of pair.
	 * @param newValue new value of pair.
	 * @return original value of pair.
	 */
	public final XDValue setValue(final XDValue newValue) {
		XDValue result = _value;
		_value = newValue == null ? new DefNull() : newValue;
		return result;
	}
	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public final XDValue cloneItem() {
		return new DefNamedValue(
			_name, _value == null ? null : _value.cloneItem());
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
	 * @return <tt>true</tt> if result value is the same type and if it is equal
	 * to the result value of the argument; otherwise return <tt>false</tt>.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_NAMEDVALUE) {
			return false;
		}
		return compareTo(arg) == 0;
	}
	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return 0, 1, -1.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) {
		if (arg.getItemId() != XD_NAMEDVALUE) {
			if (_value == null) {
				//Incomparable arguments
				throw new SIllegalArgumentException(SYS.SYS085);
			}
			return _value.compareTo(arg);
		}
		XDValue xv = ((XDNamedValue) arg).getValue();
		if (_value == null) {
			if (xv == null) {
				return 0;
			}
			//Incomparable arguments
			throw new SIllegalArgumentException(SYS.SYS085);
		}
		return _value.compareTo(xv);
	}
	@Override
	public String toString() {return "%" + _name + "=" + _value;}
	@Override
	public byte byteValue() {
		return _value == null ? 0 : ((XDValue)_value).byteValue();
	}
	@Override
	public short shortValue() {
		return _value == null ? 0 : ((XDValue)_value).shortValue();
	}
	@Override
	public int intValue() {
		return _value == null ? 0 : ((XDValue)_value).intValue();
	}
	@Override
	public long longValue() {
		return _value == null ? 0 : ((XDValue)_value).longValue();
	}
	@Override
	public float floatValue() {
		return _value == null ? 0 : ((XDValue)_value).floatValue();
	}
	@Override
	public double doubleValue() {
		return _value == null ? 0 : ((XDValue)_value).doubleValue();
	}
	@Override
	public BigDecimal decimalValue() {
		return _value == null ? null : ((XDValue)_value).decimalValue();
	}
	@Override
	public String stringValue() {
		return _value == null ? null : ((XDValue)_value).stringValue();
	}
	@Override
	public SDatetime datetimeValue() {
		return _value == null ? null : ((XDValue)_value).datetimeValue();
	}
	@Override
	public SDuration durationValue() {
		return _value == null ? null : ((XDValue)_value).durationValue();
	}
}