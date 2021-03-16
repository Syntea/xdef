package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.json.JsonTools;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-Script "char" type.
 * @author Vaclav Trojan
 */
public class XDParseChar extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "char";
	public XDParseChar() {
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
	boolean parse(final XDParseResult p) {
		char ch;
		if ((ch=p.getCurrentChar()) == 0) {
			return false;
		}
		String s;
		if (p.isChar('"')) {
			s = JsonTools.readJSONString(p);
			if (s == null && s.length() != 1) {
				return false;
			}
		} else {
			s = String.valueOf(ch);
			p.nextChar();
		}
		p.setParsedValue(s);
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}