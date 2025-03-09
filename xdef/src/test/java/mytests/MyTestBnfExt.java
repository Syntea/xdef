package mytests;

import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTestBnfExt extends XDTester {
	public MyTestBnfExt() {super();}


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

		String xdef;
		String xml;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();

////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd=\"" + _xdNS + "\" name=\"Example\" root=\"root\" >\n" +
"  <xd:BNFGrammar name=\"Expression\">\n" +
"    S ::= [#9#10#13 ]+ /* white spaces */\n" +
"    sign ::= (\"+\"|\"-\") /* sign or unary operator */\n" +
"    number ::= [0-9]+ (\".\" [0-9]+)? ((\"E\"|\"e\") (sign)? [0-9]+)?\n" +
"    identifier ::= [a-zA-Z] [a-zA-Z0-9]*\n" +
"    value ::= (sign S?)? (number | identifier | \"(\" S? expr S? \")\")\n" +
"    mul ::= value (S? (\"*\" | \"/\" | \"%\") S? mul)?\n" +
"    add ::= mul (S? (\"+\" | \"-\") S? add)?\n" +
"    expr ::= add \n" +
"  </xd:BNFGrammar>\n" +
"  <xd:BNFGrammar name = \"y\" extends = \"Expression\">\n" +
"    a::=  expr | '?'\n" +
"  </xd:BNFGrammar>\n" +
"\n" +
"  <xd:declaration>\n" +
"     type x y.rule('a');\n" +
"  </xd:declaration>\n" +
"\n" +
"  <root>\n" +
"    x;\n" +
"  </root>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<root>- abc + 3.14159 * -( 2.3 - 3e-2 ) / 2 * 10e5</root>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<root>?</root>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X_type\" root=\"a\">\n"+
"  <xd:BNFGrammar name = \"x\" scope = \"global\" >\n" +
"     numbers ::= [0-9]+ ( ',' [0-9]+ )*\n" +
"  </xd:BNFGrammar>\n" +
"  <xd:BNFGrammar name = \"y\" extends = \"x\" scope=\"local\">\n" +
"     hexa ::= ('X' | 'x') [0-9A-F]+\n" +
"\n" +
"  </xd:BNFGrammar>\n" +
"  <a a=\"y.rule('hexa')\"/>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			xml = "<a a=\"xFF00\"/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (RuntimeException ex) {fail(ex);}
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}