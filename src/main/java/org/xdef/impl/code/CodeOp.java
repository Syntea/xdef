package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SError;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;

/** Code operators.
 * @author Vaclav Trojan
 */
public class CodeOp extends XDValueAbstract {

	/** method code id. */
	public short _code;

	/** method result type id. */
	public short _resultType;

	/** Creates a new instance of CodeOp.
	 * @param resultType The type of result.
	 * @param code The code.
	 */
	public CodeOp(final short resultType, final short code) {
		_resultType = resultType;
		_code = code;
	}
	@Override
	/** Get code of operation.
	 * @return code of operation.
	 */
	public short getCode() {return _code;}
	@Override
	/** Set code of operation.
	 * @param code the new code of operation.
	 */
	public void setCode(final short code) {	_code = code;}
	@Override
	/** Get result type of operation.
	 * @return The id of result type.
	 */
	public short getItemId() {return _resultType;}
	@Override
	/** Set result type of operation.
	 * @param resultType id of result type.
	 */
	public void setItemType(final short resultType) {_resultType = resultType;}

	@Override
	/** Get parameter of operation.
	 * @return parameter.
	 */
	public int getParam() {
		//Internal error&{0}{: }
		throw new SError(SYS.SYS066, "setParam on CodeOp");
	}
	@Override
	/** Set parameter of operation.
	 * @param param value of operation parameter.
	 */
	public void setParam(final int param) {
		//Internal error&{0}{: }
		throw new SError(SYS.SYS066, "setParam on CodeOp");
	}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() { return toString(); }
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new CodeOp(_resultType, _code);}
	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeOp)) {
			return false;
		}
		CodeOp x = (CodeOp) o;
		return getCode() == x.getCode() && _resultType == x.getItemId();
	}
	@Override
	public String toString() {return CodeDisplay.codeToString(this);}

	@Override
	public XDValueType getItemType() {return XDValueType.OBJECT;}
}