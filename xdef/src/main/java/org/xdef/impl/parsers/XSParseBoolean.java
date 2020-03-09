package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefBoolean;

/** Parser of Schema "boolean" type.
 * @author Vaclav Trojan
 */
public class XSParseBoolean extends XSAbstractParser {
	private static final String ROOTBASENAME = "boolean";

	public XSParseBoolean() {
		super();
		_whiteSpace = 'c';
	}
	@Override
	public void initParams() {_whiteSpace = 'c';}
	@Override
	public int getLegalKeys() {
		return PATTERN +
//			ENUMERATION +
			WHITESPACE + //fixed collapse
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
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		int i = p.isOneOfTokens("false", "0", "true", "1");
		if (i < 0) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			String s = p.getParsedBufferPartFrom(pos);
			p.isSpaces();
			p.replaceParsedBufferFrom(pos0, s);
			p.setParsedValue(new DefBoolean(i > 1));
			checkPatterns(p);
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_BOOLEAN;}
}