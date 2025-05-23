package bugreports;

import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;


/** Test of bibliographic date from external disk.
 * @author Trojan
 */
public class B extends XDTester {
	public B() {super();}

	/** Run test and display error information. */
	@Override
	public void test() {
		XDDocument xd;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_STRING_CODES, "Windows-1250");
		try {
			xml = "<A>\n\n  <B a='б' />\n</A>";
			System.out.println(xml);
/**/
			xd = XDFactory.compileXD(props,
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <A>\n" +
"    <B a='string'/>\n" +
"  </A>\n" +
"</xd:def>").createXDDocument();
			parse(xd, xml, reporter);
			System.out.println(reporter);
/**/
			xd = XDFactory.compileXD(props,
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <A>\n" +
"    <B xd:script='option moreAttributes'/>\n" +
"  </A>\n" +
"</xd:def>").createXDDocument();
			parse(xd, xml, reporter);
			System.out.println(reporter);
/**/
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
