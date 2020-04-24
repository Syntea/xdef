package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDecimal;

/** Parser of Schema "decimal" type.
 * @author Vaclav Trojan
 */
public class XSParseDecimal extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "decimal";
	protected long _totalDigits;
	protected long _fractionDigits;

	public XSParseDecimal() {
		super();
		_whiteSpace = 'c';
		_totalDigits = _fractionDigits = -1;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed to collapse
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
			BASE +
			0;
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
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		char sign = p.isOneOfChars("+-");
		int totalDigits = 0;
		boolean wasNumber = false;
		if (p.isChar('0')) {
			wasNumber = true;
			while (p.isChar('0')) {}
		}
		if (p.isDigit() >= 0) {
			wasNumber = true;
			totalDigits = 1;
			while (p.isDigit() >= 0) {
				totalDigits++;
			}
		}
		int fractionDigits = 0;
		if (p.isChar('.')) {
			if (p.isDigit() < 0) {
				if (!wasNumber) {
					//Incorrect value of '&{0}'&{1}{: }
					p.errorWithString(XDEF.XDEF809, parserName());
					return;
				}
			} else {
				int count = 1;
				int digit;
				wasNumber = true;
				while((digit = p.isDigit()) >= 0) {
					count++;
					if (digit != 0) {
						fractionDigits = count;
					}
				}
				totalDigits += fractionDigits;
			}
		}
		if (!wasNumber) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		if (sign != 0) {
			s = s.substring(1);
		}
		if (sign == '-') {
			s = "-" + s;
		}
		p.setParsedValue(new DefDecimal(s));
		if (_totalDigits >= 0 && totalDigits > _totalDigits) {
			//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
			p.error(XDEF.XDEF813, parserName(), "totalDigits", s);
			return;
		}
		if (_fractionDigits >= 0 && fractionDigits > _fractionDigits) {
			//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
			p.error(XDEF.XDEF813, parserName(), "fractionDigits", s);
			return;
		}
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public void setTotalDigits(long x) { _totalDigits = x; }
	@Override
	public long getTotalDigits() { return _totalDigits; }
	@Override
	public void setFractionDigits(long x) {_fractionDigits = x;}
	@Override
	public long getFractionDigits() {return _fractionDigits;}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DECIMAL;}
}