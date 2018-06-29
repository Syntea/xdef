/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseEndsi.java
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
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefParseResult;

/** Parser of X-Script "endsi" type.
 * @author Vaclav Trojan
 */
public class XDParseEndsi extends XDParseEqi {
	private static final String ROOTBASENAME = "endsi";
	public XDParseEndsi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult result;
		parseObject(xnode, result = new DefParseResult(s));
		return result;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		String s = p.getUnparsedBufferPart();
		int i = s.length() - _param.length();
		if (i < 0 || !_param.equalsIgnoreCase(s.substring(i).toLowerCase())) {
			p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
		} else {
			p.setEos();
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEndsi) ) {
			return false;
		}
		XDParseEndsi x = (XDParseEndsi) o;
		return _param.equals(x._param);
	}
}