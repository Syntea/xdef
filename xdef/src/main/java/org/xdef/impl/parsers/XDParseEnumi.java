package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import java.util.Arrays;
import org.xdef.XDContainer;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-script "enumi" type.
 * @author Vaclav Trojan
 */
public class XDParseEnumi extends XDParseEnum {
	private static final String ROOTBASENAME = "enumi";

	public XDParseEnumi() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		for (String s : _list) {
			if (q.isTokenIgnoreCase(s)) {
				if (q.eos()) {
					p.setEos();
					return;
				}
			}
		}
		p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
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