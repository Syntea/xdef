package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import org.xdef.json.XonTools;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;

/** The class DefChar implements the internal object with char value.
 * @author Vaclav Trojan
 */
public final class DefChar extends XDValueAbstract {
	/** The char value of item. */
	private final char _value;
	private final boolean _isNull;

	/** Creates a new instance of DefChar as null.*/
	public DefChar() {_isNull = true; _value = SParser.NOCHAR;}

	/** Creates a new instance of DefChar from character.
	 * @param value The initial value of object.
	 */
	public DefChar(final char value) {_value = value; _isNull = false;}

	/** Creates a new instance of DefChar from integer number.
	 * @param value The initial value of object.
	 */
	public DefChar(final long value) {_value = (char) value; _isNull = false;}

	/** Creates a new instance of DefChar
	 * @param s string representing a character.
	 */
	public DefChar(final String s) {
		int i = XonTools.readJChar(new StringParser(s));
		_value = (i < 0) ? SParser.NOCHAR : (char) i;
		_isNull = false;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _isNull? null : _value;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_CHAR;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.CHAR;}

	@Override
	/** Return DefBoolean object as char.
	 * @return the DefBoolean object as char.
	 */
	public char charValue() {return isNull() ? SParser.NOCHAR : _value;}

	@Override
	public byte byteValue() {return (byte) charValue();}

	@Override
	public short shortValue() {return (short) charValue();}

	@Override
	public int intValue() {return charValue();}

	@Override
	public long longValue() {return charValue();}

	@Override
	public float floatValue() {return charValue();}

	@Override
	public double doubleValue() {return charValue();}

	@Override
	public BigDecimal decimalValue() {
		return isNull() ? null : new BigDecimal(_value);
	}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {
		return  isNull() ?"":XonTools.jstringToSource(String.valueOf(_value));
	}

	@Override
	/** Compares this object with the other DefBoolean object.
	 * @param arg other DefBoolean object to which is to be compared.
	 * @return returns 0 if this object is equal to the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (equals(arg)) {
			return 0;
		}
		if (arg != null) {
			return _value < arg.charValue() ? -1 : 1;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	@Override
	/** Check if the object is <i>null</i>.
	 * @return <i>true</i> if the object is <i>null</i> otherwise returns
	 * <i>false</i>.
	 */
	public boolean isNull() {return _isNull;}

	@Override
	public int hashCode() {return isNull() ? -1 : _value;}

	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ? equals((XDValue) arg) : false;
	}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		return isNull() ? arg == null || arg.isNull()
			: charValue() == arg.charValue();
	}

	@Override
	/** Get String created from the value of this object. */
	public String toString() {return String.valueOf(_value);}
}