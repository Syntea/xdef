package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of X-Script "an" type.
 * @author Vaclav Trojan
 */
public class XDParseAn extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "an";
	public XDParseAn() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		if (!parse(p)) {
			if (p.matches()) {
				//Incorrect value of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF809, parserName());
			}
			return;
		}
		String s = p.getBufferPart(pos, p.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
	}
	//this method is overrided in  XDParseNum, XDParseLetters,
	boolean parse(final XDParseResult p) {
		int pos = p.getIndex();
		if (p.isLetterOrDigit() == 0) {
			return false;
		}
		while(p.isLetterOrDigit() != 0){}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}