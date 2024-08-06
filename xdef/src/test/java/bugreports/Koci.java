package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/**
 * @author Vaclav Trojan
 */
public class Koci extends XDTester {
	public Koci() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		ArrayReporter reporter = new ArrayReporter();
		String xml;
		XDDocument xd;
		XDPool xp;
		String xdef;

////////////////////////////////////////////////////////////////////////////////

		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		try {
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd     =\"http://www.xdef.org/xdef/4.0\" root=\"Vehicle\"\n" +
"        impl-version =\"2024/06.0\" impl-date=\"2024-07-31\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"1..; ref Part\" />\n" +
"   </Vehicle>\n" +
"   <Part name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part2\"/>\n" +
"   </Part>\n" +
"   <Part2 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part3\"/>\n" +
"   </Part2>\n" +
"   <Part3 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part4\"/>\n" +
"   </Part3>\n" +
"   <Part4 name=\"string()\"/>\n" +
"</xd:def>";
			xml =
"<Vehicle>\n" +
"   <Part name=\"a1\">\n" +
"      <Part name=\"a2\" />\n" +
"      <Part name=\"a3\" />\n" +
"   </Part>  \n" +
"</Vehicle>";
			xp = compile(xdef);
//			xd = xp.createXDDocument();
//			assertEq(xml, xd.xparse(xml, null));
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("Vehicle", null));

			xdef =
"<xd:def xmlns:xd     =\"http://www.xdef.org/xdef/4.0\" root=\"Vehicle\"\n" +
"        impl-version =\"2024/06.0\" impl-date=\"2024-07-31\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"1..; ref Part\" />\n" +
"   </Vehicle>\n" +
"   \n" +
"   <Part name=\"string()\">\n" +
"      <Part xd:script=\"1; ref Part2\"/>\n" +
"   </Part>\n" +
"   <Part2 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part3\"/>\n" +
"   </Part2>\n" +
"   <Part3 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part4\"/>\n" +
"   </Part3>\n" +
"   <Part4 />\n" +
"</xd:def>";
			xp = compile(xdef);
//			xd = xp.createXDDocument();
//			assertEq(xml, xd.xparse(xml, null));
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("Vehicle", null));
		} catch (Exception ex) {fail(ex);}
if (true) return;
		try {
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
" int i = 0;\n"+
" Container c = [[%b=[%a = 'a', %b = 'b']], [%b=[%a = 'c', %b = 'd']]];\n"+
"</xd:declaration>\n"+
"<a xd:script='create c;'>\n"+
"  <b xd:script='occurs +;' a='string' b='string'/>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b a='a' b='b'/><b a='c' b='d'/></a>";
			assertEq(xml, create(xp, "", "a", reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, xd.xparse(xml, null));
			xd = xp.createXDDocument();
			assertEq(xml, xd.xcreate("Vehicle", null));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='Vehicle'>\n" +
//"   <Vehicle xd:script='create from(\"/Vehicle\");'>\n" +
//"   <Vehicle xd:script='create {return from(\"/Vehicle\");}'>\n" +
"   <Vehicle>\n" +
"     <Part xd:script='ref Part' />\n" +
"   </Vehicle>\n" +
"   <Part name='string()'>\n" +
"      <Part xd:script='?; ref Part; create from(\"Part\")'/>\n" +
//"      <Part xd:script='?; ref Part'/>\n" +
"   </Part>\n" +
"</xd:def>";
System.out.println(xdef);
			xp = XDFactory.compileXD(null, xdef);
			xp.displayCode();
			xd = xp.createXDDocument();
			xml =
"<Vehicle>\n" +
"  <Part name=\"platform\">\n" +
"    <Part name=\"wheel\"/>\n" +
"  </Part>\n" +
"</Vehicle>";
			assertEq(xml, xd.xparse(xml, null));
			xd = xp.createXDDocument();
			xd.setXDContext(
"<Vehicle>\n" +
"  <Part name=\"platform\">\n" +
"    <Part name=\"wheel\"/>\n" +
"    <Part name=\"x\"/>\n" +
"    <Part name=\"y\"/>\n" +
"  </Part>\n" +
"</Vehicle>");
			assertEq(xml, xd.xcreate("Vehicle", null));
		} catch (Exception ex) {fail(ex);}
if (true) return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A|B'>\n" +
"<xd:declaration scope='global'>\n" +
"     type  skodaTextDN string(1,4000);\n" +
"     type  castkaKcDN  long(0,999_999_999);\n" +
"</xd:declaration>\n"+
"  <A>\n" +
"    <B xd:script=\"*\"><C xd:script=\"0..1; ref C\"/></B>\n" +
"    <D xd:script=\"*\"><C xd:script=\"0..1; ref C\"/></D>\n" +
"  </A>\n" +
"  <B>\n" +
"    <Q xd:script=\"*\"><E xd:script=\"0..1; ref E\"/></Q>\n" +
"    <R xd:script=\"*\"><E xd:script=\"0..1; ref E\"/></R>\n" +
"  </B>\n"+
"  <C c='castkaKcDN'/>\n" +
"  <E e='skodaTextDN'>? string() </E>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xd = xp.createXDDocument("");
			xml = "<A><B><C c='1'/></B><D><C c='2'/></D></A>";
			if (reporter.errorWarnings())System.err.println(reporter);
			xml = "<B><Q><E e='1'>x</E></Q><R><E e='2'>y</E></R></B>";
			xd.xparse(xml, reporter);
			if (reporter.errorWarnings())System.err.println(reporter);
		} catch (Exception ex) {fail(ex);}
if(true)return;
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
