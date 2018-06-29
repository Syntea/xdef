/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseUnsignedInt.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;

/** Parser of Schema "unsignedInt" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedInt extends XSParseLong {
	private static final String ROOTBASENAME = "unsignedInt";

	public XSParseUnsignedInt() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.errors()) {
			return;
		}
		long val = p.getParsedValue().longValue();
		if (val > 4294967295L || val < 0) {
			p.error(XDEF.XDEF809,parserName()); //Incorrect value of '&{0}'
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}