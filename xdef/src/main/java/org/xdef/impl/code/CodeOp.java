package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.OBJECT;
import org.xdef.msg.SYS;
import org.xdef.sys.SError;

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
	public CodeOp(final short resultType, final short code) {_resultType = resultType; _code = code;}

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
	public int getParam() {
		throw new SError(SYS.SYS066, "setParam on CodeOp"); //Internal error&{0}{: }
	}

	/** Set parameter of operation.
	 * @param param value of operation parameter.
	 */
	@Override
	public void setParam(final int param) {
		throw new SError(SYS.SYS066, "setParam on CodeOp"); //Internal error&{0}{: }
	}

	/** Get string value of this object.
	 * @return string value of this object.
	 */
	@Override
	public String stringValue() { return toString(); }

	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	@Override
	public XDValue cloneItem() {return new CodeOp(_resultType, _code);}

	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	@Override
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeOp)) {
			return false;
		}
		CodeOp x = (CodeOp) o;
		return getCode() == x.getCode() && _resultType == x.getItemId();
	}

	@Override
	public XDValueType getItemType() {return OBJECT;}

	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}