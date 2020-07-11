package test.common.bnf;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;

/** Test of parsing and executions of BNF expressions an assignment commands.
 * @author Vaclav Trojan
 */
public class TestExpr extends STester {

	public TestExpr() {super();}

	/** Map of variables. */
	private static final Map<String, Object> _variables =
		new TreeMap<String, Object>();
	/** used to get printed text. */
	private static final ByteArrayOutputStream _byteArray =
		new ByteArrayOutputStream();
	/** Switch to print generated code. */
	private static boolean _displayCode;

	/** Get value of a variable.
	 * @param name Name of variable.
	 * @return value of variable or null.
	 */
	private Object getVar(String name) { return _variables.get(name); }

	/** Parse string with source data according a rule from given BNF grammar,
	 * @param grammar BNF grammar.
	 * @param name name of rule in BNF grammar.
	 * @param source source data,
	 * @return parsed data or error message.
	 */
	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
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
			return "Exception " + ex;
		}
	}

	/** Parse source data according the rule "program" from given BNF grammar.
	 * @param grammar BNF grammar.
	 * @param source source data.
	 * @return empty string or error message.
	 */
	private String prog(BNFGrammar grammar, String source) {
		String result = parse(grammar, "program", source);
		if (!source.equals(result)) {
			return result;
		}
		Object[] code = grammar.getParsedObjects();
		if (_displayCode) {
			System.out.println(TestExprDeCompiler.toSource(source, code));
		}

		TestExprCompiler.execute(source, code, _variables, _byteArray);
		String s = TestExprDeCompiler.toSource(source, code);
		result = parse(grammar, "program", s);
		if (!s.equals(result)) {
			System.out.println(source);
			System.out.println(s);
			throw new RuntimeException("error in toSource");
		}
		code = grammar.getParsedObjects();
		TestExprCompiler.execute(s, code, _variables, _byteArray);
		Charset chs = Charset.availableCharsets().get("UTF-8");
		return SUtils.modifyString(
			new String(_byteArray.toByteArray(), chs), "\r\n","\n");
	}

