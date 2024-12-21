package test.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.xdef.sys.STester;
import test.common.bnf.TestBNF;
import test.common.bnf.TestBNFJSON;
import test.common.bnf.TestExpr;
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

	static final STester[] getTests() {
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
			new TestSQL(),
			new TestXML(),
			new TestXdScript(),
		};
	}

	/** Run all available tests in this package.
	 * @param args The array of arguments.
	 * @return  error code (number of errors).
	 */
	public static final int runTests(final String... args) {
		PrintStream log;
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream("testCommon.log");
			log = new PrintStream(fis);
		} catch (IOException ex) {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException x) {}
			}
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
	public static final void main(final String... args) {
		if (runTests(args) > 0) {System.exit(1);}
	}
}
