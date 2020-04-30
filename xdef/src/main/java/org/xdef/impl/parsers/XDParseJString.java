package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.sys.SParser;

/** Parser of X-Script "jstring" (JSON string) type.
 * @author Vaclav Trojan
 */
public class XDParseJString extends XDParseAn {
	private static final String ROOTBASENAME = "jstring";
	public XDParseJString() {
		super();
		_whiteSpace = WS_PEESERVE;
	}
	@Override
	public void initParams() {
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
		int pos = p.getIndex();
		if (p.isChar('"')) { // quoted string
			if (!p.eos()) { // not separate quote mark; must end with quote mark
				for (;;) {
					if (p.eos()) {
						return false;
					}
					if (p.isChar('"')) { // quote
						if (p.eos()) {
							p.setParsedValue(p.getParsedString());
							return true;
						}
					} else {
						if (p.isChar('\\')) {
							if (p.isOneOfChars("\\\"tnrf") == SParser.NOCHAR) {
								return false;
							}
						} else {
							p.nextChar();
						}
					}
				}
			}
		} else {//not quoted string
			if (p.eos() || (p.isSignedFloat() || p.isToken("false")
				|| p.isToken("true") || p.isToken("null")) && p.eos()) {
				return false;
			}
			while (!p.eos()) {
				p.nextChar();
			}
		}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		return true;
	}
	@Override
	public byte getDefaultWhiteSpace() {return WS_PEESERVE;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}