package org.xdef.impl.code;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.sql.Connection;
import java.sql.DriverManager;
import org.xdef.XDValueType;

/** The class DefSQLService implements the internal object with JDBC database
 * connection.
 * @author Vaclav Trojan
 */
public class DefSQLService extends XDValueAbstract implements XDService {

	private final String _url;
	private final String _user;
	private Connection _conn;

	/** Creates a new empty instance of DefSQLService. */
	public DefSQLService() {_url = null; _user = null;}

	/** Creates a new instance of DefSQLService.
	 * @param url string with connection URL.
	 * @param user user name.
	 * @param pasw user password.
	 * @throws SRuntimeException if an error occurs.
	 */
	public DefSQLService(String url, String user, String pasw)
		throws SRuntimeException {
		_url = url;
		_user = user;
		try {
			_conn = DriverManager.getConnection(url, user, pasw);
		} catch (Exception ex) {
			String msg = ex.getMessage();
			//Database statement error&{0}{: }
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}

	/** Creates a new empty instance of DefSQLService.
	 * @param conn Database connection.
	 */
	public DefSQLService(Connection conn) {_conn = conn; _url = _user = null;}
	@Override
	public XDService serviceValue(){return this;}
   @Override
   /** Compile and prepare command.
	* @param statement source statement.
	 * @throws SRuntimeException if an error occurs.
	*/
	public XDStatement prepareStatement(String statement)
		throws SRuntimeException {
		return new DefSQLStatement(_conn, statement);
	}
	@Override
	/** Invoke query statement with parameters.
	 * @param statement source statement.
	 * @param params parameters of statement (sequence of values).
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet query(String statement, XDValue params)
		throws SRuntimeException{
		DefSQLStatement stmt = new DefSQLStatement(_conn, statement);
		DefSQLResultSet rs = (DefSQLResultSet) stmt.query(params);
		rs._closeStatement = true;
		return rs;
	}
	@Override
	/** Execute query and return the specified items.
	 * @param statement source statement.
	 * @param params parameters of statement.
	 * @param itemName name of items to be returned.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet queryItems(String statement,
		String itemName,
		XDValue params) throws SRuntimeException {
		DefSQLStatement stmt = new DefSQLStatement(_conn, statement);
		DefSQLResultSet rs = (DefSQLResultSet) stmt.queryItems(itemName,params);
		rs._closeStatement = true;
		return rs;
	}
	@Override
	/** Execute statement with parameters.
	 * @param statement source statement.
	 * @param params parameters of statement or <tt>null</tt>.
	 * @return result of execution.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDValue execute(String statement, XDValue params)
		throws SRuntimeException {
		return new DefSQLStatement(_conn, statement).execute(params);
	}
	@Override
	/** Close this connection and release all allocated resources. */
	public void close() {
		if (_conn != null) {
			try {
				_conn.close();
			} catch(Exception e) {}
			_conn = null;
		}
	}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return _conn == null;}
   @Override
   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	public boolean isClosed() {return _conn == null;}

	@Override
	/** Commit.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void commit() throws SRuntimeException {
		try {
			_conn.commit();
		} catch (Exception ex) {
			String msg = ex.getMessage();
			//Database statement error&{0}{: }
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}
	@Override
	/** Rollback.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void rollback() throws SRuntimeException {
		try {
			if (_conn != null) {
				_conn.rollback();
			}
		} catch (Exception ex) {
			String msg = ex.getMessage();
			//Database statement error&{0}{: }
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}
	@Override
	/** Set property.
	 * @param name name of property to be set.
	 * @param value value of property to be set.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void setProperty(String name, String value) throws SRuntimeException{
		try {
			if (_conn != null && "autocommit".equals(name)) {
				_conn.setAutoCommit("yes".equals(value));
			}
		} catch (Exception ex) {
			String msg = ex.getMessage();
			//Database statement error&{0}{: }
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}
	@Override
	/** Get property.
	 * @param name name of property.
	 * @return value of property or <tt>null</tt>.
	 */
	public String getProperty(String name) {
		try {
			if (_conn != null && "autocommit".equals(name)) {
				return _conn.getAutoCommit() ? "yes" : "no";
			}
		} catch (Exception ex) {}
		return null;
	}
	@Override
	public String getServiceName() {return "JDBC";}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _conn;}
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_SERVICE;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.SERVICE;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return stringValue();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {
		if (_url == null) {
			return "" + _conn;
		}
		return "user: " + _user + "; url: " + _url;
	}
}