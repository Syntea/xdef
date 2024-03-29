package bugreports;

import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

public class Kocman extends XDTester {

	public Kocman() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		String xml;
		XDPool xp;
		XComponent xc;
		try {
			xdef =
"<xd:def  xmlns:xd='"+_xdNS+"' name='M' root='X'>\n"+
"<xd:component>%class bugreports.data.M %link X</xd:component>\n"+
"  <xd:any xd:name='X'\n"+
"     xd:script='options moreAttributes, moreElements, moreText'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml = "<A><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></A>";
			assertEq(xml, parse(xp, "M", xml , reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp,"M", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
//System.out.println(JsonUtil.xmlToJson(xc.toXml()));
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