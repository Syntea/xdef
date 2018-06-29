/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseFloat.java
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

/** Parser of Schema "float" type.
 * @author Vaclav Trojan
 */
public class XSParseFloat extends XSParseDouble {
	private static final String ROOTBASENAME = "float";

	public XSParseFloat() {
		super();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_FLOAT;}
}