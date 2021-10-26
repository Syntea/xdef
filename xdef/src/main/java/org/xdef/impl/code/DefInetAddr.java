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
	private final InetAddress _inetAddr;

	/** Create new instance null DefInetAddr. */
	public DefInetAddr() {_inetAddr = null;}

	/** Create new instance of DefInetAddr from source string.
	 * @param inetAddr String representation of InternetAddress.
	 */
	public DefInetAddr(final String inetAddr) {
		try {
			_inetAddr = InetAddress.getByName(inetAddr);
		} catch (Exception ex) {
			throw new SRuntimeException(ex);
		}
	}
	
	/** Create new instance of DefInetAddr with InetAddress.
	 * @param inetAddr InternetAddress.
	 */
	public DefInetAddr(final InetAddress inetAddr) {_inetAddr = inetAddr;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDInetAddr interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/**	Returns the raw IP address of this InetAddress object.
	 * The result is in network byte order: the highest order byte
	 * of the address is in getAddress()[0].
	 * @return array of bytes of IP address.
	 */
	public byte[] getAddress() {
		return _inetAddr != null ? _inetAddr.getAddress() : null;
	}
	@Override
		/**	Get the raw IP address in a string format.
	 * @return raw IP address in a string format.
	 */
	public String getHostAddress() {
		return _inetAddr != null ? _inetAddr.getHostAddress() : null;
	}

	/** Get bytes from InetAddress.
	 * @return bytes from InetAddress.
	 */
	@Override
	public byte[] getBytes() {
		return _inetAddr != null ? _inetAddr.getAddress(): null;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefInetAddr) {
			DefInetAddr x = (DefInetAddr) arg;
			return _inetAddr != null ? _inetAddr.equals(x._inetAddr)
				: x._inetAddr == null;
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
		return isNull() ? "null"
			:_inetAddr.toString().substring(1);}
	@Override
	public boolean isNull() {return _inetAddr == null;}
	@Override
	public InetAddress getObject() {return _inetAddr;}
	@Override
	public String toString() {return stringValue();}
}