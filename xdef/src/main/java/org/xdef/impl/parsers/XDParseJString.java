package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDParser.WS_PRESERVE;
import org.xdef.xon.XonTools;

/** Parser of X-Script "jstring" (XON/JSON string) type.
 * @author Vaclav Trojan
 */
public class XDParseJString extends XDParseAn {

	private static final String ROOTBASENAME = "jstring";
	public XDParseJString() {
		super();
		_whiteSpace = WS_PRESERVE;
	}
	@Override
	public void initParams() {
		_whiteSpace = WS_PRESERVE;
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
			String s = XonTools.readJString(p);
			if (s != null && !p.errors()) {
				p.setParsedValue(s);
				return true;
			}
			return false;
		} else {//not quoted string -> check JSON simple values
			if (((p.isToken("false") || p.isToken("true") || p.isToken("null")
				|| ((p.isChar('-') || true) && (p.isFloat() || p.isInteger())))
				&& p.eos()) || p.eos()) {
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
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}