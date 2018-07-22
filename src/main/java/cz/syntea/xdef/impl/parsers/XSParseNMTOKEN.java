/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseNMTOKEN.java
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
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;

/** Parser of Schema "NMTOKEN" type.
 * @author Vaclav Trojan
 */
public class XSParseNMTOKEN extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "NMTOKEN";

	public XSParseNMTOKEN() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		String token = p.nextToken();
		if (!StringParser.chkNMToken(token, (byte) 10)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}