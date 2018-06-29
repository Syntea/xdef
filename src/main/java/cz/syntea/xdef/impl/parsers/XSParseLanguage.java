/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseLanguage.java
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
import cz.syntea.xdef.sys.SParser;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;

/** Parser of Schema "language" type.
 * @author Vaclav Trojan
 */
public class XSParseLanguage extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "language";

	public XSParseLanguage() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		int count = 0;
		boolean fits = true;
		while (p.isInInterval('a', 'z') != SParser.NOCHAR ||
			p.isInInterval('A', 'Z') != SParser.NOCHAR) {
			if (++count > 8) {
				p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
				return;
			}
		}
		if (count == 0) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		} else {
			while (fits && p.isChar('-')) {
				count = 0;
				while (p.isInInterval('a', 'z') != SParser.NOCHAR ||
					p.isInInterval('A', 'Z') != SParser.NOCHAR ||
					p.isInInterval('0', '9') != SParser.NOCHAR) {
					if (++count > 8) {
						//Incorrect value of '&{0}'
						p.error(XDEF.XDEF809, parserName());
						return;
					}
				}
				if (count == 0) {
					//Incorrect value of '&{0}'
					p.error(XDEF.XDEF809, parserName());
					return;
				}
			}
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