////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Run test and print error information. */
	public void test() {
		BNFGrammar g = BNFGrammar.compile(null,
			new File(getDataDir() + "TestExpr.bnf"), null);
		g.setUserObject(this);
		try {
			String s;
			g.trace(null);
			assertEq("0", prog(g, " /*x*/ print ( /*x*/ 0 /*x*/) /*x*/; "));
			assertEq("13", prog(g, "print ( /*x*/ 12/*x*//*x*/ + 1 /*x*/);"));
			assertEq("abcdef", prog(g, "print('abc' + 'def');"));
			assertEq("25abc", prog(g, "print(( + ( + 3+2 ) * + 5 ) + 'abc');"));
			assertEq("25abc", prog(g, "print( +( + 5 * ( 3 + 2 ) ) + 'abc');"));
			assertEq("50abc", prog(g, "print(+(+5 * - ( - 7 + 2 ))*2+'abc');"));
			assertEq("true", prog(g, "print( true );"));
			assertEq("false", prog(g, "print( ! true);"));
			assertEq("true", prog(g, "print(true|false);"));
			assertEq("false", prog(g, "print(true & false);"));
			assertEq("true", prog(g, "print(true ^ false);"));
			assertEq("true", prog(g, "print( ! (true & false) );"));
			assertEq("-1abc", prog(g, "print( - ( ( 3 + 3 ) / 5 ) + \"abc\" );"));
			assertEq("1.26abc", prog(g, "print(((3 + 3.3)/5) + 'abc');"));
			assertEq("2", prog(g, "print( - 1 + 3 );"));
			assertEq("1", prog(g, " print(  min ( 2 , 1 ) ) ; "));
			assertEq("1.0", prog(g, "print(min(2.0,1.0));"));
			assertEq("1.0",  prog(g, "print(min(2,1.0));"));
			assertEq("1.0", prog(g, "print(min(2.0,1));"));
			assertEq(String.valueOf(Math.sin(3.1)), prog(g,"print(sin(3.1));"));
			assertEq(String.valueOf(Math.cos(3.1)), prog(g,"print(cos(3.1));"));
			assertEq("", prog(g, "Object i;"));
			assertEq("", prog(g, "empty();"));

			assertEq("", prog(g, "j = empty() + 'abc';"));
			assertEq("abc", getVar("j"));

			assertEq("", prog(g, "i=~1;"));
			assertEq(-2, getVar("i"));

			assertEq("", prog(g, "i = ~ ( ~ 1 );"));
			assertEq(1, getVar("i"));

			assertEq("", prog(g, "i = 8; i = i << 2;"));
			assertEq(32, getVar("i"));
			assertEq("", prog(g, "j = 'abc' + empty()/*x*/; k = j + 'd';"));
			assertEq("abc", getVar("j"));
			assertEq("abcd", getVar("k"));

			assertEq("", prog(g, "/*xx*/i=sin(/*xx*/3.14/*xx*/) ;/*xx*/"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "/*xx*/i/*xx*/=/*xx*/3/*xx*/;/*xx*/ "));
			assertEq(3, getVar("i"));

			assertEq("", prog(g, "i = 3 ; j = i * 5 ;"));
			assertEq(3, getVar("i"));
			assertEq(15, getVar("j"));

			assertEq("", prog(g, "i=''; j=i+(5*3);"));
			assertEq("", getVar("i"));
			assertEq("15", getVar("j"));

			assertEq("", prog(g, "i = \"\" ; j= i + ( 5 * 3 ) ;"));
			assertEq("", getVar("i"));
			assertEq("15", getVar("j"));

			assertEq("", prog(g, "i=\"\"\"\"; j=i+(5*3);"));
			assertEq("\"15", getVar("j"));

			assertEq("", prog(g, "i=''''; j=i+(5*3);"));
			assertEq("'15", getVar("j"));

			assertEq("", prog(g, "i = ''''''; j = i + (5 *3);"));
			assertEq("''15", getVar("j"));

			assertEq("", prog(g, "i = 'x''y';"));
			assertEq("x'y", getVar("i"));

			assertEq("", prog(g, "i = '''x'''; j = i + (5 *3);"));
			assertEq("'x'15", getVar("j"));

			assertEq("", prog(g, "i = '''''x'''''; j = i + (5 *3);"));
			assertEq("''x''15", getVar("j"));

			assertEq("", prog(g, "i = '\"x\"'; j = i + (5 *3);"));
			assertEq("\"x\"15", getVar("j"));

			assertEq("", prog(g, "i = 'abc'; j = (5 *3) + i;"));
			assertEq("abc", getVar("i"));
			assertEq("15abc", getVar("j"));

			assertEq("", prog(g, "i = sin(3.14);"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "i = min(3.14, sin(3.14));"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("",  prog(g, "i = max(3.14, sin(3.14 + 4));"));
			assertEq(3.14, getVar("i"));

			assertEq("", prog(g, "i = max ( 3.15 , sin ( 3.14 ) ) ;"));
			assertEq(3.15, getVar("i"));

			assertEq("", prog(g, "i = min(3.15, sin(3.14));"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "i = sin(0);"));
			assertEq(Math.sin(0), getVar("i"));

			assertEq("", prog(g, "i = sin(1.5);"));
			assertEq(Math.sin(1.5), getVar("i"));

			assertEq("", prog(g, "i = tanh(1.5);"));
			assertEq(Math.tanh(1.5), getVar("i"));

			assertEq("", prog(g, "i=sqrt(2);"));
			assertEq(Math.sqrt(2), getVar("i"));

			assertEq("", prog(g, "i = cbrt(2);"));
			assertEq(Math.cbrt(2), getVar("i"));

			assertEq("", prog(g, "i = 3.15 == sin(0);"));
			assertEq(false, getVar("i"));

			assertEq("", prog(g, "i = 8; i = i << 2;"));
			assertEq(32, getVar("i"));

			assertEq("", prog(g, "i = 8; i <<= 2;"));
			assertEq(32, getVar("i"));

			assertEq("", prog(g, "i = 8; i = i >> 2;"));
			assertEq(2, getVar("i"));

			assertEq("", prog(g, "i = 8; i >>= 2;"));
			assertEq(2, getVar("i"));

			assertEq("", prog(g, "i = -8; i = i >> 2;"));
			assertEq(-2, getVar("i"));

			assertEq("", prog(g, "i = -8; i=i>>>2;"));
			assertEq(-8L >>> 2, getVar("i"));

			assertEq("", prog(g, "i = -8; i >>>= 2;"));
			assertEq(-8L >>> 2, getVar("i"));

			assertEq("", prog(g, "i = 3==3.0;"));
			assertEq(true, getVar("i"));

			assertEq("", prog(g, "i = 3.15!=sin(0);"));
			assertEq(true, getVar("i"));

			assertEq("", prog(g, "i = 1; j = i++;"));
			assertEq(2, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 1; j = ++i;"));
			assertEq(2, getVar("i"));
			assertEq(2, getVar("j"));

			assertEq("", prog(g, "i = 1; j = i--;"));
			assertEq(0, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 1; j = i--;"));
			assertEq(0, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 2.1; j = --i;"));
			assertEq(1.1, getVar("i"));
			assertEq(1.1, getVar("j"));

			assertEq("", prog(g, "i = 1; i += 2;"));
			assertEq(3, getVar("i"));

			assertEq("", prog(g, "i = 1; i -= 2;"));
			assertEq(-1, getVar("i"));

			assertEq("", prog(g, "i = 1; i += 3.14;"));
			assertEq(3.14 + 1, getVar("i"));

			assertEq("", prog(g, "i = 3; j = i % 2;"));
			assertEq(3, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 1; i += 2;"));
			assertEq(3, getVar("i"));

			assertEq("", prog(g, "i = 1; j = 1; k = i++; m = ++j;"));
			assertEq(2, getVar("i"));
			assertEq(2, getVar("j"));
			assertEq(1, getVar("k"));
			assertEq(2, getVar("m"));

			assertEq("", prog(g, "i=(3 + 5)*2; j=i+1; k=i/2; l=j/2;m=j/2.0;"));
			assertEq(16, getVar("i"));
			assertEq(17, getVar("j"));
			assertEq(8, getVar("k"));
			assertEq(8, getVar("l"));
			assertEq(8.5, getVar("m"));

			assertEq("", prog(g,
				"i = true; j = false; k = i == j; m = i != j;\n" +
				"o = 1; p = 0; q = o > p; r = 1.0; s = 2.0; t = r <= s;"));
			assertEq(true, getVar("i"));
			assertEq(false, getVar("j"));
			assertEq(false, getVar("k"));
			assertEq(true, getVar("m"));
			assertEq(true, getVar("q"));
			assertEq(true, getVar("t"));

			assertEq("", prog(g, "i=sin(3.14);"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "sin(3.14); i=1;"));
			assertEq(1, getVar("i"));

			assertEq("", prog(g, "i=1*2+-(1+1);"));
			assertEq(0, getVar("i"));

			assertEq("", prog(g, "i=null;"));
			assertEq(null, getVar("i"));

			assertEq("", prog(g, "Object i; i = 0.0; i += sin(3.14);"));
			assertEq(Math.sin(3.14), getVar("i"));

			prog(g, "print(min(3,14)); println('x');");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("3x\n", s);

			prog(g, "printf('Ahoj'); println();");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("Ahoj\n", s);

			prog(g, "printf('%d, %d, %d\nx\n', 3,4,5);");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("3, 4, 5\nx\n", s);

			prog(g, "println(null);println(1);");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("null\n1\n", s);

			prog(g, "Object i=3; i=null; println(i);");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("null\n", s);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}