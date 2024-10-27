package test.common.bnf;

import java.io.File;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.FUtils;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SException;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;
import org.xdef.sys.SUtils;

/** Test of BNF of JSON.
 * @author Vaclav Trojan
 */
public class TestBNFJSON extends STester {

	public TestBNFJSON() {super();}

	private String getXonDir() {
		String s = (new File(getDataDir()).getAbsolutePath()
			+ File.separator).replace('\\', '/');
		int ndx = s.indexOf("/bnf/");
		return s.substring(0, ndx) + "/xon/data/";
	}

	/** Check correct data.
	 * @param x array with data items.
	 * @param g BNF grammar.
	 * @param rule rule name.
	 */
	private void test(final String[] x, final BNFGrammar g, final String rule) {
		for (String t : x) {
			String s = parse(g, rule, t);
			if (!t.equals(s)) {
				fail("E:\n" + s + "\n" + t);
			}
		}
	}

	/** Check files with correct data.
	 * @param x array with data files.
	 * @param g BNF grammar.
	 * @param rule rule name.
	 */
	private void test(final File[] x, final BNFGrammar g, final String rule) {
		for (File f: x) {
			try {
				String name = f.getName();
				int ndx = name.indexOf("UTF");
				String t = name.equals("Test201windows-1250.xon")
					? FUtils.readString(f, "windows-1250")
					: ndx > 0
						? FUtils.readString(f, name.substring(ndx, name.indexOf(".xon")))
						: FUtils.readString(f, "UTF-8");
				String s = parse(g, rule, t);
				if (!t.equals(s)) {
					fail("E: " + f.getName() + "\n" + s + "\n" + t);
				}
			} catch (SException ex) {
				fail("E: " + f.getName() + "\n" + ex.getMessage());
			}
		}
	}

	/** Test recognize incorrect data.
	 * @param x array with data items.
	 * @param g BNF grammar.
	 * @param n rule name.
	 */
	private void testErr(final String[] x, final BNFGrammar g, final String n) {
		for (String t : x) {
			String s = parse(g, n, t);
			if (t.equals(s)) {
				fail("Error not recognized:\n" + s + "\n" + t);
			}
		}
	}

