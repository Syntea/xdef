package test.common.bnf;

import java.io.File;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.FUtils;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SException;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import test.XDTester;

/** Test JSON and XON BNF grammar with data.
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
			return name + " failed, " + (p.eos()?
				"eos" : p.getPosition().toString()) + ";";
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String bnf;
		BNFGrammar g;
		try {
			// JSON
			bnf =
"Char         ::= $UTFChar\n" +//any UTF char
"S            ::= [#9#10#13 ]+ /*whitespace*/\n" +
"HexDigit     ::= [0-9] | [a-fA-F]\n" +
"Letter       ::= $letter\n" +
"/* Simple values */\n" +
"Null         ::= 'null'\n" +
"Boolean      ::= 'true' | 'false'\n" +
"/* Number */\n" +
"IntNumber    ::= [0-9] [0-9]*\n" +
"SignedInteger::= '-'? ('0' | [1-9] [0-9]*)\n" +
"Exponent     ::= ('e' | 'E') ('+' | '-')? IntNumber\n" +
"FloatPart    ::= '.' IntNumber Exponent? | Exponent\n" +
"FloatNumber  ::= SignedInteger FloatPart\n" +
"Integer      ::= SignedInteger\n" +
"Number       ::= FloatNumber | SignedInteger | 'NaN' | '-' ? 'INF'\n" +
"/* String */\n" +
"AnyChar      ::= Char - '\\' - '\"'\n" +//any UTF char but not '\\' or '\"'
"UTFSpec      ::= '\\u' HexDigit {4} /*hexadecimal specification of char*/\n" +
"StringPart   ::= '\\\\' | '\\\"' | '\\n' | '\\r' | '\\t' | '\\f' | '\\b'\n" +
"             | UTFSpec | AnyChar\n" +
"String       ::= '\"' StringPart* '\"'\n" +
"SimpleValue  ::= S? (Number | String | Boolean | Null)\n" +
"/* Complex values */\n" +
"Identifier   ::= ('_' | Letter) ([_0-9] | Letter)*\n" +
"NamedValue   ::= S? String S? ':' Value\n" +
"Array        ::= S? '[' (Value (S? ',' Value)*)? S? ']'\n" +
"Map          ::= S? '{' (NamedValue (S? ',' NamedValue)*)?  S? '}'\n" +
"Value        ::= (Array | Map | SimpleValue)\n" +
"json         ::= Value S?\n";
			g = BNFGrammar.compile(bnf);
			test(new String[] {
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
				"{\"a\":{},\"_b\":[] , \"c_1\":null}"}, g, "json");
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

			// XON
			bnf =
