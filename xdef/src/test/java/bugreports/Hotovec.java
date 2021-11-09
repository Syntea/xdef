package bugreports;

import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

public class Hotovec extends XDTester {

	public Hotovec() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		String xml;
		XDPool xp;
		XComponent xc;
		try {
			xdef = "<xd:collection xmlns:xd='http://www.xdef.org/xdef/3.2'>\n" +
"<xd:def xd:name=\"B\" xd:root=\"Truck\">\n" +
"    <Truck V = \"string()\" MaxWeight = \"required int()\" />\n" +
" <xd:component>\n" +
"   %class bugreports.data.Truck %link B#Truck;\n" +
"   %bind V %with bugreports.data.Truck %link C#Vehicle/@VIN;\n" +
" </xd:component>\n" +
"</xd:def>\n" +
"<xd:def xd:name=\"C\" xd:root=\"Vehicle\">\n" +
"    <Vehicle VIN='string()'/>\n" + " <xd:component>\n" +
"   %class bugreports.data.Vehicle extends bugreports.data.Truck\n" +
"          %link C#Vehicle;\n" +
" </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			assertNoErrorwarnings(reporter);
			xml = "<Truck V = \"123abc\" MaxWeight = \"12345\"/>";
			assertEq(xml, parse(xp, "B", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "B", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			xml = "<Vehicle VIN='123abc'/>";
			xc = parseXC(xp, "C", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			clearTempDir(); // delete temporary files.
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = "<xd:collection xmlns:xd='http://www.xdef.org/xdef/3.2'>\n" +
"<xd:def xd:name=\"B1\"\n xd:root=\"Truck1\">\n" +
"    <Truck1 xd:script=\"ref C1#Vehicle1\" MaxWeight=\"required int()\"/>\n" +
" <xd:component>\n" +
"   %class bugreports.data.Truck1 extends bugreports.data.Vehicle1\n" +
"            %link B1#Truck1;\n" +
"   %bind VIN %with bugreports.data.Vehicle1 %link B1#Truck1/@VIN;\n" +
" </xd:component>\n" +
"</xd:def>\n" +
"<xd:def xd:name=\"C1\" xd:root=\"Vehicle1\">\n" +
"    <Vehicle1 VIN='string()'/>\n" +
" <xd:component>\n" +
"   %class bugreports.data.Vehicle1 %link C1#Vehicle1;\n" +
" </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			assertNoErrorwarnings(reporter);
			xml = "<Truck1 VIN = \"123abc\" MaxWeight = \"12345\"/>";
			assertEq(xml, parse(xp, "B1", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "B1", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			xml = "<Vehicle1 VIN='123abc'/>";
			xc = parseXC(xp, "C1", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			clearTempDir(); // delete temporary files.
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = "<xd:collection xmlns:xd='http://www.xdef.org/xdef/3.2'>\n" +
"<xd:def xd:name=\"B2\"  xd:root=\"Truck2\">\n" +
"    <Truck2 V = \"string()\" MaxWeight = \"required int()\" />\n" +
" <xd:component>\n" +
"   %class bugreports.data.Truck2\n" +
"          extends bugreports.data.Vehicle2 %link B2#Truck2;\n" +
"   %bind VIN %with bugreports.data.Vehicle2 %link B2#Truck2/@V;\n" +
" </xd:component>\n" +
"</xd:def>\n" +
"<xd:def xd:name=\"C2\" xd:root=\"Vehicle2\">\n" +
"    <Vehicle2 VIN='string()'/>\n" + " <xd:component>\n" +
"   %class bugreports.data.Vehicle2 %link C2#Vehicle2;\n" +
" </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			assertNoErrorwarnings(reporter);
			xml = "<Truck2 V = \"123abc\" MaxWeight = \"12345\"/>";
			assertEq(xml, parse(xp, "B2", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "B2", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			xml = "<Vehicle2 VIN='123abc'/>";
			xc = parseXC(xp, "C2", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			clearTempDir(); // delete temporary files.
		} catch (Exception ex) {fail(ex);}		
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