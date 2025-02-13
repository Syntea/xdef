package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.sys.SRuntimeException;

/** Parser of Xscript "eqi" type.
 * @author Vaclav Trojan
 */
public class XDParseEqi extends XDParseEq {
	private static final String ROOTBASENAME = "eqi";

	public XDParseEqi() {super();}

	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (!_param.equalsIgnoreCase(s)) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	public void setParseSQParams(final Object... param) {
		if (param.length == 1) {
			_param = param[0].toString();
		} else {
			throw new SRuntimeException("Incorrect number of parameters");
		}
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (p.isTokenIgnoreCase(_param)) {
			p.setParsedValue(_param);
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
		checkCharset(xnode, p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEqi) ) {
			return false;
		}
		return _param.equals(((XDParseEqi) o)._param);
	}
}