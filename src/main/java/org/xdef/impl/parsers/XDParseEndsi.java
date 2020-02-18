package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "endsi" type.
 * @author Vaclav Trojan
 */
public class XDParseEndsi extends XDParseEqi {
	private static final String ROOTBASENAME = "endsi";
	public XDParseEndsi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult result;
		parseObject(xnode, result = new DefParseResult(s));
		return result;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		String s = p.getUnparsedBufferPart();
		int i = s.length() - _param.length();
		if (i < 0 || !_param.equalsIgnoreCase(s.substring(i).toLowerCase())) {
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
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
		XDParseEndsi x = (XDParseEndsi) o;
		return _param.equals(x._param);
	}
}