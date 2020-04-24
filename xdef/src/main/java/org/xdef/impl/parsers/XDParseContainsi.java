package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of X-Script "containsi" type.
 * @author Vaclav Trojan
 */
public class XDParseContainsi extends XDParseEqi {
	private static final String ROOTBASENAME = "containsi";
	public XDParseContainsi() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		String s = p.getUnparsedBufferPart();
		int i = s.length() - _param.length();
		if (i < 0 || s.toLowerCase().indexOf(_param.toLowerCase()) < 0) {
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
		if (!super.equals(o) || !(o instanceof XDParseContainsi) ) {
			return false;
		}
		XDParseContainsi x = (XDParseContainsi) o;
		return _param == null && x._param == null ||
			_param != null && _param.equals(x._param);
	}
}