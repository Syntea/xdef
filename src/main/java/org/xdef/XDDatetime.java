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

//	/** Check value of datetime.
//	 * Check if the year of date in the interval
//	 * (YEAR_MIN .. YEAR_MAX) or the value of date is
//	 * one of UNDEF_YEAR[].
//	 * @return true if date is legal.
//	 */
//	public boolean isLegalDate();

}