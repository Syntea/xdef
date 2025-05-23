package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDouble;
import org.xdef.sys.SRuntimeException;

/** Parser of XML Schema (XSD) "double" type.
 * @author Vaclav Trojan
 */
public class XSParseDouble extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "double";

	public XSParseDouble() {super(); _whiteSpace = WS_COLLAPSE;}

	@Override
	public  void initParams() {
		_whiteSpace = WS_COLLAPSE;
		_patterns = null;
		_enumeration = null;
		_minExcl = _minIncl = _maxExcl = _maxIncl = null;
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
//			TOTALDIGITS +
//			FRACTIONDIGITS + //ignored
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
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		if (!p.isSignedFloat() && p.isOneOfTokens(new String[]
			{"NaN", "INF", "-INF", "+INF"})	< 0) {
			p.errorWithString(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'&{1}{: }
			return;
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.setParsedValue(new DefDouble(s));
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkPatterns(p);
		checkComparable(p);
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_DOUBLE;}

	@Override
	public void checkValue(final XDValue x) {
		if (x.doubleValue()== Double.NaN) {
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);//Incorrect range specification of &{0}
		}
	}
}