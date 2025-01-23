package org.xdef;

/** Internet IP address in Xscript. The internally used object is java.netInetAddress.
 * @author Vaclav Trojan
 */
public interface XDIPAddr extends XDValue {

	/**	Get the raw IP address in a string format.
	 * @return raw IP address in a string format.
	 */
	public String getHostAddress();

	/**	Check if IP address of this InetAddress object is IPv6 version.
	 * @return true if IP address of this InetAddress object is IPv6 version.
	 */
	public boolean isIPv6();

	/** Get bytes from InetAddress (Overrides the getBytes from XDValue)..
	 * @return bytes from InetAddress.
	 */
	@Override
	public byte[] getBytes();
}