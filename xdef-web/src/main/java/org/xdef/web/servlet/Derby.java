package org.xdef.web.servlet;

import static org.xdef.sys.SUtils.modifyFirst;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.w3c.dom.Element;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.XDService;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import org.xdef.xml.KXmlUtils;

/** Servlet for execution of examples from tutorial.
 * @author Vaclav Trojan
 */
public final class Derby extends AbstractMyServlet {
	private static final long serialVersionUID = -6985097379384362122L;
    private final static Map<String, Long> DBMAP = new HashMap<>();
	private final static String DBPASSW = "blabla"; // password for database.
	private final static String DBUSER = "myself";
////////////////////////////////////////////////////////////////////////////////
// HTML template sources
////////////////////////////////////////////////////////////////////////////////
	private static final String ADD_ITEMS =
"<html xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <head>\n"+
"    <meta http-equiv='content-type' content='text/html; charset=UTF-8' />\n"+
"    <script src='/tutorial/styles/lineNumbers.js'> </script>\n" +
"    <script src='/tutorial/styles/lineNumbers_1.js'> </script>\n" +
"    <style>.A{min-height:25rem;max-height:25rem;display:flex;overflow:hidden;}</style>\n" +
"    <style>.B{min-height:10rem;max-height:10rem;display:flex;overflow:hidden;}</style>\n" +
"    <title>Add items</title>\n"+
"  </head>\n"+
"  <body style='background: #EAFFFD'>\n"+
"    <form method='post' action='/tutorial/Derby'>\n"+
"      <b>X-definition</b>&nbsp;\n"+
"      <i>If you have a collection with more\n"+
"        X-definitions specify the name of root X-definition:</i>\n"+
"      <input name='xdname' value='' type='text'/>\n"+
"      <div class='A'>\n" +
"        <div id='line-numbers' class='container_1'></div>\n" +
"        <textarea id='textarea' style='width: 100%;' name='xdef'>\n" +
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"Books|Book\">\n"+
"&lt;xd:declaration>\n"+
"  external Service service; boolean ignored = false;\n"+
"  Statement isAuthor = service.prepareStatement(\"SELECT AUTHOR FROM BOOKS.AUTHOR WHERE BOOKS.AUTHOR.AUTHOR = ?\");\n"+
"  Statement isTitle = service.prepareStatement(\"SELECT TITLE FROM BOOKS.TITLE WHERE BOOKS.TITLE.TITLE = ?\");\n"+
"  Statement insertAuthor = service.prepareStatement(\"INSERT INTO BOOKS.AUTHOR(AUTHOR) VALUES (?)\");\n"+
"  Statement insertTitle = service.prepareStatement(\"INSERT INTO BOOKS.TITLE(TITLE,ISBN,ISSUED) VALUES (?,?,?)\");\n"+
"  Statement insertTitleAuthor = service.prepareStatement(\"INSERT INTO BOOKS.TITLE_AUTHOR(IDAUTHOR,IDTITLE)\n"+
"    VALUES ((SELECT IDAUTHOR FROM BOOKS.AUTHOR WHERE AUTHOR=?), (SELECT IDTITLE FROM BOOKS.TITLE WHERE TITLE=?))\");\n"+
"&lt;/xd:declaration>\n"+
"  &lt;Book title=\"string\" ISBN=\"regex('\\\\d{8,10}')\" issued=\"optional gYear()\"\n"+
"     xd:script=\"*; onStartElement {\n"+
"       if (ignored = isTitle.hasItem(toString(@title))) outln('Error: book \\'' + @title + '\\' already exists');\n"+
"       else {insertTitle.execute(toString(@title), toString(@ISBN), toString(@issued)); outln('ISBN: ' + @ISBN + '; ' + @title);}}\">\n"+
"     &lt;Author xd:script=\"occurs *\">\n"+
"        optional string;\n"+
"        finally if (!ignored) {\n"+
"          String s = getText();\n"+
"          if (!isAuthor.hasItem(s)) insertAuthor.execute(s); /*new author*/\n"+
"          insertTitleAuthor.execute(s, xpath(\"../@title\").toString());\n"+
"        }\n"+
"     &lt;/Author>\n"+
"  &lt;/Book>\n"+
"  &lt;Books> &lt;Book xd:script=\"occurs +; ref Book\"/> &lt;/Books>\n"+
"&lt;/xd:def>"+
"</textarea>\n"+
"      </div>\n"+
"      <b>XML data</b>\n"+
"      <div class=\"B\">\n" +
"        <div id=\"line-numbers_1\" class=\"container_1\"></div>\n" +
"        <textarea id=\"textarea_1\" style=\"width: 100%;\" name=\"data\">\n" +
"&lt;Books>\n"+
"  &lt;Book issued='2008' ISBN='12345678' title='The Last Theorem'>\n"+
"    &lt;Author>Arthur C. Clarke&lt;/Author>\n"+
"  &lt;/Book>\n"+
"  &lt;Book issued='2007' ISBN='8345678191' title='The X-definition 3.1'>\n"+
"    &lt;Author>Jindřich Kocman&lt;/Author>\n"+
"    &lt;Author>Jiří Kamenický&lt;/Author>\n"+
"  &lt;/Book>\n"+
"&lt;/Books>"+
"</textarea>\n"+
"      </div>\n"+
"      <input name='submit' value='Add items' type='submit'/>\n"+
"      <input name=\"database\" value=\"&{database}\" type=\"hidden\"/>\n"+
"      <input name=\"task\" value=\"finished\" type=\"hidden\"/>\n"+
"      <i>&nbsp;You can edit <b>X-definition</b> or <b>XML data</b></i>\n"+
"    </form>\n"+
"  </body>\n"+
"</html>";

