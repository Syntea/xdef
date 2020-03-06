package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueID;

/** Implementation of CodeI1 used for switch operation table with integer
 * values.
 * @author Vaclav Trojan
 */
public class CodeSWTableInt extends CodeI1 {

	/** List of integer values. */
	public long[] _list;
	/** List of addresses. */
	public int[] _adrs;

	/** Creates a new instance of CodeSWTableInt. */
	public CodeSWTableInt() {
		super(XDValueID.XD_VOID, CodeTable.SWITCH_I);
		_list = new long[0];
		_adrs = new int[0];
	}

	public final int getTabAddr(long arg) {
		for (int i = 0; i < _list.length; i++) {
			if (arg == _list[i]) {
				return _adrs[i];
			}
		}
		return getParam();
	}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		String s = CodeDisplay.getCodeName(_code);
		StringBuilder sb = new StringBuilder(s).append('(');
		for (int i = 0; i < _list.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(String.valueOf(_list[i]));
			sb.append(':');
			sb.append(String.valueOf(_adrs[i]));
		}
		return sb.append(')').toString();
	}
	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeSWTableInt)) {
			return false;
		}
		CodeSWTableInt x = (CodeSWTableInt) o;
		if (getCode() != x.getCode() ||	getParam() != x.getParam() ||
			_resultType != x.getItemId()) {
			return false;
		}
		if (_list == null) {
			if (x._list != null) {
				return false;
			}
		} else {
			if (_list.length != x._list.length) {
				return false;
			}
			for (int i = 0; i < _list.length; i++) {
				if (_list[i] != x._list[i]) {
					return false;
				}
			}
		}
		return true;
	}
}