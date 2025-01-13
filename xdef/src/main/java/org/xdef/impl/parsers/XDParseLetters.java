package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-Script "letters" type.
 * @author Vaclav Trojan
 */
public class XDParseLetters extends XDParseAn {
	private static final String ROOTBASENAME = "letters";

	public XDParseLetters() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (p.isLetter() == 0) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		while(p.isLetter() != 0){}
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