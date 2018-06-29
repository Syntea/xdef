/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseAnyURI.java
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
import cz.syntea.xdef.proc.XXNode;
import java.net.URI;
import java.net.URISyntaxException;

/** Parser of Schema "anyURI" type.
 * @author Vaclav Trojan
 */
public class XSParseAnyURI extends XSAbstractParseString {
	private static final String ROOTBASENAME = "anyURI";

	public XSParseAnyURI() {
		super();
		_whiteSpace = 'c';
	}
	@Override
	public void initParams() {
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
		_whiteSpace = 'c';
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
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		String s = p.nextToken();
		try {
			URI u = new URI(s);
		} catch (URISyntaxException ex) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkPatterns(p);
		checkEnumeration(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}