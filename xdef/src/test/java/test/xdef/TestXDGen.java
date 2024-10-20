package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import static org.xdef.sys.STester.runTest;
import org.xdef.util.GenXDefinition;
import org.xdef.xon.XonUtils;

/** Generation of XDefinition from XML and JSON/XON
 * @author Vaclav Trojan
 */
public class TestXDGen extends XDTester {

	public TestXDGen() {super();}

	private static ReportWriter genXDefXml(final String x) throws Exception{
		return genXDefXml(x, false);
	}

	private static ReportWriter genXDefXml(final String x, boolean display)
		throws Exception{
		String s = KXmlUtils.nodeToString(GenXDefinition.genXdef(x), true);
		if (display) {
			System.out.println("== xml ==\n"
				+ KXmlUtils.nodeToString(KXmlUtils.parseXml(x), true).trim()
				+ "\n== xdef ==\n"+ s.trim());
		}
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp = XDFactory.compileXD(null,s);
		XDDocument xd = xp.createXDDocument();
		Element el = xd.xparse(x, reporter);
		if (reporter.errors()) {
			return reporter;
		}
		return KXmlUtils.compareElements(x, el, true);
	}

	private static ReportWriter genXDefXon(final String x) throws Exception{
		return genXDefXon(x, false);
	}

	private static ReportWriter genXDefXon(final String x, boolean display)
		throws Exception{
		String s = KXmlUtils.nodeToString(GenXDefinition.genXdef(x), true);
		if (display) {
			System.out.println("== xon ==\n"
				+ XonUtils.toXonString(XonUtils.parseXON(x), true).trim()
				+ "\n== xdef ==\n"+ s.trim());
		}
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp = XDFactory.compileXD(null,s);
		XDDocument xd = xp.createXDDocument();
		Object o = xd.jparse(x, reporter);
		if (reporter.errors()) {
			return reporter;
		}
		s = XonUtils.xonDiff(o, XonUtils.parseXON(x));
		if (!s.isEmpty()) {
			reporter.add(Report.error("", "", s));
		}
		return reporter;
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		final String dataDir = getDataDir() + "test/";
		// test xdef from XML
		try {
			assertNoErrorwarnings(genXDefXml("<a></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b>2015-01-01T23:00</b></a>"));
			assertNoErrorwarnings(
				genXDefXml("<a><b>1</b><c/><b a='1'/><b b='x'/></a>"));
			assertNoErrorwarnings(
				genXDefXml("<a><b>1<c>text</c></b><b>2<c/></b></a>"));
			assertNoErrorwarnings(
				genXDefXml("<z:a xmlns:z='www.a.b'><b>0</b></z:a>"));
			assertNoErrorwarnings(genXDefXml("<a xmlns='www.a.b'><b>1</b></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b>1<c>1</c></b></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b/><b><c/></b></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b><c/></b><b/></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b>1<c/></b><b/></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b>1<c>1</c></b><b/></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b>1<c>1</c></b><b>xx</b></a>"));
			assertNoErrorwarnings(
				genXDefXml("<a>\n <b/>\n <b>1</b><b>1<c/></b>\n</a>"));
			assertNoErrorwarnings(
				genXDefXml("<a><b>1<c>1</c></b><b a='a'/><b/></a>"));
			assertNoErrorwarnings(genXDefXml("<a><b c='a'/><b/><b c='b'/></a>"));
			assertNoErrorwarnings(genXDefXml(
				"<a r='true'>1<b>1</b><b a='1' b='a'/><b b='c'/></a>"));
			assertNoErrorwarnings(genXDefXml(
"<a>\n"+
"  <b>1<c>1</c></b>\n"+
"  <b a='123'>2<c>2</c></b>\n"+
"  <b b='xyz'>3<c d=''></c></b>\n"+
"</a>"));
			assertNoErrorwarnings(genXDefXml(
"<a>\n"+
"  <b a='a'>\n"+
"    <c>a</c>\n"+
"  </b>\n"+
"  <b a='a'>\n"+
"    <c>\n"+
"      d\n"+
"    </c>\n"+
"    <c>\n"+
"      e\n"+
"    </c>\n"+
"  </b>\n"+
"</a>"));
			assertNoErrorwarnings(genXDefXml(
"<a>\n"+
"  <b>\n"+
"    <c><d/></c>\n"+
"    <c><d/><e/></c>\n"+
"    <c><e/></c>\n"+
"  </b>\n"+
"  <b>\n"+
"    <c><d/></c>\n"+
"    <c><d/><e/></c>\n"+
"    <c/>\n"+
"  </b>\n"+
"</a>"));
			assertNoErrorwarnings(genXDefXml(
"<a>\n"+
"  <b>\n"+
"    <c><d/><f/><e/></c>\n"+
"    <c><e/></c>\n"+
"  </b>\n"+
"  <b><c/></b>\n"+
"</a>"));
			assertNoErrorwarnings(genXDefXml(
"<a>\n"+
"  <b>\n"+
"    <c><d/></c>\n"+
"    <c><d/>1<e/></c>\n"+
"    <c><e/></c>\n"+
"  </b>\n"+
"  <b>\n"+
"    <c><d/></c>\n"+
"    <c><d/><e/></c>\n"+
"    <c/>\n"+
"  </b>\n"+
"</a>"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_02.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_02_1.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_03.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_04.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_05.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_06.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_06_out.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_07_1.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_07_2.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_rus.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_rus_1.xml"));
/*#if DEBUG*#/
			// code page ISO8859-5
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_rus_2.xml"));
			//code page KOI8-R
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_rus_3.xml"));
/*#end*/
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_rus_4.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test000_rus_5.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test002_3.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test002_5.xml"));
			assertNoErrorwarnings(genXDefXml(dataDir + "Test002_6.xml"));
		} catch (Exception ex) {fail(ex);}
		// test xdef from JSON, XON
		try {
			assertNoErrorwarnings(genXDefXon("1"));
			assertNoErrorwarnings(genXDefXon("1d"));
			assertNoErrorwarnings(genXDefXon("[]"));
			assertNoErrorwarnings(genXDefXon("[1b]"));
			assertNoErrorwarnings(genXDefXon("[1s,2i,3.14,\"\",true,null]"));
			assertNoErrorwarnings(genXDefXon("{ a:[1d]}"));
		} catch (Exception ex) {fail(ex);}
		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}