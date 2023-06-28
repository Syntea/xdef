package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/**
 *
 * @author Vaclav Trojan
 */
public class Michal extends XDTester {
	public Michal() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
		Element el;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='A'>\n" +
"<A>\n"+
"  <X>\n"+
"    <C/>\n"+
"  </X>\n"+
"  <B>\n"+
//"    <C/>\n"+
"    <C xd:script='ref C'/>\n"+
"  </B>\n"+
"  <C/>\n"+
"</A>\n"+
"<C/>\n"+
"<xd:component>\n"+
"  %class bugreports.A %link test#A;\n"+
"</xd:component>\n"+
"</xd:def>");
			reporter = xp.genXComponent(clearTempDir(), "UTF-8", false, true);
			if (reporter.errorWarnings()) {
				fail(reporter.printToString());
			}
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