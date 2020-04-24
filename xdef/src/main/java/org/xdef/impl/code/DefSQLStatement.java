package org.xdef.impl.code;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDConstructor;
import org.xdef.XDResultSet;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.xdef.XDValueType;

/** The class DefDBIterator implements the internal object with database query.
 * @author Vaclav Trojan
 */
public class DefSQLStatement extends XDValueAbstract implements XDStatement {

	private PreparedStatement _stmt;
	private final String _source;
	private XDConstructor _constructor;

	/** Creates a new instance of DefDBIterator. */
	public DefSQLStatement() {_source = null;}

	/** Creates a new instance of DefDBIterator with prepared statement.
	 * @param conn database connection.
	 * @param query string with query statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	DefSQLStatement(Connection conn, String query)
		throws SRuntimeException {
		_source = query;
		try {
			_stmt = conn.prepareStatement(query);
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
	public XDStatement statementValue(){return this;}
	@Override
	/** Set constructor.
	 * @param constructor constructor for result of statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void setXDConstructor(XDConstructor constructor) {
		_constructor = constructor;
	}
	@Override
	public XDConstructor getXDConstructor() {return _constructor;}
	@Override
	/** Bind indexed value to statement.
	 * @param value value to be bound.
	 * @return true if value was bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	public boolean bind(XDValue value) throws SRuntimeException {
		if (value == null) {
			return true;
		}
		try {
			switch (value.getItemId()) {
				case XD_CONTAINER: {
					DefContainer list = (DefContainer) value;
					for (int i = 0; i < list.getXDItemsNumber(); i++) {
						String s = list.getXDItem(i).stringValue();
						if (s != null && s.length() == 0 ) {//TODO
							s = null;
						}
						_stmt.setString(i + 1, s);
					}
					return true;
				}
				default: {
					String s = value.stringValue();
					if (s != null && s.length() == 0 ) {//TODO
						s = null;
					}
					_stmt.setString(1, s);
					return true;
				}
			}
		} catch (Exception ex) {}
		return false;
	}
   @Override
   /** Close this statement and release all allocated resources.*/
	public void close() {
		if (_stmt != null) {
			try {
				_stmt.close();
			} catch(Exception e) {}
			_stmt = null;
		}
	}
   @Override
   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	public boolean isClosed() {return _stmt == null;}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return _stmt == null;}
	@Override
	/** Execute statement.
	 * @param params parameters or <tt>null</tt>.
	 * @throws SRuntimeException id an error occurs.
	 */
	public final XDValue execute(final XDValue params)
		throws SRuntimeException {
		try {
			if (params != null) {
				bind(params);
			}
			DefBoolean result = new DefBoolean(_stmt.execute());
			return result;
		} catch(Exception ex) {
			String msg = ex.getMessage();
			//Database statement error&{0}{: }
			if (msg == null || msg.isEmpty()) {
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}
	@Override
	/** Invoke query statement with parameters.
	 * @param params parameters of statement (sequence of values).
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDResultSet query(XDValue params)
		throws SRuntimeException {
		return new DefSQLResultSet(this, params);
	}
	@Override
	/** Execute query and return the specified items.
	 * @param itemName name of items to be returned.
	 * @param params parameters of statement or <tt>null</tt>.
	 * @return XDResultSet with result of query on this statement.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final XDResultSet queryItems(String itemName, XDValue params)
		throws SRuntimeException{
		return new DefSQLResultSet(this, itemName, params);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _stmt;}
	@Override
	/** Get ID of the type of value (int, float, boolean, date, regex - see
	 * the interface org.xdef.XDValueTypes).
	 * @return item type.
	 */
	public short getItemId() {return XD_STATEMENT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.STATEMENT;}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return _source;}
}