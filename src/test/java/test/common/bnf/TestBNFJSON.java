package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import java.io.File;
import builtools.STester;

/** Test of BNF of JSON.
 * @author Vaclav Trojan
 */
public class TestBNFJSON extends STester {

	public TestBNFJSON() {super();}

	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			if (grammar.parse(p, name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (p.eos()?
					"eos" : p.getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "Exception " + ex;
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			BNFGrammar grammar;
			grammar = BNFGrammar.compile(null,
				new File(getDataDir() + "TestJSON.bnf"), null);
//			grammar.display(System.out, true);

////////////////////////////////////////////////////////////////////////////////
			String s;
			s = "{}";
			assertEq(s, parse(grammar, "value", s));
			s ="1";
			assertEq(s, parse(grammar, "value", s));
			s ="-2";
			assertEq(s, parse(grammar, "value", s));
			s ="3.333";
			assertEq(s, parse(grammar, "value", s));
			s ="4e17";
			assertEq(s, parse(grammar, "value", s));
			s ="\"abc\"";
			assertEq(s, parse(grammar, "value", s));
			s ="\"x\\n\"";
			assertEq(s, parse(grammar, "value", s));
			s ="null";
			assertEq(s, parse(grammar, "value", s));
			s ="true";
			assertEq(s, parse(grammar, "value", s));
			s ="false";
			assertEq(s, parse(grammar, "value", s));
			s = "[]";
			assertEq(s, parse(grammar, "value", s));
			s = "[1, -2, 3.333, 4e17, \"abc\", \"x\\n\", null]";
			assertEq(s, parse(grammar, "value", s));
			s = "[1, -2, 3.333, 4e17, \"abc\", \"x\\n\", null," +
				"[2.1, 2.2, [\"2.2.1\"]], \"key\":\"value\"]";
			assertEq(s, parse(grammar, "value", s));
			s = "'a': 'b'";
			assertEq(s, parse(grammar, "value", s));
			s = "{}";
			assertEq(s, parse(grammar, "value", s));
			s = "{ 'a': [1, null, 'a': 'b', {'c': []}]}";
			assertEq(s, parse(grammar, "value", s));

			s = "[ 1, -2,3.333, 4e17, \"abc\", \"x\\n\", null,"+
				"[2.1, 2.2, [\"2.2.1\" ]], false, true,\"\","+
				"\"key\": \"value\", 'abc\"def' : [ ], [],"+
				"{'x':{'a':'b'}}, {}]";
			assertEq(s, parse(grammar, "value", s));

			s = "[ 1, -2,3.333, 4e17, \"abc\", \"x\\n\", null,"+
				"[2.1, 2.2, [\"2.2.1\" ] ], false,true,\"\","+
				"\"key\" : \"value\", 'abc\"def' : [], [],"+
				"{'x':{'a':'b'}}, []]";
			assertEq(s, parse(grammar, "value", s));
			s = "[1,-2,3.333,4.0e+17,\"abc\","+
				"\"\\u00e1\\n\",null,[2.1,2.2,[\"2.2.1\"]],false,true,\"\","+
				"\"key\":\"value\",\"abc\\\"def\":[]]";
			assertEq(s, parse(grammar, "value", s));
			s = "{\"0\":1,\"1\":-2,\"2\":3.333,\"3\":4.0e+17,\"4\":\"abc\","+
				"\"5\":\"\\u00e1\\n\",\"6\":null,\"7\":[2.1,2.2,[\"2.2.1\"]],"+
				"\"8\":false,\"9\":true,\"10\":\"\",\"key\":\"value\","+
				"\"abc\\\"def\":[]}";
			assertEq(s, parse(grammar, "value", s));
			s =
"\"root\": {\n"+
"  \"glossary\": {\n"+
"     \"title\": \"example glossary\",\n"+
"     \"GlossDiv\": {\n"+
"        \"title\": \"S\",\n"+
"        \"GlossList\": {\n"+
"           \"GlossEntry\": {\n"+
"              \"ID\": \"SGML\",\n"+
"              \"SortAs\": \"SGML\",\n"+
"              \"GlossTerm\": \"Standard Generalized Markup Language\",\n"+
"              \"Acronym\": \"SGML\",\n"+
"              \"Abbrev\": \"ISO 8879:1986\",\n"+
"              \"GlossDef\": {\n"+
"                 \"para\": \"A meta-markup used to create DocBook.\",\n"+
"                 \"GlossSeeAlso\": [\n"+
"                    \"GML\",\n"+
"                    \"XML\"\n"+
"                 ]\n"+
"              },\n"+
"              \"GlossSee\": \"markup\"\n"+
"           }\n"+
"        }\n"+
"     }\n"+
"  }\n"+
"}";
			assertEq(s, parse(grammar, "value", s));
			s =
"[\n"
+ "\t1,\n"
+ "\t-2,\n"
+ "\t3.333,\n"
+ "\t4.0e+17,\n"
+ "\t\"abc\",\n"
+ "\t\"\\u00e1\\n\",\n"
+ "\tnull,\n"
+ "\t-12.3e+5,\n"
+ "\t[\n"
+ "\t\t2.1,2.2,\n"
+ "\t\t[\"2.2.1\"]\n"
+ "\t],\n"
+ "\tfalse\t,\n"
+ "\ttrue,\n"
+ "\t\"\",\n"
+ "\t{\"key\":\"value\",\n"
+ "\t\"abc\\\"def\":[]}\n"
+ "]\n";
			assertEq(s, parse(grammar, "value", s));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
