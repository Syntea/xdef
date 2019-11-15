package test;

import buildtools.XDTester;

/** Execute all tests verbose.
 * @author Vaclav Trojan
 */
public class FullTestAll {
	/** @param args the command line arguments. */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		test.common.TestAll.runTests(args);
		test.xdef.TestAll.runTests(args);
		test.xdutils.TestAll.runTests(args);
	}
}
