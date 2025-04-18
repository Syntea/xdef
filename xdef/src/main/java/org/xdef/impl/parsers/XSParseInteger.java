package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import java.math.BigInteger;
import org.xdef.impl.code.DefBigInteger;

/** Parser of XML Schema (XSD) "integer" type.
 * @author Vaclav Trojan
 */
public class XSParseInteger extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "integer";
	private long _totalDigits;

	public XSParseInteger() {super(); _totalDigits = -1;}

	@Override
	public  void initParams() {
		_whiteSpace = WS_COLLAPSE;
		_patterns = null;
		_enumeration = null;
		_minExcl = _minIncl = _maxExcl = _maxIncl = null;
		_totalDigits = -1;
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
//			FRACTIONDIGITS +
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
	public void setTotalDigits(final long x) { _totalDigits = x; }

	@Override
	public long getTotalDigits() { return _totalDigits; }

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		char sign = p.isOneOfChars("+-");
		int totalDigits = 0;
		int i;
		if ((i = p.isDigit()) < 0) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
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
		BigInteger val;
		try {
			val = new BigInteger(s = sign == '+' ? s.substring(1) : s);
		} catch (Exception ex) {
			p.error(XDEF.XDEF809, parserName(), s); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		p.setParsedValue(new DefBigInteger(val));
		if (_totalDigits >= 0) {
			if (totalDigits > _totalDigits) {
				//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
				p.error(XDEF.XDEF813, parserName(), "totalDigits", s);
				return;
			}
		}
		checkPatterns(p);
		checkComparable(p);
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_BIGINTEGER;}
}