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
		String u;
		if (!(u=json.trim()).equals(t=t.trim())) {
			GenXJsonModelToJson.parse(t, "STRING"); // test re-converted result
			int i = 0;
			for (; i < t.length() && i < u.length(); i++) {
				char c;
				if ((c = u.charAt(i)) == t.charAt(i)) {
					System.out.print(c);
				} else {
					int j = i;
					for (j = i; j >= 0; j--) {
						if (u.charAt(j) == '\n') {
							j++;
							break;
						}
					}
					if (j < 0) j = 0;
					int k = i;
					int len = u.length();
					if (len > t.length()) len = t.length();
					for (; k < len; k++) {
						if (u.charAt(k) == '\n') {
							k--;
							break;
						}
					}
					System.out.println(u.substring(i,k));
					for (int n = j; n < i; n++) {
						System.out.print(' ');
					}
					System.out.println('|');
					System.out.println(t.substring(j,k));
					break;
				}
			}
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		try {
			test("\"int\"");
			test("%anyObj");
			test("%anyObj=\"int()\"");
			test("[%anyObj=\"*;\"]");
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
//"         {%anyName: [%oneOf= \"ref test\"]}\n"+
"         {%anyName: [%oneOf = \"ref test\"]}\n"+
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
		} catch (RuntimeException ex) {fail(ex);}
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