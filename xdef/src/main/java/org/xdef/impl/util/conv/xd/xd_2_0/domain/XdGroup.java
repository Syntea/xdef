package org.xdef.impl.util.conv.xd.xd_2_0.domain;

/** Represents node group model.
 * @author Ilia Alexandrov
 */
public final class XdGroup extends XdModel {

	/** Hashcode. */
	private int _hashCode = 0;
	/** Group model type. */
	private final int _type;

	/** Creates instance of X-definition node group model.
	 * @param def model Xdefiniton.
	 * @param name group name.
	 * @param type group type.
	 * @throws NullPointerException if given model X-definition or group name is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given group name is empty or given
	 * group type is unknown.
	 */
	public XdGroup(XdDef def, String name, int type) {
		super(def, name);
		_type = type;
	}

	/** Group type getter.
	 * @return group type.
	 */
	public int getGroupType() {
		return _type;
	}

	@Override
	public int getType() {return Type.GROUP;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdGroup)) {
			return false;
		}
		XdGroup g = (XdGroup) obj;
		if (!_def.equals(g._def)) {
			return false;
		}
		if (!_name.equals(g._name)) {
			return false;
		}
		return _type == g._type;
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 41 * _def.hashCode();
			_hashCode = 41 * _hashCode + _name.hashCode();
			_hashCode = 41 * _hashCode + _type;
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XdGroup[def='" + _def.toString() + "', "
				+ "name='" + _name + "', "
				+ "type='" + _type + "']";
	}

	/** Group type id. */
	public static interface GroupType {

		/** Group <code>choice</code> type id. */
		public static final int CHOICE = 1;
		/** Group <code>mixed</code> type id. */
		public static final int MIXED = CHOICE + 1;
		/** Group <code>sequence</code> type id. */
		public static final int SEQUENCE = MIXED + 1;
	}
}