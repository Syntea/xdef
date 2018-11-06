package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefParseResult;

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
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (!p.getUnparsedBufferPart().endsWith(_param)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
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