package mytests;

import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Generate JSON model -> JSON, and JSON -> JSON model.
 * @author Vaclav Trojan
 */
public final class GenX extends XDTester {

	public GenX() {super();}

	private String test(final String json) {
		String s = null, t = null;
		try {
			s = GenXJsonModelToJson.parse(json, "STRING"); // to JSON conversion
			XonUtils.parseXON(s);// just test syntax
			t = GenXJsonToJsonModel.parse(s, "STRING");
			String u;
			if (!(u=json).equals(t)) {
				GenXJsonModelToJson.parse(t, "STRING"); // test re-converted result
				int i = 0;
				StringBuilder sb = new StringBuilder();
				for (; i < t.length() && i < u.length(); i++) {
					char c;
					if ((c = u.charAt(i)) == t.charAt(i)) {
						sb.append(c);
					} else {
						int j;
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
		} catch (RuntimeException ex) {
			return ex.getMessage() + "\n"
				+ (s == null ? "json\n" + json : t == null ? "s\n" + s : "\nt\n" + t);
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
//		assertEq("", test("/*www*/%anyObj/*xx*/=/*yy*/\"int()\"/*zz*/"));
//		assertEq("", test("/* */[ %oneOf= \"ref test\" ]/* */\n"));
//if(true)return;
		assertEq("", test("\"int\""));
		assertEq("", test("#xxx\n\"int\"\n#xxx"));
		assertEq("", test("/*xxx*/\n\"int\"/*xxx*/"));
		assertEq("", test("%anyObj"));
		assertEq("", test("#xxx\n%anyObj\n#xxx"));
		assertEq("", test("/*xxx*/%anyObj/*xxx*/"));
		assertEq("", test("%anyObj=\"int()\""));
		assertEq("", test("/*www*/%anyObj/*xx*/=\"int()\"/*zz*/"));
		assertEq("", test("#www\n%anyObj#xx\n=\"int()\"#zz"));
		assertEq("", test("[%anyObj=\"*;\"]"));
		assertEq("", test(" { %anyName: %anyObj=\"*;\" } "));
		assertEq("", test("{\"Genre\":[%oneOf,\"string()\",[\"occurs *; string()\"]]}"));
		assertEq("", test("/*xx*/\n[\n   [ %script = \"occurs 3\", \"occurs 3 jvalue()\" ]\n]\n/*xx*/"));
		assertEq("", test(" { %anyName: %anyObj=\"*;\" } "));
		assertEq("", test("{%anyName:[%oneOf,[\"* jvalue\"],{%anyName:[%oneOf=\"ref test\"]},\"jvalue\"]}"));
		assertEq("", test("{%anyName:[%oneOf,[\"* int\" ],{%anyName:[%oneOf =\" ref test\"]},\"jvalue\"]}"));
		assertEq("", test("{%anyName:[%oneOf,\"string()\",[\"occurs *; string()\"]]}"));
		assertEq("", test("{%oneOf=\"optional;\",\"manager\":\"string()\",\"subordinates\":[\"* int();\"]}"));
		assertEq("", test("[\"? int\",{%script =\"+\",I:[%script=\"?\",[%script=\"*\",\"* int\"]]}]\n# y"));
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
"#xxx\n"+
"[ %oneOf\t,\n" +
"    \"jvalue();\"\t,\n" +
"    [ \t\"* jvalue();\" ],\n" +
"    { %anyName\t:\n" +
"       [\t%oneOf,\n" +
"         \"jvalue();\",\n" +
"         [ \"* jvalue();\" ],\n" +
"         { %anyName: [\n" +
"             [ %oneOf=\"ref test\" ]\n" +
"           ]\n" +
"         }\n" +
"       ]\n" +
"    }\n" +
"]\n" +
"#yyy"));
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