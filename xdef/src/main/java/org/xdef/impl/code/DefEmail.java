package org.xdef.impl.code;

import org.xdef.XDEmail;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** Implements the internal object with Email value.
 * @author Vaclav Trojan
 */
public final class DefEmail extends XDValueAbstract implements XDEmail {

	/** Email source value. */
	private final String _value;
	/** Email domain. */
	private final String _domain;
	/** Email user. */
	private final String _localPart;
	/** Email user name. */
	private final String _userName;

	/** Creates a new instance of DefEmauil as null.*/
	public DefEmail() {this(null);}

	/** Creates a new instance of DefEmauil
	 * @param value The initial value of object.
	 */
	public DefEmail(final String value) {
		if (value == null || value.isEmpty()) {
			_value = _domain = _userName = null;
		} else {
			String s = value.trim();
			int angelBr = s.lastIndexOf('<');
			int x = angelBr >= 0 ? angelBr : 0;
			int domSep = s.indexOf('@', x);
			String domain = s.substring(domSep + 1). trim();
			String userName = s.substring(0, angelBr >= 0 ? angelBr : 0).trim();
			String localPart = s.substring(angelBr >= 0 ? angelBr + 1 : 0, domSep).trim();
			if (angelBr >= 0) {
				if (domain.endsWith(">")) {
					domain = domain.substring(0, domain.length() - 1). trim();
				} else {
					s = null;
				}
			}
			String emailregex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*"
				+ "@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
			if (s != null && (localPart + '@' + domain).matches(emailregex)) {
				_domain = domain;
				_localPart = localPart;
				_userName = userName;
				_value = s;
				return;
			}
		}
		//Incorrect value of &{0}&{1}&{: }
		throw new SRuntimeException(XDEF.XDEF809, "email");
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return this;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_EMAIL;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.EMAIL;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return isNull() ? "" : _value;}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return isNull()? null: toString();}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefEmail(_value);}

	@Override
	public int hashCode() {
		return isNull() ? 1 : _localPart.hashCode() + _domain.hashCode()*3;
	}

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
		} else if (arg == null || arg.isNull()) {
			return false;
		} else if (arg instanceof XDEmail) {
			return _localPart.equals(((XDEmail)arg).getLocalPart())
				&& _domain.equals(((XDEmail)arg).getDomain());
		}
		return false;
	}
	@Override
	/** Compares this object with the other DefEmail object.
	 * @param arg other DefEmail object to which is to be compared.
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

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDEmail interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getDomain() {return _domain;}
	@Override
	public String getLocalPart() {return _localPart;}
	@Override
	public String getUserName() {return _userName;}
	@Override
	public String getEmailAddr() {return _value;}
}