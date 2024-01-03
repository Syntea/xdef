package org.xdef.impl.compile;

import org.xdef.impl.code.CodeOp;

/** CodeUndefined
 * @author  Vaclav Trojan
 */
class CodeUndefined extends CodeOp {

	/** Creates a new instance of CodeUndefined. */
	public CodeUndefined() {
		super(CompileBase.UNDEF_CODE, XD_ANY);
	}
	@Override
	public String toString() {return "UNDEF";}
	@Override
	public String stringValue() {return toString();}
}