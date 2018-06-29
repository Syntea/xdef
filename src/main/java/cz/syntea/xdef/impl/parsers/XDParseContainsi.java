/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseContainsi.java
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

/** Parser of X-Script "containsi" type.
 * @author Vaclav Trojan
 */
public class XDParseContainsi extends XDParseEqi {
	private static final String ROOTBASENAME = "containsi";
	public XDParseContainsi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		String s = p.getUnparsedBufferPart();
		int i = s.length() - _param.length();
		if (i < 0 || s.toLowerCase().indexOf(_param.toLowerCase()) < 0) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		} else {
			p.setEos();
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseContainsi) ) {
			return false;
		}
		XDParseContainsi x = (XDParseContainsi) o;
		return _param == null && x._param == null ||
			_param != null && _param.equals(x._param);
	}
}
