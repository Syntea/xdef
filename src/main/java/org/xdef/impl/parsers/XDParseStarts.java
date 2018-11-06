package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "starts" type.
 * @author Vaclav Trojan
 */
public class XDParseStarts extends XDParseEq {
	private static final String ROOTBASENAME = "starts";
	public XDParseStarts() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		if (!p.eos()) {
			if (p.matches()) {
				p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
			}
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (!p.isToken(_param)) {
			p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
		}
		p.setEos();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseStarts) ) {
			return false;
		}
		XDParseStarts x = (XDParseStarts) o;
		return _param.equals(x._param);
	}
}