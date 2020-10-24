package bugreports;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** Test JSON/XML conversions
 * @author Vaclav Trojan
 */
public class TestJsonUtils extends XDTester {

	public TestJsonUtils() {super();}

	private static String jtest(String json) {
		return jtestObj(JsonUtil.parse(json));
	}

	private static String jtestObj(Object json) {
		Element el = TestJson_Util.jsonToXmlXD(json); // new XD XML
		Object json1 = TestJson_Util.xmlToJson(el);
		if (!JsonUtil.jsonEqual(json, json1)) {
			return "Error new xd:\n"
				+ KXmlUtils.nodeToString(el, true) + '\n'
				+ json+'\n' + JsonUtil.toJsonString(json1,true);
		}
		el = JsonUtil.jsonToXmlXD(json); // old XD XML
		json1 = TestJson_Util.xmlToJson(el);
		if (!JsonUtil.jsonEqual(json, json1)) {
			return "Error old XD:\n"
				+ json+'\n' + JsonUtil.toJsonString(json1,true);
		}
		el = JsonUtil.jsonToXml(json); // w3c XML
		json1 = TestJson_Util.xmlToJson(el);
		if (!JsonUtil.jsonEqual(json, json1)) {
			return "Error w3c:\n"
				+ json+'\n' + JsonUtil.toJsonString(json1,true);
		}
		return "";
	}

	private static String xtest(String xml) {
		Element el = KXmlUtils.parseXml(xml).getDocumentElement();
		Object jsobj = TestJson_Util.xmlToJson(el);
		Object jsobj1 = JsonUtil.xmlToJson(el);
		return !JsonUtil.jsonEqual(jsobj, jsobj1)
			? "Error:\n" + xml +'\n' + JsonUtil.toJsonString(jsobj,true) : "";
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String s;
		String xdef;
		String xml;
		Element el;
		XDPool xp;
		Object o;
		ArrayReporter reporter = new ArrayReporter();
		try {
			assertEq("", jtestObj(null));
			assertEq("", jtestObj("abc d"));
			assertEq("", jtestObj(""));
			assertEq("", jtestObj(true));
			assertEq("", jtestObj(false));
			assertEq("", jtestObj(-3.15e3));
			assertEq("", jtestObj(0));

			assertEq("", jtest("{}"));
			assertEq("", jtest("[]"));
			assertEq("", jtest("[[]]"));
			assertEq("", jtest("[{}]"));
			assertEq("", jtest("{ \"a\":[\n" +
"  [\"\", \"[\", \",\", \"]\", \"xyz\", 123.4, false, null, \"ab \",\n"+
"     {\"x\":1}],\n" +
"  [0, 0, 1],\n" +
"  [-5, 33, 0.5]\n" +
"]}"));
//if (true) return;
			assertEq("", jtest("{}"));
			assertEq("", jtest("{\"a\":1}"));
			assertEq("", jtest("{\"a\":{}}"));
			assertEq("", jtest("{\"a\":[]}"));
			assertEq("", jtest("{\"a\":1,\"b\":[]}"));
			assertEq("", jtest("{\"a\":{\"b\":1,\"c\":[]}}"));
			assertEq("", jtest("[]"));
			assertEq("", jtest("[1]"));
			assertEq("", jtest("[1,2]"));
			assertEq("", jtest("[{}]"));
			assertEq("", jtest("[{\"a\":1}]"));
			assertEq("", jtest("[{\"a\":1, \"b\":2}]"));
			assertEq("", jtest("[{\"a\":1, \"b\":[]}]"));
			assertEq("", jtest("[{\"a\":1, \"b\":{}}]"));
			assertEq("", jtest("[{\"a\":1, \"b\":{\"c\":null}}]"));
			assertEq("", jtest("[[]]"));
			assertEq("", jtest("[[{}]]"));
			assertEq("", jtest(
"[\"\", \"[\", \",\", \"]\", \"xyz\", 123.4, false, null, {\"x\":1}, \"ab \"]"
			));
		assertEq("", jtest(
"{\"a\":[{\"x\":1}, \"\", \"[\", \",\", \"]\", 123.4, false, null]}"));
		assertEq("", jtest("[\n" +
"  [\"\", \"[\", \",\", \"]\", \"xyz\", 123.4, false, null, \"ab \",\n"+
"     {\"x\":1}],\n" +
"  [0, 0, 1],\n" +
"  [-5, 33, 0.5]\n" +
"]"));
		assertEq("", jtest("{ \"a\":[\n" +
"  [\"\", \"[\", \",\", \"]\", \"xyz\", 123.4, false, null, \"ab \",\n"+
"     {\"x\":1}],\n" +
"  [0, 0, 1],\n" +
"  [-5, 33, 0.5]\n" +
"]}"));
		assertEq("", jtest(
"{\"Seznam\":\n"+
" [\n" +
"    { \"Osoba\":{\n" +
"        \"Jmeno\":\"Václav Novák\",\n" +
"        \"Plat\":12345,\n" +
"        \"Nar.\":\"1980-11-07\"\n" +
"      }\n" +
"    },\n" +
"    { \"Osoba\":{\n" +
"        \"Jmeno\":\"Ivan Bílý\",\n" +
"        \"Plat\":23450,\n" +
"        \"Nar.\":\"1977-01-17\"\n" +
"      }\n" +
"    },\n" +
"    { \"Osoba\":{\n" +
"        \"Jmeno\":\"Karel Kuchta\",\n" +
"        \"Plat\":1340,\n" +
"        \"Nar.\":\"1995-10-06\"\n" +
"      }\n" +
"    }\n" +
"  ]\n" +
"}"));
			assertEq("", jtest("{\"a:b\":{\"xmlns:a\" : \"a.b.cz\"}}"));
		} catch (Exception ex) {fail(ex);}
////////////////////////////////////////////////////////////////////////////////
		try {
			String dataDir =
				"D:/cvs/DEV/java/xdef31/test/resources/test/xdef/data/json/";
			File[] files = SUtils.getFileGroup(dataDir + '*' + ".json");
			for(File f: files) {
				assertEq("", jtest(f.getAbsolutePath()));
			}
			dataDir =
				"D:/cvs/DEV/java/xdef31/test/resources/test/common/json/data/";
			files = SUtils.getFileGroup(dataDir + '*' + ".json");
			for(File f: files) {
				assertEq("", jtest(f.getAbsolutePath()));
			}
			files = SUtils.getFileGroup(dataDir + '*' + ".xml");
			for(File f: files) {
				assertEq("", xtest(f.getAbsolutePath()));
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"root\" >\n" +
"  <xd:json xd:name='root'>\n"+
"     \"jvalue();\"\n"+
"  </xd:json>\n"+
"</xd:def>\n" +
"";
			xp = compile(xdef);
			o = 123;
			assertEq(o, jparse(xp, "", (Object) o, reporter));
			o = null;
			assertNull(jparse(xp, "", (Object) o, reporter));
		} catch (Exception ex) {fail(ex);}
	}

	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}