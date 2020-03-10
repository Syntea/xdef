package org.xdef.impl.util.conv.xd.xd_2_0.domain;

/** Represents X-definition.
 * @author Ilia Alexandrov
 */
public final class XdDef {

	/** Hashcode. */
	private int _hashCode = 0;
	/** X-definition name as is in X-definition<tt>name</tt> attribute. */
	private final String _name;

	/** Creates instance of X-definition representation with given name.
	 * @param name name of X-definition.
	 * @throws NullPointerException if given X-definition name is <tt>null</tt>.
	 * @throws IllegalArgumentException ig given X-definition name is empty.
	 */
	public XdDef(String name) {
		if (name == null) {
			throw new NullPointerException("Given X-definition name is null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given X-definition name is empty!");
		}
		_name = name;
	}
	/** X-definition name getter.
	 * @return name of X-definition.
	 */
	public String getName() { return _name; }
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