"AnyChar      ::= $UTFChar - '\\' - '\"'\n" + //any UTF char but not '\' or '"'
"Letter       ::= $letter\n" +
"AsciiChar    ::= [ -~]\n"+
"WhiteSpace   ::= [#9#10#13 ]\n" +
"W            ::= [#9 ]+ /* linear white space */\n"+
"Comment ::= \"/*\" ([^*]+ | \"*\" - \"*/\")* \"*/\"\n" +
"B64text      ::= ((S | [-a-zA-Z0-9+/])+ '='? S? '='? S?) /* base64 */\n"+
"CharsetName ::= ([a-zA-Z] ('-'? [a-zA-Z0-9]+)*) $rule \n"+
"S ::= (WhiteSpace | Comment)+ /* Sequence of whitespaces or comments */\n" +
"IntNumber    ::= [0-9] [0-9]*\n" +
"HexDigit     ::= [0-9] | [a-fA-F]\n" +
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
"Number       ::= SignedInteger | FloatNumber | 'NaN' | '-'? 'INF'\n" +
"/* Date and time */\n" +
"yearFrag     ::= '-'? [0-9]*\n" +
"monthFrag    ::= '0' [1-9] | '1' [0-2]\n" +
"dayFrag      ::= '0' [1-9] | [12] [0-9] | '3' [01]\n" +
"hourFrag     ::= [01] [0-9] | '2' [0-3]\n" +
"minuteFrag   ::= [0-5] [0-9]\n" +
"secondFrag   ::= ([0-5] [0-9]) ('.' [0-9]+)?\n" +
"endOfDayFrag ::= '24:00:00' ('.' '0'+)?\n" +
"timezoneFrag ::= 'Z' | ('+' | '-') (('0' [0-9] | '1' [0-3])\n" +
"             (':' minuteFrag )? | '14:00'\n"+
"             | ('0' [0-9] | '1' [0-3]) [0-5] [0-9] | '1400')\n" +
"dateFrag     ::= yearFrag | yearFrag '-' monthFrag ('-' dayFrag)?\n" +
"             | '--' monthFrag | '--' monthFrag '-' dayFrag\n" +
"             | '---' dayFrag\n" +
"date         ::= 'd' (yearFrag '-' monthFrag ('-' dayFrag)? timezoneFrag?\n"+
"             | yearFrag timezoneFrag? | '--' monthFrag timezoneFrag?\n" +
"             | '--' monthFrag '-' dayFrag timezoneFrag?\n" +
"             | '---' dayFrag timezoneFrag?)\n" +
"timeFrag     ::= ( (hourFrag ':' minuteFrag ':' secondFrag)\n" +
"             | endOfDayFrag ) | (hourFrag ':' minuteFrag)\n" +
"time         ::= 'd' timeFrag timezoneFrag?\n" +
"dateTime     ::= 'd' dateFrag 'T' (timeFrag | endOfDayFrag) timezoneFrag?\n" +
"/* Duration */\n" +
"duYearFrag   ::= [0-9]+ 'Y'\n" +
"duMonthFrag  ::= [0-9]+ 'M'\n" +
"duDayFrag    ::= [0-9]+ 'D'\n" +
"duHourFrag   ::= [0-9]+ 'H'\n" +
"duMinuteFrag ::= [0-9]+ 'M'\n" +
"duSecondFrag ::= [0-9]+ ('.' [0-9]+)? 'S'\n" +
"duYMonthFrag ::= (duYearFrag duMonthFrag?) | duMonthFrag\n" +
"duTimeFrag   ::= 'T' ((duHourFrag duMinuteFrag? duSecondFrag?)\n" +
"             | (duMinuteFrag duSecondFrag?) | duSecondFrag)\n" +
"duDayTimeFrag::= (duDayFrag duTimeFrag?) | duTimeFrag\n" +
"Duration     ::= '-'? 'P' ((duYMonthFrag duDayTimeFrag?) | duDayTimeFrag)\n" +
"/* bytes */\n" +
"Bytes        ::= 'b(' B64text? ')'\n" +
"/* GPS */\n" +
"BPSName      ::= ('_' | Letter)+ ([- _.,/0-9] | Letter)*\n" +
"GPS          ::= 'g(' Number ',' ' '? Number (',' ' '? Number)?\n" +
"             (',' ' '? (BPSName | String) )? ')'\n" +
"/* Currency */\n" +
"Currency     ::= 'C('[A-Z]{3}')'\n" +
"/* Price */\n" +
"Price        ::= 'p(' IntNumber FloatPart? ' ' [A-Z]{3}')'\n" +
"/* Char */\n" +
"Char         ::= 'c\"' StringPart '\"'\n" +
"/* email address */\n" +
"CommentList  ::= ( '(' CommentPart* ')' (W? '(' CommentPart* ')')* )\n"+
"CommentPart  ::= (AsciiChar - [()])+ (W? CommentList)?\n"+
"EmailComment ::=  W? ( CommentList $rule) W?\n"+
"Atom         ::= ([-0-9a-zA-Z_])+\n"+
"Domain       ::= '@' Atom ('.' Atom)*\n"+
"LocalPart    ::= Atom ('.' Atom)*\n"+
"EmailAddr    ::= LocalPart Domain\n"+
"EnclosedEmail::= '<' EmailAddr '>' \n"+
"Ptext        ::= ((AsciiChar - [@><()=])+)\n"+
"EText        ::= ((EmailComment* (TextItem | EmailComment)*)\n"+
"             | EmailComment* W? Ptext)? EmailComment*\n"+
"HexOctet     ::= '=' [0-9A-F] [0-9A-F]\n"+
"Qtext        ::= ((HexOctet | AsciiChar - [=?])+)/*quoted*/\n"+
"TextItem     ::= W? '=?' CharsetName '?' ('Q?' Qtext | 'B?' B64text) '?='\n"+
"Email        ::= 'e\"' ( (EText? W? EnclosedEmail\n"+
"             | (EmailComment* EmailAddr)) (W? EmailComment)* ) '\"'\n" +
"/* URI */\n" +
"SchemeName   ::= [a-zA-Z]+ '://'\n" +
"URI          ::= 'u\"' SchemeName AnyChar+ '\"'\n" +
"/* IPAddr */\n" +
"IPv4part        ::= '2' [0-5] [0-9] | [0-1] [0-9] [0-9] | [0-9]{1,2}\n" +
"IPv4          ::= IPv4part ('.' IPv4part) {3}\n" +
"IPv6part       ::= HexDigit {1,4}\n" +
"IPv6          ::= IPv6part (':' IPv6part) {7}\n" +
"IPAddr        ::= '/' (IPv6 | IPv4)\n" +
"/* String */\n" +
"UTFChar      ::= '\\u' HexDigit {4} /*hexadecimal specification of char*/\n" +
"StringPart   ::= '\\\\' | '\\\"' | '\\n' | '\\r' | '\\t' | '\\f' | '\\b'\n" +
"             | UTFChar | AnyChar\n" +
"String       ::= '\"' StringPart* '\"'\n" +
"Boolean      ::= 'true' | 'false'\n" +
"SimpleValue  ::= S? ('null' | Boolean | String\n" +
"             | Byte | Short | Integer | Long | BigInteger\n" +
"             | Double | Float | Decimal\n" +
"             | dateTime | time | date | Duration\n" +
"             | Bytes | Char | URI | IPAddr | Email\n"+
"             | GPS | Currency | Price)\n" +
"Array        ::= S? '[' (Value (S? ',' Value)*)? S? ']'\n" +
"NCName   ::= ('_' | Letter) ([-_.0-9] | Letter)*\n" +
"NamedValue   ::= S? (NCName | String) S? ':' Value\n" +
"Map          ::= S? '{' (NamedValue (S? ',' NamedValue)* )?  S? '}'\n" +
"Value        ::= Array | Map | SimpleValue\n" +
"Directive    ::= S? '%encoding' S? '=' S? '\"' CharsetName '\"' S?\n" +
"xon          ::= Directive? Value S?\n";
			g = BNFGrammar.compile(bnf);
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