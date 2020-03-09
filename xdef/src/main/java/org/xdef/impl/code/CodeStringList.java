package org.xdef.impl.code;

import org.xdef.XDValue;

/** CodeStringList implements CodeI1 for direct list of string parameters.
 * @author  Vaclav Trojan
 */
public class CodeStringList extends CodeI1 {

	private String[] _list;

	/** Creates a new instance of CodeStringList
	 * @param resultType The type of result.
	 * @param code The code.
	 */
	public CodeStringList(final short resultType, final short code) {
		super(resultType, code);
		_list = new String[0];
	}

	/** Creates a new instance of CodeStringList
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param list The array of objects.
	 */
	public CodeStringList(final short resultType,
		final short code,
		final String[] list) {
		super(resultType, code, list.length);
		_list = list;
	}

	/** Add parameter to the list.
	 * @param par The object to be addted to the list of parameters.
	 */
	public void addParam(final String par) {
		String[] list = new String[_list.length + 1];
		System.arraycopy(_list, 0, list, 0, _list.length);
		list[_list.length] = par;
		_list = list;
		_param++;
	}

	/** Get the list of strings.
	 * @return list of strings.
	 */
	public String[] getStringList() {return _list;}

	@Override
	public String toString() {
		String s = CodeDisplay.getCodeName(_code);
		StringBuilder sb = new StringBuilder(s).append('(');
		for (int i = 0; i < _list.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(_list[i]);
		}
		return sb.append(')').toString();
	}
	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeStringList)) {
			return false;
		}
		CodeStringList x = (CodeStringList) o;
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
				if (_list[i] == null) {
					if (x._list[i] != null) {
						return false;
					}
				} else if (!_list[i].equals(x._list[i])) {
					return false;
				}
			}
		}
		return true;
	}
}