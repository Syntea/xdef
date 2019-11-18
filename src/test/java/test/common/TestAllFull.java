package test.common;

import java.io.FileOutputStream;
import java.io.PrintStream;
import test.common.bnf.TestBNF;
import test.common.bnf.TestExpr;
import test.common.bnf.TestBNFJSON;
import test.common.bnf.TestXML;
import test.common.bnf.TestXdScript;
import test.common.json.TestJsonUtil;
import test.common.sys.TestErrorReporting;
import test.common.sys.TestObjectWriter;
import test.common.sys.TestReport;
import test.common.sys.TestSParser;
import test.common.sys.TestSUtils;
import test.common.xml.TestKDOMBuilder;
import test.common.xml.TestKXmlUtils;
import test.common.xml.TestXmOutStream;
import test.common.xml.TestXml;
import buildtools.XDTester;
import buildtools.STester;

/** Run all available tests for package org.xdef.sys with all features
 * of the tester.
 * @author Vaclav Trojan
 */
public class TestAllFull {

	TestAllFull() {}

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
		STester[] tests = new STester[] {
			// sys
			new TestReport(),
			new TestErrorReporting(),
			new TestSParser(),
			new TestSUtils(),
			new TestObjectWriter(),
			// JSON
			new TestJsonUtil(),
			// xml
			new TestXml(),
			new TestKDOMBuilder(),
			new TestKXmlUtils(),
			new TestXmOutStream(),
			// BNF
			new TestBNF(),
			new TestExpr(),
			new TestBNFJSON(),
			new TestXML(),
			new TestXdScript(),
		};
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
