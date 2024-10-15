package test.common.bnf;

import java.io.File;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.FUtils;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import test.XDTester;

/** Test XDefinition script.
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
		String s;
		try { // JSON
			bnf =
"S            ::= [#9#10#13 ]+ /*whitespaces*/\n" +
"IntNumber    ::= $digit ($digit)*\n" +
"SignedInteger::= '-'? IntNumber\n" +
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
			assertEq(s="null", parse(g,"json",s));
			assertEq(s="false", parse(g,"json",s));
			assertEq(s="\"\"", parse(g,"json",s));
			assertEq(s="\"a\"", parse(g,"json",s));
			assertEq(s="\"\\u0045\\n\\t\\\\\\\"\\b\"", parse(g,"json",s));
			assertEq(s="1", parse(g,"json",s));
			assertEq(s="-3.14e+10", parse(g,"json",s));
			assertEq(s="[]", parse(g,"json",s));
			assertEq(s="{}", parse(g,"json",s));
			assertEq(s="[ null ]", parse(g,"json",s));
			assertEq(s="[1e-3]", parse(g,"json",s));
			assertEq(s="[ 1.5, true, \"\", [[]], {} ]", parse(g,"json",s));
			assertEq(s="{\"a\" : 1e2}", parse(g,"json",s));
			assertEq(s="{ \"a\" :{} }", parse(g,"json",s));
			assertEq(s="{\"a\":{},\"b\":[] , \"c\":null}", parse(g,"json",s));
			assertEq(parse(g,"json","aABB"),
				"json failed, Line: 1, column: 1, position: 0, startLine: 0;");
			s = getXonDir();
			System.out.println(s);
			for (File f: SUtils.getFileGroup(s + "Test*.json")) {
				String t = FUtils.readString(f, "UTF-8");
				String u = parse(g, "json", t);
				if (!t.equals(u)) {
					fail("E: " + f.getName() + "\n" + u + "\n" + t); 
				}
			}
		} catch (Exception ex) {fail(ex);}
		try { // XON
			bnf =
"S            ::= [#9#10#13 ]+ /*whitespaces*/\n" +
"IntNumber    ::= $digit ($digit)*\n" +
"SignedInteger::= '-'? IntNumber\n" +
"Exponent     ::= ('e' | 'E') ('+' | '-')? IntNumber\n" +
"FloatPart    ::= '.' IntNumber Exponent? | Exponent\n" +
"FloatNumber  ::= SignedInteger FloatPart\n" +
"Byte         ::= SignedInteger 'b'\n" +
"Short        ::= SignedInteger 's'\n" +
"Integer      ::= SignedInteger 'i'\n" +
"Long         ::= SignedInteger 'l' ?\n" +
"BigInteger   ::= SignedInteger 'N' \"l\" ?\n" +
"Decimal      ::= SignedInteger 'D' | FloatNumber 'D'\n" +
"Float        ::= SignedInteger 'f' | FloatNumber 'f' | 'NaNf' | '-' ? 'INFf'\n" +
"Double       ::= SignedInteger 'd' | FloatNumber 'd'? | 'NaN' | '-' ? 'INF'\n" +
"yearFrag     ::= '-'? $digit*\n" +
"monthFrag    ::= ('0' [1-9]) | ('1' [0-2])\n" +
"dayFrag      ::= ('0' [1-9]) | ([12] $digit) | ('3' [01])\n" +
"hourFrag     ::= ([01] $digit) | ('2' [0-3])\n" +
"minuteFrag   ::= [0-5] $digit\n" +
"secondFrag   ::= ([0-5] $digit) ('.' $digit+)?\n" +
"endOfDayFrag ::= '24:00:00' ('.' '0'+)?\n" +
"timezoneFrag ::= 'Z' | ('+' | '-') (('0' $digit | '1' [0-3])\n" +
"             (':' minuteFrag )? | '14:00')\n" +
"dateFrag     ::= yearFrag | yearFrag '-' monthFrag ( '-' dayFrag)?\n" +
"             | '--' monthFrag? '-' dayFrag | '--' monthFrag\n" +
"timeFrag     ::= ( (hourFrag ':' minuteFrag ':' secondFrag)\n" +
"             | endOfDayFrag ) | (hourFrag ':' minuteFrag)\n" +
"time         ::= 'd' timeFrag timezoneFrag?\n" +
"date         ::= 'd' dateFrag  timezoneFrag?\n" +
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
			assertEq(s="null", parse(g, "xon", s));
			assertEq(s="false", parse(g, "xon", s));
			assertEq(s="\"\"", parse(g, "xon", s));
			assertEq(s="\"a\"", parse(g, "xon", s));
			assertEq(s="\"\\u0045\\n\\t\\\\\\\"\\b\"", parse(g, "xon", s));
			assertEq(s="1", parse(g, "xon", s));
			assertEq(s="-3.14e+10", parse(g, "xon", s));
			assertEq(s="0N", parse(g, "xon", s));
			assertEq(s="[]", parse(g, "xon", s));
			assertEq(s="{}", parse(g, "xon", s));
			assertEq(s="[ null ]", parse(g, "xon", s));
			assertEq(s="[1e-3]", parse(g, "xon", s));
			assertEq(s="[ 1.5, true, \"\", [[]], {} ]", parse(g, "xon", s));
			assertEq(s="{\"a\": 1e2}", parse(g, "xon", s));
			assertEq(s="{ a :{}, b:[], c:null }", parse(g, "xon", s));
			s = "[1, { a : 1, b:\"a\", \"\":false, array:[], map:{}}, \"abc\"]";
			assertEq(s, parse(g, "xon", s));
			assertEq(s, parse(g, "xon", s));
			assertEq(parse(g, "xon", "aABB"),
				"xon failed, Line: 1, column: 1, position: 0, startLine: 0;");
			for (String t : new String[] {
				"true",
				"\"true\"",
				"null",
				"1l", //long
				"-1.25e-3f",
				"d1949-11-07",
				"d1949-11-07T15:59Z",
				"d1949-11-07T15:59:01.123",
				"d1949-11-07T15:59:01.123-02:00",
				"d1949-11-07T15:59:01.123-02",
				"d1945", //gYear
				"d---29", //gDay
				"d--12", //gMonth
				"d--12-29", //gMonthDay
				"d11:20",
				"d11:20:31",
				"d11:20:31.123",
				"d11:20:31.123Z",
				"d11:20:31.123-02:00",
				"d11:20+02:00",
				"d11:20:31-02:00",
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
				"[[3,null,false],[3.14,\"\",false],[\"aa\",true,false]]",
				"[1, { _x69_:1, _x5f_map:null, \"a_x\tb\":[null], item:{}}]",
				"[1,{ a:1, b:\"a\", \"\":false, array : [], map:{}}, \"abc\"]",
				"[{a:[{},[1,2],{},[3,4]]}]",
				"[{a:[{a:1},[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{a:1},[30,4]]}]",
				"[{a:[[1,2],[3,4],{}]}]",
				"[{a:[[1,2],[3,4],{a:1,b:2}]}]",
				}) {
				assertEq(t, parse(g, "xon", t));
			}
			s = getXonDir();
			for (File f: SUtils.getFileGroup(s + "Test*.xon")) {
				String name = f.getName();
				int ndx = name.indexOf("UTF");
				String t = name.equals("Test201windows-1250.xon")
					? FUtils.readString(f, "windows-1250")
					: ndx > 0 ? FUtils.readString(f,
						name.substring(ndx, name.indexOf(".xon")))
					: FUtils.readString(f, "UTF-8");
				String u = parse(g, "xon", t);
				if (!t.equals(u)) {
					fail("E: " + f.getName() + "\n" + u + "\n" + t); 
				}
			}			
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}