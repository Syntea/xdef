package test.common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.xdef.sys.STester;
import test.common.bnf.TestBNF;
import test.common.bnf.TestBNFJSON;
import test.common.bnf.TestExpr;
import test.common.bnf.TestJsonXon;
import test.common.bnf.TestSQL;
import test.common.bnf.TestXML;
import test.common.bnf.TestXdScript;
import test.common.xon.TestXonUtil;
import test.common.sys.TestErrorReporting;
import test.common.sys.TestObjectWriter;
import test.common.sys.TestReport;
import test.common.sys.TestSParser;
import test.common.sys.TestSUtils;
import test.common.xml.TestKDOMBuilder;
import test.common.xml.TestKXmlUtils;
import test.common.xml.TestXmOutStream;
import test.common.xml.TestXml;
import test.XDTester;
import test.common.bnf.TestEmailAddr;
import test.common.xon.TestIni;


/** Run all available basic tests for package org.xdef.sys.
 * @author Vaclav Trojan
 */
public class TestAll {

	public static STester[] getTests() {
		return new STester[] {
			// sys
			new TestReport(),
			new TestErrorReporting(),
			new TestSParser(),
			new TestSUtils(),
			new TestObjectWriter(),
			// XON/INI
			new TestXonUtil(),
			new TestIni(),
			// XML
			new TestXml(),
			new TestKDOMBuilder(),
			new TestKXmlUtils(),
			new TestXmOutStream(),
			// BNF
			new TestBNF(),
			new TestBNFJSON(),
			new TestEmailAddr(),
			new TestExpr(),
			new TestJsonXon(),
			new TestSQL(),
			new TestXML(),
			new TestXdScript(),
		};
	}

	/** Run all available tests in this package.
	 * @param args The array of arguments.
	 * @return  error code (number of errors).
	 */
	public static int runTests(String... args) {
		PrintStream log;
		try {
			log = new PrintStream(new FileOutputStream("testCommon.log"));
		} catch (FileNotFoundException ex) {
			log = null;
		}
		STester[] tests = getTests();
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
		if (runTests(args) > 0) {System.exit(1);}
	}
}