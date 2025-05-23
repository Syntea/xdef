package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDecimal;

/** Parser of XML Schema (XSD) "decimal" type.
 * @author Vaclav Trojan
 */
public class XSParseDecimal extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "decimal";
	protected long _totalDigits;
	protected long _fractionDigits;

	public XSParseDecimal() {super(); _whiteSpace = WS_COLLAPSE; _totalDigits = _fractionDigits = -1;}

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
		_whiteSpace = WS_COLLAPSE;
		_patterns = null;
		_enumeration = null;
		_minExcl = _minIncl = _maxExcl = _maxIncl = null;
		_fractionDigits = _totalDigits = -1;
	}

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
					p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
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
			p.errorWithString(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'&{1}{: }
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
	public void setTotalDigits(final long x) {_totalDigits = x;}

	@Override
	public long getTotalDigits() {return _totalDigits;}

	@Override
	public void setFractionDigits(final long x) {_fractionDigits = x;}

	@Override
	public long getFractionDigits() {return _fractionDigits;}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_DECIMAL;}
}