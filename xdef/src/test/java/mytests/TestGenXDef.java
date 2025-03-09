package mytests;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.STester;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Test genXDef
 * @author Trojan
 */
public class TestGenXDef  extends XDTester {

	public TestGenXDef() {super();}

	String test(String data) {
		String result = "";
		try {
			Object a = XonUtils.parseXON(data);
			String xdName = "Example";
			Element el = org.xdef.impl.GenXDefXON.genXdef(a, xdName);
System.out.println(KXmlUtils.nodeToString(el));
			XDPool xp = XDFactory.compileXD(
				null, KXmlUtils.nodeToString(el, true));
			XDDocument xd = xp.createXDDocument(xdName);
			ArrayReporter reporter = new ArrayReporter();
			Object b = xd.jparse(data, reporter);
			if (reporter.errors()) {
				if (!result.isEmpty()) {
					result += "\n";
				}
				result += reporter.printToString();
			}
			if (!XonUtils.xonEqual(a, b)) {
				if (!result.isEmpty()) {
					result += "\n";
				}
				result += XonUtils.xonDiff(a, b);
			}
		} catch (RuntimeException ex) {
			if (!result.isEmpty()) {
				result += "\n";
			}
			result += STester.printThrowable(ex);
		}
		return result;
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		try {
			assertEq("", test("{a:[[[[1,2],[1,2]]],[[3,4],[3,4]]]}"));
//			assertEq("", test("[ ]"));
//			assertEq("", test("[ 1 ]"));
//			assertEq("", test("[ [] ]"));
//			assertEq("", test("[ [-1] ]"));
//
//			assertEq("", test("[ [-1], [2] ]"));
//			assertEq("", test("[ [-1], [2], [] ]"));
//
//			assertEq("", test("{ }"));
//			assertEq("", test("{ a:1 }"));
//			assertEq("", test("{ a:1, b:2 }"));
//			assertEq("", test("{ a: {} }"));
//			assertEq("", test("{ a: {b:1} }"));
//			assertEq("", test("{ a: [] }"));
//			assertEq("", test("{ a: [1] }"));
//
//			assertEq("", test("[ {a:1} ]"));
//			assertEq("", test("[ {a:1}, {a:2} ]"));
//			assertEq("", test("[ {a:1}, {a:2, b:3} ]"));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}