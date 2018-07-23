/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseNMTOKENS.java
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
import cz.syntea.xdef.impl.code.DefContainer;
import cz.syntea.xdef.impl.code.DefString;

/** Parser of Schema "NMTOKENS" type.
 * @author Vaclav Trojan
 */
public class XSParseNMTOKENS extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "NMTOKENS";

	public XSParseNMTOKENS() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parser.isNMToken((byte) 10)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}
		String s = parser.getParsedString();
		StringBuilder sb = new StringBuilder(s);
		DefContainer val = new DefContainer();
		val.addXDItem(new DefString(s));
		while (parser.isSpaces() && !parser.eos()) {
			if (!parser.isNMToken((byte) 10)) {
				p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
				return;
			}
			sb.append(' ').append(s = parser.getParsedString());
			val.addXDItem(new DefString(s));
		}
		p.setBufIndex(parser.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, sb.toString());
		p.setParsedValue(val);
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(val)){
					found = true;
					break;
				}
			}
			if (!found) {
				//Doesn't fit enumeration list of '&{0}'
				p.error(XDEF.XDEF810, parserName());
				return;
			}
		}
		checkPatterns(p);
		if (p.errors()) {
			return;
		}
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(val)){
					found = true;
					break;
				}
			}
			if (!found) {
				//Doesn't fit enumeration list of '&{0}'
				p.error(XDEF.XDEF810, parserName());
				return;
			}
		}
		if (_minLength!=-1 && val.getXDItemsNumber() < _minLength) {
			//Length of value of '&{0}' is too short
			p.error(XDEF.XDEF814, parserName());
		} else if (_maxLength!=-1 && val.getXDItemsNumber() > _maxLength) {
			//Length of value of '&{0}' is too long
			p.error(XDEF.XDEF815, parserName());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
}