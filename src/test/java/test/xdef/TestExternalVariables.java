package test.xdef;

import builtools.XDTester;
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
		StringWriter strw;
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
			xd.setStdOut(strw = new StringWriter());
			xd.xparse(xml, reporter);
			xd.setVariable("i", "1");
			xd.xparse(xml, reporter);
			xd.setVariable("i","2"); //now variable is 2
			xd.xparse(xml, reporter);
			assertNoErrors(reporter);
			strw.close();
			assertEq("truetrue\n1falsetrue\n2falsetrue\n", strw.toString());
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
			xd.setStdOut(strw = new StringWriter());
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			strw.close();
			assertEq(strw.toString(),"truetrue");
			xd = xp.createXDDocument();
			xd.setStdOut(strw = new StringWriter());
			xd.setVariable("i", "1");
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			try {
				xd.setVariable("i", "2"); //throws exception - variable is fixed
				fail("error not reported");
			} catch (Exception ex) {
				s = ex.getMessage();
				if (s == null || s.indexOf("XDEF562") < 0) {
					fail(ex);
				}
			}
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			strw.close();
			//variable i remains unchaged
			assertEq(strw.toString(),"1false1false1false1false");
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
			xd.setStdOut(strw = new StringWriter());
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(strw.toString(),"truetrue");
			xd = xp.createXDDocument();
			xd.setStdOut(strw = new StringWriter());
			xd.setVariable("#i", "1");
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(0, xd.getVariable("#i").intValue());
			assertEq(strw.toString(),"1false1false");
		} catch (Exception ex) {fail(ex);}

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