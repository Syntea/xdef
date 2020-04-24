package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDDatetime;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.util.Calendar;
import org.xdef.XDValueType;

/** The class DefDate implements the internal object with date value.
 * @author Vaclav Trojan
 */
public final class DefDate extends XDValueAbstract implements XDDatetime {

	/** value of this object */
	private SDatetime _value;

	/** Creates a new instance of DefDate, date and time is set to zeroes. */
	public DefDate() {}

	/** Creates a new instance of DefDate. The value is set from
	 * parameter.
	 * @param value The initial date and time value
	 */
	public DefDate(final SDatetime value) {_value = value;}

	/** Creates a new instance of DefDate. The value is set from
	 * parameter.
	 * @param value The initial date and time value
	 */
	public DefDate(final String value) {_value = new SDatetime(value);}
	/** Creates a new instance of DefDate. The value is set from
	 * parameter.
	 * @param value The initial date and time value
	 */
	public DefDate(final Calendar value) {_value = new SDatetime(value);}

	@Override
	/** Return the value of DefDate object.
	 * @return the SDatetime value of this item.
	 */
	public SDatetime datetimeValue() {return _value;}
	@Override
	/** Set datetime.
	 * @param value SDatetime object.
	 */
	public void setDatetime(final SDatetime value) {_value = value;}
	@Override
	/** Set datetime.
	 * @param value SDatetime object.
	 */
	public void setCalendar(final Calendar value) {
		_value = new SDatetime(value);
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
	public short getItemId() {return XD_DATETIME;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.DATETIME;}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _value == null;}
	@Override
	/** Get value as String.
	 * @return ISO8601 string created from value or "null".
	 */
	public String toString() {return _value == null ? "" : _value.toISO8601();}
	@Override
	/** Get ISO8601 string value of this object.
	 * @return ISO8601 string created from value or "null".
	 */
	public String stringValue() {return toString();}
	@Override
	public XDValue cloneItem() {
		return _value == null ?
			new DefDate() : new DefDate((SDatetime) _value.clone());
	}
	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		return (arg instanceof XDValue) ? equals((XDValue) arg) : false;
	}
	@Override
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_DATETIME) {
			return false;
		}
		return _value.equals(arg.datetimeValue());
	}

	@Override
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (arg.getItemId() == XD_DATETIME) {
			try {
				return _value.compareTo(arg.datetimeValue());
			} catch (RuntimeException ex) {}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
}