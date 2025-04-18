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
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonTools;

/** Parser of X-script "jstring" (XON/JSON string) type.
 * @author Vaclav Trojan
 */
public class XDParseJString extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "jstring";

	public XDParseJString() {super(); _whiteSpace = WS_PRESERVE;}

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
	public void parseObject(final XXNode xn, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		if (xn != null && xn.getXonMode() > 0 && p.isChar('"')) {
			String s = XonTools.readJString(p);
			if (s == null || p.errors()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
				return;
			}
			p.setParsedValue(s);
		} else {//not quoted string -> check JSON simple values
			if (((p.isOneOfTokens("false","true","null") >= 0
				|| ((p.isChar('-') || true) && (p.isFloat() || p.isInteger()))) && p.eos()) || p.eos()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
				return;
			}
			while (!p.eos()) {
				p.nextChar();
			}
		}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		String s = p.getBufferPart(pos, p.getIndex());
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
		checkCharset(xn, p);
	}

	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}