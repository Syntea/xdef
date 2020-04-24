package org.xdef.impl.code;

import org.xdef.sys.StringParser;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;

/** The class DefString implements items with string values.
 * @author Vaclav Trojan
 */
public final class DefString extends XDValueAbstract {
	/** The string value of this item. */
	private String _value;

	/** Creates a new instance of empty DefString. */
	public DefString() {}

	/** Creates a new instance of DefString
	 * @param value The value to be assigned with the item.
	 */
	public DefString(final String value) {_value = value;}

	/** Get value of item as String representation of value in the form of
	 * a X-definition script.
	 * @return The string representation of value of the object.
	 */
	public String sourceValue() {
		if (_value == null) {return "null";}
		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		for (int i=0; i < _value.length(); i++) {
			char c;
			switch (c = _value.charAt(i)) {
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\'':
					sb.append("\\'");
					break;
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				default:
					if (StringParser.getXmlCharType(c, (byte) 10) == 0) {
						sb.append(c); //TODO ???
					} else {
						sb.append(c);
					}
			}
		}
		return sb.append('\'').toString();
	}

	void setNull() {_value = null;}

	int length() {return _value == null? 0 : _value.length();}

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
	public short getItemId() {return XD_STRING;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.STRING;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value == null ? "" : _value;}
	@Override
	public char charValue() {
		return isNull() || _value.length() != 1 ? 0 : _value.charAt(0);
	}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return _value;}
	@Override
	public boolean booleanValue() {return _value != null && !_value.isEmpty();}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefString(_value);}
	@Override
	public int hashCode() {return _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof XDValue) {
			return equals(((XDValue) arg));
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
		if (arg == null || arg.isNull() || arg.getItemId() != XD_STRING) {
			return false;
		}
		return _value.equals(arg.stringValue());
	}
	/** Check whether some other DefString object is "equal to" this one.
	 * @param arg other DefString object to which is to be compared.
	 * @return true if the value of the argument is equal to this one.
	 */
	public boolean equals(final DefString arg) {
		return _value == null ? arg._value == null : _value.equals(arg._value);}
	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return If both objects are comparable then returns -1, 0, or a 1
	 * as this XDValue object is less than, equal to, or greater than the
	 * specified object. If both objects are not comparable the return
	 * <tt>Integer.MIN_VALUE</tt>.
	 */
	public int compareTo(final XDValue arg) {
		return _value == null ? arg.stringValue() == null ? 0 : -1 :
				arg.stringValue() == null ?
					1 : _value.compareTo(arg.stringValue());
	}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _value == null;}
}