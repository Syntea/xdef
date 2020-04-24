package org.xdef.impl.code;

import org.xdef.XDValue;

/** Implements CodeI1 with string as the second parameter.
 * @author  Vaclav Trojan
 */
public class CodeS1 extends CodeI1 {

	private String _param2;

	/** Creates a new instance of CodeString.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param spar The parameter string.
	 */
	public CodeS1(final short resultType,
		final short code,
		final String spar) {
		this(resultType, code, 0, spar);
	}

	/** Creates a new instance of CodeString.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param param The integer parameter.
	 * @param spar The parameter string.
	 */
	public CodeS1(final short resultType,
		final short code,
		int param,
		final String spar) {
		super(resultType, code, param);
		_param2 = spar;
	}

	@Override
	public String stringValue() {return _param2;}

	/** Set parameter 2.
	 * @param param2 value of the parameter 2.
	 */
	public void setParam2(String param2) {_param2 = param2;}

	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeS1)) {
			return false;
		}
		CodeS1 x = (CodeS1) o;
		if (getCode() != x.getCode() ||	getParam() != x.getParam() ||
			_resultType != x.getItemId()) {
			return false;
		}
		return _param2 == null ? x._param2 == null : _param2.equals(x._param2);
	}
	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}