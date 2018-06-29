/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseUnsignedLong.java
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
import cz.syntea.xdef.impl.code.DefDecimal;
import java.math.BigDecimal;

/** Parser of Schema "unsignedLong" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedLong extends XSParseInteger {
	private static final String ROOTBASENAME = "unsignedLong";
	private int _totalDigits;

	public XSParseUnsignedLong() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		boolean plus = p.isChar('+');
		int totalDigits = 0;
		int i;
		if ((i = p.isDigit()) < 0) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		} else if (i > 0) {
			totalDigits++;
		}
		while ((i = p.isDigit()) >= 0) {
			if (i > 0 || totalDigits > 0) {
				totalDigits++;
			}
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		BigDecimal val;
		try {
			val = new BigDecimal(plus ? s.substring(1) : s);
			if (val.signum() < 0 ||
				val.compareTo(new BigDecimal("18446744073709551615")) > 0) {
				throw new Exception();
			}
		} catch (Exception ex) {
			//Value of '&{0}' is out of range'
			p.error(XDEF.XDEF806, parserName());
			return;
		}
		p.setParsedValue(new DefDecimal(val));
		if (_totalDigits >= 0) {
			if (totalDigits > _totalDigits) {
				//Value of '&{0}' doesn't fit to '&{1}'
				p.error(XDEF.XDEF813,parserName(), "totalDigits");
				return;
			}
		}
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DECIMAL;}
}