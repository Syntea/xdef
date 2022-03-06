package bugreports;

import java.io.StringReader;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.CsvReader;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test CSV data
 * @author Vaclav Trojan
 */
public class CsvTest extends XDTester {
	public CsvTest() {super();}

	/** Display CSV object. */
	private static String printCSV(final Object o) {
		List x = (List) o;
		String s = "";
		for (int i = 0; i < x.size(); i++) {
			s += "Row[" + i + "]\n";
			for (Object y: (List) x.get(i)) {
				s += (y != null ? y+"; "+y.getClass() : "*null*") + "\n";
			}
		}
		return s;
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		boolean T = false; // if false, all tests are invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
//		System.setProperty(XConstants.DEBUG_SWITCHES,
//			XConstants.DEBUG_SHOW_XON_MODEL);
////////////////////////////////////////////////////////////////////////////////
		String s;
		String xdef, xml;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		Object x, o;
		List list;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
////////////////////////////////////////////////////////////////////////////////
		reporter.clear();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class bugreports.data.Csvxx %link CSV</xd:component>\n"+
"<xd:declaration>\n"+
" type item union(%item=[emailAddr(), telephone(), string()]);\n"+
" type nstring union(%item=[string(),jnull()]);\n"+
" type nemail union(%item=[emailAddr(),jnull()]);\n"+
" type ntelephone union(%item=[telephone(),jnull()]);\n"+
"</xd:declaration>\n"+
"<xd:json name=\"CSV\">\n"+
"[\n"+
"  [\"3..3 string();\"],\n"+
//"  [$script=\"+\", \"0..3 item();\"]\n"+
"  [$script=\"+\", \"? nstring()\", \"? nemail\", \"? ntelephone()\"]\n"+
"]\n"+
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			s =
"Name, Email, Mobile Number\n"+
"abc, a@b.c, +420 601 349 889\n"+
"\n"+
"xyz, d@e.f,\n"+
"xyz,,\n"+
",,\n"+
"xyz, , 123 456 789\n";
			x = xd.cparse(new StringReader(s), null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			s =
"[\n"+
" [\"Name\",\"Email\",\"Mobile Number\"],\n"+
" [\"abc\", e\"a@b.c\", \"+420 601 349 889\"],\n"+
" [],\n"+
" [\"xyz\", e\"d@e.f\",null],\n"+
" [\"xyz\", null, null],\n"+
" [null, null, null],\n"+
" [\"xyz\", null, \"123 456 789\"]\n"+
"]";
			o = xd.jparse(s, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + printCSV(x) + "\n*** B *\n" + printCSV(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n"
					+ "*** A *\n" + printCSV(x) + "\n*** B *\n" + printCSV(o));
			}
//if(true)return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<CSV>\n"+
"<hdr xd:script='?; options acceptEmptyAttributes'\n"+
"   A=\"fixed 'Name'\" B=\"fixed 'Email'\" C=\"fixed 'Mobile Number'\"/>\n"+
"<row xd:script='*; options acceptEmptyAttributes'\n"+
"   A=\"? string()\" B=\"? emailAddr()\" C=\"? telephone()\"/>\n"+
"</CSV>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<CSV>\n"+
"<hdr A=\"Name\" B=\"Email\" C=\"Mobile Number\"/>\n"+
"<row/>\n"+
"<row A=\"abc\" B=\"a@b.c\" C=\"+420 601 349 889\"/>\n"+
"<row A=\"null\"/>\n"+
"<row A=\"def\"/>\n"+
"</CSV>";
			s =
"Name, Email, Mobile Number\n"+
"\n"+
"abc, a@b.c, +420 601 349 889\n"+
"xxx,\n"+
"\n"+
"def,,\n";
			list = XonUtils.parseCSV(new StringReader(s), "STRING");
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class bugreports.data.Csv1 %link CSV</xd:component>\n"+
"<CSV\n"+
"     A='jlist(%item=string());'\n" +
"     B='jlist(%item=emailAddr());'\n" +
"     C='jlist(%item=string());'\n" +
"  >\n" +
"</CSV>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument();
			xml =
"<CSV\n" +
"  A='[\"Helena \\\"\\\"Klímová\\\"\\\"\", \"Eva Kuželová, Epor \\\"Prix\\\"\"]'\n" +
"  B='[\"hklimova@volny.cz\", \"epor@email.cz\"]'\n" +
"  C='[\"+420 602 345 678\", null ]'\n" +
" />\n" +
"";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xc = parseXC(xd, xml, null, reporter);
			xdef=
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class bugreports.data.Csv3 %link X</xd:component>\n"+
"<xd:declaration>Telephone t = new Telephone('+420 601349889');</xd:declaration>\n"+
"<xd:xon name = 'X'>\n"+
"{\n" +
"  C=[\"+ telephone()\"]\n"+
"}\n"+
"</xd:xon>\n"+
"</xd:def>";
			xd = XDFactory.compileXD(null,xdef).createXDDocument();
			compile(xdef);
			genXComponent(xd.getXDPool(), clearTempDir()).checkAndThrowErrors();
			s =
"{\n" +
"    C=[T\"+420 602 345 678\", T\"+420 602345679\", T\"123456789\"]\n"+
"}";
			x = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x,
				XonUtils.xmlToXon(XonUtils.xonToXml(x))));
			o = xd.jparse(s, reporter);
			assertTrue(XonUtils.xonEqual(o, x));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class bugreports.data.Csv4 %link X</xd:component>\n"+
"<xd:xon name = 'X'>\n"+
"[\n"+
" [$script=\"occurs 1\",\"fixed 'Name'\",\"fixed 'Email'\",\"fixed 'Mobile Number'\"],\n"+
" [$script=\"occurs 1..*\", \"string()\", \"emailAddr()\", \"? telephone()\"]\n"+
"]\n"+
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument();
			s =
"[\n"+
" [\"Name\",\"Email\",\"Mobile Number\"],\n"+
" [\"abc\", e\"a@b.c\", T\"+420 601 349 889\"]\n"+
"]";
			x = XonUtils.parseXON(s);
			if (!XonUtils.xonEqual(x, o = xd.jparse(s, reporter))) {
				fail(XonUtils.toXonString(x, true)
					+ "\n" + XonUtils.toXonString(o, true));
			}
			assertNoErrors(reporter);
			xc = xd.jparseXComponent(s, null, reporter);
			if (!XonUtils.xonEqual(x, o = XComponentUtil.toXon(xc))) {
				fail(XonUtils.toXonString(o, true));
			}
		} catch (Exception ex) {fail(ex);}
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}