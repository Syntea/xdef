package org.xdef.impl.util.conv.type.domain;

/** Represents other type.
 * @author Ilia Alexandrov
 */
public class Other extends ValueType {

	/** Simple type switch. */
	private boolean _simple = true;
	/** Type name. */
	private final String _name;
	/** Name of XDefinition where it was declared. */
	private String _xdName;

	/** Creates instance of other type.
	 * @param name type name.
	 * @throws NullPointerException if given type name is <code>null</code>.
	 * @throws IllegalArgumentException if given type name is empty.
	 */
	public Other(final String name) {
		if (name == null) {
			throw new NullPointerException("Given name is null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given name is empty!");
		}
		_name = name;
	}

	/** Gets type name.
	 * @return type name.
	 */
	public String getName() {return _name;}

	/** Returns <code>true</code> if type is simple (does not contain parameters).
	 * @return <code>true</code> if type is simple.
	 */
	public boolean isSimple() {return _simple;}

	/** Sets type simplicity.
	 * @param isSimple simple type switch.
	 */
	public void setSimple(boolean isSimple) {_simple = isSimple;}

	@Override
	public int getKind() {return OTHER;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Other)) {
			return false;
		}
		Other o = (Other) obj;
		return _name.equals(o._name);
	}

	/** Set name of X-definition.
	 * @param xdName of X-definition where the type was declared.
	 */
	public final void setXdefName(final String xdName) {_xdName = xdName;}

	/** Get name of X-definition.
	 * @return name of X-definition where the type was declared.
	 */
	public final String getXdefName() {return _xdName;}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + (this._name != null ? this._name.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "Other[name='" + _name + "', " + "simple='" + _simple
			+ "', xd='" + _xdName + "']";
	}
}