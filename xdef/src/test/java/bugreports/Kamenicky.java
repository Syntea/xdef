package bugreports;

import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

public class Kamenicky extends XDTester {

	public Kamenicky() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		String xdef;
		XDPool xp;
		try {
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" root=\"a\" >\n" +
" <xd:declaration scope=\"local\">\n" +
"     ParseResult pr() {\n" +
"        ParseResult p = string(2);\n" +
"        if (p.matches()) p = enum(\"ab\",\"cd\");\n" +
"        return p;\n"+
"     }\n"+
"   type x pr();\n" +
"</xd:declaration>\n" +
"<a a=\"x()\" />\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xp.displayCode();
			xml = "<a a='ab'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='a'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xml = "<a a='12'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}