package org.xdef.impl.parsers;

import org.xdef.XDParseResult;

/** Parser of X-Script "num" type.
 * @author Vaclav Trojan
 */
public class XDParseNum extends XDParseAn {
	private static final String ROOTBASENAME = "num";
	public XDParseNum() {
		super();
	}
	@Override
	public boolean parse(final XDParseResult p) {
		int pos = p.getIndex();
		if (p.isDigit() < 0) {
			return false;
		}
		while(p.isDigit() >= 0) {}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}