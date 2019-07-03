package org.xdef.impl.parsers;

import org.xdef.XDParseResult;

/** Parser of X-Script "jstring" type.
 * @author Vaclav Trojan
 */
public class XDParseJString extends XDParseAn {
	private static final String ROOTBASENAME = "jstring";
	public XDParseJString() {
		super();
		_whiteSpace = WS_PEESERVE;
	}
	@Override
	public  void initParams() {
		_whiteSpace = WS_PEESERVE;
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
	}
	
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed to preserve
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
			LENGTH +
			MAXLENGTH +
			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}
	
	@Override
	boolean parse(final XDParseResult p) {
		p.isSpaces();
		if (p.isChar('"')) { // quoted string
			StringBuilder sb = new StringBuilder();
			for (;;) {
				if (p.eos()) {
					return false;
				}
				if (p.isChar('"')) {
					p.setParsedValue(sb.toString());
					return true;
				} else {
					sb.append(p.peekChar());
				}
			}
		} else if (!p.eos()) {//not quoed string
			int pos = p.getIndex();
			char ch;
			while (!p.eos() && (ch = p.getCurrentChar()) != '\t'
				&& ch != '\r' && ch != '\n') {
				ch = p.peekChar();
			}
			p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
			return true;
		}
		return false;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}