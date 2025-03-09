package mytests;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** Test ...
 * @author Trojan
 */
public class A extends XDTester {

	public A() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
		Element el;
		ArrayReporter reporter = new ArrayReporter();
		String xdef, xml;
		XDPool xp;
		XDDocument xd;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <A a='int()' b='int()'>\n" +
"    <B></B>\n" +
"  </A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xd = xp.createXDDocument();
			xml = "<A a='111' b='222'><B/></A>";
			reporter.clear();
			el = xd.xparse(xml, reporter);
			assertEq(el, KXmlUtils.parseXml(xml).getDocumentElement());
			assertNoErrorwarningsAndClear(reporter);
			System.out.println(KXmlUtils.nodeToString(el, true));
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
