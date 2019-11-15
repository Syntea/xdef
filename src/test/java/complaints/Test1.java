package complaints;

import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import buildtools.XDTester;

public class Test1 extends XDTester {

	public Test1() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		String xdef;
		XDPool xp;
		Element el;
		String s;
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <xd:declaration> Element $e = null; </xd:declaration>\n" +
"  <a b=\"optional string; create from($e,'@b')\"/>\n" +
"</xd:def>";
//			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
//			assertNoErrors(reporter.errorWarnings(), reporter.printToString());
			assertEq("<a/>", create(compile(xdef), "", reporter, "<a/>"));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}

}
