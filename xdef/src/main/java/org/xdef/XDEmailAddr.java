package org.xdef;

/** Email address.
 * @author Vaclav Trojan
 */
public interface XDEmailAddr extends XDValue {
	public String getDomain();
	public String getLocalPart();
	public String getUserName();
	public String getEmailAddr();
}