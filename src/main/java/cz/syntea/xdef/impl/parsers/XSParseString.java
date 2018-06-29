/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseString.java
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

/** Parser of Schema "string" type.
 * @author Vaclav Trojan
 */
public class XSParseString extends XSAbstractParseString {
	private static final String ROOTBASENAME = "string";

	public XSParseString() {
		super();
		_whiteSpace = 0;
		_minLength = _maxLength = -1;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE +
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
			LENGTH +
			MAXLENGTH +
			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}

	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_whiteSpace = 0;
		_minLength = _maxLength = -1;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 0;}
	@Override
	/** Get name of value.
	 * @return The name.
	 */
	public String parserName() {return ROOTBASENAME;}
}