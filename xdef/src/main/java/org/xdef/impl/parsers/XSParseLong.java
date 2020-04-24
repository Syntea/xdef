package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefLong;

/** Parser of Schema "long" type.
 * @author Vaclav Trojan
 */
public class XSParseLong extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "long";

	private long _totalDigits;

	public XSParseLong() {
		super();
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
	public void setTotalDigits(long x) { _totalDigits = x; }
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
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
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
		long val;
		try {
			val = Long.parseLong(s = sign == '+' ? s.substring(1) : s);
		} catch (Exception ex) {
			//Value of '&{0}' is out of range&{1}{: }
			p.error(XDEF.XDEF806, parserName(), s);
			return;
		}
		p.setParsedValue(new DefLong(val));
		if (_totalDigits >= 0) {
			if (totalDigits > _totalDigits) {
				//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
				p.error(XDEF.XDEF813, s, "totalDigits", val);
				return;
			}
		}
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_INT;}

}