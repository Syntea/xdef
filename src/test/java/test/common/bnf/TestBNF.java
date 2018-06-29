/*
 * File: TestBNF.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test.common.bnf;

import cz.syntea.xdef.sys.STester;
import cz.syntea.xdef.sys.BNFExtMethod;
import cz.syntea.xdef.sys.BNFGrammar;
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.StringParser;
import java.io.File;
import java.util.HashMap;
import java.util.Stack;

/** Test of BNF.
 * @author Vaclav Trojan
 */
public class TestBNF extends STester {
	private final Stack<Object> _stack = new Stack<Object>();
	private final Stack<Object> _opers = new Stack<Object>();
	private final HashMap<Object, Object> _variables =
		new HashMap<Object, Object>();
	private String _s;
	private Object _result;
	public TestBNF() {super();}

	private String parse(BNFGrammar grammar, String name, String source) {
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
			ex.printStackTrace(System.err);
			return "Exception " + ex;
		}
	}

	private String parse(BNFGrammar grammar, String name) {
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
			ex.printStackTrace(System.err);
			return "Exception " + ex;
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
		long i = new Long(p.getParser().getParsedString());
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
		Stack<Object> stack = new Stack<Object>();
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
							if (ch == '+') {
								stack.push(x + y);
							} else if (ch == '-') {
								stack.push(x - y);
							} else if (ch == '*') {
								stack.push(x * y);
							} else if (ch == '/') {
								stack.push(x / y);
							}
							continue;
						} else if (obj instanceof Double) {
							double y = (Double) obj;
							if (ch == '+') {
								stack.push(x + y);
							} else if (ch == '-') {
								stack.push(x - y);
							} else if (ch == '*') {
								stack.push(x * y);
							} else if (ch == '/') {
								stack.push(x / y);
							}
							continue;
						}
					} else if (obj1 instanceof Double) {
						double x = ((Double) obj1);
						if (obj instanceof Long) {
							long y = (Long) obj;
							if (ch == '+') {
								stack.push(x + y);
							} else if (ch == '-') {
								stack.push(x - y);
							} else if (ch == '*') {
								stack.push(x * y);
							} else if (ch == '/') {
								stack.push(x / y);
							}
							continue;
						} else if (obj instanceof Double) {
							double y = (Double) obj;
							if (ch == '+') {
								stack.push(x + y);
							} else if (ch == '-') {
								stack.push(x - y);
							} else if (ch == '*') {
								stack.push(x * y);
							} else if (ch == '/') {
								stack.push(x / y);
							}
							continue;
						}
					}
					if (ch == '+') {
						stack.push(obj1.toString() + obj.toString());
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
							stack.push(new Long(s));
						} else {
							stack.push(new Double(s));
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
		String bnf, data;
		BNFGrammar grammar, grammar1, bnfOfBnf;
		try {
			bnf = FUtils.readString(new File(getDataDir() + "TestBNF.bnf"),
				"UTF-8");
			bnfOfBnf = BNFGrammar.compile(bnf);
			bnf = "x ::= a";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= a /*x*/ b";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= ([a-bA-Z] - 'Y')+";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= ([^a-bA-Z] 'Y')+";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= 'aZ'";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= \"aZ\"";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "/*x*/x ::= /*x*/\"abc\"/*x*/y ::= 'abc' /*x*/";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= 'a' y ::= 'b' z ::= x";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= ( 'a'+ | (b c) {1,5}) + ";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "x ::= ( 'a'+  (b c) { 1, 5 } ) + ";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "intList ::= integer (S? \",\" S? integer)*";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "S ::= ( a - b )";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "S ::= ( a | b )+";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "SignedIntegerLiteral ::= (\"+\" | \"-\")?";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "NumberLiteral ::= DecimalInteger\n" +
				"( '.' DecimalInteger )? ( 'E' [-+]? DecimalInteger )?";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
			bnf = "L::='a'/*E*/";
			assertEq(bnf, parse(bnfOfBnf, "BNFGrammar", bnf));
//			source = FUtils.readString(new File(getDataDir() + "TestBNF_1.bnf"),
//				"UTF-8");
//			assertEq(source, parse(bnfOfBnf, "BNFGrammar", source));
////////////////////////////////////////////////////////////////////////////////
			grammar = BNFGrammar.compile("x ::= $JavaName");
			assertEq("a1", parse(grammar, "x", "a1"));
			grammar = BNFGrammar.compile("x ::= $JavaQName");
			assertEq("a.b", parse(grammar, "x", "a.b"));
			assertEq("ab", parse(grammar, "x", "ab"));
			grammar = BNFGrammar.compile("x ::= 'a' $anyChar");
			assertEq("ax", parse(grammar, "x", "ax"));
			assertTrue(parse(grammar, "x", "a").indexOf("x failed, ") >= 0);
			assertTrue(parse(grammar, "x", "bb").indexOf("x failed, ") >= 0);
			grammar = BNFGrammar.compile("x ::= 'a' $anyChar*");
			assertEq("a", parse(grammar, "x", "a"));
			assertNull(grammar.getAndClearParsedObjects());
			assertEq("ax", parse(grammar, "x", "ax"));
			assertEq("axy", parse(grammar, "x", "axy"));
			assertTrue(parse(grammar, "x", "b").indexOf("x failed, ") >= 0);
			assertTrue(parse(grammar, "x", "bb").indexOf("x failed, ") >= 0);
			bnf =
"XMLName ::= $xmlName\n"+
"S ::= $whitespace+\n"+
"RefName ::= XMLName | XMLName? \"#\" XMLName\n" +
"RootList ::= S? (RefName | \"*\") (S? \"|\" S? (RefName | \"*\"))* S?\n" +
"Reference ::= \"ref\" S RefName";
			grammar = BNFGrammar.compile(bnf);
			data = "#A";
			assertEq(data, parse(grammar, "RefName", data));
			data = "A";
			assertEq(data, parse(grammar, "RefName", data));
			data = "A#B";
			assertEq(data, parse(grammar, "RefName", data));
////////////////////////////////////////////////////////////////////////////////
			bnf =
"%define $x: $test.common.bnf.TestBNF.xxx\n"+
"digit ::= [0-9]" +
"signedInteger::= [-+]? digit+ $x\n"+
"signedIntegers::= signedInteger (',' signedInteger)*\n";
			grammar = BNFGrammar.compile(bnf);
			grammar.setUserObject(this);
			data = "-12345,3";
			assertEq(data, parse(grammar, "signedIntegers", data));
			assertEq(2, _stack.size());
			assertEq("3", _stack.pop());
			assertEq("-12345", _stack.pop());
			assertTrue(grammar.isEOS());
			data = "-12345,++3";
			grammar = BNFGrammar.compile(bnf);
			grammar.setUserObject(this);
			_stack.clear();
			if (!grammar.parse(data, "signedIntegers")) {
				fail(_stack.toString());
			} else {
				assertFalse(grammar.isEOS());
				assertEq("-12345", grammar.getParsedString());
				assertEq(",++3", grammar.getUnparsedSourceBuffer());
				assertEq(1, _stack.size());
				assertEq("-12345", _stack.pop());
			}
////////////////////////////////////////////////////////////////////////////////
			bnf =
"%define $x: $test.common.bnf.TestBNF.yyy\n"+
"digit ::= [0-9]" +
"signedInteger::= [-+]? digit+ $x\n"+
"signedIntegers::= signedInteger (',' signedInteger)*\n";
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "-12345,3";
			grammar.setUserObject(null);
			assertTrue(grammar.parse(data,"signedIntegers"));
			assertEq(data, grammar.getParsedString());
			assertEq("signedInteger:-12345,p:0\nsignedInteger:3,p:7",
				grammar.getUserObject());
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
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "2H";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(09:00)";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(10:00),W(1,2,3,4,5,6)";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(14:45,20:00),W(1,2,3,4,5)";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(11:00),W(1)";
			assertEq(data, parse(grammar, "r" , data));
			data = "1W(2)";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(12:00),W(-1)";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(12:00),M(-1)";
			assertEq(data, parse(grammar, "r" , data));
			data = "D(07:00),M(20)";
			assertEq(data, parse(grammar, "r" , data));
			data = "M(3)";
			assertEq(data, parse(grammar, "r" , data));
			data = "M(9)";
			assertEq(data, parse(grammar, "r" , data));
			data = "M(31)";
			assertEq(data, parse(grammar, "r" , data));
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
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "a ?";
			assertEq(data, parse(grammar, "X", data));
			data = "a:b-c.d ?";
			assertEq(data, parse(grammar, "X", data));
			data = "a:b-c.d.e() ?";
			assertEq(data, parse(grammar, "X", data));
			data = "a:b-c.d. e ( ) ?";
			assertEq(data, parse(grammar, "X", data));
			data = "a:b-c.d .e ( ) ?";
			assertEq(data, parse(grammar, "X", data));
			data = "a:b-c.d . e ( ) ?";
			assertEq(data, parse(grammar, "X", data));
////////////////////////////////////////////////////////////////////////////////
			bnf = FUtils.readString(
				new File(getDataDir() + "TestXPath2.bnf"), "windows-1250");
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "a/b";
			assertEq(data, parse(grammar, "XPath", data));
			data = "/a/b";
			assertEq(data, parse(grammar, "XPath", data));
			data = "/*";
			assertEq(data, parse(grammar, "XPath", data));
			data = "/*[@a]";
			assertEq(data, parse(grammar, "XPath", data));
			data = "/*[@a='1']/text()";
			assertEq(data, parse(grammar, "XPath", data));
			data = "//a/text()";
			assertEq(data, parse(grammar, "XPath", data));
////////////////////////////////////////////////////////////////////////////////
			bnf = "Comment ::= \"/*\" ( [^*]+ | \"*\" [^/] )* \"*/\"\n"+
			"S ::= ( ' ' | Comment )+\n"+
			"A ::= S? 'X' S?";
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "X /*jmeno - znaky*/  ";
			assertEq("X /*jmeno - znaky*/  ", parse(grammar, "A" , data));

			bnf = "A ::= (B - ']]>')*\n" +
				"B ::= [#x20-#xD7FF]";
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "C ]]>";
			assertEq("C ", parse(grammar, "A" , data));

			bnf = "A ::= '<!--' ((B - '-') | ('-' (B - '-')))* '-->'\n" +
				"B ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD]";
			grammar = BNFGrammar.compile(null, bnf, null);
			data = "<!-- c - C -->";
			assertEq(data, parse(grammar, "A" , data));
			bnf =
"%define $value:         $test.common.bnf.TestBNF.myValue\n"+
"%define $operator:      $test.common.bnf.TestBNF.myOperator\n"+
"%define $mypush:          $test.common.bnf.TestBNF.myPushOperator\n"+
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
"tabelator ::= #9\n"+
"backslash ::= '\\'\n"+
"NL ::= #10\n"+
"CR ::= #13\n"+
"FF ::= #12\n"+
"ch01 ::= #1\n"+
"testMethod::=$mytest\n"+
"S ::= [#9#10#13 ]* /*skipped white spaces*/\n"+
"boolean::= ('true' | 'false')\n"+
"string::= \"'\" (\"''\" | [^']+)+ \"'\" |\n" +
"          '\"' ('\"\"' | [^\"]+)+ '\"'\n"+
"integer ::= [0-9]+\n"+
"float ::= [0-9]+ ('.' [0-9]+ ('E' [-+]? [0-9]+ )? |\n" +
"          'E' [-+]? [0-9]+ )\n"+
"identifier::= [a-zA-Z][a-zA-Z0-0]*\n"+
"e::= expression $exec\n"+
"expression::= S simpleExpr\n"+
"simpleExpr::= factor S ( [-+]$operator S factor $mypush)*\n"+
"factor::= term S ( [*/]$operator S term $mypush)*\n"+
"term::= S ('-'  S value $unaryMinus) | ('+'? S) value\n"+
"value::= ('(' S expression S ')') |\n" +
"       ((float | integer | boolean | string | identifier) $value)\n"+
"assignment::= identifier  $value S '=' $assign e\n"+
"statement::= S? (assignment  S)? ';' \n"+
"program::= statement+ \n"+
"";
			grammar = BNFGrammar.compile(null, bnf, null);
			assertEq("x=x-555.1", parse(grammar, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("a", parse(grammar, "set1", "a"));
			data = parse(grammar, "set1", "b");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("^", parse(grammar, "set2", "^"));
			assertEq("\"", parse(grammar, "set2", "\""));
			assertEq("#", parse(grammar, "set2", "#"));
			data = parse(grammar, "set2a", "^");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar, "set2a", "\"");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar, "set2a", "#");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("?", parse(grammar, "set2a", "?"));
			assertEq("-", parse(grammar, "set3", "-"));
			assertEq("^", parse(grammar, "set3", "^"));
			assertEq("#", parse(grammar, "set3", "#"));
			assertEq("_", parse(grammar, "set3", "_"));
			assertTrue(parse(grammar, "set3a", "-").startsWith("set3a failed"));
			assertTrue(parse(grammar, "set3a", "^").startsWith("set3a failed"));
			assertTrue(parse(grammar, "set3a", "#").startsWith("set3a failed"));
			assertTrue(parse(grammar, "set3a", "_").startsWith("set3a failed"));
			assertEq("a", parse(grammar, "set3a", "a"));
			data = parse(grammar, "set3a", "");
			assertTrue(data.indexOf("failed, eos;") >= 0, data);
			assertEq("AAA", parse(grammar, "quantif1", "AAA"));
			assertEq("AAA", parse(grammar, "quantif1", "AAAA"));
			assertEq("AAA", parse(grammar, "quantif2", "AAA"));
			assertEq("AAAA", parse(grammar, "quantif2", "AAAA"));
			assertEq("AAAA", parse(grammar, "quantif2", "AAAAA"));
			assertEq("AAA", parse(grammar, "quantif3", "AAA"));
			assertEq("AAAA", parse(grammar, "quantif3", "AAAA"));
			assertEq("AAAAA", parse(grammar, "quantif3", "AAAAA"));
			data = parse(grammar, "quantif1", "AA");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("", parse(grammar, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",parse(grammar, "S", " \t \n -123  456"));
			assertEq("", parse(grammar, "S")); //nothing parsed
			assertEq("  ", parse(grammar, "S", "  "));
			assertEq("\t", parse(grammar, "tabelator", "\txxx"));
			assertEq("-123",parse(grammar, "e", "-123"));
			assertEq(-123, _result, printStack());
			assertEq("456", parse(grammar, "e", "456"));
			assertEq(456, _result, printStack());
			assertEq("\n\n  -\n123+\t456",
				parse(grammar, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar, "e", "+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(44, _result, printStack());
			assertEq("1", parse(grammar, "e", "1"));
			assertEq(1, _result, printStack());
			assertEq("+1", parse(grammar, "e", "+1"));
			assertEq(1, _result, printStack());
			assertEq("-1", parse(grammar, "e", "-1"));
			assertEq(-1, _result, printStack());
			assertEq("-(-1)", parse(grammar, "e", "-(-1)"));
			assertEq(1, _result, printStack());
			assertEq("+(-1)", parse(grammar, "e", "+(-1)"));
			assertEq(-1, _result, printStack());
			assertEq("-(+1)", parse(grammar, "e", "-(+1)"));
			assertEq(-1, _result, printStack());
			assertEq("+(-(((1))))", parse(grammar, "e", "+(-(((1))))"));
			assertEq(-1, _result, printStack());
			assertEq("1-x", parse(grammar, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", parse(grammar, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("\"x-1\"", parse(grammar, "e", "\"x-1\""));
			assertEq("x-1", _result, printStack());
			assertEq("'x-\"1'", parse(grammar, "e", "'x-\"1'"));
			assertEq("x-\"1", _result, printStack());
			assertEq("x=x-555", parse(grammar, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", parse(grammar, "program",	"; xxx"));
			assertEq("x=x", parse(grammar, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", parse(grammar, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", parse(grammar, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", parse(grammar, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", parse(grammar, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", parse(grammar, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1",
				parse(grammar, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				parse(grammar, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", parse(grammar, "assignment", "x=true"));
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
			grammar1 = BNFGrammar.compile(grammar, bnf, null);
			assertEq("a", parse(grammar1, "set1", "a"));
			assertEq("x='ab'+1+2", parse(grammar1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("i=x-555;\nj=5; j = i - j;", parse(grammar1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));

			assertEq("AB", parse(grammar1, "rule1a", "AB"));
			assertEq("CD", parse(grammar1, "rule1a", "CD"));
			assertEq("AB", parse(grammar1, "rule1b", "AB"));
			assertEq("CD", parse(grammar1, "rule1b", "CD"));
			assertEq("cd", parse(grammar1, "lid", "cd"));
			assertEq("abc", parse(grammar1, "lid", "abc"));
			data = parse(grammar1, "lid", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd23", parse(grammar1, "lid1a", "cd23"));
			assertEq("cd23", parse(grammar1, "lid1b", "cd23"));
			data = parse(grammar1, "lid1a", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid1b", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid1b", "de12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd", parse(grammar1, "lid2a", "cd"));
			assertEq("23", parse(grammar1, "lid2b", "23"));
			data = parse(grammar1, "lid2a", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2b", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2a", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2b", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("xy", parse(grammar1, "lid3", "xy"));
			data = parse(grammar1, "lid3", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid3", "cd");
			assertTrue(data.indexOf("failed, ") >= 0, data);
//          grammar1.display(System.out, false);
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
"tabelator ::= #9 \n" +
"backslash ::= \"\\\" \n" +
"NL ::= #10 \n" +
"CR ::= #13 \n" +
"FF ::= #12 \n" +
"ch01 ::= #1 \n" +
"testMethod ::= $mytest \n" +
"S ::= [#9#10#13 ]* \n" +
"boolean ::= ( \"true\" | \"false\" ) \n" +
"string ::= ( \"'\" ( \"''\" | [^']+ )+ \"'\" | #34 ( (#34 #34) | [^\"]+ )+ #34 ) \n" +
"integer ::= [0-9]+ \n" +
"float ::= [0-9]+ ( \".\" [0-9]+ ( \"E\" [-+]? [0-9]+ )? | \"E\" [-+]? [0-9]+ ) \n" +
"identifier ::= [a-zA-Z] [a-zA-Z0-0]* \n" +
"e ::= expression $exec \n" +
"expression ::= S simpleExpr \n" +
"simpleExpr ::= factor S ( [-+] $operator S factor $mypush )* \n" +
"factor ::= term S ( [*/] $operator S term $mypush )* \n" +
"term ::= ( S \"-\" S value $unaryMinus | \"+\"? S value ) \n" +
"value ::= ( \"(\" S expression S \")\" | ( float | integer | boolean | string | identifier ) $value ) \n" +
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
			grammar = BNFGrammar.compile(null, bnf, null);
			assertEq("a", parse(grammar, "set1", "a"));
			data = parse(grammar, "set1", "b");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("^", parse(grammar, "set2", "^"));
			assertEq("\"", parse(grammar, "set2", "\""));
			assertEq("#", parse(grammar, "set2", "#"));
			data = parse(grammar, "set2a", "^");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar, "set2a", "\"");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar, "set2a", "#");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("?", parse(grammar, "set2a", "?"));
			assertEq("-", parse(grammar, "set3", "-"));
			assertEq("^", parse(grammar, "set3", "^"));
			assertEq("#", parse(grammar, "set3", "#"));
			assertEq("_", parse(grammar, "set3", "_"));
			assertTrue(parse(grammar, "set3a", "-").startsWith("set3a failed"));
			assertTrue(parse(grammar, "set3a", "^").startsWith("set3a failed"));
			assertTrue(parse(grammar, "set3a", "#").startsWith("set3a failed"));
			assertTrue(parse(grammar, "set3a", "_").startsWith("set3a failed"));
			assertEq("a", parse(grammar, "set3a", "a"));
			data = parse(grammar, "set3a", "");
			assertTrue(data.indexOf("failed, eos;") >= 0, data);
			assertEq("", parse(grammar, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",parse(grammar, "S", " \t \n -123  456"));
			assertEq("", parse(grammar, "S")); //nothing parsed
			assertEq("\t", parse(grammar, "tabelator", "\txxx"));
			assertEq("-123",parse(grammar, "e", "-123"));
			assertEq(-123, _result, printStack());
			assertEq("456", parse(grammar, "e", "456"));
			assertEq(456, _result, printStack());
			assertEq("\n\n  -\n123+\t456",
				parse(grammar, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar, "e", "+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(44, _result, printStack());
			assertEq("-1", parse(grammar, "e", "-1"));
			assertEq(-1, _result, printStack());
			assertEq("+1", parse(grammar, "e", "+1"));
			assertEq(1, _result, printStack());
			assertEq("1-x", parse(grammar, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", parse(grammar, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("x=x-555", parse(grammar, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", parse(grammar, "program",	"; xxx"));
			assertEq("x=x", parse(grammar, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", parse(grammar, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", parse(grammar, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", parse(grammar, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", parse(grammar, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", parse(grammar, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1",
				parse(grammar, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				parse(grammar, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", parse(grammar, "assignment", "x=true"));
			assertEq(Boolean.TRUE, _result, printStack());
			assertEq(Boolean.TRUE, _variables.get("x"));
			assertEq("a", parse(grammar, "set1", "a"));

			assertEq("AB", parse(grammar, "rule1a", "AB"));
			assertEq("CD", parse(grammar, "rule1a", "CD"));
			assertEq("AB", parse(grammar, "rule1b", "AB"));
			assertEq("CD", parse(grammar, "rule1b", "CD"));
			assertEq("cd", parse(grammar, "lid", "cd"));
			data = parse(grammar, "lid", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd23", parse(grammar, "lid1a", "cd23"));
			assertEq("cd23", parse(grammar, "lid1b", "cd23"));
			data = parse(grammar, "lid1a", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar, "lid1b", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar, "lid1b", "de12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd", parse(grammar, "lid2a", "cd"));
			assertEq("23", parse(grammar, "lid2b", "23"));
			data = parse(grammar, "lid2a", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar, "lid2b", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar, "lid2a", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar, "lid2b", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("xy", parse(grammar, "lid3", "xy"));
			data = parse(grammar, "lid3", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar, "lid3", "cd");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("123", parse(grammar, "myInteger2", "123"));
			assertEq("123", parse(grammar, "myInteger", "123"));
			data = parse(grammar, "myInteger", "x123");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertTrue(parse(grammar,
				"myInteger1", "123").indexOf("greater then max") > 0);
			assertEq("123 3.14 1999-01-02T23:10:54+01:00", parse(grammar,
				"testBuildIn", "123 3.14 1999-01-02T23:10:54+01:00"));
			assertEq("2.12.1945", parse(grammar, "myDatetime", "2.12.1945"));
			assertEq("1.1.1990, 6.12.1945",
				parse(grammar, "mydate", "1.1.1990, 6.12.1945"));
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
			grammar1 = BNFGrammar.compile(grammar, bnf, null);
			assertEq("a", parse(grammar1, "set1", "a"));
			data = parse(grammar1, "set1", "b");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("^", parse(grammar1, "set2", "^"));
			assertEq("\"", parse(grammar1, "set2", "\""));
			assertEq("#", parse(grammar1, "set2", "#"));
			data = parse(grammar1, "set2a", "^");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar1, "set2a", "\"");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar1, "set2a", "#");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("?", parse(grammar1, "set2a", "?"));
			assertEq("-", parse(grammar1, "set3", "-"));
			assertEq("^", parse(grammar1, "set3", "^"));
			assertEq("#", parse(grammar1, "set3", "#"));
			assertEq("_", parse(grammar1, "set3", "_"));
			assertTrue(parse(grammar1,"set3a", "-").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "^").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "#").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "_").startsWith("set3a failed"));
			assertEq("a", parse(grammar1, "set3a", "a"));
			data = parse(grammar1, "set3a", "");
			assertTrue(data.indexOf("failed, eos;") >= 0, data);
			assertEq("AAA", parse(grammar1, "quantif1", "AAA"));
			assertEq("AAA", parse(grammar1, "quantif1", "AAAA"));
			assertEq("AAA", parse(grammar1, "quantif2", "AAA"));
			assertEq("AAAA", parse(grammar1, "quantif2", "AAAA"));
			assertEq("AAAA", parse(grammar1, "quantif2", "AAAAA"));
			assertEq("AAA", parse(grammar1, "quantif3", "AAA"));
			assertEq("AAAA", parse(grammar1, "quantif3", "AAAA"));
			assertEq("AAAAA", parse(grammar1, "quantif3", "AAAAA"));
			data = parse(grammar1, "quantif1", "AA");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("", parse(grammar1, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",parse(grammar1, "S", " \t \n -123  456"));
			assertEq("", parse(grammar1, "S")); //nothing parsed
			assertEq("\t", parse(grammar1, "tabelator", "\txxx"));
			assertEq("\n\n  -\n123+\t456",
				parse(grammar1, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar1, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar1, "e", "+ ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(44, _result, printStack());
			assertEq("-1", parse(grammar, "e", "-1"));
			assertEq(-1, _result, printStack());
			assertEq("+1", parse(grammar, "e", "+1"));
			assertEq(1, _result, printStack());
			assertEq("1-x", parse(grammar1, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", parse(grammar1, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("x=x-555", parse(grammar1, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", parse(grammar1, "program",	"; xxx"));
			assertEq("x=x", parse(grammar1, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", parse(grammar1, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", parse(grammar1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", parse(grammar1, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", parse(grammar1, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", parse(grammar1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1",
				parse(grammar1, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				parse(grammar1, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", parse(grammar1, "assignment", "x=true"));
			assertEq(Boolean.TRUE, _result, printStack());
			assertEq(Boolean.TRUE, _variables.get("x"));
			assertEq("123 3.14 1999-01-02T23:10:54+01:00", parse(grammar,
				"testBuildIn",
				"123 3.14 1999-01-02T23:10:54+01:00"));
			assertEq("2.12.1945", parse(grammar, "myDatetime", "2.12.1945"));

			assertEq("2.12.1945", parse(grammar1, "myDatetime", "2.12.1945"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				parse(grammar1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("null", "" + grammar1.popObject());
			assertEq("a-b c:d.e f1",parse(grammar1,"xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				parse(grammar1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", parse(grammar1, "nmtokens", "123"));
			assertEq("a", parse(grammar1, "set1", "a"));
			assertEq("x='ab'+1+2", parse(grammar1, "assignment", "x='ab'+1+2"));
			assertEq("i=x-555;\nj=5; j = i - j;", parse(grammar1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));

			assertEq("AB", parse(grammar1, "rule1a", "AB"));
			assertEq("CD", parse(grammar1, "rule1a", "CD"));
			assertEq("AB", parse(grammar1, "rule1b", "AB"));
			assertEq("CD", parse(grammar1, "rule1b", "CD"));
			assertEq("cd", parse(grammar1, "lid", "cd"));
			data = parse(grammar1, "lid", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd23", parse(grammar1, "lid1a", "cd23"));
			assertEq("cd23", parse(grammar1, "lid1b", "cd23"));
			data = parse(grammar1, "lid1a", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid1b", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid1b", "de12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd", parse(grammar1, "lid2a", "cd"));
			assertEq("23", parse(grammar1, "lid2b", "23"));
			data = parse(grammar1, "lid2a", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2b", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2a", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2b", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("xy", parse(grammar1, "lid3", "xy"));
			data = parse(grammar1, "lid3", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid3", "cd");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				parse(grammar1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1",
				parse(grammar1, "xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				parse(grammar1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", parse(grammar1, "nmtokens", "123"));
			assertEq("1.1.1990, 6.12.1945",
				parse(grammar, "mydate", "1.1.1990, 6.12.1945"));
			grammar1 = BNFGrammar.compile(null, grammar1.toString(), null);
			assertEq("a", parse(grammar1, "set1", "a"));
			data = parse(grammar1, "set1", "b");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("^", parse(grammar1, "set2", "^"));
			assertEq("\"", parse(grammar1, "set2", "\""));
			assertEq("#", parse(grammar1, "set2", "#"));
			data = parse(grammar1, "set2a", "^");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar1, "set2a", "\"");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar1, "set2a", "#");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("?", parse(grammar1, "set2a", "?"));
			assertEq("-", parse(grammar1, "set3", "-"));
			assertEq("^", parse(grammar1, "set3", "^"));
			assertEq("#", parse(grammar1, "set3", "#"));
			assertEq("_", parse(grammar1, "set3", "_"));
			assertTrue(parse(grammar1,"set3a", "-").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "^").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "#").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "_").startsWith("set3a failed"));
			assertEq("a", parse(grammar1, "set3a", "a"));
			data = parse(grammar1, "set3a", "");
			assertTrue(data.indexOf("failed, eos;") >= 0, data);
			assertEq("AAA", parse(grammar1, "quantif1", "AAA"));
			assertEq("AAA", parse(grammar1, "quantif1", "AAAA"));
			assertEq("AAA", parse(grammar1, "quantif2", "AAA"));
			assertEq("AAAA", parse(grammar1, "quantif2", "AAAA"));
			assertEq("AAAA", parse(grammar1, "quantif2", "AAAAA"));
			assertEq("AAA", parse(grammar1, "quantif3", "AAA"));
			assertEq("AAAA", parse(grammar1, "quantif3", "AAAA"));
			assertEq("AAAAA", parse(grammar1, "quantif3", "AAAAA"));
			data = parse(grammar1, "quantif1", "AA");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("", parse(grammar1, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",parse(grammar1, "S", " \t \n -123  456"));
			assertEq("", parse(grammar1, "S")); //nothing parsed
			assertEq("\t", parse(grammar1, "tabelator", "\txxx"));
			assertEq("\n\n  -\n123+\t456",
				parse(grammar1, "e", "\n\n  -\n123+\t456"));
			assertEq(333, _result, printStack());
			assertEq("- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ",
				parse(grammar1, "e", "- ( 123 - 5 * ( 3 - 2 + 6) ) / 2 ::"));
			assertEq(-44, _result, printStack());
			assertEq("+(123-5*(3-2+6))/2",
				parse(grammar1, "e", "+(123-5*(3-2+6))/2::"));
			assertEq(44, _result, printStack());
			assertEq("1-x", parse(grammar1, "e", "1-x"));
			assertEq(-998, _result, printStack());
			assertEq("x-1", parse(grammar1, "e", "x-1"));
			assertEq(998, _result, printStack());
			assertEq("x=x-555", parse(grammar1, "assignment", "x=x-555;"));
			assertEq(444, _result, printStack());
			assertEq(";", parse(grammar1, "program", "; xxx"));
			assertEq("x=x", parse(grammar1, "assignment", "x=x"));
			assertEq(999, _variables.get("x"));
			assertEq("x=-x", parse(grammar1, "assignment", "x=-x"));
			assertEq(-999, _variables.get("x"));
			assertEq("i=x-555;\nj=5; j = i - j;", parse(grammar1, "program",
				"i=x-555;\nj=5; j = i - j; xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("x=x-555.1", parse(grammar1, "assignment", "x=x-555.1;"));
			assertEq(443.9, _result, printStack());
			assertEq("x=2.14E-2", parse(grammar1, "assignment", "x=2.14E-2;"));
			assertEq(2.14E-2, _result, printStack());
			assertEq(2.14E-2, _variables.get("x"));
			assertEq("x='ab'+1+2", parse(grammar1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("ab12", _variables.get("x"));
			assertEq("x='a'+1.0E+1",
				parse(grammar1, "assignment", "x='a'+1.0E+1"));
			assertEq("a10.0", _result, printStack());
			assertEq("a10.0", _variables.get("x"));
			assertEq("x=1.0+1.0E-1+'a'",
				parse(grammar1, "assignment", "x=1.0+1.0E-1+'a'"));
			assertEq("1.1a", _result, printStack());
			assertEq("1.1a", _variables.get("x"));
			assertEq("x=true", parse(grammar1, "assignment", "x=true"));
			assertEq(true, _result, printStack());
			assertEq(true, _variables.get("x"));
			assertEq("123 3.14 1999-01-02T23:10:54+01:00", parse(grammar,
				"testBuildIn",
				"123 3.14 1999-01-02T23:10:54+01:00"));
			assertEq("2.12.1945", parse(grammar, "myDatetime", "2.12.1945"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				parse(grammar1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1",parse(grammar1,"xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				parse(grammar1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", parse(grammar1, "nmtokens", "123"));
			assertEq("a", parse(grammar1, "set1", "a"));
			assertEq("x='ab'+1+2", parse(grammar1, "assignment", "x='ab'+1+2"));
			assertEq("ab12", _result, printStack());
			assertEq("i=x-555;j=5;j=i-j;", 
				parse(grammar1, "program", "i=x-555;j=5;j=i-j;xxx"));
			assertEq(444, _variables.get("i"));
			assertEq(439, _variables.get("j"));
			assertEq("AB", parse(grammar1, "rule1a", "AB"));
			assertEq("CD", parse(grammar1, "rule1a", "CD"));
			assertEq("AB", parse(grammar1, "rule1b", "AB"));
			assertEq("CD", parse(grammar1, "rule1b", "CD"));
			assertEq("cd", parse(grammar1, "lid", "cd"));
			data = parse(grammar1, "lid", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd23", parse(grammar1, "lid1a", "cd23"));
			assertEq("cd23", parse(grammar1, "lid1b", "cd23"));
			data = parse(grammar1, "lid1a", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid1b", "ab12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid1b", "de12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("cd", parse(grammar1, "lid2a", "cd"));
			assertEq("23", parse(grammar1, "lid2b", "23"));
			data = parse(grammar1, "lid2a", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2b", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2a", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid2b", "12");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("xy", parse(grammar1, "lid3", "xy"));
			data = parse(grammar1, "lid3", "ab");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			data = parse(grammar1, "lid3", "cd");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				parse(grammar1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1",
				parse(grammar1, "xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				parse(grammar1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", parse(grammar1, "nmtokens", "123"));
			assertEq("a", parse(grammar1, "set1", "a"));
			data = parse(grammar1, "set1", "b");
			assertTrue(data.indexOf("failed, ") >= 0, data);
			assertEq("^", parse(grammar1, "set2", "^"));
			assertEq("\"", parse(grammar1, "set2", "\""));
			assertEq("#", parse(grammar1, "set2", "#"));
			data = parse(grammar1, "set2a", "^");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar1, "set2a", "\"");
			assertTrue(data.indexOf("failed,") >= 0, data);
			data = parse(grammar1, "set2a", "#");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("?", parse(grammar1, "set2a", "?"));
			assertEq("-", parse(grammar1, "set3", "-"));
			assertEq("^", parse(grammar1, "set3", "^"));
			assertEq("#", parse(grammar1, "set3", "#"));
			assertEq("_", parse(grammar1, "set3", "_"));
			assertTrue(parse(grammar1,"set3a", "-").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "^").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "#").startsWith("set3a failed"));
			assertTrue(parse(grammar1,"set3a", "_").startsWith("set3a failed"));
			assertEq("a", parse(grammar1, "set3a", "a"));
			data = parse(grammar1, "set3a", "");
			assertTrue(data.indexOf("failed, eos;") >= 0, data);
			assertEq("AAA", parse(grammar1, "quantif1", "AAA"));
			assertEq("AAA", parse(grammar1, "quantif1", "AAAA"));
			assertEq("AAA", parse(grammar1, "quantif2", "AAA"));
			assertEq("AAAA", parse(grammar1, "quantif2", "AAAA"));
			assertEq("AAAA", parse(grammar1, "quantif2", "AAAAA"));
			assertEq("AAA", parse(grammar1, "quantif3", "AAA"));
			assertEq("AAAA", parse(grammar1, "quantif3", "AAAA"));
			assertEq("AAAAA", parse(grammar1, "quantif3", "AAAAA"));
			data = parse(grammar1, "quantif1", "AA");
			assertTrue(data.indexOf("failed,") >= 0, data);
			assertEq("", parse(grammar1, "testMethod", ""));
			assertEq("test(123,a\"\n\\)", _s);
			assertEq(" \t \n ",parse(grammar1, "S", " \t \n -123  456"));
			assertEq("", parse(grammar1, "S")); //nothing parsed
			assertEq("\t", parse(grammar1, "tabelator", "\txxx"));
			assertEq(";", parse(grammar1, "program",	"; xxx"));
			assertEq("a", parse(grammar1, "set1", "a"));
			assertEq("123", parse(grammar1, "myInteger", "123"));
			assertEq("2.12.1945", parse(grammar1, "myDatetime", "2.12.1945"));
			assertEq("P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H",
				parse(grammar1, "myDuration",
					"P0001-10-11T23:01:55/2009-11-05T23:11:05PT15H"));
			assertEq("a-b c:d.e f1",parse(grammar1,"xmlnames", "a-b c:d.e f1"));
			assertEq("a-b c:d.e f1 123",
				parse(grammar1, "nmtokens", "a-b c:d.e f1 123"));
			assertEq("123", parse(grammar1, "nmtokens", "123"));
			assertEq("1.1.1990, 6.12.1945",
				parse(grammar, "mydate", "1.1.1990, 6.12.1945"));
			
		} catch (Exception ex) {
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