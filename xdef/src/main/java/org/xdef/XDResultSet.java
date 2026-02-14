package org.xdef;

import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Iterator/Map used as context in create mode of X-definition in X-script.
 * This interface can be either an iterator or a map.
 * @author Vaclav Trojan
 */
public interface XDResultSet extends XDValue {

	/** Get next item of this iterator or null. If the object has to be closed then if no more values are available
	 * the close() method must be invoked.
	 * @param xnode XXnode from which this method was called (may be null).
	 * @return the next value of this iterator or return null.
	 * @throws SRuntimeException id an error occurs.
	 */
	public XDValue nextXDItem(XXNode xnode) throws SRuntimeException;

	/** Get the item returned by last nextItem method or return null.
	 * @return item returned by last nextItem method or return null.
	 */
	public XDValue lastXDItem();

	/** Get constructor for creation of item.
	 * @return constructor for creation of item.
	 */
	public XDConstructor getXDConstructor();

	/** Get statement from which ResultSet was created.
	 * @return statement from which ResultSet was created.
	 */
	public XDStatement getStatement();

	/** Set constructor for creation of item.
	 * @param constructor constructor for creation of item.
	 */
	public void setXDConstructor(XDConstructor constructor);

	/** Get count of iteration.
	 * @return count of iteration.
	 */
	public int getCount();

	/** Return value of iterated object as string if it has a string value (text of element), otherwise return null.
	 * @return value of iterated object as string or return null.
	 */
	public String itemAsString();

	/** If the iterated object is Container then return relevant item value as string, otherwise return null.
	 * @param index the index of item.
	 * @return value of the specified item as a string or return null.
	 */
	public String itemAsString(int index);

	/** If iterated object has named items then return relevant item value as string, otherwise return null.
	 * @param name name of map item.
	 * @return value of map item as a string or return null.
	 */
	public String itemAsString(String name);

	/** If the iterated object has named items, then return true if the specified item exists.
	 * @param name name item.
	 * @return true if and only if the specified item exists.
	 */
	public boolean hasItem(String name);

	/** If iterator is created from an array then return the size of array, otherwise otherwise return -1.
	 * @return size of array or -1.
	 */
	public int getSize();

   /** Close this iterator and release all allocated resources.*/
	public void close();

	/** Closes both this iterator and the underlying Statement from which this ResultSet created. */
	public void closeStatement();

   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	public boolean isClosed();
}