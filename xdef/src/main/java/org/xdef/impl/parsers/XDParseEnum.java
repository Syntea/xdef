package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xdef.XDContainer;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.xon.XonTools;

/** Parser of X-script "enum" type.
 * @author Vaclav Trojan
 */
public class XDParseEnum extends XDParserAbstract {
	private static final String ROOTBASENAME = "enum";
	String[] _list;

	public XDParseEnum() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (quoted) {
			int pos = p.getIndex();
			String x = XonTools.readJString(p);
			for (String s: _list) {
				if (s.equals(x)) {
					p.setParsedValue(p.getParsedString());
					return;
				}
			}
		} else {
			int i;
			if ((i = p.isOneOfTokens(_list)) >= 0) {
				p.setParsedValue(_list[i]);
				return;
			}
		}
		p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of &{0}&{1}{: }
	}

	@Override
	public void setParseSQParams(final Object... params) {
		int n = params.length;
		_list = new String[n];
		for (int i = 0; i < n; i++) {
			_list[i] = params[i].toString();
		}
	}

	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		_list = null;
		XDNamedValue[] items = params.getXDNamedItems();
		for (int i = 0; i < num; i++) {
			String name = items[i].getName();
			if ("argument".equals(name)) {
				XDValue val = items[i].getValue();
				if (val != null && !val.isNull()) {
					if (val.getItemId() == XD_CONTAINER) {
						toList(val);
					}  else {
						_list = new String[]{val.toString()};
					}
				}
				if (_list == null || _list.length == 0) {
					//Incorrect value of '&{0}'&{1}{: }
					throw new SException(XDEF.XDEF809, parserName()+" ("+name+ ")", val);
				}
			} else {
				//Illegal parameter name '&{0}'
				throw new SException(XDEF.XDEF801, parserName() + " (" + name + ")");
			}
		}
	}

	/** Convert string with tokens separated by "|" to array in Container.
	 * @param s string with tokens separated by "|".
	 * @return array of strings in Container
	 */
	public static XDContainer tokensToContainer(final String s) {
		XDContainer container = new DefContainer();
		int j = 0;
		int k;
		String t = "";
		while ((k = s.indexOf('|', j)) >= 0) {
			if (k + 1 < s.length() && s.charAt(k+1) == '|') {
				t += s.substring(j, k + 1);
				j = k+2;
				continue;
			}
			if (k > 0) {
				container.addXDItem(new DefString((t+s.substring(j,k)).trim()));
			}
			t = "";
			j = k + 1;
		}
		t += s.substring(j);
		container.addXDItem(new DefString(t.trim()));
		return container;
	}

	/** Create _list of strings sorted descendant according to length and equal items are ignored.
	 * @param val argument to be converted.
	 * @throws SException if an error occurs.
	 */
	void toList(final XDValue val) throws SException {
		_list = null;
		if (val == null || val.isNull()) {
			return;
		}
		XDContainer container;
		if (val.getItemId() == XD_CONTAINER) {
			container = (XDContainer) val;
		} else {
			String s = val.toString().trim();
			container = tokensToContainer(s);
		}
		int num = container.getXDItemsNumber();
		if (num == 0) {
			//Incorrect value of '&{0}'&{1}{: }
			throw new SException(XDEF.XDEF809, parserName() + " (argument)", container);
		}
		List<String> list = new ArrayList<>();
		for(int j = num-1; j >= 0; j--) {
			String s = container.getXDItem(j).toString();
			for (int k=0; k < list.size(); k++) {
				String t = list.get(k);
				if (t.equals(s)) {
					s = null;
					break;
				}
				if (t.length() < s.length() && s.startsWith(t)) {
					list.add(k, s);
					s = null;
					break;
				}
			}
			if (s != null) {
				list.add(s);
			}
		}
		_list = new String[list.size()];
		list.toArray(_list);
	}

	@Override
	public XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_list != null) {
			DefContainer c = new DefContainer();
			for (String list1 : _list) {
				c.addXDItem(new DefString(list1));
			}
			map.setXDNamedItem("argument", c);
		}
		return map;
	}

	@Override
	public short parsedType() {return XD_STRING;}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEnum) ) {
			return false;
		}
		XDParseEnum x = (XDParseEnum) o;
		return _list == null && x. _list == null || _list != null && Arrays.equals(_list, x._list);
	}
}
