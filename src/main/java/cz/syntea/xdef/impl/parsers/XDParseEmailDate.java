/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseEmailDate.java
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

/** Parser of Schema "date" type.
 * @author Vaclav Trojan
 */
public class XDParseEmailDate extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "emailDate";

	public XDParseEmailDate() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isRFC822Datetime();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DATETIME;}

}
