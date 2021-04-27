package test.xdutils;

import java.util.Properties;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import test.XDTester;

/** Example of using method parseType. */
public class TestParseType extends XDTester {

	public TestParseType() {super();}

	private static String test(XDPool xp,
		String xdName,
		String type,
		String dataOK,
		String dataErr) {
		XDDocument xd = xp.createXDDocument(xdName);
		XDParseResult pr = xd.parseXDType(type, dataOK);
		String result = pr.errors() ? "ERR " +  pr.getReporter() : "";
		pr = xd.parseXDType(type, dataErr);
		result += pr.errors() ? "" :
			(result.isEmpty() ? "" : "; ") + "Error not detected";
		return type + " " + (result.isEmpty() ? "OK" : result);
	}

	@Override
	public void test() {
		String xdef1 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='A'>\n"+
"  /* This declaration contains types to be checked. */\n"+
"  <xd:declaration scope='local'>\n"+
"    type t1 int();\n"+
"    type t2 starts('wsdl:');\n"+
"    uniqueSet u{t:t1; s:t2};\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef2 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='B'>\n"+
"  <xd:declaration scope='local'>\n"+
"    BNFGrammar g = new BNFGrammar('\n"+
"      x ::= S? [0-9]+\n"+
"      y ::= S? [a-zA-Z]+\n"+
"      S ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      z ::= x | y\n"+
"    ');\n"+
"    type t3 g.rule('z');\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef3 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='C'>\n"+
"  <xd:declaration scope='local'>\n"+
"    type t4 tt();\n"+
"    boolean tt() {\n"+
"      return getText().startsWith('a');\n"+
"    }\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef4 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='X'\n"+
"        importLocal='A, B, C'>\n"+
"</xd:def>";

		Properties props = new Properties();
		XDPool xp = XDFactory.compileXD(props, xdef1, xdef2, xdef3, xdef4);
		String xdefName = "X";

		assertEq("t1 OK", test(xp, xdefName, "t1", "123", "123s"));
		assertEq("t2 OK", test(xp, xdefName, "t2", "wsdl:1", "wsdl1"));
		assertEq("t3 OK", test(xp, xdefName, "t3", "abc", "xy12"));
		assertEq("t4 OK", test(xp, xdefName, "t4", "a1 2x", "1a 2x"));
		assertEq("u.t OK", test(xp, xdefName, "u.t", "123", "123x"));
		assertEq("u.s OK", test(xp, xdefName, "u.s", "wsdl:x", "wdl:x"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}