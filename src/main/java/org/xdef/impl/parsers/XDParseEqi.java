package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "eqi" type.
 * @author Vaclav Trojan
 */
public class XDParseEqi extends XDParseEq {
	private static final String ROOTBASENAME = "eqi";
	public XDParseEqi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (!_param.equalsIgnoreCase(s)) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	/** Set value of one "sequential" parameter of parser.
	 * @param par "sequential" parameters.
	 */
	public void setParseParam(Object param) {
		_param = param.toString();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (p.isTokenIgnoreCase(_param)) {
			p.setParsedValue(_param);
		} else {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEqi) ) {
			return false;
		}
		XDParseEqi x = (XDParseEqi) o;
		return _param.equals(x._param);
	}
}