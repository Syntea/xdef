/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseRegex.java
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
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDParserAbstract;
import cz.syntea.xdef.XDRegex;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefContainer;
import cz.syntea.xdef.impl.code.DefRegex;
import cz.syntea.xdef.impl.code.DefString;
import cz.syntea.xdef.XDContainer;

/** Parser of X-Script "regex" type.
 * @author Vaclav Trojan
 */
public class XDParseRegex extends XDParserAbstract
	implements cz.syntea.xdef.msg.XDEF {
	private static final String ROOTBASENAME = "regex";
	private XDRegex _regex;
	public XDParseRegex() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (!_regex.matches(p.getUnparsedBufferPart())) {
			p.error(XDEF809, parserName());//Incorrect value of '&{0}'
		} else {
			p.setEos();
		}
	}
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		_regex = null;
		XDNamedValue[] items = params.getXDNamedItems();
		for (int i = 0; i < num; i++) {
			String name = items[i].getName();
			if ("argument".equals(name)) {
				XDValue val = items[i].getValue();
				if (val == null) {
					//Value of enumeration for 'eq' must be just one
					throw new SException(XDEF816);
				}
				_regex = new DefRegex(val.toString());
			} else {
				//Illegal parameter name '&{0}'
				throw new SException(XDEF801, name);
			}
		}
	}
	@Override
	public final XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_regex != null) {
			map.setXDNamedItem("argument", new DefString(_regex.toString()));
		}
		return map;
	}
	@Override
	public short parsedType() {return XD_STRING;}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseRegex) ) {
			return false;
		}
		XDParseRegex x = (XDParseRegex) o;
		return _regex == null && x._regex == null ||
			_regex != null && _regex.equals(x._regex);
	}
}