package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of X-Script "an" type.
 * @author Vaclav Trojan
 */
public class XDParseAn extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "an";

	public XDParseAn() {super();}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (p.isLetterOrDigit() == 0) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		while(p.isLetterOrDigit() != 0){}
		if (quoted && !p.isChar('"')) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
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