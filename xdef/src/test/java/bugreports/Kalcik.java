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
		String dataDir = getSourceDir() + "data/";
		try {
			System.out.println();
			xdef = dataDir+"UserCommands.xdef";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			if (reporter.errorWarnings()) {
				System.out.println(reporter.printToString());
			}
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
