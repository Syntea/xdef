package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** test create mode with recursive reference.
 * @author Trojan
 */
public class Koci extends XDTester {

	public Koci() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}
	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////
		String xdef, xml;
		XDDocument xd;
		XDPool xp;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <Vehicle><Part xd:script=\"0..; ref X\"/></Vehicle>\n" +
"   <X name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref X\"/>\n" + ///////
"   </X>   \n" +
"</xd:def>";

			compile(xdef);
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<Vehicle><Part name=\"a1\"><Part name=\"a2\"/><Part name=\"a3\"/></Part></Vehicle>";
			xd.setXDContext(xml);
			el = xd.xcreate("Vehicle", reporter);
			assertEq(xml, el); // error?
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <Vehicle><Part xd:script=\"0..; ref X\"/></Vehicle>\n" +
"   <X xd:script=\"create from('Part')\" name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref X\"/>\n" + ///////
"   </X>  \n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<Vehicle><Part name=\"a1\"><Part name=\"a2\"/><Part name=\"a3\"/></Part></Vehicle>";
			xd.setXDContext(xml);
			el = xd.xcreate("Vehicle", reporter);
			assertEq(xml, el);
		} catch (RuntimeException ex) {fail(ex);}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}
}
