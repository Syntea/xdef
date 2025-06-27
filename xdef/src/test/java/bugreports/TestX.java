package bugreports;

import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import java.util.Properties;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** Tests used for development..
 * @author Vaclav Trojan
 */
public class TestX extends XDTester {

	public TestX() {super();}

	public static boolean x(XXData x) {return true;}

	/** Run test and display error information. */
	@Override
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		XDPool xp;
		XDDocument xd;
		String json;
		String xdef;
		Properties props = new Properties();
		ArrayReporter reporter = new ArrayReporter();
		try {
			System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"[\n" +
"  { %script= \"occurs 1..*\",\n" +
"    \"Name\": \"string()\",\n" +
"    \"Genre\": [ %oneOf,\n" +
"      \"string()\",\n" +
"       [\"occurs 1..* string()\"]\n" +
"    ]\n" +
"  }\n" +
"]\n" +
" </xd:json>\n"+
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
"  {\n" +
"    \"Name\": \" cc dd \",\n" +
"    \"Genre\": \"C1\"\n" +
"  }\n" +
"]";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"{ %oneOf= \"optional;\",\n" +
"  \"manager\": \"string()\",\n" +
"  \"subordinates\":[ \"* string();\" ]\n" +
"}\n" +
" </xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "{\"manager\": \"BigBoss\"}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			json = "{\"subordinates\": []}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			json = "{\"subordinates\": [\"first\", \"second\"]}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			json = "{}";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"[\n" +
"  {\n" +
"    \"A\": [%oneOf= \"occurs *\", \"string()\", [\"occurs 1..* string()\"]]\n"+
"  }\n" +
"]\n" +
" </xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "[\n" +
"  {\"A\": [\"A1\"]},\n" +
"  {\"A\": [\"B1\", \"B2\"]},\n" +
"  {\"A\": \"C1\"}\n" +
"]";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			json = "[]";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"   { \"date\" : \"date()\",\n" +
"     \"cities\"  : [\n" +
"       { %script= \"occurs 1..*\",\n" +
"         \"from\": [\n" +
"           \"string()\",\n" +
"           {%script= \"*\", \"to\":\"jstring()\", \"dist\":\"int()\"}\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"  }\n" +
" </xd:json>\n"+
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
			assertNoErrorwarnings(reporter);
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"  {\n" +
"    \"A\":  \"string()\",\n" +
"    \"B\":  \"string()\",\n" +
"  }\n" +
" </xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "{ \"A\": \"a\", \"B\": \"b\" }";
			reporter.clear();
			xd.jparse(json, reporter);
			assertNoErrorwarnings(reporter);
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}