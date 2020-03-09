package org.xdef;

import org.xdef.sys.SRuntimeException;

/** External service (JDBC, XQuery, database etc).
 * @author Vaclav Trojan
 */
public interface XDService extends XDValue {

	/** Prepare statement.
	 * @param statement source statement.
	 * @return prepared statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDStatement prepareStatement(String statement)
		throws SRuntimeException;

	/** Execute statement with parameters.
	 * @param statement source statement or <tt>null</tt>.
	 * @param params parameters of statement (sequence of values).
	 * @return result of execution.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDValue execute(String statement, XDValue params)
		throws SRuntimeException;

	/** Invoke query statement with parameters.
	 * @param statement source statement.
	 * @param params parameters of statement or <tt>null</tt>.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet query(String statement, XDValue params)
		throws SRuntimeException;

	/** Execute query and return the specified items.
	 * @param statement source statement.
	 * @param itemName name of items to be returned.
	 * @param params parameters of statement or <tt>null</tt>.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet queryItems(String statement,
		String itemName,
		XDValue params) throws SRuntimeException;

   /** Close this service all allocated resources.*/
	public void close();

//   /** Start transaction.*/
//	public void startTransaction();

	/** Commit.
	 * @throws Exception if an error occurs.
	 */
	public void commit() throws Exception;

	/** Rollback.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void rollback() throws SRuntimeException;

	/** Set property.
	 * @param name name of property to be set.
	 * @param value value of property to be set.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void setProperty(String name, String value) throws SRuntimeException;

	/** Get property.
	 * @param name name of property.
	 * @return value of property.
	 * @throws SRuntimeException if an error occurs.
	 */
	public String getProperty(String name) throws SRuntimeException;

	/** Get name of service.
	 * @return name of service.
	 */
	public String getServiceName();

   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	public boolean isClosed();

}