	private static final String DISPLAY =
"<html xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <head>\n"+
"    <meta http-equiv='content-type' content='text/html; charset=UTF-8' />\n"+
"    <script src='/tutorial/styles/lineNumbers.js'> </script>\n" +
"    <style>.container{min-height:35rem;max-height:35rem;display:flex;overflow:hidden;}</style>\n" +
"    <title>Display database</title>\n"+
"  </head>\n"+
"  <body style='background: #EAFFFD'>\n"+
"    <form method='post' action='/tutorial/Derby'>\n"+
"      <b>X-definition</b>&nbsp;\n"+
"      <i>If you have a collection with more\n"+
"        X-definitions specify the name of root X-definition:</i>\n"+
"      <input name='xdname' value='' type='text'/>\n"+
"      <div class='container'>\n" +
"        <div id='line-numbers' class='container_1'></div>\n" +
"        <textarea id='textarea' style='width: 100%;' name='xdef'>\n" +
"&lt;xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:name=\"query\">\n"+
"  &lt;xd:declaration>\n"+
"    external Service service; /* connection to database from outside */\n"+
"    String qry = \"SELECT AUTHOR\n"+
"         FROM BOOKS.AUTHOR, BOOKS.TITLE_AUTHOR, BOOKS.TITLE\n"+
"         WHERE BOOKS.AUTHOR.IDAUTHOR = BOOKS.TITLE_AUTHOR.IDAUTHOR AND\n"+
"         BOOKS.TITLE.IDTITLE = BOOKS.TITLE_AUTHOR.IDTITLE AND\n"+
"         BOOKS.TITLE.IDTITLE = ? ORDER BY AUTHOR ASC\";\n"+
"    ResultSet rs = service.query('SELECT * FROM BOOKS.TITLE ORDER BY TITLE ASC');\n"+
"  &lt;/xd:declaration>\n"+
"\n"+
"  &lt;Books>\n"+
"    &lt;Book xd:script=\"occurs *; create rs /*iterator for generation of Book*/\"\n"+
"      title=\"string()\"\n"+
"      ISBN=\"regex('\\\\d{8,10}')\"\n"+
"      editor=\"optional string()\"\n"+
"      issued=\"optional gYear()\">\n"+
"      &lt;Author xd:script=\"occurs *;\n"+
"          create service.queryItem(qry, 'AUTHOR', rs.getItem('IDTITLE'));\">\n"+
"        string();\n"+
"      &lt;/Author>\n"+
"    &lt;/Book>\n"+
"  &lt;/Books>\n"+
"</xd:def>"+
"</textarea>\n"+
"      </div>\n"+
"      <input name='submit' value='Display database' type='submit'/>\n"+
"      <input name=\"database\" value=\"&{database}\" type=\"hidden\"/>\n"+
"      <input name=\"mode\" value=\"compose\" type=\"hidden\"/>\n"+
"      <input name=\"task\" value=\"finished\" type=\"hidden\"/>\n"+
"      <i>&nbsp;You can edit <b>X-definition</b> or <b>XML data</b></i>\n"+
"    </form>\n"+
"  </body>\n"+
"</html>";

