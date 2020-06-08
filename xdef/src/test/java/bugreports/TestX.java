package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import java.util.Properties;
import test.XDTester;

/** Test default property "xdef_warning"s and values "true" and "false".
 * @author Vaclav Trojan
 */
public class TestX extends XDTester {

	public TestX() {super();}

	public static boolean x(XXData x) {return true;}

	@Override
	/** Run test and display error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		String xml;
		String s;
		String xdef;
		xml = "<a a='y' b='z'/>";
		xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <a a=\"list('x','y')\" b=\"x()\">\n"+
" </a>\n"+
"</xd:def>";
		Properties props = new Properties();
		try {
			xp = XDFactory.compileXD(props, xdef, TestX.class); // no property
			xd = xp.createXDDocument();
			xd.xparse(xml, null);
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !s.contains("XDEF998")) {fail(ex);}
		}
		try {
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
				XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true
			xp = XDFactory.compileXD(null, xdef, TestX.class);
			xd = xp.createXDDocument();
			xd.xparse(xml, null);
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !s.contains("XDEF998")) {fail(ex);}
		}
		try {
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
				XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE); // false
			xp = XDFactory.compileXD(props, xdef, TestX.class);
			xd = xp.createXDDocument();
			xd.xparse(xml, null);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}