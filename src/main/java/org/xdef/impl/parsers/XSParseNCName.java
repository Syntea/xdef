package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.impl.XConstants;
import org.xdef.proc.XXNode;

/** Parser of Schema "NCName" type.
 * @author Vaclav Trojan
 */
public class XSParseNCName extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "NCName";

	public XSParseNCName() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parser.isNCName((byte) XConstants.XML10)) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		String s = parser.getParsedString();
		p.setBufIndex(parser.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}