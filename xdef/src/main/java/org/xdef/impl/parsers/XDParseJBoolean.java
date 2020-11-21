package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.impl.code.DefBoolean;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-Script "jboolean" type.
 * @author Vaclav Trojan
 */
public class XDParseJBoolean extends XSParseBoolean {
	private static final String ROOTBASENAME = "jboolean";

	public XDParseJBoolean() {
		super();
		_whiteSpace = WS_PRESERVE;
	}
	@Override
	public void initParams() {_whiteSpace = WS_PRESERVE;}
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
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		int i = p.isOneOfTokens("false", "true");
		if (i < 0) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			String s = p.getParsedBufferPartFrom(pos);
			p.isSpaces();
			p.replaceParsedBufferFrom(pos0, s);
			p.setParsedValue(new DefBoolean(i == 1));
			checkPatterns(p);
		}
	}
	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_BOOLEAN;}
}