package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.WS_PRESERVE;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.impl.code.DefParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonTools;

/** Parser of Xscript "empty" value type.
 * @author Vaclav Trojan
 */
public class XDParseEmpty extends XSAbstractParser {
	private static final String ROOTBASENAME = "empty";

	public XDParseEmpty() {super(); _whiteSpace = WS_PRESERVE;}

	@Override
	public int getLegalKeys() {return BASE;}
	@Override
	public void initParams() {_whiteSpace = WS_PRESERVE;}
	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}
	@Override
	public XDParseResult check(final XXNode xn, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xn, p);
		return p;
	}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
		if (!s.isEmpty()) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
		p.setParsedValue(s);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_STRING;}
}
