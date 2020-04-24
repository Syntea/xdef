package org.xdef.impl.code;

import org.xdef.XDValue;

/** Implements CodeXD with XDValue as the second parameter.
 * @author Vaclav Trojan
 */
public class CodeXD extends CodeI1 {

	private XDValue _p2;

	/** Creates a new instance of CodeX.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param param The code parameter 1.
	 * @param param2 The code parameter 2.
	 */
	public CodeXD(final short resultType,
		final short code,
		final int param,
		final XDValue param2) {
		super(resultType, code, param);
		_p2 = param2;
	}

	@Override
	/** Get value as string.
	 * @return string value of parameter 2.
	 */
	public String stringValue() {return _p2==null ? "null" : _p2.stringValue();}

	/** Set parameter 2.
	 * @param param2 value of parameter 2.
	 */
	public void setParam2(XDValue param2) {_p2 = param2;}

	/** Get parameter 2.
	 * @return value of parameter 2.
	 */
	public XDValue getParam2() {return _p2;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		return CodeDisplay.getCodeName(_code)+"("+_param+","+_p2+")";
	}

	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeXD)) {
			return false;
		}
		CodeXD x = (CodeXD) o;
		return getCode() == x.getCode() && _resultType == x.getItemId() &&
			getParam() == x.getParam() && _p2 == x._p2;
	}
}