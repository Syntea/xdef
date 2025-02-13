package org.xdef.util.xsd2xd.xd;

/** Represents Xdefinition.
 * @author Ilia Alexandrov
 */
public final class XdDef {

	/** Hashcode. */
	private int _hashCode = 0;
	/** Xdefinition name as is in Xdefinitionname attribute.*/
	private final String _name;

	/** Creates instance of Xdefinition representation with given name.
	 *
	 * @param name name of Xdefinition.
	 * @throws NullPointerException if given Xdefinition name is null.
	 * @throws IllegalArgumentException ig given Xdefinition name is empty.
	 */
	public XdDef(String name) {
		if (name == null) {
			throw new NullPointerException("Given Xdefinition name is null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given Xdefinition name is empty!");
		}
		_name = name;
	}
	/** Xdefinition name getter.
	 * @return name of Xdefinition.
	 */
	public String getName() {
		return _name;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdDef)) {
			return false;
		}
		return _name.equals(((XdDef) obj)._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 31 * _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {return "XdDef[name='" + _name + "']";}
}