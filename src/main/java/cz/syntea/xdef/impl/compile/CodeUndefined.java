package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.impl.code.CodeOp;
import cz.syntea.xdef.XDValueID;

/** CodeUndefined
 * @author  Vaclav Trojan
 */
class CodeUndefined extends CodeOp {

	/** Creates a new instance of CodeUndefined. */
	public CodeUndefined() {
		super(CompileBase.UNDEF_CODE, XDValueID.XD_ANY);
	}
	@Override
	public String toString() {return "UNDEF";}
	@Override
	public String stringValue() {return toString();}
}
