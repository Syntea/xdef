package org.xdef;

import org.w3c.dom.Element;

/** Map of named items and sequence items in X-script. Contains both, the table of named items and
 * sequence of items.
 * @author Vaclav Trojan
 */
public interface XDContainer extends XDValue {

	/** Get the item from sequence at given index.
	 * @param index index of item.
	 * @return item at given index or return null.
	 */
	public XDValue getXDItem(int index);

	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	public void addXDItem(XDValue value);

	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	public void addXDItem(String value);

	/** Add item to the end of sequence.
	 * @param value new item.
	 */
	public void addXDItem(Element value);
	/** Set item at position given by index.
	 * @param index index of item item. If index is out of range of items this
	 * method does nothing.
	 * @param value of item.
	 * @return original value or null;
	 */
	public XDValue replaceXDItem(final int index, XDValue value);

	/** Insert item before given index to the sequence.
	 * @param index index of required item.
	 * @param value item to be inserted.
	 */
	public void insertXDItemBefore(int index, XDValue value);

	/** Remove item from the sequence at given index.
	 * @param index the index of item in the sequence which will be removed.
	 * @return removed value or null.
	 */
	public XDValue removeXDItem(int index);

	/** Get number of items in the sequence.
	 * @return number of items in the sequence.
	 */
	public int getXDItemsNumber();

	/** Get array of all items in the sequence.
	 * @return array of all items in the sequence.
	 */
	public XDValue[] getXDItems();

////////////////////////////////////////////////////////////////////////////////

	/** Set named item to the table of named items.
	 * @param item the named item.
	 * @return if the named item not exists then return null or return the named item value which was replaced
	 * in the table of named items.
	 */
	public XDValue setXDNamedItem(XDNamedValue item);

	/** Set named item to the table of named items.
	 * @param name the name of item.
	 * @param value the value of item.
	 * @return if the named item not exists in the table of named items then* return null or return the value
	 * which was replaced.
	 */
	public XDValue setXDNamedItem(String name, XDValue value);

	/** Check if named item exists in the table of named items.
	 * @param name the name of named item.
	 * @return true if and only if named item exists in the table.
	 */
	public boolean hasXDNamedItem(String name);

	/** Get named item from the table of named items.
	 * @param name the name of named item.
	 * @return if item not exists in table return null or
	 * return the named item from the table of named items.
	 */
	public XDNamedValue getXDNamedItem(String name);

	/** Get value of named item from the table of named items as string.
	 * @param name the name of named item.
	 * @return if item not exists in table return null or
	 * return the value of named item as string.
	 */
	public String getXDNamedItemAsString(String name);

	/** Get value of named item from the table of named items.
	 * @param name the name of named item.
	 * @return if item not exists the return null or return the named item.
	 */
	public XDValue getXDNamedItemValue(String name);

	/** Remove named item from the table of named items.
	 * @param name the name of named item.
	 * @return the removed named item or null.
	 */
	public XDValue removeXDNamedItem(final String name);

	/** Get name of i-th named item.
	 * @param index index of item.
	 * @return name of item.
	 */
	public String getXDNamedItemName(int index);

	/** Get array with named items in the table.
	 * @return array with named items.
	 */
	public XDNamedValue[] getXDNamedItems();

	/** Get number of named items in the table of named items.
	 * @return The number of items.
	 */
	public int getXDNamedItemsNumber();

	/** Create new XDContainer with all elements from XDContainer.
	 * @return new XDContainer with elements.
	 */
	public XDContainer getXDElements();

	/** Get all elements with given name from XDContainer.
	 * @param name The name of element.
	 * @return new XDContainer with elements.
	 */
	public XDContainer getXDElements(String name);

	/** Get the n-th element from XDContainer or null.
	 * @param n The index of element.
	 * @return the n-th element from XDContainer or null.
	 */
	public Element getXDElement(int n);

	/** Create element from XDContainer.
	 * @param nsUri of created element.
	 * @param name of created element.
	 * @return element created from this XDContainer.
	 */
	public Element toElement(String nsUri, String name);

	/** Get all elements with given name and namespace from XDContainer.
	 * @param nsURI namespace URI.
	 * @param localName local name of element.
	 * @return new XDContainer with all elements with given name and namespace.
	 */
	public XDContainer getXDElementsNS(String nsURI, String localName);

	/** Get all text nodes concatenated as a string.
	 * @return string concatenated from all text nodes.
	 */
	public String getXDText();

	/** Get string from n-th item from this XDContainer. If the node does not exist or if it is not text
	 * then return the empty string.
	 * @param n The index of item.
	 * @return The string.
	 */
	public String getXDTextItem(int n);

	/** Check if the XDContainer is empty.
	 * @return true if the object is empty; otherwise returns false.
	 */
	public boolean isEmpty();

	/** Sorts this XDContainer.
	 * If an item is an org.w3c.Node object then as a key it is used the text value of an item).
	 * @param asc if true the XDContainer will be sorted ascendant, otherwise descendant.
	 * @return this XDContainer sorted.
	 */
	public XDContainer sortXD(boolean asc);

	/** Sorts this XDContainer.
	 * @param key String with xpath expression or null (if null or empty string then for org.w3c.Node items
	 * it is used as a key the text value of an item). For items other then  org.w3c.Node objects this
	 * parameter is ignored.
	 * @param asc if true the XDContainer will be sorted ascendant, otherwise descendant.
	 * @return this XDContainer sorted.
	 */
	public XDContainer sortXD(String key, boolean asc);
}