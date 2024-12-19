package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import java.util.Arrays;
import org.xdef.XDContainer;

/** Parser of X-Script "enumi" type.
 * @author Vaclav Trojan
 */
public class XDParseEnumi extends XDParseEnum {
	private static final String ROOTBASENAME = "enumi";

	public XDParseEnumi() {super();}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos = p.getIndex();
		int len = -1;
		for (String s : _list) {
			if (p.isTokenIgnoreCase(s)) {
				int tlen = s.length();
				if (tlen > len) {
					len = tlen;
				}
				p.setIndex(pos);
			}
		}
		if (len != -1) {
			int i = pos + len;
			p.setParsedValue(p.getSourceBuffer().substring(pos, i));
			p.setIndex(i);
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
	}
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		super.setNamedParams(xnode, params);
		if (_list != null && _list.length > 0) {
			for (int i = 0; i < _list.length; i++) {
				_list[i] = _list[i].toLowerCase();
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEnumi) ) {
			return false;
		}
		XDParseEnumi x = (XDParseEnumi) o;
		return _list == null && x. _list == null || _list != null && Arrays.equals(_list, x._list);
	}
}