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
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' xd:name='B' xd:root='B'>\n" +
"<xd:component>\n" +
"  %class bugreports.HotovecBegControlId %link B#B;\n" +
"  %class bugreports.HotovecControlId\n" +
"           extends bugreports.Hotovec1\n" +
"           implements bugreports.Hotovec2\n" +
"           %link B#B/ControlId;\n" +
"  %bind OperationFormString\n" +
"           %with bugreports.Hotovec1\n" +
"           %link B#B/ControlId/@OperationForm;\n" +
"</xd:component>\n" +
"<B IdFlow=\"long()\">\n" +
"  <ControlId IdDefPartner=\"long()\" OperationForm=\"enum('DIR','TRY')\"/>\n" +
"</B>\n" +
"</xd:def>";

			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			assertNoErrorwarnings(reporter);
			xml =
"<B IdFlow=\"1012931\">\n" +
"  <ControlId IdDefPartner=\"5\" OperationForm=\"DIR\"/>\n" +
"</B>";
			assertEq(xml, parse(xp, "B", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "B", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
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