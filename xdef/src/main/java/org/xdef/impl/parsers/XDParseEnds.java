package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-script "ends" type.
 * @author Vaclav Trojan
 */
public class XDParseEnds extends XDParseEq {
	private static final String ROOTBASENAME = "ends";

	public XDParseEnds() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		if (!q.getUnparsedBufferPart().endsWith(_param)) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of &{0}&{1}{: }
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
		if (!super.equals(o) || !(o instanceof XDParseEnds) ) {
			return false;
		}
		XDParseEnds x = (XDParseEnds) o;
		return _param.equals(x._param);
	}
}