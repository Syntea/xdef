package bugreports;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonTools.JNull;
import org.xdef.xon.XonUtil;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test CSV data
 * @author Vaclav Trojan
 */
public class CsvTest extends XDTester {
	public CsvTest() {super();}

	/** Convert index of column to column name.
	 * @param index index of column.
	 * @return column name.
	 */
	private static String genColumnName(final int index) {
		String result = "";
		int i = index;
		for (;;) {
			result = (char) ('A' + (i % ('Z' - 'A' + 1))) + result;
			if ((i = i / ('Z' - 'A' + 1)) == 0) {
				return result;
			}
			i--;
		}
	}

	/** Add element witn CSV line to XML.
	 * @param result where to add add element.
	 * @param name name of element.
	 * @param line array with values of line.
	 */
	private static void createLineElem(Element result, String name, List line) {
		Element e = result.getOwnerDocument().createElement(name);
		for (int j = 0; j < line.size(); j++) {
			Object val = line.get(j);
			String key = genColumnName(j);
			if (val != null && !(val instanceof JNull)) {
				e.setAttribute(key, 
					XonTools.jstringToXML(val.toString(), 1));
			} else {
				e.setAttribute(key, "");
			}
		}
		result.appendChild(e);
	}
	
	/** Create XML element from CSV data.
	 * @param csv object with CSV data.
	 * @param hdr names of header line.
	 * @return XML element created from CSV data
	 */
	public final static Element csvToXml(final List csv, String[] hdr) {
		Element result =
			KXmlUtils.newDocument(null, "CSV", null).getDocumentElement();
		if (csv.isEmpty()) {
			return result;
		}
		int i = 0;
		if (hdr != null && csv.size() > 0) {
			i = 1;
			List x = (List) csv.get(0);
			for (int j = 0; j < hdr.length; j++) {
				String s = hdr[j];
				if (s != null) {
					if (!s.equals(x.get(j))) {
						i = 0;
						break;
					}
				} else {
					if (x.get(j) != null) {
						i = 0;
						break;
					}
				} 
			}
			if (i == 1) {
				createLineElem(result, "hdr", x);
			}
		}
		for (; i < csv.size(); i++) {
			createLineElem(result, "row", (List) csv.get(i));
		}
		return result;
	}

	/** Get index of column from column name.
	 * @param name column name.
	 * @return  index of column.
	 */
	private static int getColumnIndex(final String name) {
		int result = 0;
		for (int i = 0; i < name.length(); i++) {
			result = result*24 + name.charAt(i) - 'A';
		}
		return result;
	}
	
	/** Create array of column items from XML element.
	 * @param e XML element.
	 * @return array of column items.
	 */
	private static List<Object> createLineArray(final Element e) {
		List<Object> result = new ArrayList<Object>();
		NamedNodeMap nnm = e.getAttributes();
		if (nnm == null || nnm.getLength() == 0) {
			return result; // empty line
		}
		int lineLen = 0;
		for (int i = 0; i < nnm.getLength(); i++) {
			int x = getColumnIndex(nnm.item(i).getNodeName());
			if (x > lineLen) {
				lineLen = x;
			}
		}
		for (int i=0; i <= lineLen; i++) {
			String s = e.getAttribute(genColumnName(i));
			result.add(s == null || s.isEmpty()
				? null : XonTools.xmlToJValue(s));
		}
		return result;
	}
	
	/** Create CSV data from XML element.
	 * @param e XML element.
	 * @return CSV data
	 */
	public final static List<Object> xmlToCsv(final Element e) {
		List<Object> result = new ArrayList<Object>();
		Node node = e.getFirstChild();
		while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
			node = node.getNextSibling();
		}
		if (node != null && "hdr".equals(node.getNodeName())) {
			result.add(createLineArray((Element) node));
			node = node.getNextSibling();
			while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
				node = node.getNextSibling();
			}
		}
		while (node != null) {
			result.add(createLineArray((Element) node));
			node = node.getNextSibling();
			while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
				node = node.getNextSibling();
			}
		}
		return result;
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
			o = XonUtil.parseCSV(s);
			el = csvToXml((List) o,
				new String[]{"Name", "Email", "Mobile Number"});
			assertEq(el, el = parse(xp, "", el, reporter));
			assertNoErrors(reporter);
			x = xmlToCsv(el);
			assertTrue(XonUtil.xonEqual(x, o));
//System.out.println(o);
//System.out.println(x);
			assertEq(el, el = csvToXml((List) o,
				new String[]{"Name", "Email", "Mobile Number"}));
			s =
"hdr: $script='?; options acceptEmptyAttributes'\n"+
"  \"fixed 'Name'\", \"fixed 'Email'\", \"fixed 'Mobile Number'\"\n"+
"row: xd:script='*; options acceptEmptyAttributes'\n"+
"  \"? string()\", \"? emailAddr()\", \"? telephone()\"\n";

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='csv'>\n"+
"<csv name = 'CSV'>\n"+
"hdr: $script='?; options acceptEmptyAttributes'\n"+
"  \"fixed 'Name'\", \"fixed 'Email'\", \"fixed 'Mobile Number'\"\n"+
"row: xd:script='*; options acceptEmptyAttributes'\n"+
"  \"? string()\", \"? emailAddr()\", \"? telephone()\"\n"+
"</csv>\n"+
"</xd:def>";
			xp = compile(xdef);
if(true)return;
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
			x = XonUtil.parseXON(s);
			assertTrue(XonUtil.xonEqual(x,
				XonUtil.xmlToXon(XonUtil.xonToXml(x))));
			o = xd.jparse(s, reporter);
			assertTrue(XonUtil.xonEqual(o, x));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class bugreports.data.Csv4 %link X</xd:component>\n"+
"<xd:xon name = 'X'>\n"+
"[\n"+
" [\"fixed 'Name'\",\"fixed 'Email'\",\"fixed 'Mobile Number'\"],\n"+
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
			x = XonUtil.parseXON(s);
			if (!XonUtil.xonEqual(x, o = xd.jparse(s, reporter))) {
				fail(XonUtil.toXonString(x, true)
					+ "\n" + XonUtil.toXonString(o, true));
			}
			assertNoErrors(reporter);
			xc = xd.jparseXComponent(s, null, reporter);
			if (!XonUtil.xonEqual(x, o = XComponentUtil.toXon(xc))) {
				fail(XonUtil.toXonString(o, true));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='csv'>\n"+
"<xd:component>%class bugreports.data.Csv5 %link csv</xd:component>\n"+
"<csv>\n"+
" <row xd:script='+'>jlist();</row>\n"+
"</csv>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument();
			el = XonUtil.csvToXml((List) x);
			assertEq(el, el = parse(xp, "", el, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		if (T) {
			clearTempDir(); // delete temporary files.
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}