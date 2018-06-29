/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDDate.java, created 2011-09-07.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */

package cz.syntea.xdef;

import cz.syntea.xdef.sys.SDatetime;
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