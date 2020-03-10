package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

/** Represents XML Schema <tt>group</tt> model.
 * @author Ilia Alexandrov
 */
public final class XsdGroup extends XsdModel {

	/** Hashcode. */
	private int _hashCode = 0;

	/** Creates instance of group model.
	 *
	 * @param schema model schema.
	 * @param name group name.
	 * @throws NullPointerException if given model schema or group name is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given group name is empty.
	 */
	public XsdGroup(XsdSchema schema, String name) {
		super(schema, name);
	}
	@Override
	public int getType() {return Type.GROUP;}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdGroup)) {
			return false;
		}
		XsdGroup g = (XsdGroup) obj;
		if (!_schema.equals(g._schema)) {
			return false;
		}
		return _name.equals(g._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 59 *_schema.hashCode() + _hashCode + _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XsdGroup[schema='" + _schema.toString() + "', "
				+ "name='" + _name + "']";
	}
}