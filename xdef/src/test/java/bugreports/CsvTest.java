package bugreports;

import java.io.StringReader;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.CsvReader;
import org.xdef.xon.XonUtils;
import test.XDTester;

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
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
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
"Name | Email | Mobile Number\n"+
"abc | a@b.c | +420 601 349 889\n"+
"\n"+
"xyz | d@e.f |\n"+
"xyz | |\n"+
" | |\n"+
"xyz | | 123 456 789\n";
			o = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + printCSV(x) + "\n*** B *\n" + printCSV(o));
			}
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