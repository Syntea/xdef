package bugreports;

import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

public class Selak extends XDTester {

	public Selak() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		String xml;
		XDPool xp;
		try {
			xdef =
"<xd:def  xmlns:xd='http://www.xdef.org/xdef/4.1' root='a'>\n"+
"  <xd:declaration>\n"+
"    String xp = \"let $b := 'abcd' return (0 to string-length($b)) !(substring($b,1,string-length($b) - .))\";\n"+
"    String s = xquery(xp);\n"+
"  </xd:declaration>\n"+
"  <a xd:script='init outln(s);'/>\n"+
"</xd:def>";
			xml = "<a/>";
			assertEq(xml, parse(xdef, "", xml , reporter));
			assertNoErrors(reporter);
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
