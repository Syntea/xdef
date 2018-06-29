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

import cz.syntea.xdef.sys.BNFGrammar;

/** Datetime in x-script.
 * @author Vaclav Trojan
 */
public interface XDBNFGrammar extends XDValue {

	public XDBNFRule getRule(final String name);

	/** Get this object as BNFGrammar.
	 * @return value of this item as BNFGrammar object.
	 */
	public BNFGrammar grammarValue();

	/** Set source.
	 * @param source set source to this object.
	 */
	public void setSource(String source);

}
