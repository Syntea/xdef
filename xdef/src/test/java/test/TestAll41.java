package test;

import org.xdef.XDFactory;
import org.xdef.impl.code.DefXQueryExpr;

/** Execute all tests fast (only version 4.0).
 * @author Vaclav Trojan
 */
public class TestAll41 {

	/** @param args the command line arguments. */
	public static void main(String... args) {
		System.out.println("Build: " + XDFactory.getXDVersion());
		System.out.println("Testing java version: "
			+ System.getProperty("java.version") + " ("
			+ (DefXQueryExpr.isXQueryImplementation() ? "with" : "without")
			+ " Saxon library) ...");
		XDTester.setFulltestMode(false);
		test.common.TestAll.runTests(args);
		test.xdef.TestAll41.runTests(args);
		test.xdutils.TestAll41.runTests(args);
	}
}