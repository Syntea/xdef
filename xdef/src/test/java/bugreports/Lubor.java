package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class Lubor extends XDTester {

	public Lubor() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	/** Run test and display error information. */
	@Override
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
		Object o;
		String json, xdef, xml;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"PlatneOd\">\n" +
"<xd:json name='PlatneOd'>[\"*; dateTime()\"]</xd:json>\n" +
"<xd:component>\n" +
"  %interface "+_package+".Lubor_I_3 %link #PlatneOd;\n" +
"  %class "+_package+".Lubor_XC_3 %link #PlatneOd;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xd = xp.createXDDocument("");
			json = "[\"2025-03-12T16:37:09\"]";
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(o, xc.toXon());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"PlatneOd\">\n" +
"<xd:json name='PlatneOd'>{a: [\"*; dateTime()\"] }</xd:json>\n" +
"<xd:component>\n" +
"  %interface "+_package+".Lubor_I_2 %link #PlatneOd;\n" +
"  %class "+_package+".Lubor_XC_2 %link #PlatneOd;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xd = xp.createXDDocument("");
			json = "{\"a\": [\"2025-03-12T16:37:09\"]}";
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(o, xc.toXon());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"PlatneOd\">\n" +
"<PlatneOd PlatnostOd=\"dateTime()\" />\n" +
"<xd:component>\n" +
"  %interface "+_package+".Lubor_I %link #PlatneOd;\n" +
"  %class "+_package+".Lubor_XC %link #PlatneOd;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xd = xp.createXDDocument("");
			xml = "<PlatneOd PlatnostOd=\"2025-03-12T16:37:09\" />";
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"PlatneOd\">\n" +
"<PlatneOd>dateTime()</PlatneOd>\n" +
"<xd:component>\n" +
"  %interface "+_package+".Lubor_I_1 %link #PlatneOd;\n" +
"  %class "+_package+".Lubor_XC_1 %link #PlatneOd;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xd = xp.createXDDocument("");
			xml = "<PlatneOd>2025-03-12T16:37:09</PlatneOd>";
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}
}