package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of X-Script "num" type.
 * @author Vaclav Trojan
 */
public class XDParseNum extends XDParseAn {
	private static final String ROOTBASENAME = "num";

	public XDParseNum() {super();}
	@Override
	public boolean parse(final XXNode xn, final XDParseResult p) {
		int pos = p.getIndex();
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (p.isDigit() < 0) {
			return false;
		}
		while(p.isDigit() >= 0) {}
		if (quoted && !p.isChar('"')) {
			return false;
		}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}