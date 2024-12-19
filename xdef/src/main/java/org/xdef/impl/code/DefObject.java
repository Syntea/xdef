package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.OBJECT;

/** DefObject
 * @author  Vaclav Trojan
 */
public final class DefObject extends XDValueAbstract {
	/** The value associated with this item. */
	private Object _value;

	/** Creates a new empty instance of DefInteger
	 */
	public DefObject() {_value = null;}

	/** Creates a new instance of DefInteger
	 * @param value The object.
	 */
	public DefObject(Object value) {_value = value;}

	/** Set object value.
	 * @param obj object to be set as value.
	 */
	public void setObject(final Object obj) {_value = obj;}

	@Override
	/** Get object representing value */
	public Object getObject() {return _value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_OBJECT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return OBJECT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value == null ? "" : _value.toString();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return toString();}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefObject(_value);}
	@Override
	public int hashCode() {return _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		return arg instanceof DefObject ? _value.equals(((DefObject) arg)._value) : false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true ig objects are equal.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_OBJECT) {
			return false;
		}
		return _value.equals(((DefObject) arg)._value);
	}
}