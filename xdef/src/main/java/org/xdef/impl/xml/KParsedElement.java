package org.xdef.impl.xml;

import org.xdef.sys.SPosition;
import java.util.Arrays;

/** Container for attribute names, values and source positions of values.
 * @author Vaclav Trojan
 */
public class KParsedElement {

	private static final int STEP = 16;
	private static final int STEP2 = 32;

	private KParsedAttr[] _list;
	private int _size;
	private String _tagname;
	private String _nsURI;
	private SPosition _pos;

	public KParsedElement() {
//		_tagname = null; _nsURI = null; _pos = null; _size = 0; _list = null;
	}

	/** Get size of the list.
	 * @return number of attributes.
	 */
	public int getLength() {return _size;}

	/** Clear list of attributes. */
	public void clear() {
		if (_size > 0) {
			if (_size > STEP2) {
				_list = null;
			} else {
				Arrays.fill(_list, null);
			}
			_size = 0;
		}
		_tagname = null;
		_nsURI = null;
		_pos = null;
	}

	public void setParsedNameParams(final String nsURI,
		final String name,
		final SPosition spos) {
		_tagname = name;
		_nsURI = nsURI;
		_pos = spos;
	}

	public String getParsedName() {return _tagname;}
	public SPosition getParsedNameSourcePosition() {return _pos;}
	public String getParsedNSURI() {return _nsURI;}

	/** Add attribute to the list.
	 * @param item parsed attribute.
	 * @return true if attribute was added.
	 */
	public boolean addAttr(KParsedAttr item) {
		if (_list == null) {
			_list = new KParsedAttr[STEP];
			_list[_size++] = item;
			return true;
		}
		String name = item.getName();
		for (int i = _size - 1; i >= 0; i--) {
			if (name.equals(_list[i].getName())) {
				return false;
			}
		}
		if (_size >= _list.length) {
			KParsedAttr[] list = _list;
			_list = new KParsedAttr[_size + STEP];
			System.arraycopy(list, 0, _list, 0, _size);
		}
		_list[_size++] = item;
		return true;
	}

	/** Get attribute at given position from the list.
	 * @param index position of attribute. If value of this argument
	 * is out of range of the list the method returns <tt>null</tt>.
	 * @return attribute from given position or <tt>null</tt>.
	 */
	public KParsedAttr getAttr(int index) {
		if (index < 0 || index > _size) {
			return null;
		}
		return _list[index];
	}

	/** Get attribute with given raw name from the list.
	 * @param name name of required attribute (may be qualified).
	 * @return attribute with given name or <tt>null</tt>.
	 */
	public KParsedAttr getAttr(final String name) {
		int i;
		if ((i = indexOf(name)) >= 0) {
			return _list[i];
		}
		return null;
	}

	/** Get attribute with given raw name from the list.
	 * @param nsURI name space URI.
	 * @param name local name or qualified name.
	 * @return attribute with given name or <tt>null</tt>.
	 */
	public KParsedAttr getAttrNS(final String nsURI, final String name) {
		int i;
		if (nsURI == null) {
			i = indexOf(name);
		} else {
			i = name.indexOf(':');
			String localname = i <= 0 ? name : name.substring(i + 1);
			i = indexOfNS(nsURI, localname);
		}
		return i >= 0 ? _list[i] : null;
	}

	/** Get index of attribute with given raw name in the list.
	 * @param name name of required attribute (may be qualified).
	 * @return index of attribute in the list or <tt>-1</tt>.
	 */
	public int indexOf(final String name) {
		for (int i = _size - 1; i >= 0; i--) {
			if (name.equals(_list[i].getName())) {
				return i;
			}
		}
		return -1;
	}

	/** Get index of an attribute with namespace URI and local name in the list.
	 * @param nsURI namespace URI.
	 * @param localname local name.
	 * @return index of an attribute in the list or <tt>-1</tt>.
	 */
	public int indexOfNS(final String nsURI, final String localname) {
		if (nsURI == null) {
			return -1;
		}
		for (int i = _size - 1; i >= 0; i--) {
			if (nsURI.equals(_list[i].getNamespaceURI())) {
				int ndx;
				String name;
				if ((ndx = (name = _list[i].getName()).indexOf(':')) >= 0) {
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
	 * @return removed object or <tt>null</tt>.
	 */
	public KParsedAttr remove(String name) {
		return remove(indexOf(name));
	}

	/** Remove attribute with given name in the list.
	 * @param ka KParsedAttr object to be removed.
	 * @return removed object or <tt>null</tt>.
	 */
	public KParsedAttr remove(final KParsedAttr ka) {
		return remove(indexOf(ka.getName()));
	}

	/** Remove attribute from given position in the list.
	 * @param index position of attribute. If value of this argument
	 * is out of range of the list the method returns <tt>null</tt> and no
	 * attribute is deleted.
	 * @return removed object or <tt>null</tt>.
	 */
	public KParsedAttr remove(int index) {
		if (index < 0 || index >= _size) {
			return null;
		}
		KParsedAttr result = _list[index];
		if (--_size == 0) {
			if (_list.length > STEP2) {
				_list = null; //let gc do the job
			} else {
				_list[_size] = null; //let gc do the job
			}
			return result;
		}
		if (_size + STEP2 < _list.length) {
			KParsedAttr[] list = _list;
			_list = new KParsedAttr[_size + STEP];
			if (index > 0) {
				System.arraycopy(list, 0, _list, 0, index);
			}
			if (index < _size) {
				System.arraycopy(list, index + 1, _list, index, _size - index);
			}
		} else {
			for (int i = index; i < _size; i++) {
				_list[i] = _list[i + 1];
			}
			_list[_size] = null; //let gc do the job
		}
		return result;
	}

	@Override
	public String toString() {
		return _tagname + "; uri: " + _nsURI + "; numAttrs: " + _size;
	}
}