package test.common.bnf;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;
import org.xdef.sys.BNFExtMethod;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.FUtils;
import org.xdef.sys.Report;
import org.xdef.sys.SException;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;

/** Test of BNF.
 * @author Vaclav Trojan
 */
public class TestBNF extends STester {

	public TestBNF() {super();}

	private final Stack<Object> _stack = new Stack<>();
	private final Stack<Object> _opers = new Stack<>();
	private final HashMap<Object, Object> _variables = new HashMap<>();
	private String _s;
	private Object _result;

	private String p(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			_stack.clear();
			_opers.clear();
			_s = "";
			_result = null;
			_variables.clear();
			_variables.put("x", 999L);
			grammar.setUserObject(this);
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

	private String p(BNFGrammar grammar, String name) {
		try {
			_stack.clear();
			_opers.clear();
			_s = "";
			_result = null;
			_variables.clear();
			_variables.put("x", 999L);
			grammar.setUserObject(this);
			if (grammar.parse(name)) {
				return grammar.getParsedString();
			} else {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return name + " failed, " + (grammar.getParser().eos()? "eos" :
					grammar.getParser().getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			return printThrowable(ex);
		}
	}

	private String printStack() {
		StringBuilder sb = new StringBuilder("\nStack:\n");
		for (int i = 0; i < _stack.size(); i++) {
			sb.append((String) _stack.get(i)).append('\n');
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
//	User methods
////////////////////////////////////////////////////////////////////////////////

	public boolean myInteger(BNFExtMethod p) {
		return p.getParser().isInteger();
	}
	public boolean myInteger(BNFExtMethod p, Long min, Long max) {
		if (!p.getParser().isInteger()) {
			return false;
		}
		long i = Long.parseLong(p.getParser().getParsedString());
		if (i < min) {
			p.getParser().putReport(
				Report.error("", "integer is lower then minimum"));
		}
		if (i > max) {
			p.getParser().putReport(
				Report.error("", "integer is greater then maximum"));
		}
		return true;
	}
	public boolean myTest(BNFExtMethod p, Long i, String s) {
		_s = "test(" + i + "," + s + ")";
		return true;
	}
	public boolean myValue(BNFExtMethod p) {
		_stack.push(p.getParsedString());
		return true;
	}
	public boolean myValue(BNFExtMethod p, String s) {
		_stack.push(s + p.getParsedString());
		return true;
	}
	public boolean myOperator(BNFExtMethod p) {
		String s = p.getParsedString();
		_opers.push("" + s.charAt(s.length() - 1));
		return true;
	}
	public boolean myUnaryOperator(BNFExtMethod p) {
		String s = p.getParsedString();
		_opers.push("," + s);
		return true;
	}
	public boolean myPushOperator(BNFExtMethod p) {
		if (!_opers.empty()) {
			_stack.push(_opers.pop());
		}
		return true;
	}
	public boolean myPushOperator(BNFExtMethod p, String s) {
		_stack.push(s);
		return true;
	}
	public boolean exec(BNFExtMethod p) {
		Stack<Object> stack = new Stack<>();
		String varname = null;
		for (int i = 0; i < _stack.size(); i++) {
			String s = (String) _stack.get(i);
			Object obj;
			Object obj1;
			char ch;
			switch (ch = s.charAt(0)) {
				case ',':
					if (s.charAt(1) == '-') {
						obj = stack.pop();
						if (obj instanceof Long) {
							long x = (Long) obj;
							stack.push(-x);
						} else if (obj instanceof Double) {
							double x = ((Double) obj);
							stack.push(-x);
						} else {
							throw new RuntimeException("Incorrect object");
						}
					}
					continue;
				case '+':
				case '-':
				case '*':
				case '/':
					obj = stack.pop();
					obj1 = stack.pop();
					if (obj1 instanceof Long) {
						long x = (Long) obj1;
						if (obj instanceof Long) {
							long y = (Long) obj;
							switch (ch) {
								case '+':
									stack.push(x + y);
									break;
								case '-':
									stack.push(x - y);
									break;
								case '*':
									stack.push(x * y);
									break;
								case '/':
									stack.push(x / y);
									break;
								default:
									break;
							}
							continue;
						} else if (obj instanceof Double) {
							double y = (Double) obj;
							switch (ch) {
								case '+':
									stack.push(x + y);
									break;
								case '-':
									stack.push(x - y);
									break;
								case '*':
									stack.push(x * y);
									break;
								case '/':
									stack.push(x / y);
									break;
								default:
									break;
							}
							continue;
						}
					} else if (obj1 instanceof Double) {
						double x = ((Double) obj1);
						if (obj instanceof Long) {
							long y = (Long) obj;
							switch (ch) {
								case '+':
									stack.push(x + y);
									break;
								case '-':
									stack.push(x - y);
									break;
								case '*':
									stack.push(x * y);
									break;
								case '/':
									stack.push(x / y);
									break;
								default:
									break;
							}
							continue;
						} else if (obj instanceof Double) {
							double y = (Double) obj;
							switch (ch) {
								case '+':
									stack.push(x + y);
									break;
								case '-':
									stack.push(x - y);
									break;
								case '*':
									stack.push(x * y);
									break;
								case '/':
									stack.push(x / y);
									break;
								default:
									break;
							}
							continue;
						}
					}
					if (ch == '+') {
						stack.push(obj1 + obj.toString());
					} else {
						throw new RuntimeException("Incorrect object");
					}
					continue;
				default:
					if (ch == '\'' || ch == '"') {//string
						int len = s.length();
						stack.push(s.substring(1, len-1));
					} else if (ch >= '0' && ch <= '9') { //integer or float
						if (s.indexOf('.') < 0 &&
							s.indexOf('e') < 0 && s.indexOf('R') < 0) {
							stack.push(Long.valueOf(s));
						} else {
							stack.push(Double.valueOf(s));
						}
					} else { //var name
						if (s.equals("true")) {
							stack.push(Boolean.TRUE);
						} else if (s.equals("false")) {
							stack.push(Boolean.FALSE);
						} else if (_stack.size() > i + 1 &&
							((String) _stack.get(i + 1)).charAt(0) == '=') {
							i++;
							varname = s;
						} else {
							if (_variables.containsKey(s)) {
								stack.push(_variables.get(s));
							} else {
								throw new RuntimeException(
									"Variable '" + s + "' is undefined");
							}
						}
					}
			}
		}
		_result = stack.pop();
		if (varname != null) {
			_variables.put(varname, _result);
		}
		return true;
	}

	public boolean xxx(BNFExtMethod p) {
		_stack.push(p.getParsedString());
		return true;
	}

	public static boolean yyy(BNFExtMethod p) {
		String s = p.getRuleName() + ":" + p.getParsedString()
			+ ",p:" + p.getSPosition().getIndex();
		Object o = p.setUserObject(s);
		if (o != null) {
			p.setUserObject(o  + "\n" + s);
		}
		return true;
	}

////////////////////////////////////////////////////////////////////////////////

	@Override
	public void test() {
		String bnf;
		BNFGrammar g, g1;
		String s;
		try {
			g = BNFGrammar.compile("X::= 'A'?");
			assertEq("", p(g, "X", ""));
			assertEq("A", p(g, "X", "A"));
			g = BNFGrammar.compile("X::= 'A'? 'B'?");
			assertEq("", p(g, "X", ""));
			assertEq("A", p(g, "X", "A"));
			assertEq("B", p(g, "X", "B"));
			assertEq("AB", p(g, "X", "AB"));
			g = BNFGrammar.compile("X::= \"A\"* \"B\"? \"C\"*");
			assertEq("", p(g, "X", ""));
			assertEq("A", p(g, "X", "A"));
			assertEq("B", p(g, "X", "B"));
			assertEq("AB", p(g, "X", "AB"));
			assertEq("C", p(g, "X", "C"));
			assertEq("CC", p(g, "X", "CC"));
			assertEq("ACC", p(g, "X", "ACC"));
			assertEq("BCC", p(g, "X", "BCC"));
			assertEq("ABC", p(g, "X", "ABC"));
			assertEq("AAABCC", p(g, "X", "AAABCC"));
			g = BNFGrammar.compile("X::= A|B A::=$integer B::=$float");
			assertEq("123", p(g, "X", "123"));
			assertEq("123.5", p(g, "X", "123.5"));
			g = BNFGrammar.compile("C::= $xmlChar B::= \"'\"");
			assertEq("'", p(g, "B", "'"));
			assertEq("'", p(g, "C", "'"));
			assertEq(" ", p(g, "C", " "));
			assertEq("@", p(g, "C", "@"));
			g = BNFGrammar.compile("x ::= $JavaName");
			assertEq("a1", p(g, "x", "a1"));
			g = BNFGrammar.compile("x::=$JavaQName");
			assertEq("a.b", p(g, "x", "a.b"));
			assertEq("ab", p(g, "x", "ab"));
			g = BNFGrammar.compile("x ::= 'a' $anyChar");
			assertEq("ax", p(g, "x", "ax"));
			assertTrue(p(g, "x", "a").contains("x failed, "));
			assertTrue(p(g, "x", "bb").contains("x failed, "));
			g = BNFGrammar.compile("x ::= 'a' $anyChar * ");
			assertEq("a", p(g, "x", "a"));
			assertNull(g.getParsedObjects());
			assertEq("ax", p(g, "x", "ax"));
			assertEq("axy", p(g, "x", "axy"));
			assertTrue(p(g, "x", "b").contains("x failed, "));
			assertTrue(p(g, "x", "bb").contains("x failed, "));
			g = BNFGrammar.compile("x ::= 'a' $stop 'b' ");
			assertEq("a", p(g, "x", "abc"));
			assertNull(g.getParsedObjects());
			g = BNFGrammar.compile("%define $x: $stop(123) x ::= 'a' $x 'b' ");
			assertEq("a", p(g, "x", "abc"));
			assertEq("STOP 123", (String) g.getParsedObjects()[0]);
			g = BNFGrammar.compile("%define $x:$stop(1,'x')x::='a'$x'b'");
			assertEq("a", p(g, "x", "abc"));
			assertEq("STOP 1,\"x\"", (String) g.getParsedObjects()[0]);
			g = BNFGrammar.compile(
"S::=$whitespace+XMLName::=$xmlName RefName::=XMLName|XMLName?\"#\"XMLName" +
" RootList::=S?(RefName|\"*\")(S?\"|\"S?(RefName|\"*\"))*S?" +
"Reference::=\"ref\"S RefName");
			assertEq("#A", p(g, "RefName", "#A"));
			assertEq("A", p(g, "RefName", "A"));
			assertEq("A#B", p(g, "RefName", "A#B"));
			g = BNFGrammar.compile( // whitespaces
"S ::= $whitespace+\n"+
"ElementLink ::= (\"implements\" | \"uses\")  S XPosition\n" +
"XPosition ::= (XDefName? \"#\")? XModelName\n" +
"  (\"/\" XMLName)*\n" +
"XMLName ::= $xmlName\n"+
"XDefName ::= XMLName\n" +
"XModelName ::= XMLName"			);
			assertEq("implements #A", p(g, "ElementLink", "implements #A"));
			assertEq("implements A", p(g, "ElementLink", "implements A"));
			assertEq("uses A#B", p(g, "ElementLink", "uses A#B"));
			g = BNFGrammar.compile("X::='A' 'A'%");
			assertEq("AA", p(g, "X", "AA"));
			assertEq("Aa", p(g, "X", "Aa"));
			g = BNFGrammar.compile("X::= \"ABC\"% \"ABC\"");
			assertEq("ABCABC", p(g, "X", "ABCABC"));
			assertEq("abcABC", p(g, "X", "abcABC"));
			g = BNFGrammar.compile("X::=('AA'|'BC')('AA'%|'BC'%|'ACC'%)");
			assertEq("AAAA", p(g, "X", "AAAA"));
			assertEq("AAaa", p(g, "X", "AAaa"));
			assertEq("AAbc", p(g, "X", "AAbc"));
			assertEq("BCacc", p(g, "X", "BCacc"));
			assertEq("BCACC", p(g, "X", "BCACC"));
/**/
			g = BNFGrammar.compile( // test "all" selections
"A::= 'a' ('A', 'B')\n" +
"B::= 'a' ('A','B'?)\n" +
"C::= 'a' ('A'?,'B')\n" +
"D::= 'a' ('A'?,'B'?)\n" +
"E::= A?, 'Y'?\n"+
"F::= 'Y'?, A?\n"+
"G::= A, 'Y'\n"+
"H::= G, 'X'\n"+
"I::= G, 'X', 'Z'\n"+
"J::= (G, 'X') | 'Z'\n"+
"K::= (G, 'X')? 'Z'?\n"+
"L::= 'A', 'B' 'Z'\n"+
"M::= 'A', 'B' | 'Z'\n"+
"N::= 'Z' | 'A', 'B'\n"+
"O::= 'A', 'B' | 'C', 'D'\n"+
"P::= ('A', 'B') | ('C', 'D')\n"+
"");
			g = BNFGrammar.compile(g.display(false));
			assertEq(s="aBA", p(g, "A", s));
			assertTrue(!(s="a").equals(p(g, "A", s)));
			assertTrue(!(s="aA").equals(p(g, "A", s)));
			assertTrue(!(s="aB").equals(p(g, "A", s)));
			assertTrue(!(s="aBB").equals(p(g, "A", s)));
			assertTrue(!(s="aABB").equals(p(g, "A", s)));
			assertEq(s="aA", p(g, "B", s));
			assertEq(s="aAB", p(g, "B", s));
			assertEq(s="aBA", p(g, "B", s));
			assertTrue(!(s="aB").equals(p(g, "B", s)));
			assertTrue(!(s="a").equals(p(g, "B", s)));
			assertTrue(!(s="aABA").equals(p(g, "B", s)));
			assertEq(s="aB", p(g, "C", s));
			assertEq(s="aAB", p(g, "C", s));
			assertEq(s="aBA", p(g, "C", s));
			assertTrue(!(s="aA").equals(p(g, "C", s)));
			assertTrue(!(s="a").equals(p(g, "C", s)));
			assertTrue(!(s="aABA").equals(p(g, "C", s)));
			assertEq(s="aAB", p(g, "D", s));
			assertEq(s="aBA", p(g, "D", s));
			assertEq(s="aA", p(g, "D", s));
			assertEq(s="aB", p(g, "D", s));
			assertEq(s="a", p(g, "D", s));
			assertTrue(!(s="aC").equals(p(g, "D", s)));
			assertTrue(!(s="aABB").equals(p(g, "D", s)));
			assertEq(s="aABY", p(g, "E", s));
			assertEq(s="YaAB", p(g, "E", s));
			assertEq(s="Y", p(g, "E", s));
			assertEq(s="aAB", p(g, "E", s));
			assertEq(s="aABY", p(g, "F", s));
			assertEq(s="YaAB", p(g, "F", s));
			assertEq(s="Y", p(g, "F", s));
			assertEq(s="aAB", p(g, "F", s));
			assertEq(s="aABY", p(g, "G", s));
			assertEq(s="YaAB", p(g, "G", s));
			assertTrue(!(s="Y").equals(p(g, "G", s)));
			assertTrue(!(s="aAB").equals(p(g, "G", s)));
			assertEq(s="aABYX", p(g, "H", s));
			assertEq(s="aABYXZ", p(g, "I", s));
			assertEq(s="YaBAX", p(g, "J", s));
			assertEq(s="Z", p(g, "J", s));
			assertEq(s="aBAYX", p(g, "K", s));
			assertEq(s="YaABXZ", p(g, "K", s));
			assertEq(s="Z", p(g, "K", s));
			assertEq(s="ABZ", p(g, "L", s));
			assertEq(s="BZA", p(g, "L", s));
			assertEq(s="AB", p(g, "M", s));
			assertEq(s="BA", p(g, "M", s));
			assertEq(s="Z", p(g, "M", s));
			assertEq(s="Z", p(g, "M", s));
			assertEq(s="AB", p(g, "N", s));
			assertEq(s="BA", p(g, "N", s));
			assertEq(s="Z", p(g, "N", s));
			assertEq(s="Z", p(g, "N", s));
			assertEq(s="BA", p(g, "O", s));
			assertEq(s="DC", p(g, "O", s));
			assertEq(s="BA", p(g, "P", s));
			assertEq(s="DC", p(g, "P", s));
/**/
////////////////////////////////////////////////////////////////////////////////
			bnf =
"%define $x: $test.common.bnf.TestBNF.xxx\n"+
"digit ::= [0-9]" +
"signedInteger::= [-+]? digit+ $x\n"+
"signedIntegers::= signedInteger (',' signedInteger)*\n";
			g = BNFGrammar.compile(bnf);
			g.setUserObject(this);
			assertEq("-12345,3", p(g, "signedIntegers", "-12345,3"));
			assertEq(2, _stack.size());
			assertEq("3", _stack.pop());
			assertEq("-12345", _stack.pop());
			assertTrue(g.isEOS());
			g = BNFGrammar.compile(bnf);
			g.setUserObject(this);
			_stack.clear();
			if (!g.parse("-12345,++3", "signedIntegers")) {
				fail(_stack.toString());
			} else {
				assertFalse(g.isEOS());
				assertEq("-12345", g.getParsedString());
				assertEq(",++3", g.getUnparsedSourceBuffer());
				assertEq(1, _stack.size());
				assertEq("-12345", _stack.pop());
			}
////////////////////////////////////////////////////////////////////////////////
			bnf =
"%define $x: $test.common.bnf.TestBNF.yyy\n"+
"digit ::= [0-9]" +
"signedInteger::= [-+]? digit+ $x\n"+
"signedIntegers::= signedInteger (',' signedInteger)*\n";
			g = BNFGrammar.compile(null, bnf, null);
			g.setUserObject(null);
			assertTrue(g.parse("-12345,3","signedIntegers"));
			assertEq("-12345,3", g.getParsedString());
			assertEq("signedInteger:-12345,p:0\nsignedInteger:3,p:7",
				g.getUserObject());
			bnf =
"M      ::= [#9#10#13 ]*   /*skip white spaces*/\n" +
"OD     ::= M \",\" M      /*separator of values*/\n" +
"LnPrd  ::= [1-9] | [1-4] [0-9]\n" +
"Month  ::= [1-9] | [1] [0-2]\n" +
"Months ::= Month ( OD Month )*\n" +
"YPrd   ::= LnPrd? \"Y\" \"(\" Months \")\"\n" +
"MDay   ::= [1-9] | [1-2] [0-9] | [3] [0-1] | \"-1\"\n" +
"MDays  ::= MDay (OD MDay)*\n" +
"MPrd   ::= LnPrd? \"M\" \"(\" MDays \")\"\n" +
"WDay   ::= [0-7] | \"-1\"\n" +
"WDays  ::= WDay (OD WDay)*\n" +
"WPrd   ::= LnPrd? \"W\" \"(\" WDays \")\"\n" +
"TimeH  ::= [0-1] [0-9] | [2] [0-3]\n" +
"TimeM  ::= [0-5] [0-9]\n" +
"Time   ::= TimeH \":\" TimeM\n" +
"Times  ::= Time (OD Time)*\n" +
"DPrd   ::= LnPrd? \"D\" \"(\" Times \")\"\n" +
"HPrd   ::= LnPrd \"H\"\n" +
"MinPrd ::= LnPrd \"Min\"\n" +
"MinPrd1::= MinPrd ( OD (HPrd1 | DPrd1 | WPrd1 | MPrd1 | YPrd) )?\n" +
"HPrd1  ::= HPrd (OD (DPrd1 | WPrd1 | MPrd1 | YPrd))?\n" +
"DPrd1  ::= DPrd (OD (WPrd1 | MPrd1 | YPrd))?\n" +
"WPrd1  ::= WPrd (OD (MPrd1 | YPrd))?\n" +
"MPrd1  ::= MPrd (OD YPrd)?\n" +
"r ::= (MinPrd1 | HPrd1 | DPrd1 | WPrd1 | MPrd1 | YPrd?)?\n";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("2H", p(g, "r" , "2H"));
			assertEq("D(09:00)", p(g, "r" , "D(09:00)"));
			assertEq("D(10:00),W(1,2,3,4,5,6)",
				p(g, "r" , "D(10:00),W(1,2,3,4,5,6)"));
			assertEq("D(14:45,20:00),W(1,2,3,4,5)",
				p(g, "r" , "D(14:45,20:00),W(1,2,3,4,5)"));
			assertEq("D(11:00),W(1)", p(g, "r" , "D(11:00),W(1)"));
			assertEq("1W(2)", p(g, "r" , "1W(2)"));
			assertEq("D(12:00),W(-1)", p(g, "r" , "D(12:00),W(-1)"));
			assertEq("D(12:00),M(-1)", p(g, "r" , "D(12:00),M(-1)"));
			assertEq("D(07:00),M(20)", p(g, "r" , "D(07:00),M(20)"));
			assertEq("M(3)", p(g, "r" , "M(3)"));
			assertEq("M(9)", p(g, "r" , "M(9)"));
			assertEq("M(31)", p(g, "r" , "M(31)"));
////////////////////////////////////////////////////////////////////////////////
			bnf =
"S ::= ' '*\n"+
"Letter ::= [a-zA-Z]\n"+
"Digit ::= [0-9]\n"+
"Identifier ::= (BaseIdentifier ( ':' BaseIdentifier)? )\n"+
"BaseIdentifier ::= (( Letter | '_' | '$')  ( Letter | Digit | '_' )*)\n"+
"RawIdentifier ::= ( Letter | '_')  ( Letter | Digit | '_' | '-' | ':')*\n"+
"QIdentifier ::= RawIdentifier('.'RawIdentifier)+ | RawIdentifier\n"+
"X ::= QIdentifier ((S? '.' S?) Identifier)? (S? '(' S? ')')? S? '?'\n";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("a ?", p(g, "X", "a ?"));
			assertEq("a:b-c.d ?", p(g, "X", "a:b-c.d ?"));
			assertEq("a:b-c.d.e() ?", p(g, "X", "a:b-c.d.e() ?"));
			assertEq("a:b-c.d. e ( ) ?", p(g, "X", "a:b-c.d. e ( ) ?"));
			assertEq("a:b-c.d .e ( ) ?", p(g, "X", "a:b-c.d .e ( ) ?"));
			assertEq("a:b-c.d . e ( ) ?", p(g, "X", "a:b-c.d . e ( ) ?"));
////////////////////////////////////////////////////////////////////////////////
			bnf = FUtils.readString(
				new File(getDataDir() + "TestXPath2.bnf"), "windows-1250");
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("a/b", p(g, "XPath", "a/b"));
			assertEq("/a/b", p(g, "XPath", "/a/b"));
			assertEq("/*", p(g, "XPath", "/*"));
			assertEq("/*[@a]", p(g, "XPath", "/*[@a]"));
			assertEq("/*[@a='1']/text()", p(g,"XPath","/*[@a='1']/text()"));
			assertEq("//a/text()", p(g, "XPath", "//a/text()"));
////////////////////////////////////////////////////////////////////////////////
			bnf = "Comment ::= \"/*\" ( [^*]+ | \"*\" [^/] )* \"*/\"\n"+
			"S ::= ( ' ' | Comment )+\n"+
			"A ::= S? 'X' S?";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("X /*jmeno - znaky*/  ",
				p(g, "A" , "X /*jmeno - znaky*/  "));
			bnf = "A::=B-C*C::='c'B::=[a-z]";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("b", p(g, "A" , "b"));
			assertFalse(g.parse("c", "A"));
			bnf = "A::=(B-']]>')*B::=[#x20-#xD7FF]";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("C ", p(g, "A" , "C ]]>"));
			bnf = "A ::= '<!--' ((B - '-') | ('-' (B - '-')))* '-->'\n" +
				"B ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD]";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("<!-- c - C -->", p(g, "A" , "<!-- c - C -->"));
			bnf =
"%define $value:         $test.common.bnf.TestBNF.myValue\n"+
"%define $operator:      $test.common.bnf.TestBNF.myOperator\n"+
"%define $mypush:        $test.common.bnf.TestBNF.myPushOperator\n"+
"%define $assign:        $test.common.bnf.TestBNF.myPushOperator('=')\n"+
"%define $unaryOperator: $test.common.bnf.TestBNF.myUnaryOperator\n"+
"%define $unaryMinus:    $test.common.bnf.TestBNF.myPushOperator(',-')\n"+
"%define $exec:          $test.common.bnf.TestBNF.exec\n"+
"%define $mytest:        $test.common.bnf.TestBNF.myTest(123,'a\"\n\\')\n"+
"\n"+
"/*BNF grammar*/\n"+
"set1 /*comment4*/ ::= /*comment5*/[a]/*comment6*/\n"+
"set1a ::= [^a]?\n"+
"set2 ::= [<^&#x@\"]\n" +
"set2a ::= [^^<&#x@\"]\n" +
"set3 ::= [-^'()+,./:=?;!*#x@$_%]\n" +
"set3a ::= [^-^'()+,./:=?;!*#x@$_%]\n" +
"set4 ::= [a-zA-Z0-9]\n" +
"set4a ::= [^a-zA-Z]\n"+
"set5 ::=  [#x20-#xFF]\n" +
"set5a ::=  [^#x20-#xFF]\n" +
"quantif1 ::= 'A'{3}\n"+
"quantif2 ::= 'A'{3,4}\n"+
"quantif3 ::= 'A' { 3 , * }\n"+
"tab ::= #9\n"+
"backslash ::= '\\'\n"+
"NL ::= #10\n"+
"CR ::= #13\n"+
"FF ::= #12\n"+
"ch01 ::= #1\n"+
"testMethod::=$mytest\n"+
"S ::= [#9#10#13 ]* /*skipped white spaces*/\n"+
"boolean::= ('true' | 'false')\n"+
"string::= \"'\" (\"''\" | [^']+)+ \"'\" | '\"' ('\"\"' | [^\"]+)+ '\"'\n"+
"integer ::= [0-9]+\n"+
"float ::= [0-9]+ ('.' [0-9]+ ('E' [-+]? [0-9]+ )? | 'E' [-+]? [0-9]+ )\n"+
"identifier::= [a-zA-Z][a-zA-Z0-0]*\n"+
"e::= expression $exec\n"+
"expression::= S simpleExpr\n"+
"simpleExpr::= factor S ( [-+]$operator S factor $mypush)*\n"+
"factor::= term S ( [*/]$operator S term $mypush)*\n"+
"term::= S ('-'  S value $unaryMinus) | ('+'? S) value\n"+
"value::= ('(' S expression S ')')\n" +
"  | ((float | integer | boolean | string | identifier) $value)\n"+
"assignment::= identifier  $value S '=' $assign e\n"+
"statement::= S? (assignment  S)? ';' \n"+
"program::= statement+ \n"+
"";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("x=x-555.1", p(g, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("a", p(g, "set1", "a"));
			assertFalse(g.parse("b", "set1"));
			assertEq("^", p(g, "set2", "^"));
			assertEq("\"", p(g, "set2", "\""));
			assertEq("#", p(g, "set2", "#"));
			assertFalse(g.parse("^", "set2a"));
			assertFalse(g.parse("\"", "set2a"));
			assertFalse(g.parse("#", "set2a"));
			assertEq("?", p(g, "set2a", "?"));
			assertEq("-", p(g, "set3", "-"));
			assertEq("^", p(g, "set3", "^"));
			assertEq("#", p(g, "set3", "#"));
			assertEq("_", p(g, "set3", "_"));
			assertTrue(p(g, "set3a", "-").startsWith("set3a failed"));
			assertTrue(p(g, "set3a", "^").startsWith("set3a failed"));
			assertTrue(p(g, "set3a", "#").startsWith("set3a failed"));
			assertTrue(p(g, "set3a", "_").startsWith("set3a failed"));
			assertEq("a", p(g, "set3a", "a"));
			assertFalse(g.parse("", "set3a"));
			assertEq("AAA", p(g, "quantif1", "AAA"));
			assertEq("AAA", p(g, "quantif1", "AAAA"));
			assertEq("AAA", p(g, "quantif2", "AAA"));
			assertEq("AAAA", p(g, "quantif2", "AAAA"));
			assertEq("AAAA", p(g, "quantif2", "AAAAA"));
			assertEq("AAA", p(g, "quantif3", "AAA"));
			assertEq("AAAA", p(g, "quantif3", "AAAA"));
			assertEq("AAAAA", p(g, "quantif3", "AAAAA"));
			assertFalse(g.parse("AA", "quantif1"));
			assertEq("", p(g, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",p(g, "S", " \t \n -123  456"));
			assertEq("", p(g, "S")); //nothing parsed
			assertEq("  ", p(g, "S", "  "));
			assertEq("\t", p(g, "tab", "\t blabla \n"));
			assertEq("-123",p(g, "e", "-123"));
			assertEq(-123, _result, printStack());
			assertEq("456", p(g, "e", "456"));
			assertEq(456, _result, printStack());
			assertEq("\n\n  -\n123+\t456", p(g, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g, "e", "+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(44, _result, printStack());
			assertEq("1", p(g, "e", "1"));
			assertEq(1, _result, printStack());
			assertEq("+1", p(g, "e", "+1"));
			assertEq(1, _result, printStack());
			assertEq("-1", p(g, "e", "-1"));
			assertEq(-1, _result, printStack());
			assertEq("-(-1)", p(g, "e", "-(-1)"));
			assertEq(1, _result, printStack());
			assertEq("+(-1)", p(g, "e", "+(-1)"));
			assertEq(-1, _result, printStack());
			assertEq("-(+1)", p(g, "e", "-(+1)"));
			assertEq(-1, _result, printStack());
			assertEq("+(-(((1))))", p(g, "e", "+(-(((1))))"));
			assertEq(-1, _result, printStack());
			assertEq("1-x", p(g, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", p(g, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("\"x-1\"", p(g, "e", "\"x-1\""));
			assertEq("x-1", _result, printStack());
			assertEq("'x-\"1'", p(g, "e", "'x-\"1'"));
			assertEq("x-\"1", _result, printStack());
			assertEq("x=x-555", p(g, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", p(g, "program",	"; xxx"));
			assertEq("x=x", p(g, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", p(g, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;",
				p(g, "program", "i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", p(g, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", p(g, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", p(g, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1", p(g, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				p(g, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", p(g, "assignment", "x=true"));
			assertEq(Boolean.TRUE, _result, printStack());
			assertEq(Boolean.TRUE, _variables.get("x"));
////////////////////////////////////////////////////////////////////////////////
			//extension
			bnf =
"%define $mydate:        $datetime(\"d.M.yyyy\")\n"+
"%define $myInteger:     $test.common.bnf.TestBNF.myInteger\n"+
"%define $myInteger1:    $test.common.bnf.TestBNF.myInteger(-10,10)\n"+
"%define $myDatetime:    $datetime('d.M.yyyy')\n"+
"\n"+
"rule1a ::=  'A' 'B' | 'C' 'D'\n" +
"rule1b ::=  ('A' 'B') | ('C' 'D')\n" +
"lid ::= [a-z]+ - 'ab'\n"+
"lid1a ::= [a-z]+ - 'ab' - 'de' [0-9]+ - '12'\n"+
"lid1b ::= ([a-z]+ - 'ab') ([0-9]+ - '12')\n"+
"lid2a ::= [a-z]+ - 'ab' | [0-9]+ - '12'\n"+
"lid2b ::= ([a-z]+ - 'ab') | ([0-9]+ - '12')\n" +
"lid3 ::= [a-z]+ - 'ab' - 'cd'\n" +
"testBuildIn ::= $integer S $float S $datetime? \n"+
"myDatetime ::= $myDatetime\n"+
"mydate  ::= $mydate (S? \",\" S? mydate)*\n"+
"myInteger ::= $myInteger\n"+
"myInteger1 ::= $myInteger1\n"+
"myInteger2 ::= $integer\n"+
"";
			g1 = BNFGrammar.compile(g, bnf, null);
			assertEq("a", p(g1, "set1", "a"));
			assertEq("x='ab'+1+2", p(g1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("i=x-555;\nj=5; j = i - j;", p(g1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("AB", p(g1, "rule1a", "AB"));
			assertEq("CD", p(g1, "rule1a", "CD"));
			assertEq("AB", p(g1, "rule1b", "AB"));
			assertEq("CD", p(g1, "rule1b", "CD"));
			assertEq("cd", p(g1, "lid", "cd"));
			assertEq("abc", p(g1, "lid", "abc"));
			assertFalse(g1.parse("ab", "lid"));
			assertEq("cd23", p(g1, "lid1a", "cd23"));
			assertEq("cd23", p(g1, "lid1b", "cd23"));
			assertFalse(g1.parse("ab12", "lid1a"));
			assertFalse(g1.parse("ab12", "lid1b"));
			assertFalse(g1.parse("de12", "lid1b"));
			assertEq("cd", p(g1, "lid2a", "cd"));
			assertEq("23", p(g1, "lid2b", "23"));
			assertFalse(g1.parse("ab", "lid2a"));
			assertFalse(g1.parse("12", "lid2a"));
			assertFalse(g1.parse("12", "lid2b"));
			assertFalse(g1.parse("ab", "lid2b"));
			assertEq("xy", p(g1, "lid3", "xy"));
			assertFalse(g1.parse("ab", "lid3"));
			assertFalse(g1.parse("cd", "lid3"));
////////////////////////////////////////////////////////////////////////////////
			bnf =
"%define $myDatetime: $datetime(\"d.M.yyyy\")\n" +
"%define $myInteger: $test.common.bnf.TestBNF.myInteger\n" +
"%define $unaryOperator: $test.common.bnf.TestBNF.myUnaryOperator\n" +
"%define $assign: $test.common.bnf.TestBNF.myPushOperator(\"=\")\n" +
"%define $mydate: $datetime(\"d.M.yyyy\")\n" +
"%define $unaryMinus: $test.common.bnf.TestBNF.myPushOperator(\",-\")\n" +
"%define $value: $test.common.bnf.TestBNF.myValue\n" +
"%define $exec: $test.common.bnf.TestBNF.exec\n" +
"%define $myInteger1: $test.common.bnf.TestBNF.myInteger(-10,10)\n" +
"%define $mytest: $test.common.bnf.TestBNF.myTest(123,\"a\" #34 #10 \"\\\")\n" +
"%define $operator: $test.common.bnf.TestBNF.myOperator\n" +
"%define $mypush: $test.common.bnf.TestBNF.myPushOperator\n" +
"\n" +
"set1 ::= [a] \n" +
"set1a ::= [^a]? \n" +
"set2 ::= [<^&#x@\"] \n" +
"set2a ::= [^^<&#x@\"] \n" +
"set3 ::= [-^'()+,./:=?;!*#x@$_%] \n" +
"set3a ::= [^-^'()+,./:=?;!*#x@$_%] \n" +
"set4 ::= [a-zA-Z0-9] \n" +
"set4a ::= [^a-zA-Z] \n" +
"set5 ::= [ -?] \n" +
"set5a ::= [^ -?] \n" +
"quantif1 ::= \"A\"{3} \n" +
"quantif2 ::= \"A\"{3,4} \n" +
"quantif3 ::= \"A\"{3,} \n" +
"tab ::= #9 \n" +
"backslash ::= \"\\\" \n" +
"NL ::= #10 \n" +
"CR ::= #13 \n" +
"FF ::= #12 \n" +
"ch01 ::= #1 \n" +
"testMethod ::= $mytest \n" +
"S ::= [#9#10#13 ]* \n" +
"boolean ::= ( \"true\" | \"false\" ) \n" +
"string ::= (\"'\" (\"''\" | [^']+)+ \"'\" | #34 ((#34 #34) | [^\"]+)+ #34)\n" +
"integer ::= [0-9]+ \n" +
"float ::= [0-9]+ (\".\" [0-9]+ (\"E\" [-+]? [0-9]+)? | \"E\" [-+]? [0-9]+)\n" +
"identifier ::= [a-zA-Z] [a-zA-Z0-0]* \n" +
"e ::= expression $exec \n" +
"expression ::= S simpleExpr \n" +
"simpleExpr ::= factor S ( [-+] $operator S factor $mypush )* \n" +
"factor ::= term S ( [*/] $operator S term $mypush )* \n" +
"term ::= ( S \"-\" S value $unaryMinus | \"+\"? S value ) \n" +
"value ::= ( \"(\" S expression S \")\" | ( float | integer | boolean\n"+
"  | string | identifier ) $value ) \n" +
"assignment ::= identifier $value S \"=\" $assign e \n" +
"statement ::= S? ( assignment S )? \";\" \n" +
"program ::= statement+ \n" +
"rule1a ::= ( \"A\" \"B\" | \"C\" \"D\" ) \n" +
"rule1b ::= ( \"A\" \"B\" | \"C\" \"D\" ) \n" +
"lid ::= [a-z]+ - \"ab\"  \n" +
"lid1a ::= [a-z]+ - \"ab\" - \"de\"  [0-9]+ - \"12\"  \n" +
"lid1b ::= [a-z]+ - \"ab\"  [0-9]+ - \"12\"  \n" +
"lid2a ::= ( [a-z]+ - \"ab\"  | [0-9]+ - \"12\"  ) \n" +
"lid2b ::= ( [a-z]+ - \"ab\"  | [0-9]+ - \"12\"  ) \n" +
"lid3 ::= [a-z]+ - \"ab\" - \"cd\"  \n" +
"testBuildIn ::= $integer S $float S $datetime? \n" +
"myDatetime ::= $myDatetime \n" +
"mydate ::= $mydate ( S? \",\" S? mydate )* \n" +
"myInteger ::= $myInteger \n" +
"myInteger1 ::= $myInteger1 \n" +
"myInteger2 ::= $integer \n" +
"";
			g = BNFGrammar.compile(null, bnf, null);
			assertEq("a", p(g, "set1", "a"));
			assertFalse(g.parse("b", "set1"));
			assertEq("^", p(g, "set2", "^"));
			assertEq("\"", p(g, "set2", "\""));
			assertEq("#", p(g, "set2", "#"));
			assertFalse(g.parse("^", "set2a"));
			assertFalse(g.parse("\"", "set2a"));
			assertFalse(g.parse("#", "set2a"));
			assertEq("?", p(g, "set2a", "?"));
			assertEq("-", p(g, "set3", "-"));
			assertEq("^", p(g, "set3", "^"));
			assertEq("#", p(g, "set3", "#"));
			assertEq("_", p(g, "set3", "_"));
			assertTrue(p(g, "set3a", "-").startsWith("set3a failed"));
			assertTrue(p(g, "set3a", "^").startsWith("set3a failed"));
			assertTrue(p(g, "set3a", "#").startsWith("set3a failed"));
			assertTrue(p(g, "set3a", "_").startsWith("set3a failed"));
			assertEq("a", p(g, "set3a", "a"));
			assertFalse(g1.parse("", "set3a"));
			assertEq("", p(g, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",p(g, "S", " \t \n -123  456"));
			assertEq("", p(g, "S")); //nothing parsed
			assertEq("\t", p(g, "tab", "\txxx"));
			assertEq("-123",p(g, "e", "-123"));
			assertEq(-123, _result, printStack());
			assertEq("456", p(g, "e", "456"));
			assertEq(456, _result, printStack());
			assertEq("\n\n  -\n123+\t456", p(g, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g, "e", "+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(44, _result, printStack());
			assertEq("-1", p(g, "e", "-1"));
			assertEq(-1, _result, printStack());
			assertEq("+1", p(g, "e", "+1"));
			assertEq(1, _result, printStack());
			assertEq("1-x", p(g, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", p(g, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("x=x-555", p(g, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", p(g, "program",	"; xxx"));
			assertEq("x=x", p(g, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", p(g, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", p(g, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", p(g, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", p(g, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", p(g, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1", p(g, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				p(g, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", p(g, "assignment", "x=true"));
			assertEq(Boolean.TRUE, _result, printStack());
			assertEq(Boolean.TRUE, _variables.get("x"));
			assertEq("a", p(g, "set1", "a"));
			assertEq("AB", p(g, "rule1a", "AB"));
			assertEq("CD", p(g, "rule1a", "CD"));
			assertEq("AB", p(g, "rule1b", "AB"));
			assertEq("CD", p(g, "rule1b", "CD"));
			assertEq("cd", p(g, "lid", "cd"));
			assertFalse(g1.parse("ab", "lid"));
			assertEq("cd23", p(g, "lid1a", "cd23"));
			assertEq("cd23", p(g, "lid1b", "cd23"));
			assertFalse(g1.parse("ab12", "lid1b"));
			assertFalse(g1.parse("de12", "lid1b"));
			assertEq("cd", p(g, "lid2a", "cd"));
			assertEq("23", p(g, "lid2b", "23"));
			assertFalse(g1.parse("ab", "lid2a"));
			assertFalse(g1.parse("12", "lid2a"));
			assertFalse(g1.parse("ab", "lid2b"));
			assertFalse(g1.parse("12", "lid2b"));
			assertEq("xy", p(g, "lid3", "xy"));
			assertFalse(g1.parse("ab", "lid3"));
			assertFalse(g1.parse("cd", "lid3"));
			assertEq("123", p(g, "myInteger2", "123"));
			assertEq("123", p(g, "myInteger", "123"));
			assertFalse(g1.parse("x123", "myInteger"));
			assertTrue(p(g,
				"myInteger1", "123").indexOf("greater then max") > 0);
			assertEq("123 3.14 1999-01-02T23:10:54+01:00", p(g,
				"testBuildIn", "123 3.14 1999-01-02T23:10:54+01:00"));
			assertEq("2.12.1945", p(g, "myDatetime", "2.12.1945"));
			assertEq("1.1.1990, 6.12.1945",
				p(g, "mydate", "1.1.1990, 6.12.1945"));
////////////////////////////////////////////////////////////////////////////////
			// BNF extension
			bnf =
"myDuration ::= $duration (S? $duration)* \n"+
"xmlname ::= $xmlName \n"+
"xmlnames ::= $xmlName (S $xmlName)*\n"+
"ncname ::= $ncName \n"+
"ncnames ::= $ncName (S $ncName)*\n"+
"nmtoken ::= $nmToken \n"+
"nmtokens ::= nmtoken (S nmtoken)*\n"+
"";
			g1 = BNFGrammar.compile(g, bnf, null);
			assertEq("a", p(g1, "set1", "a"));
			assertFalse(g1.parse("b", "set1"));
			assertFalse(g1.parse("b", "set1"));
			assertEq("^", p(g1, "set2", "^"));
			assertEq("\"", p(g1, "set2", "\""));
			assertEq("#", p(g1, "set2", "#"));
			assertFalse(g1.parse("^", "set2a"));
			assertFalse(g1.parse("\"", "set2a"));
			assertFalse(g1.parse("#", "set2a"));
			assertEq("?", p(g1, "set2a", "?"));
			assertEq("-", p(g1, "set3", "-"));
			assertEq("^", p(g1, "set3", "^"));
			assertEq("#", p(g1, "set3", "#"));
			assertEq("_", p(g1, "set3", "_"));
			assertTrue(p(g1,"set3a", "-").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "^").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "#").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "_").startsWith("set3a failed"));
			assertEq("a", p(g1, "set3a", "a"));
			assertFalse(g1.parse("", "set3a"));
			assertEq("AAA", p(g1, "quantif1", "AAA"));
			assertEq("AAA", p(g1, "quantif1", "AAAA"));
			assertEq("AAA", p(g1, "quantif2", "AAA"));
			assertEq("AAAA", p(g1, "quantif2", "AAAA"));
			assertEq("AAAA", p(g1, "quantif2", "AAAAA"));
			assertEq("AAA", p(g1, "quantif3", "AAA"));
			assertEq("AAAA", p(g1, "quantif3", "AAAA"));
			assertEq("AAAAA", p(g1, "quantif3", "AAAAA"));
			assertFalse(g1.parse("AA", "quantif1"));
			assertEq("", p(g1, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",p(g1, "S", " \t \n -123  456"));
			assertEq("", p(g1, "S")); //nothing parsed
			assertEq("\t", p(g1, "tab", "\txxx"));
			assertEq("\n\n  -\n123+\t456",p(g1, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g1, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g1, "e", "+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(44, _result, printStack());
			assertEq("-1", p(g, "e", "-1"));
			assertEq(-1, _result, printStack());
			assertEq("+1", p(g, "e", "+1"));
			assertEq(1, _result, printStack());
			assertEq("1-x", p(g1, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", p(g1, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("x=x-555", p(g1, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", p(g1, "program",	"; xxx"));
			assertEq("x=x", p(g1, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", p(g1, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", p(g1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", p(g1, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", p(g1, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", p(g1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1",
				p(g1, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				p(g1, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", p(g1, "assignment", "x=true"));
			assertEq(Boolean.TRUE, _result, printStack());
			assertEq(Boolean.TRUE, _variables.get("x"));
			assertEq("123 3.14 1999-01-02T23:10:54+01:00", p(g,
				"testBuildIn", "123 3.14 1999-01-02T23:10:54+01:00"));
			assertEq("2.12.1945", p(g, "myDatetime", "2.12.1945"));
			assertEq("2.12.1945", p(g1, "myDatetime", "2.12.1945"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				p(g1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("null", "" + g1.popObject());
			assertEq("a-b c:d.e f1",p(g1,"xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				p(g1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", p(g1, "nmtokens", "123"));
			assertEq("a", p(g1, "set1", "a"));
			assertEq("x='ab'+1+2", p(g1, "assignment", "x='ab'+1+2"));
			assertEq("i=x-555;\nj=5; j = i - j;",
				p(g1, "program", "i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("AB", p(g1, "rule1a", "AB"));
			assertEq("CD", p(g1, "rule1a", "CD"));
			assertEq("AB", p(g1, "rule1b", "AB"));
			assertEq("CD", p(g1, "rule1b", "CD"));
			assertEq("cd", p(g1, "lid", "cd"));
			assertFalse(g1.parse("ab", "lid"));
			assertEq("cd23", p(g1, "lid1a", "cd23"));
			assertEq("cd23", p(g1, "lid1b", "cd23"));
			assertFalse(g1.parse("ab12", "lid1a"));
			assertFalse(g1.parse("ab12", "lid1b"));
			assertFalse(g1.parse("de12", "lid1b"));
			assertEq("cd", p(g1, "lid2a", "cd"));
			assertEq("23", p(g1, "lid2b", "23"));
			assertFalse(g1.parse("ab", "lid2b"));
			assertFalse(g1.parse("12", "lid2a"));
			assertFalse(g1.parse("12", "lid2b"));
			assertEq("xy", p(g1, "lid3", "xy"));
			assertFalse(g1.parse("ab", "lid3"));
			assertFalse(g1.parse("cd", "lid3"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				p(g1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1", p(g1, "xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				p(g1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", p(g1, "nmtokens", "123"));
			assertEq("1.1.1990, 6.12.1945",
				p(g, "mydate", "1.1.1990, 6.12.1945"));
			g1 = BNFGrammar.compile(null, g1.toString(), null);
			assertEq("a", p(g1, "set1", "a"));
			assertFalse(g1.parse("b", "set1"));
			assertEq("^", p(g1, "set2", "^"));
			assertEq("\"", p(g1, "set2", "\""));
			assertEq("#", p(g1, "set2", "#"));
			assertFalse(g1.parse("^", "set2a"));
			assertFalse(g1.parse("\"", "set2a"));
			assertFalse(g1.parse("#", "set2a"));
			assertEq("?", p(g1, "set2a", "?"));
			assertEq("-", p(g1, "set3", "-"));
			assertEq("^", p(g1, "set3", "^"));
			assertEq("#", p(g1, "set3", "#"));
			assertEq("_", p(g1, "set3", "_"));
			assertTrue(p(g1,"set3a", "-").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "^").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "#").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "_").startsWith("set3a failed"));
			assertEq("a", p(g1, "set3a", "a"));
			assertFalse(g1.parse("", "set3a"));
			assertEq("AAA", p(g1, "quantif1", "AAA"));
			assertEq("AAA", p(g1, "quantif1", "AAAA"));
			assertEq("AAA", p(g1, "quantif2", "AAA"));
			assertEq("AAAA", p(g1, "quantif2", "AAAA"));
			assertEq("AAAA", p(g1, "quantif2", "AAAAA"));
			assertEq("AAA", p(g1, "quantif3", "AAA"));
			assertEq("AAAA", p(g1, "quantif3", "AAAA"));
			assertEq("AAAAA", p(g1, "quantif3", "AAAAA"));
			assertFalse(g1.parse("AA", "quantif1"));
			assertEq("", p(g1, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",p(g1, "S", " \t \n -123  456"));
			assertEq("", p(g1, "S")); //nothing parsed
			assertEq("\t", p(g1, "tab", "\txxx"));
			assertEq("\n\n  -\n123+\t456", p(g1,"e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				p(g1, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+(123-5*(3-2+6))/2",
				p(g1, "e", "+(123-5*(3-2+6))/2::"));
			assertEq(44, _result, printStack());
			assertEq("1-x", p(g1, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", p(g1, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("x=x-555", p(g1, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", p(g1, "program", "; xxx"));
			assertEq("x=x", p(g1, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", p(g1, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", p(g1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", p(g1, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", p(g1, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", p(g1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1", p(g1, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				p(g1, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", p(g1, "assignment", "x=true"));
			assertEq(true, _result, printStack());
			assertEq(true, _variables.get("x"));
			assertEq("123 3.14 1999-01-02T23:10:54+01:00", p(g,
				"testBuildIn", "123 3.14 1999-01-02T23:10:54+01:00"));
			assertEq("2.12.1945", p(g, "myDatetime", "2.12.1945"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				p(g1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1",p(g1,"xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				p(g1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", p(g1, "nmtokens", "123"));
			assertEq("a", p(g1, "set1", "a"));
			assertEq("x='ab'+1+2", p(g1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("i=x-555;j=5;j=i-j;",
				p(g1, "program", "i=x-555;j=5;j=i-j;xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("AB", p(g1, "rule1a", "AB"));
			assertEq("CD", p(g1, "rule1a", "CD"));
			assertEq("AB", p(g1, "rule1b", "AB"));
			assertEq("CD", p(g1, "rule1b", "CD"));
			assertEq("cd", p(g1, "lid", "cd"));
			assertFalse(g1.parse("ab", "lid"));
			assertEq("cd23", p(g1, "lid1a", "cd23"));
			assertEq("cd23", p(g1, "lid1b", "cd23"));
			assertFalse(g1.parse("ab12", "lid1a"));
			assertFalse(g1.parse("ab12", "lid1b"));
			assertFalse(g1.parse("de12", "lid1b"));
			assertEq("cd", p(g1, "lid2a", "cd"));
			assertEq("23", p(g1, "lid2b", "23"));
			assertFalse(g1.parse("ab", "lid2a"));
			assertFalse(g1.parse("ab", "lid2b"));
			assertFalse(g1.parse("12", "lid2a"));
			assertFalse(g1.parse("12", "lid2b"));
			assertEq("xy", p(g1, "lid3", "xy"));
			assertFalse(g1.parse("ab", "lid3"));
			assertFalse(g1.parse("cd", "lid3"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				p(g1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1", p(g1, "xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				p(g1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", p(g1, "nmtokens", "123"));
			assertEq("a", p(g1, "set1", "a"));
			assertFalse(g1.parse("b", "set1"));
			assertEq("^", p(g1, "set2", "^"));
			assertEq("\"", p(g1, "set2", "\""));
			assertEq("#", p(g1, "set2", "#"));
			assertFalse(g1.parse("^", "set2a"));
			assertFalse(g1.parse("\"", "set2a"));
			assertFalse(g1.parse("#", "set2a"));
			assertEq("?", p(g1, "set2a", "?"));
			assertEq("-", p(g1, "set3", "-"));
			assertEq("^", p(g1, "set3", "^"));
			assertEq("#", p(g1, "set3", "#"));
			assertEq("_", p(g1, "set3", "_"));
			assertTrue(p(g1,"set3a", "-").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "^").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "#").startsWith("set3a failed"));
			assertTrue(p(g1,"set3a", "_").startsWith("set3a failed"));
			assertEq("a", p(g1, "set3a", "a"));
			assertFalse(g1.parse("", "set3a"));
			assertEq("AAA", p(g1, "quantif1", "AAA"));
			assertEq("AAA", p(g1, "quantif1", "AAAA"));
			assertEq("AAA", p(g1, "quantif2", "AAA"));
			assertEq("AAAA", p(g1, "quantif2", "AAAA"));
			assertEq("AAAA", p(g1, "quantif2", "AAAAA"));
			assertEq("AAA", p(g1, "quantif3", "AAA"));
			assertEq("AAAA", p(g1, "quantif3", "AAAA"));
			assertEq("AAAAA", p(g1, "quantif3", "AAAAA"));
			assertFalse(g1.parse("AA", "quantif1"));
			assertEq("", p(g1, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",p(g1, "S", " \t \n -123  456"));
			assertEq("", p(g1, "S")); //nothing parsed
			assertEq("\t", p(g1, "tab", "\txxx"));
			assertEq(";", p(g1, "program",	"; xxx"));
			assertEq("a", p(g1, "set1", "a"));
			assertEq("123", p(g1, "myInteger", "123"));
			assertEq("2.12.1945", p(g1, "myDatetime", "2.12.1945"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				p(g1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1",p(g1,"xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				p(g1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", p(g1, "nmtokens", "123"));
			assertEq("1.1.1990, 6.12.1945",
				p(g, "mydate", "1.1.1990, 6.12.1945"));
		} catch (SException | RuntimeException ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}