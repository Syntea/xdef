package bugreports;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import test.XDTester;
import java.io.File;
import org.xdef.component.XComponent;

public class Lubor extends XDTester {

	public Lubor() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		XDPool xp;
		XComponent xc;
		try {
			File f = new File(getDataDir() + "Lubor_0.xdef");
			File f1 = new File(getDataDir() + "Lubor_1.xdef");
			xp = XDFactory.compileXD(null, f, f1);
			// Generate X-components
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xml = "<A c='c'><D d='d'/><X/></A>";
			xc = parseXC(xp,"A", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			xml = "<B c='c'><D d='d'/></B>";
			xc = parseXC(xp,"A", xml, null, reporter);
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