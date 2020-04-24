package org.xdef.impl.util.conv.type.domain;

/** Represents XDefinition list type.
 * @author Ilia Alexandrov
 */
public class XdefList extends ValueType {

	/**
	 * List item type.
	 */
	private ValueType _item;
	/**
	 * List item delimeter.
	 */
	private String _delimeter;

	/** Gets list item type.
	 *
	 * @return list item type or <code>null</code>.
	 */
	public ValueType getItem() {
		return _item;
	}

	/** Sets list item type.
	 *
	 * @param item list item type.
	 * @throws NullPointerException if given item type is <code>null</code>.
	 */
	public void setItem(ValueType item) {
		if (item == null) {
			throw new NullPointerException("Given item is null!");
		}
		_item = item;
	}

	/** Gets list item delimeter.
	 *
	 * @return list item delimeter or <code>null</code>.
	 */
	public String getDelimeter() {
		return _delimeter;
	}

	/** Sets list item delimeter.
	 *
	 * @param delimenter item delimeter.
	 * @throws NullPointerException if given item delimeter is <code>null</code>.
	 */
	public void setDelimeter(String delimenter) {
		if (delimenter == null) {
			throw new NullPointerException("Given item delimenter is null!");
		}
		_delimeter = delimenter;
	}

	@Override
	public int getKind() {return XDEF_LIST;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdefList)) {
			return false;
		}
		XdefList l = (XdefList) obj;
		if (_item == null ? l._item != null : !_item.equals(l._item)) {
			return false;
		}
		return !(_delimeter == null ?
			l._delimeter != null : !_delimeter.equals(l._delimeter));
	}

	@Override
	public int hashCode() {
		int hash = (_item != null ? _item.hashCode() : 0);
		return 3 * hash + (_delimeter != null ? _delimeter.hashCode() : 0);
	}

	@Override
	public String toString() {
		return "XdefList[item='" + _item + "', delimeter='" + _delimeter + "']";
	}
}