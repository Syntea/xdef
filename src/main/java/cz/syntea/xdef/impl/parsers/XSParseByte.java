/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseByte.java
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

/** Parser of Schema "byte" type.
 * @author Vaclav Trojan
 */
public class XSParseByte extends XSParseLong {
	private static final String ROOTBASENAME = "byte";

	public XSParseByte() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if (p.matches()) {
			long parsed = p.getParsedValue().longValue();
			if (parsed < -128 || parsed > 127) {
				//Value of '&{0}' is out of range
				p.error(XDEF.XDEF806, parserName());
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}