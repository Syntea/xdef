package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;

/** Parser of Xcript "pic" type.
 * @author Vaclav Trojan
 */
public class XDParsePic extends XDParseEq {
	private static final String ROOTBASENAME = "pic";

	public XDParsePic() {super();}

	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		if (!p.eos()) {
			if (p.matches()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			}
		}
		checkCharset(xnode, p);
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		for (int i = 0; (i < _param.length()); i++) {
			if (p.eos()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
				return;
			}
			switch (_param.charAt(i)) {
				case '9':
					if (p.isDigit() < 0) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
					continue;
				case 'A':
					if (p.isLetter() <= 0) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
					continue;
				case 'X':
					if (p.isLetterOrDigit() <= 0) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
					continue;
				default:
					if (!p.isChar(_param.charAt(i))) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParsePic) ) {
			return false;
		}
		XDParsePic x = (XDParsePic) o;
		return _param == null && x._param == null || _param != null && _param.equals(x._param);
	}
}