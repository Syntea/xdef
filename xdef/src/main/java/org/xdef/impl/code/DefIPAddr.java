package org.xdef.impl.code;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDIPAddr;
import static org.xdef.XDValueID.XD_IPADDR;
import static org.xdef.XDValueType.IPADDR;
import org.xdef.msg.XDEF;

/** IP address.
 * @author Vaclav Trojan
 */
public class DefIPAddr extends XDValueAbstract implements XDIPAddr {
	/** Value of InetAddress. */
	private final InetAddress _value;

	/** Create new instance null DefIPAddr. */
	public DefIPAddr() {_value = null;}

	/** Create new instance of DefIPAddr from source string.
	 * @param ipAddr String representation of InternetAddress.
	 */
	public DefIPAddr(final String ipAddr) {
		try {
			boolean ndx = ipAddr.charAt(0) == '/'; // may start with slash
			_value = InetAddress.getByName(ndx ? ipAddr.substring(1) : ipAddr);
		} catch (UnknownHostException ex) {
			//Incorrect value&{0}{ of '}{'}&{1}{: '}{'}
			throw new SRuntimeException(XDEF.XDEF809, ex, "ipAddr", ipAddr);
		}
	}

	/** Create new instance of DefIPAddr with InetAddress.
	 * @param ipAddr InternetAddress.
	 */
	public DefIPAddr(final InetAddress ipAddr) {_value = ipAddr;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDIPAddr interface
////////////////////////////////////////////////////////////////////////////////

	/**	Get the raw IP address in a string format.
	 * @return raw IP address in a string format.
	 */
	@Override
	public String getHostAddress() {return _value != null ? _value.getHostAddress() : null;}

	/**	Check if IP address of this IPAddrress object is IPv6 version.
	 * @return true if IP address of this IPAddress object is IPv6 version.
	 */
	@Override
	public boolean isIPv6() {return _value == null ? false : _value.getAddress().length == 16;}

	/** Get bytes from the internal InetAddress.
	 * @return bytes from InetAddress.
	 */
	@Override
	public byte[] getBytes() {return _value != null ? _value.getAddress(): null;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}

	@Override
	public boolean equals(final Object arg) {return arg instanceof XDValue ? equals((XDValue) arg) : false;}

	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefIPAddr) {
			DefIPAddr x = (DefIPAddr) arg;
			return _value != null ? _value.equals(x._value) : x._value == null;
		}
		return false;
	}

	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefIPAddr) {
			if (this.equals((DefIPAddr) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	@Override
	public short getItemId() {return XD_IPADDR;}

	@Override
	public XDValueType getItemType() {return IPADDR;}

	@Override
	public String stringValue() {return isNull() ? "null" :_value.toString().substring(1);}

	@Override
	public boolean isNull() {return _value == null;}

	@Override
	public InetAddress getObject() {return _value;}

	@Override
	public String toString() {return stringValue();}
}