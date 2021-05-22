package bugreports;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import test.XDTester;
import org.w3c.dom.Element;
import org.xdef.XDConstants;

public class Smid extends XDTester {

	public Smid() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef, xml;
		XDPool xp;
		Element el;
		try {
			xdef =
"<xd:def xmlns:xd='"+XDConstants.XDEF31_NS_URI+"' xd:root='A'>\n" +
"  <xd:declaration>\n" +
"    type t1 string(1, 40);\n" +
"  </xd:declaration>\n" +
"  <xd:declaration>\n" +
"    type t2 t1();\n" +
"  </xd:declaration>\n" +
"<A>t2();</A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			// Generate X-components
			xml = "<A>olijrpgio</A>";
			el = parse(xp,"", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
		try {
			xp = XDFactory.compileXD(null, getDataDir() + "D3A.xdef");
			// Generate X-components
			xml = getDataDir() + "D3A.xml";
			el = parse(xp,"", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}