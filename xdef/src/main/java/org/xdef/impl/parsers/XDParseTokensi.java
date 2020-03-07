package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import java.util.Arrays;

/** Parser of X-Script "tokensi" type.
 * @author Vaclav Trojan
 */
public class XDParseTokensi extends XDParseTokens {
	private static final String ROOTBASENAME = "tokensi";
	public XDParseTokensi() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos = p.getIndex();
		int len = -1;
		for (int i = 0; i < _list.length; i++) {
			if (p.isTokenIgnoreCase(_list[i])) {
				int tlen = _list[i].length();
				if (tlen > len) {
					len = tlen;
				}
				p.setBufIndex(pos);
			}
		}
		if (len != -1) {
			p.setBufIndex(pos + len);
		} else {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseTokensi) ) {
			return false;
		}
		XDParseTokensi x = (XDParseTokensi) o;
		return _list != null && x._list != null && Arrays.equals(_list,x._list);
	}
}