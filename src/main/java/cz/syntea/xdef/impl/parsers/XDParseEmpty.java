/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseEmpty.java
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

import cz.syntea.xdef.XDValue;

/** Parser of X-Script "empty" type.
 * @author Vaclav Trojan
 */
public class XDParseEmpty extends XDParseCDATA {
	private static final String ROOTBASENAME = "empty";
	public XDParseEmpty() {
		super();
		_minLength = _maxLength = 0;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {return o instanceof XDParseEmpty;}
}