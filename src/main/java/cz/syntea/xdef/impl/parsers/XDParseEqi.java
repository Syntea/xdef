/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseEqi.java
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

/** Parser of X-Script "eqi" type.
 * @author Vaclav Trojan
 */
public class XDParseEqi extends XDParseEq {
	private static final String ROOTBASENAME = "eqi";
	public XDParseEqi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (!_param.equalsIgnoreCase(s)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	/** Set value of one "sequential" parameter of parser.
	 * @param par "sequential" parameters.
	 */
	public void setParseParam(Object param) {
		_param = param.toString();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (p.isTokenIgnoreCase(_param)) {
			p.setParsedValue(_param);
		} else {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEqi) ) {
			return false;
		}
		XDParseEqi x = (XDParseEqi) o;
		return _param.equals(x._param);
	}
}