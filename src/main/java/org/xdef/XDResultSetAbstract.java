package org.xdef;

import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** The abstract class for implementation of user objects with the
 * implementation of XDResultSet interface.
 * @author Vaclav Trojan
 */
public abstract class XDResultSetAbstract extends XDValueAbstract
	implements XDResultSet {

	@Override
	/** Get ID of the type of this object (i.e. XDValueTypes.RESULTSET_VALUE).
	 * @return item type (RESULTSET_VALUE).
	 */
	public final short getItemId() {return XDValueID.XD_RESULTSET;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.RESULTSET;}

	@Override
	/** Get next item of this iterator or <tt>null</tt>. If the object
	 * has to be closed then if no more values are available the close() method
	 * must be invoked.
	 * @param xnode XXnode from which this method was called.
	 * @return the next value of this iterator or return <tt>null</tt>.
	 * @throws SRuntimeException id an error occurs.
	 */
	abstract public XDValue nextXDItem(XXNode xnode) throws SRuntimeException;

	@Override
	/** Get the item returned by last nextItem method or return <tt>null</tt>.
	 * @return item returned by last nextItem method or return <tt>null</tt>.
	 */
	abstract public XDValue lastXDItem() throws SRuntimeException;

	@Override
	/** Set constructor for creation of item.
	 * @param constructor constructor for creation of item.
	 */
	public void setXDConstructor(XDConstructor constructor) {}

	@Override
	/** Get constructor for creation of item.
	 * @return constructor for creation of item.
	 */
	public XDConstructor getXDConstructor() {return null;}

	@Override
	/** Get count of iteration.
	 * @return count of iteration.
	 */
	abstract public int getCount();

	@Override
	/** Return value of iterated object as string if it has a string value
	 * (text of element); otherwise return <tt>null</tt>.
	 * @return value of iterated object as string or return <tt>null</tt>.
	 */
	public String itemAsString() {return null;}

	@Override
	/** If the iterated object is a context then return relevant item value
	 * as string; otherwise return <tt>null</tt>.
	 * @param index the index of item.
	 * @return value of the specified item as a string or return <tt>null</tt>.
	 */
	public String itemAsString(int index) {return null;}

	@Override
	/** If the iterated object has named items then return relevant item value
	 * as string; otherwise return <tt>null</tt>.
	 * @param name name of map item.
	 * @return value of map item as a string or return <tt>null</tt>.
	 */
	abstract public String itemAsString(String name);

	@Override
	/** If the iterated object has named items, then return <tt>true</tt> if
	 * the specified item exists.
	 * @param name name item.
	 * @return <tt>true</tt> if and only if the specified item exists.
	 */
	abstract public boolean hasItem(String name);

	@Override
	/** If this iterator is created from an array then return the size of array,
	 * otherwise otherwise return -1.
	 * @return size of array or -1.
	 */
	public int getSize() {return -1;}

   @Override
   /** Close this iterator and release all allocated resources.*/
	abstract public void close();

   @Override
   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	abstract public boolean isClosed();

}