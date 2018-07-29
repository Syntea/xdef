/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: PAttr.java, created 2018-07-22.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.sys.SBuffer;

/** Attribute in PNode.
 * @author Trojan
 */
public final class PAttr {
	public final String _name; //qualified name of the attribute
	public String _localName; //Local name of the attribute
	public final SBuffer _value; //Value of attribute
	int _nsindex; //Index to the namespace id (-1 in no namespace)
	public String _nsURI;  //namespace URI

	/** Create new instance of AttrValue.
	 * @param name the quoted name of attribute.
	 * @param nsURI namespace.
	 * @param nsindex index to namespace list.
	 * @param value the SBuffer object with the value of attribute.
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
	}

	/** Get node name.
	 * @return node name.
	 */
	public final String getName() {return _name;}

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