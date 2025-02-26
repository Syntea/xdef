package org.xdef;

/** Value of array of bytes in Xscript.
 * @author Vaclav Trojan
 */
public interface XDBytes extends XDValue {
	/** Return the value of DefBytes as string in Base64 format.
	 * @return string with value of this object in Base64 format.
	 */
	public String getBase64();

	/** Return the value of DefBytes as string in hexadecimal format.
	 * @return string with value of this object in hexadecimal format.
	 */
	public String getHex();

	/** Return true if the format is base64.
	 * @return true if the format is base64 otherwise it is hexadecimal.
	 */
	public boolean isBase64();
}