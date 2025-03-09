package mytests;

import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import test.XDTester;

/** Tests of BNF.
 * @author Vaclav Trojan
 */
public class MyTestBNF extends XDTester {
	public MyTestBNF() {super();}

	/** Parse source with rule with given name from BNF grammar.
	 * @param g BNF grammar.
	 * @param name rule name.
	 * @param source source to be parsed.
	 * @return parsed part of source.
	 */
	private static String p(final BNFGrammar g,
		final String name,
		final String source) {
		try {
			if (g.parse(new StringParser(source), name)) {
				return g.getParsedString();
			} else {
				return name + " failed, " + (g.getParser().eos()?
					"eos" : g.getParser().getPosition().toString()) +";\n"
					+ g.display(true);
			}
		} catch (Exception ex) {
			return printThrowable(ex);
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////
			_xdOfxd = XDFactory.compileXD(null,
					"classpath://org.xdef.impl.compile.XdefOfXdef*.xdef");

		String s, xdef, xml;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		try {
			BNFGrammar g = BNFGrammar.compile(
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
""			);
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
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:BNFGrammar name=\"test\">\n" +
"    S ::= [#9#10#13 ]+ /* white spaces */\n" +
"    A::= ('A', 'B') | ('C', 'D')\n"+
"  </xd:BNFGrammar>\n" +
"  <xd:declaration>\n" +
"     type x test.rule('A');\n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <b xd:script='*'> x; </b>\n" +
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b>BA</b><b>DC</b></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}