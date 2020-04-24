package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "startsi" type.
 * @author Vaclav Trojan
 */
public class XDParseStartsi extends XDParseEqi {
	private static final String ROOTBASENAME = "startsi";
	public XDParseStartsi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		if (!p.eos()) {
			if (p.matches()) {
				//Incorrect value of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF809, parserName());
			}
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		if (!p.isTokenIgnoreCase(_param)) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			p.setEos();
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseStartsi) ) {
			return false;
		}
		XDParseStartsi x = (XDParseStartsi) o;
		return _param.equals(x._param);
	}
}