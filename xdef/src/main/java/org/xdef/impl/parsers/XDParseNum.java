package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-Script "num" type.
 * @author Vaclav Trojan
 */
public class XDParseNum extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "num";

	public XDParseNum() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (p.isDigit() < 0) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		while(p.isDigit() >= 0) {}
		if (quoted && !p.isChar('"')) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		String s = p.getBufferPart(pos, p.getIndex());
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}