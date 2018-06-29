/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseDec.java
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

/** Parse decimal number with decimal point in X-Script. Decimal point may be
 * either '.' or ','
 * @author Vaclav Trojan
 */
public class XDParseDec extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "dec";
	private long _totalDigits;
	private long _fractionDigits;

	public XDParseDec() {
		super();
		_whiteSpace = 'c';
		_totalDigits = _fractionDigits = -1;
	}

	@Override
	public  void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minExcl = _minIncl = _maxExcl = _maxIncl = null;
		_fractionDigits = _totalDigits = -1;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
//			WHITESPACE + //fixed to collapse
			MAXINCLUSIVE +
			MAXEXCLUSIVE +
			MININCLUSIVE +
			MINEXCLUSIVE +
			TOTALDIGITS +
			FRACTIONDIGITS +
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
//			BASE +
			0;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void setTotalDigits(long x) { _totalDigits = x; }
	@Override
	public long getTotalDigits() { return _totalDigits; }
	@Override
	public void setFractionDigits(long x) {_fractionDigits = x;}
	@Override
	public long getFractionDigits() {return _fractionDigits;}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos0 = p.getIndex();
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		int pos = p.getIndex();
		int i = (p.isChar('-') || p.isChar('+')) ? 1 : 0;
		boolean wasdigit = false;
		while (p.isChar('0')) {
			i++;
			wasdigit = true; //digit recognized
		}
		int h = i; //first position
		while (p.isDigit() != -1) {
			i++;
			wasdigit = true; //digit recognized
		}
		int k = -1; //decimal point position
		if (p.isOneOfChars(".,") > 0) {
			k = i;
			i++;
			while (p.isDigit() != -1) {
				i++;
				wasdigit = true; //digit recognized
			}
		}
		String s = p.getParsedBufferPartFrom(pos);
		int j;
		if ((j = s.length() - 1) < 0) {
			p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
			return;
		}
		if (!wasdigit || i <= j) {
			p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
			return;
		}
		if (_totalDigits == -1) {
			if (_fractionDigits != -1) { // only fraction digits
				if (((k != -1) ? j - k : 0) < _fractionDigits) {
					//Value of '&{0}' doesn't fit to '&{1}'
					p.error(XDEF.XDEF813, parserName(), "fractionDigits");
					return;
				}
			}
		} else { //a prameter
			j = (k != -1) ? j - k : 0;
			i = (k != -1) ? k - h : i - h;
			if (!(_fractionDigits == -1 ? //dec(m)
				i + j <= _totalDigits : //dec(m, n)
				i <= _totalDigits - _fractionDigits && j <= _fractionDigits)) {
				//Value of '&{0}' doesn't fit to '&{1}'
				p.error(XDEF.XDEF813, parserName(), "totalDigits");
				return;
			}
		}
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(new DefDecimal(
			(s.charAt(0) == '+'? s.substring(1) : s).replace(',', '.')));
		checkPatterns(p);
		checkComparable(p);
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_DECIMAL;}

}