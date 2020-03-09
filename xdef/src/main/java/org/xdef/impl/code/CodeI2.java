package org.xdef.impl.code;

import org.xdef.XDValue;

/** Implements CodeI1 with integer as the second parameter.
 * @author  Vaclav Trojan
 */
public class CodeI2 extends CodeI1 {

	private int _p2;

	/** Creates a new instance of CodeI2.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param param The code parameter 1.
	 * @param param2 The code parameter 2.
	 */
	public CodeI2(final short resultType,
		final short code,
		final int param,
		final int param2) {
		super(resultType, code, param);
		_p2 = param2;
	}

	@Override
	/** Get parameter 2.
	 * @return value of the parameter 2.
	 */
	public int intValue() {return _p2;}
	@Override
	/** Get parameter 2.
	 * @return value of the parameter 2.
	 */
	public long longValue() {return _p2;}
	@Override
	/** Get parameter 2.
	 * @return value of the parameter 2.
	 */
	public String stringValue() {return "" + _p2;}

	/** Set parameter 2.
	 * @param param2 value of the parameter 2.
	 */
	public void setParam2(int param2) {_p2 = param2;}
	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeI2)) {
			return false;
		}
		CodeI2 x = (CodeI2) o;
		return getCode() == x.getCode() && _resultType == x.getItemId() &&
			getParam() == x.getParam() && _p2 == x._p2;
	}

	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}