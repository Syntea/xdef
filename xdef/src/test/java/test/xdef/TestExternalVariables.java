package test.xdef;

import java.io.IOException;
import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import java.io.StringWriter;

/** Test getXComponentVariable/setVariable
 * @author Vaclav Trojan
 */
public final class TestExternalVariables extends XDTester {

	public TestExternalVariables() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		String xml;
		String xdef;
		XDPool xp;
		XDDocument xd;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
		String s;
		try {//dynamic errors
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external String i;\n"+
"    String j = i;\n"+
"  </xd:declaration>\n"+
"  <a xd:script='finally{out(i); out(i==null); out(j); outln(j==null);}'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a/>";
			xd.setStdOut(swr = new StringWriter());
			xd.xparse(xml, reporter);
			xd.setVariable("i", "1");
			xd.xparse(xml, reporter);
			xd.setVariable("i","2"); //now variable is 2
			xd.xparse(xml, reporter);
			assertNoErrorwarnings(reporter);
			swr.close();
			assertEq("truetrue\n1falsetrue\n2falsetrue\n", swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration scope='global'>\n"+
"    external final String i;\n"+
"    String j = i;\n"+
"  </xd:declaration>\n"+
"  <a xd:script='finally{out(i); out(i==null); out(j); out(j==null);}'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			xd = xp.createXDDocument();
			xd.setStdOut(swr = new StringWriter());
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			swr.close();
			assertEq(swr.toString(),"truetrue");
			xd = xp.createXDDocument();
			xd.setStdOut(swr = new StringWriter());
			xd.setVariable("i", "1");
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			try {
				xd.setVariable("i", "2"); //throws exception - variable is fixed
				fail("error not reported");
			} catch (Exception ex) {
				s = ex.getMessage();
				if (s == null || !s.contains("XDEF562")) {
					fail(ex);
				}
			}
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			swr.close();
			//variable i remains unchaged
			assertEq(swr.toString(),"1false1false1false1false");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration scope='local'>\n"+
"    external final String i;\n"+
"    String j = i;\n"+
"  </xd:declaration>\n"+
"  <a xd:script='finally{out(i); out(i==null); out(j); out(j==null);}'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			xd = xp.createXDDocument();
			xd.setStdOut(swr = new StringWriter());
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(),"truetrue");
			xd = xp.createXDDocument();
			xd.setStdOut(swr = new StringWriter());
			xd.setVariable("#i", "1");
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(0, xd.getVariable("#i").intValue());
			assertEq(swr.toString(),"1false1false");
		} catch (IOException | RuntimeException ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}