package test.common.bnf;

import java.io.File;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import test.XDTester;

/** Test JSON and XON data.
 * @author Vaclav Trojan
 */
public class TestJsonXon extends XDTester {

	public TestJsonXon() {super();}

	private String getXonDir() {
		String s = (new File(getDataDir()).getAbsolutePath()
			+ File.separator).replace('\\', '/');
		int ndx = s.indexOf("/bnf/");
		return s.substring(0, ndx) + "/xon/data/";
		
	}
	
	/** Test incorrect data.
	 * @param x array with data items.
	 * @param g BNF grammar.
	 * @param rule rule name.
	 */
	private void testfail(final String[] x,
		final BNFGrammar g,
		final String rule){
		for (String t : x) {
			String s = parse(g, rule, t);
			if (t.equals(s)) {
				fail("Error not recognized:\n" + s + "\n" + t); 
			}
		}
	}
	
	/** Check correct data.
	 * @param x array with data items.
	 * @param g BNF grammar.
	 * @param rule rule name.
	 */
	private void testx(final String[] x, final BNFGrammar g, final String rule){
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
	private void testx(final File[] x, final BNFGrammar g, final String rule) {
		for (File f: x) {
			try {
				String name = f.getName();
				int ndx = name.indexOf("UTF");
				String t = name.equals("Test201windows-1250.xon")
					? FUtils.readString(f, "windows-1250")
					: ndx > 0 ? FUtils.readString(f,
						name.substring(ndx, name.indexOf(".xon")))
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
		
	private String parse(BNFGrammar grammar, String name, String source) {
		StringParser p = new StringParser(source);
		if (grammar.parse(p, name)) {
			if (grammar.getParser().errorWarnings()) {
				return grammar.getParser().getReportWriter().
					getReportReader().printToString();
			}
			return grammar.getParsedString();
		} else {
			return name + " failed, " + (p.eos()?
				"eos" : p.getPosition().toString()) + ";";
		}
	}

	@Override
	public void test() {
		String bnf;
		BNFGrammar g;
		try {
			bnf =  // JSON
"S            ::= [#9#10#13 ]+ /*whitespace*/\n" +
"IntNumber    ::= $digit ($digit)*\n" +
"SignedInteger::= '-'? ('0' | [1-9] [0-9]*)\n" +
"Exponent     ::= ('e' | 'E') ('+' | '-')? IntNumber\n" +
"FloatPart    ::= '.' IntNumber Exponent? | Exponent\n" +
"FloatNumber  ::= SignedInteger FloatPart\n" +
"Integer      ::= SignedInteger\n" +
"Number       ::= FloatNumber | SignedInteger | 'NaN' | '-' ? 'INF'\n" +
"Null         ::= 'null'\n" +
"Boolean      ::= 'true' | 'false'\n" +
"HexDigit     ::= $digit | [a-fA-F]\n" +
"UTFChar      ::= '\\u' HexDigit {4}\n" +
"AnyChar      ::= $UTFChar - '\\' - '\"'\n" +//any UTF char but not '\\' or '\"'
"OtherChar    ::= UTFChar | AnyChar\n" +
"StringPart   ::= '\\\\' | '\\\"' | '\\n' | '\\r' | '\\t' | '\\f' | '\\b'\n" +
"             | OtherChar\n" +				
"String       ::= '\"' StringPart* '\"'\n" +
"SimpleValue  ::= S? ( Number | String | Boolean | Null )\n" +
"Identifier   ::= ('_' | $letter) ('_' | $letter | $digit)*\n" +
"NamedValue   ::= S? String S? ':' Value\n" +
"Array        ::= S? '[' (Value (S? ',' Value)*)? S? ']'\n" +
"Map          ::= S? '{' (NamedValue (S? ',' NamedValue)* )?  S? '}'\n" +
"Value        ::= Array | Map | SimpleValue\n" + 
"json         ::= Value S?\n";
			g = BNFGrammar.compile(bnf);
			testx(new String[] {
				"null",
				"false",
				"1",
				"-3.14e+10",
				"\"\"",
				"\"a\"",
				"\"\\u0045\\n\\t\\\\\\\"\\b\"",
				"[]",
				"{}",
				"[ null ]",
				"[1e-3]",
				"[ 1.5, true, \"\", [[]], {} ]",
				"{\"a\" : 1e2}",
				"{ \"a\" :{} }",
				"{\"a\":{},\"b\":[] , \"c\":null}"}, g, "json");
			testx(SUtils.getFileGroup(getXonDir() + "Test*.json"), g, "json");
			testfail(new String[] { // test JSON incorrect data
				"aABB",
				"\"\\A\"",
				"\"\\u0xyz\"",
				"[1,2,]",
				"{\"\":1,}",
				"[01.1]",
				"[01]",
				"[1.]",
				"[.1]"}, g, "json");

			bnf = // XON
"WhiteSpace ::= [#9#10#13 ]\n" +
"Comment ::= \"/*\" ([^*]+ | \"*\" - \"*/\")* \"*/\"\n" +
"S ::= (WhiteSpace | Comment)+ /* Sequence of whitespaces or comments */\n" +
"IntNumber    ::= $digit ($digit)*\n" +
"SignedInteger::= '-'? ('0' | [1-9] [0-9]*)\n" +
"Exponent     ::= ('e' | 'E') ('+' | '-')? IntNumber\n" +
"FloatPart    ::= '.' IntNumber Exponent? | Exponent\n" +
"FloatNumber  ::= SignedInteger FloatPart\n" +
"Byte         ::= SignedInteger 'b'\n" +
"Short        ::= SignedInteger 's'\n" +
"Integer      ::= SignedInteger 'i'\n" +
"Long         ::= SignedInteger 'l' ?\n" +
"BigInteger   ::= SignedInteger 'N' \"l\" ?\n" +
"Decimal      ::= SignedInteger 'D' | FloatNumber 'D'\n" +
"Float        ::= SignedInteger 'f' | FloatNumber 'f' | 'NaNf' | '-'? 'INFf'\n"+
"Double       ::= SignedInteger 'd' | FloatNumber 'd'? | 'NaN' | '-'? 'INF'\n" +
"yearFrag     ::= '-'? $digit*\n" +
"monthFrag    ::= ('0' [1-9]) | ('1' [0-2])\n" +
"dayFrag      ::= ('0' [1-9]) | ([12] $digit) | ('3' [01])\n" +
"hourFrag     ::= ([01] $digit) | ('2' [0-3])\n" +
"minuteFrag   ::= [0-5] $digit\n" +
"secondFrag   ::= ([0-5] $digit) ('.' $digit+)?\n" +
"endOfDayFrag ::= '24:00:00' ('.' '0'+)?\n" +
"timezoneFrag ::= 'Z' | ('+' | '-') (('0' $digit | '1' [0-3])\n" +
"             (':' minuteFrag )? | '14:00'\n"+
"             | ('0' $digit | '1' [0-3]) [0-5] $digit | '1400')\n" +
"dateFrag     ::= yearFrag | yearFrag '-' monthFrag ('-' dayFrag)?\n" +
"             | '--' monthFrag | '--' monthFrag '-' dayFrag\n" +
"             | '---' dayFrag\n" +
"/* dateFrag and zone */\n" +
"dateFragZ    ::= yearFrag | yearFrag '-' monthFrag\n" +
"             ('-' dayFrag)? timezoneFrag? | '--' monthFrag timezoneFrag?\n" +
"             | '--' monthFrag '-' dayFrag timezoneFrag?\n" +
"             | '---' dayFrag timezoneFrag?\n" +
"date         ::= 'd' dateFragZ\n" +
"timeFrag     ::= ( (hourFrag ':' minuteFrag ':' secondFrag)\n" +
"             | endOfDayFrag ) | (hourFrag ':' minuteFrag)\n" +
"time         ::= 'd' timeFrag timezoneFrag?\n" +
"dateTime     ::= 'd' dateFrag 'T' (timeFrag | endOfDayFrag) timezoneFrag?\n" +
"duYearFrag   ::= $digit+ 'Y'\n" +
"duMonthFrag  ::= $digit+ 'M'\n" +
"duDayFrag    ::= $digit+ 'D'\n" +
"duHourFrag   ::= $digit+ 'H'\n" +
"duMinuteFrag ::= $digit+ 'M'\n" +
"duSecondFrag ::= $digit+ ('.' $digit+)? 'S'\n" +
"duYMonthFrag ::= (duYearFrag duMonthFrag?) | duMonthFrag\n" +
"duTimeFrag   ::= 'T' ((duHourFrag duMinuteFrag? duSecondFrag?)\n" +
"             | (duMinuteFrag duSecondFrag?) | duSecondFrag)\n" +
"duDayTimeFrag::= (duDayFrag duTimeFrag?) | duTimeFrag\n" +
"Duration     ::= '-'? 'P' ((duYMonthFrag duDayTimeFrag?) | duDayTimeFrag)\n" +
"Null         ::= 'null'\n" +
"Boolean      ::= 'true' | 'false'\n" +
"HexDigit     ::= $digit | [a-fA-F]\n" +
"UTFChar      ::= '\\u' HexDigit {4}\n" +
"AnyChar      ::= $UTFChar - '\\' - '\"'\n" + //any UTF char but not '\' or '"'
"OtherChar    ::= UTFChar | AnyChar\n" +
"StringPart   ::= '\\\\' | '\\\"' | '\\n' | '\\r' | '\\t' | '\\f' | '\\b'\n" +
"             | OtherChar\n" +				
"String       ::= '\"' StringPart* '\"'\n" +
"SimpleValue  ::= S? (Double | Float | Decimal | Byte | Short | Integer\n" +
"             | Long | BigInteger | dateTime | time | date | Duration\n" +
"             | String | Boolean | Null )\n" +
"Array        ::= S? '[' (Value (S? ',' Value)*)? S? ']'\n" +
"Identifier   ::= ('_' | $letter) ('_' | $letter | $digit)*\n" +
"NamedValue   ::= S? (Identifier | String) S? ':' Value\n" +
"Map          ::= S? '{' (NamedValue (S? ',' NamedValue)* )?  S? '}'\n" +
"Value        ::= Array | Map | SimpleValue\n" +
"CharsetName  ::= [a-zA-Z] [0-9a-zA-Z-]*\n" +
"Directive    ::= S? '%encoding' S? '=' S? '\"' CharsetName '\"' S?\n" +
"xon          ::=  Directive? Value S?\n";
			g = BNFGrammar.compile(bnf);
			testx(new String[] {
				"\"true\"",
				"null",
				"1l", //long
				"\"\"", // empty string
				"\"\\u0045\"", // string with UTFChar
				"\"\\n\\t\\\\\\\"\\b\"", // esccaped chars
				"-1.25e-3f",
				"d1949-11-07",
				"d1949-11-07T15:59Z",
				"d1949-11-07T15:59:01.123",
				"d1949-11-07T15:59:01.123-02:00",
				"d1949-11-07T15:59:01.123-0200",
				"d1949-11-07T15:59:01.123-02",
				"d1945", //gYear
				"d---29", //gDay
				"d---29-02:00", //gDay
				"d--12", //gMonth
				"d--12-02:00", //gMonth
				"d--12-29", //gMonthDay
				"d--12-29-02:00", //gMonthDay
				"d11:20",
				"d11:20:31",
				"d11:20:31.123",
				"d11:20:31.123Z",
				"d11:20:31.123-02:00",
				"d11:20+02:00",
				"d11:20:31-02:00",
				"P2Y6M5DT12H35M30S",
				"P1Y2M3DT10H30M123.123456S",
				"-P1DT2H",
				"P1Y1M1DT1H1M1.1234567S",
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
				"{ a :{}, b:[], c:null }",
				"[[3,null,false],[3.14,\"\",false],[\"aa\",true,false]]",
				"[1, { _x69_:1, _x5f_map:null, \"a_x\tb\":[null], item:{}}]",
				"[1, { a : 1, b:\"a\", \"\":false, array:[], map:{}}, \"abc\"]",
				"[{a:[{},[1,2],{},[3,4]]}]",
				"[{a:[{a:1},[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{a:1},[30,4]]}]",
				"[{a:[[1,2],[3,4],{}]}]",
				"[{a:[[1,2],[3,4],{a:1,b:2}]}]"}, g, "xon");
			testx(SUtils.getFileGroup(getXonDir() + "Test*.xon"),  g, "xon");
			testfail(new String[] { // test XON incorrect data
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