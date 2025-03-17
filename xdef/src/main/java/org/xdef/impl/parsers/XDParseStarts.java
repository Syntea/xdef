package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-script "starts" type.
 * @author Vaclav Trojan
 */
public class XDParseStarts extends XDParseEq {
	private static final String ROOTBASENAME = "starts";

	public XDParseStarts() {super();}

	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		if (!p.eos()) {
			if (p.matches()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			}
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int i = p.getIndex();
		if (p.isToken(_param)) {
			p.setParsedValue(p.getSourceBuffer().substring(i));
			p.setEos();
			checkCharset(xnode, p);
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseStarts) ) {
			return false;
		}
		return _param.equals(((XDParseStarts) o)._param);
	}
}