package test.xdef;

import test.XDTester;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.xdef.XDConstants;
import org.xdef.sys.STester;

/** Run all available basic tests for package org.xdef.
 * @author Vaclav Trojan
 */
public class TestAll {

	static final XDTester[] getTests() {
		 return new XDTester[] {
			new Test000(),
			new Test001(),
			new Test002(),
			new Test003(),
			new TestBNF(),
			new TestCompose(),
			new TestConstruct(),
			new TestDatabase(),
			new TestDebug(),
			new TestDOMParse(),
			new TestDTDTypes(),
			new TestEmailAddr(),
			new TestErrors(),
			new TestExtenalMethods(),
			new TestExternalVariables(),
			new TestGroups(),
			new TestImplementsAndUses(),
			new TestInclude(),
			new TestJsonXdef(),
			new TestKeyAndRef(),
			new TestLexicon(),
			new TestNamespaces(),
			new TestOptions(),
			new TestSaxon(),
			new TestScript(),
			new TestTemplate(),
			new TestTryCatch(),
			new TestTypes(),
			new TestUserMethods(),
			new TestUserQuery(),
			new TestXComponents(),
			new TestXDService(),
			new TestXSTypes(),
			new TestXdef(),
			new TestXdefOfXdef(),
			new TestXmlWriter(),
			new TestXon(),
		};
	}

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static final int runTests(final String... args) {
		PrintStream log;
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream("testXdef.log");
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
		String xdNS = XDTester._xdNS;
		XDTester._xdNS = XDConstants.XDEF42_NS_URI;
		System.out.println("[INFO] Testing X-definition version " + XDConstants.BUILD_VERSION);
		int result = STester.runTests(System.out, System.err, log,
			tests, "package xdef", XDTester.getFulltestMode(), args);
		if (log != null) {
			log.close();
		}
		XDTester._xdNS = xdNS;
		return result;
	}

	/** Run all basic tests
	 * @param args the command line arguments
	 */
	public static final void main(final String... args) {
		XDTester.setFulltestMode(false);
		if (runTests(args) > 0) {System.exit(1);}
	}
}