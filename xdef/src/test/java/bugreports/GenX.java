package bugreports;

import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Generate JSON model -> JSON, and JSON -> JSON model.
 * @author Vaclav Trojan
 */
public final class GenX extends XDTester {

	public GenX() {super();}

	private static void test(final String json) {
		String s = GenXJsonModelToJson.parse(json, "STRING"); // to JSON conversion
		XonUtils.parseXON(s.trim());// just test syntax
		String t = GenXJsonToJsonModel.parse(s, "STRING");
		if (!json.trim().equals(t.trim())) { //????
			System.out.println("***\n" + json);
			System.out.println("***\n" + s);
			System.out.println("***\n" + t);
			GenXJsonModelToJson.parse(t, "STRING"); // test re-converted result
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		test(" { %anyName: %anyObj=\"*;\" } ");
		test("{\"Genre\":[%oneOf,\"string()\",[\"occurs *; string()\"]]}");
		test("[\n   [ %script = \"occurs 3\", \"occurs 3 jvalue()\" ]\n]");
		test(" { %anyName: %anyObj=\"*;\" } ");
		test("{%anyName:[%oneOf,[\"* jvalue();\" ],{%anyName:[%oneOf=\"ref test\"]},\"jvalue();\"]}");
		test("{%anyName:[%oneOf,[\"* jvalue();\" ],{%anyName:[%oneOf =\" ref test\"]},\"jvalue();\"]}");
		test("{%anyName:[%oneOf,\"string()\",[\"occurs *; string()\"]]}");
		test("/** Test */{%oneOf=\"optional;\",\"manager\":\"string()\",\"subordinates\":[\"* int();\"]}");
		test("/*x*/\n[\"? jvalue\",{%script =\"+\",I:[%script=\"?\",[%script=\"*\",\"* int\"]]}]\n# y");
		test(
"[%oneOf,\n"+
"    \"jvalue();\",\n"+
"    [\"* jvalue();\" ],\n"+
"    {%anyName:\n"+
"       [%oneOf,\n"+
"         \"jvalue();\",\n"+
"         [\"* jvalue();\" ],\n"+
"         {%anyName: [%oneOf =\" ref test\"]}\n"+
"       ]\n"+
"    }\n"+
"]");
		test(
"{ \"cities\"  : [\n" +
"    {%script = \"occurs 1..*\",\n" +
"      \"from\": [\n" +
"         \"string()\",\n" +
"         {%script = \"occurs 1..*\", \"to\": \"jstring()\", \"distance\": \"int()\" }\n" +
"	  ]\n" +
"    }\n" +
"  ]\n" +
"}");
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}
}