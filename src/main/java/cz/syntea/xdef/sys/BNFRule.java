/*
 * Copyright 2014 Syntea software group a.s. All rights reserved.
 *
 * File: BNFRule.java, created 2014-02-03.
 * Package: cz.syntea.xdef.sys
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */

package cz.syntea.xdef.sys;

/** Provides BNF grammar rule.
 * @author Vaclav Trojan
 */
public interface BNFRule {

	/** Get name of this rule.
	 * @return name of this rule.
	 */
	public String getName();

	/** Get string with parsed part by this rule.
	 * @return string with parsed part by this rule.
	 */
	public String getParsedString();

	/** Parse string assigned to SParser by this rule.
	 * @param parser SParser containing string and position from which parsing
	 * will be started.
	 * @return true if parsing was successful.
	 */
	public boolean parse(StringParser parser);

}