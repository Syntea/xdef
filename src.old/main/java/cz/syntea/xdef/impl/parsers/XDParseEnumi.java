package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.SException;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import java.util.Arrays;
import cz.syntea.xdef.XDContainer;

/** Parser of X-Script "listi" type.
 * @author Vaclav Trojan
 */
public class XDParseEnumi extends XDParseEnum {
	private static final String ROOTBASENAME = "listi";
	public XDParseEnumi() {
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
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
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
		return _list == null && x. _list == null ||
			 _list != null && Arrays.equals(_list, x._list);
	}
}