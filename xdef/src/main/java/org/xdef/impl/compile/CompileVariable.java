package org.xdef.impl.compile;

import org.xdef.impl.code.CodeI1;
import org.xdef.XDValue;
import org.xdef.impl.XVariable;
import static org.xdef.XDValueID.XD_BNFGRAMMAR;
import static org.xdef.impl.compile.CompileBase.getTypeName;
import org.xdef.sys.SPosition;

/** Represents variable parameters used by compiler.
 * @author Vaclav Trojan
 */
public final class CompileVariable extends XVariable {
	/** Object with initial value of a variable. */
	private XDValue _value;
	/** Address of final constant (or  -1 such code address not exists). */
	private int _codeAddr;
	/** List of code code addresses where method was called (or null). */
	private int[] _postdefs;
	/** Source position where variable was declared. */
	private final SPosition _spos;

	/** Creates a new instance of ScriptVariable.
	 * @param name Name of variable.
	 * @param typ The type of variable.
	 * @param offset The offset of variable.
	 * @param kind kind of variable: 'G' .. global, 'L' .. local, 'X' .. XModel.
	 * @param pos source position where the variable was declared.
	 */
	CompileVariable(final String name,final short typ,final int offset,final byte kind,final SPosition pos) {
		super(name, typ, kind, offset, false, false, false);
		_spos = pos;
		_codeAddr = -1;
	}

	/** Resolves post-definition of a method (set address to all previous references).
	 * @param address address of method.
	 * @param g code generator.
	 * @return false if address was already set(i.e. error), otherwise return true (i.e. OK).
	 */
	final boolean resolvePostDef(final int address, final CompileCode g) {
		if (getOffset() != -1 || getKind() != 'G') {
			return false; //not global, no postdefinitions
		}
		setOffset(address);
		if (_postdefs != null) {
			for (int i = 0; i < _postdefs.length; i++) {
				((CodeI1) g.getCodeItem(_postdefs[i])).setParam(address);
			}
			_postdefs = null;
		}
		return true;
	}

	/** Clear post-definition info. */
	final void clearPostdefs() {_postdefs = null;}

	/** Get address of code (of constant).*/
	final int getCodeAddr() {return _codeAddr;}

	/** Set address of code (of constant).*/
	final void setCodeAddr(int codeAddr) {_codeAddr = codeAddr;}

	/** Get value of variable. */
	final XDValue getValue() {return _value;}

	/** Set value of variable. */
	final void setValue(final XDValue value) {_value = value;}

	/** Check if value of variable is a constant. */
	final boolean isConstant() {return getType() != XD_BNFGRAMMAR && isFinal() && _value != null;}

	/** Get source position where the variable was declared.
	 * @return source position where the variable was declared.
	 */
	public final SPosition getSourcePosition() {return _spos;}

	@Override
	/** Set value of variable. */
	public String toString() {
		return super.toString() + ", parseMethodAddr=" + getParseMethodAddr() + ", codeAddr=" + _codeAddr
			+ ", parseResultType="  + getTypeName(getParseResultType()) + ", val=" + _value;
	}
}