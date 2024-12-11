package bugreports;

import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Generate JSON model -> JSON, and JSON -> JSON model.
 * @author Vaclav Trojan
 */
public final class GenX extends XDTester {

	public GenX() {super();}

	private static String test(final String json) {
		try {
			String s = GenXJsonModelToJson.parse(json, "STRING"); // to JSON conversion
			XonUtils.parseXON(s);// just test syntax
			String t = GenXJsonToJsonModel.parse(s, "STRING");
			String u;
			if (!(u=json).equals(t=t)) {
				GenXJsonModelToJson.parse(t, "STRING"); // test re-converted result
				int i = 0;
				StringBuilder sb = new StringBuilder();
				for (; i < t.length() && i < u.length(); i++) {
					char c;
					if ((c = u.charAt(i)) == t.charAt(i)) {
						sb.append(c);
					} else {
						int j = i;
						for (j = i; j >= 0 && u.charAt(j) != '\n'; j--) {}
						if (j < 0) {
							j = 0;
						} else {
							j++;
						}
						int k = i;
						int len = u.length();
						if (len > t.length()) len = t.length();
						for (; k < len; k++) {
							if (u.charAt(k) == '\n') {
								break;
							}
						}
						sb.append(u.substring(i,k)).append('\n');
						sb.append(t.substring(j,k)).append('\n');
						for (int n = j; n < i; n++) {
							sb.append('.');
						}
						sb.append('|');
						for (int n = i+1; n < len-1 && u.charAt(n) != '\n'; n++) {
							sb.append('.');
						}
						sb.append('\n');
						return sb.toString();
					}
				}
			}
			return "";
		} catch (Exception ex) {
			return printThrowable(ex);
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		assertEq("", test("\"int\""));
		assertEq("", test("%anyObj"));
		assertEq("", test("%anyObj=\"int()\""));
		assertEq("", test("[%anyObj=\"*;\"]"));
		assertEq("", test(" { %anyName: %anyObj=\"*;\" } "));
		assertEq("", test("{\"Genre\":[%oneOf,\"string()\",[\"occurs *; string()\"]]}"));
		assertEq("", test("[\n   [ %script = \"occurs 3\", \"occurs 3 jvalue()\" ]\n]"));
		assertEq("", test(" { %anyName: %anyObj=\"*;\" } "));
		assertEq("", test(
			"{%anyName:[%oneOf,[\"* jvalue();\" ],{%anyName:[%oneOf=\"ref test\"]},\"jvalue();\"]}"));
		assertEq("", test(
			"{%anyName:[%oneOf,[\"* jvalue();\" ],{%anyName:[%oneOf =\" ref test\"]},\"jvalue();\"]}"));
		assertEq("", test("{%anyName:[%oneOf,\"string()\",[\"occurs *; string()\"]]}"));
		assertEq("", test(
			"/** x */{%oneOf=\"optional;\",\"manager\":\"string()\",\"subordinates\":[\"* int();\"]}"));
		assertEq("", test(
			"/*x*/\n[\"? jvalue\",{%script =\"+\",I:[%script=\"?\",[%script=\"*\",\"* int\"]]}]\n# y"));
		assertEq("", test(
"{ \"cities\"  : [\n" +
"    {%script = \"occurs 1..*\",\n" +
"      \"from\": [\n" +
"         \"string()\",\n" +
"         {%script = \"occurs 1..*\", \"to\": \"jstring()\", \"distance\": \"int()\" }\n" +
"	  ]\n" +
"    }\n" +
"  ]\n" +
"}"));
		assertEq("", test(
"  [%oneOf,\n"+
"    \"jvalue();\",\n"+
"    [\"* jvalue();\" ],\n"+
"    {%anyName:\n"+
"       [%oneOf,\n"+
"         \"jvalue();\",\n"+
"         [\"* jvalue();\" ],\n"+
"         {%anyName: [\n"+
"/* *?      [%oneOf= \"ref test\"], /* */\n"+
"           [%oneOf=\"ref test\"]\n"+
"         ]}\n"+
"       ]\n"+
"    }\n"+
"  ]"));
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