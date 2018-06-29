/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: DefParseQName.java
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

/** Parser of Schema "QName" type.
 * @author Vaclav Trojan
 */
public class XSParseQName extends XSParseName {
	private static final String ROOTBASENAME = "QName";
	public XSParseQName() {super();}
	@Override
	public String parserName() {return ROOTBASENAME;}
}