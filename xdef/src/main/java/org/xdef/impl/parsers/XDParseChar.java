package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import static org.xdef.XDParser.WS_PRESERVE;
import static org.xdef.XDParserAbstract.checkCharset;
import static org.xdef.XDValueID.XD_CHAR;
import org.xdef.impl.code.DefChar;
import org.xdef.xon.XonTools;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-script "char" type.
 * @author Vaclav Trojan
 */
public class XDParseChar extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "char";

	public XDParseChar() {super(); _whiteSpace = WS_PRESERVE;}

	@Override
	public void initParams() {_whiteSpace = WS_PRESERVE;}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		if (!parse(p)) {
			if (p.matches()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			}
			return;
		}
		String s = p.getBufferPart(pos, p.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkCharset(xn, p);
		checkItem(p);
	}

	boolean parse(final XDParseResult p) {
		if (p.eos()) {
			return false;
		}
		boolean xon = p.isChar('c');
		char ch = p.peekChar();
		if (ch == '"') {
			int i = XonTools.readJChar(p);
			if (i < 1) {
				return false;
			}
			ch = (char) i;
			if (!p.isChar('"')) {
				return false;
			}
		} else if (xon) {
			return false;
		}
		p.setParsedValue(new DefChar(ch));
		return true;
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_CHAR;}
}