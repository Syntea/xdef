package org.xdef;

/** Price (amount and currency).
 * @author Vaclav Trojan
 */
public interface XDInetAddr extends XDValue {

	/**	Get the raw IP address in a string format.
	 * @return raw IP address in a string format.
	 */
	public String getHostAddress();
	
	/**	Check if IP address of this InetAddress object is IPv6.
	 * @return true if IP address of this InetAddress object is IPv6.
	 */
	public boolean isIPv6();

	/** Get bytes from InetAddress (Overrides the getBytes from XDValue)..
	 * @return bytes from InetAddress.
	 */
	@Override
	public byte[] getBytes();
}