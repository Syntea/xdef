package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.NULL;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.xon.XonTools;

/** The class DefJNull implements the internal object with XON/JSON null value.
 * @author Vaclav Trojan
 */
public final class DefJNull extends XDValueAbstract {
	/** The value of item. */
	private final XonTools.JNull _value;

	/** Creates a new instance of DefBoolean as null.*/
	public DefJNull() {_value = null;}

	/** Creates a new instance of DefBoolean
	 * @param value The initial value of object.
	 */
	public DefJNull(final XonTools.JNull value) {_value = value!=null ? XonTools.JNULL : null;}

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
	public short getItemId() {return XD_NULL;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return NULL;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return "null";}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return toString();}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefJNull(_value);}
	@Override
	public int hashCode() {return _value==null ? 0 : _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		return arg==null || arg instanceof XDValue ? ((XDValue) arg).isNull() : false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value of the object is comparable
	 * and equals to this one.
	 */
	public boolean equals(final XDValue arg) {return arg == null || arg.getItemId() == XD_NULL;}
	@Override
	/** Compares this object with the other DefBoolean object.
	 * @param arg other DefBoolean object to which is to be compared.
	 * @return returns 0 if this object is equal to the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (arg == null || arg.getItemId() == XD_NULL) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	/** Check if the object is null.
	 * @return true if the object is null otherwise return false.
	 */
	public boolean isNull() {return true;}
}