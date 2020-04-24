package test;

import org.xdef.impl.code.DefXQueryExpr;

/** Execute all tests verbose.
 * @author Vaclav Trojan
 */
public class FullTestAll {
	/** @param args the command line arguments. */
	public static void main(String... args) {
		System.out.println("Testing java version: "
			+System.getProperty("java.version") + " (with"
			+ (DefXQueryExpr.isXQueryImplementation() ? "" : "out")
			+ " Saxon library) ...");
		XDTester.setFulltestMode(true);
		test.common.TestAll.runTests(args);
		test.xdef.TestAll.runTests(args);
		test.xdutils.TestAll.runTests(args);
	}
}