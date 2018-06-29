/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDException.java, created 2011-08-23.
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

import cz.syntea.xdef.sys.Report;

/** Exceptions in the x-script.
 *
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
