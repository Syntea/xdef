package org.xdef.impl.parsers;

/** Parser of Schema "SHA1" type.
 * @author Vaclav Trojan
 */
public class XDParseSHA1 extends XSParseHexBinary {
	private static final String ROOTBASENAME = "SHA1";
	public XDParseSHA1() {
		super();
		_minLength = _maxLength = 20;
	}
	@Override
	public void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = 20;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed collapse
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
			LENGTH + //fixed to 20
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
//			BASE +
			0;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}