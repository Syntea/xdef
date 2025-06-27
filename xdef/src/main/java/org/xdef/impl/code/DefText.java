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
	public DefText(final Document doc, final String value) {_value = doc.createTextNode(value);}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public short getItemId() {return XD_TEXT;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return XDValueType.TEXT;}

	/** Get value as String.
	 * @return The string from value.
	 */
	@Override
	public String toString() {return _value == null ? "" : _value.getData();}

	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	@Override
	public String stringValue() {return toString();}

	@Override
	public boolean booleanValue() {
		String s = _value == null ? null : _value.getData();
		return s != null && !s.isEmpty();
	}

	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	@Override
	public XDValue cloneItem() {return new DefText(_value);}

	@Override
	public int hashCode() {return _value.hashCode();}

	@Override
	public boolean equals(final Object arg) {return (arg instanceof DefText) ? equals((DefText) arg) : false;}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value of the object is comparable and
	 * equals to this one.
	 */
	@Override
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_TEXT) {
			return false;
		}
		return _value.equals(((DefText) arg)._value);
	}

	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return 0 if the object is identical to the argument.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	@Override
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