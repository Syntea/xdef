package bugreports;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import test.XDTester;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;

public class Smid extends XDTester {

	public Smid() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\"\n" +
"  xmlns:tns=\"http://www.w3schools.com\">\n" +
"  <tns:note>\n" +
"    <xd:sequence xd:script=\"occurs 1\">\n" +
"      <tns:to xd:script=\"occurs 1\">required string()</tns:to>\n" +
"    </xd:sequence>\n" +
"  </tns:note>\n" +
"</xd:def>";
			XDPool xp = compile(xdef);
			parse(xp,"", "<tns:note><tns:to/></tns:note>", reporter);
			System.out.println("Error not recognized");
		} catch (Exception ex) {
			if (!reporter.printToString().contains("XML080")) {fail(ex);}
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}