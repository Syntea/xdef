package bugreports;

import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

public class Test1 extends XDTester {

	public Test1() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		String xdef;
		XDPool xp;
		XDDocument xd;
		Element el;
		String s;
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"A|B\" >\n" +
" <A>\n" +
"   <a xd:script=\"*\" a=\"xdatetime('yyyy-d-M')\"/>\n" +
" </A>\n" +
" <B>\n" +
"   <a xd:script=\"*\" a=\"xdatetime('yyyy-d-M'\n"+
"      , %minInclusive='1620-2-1', %maxInclusive='2022-12-6'\n" +
"      )\"/>\n" +
" </B>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			xml =
"<A>\n" +
"   <a a='1620-1-1'/>\n"+
"   <a a='1620-1-2'/>\n"+
"   <a a='2101-12-6'/>\n"+
"   <a a='2022-12-7'/>\n"+
" </A>";
			xd = xp.createXDDocument();
			xd.setMinYear(1800);
			xd.setMaxYear(2100);
			el = parse(xd, xml, reporter);
			assertEq(3, reporter.getErrorCount(),
				reporter.printToString());
			assertEq(el, xml);
			xd = xp.createXDDocument();
			xd.setMinYear(1800);
			xd.setMaxYear(2100);
			xml =
"<B>\n" +
"   <a a='1620-1-1'/>\n"+
"   <a a='1620-1-2'/>\n"+
"   <a a='2101-12-6'/>\n"+
"   <a a='2022-12-7'/>\n"+
" </B>";
			el = parse(xd, xml, reporter);
			assertEq(3, reporter.getErrorCount(),
				reporter.printToString());
			assertEq(el, xml);
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