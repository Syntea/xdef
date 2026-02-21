package org.xdef.impl.code;

import org.xdef.XDObject;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.OBJECT;

/** DefObject
 * @author  Vaclav Trojan
 */
public final class DefObject extends XDValueAbstract implements XDObject{

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
	@Override
	public void setObject(final Object obj) {_value = obj;}

	/** Get object representing value */
	@Override
	public Object getObject() {return _value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return OBJECT;}

	/** Get value as String.
	 * @return The string from value.
	 */
	@Override
	public String toString() {return _value == null ? "" : _value.toString();}

	/** Get string value of this object.
	 * @return string value of this object.
	 */
	@Override
	public String stringValue() {return toString();}

	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	@Override
	public XDValue cloneItem() {return new DefObject(_value);}

	@Override
	public int hashCode() {return _value.hashCode();}

	@Override
	public boolean equals(final Object arg) {
		return arg instanceof DefObject ? _value.equals(((DefObject) arg)._value) : false;
	}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true ig objects are equal.
	 */
	@Override
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_OBJECT) {
			return false;
		}
		return _value.equals(((DefObject) arg)._value);
	}

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public short getItemId() {return XD_OBJECT;}
}