package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.impl.code.DefChar;
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
		_whiteSpace = WS_PRESERVE;
	}
	@Override
	public void initParams() {_whiteSpace = WS_PRESERVE;}
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
		if (p.eos()) {
			return false;
		}
		char ch = p.peekChar();
		if (ch == '"') {
			int i = JsonTools.readJSONChar(p);
			if (i < 1) {
				return false;
			}
			ch = (char) i;
			if (!p.isChar('"')) {
				return false;
			}
		}
		p.setParsedValue(new DefChar(ch));
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_CHAR;}
}