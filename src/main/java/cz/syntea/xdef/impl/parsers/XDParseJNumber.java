/*
 * Copyright 2016 Syntea software group a.s. All rights reserved.
 *
 * File: JString.java
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
import cz.syntea.xdef.impl.code.DefDecimal;

/** Parser of X-Script "js:string" type.
 * @author Vaclav Trojan
 */
public class XDParseJNumber extends XSParseDouble {
	private static final String ROOTBASENAME = "jnumber";
	public XDParseJNumber() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		if (p.getCurrentChar() == '+') {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}
		int pos = p.getIndex();
		if (p.isSignedFloat() || p.isSignedInteger()) {
			p.setParsedValue(new DefDecimal(p.getBufferPart(pos, p.getIndex())));
		} else {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}

		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public short parsedType() {return XD_DECIMAL;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}