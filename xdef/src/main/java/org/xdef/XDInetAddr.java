package org.xdef;

/** Price (amount and currency).
 * @author Vaclav Trojan
 */
public interface XDInetAddr extends XDValue {

	/**	Returns the raw IP address of this InetAddress object.
	 * The result is in network byte order: the highest order byte
	 * of the address is in getAddress()[0].
	 * @return array of bytes of IP address.
	 */
	public byte[] getAddress();

	/**	Get the raw IP address in a string format.
	 * @return raw IP address in a string format.
	 */
	public String getHostAddress();

	/** Get bytes from InetAddress (Overrides the getBytes from XDValue)..
	 * @return bytes from InetAddress.
	 */
	@Override
	public byte[] getBytes();
}