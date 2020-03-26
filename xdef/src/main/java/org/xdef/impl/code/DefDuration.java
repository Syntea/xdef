package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDuration;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDDuration;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;

/** The class DefDate implements the internal object with duration value.
 * @author Vaclav Trojan
 */
public final class DefDuration extends XDValueAbstract implements XDDuration {

	/** value of this object */
	private SDuration _value;

	/** Creates a new instance of DefDuration. The value is set to the current
	 * time.
	 */
	public DefDuration() {_value = null;}

	/** Creates a new instance of DefDuration. The value is set from
	 * parameter.
	 * @param value The duration value.
	 */
	public DefDuration(final SDuration value) {_value = value;}

	/** Creates a new instance of DefDuration. The value is set from
	 * parameter.
	 * @param value The string with duration value.
	 * @throws SRuntimeException if value is incorrect.
	 */
	public DefDuration(final String value) throws SRuntimeException {
		_value = new SDuration(value);
	}

	@Override
	public SDuration durationValue() {return _value;}
	@Override
	/** Set duration.
	 * @param value SDuration object.
	 */
	public void setDuration(SDuration value) {_value = value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getObject() {return _value;}
	@Override
	public short getItemId() {return XD_DURATION;}
	@Override
	public XDValueType getItemType() {return XDValueType.DURATION;}
	@Override
	public boolean isNull() {return _value == null;}
	@Override
	/** Get value as String.
	 * @return ISO8601 string value of this object or "null".
	 */
	public String toString() {return _value==null ? "" : _value.toString();}
	@Override
	public String stringValue() {return toString();}
	@Override
	public XDValue cloneItem() {
		return _value == null ?
			new DefDuration() : new DefDuration(new SDuration(_value));
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
		if (arg == null || arg.isNull() || arg.getItemId() != XD_DURATION) {
			return false;
		}
		return _value.equals(arg.durationValue());
	}
	@Override
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (arg.getItemId() == XD_DURATION) {
			return _value.compareTo(arg.durationValue());
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
}