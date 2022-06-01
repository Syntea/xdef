package bugreports;

import org.xdef.XDFactory;
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
			String xdef1 =
"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
"<xd:def  xmlns:xd     =\"http://www.xdef.org/xdef/4.0\"\n" +
"         xmlns:s      =\"http://schemas.xmlsoap.org/soap/envelope\"\n" +
"         xd:name      =\"SoapRequest\"\n" +
"         xd:root      =\"s:Envelope\">\n" +
"\n" +
"    <s:Envelope>\n" +
"        <Header>\n" +
"        </Header>\n" +
"        <Body>\n" +
"        </Body>\n" +
"    </s:Envelope>\n" +
"</xd:def>";
			String xdef2 =
"<xd:def  xmlns:xd     =\"http://www.xdef.org/xdef/4.0\"\n" +
"         xmlns:s      =\"http://schemas.xmlsoap.org/soap/envelope\"\n" +
"         xd:name      =\"SoapRequest_impl\"\n" +
"         xd:root      =\"s:Envelope\">\n" +
"\n" +
//"    <s:Envelope  xd:script=\"implements SoapRequest#s:Envelope\">\n" +
"    <s:Envelope  xd:script=\"implements SoapRequest#s:Envelope\">\n" +
"        <Header>\n" +
"        </Header>\n" +
"        <Body>\n" +
"        </Body>\n" +
"    </s:Envelope>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, new String[]{xdef1, xdef2});
			
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def  xmlns:xd='http://www.xdef.org/xdef/4.1' name='M' root='X'>\n"+
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