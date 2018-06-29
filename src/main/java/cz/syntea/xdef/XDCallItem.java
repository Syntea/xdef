/*
 * Copyright 2007 Syntea software group a.s. All rights reserved.
 *
 * File: XDCallItem.java
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

/** Interface used to connect ScriptProcessor to debugging tools.
 *
 * @author Vaclav Trojan
 */
public interface XDCallItem {

	/** Get parent call item.
	 * @return parent CallItemInterface
	 */
	XDCallItem getParentCallItem();

	/** Get return code address.
	 * @return index to code array.
	 */
	int getReturnAddr();

	/** Get debug mode (see constants in cz.syntea.XXDebug).
	 * @return debug mode (see constants in cz.syntea.XXDebug).
	 */
	public int getDebugMode();

}