	/** Parse data with a rule from BNF grammar.
	 * @param g BNF grammar.
	 * @param name name of rule in BNF grammar.
	 * @param s string with data.
	 * @return parsed part of data or error message.
	 */
	private String parse(final BNFGrammar g, final String name, final String s){
		StringParser p = new StringParser(s);
		if (g.parse(p, name)) {
			if (g.getParser().errorWarnings()) {
				ReportWriter rw = g.getParser().getReportWriter();
				return rw.getReportReader().printToString();
			}
			return g.getParsedString();
		} else {
			return name + " failed, " + (p.eos()? "eos" : p.getPosition().toString()) + ";";
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		String s;
		BNFGrammar g;
		try { // JSON
			g = BNFGrammar.compile(null, new File(getDataDir() + "TestJSON.bnf"), null);
//			grammar.display(System.out, true);

////////////////////////////////////////////////////////////////////////////////
			test(new String[] {
				"null",
				"false",
				"1",
				"-2",
				"3.333",
				"4e17",
				"-3.14e+10",
				"14e-10",
				"\"\"",
				"\"a\"",
				"\"abc\"",
				"\"x\\n\"",
				"false",
				"\"\\u0045\\n\\t\\\\\\\"\\b\"",
				"[]",
				"{}",
				"[ null ]",
				"[1e-3]",
				"[ 1.5, true, \"\", [[]], {} ]",
				"{\"a\" : 1e2}",
				"{ \"a\" :{} }",
				"{\"a\":{},\"_b\":[] , \"c_1\":null}",
				"[1, -2, 3.333, 4e17, \"abc\", \"x\\n\", null]",
				"[1, -2, 3.333, 4e17, \"abc\", \"x\\n\", null, [2.1, 2.2, [\"2.2.1\"]]]",
				"{ \"a\": [1, null], \"c\": []}",
				"[1,-2,3.333,4.0e+17,\"abc\", \"\\u00e1\\n\", \"\", \"abc\\\"def\"]",
				"[ 1, -2,3.333, 4e17, \"abc\", \"x\\n\", null," +
					"[2.1, 2.2, [\"2.2.1\" ]], false, true,\"\", [], {\"x\":{}}]",
				"{\"0\":1,\"1\":-2,\"2\":3.333,\"3\":4.0e+17,\"4\":\"abc\","+
					"\"5\":\"\\u00e1\\n\",\"6\":null,\"7\":[2.1,2.2,[\"2.2.1\"]],"+
					"\"8\":false,\"9\":true,\"10\":\"\",\"key\":\"value\","+
					"\"abc\\\"def\":[]}",
			}, g, "json");
			test(SUtils.getFileGroup(getXonDir() + "Test*.json"), g, "json");
			// test incorrect JSON data
			testErr(new String[] {
				"aABB",
				"\"\\A\"",
				"\"\\u0xyz\"",
				"[1,2,]",
				"{\"\":1,}",
				"[01.1]",
				"[01]",
				"[1.]",
				"[.1]"}, g, "json");
			s =
"{ \"root\": {\n"+
"   \"glossary\": {\n"+
"     \"title\": \"example glossary\",\n"+
"     \"GlossDiv\": {\n"+
"       \"title\": \"S\",\n"+
"       \"GlossList\": {\n"+
"         \"GlossEntry\": {\n"+
"             \"ID\": \"SGML\",\n"+
"             \"SortAs\": \"SGML\",\n"+
"             \"GlossTerm\": \"Standard Generalized Markup Language\",\n"+
"             \"Acronym\": \"SGML\",\n"+
"             \"Abbrev\": \"ISO 8879:1986\",\n"+
"             \"GlossDef\": {\n"+
"               \"para\": \"A meta-markup used to create DocBook.\",\n"+
"               \"GlossSeeAlso\": [\n"+
"                 \"GML\",\n"+
"                 \"XML\"\n"+
"               ]\n"+
"             },\n"+
"             \"GlossSee\": \"markup\"\n"+
"         }\n"+
 "      }\n"+
"     }\n"+
"   }\n"+
"  }\n"+
"}";
			assertEq(s, parse(g, "json", s));
			s =
"[\n" +
"	1,\n" +
"	-2,\n" +
"	3.333,\n" +
"	4.0e+17,\n" +
"	\"abc\",\n" +
"	\"\\u00e1\\n\",\n" +
"	null,\n" +
"	-12.3e+5,\n" +
"	[\n" +
"		2.1,2.2,\n" +
"		[\"2.2.1\"]\n" +
"	],\n" +
"	false\t,\n"+
"	true,\n" +
"	\"\",\n" +
"	{\"key\":\"value\",\n" +
"		\"abc\\\"def\":[]\n" +
"	}\n" +
"]\n";
			assertEq(s, parse(g, "json", s));
		} catch (RuntimeException ex) {fail(ex);}
		try { // XON
			g = BNFGrammar.compile(null, new File(getDataDir() + "TestJSON_XON.bnf"), null);
			test(new String[] {
				"\"true\"",
				"null",
				"1l",
				"-1.25e-3f",
				"0N",
				"-3e-5D",
				// string
				"\"\"",
				"\"\\u0045\"", // string with UTFChar
				"\"\\n\\t\\\\\\\"\\b\"", // esccaped chars
				"d1949-11-07", // date
				"d1949-11-07T15:59:00Z",  // dateTime
				"d1949-11-07T15:59:01.123",
				"d1949-11-07T15:59:01.123-02:00",
				"d1945 ", //* GYear *
				"d1945Z",
				"d194+00:00",
				"d---29", //* GDay *
				"d---29-02:00",
				"d--05", //* GMonth *
				"d--12-02-02:00",
				"d--12-29 ", //* GMonthDay *
				"d--12-29-02:00",
				"d11:20:00 ", //* time *
				"d11:20:31",
				"d11:20:31.123",
				"d11:20:31.123Z",
				"d11:20:31.123-02:00",
				"d11:20:31+02:00",
				// Duration
				"P2Y6M5DT12H35M30S",
				"P1Y2M3DT10H30M123.123456S",
				"-P1DT2H",
				"P1Y1M1DT1H1M1.1234567S",
				// Email address
				"e\"a@b\"",
				"e\"a@b (A. Bc)\"",
				"e\"A. Bc <x-y.z@cd.ef>\"",
				// bytes
				"b()",
				"b( a b\n\tc = )",
				// GPS
				"g(1,0)",
				"g(1.5, -3, -5)",
				"g(1.5, -3, -5, Lon)",
				"g(1.5, -3, -5, \"a b\")",
				"g(0, 0, Lon)",
				"g(-0, 0, \"a b\")",
				"g(0, 0, Lon3, city/Center)",
				// Currency
				"C(CZK)",
				"C(USD)",
				// Price
				"p(12 CZK)",
				"p(0.0 USD)",
				// Character
				"c\" \"",
				"c\"\\u0045\"",
				// URI
				"u\"https://org.xdef/ver1\"",
				// InetAddr
				"/0.00.000.038",
				"/129.255.0.99",
				"/0:0:0:0:0:0:0:0",
				"/FABC:0:01:002:0008:8000:000C:41DE",
				"/fabc:0:01:002:0008:8000:000C:41de",
				// Complex values
				"{}",
				"{\"\":\"\"}",
				"{\"A B\":{\"a b\":\"\"}}",
				"[]",
				"[[]]",
				"[[{}]]",
				"[[[]]]",
				"[[[1b]]]",
				"[[],[]]",
				"[[1d,2D],[null,4s]]",
				"[{a:{b:1}}]",
				"[{a:[]}]",
				"[{a:[1]}]",
				"[{a:[[]]}]",
				"[{a:[{}]}]",
				"[{a:[{a:1}]}]",
				"[1, -1.25e-3, true, \"abc\", null]",
				"{ a:{}, _b:[], c_:null }",
				"[[3,null,false],[3.14,\"\",false],[\"aa\",true,false]]",
				"[1, { _x69_:1, _x5f_map:null, \"a_x\tb\":[null], item:{}}]",
				"[1, {_a._b:1,b-c:\"a\",\"\":false,array:[],map:{}},\"abc\"]",
				"[{a:[{},[1,2],{},[3,4]]}]",
				"[{a:[{a:1},[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{a:1},[30,4]]}]",
				"[{a:[[1,2],[3,4],{}]}]",
				"[{a:[[1,2],[3,4],{a:1,b:2}]}]"}, g, "xon");
			test(SUtils.getFileGroup(getXonDir() + "Test*.xon"),  g, "xon");
			// test incorrect XON data
			testErr(new String[] { // test XON incorrect data
				"aABB",
				"\"\\A\"",
				"\"\\u0xyz\"",
				"[1,2,]",
				"{\"\":1,}",
				"[01.1]",
				"[01]",
				"[1.]",
				"[.1]",
				"d--01-",
				"d11:20:31.123-020",
				"P",
				"P1.1Y",
				"PT+1H"}, g, "xon");
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
