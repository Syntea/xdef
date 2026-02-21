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

	// Test declared type with valid data.
	private static String testValid(XDPool xp, // compiled XDPool
		String xdName, // name of Xdefinition
		String type, // name of declared type
		String dataOK) {// valid data
		XDDocument xd = xp.createXDDocument(xdName);
		XDParseResult pr = xd.parseXDType(type, dataOK);
		return pr.errors() ? "Error " +  pr.getReporter() : "";
	}

	// Test declared type with invalid data.
	private static String testInvalid(XDPool xp, // compiled XDPool
		String xdName, // name of Xdefinition
		String type, // name of declared type
		String dataOK) {// invalid data
		XDDocument xd = xp.createXDDocument(xdName);
		XDParseResult pr = xd.parseXDType(type, dataOK);
		return !pr.errors() ? "Error not detected" : "";
	}

	@Override
	public void test() {
		String xdef1 =
"<xd:def xmlns:xd='"+_xdNS+"' name='A'>\n"+
"  /* This declaration contains types to be checked. */\n"+
"  <xd:declaration scope='local'>\n"+
"    type t1 int();\n"+
"    type t2 starts('wsdl:');\n"+
"    uniqueSet u{t:t1; s:t2};\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef2 =
"<xd:def xmlns:xd='"+_xdNS+"' name='B'>\n"+
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
"<xd:def xmlns:xd='"+_xdNS+"' name='C'>\n"+
"  <xd:declaration scope='local'>\n"+
"    type t4 tt();\n"+
"    boolean tt() { return getText().startsWith('a'); }\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef4 = "<xd:def xmlns:xd='"+_xdNS+"' name='X' importLocal='A, B, C'> </xd:def>";

		Properties props = new Properties();
		XDPool xp = XDFactory.compileXD(props, xdef1, xdef2, xdef3, xdef4);

		assertEq("", testValid(xp, "X","t1","123"));
		assertEq("", testInvalid(xp, "X","t1","123s"));

		assertEq("", testValid(xp, "X","t2","wsdl:1"));
		assertEq("", testInvalid(xp, "X","t2","wsdl1"));

		assertEq("", testValid(xp, "X","t3","abc"));
		assertEq("", testInvalid(xp, "X","t3","xy12"));

		assertEq("", testValid(xp, "X","t4","a1 2x"));
		assertEq("", testInvalid(xp, "X","t4","1a 2x"));

		assertEq("", testValid(xp, "X","u.t","123"));
		assertEq("", testInvalid(xp, "X","u.t","123x"));

		assertEq("", testValid(xp, "X","u.s","wsdl:x"));
		assertEq("", testInvalid(xp, "X","u.s","wdl:x"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}