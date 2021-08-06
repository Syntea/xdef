package test.common;

import java.io.FileOutputStream;
import java.io.PrintStream;
import org.xdef.sys.STester;
import test.XDTester;

/** Run all available tests for package org.xdef.sys with all features
 * of the tester.
 * @author Vaclav Trojan
 */
public class TestAllFull {

	/** Run all available tests in this package.
	 * @param args The array of arguments.
	 * @return  error code (number of errors).
	 */
	public static int runTests(String... args) {
		XDTester.setFulltestMode(true);
		PrintStream log;
		try {
			log = new PrintStream(new FileOutputStream("testCommon.log"));
		} catch (Exception ex) {
			log = null;
		}
		STester[] tests = TestAll.getTests();
		int result = STester.runTests(System.out, System.err, log,
			tests, "package common", XDTester.getFulltestMode(), args);
		if (log != null) {
			log.close();
		}
		return result;
	}

	/** Run all available tests in this package.
	 * @param args The array of arguments (not used).
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTests(args) > 0) {System.exit(1);}
	}
}