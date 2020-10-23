package bugreports;

import java.io.File;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.component.GenXComponent;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTest extends XDTester {
	public MyTest() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	public static boolean a(boolean a, String b) {return true;}
	public static int b(String b) {return 0;}
	public static void c() {}

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

		String tempDir = getTempDir();
		File f = new File(getTempDir());
		if (f.exists() && !f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()
				+ " is not directory");
		}
		f.mkdir();
		tempDir = f.getAbsolutePath().replace('\\', '/');
		if (!tempDir.endsWith("/")) {
			tempDir += '/';
		}
		XDPool xp;
		String xdef;
		String xml;
		String s;
		XDDocument xd;
		Element el;
		XDOutput xout;
		StringWriter strw;
		Report rep;
		ArrayReporter reporter = new ArrayReporter();

////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='x'>\n"+
"<x>\n"+
"  <a xd:script='*'>\n"+
"    jlist(1,999, %item=[jvalue()])\n"+
"  </a>\n"+
"  <b xd:script='*'>\n"+
"    jlist(1,999, %item=[union(%item=[jnull,boolean()])])\n"+
"  </b>\n"+
"  <c xd:script='*'>\n"+
"    jlist(1, 999, %item=[jnumber()])\n"+
"  </c>\n"+
"  <d xd:script='*'>\n"+
"    jlist(1,999, %item=[int()])\n"+
"  </d>\n"+
"  <e xd:script='*'>\n"+
"    jlist(1,999, %item=[jstring(), jnumber()])\n"+
"  </e>\n"+
"</x>\n"+
"<xd:component>%class bugreports.MyTestX %link x</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			String components = new File(getSourceDir()).getParent();
			GenXComponent.genXComponent(xp, components, "UTF-8");
//if(true)return;
			xml =
"<x>\n"+
"  <a>123 null false</a>\n"+
"  <a>\"false\"</a>\n"+
"  <a>123</a>\n"+
"  <a>3.14E+3</a>\n"+
"  <a>false</a>\n"+
"  <a>abc</a>\n"+
"  <a>1 abc</a>\n"+
"  <a>1 false abc</a>\n"+
"  <a>null 123 1 false</a>\n"+
"  <a>null 123 false abc \"\"</a>\n"+
"  <b>null</b>\n"+
"  <b>true</b>\n"+
"  <b>true null</b>\n"+
"  <b>null true</b>\n"+
"  <b>null true false null</b>\n"+
"  <c>0</c>\n"+
"  <c>123.45</c>\n"+
"  <c>1 -2 3</c>\n"+
"  <c>-0.99</c>\n"+
"  <c>-0.99E+3</c>\n"+
"  <d>1 -2 3</d>\n"+
"  <e>abc -2</e>\n"+
"  <e>abc -2</e>\n"+
"  <e>abc -2 \"\" 3.14E+3 x 1.5 y -0.99</e>\n"+
"</x>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
//			bugreports.MyTestX p =
//				(bugreports.MyTestX) parseXC(xp,"", xml , null, reporter);
////			System.out.println(p.get$a());
//			assertNoErrorwarnings(reporter);
//			assertEq(xml, p.toXml());
		} catch (Exception ex) {fail(ex);}
if(true)return;
		try {
			// \p{Lu} capital letters
			// \p{Ll} small letters
			// \.     dot
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='string(%pattern=[\"\\\\p{Lu}(\\\\.|\\\\p{Ll}+)( \\\\p{Lu}(\\\\p{Ll}*|\\\\.))*\"]);'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='Novák'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='P. Novák'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='Č.'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='Č. Ž.'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='F. X. Šalda'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method XDParseResult bugreports.MyTest_0.kp(XXNode, XDValue[]);\n"+
"</xd:declaration>\n"+
"<a a='kp(1,5,%totalDigits=1,%enumeration=[1,3],%pattern=[\"\\\\d\"])'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='3'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
////////////////////////////////////////////////////////////////////////////////
		try {
			if (new File(tempDir).exists()) {
				SUtils.deleteAll(tempDir, true);
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