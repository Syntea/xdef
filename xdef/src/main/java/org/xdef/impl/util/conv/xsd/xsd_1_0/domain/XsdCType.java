package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

/** Represents XML Schema <tt>complexType</tt> model.
 * @author Ilia Alexandrov
 */
public final class XsdCType extends XsdModel {

	/** HashCode. */
	private int _hashCode = 0;
	/** External attribute group name. */
	private String _extAttrGroupName;
	/** External group name. */
	private String _extGroupName;
	/** External simple type name. */
	private String _extSTypeName;

	/** Creates instance of complex type model.
	 *
	 * @param schema model schema.
	 * @param name complex type name.
	 * @throws NullPointerException if given model schema or complex type name
	 * is <tt>null</tt>.
	 * @throws IllegalArgumentException if given complex type name is empty.
	 */
	public XsdCType(XsdSchema schema, String name) {
		super(schema, name);
	}

	/** Sets external attribute group name.
	 *
	 * @param attrGrpName external attribute group model name.
	 * @throws NullPointerException if given attribute group name
	 * is <tt>null</tt>.
	 * @throws IllegalArgumentException if given attribute group name is empty.
	 * @throws IllegalStateException if external attribute group name
	 * is already set.
	 */
	public void setAttrGroup(String attrGrpName) {
		if (attrGrpName == null) {
			throw new NullPointerException("Given attribute group name is null!");
		}
		if (attrGrpName.length() == 0) {
			throw new IllegalArgumentException("Given attribute group name is empty!");
		}
		if (_extAttrGroupName != null) {
			throw new IllegalStateException("External attribute group name is already set!");
		}
		_extAttrGroupName = attrGrpName;
	}

	/** External attribute group getter.
	 * @return external attribute group.
	 */
	public String getAttrGroup() {return _extAttrGroupName;}

	/** Sets external group name.
	 * @param groupName external group name.
	 * @throws NullPointerException if given group name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given group name is empty.
	 * @throws IllegalStateException if external group name is already set.
	 */
	public void setGroup(String groupName) {
		if (groupName == null) {
			throw new NullPointerException("Given group name is null!");
		}
		if (groupName.length() == 0) {
			throw new IllegalArgumentException("Given group name is empty!");
		}
		if (_extGroupName != null) {
			throw new IllegalStateException("External group name is already set!");
		}
		_extGroupName = groupName;
	}

	/** External group name getter.
	 * @return external group name.
	 */
	public String getGroup() {return _extGroupName;}

	/** Sets external simple type name.
	 *
	 * @param sTypeName external simple type name.
	 * @throws NullPointerException if given simple type name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given simple type name is empty.
	 * @throws IllegalStateException if external simple type is already set.
	 */
	public void setSType(String sTypeName) {
		if (sTypeName == null) {
			throw new NullPointerException("Given simple type is null!");
		}
		if (sTypeName.length() == 0) {
			throw new IllegalArgumentException("Given simple type name is empty!");
		}
		if (_extSTypeName != null) {
			throw new IllegalStateException("External simple type name is already set!");
		}
		_extSTypeName = sTypeName;
	}

	/** Gets external simple type name.
	 * @return external simple type name or <tt>null</tt>.
	 */
	public String getSType() {return _extSTypeName;}

	@Override
	public int getType() {return Type.COMPLEX_TYPE;}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdCType)) {
			return false;
		}
		XsdCType c = (XsdCType) obj;
		if (!_schema.equals(c._schema)) {
			return false;
		}
		return _name.equals(c._name);
	}

	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 67 * _schema.hashCode() + _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XsdCType[schema='" + _schema + "',"
				+ "name='" + _name + "', "
				+ "extAttrGroupName='" + _extAttrGroupName + "', "
				+ "extGroupName='" + _extGroupName + "', "
				+ "extSTypeName='" + _extSTypeName + "]";
	}
}