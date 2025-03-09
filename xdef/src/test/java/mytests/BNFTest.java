package mytests;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.StringParser;

/** Tests.
 * @author Vaclav Trojan
 */
public class BNFTest extends STester {
	public BNFTest() {super();}

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
			return printThrowable(ex);
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String bnf;
		BNFGrammar g;
		String s;
//		try {
//			bnf = "X::='a'('A'|'B'?)! Y::='a'('A'?|'B')! Z::='a'('A'?|'B'?)!";
//
//			g = BNFGrammar.compile(bnf);
//
//			assertEq(s="aA", parse(g, "X", s));
//			assertEq(s="aAB", parse(g, "X", s));
//			assertEq(s="aBA", parse(g, "X", s));
//			assertTrue(!(s="aB").equals(parse(g, "X", s)));
//			assertTrue(!(s="a").equals(parse(g, "X", s)));
//			assertTrue(!(s="aABA").equals(parse(g, "X", s)));
//
//			assertEq(s="aB", parse(g, "Y", s));
//			assertEq(s="aAB", parse(g, "Y", s));
//			assertEq(s="aBA", parse(g, "Y", s));
//			assertTrue(!(s="aA").equals(parse(g, "Y", s)));
//			assertTrue(!(s="a").equals(parse(g, "Y", s)));
//			assertTrue(!(s="aABA").equals(parse(g, "Y", s)));
//
//			assertEq(s="aAB", parse(g, "Z", s));
//			assertEq(s="aBA", parse(g, "Z", s));
//			assertEq(s="aA", parse(g, "Z", s));
//			assertEq(s="aB", parse(g, "Z", s));
//			assertEq(s="a", parse(g, "Z", s));
//			assertTrue(!(s="aC").equals(parse(g, "Z", s)));
//			assertTrue(!(s="aABB").equals(parse(g, "Z", s)));
//		} catch (Exception ex) {fail(ex);}
		try {
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
"Null         ::= \"null\"\n" +
"Boolean      ::= \"true\" | \"false\"\n" +
"HexDigit     ::= $digit | [a-fA-F]\n" +
"UTFChar      ::= '\\\\u' HexDigit HexDigit HexDigit HexDigit\n" +
"AnyChar      ::= $UTFChar - \"\\\" - '\"'\n" +//any valid UTF-16 character
"OtherChar    ::= UTFChar | AnyChar\n" +
"StringPart   ::= '\\\\' | '\\\"' | '\\n' | '\\r' | '\\t' | '\\f' | '\\b'\n" +
"             | OtherChar\n" +
"String       ::= '\"' StringPart? '\"'\n" +
"SimpleValue  ::= S? (Double | Float | Decimal | Byte | Short | Integer\n" +
"             | Long | BigInteger | dateTime | time | date | Duration\n" +
"             | String | Boolean | Null )\n" +
"Array        ::= S? '[' (Value (S? ',' Value)*)? S? ']'\n" +
"Identifier   ::= ('_' | $letter) ('_' | $letter | $digit)*\n" +
"NamedValue   ::= S? (Identifier | String) S? ':' Value\n" +
"Map          ::= S? '{' NamedValue* S? '}'\n" +
"Value        ::= Array | Map | SimpleValue\n" +
"xon          ::= Value\n" +
"";
			System.out.println(bnf);
			g = BNFGrammar.compile(bnf);
			assertEq(s="null", parse(g, "xon", s));
			assertEq(s="false", parse(g, "xon", s));
			assertEq(s="\"a\"", parse(g, "xon", s));
			assertEq(s="1", parse(g, "xon", s));
			assertEq(s="-3.14e+10", parse(g, "xon", s));
			assertEq(s="0N", parse(g, "xon", s));
			assertEq(s="[]", parse(g, "xon", s));
			assertEq(s="[ null ]", parse(g, "xon", s));
			assertEq(s="[1]", parse(g, "xon", s));
			assertEq(s="[ 1, true, \"\" ]", parse(g, "xon", s));
			assertEq(s="{\"a\":1}", parse(g, "xon", s));
			assertEq(s="{ \"a\":{} }", parse(g, "xon", s));
			assertTrue(!(s="aABB").equals(parse(g, "xon", s)));
		} catch (RuntimeException ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
