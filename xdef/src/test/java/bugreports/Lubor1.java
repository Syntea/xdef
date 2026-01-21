package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.chkCompoinentSerializable;

/** Tests.
 * @author Vaclav Trojan
 */
public class Lubor1 extends XDTester {

	public Lubor1() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	/** Run test and display error information. */
	@Override
	public void test() {
		boolean T = false;
		System.out.println("TempDir: " + getTempDir());
		System.out.println("SourceDir: " + getSourceDir());
////////////////////////////////////////////////////////////////////////////////
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
		Object o;
		String xdef, xml;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try {
			test.xdef.TestXComponents_G.class.getClass();
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='G' root='G'>\n" +
"  <xd:declaration scope=\"local\">\n" +
"     external method void test.xdef.TestXComponents_G.genXC(XXNode);\n" +
"  </xd:declaration>\n" +
"  <xd:component>\n" +
"   %bind g %with test.xdef.TestXComponents_G %link G#G/@g;\n" +
"   %bind YYY %with test.xdef.TestXComponents_G %link G#G/YYY;\n" +
"   %bind XXX %with test.xdef.TestXComponents_G %link G#G/XXX;\n" +
"   %class test.xdef.component.G extends test.xdef.TestXComponents_G %link G\n" +
"  </xd:component>\n" +
"\n" +
"  <G xd:script = 'finally ;' g = 'string'>\n" +
"    <XXX x = 'string; finally genXC()'/>\n" +
"    <YYY xd:script = '+' y = 'string; finally genXC()'/>\n" +
"  </G>\n" +
"\n" +
"</xd:def>";
			xp = org.xdef.XDFactory.compileXD(null, xdef);
			genXComponent(xp);
			xml = "<G g='g'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>";
			xd = xp.createXDDocument("G");
			o = org.xdef.sys.SUtils.getNewInstance("test.xdef.component.G");
			xd.setUserObject(o);
			xc = parseXC(xd, xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("<G g='g_'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>", xc.toXml());
			assertEq("x", XComponentUtil.get((XComponent) org.xdef.sys.SUtils.getObjectField(o, "_X"), "x"));
			assertEq("z",
				XComponentUtil.getVariable((XComponent) org.xdef.sys.SUtils.getObjectField(o, "_Y"),"y"));
			assertEq("g_", XComponentUtil.get(xc, "g"));
			assertEq("x", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "XXX"), "x"));
			assertEq("y", XComponentUtil.get((XComponent) XComponentUtil.listOf(xc, "YYY").get(0), "y"));
			XComponentUtil.set(xc, "XX", "abc");
			assertEq("abc", XComponentUtil.get(xc, "XX"));
		} catch (Exception ex) {fail(ex);}
if (T) return;
/**/
		try {
			test.xdef.TestXComponents_bindAbstract.class.getClass(); //force compilation
			test.xdef.TestXComponents_bindInterface.class.getClass(); //force compilation
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='XPerson' root='Person'>\n"+
"  <Person Name  = \"string()\"\n" +
"          Birth = \"xdatetime('dd.MM.yyyy')\"\n" +
"          Sex   = \"enum('M', 'W', 'X')\"/>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Lubor1_XCPerson\n" +
"        extends test.xdef.TestXComponents_bindAbstract\n" +
"        implements test.xdef.TestXComponents_bindInterface\n" +
"        %link XPerson#Person;\n" +
"    %bind Name %with test.xdef.TestXComponents_bindAbstract %link XPerson#Person/@Name;\n" +
"    %bind SBirth %with test.xdef.TestXComponents_bindAbstract %link XPerson#Person/@Birth;\n" +
"    %bind SexString %with test.xdef.TestXComponents_bindAbstract %link XPerson#Person/@Sex;\n" +
"  </xd:component>\n" +
"</xd:def>";
			xp = org.xdef.XDFactory.compileXD(null, xdef);
			genXComponent(xp);
//			genXComponentAndCopySources(xp);
			xc = (XComponent) org.xdef.sys.SUtils.getNewInstance(_package+".Lubor1_XCPerson");
			XComponentUtil.set(xc, "Name", "John Brown");
			XComponentUtil.set(xc, "Birth", new java.sql.Timestamp(new java.util.Date(0).getTime()));
			XComponentUtil.set(xc, "Sex", test.xdef.TestXComponents_bindEnum.M);
			xml = "<Person Birth='01.01.1970' Name='John Brown' Sex='M'/>";
			assertEq(xml, xc.toXml());
			xc = (XComponent) org.xdef.sys.SUtils.getNewInstance(_package+".Lubor1_XCPerson");
			XComponentUtil.set(xc, "Name", "John Brown");
			XComponentUtil.set(xc, "SBirth", new org.xdef.sys.SDatetime(new java.util.Date(0)));
			XComponentUtil.set(xc, "SexString", "M");
			assertEq(xml, xc.toXml());
			xd = xp.createXDDocument("XPerson");
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent(null, "Person", null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("M", ((test.xdef.TestXComponents_bindAbstract) xc).getSexString());
		} catch (RuntimeException ex) {fail(ex);}
deleteCreatedSources();
if (T) return;
/**/
		try { // the command %interface with extension
			xp = org.xdef.XDFactory.compileXD(null,
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"A | B\">\n" +
"  <A a = \"int()\" />\n" +
"  <B xd:script =\"ref A\" b=\"string()\" />\n" +
"<xd:component>\n" +
"%class "+_package+".LA_1 %link #A;\n" +
"%class "+_package+".LB_1 %link #B;\n" +
//"%class "+_package+".LB_1 extends LA_1 %link #B;\n" +
"%interface "+_package+".LA_1_I %link #A;\n" +
"%interface "+_package+".LB_1_I extends "+_package+".LA_1_I %link #B;\n" + // kasle na extends!!!!
"</xd:component>\n" +
"</xd:def>");
			genXComponent(xp);
//			genXComponentAndCopySources(xp);
			xd = xp.createXDDocument();
			xml = "<A a=\"1\" />";
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			assertEq(1, XComponentUtil.get(xc, "a"));
//			assertEq(1, ((LA_1_I) xc).geta());
			xml = "<B a=\"2\" b=\"c d\" />";
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			try {
//				assertEq(2, ((mytests.LA_1_I) xc).geta());
			} catch (Exception ex) {fail(ex);}
		} catch (RuntimeException ex) {fail(ex);}
deleteCreatedSources();
if (T) return;
/**/
		try {
			xp = org.xdef.XDFactory.compileXD(null, new String[] { // nested declaration of type
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_xc'>\n" +
"  <xd:component>\n" +
"    %class "+_package+".IdentDN %link D7_#A;\n" +
"    %class "+_package+".VymazDN extends "+_package+".IdentDN %link D7_#B;\n" +
"  </xd:component>\n" +
"</xd:def>",
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_' root='A | B'>\n" +
"  <xd:declaration scope=\"global\">\n" +
"    type  cisloDN num(5);\n" +
"    type  cj      string(1,50);\n" +
"    type  plan    gamDate();\n" +
"    type  rokDN   gamYear();\n" +
"  </xd:declaration>\n" +
"  <A RokDN=\"rokDN()\" CisloDN=\"cisloDN()\"/>\n" +
"  <B xd:script=\"ref A\" C=\"cj()\" P=\"? plan()\"/>\n" +
"</xd:def>",
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_decl'>\n" +
"  <xd:declaration scope=\"global\">\n" +
"    type  gamYear  long(1800, 2200);\n" +
"    type  gamDate  xdatetime('yyyyMMdd');\n" +
"  </xd:declaration>\n" +
"</xd:def>"});
			genXComponent(xp);
//			genXComponentAndCopySources(xp);
			xml = "<A RokDN=\"2021\" CisloDN=\"12345\"/>";
			assertEq(xml, parse(xp, "D7_", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "D7_", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(2021L, XComponentUtil.get(xc,"RokDN"));
			assertEq("12345", XComponentUtil.get(xc,"CisloDN"));
			xml ="<B RokDN=\"2021\" CisloDN=\"12345\" C=\"x\" P=\"20210524\"/>";
			assertEq(xml, parse(xp, "D7_", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "D7_", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(2021L, XComponentUtil.get(xc,"RokDN"));
			assertEq("12345", XComponentUtil.get(xc, "CisloDN"));
			assertEq("x", XComponentUtil.get(xc,"C"));
			assertEq(new org.xdef.sys.SDatetime("2021-05-24"), XComponentUtil.get(xc,"P"));
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
deleteCreatedSources();
if (T) return;
		try {
			xp = org.xdef.XDFactory.compileXD(null, new String[] { // nested declaration of type
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_xc'>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Lubor1Ident %link D7_#A;\n" +
"    %class "+_package+".Lubor1Vymaz extends "+_package+".Lubor1Ident %link D7_#B;\n" +
"    %interface "+_package+".Lubor1Ident_I %link D7_#A;\n" +
"    %interface "+_package+".Lubor1Vymaz_I extends "+_package+".Lubor1Ident_I %link D7_#B;\n" +
"  </xd:component>\n" +
"</xd:def>",
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_' root='A | B'>\n" +
"  <xd:declaration scope=\"global\">\n" +
"    type  cisloDN num(5);\n" +
"    type  cj      string(1,50);\n" +
"    type  plan    gamDate();\n" +
"    type  rokDN   gamYear();\n" +
"  </xd:declaration>\n" +
"  <A RokDN=\"rokDN()\" CisloDN=\"cisloDN()\"/>\n" +
"  <B xd:script=\"ref A\" C=\"cj()\" P=\"? plan()\"/>\n" +
"</xd:def>",
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_decl'>\n" +
"  <xd:declaration scope=\"global\">\n" +
"    type  gamYear  long(1800, 2200);\n" +
"    type  gamDate  xdatetime('yyyyMMdd');\n" +
"  </xd:declaration>\n" +
"</xd:def>"});
			genXComponent(xp);
//			genXComponentAndCopySources(xp);
			xml = "<A RokDN=\"2021\" CisloDN=\"12345\"/>";
			assertEq(xml, parse(xp, "D7_", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "D7_", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(2021L, XComponentUtil.get(xc,"RokDN"));
			assertEq("12345", XComponentUtil.get(xc,"CisloDN"));
			xml ="<B RokDN=\"2021\" CisloDN=\"12345\" C=\"x\" P=\"20210524\"/>";
			assertEq(xml, parse(xp, "D7_", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "D7_", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(2021L, XComponentUtil.get(xc,"RokDN"));
			assertEq("12345", XComponentUtil.get(xc, "CisloDN"));
			assertEq("x", XComponentUtil.get(xc,"C"));
			assertEq(new org.xdef.sys.SDatetime("2021-05-24"), XComponentUtil.get(xc,"P"));
		} catch (RuntimeException ex) {fail(ex);}
deleteCreatedSources();
if (T) return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='PlatneOd|PlatneOdDo' name='PIS_iop_common'>\n" +
"<PlatneOd PlatnostOd='date()'/>\n" +
"<PlatneOdDo xd:script='ref PlatneOd' PlatnostDo='date()'/>\n" +
"<xd:component>\n" +
" %class bugreports.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %class bugreports.PlatneOdDo extends bugreports.PlatneOd %link PIS_iop_common#PlatneOdDo;\n" +
" %interface bugreports.subelem.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %interface bugreports.subelem.PlatneOdDo extends bugreports.subelem.PlatneOd\n" +
"   %link PIS_iop_common#PlatneOdDo;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = org.xdef.XDFactory.compileXD(null, xdef);
//			genXComponent(xp);
			genXComponentAndCopySources(xp);
			xd = xp.createXDDocument("");
			xml = "<PlatneOd PlatnostOd='2025-01-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
			assertEq("2025-01-01", ((bugreports.PlatneOd) xc).getPlatnostOd().toString());

			xml = "<PlatneOdDo PlatnostOd='2025-01-01' PlatnostDo='2025-02-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
			assertEq("2025-01-01", ((bugreports.PlatneOdDo) xc).getPlatnostOd().toString());
			assertEq("2025-02-01", ((bugreports.PlatneOdDo) xc).getPlatnostDo().toString());
			assertEq("2025-01-01", ((bugreports.PlatneOd) xc).getPlatnostOd().toString());
		} catch (RuntimeException ex) {fail(ex);}
//deleteCreatedSources();
if (T) return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='PlatneOd|PlatneOdDo' name='PIS_iop_common'>\n" +
"<PlatneOd PlatnostOd='date()'/>\n" +
"<PlatneOdDo xd:script='ref PlatneOd' PlatnostDo='date()'/>\n" +
"<xd:component>\n" +
"%class bugreports.Lubor1Od %link PIS_iop_common#PlatneOd;\n" +
"%class bugreports.Lubor1OdDo\n" +
"       extends bugreports.Lubor1Od\n" +
"       %link PIS_iop_common#PlatneOdDo;\n" +
"\n" +
" %interface bugreports.Lubor1_IOd\n" +
"   %link PIS_iop_common#PlatneOd;\n" +
"%interface bugreports.Lubor1_IOdDo\n" +
//"   extends bugreports.Lubor1_IOd\n" +
"   %link PIS_iop_common#PlatneOdDo;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = org.xdef.XDFactory.compileXD(null, xdef);
			genXComponent(xp);
//			genXComponentAndCopySources(xp);
			xd = xp.createXDDocument("");
			xml = "<PlatneOd PlatnostOd='2025-01-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
//			assertEq("2025-01-01", ((bugreports.Lubor1_IOd) xc).getPlatnostOd().toString());
//			assertEq("2025-01-01", ((bugreports.Lubor1Od) xc).getPlatnostOd().toString());
			xml = "<PlatneOdDo PlatnostOd='2025-01-01' PlatnostDo='2025-02-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
//			assertEq("2025-01-01", ((bugreports.Lubor1_IOdDo) xc).getPlatnostOd().toString());
//			assertEq("2025-01-01", ((bugreports.Lubor1OdDo) xc).getPlatnostOd().toString());
//			assertEq("2025-02-01", ((bugreports.Lubor1_IOdDo) xc).getPlatnostDo().toString());
//			assertEq("2025-02-01", ((bugreports.Lubor1OdDo) xc).getPlatnostDo().toString());
////			assertEq("2025-01-01", ((bugreports.Lubor1_IOd) xc).getPlatnostOd().toString());
//			assertEq("2025-01-01", ((bugreports.Lubor1Od) xc).getPlatnostOd().toString());
		} catch (RuntimeException ex) {fail(ex);}
deleteCreatedSources();
if (T) return;
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
