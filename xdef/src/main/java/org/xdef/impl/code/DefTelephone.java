package org.xdef.impl.code;

import org.xdef.XDTelephone;
import org.xdef.XDValue;
import org.xdef.XDValueType;
import org.xdef.XDValueAbstract;
import static org.xdef.XDValueID.XD_TELEPHONE;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SParser;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;

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
	 * @throws SRuntimeException in telephone number is incorrect.
	 */
	public DefTelephone(final String tel) {
		_value = tel != null ? parseTelephone(new StringParser(tel)) : null;
		if (_value == null) {
			throw new SRuntimeException(XDEF.XDEF809, "telehone");
		}
	}

	/** Check local part of telephone number and return string with telephone
	 * number.
	 * @param p parser (position is after area code).
	 * @param wasQuote true if number is in quotation marks.
	 * @return string with telephone number.
	 */
	private static String localPart(final SParser p,
		final int pos1,
		final boolean wasQuote) {
		while (p.isInteger()) {
			if (p.eos() || wasQuote && p.isChar('"')) {
				return p.getBufferPart(pos1,
					wasQuote ? p.getIndex() - 1 : p.getIndex());
			} else if (!p.isSpace()) {
				break;
			}
		}
		return null; // nol XDTelephone
	}

	/** Parse telephone number.
	 * @param p SParser with the telephone number.
	 * @return string with telephone number.
	 */
	public static final String parseTelephone(final SParser p) {
		boolean wasQuote = p.isChar('"') || p.isToken("T\"");
		int pos1 = p.getIndex();
		if (p.isChar('+')) {
			p.isInteger();
			p.isSpace();
		}
		return localPart(p, pos1, wasQuote);
	}


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
				return (i == 1) ? -1 : Integer.parseInt(_value.substring(1, i));
			}
		}
		return (_value.length()>6) ? -1 : Integer.parseInt(_value.substring(1));
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
		return s.isEmpty() ? -1 : Integer.parseInt(s);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getObject() {return this;}
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
	public String stringValue() {return isNull() ? "" : _value;}
	@Override
	public boolean isNull() {return _value == null;}
	@Override
	public String toString() {return stringValue();}
}