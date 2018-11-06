package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.impl.code.DefDecimal;
import cz.syntea.xdef.impl.code.DefBoolean;
import cz.syntea.xdef.impl.code.DefNull;

/** Parser of X-Script "jvalue" type.
 * @author Vaclav Trojan
 */
public class XDParseJValue extends XDParseAn {
	private static final String ROOTBASENAME = "jvalue";
	public XDParseJValue() {
		super();
	}
	@Override
	boolean parse(final XDParseResult p) {
		if (p.isChar('"')) { // quoted string
			StringBuilder sb = new StringBuilder();
			for (;;) {
				if (p.eos()) {
					return false;
				}
				if (p.isToken("\"\"")) {
					sb.append('"');
				} else if (p.isChar('"')) {
					p.setParsedValue(sb.toString());
					return true;
				} else {
					sb.append(p.peekChar());
				}
			}
		} else if (!p.eos()) {//not quoed string
			if (p.isToken("null")) {
				p.setParsedValue(DefNull.genNullValue(XD_ANY));
				return true;
			}
			if (p.isToken("true")) {
				p.setParsedValue(new DefBoolean(true));
				return true;
			}
			if (p.isToken("false")) {
				p.setParsedValue(new DefBoolean(false));
			}
			if (p.getCurrentChar() != '+') {
				int pos = p.getIndex();
				if (p.isSignedFloat() || p.isSignedInteger()) {
					p.setParsedValue(new DefDecimal(
						p.getBufferPart(pos, p.getIndex())));
					return true;
				}
			}
			int pos = p.getIndex();
			char ch;
			while ((ch = p.getCurrentChar()) != 0 && ch != ' '
				&& ch != '\t' && ch != '\r' && ch != '\n') {
				p.peekChar();
			}
			p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
			return true;
		}
		return false;
	}
	@Override
	public short parsedType() {return XD_ANY;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}