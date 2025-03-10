package org.xdef.util.xsd2xd.xd;

/** Represents Xdefinition declaration.
 * @author Ilia Alexandrov
 */
public final class XdDecl extends XdModel {
	/** Hashcode.*/
	private int _hashCode = 0;

	/** Creates instance of declaration model.
	 * @param def declaration Xdefinition.
	 * @param name name of delcaration.
	 * @throws NullPointerException if given model Xdefinition or declaration
	 * name is null.
	 * @throws IllegalArgumentException if given declaration name is empty.
	 */
	public XdDecl(XdDef def, String name) {super(def, name);}

	@Override
	public int getType() {return Type.DECLARATION;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdDecl)) {
			return false;
		}
		XdDecl d = (XdDecl) obj;
		return _name.equals(d._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 43 * _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XdDecl[def='" + _def + "', " + "name='" + _name + "']";
	}
}