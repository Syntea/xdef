/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseNum.java
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

import cz.syntea.xdef.XDParseResult;

/** Parser of X-Script "num" type.
 * @author Vaclav Trojan
 */
public class XDParseNum extends XDParseAn {
	private static final String ROOTBASENAME = "num";
	public XDParseNum() {
		super();
	}
	@Override
	public boolean parse(final XDParseResult p) {
		int pos = p.getIndex();
		if (p.isDigit() < 0) {
			return false;
		}
		while(p.isDigit() >= 0) {}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
