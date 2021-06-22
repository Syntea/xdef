package org.xdef.impl.code;

import java.io.File;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** Implements the internal object with File value.
 * @author Vaclav Trojan
 */
public final class DefFile extends XDValueAbstract {

	/** The file value. */
	private final File _value;

	/** Creates a new instance of DefFile as null.*/
	public DefFile() {_value = null;}

	/** Creates a new instance of DefFile
	 * @param value The initial value of object.
	 */
	public DefFile(final File value) {_value = value;}

	/** Creates a new instance of DefFile
	 * @param value The string with initial value of object ("true" or "false").
	 * @throws SRuntimeException if an error occurs.
	 */
	public DefFile(final String value) {
		try {
			_value = new File(value).getCanonicalFile();
		} catch (Exception ex) {
			//Incorrect value of '&{0}'&{1}{: }
			throw new SRuntimeException(XDEF.XDEF809, "file", value);
		}
	}

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
	public short getItemId() {return XDValueID.XD_FILE;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.FILE;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		return isNull() ? "" : _value.getAbsolutePath();
	}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return isNull()? null: toString();}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefFile(_value);}

	@Override
	public int hashCode() {return isNull() ? 1 : _value.hashCode();}

	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ?  equals((XDValue) arg) : false;
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
		if (arg == null || arg.isNull()) {
			return false;
		}
		return _value.equals(arg.getObject());
	}

	@Override
	/** Compares this object with the other DefFile object.
	 * @param arg other DefFile object to which is to be compared.
	 * @return returns 0 if this object is equal to the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (arg.getItemId() == XDValueID.XD_BOOLEAN) {
			if (equals(arg)) return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	@Override
	/** Check if the object is <i>null</i>.
	 * @return <i>true</i> if the object is <i>null</i> otherwise returns
	 * <i>false</i>.
	 */
	public boolean isNull() {return _value == null;}
}