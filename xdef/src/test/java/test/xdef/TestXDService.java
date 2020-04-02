package test.xdef;

import test.XDTester;
import org.xdef.XDStatement;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.proc.XXNode;
import org.xdef.XDValue;
import org.xdef.XDService;
import org.xdef.XDConstructor;
import org.xdef.XDValueAbstract;
import org.xdef.XDFactory;
import org.xdef.XDResultSet;
import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUnsupportedOperationException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDValueID;
import org.xdef.XDValueType;

/** Test of external services.
 *
 * @author Vaclav Trojan
 */
public final class TestXDService extends XDTester {

	public TestXDService() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
			//Create object with database connection
			String url = "jdbc:derby://localhost:1527/sample;";
			String user = "app";
			String password = "app";
			MyService service = new MyService(url, user, password);
			//Generate XDPool
			String xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='query'>\n"+
"  <xd:declaration>\n"+
"    external final Service ser.vice;\n"+
"  </xd:declaration>\n"+
"  <Books>\n"+
"    <Book xd:script=\"occurs *; create ser.vice.query('//Book')\"\n"+
"     TITLE=\"string\"\n"+
"     ISBN=\"regex('\\\\d{8,10}')\"\n"+
"     EDITOR=\"optional string\"\n"+
"     ISSUED=\"optional int\">\n"+
"      <Author xd:script=\"occurs *;\n"+
"                 create ser.vice.queryItem('Book[@TITLE=?]/Author/text()',\n"+
"                           'AUTHOR', (String) xpath('../@TITLE'));\">\n"+
"        string;\n"+
"      </Author>\n"+
"    </Book>\n"+
"  </Books>\n"+
"</xd:def>";
			XDPool xp = XDFactory.compileXD(System.getProperties(), xdef);
			// Create XDDocument
			XDDocument xd = xp.createXDDocument();
			// Set external variable with database connection to XDDocument
			xd.setVariable("ser.vice", service); //set connection to XDefinition
			// Construct element with books
			Element el = xd.xcreate("Books", null); //execute construction
			assertEq(el, service._data);
			//close database connection
			service.close();
			// Print created element
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	class MyService extends XDValueAbstract implements XDService {

		private String _url;
		private String _user;
		Element _data;

		/** Create new ExtExistsService.
		 * @param query db query statement.
		 * @param params array of parameters or <tt>null</tt>.
		 */
		public MyService(String url, String user, String password)
			throws SRuntimeException {
			_url = url;
			_user = user;
			_data = KXmlUtils.parseXml(
"<Books>"+
	"<Book ISBN='234567819' ISSUED='1968' TITLE='2001: A Space Odyssey'>"+
		"<Author>Arthur C. Clarke</Author>"+
	"</Book>"+
	"<Book ISBN='9345678199' TITLE='Bible'/>"+
	"<Book ISBN='9345478191' TITLE='Koran'/>"+
	"<Book ISBN='230567819' ISSUED='1935' TITLE='Krakatit'>"+
		"<Author>Karel Čapek</Author>"+
	"</Book>"+
	"<Book EDITOR='XML Prague' ISBN='8345678191' ISSUED='2007'\n"+
		"TITLE='Proč svět nemluví esperantem'>"+
		"<Author>Jiří Kamenický</Author>"+
		"<Author>Jiří Měska</Author>"+
		"<Author>Václav Trojan</Author>"+
	"</Book>"+
	"<Book EDITOR='HarperCollins Publishers' ISBN='12345678' ISSUED='2008'\n"+
		"TITLE=\"The Last Theorem\">"+
		"<Author>Arthur C. Clarke</Author>"+
	"</Book>"+
"</Books>").getDocumentElement();
		}

		@Override
		public XDService serviceValue(){return this;}

		@Override
		public XDStatement prepareStatement(String statement) {
			return new MyStatement(statement, _data);
		}

		@Override
		public XDResultSet query(String statement, XDValue params)
			throws SRuntimeException {
			return prepareStatement(statement).query(params);
		}

		@Override
		public XDResultSet queryItems(String statement,
			String itemName,
			XDValue params) throws SRuntimeException {
			return prepareStatement(statement).queryItems(itemName, params);
		}

		@Override
		public XDValue execute(String statement, XDValue params)
			throws SRuntimeException {
			return prepareStatement(statement).execute(params);
		}

		@Override
		public void close() {_data = null;}

		@Override
		/** Check if the object is <tt>null</tt>.
		 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise return
		 * <tt>false</tt>.
		 */
		public boolean isNull() { return _data == null;}

	   @Override
	   /** Check if this object is closed.
		* @return true if and only if this object is closed.
		*/
		public boolean isClosed() {return _data == null;}

		@Override
		public void commit() throws SRuntimeException {
			throw new SUnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void rollback() throws SRuntimeException {
			throw new SUnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setProperty(String name, String value)
			throws SRuntimeException{
			//Database statement error&{msg}{: }
			throw new SRuntimeException(XDEF.XDEF568,
				"&{msg}Unknown property: " + name);
		}

		@Override
		public String getProperty(String name) {return null;}

		@Override
		public String getServiceName() {return "xmldb:exist";}

		@Override
		public short getItemId() {return XDValueID.XD_SERVICE;}

		@Override
		public XDValueType getItemType() {return XDValueType.SERVICE;}

		@Override
		public String stringValue() {return "eXist user:"+_user+"; url:"+_url;}

	}

	class MyStatement extends XDValueAbstract implements XDStatement {
		private String _source;
		private XDConstructor _constructor;
		private Element _rs;

		public MyStatement(String statement, Element rs) {
			_source = statement;
			_rs = rs;
		}

		@Override
		public XDStatement statementValue(){return this;}

		@Override
		public boolean bind(XDValue params) throws SRuntimeException {
			if (params == null) {
				return true;
			}
			throw new SUnsupportedOperationException("Not supported yet.");
		}

		@Override
		public XDValue execute(XDValue params) throws SRuntimeException {
			return query(params);
		}

		@Override
		public XDResultSet query(XDValue params) {
			try {
				String source = _source;
				if (params != null) {
					source = SUtils.modifyString(
						source, "?", '"' + params.toString() + '"');
				}
				return new MyResultSet(
					new MyIterator((NodeList)KXpathExpr.evaluate(_rs, source)));
			} catch (Exception ex) {
				//Database statement error&{msg}{: }
				throw new SRuntimeException(XDEF.XDEF568, "&{msg}" + ex);
			}
		}

		@Override
		public XDResultSet queryItems(String itemName, XDValue params)
			throws SRuntimeException {
			try {
				String source = _source;
				if (params != null) {
					source = SUtils.modifyString(
						source, "?", '"' + params.toString() + '"');
				}
				return new MyResultSet(
					new MyIterator((NodeList) KXpathExpr.evaluate(_rs, source)),
					itemName);
			} catch (Exception ex) {
				//Database statement error&{msg}{: }
				throw new SRuntimeException(XDEF.XDEF568, "&{msg}" + ex);
			}
		}

		@Override
		public void close() {_rs = null;}

		@Override
		/** Check if the object is <tt>null</tt>.
		 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
		 * <tt>false</tt>.
		 */
		public boolean isNull() { return _rs == null;}

	   @Override
	   /** Check if this object is closed.
		* @return true if and only if this object is closed.
		*/
		public boolean isClosed() {return _rs == null;}

		@Override
		public short getItemId() {return XDValueID.XD_STATEMENT;}

		@Override
		public XDValueType getItemType() {return XDValueType.STATEMENT;}

		@Override
		public String stringValue() {return _source;}

		@Override
		public void setXDConstructor(XDConstructor c) {_constructor = c;}

		@Override
		public XDConstructor getXDConstructor() {return _constructor;}

	}

	private static class MyResultSet extends XDValueAbstract
	implements XDResultSet {

		private MyIterator _ri;
		private int _count;
		private XDValue _item;
		private String _itemName;
		private XDConstructor _constructor;
		private final Document DOC = KXmlUtils.newDocument(null, "x", null);

		MyResultSet(MyIterator ri) {_ri = ri;}

		MyResultSet(MyIterator ri, String itemName) {
			_ri = ri;
			_itemName = itemName;
		}

		@Override
		public XDResultSet resultSetValue() {return this;}

		@Override
		public XDValue nextXDItem(XXNode xnode) throws SRuntimeException {
			try {
				if (_ri.hasNext()) {
					_count++;
					Object o = _ri.next();
					if (o instanceof Element) {
						Element e = (Element) o;
						if (_itemName != null) {
							_item = e.hasAttribute(_itemName) ?
								new DefString(e.getAttribute(_itemName)) :
								new DefString();
						} else {
							if (_constructor == null) {
								return _item = new DefElement(e);
							} else {
								XDValue v = _constructor.construct(this, xnode);
								return _item = v!=null ? v : new DefElement(e);
							}
						}
					} else if (o instanceof Node) {
						return _item = new DefString(((Node) o).getNodeValue());
					}
				}
				close();
				_ri = null;
				return null;
			} catch(Exception ex) {
				close();
				//Database statement error&{msg}{: }
				throw new SRuntimeException(XDEF.XDEF568, "&{msg}" + ex);
			}
		}

		@Override
		public XDValue lastXDItem() {return _item;}

		@Override
		public int getCount() {return _count;}

		@Override
		public String itemAsString() {
			try {
				if (_item.getItemId() == XDValueID.XD_ELEMENT) {
					return KXmlUtils.getTextValue(_item.getElement());
				}
			} catch (Exception ex) {}
			return null;
		}

		@Override
		public String itemAsString(int index) {return null;}


		@Override
		public String itemAsString(String name) {
			try {
				if (_item.getItemId() == XDValueID.XD_ELEMENT) {
					return _item.getElement().getAttribute(name);
				}
			} catch (Exception ex) {}
			return null;
		}

		@Override
		public boolean hasItem(String name) {return itemAsString(name) != null;}

		@Override
		public int getSize() {return -1;}

		@Override
		public void close() {
			_item = null;
		}

		@Override
		public void closeStatement() {
			_ri = null;
			_item = null;
		}

		@Override
		/** Check if the object is <tt>null</tt>.
		 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
		 * <tt>false</tt>.
		 */
		public boolean isNull() { return _ri == null;}

	   @Override
	   /** Check if this object is closed.
		* @return true if and only if this object is closed.
		*/
		public boolean isClosed() {return _ri == null;}

		@Override
		public short getItemId() {return XDValueID.XD_RESULTSET;}

		@Override
		public XDValueType getItemType() {return XDValueType.RESULTSET;}

		@Override
		public Element getElement() {
			if (_item != null) {
				if (_item.getItemId() == XDValueID.XD_ELEMENT) {
					return _item.getElement();
				}
				Element el = DOC.createElement("_");
				el.appendChild(DOC.createTextNode(_item.toString()));
				return el;
			}
			return null;
		}

		@Override
		/** Get statement from which ResultSet was created.
		 * @return null here.
		 */
		public XDStatement getStatement() {return null;}

		@Override
		/** Get constructor for creation of item.
		 * @return constructor for creation of item.
		 */
		public XDConstructor getXDConstructor() {return _constructor;}

		@Override
		public void setXDConstructor(XDConstructor c) {_constructor = c;}

	}

	class MyIterator implements java.util.Iterator {

		private final NodeList _nl;
		private int _ndx;

		MyIterator(NodeList nl) { _nl = nl;	_ndx = 0; }
		@Override
		public boolean hasNext() {return _ndx < _nl.getLength();}

		@Override
		public Object next() {return hasNext() ? _nl.item(_ndx++) : null;}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}