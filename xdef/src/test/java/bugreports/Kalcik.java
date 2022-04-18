package bugreports;

import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester.genXComponent;

public class Kalcik extends XDTester {

	public Kalcik() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		String xml;
		XDPool xp;
		XComponent xc;
		try {
			xdef = getDataDir() + "Kalcik.xdef";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml =
"<UserCommands>\n" +
"    <UserCommand Name = \"Abcd\" Label = \"efgh\">\n" +
"       <Output PlaceName = \"ijk\" ><any a='a' /></Output>\n" +
"    </UserCommand>\n" +
"</UserCommands>";
			assertEq(xml, parse(xp, "UserCommands", xml , reporter));
			assertNoErrors(reporter);
			xc = parseXC(xp,"UserCommands", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
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