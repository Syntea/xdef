package org.xdef.impl.compile;

import static org.xdef.XDValueID.XD_ANY;
import org.xdef.impl.code.CodeOp;
import static org.xdef.impl.compile.CompileBase.UNDEF_CODE;

/** CodeUndefined
 * @author  Vaclav Trojan
 */
class CodeUndefined extends CodeOp {

	/** Creates a new instance of CodeUndefined. */
	public CodeUndefined() {super(UNDEF_CODE, XD_ANY);}

	@Override
	public String toString() {return "UNDEF";}

	@Override
	public String stringValue() {return toString();}
}