package org.xdef;

import org.xdef.sys.SDuration;

/** Duration in x-script.
 * @author Vaclav Trojan
 */
public interface XDDuration extends XDValue {

	/** Set duration.
	 * @param value SDuration object.
	 */
	public void setDuration(SDuration value);

}