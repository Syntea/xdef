package complaints;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import buildtools.XDTester;
import java.io.File;

public class Lubor extends XDTester {

	public Lubor() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		XDPool xp;
		try {
			File f = new File(getDataDir() + "Lubor_0.xdef");
			File f1 = new File(getDataDir() + "Lubor_1.xdef");
			xp = XDFactory.compileXD(null, f, f1);
			// Generate X-components
			reporter = org.xdef.component.GenXComponent.genXComponent(
				xp, "src/test/java", null, false, true);
			if (reporter.errorWarnings()) {
				reporter.checkAndThrowErrors();
			}
/*xx*/
			xml = "<A c='c'><D d='d'/><X/></A>";
			complaints.data.A p = (complaints.data.A)
				parseXC(xp,"A", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			xml = "<B c='c'><D d='d'/></B>";
			complaints.data.B q = (complaints.data.B)
				parseXC(xp,"A", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, q.toXml());
/*xx*/
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}
