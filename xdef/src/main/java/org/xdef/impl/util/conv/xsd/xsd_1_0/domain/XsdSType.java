package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

/** Represents Schema <tt>simpleType</tt> model.
 * @author Ilia Alexandrov
 */
public final class XsdSType extends XsdModel {

	/** Hashcode. */
	private int _hashCode = 0;

	/** Creates instance of simple type model.
	 *
	 * @param schema model schema.
	 * @param name simple type name.
	 * @throws NullPointerException if given model schema or simple type name is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given simple type name is empty.
	 */
	public XsdSType(XsdSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public int getType() {return Type.SIMPLE_TYPE;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdSType)) {
			return false;
		}
		XsdSType s = (XsdSType) obj;
		if (!_schema.equals(s._schema)) {
			return false;
		}
		return _name.equals(s._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 53 * _schema.hashCode() + _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XsdSType[schema='" + _schema.toString() + "', "
				+ "name='" + _name + "']";
	}
}