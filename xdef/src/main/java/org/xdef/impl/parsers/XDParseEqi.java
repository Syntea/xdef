package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.sys.SRuntimeException;
import org.xdef.xon.XonTools;

/** Parser of X-script "eqi" type.
 * @author Vaclav Trojan
 */
public class XDParseEqi extends XDParseEq {
	private static final String ROOTBASENAME = "eqi";

	public XDParseEqi() {super();}

	@Override
	public void setParseSQParams(final Object... param) {
		if (param.length == 1) {
			_param = param[0].toString();
		} else {
			throw new SRuntimeException("Incorrect number of parameters");
		}
	}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		if (!q.isTokenIgnoreCase(_param)) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
		if (p != q) {
			p.setEos();
		}
		checkCharset(xn, p);
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