package test.xdutils;

import java.io.FileOutputStream;
import java.io.PrintStream;
import org.xdef.XDConstants;
import test.XDTester;

/** Run all available basic tests for package org.xdef.util.
 * @author Vaclav Trojan
 */
public class TestAll {

	private TestAll() {  }

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static int runTests(String[] args) {
		PrintStream log;
		try {
			log = new PrintStream(new FileOutputStream("testUtils.log"));
		} catch (Exception ex) {
			log = null;
		}
		XDTester[] tests = new XDTester[]{
			new TestDTDToXdef(),
			new TestGenCollection(),
			new TestGenDTD(),
			new TestGenXdef(),
			new TestParseType(),
			new TestPrettyXdef(),
			new TestValidate(),
			new TestXDefUtils(),
			new TestXd2XsdConv(),
			new TestXsd2XdConv(),
		};
		String xdNS = XDTester._xdNS;
		XDTester._xdNS = XDConstants.XDEF40_NS_URI;
		System.out.println("Testing X-definition utilities version 4.0");
		int result = XDTester.runTests(System.out, System.err, log,
			tests, "package xdutils", XDTester.getFulltestMode(), args);
		XDTester._xdNS = XDConstants.XDEF41_NS_URI;
		System.out.println("Testing X-definition utilities version 4.1");
		result += XDTester.runTests(System.out, System.err, log,
			tests, "package xdutils", XDTester.getFulltestMode(), args);
		if (log!= null) {
			log.close();
		}
		XDTester._xdNS = xdNS;
		return result;
	}

	/** Run all available tests in this package
	 * @param args list of of arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(false);
		System.exit(runTests(args));
	}
}