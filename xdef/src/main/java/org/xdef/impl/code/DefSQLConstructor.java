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

/** The class DefSQLConstructor implements the XDConstructor for database query.
 * @author Vaclav Trojan
 */
public class DefSQLConstructor implements XDConstructor {
    private ResultSet _rs;

    public DefSQLConstructor(final XDResultSet rs) {_rs = (ResultSet) rs.getObject();}

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
        MyElement(String nsUri, String name) {super(nsUri, name);}
        @Override
        public String getAttribute(String name)  {
            try {
                String s; //if value of a column is null then return the empty string!
                return (s=_rs.getString(name)) == null ? "" : s;
            } catch (SQLException ex) {
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
            } catch (SQLException ex) {
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
            return url != null && url.length() > 0 ? null : getAttributeNode(name);
        }

        @Override
        public boolean hasAttributeNS(String url, String name) {
            return url != null && url.length() > 0 ? false : hasAttribute(name);
        }

        /** Get array with named items in the table.
         * @return array with named items or null.
         */
        @Override
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
            } catch (SQLException ex) {
                return null;
            }
        }

        @Override
        public int getXDNamedItemsNumber() {
            try {
                return _rs.getMetaData().getColumnCount();
            } catch (SQLException ex) {
                return 0;
            }
        }

        @Override
        public String getXDNamedItemName(int index) {
            try {
                return _rs.getMetaData().getColumnName(index + 1);
            } catch (SQLException ex) {
                return null;
            }
        }

        /** Check if the object is empty.
         * @return true if the object is empty; otherwise return false.
         */
        @Override
        public boolean isEmpty() {
            try {
                return _rs == null || _rs.isAfterLast();
            } catch (SQLException ex) {
                String msg = ex.getMessage();
                if (msg == null || msg.isEmpty()) {
                    throw new SRuntimeException(XDEF.XDEF568, ex);//Database statement error&{0}{: }
                }
                throw new SRuntimeException(XDEF.XDEF568, msg);//Database statement error&{0}{: }
            }
        }
    }
}