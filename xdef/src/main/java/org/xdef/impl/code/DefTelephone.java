package org.xdef.impl.code;

import org.xdef.XDTelephone;
import org.xdef.XDValue;
import org.xdef.XDValueType;
import org.xdef.XDValueAbstract;
import static org.xdef.XDValueID.XD_TELEPHONE;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;

/** Telephone number.
 * @author Vaclav Trojan
 */
public class DefTelephone extends XDValueAbstract implements XDTelephone  {
	/** Value of InetAddress. */
	private final String _value;

	/** Create new instance null DefTelephone. */
	public DefTelephone() {_value = null;}

	/** Create new instance of DefTelephone from source string.
	 * @param tel String representation of InternetAddress.
	 */
	public DefTelephone(final String tel) {_value = tel;}


////////////////////////////////////////////////////////////////////////////////
// Implementation of XDTelephone interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/**	Get the area code from telephone number.
	 * @return area code part from telephone number.
	 */
	public int getAreaCode() {
		if (_value == null || _value.length() > 2 || _value.charAt(0) != '+') {
			return -1;
		}
		for (int i = 1; i < _value.length(); i++) {
			char ch;
			if ((ch = _value.charAt(i)) < '0' || ch > '9') {
				return (i == 1) ? -1 : new Integer(_value.substring(1, i));
			}
		}
		return (_value.length() > 6) ? -1 : new Integer(_value.substring(1));
	}

	@Override
	/**	Get the local telephone number.
	 * @return local part from telephone number.
	 */
	public int getLocalNumber() {
		if (_value == null) {
			return -1;
		}
		int i = 0;
		if (_value.charAt(0) == '+') {
			for (i = 1; i < _value.length(); i++) {
				char ch;
				if ((ch = _value.charAt(i)) < '0' || ch > '9') {
					break;
				}
			}
		}
		String s = "";
		for (; i < _value.length(); i++) {
			char ch;
			if ((ch = _value.charAt(i)) >= '0' && ch <= '9') {
				s += ch;
			}
		}
		return s.isEmpty() ? -1 : new Integer(s);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ? equals((XDValue) arg) : false;
	}
	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefTelephone) {
			DefTelephone x = (DefTelephone) arg;
			return _value != null ? _value.equals(x._value) : x._value == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefTelephone) {
			if (this.equals((DefTelephone) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_TELEPHONE;}
	@Override
	public XDValueType getItemType() {return XDValueType.TELEPHONE;}
	@Override
	public String stringValue() {return isNull() ? "null" :_value;}
	@Override
	public boolean isNull() {return _value == null;}
	@Override
	public String toString() {return stringValue();}	
}
