package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Test of X-Definition relational database processing.
 * @author Vaclav Trojan
 */
public final class TestDatabase extends XDTester {

	public TestDatabase() {super();}

	private static final String TABLE_A = "ta";
	private static final String TABLE_B = "tb";
	private static final String ATTR_A = "a";
	private static final String ATTR_B = "b";
	private static final String ATTR_C = "c";
	private static final String ATTR_A_INT = ATTR_A + " int";
	private static final String ATTR_B_VARCHAR = ATTR_B + " varchar(256)";
	private static final String ATTR_C_VARCHAR = ATTR_C + " varchar(256)";
	/** Number of rows in the table A. */
	private static final int ROWS_A = 10;
	/** Number of rows in the table B. */
	private static final int ROWS_B = 10;
	/** Number of total ResultSet made during test is given as ROWS_B multiple
	 * by RESULT_SET_NUM.
	 */
	private static final int RESULT_SET_NUM = 10;
	/** Directory name for the test database. */
	private static final String DERBY_STORE = "derby-store";
	/** User DB login for test purpose. */
	private static final String TEST_USER = "usr123";
	/** User DB password for test purpose. */
	private static final String TEST_PWD = "--*..-*";
	/** Test DB schema. */
	private static final String SCHEMA = "create";

	/** The database engine. */
	private EmbeddedDataSource _ds = null;
	/** Shared connection into the test relational database Derby. */
	private Connection _con = null;

	/** Creates database environment for database testing. */
	private void createDBEnv(final String tempDir) {
		// configure and start database engine
		_ds = new EmbeddedDataSource();
		// path to store data for database schema "test"
		_ds.setDatabaseName(tempDir + File.separatorChar + DERBY_STORE +
				File.separatorChar + "test");
		// for security reason restrict access to the test database for
		// the specific user identified by the password
		_ds.setUser(TEST_USER);
		_ds.setPassword(TEST_PWD);
		// create a new database (or use the existing one)
		_ds.setCreateDatabase(SCHEMA);
		// retreive connection to the Derby database
		try {
			_con = _ds.getConnection();
		} catch (SQLException ex) {
			fail(ex);
		}
		// prepare test data
		fillDB();
	}

	/** Creates a new connection to the test database.
	 * @return new connection to the database.
	 */
	private Connection getConnection() {
		try {
			// if the shared connection is opend return it
			if(_con != null && !_con.isClosed()) {
				return _con;
			}
		} catch (SQLException ex) {
			fail(ex);
		}
		if (_ds != null) {
			// create a new connection
			try {
				return _ds.getConnection();
			} catch (SQLException ex) {
				fail(ex);
			}
		} else {
			fail("EmbeddedDataSource is null.");
		}
		return null;
	}

	/** Clean the environment (connections and data stores) created and used for
	 * database testing.
	 * @param tempDir temp directory used by test mechanism of X-Definition.
	 */
	private void cleanDBEnv(final String tempDir) {
		// close resources and shutdown the DB
		closeDB();
		// remove directory used for database data
		deleteStore(new File(tempDir + File.separatorChar + DERBY_STORE));
	}

