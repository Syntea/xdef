package test;

import org.xdef.XDFactory;
import org.xdef.impl.code.DefXQueryExpr;

/** Execute all tests verbose.
 * @author Vaclav Trojan
 */
public class FullTestAll {
	/** @param args the command line arguments. */
	public static void main(String... args) {
		System.out.println("[INFO] Java version: "
			+ System.getProperty("java.version") + " ("
			+ (DefXQueryExpr.isXQueryImplementation() ? "with" : "without")
			+ " Saxon library) ...");
		System.out.println(
			"[INFO] X-definition version: " + XDFactory.getXDVersion());
		XDTester.setFulltestMode(true);
		test.common.TestAll.runTests(args);
		test.xdef.TestAllFull.runTests(args);
		test.xdutils.TestAll.runTests(args);
	}
}