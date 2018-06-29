/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseUnsignedShort.java
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

/** Parser of Schema "unsignedShort" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedShort extends XSParseLong {
	public XSParseUnsignedShort() {super();}
	private static final String ROOTBASENAME = "unsignedShort";
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.errors()) {
			return;
		}
		long val =  p.getParsedValue().longValue();
		if (val < 0 || val > 655355) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		}
	}
	@Override
	public short parsedType() {return XD_INT;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}