	/** Closes database resources and makes database shutdown. */
	private void closeDB() {
		// close shared connection
		if(_con != null) {
			try {
				if(!_con.isClosed()) {
					_con.close();
				}
			} catch (SQLException ex) {
				fail(ex);
			}
		}
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true", null,null);
			fail("Derby database cannot be halted.");
		} catch (SQLException ex) {
			// accept exception: database Derby is shutdown
		}
	}

	/** Recursively delete all files and directories created in the database
	 * store during the database testing.
	 * @param f file or directory has to be deleted.
	 */
	private void deleteStore(final File f) {
		if(f.isDirectory()) {
			for(File file : f.listFiles()) {
				deleteStore(file);
			}
		}
		if(!f.delete()) {
			fail("File or directory '"
				+ f.getAbsolutePath() + "' cannot be deleted.");
		}
	}

	/** Fill in the test database by the test data. */
	private void fillDB() {
		Statement st = null;
		try {
			st = _con.createStatement();
			// table A
			String sql = "create table " + TABLE_A + "(" + ATTR_A_INT +
					"," + ATTR_B_VARCHAR + "," + ATTR_C_VARCHAR + ")";
			st.executeUpdate(sql);
			for(int i=1; i <= ROWS_A; i++) {
				sql = "insert into " + TABLE_A + " (" + ATTR_A + ","
						+ ATTR_B + "," + ATTR_C + ") values(" +
						i + ",'" + (ATTR_B+i) + "','" + (ATTR_C+i) + "')";
				st.executeUpdate(sql);
			}
			// table B (big table)
			sql = "create table " + TABLE_B + "(" + ATTR_A_INT +
					"," + ATTR_B_VARCHAR + ")";
			st.executeUpdate(sql);
			for(int i=1; i <= ROWS_B; i++) {
				sql = "insert into " + TABLE_B + " (" + ATTR_A + ","
						+ ATTR_B + ") values(" + i + ",'" + (ATTR_B+i)  + "')";
				st.executeUpdate(sql);
			}
		} catch (SQLException ex) {
			fail(ex);
		} finally {
			try {
				st.close();
			} catch (SQLException ex) {
				fail(ex);
			}
		}
	}

	@Override
	public void test() {
		XDPool xp;
		XDDocument xd;
		String xml;
		String xdef;
		ArrayReporter reporter = new ArrayReporter();
		String tempDir = getTempDir();
		Element el;
		NodeList nl;
		String s;

		// creates DB environment
		createDBEnv(tempDir);

		////////////////////////////////////////////////////////////////////////
		// Primary tests: test DB environment
		////////////////////////////////////////////////////////////////////////
		Statement st = null;
		ResultSet rs = null;
		try {
			st = _con.createStatement();
			st.executeUpdate("create table abc(id int)");
			st.executeUpdate("insert into abc (id) values(1)");
			st.executeUpdate("insert into abc (id) values(2)");
			rs = st.executeQuery("select * from abc");
			rs.next();
			assertEq(1, rs.getInt(1));
			rs.next();
			assertEq(2, rs.getInt(1));
			rs.next();
			rs.getInt(1);
			fail("In the resultset was no data and no SQLException was "
					+ "generated.");
		} catch (Exception ex) {
			// expected
		} finally {
			try {
				rs.close();
				st.executeUpdate("drop table abc");
				st.close();
				_con.close();
			} catch (SQLException ex) {
				fail(ex);
			}
		}

		////////////////////////////////////////////////////////////////////////
		// Tests in the X-Definitions
		////////////////////////////////////////////////////////////////////////

		// Use non-external connection
		try {
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"  Service service;\n"+
"  ResultSet data = service.query('SELECT * FROM " + TABLE_A + "');\n"+
"</xd:declaration>\n"+
"\n"+
"<A xd:script= \"create 1\">\n"+
"    <" + TABLE_A + " xd:script= \"occurs *; create data\"\n"+
"         a = \"int; create data.getItem('" + ATTR_A + "')\"\n"+
"         b = \"string; create data.getItem('" + ATTR_B + "')\">\n"+
"      <P xd:script= \"create data.getItem('" + ATTR_C + "') != null\"> \n"+
"        string; create data.getItem('" + ATTR_C + "');\n"+
"      </P> \n"+
"    </" + TABLE_A + ">\n"+
"  </A>\n"+
"\n"+
"</xd:def>";
			_con = getConnection();
			el = create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			if(reporter.errorWarnings()) {
				fail(reporter.getReport());
			}
			// check the data equality
			nl = el.getChildNodes();
			assertEq(nl.getLength(), ROWS_A);
			for(int i=1; i<=nl.getLength(); i++) {
				Element ele = (Element)nl.item(i-1);
				assertEq(Integer.parseInt(ele.getAttribute("a")), i);
				assertEq(ATTR_B+i, ele.getAttribute("b"));
				assertEq(ATTR_C+i,
					((Element)ele.getChildNodes().item(0)).getTextContent());
			}

			// check implicit closing of the Service
			if(!_con.isClosed()) {
				fail("X-Definition hasn't closed the Service object.");
			} else {
				_con.close();
			}
		// Use external connection
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"  external Service service;\n"+
"  ResultSet data = service.query('SELECT * FROM " + TABLE_A + "');\n"+
"</xd:declaration>\n"+
"\n"+
"<A xd:script= \"create 1\">\n"+
"    <" + TABLE_A + " xd:script= \"occurs *; create data\"\n"+
"         a = \"int; create data.getItem('" + ATTR_A + "')\"\n"+
"         b = \"string; create data.getItem('" + ATTR_B + "')\">\n"+
"      <P xd:script= \"create data.getItem('" + ATTR_C + "') != null\"> \n"+
"        string; create data.getItem('" + ATTR_C + "');\n"+
"      </P> \n"+
"    </" + TABLE_A + ">\n"+
"  </A>\n"+
"\n"+
"</xd:def>";
			_con = getConnection();
			el = create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			if(reporter.errorWarnings()) {
				fail(reporter.getReport());
			}
			// check the data equality
			nl = el.getChildNodes();
			assertEq(nl.getLength(), ROWS_A);
			for(int i=1; i<=nl.getLength(); i++) {
				Element ele = (Element)nl.item(i-1);
				assertEq(Integer.parseInt(ele.getAttribute("a")), i);
				assertEq(ATTR_B+i, ele.getAttribute("b"));
				assertEq(ATTR_C+i,
					((Element)ele.getChildNodes().item(0)).getTextContent());
			}

			// check that external Service wasn't closed
			if(_con.isClosed()) {
				fail("X-Definition has closed the Service object.");
			} else {
				_con.close();
			}
			// using ResultSet created implicitly
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"  external Service service;\n"+
"</xd:declaration>\n"+
"\n"+
"<A xd:script= \"create 1\">\n"+
"    <" + TABLE_A + " xd:script= \"occurs *; create service.query('SELECT * "
					+ "FROM " + TABLE_A + "')\"\n"+
"         a = \"int\"\n"+
"         b = \"string\">\n"+
"    </" + TABLE_A + ">\n"+
"  </A>\n"+
"\n"+
"</xd:def>";
			_con = getConnection();
			el = create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			if(reporter.errorWarnings()) {
				fail(reporter.getReport());
			}
			// check the data equality
			nl = el.getChildNodes();
			assertEq(nl.getLength(), ROWS_A);
			for(int i=1; i<=nl.getLength(); i++) {
				Element ele = (Element)nl.item(i-1);
				assertEq(Integer.parseInt(ele.getAttribute("a")), i);
				assertEq(ele.getAttribute("b"), ATTR_B+i);
			}
			// check that external Service wasn't closed
			if(_con.isClosed()) {
				fail("X-Definition has closed the Service object.");
			} else {
				_con.close();
			}
		// using macro
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"  external Service service;\n"+
"  ResultSet rs;"+
"</xd:declaration>\n"+
"\n"+
"<A xd:script= \"create 1\">\n"+
"  <" + TABLE_A + " xd:script= \"occurs *; create {\n"+
"    rs=${m_si};\n"+
"    return rs}\"\n"+
"    a = \"int\"\n"+
"    b = \"string\">\n"+
"    </" + TABLE_A + ">\n"+
"</A>\n"+
"<xd:macro name=\"m_si\">\n"+
"    service.query('SELECT * FROM " + TABLE_A + "')\n"+
"</xd:macro>\n"+
"</xd:def>";
			_con = getConnection();
			el = create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			if(reporter.errorWarnings()) {
				fail(reporter.getReport());
			}
			// check the data equality
			nl = el.getChildNodes();
			assertEq(nl.getLength(), ROWS_A);
			for(int i=1; i<=nl.getLength(); i++) {
				Element ele = (Element)nl.item(i-1);
				assertEq(Integer.parseInt(ele.getAttribute("a")), i);
				assertEq(ele.getAttribute("b"), ATTR_B+i);
			}

			// check that external Service wasn't closed
			if(_con.isClosed()) {
				fail("X-Definition has closed the Service object.");
			} else {
				_con.close();
			}
// test "boolean ResultSet.next()" and "boolean ResultSet.hasNext()" method
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"  external Service service;\n"+
"  ResultSet rs = service.query('SELECT * FROM " + TABLE_A + "');\n"+
"  Container ct = [];\n"+
"  \n"+
"  void doQ() {\n"+
"    Container c;\n"+
"    while(rs.hasNext()) {\n"+
"      c = [%a=rs.getItem('" + ATTR_A + "'),\n"+
"        %b=rs.getItem('" + ATTR_B + "')];\n"+
"      ct.addItem(c);\n"+
"      rs.next();\n"+
"    }\n"+
"  }\n"+
"</xd:declaration>\n"+
"\n"+
"<A xd:script= \"onStartElement doQ(); create 1\">\n"+
"    <" + TABLE_A + " xd:script= \"occurs *; create ct\"\n"+
"         a = \"int\"\n"+
"         b = \"string\">\n"+
"    </" + TABLE_A + ">\n"+
"  </A>\n"+
"</xd:def>";
			_con = getConnection();
			el = create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			if(reporter.errorWarnings()) {
				fail(reporter.getReport());
			}
			// check the data equality
			nl = el.getChildNodes();
			assertEq(nl.getLength(), ROWS_A);
			for(int i=1; i<=nl.getLength(); i++) {
				Element ele = (Element)nl.item(i-1);
				assertEq(Integer.parseInt(ele.getAttribute("a")), i);
				assertEq(ele.getAttribute("b"), ATTR_B+i);
			}

			// check that external Service wasn't closed
			if(_con.isClosed()) {
				fail("X-Definition has closed the Service object.");
			} else {
				_con.close();
			}
			XDService service = XDFactory.createSQLService(getConnection());
			//create database
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='schema'>\n"+
"  <xd:declaration scope='local'>\n"+
"    external Service con;\n"+
"  </xd:declaration>\n"+
"  <schema name=\"string; onTrue con.execute('CREATE SCHEMA '+ getText())\">\n"+
"    <table xd:script=\"occurs +\" name = \"string\">\n"+
"      string;\n"+
"      onTrue try {\n"+
"        con.execute('CREATE TABLE ' + xpath('../@name') +\n"+
"          '.' + @name + ' (' + getText() + ')');\n"+
"      } catch (Exception ex) {\n"+
"        error(toString(ex));\n"+
"	  }\n"+
"    </table>\n"+
"  </schema>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("#con", service);
			xml =
"<schema name=\"MYTEST\">\n"+
"  <table name=\"AUTHOR\">\n"+
"    IDAUTHOR INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,\n"+
"    AUTHOR VARCHAR(100) NOT NULL,\n"+
"    PRIMARY KEY(IDAUTHOR),\n"+
"    UNIQUE (AUTHOR)\n"+
"  </table>\n"+
"  <table name=\"TITLE\">\n"+
"    IDTITLE INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,\n"+
"    TITLE VARCHAR(100) NOT NULL,\n"+
"    ISSUED INTEGER,\n"+
"    EDITOR VARCHAR(100),\n"+
"    ISBN VARCHAR(10) NOT NULL,\n"+
"    PRIMARY KEY(IDTITLE),\n"+
"    UNIQUE (TITLE, ISBN)\n"+
"  </table>\n"+
"  <table name=\"TITLE_AUTHOR\">\n"+
"    IDTITLE_AUTHOR INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,\n"+
"    IDAUTHOR INTEGER NOT NULL,\n"+
"    IDTITLE INTEGER NOT NULL,\n"+
"    PRIMARY KEY(IDTITLE_AUTHOR),\n"+
"    UNIQUE (IDAUTHOR, IDTITLE),\n"+
"    FOREIGN KEY (IDAUTHOR) REFERENCES MYTEST.AUTHOR(IDAUTHOR),\n"+
"    FOREIGN KEY (IDTITLE) REFERENCES MYTEST.TITLE(IDTITLE)\n"+
"  </table>\n"+
"</schema>";
			xd.xparse(xml, null);
			//insert 1
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"Books|Book\">\n"+
"\n"+
"  <xd:declaration scope='local'>\n"+
"    external Service service;\n"+
"	int inserted = 0;\n"+
"	boolean ignored = false;\n"+
"    Statement isAuthor = service.prepareStatement(\n"+
"      \"SELECT AUTHOR FROM MYTEST.AUTHOR WHERE MYTEST.AUTHOR.AUTHOR = ?\");\n"+
"    Statement isTitle = service.prepareStatement(\n"+
"      \"SELECT TITLE FROM MYTEST.TITLE WHERE MYTEST.TITLE.TITLE = ?\");\n"+
"    Statement insertAuthor = service.prepareStatement(\n"+
"      \"INSERT INTO MYTEST.AUTHOR(AUTHOR) VALUES (?)\");\n"+
"    Statement insertTitle = service.prepareStatement(\n"+
"  \"INSERT INTO MYTEST.TITLE(TITLE,EDITOR,ISBN,ISSUED) VALUES (?,?,?,?)\");\n"+
"    Statement insertTitleAuthor = service.prepareStatement(\n"+
"      \"INSERT INTO MYTEST.TITLE_AUTHOR(IDAUTHOR,IDTITLE)\n"+
"         VALUES ((SELECT IDAUTHOR FROM MYTEST.AUTHOR WHERE AUTHOR=?),\n"+
"         (SELECT IDTITLE FROM MYTEST.TITLE WHERE TITLE=?))\");\n"+
"   </xd:declaration>\n"+
"  <Books>\n"+
"    <Book xd:script=\"occurs *; ref item\"/>\n"+
"  </Books>\n"+
"  <Book xd:script=\"ref item\"/>\n"+
"  <item xd:script=\"onStartElement {\n"+
"            String s = @TITLE.toString ( );\n"+
"            if (ignored = isTitle.hasItem(s)) {\n"+
"               error('Book \\'' + @TITLE + '\\' already exists');\n"+
"            } else {\n"+
"               insertTitle.execute((String) @TITLE,\n"+
"				  toString (@EDITOR),\n"+
"                  @ISBN . toString(),\n"+
"                  (String)(@ISSUED));\n"+
"               inserted++;\n"+
"            }\n"+
"        };\"\n"+
"     TITLE=\"string\"\n"+
"     ISBN=\"regex('\\\\d{8,10}')\"\n"+
"     EDITOR=\"optional string\"\n"+
"     ISSUED=\"optional int\">\n"+
"     <Author xd:script=\"occurs *\">\n"+
"        optional string;\n"+
"        finally if (!ignored) {\n"+
"          String s = getText();\n"+
"          if (!isAuthor.hasItem(s)) {\n"+
"            insertAuthor.execute(s); /*new author*/\n"+
"          }\n"+
"          insertTitleAuthor.execute(s, xpath(\"../@TITLE\").toString());\n"+
"        }\n"+
"     </Author>\n"+
"  </item>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("#service", service); //set connection
			xml =
"<Books>\n"+
"  <Book ISSUED='2008' ISBN='12345678'\n"+
"    EDITOR='HarperCollins Publishers'\n"+
"    TITLE='The Last Theorem'>\n"+
"    <Author>Arthur C. Clarke</Author>\n"+
"  </Book>\n"+
"  <Book ISSUED='1968' ISBN='234567819' TITLE='2001: A Space Odyssey'>\n"+
"    <Author>Arthur C. Clarke</Author>\n"+
"  </Book>\n"+
"  <Book TITLE='Bible' ISBN='9345678199'/>\n"+
"  <Book ISSUED='2007' ISBN='8345678191'\n"+
"  EDITOR='XML Prague'\n"+
"  TITLE='Proc svet nemluvi esperantem'>\n"+
"    <Author>Vaclav Trojan</Author>\n"+
"    <Author>Jiri Meska</Author>\n"+
"    <Author>Jiri Kamenicky˝</Author>\n"+
"  </Book>\n"+
"  <Book TITLE='Koran' ISBN='9345478191'/>\n"+
"</Books>";
			parse(xd, xml, reporter); //process data
			assertNoErrors(reporter);
			assertEq(5, xd.getVariable("#inserted").intValue());
			// insert 2
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"Books\">\n"+
"<xd:declaration scope='local'>\n"+
"external Service service;\n"+
"int inserted = 0;\n"+
"boolean ignored = false;\n"+
"String isAuthor =\n"+
"  \"SELECT AUTHOR FROM MYTEST.AUTHOR WHERE MYTEST.AUTHOR.AUTHOR = ?\";\n"+
"Statement isTitle = service.prepareStatement(\n"+
"  \"SELECT TITLE FROM MYTEST.TITLE WHERE MYTEST.TITLE.TITLE = ?\");\n"+
"Statement insertAuthor = service.prepareStatement(\n"+
"  \"INSERT INTO MYTEST.AUTHOR(AUTHOR) VALUES (?)\");\n"+
"Statement insertTitle = service.prepareStatement(\n"+
"  \"INSERT INTO MYTEST.TITLE(TITLE,EDITOR,ISBN,ISSUED) VALUES (?,?,?,?)\");\n"+
"Statement insertTitleAuthor = service.prepareStatement(\n"+
"  \"INSERT INTO MYTEST.TITLE_AUTHOR(IDAUTHOR,IDTITLE)\n"+
"   VALUES ((SELECT IDAUTHOR FROM MYTEST.AUTHOR WHERE AUTHOR=?),\n"+
"   (SELECT IDTITLE FROM MYTEST.TITLE WHERE TITLE=?))\");\n"+
"\n"+
"void insertTitle(String title, String editor, String isbn, String issued) {\n"+
"  if (ignored = isTitle.hasItem(title)) {\n"+
"     error('TEST001','Book \"&amp;{b}\" already exists','&amp;{b}'+title);\n"+
"  } else {\n"+
"     insertTitle.execute(title, editor, isbn, issued);\n"+
"     inserted++;\n"+
"  }\n"+
"}\n"+
"void insertAuthor(String title, String author) {\n"+
"  if (!ignored) {\n"+
"    if (!service.hasItem(isAuthor,author)) {\n"+
"      insertAuthor.execute(author); /*new author*/\n"+
"    }\n"+
"    insertTitleAuthor.execute(author, title);\n"+
"  }\n"+
"}\n"+
"</xd:declaration>\n"+
"  <Books>\n"+
"    <Book xd:script=\"occurs *; ref item\"/>\n"+
"  </Books>\n"+
"  <Book xd:script=\"ref item\"/>\n"+
"  <item xd:script=\"onStartElement insertTitle(toString(@TITLE),\n"+
"          toString(@EDITOR), toString(@ISBN), toString(@ISSUED))\"\n"+
"     TITLE=\"string\"\n"+
"     ISBN=\"regex('\\\\d{8,10}')\"\n"+
"     EDITOR=\"optional string\"\n"+
"     ISSUED=\"optional int\">\n"+
"     <Author xd:script=\"occurs *\">\n"+
"        optional string;\n"+
"        finally insertAuthor(xpath(\"../@TITLE\").toString(),getText());\n"+
"     </Author>\n"+
"  </item>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("#service", service);
			xml =
"<Books>\n"+
"  <Book ISSUED='1935' TITLE='Krakatit'  ISBN='230567819'>\n"+
"    <Author>Karel Capek</Author>\n"+
"  </Book>\n"+
"  <Book ISSUED='1968' ISBN='234567819' TITLE='2001: A Space Odyssey'>\n"+
"    <Author>Arthur C. Clarke</Author>\n"+
"  </Book>\n"+
"</Books>";
			parse(xd, xml, reporter);
			assertEq(1, xd.getVariable("#inserted").intValue());
			s = reporter.printToString();
			assertTrue(s.contains("TEST001: Book \"2001: A Space Odyssey\""),s);

			// read 1
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>\n"+
"external Service service;\n"+
"String qry = \"SELECT AUTHOR\n"+
"  FROM MYTEST.AUTHOR, MYTEST.TITLE_AUTHOR, MYTEST.TITLE\n"+
"  WHERE MYTEST.AUTHOR.IDAUTHOR = MYTEST.TITLE_AUTHOR.IDAUTHOR AND\n"+
"  MYTEST.TITLE.IDTITLE = MYTEST.TITLE_AUTHOR.IDTITLE AND\n"+
"  MYTEST.TITLE.IDTITLE = ? ORDER BY AUTHOR ASC\";\n"+
"ResultSet rs = \n"+
"  service.query('SELECT * FROM MYTEST.TITLE ORDER BY TITLE ASC');\n"+
"</xd:declaration>\n"+
"  <Books>\n"+
"    <Book xd:script=\"occurs *; create rs\"\n"+
"     TITLE=\"string\"\n"+
"     ISBN=\"regex('\\\\d{8,10}')\"\n"+
"     EDITOR=\"optional string\"\n"+
"     ISSUED=\"optional int\">\n"+
"      <Author xd:script=\"occurs *;\n"+
"          create service.queryItem(qry, 'AUTHOR', getItem('IDTITLE'));\">\n"+
"        string;\n"+
"      </Author>\n"+
"    </Book>\n"+
"  </Books>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("#service", service); //set connection
			assertEq(create(xd, "Books", reporter),
"<Books>"+
	"<Book TITLE=\"2001: A Space Odyssey\" ISBN=\"234567819\" ISSUED=\"1968\">"+
		"<Author>Arthur C. Clarke</Author></Book>"+
	"<Book TITLE=\"Bible\" ISBN=\"9345678199\"/>"+
	"<Book TITLE=\"Koran\" ISBN=\"9345478191\"/>"+
	"<Book TITLE=\"Krakatit\" ISBN=\"230567819\" ISSUED=\"1935\">"+
	"<Author>Karel Capek</Author></Book>"+
	"<Book TITLE=\"Proc svet nemluvi esperantem\"\n"+
	"ISBN=\"8345678191\" EDITOR=\"XML Prague\" ISSUED=\"2007\">"+
	"<Author>Jiri Kamenicky˝</Author>"+
	"<Author>Jiri Meska</Author>"+
	"<Author>Vaclav Trojan</Author>"+
	"</Book>"+
	"<Book TITLE=\"The Last Theorem\" ISBN=\"12345678\"\n"+
	"EDITOR=\"HarperCollins Publishers\" ISSUED=\"2008\">"+
	"<Author>Arthur C. Clarke</Author>"+
	"</Book>"+
"</Books>");
			// read 2
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:name=\"query\" >\n"+
"<xd:declaration scope='global'>\n"+
"external String url;\n"+
"external String usr;\n"+
"external String passw;	  \n"+
"Service service = new Service(\"jdbc\", url, usr, passw);\n"+
"String qry = \"SELECT AUTHOR\n"+
"  FROM MYTEST.AUTHOR, MYTEST.TITLE_AUTHOR, MYTEST.TITLE\n"+
"  WHERE MYTEST.AUTHOR.IDAUTHOR = MYTEST.TITLE_AUTHOR.IDAUTHOR AND\n"+
"  MYTEST.TITLE.IDTITLE = MYTEST.TITLE_AUTHOR.IDTITLE AND\n"+
"  MYTEST.TITLE.IDTITLE = ? ORDER BY AUTHOR ASC\";\n"+
"</xd:declaration>\n"+
"  <Books xd:script=\"\">\n"+
"    <Book xd:script=\"occurs *;create service.query(\n"+
"            'SELECT TITLE FROM MYTEST.TITLE ORDER BY TITLE ASC')\"\n"+
"     TITLE=\"optional string\"\n"+
"     ISBN=\"optional regex('\\\\d{8,10}')\"\n"+
"     EDITOR=\"optional string\"\n"+
"     ISSUED=\"optional int\">\n"+
"    </Book>\n"+
"  </Books>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("url",
				"jdbc:derby:" +	tempDir + File.separatorChar + DERBY_STORE +
				File.separatorChar + "test");
			xd.setVariable("usr", TEST_USER);
			xd.setVariable("passw", TEST_PWD);
			assertEq(xd.xcreate("Books", null), //execute construction,
"<Books>"+
"<Book TITLE=\"2001: A Space Odyssey\"/>"+
"<Book TITLE=\"Bible\"/>"+
"<Book TITLE=\"Koran\"/>"+
"<Book TITLE=\"Krakatit\"/>"+
"<Book TITLE=\"Proc svet nemluvi esperantem\"/>"+
"<Book TITLE=\"The Last Theorem\"/>"+
"</Books>");
			//Drop database
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"dropSchema\">\n"+
"<xd:declaration scope='local'> external Service con; </xd:declaration>\n"+
"  <dropSchema name=\"string; finally\n"+
"              con.execute('DROP SCHEMA '+ getText() + ' RESTRICT');\" >\n"+
"    <table xd:script=\"occurs *\"\n"+
"           name = \"string; onTrue con.execute('DROP TABLE ' +\n"+
"             xpath('../../@name') + '.' + getText());\" />\n"+
"  </dropSchema>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(System.getProperties(), xdef);
			xd = xp.createXDDocument();
			xd.setVariable("#con", service); //set connection
			xml =
"<dropSchema name = \"MYTEST\">\n"+
"  <table name = \"TITLE_AUTHOR\"/>\n"+
"  <table name = \"TITLE\"/>\n"+
"  <table name = \"AUTHOR\"/>\n"+
"</dropSchema>";
			xd.xparse(xml, null); //process data (insert to database)
			//close database connection
			service.close();
		// multiple query with a big amout of ResultSet cursors
		// test implicitly and manually closing of cursors
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"ResultSet rs;\n"+
"external Service service;\n"+
"int aVal;\n"+
"void doSQL(int a) {\n"+
"  aVal = a;\n"+
"  xcreate('C');\n"+
"}\n"+
"void doCB() {\n"+
"  for(int i=0; i LT " + RESULT_SET_NUM + "; i++) {\n"+
"    rs = service.query(\"SELECT * FROM " + TABLE_B + " WHERE a=\" + i);\n"+
"  }\n"+
"}\n"+
"</xd:declaration>\n"+
"\n"+
"  <A xd:script= \"create 1\">\n"+
"    <BB xd:script= \"occurs *; create service.query('SELECT * \n"+
"       FROM " + TABLE_B + "'); finally doSQL(parseInt(toString(@a)))\" \n"+
"         a = \"int\"\n"+
"         b = \"string\">\n"+
"    </BB>\n"+
"  </A>\n"+
"  \n"+
"  <C xd:script= \"occurs 1; create 1;\">"+
"    <B xd:script= \"occurs *; create service.query('SELECT * \n"+
"        FROM " + TABLE_B + " WHERE a=' + aVal); finally doCB();\"\n"+
"           a = \"? int\"\n"+
"           b = \"? string\">\n"+
"    </B>\n"+
"  </C>"+
"\n"+
"</xd:def>";
			_con = getConnection();
			create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			assertNoErrors(reporter);
			// check that external Service wasn't closed
			if(_con.isClosed()) {
				fail("X-Definition has closed the Service object.");
			} else {
				_con.close();
			}
		// test close(), isClosed() and closeStatement() function
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration scope='local'>  \n"+
"external Service service;\n"+
"void doA() {\n"+
"  Statement st = service.prepareStatement(\"SELECT * FROM " + TABLE_A +
"      WHERE a=?\");\n"+
"  ResultSet rs = st.query(\"1\");\n"+
"  rs.close();\n"+
"  st.close();\n"+
"  if(!rs.isClosed()) {\n"+
"    error(\"ResultSet isn't closed (close()).\");\n"+
"  }\n"+
"  if(!st.isClosed()) {\n"+
"    error(\"Statement isn't closed (close()).\");\n"+
"  }\n"+
"  st = service.prepareStatement(\"SELECT * FROM " + TABLE_A +
"      WHERE a=?\");\n"+
"  rs = st.query(\"1\");\n"+
"  rs.closeStatement();\n"+
"  if(!rs.isClosed()) {\n"+
"    error(\"ResultSet isn't closed (closeStatement()).\");\n"+
"  }\n"+
"  if(!st.isClosed()) {\n"+
"    /*error(\"Statement isn't closed (closeStatement()).\");*/\n"+
"  }\n"+
"  service.close();\n"+
"  if(!service.isClosed()) {\n"+
"    error(\"Service isn't closed (close()).\");\n"+
"  }\n"+
"}\n"+
"</xd:declaration>\n"+
"\n"+
"<A xd:script=\"finally doA()\"/>\n"+
"\n"+
"</xd:def>";
			_con = getConnection();
			create(xdef, null, null, "A", "#service",
				XDFactory.createSQLService(_con), reporter);
			assertNoErrors(reporter);
			// check that external Service wasn't closed
			if(!_con.isClosed()) {
				fail("X-Definition has't closed the Service object by close() "
						+ "script method.");
			}
		} catch (Exception ex) {fail(ex);}
		/* TODO:
		 * - pro ResultSet, Statement a Service jsou implementovany metody
		 *   void close() a boolean isClosed().
		 * - pokud neni Statement, Service nebo ResultSet deklarovan jako
		 *   "external", pak se provede automaticky close techto objektu
		 *   na konci zpracovani XDefinice.
		 *   Pro externi data ovsem musi uzivatel provest close
		 *   ve volajicim programu!
		 */
		// clean environment created and used for testing database
		cleanDBEnv(tempDir);
		// in case of none test failures remove the temporary and test data
		try {
			FUtils.deleteAll(tempDir, true);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
