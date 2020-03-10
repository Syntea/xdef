package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "contains" type.
 * @author Vaclav Trojan
 */
public class XDParseContains extends XDParseEq {
	private static final String ROOTBASENAME = "contains";
	public XDParseContains() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (s.indexOf(_param, p.getIndex()) < 0) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (p.getSourceBuffer().indexOf(_param, p.getIndex()) < 0) {
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
		if (!super.equals(o) || !(o instanceof XDParseContains) ) {
			return false;
		}
		XDParseContains x = (XDParseContains) o;
		return _param == null && x._param == null ||
			_param != null && _param.equals(x._param);
	}
}