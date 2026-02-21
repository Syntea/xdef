package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-script "containsi" type.
 * @author Vaclav Trojan
 */
public class XDParseContainsi extends XDParseEqi {
	private static final String ROOTBASENAME = "containsi";

	public XDParseContainsi() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		if (q.getUnparsedBufferPart().toLowerCase().contains(_param.toLowerCase())) {
			if (p != q) {
				p.setEos();
			}
			checkCharset(xn, p);
			return;
		}
		p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }

	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseContainsi) ) {
			return false;
		}
		XDParseContainsi x = (XDParseContainsi) o;
		return _param == null && x._param == null || _param != null && _param.equals(x._param);
	}
}