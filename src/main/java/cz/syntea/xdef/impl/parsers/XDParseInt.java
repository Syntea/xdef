/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseInt.java
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

/** Parser of X-Script "int" type.
 * @author Vaclav Trojan
 */
public class XDParseInt extends XSParseLong {
	private static final String ROOTBASENAME = "int";
	public XDParseInt() {
		super();
	}
	@Override
	/** Get name of value.
	 * @return The name.
	 */
	public String parserName() {return ROOTBASENAME;}
}
