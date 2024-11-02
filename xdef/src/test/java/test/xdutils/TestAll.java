package test.xdutils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.xdef.XDConstants;
import test.XDTester;

/** Run all available basic tests for package org.xdef.util.
 * @author Vaclav Trojan
 */
public class TestAll {

	public static XDTester[] getTests() {
		 return new XDTester[] {
			new TestDTDToXdef(),
			new TestGenCollection(),
			new TestGenDTD(),
			new TestGenXdef(),
			new TestParseType(),
			new TestPrettyXdef(),
			new TestValidate(),
			new TestXd2XsdConv(),
			new TestXsd2XdConv(),
		};
	}

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static int runTests(String... args) {
		PrintStream log;
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream("testUtils.log");
			log = new PrintStream(fis);
		} catch (IOException ex) {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException x) {}
			}
			log = null;
		}
		XDTester[] tests = getTests();
		String xdNS = XDTester._xdNS;
		XDTester._xdNS = XDConstants.XDEF42_NS_URI;
		System.out.println("[INFO] Testing X-definition utilities version 4.2");
		int result = XDTester.runTests(System.out, System.err, log,
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