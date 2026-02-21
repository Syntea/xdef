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
import java.sql.SQLException;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.SERVICE;

/** The class DefSQLService implements the internal object with JDBC database connection.
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
	public DefSQLService(String url, String user, String pasw) throws SRuntimeException {
		_url = url;
		_user = user;
		try {
			_conn = DriverManager.getConnection(url, user, pasw);
		} catch (SQLException ex) {
			String msg = ex.getMessage();
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex); //Database statement error&{0}{: }
			}
			throw new SRuntimeException(XDEF.XDEF568, msg); //Database statement error&{0}{: }
		}
	}

	/** Creates a new empty instance of DefSQLService.
	 * @param conn Database connection.
	 */
	public DefSQLService(Connection conn) {_conn = conn; _url = _user = null;}

	@Override
	public XDService serviceValue(){return this;}

   /** Compile and prepare command.
	 * @param statement source statement.
	 * @throws SRuntimeException if an error occurs.
	*/
	@Override
	public XDStatement prepareStatement(String statement) throws SRuntimeException {
		return new DefSQLStatement(_conn, statement);
	}

	/** Invoke query statement with parameters.
	 * @param statement source statement.
	 * @param params parameters of statement (sequence of values).
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public XDResultSet query(String statement, XDValue params) throws SRuntimeException {
		DefSQLStatement stmt = new DefSQLStatement(_conn, statement);
		DefSQLResultSet rs = (DefSQLResultSet) stmt.query(params);
		rs._closeStatement = true;
		return rs;
	}

	/** Execute query and return the specified items.
	 * @param statement source statement.
	 * @param params parameters of statement.
	 * @param itemName name of items to be returned.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public XDResultSet queryItems(String statement, String itemName, XDValue params) throws SRuntimeException{
		DefSQLStatement stmt = new DefSQLStatement(_conn, statement);
		DefSQLResultSet rs = (DefSQLResultSet) stmt.queryItems(itemName,params);
		rs._closeStatement = true;
		return rs;
	}

	/** Execute statement with parameters.
	 * @param statement source statement.
	 * @param params parameters of statement or null.
	 * @return result of execution.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public XDValue execute(String statement, XDValue params) throws SRuntimeException {
		return new DefSQLStatement(_conn, statement).execute(params);
	}

	/** Close this connection and release all allocated resources. */
	@Override
	public void close() {
		if (_conn != null) {
			try {
				_conn.close();
			} catch(SQLException e) {}
			_conn = null;
		}
	}

	/** Check if the object is null.
	 * @return true if the object is null otherwise return false.
	 */
	@Override
	public boolean isNull() { return _conn == null;}

   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	@Override
	public boolean isClosed() {return _conn == null;}

	/** Commit.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public void commit() throws SRuntimeException {
		try {
			_conn.commit();
		} catch (SQLException ex) {
			String msg = ex.getMessage();
			//Database statement error&{0}{: }
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}

	/** Rollback.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public void rollback() throws SRuntimeException {
		try {
			if (_conn != null) {
				_conn.rollback();
			}
		} catch (SQLException ex) {
			String msg = ex.getMessage();
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex); //Database statement error&{0}{: }
			}
			throw new SRuntimeException(XDEF.XDEF568, msg); //Database statement error&{0}{: }
		}
	}

	/** Set property.
	 * @param name name of property to be set.
	 * @param value value of property to be set.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public void setProperty(String name, String value) throws SRuntimeException{
		try {
			if (_conn != null && "autocommit".equals(name)) {
				_conn.setAutoCommit("yes".equals(value));
			}
		} catch (SQLException ex) {
			String msg = ex.getMessage();
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex); //Database statement error&{0}{: }
			}
			throw new SRuntimeException(XDEF.XDEF568, msg); //Database statement error&{0}{: }
		}
	}

	/** Get property.
	 * @param name name of property.
	 * @return value of property or null.
	 */
	@Override
	public String getProperty(String name) {
		try {
			if (_conn != null && "autocommit".equals(name)) {
				return _conn.getAutoCommit() ? "yes" : "no";
			}
		} catch (SQLException ex) {}
		return null;
	}

	@Override
	public String getServiceName() {return "JDBC";}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get associated object.
	 * @return the associated object or null.
	 */
	@Override
	public Object getObject() {return _conn;}

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public short getItemId() {return XD_SERVICE;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return SERVICE;}

	/** Get value as String.
	 * @return The string from value.
	 */
	@Override
	public String toString() {return stringValue();}

	/** Get string value of this object.
	 * @return string value of this object.
	 */
	@Override
	public String stringValue() {return _url == null ? "" + _conn : ("user: " + _user + "; url: " + _url);}
}