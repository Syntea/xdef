package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-Script "endsi" type.
 * @author Vaclav Trojan
 */
public class XDParseEndsi extends XDParseEqi {
	private static final String ROOTBASENAME = "endsi";

	public XDParseEndsi() {super();}

	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		return p;
	}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
		s = s.trim();
		int i = s.length() - _param.length();
		if (i < 0 || !_param.equalsIgnoreCase(s.substring(i))) {
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			p.setParsedValue(s);
			p.setEos();
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEndsi) ) {
			return false;
		}
		return _param.equals(((XDParseEndsi) o)._param);
	}
}