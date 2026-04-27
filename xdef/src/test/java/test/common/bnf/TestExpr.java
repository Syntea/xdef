package test.common.bnf;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.runTest;
import static test.common.bnf.TestExprCompiler.precompile;

/** Test of parsing and executions of BNF expressions an assignment commands.
 * @author Vaclav Trojan
 */
public class TestExpr extends STester {

    public TestExpr() {super();}

    /** Map of variables. */
    private final Map<String, Object> _variables = new TreeMap<>();
    /** used to get printed text. */
    private final ByteArrayOutputStream _byteArray = new ByteArrayOutputStream();
    /** Switch to print generated code. */
    private boolean _displayCode;

    /** Get value of a variable.
     * @param name Name of variable.
     * @return value of variable or null.
     */
    private Object getVar(String name) { return _variables.get(name); }

    /** Create PrintStream from byte array.
     * @return created PrintStream.
     */
    private PrintStream getPrintStream() {
        _byteArray.reset();
        try { // prepare printing commands
            return new PrintStream(_byteArray, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {  // never happens
            return new PrintStream(_byteArray, true);
        }
    }

    /** Parse string with source data according a rule from given BNF grammar,
     * @param g BNF grammar.
     * @param name name of rule in BNF grammar.
     * @param source source data,
     * @return null if no errors or error message.
     */
    private String parse(BNFGrammar g, String name, String source) {
        try {
            StringParser p = new StringParser(source);
            g.setUserObject(this);
            if (g.parse(p, name)) {
                if (g.getParser().errorWarnings()) {
                    return g.getParser().getReportWriter().getReportReader().printToString();
                }
                String s = g.getParsedString();
                return source.equals(s) ? null : s;
            } else {
                return name + " failed, " + (p.eos() ? "eos" : p.getPosition().toString()) + "; ";
            }
        } catch (Exception ex) {
            return "Exception " + ex;
        }
    }

    /** Parse string with source data according a rule "program" from given BNF grammar,
     * @param g BNF grammar.
     * @param source source data,
     * @return null if no errors or error message.
     */
    private String parse(BNFGrammar g, String source) {return parse(g, "program", source);}

////////////////////////////////////////////////////////////////////////////////


    String test(final String x, final BNFGrammar g, final String prog) {
        String s = parse(g, "program", prog);
        if (s != null) {
            return "Syntax error: " + s;
        }
        Object[] code = g.getParsedObjects();
        TestExprCompiler.CodeItem[] pc = null;
        try {
            pc = precompile(prog, code);
            if (_displayCode) {
                System.out.println(TestExprCompiler.printCode(prog, code, pc));
            }
            TestExprCompiler.execute(pc, _variables, getPrintStream());
            String y =new String(_byteArray.toByteArray(), Charset.availableCharsets().get("UTF-8"));
            y = SUtils.modifyString(y, "\r\n","\n");
            if (x.equals(y)) {
                return null;
            }
            return "'" + x + "', '" + y + "'\n" + TestExprCompiler.printCode(prog, code, pc);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return "'" + x + "', '?????'\n" + TestExprCompiler.printCode(prog, code, pc);
        }
    }

    private static void print(int i) {System.out.print(i);}

    /** Run test and print error information. */
    @Override
    public void test() {
        BNFGrammar g;
        g = BNFGrammar.compile(null, new File(getDataDir() + "TestExpr.bnf"), null);
        g.setUserObject(this);
        try {
            g.trace(null);
/**
if(false){}else print(2);print(1);
int i=1; switch(1) { case 1: print(i); break; default: print(0);}
           _displayCode = true;
            assertNull(test("1", g, "int i=1; switch(1) { case 1: print(i); break; default: print(0);}"));
if(true) return;
if(true) return;
//            assertNull(test("", g, "int i=0; switch(i) {}"));
if(true)return;
/**/
            assertNull(test("0", g, " /*x*/ print ( /*x*/ 0 /*x*/) /*x*/; "));
            assertNull(test("13", g, "print ( /*x*/ 12/*x*//*x*/ + 1 /*x*/);"));
            assertNull(test("abcdef", g, "print('abc' + 'def');"));
            assertNull(test("25abc", g, "print(( + ( + 3+2 ) * + 5 ) + 'abc');"));
            assertNull(test("25abc", g, "print( +( + 5 * ( 3 + 2 ) ) + 'abc');"));
            assertNull(test("50abc", g, "print(+(+5 * - ( - 7 + 2 ))*2+'abc');"));
            assertNull(test("true", g, "print( true );"));

            assertNull(test("false", g, "print( ! true);"));
            assertNull(test("true", g, "print(true|false);"));
            assertNull(test("false", g, "print(true & false);"));
            assertNull(test("true", g, "print(true ^ false);"));
            assertNull(test("true", g, "print( ! (true & false) );"));
            assertNull(test("-1abc", g, "print( - ( ( 3 + 3 ) / 5 ) + \"abc\" );"));
            assertNull(test("1.26abc", g, "print(((3 + 3.3)/5) + 'abc');"));
            assertNull(test("2", g, "print( - 1 + 3 );"));
            assertNull(test("1", g, " print(  min ( 2 , 1 ) ) ; "));
            assertNull(test("1.0", g, "print(min(2.0,1.0));"));
            assertNull(test("1.0",  g, "print(min(2,1.0));"));
            assertNull(test("1.0", g, "print(min(2.0,1));"));
            assertNull(test(String.valueOf(Math.sin(3.1)), g,"print(sin(3.1));"));
            assertNull(test(String.valueOf(Math.cos(3.1)), g,"print(cos(3.1));"));
            assertNull(test("", g, "Object i;"));
            assertNull(test("", g, "empty();"));
            assertNull(test("", g, "j = empty() + 'abc';"));
            assertEq("abc", getVar("j"));
            assertNull(test("", g, "i=~1;"));
            assertEq(-2, getVar("i"));
            assertNull(test("", g, "i = ~ ( ~ 1 );"));
            assertEq(1, getVar("i"));
            assertNull(test("", g, "i = 8; i = i << 2;"));
            assertEq(32, getVar("i"));
            assertNull(test("", g, "j = 'abc' + empty(); k = j + 'd';"));
            assertEq("abc", getVar("j"));
            assertEq("abcd", getVar("k"));
            assertNull(test("", g, "i=sin(3.14) ; "));
            assertEq(Math.sin(3.14), getVar("i"));
            assertNull(test("", g, "i=3; "));
            assertEq(3, getVar("i"));
            assertNull(test("", g, "i = 3 ; j = i * 5 ;"));
            assertEq(3, getVar("i"));
            assertEq(15, getVar("j"));
            assertNull(test("", g, "i=''; j=i+(5*3);"));
            assertEq("", getVar("i"));
            assertEq("15", getVar("j"));
            assertNull(test("", g, "i = \"\" ; j= i + ( 5 * 3 ) ;"));
            assertEq("", getVar("i"));
            assertEq("15", getVar("j"));
            assertNull(test("", g, "i=\"\"\"\"; j=i+(5*3);"));
            assertEq("\"15", getVar("j"));
            assertNull(test("", g, "i=''''; j=i+(5*3);"));
            assertEq("'15", getVar("j"));
            assertNull(test("", g, "i = ''''''; j = i + (5 *3);"));
            assertEq("''15", getVar("j"));
            assertNull(test("", g, "i = 'x''y';"));
            assertEq("x'y", getVar("i"));
            assertNull(test("", g, "i = '''x'''; j = i + (5 *3);"));
            assertEq("'x'15", getVar("j"));
            assertNull(test("", g, "i = '''''x'''''; j = i + (5 *3);"));
            assertEq("''x''15", getVar("j"));
            assertNull(test("", g, "i = '\"x\"'; j = i + (5 *3);"));
            assertEq("\"x\"15", getVar("j"));
            assertNull(test("", g, "i = 'abc'; j = (5 *3) + i;"));
            assertEq("abc", getVar("i"));
            assertEq("15abc", getVar("j"));
            assertNull(test("", g, "i = sin(3.14);"));
            assertEq(Math.sin(3.14), getVar("i"));
            assertNull(test("", g, "i = min(3.14, sin(3.14));"));
            assertEq(Math.sin(3.14), getVar("i"));
            assertNull(test("",  g, "i = max(3.14, sin(3.14 + 4));"));
            assertEq(3.14, getVar("i"));
            assertNull(test("", g, "i = max ( 3.15 , sin ( 3.14 ) ) ;"));
            assertEq(3.15, getVar("i"));
            assertNull(test("", g, "i = min(3.15, sin(3.14));"));
            assertEq(Math.sin(3.14), getVar("i"));
            assertNull(test("", g, "i = sin(0);"));
            assertEq(Math.sin(0), getVar("i"));
            assertNull(test("", g, "i = sin(1.5);"));
            assertEq(Math.sin(1.5), getVar("i"));
            assertNull(test("", g, "i = tanh(1.5);"));
            assertEq(Math.tanh(1.5), getVar("i"));
            assertNull(test("", g, "i=sqrt(2);"));
            assertEq(Math.sqrt(2), getVar("i"));
            assertNull(test("", g, "i = cbrt(2);"));
            assertEq(Math.cbrt(2), getVar("i"));
            assertNull(test("", g, "i = 3.15 == sin(0);"));
            assertEq(false, getVar("i"));
            assertNull(test("", g, "i = 8; i = i << 2;"));
            assertEq(32, getVar("i"));
            assertNull(test("", g, "i = 8; i <<= 2;"));
            assertEq(32, getVar("i"));
            assertNull(test("", g, "i = 8; i = i >> 2;"));
            assertEq(2, getVar("i"));
            assertNull(test("", g, "i = 8; i >>= 2;"));
            assertEq(2, getVar("i"));
            assertNull(test("", g, "i = -8; i = i >> 2;"));
            assertEq(-2, getVar("i"));
            assertNull(test("", g, "i = -8; i=i>>>2;"));
            assertEq(-8L >>> 2, getVar("i"));
            assertNull(test("", g, "i = -8; i >>>= 2;"));
            assertEq(-8L >>> 2, getVar("i"));
            assertNull(test("", g, "i = 3==3.0;"));
            assertEq(true, getVar("i"));
            assertNull(test("", g, "i = 3.15!=sin(0);"));
            assertEq(true, getVar("i"));
            assertNull(test("", g, "i = 1; j = i++;"));
            assertEq(2, getVar("i"));
            assertEq(1, getVar("j"));
            assertNull(test("", g, "i = 1; j = ++i;"));
            assertEq(2, getVar("i"));
            assertEq(2, getVar("j"));
            assertNull(test("", g, "i = 1; j = i--;"));
            assertEq(0, getVar("i"));
            assertEq(1, getVar("j"));
            assertNull(test("", g, "i = 1; j = i--;"));
            assertEq(0, getVar("i"));
            assertEq(1, getVar("j"));
            assertNull(test("", g, "i = 2.1; j = --i;"));
            assertEq(1.1, getVar("i"));
            assertEq(1.1, getVar("j"));
            assertNull(test("", g, "i = 1; i += 2;"));
            assertEq(3, getVar("i"));
            assertNull(test("", g, "i = 1; i -= 2;"));
            assertEq(-1, getVar("i"));
            assertNull(test("", g, "i = 1; i += 3.14;"));
            assertEq(3.14 + 1, getVar("i"));
            assertNull(test("", g, "i = 3; j = i % 2;"));
            assertEq(3, getVar("i"));
            assertEq(1, getVar("j"));
            assertNull(test("", g, "i = 1; i += 2;"));
            assertEq(3, getVar("i"));
            assertNull(test("", g, "i = 1; j = 1; k = i++; m = ++j;"));
            assertEq(2, getVar("i"));
            assertEq(2, getVar("j"));
            assertEq(1, getVar("k"));
            assertEq(2, getVar("m"));
            assertNull(test("", g, "i=(3 + 5)*2; j=i+1; k=i/2; l=j/2;m=j/2.0;"));
            assertEq(16, getVar("i"));
            assertEq(17, getVar("j"));
            assertEq(8, getVar("k"));
            assertEq(8, getVar("l"));
            assertEq(8.5, getVar("m"));
            assertNull(test("", g, "i = true; j = false; k = i == j; m = i != j;\n"
                 + "o = 1; p = 0; q = o > p; r = 1.0; s = 2.0; t = r <= s;"));
            assertEq(true, getVar("i"));
            assertEq(false, getVar("j"));
            assertEq(false, getVar("k"));
            assertEq(true, getVar("m"));
            assertEq(true, getVar("q"));
            assertEq(true, getVar("t"));
            assertNull(test("", g, "i=sin(3.14);"));
            assertEq(Math.sin(3.14), getVar("i"));
            assertNull(test("", g, "sin(3.14); i=1;"));
            assertEq(1, getVar("i"));
            assertNull(test("", g, "i=1*2+-(1+1);"));
            assertEq(0, getVar("i"));
            assertNull(test("", g, "i=null;"));
            assertEq(null, getVar("i"));
            assertNull(test("", g, "Object i; i = 0.0; i += sin(3.14);"));
            assertEq(Math.sin(3.14), getVar("i"));
            assertNull(test("3x\n", g, "print(min(3,14)); println('x');"));
            assertNull(test("Ahoj\n", g, "printf('Ahoj'); println();"));
            assertNull(test("3, 4, 5\nx\n", g, "printf('%d, %d, %d\nx\n', 3,4,5);"));
            assertNull(test("null\n1\n", g, "println(null);println(1);"));
            assertNull(test("null\n", g, "Object i=3; i=null; println(i);"));
            assertNull(test("12", g, "int i=1; do print(i); while (i++ < 2) ;"));
            assertNull(test("112", g, "int i=1; int j=1; do do print(j); while (i++ < 2); while (j++ < 2) ;"));
            assertNull(test("23", g, "int i=1; while ( i++ < 3 ) print(i);"));
            assertNull(test("5434", g, "int i=1; int j=6; {while (i++<3) {while (j-->3) print(j);} print(i);}"));
            assertNull(test("12", g, "for(int i=1; i<3; i++) print(i);"));
            assertNull(test("112212", g, "for(int i=1; i<3; i++) { print(i); for(int j=1; j<3; j++) print(j); }\n"));
            assertNull(test("", g, "if (false) print(1); "));
            assertNull(test("1", g, "if (true) print(1); "));
            assertNull(test("21", g, "if(false){} else print(2); print(1);"));
//            assertNull(test("21", g, "if(false){} else print(2);print(1);"));
            assertNull(test("12", g, "int i=1; if (i==1) {print(1); print(2);}"));
            assertNull(test("2", g, "int i = 1; { if (i == 1) {i = 2;} else {i = 0;} print(i);}"));
            assertNull(test("", g, "int i=1;if (i==1) {if (i!=1) print(1);}"));
            assertNull(test("1", g, "int i=1;if (i==1) {if (i==1) print(1);}"));
            assertNull(test("", g, "int i=1;if (i==1) if (i==1) if (i!=1) print(1);"));
            assertNull(test("2", g, "int i=1;if (i==1) {while (i++ ==1) print(i);}"));
            assertNull(test("1", g, "int i=1;if (i==1) if (i==1) if (i==1) print(1); else print(2);"));
            assertNull(test("19", g, "int i=1; if (i==1) print(1); print(9);"));
            assertNull(test("9", g, "int i=1; if (i!=1) print(1); print(9);"));
            assertNull(test("1", g, "int i=1; if (i==1) print(1); else print(-1);"));
            assertNull(test("-1", g, "int i=1; if (i!=1) print(1); else print(-1);"));
            assertNull(test("19", g, "int i=1; if (i==1) print(1); else print(-1); print(9);"));
            assertNull(test("-19", g, "int i=1; if (i!=1) print(1); else print(-1); print(9);"));
            assertNull(test("19", g, "int i=1; if (i==1) print(1); else print(-1); print(9);"));
            assertNull(test("-19", g, "int i=1; if (i!=1) print(1); else print(-1); print(9);"));
            assertNull(test("1", g, "int i=1; if (i==1) if (i==1) if (i==1) print(1); else print(2);"));
            assertNull(test("2", g, "int i=1;if (i==1) if (i==1) if (i!=1) print(1); else print(2);"));
//            assertNull(test("0", g, "int i=0; switch(i) {case 1: print(i); break; dafault: print(0);}"));
//            assertNull(test("1", g, "int i=1; switch(i) {case 1: print(i); break; dafault: print(0);}"));
        } catch (Exception ex) {fail(ex);}
    }

    /** Run test
     * @param args the command line arguments
     */
    public static void main(String... args) {
        if (runTest(args) > 0) {System.exit(1);}
    }
}
