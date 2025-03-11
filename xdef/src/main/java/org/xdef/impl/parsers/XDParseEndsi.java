package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of Xscript "endsi" type.
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
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		String s = p.getUnparsedBufferPart();
		int i = s.length() - _param.length();
		if (i < 0 || !_param.equalsIgnoreCase(s.substring(i))) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of &{0}&{1}{: }
		} else {
			p.setEos();
			checkCharset(xnode, p);
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