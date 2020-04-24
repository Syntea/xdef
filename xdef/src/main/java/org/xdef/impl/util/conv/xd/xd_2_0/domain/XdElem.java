package org.xdef.impl.util.conv.xd.xd_2_0.domain;

/** Represents element model.
 * @author Ilia Alexandrov
 */
public final class XdElem extends XdModel {

	/** HashCode. */
	private int _hashCode = 0;
	/** Element nameSpace. */
	private final String _namespace;

	/** Creates instance of element model.
	 * @param def model X-definition.
	 * @param namespace element namespace.
	 * @param name element name.
	 * @throws NullPointerException if given model X-definition or element name
	 * is <tt>null</tt>.
	 * @throws IllegalArgumentException if given element name is empty.
	 */
	public XdElem(XdDef def, String namespace, String name) {
		super(def, name);
		_namespace = namespace != null && namespace.length() == 0
			? null : namespace;
	}
	/** Element namespcae getter.
	 * @return element namespace.
	 */
	public String getNamespace() {return _namespace;}
	@Override
	public int getType() {return Type.ELEMENT;}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdElem)) {
			return false;
		}
		XdElem e = (XdElem) obj;
		if (!_def.equals(e._def)) {
			return false;
		}
		if (_namespace == null ? e._namespace != null : !_namespace.equals(e._namespace)) {
			return false;
		}
		 return _name.equals(e._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 43 * _def.hashCode();
			_hashCode = 43 * _hashCode + (_namespace == null ? 0 : _namespace.hashCode());
			_hashCode = 43 * _hashCode + _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XDElem[def='" + _def.toString() + "', "
				+ "namespace='" + _namespace + "', "
				+ "name='" + _name + "']";
	}

	/** Element types. */
	public static interface ElemType {
		/** Element has no text, no attributes and no child element nodes. */
		public static final int EMPTY = 1;
		/** Element has only text node(s). */
		public static final int TEXT = EMPTY + 1;
		/** Element has only attribute node(s). */
		public static final int ATTR = TEXT + 1;
		/** Element has only child element node(s). */
		public static final int CHLD = ATTR + 1;
		/** Element has text and attribute nodes. */
		public static final int TEXT_ATTR = CHLD + 1;
		/** Element has text and child element nodes. */
		public static final int TEXT_CHLD = TEXT_ATTR + 1;
		/** Element has attribute and child element nodes. */
		public static final int ATTR_CHLD = TEXT_CHLD + 1;
		/** Element has text, attribute and child element nodes. */
		public static final int TEXT_ATTR_CHLD = ATTR_CHLD + 1;
	}
}