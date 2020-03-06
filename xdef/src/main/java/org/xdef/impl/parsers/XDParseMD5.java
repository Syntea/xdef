package org.xdef.impl.parsers;

/** Parser of Schema "MD5" type.
 * @author Vaclav Trojan
 */
public class XDParseMD5 extends XSParseHexBinary {
	private static final String ROOTBASENAME = "MD5";
	public XDParseMD5() {
		super();
		_minLength = _maxLength = 16;
	}
	@Override
	public void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = 16;
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
			LENGTH + //fixed to 16
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