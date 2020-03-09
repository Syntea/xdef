package org.xdef;

import org.xdef.sys.SRuntimeException;

/** External service statements (database etc) in x-script.
 * @author Vaclav Trojan
 */
public interface XDStatement extends XDValue {

	/** Set constructor.
	 * @param constructor constructor for result of statement.
	 */
	public void setXDConstructor(XDConstructor constructor);

	/** Get constructor.
	 * @return constructor for result of statement.
	 */
	public XDConstructor getXDConstructor();

	/** Bind indexed value to statement.
	 * @param value value to be bound.
	 * @return true if value was bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	public boolean bind(XDValue value) throws SRuntimeException;

	/** Execute statement.
	 * @param params parameters of statement (sequence of values).
	 * @return result of execution or <tt>null</tt>.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDValue execute(XDValue params) throws SRuntimeException;

	/** Execute query.
	 * @param params parameters of statement or <tt>null</tt>.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet query(XDValue params) throws SRuntimeException;

	/** Execute query and return the specified items.
	 * @param itemName name of items to be returned.
	 * @param params parameters of statement or <tt>null</tt>.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet queryItems(String itemName, XDValue params)
		throws SRuntimeException;

   /** Close this statement (release all allocated resources).*/
	public void close();

   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	public boolean isClosed();

}