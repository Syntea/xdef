package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import java.io.File;
import buildtools.STester;

/** Test of BNF.
 * @author Vaclav Trojan
 */
public class TestJava extends STester {

	public TestJava() {super();}

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
			ex.printStackTrace(System.err);
			return "Exception " + ex;
		}
	}


////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		String s;
		BNFGrammar g;
		try {
			g = BNFGrammar.compile(
				null, new File(getDataDir() + "JavaSyntax.bnf"), null);
			s = "i+1";
			assertEq(s, parse(g, "additive_expression", s));
//			s =
//"/*soubor LICENSE.TXT.*/\n"+
//"package test.common.bnf;\n" +
//"import org.xdef.sys.STester;\n" +
//"import x.Y;\n" +
//"public final class TestJava extends STester implements a.b, c.d {n" +
//"	public TestJava() {super();}\n" +
//"}" +
//"\n" +
//"";
//			assertEq(s, parse(g, "java_source", s));
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
