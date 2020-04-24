package org.xdef.impl.parsers;

/** Parser of X-Script "jboolean" type.
 * @author Vaclav Trojan
 */
public class XDParseJBoolean extends XSParseBoolean {
	private static final String ROOTBASENAME = "jboolean";

	public XDParseJBoolean() {
		super();
		_whiteSpace = WS_PEESERVE;
	}
	@Override
	public void initParams() {_whiteSpace = WS_PEESERVE;}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
//			WHITESPACE + //fixed collapse
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
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
	public byte getDefaultWhiteSpace() {return WS_PEESERVE;}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_BOOLEAN;}
}