package org.xdef.impl.code;

import org.xdef.XDValue;

/** Implements CodeI1 with long as the second parameter.
 * @author  Vaclav Trojan
 */
public class CodeL2 extends CodeI1 {

	private long _param2;

	/** Creates a new instance of CodeL2.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param param The code parameter 1.
	 * @param param2 The code parameter 2.
	 */
	public CodeL2(final short resultType,
		final short code,
		final int param,
		final long param2) {
		super(resultType, code, param);
		_param2 = param2;
	}

	@Override
	/** Get parameter 2.
	 * @return value of the parameter 2.
	 */
	public int intValue() {return (int) _param2;}
	@Override
	/** Get parameter 2.
	 * @return value of the parameter 2.
	 */
	public long longValue() {return _param2;}
	@Override
	/** Get parameter 2.
	 * @return value of the parameter 2.
	 */
	public String stringValue() {return "" + _param2;}

	/** Set parameter 2.
	 * @param param2 value of the parameter 2.
	 */
	public void setParam2(long param2) {_param2 = param2;}

	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeL2)) {
			return false;
		}
		CodeL2 x = (CodeL2) o;
		return getCode() == x.getCode() && _resultType == x.getItemId() &&
			getParam() == x.getParam() && _param2 == x._param2;
	}
	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}