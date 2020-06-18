package org.xdef;

import org.xdef.sys.SDatetime;
import java.util.Calendar;

/** Datetime in x-script.
 * @author Vaclav Trojan
 */
public interface XDDatetime extends XDValue {

	/** Set datetime.
	 * @param value SDatetime object.
	 */
	public void setDatetime(SDatetime value);

	/** Set datetime.
	 * @param value SDatetime object.
	 */
	public void setCalendar(Calendar value);
}