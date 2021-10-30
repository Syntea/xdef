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
/*
extual representation of IPv6 address used as input to methods takes one of
the following forms:
  The preferred form is x:x:x:x:x:x:x:x, where the 'x's are the hexadecimal
  values of the eight 16-bit pieces of the address. This is the full form.
  For example, 1080:0:0:0:8:800:200C:417A
Note that it is not necessary to write the leading zeros in an individual field.
However, there must be at least one numeral in every field, except as described
below.
Due to some methods of allocating certain styles of IPv6 addresses,
it will be common for addresses to contain long strings of zero bits. In order
to make writing addresses containing zero bits easier, a special syntax
is available to compress the zeros. The use of "::" indicates multiple groups 
of 16-bits of zeros. The "::" can only appear once in an address.
The "::" can also be used to compress the leading and/or trailing zeros
in an address. For example,
1080::8:800:200C:417A
An alternative form that is sometimes more convenient when dealing with a mixed
environment of IPv4 and IPv6 nodes is x:x:x:x:x:x:d.d.d.d, where the 'x's
are the hexadecimal values of the six high-order 16-bit pieces of the address,
and the 'd's are the decimal values of the four low-order 8-bit pieces of the
standard IPv4 representation address, for example,
::FFFF:129.144.52.38
::129.144.52.38
where "::FFFF:d.d.d.d" and "::d.d.d.d" are, respectively, the general forms
of an IPv4-mapped IPv6 address and an IPv4-compatible IPv6 address. Note that
the IPv4 portion must be in the "d.d.d.d" form. The following forms are invalid:
::FFFF:d.d.d
::FFFF:d.d
::d.d.d
::d.d
The following form:
::FFFF:d
is valid, however it is an unconventional representation of the IPv4-compatible
IPv6 address,
::255.255.0.d
while "::d" corresponds to the general IPv6 address "0:0:0:0:0:0:0:d".
For methods that return a textual representation as output value, the full
form is used. Inet6Address will return the full form because it is unambiguous
when used in combination with other textual data.
Special IPv6 address
Description of IPv4-mapped address
IPv4-mapped address
Of the form ::ffff:w.x.y.z, this IPv6 address is used to represent an IPv4
address. It allows the native program to use the same address data structure
and also the same socket when communicating with both IPv4 and IPv6 nodes.
In InetAddress and Inet6Address, it is used for internal representation; it has
no functional role. Java will never return an IPv4-mapped address. These classes
can take an IPv4-mapped address as input, both in byte array and text
representation. However, it will be converted into an IPv4 address.
Textual representation of IPv6 scoped addresses
The textual representation of IPv6 addresses as described above can be extended
to specify IPv6 scoped addresses. This extension to the basic addressing
architecture is described in [draft-ietf-ipngwg-scoping-arch-04.txt].
Because link-local and site-local addresses are non-global, it is possible that
different hosts may have the same destination address and may be reachable
through different interfaces on the same originating system. In this case,
the originating system is said to be connected to multiple zones of the same
scope. In order to disambiguate which is the intended destination zone,
it is possible to append a zone identifier (or scope_id) to an IPv6 address.
The general format for specifying the scope_id is the following:
IPv6-address%scope_id
The IPv6-address is a literal IPv6 address as described above. The scope_id
refers to an interface on the local system, and it can be specified in two ways.
As a numeric identifier. This must be a positive integer that identifies the
particular interface and scope as understood by the system. Usually, the numeric
values can be determined through administration tools on the system. Each
interface may have multiple values, one for each scope. If the scope is
unspecified, then the default value used is zero.
As a string. This must be the exact string that is returned by
java.net.NetworkInterface.getName() for the particular interface in question.
When an Inet6Address is created in this way, the numeric scope-id is determined
at the time the object is created by querying the relevant NetworkInterface.
Note also, that the numeric scope_id can be retrieved from Inet6Address
instances returned from the NetworkInterface class. This can be used to
find out the current scope ids configured on the system.
	
	*/
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