	private static final String DROP =
"<html xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <head>\n"+
"    <meta http-equiv='content-type' content='text/html; charset=UTF-8' />\n"+
"    <script src='/tutorial/styles/lineNumbers.js'> </script>\n" +
"    <script src='/tutorial/styles/lineNumbers_1.js'> </script>\n" +
"    <style>.A{min-height:17rem;max-height:17rem;display:flex;overflow:hidden;}</style>\n" +
"    <style>.B{min-height:17rem;max-height:17rem;display:flex;overflow:hidden;}</style>\n" +
"    <title>Add items</title>\n"+
"  </head>\n"+
"  <body style='background: #EAFFFD'>\n"+
"    <form method='post' action='/tutorial/Derby'>\n"+
"      <b>X-definition</b>&nbsp;\n"+
"      <i>If you have a collection with more\n"+
"        X-definitions specify the name of root X-definition:</i>\n"+
"      <input name='xdname' value='' type='text'/>\n"+
"      <div class='A'>\n" +
"        <div id='line-numbers' class='container_1'></div>\n" +
"        <textarea id='textarea' style='width: 100%;' name='xdef'>\n" +
"&lt;xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"dropSchema\">\n"+
"\n"+
"  &lt;xd:declaration> external Service service; &lt;/xd:declaration>\n"+
"\n"+
"  &lt;dropSchema name=\"string; finally\n"+
"                 service.execute('DROP SCHEMA ' + getText() + ' RESTRICT');\">\n"+
"    &lt;table xd:script=\"occurs *\"\n"+
"           name = \"string; onTrue service.execute('DROP TABLE '\n"+
"                   + xpath('../../@name') + '.' + getText());\" />\n"+
"  &lt;/dropSchema>\n"+
"&lt;/xd:def>"+
"</textarea>\n"+
"      </div>\n"+
"      <b>XML data</b>\n"+
"      <div class=\"B\">\n" +
"        <div id=\"line-numbers_1\" class=\"container_1\"></div>\n" +
"        <textarea id=\"textarea_1\" style=\"width: 100%;\" name=\"data\">\n" +
"&lt;dropSchema name = \"BOOKS\">\n"+
"  &lt;table name = \"TITLE_AUTHOR\"/>\n"+
"  &lt;table name = \"TITLE\"/>\n"+
"  &lt;table name = \"AUTHOR\"/>\n"+
"&lt;/dropSchema>"+
"</textarea>\n"+
"      </div>\n"+
"      <input name='submit' value='Drop database' type='submit'/>\n"+
"      <input name=\"task\" value=\"finished\" type=\"hidden\"/>\n"+
"      <input name=\"database\" value=\"&{database}\" type=\"hidden\"/>\n"+
"      <i>&nbsp;You can edit <b>X-definition</b> or <b>XML data</b></i>\n"+
"    </form>\n"+
"  </body>\n"+
"</html>";

	private static final String HTML_RESULT =
"<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
"  <head>\n" +
"     <meta http-equiv='content-type' content='text/html; charset=UTF-8'/>\n"+
"     <title>&{title}</title>\n" +
"  </head>\n" +
"  <body>\n" +
"    <div align='right'>\n" +
"      <font size=1><i style='test-align:right'>\n" +
"        (X-definition version: <b>" + (XDConstants.BUILD_VERSION.endsWith("-SNAPSHOT")
					? XDConstants.BUILD_VERSION + " " + XDConstants.BUILD_DATETIME.substring(0, 16)
					: XDConstants.BUILD_VERSION) +
		"</b>)</i>\n" +
"      </font>\n" +
"    </div>\n" +
"    <p><font size=4><b>&{result-title}</b></font></p>\n" +
"    <pre><tt>&{result}</tt></pre>&{stdout}"+
"  </body>\n" +
"</html>";
////////////////////////////////////////////////////////////////////////////////

	private static String genDatabaseURL(final String dbname) {
		return "jdbc:derby:memory:"+ dbname + ";create=true";
	}

