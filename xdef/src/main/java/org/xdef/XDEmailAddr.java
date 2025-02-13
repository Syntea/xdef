package org.xdef;

/** Email address in Xscript.
 * @author Vaclav Trojan
 */
public interface XDEmailAddr extends XDValue {

	/** Get domain part of this email address.
	 * @return string with domain part of this email address.
	 */
	public String getDomain();

	/** Get local part of email this address (user).
	 * @return string with local part of this email address.
	 */
	public String getLocalPart();

	/** Get user name (display form) of this email address.
	 * @return string with user name of email this address (or an empty string).
	 */
	public String getUserName();

	/** Get source form of this email address.
	 * @return source form of this email address.
	 */
	public String getEmailAddr();
}