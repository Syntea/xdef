package org.xdef.impl.code;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDConstructor;
import org.xdef.XDElementAbstract;
import org.xdef.XDNamedValue;
import org.xdef.XDResultSet;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Attr;

/** The class DefSQLConstructor implements the XDConstructor for database
 * query.
 * @author Vaclav Trojan
 */
public class DefSQLConstructor implements XDConstructor {

	private ResultSet _rs;

	public DefSQLConstructor(final XDResultSet rs) {
		_rs = (ResultSet) rs.getObject();
	}

	@Override
	public XDValue construct(XDResultSet rs, XXNode xnode) {
		_rs = (ResultSet) rs.getObject();
		if (xnode == null) {
			return new MyElement();
		}
		String nsUri = xnode.getXMElement().getNSUri();
		String name = xnode.getXMElement().getName();
		if ("$any".equals(name)) {
			name = "_ANY_";
		}
		return new MyElement(nsUri, name);
	}

	public final class MyElement extends XDElementAbstract {

		MyElement() {super();}

		MyElement(String nsUri, String name) {
			super(nsUri, name);
		}
		@Override
		public String getAttribute(String name)  {
			try {
				//if value of a column is null then return the empty string!
				String s;
				return (s=_rs.getString(name)) == null ? "" : s;
			} catch (Exception ex) {
				return "";
			}
		}

		@Override
		public String getAttributeNS(String url, String name)  {
			return url != null && url.length() > 0 ? "" : getAttribute(name);
		}
		@Override
		public boolean hasAttribute(String name) {
			try {
				return _rs.getObject(name) != null;
			} catch (Exception ex) {
				return false;
			}
		}
		@Override
		public Attr getAttributeNode(String name) {
			String val = getAttribute(name);
			return val == null ? null : createAttr(name, val);
		}
		@Override
		public Attr getAttributeNodeNS(String url, String name) {
			return url != null && url.length() > 0 ?
				null : getAttributeNode(name);
		}
		@Override
		public boolean hasAttributeNS(String url, String name) {
			return url != null && url.length() > 0 ? false : hasAttribute(name);
		}
		@Override
		/** Get array with named items in the table.
		 * @return array with named items or null.
		 */
		public final XDNamedValue[] getXDNamedItems() {
			try {
				int n = _rs.getMetaData().getColumnCount();
				if (n == 0) {
					return null;
				}
				XDNamedValue[] result = new XDNamedValue[n];
				for (int i = 0; i < result.length; i++) {
					String s = _rs.getMetaData().getColumnName(i + 1);
					String v = _rs.getString(s);
					result[i] = new DefNamedValue(s, new DefString(v));
				}
				return result;
			} catch (Exception ex) {
				return null;
			}
		}
		@Override
		public int getXDNamedItemsNumber() {
			try {
				return _rs.getMetaData().getColumnCount();
			} catch (Exception ex) {
				return 0;
			}
		}
		@Override
		public String getXDNamedItemName(int index) {
			try {
				return _rs.getMetaData().getColumnName(index + 1);
			} catch (Exception ex) {
				return null;
			}
		}
		@Override
		/** Check if the object is empty.
		 * @return <tt>true</tt> if the object is empty; otherwise returns
		 * <tt>false</tt>.
		 */
		public boolean isEmpty() {
			try {
				return _rs == null || _rs.isAfterLast();
			} catch (SQLException ex) {
				String msg = ex.getMessage();
				//Database statement error&{0}{: }
				if (msg == null || msg.isEmpty()) {
					throw new SRuntimeException(XDEF.XDEF568, ex);
				}
				throw new SRuntimeException(XDEF.XDEF568, msg);
			}
		}
	}
}