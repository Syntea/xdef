package complaints;

import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import buildtools.XDTester;

public class Kocman extends XDTester {

	public Kocman() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		String xml;
		XDPool xp;
		try {
			xdef =
"<xd:def  xmlns:xd='http://www.syntea.cz/xdef/3.1' name='M' root='X'>\n"+
"<xd:component>%class complaints.data.M %link X</xd:component>\n"+
"  <xd:any xd:name='X'\n"+
"     xd:script='options moreAttributes, moreElements, moreText'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			reporter = org.xdef.component.GenXComponent.genXComponent(xp,
				"src/test/java", null, false, true);
			if (reporter.errorWarnings()) {
				System.out.println(reporter.printToString());
			}
		} catch (Exception ex) {
			fail(ex);
			return;
		}
/*xx*/
		try {
			xml = "<A><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></A>";
			assertEq(xml, parse(xp, "M", xml , reporter));
			assertNoErrors(reporter);
			complaints.data.M p = (complaints.data.M)
				parseXC(xp,"M", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			System.out.println(p.toJson());
		} catch (Exception ex) {fail(ex);}
/*xx*/
	}
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}