	/** Display error to reponse writer.
	 * @param out reponse writer.
	 * @param title title of message header.
	 * @param msg message to be displayed.
	 */
	private void displayMsg(final PrintWriter out, final String title, final String msg) {
		out.print(genHtmlMessage(title, "<h3>" + msg + "</h3>"));
	}

	/** Processes requests with respect to required language.
	 * methods. The Language is set according to request parameter "submit".
	 * @param req servlet-request object.
	 * @param resp servlet-response object.
	 * @throws IOException if a IO error occurs.
	 */
	@Override
	public final void procReq(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException,IOException{
		// This part we must synchronize to keep language settings etc
		// for whole process of the X-definition.
		synchronized(MANAGER) {
			resp.setContentType("text/html;charset=UTF-8");
			resp.setCharacterEncoding("UTF-8");
			PrintWriter out = resp.getWriter();
			req.setCharacterEncoding("UTF-8");
			String database = getParam(req, "database");
			String task = getParam(req, "task");
			String mode = getParam(req, "mode");
			String outHtml;
			if (database.isEmpty()) { // new database
				if (!task.equals("Create database")) { // incorrect task
					displayMsg(out, "Internal error",  "Incorrect Expected task: "+ task);
					return;
				}
				long time = System.currentTimeMillis();
				for (;;) {
					boolean wasRemoved = false;
					for (Entry<String, Long> x : DBMAP.entrySet()) {
						long dif = time - x.getValue();
						if (dif < 0) {
							dif = -dif;
						}
						if (dif > 24*3600*1000) { //older then 1 day
							wasRemoved = true;
							DBMAP.remove(x.getKey());
						}
						if (wasRemoved) {
							break;
						}
					}
					if (!wasRemoved) {
						break;
					}
				}
				long x = time;
				while (DBMAP.containsKey(database="DB"+Long.toHexString(x))){
					x++;
				}
				try {
					EmbeddedDataSource dbSource = new EmbeddedDataSource();
					dbSource.setDatabaseName("memory:"+ database);
					dbSource.setUser(DBUSER);
					dbSource.setPassword(DBPASSW);
					// create a new database
					dbSource.setCreateDatabase("create");
					Connection con = dbSource.getConnection( DBUSER, DBPASSW);
					con.close();
					DBMAP.put(database, time);
				} catch (SQLException ex) {
					displayMsg(out,
						"Error",
						"Database creation error<br/>"+
							stringToHTml(STester.printThrowable(ex),true));
					return;
				}
			} else {
				if (!DBMAP.containsKey(database)) {
					displayMsg(out, "Error", "Database is not available (was dropped or deleted).");
					return;
				}
				if (!"finished".equals(task)) {
					switch (task) {
						case "Add items":
							outHtml = ADD_ITEMS;
							break;
						case "Display database":
							outHtml = DISPLAY;
							break;
						case "Drop database":
							outHtml = DROP;
							break;
						default:
							out.print("ERROR unsuppoeted task: "+ task);
							return;
					}
					out.print(modifyFirst(outHtml,"&{database}",database));
					return;
				}
			}
			Report.setLanguage("eng");
			ArrayReporter reporter = new ArrayReporter();
			String xdef = getParam(req, "xdef");
			String data = getParam(req, "data");
			String xdName = getParam(req, "xdname");
			String result;
			String stdOutput = "";
			XDPool xp = null;
			Properties props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS,
				XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
			try {
				XDBuilder xb = XDFactory.getXDBuilder(reporter, props);
				xb.setSource(xdef);
				xp = xb.compileXD();
			} catch (Throwable ex) {
				if (null == xp || !reporter.errorWarnings()) {
					out.print("<html xmlns='http://www.w3.org/1999/xhtml'>"
						+ "<body><h1>Exception</h1><pre><tt><b>"
						+ stringToHTml(STester.printThrowable(ex), true)
						+ "</b></tt></pre></body></html>");
					return;
				} else {
					reporter.reset();
				}
			}
			try {
				if (reporter.errorWarnings()) {//incorrect xdef
					Report rep = reporter.getReport();
					if (null != rep && "XDEF903".equals(rep.getMsgID())
						&& null != rep.getModification()
						&& !rep.getModification().startsWith("&{0}<")) {
					} else {
						reporter.reset();
					}
					result = printReports(reporter, xdef);
					outHtml = modifyFirst(modifyFirst(HTML_RESULT, "&{title}", "Error"),
						"&{result-title}", "X-definition error(s):");
				} else {
					Element resultElement;
					reporter.clear();
					try (CharArrayWriter caw = new CharArrayWriter()) {
						XDOutput stdout = XDFactory.createXDOutput(caw, false);
						XDDocument xd;
						try {
							xd = xp.createXDDocument(xdName);
						} catch (RuntimeException ex) {
							xd = xp.createXDDocument("Example");
						}
						xd.setProperties(props);
						xd.setStdOut(stdout);
						//Set external variable with database connection
						String url = genDatabaseURL(database);
						String user = DBUSER;
						String password = DBPASSW;
						XDService service = XDFactory.createSQLService(url, user, password);
						xd.setVariable("service", service);
						if ("compose".equals(mode)) {
							String name;
							String uri;
							XMDefinition def =  xd.getXMDefinition();
							String mName = getParam(req, "mName");
							if (null!=mName&&!(mName = mName.trim()).isEmpty()){
								name = mName;
								String mURI = getParam(req, "mURI");
								uri = null != mURI && !(mURI= mURI.trim()).isEmpty() ? mURI : null;
							} else {
								XMElement[] x =xd.getXMDefinition().getModels();
								name = x[0].getName();
								uri = x[0].getNSUri();
								if (null != data && data.trim().length() > 0) {
									Element el = KXmlUtils.parseXml(data).getDocumentElement();
									xd.setXDContext(el);
									String n = el.getLocalName();
									String u = el.getNamespaceURI();
									if (null!=def && null!=def.getModel(u, n)) {
										uri = u;
										name = n;
									}
								}
							}
							resultElement = xd.xcreate(new QName(uri, name), reporter);
						} else {
							resultElement = xd.xparse(data, reporter);
						}
						try {
							service.commit();
						} catch (Exception ex) {
							System.out.println("commit: "+ ex);
						}
						service.close();
						if (caw.size() > 0) {
							stdOutput = "<h3>Output stream (System.out):</h3><pre><tt>"
								+ stringToHTml(caw.toString(), true) + "</tt></pre>";
						}
					}
					if (reporter.errors()) {
						outHtml = modifyFirst(modifyFirst(HTML_RESULT, "&{title}", "Error"),
							"&{result-title}", "Input data error(s):");
						result = printReports(reporter, data);
					} else {
						String title = "Task finished";
						if (mode.equals("compose")) {
							title = "Display database";
							result = "<h3>Database as XML:</h3><pre><tt>"
								+ stringToHTml(KXmlUtils.nodeToString(
									resultElement,true,false,true,110), true)
								+ "</tt></pre>\n";
						} else if (task.equals("finished")) {
							if ("Drop database".equals(getParam(req,"submit"))){
								DBMAP.remove(database);
								displayMsg(out, "Database removed",
									"Database has been cleaned and removed from database list.");
								return;
							}
							result = stdOutput;
						} else {
							title = "Database created";
							result =
"<form style='background: #EAFFFD' method='post' action='/tutorial/Derby' >\n"+
"<h2>Select task:</h2>\n"+
"<input name='task' value='Add items' type='submit'/>\n"+
"<input name='task' value='Display database' type='submit'/>\n"+
"<input name='task' value='Drop database' type='submit'/>\n"+
"<input name='database' value='"+ database +"' type='hidden'/>\n"+
"</form>";
						}
						displayMsg(out, title, result);
						return;
					}
				}
				result = stringToHTml(result, true);
			} catch (SRuntimeException ex) {
				if ("SYS024".equals(ex.getMsgID())) {
					reporter.putReport(Report.fatal(XML.XML080, //XML parser was canceled by error&{0}{: }
						"The XML document must start with '<'", "&{line}1&{column}1"));
				} else if (!reporter.errorWarnings()) {
					reporter.putReport(Report.fatal(ex.getMsgID(),
						ex.getReport().getText(), ex.getReport().getModification()));
				}
				displayMsg(out, "Input data error", stringToHTml(printReports(reporter,data),true));
				reporter.reset();
				return;
			}
			out.print(modifyFirst(modifyFirst(outHtml,"&{result}",result),"&{stdout}",stdOutput));
		}
	}

	/** Returns a short description of this servlet.
	 * @return short description of this servlet.	 */
	@Override
	public final String getServletInfo() {return "This servlet executes a X-definition with database.";}
}