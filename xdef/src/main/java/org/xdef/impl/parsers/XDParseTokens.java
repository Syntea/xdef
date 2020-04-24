package org.xdef.impl.parsers;

import org.xdef.sys.SException;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;
import java.util.Arrays;
import org.xdef.XDContainer;

/** Parser of X-Script "tokens" type.
 * @author Vaclav Trojan
 */
public class XDParseTokens extends XDParseEnum {
	private static final String ROOTBASENAME = "tokens";
	public XDParseTokens() {
		super();
	}
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		XDNamedValue[] items = params.getXDNamedItems();
		for (int i = 0; i < num; i++) {
			if ("argument".equals(items[i].getName())) {
				toList(items[i].getValue());
			}
		}
	}
	@Override
	public XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_list != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < _list.length; i++) {
				if (i > 0) {
					sb.append(" | ");
				}
				String s = _list[i];
				for (int j = 0; j < s.length(); j++) {
					char c;
					if ((c = s.charAt(j)) == '|') {
						sb.append('|');
					}
					sb.append(c);
				}
			}
			map.setXDNamedItem("argument", new DefString(sb.toString()));
		}
		return map;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseTokens) ) {
			return false;
		}
		XDParseTokens x = (XDParseTokens) o;
		return _list!=null && x. _list!=null && Arrays.equals(_list, x._list);
	}
}