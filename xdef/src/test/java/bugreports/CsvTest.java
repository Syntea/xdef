package bugreports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXData;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonUtil;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test CSV data
 * @author Vaclav Trojan
 */
public class CsvTest extends XDTester {
	public CsvTest() {super();}

	private class BoolParser extends XDParserAbstract {
		BoolParser() {}
		@Override
		public void parseObject(XXNode xnode, XDParseResult p) {
			p.setEos();
			if (tab0((XXData) xnode, "a", "b")) {
				return;
			}
			//Inorrect value&{0}{ of '}{'}&{1}{: '}{'}&{#SYS000}
			p.error(XDEF.XDEF809, parserName());
		}
		@Override
		public String parserName() {return "tab";}
	}

	public static boolean tab0(XXNode xnode, String a, String b) {return true;}

	private static Element csvToXml(final List o) {
		Document doc = KXmlUtils.newDocument(null, "CSV", null);
		Element result = doc.getDocumentElement();
		for (Object x: o) {
			Map m = (Map) x;
			Element row = doc.createElement("row");
			for (Object y : m.entrySet()) {
				Map.Entry en = (Map.Entry) y;
				String key = (String) en.getKey();
				Object val = en.getValue();
				row.setAttribute(key, XonTools.jstringToXML(val.toString(), 1));
			}
			result.appendChild(row);
		}
		return result;
	}

	private static List<Object> xmlToCsv(final Element elem) {
		List<Object> result = new ArrayList<Object>();
		Node node = elem.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				NamedNodeMap nnm = node.getAttributes();
				for (int i = 0; i < nnm.getLength(); i++) {
					Node n = nnm.item(i);
					map.put(n.getNodeName(),
						XonTools.xmlToJValue(n.getNodeValue()));
				}
				result.add(map);
			}
			node = node.getNextSibling();
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
		Element el;
		ArrayReporter reporter = new ArrayReporter();
////////////////////////////////////////////////////////////////////////////////
/**/
		try {
			xdef =
//"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A|B|C|D'>\n"+
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A|B|C'>\n"+
"<xd:declaration>\n"+
" external method boolean bugreports.CsvTest.tab0(XXNode xnode, String a, String b);\n" +
" boolean tab1(String a, String b){return true;}\n" +
" Parser parser;\n" +
"</xd:declaration>\n"+
"<A a=\"string(%base=parser);\"/>\n" +
"<B a=\"string(%base=tab0('a', 'b'));\"/>\n" +
"<C a=\"string(%base=tab1('a', 'b'));\"/>\n" +
//"<D a=\"string(%base=true);\"/>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			xp.displayCode();
			xd = xp.createXDDocument();
			xd.setVariable("parser", new BoolParser());
			xml = "<A a='a'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xml = "<B a='a'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xml = "<C a='a'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xml = "<D a='a'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class bugreports.CsvTest0 %link CSV</xd:component>\n"+
"<xd:xon name=\"CSV\">\n"+
"[\n" +
"[$script=\"+\",\"string()\",\"union(%item=[emailAddr(), jnull])\",\"union(%item=[string(), jnull])\"]\n"+
"]\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = compile(xdef);
			xp = XDFactory.compileXD(null, xdef);
/**/
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
//if(true)return;
/**/
			xd = xp.createXDDocument();
			s =
"[\n" +
" [\"Helena \\\"\\\"Klímová\\\"\\\"\",\"hklimova@volny.cz\",\"+420 602 345 678\"],\n" +
" [\"Eva Kuželová, Epor \\\"Prix\\\"\", \"epor@email.cz\", null],\n" +
" [\"Jirová\", null, null]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorsAndClear(reporter);
			genXComponent(xd.getXDPool(), clearTempDir()).checkAndThrowErrors();
			xc = xd.jparseXComponent(s, null, reporter);
			x = XComponentUtil.toXon(xc);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtil.xonEqual(o, x));
			List list = (List) ((List) x).get(1);
			assertNull(list.get(2));
			list = (List) ((List) x).get(2);
			assertNull(list.get(1));
			assertNull(list.get(2));
//if(true)return;
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
//			assertEq(xml, xc.toXml());
//			o = XComponentUtil.toXon(xc);
//System.out.println(XonUtil.toXonString(o, true));
//System.out.println(XonUtil.toXonString(o, true));
//if(true)return;
			xdef=
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class bugreports.data.Csv3 %link X</xd:component>\n"+
"<xd:declaration>Telephone t = new Telephone('+420 601349889');</xd:declaration>\n"+
"<xd:xon name = 'X'>\n"+
"{\n" +
//"  A=[\n" +
//"     $script=\"finally outln(t);\",\n" +
//"     \"+ jstring()\"],\n" +
//"  B=[\"+ emailAddr()\"],\n" +
"  C=[\"+ telephone()\"]\n"+
"}\n"+
"</xd:xon>\n"+
"</xd:def>";
			xd = XDFactory.compileXD(null,xdef).createXDDocument();
			compile(xdef);
			genXComponent(xd.getXDPool(), clearTempDir()).checkAndThrowErrors();
			s =
"{\n" +
//"    A=[\"Hele \\\"\\\"Klímová\\\"\\\"\", \"Eva Kuželová, Epor \\\"Prix\\\"\", \"Eliška Jírová\"],\n"+
//"    B=[e\"klimova<hklimova@volny.cz>\", e\"epor@email.cz\", e\"a@b.cz\"],\n"+
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
//" [\"fixed 'Name'\",\"fixed 'Email'\",\"fixed 'Mobile Number'\"],\n"+
" [$script=\"occurs 1..*\", \"string()\", \"emailAddr()\", \"? telephone()\"]\n"+
"]\n"+
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument();
			s =
"[\n"+
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
//if(true)return;
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