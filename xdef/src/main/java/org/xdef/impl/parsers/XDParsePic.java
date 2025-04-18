package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of Xcript "pic" type.
 * @author Vaclav Trojan
 */
public class XDParsePic extends XDParseEq {
	private static final String ROOTBASENAME = "pic";

	public XDParsePic() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		for (int i = 0; (i < _param.length()); i++) {
			if (q.eos()) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
				return;
			}
			switch (_param.charAt(i)) {
				case '9':
					if (q.isDigit() < 0) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
					continue;
				case 'A':
					if (q.isLetter() <= 0) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
					continue;
				case 'X':
					if (q.isLetterOrDigit() <= 0) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
					continue;
				default:
					if (!q.isChar(_param.charAt(i))) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
			}
		}
		if (p != q && q.eos()) {
			p.eos();
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