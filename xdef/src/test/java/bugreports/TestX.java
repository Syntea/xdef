package bugreports;

import java.util.List;
import java.util.Map;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import java.util.Properties;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Tests used for development..
 * @author Vaclav Trojan
 */
public class TestX extends XDTester {

	public TestX() {super();}

	public static boolean x(XXData x) {return true;}

	/** Display object. */
	private static String printObject(final Object o) {
		if (o == null) {
			return "null\n";
		} else if (o instanceof List) {
			List x = (List) o;
			String s = "[\n";
			for (int i = 0; i < x.size(); i++) {
				s += "  index " + i + ": " + printObject(x.get(i));
			}
			return s + "]\n";
		} else if (o instanceof Map) {
			String s = "{\n";
			for (Object x: ((Map) o).entrySet()) {
				s += "\n  " + ((Map.Entry) x).getKey() + ": "
				  + printObject(((Map.Entry) x).getValue());
			}
			return s + "}\n";
		} else {
			return o + "; " + o.getClass() + "\n";
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		XDPool xp;
		XComponent xc;
		XDDocument xd;
		String json;
		Object o, x;
		String xdef;
		Properties props = new Properties();
		ArrayReporter reporter = new ArrayReporter();
		try {
			props.setProperty("xdef-debug", "showXonModel");
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
"<xd:component>%class test.xdef.Xona %link a</xd:component>\n"+
" <xd:xon name='a'>\n"+
"[\n" +
"  [ $script= \"optional\", \"boolean();\", \"optional int();\" ]\n" +
"]\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument();
			json = "[ [ true, 123 ] ]";
			reporter.clear();
			o = xd.jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			if (!XonUtils.xonEqual(o, (x=XComponentUtil.toXon(xc)))) {
				fail(printObject(o)
					+ "\n***\n" + printObject(x));
			}
			json = "[\n]";
			o = xd.jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			if (!XonUtils.xonEqual(o, (x=XComponentUtil.toXon(xc)))) {
				fail(printObject(o)
					+ "\n***\n" + printObject(x));
			}
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:xon name='a'>\n"+
"[\n" +
"  { $script= \"occurs 1..*\",\n" +
"    \"Name\": \"string()\",\n" +
"    \"Genre\": [ $oneOf,\n" +
"      \"string()\",\n" +
"       [\"occurs 1..* string()\"]\n" +
"    ]\n" +
"  }\n" +
"]\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "[\n" +
"  {\n" +
"    \"Name\": \"A\",\n" +
"    \"Genre\": [\"A1\"]\n" +
"  },\n" +
"  {\n" +
"    \"Name\": \"B\",\n" +
"    \"Genre\": [\"B1\", \"B2\"]\n" +
"  },\n" +
//"  {\n" +
//"    \"Name\": \" cc dd \",\n" +
//"    \"Genre\": \"C1\"\n" +
//"  }\n" +
"]";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:xon name='a'>\n"+
"{ $oneOf= \"optional;\",\n" +
"  \"manager\": \"string()\",\n" +
"  \"subordinates\":[ \"* string();\" ]\n" +
"}\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "{\"manager\": \"BigBoss\"}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
			json = "{\"subordinates\": []}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
			json = "{\"subordinates\": [\"first\", \"second\"]}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
			json = "{}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:xon name='a'>\n"+
"[\n" +
"  {\n" +
"    \"A\": [$oneOf= \"occurs *\", \"string()\", [\"occurs 1..* string()\"]]\n"+
"  }\n" +
"]\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "[\n" +
"  {\"A\": [\"A1\"]},\n" +
"  {\"A\": [\"B1\", \"B2\"]},\n" +
//"  {\"A\": \"C1\"}\n" +
"]";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
			json = "[]";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
// Required element 'js:item' is missing; path=$; X-position=Example#test/$.['date']
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:xon name='a'>\n"+
"   { \"date\" : \"date()\",\n" +
"     \"cities\"  : [\n" +
"       { $script= \"occurs 1..*\",\n" +
"         \"from\": [\n" +
"           \"string()\",\n" +
"           {$script= \"*\", \"to\":\"jstring()\", \"dist\":\"int()\"}\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"  }\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json =
"{ \"date\" : \"2020-02-22\",\n" +
"\"cities\" : [ \n" +
" {\"from\": [\"Brussels\",\n" +
"   {\"to\":\"London\",\"dist\":322},{\"to\":\"Paris\",\"dist\":265}\n" +
"  ]\n" +
" },\n" +
" {\"from\": [\"London\",\n" +
"   {\"to\":\"Brussels\",\"dist\":322},{\"to\":\"Paris\",\"dist\":344}\n" +
"  ]\n" +
" }\n" +
"]\n" +
"}";
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:xon name='a'>\n"+
"  {\n" +
"    \"A\":  \"string()\",\n" +
"    \"B\":  \"string()\",\n" +
"  }\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "{ \"A\": \"a\", \"B\": \"b\" }";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}