package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-Script "contains" type.
 * @author Vaclav Trojan
 */
public class XDParseContains extends XDParseEq {
	private static final String ROOTBASENAME = "contains";

	public XDParseContains() {super();}

	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (s.contains(_param)) {
			p.setParsedValue(s);
			p.setEos();
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
		if (s.contains(_param)) {
			p.setParsedValue(s);
			p.setEos();
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseContains) ) {
			return false;
		}
		XDParseContains x = (XDParseContains) o;
		return _param == null && x._param == null || _param != null && _param.equals(x._param);
	}
}