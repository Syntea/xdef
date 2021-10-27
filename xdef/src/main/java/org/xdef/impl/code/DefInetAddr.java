package org.xdef.impl.code;

import java.net.InetAddress;
import org.xdef.XDInetAddr;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** Internet address.
 * @author Vaclav Trojan
 */
public class DefInetAddr extends XDValueAbstract implements XDInetAddr {
	/** Value of InetAddress. */
	private final InetAddress _value;

	/** Create new instance null DefInetAddr. */
	public DefInetAddr() {_value = null;}

	/** Create new instance of DefInetAddr from source string.
	 * @param inetAddr String representation of InternetAddress.
	 */
	public DefInetAddr(final String inetAddr) {
		try {
			_value = InetAddress.getByName(inetAddr);
		} catch (Exception ex) {
			throw new SRuntimeException(ex);
		}
	}

	/** Create new instance of DefInetAddr with InetAddress.
	 * @param inetAddr InternetAddress.
	 */
	public DefInetAddr(final InetAddress inetAddr) {_value = inetAddr;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDInetAddr interface
////////////////////////////////////////////////////////////////////////////////

	@Override
		/**	Get the raw IP address in a string format.
	 * @return raw IP address in a string format.
	 */
	public String getHostAddress() {
		return _value != null ? _value.getHostAddress() : null;
	}

	/** Get bytes from InetAddress.
	 * @return bytes from InetAddress.
	 */
	@Override
	public byte[] getBytes() {
		return _value != null ? _value.getAddress(): null;
	}

	@Override
	/**	Check if IP address of this InetAddress object is IPv6.
	 * @return true if IP address of this InetAddress object is IPv6.
	 */
	public boolean isIPv6() {
		return _value == null ? false : _value.getAddress().length == 16;
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
		if (arg instanceof DefInetAddr) {
			DefInetAddr x = (DefInetAddr) arg;
			return _value != null ? _value.equals(x._value) : x._value == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefInetAddr) {
			if (this.equals((DefInetAddr) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_INETADDR;}
	@Override
	public XDValueType getItemType() {return XDValueType.INETADDR;}
	@Override
	public String stringValue() {
		return isNull() ? "null" :_value.toString().substring(1);
	}
	@Override
	public boolean isNull() {return _value == null;}
	@Override
	public InetAddress getObject() {return _value;}
	@Override
	public String toString() {return stringValue();}
}