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
public class Lubor2 extends XDTester {

	public Lubor2() {super();}

	/** Run test and display error information. */
	@Override
	public void test() {
		boolean T = false;
////////////////////////////////////////////////////////////////////////////////
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
		String xdef, xml;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try { //TEST1
			System.out.println("[INFO] Xdefinition version: " + XDFactory.getXDVersion());
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='PlatneOd|PlatneX' name='PIS_iop_common'>\n" +
"<PlatneOd PlatnostOd='date()'/>\n" +
"<PlatneOdDo xd:script='ref PlatneOd' PlatnostDo='date()'/>\n" +
"<PlatneX xd:script='ref PlatneOdDo' Hodnota = '? int()'/>\n" +
"<xd:component>\n" +
" %class bugreports.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %class bugreports.PlatneX1\n" +
"   implements bugreports.subelem.X\n" +
"   %link PIS_iop_common#PlatneX;\n" +
" %interface bugreports.subelem.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %interface bugreports.subelem.PlatneOdDo\n" +
"   extends bugreports.subelem.PlatneOd\n" +
"   %link PIS_iop_common#PlatneOdDo;\n" +
" %interface bugreports.subelem.PlatneX\n" +
"   extends bugreports.subelem.PlatneOdDo\n" +
"   %link PIS_iop_common#PlatneX;\n" +
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
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			xml = "<PlatneX PlatnostOd='2025-01-01' PlatnostDo='2025-02-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
			assertEq("2025-01-01", ((bugreports.PlatneX1) xc).getPlatnostOd().toString());
			assertEq("2025-02-01", ((bugreports.subelem.PlatneOdDo) xc).getPlatnostDo().toString());
			assertEq("2025-01-01", ((bugreports.PlatneX1) xc).getPlatnostOd().toString());
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			assertNull(((bugreports.PlatneX1 ) xc).getHodnota());
		} catch (RuntimeException ex) {fail(ex);}
//deleteCreatedSources();
if (T) return;
/**/
		try { //TEST2
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='PlatneOd|PlatneX' name='PIS_iop_common'>\n" +
"<PlatneOd PlatnostOd='date()'/>\n" +
"<PlatneOdDo xd:script='ref PlatneOd' PlatnostDo='date()'/>\n" +
"<PlatneX xd:script='ref PlatneOdDo' Hodnota = '? int()'/>\n" +
"<xd:component>\n" +
" %class bugreports.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %class bugreports.PlatneX2\n" +
"   implements bugreports.subelem.X\n" +
"   %link PIS_iop_common#PlatneX;\n" +
" %interface bugreports.subelem.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %interface bugreports.subelem.PlatneOdDo\n" +
"   extends bugreports.subelem.PlatneOd\n" +
"   %link PIS_iop_common#PlatneOdDo;\n" +
" %interface bugreports.subelem.PlatneX\n" +
"   extends bugreports.subelem.PlatneOdDo\n" +
"   %link PIS_iop_common#PlatneX;\n" +
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
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			xml = "<PlatneX PlatnostOd='2025-01-01' PlatnostDo='2025-02-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
			assertEq("2025-01-01", ((bugreports.PlatneX2) xc).getPlatnostOd().toString());
			assertEq("2025-02-01", ((bugreports.subelem.PlatneOdDo) xc).getPlatnostDo().toString());
			assertEq("2025-01-01", ((bugreports.PlatneX2) xc).getPlatnostOd().toString());
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			assertNull(((bugreports.PlatneX2 ) xc).getHodnota());
		} catch (RuntimeException ex) {fail(ex);}
//deleteCreatedSources();
if (T) return;
/**/
		try { //TEST3
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='PlatneOd|PlatneX' name='PIS_iop_common'>\n" +
"<PlatneOd PlatnostOd='date()'/>\n" +
"<PlatneOdDo xd:script='ref PlatneOd' PlatnostDo='date()'/>\n" +
"<PlatneX xd:script='ref PlatneOdDo' Hodnota = '? int()'/>\n" +
"<xd:component>\n" +
" %class bugreports.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %class bugreports.PlatneX3\n" +
"   implements bugreports.subelem.X\n" +
"   %link PIS_iop_common#PlatneX;\n" +
" %interface bugreports.subelem.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %interface bugreports.subelem.PlatneOdDo\n" +
"   %link PIS_iop_common#PlatneOdDo;\n" +
" %interface bugreports.subelem.PlatneX\n" +
"   %link PIS_iop_common#PlatneX;\n" +
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
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			xml = "<PlatneX PlatnostOd='2025-01-01' PlatnostDo='2025-02-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
			assertEq("2025-01-01", ((bugreports.PlatneX3) xc).getPlatnostOd().toString());
			assertEq("2025-02-01", ((bugreports.subelem.PlatneOdDo) xc).getPlatnostDo().toString());
			assertEq("2025-01-01", ((bugreports.PlatneX3) xc).getPlatnostOd().toString());
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			assertNull(((bugreports.PlatneX3 ) xc).getHodnota());
		} catch (RuntimeException ex) {fail(ex);}
//deleteCreatedSources();
if (T) return;
/**/
		try { //TEST4
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='PlatneOd|PlatneX' name='PIS_iop_common'>\n" +
"<PlatneOd PlatnostOd='date()'/>\n" +
"<PlatneOdDo xd:script='ref PlatneOd' PlatnostDo='date()'/>\n" +
"<PlatneX xd:script='ref PlatneOdDo' Hodnota = '? int()'/>\n" +
"<xd:component>\n" +
" %class bugreports.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %class bugreports.PlatneX4\n" +
"   implements bugreports.subelem.X, bugreports.subelem.PlatneOdDo, bugreports.subelem.PlatneOd\n" +
"   %link PIS_iop_common#PlatneX;\n" +
" %interface bugreports.subelem.PlatneOd %link PIS_iop_common#PlatneOd;\n" +
" %interface bugreports.subelem.PlatneOdDo\n" +
"   extends bugreports.subelem.PlatneOd\n" +
"   %link PIS_iop_common#PlatneOdDo;\n" +
" %interface bugreports.subelem.PlatneX\n" +
"   extends bugreports.subelem.PlatneOdDo\n" +
"   %link PIS_iop_common#PlatneX;\n" +
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
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			xml = "<PlatneX PlatnostOd='2025-01-01' PlatnostDo='2025-02-01'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(xml, (xc = xd.xparseXComponent(xml, null, reporter)).toXml());
			assertNoErrorsAndClear(reporter);
			assertEq("2025-01-01", ((bugreports.PlatneX4) xc).getPlatnostOd().toString());
			assertEq("2025-02-01", ((bugreports.subelem.PlatneOdDo) xc).getPlatnostDo().toString());
			assertEq("2025-01-01", ((bugreports.PlatneX4 ) xc).getPlatnostOd().toString());
			assertEq("2025-01-01", ((bugreports.subelem.PlatneOd) xc).getPlatnostOd().toString());
			assertNull(((bugreports.PlatneX4 ) xc).getHodnota());
		} catch (RuntimeException ex) {fail(ex);}
//deleteCreatedSources();
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