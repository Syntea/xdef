package org.xdef.impl.xml;

import org.xdef.sys.SPosition;
import java.util.Arrays;

/** Container for attribute names, values and source positions of values.
 * @author Vaclav Trojan
 */
public final class KParsedElement {
	private static final int STEP = 16;
	private static final int STEP2 = 32;
	private KParsedAttr[] _attrs;
	private int _size;
	private String _tagname;
	private String _nsURI;
	private SPosition _pos;

	public KParsedElement() {} // _tagname,_nsURI,_pos,_attrs = null; _size=0;

	/** Get size of the list.
	 * @return number of attributes.
	 */
	public final int getLength() {return _size;}

	/** Clear list of attributes. */
	public final void clear() {
		if (_size > 0) {
			if (_size > STEP2) {
				_attrs = null;
			} else {
				Arrays.fill(_attrs, null);
			}
			_size = 0;
		}
		_tagname = null;
		_nsURI = null;
		_pos = null;
	}

	public final void setParsedNameParams(final String nsURI, final String name, final SPosition spos) {
		_tagname = name;
		_nsURI = nsURI;
		_pos = spos;
	}

	public final String getParsedName() {return _tagname;}
	public final SPosition getParsedNameSourcePosition() {return _pos;}
	public final String getParsedNSURI() {return _nsURI;}

	/** Add attribute to the list.
	 * @param item parsed attribute.
	 * @return true if attribute was added.
	 */
	public final boolean addAttr(final KParsedAttr item) {
		if (_attrs == null) {
			_attrs = new KParsedAttr[STEP];
			_attrs[_size++] = item;
			return true;
		}
		String name = item.getName();
		for (int i = _size - 1; i >= 0; i--) {
			if (name.equals(_attrs[i].getName())) {
				return false;
			}
		}
		if (_size >= _attrs.length) {
			KParsedAttr[] list = _attrs;
			_attrs = new KParsedAttr[_size + STEP];
			System.arraycopy(list, 0, _attrs, 0, _size);
		}
		_attrs[_size++] = item;
		return true;
	}

	/** Get attribute at given position from the list.
	 * @param index position of attribute. If value of this argument is out of range of the list the method
	 * return null.
	 * @return attribute from given position or null.
	 */
	public final KParsedAttr getAttr(final int index) {
		if (_attrs == null || index < 0 || index > _size) {
			return null;
		}
		return _attrs[index];
	}

	/** Get attribute with given raw name from the list.
	 * @param name name of required attribute (may be qualified).
	 * @return attribute with given name or null.
	 */
	public final KParsedAttr getAttr(final String name) {
		int i;
		if ((i = indexOf(name)) >= 0) {
			return _attrs[i];
		}
		return null;
	}

	/** Get attribute with given raw name from the list.
	 * @param nsURI namespace URI.
	 * @param name local name or qualified name.
	 * @return attribute with given name or null.
	 */
	public final KParsedAttr getAttrNS(final String nsURI, final String name) {
		int i;
		if (nsURI == null) {
			i = indexOf(name);
		} else {
			i = name.indexOf(':');
			String localname = i <= 0 ? name : name.substring(i + 1);
			i = indexOfNS(nsURI, localname);
		}
		return i >= 0 ? _attrs[i] : null;
	}

	/** Get index of attribute with given raw name in the list.
	 * @param name name of required attribute (may be qualified).
	 * @return index of attribute in the list or -1.
	 */
	public final int indexOf(final String name) {
		for (int i = _size - 1; i >= 0; i--) {
			if (name.equals(_attrs[i].getName())) {
				return i;
			}
		}
		return -1;
	}

	/** Get index of an attribute with namespace URI and local name in the list.
	 * @param nsURI namespace URI.
	 * @param localname local name.
	 * @return index of an attribute in the list or -1.
	 */
	public final int indexOfNS(final String nsURI, final String localname) {
		if (nsURI == null) {
			return -1;
		}
		for (int i = _size - 1; i >= 0; i--) {
			if (nsURI.equals(_attrs[i].getNamespaceURI())) {
				int ndx;
				String name;
				if ((ndx = (name = _attrs[i].getName()).indexOf(':')) >= 0) {
					if (localname.equals(name.substring(ndx + 1))) {
						return i;
					}
				} else {// never happens
					if (localname.equals(name)) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	/** Remove attribute with given name in the list.
	 * @param name name of attribute.
	 * @return removed object or null.
	 */
	public final KParsedAttr remove(final String name) {return remove(indexOf(name));}

	/** Remove attribute with given name in the list.
	 * @param ka KParsedAttr object to be removed.
	 * @return removed object or null.
	 */
	public final KParsedAttr remove(final KParsedAttr ka) {return remove(indexOf(ka.getName()));}

	/** Remove attribute from given position in the list.
	 * @param index position of attribute. If value of this argument
	 * is out of range of the list the method returns null and no
	 * attribute is deleted.
	 * @return removed object or null.
	 */
	public final KParsedAttr remove(int index) {
		if (index < 0 || index >= _size) {
			return null;
		}
		KParsedAttr result = _attrs[index];
		if (--_size == 0) {
			if (_attrs.length > STEP2) {
				_attrs = null; //let gc do the job
			} else {
				_attrs[_size] = null; //let gc do the job
			}
			return result;
		}
		if (_size + STEP2 < _attrs.length) {
			KParsedAttr[] list = _attrs;
			_attrs = new KParsedAttr[_size + STEP];
			if (index > 0) {
				System.arraycopy(list, 0, _attrs, 0, index);
			}
			if (index < _size) {
				System.arraycopy(list, index + 1, _attrs, index, _size - index);
			}
		} else {
			for (int i = index; i < _size; i++) {
				_attrs[i] = _attrs[i + 1];
			}
			_attrs[_size] = null; //let gc do the job
		}
		return result;
	}

	@Override
	public final String toString() {return _tagname + "; uri: " + _nsURI + "; numAttrs: " + _size;}
}