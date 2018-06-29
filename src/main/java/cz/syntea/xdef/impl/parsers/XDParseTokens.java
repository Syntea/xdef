/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseTokens.java
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

import cz.syntea.xdef.sys.SException;
import cz.syntea.xdef.XDNamedValue;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefContainer;
import cz.syntea.xdef.impl.code.DefString;
import java.util.Arrays;
import cz.syntea.xdef.XDContainer;

/** Parser of X-Script "tokens" type.
 * @author Vaclav Trojan
 */
public class XDParseTokens extends XDParseEnum {
	private static final String ROOTBASENAME = "tokens";
	public XDParseTokens() {
		super();
	}
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		XDNamedValue[] items = params.getXDNamedItems();
		for (int i = 0; i < num; i++) {
			if ("argument".equals(items[i].getName())) {
				toList(items[i].getValue());
			}
		}
	}
	@Override
	public XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_list != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < _list.length; i++) {
				if (i > 0) {
					sb.append(" | ");
				}
				String s = _list[i];
				for (int j = 0; j < s.length(); j++) {
					char c;
					if ((c = s.charAt(j)) == '|') {
						sb.append('|');
					}
					sb.append(c);
				}
			}
			map.setXDNamedItem("argument", new DefString(sb.toString()));
		}
		return map;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseTokens) ) {
			return false;
		}
		XDParseTokens x = (XDParseTokens) o;
		return _list!=null && x. _list!=null && Arrays.equals(_list, x._list);
	}
}
