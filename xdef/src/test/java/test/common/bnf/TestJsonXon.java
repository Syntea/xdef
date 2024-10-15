package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.StringParser;
import test.XDTester;

/** Test XDefinition script.
 * @author Vaclav Trojan
 */
public class TestJsonXon extends XDTester {

	public TestJsonXon() {super();}

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
"Value        ::= Array | Map | SimpleValue\n";
			g = BNFGrammar.compile(bnf);
			assertEq(s="null", parse(g,"Value",s));
			assertEq(s="false", parse(g,"Value",s));
			assertEq(s="\"\"", parse(g,"Value",s));
			assertEq(s="\"a\"", parse(g,"Value",s));
			assertEq(s="\"\\u0045\\n\\t\\\\\\\"\\b\"", parse(g,"Value",s));
			assertEq(s="1", parse(g,"Value",s));
			assertEq(s="-3.14e+10", parse(g,"Value",s));
			assertEq(s="[]", parse(g,"Value",s));
			assertEq(s="{}", parse(g,"Value",s));
			assertEq(s="[ null ]", parse(g,"Value",s));
			assertEq(s="[1e-3]", parse(g,"Value",s));
			assertEq(s="[ 1.5, true, \"\", [[]], {} ]", parse(g,"Value",s));
			assertEq(s="{\"a\" : 1e2}", parse(g,"Value",s));
			assertEq(s="{ \"a\" :{} }", parse(g,"Value",s));
			assertEq(s="{\"a\":{},\"b\":[] , \"c\":null}", parse(g,"Value",s));
			assertEq(parse(g,"Value","aABB"),
				"Value failed, Line: 1, column: 1, position: 0, startLine: 0;");
		} catch (RuntimeException ex) {fail(ex);}
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
"yearFrag     ::= '-'? [1-9] $digit* | $digit+\n" +
"monthFrag    ::= ('0' [1-9]) | ('1' [0-2])\n" +
"dayFrag      ::= ('0' [1-9]) | ([12] $digit) | ('3' [01])\n" +
"hourFrag     ::= ([01] $digit) | ('2' [0-3])\n" +
"minuteFrag   ::= [0-5] $digit\n" +
"secondFrag   ::= ([0-5] $digit) ('.' $digit+)?\n" +
"endOfDayFrag ::= '24:00:00' ('.' '0'+)?\n" +
"timezoneFrag ::= 'Z' | ('+' | '-') (('0' $digit | '1' [0-3])\n" +
"             ':' minuteFrag | '14:00')\n" +
"dateFrag     ::= yearFrag '-' monthFrag '-' dayFrag\n" +
"timeFrag     ::= ((hourFrag ':' minuteFrag ':' secondFrag)\n" +
"             | '24:00:00') ('.' $digit+)?\n" +
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
"Value        ::= Array | Map | SimpleValue\n";
			g = BNFGrammar.compile(bnf);
			assertEq(s="null", parse(g, "Value", s));
			assertEq(s="false", parse(g, "Value", s));
			assertEq(s="\"\"", parse(g, "Value", s));
			assertEq(s="\"a\"", parse(g, "Value", s));
			assertEq(s="\"\\u0045\\n\\t\\\\\\\"\\b\"", parse(g, "Value", s));
			assertEq(s="1", parse(g, "Value", s));
			assertEq(s="-3.14e+10", parse(g, "Value", s));
			assertEq(s="0N", parse(g, "Value", s));
			assertEq(s="[]", parse(g, "Value", s));
			assertEq(s="{}", parse(g, "Value", s));
			assertEq(s="[ null ]", parse(g, "Value", s));
			assertEq(s="[1e-3]", parse(g, "Value", s));
			assertEq(s="[ 1.5, true, \"\", [[]], {} ]", parse(g, "Value", s));
			assertEq(s="{\"a\": 1e2}", parse(g, "Value", s));
			assertEq(s="{ a :{}, b:[], c:null }", parse(g, "Value", s));
			s = "[1, { a : 1, b:\"a\", \"\":false, array:[], map:{}}, \"abc\"]";
			assertEq(s, parse(g, "Value", s));
			assertEq(s, parse(g, "Value", s));
			assertEq(parse(g, "Value", "aABB"),
				"Value failed, Line: 1, column: 1, position: 0, startLine: 0;");
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}