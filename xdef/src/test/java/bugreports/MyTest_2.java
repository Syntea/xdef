package bugreports;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import test.XDTester;
import java.net.URL;
import org.xdef.component.XComponent;
import org.xdef.json.JNull;
import org.xdef.sys.SUtils;

/** Various tests JSON, X-component.
 * @author Vaclav Trojan
 */
public class MyTest_2 extends XDTester {

	public MyTest_2() {super();}

////////////////////////////////////////////////////////////////////////////////
// User methods used in X-definitions tests
////////////////////////////////////////////////////////////////////////////////

	/** Create X-components from XDPool to the directory from argument "srcDir"
	 * and compile it. Clear tempDir before generation.
	 * @param xp XDPool from which the X-components created.
	 * @param tempDir the directory where to generate Java sources.
	 * @return reporter with reported errors and warnings.
	 * @throws RuntimeException if an error occurs.
	 */
	private static final ArrayReporter genXComponent(final XDPool xp,
		final String tempDir) {
		File fdir = new File(tempDir);
		if (fdir.exists()) { // ensure the src directory exists.
			if (fdir.isFile()) {
				throw new RuntimeException(tempDir + " is not directory");
			}
			try {
				SUtils.deleteAll(tempDir, true); // delete this directory
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		fdir.mkdirs();
		try {
			return genXComponent(xp, fdir);
		} catch (Exception ex) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
//		T = true; // if true, only the first one test is invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		String tempDir = getTempDir();
		File f = new File(getTempDir());
		if (f.exists() && !f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()
				+ " is not directory");
		}
		f.mkdir();
		tempDir = f.getAbsolutePath().replace('\\', '/');
		if (!tempDir.endsWith("/")) {
			tempDir += '/';
		}
		if (!f.isDirectory()) {
			fail('\"' + tempDir + "\" is not directory");
			return;
		}
		XDPool xp;
		String xdef;
		String xml;
		String s;
		String json;
		Object j;
		Object o;
		XDDocument xd;
		Element el;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
////////////////////////////////////////////////////////////////////////////////
//		try {
//			xdef =
//"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.0'>\n"+
//"<xd:def name='A'>\n"+
//" <xd:declaration scope='global'>Parser p;uniqueSet u{x:p}</xd:declaration>\n"+
//"</xd:def>\n"+
//"<xd:def name='B' root='A'>\n"+
//"  <A id=\"? xdType(); onTrue p = getParsedValue();\" />\n"+
//"</xd:def>\n"+
//"</xd:collection>";
//			xb = XDFactory.getXDBuilder(null, null);
//			xb.setSource(xdef);
//			xb.compileXD();;
//		} catch (Exception ex) {fail(ex);}
//if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"[\"int()\", \"int\", \"jstring()\"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.A002 %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, tempDir);
			json = "[1, \"2\", 3]"; //error (not string but number!)
			j = xp.createXDDocument().jparse(json, reporter);
			assertTrue(reporter.printToString().contains("XDEF809"));
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& reporter.printToString().contains("XDEF809"));
			assertTrue(JsonUtil.jsonEqual(
				JsonUtil.parse("[1,\"\",\"\"]"),xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"[\n" +
" { $script:\"+\",\n" +
"  \"first name\": \"? string;\",\n" +
"  \"last name\" : \"string;\",\n" +
"  \"age\": \"int(1,100); finally outln(getXPos()+'; '+getXDPosition());\",\n" +
"  \"address\"  : {$script: \"?\",\n" +
"    \"street address\": \"string;\",\n"+
"    \"city\"         : \"string;\",\n" +
"    \"postal code\"   : \"? string(%pattern='[0-9]+(-[0-9]+)?');\"\n" +
"  },\n" +
"  \"phone numbers\": [ $script: \"?\",\n" +
"    { $script: \"occurs *\",\n" +
"      \"type\"  : \"an;\",\n" +
"      \"number\": \"string(%pattern='[0-9]+(-[0-9]+)*');finally outln(getXPos());\"\n" +
"    }\n" +
"  ]\n" +
" }\n" +
"]\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.A0 %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			json =
"[\n" +
" { \"first name\": \"John\",\n" +
"  \"last name\" : \"doe\",\n" +
"  \"age\"       : 100,\n" +
"  \"address\"   : {\n" +
"    \"street address\": \"naist street\",\n" +
"    \"city\"          : \"Nara\",\n" +
"    \"postal code\"   : \"630-0192\"\n" +
"  },\n" +
"  \"phone numbers\": [\n" +
"    {\n" +
"      \"type\"  : \"iPhone\",\n" +
"      \"number\": \"0123-4567-8888\"\n" +
"    },\n" +
"    {\n" +
"      \"type\"  : \"home\",\n" +
"      \"number\": \"0123-4567-8910\"\n" +
"    }\n" +
"  ]\n" +
" },\n" +
" {\"first name\": \"John\",\n" +
"  \"last name\" : \"Brown\",\n" +
"  \"age\"       : 1,\n" +
" }\n" +
"]";
			xd = xp.createXDDocument();
			j = xd.jparse(json, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& reporter.printToString().contains("XDEF809"));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"{ \"store\": {\n" +
"    \"book\": [\n" +
"      {$script: \"occurs +\",\n" +
"        \"category\": \"enum('reference', 'fiction')\",\n" +
"        \"author\": \"string\",\n" +
"        \"title\": \"string\",\n" +
"        \"isbn\": \"? string\",\n" +
"        \"price\": \"float\"\n" +
"      }\n" +
"    ],\n" +
"    \"bicycle\": {\n" +
"      \"color\": \"enum('red', 'white', 'black'); finally outln(getXPos() + '; ' + getXDPosition());\",\n" +
"      \"price\": \"float\"\n" +
"    }\n" +
"  }\n" +
"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.A1 %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
//"/array/map[1]/item[1]/@value"
//			"[{\"a\":@value}}"
/*
$.store.book[0].title			$['store']['book'][0]['title']

*/
//System.out.println(getJPosition(xp, "/map/map/array/map/item[1]/@value"));
//System.out.println(getJPosition(xp, "/map/map/map/item/@value"));

			json =
"{ \"store\": {\n" +
"    \"book\": [ \n" +
"      { \"category\": \"reference\",\n" +
"        \"author\": \"Nigel Rees\",\n" +
"        \"title\": \"Sayings of the Century\",\n" +
"        \"price\": 8.95\n" +
"      },\n" +
"      { \"category\": \"fiction\",\n" +
"        \"title\": \"Sword of Honour\",\n" +
"        \"author\": \"Evelyn Waugh\",\n" +
"        \"price\": 12.99\n" +
"      },\n" +
"      { \"price\": 8.99,\n" +
"        \"isbn\": \"0-553-21311-3\",\n" +
"        \"author\": \"Herman Melville\",\n" +
"        \"title\": \"Moby Dick\",\n" +
"        \"category\": \"fiction\",\n" +
"      },\n" +
"      { \"category\": \"fiction\",\n" +
"        \"author\": \"J. R. R. Tolkien\",\n" +
"        \"title\": \"The Lord of the Rings\",\n" +
"        \"isbn\": \"0-395-19395-8\",\n" +
"        \"price\": 22.99\n" +
"      }\n" +
"    ],\n" +
"    \"bicycle\": {\n" +
"      \"color\": \"red\",\n" +
"      \"price\": 19.95\n" +
"    }\n" +
"  }\n" +
"}";
//System.out.println(json);
//System.out.println(xdef);
			j = xp.createXDDocument().jparse(json, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& reporter.printToString().contains("XDEF809"));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
//System.out.println(JsonUtil.toJsonString(j, true));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='Y'>\n"+
"<xd:json name='Y'>\n"+
"[{\"a\":\"jboolean\"},\"jstring()\",\"jnumber()\",\"? jboolean()\"]\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.TY_X %link #Y;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			Class<?> clazz = MyTest_2.class;
			String className = clazz.getName().replace('.', '/') + ".class";
			URL u = clazz.getClassLoader().getResource(className);
			String classDir =
				new File(u.getFile()).getAbsolutePath().replace('\\', '/');
			classDir = classDir.substring(0, classDir.indexOf(className));
			System.out.println(classDir + "bugreports/xp.xp");
			XDFactory.writeXDPool(classDir + "bugreports/xp.xp", xp);
			xp = XDFactory.readXDPool("classpath://bugreports.xp.xp");
			genXComponent(xp, tempDir);
			json = "[{\"a\":false},\"xxx\",125, true]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& reporter.printToString().contains("XDEF809"));
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("xxx", getValueFromGetter(xc, "get$item"));
			assertEq(125, getValueFromGetter(xc, "get$item_1"));
			assertEq(true, getValueFromGetter(xc, "get$item_2"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='Y'>\n"+
"<xd:json name='Y'>\n"+
"[{\"a\":\"jboolean\"},\"jstring()\",\"jnumber()\",\"? jboolean()\"]\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.TY_X %link #Y;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			Class<?> clazz = MyTest_2.class;
			String className = clazz.getName().replace('.', '/') + ".class";
			URL u = clazz.getClassLoader().getResource(className);
			String classDir =
				new File(u.getFile()).getAbsolutePath().replace('\\', '/');
			classDir = classDir.substring(0, classDir.indexOf(className));
			System.out.println(classDir + "bugreports/xp.xp");
			XDFactory.writeXDPool(classDir + "bugreports/xp.xp", xp);
			xp = XDFactory.readXDPool("classpath://bugreports.xp.xp");
			genXComponent(xp, tempDir);
			json = "[{\"a\":true},\"xxx\",125, true]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("xxx", getValueFromGetter(xc, "get$item"));
			assertEq(125, getValueFromGetter(xc, "get$item_1"));
			assertEq(true, getValueFromGetter(xc, "get$item_2"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n" +
"<xd:declaration>\n" +
"  type genre enum(\"Classic\", \"Country\", \"Folk\", \"Jazz\", \"Pop\", \"Pop_punk\",\n" +
"             \"Punk\", \"Punk_rock\", \"Rap\", \"Rock\", \"R&amp;B\", \"Other\");\n" +
"  boolean yy = genre().parse('Country').matches();\n"+
"  Parser p = enum(\"Classic\", \"Country\", \"Folk\", \"Jazz\", \"Pop\", \"Pop_punk\",\n" +
"             \"Punk\", \"Punk_rock\", \"Rap\", \"Rock\", \"R&amp;B\", \"Other\");\n" +
"  boolean zz = p.parse('Country').matches();\n"+
"</xd:declaration>\n" +
"<xd:json name='a'>\n" +
"{\n" +
"  \"Genre\": [$oneOf,\n" +
"     \"genre\",\n" +
"     [\"occurs 1..*; genre\"]\n" +
"  ]\n" +
"}\n" +
"</xd:json>"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"Genre\": \"Classic\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
			json = "{\"Genre\": [\"Classic\"]}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
			json = "{\"Genre\": [\"Punk\", \"Other\"]}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.XAA %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq("aaa", getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", 123);
			assertEq(123, getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", null);
			assertNull(getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", " a b \t");
			assertEq(" a b \t", getValueFromGetter(xc, "get$a"));

			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(getValueFromGetter(
				xc, "getjs$item"), "getvalue"));

			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(false, getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", 123);
			assertEq(123, getValueFromGetter(xc, "get$a"));

			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$a"));

			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", 123);
			assertEq(123, getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", null);
			assertNull(getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", " a b \t");
			assertEq(" a b \t", getValueFromGetter(xc, "get$a"));

			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(xc, "get$a"));

			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(false, getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", JNull.JNULL);
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$a"));

			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$a"));

			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "get$a"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A | B'>\n"+
"<xd:json name='A'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"[\"? jvalue()\"]\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.XA %link #A;\n"+
"  %class bugreports.data.XB %link #B;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq("aaa", getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", 123);
			assertEq(123, getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", null);
			assertNull(getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", "xyz");
			assertEq("xyz", getValueFromGetter(xc, "get$a"));
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(xc, "get$a"));
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(false, getValueFromGetter(xc, "get$a"));
			setValueToSetter(xc, "set$a", 123);
			assertEq(123, getValueFromGetter(xc, "get$a"));

			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$a"));

			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "get$a"));

			json = "[null]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("bugreports.data.XB"), reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$item"));
			json = "[123]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("bugreports.data.XB"), reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(xc, "get$item"));
			json = "[true]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("bugreports.data.XB"), reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(true, getValueFromGetter(xc, "get$item"));
			json = "[]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("bugreports.data.XB"), reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "get$item"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.XD %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertEq("aaa", getValueFromGetter(xc, "get$a"));
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertEq(123, getValueFromGetter(xc, "get$a"));
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertTrue(!(Boolean) getValueFromGetter(xc, "get$a"));
			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$a"));
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertNull(getValueFromGetter(xc, "get$a"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
"[\n"+
"    \"? jnull\",\n"+
"    \"? int()\",\n"+
"    \"? string()\"\n"+
"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.TJ1 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "[null, 12, \" a b \"]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$item"));
			assertEq(12, getValueFromGetter(xc, "get$item_1"));
			assertEq(" a b ", getValueFromGetter(xc, "get$item_2"));
			json = "[null]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			assertEq(JNull.JNULL, getValueFromGetter(xc, "get$item"));
			assertNull(getValueFromGetter(xc, "get$item_1"));
			assertNull(getValueFromGetter(xc, "get$item_2"));
			json = "[12]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			assertNull(getValueFromGetter(xc, "get$item"));
			assertEq(12, getValueFromGetter(xc, "get$item_1"));
			assertNull(getValueFromGetter(xc, "get$item_2"));
			json = "[\"\"]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			assertNull(getValueFromGetter(xc, "get$item"));
			assertNull(getValueFromGetter(xc, "get$item_1"));
			assertEq("", getValueFromGetter(xc, "get$item_2"));
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.0'>\n"+
"<xd:def name='X' root='X'>\n"+
"<xd:json xd:name='X'>\n"+
"[\"int()\"]\n"+
"</xd:json>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='Y'>\n"+
"<xd:json name='Y'>\n"+
"[{\"a\":\"jboolean\"},\"jstring()\",\"jnumber()\",\"? jboolean()\"]\n" +
"</xd:json>\n"+
"</xd:def>\n"+
"<xd:def name='Z' root='Z | json'>\n"+
"<xd:json name='Z'>\n"+
"{\"a\":\"string()\"}\n" +
"</xd:json>\n"+
"<xd:json xd:name='json'>\n"+
"[\"date()\"]\n"+
"</xd:json>\n"+
"</xd:def>\n"+
"<xd:component>\n"+
"  %class bugreports.data.TX %link X#X;\n"+
"  %class bugreports.data.TY %link Y#Y;\n"+
"  %class bugreports.data.TZ %link Z#Z;\n"+
"  %class bugreports.data.TJson %link Z#json;\n"+
"</xd:component>\n"+
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			Class<?> TX = Class.forName("bugreports.data.TX");
			Class<?> TY = Class.forName("bugreports.data.TY");
			Class<?> TZ = Class.forName("bugreports.data.TZ");
			Class<?> TJson = Class.forName("bugreports.data.TJson");
			json = "[\"2020-01-01\"]";
			j = xp.createXDDocument("Z").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument("Z").jparseXComponent(json,TJson,reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				xc.toJson());
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.xmlToJson(xc.toXml()));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.xmlToJson(JsonUtil.jsonToXml(xc.toJson())));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.xmlToJson(JsonUtil.jsonToXmlXdef(xc.toJson())));
			json = "[123]";
			j = xp.createXDDocument("X").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument("X").jparseXComponent(json, TX, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(123, getValueFromGetter(xc, "get$item"));
			json = "[{\"a\":true},\"xxx\",125, true]";
			j = xp.createXDDocument("Y").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument("Y").jparseXComponent(json, TY, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("xxx", getValueFromGetter(xc, "get$item"));
			assertEq(125, getValueFromGetter(xc, "get$item_1"));
			assertEq(true, getValueFromGetter(xc, "get$item_2"));
			json = "{\"a\":\"2020-01-01\"}";
			j = xp.createXDDocument("Z").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument("Z").jparseXComponent(json, TZ, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("2020-01-01", getValueFromGetter(xc, "get$a"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A|B|json'>\n"+
"<xd:json name='json'>\n"+
"[\n"+
"  {\"a\":\"boolean\"},\n"+
"  \"string()\",\n"+
"  \"int()\"\n"+
"]\n" +
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"{\"a\":\"int\"}\n"+
"</xd:json>\n"+
"  <A/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			json = "[{\"a\":true},\"x\",-1]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='T' >\n" +
"  <xd:declaration>\n" +
"    uniqueSet r {a: string(1,2); b: string(1,2)};\n" +
"  </xd:declaration>\n" +
"  <T>\n" +
"    <R xd:script='*; finally r.ID()' A='r.a' B='r.b'/>\n" +
"  </T>\n" +
"<xd:component>\n"+
"  %class bugreports.data.T %link #T;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			xd = xp.createXDDocument();
			xml =
"<T>\n" +
"  <R A='xx' B='aaa'/>\n" +
"  <R A='xxx' B='aa'/>\n" +
"  <R A='xxx' B='aaa'/>\n" +
"  <R A='xx' B='aa'/>\n" +
"</T>";
			assertEq(xml, parse(xd, xml, reporter));
			s = reporter.printToString();
			assertTrue(s.contains(" \"a\")") && s.contains(" \"b\")")
				&& s.contains(" \"a\", \"b\"")&&reporter.getErrorCount()==7,s);
//			Class.forName("bugreports.data.T");
			xml =
"<T>\n" +
"  <R A='xx' B='aa'/>\n" +
"  <R A='yy' B='aa'/>\n" +
"  <R A='xx' B='yy'/>\n" +
"  <R A='zz' B='zz'/>\n" +
"</T>";
			el = parseXC(xp, "", xml, null, reporter).toXml();
			assertNoErrors(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:lexicon language='eng' >\n"+
"#a      =    a\n"+
"#a/c    =    b\n"+
"#a/c/@f =    e\n"+
"</xd:lexicon>\n"+
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='slk' >\n"+
"#a      =    a\n"+
"#a/c    =    d\n"+
"#a/c/@f =    g\n"+
"</xd:lexicon>\n"+
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='ces' default='yes' />\n"+
"<xd:component>\n"+
"  %class bugreports.data.A %link #a;\n"+
"</xd:component>\n"+
"<a><c f='string'/></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			xd = xp.createXDDocument();

			xd.setLexiconLanguage("eng");
			xml = "<a><b e='a'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("slk");
			xml = "<a><d g='a'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("ces");
			xml = "<a><c f='a'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("ces");
			xml = "<a><c f='a'/></a>";
			XComponent xcA =
				parseXC(xd, xml, Class.forName("bugreports.data.A"), reporter);
			assertNoErrors(reporter);
			assertEq(xml, xcA.toXml());
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("eng");
			xml = "<a><b e='a'/></a>";
			xc = parseXC(xd,
				xml, Class.forName("bugreports.data.A"), reporter);
			assertNoErrors(reporter);
			assertEq("<a><b e='a'/></a>", xc.toXml());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Contract'>\n"+
"<xd:component>\n"+
"  %class bugreports.data.Contract %link #Contract;\n"+
"</xd:component>\n"+
"<Contract Number=\"num()\">\n"+
"  <Client xd:script=\"+\"\n"+
"     Type=\"int()\"\n"+
"     Name=\"? string\"\n"+
"     ID=\"? num()\"\n"+
"     GivenName=\"? string\"\n"+
"     LastName=\"? string\"\n"+
"     PersonalID=\"? string\" />\n"+
"</Contract>\n"+
"<Agreement Date=\"required; create toString(now(),'yyyy-MM-dd HH:mm');\"\n"+
"           Number=\"required num(10); create from('@Number');\" >\n"+
"  <Owner xd:script= \"occurs 1;\n"+
"                         create from('Client[@Typ=\\'1\\']');\" \n"+
"           ID=\"required num(8); create from('@ID');\"\n"+
"           Name=\"required string(1,30); create from('@Name');\" />\n"+
"  <Holder xd:script=\"occurs 1; create from('Client[@Typ=\\'2\\']');\" \n"+
"          PID=\"required string(10,11); create from('@PID');\"\n"+
"          GivenName=\"required string(1,30); create from('@GivenName');\"\n"+
"          LastName=\"required string(1,30); create from('@LastName');\" />\n"+
"  <Mediator xd:script=\"occurs 1; create from('Client[@Typ=\\'3\\']');\"\n"+
"            ID=\"required num(8); create from('@IČO');\"\n"+
"            Name=\"required string(1,30);\n"+
"              create toString(from('@GivenName'))+' '+from('@LastName');\"/>\n"+
"</Agreement>\n"+
"</xd:def>";
			String[] params = new String[]{xdef,
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='eng'>\n"+
"#Contract =                         Contract\n"+
"#Contract/@Number =                 Number\n"+
"#Contract/Client =                  Client\n"+
"#Contract/Client/@Type =            Type\n"+
"#Contract/Client/@Name =            Name\n"+
"#Contract/Client/@ID =              ID\n"+
"#Contract/Client/@GivenName =       GivenName\n"+
"#Contract/Client/@LastName =        LastName\n"+
"#Contract/Client/@PersonalID =      PersonalID\n"+
"#Agreement =                        Agreement\n"+
"#Agreement/@Date =                  Date\n"+
"#Agreement/@Number =                Number\n"+
"#Agreement/Owner =                  Owner\n"+
"#Agreement/Owner/@ID =              ID\n"+
"#Agreement/Owner/@Name =            Name\n"+
"#Agreement/Holder =                 Holder\n"+
"#Agreement/Holder/@PID =            PID\n"+
"#Agreement/Holder/@GivenName =      GivenName\n"+
"#Agreement/Holder/@LastName =       LastName\n"+
"#Agreement/Mediator =               Mediator\n"+
"#Agreement/Mediator/@ID =           ID\n"+
"#Agreement/Mediator/@Name =         Name\n"+
"</xd:lexicon>",
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='ces'>\n"+
"#Contract =                         Smlouva\n"+
"#Contract/@Number =                 Číslo\n"+
"#Contract/Client =                  Klient\n"+
"#Contract/Client/@Type =            Role\n"+
"#Contract/Client/@Name =            Název\n"+
"#Contract/Client/@ID =              IČO\n"+
"#Contract/Client/@GivenName =       Jméno\n"+
"#Contract/Client/@LastName =        Příjmení\n"+
"#Contract/Client/@PersonalID =      RodnéČíslo\n"+
"#Agreement =                        Dohoda\n"+
"#Agreement/@Date =                  Datum\n"+
"#Agreement/@Number =                Číslo\n"+
"#Agreement/Owner =                  Vlastník\n"+
"#Agreement/Owner/@ID =              IČO\n"+
"#Agreement/Owner/@Name =            Název\n"+
"#Agreement/Holder =                 Držitel\n"+
"#Agreement/Holder/@PID =            RČ\n"+
"#Agreement/Holder/@GivenName =      Jméno\n"+
"#Agreement/Holder/@LastName =       Příjmení\n"+
"#Agreement/Mediator =               Prostředník\n"+
"#Agreement/Mediator/@ID =           IČO\n"+
"#Agreement/Mediator/@Name =         Název\n"+
"</xd:lexicon>",
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='deu'>\n"+
"#Contract =                         Vertrag\n"+
"#Contract/@Number =                 Nummer\n"+
"#Contract/Client =                  Klient\n"+
"#Contract/Client/@Type =            Art\n"+
"#Contract/Client/@Name =            Name\n"+
"#Contract/Client/@ID =              Organisations-ID\n"+
"#Contract/Client/@GivenName =       Vorname\n"+
"#Contract/Client/@LastName =        Nachname\n"+
"#Contract/Client/@PersonalID =      Personalausweis\n"+
"#Agreement =                        Zustimmung\n"+
"#Agreement/@Date =                  Datum\n"+
"#Agreement/@Number =                Nummer\n"+
"#Agreement/Owner =                  Inhaber\n"+
"#Agreement/Owner/@ID =              Organisations-ID\n"+
"#Agreement/Owner/@Name =            Name\n"+
"#Agreement/Holder =                 Halter\n"+
"#Agreement/Holder/@PID =            Geburtsnummer\n"+
"#Agreement/Holder/@GivenName =      Vorname\n"+
"#Agreement/Holder/@LastName =       Nachname\n"+
"#Agreement/Mediator =               Vermittler\n"+
"#Agreement/Mediator/@ID =           Organisations-ID\n"+
"#Agreement/Mediator/@Name =         Name\n"+
"</xd:lexicon>",
			};
			xp = compile(params);
			genXComponent(xp, tempDir);
			xd = xp.createXDDocument();
			xml =
"<Smlouva Číslo = \"0123456789\">\n"+
"  <Klient Role       = \"1\"\n"+
"          Název      = \"Nějaká Firma s.r.o.\"\n"+
"          IČO        = \"12345678\" />\n"+
"  <Klient Role       = \"2\"\n"+
"          Jméno      = \"Jan\"\n"+
"          Příjmení   = \"Kovář\"\n"+
"          RodnéČíslo = \"311270/1234\" />\n"+
"  <Klient Role       = \"3\"\n"+
"          Jméno      = \"František\"\n"+
"          Příjmení   = \"Bílý\"\n"+
"          RodnéČíslo = \"311270/1234\"\n"+
"          IČO        = \"87654321\" />\n"+
"</Smlouva>";
			xd.setLexiconLanguage("ces");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Type  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd.setLexiconLanguage("deu");
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Type  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
//			System.out.println(KXmlUtils.nodeToString(el, true));
			assertNoErrors(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
if(T){return;}

		try {
			if (new File(tempDir).exists()) {
				SUtils.deleteAll(tempDir, true);
			}
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}