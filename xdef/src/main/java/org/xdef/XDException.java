package org.xdef;

import org.xdef.sys.Report;

/** Exceptions in the x-script.
 * @author Vaclav Trojan
 */
public interface XDException extends XDValue {

	/** Return assigned Report.
	 * @return the value of assigned Report.
	 */
	public Report reportValue();

	/** Get script code address.
	 * @return script code address.
	 */
	public int getCodeAddr();

	/** Get position of actual XML node.
	 * @return position of actual XML node.
	 */
	public String getXPos();

}