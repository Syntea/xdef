package test;

import org.xdef.XDFactory;
import org.xdef.impl.code.DefXQueryExpr;

/** Execute all tests fast.
 * @author Vaclav Trojan
 */
public class TestAll40 {

	/** @param args the command line arguments. */
	public static void main(String... args) {
		System.out.println("Build: " + XDFactory.getXDVersion());
		System.out.println("Testing java version: "
			+ System.getProperty("java.version") + " ("
			+ (DefXQueryExpr.isXQueryImplementation() ? "with" : "without")
			+ " Saxon library) ...");
		XDTester.setFulltestMode(false);
		test.xdef.TestAll40.runTests(args);
		test.xdutils.TestAll40.runTests(args);
	}
}