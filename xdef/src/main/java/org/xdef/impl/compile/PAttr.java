package org.xdef.impl.compile;

import org.xdef.sys.SBuffer;

/** Attribute in PNode.
 * @author Trojan
 */
public final class PAttr {
	public final String _name; //qualified name of the attribute
	public String _localName; //Local name of the attribute
	public final SBuffer _value; //Value of attribute
	int _nsindex; //Index to the namespace id (-1 in no namespace)
	public String _nsURI;  //namespace URI
	String _xpathPos; // xpath position
	PNode _parent; // parent node

	/** Create new instance of AttrValue.
	 * @param name the quoted name of attribute.
	 * @param value the SBuffer object with the value of attribute.
	 * @param nsURI namespace.
	 * @param nsindex index to the namespace list.
	 * @param parent parent node.
	 */
	PAttr(final String name,
		final SBuffer value,
		final String nsURI,
		final int nsindex) {
		_name = name;
		_localName = null;
		_nsURI = nsURI;
		_nsindex = nsindex;
		_value = value;
		// _parent = null; // java makes it
	}

	/** Get node name.
	 * @return node name.
	 */
	public final String getName() {return _name;}

	/** Get node name.
	 * @return node name.
	 */
	public final String getPrefix() {
		int ndx = _name.indexOf(':');
		return ndx < 0 ? _name : _name.substring(ndx + 1);
	}

	/** Get node namespace.
	 * @return node namespace.
	 */
	public final String getNamespace() {return _nsURI;}

	/** Get list of child nodes.
	 * @return list of child nodes.
	 */
	public final SBuffer getValue() {return _value;}

	@Override
	/** Check another attribute if it is equal to this one. We consider
	 * two attributes equal if both local names and name spaces are equal.
	 * @param o The object to be compared.
	 */
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof PAttr)) {
			return false;
		}
		PAttr attr = (PAttr) o;
		return _localName.equals(attr._localName) &&
			_nsindex == attr._nsindex;
	}

	@Override
	/** Returns hash code of the object. */
	public int hashCode() {
		int hash = 89 * 7 + _localName.hashCode();
		return 89 * hash + _nsindex;
	}

	@Override
	public String toString() {return _name + "=" + _value.getString();}
}