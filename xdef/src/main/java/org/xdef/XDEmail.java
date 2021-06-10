package org.xdef;

/** Email address.
 * @author Vaclav Trojan
 */
public interface XDEmail extends XDValue {
	public String getDomain();
	public String getLocalPart();
	public String getUserName();
	public String getEmailAddr();
}