package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefParseResult;

/** Parser of X-Script "pic" type.
 * @author Vaclav Trojan
 */
public class XDParsePic extends XDParseEq {
	private static final String ROOTBASENAME = "pic";
	public XDParsePic() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xnode, p);
		if (!p.eos()) {
			if (p.matches()) {
				p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			}
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		for (int i = 0; (i < _param.length()); i++) {
			if (p.eos()) {
				p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
				return;
			}
			switch(_param.charAt(i)) {
				case '9':
					if (p.isDigit() < 0) {
						//Incorrect value of '&{0}'
						p.error(XDEF.XDEF809, parserName());
						return;
					}
					continue;
				case 'A':
					if (p.isLetter() <= 0) {
						//Incorrect value of '&{0}'
						p.error(XDEF.XDEF809, parserName());
						return;
					}
					continue;
				case 'X':
					if (p.isLetterOrDigit() <= 0) {
						//Incorrect value of '&{0}'
						p.error(XDEF.XDEF809, parserName());
						return;
					}
					continue;
				default:
					if (!p.isChar(_param.charAt(i))) {
						//Incorrect value of '&{0}'
						p.error(XDEF.XDEF809, parserName());
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
		return _param == null && x._param == null
			|| _param != null && _param.equals(x._param);
	}
}