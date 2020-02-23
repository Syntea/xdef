package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDecimal;
import org.xdef.sys.SRuntimeException;
import java.math.BigDecimal;

/** Parser of Schema "unsignedLong" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedLong extends XSParseInteger {
	private static final String ROOTBASENAME = "unsignedLong";
	private static final BigDecimal MAX_VALUE =
		new BigDecimal("18446744073709551615");
	private int _totalDigits;

	public XSParseUnsignedLong() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		boolean plus = p.isChar('+');
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
		BigDecimal val;
		try {
			val = new BigDecimal(plus ? s = s.substring(1) : s);
			if (val.signum() < 0 ||	val.compareTo(MAX_VALUE) > 0) {
				throw new Exception();
			}
		} catch (Exception ex) {
			//Value of '&{0}' is out of range&{1}{: }
			p.error(XDEF.XDEF806, parserName(), s);
			return;
		}
		p.setParsedValue(new DefDecimal(val));
		if (_totalDigits >= 0) {
			if (totalDigits > _totalDigits) {
				//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
				p.error(XDEF.XDEF813,parserName(), "totalDigits", val);
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

	@Override
	public void checkValue(final XDValue x) {
		BigDecimal val = x.decimalValue();
		if (val.signum() < 0 ||
			val.compareTo(new BigDecimal("18446744073709551615")) > 0) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}