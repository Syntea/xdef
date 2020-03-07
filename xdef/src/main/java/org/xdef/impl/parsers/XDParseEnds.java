package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "ends" type.
 * @author Vaclav Trojan
 */
public class XDParseEnds extends XDParseEq {
	private static final String ROOTBASENAME = "ends";
	public XDParseEnds() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (!s.endsWith(_param)) {
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (!p.getUnparsedBufferPart().endsWith(_param)) {
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		}
		p.setEos();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEnds) ) {
			return false;
		}
		XDParseEnds x = (XDParseEnds) o;
		return _param.equals(x._param);
	}
}