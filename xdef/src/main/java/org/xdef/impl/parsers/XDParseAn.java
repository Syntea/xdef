package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.impl.code.DefParseResult;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonTools;

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
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		XDParseResult q = quoted?new DefParseResult(XonTools.readJString(p)):p;
		int pos = p.getIndex();
		if (q.isLetterOrDigit() == 0) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		while(q.isLetterOrDigit() != 0){}
		String s = q.getBufferPart(pos, q.getIndex());
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}