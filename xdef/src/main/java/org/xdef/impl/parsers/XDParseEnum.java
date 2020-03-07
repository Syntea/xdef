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
import org.xdef.XDContainer;

/** Parser of X-Script "enum" type.
 * @author Vaclav Trojan
 */
public class XDParseEnum extends XDParserAbstract {
	private static final String ROOTBASENAME = "enum";
	String[] _list;
	public XDParseEnum() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (p.isOneOfTokens(_list) >= 0) {
			return;
		}
		//Incorrect value of &{0}&{1}{: }
		p.errorWithString(XDEF.XDEF809, parserName());
	}
	@Override
	/** Set value of one "sequential" parameter of parser.
	 * @param par "sequential" parameters.
	 */
	public void setParseParam(Object param) {
		_list = new String[] {param.toString()};
	}
	@Override
	/** Set value of two "sequential" parameters of parser.
	 * @param par1 the first "sequential" parameter.
	 * @param par2 the second "sequential" parameter.
	 */
	public void setParseParams(final Object par1, final Object par2) {
		_list = new String[] {par1.toString(), par2.toString()};
	}
	@Override
	/** Set value of two "sequential" parameters of parser.
	 * @param params the array "sequential" parameters.
	 */
	public void setParseParams(final Object[] params) {
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
					throw new SException(XDEF.XDEF809,
						parserName()+" ("+name+ ")", val);
				}
			} else {
				//Illegal parameter name '&{0}'
				throw new SException(XDEF.XDEF801,
					parserName() + " (" + name + ")");
			}
		}
	}

	/** Create _list of strings sorted descendant according to length
	 * and equal items are ignored.
	 * @param val argument to be converted.
	 * @throws SException if an error occurs.
	 */
	void toList(final XDValue val) throws SException {
		_list = null;
		if (val == null || val.isNull()) {
			return;
		}
		XDContainer context;
		if (val.getItemId() == XD_CONTAINER) {
			context = (XDContainer) val;
		} else {
			String s = val.toString().trim();
			context = tokensToContext(s);
		}
		int num = context.getXDItemsNumber();
		if (num == 0) {
			//Incorrect value of '&{0}'&{1}{: }
			throw new SException(XDEF.XDEF809, parserName() + " (argument)",
				context);
		}
		ArrayList<String> list = new ArrayList<String>();
		for(int j = num-1; j >= 0; j--) {
			String s = context.getXDItem(j).toString();
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
	/** Convert string with tokens separated by "|" to array in context.
	 * @param s string with tokens separated by "|".
	 * @return array of strings in context
	 */
	public static XDContainer tokensToContext(final String s) {
		XDContainer context = new DefContainer();
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
				context.addXDItem(new DefString((t+s.substring(j, k)).trim()));
			}
			t = "";
			j = k + 1;
		}
		t += s.substring(j);
		context.addXDItem(new DefString(t.trim()));
		return context;
	}
	@Override
	public XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_list != null) {
			DefContainer c = new DefContainer();
			for (int i = 0; i < _list.length; i++) {
				c.addXDItem(new DefString(_list[i]));
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
		return _list == null && x. _list == null ||
			 _list != null && Arrays.equals(_list, x._list);
	}

}