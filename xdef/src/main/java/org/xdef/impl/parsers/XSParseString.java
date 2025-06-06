package org.xdef.impl.parsers;

/** Parser of XML Schema (XSD) "string" type.
 * @author Vaclav Trojan
 */
public class XSParseString extends XSAbstractParseString {
	private static final String ROOTBASENAME = "string";

	public XSParseString() {super(); _whiteSpace = 0; _minLength = _maxLength = -1;}

	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE +
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
			LENGTH +
			MAXLENGTH +
			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}

	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_whiteSpace = 0;
		_minLength = _maxLength = -1;
	}

	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}