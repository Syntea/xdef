/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseTokensi.java
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
import java.util.Arrays;

/** Parser of X-Script "tokensi" type.
 * @author Vaclav Trojan
 */
public class XDParseTokensi extends XDParseTokens {
	private static final String ROOTBASENAME = "tokensi";
	public XDParseTokensi() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos = p.getIndex();
		int len = -1;
		for (int i = 0; i < _list.length; i++) {
			if (p.isTokenIgnoreCase(_list[i])) {
				int tlen = _list[i].length();
				if (tlen > len) {
					len = tlen;
				}
				p.setBufIndex(pos);
			}
		}
		if (len != -1) {
			p.setBufIndex(pos + len);
		} else {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseTokensi) ) {
			return false;
		}
		XDParseTokensi x = (XDParseTokensi) o;
		return _list != null && x._list != null && Arrays.equals(_list,x._list);
	}
}