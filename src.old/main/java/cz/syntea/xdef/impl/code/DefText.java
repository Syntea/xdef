package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.sys.SIllegalArgumentException;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

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
	public short getItemId() {return XDValueID.XD_TEXT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.TEXT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value.getData();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return _value == null ? "" : _value.getData();}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefText(_value);}
	@Override
	public int hashCode() {return _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof DefText) {
			return equals((DefText) arg);
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
		return arg.getItemId() != XDValueID.XD_TEXT ?
			false : equals((DefText) arg);
	}
	/** Check whether some other DefText object is "equal to" this one.
	 * @param arg other DefText object to which is to be compared.
	 * @return true if the value of the argument is equal to this one.
	 */
	public boolean equals(final DefText arg) {return _value.equals(arg._value);}
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