package test.xdef;

import test.XDTester;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.xdef.XDConstants;

/** Execute all tests fast (only version 4.0).
 * @author Vaclav Trojan
 */
public class TestAll41 {

	private TestAll41() {}

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static int runTests(String[] args) {
		PrintStream log;
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream("testXdef.log");
			log = new PrintStream(fis);
		} catch (Exception ex) {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException x) {}
			}
			log = null;
		}
		XDTester[] tests = new XDTester[] {
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
			new TestXDChecker(),
			new TestXDGen(),
			new TestXDService(),
			new TestXSTypes(),
			new TestXdef(),
			new TestXdefOfXdef(),
			new TestXmlWriter(),
			new TestXon(),
		};
		String xdNS = XDTester._xdNS;
		XDTester._xdNS = XDConstants.XDEF41_NS_URI;
		System.out.println("Testing X-definition version 4.1");
		int result = XDTester.runTests(System.out, System.err, log,
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
	public static void main(String... args) {
		XDTester.setFulltestMode(false);
		if (runTests(args) > 0) {System.exit(1);}
	}
}