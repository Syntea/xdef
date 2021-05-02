package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import java.util.Properties;
import test.XDTester;

/** Tests used for development..
 * @author Vaclav Trojan
 */
public class TestX extends XDTester {

	public TestX() {super();}

	public static boolean x(XXData x) {return true;}

	@Override
	/** Run test and display error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		String json;
		String xdef;
		Properties props = new Properties();
	try {
/**/
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"[\n" +
"  [\n" +
"     $script: \"optional\",\n" +
"     \"boolean();\", \n" +
"     \"optional int();\"\n" +
"  ]\n" +
"]\n" +
" </xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "[\n" +
"  [\n" +
"    true,\n" +
"    123\n" +
"  ]\n" +
"]";
			xd.jparse(json, null);
			json = "[\n" +
"]";
			xd.jparse(json, null);
/**/
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"[\n" +
"  { $script: \"occurs 1..*\",\n" +
"    \"Name\": \"string()\",\n" +
"    \"Genre\": [ $oneOf,\n" +
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
			xd.jparse(json, null);
/**/
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"{\n" +
"  $oneOf: \"optional;\",\n" +
"  \"manager\": \"string()\",\n" +
"  \"subordinates\":[ \"* string();\" ]\n" +
"}\n" +
" </xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json = "{\"manager\": \"BigBoss\"}";
			xd.jparse(json, null);
			json = "{\"subordinates\": []}";
			xd.jparse(json, null);
			json = "{\"subordinates\": [\"first\", \"second\"]}";
			xd.jparse(json, null);
			json = "{}";
			xd.jparse(json, null);
/**/
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"[\n" +
"  {\n" +
"    \"A\": [$oneOf: \"occurs *\",\n" +
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
"  {\"A\": [\"A1\"]},\n" +
"  {\"A\": [\"B1\", \"B2\"]},\n" +
"  {\"A\": \"C1\"}\n" +
"]";
			xd.jparse(json, null);
			json = "[]";
			xd.jparse(json, null);
/**/
// Required element 'js:item' is missing; path=$; X-position=Example#test/$.['date']
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <xd:json name='a'>\n"+
"   { \"date\" : \"date()\",\n" +
"     \"cities\"  : [\n" +
"       { $script: \"occurs 1..*\",\n" +
"         \"from\": [\n" +
"           \"string()\",\n" +
"           {$script: \"*\", \"to\":\"jstring()\", \"dist\":\"int()\"}\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"  }\n" +
"]\n" +
" </xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			json =
"{ # \"date\" : \"2020-02-22\",\n" +
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
			xd.jparse(json, null);
/**/
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}