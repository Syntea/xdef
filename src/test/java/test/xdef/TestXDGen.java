package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.w3c.dom.Element;
import org.xdef.sys.ReportWriter;
import org.xdef.util.GenXDefinition;

/** Generation of XDefinition from XML
 * @author Vaclav Trojan
 */
public class TestXDGen extends XDTester {

	public TestXDGen() {super();}

	private ReportWriter genXDef(String xml) throws Exception{
		return genXDef(xml, false);
	}

	private ReportWriter genXDef(String xml, boolean display)
		throws Exception{
		String s = KXmlUtils.nodeToString(GenXDefinition.genXdef(xml), true);
		if (display) {
			System.out.println("== xml ==\n"
				+ KXmlUtils.nodeToString(KXmlUtils.parseXml(xml), true).trim()
				+ "\n== xdef ==\n"+ s.trim());
		}
		ArrayReporter reporter = new ArrayReporter();
		Element el = null;
		try {
			el = parse(compile(s), "", xml, reporter);
		} catch (RuntimeException ex) {
			System.err.println("===\n"+ s + "\n===");
			throw ex;
		}
		if (reporter.errors()) {
			return reporter;
		}
		return KXmlUtils.compareXML(xml, KXmlUtils.nodeToString(el), true);
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		final String dataDir = getDataDir() + "test/";
		try {
			assertNoErrors(genXDef("<a></a>"));
			assertNoErrors(genXDef("<a><b>2015-01-01T23:00</b></a>"));
			assertNoErrors(genXDef("<a><b>1</b><c/><b a='1'/><b b='x'/></a>"));
			assertNoErrors(genXDef("<a><b>1<c>text</c></b><b>2<c/></b></a>"));
			assertNoErrors(genXDef("<z:a xmlns:z='www.a.b'><b>0</b></z:a>"));
			assertNoErrors(genXDef("<a xmlns='www.a.b'><b>1</b></a>"));
			assertNoErrors(genXDef("<a><b>1<c>1</c></b></a>"));
			assertNoErrors(genXDef("<a><b/><b><c/></b></a>"));
			assertNoErrors(genXDef("<a><b><c/></b><b/></a>"));
			assertNoErrors(genXDef("<a><b>1<c/></b><b/></a>"));
			assertNoErrors(genXDef("<a><b>1<c>1</c></b><b/></a>"));
			assertNoErrors(genXDef("<a><b>1<c>1</c></b><b>xx</b></a>"));
			assertNoErrors(genXDef("<a>\n <b/>\n <b>1</b><b>1<c/></b>\n</a>"));
			assertNoErrors(genXDef("<a><b>1<c>1</c></b><b a='a'/><b/></a>"));
			assertNoErrors(genXDef("<a><b c='a'/><b/><b c='b'/></a>"));
			assertNoErrors(genXDef(
				"<a r='true'>1<b>1</b><b a='1' b='a'/><b b='c'/></a>"));
			assertNoErrors(genXDef(
"<a>\n"+
"  <b>1<c>1</c></b>\n"+
"  <b a='123'>2<c>2</c></b>\n"+
"  <b b='xyz'>3<c d=''></c></b>\n"+
"</a>"));
			assertNoErrors(genXDef(
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
			assertNoErrors(genXDef(
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
			assertNoErrors(genXDef(
"<a>\n"+
"  <b>\n"+
"    <c><d/><f/><e/></c>\n"+
"    <c><e/></c>\n"+
"  </b>\n"+
"  <b><c/></b>\n"+
"</a>"));
			assertNoErrors(genXDef(
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
			assertNoErrors(genXDef(dataDir + "Test000_02.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_02_1.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_03.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_04.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_05.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_06.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_06_out.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_07_1.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_07_2.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_rus.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_rus_1.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_rus_2.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_rus_3.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_rus_4.xml"));
			assertNoErrors(genXDef(dataDir + "Test000_rus_5.xml"));
			assertNoErrors(genXDef(dataDir + "Test002_3.xml"));
			assertNoErrors(genXDef(dataDir + "Test002_5.xml"));
			assertNoErrors(genXDef(dataDir + "Test002_6.xml"));
//			assertNoErrors(genXDef(dataDir + "TestErrors3.xml"));
//			assertNoErrors(genXDef(dataDir + ""));
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