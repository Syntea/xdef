package org.xdef.impl.code;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDConstructor;
import org.xdef.XDResultSet;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.proc.XXNode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDValueType;

/** The class DefDBIterator implements the internal object with database query.
 * @author Vaclav Trojan
 */
public class DefSQLResultSet extends XDValueAbstract implements XDResultSet {

	private ResultSet _resultSet;
	private DefSQLStatement stmt;
	protected int _count;
	protected String _itemName;
	protected XDValue _item;
	protected XDConstructor _constructor;
	protected boolean _closeStatement = false;

	private static final Document DOC = KXmlUtils.newDocument(null, "x", null);

	/** Creates a new empty instance of DefDBIterator. */
	public DefSQLResultSet() {}

	/** Creates a new instance of DefDBIterator.
	 * @param resultSet created result set.
	 */
	public DefSQLResultSet(ResultSet resultSet) {
		_resultSet = resultSet;
		stmt = null;
		_constructor = new DefSQLConstructor(this);
	}

	/** Creates a new instance of DefDBIterator.
	 * @param itemName name of required column.
	 * @param resultSet created result set.
	 */
	public DefSQLResultSet(String itemName, ResultSet resultSet) {
		this(resultSet);
		_itemName = itemName;
	}

	/** Create new DefDBIterator.
	 * @param query db query statement.
	 * @param params parameters of statement (sequence of values).
	 * @throws SRuntimeException id an error occurs.
	 */
	DefSQLResultSet(DefSQLStatement query, XDValue params)
		throws SRuntimeException {
		_constructor = new DefSQLConstructor(this);
		execute(query, params);
	}

	/** Create new DefDBIterator.
	 * @param query db query statement.
	 * @param params parameters of statement (sequence of values).
	 * @param itemName name of item from which the iterator will be constructed.
	 * @throws SRuntimeException id an error occurs.
	 */
	DefSQLResultSet(DefSQLStatement query,
		String itemName,
		XDValue params) throws SRuntimeException {
		_itemName = itemName;
		execute(query, params);
	}

	@Override
	public XDResultSet resultSetValue() {return this;}

	/** Execute query.
	 * @param query db query statement.
	 * @param params parameters of statement (sequence of values).
	 * @throws SRuntimeException id an error occurs.
	 */
	private void execute(final DefSQLStatement query, final XDValue params)
		throws SRuntimeException {
		try {
			if (_resultSet != null) {
				_resultSet.close();
			}
			if (params != null) {
				query.bind(params);
			}
			_resultSet = ((PreparedStatement) query.getObject()).executeQuery();
		} catch(Exception ex) {
			close();
			String msg = ex.getMessage();
			if (msg == null || msg.isEmpty()) {
				//Database statement error&{0}{: }
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Methods from XDResultSet.
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Close this iterator and release all allocated resources. */
	public void close() {
		_item = null;
		_itemName = null;
		if (_resultSet != null) {
			try {
				_resultSet.close();
			} catch(SQLException e) {}
			_resultSet = null;
		}
		_count = 0;
		if (_closeStatement && stmt != null && !stmt.isClosed()) {
			stmt.close();
		}
	}
	@Override
	/** Closes both this iterator and the underlying Statement from which
	 * this ResultSet was created. */
	public void closeStatement() {
		close();
		if (stmt != null && !stmt.isClosed()) {
			stmt.close();
		}
	}
   @Override
   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	public boolean isClosed() {return _resultSet == null;}
	@Override
	/** Get next item of this iterator or <tt>null</tt>. If the object
	 * has to be closed then if no more values are available the close() method
	 * must be invoked.
	 * @param xnode XXnode from which this method was called.
	 * @return the next value of this iterator or return <tt>null</tt>.
	 * @throws SRuntimeException id an error occurs.
	 */
	public XDValue nextXDItem(XXNode xnode) throws SRuntimeException {
		if (_resultSet == null) {
			return null;
		}
		try {
			if (_resultSet.next()) {
				_count++;
				if (_itemName != null) {
					_item = new DefString(_resultSet.getString(_itemName));
				} else {
					XDValue v = getXDConstructor().construct(this, xnode);
					if (v != null) {
						_item = v;
					}
				}
				return _item;
			}
			close();
			return null;
		} catch(Exception ex) {
			close();
			String msg = ex.getMessage();
			if (msg == null || msg.isEmpty()) {
				//Database statement error&{0}{: }
				throw new SRuntimeException(XDEF.XDEF568, ex);
			}
			throw new SRuntimeException(XDEF.XDEF568, msg);
		}
	}
	@Override
	/** Get the item returned by last nextItem method or return <tt>null</tt>.
	 * @return item returned by last nextItem method or return <tt>null</tt>.
	 */
	public XDValue lastXDItem() {return _item;}
	@Override
	/** Get count of iteration.
	 * @return count of iteration.
	 */
	public int getCount() {return _count;}
	@Override
	/** Return value of iterated object if it has value (text of element),
	 * otherwise return <tt>null</tt>.
	 * @return value of iterated object or return <tt>null</tt>.
	 */
	public String itemAsString() {
		return _itemName == null ? null : _item.stringValue();
	}
	@Override
	/** If the iterated object is an array then return relevant item value,
	 * otherwise return <tt>null</tt>.
	 * @param index the index of item.
	 * @return value of the array item as a string or return <tt>null</tt>.
	 */
	public String itemAsString(int index) {
		try {
			return _resultSet.getString(index);
		} catch (Exception ex) {}
		return null;
	}
	@Override
	/** If the iterated object is a map, then return relevant item value,
	 * otherwise return <tt>null</tt>.
	 * @param name the name of map item.
	 * @return value of map item as a string or return <tt>null</tt>.
	 */
	public String itemAsString(String name) {
		try {
			return _resultSet.getString(name);
		} catch (Exception ex) {}
		return null;
	}
	@Override
	/** If the iterated object contains the specified item then return
	 * <tt>true</tt>.
	 * @param name name item.
	 * @return <tt>true</tt> if and only if the specified item exists.
	 */
	public boolean hasItem(String name) {return itemAsString(name) != null;}
	@Override
	/** If this iterator is created from an array then return the size of array,
	 * otherwise otherwise return -1.
	 * @return size of array or -1.
	 */
	public int getSize()  {
		try {
			return _resultSet.getFetchSize();
		} catch (Exception ex) {}
		return -1;
	}
	@Override
	/** Get statement from which ResultSet was created.
	 * @return statement from which ResultSet was created.
	 */
	public XDStatement getStatement() {return stmt;}
	@Override
	/** Set constructor for creation of item.
	 * @param constructor constructor for creation of item.
	 */
	public void setXDConstructor(XDConstructor constructor){
		_constructor = constructor;
	}
	@Override
	/** Get constructor for creation of item.
	 * @return constructor for creation of item.
	 */
	public XDConstructor getXDConstructor() {return _constructor;}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return _resultSet == null;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {
		try {
			if (_resultSet != null &&
				_resultSet.getMetaData().getColumnCount() > 0) {
				return _resultSet.getString(1);
			}
		} catch (Exception ex) {}
		return null;
	}
	@Override
	public Element getElement() {
		if (_item != null) {
			if (_item.getItemId() == XD_ELEMENT) {
				return _item.getElement();
			}
			Element el = DOC.createElement("_");
			el.appendChild(DOC.createTextNode(_item.toString()));
			return el;
		}
		return null;
	}
	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _resultSet;}
	@Override
	public short getItemId() {return XD_RESULTSET;}
	@Override
	public XDValueType getItemType() {return XDValueType.RESULTSET;}

}