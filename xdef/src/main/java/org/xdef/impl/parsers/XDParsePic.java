package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-Script "pic" type.
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
				//Incorrect value of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF809, parserName());
			}
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
		s = s.trim();
		if (s.length() == _param.length()) {
			for (int i = 0; (i < _param.length()); i++) {
				switch (_param.charAt(i)) {
					case '9':
						if (!Character.isDigit(s.charAt(i))) {
							//Incorrect value of '&{0}'&{1}{: }
							p.errorWithString(XDEF.XDEF809, parserName());
							return;
						}
						continue;
					case 'A':
						if (!Character.isLetter(s.charAt(i))) {
							//Incorrect value of '&{0}'&{1}{: }
							p.errorWithString(XDEF.XDEF809, parserName());
							return;
						}
						continue;
					case 'X':
						if (!Character.isLetterOrDigit(s.charAt(i))) {
							//Incorrect value of '&{0}'&{1}{: }
							p.errorWithString(XDEF.XDEF809, parserName());
							return;
						}
						continue;
					default:
						if (s.charAt(i) != _param.charAt(i)) {
							//Incorrect value of '&{0}'&{1}{: }
							p.errorWithString(XDEF.XDEF809, parserName());
							return;
						}
				}
				p.setParsedValue(s);
				p.setEos();
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
		return _param == null && x._param == null
			|| _param != null && _param.equals(x._param);
	}
}
