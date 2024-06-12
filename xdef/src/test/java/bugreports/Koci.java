package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test illegal and moreAttributes.
 * @author Trojan
 */
public class Koci extends XDTester {
	public Koci() {super();}
	@Override
	/** Run test and display error information. */
	public void test() {
		Element el;
		String xdef, xml;
		XDDocument xd;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"  <A xd:script='option moreAttributes' x='illegal int' y='ignore int' />" +
"</xd:def>";
			xml = "<A x='1' y='2' z='3' />";
			parse(xdef, null, xml, reporter); //not reported!
			if (reporter.errors()) {
				System.out.println(reporter);
			} else {
				fail("error not reported");
			}
		} catch (Exception ex) {fail(ex);}
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"  <A xd:script='ref B; option moreAttributes' x='illegal' y='ignore int'/>\n" +
"  <B x='string()'/>" +
"</xd:def>";
			xml = "<A x='1' y='2' z='3' />";
			parse(xdef, null, xml, reporter);
			if (reporter.errors()) {
				System.out.println(reporter);
			} else {
				fail("error not reported");
			}
		} catch (Exception ex) {fail(ex);}
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"  <A xd:script='ref B;' x='illegal'/>\n" +
"  <B xd:script='option moreAttributes' x='int()' y='ignore int' />" +
"</xd:def>";
			xml = "<A x='1' y='2' z='3' />";
			parse(xdef, null, xml, reporter);
			if (reporter.errors()) {
				System.out.println(reporter);
			} else {
				fail("error not reported");
			}
		} catch (Exception ex) {fail(ex);}
//if(true)return;

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
