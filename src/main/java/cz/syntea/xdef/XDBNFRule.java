/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDBnfRule.java
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

import cz.syntea.xdef.sys.BNFRule;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.StringParser;

/** BNF rule in x-script.
 * @author Vaclav Trojan
 */
public interface XDBNFRule extends XDValue {

	public XDParseResult perform(XDValue source);

	public XDParseResult perform(String source);

	public XDParseResult perform(SBuffer source);

	public XDParseResult perform(StringParser p);

	public String getName();

	public BNFRule ruleValue();

}
