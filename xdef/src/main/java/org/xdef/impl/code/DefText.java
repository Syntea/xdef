package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.xdef.XDValueType;

/** Implementation of script value with org.w3c.dom.CharacterData node.
 * @author  Vaclav Trojan
 */
public final class DefText extends XDValueAbstract {

	/** CharacterData value of this item. */
	private final CharacterData _value;

	/** Creates a new instance of DefText */
	DefText() {_value = null;}

	/** Creates a new instance of DefText with value from the argument.
	 * @param value the CharacterData value.
	 */
	public DefText(final CharacterData value) {_value = value;}

	/** Creates a new instance of DefText with value created from arguments.
	 * @param doc the Document where data is to be created.
	 * @param value string value.
	 */
	public DefText(final Document doc, final String value) {
		_value = doc.createTextNode(value);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_TEXT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.TEXT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value == null ? "" : _value.getData();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}
	@Override
	public boolean booleanValue() {
		String s = _value == null ? null : _value.getData();
		return s != null && !s.isEmpty();
	}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefText(_value);}
	@Override
	public int hashCode() {return _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		return (arg instanceof DefText) ? equals((DefText) arg) : false;
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
		if (arg == null || arg.isNull() || arg.getItemId() != XD_TEXT) {
			return false;
		}
		return _value.equals(((DefText) arg)._value);
	}
	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return 0 if the object is identical to the argument.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (equals(arg)) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	/** Compares this object with the other DefText object.
	 * @param arg other DefText object to which is to be compared.
	 * @return 0 if the object is identical to the argument.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final DefText arg) throws SIllegalArgumentException {
		if (equals(arg)) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
}