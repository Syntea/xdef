package org.xdef.impl.compile;

import org.xdef.impl.code.CodeI1;
import org.xdef.XDValue;
import org.xdef.impl.XVariable;
import org.xdef.XDValueID;
import org.xdef.sys.SPosition;

/** Represents variable parameters used by compiler.
 * @author Vaclav Trojan
 */
final class CompileVariable extends XVariable {
	/** Object with initial value of a variable. */
	private XDValue _value;
	/** Address of final constant (or  -1 such code address not exists). */
	private int _codeAddr;
	/** Code address of check method or -1 if it not exists. */
	private int _parseMethodAddr;
	/** Type of parsed object or XDValueID.XD_VOID if it not exists. */
	private short _parseResultType;
	/** Reference name to declared type (valid only for uniqueset keys).*/
	private String _refTypeName;
	/** List of code code addresses where method was called (or null). */
	private int[] _postdefs;
	/** Source position where the variable was declared. */
	private final SPosition _spos;

	/** Creates a new instance of ScriptVariable.
	 * @param name Name of variable.
	 * @param type The type of variable.
	 * @param offset The offset of variable.
	 * @param kind kind of variable: 'G' .. global, 'L' .. local, 'X' .. XModel.
	 * @param spos source position where the variable was declared.
	 */
	CompileVariable(final String name,
		final short type,
		final int offset,
		final byte kind,
		final SPosition spos) {
		super(name, type, kind, offset, false, false, false);
		_spos = spos;
		_parseMethodAddr = -1;
		_codeAddr = -1;
		_parseResultType = XDValueID.XD_VOID;
//		_postdefs = null; //java does it
	}

	/** Resolves post-definition of a method (set address to all previous
	 * references).
	 * @param address address of method.
	 * @param g code generator.
	 * @return <tt>false</tt> if address was already set(i.e. error), otherwise
	 * return <tt>true</tt> (i.e. OK).
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

	/** Add address of post defined item to the list. */
	final void addPostDef(final int codeIndex) {
		if (_postdefs == null) {
			_postdefs = new int[]{codeIndex};
		} else {
			int[] x = _postdefs;
			int len = x.length;
			_postdefs = new int[len + 1];
			System.arraycopy(x, 0, _postdefs, 0, len);
			_postdefs[len] = codeIndex;
		}
	}

	/** Clear post-definition info. */
	final void clearPostdefs() {_postdefs = null;}

	/** Get parsed result type of variable. */
	final short getParseResultType() {return _parseResultType;}

	/** Set parsed result type of variable. */
	final void setParseResultType(short type) {_parseResultType = type;}

	/** Get parse method address. */
	final int getParseMethodAddr() {return _parseMethodAddr;}

	/** Set parse method address. */
	final void setParseMethodAddr(int method) {_parseMethodAddr = method;}

	/** Get address of code (of constant).*/
	final int getCodeAddr() {return _codeAddr;}

	/** Set address of code (of constant).*/
	final void setCodeAddr(int codeAddr) {_codeAddr = codeAddr;}

	/** Get value of variable. */
	final XDValue getValue() {return _value;}

	/** Set value of variable. */
	final void setValue(final XDValue value) {_value = value;}

	/** Check if value of variable is a constant. */
	final boolean isConstant() {
		return getType() != XDValueID.XD_BNFGRAMMAR
//			&& getType() != XDValueID.XD_BNFRULE
			&& isFinal() && _value != null;
	}

	/** Get source position where the variable was declared.
	 * @return source position where the variable was declared.
	 */
	final SPosition getSourcePosition() {return _spos;}

	/** Get reference name of declared type (valid only for uniqueset keys).
	 * @return reference name of declared type or null.
	 */
	public final String getKeyRefName() {return _refTypeName;}

	/** Set reference name of declared type (valid only for uniqueset keys).
	 * @param x reference name of declared type or null.
	 */
	public final void setKeyRefName(final String x) {_refTypeName = x;}

	@Override
	/** Set value of variable. */
	public String toString() {return super.toString()
		+ ", parseMethodAddr=" + _parseMethodAddr + ", codeAddr=" + _codeAddr
		+ ", parseResultType="  + CompileBase.getTypeName(_parseResultType)
		+ ", val=" + _value;
	}
}