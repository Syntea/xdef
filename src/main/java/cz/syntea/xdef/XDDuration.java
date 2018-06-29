/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDDuration.java, created 2011-09-07.
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

import cz.syntea.xdef.sys.SDuration;

/** Duration in x-script.
 * @author Vaclav Trojan
 */
public interface XDDuration extends XDValue {

	/** Set duration.
	 * @param value SDuration object.
	 */
	public void setDuration(SDuration value);

}
