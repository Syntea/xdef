package org.xdef.impl.code;

import org.xdef.XDValue;

/** Code operators.
 * @author Vaclav Trojan
 */
public class CodeI1 extends CodeOp {
	/** Parameter (usually number of method parameters). */
	int _param;

	/** Creates a new instance of CodeI1.
	 * @param resultType The type of result.
	 * @param code The code.
	 */
	public CodeI1(final short resultType, final short code) {
		super(resultType, code);
		_param = 0;
	}

	/** Creates a new instance of CodeI1.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param param The code parameter.
	 */
	public CodeI1(final short resultType, final short code, final int param) {
		super(resultType, code);
		_param = param;
	}

	/** Get code of operation.
	 * @return code of operation.
	 */
	@Override
	public short getCode() {return _code;}

	/** Set code of operation.
	 * @param code the new code of operation.
	 */
	@Override
	public void setCode(final short code) {	_code = code;}

	/** Get result type of operation.
	 * @return The id of result type.
	 */
	@Override
	public short getItemId() {return _resultType;}

	/** Set result type of operation.
	 * @param resultType id of result type.
	 */
	@Override
	public void setItemType(final short resultType) {_resultType = resultType;}

	/** Get parameter of operation.
	 * @return parameter.
	 */
	@Override
	public int getParam() {return _param;}

	/** Set parameter of operation.
	 * @param param value of operation parameter.
	 */
	@Override
	public void setParam(final int param) {_param = param;}

	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	@Override
	public String stringValue() { return toString(); }

	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	@Override
	public XDValue cloneItem() {return new CodeI1(_resultType, _code, _param);}

	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	@Override
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeI1)) {
			return false;
		}
		CodeI1 x = (CodeI1) o;
		return getCode() == x.getCode() && _resultType == x.getItemId() && _param == x.getParam();
	}

	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}