/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseGYear.java
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

import cz.syntea.xdef.sys.StringParser;

/** Parser of Schema "gYear" type.
 * @author Vaclav Trojan
 */
public class XSParseGYear extends XSParseDate {
	private static final String ROOTBASENAME = "gYear";

	public XSParseGYear() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLYear();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
