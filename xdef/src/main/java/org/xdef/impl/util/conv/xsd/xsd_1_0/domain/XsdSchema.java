package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

/** Represents XML Schema.
 * @author Ilia Alexandrov
 */
public final class XsdSchema implements XsdSchemaContainer {

	/** HashCode. */
	private int _hashCode = 0;
	/** Schema file name. */
	private final String _name;
	/** Schema target namespace URI. */
	private final String _targetNS;

	/** Creates instance of schema.
	 *
	 * @param name schema file name.
	 * @param targetNS schema target namespace URI.
	 * @throws NullPointerException if given schema file name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given schema file name is empty.
	 */
	public XsdSchema(String name, String targetNS) {
		if (name == null) {
			throw new NullPointerException("Given schema name is null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given schema name is empty!");
		}
		_name = name;
		if (targetNS != null && targetNS.length() == 0) {
			_targetNS = null;
		} else {
			_targetNS = targetNS;
		}
	}

	/** Schema file name getter.
	 * @return schema file name.
	 */
	public String getName() {return _name;}

	/** Schema target namespace URI getter.
	 * @return schema target namespace URI.
	 */
	public String getTargetNS() {return _targetNS;}

	@Override
	public int getType() {return Type.SINGLE_SCHEMA;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdSchema)) {
			return false;
		}
		XsdSchema s = (XsdSchema) obj;
		return _name.equals(s._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 47 + _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XsdSchema[name='" + _name + "', "
				+ "targetNS='" + _targetNS + "']";
	}
}