package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of Xscript "containsi" type.
 * @author Vaclav Trojan
 */
public class XDParseContainsi extends XDParseEqi {
	private static final String ROOTBASENAME = "containsi";

	public XDParseContainsi() {super();}

	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (s.toLowerCase().contains(_param.toLowerCase())) {
			p.setParsedValue(s);
			p.setEos();
			checkCharset(xnode, p);
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
		if (s.toLowerCase().contains(_param.toLowerCase())) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		} else {
			p.setParsedValue(s);
			p.setEos();
			checkCharset(xn, p);
		}
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