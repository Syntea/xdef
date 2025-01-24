package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.XDContainer;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefXPathExpr;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDFactory;
import java.io.StringWriter;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import javax.xml.namespace.QName;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xdef.XDValueID;
import org.xdef.proc.XXData;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import static test.XDTester._xdNS;

/** Test construction mode of Xdefinition .
 * @author Vaclav Trojan
 */
final public class TestCompose extends XDTester {

	public TestCompose() {super();}

	@Override
	public void test() {
		Report rep;
		XDPool xp;
		XDDocument xd;
		StringWriter swr;
		PrintStream ps;
		Element el;
		String xml;
		String xdef;
		String s;
		Object obj;
		ArrayReporter reporter = new ArrayReporter();
		final String dataDir = getDataDir();
		try {
			xdef = "<xd:def xmlns:xd='" + _xdNS + "'><a/></xd:def>";
			xp = compile(xdef);
			//with data: null
			assertEq("<a/>", create(xp, null,(Element) null,"a"));
			assertNoErrorwarnings(reporter);
			//with data: source string
			assertEq("<a/>", create(xp, null, "a", reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			//with data: element
			el = KXmlUtils.newDocument(null, "a", null).getDocumentElement();
			assertEq("<a/>", create(xp, null, el, "a"));
			assertNoErrorwarnings(reporter);
//Create mode with recursive children and of usage of occurrence ('+','?','*').
// create expression specified
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a N = 'required string()' xd:script='create from(\"/a\")' >\n"+
"    <S xd:script='occurs ?; create from(\"S\")'>\n"+
"      <a xd:script = 'occurs +; create from(\"a\"); ref a' />\n"+ //recurse a
"    </S>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a N='1'><S><a N='2'><S><a N='3'/></S></a></S></a>";
			assertEq(xml, create(compile(xdef), null, reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = // default create expression
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a N = 'required string()' >\n"+
"    <S xd:script='occurs ?'>\n"+
"      <a xd:script = 'occurs *; ref a' />\n"+ //recurse a
"    </S>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(xml, create(compile(xdef), null, reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //default context; attibutes and text values are from source
"<xd:def xmlns:xd='" + _xdNS + "'>" +
"  <a>" +
"    <A xd:script='occurs 0..1; ref B; create from(\"A\")' />" +
"  </a>" +
"  <B a='create from(\"@A\").toString()'>" +
"    <C a='create from(\"@A\").toString()'>" +
"      create from(\"@A\").toString()" +
"    </C>" +
"  </B>" +
"</xd:def>";
			xml = "<a><A A='A'/></a>";
			assertEq(create(compile(xdef), null, reporter, xml),
				"<a><A a='A'><C a='A'>A</C></A></a>");
			assertNoErrorwarnings(reporter);
			xdef = //test if default context refers to element with same name
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"create from('/a')\">\n"+
"    <B>\n"+
"      <C xd:script=\"create from('X')\" c = 'string()' />\n"+
"    </B>\n"+
"    <D xd:script=\"create from('X')\"  c = 'string()'/>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(create(compile(xdef), null, reporter,
				"<a><B><X b='b' c='x'/></B><X c='y'/></a>"),
				"<a><B><C c='x'/></B><D c='y'/></a>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"create from('/a')\">\n"+
"    <B xd:script=\"occurs 0..;\" a='required string()'>\n"+
"      <C xd:script=\"occurs 0..; ref X; create from('X')\" />\n"+
"    </B>\n"+
"    <D xd:script=\"occurs 0..; ref X; create from('X')\" />\n"+
"  </a>\n"+
"  <X b = 'optional string()' c = 'optional string()'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq(create(xp, null, "a", reporter,
				"<a><B a='a'><X b='b' c='x'/></B><X c='y'/></a>"),
				"<a><B a='a'><C b='b' c='x'/></B><D c='y'/></a>");
			assertNoErrorwarnings(reporter);
			assertEq(create(xp, null, "a", reporter,
				"<a><B a='a'><X b='b' c='x'/><X c='y'/></B><X c='z'/></a>"),
				"<a><B a='a'><C b='b' c='x'/><C c='y'/></B><D c='z'/></a>");
			assertNoErrorwarnings(reporter);
			xdef = //in root children
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a> <b xd:script=\"occurs 1..*; create from('b')\"/> </a>\n"+
"</xd:def>";
			create(xdef, null, "a", reporter, "<a/>");
			//test if error "missing required element is reported"
			assertTrue(reporter.getErrorCount() == 1 && "XDEF539".equals(reporter.getReport().getMsgID()));
			xdef = // in nested nodes
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a><b><c xd:script=\"occurs 1..*; create from('d')\"/></b></a>\n"+
"</xd:def>";
			create(xdef, null, "a", reporter, "<a/>");
			assertTrue(reporter.errorWarnings() && "XDEF539".equals(reporter.getReport().getMsgID()));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a>\n"+
"    <b xd:script=\"occurs ?; create from('/a/b')\"/>\n"+
"    <c xd:script=\"occurs +; create from('/a/c')\"/>\n"+
"    <d xd:script=\"occurs *; create from('/a/d')\"/>\n"+
"    <e xd:script=\"occurs 1; create from('/a/d')\"/>\n"+
"  </a>\n"+
"</xd:def>";
			create(xdef, null, "a", reporter, "<a/>");
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF539") > 0 && s.contains("'c'") && s.contains("'e'")
				&& !s.contains("'b'") && !s.contains("'b'"), s);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'><a xd:script=\"create true\"/></xd:def>";
			assertEq("<a/>", create(xdef, null, "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // root not created
"<xd:def xmlns:xd = '" + _xdNS + "'><a xd:script=\"create false\"/></xd:def>";
			assertNull(create(xdef, null, "a", reporter, null));
			assertTrue(reporter.getErrorCount() == 1 && "XDEF556".equals(reporter.getReport().getMsgID()));
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create 1\">\n"+
"    <B xd:script=\"occurs *; create 3\"/>\n"+
"  </a>\n"+
"</xd:def>";
			el = create(xdef, null, "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(3, el.getElementsByTagName("B").getLength());
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create [true]\">\n"+
"    <B xd:script=\"occurs *; create [true,true,true]\"/>\n"+
"  </a>\n"+
"</xd:def>";
			el = create(xdef, null, "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(3, el.getElementsByTagName("B").getLength());
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create [false]\">\n"+ //even "false" is a value
"    <B xd:script=\"occurs *; create [true,true,true]\"/>\n"+
"  </a>\n"+
"</xd:def>";
			el = create(xdef, null, "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(3, el.getElementsByTagName("B").getLength());
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create [true]\">\n"+
"    <B xd:script=\"occurs *; create [true,false,true]\"/>\n"+
"  </a>\n"+
"</xd:def>";
			el = create(xdef, null, "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(3, el.getElementsByTagName("B").getLength());
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create [true]\">\n"+
"    <B xd:script=\"occurs *; create [true,null,true]\"/>\n"+
"  </a>\n"+
"</xd:def>";
			el = create(xdef, null, "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(2, el.getElementsByTagName("B").getLength());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'><a> string(); create [1,2]; </a></xd:def>";
			assertEq("<a>1\n2</a>", create(xdef, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create [1]\">\n"+
"    <B xd:script=\"occurs 1..5; create [2,1,0,3]\"/>\n"+
"  </a>\n"+ "</xd:def>";
			el = create(xdef, null, "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(4, el.getElementsByTagName("B").getLength());
			xdef = // root not created
"<xd:def xmlns:xd='" + _xdNS + "'><a xd:script=\"create null;\"/></xd:def>";
			assertNull(create(xdef, null, "a", reporter, null));
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF556".equals(reporter.getReport().getMsgID()), reporter);
			xdef = // required element missing
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a xd:script=\"create true;\">\n"+
"    <b xd:script=\"create null;\"/>\n"+
"  </a>\n"+
"</xd:def>";
			assertTrue(create(xdef, null, "a", reporter, null) != null);
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF539".equals(reporter.getReport().getMsgID()), reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"  <A xd:script= \"create [%a='1234']\" a = \"?\" b = \"?\"/> \n" +
"</xd:def>";
			xml ="<A a='1234'/>";
			assertEq(xml, create(xdef, "", "A", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"  <A> \n" +
"    <B xd:script= \"create [%a='1234']\" a = \"?\" b = \"?\"/> \n" +
"  </A> \n" +
"</xd:def>";
			xml ="<A><B a='1234'/></A>";
			assertEq(xml, create(xdef, "", "A", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // root not created
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a xd:script='create (Element) null;'><b xd:script='create true;'/></a>\n" +
"</xd:def>";
			assertNull(create(xdef, null, "a", reporter, null));
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF556".equals(reporter.getReport().getMsgID()), reporter);
			xdef = // root not created
"<xd:def xmlns:xd='"+_xdNS+"'><a xd:script='create(Container)null;'/></xd:def>";
			assertNull(create(xdef, null, "a", reporter, null));
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF556".equals(reporter.getReport().getMsgID()), reporter);
			xdef = // root not created
"<xd:def xmlns:xd='"+_xdNS+"'><a xd:script=\"create (String)null;\"/></xd:def>";
			assertNull(create(xdef, null, "a", reporter, null));
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF556".equals(reporter.getReport().getMsgID()), reporter);
			xdef = // context is an empty string, root created.
"<xd:def xmlns:xd='" + _xdNS + "'><a xd:script=\"create '';\"/></xd:def>";
			assertEq("<a/>", create(xdef, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice script = \"*; create from('//a/*')\">\n"+
"      <A xd:script = \"*; create {\n"+
"           Element c = from('.').getElement();\n"+
"           if (c.getTagName() != 'A') return false;\n"+
"           return c;}\"/>\n"+
"      <B xd:script = \"*; create {\n"+
"           Element c = from('.').getElement();\n"+
"           return (c.getTagName() == 'B') ? c : null;}\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><A/><B/><A/><B/></a>";
			assertEq(xml, parse(xdef, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration><![CDATA[Element source = xparse('<X><A/><Y/><Z/><A/><Z/></X>');]]></xd:declaration>\n"+
"  <a xd:script=\"create source\">\n"+
"    <b  xd:script=\"occurs 1..*; create xpath('//X/A', source)\"/>\n"+
"    <c  xd:script=\"occurs 1; create xpath('//X/Z[2]', source)\"/>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a><b/><b/><c/></a>",
				create(xdef, null, "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = //check all generated elements from number
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a> <B xd:script=\"*; create [3]\" a=\"string(); create 'x'\"/> </a>\n"+
"</xd:def>";
			NodeList nl = create(xdef, "", "a", reporter,null).getChildNodes();
			assertNoErrorwarnings(reporter);
			assertEq(nl.getLength(), 1);
			for (int i = 0; i < nl.getLength(); i++) {
				assertEq("x", ((Element) nl.item(i)).getAttribute("a"), "" +i);
			}
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration> external method long test.xdef.TestCompose.myMethod(long); </xd:declaration>\n"+
"  <a pi='required float(); create $PI' >\n"+
"    <B a='required num(); create myMethod(1)'\n"+
"       b=\"required string(1,30); create 'c';\" >\n"+
"      string(); create 'd'\n"+
"    </B>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(create(xdef, null, "a", reporter, null),
				"<a pi='3.141592653589793'><B a='123457' b='c'>d</B></a>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a>\n"+
"    <xd:choice script = \"*; create from('//a/*')\">\n"+
"      <A xd:script = \"*; create {\n"+
"         Element c = from('.').getElement();\n"+
"         return (c.getTagName() == 'A') ? c : null;}\"/>\n"+
"      <B xd:script = \"*; create {\n"+
"         Element c = from('.').getElement();\n"+
"         return (c.getTagName() == 'B') ? c : null;}\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><A/><B/><A/><B/></a>";
			assertEq(create(xdef, null, "a", reporter, xml), xml);
			assertNoErrorwarnings(reporter);
			xdef = // root not created
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script=\"create 0\">\n"+
"    <B xd:script=\"occurs +; create 0\"/>\n"+
"  </a>\n"+
"</xd:def>";
			assertTrue(create(xdef, null,  "a", reporter, null) == null);
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF556".equals(reporter.getReport().getMsgID()), reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<a>\n"+
" <xd:sequence script=\"+; create from('b')\">\n"+
"  <b>? string</b>\n"+
" </xd:sequence>\n"+
"\n"+
"</a>\n"+
"</xd:def>";
			xml = "<x><b>1</b><b>2</b></x>";
			assertEq("<a><b>1</b><b>2</b></a>",
				create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<a>\n"+
"  <b xd:script=\"create from('b[1]')\">string</b>\n"+
"  <b xd:script=\"create from('b[2]')\">string</b>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<x><b>1</b><y/><b>2</b></x>";
			assertEq("<a><b>1</b><b>2</b></a>",
				create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<a> <b xd:script=\"2; create from('c')\">string</b> </a>\n"+
"</xd:def>";
			xml = "<x><c>1</c><c>2</c></x>";
			assertEq(create(xdef, null, "a", reporter, xml), "<a><b>1</b><b>2</b></a>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:declaration>"+
"  int i = 1;\n"+
"</xd:declaration>"+
"<a xd:script=\"create 1\">\n"+
"  <b xd:script=\"2..3; create 2\">string; create i++</b>\n"+
"</a>\n"+
"</xd:def>";
			assertEq("<a><b>1</b><b>2</b></a>", create(xdef, null,  "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a> <b xd:script=\"+; create ['1', 'abc']\">string</b> </a>\n"+
"</xd:def>";
			assertEq("<a><b>1</b><b>abc</b></a>", create(xdef, null, "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"<a>\n"+
"  string; create from('./text()[1]')\n"+
"  <b xd:script=\"2; create from('//*')\">string</b>\n"+
"  string; create from('./text()[2]')\n"+
"</a>\n"+
"</xd:def>";
			xml = "<x>t1<b>1</b><c>2</c>t2</x>";
			assertEq(create(xdef, null, "a", reporter, xml), "<a>t1<b>t1</b><b>1</b>t2</a>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>\n"+
"    <![CDATA[Element e = xparse('<X><A/><Y/><Z/><A/><Z/></X>');]]>\n"+
"  </xd:declaration>\n"+
"  <a xd:script=\"create e\">\n"+
"    <b  xd:script=\"occurs 1..*; create xpath('//X/A', e)\"/>\n"+
"    <c  xd:script=\"occurs 1; create xpath('//X/Z[2]', e)\"/>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a><b/><b/><c/></a>", create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //this should be OK, value is trimmed
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<A a=\"required num(1,6);\n"+
"      onTrue out('OK');\n"+
"      create ' \n68829\t ';\n"+
"      onFalse {clearReports(); out('err: ' + getText());}\n"+
"      onAbsence {clearReports(); out('Attribute is missing!');}\">\n"+
"</A>\n"+
"</xd:def>";
			swr = new StringWriter();
			assertEq("<A a='68829'/>", create(xdef, null, "A", reporter, null, swr, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "OK");
			xdef = //this should be error, value is not numeric
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<A a=\"required num(1,6);\n"+
"      onTrue out('OK');\n"+
"      create '6x8829';\n"+
"      onFalse {clearReports(); out('err: ' + getText());}\n"+
"      onAbsence {clearReports(); out('Attribute is missing!');}\">\n"+
"</A>\n"+
"</xd:def>";
			swr = new StringWriter();
			assertEq("<A a='6x8829'/>", create(xdef, null, "A", reporter, null, swr, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "err: 6x8829");
			xdef = //this should be error, value is missing
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<A a=\"required num(1,6);\n"+
"      create null;\n"+
"      onFalse {clearReports(); out('err: ' + getText());}\n"+
"      onTrue out('OK');\n"+
"      onAbsence {clearReports(); out('Attribute is missing!');}\">\n"+
"</A>\n"+
"</xd:def>";
			swr = new StringWriter();
			assertEq(create(xdef, null, "A", reporter, null, swr, null), "<A/>");
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "Attribute is missing!");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a a=\"xdatetime('d-M-y');"+
" create new Datetime('2012-05-03').addDay(2).toString('dd-MM-yyyy');\">\n"+
"</a>\n"+
"</xd:def>";
			assertEq("05-05-2012", create(xdef, "", "a", reporter, null).getAttribute("a"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a a=\"xdatetime('d-M-y');"+
" create new Datetime('2012-05-03').addMonth(2).toString('dd-MM-yyyy');\">\n"+
"</a>\n"+
"</xd:def>";
			assertEq("03-07-2012", create(xdef, "", "a", reporter, null).getAttribute("a"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a a=\"xdatetime('d-M-y');"+
" create new Datetime('2012-05-03').addYear(2).toString('dd-MM-yyyy');\">\n"+
"</a>\n"+
"</xd:def>";
			assertEq("03-05-2014", create(xdef, "", "a", reporter, null).getAttribute("a"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a a=\"xdatetime('d-M-y');"+
" create new Datetime('2012-05-03').addDay(730).toString('dd-MM-yyyy');\">\n"+
"</a>\n"+
"</xd:def>";
			assertEq("03-05-2014", create(xdef, "", "a", reporter, null).getAttribute("a"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"<b v=\"float; fixed '2.0'\"\n"+
"   d=\"optional; create '123'\"/>\n"+
"<b v=\"fixed '2.0'\"\n"+
"   d=\"optional; create '456'\"/>\n"+
"</a>\n"+
"</xd:def>";
			assertEq("<a><b v='2.0' d='123'/><b v='2.0' d='456'/></a>", create(xdef,null,"a",reporter,null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"xd:script=\"options trimAttr\" root=\"Old | New\" >\n"+
"<xd:declaration>\n"+
" external method String test.xdef.TestCompose.myProc1(XXElement,XDValue[]);\n"+
" external method void test.xdef.TestCompose.myProc2(XXNode, XDValue[]);\n"+
" external method void test.xdef.TestCompose.myOutput(XXNode, XDValue[]);\n"+
"</xd:declaration>\n"+
"<New xd:script=\"create from('/Old/Old1');finally {myProc2();myOutput();}\"\n"+
"     VER=\"fixed '2.0'\"\n"+
"     myOutput=\"optional\">\n"+
"  <New1 xd:script=\"create from('inside1'); finally myProc2();\"\n"+
"        P1=\"optional string(); create from('@Q1');\"\n"+
"        P2=\"optional string();\n"+
"           create from('@Q2').toString();\"\n"+
"        X=\"optional; create myProc1('@Q1');\">\n"+
"  </New1>\n"+
"  <inside2 xd:script=\"finally myProc2();\"\n"+
"           Q1=\"required string()\"\n"+
"           Q2=\"required string()\"\n"+
"           X=\"required; create myProc1('@Q1');\">\n"+
"  </inside2>\n"+
"</New>\n"+
"<Old >\n"+
"<Old1 xd:script=\"finally setElement(xcreate('New'));\"\n"+
"       ver=\"fixed '1.0'\">\n"+
"  <foo xd:script=\"occurs 0..1\"/>\n"+
"  <inside1 xd:script=\"occurs 1..\"\n"+
"           Q1=\"required string();\"\n"+
"           Q2=\"required string();\"/>\n"+
"  <inside2 xd:script=\"occurs 1..\"\n"+
"           Q1=\"required string();\"\n"+
"           Q2=\"required string();\"/>\n"+
"</Old1>\n"+
"</Old>\n"+
"</xd:def>";
			xml =
"<Old>\n"+
"<Old1 ver=\"1.0\">\n"+
"  <foo>\n"+
"  </foo>\n"+
"  <inside1\n"+
"       Q1=\"Q1\"\n"+
"       Q2=\"Q2\">\n"+
"  </inside1>\n"+
"  <inside2\n"+
"       Q1=\"Q1\"\n"+
"       Q2=\"Q2\">\n"+
"  </inside2>\n"+
"</Old1>\n"+
"</Old>";
			xp = compile(xdef);
			//a) Result created according to Xdefinition
			assertEq(create(xp, "", "New", reporter, xml),
"<New VER='2.0' myOutput='null'><New1 P1='Q1' X='Q1' P2='Q2'/><inside2 Q2='Q2' Q1='Q1' X='Q1'/></New>");
			assertNoErrorwarnings(reporter);
			//b) Result created by parsing
			assertEq(parse(xp, null, xml, reporter),
"<Old><New VER=\"2.0\" myOutput=\"null\">"+
"<New1 P1=\"Q1\" P2=\"Q2\" X=\"Q1\"/><inside2 Q1=\"Q1\" Q2=\"Q2\" X=\"Q1\"/></New>"+
"</Old>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"Old | New\" >\n"+
"<xd:declaration>\n"+
" external method String test.xdef.TestCompose.myProc1(XXElement,XDValue[]);\n"+
" external method void test.xdef.TestCompose.myProc2(XXNode, XDValue[]);\n"+
"</xd:declaration>\n"+
"<New xd:script=\"create from('/Old'); finally myProc2();\"\n"+
"     VER=\"fixed '2.0'\"\n"+
"     P1=\"required string(); create from('@Q1').toString();\"\n"+
"     P2=\"required string(); create from('@Q2').toString();\"\n"+
"     X=\"required; create myProc1('@Q1','/Old/@ver');\">\n"+
"</New>\n"+
"<Old ver=\"fixed '1.0'\"\n"+
"     Q1=\"required string();\"\n"+
"     Q2=\"required string();\">\n"+
"</Old>\n"+
"</xd:def>";
			xml ="<Old ver=\"1.0\" Q1=\"Q1\" Q2=\"Q2\"/>";
			assertEq("<New P1=\"Q1\" P2=\"Q2\" X=\"Q1 1.0\" VER=\"2.0\"/>",
				create(compile(xdef), "", "New", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"Old | New\" >\n"+
"<xd:declaration>\n"+
" external method String test.xdef.TestCompose.myProc1(XXElement,XDValue[]);\n"+
"</xd:declaration>\n"+
"<New xd:script=\"create from('/Old/inside');\"\n"+
"     VER=\"fixed '2.0'\"\n"+
"     P1=\"required string(); create from('@Q1');\"\n"+
"     P2=\"required string(); create from('@Q2');\"\n"+
"     X=\"required; create myProc1('@Q1','/Old/@ver');\">\n"+
"</New>\n"+
"<Old ver=\"fixed '1.0'\">\n"+
"  <inside xd:script=\"occurs 1..\"\n"+
"    Q1=\"required string();\"\n"+
"    Q2=\"required string();\"/>\n"+
"</Old>\n"+
"</xd:def>";
			xml = "<Old ver=\"1.0\"><inside Q1=\"Q1\" Q2=\"Q2\"/></Old>";
			assertEq("<New P1=\"Q1\" P2=\"Q2\" X=\"Q1 1.0\" VER=\"2.0\"/>",
				create(xdef, "", "New", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"xd:script=\"options ignoreEmptyAttributes\" root=\"Old | New\" >\n"+
"<New VER=\"fixed '2.0'\"\n"+
"     xd:script=\"create from('self::Old');\">\n"+
"<inside xd:script=\"occurs 1..; create from('A');\"\n"+
"  N=\"required string(); create getAttr('i');\"\n"+
"  P=\"optional string(); create from('B/@a');\"/>\n"+
"</New>\n"+
"<Old ver=\"fixed '1.0'\">\n"+
"  <A i=\"required string()\"\n"+
"     xd:script=\"occurs 0..\">\n"+
"    <B x=\"required string();\"\n"+
"       a=\"optional string();\"\n"+
"       b=\"optional string();\"\n>"+
"    </B>\n"+
"  </A>\n"+
"</Old>\n"+
"</xd:def>";
			xml =
"<Old ver=\"1.0\">\n"+
"<A i=\"1\">\n"+
"  <B x=\"x1\" a=\"a1\" b=\"b1\">\n"+
"  </B>\n"+
"</A>\n"+
"<A i=\"2\">\n"+
"  <B x=\"x2\" b=\"b2\">\n"+
"  </B>\n"+
"</A>\n"+
"</Old>";
			assertEq("<New VER=\"2.0\"><inside N=\"1\" P=\"a1\"/><inside N=\"2\"/></New>",
				create(xdef, null, "New", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"N\">\n"+
"<xd:declaration>\n"+
"  external method void test.xdef.TestCompose.hasElement(\n"+
"           XXElement, XDValue[]);\n"+
"</xd:declaration>\n"+
" <N xd:script=\"finally {hasElement(4260,'D'); hasElement(4261,'V');}\"\n"+
"   V=\"fixed '2.0'\"\n"+
"   E=\"optional\">\n"+
"  <P xd:script=\"occurs 0..1\"/>\n"+
"  <D xd:script=\"occurs 0..1\"/>\n"+
"  <V xd:script=\"occurs 0..1\" />\n"+
"</N>\n"+
"</xd:def>\n";
			xml = "<N V='2.0'>\n  <P/>\n  <Z A='20040221'/>\n</N>";
			assertEq("<N V='2.0' E='D, V'><P/></N>", create(xdef, null, "N", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='B64'>\n"+
"  <B64 Data=\"required; create 'Data';\">\n"+
"	 base64Binary(7); create 'ahgkjfd01Q==';\n"+
"  </B64>\n"+
"</xd:def>";
			assertEq("<B64 Data=\"Data\">ahgkjfd01Q==</B64>", create(xdef, null, (Element) null, "B64"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Hex'>\n"+
"  <Hex Data=\"required; create 'Data';\">\n"+
"	 hexBinary(5); create ' af0dFFFFFF ';\n"+
"  </Hex>\n"+
"</xd:def>";
			assertEq("<Hex Data=\"Data\">af0dFFFFFF</Hex>", create(xdef, null, (Element) null,"Hex"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"EndPrgInfo | Complex\" >\n"+
"<xd:declaration>\n"+
" external method String test.xdef.TestCompose.MyProc(XXElement, XDValue[]);\n"+
"</xd:declaration>\n"+
"  <EndPrgInfo Verze=\"fixed '2.0'\"\n"+
"       Program=\"required string(1,4);\n"+
"                 create getAttr('Programx');\"\n"+
"       IdProces=\"required int()\"\n"+
"       Prg=\"required string(3,3)\"\n"+
"       Vysledek=\"required enum('OK','ERR')\"\n"+
"       MyAttr=\"required enumi('ab','cd'); create 'AB';\"\n"+
"       Kanal=\"optional num(2,2)\"\n"+
"       Souhrn=\"required; create MyProc('@Prg',\n"+
"                '/omplex/x[1]/text()',\n"+
"                '/omplex/x[2]/@attr');\">\n"+
"  </EndPrgInfo>\n"+
"\n"+
"<Complex ver=\"fixed '1.0'\">\n"+
"  <inside xd:script=\"occurs 1..;ref EndPrgInfo; create from('insidx')\"/>\n"+
"  <x xd:script=\"occurs 0..2\"\n"+
"     attr=\"optional string(); create getAttr('bttr');\">\n"+
"    optional\n"+
"  </x>\n"+
"</Complex>\n"+
"</xd:def>";
			xml =
"<omplex>\n"+
"  <insidx Verze=\"2.0\"\n"+
"       Programx=\"abcd\"\n"+
"       IdProces=\"123\"\n"+
"       Prg=\"xyz\"\n"+
"       Vysledek=\"OK\"\n"+
"       Kanal=\"22\">\n"+
"  </insidx>\n"+
"  <x>test</x>\n"+
"  <x bttr=\"neco\"></x>\n"+
"</omplex>\n";
			assertEq(create(xdef, "", "Complex", reporter, xml),
"<Complex ver=\"1.0\"><inside Verze=\"2.0\" Program=\"abcd\"" +
" IdProces=\"123\" Prg=\"xyz\" Vysledek=\"OK\" MyAttr=\"AB\"" +
" Kanal=\"22\" Souhrn=\"xyz test null\"/><x>test</x>" +
"<x attr=\"neco\"/></Complex>");
			assertNoErrorwarnings(reporter);
			xml =
"<DavkaA>\n"
+ "  <ZaznamA attrA=\"aaa1\"/>\n"
+ "  <ZaznamA attrA=\"aaa2\">ahoj</ZaznamA>\n"
+ "</DavkaA>\n";
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='DavkaA'>\n"+
"<xd:declaration>\n"+
"  external method void test.xdef.TestCompose.setUserResult(XXElement, XDValue[]);\n"+
"</xd:declaration>\n"+
"<DavkaA xd:script=\"finally setUserResult(); forget\">\n"+
"  <ZaznamA xd:script=\"+; finally setElement(xcreate('ZaznamB'));\" attrA=\"required\">\n"+
"    optional\n"+
"  </ZaznamA>\n"+
"</DavkaA>\n"+
"<ZaznamB attrB=\"required; create from('@attrA');\">\n"+
"  optional;onTrue setText('&quot;nazdar, ' + getText().toUpper() + ', tepic&quot;');\n"+
"</ZaznamB>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<DavkaA><ZaznamB attrB=\"aaa1\"/>"+
"<ZaznamB attrB=\"aaa2\">\"nazdar, AHOJ, tepic\"</ZaznamB></DavkaA>", xd.getUserObject().toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='DavkaA|ZaznamB'>\n"
+ "  <DavkaA>\n"
+ "    <ZaznamA xd:script=\"occurs 1..; finally setElement(xcreate('ZaznamB')); forget\"\n"
+ "             attrA=\"required\" >\n"
+ "      <ChildA1 xd:script=\"occurs 1..\"\n"
+ "               ChildA1Attr=\"required\" >\n"
+ "      </ChildA1>\n"
+ "      <ChildA2 xd:script=\"occurs 0..1\"\n"
+ "               ChildA2Attr=\"required\" >\n"
+ "        required\n"
+ "      </ChildA2>\n"
+ "    </ZaznamA>\n"
+ "  </DavkaA>\n"
+ "  <ZaznamB xd:script=\"create from('self::ZaznamA'); finally out(getElement());\"\n"
+ "           attrB=\"required; create getAttr('attrA');\">\n"
+ "    <ChildB1 xd:script=\"occurs 1..; create from('ChildA1');\" \n"
+ "             ChildB1Attr=\"required; create getAttr('ChildA1Attr');\" >\n"
+ "    </ChildB1>\n"
+ "    <ChildB2 xd:script=\"occurs 0..1; create from('ChildA2');\"\n"
+ "             ChildB2Attr=\"required; create getAttr('ChildA2Attr')\" >\n"
+ "      required\n"
+ "    </ChildB2>\n"
+ "  </ZaznamB>\n"
+ "</xd:def>";
			xml =
"<DavkaA>\n"
+ "  <ZaznamA attrA=\"aaa1\">\n"
+ "    <ChildA1 ChildA1Attr=\"1 ChildA1 1\">\n"
+ "    </ChildA1>\n"
+ "    <ChildA1 ChildA1Attr=\"1 ChildA1 2\">\n"
+ "    </ChildA1>\n"
+ "    <ChildA2 ChildA2Attr=\"1 ChildA2 1\" >\n"
+ "      text 1\n"
+ "    </ChildA2>\n"
+ "  </ZaznamA>\n"
+ "  <ZaznamA attrA=\"aaa2\">\n"
+ "    <ChildA1 ChildA1Attr=\"2 1\">\n"
+ "    </ChildA1>\n"
+ "  </ZaznamA>\n"
+ "</DavkaA>";
			//spustime parser, ktery na kazdy ZaznamA vytvori ZaznamB
			swr = new StringWriter();
			assertEq("<DavkaA/>", parse(xdef, null, xml, reporter, swr, null, null));
			s = swr.toString().trim().replace('\'', '"');
			assertEq("<ZaznamB attrB=\"aaa1\">" +
"<ChildB1 ChildB1Attr=\"1 ChildA1 1\"/>" +
"<ChildB1 ChildB1Attr=\"1 ChildA1 2\"/>" +
"<ChildB2 ChildB2Attr=\"1 ChildA2 1\">text 1</ChildB2>" +
"</ZaznamB><ZaznamB attrB=\"aaa2\">" +
"<ChildB1 ChildB1Attr=\"2 1\"/>" +
"</ZaznamB>", s);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"
+ "<Seznam_jmen xd:script=\"create from('/Davka')\" >\n"
+ "  <Zamestanec xd:script=\"occurs 1..; create from('Osoba')\"> \n"
+ "    optional string(); create @Jmeno + ' ' + from('@Prijmeni')\n"
+ "  </Zamestanec>\n"
+ "</Seznam_jmen>\n"
+ "</xd:def>";
			xml =
"<Davka>\n"
+ "  <Osoba Jmeno=\"Jan\" Prijmeni=\"Novak\" Cislo=\"123\" Plat=\"25300\" >\n"
+ "    <Charakteristika x=\"aaa\">Dobry</Charakteristika>\n"
+ "  </Osoba>\n"
+ "  <Osoba Jmeno=\"Frantisek\" Prijmeni=\"Valouch\" Cislo=\"456\" Plat=\"5200\" >\n"
+ "    <Charakteristika x=\"bbb\">Spatny</Charakteristika>\n"
+ "  </Osoba>\n"
+ "</Davka>";
			assertEq(create(xdef, "", "Seznam_jmen", reporter, xml),
"<Seznam_jmen><Zamestanec>Jan Novak</Zamestanec><Zamestanec>Frantisek Valouch</Zamestanec></Seznam_jmen>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"
+ "root='empty|EndPrgInfo|Complex|*'>\n"
+ "  <empty/>\n"
+ "  <EndPrgInfo Verze=\"fixed '2.0'\"\n"
+ "       Program=\"required string(1,4); create getAttr('Programx');\"\n"
+ "       IdProces=\"required int()\"\n"
+ "       Prg=\"required string(3,3)\"\n"
+ "       Vysledek=\"required enum('OK','ERR')\"\n"
+ "       MyAttr=\"required enumi('ab','cd'); create 'AB'\"\n"
+ "       Kanal=\"required num(2,2)\">\n"
+ "  </EndPrgInfo>\n"
+ "  <Complex ver=\"fixed '1.0'\">\n"
+ "  <inside xd:script=\"occurs 1..; create from('insidx'); ref EndPrgInfo\"/>\n"
+ "  <x xd:script=\"occurs 0..1\">\n"
+ "    required string()\n"
+ "  </x>\n"
+ "  </Complex>\n"
+ "</xd:def>";
			xml =
"<omplex>\n"
+ "  <insidx Verze=\"2.0\"\n"
+ "    Programx=\"abcd\"\n"
+ "    IdProces=\"123\"\n"
+ "    Prg=\"xyz\"\n"
+ "    Vysledek=\"OK\"\n"
+ "    xxxxxx=\"xxxxxx\"\n" //ignored
+ "    Kanal=\"22\">\n"
+ "  </insidx>\n"
+ "  <x>test</x>\n"
+ "  <x></x>\n" //ignored
+ "  <y>xxx</y>\n" //ignored
+ "</omplex>";
			assertEq(create(xdef, "", "Complex", reporter, xml),
"<Complex ver=\"1.0\">"+
"<inside "+
"Program=\"abcd\" Kanal=\"22\" Prg=\"xyz\" Vysledek=\"OK\""+
" IdProces=\"123\" Verze=\"2.0\" MyAttr=\"AB\"/>"+
"<x>test</x></Complex>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='DavkaA|ZaznamB'>\n"
+ "  <DavkaA>\n"
+ "    <ZaznamA xd:script=\"occurs 1..; finally setElement(xcreate('ZaznamB')); forget\"\n"
+ "             attrA=\"required\" >\n"
+ "      <ChildA1 xd:script=\"occurs 1..\"\n"
+ "               ChildA1Attr=\"required\" >\n"
+ "      </ChildA1>\n"
+ "      <ChildA2 xd:script=\"occurs 0..1\"\n"
+ "               ChildA2Attr=\"required\" >\n"
+ "        required\n"
+ "      </ChildA2>\n"
+ "    </ZaznamA>\n"
+ "  </DavkaA>\n"
+ "  <ZaznamB xd:script=\"create from('self::ZaznamA'); finally out(getElement());\"\n"
+ "           attrB=\"required; create getAttr('attrA');\">\n"
+ "    <ChildB1 xd:script=\"occurs 1..; create from('ChildA1');\" \n"
+ "             ChildB1Attr=\"required;\n"
+ " create getAttr('ChildA1Attr');\" >\n"
+ "    </ChildB1>\n"
+ "    <ChildB2 xd:script=\"occurs 0..1; create from('ChildA2');\"\n"
+ "             ChildB2Attr=\"required; create getAttr('ChildA2Attr')\" >\n"
+ "      required string;\n"
+ "    </ChildB2>\n"
+ "  </ZaznamB>\n"
+ "</xd:def>";
			xml =
"<DavkaA>\n"
+ "  <ZaznamA attrA=\"aaa1\">\n"
+ "    <ChildA1 ChildA1Attr=\"1 ChildA1 1\">\n"
+ "    </ChildA1>\n"
+ "    <ChildA1 ChildA1Attr=\"1 ChildA1 2\">\n"
+ "    </ChildA1>\n"
+ "    <ChildA2 ChildA2Attr=\"1 ChildA2 1\" >\n"
+ "      text 1\n"
+ "    </ChildA2>\n"
+ "  </ZaznamA>\n"
+ "  <ZaznamA attrA=\"aaa2\">\n"
+ "    <ChildA1 ChildA1Attr=\"2 1\">\n"
+ "    </ChildA1>\n"
+ "  </ZaznamA>\n"
+ "</DavkaA>";
			swr = new StringWriter();
			assertEq("<DavkaA/>",
				parse(xdef, null, xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq("<ZaznamB attrB=\"aaa1\">"
				+ "<ChildB1 ChildB1Attr=\"1 ChildA1 1\"/>"
				+ "<ChildB1 ChildB1Attr=\"1 ChildA1 2\"/>"
				+ "<ChildB2 ChildB2Attr=\"1 ChildA2 1\">text 1</ChildB2>"
				+ "</ZaznamB>"
				+ "<ZaznamB attrB=\"aaa2\">"
				+ "<ChildB1 ChildB1Attr=\"2 1\"/>"
				+ "</ZaznamB>",
				swr.toString());
			xdef = // input data parsed from element
"<xd:def xmlns:xd='" + _xdNS + "' root='Weather'>\n"
+ "<xd:declaration> float $sum = 0; int $n = 0; </xd:declaration>\n"
+ "<html>\n"
+ "  <body>\n"
+ "    <h2>"
+ "      create 'Date: ' + parseDate(from('@date'), 'yyyy-MM-dd').toString('{L(en)}EEEE, d. MMMM yyyy');\n"
+ "    </h2>\n"
+ "    <li xd:script=\"occurs 1..; create from('/Weather/Measurement')\">\n"
+ "    create 'Time: ' + toString(from('@time')) + ', wind: ' + toString(from('@wind'))\n"
+ "           + ', temperature: ' + toString(from('@temperature'));\n"
+ "    </li>\n"
+ "    <h3>\n"
+ "      create $n>0 ? 'Average temperature: '+toString($sum/$n) : 'No data'\n"
+ "    </h3>\n"
+ "  </body>\n"
+ "</html>\n"
+ "<Weather xd:script=\"finally setElement(xcreate('html'))\"\n"
+ "     date=\"optional string()\">\n"
+ "     <Measurement wind=\"required float()\"\n"
+ "                  temperature=\"required float(-99, +99); onTrue {$n++;$sum+=parseFloat(getText());}\"\n"
+ "                  time=\"required xdatetime('HH:mm')\"\n"
+ "                  xd:script=\"occurs 1..;\" />\n"
+ "</Weather>\n"
+ "</xd:def>";
			xml =
"<Weather date='2005-05-11' >\n"
+"<Measurement wind='5.3' temperature='13.0' time='05:00'/>\n"
+"<Measurement wind='7.2' temperature='15.2' time='11:00'/>\n"
+"<Measurement wind='8.7' temperature='18.1' time='15:00'/>\n"
+"<Measurement wind='3.9' temperature='16.5' time='20:00'/>\n"
+"</Weather>";
			s = // expected result
"<html><body><h2>Date: Wednesday, 11. May 2005</h2>"+
"<li>Time: 05:00, wind: 5.3, temperature: 13.0</li>"+
"<li>Time: 11:00, wind: 7.2, temperature: 15.2</li>"+
"<li>Time: 15:00, wind: 8.7, temperature: 18.1</li>"+
"<li>Time: 20:00, wind: 3.9, temperature: 16.5</li>"+
"<h3>Average temperature: 15.7</h3></body></html>";
			assertEq(parse(xdef, "", xml, reporter), s);
			assertNoErrorwarnings(reporter);
			xdef = // input data created from context and converted to element
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='Weather'>\n"+
"<xd:declaration>\n"+
"  float sum = 0; /*sum of temperatures*/\n"+
"  int   count = 0; /*number of measurements*/\n"+
"  Container c = [%Weather = [%date='2005-05-11',\n"+
"    [%Measurement =[%wind='5.3',%temperature='13.0',%time='05:00']],\n"+
"    [%Measurement =[%wind='7.2',%temperature='15.2',%time='11:00']],\n"+
"    [%Measurement =[%wind='8.7',%temperature='18.1',%time='15:00']],\n"+
"    [%Measurement =[%wind='3.9',%temperature='16.5',%time='20:00']]\n"+
"  ]];\n"+
"  Element el=c.toElement().toContainer().toElement(); /*check conversions*/\n"+
"</xd:declaration>\n"+
"<Weather date = \"optional xdatetime('yyyy-MM-dd')\" >\n"+
"  <Measurement xd:script = \"occurs 1..\"\n"+
"     wind        = \"required float(0,150)\"\n"+
"     temperature = \"required float(-30.0, +50.0);\"\n"+
"     time        = \"required xdatetime('HH:mm')\" />\n"+
"</Weather>\n"+
"<html xd:script=\"create xparse(el);\">\n"+
"<body>\n"+
"  <h2>create 'Date: ' + toString(parseDate(from('@date'),'yyyy-MM-dd'),'{L(en)}EEEE, d. MMMM yyyy')</h2>\n"+
"  <li xd:script=\"occurs 0..; create from('Measurement')\" >\n"+
"    create { count++; sum += parseFloat(from('@temperature'));\n"+
"      return 'Time: '+from('@time')+', wind: '+from('@wind')+', temperature: '+from('@temperature');}\n"+
"  </li>\n"+
"  <h3>create count>0 ? 'Average temperature: '+sum/count : 'No data';</h3>\n"+
"</body>\n"+
"</html>\n"+
"</xd:def>";
			assertEq(create(xdef,"", "html", reporter, null), s);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='N'>\n"+
" <N xd:script=\"create {Container c = from('/M/N');\n"+
"                  return (c.getLength() == 0) ? new Container(newElement()) : c;\n"+
"                }\"\n"+
"   V=\"fixed '2.0';\"\n"+
"   E=\"optional string()\" >\n"+
"   <P xd:script=\"occurs 1; create { Container c = from();\n"+
"        return (c.getLength() == 0) ? new Container(newElement()) : c;\n"+
"      }\" />\n"+
"   <Z xd:script=\"occurs 0..1\" A=\"required num()\"/>\n"+
" </N>\n"+
" </xd:def>";
			xml =
"<M>\n"
+"  <N E=\"E\" >\n"
+"    <P/>\n"
+"    <Z A=\"20040221\" />\n"
+"  </N>\n"
+"</M>\n";
			assertEq(create(xdef,"", "N", reporter, xml), "<N E=\"E\" V=\"2.0\"><P/><Z A=\"20040221\"/></N>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd= '" + _xdNS + "'>\n"
+"<xd:declaration>\n"
+" external method\n"
+"   void test.xdef.TestCompose.mySetAttrFromXpath(XXData, String);\n"
+"</xd:declaration>\n"
+"<xd:def name=\"a\" root=\"DN\" >\n"
+"<DN>\n"
+"  <Osoba xd:script = \"occurs 0..\" id=\"required num()\">\n"
+"    <xd:choice>\n"
+"    <FO xd:script = \"occurs 1\"\n"
+"        rc = \"optional num()\"\n"
+"        nar = \"optional xdatetime('d.M.y')\" >\n"
+"        <Adr1 xd:script = \"occurs 0..\" psc = \"optional num()\" />\n"
+"    </FO>\n"
+"    <PO xd:script = \"occurs 1\" ICO = \"optional num()\" >\n"
+"    </PO>\n"
+"    </xd:choice>\n"
+"    <Adr xd:script = \"occurs 0..\" psc = \"optional num()\" />\n"
+"    <Test xd:script = \"occurs 0..\"\n"
+"          id        = \"required num()\"\n"
+"          souprava  = \"optional string()\"\n"
+"    />\n"
+"  </Osoba>\n"
+" <Ukon xd:script = \"occurs 0..; ref Ukon#Ukon; \" />\n"
+"</DN>\n"
+"</xd:def>\n"
+"<xd:def name = \"Ukon\" root = \"Ukon\" >\n"
+"<Ukon xd:script = \"create {\n"
+"        String element0 = 'Osoba';\n"
+"        String s = element0 + '[@id=' + '1' + ']';\n"
+"        Container c = from(s);\n"
+"        if (c.getLength() EQ 0) {\n"
+"          return new Container(newElement());\n"
+"        } else {\n"
+"          return c;\n"
+"        }\n"
+"      }\"\n"
+"      rc    = \"optional string(); create toString(from('FO/@rc')); onAbsence setText('')\"\n"
+"      nar   = \"optional string(); create toString(from('FO/@nar'))\"\n"
+"      psc   = \"optional string();\n"
+"        create {\n"
+"          String s = toString(from('FO/Adr1/@psc'));\n"
+"          trace(s);\n"
+"          return s;\n"
+"        }\"\n"
+"      month = \"optional int(); create {\n"
+"           String s = (String) from('FO/@nar');\n"
+"           return toString(parseDate(s,'d.M.y').getMonth());\n"
+"      }\"\n"
+"      day      = \"optional int(); create {\n"
+"           String s = from('FO/@nar').toString();\n"
+"           return toString(parseDate(s,'d.M.y').getDay());\n"
+"      }\"\n"
+"      year     = \"optional int(); create {\n"
+"           String s = toString(from('FO/@nar'));\n"
+"           return toString(parseDate(s,'d.M.y').getYear());\n"
+"      }\"\n"
+"/>\n"
+"</xd:def>\n"
+"<xd:def name =\"Ukon1\" root = \"Ukon\" >\n"
+"<Ukon xd:script = \"finally {\n"
+"        String s = '5' + '.' + getAttr('month') + '.' + getAttr('year');\n"
+"        if (isDatetime(s, 'd.M.y')) {\n"
+"          setAttr('nar', s);\n"
+"        }\n"
+"        trace(getAttr('nar'));\n"
+"      }\"\n"
+"      rc  = \"optional string();\"\n"
+"      day = \"optional int(1,31);\"\n"
+"      month = \"optional int(1,12)\"\n"
+"      year = \"optional int(1900,2050)\"\n"
+"      nar = \"optional string()\"\n"
+"      psc  = \"optional string()\"/>\n"
+"</xd:def>\n"
+"<xd:def name = \"Ukon2\" root = \"Ukon\" >\n"
+"<Ukon rc = \"optional string(); finally mySetAttrFromXpath('Osoba[@id=\\'1\\']/FO[1]')\"\n"
+"      nar = \"optional string(); finally mySetAttrFromXpath('Osoba[@id=\\'1\\']')\"\n"
+"      day = \"optional int(1,31);\"\n"
+"      month = \"optional int(1,12)\"\n"
+"      year = \"optional int(1900,2050)\"\n"
+"      psc  = \"optional string()\"/>\n"
+"</xd:def>\n"
+"</xd:collection>";
			xml =
"<DN>\n"
+"  <Osoba id=\"1\">\n"
+"    <FO rc=\"12345\" nar='3.12.1921'>\n"
+"       <Adr1 psc=\"12\" />\n"
+"    </FO>\n"
+"    <Adr psc=\"12345\" />\n"
+"    <Test id=\"1\" souprava=\"12\" />\n"
+"    <Test id=\"2\" souprava=\"34\" />\n"
+"  </Osoba>\n"
+"  <Osoba id=\"2\">\n"
+"    <FO rc=\"12358\" >\n"
+"       <Adr1 psc=\"98623\" />\n"
+"    </FO>\n"
+"    <Adr psc=\"12345\" />\n"
+"    <Test id=\"1\" souprava=\"12\" />\n"
+"    <Test id=\"2\" souprava=\"34\" />\n"
+"  </Osoba>\n"
+"</DN>";
			setProperty("xdef.debug", "true");
			setProperty("xdef.externalmode", "both");
			xp = compile(xdef);
			xd = xp.createXDDocument("a");
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			Element db = xd.getElement();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			XDDocument xd1 = xp.createXDDocument("Ukon");
////////////////////////////
			xd1.setXDContext(db);
			xd1.getDebugger().setOutDebug(ps);
			el = create(xd1, "Ukon", null);
			s = baos.toString();
			assertTrue(s.startsWith("TRACE /Ukon/@psc; pc=") && s.indexOf("; \"12\";") > 0, s);
			el.setAttribute("rc", "45678");
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd1 = xp.createXDDocument("Ukon1");
			xd1.getDebugger().setOutDebug(ps);
			el = xd1.xparse(el, reporter);
			assertNoErrorwarnings(reporter);
			s = baos.toString();
			assertTrue(s.startsWith("TRACE /Ukon; pc=") && s.indexOf("; \"5.12.1921\";") > 0, s);
			xd1 = xp.createXDDocument("Ukon2");
			xd1.setUserObject(db);
			xd1.xparse(el, reporter);
			assertNoErrorwarnings(reporter);
			obj = KXpathExpr.evaluate(xd.getElement(),"/DN/Osoba[1]/@nar");
			if (obj instanceof Node) {
				obj = ((Node) obj).getNodeValue();
			} else if (obj instanceof NodeList) {
				obj = ((NodeList) obj).item(0).getNodeValue();
			} else if (obj instanceof ArrayList) {
				obj = ((ArrayList) obj).get(0);
			}
			if (!"5.12.1921".equals(obj)) {
				if (obj == null) {
					obj = "null";
				}
				fail(obj.toString());
			}
			obj = KXpathExpr.evaluate(xd.getElement(),"/DN/Osoba[1]/FO/@rc");
			if (obj instanceof Node) {
				obj = ((Node) obj).getNodeValue();
			} else if (obj instanceof NodeList) {
				obj = ((NodeList) obj).item(0).getNodeValue();
			} else if (obj instanceof ArrayList) {
				obj = ((ArrayList) obj).get(0);
			}
			if (!"45678".equals(obj)) {
				if (obj == null) {
					obj = "null";
				}
				fail(obj.toString());
			}
			xdef = // create only elements "b" with child nodes
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <a>\n" +
"    <b xd:script=\"*; create from('b/*/..');\" x=\"string()\"><c xd:script=\"*\" y=\"string()\"/></b>\n" +
"  </a> \n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml =
"<a><b x='1'><c y='1'/></b><b x='2'/><b x='3'><c y='2'/></b><b x='4'/></a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b x='1'><c y='1'/></b><b x='3'><c y='2'/></b></a>",
				create(xd, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
		} catch (DOMException | SRuntimeException ex) {fail(ex);}
		setProperty("xdef.debug", "false");
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"    impl-version='1.0.0.0' impl-date='9.3.2008'\n"+
"    xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"    xmlns:c=\"http://ws.ckp.cz/pis/B1/2007/04\"\n"+
"    xmlns:k=\"http://ws.koop.cz/B1A/2008/01\">\n"+
"  <s:Envelope xd:script='create newElement()'\n"+
"      s:encodingStyle =\"create 'http://www.syntea.cz/ckp/pis/encoding'\">\n"+
"    <s:Header k:in=\"required string(3); create 'xxx'\"\n"+
"        xd:script='create newElement()'>\n"+
"      <c:out xd:script='occurs 1; create newElement()'/>\n"+
"    </s:Header>\n"+
"    <s:Body xd:script='create newElement()'>\n"+
"      <k:in xd:script='occurs 1; create newElement()'/>\n"+
"    </s:Body>\n"+
"  </s:Envelope>\n"+
"</xd:def>";
			xml =
"<s:Envelope xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\"/>";
			setProperty("xdef.externalmode", "both");
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			el = xd.xcreate(new QName("http://www.w3c.org/2003/05/soap-envelope", "s:Envelope"),null);
			assertNoErrorwarnings(reporter);
			assertEq("<s:Envelope " +
				"xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\" " +
				"s:encodingStyle=\"http://www.syntea.cz/ckp/pis/encoding\">"+
				"<s:Header k:in=\"xxx\" "+
				"xmlns:k=\"http://ws.koop.cz/B1A/2008/01\">"+
				"<c:out xmlns:c=\"http://ws.ckp.cz/pis/B1/2007/04\"/>"+
				"</s:Header>"+
				"<s:Body>" +
				"<k:in xmlns:k=\"http://ws.koop.cz/B1A/2008/01\"/>" +
				"</s:Body>" +
				"</s:Envelope>", el);
			xd = xp.createXDDocument(); //with data == null
			el = xd.xcreate(new QName("http://www.w3c.org/2003/05/soap-envelope", "s:Envelope"), null);
			assertEq("<s:Envelope " +
				"xmlns:s='http://www.w3c.org/2003/05/soap-envelope' " +
				"s:encodingStyle='http://www.syntea.cz/ckp/pis/encoding'>"+
				"<s:Header k:in='xxx' "+
				"xmlns:k='http://ws.koop.cz/B1A/2008/01'>"+
				"<c:out xmlns:c='http://ws.ckp.cz/pis/B1/2007/04'/>"+
				"</s:Header>"+
				"<s:Body>" +
				"<k:in xmlns:k='http://ws.koop.cz/B1A/2008/01'/>" +
				"</s:Body>" +
				"</s:Envelope>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:s='http://soap'>\n"+
"  <root xd:script='create newElement()'\n"+
"      b =\"fixed 'b'\" >\n"+
"  <s:child xd:script='occurs 1..*; create {\n"+
"      Container c = [];\n"+
"      for (int i = 0; i LT 3; i++) {\n"+
"        c.addItem(newElement());\n"+
"      }\n"+
"      return c;}'/>\n"+
"  </root>\n"+
"</xd:def>";
			setProperty("xdef.externalmode", "both");
			//with data == null
			el = create(xdef, null, (Element) null, "root");
			assertEq("<root b='b' xmlns:s='http://soap'><s:child/><s:child/><s:child/></root>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script=\"*; create from('//b')\">\n"+
"      create from('c/text()');\n"+
"      <b/>\n"+
"    </xd:sequence>\n"+
"    <c/>\n"+
"    <xd:sequence script=\"*; create from('//b/d/e')\">\n"+
"	   create from('./text()');\n"+
"      <b/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xml =
"<a>\n"+
"  <b>\n"+
"    <c>1</c>\n"+
"    <d><e>3</e><e>4</e><e>5</e><e>6</e></d>\n"+
"  </b>\n"+
"  <b>\n"+
"    <c>2</c>\n"+
"    <d><e>7</e></d>\n"+
"  </b>\n"+
"</a>";
			xp = compile(xdef);
			s = "<a>1<b/>2<b/><c/>3<b/>4<b/>5<b/>6<b/>7<b/></a>";
			assertEq(s, create(xp, null, reporter, xml));
			assertEq(s, parse(xp, null, s, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a>\n"+
"    <b v=\"fixed '2.0'\" d=\"optional; create '123'\"/>\n"+
"    <b v=\"fixed '2.0'\" d=\"optional; create '456'\"/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a><b v='2.0' d='123'/><b v='2.0' d='456'/></a>", create(xp,null,"a",reporter,null));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b v='2.0' d='123'/><b v='2.0' d='456'/></a>", create(xp,null,"a",reporter,"<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b v='2.0' d='123'/><b v='2.0' d='456'/></a>",
				create(xp, null, "a", reporter, "<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a a = \"required num(3); create '123'\">\n"+
"  <b a = \"required num(2); create '99'\"\n"+
"     b = \"required string(1,30); create 'x';\" >\n"+
"    required string(); create 'OK'\n"+
"  </b>\n"+
"</a>\n"+
"</xd:def>\n";
			assertEq("<a a='123'><b a='99' b='x'>OK</b></a>", create(xdef, null, "a", reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd ='" + _xdNS + "'\n"+
"    xmlns:s =\"http://schemas.xmlsoap.org/wsdl/soap/\"\n"+
"    xmlns:b =\"http://ws.koop.cz/B1A/2008/01\"\n"+
"    root='s:Envelope'>\n"+
"  <s:Envelope xd:script='create newElement()'\n"+
"      s:encodingStyle =\"create 'http://ws.koop.cz/B1A/2008/01'\"\n"+
"      xmlns:k='http://ws.koop.cz/B1A/2008/01'/>\n"+
"</xd:def>";
			xml="<s:Envelope xmlns:s='http://schemas.xmlsoap.org/wsdl/soap/'/>";
			assertEq("<s:Envelope " +
				"xmlns:s=\"http://schemas.xmlsoap.org/wsdl/soap/\" " +
				"s:encodingStyle=\"http://ws.koop.cz/B1A/2008/01\"/>",
				create(compile(xdef), null, reporter,xml)); //SoapFaultAnswerB1A
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:s='soap'>\n"+
"  <root xd:script ='create newElement()' s:a =\"fixed 'sa'\" b =\"fixed 'b'\" >\n"+
"    <s:child xd:script='create newElement()' s:a =\"fixed 'aa'\" b =\"fixed 'bb'\" />\n"+
"  </root>\n"+
"</xd:def>";
			assertEq("<root s:a=\"sa\" b=\"b\" xmlns:s =\"soap\"><s:child b=\"bb\" s:a=\"aa\"/></root>",
				create(compile(xdef), null, (Element) null, "root"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:s='http://soap'>\n"+
"  <root xd:script='create newElement()' b =\"fixed 'b'\" >\n"+
"    <s:child xd:script='occurs 1..*; create {Container c = new Container();\n"+
"        for (int i = 0; i LT 3; i++) c.addItem(newElement());\n"+
"        return c;}'/>\n"+
"  </root>\n"+
"</xd:def>";
			el = create(xdef, null, null, "root");
			assertEq("<root b=\"b\" xmlns:s =\"http://soap\"><s:child/><s:child/><s:child/></root>", el);
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xp = compile(dataDir + "compose/panovnici-seznamy.xdef");
			el = create(xp, "", "HTML", reporter, dataDir + "compose/panovnici-seznamy.xml");
			assertEq(el, dataDir + "compose/panovnici-seznamy-output.xml");
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "", el, reporter), dataDir + "compose/panovnici-seznamy-output.xml");
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "compose/panovnici-tabulka.xdef");
			el = create(xp, "", "HTML", reporter, dataDir + "compose/panovnici-tabulka.xml");
			assertEq(el, dataDir + "compose/panovnici-tabulka-output.xml");
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "", el, reporter), dataDir + "compose/panovnici-tabulka-output.xml");
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "compose/panovnici-sort.xdef");
			el = create(xp, "", "HTML", reporter, dataDir + "compose/panovnici-sort.xml");
			assertEq(el, dataDir + "compose/panovnici-sort-output.xml");
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "", el, reporter), dataDir + "compose/panovnici-sort-output.xml");
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "compose/panovnici-historie.xdef");
			el = create(xp, "", "HTML", reporter, dataDir + "compose/panovnici-historie.xml");
			assertEq(el, dataDir + "compose/panovnici-historie-output.xml");
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "", el, reporter), dataDir + "compose/panovnici-historie-output.xml");
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "compose/panovnici-atributy.xdef");
			el = create(xp, "", "panovnici-ceskeho-statu", reporter,dataDir+"compose/panovnici-atributy.xml");
			assertEq(el, dataDir + "compose/panovnici-atributy-output.xml");
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "", el, reporter), dataDir + "compose/panovnici-atributy-output.xml");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='s:Envelope'\n"+
"    xmlns:s='schemas.xmlsoap.org/soap/envelope/'\n"+
"    xmlns:k='ws.koop.cz/B1A/2008/01'>\n"+
"  <xd:declaration>int i = 0;</xd:declaration>\n"+
"   <s:Envelope>\n"+
"      <s:Header>\n"+
"         <k:Request s:mustUnderstand = \"create 'true'\"/>\n"+
"         <k:User s:mustUnderstand = \"create 'true'\"/>\n"+
"      </s:Header>\n"+
"      <s:Body>\n"+
"         <k:Set_PrenosPSP  KodPojistitele = \"create '0'\">\n"+
"            <k:ZdrojovyPSP xd:script = \"occurs 2..5\"\n"+
"                           KodPojistitele = \"create '' + (i++)\"/>\n"+
"         </k:Set_PrenosPSP>\n"+
"      </s:Body>\n"+
"   </s:Envelope>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			el = xd.xcreate(new QName("schemas.xmlsoap.org/soap/envelope/", "s:Envelope"), null);
			assertNoErrors(reporter);
			assertEq(
"<s:Envelope xmlns:s='schemas.xmlsoap.org/soap/envelope/'>"+
"<s:Header>"+
"<k:Request s:mustUnderstand='true' xmlns:k='ws.koop.cz/B1A/2008/01'/>"+
"<k:User s:mustUnderstand='true' xmlns:k='ws.koop.cz/B1A/2008/01'/>"+
"</s:Header>"+
"<s:Body>"+
"<k:Set_PrenosPSP KodPojistitele='0' xmlns:k='ws.koop.cz/B1A/2008/01'>"+
"<k:ZdrojovyPSP KodPojistitele='0'/>"+
"<k:ZdrojovyPSP KodPojistitele='1'/>"+
"</k:Set_PrenosPSP>"+
"</s:Body>"+
"</s:Envelope>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='s:Envelope'\n"+
"     xmlns:s='schemas.xmlsoap.org/soap/envelope/'\n"+
"     xmlns:k='koop.cz/B1A/2008/01'>\n"+
"  <xd:declaration>\n"+
"    Container genContainer(Element el, int n) {\n"+
"      Container c = new Container();\n"+
"      for (int i = n; i > 0; i--) { c.addItem(el); }\n"+
"      return c;\n"+
"    }\n"+
"    int i = 0;\n"+
"  </xd:declaration>\n"+
"  <s:Envelope>\n"+
"    <s:Header>\n"+
"      <k:Request s:mustUnderstand=\"create 'true'\"/>\n"+
"      <k:User s:mustUnderstand=\"create 'true'\"/>\n"+
"    </s:Header>\n"+
"    <s:Body>\n"+
"      <k:Set_PrenosPSP  KodPojistitele=\"create '0'\">\n"+
"        <k:ZdrojovyPSP xd:script=\"occurs 1..3;\n"+
"                                   create genContainer(newElement(), 5);\"\n"+
"            KodPojistitele=\"create '' + (i++)\"/>\n"+
"      </k:Set_PrenosPSP>\n"+
"    </s:Body>\n"+
"  </s:Envelope>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			el = xd.xcreate(new QName("schemas.xmlsoap.org/soap/envelope/", "s:Envelope"), reporter);
			assertNoErrors(reporter);
			assertEq(
"<s:Envelope xmlns:s='schemas.xmlsoap.org/soap/envelope/'>"+
"<s:Header>"+
"<k:Request s:mustUnderstand='true' xmlns:k='koop.cz/B1A/2008/01'/>"+
"<k:User s:mustUnderstand='true' xmlns:k='koop.cz/B1A/2008/01'/>"+
"</s:Header>"+
"<s:Body>"+
"<k:Set_PrenosPSP KodPojistitele='0' xmlns:k='koop.cz/B1A/2008/01'>"+
"<k:ZdrojovyPSP KodPojistitele='0'/>"+
"<k:ZdrojovyPSP KodPojistitele='1'/>"+
"<k:ZdrojovyPSP KodPojistitele='2'/>"+
"</k:Set_PrenosPSP>"+
"</s:Body>"+
"</s:Envelope>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
  "<xd:declaration>\n"+
"    Container c = new Container();\n"+
"    Container ctx(int i) {\n"+
"      Container c = new Container();\n"+
"      while(i-- > 0) {\n"+
"        c.addItem(newElement('b'));\n"+
"      }\n"+
"      return c;\n"+
"    }\n"+
"    Container ctx() {\n"+
"      Container c = new Container();\n"+
"      c.addItem(newElement('c'));\n"+
"      return c;" +
"    }\n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    <b xd:script = 'occurs 0..*; create ctx(3)'/>\n"+
"    <c xd:script = 'create ctx()'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			assertEq("<a><b/><b/><b/><c/></a>",
				create(xdef, "", "a", reporter, "<a/>"));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"\">\n"+ //default => /a
"   <b xd:script=\"occurs +; create from ('b')\" />\n"+
"  </a>\n"+
"</xd:def>\n";
			assertEq("<a/>", create(xdef, "", "a", reporter, "<a/>", null, null));
			assertErrors(reporter);
			assertEq("<a><b/><b/></a>", create(xdef, "", "a", reporter, "<a><b/><b/></a>", null, null));
			assertNoErrorwarnings(reporter);
			xdef = //test default create
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"\">\n"+
"   <b xd:script=\"occurs +\" />\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b/></a>", create(xp, "", "a", reporter, "<a></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp,"","a",reporter,"<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "", "a", reporter, "<a><b/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><b/></a>", create(xp, "", "a", reporter, "<a><c/><b/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>",create(xp, "", "a", reporter, "<a><c/><d/><e/></a>"));
			assertNoErrorwarnings(reporter);
			xdef = // test sequence methods and external create methods
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDContainer test.xdef.TestCompose.ctx();\n"+
"    external method XDContainer test.xdef.TestCompose.ctx(long);\n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    <xd:sequence xd:script= \"init outln('start'); finally outln('end')\">\n"+
"      <b xd:script = 'occurs 0..*; create ctx(3)'/>\n"+
"      <c xd:script = 'create ctx()'/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n";
			swr = new StringWriter();
			assertEq("<a><b/><b/><b/><c/></a>", create(xdef, "a", "a", reporter, null, swr, null));
			assertNoErrorwarnings(reporter);
			assertEq("start\nend\n", swr.toString());
			xdef = //test default create in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a> <xd:mixed> <b/> <c/> <d/> </xd:mixed> </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><b/><d/><c/></a>"));
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><c/><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed script = '?'> <b/> <c/> <d/> </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><b/><d/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", parse(xp, "", "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><c/><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", parse(xp, "", "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", parse(xp, "", "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script = \"occurs *\">\n"+
"      <b xd:script = \"occurs *\"/>\n"+
"      <c xd:script = \"occurs *\"/>\n"+
"      <d xd:script = \"occurs *\"/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a/>", create(xp, "","a", reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp,"","a",reporter,"<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp,"","a",reporter,"<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp,"","a",reporter,"<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/></a>", create(xp, "", "a", reporter, "<a><b/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/></a>", create(xp, "", "a", reporter, "<a><c/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, "", "a", reporter, "<a><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, "", "a", reporter, "<a><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a", reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b xd:script = \"occurs *; create from('b')\"/>\n"+
"      <c xd:script = \"occurs *; create from('c')\"/>\n"+
"      <d xd:script = \"occurs *; create from('d')\"/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a/>", create(xp, "", "a", reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "","a",reporter,"<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp, "","a",reporter,"<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp, "","a",reporter, "<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/></a>", create(xp, "","a",reporter, "<a><b/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/></a>", create(xp, "","a",reporter, "<a><c/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, "","a",reporter, "<a><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, "","a",reporter, "<a><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "","a",reporter, "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "","a",reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef = //test default create in multiple choice
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice occurs = '0..2'>\n"+
"      <b xd:script = \"create from('b')\"/>\n"+
"      <c xd:script = \"create from('c')\"/>\n"+
"      <d xd:script = \"create from('d')\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a/>", create(xp, "","a",reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "","a",reporter, "<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp, "","a",reporter,"<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp, "","a",reporter, "<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "","a",reporter,"<a><b/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "", "a",reporter, "<a><c/><c/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "", "a",reporter, "<a><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp, "","a",reporter,"<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp, "","a",reporter, "<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice occurs = '*' create = \"from('/a/*')\">\n"+
"      <b xd:script = \"occurs *; create from('self::b')\"/>\n"+
"      <c xd:script = \"occurs *; create from('self::c')\"/>\n"+
"      <d xd:script = \"occurs *; create from('self::d')\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a/>", create(xp, "","a",reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "","a",reporter, "<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp, "","a",reporter, "<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp, "","a",reporter, "<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/></a>", create(xp, "", "a",reporter, "<a><b/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/><b/></a>", create(xp, "", "a",reporter, "<a><c/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, "", "a",reporter, "<a><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/><b/></a>", create(xp, "", "a",reporter, "<a><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a",reporter, "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/><d/><b/></a>", create(xp, "", "a",reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed script = \"occurs ?; create from('//a/*')\">\n"+
"      <b xd:script = \"occurs *; create from('self::b')\"/>\n"+
"      <c xd:script = \"occurs *; create from('self::c')\"/>\n"+
"      <d xd:script = \"occurs *; create from('self::d')\"/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a/>", create(xp, "","a",reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp, "","a",reporter, "<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp, "","a",reporter, "<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp, "","a",reporter, "<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/></a>", create(xp, "","a",reporter, "<a><b/><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/><b/></a>", create(xp, "", "a",reporter, "<a><c/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, "", "a",reporter, "<a><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/><b/></a>", create(xp,"", "a",reporter, "<a><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, "", "a",reporter, "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/><d/><b/></a>", create(xp, "", "a",reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script = \"occurs *; create from('//a/*')\">\n"+
"      <b xd:script = \"occurs *; create from('self::b')\"/>\n"+
"      <c xd:script = \"occurs *; create from('self::c')\"/>\n"+
"      <d xd:script = \"occurs *; create from('self::d')\"/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a/>", create(xp, null,"a",reporter, "<a/>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/></a>", create(xp,null,"a",reporter,"<a><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/></a>", create(xp, null, "a", reporter, "<a><c/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/></a>", create(xp, null, "a",reporter, "<a><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/><b/></a>", create(xp, "", "a",reporter, "<a><c/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><d/></a>", create(xp, null, "a",reporter, "<a><b/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><d/><b/></a>", create(xp, null, "a",reporter, "<a><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><c/><d/></a>", create(xp, null, "a",reporter, "<a><b/><c/><d/></a>"));
			assertNoErrorwarnings(reporter);
			assertEq("<a><c/><d/><b/></a>", create(xp, null, "a",reporter, "<a><c/><d/><b/></a>"));
			assertNoErrorwarnings(reporter);
			xdef = //test default create in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b xd:script = \"occurs ?; create from('/a/@b').toString().length()\">\n"+
"      required string(); create from('/a/@b')\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b>bbb</b></a>", create(xp, "", "a", reporter, "<a b='bbb'/>"));
			assertEq("<a/>", create(xp, "", "a", reporter, "<a/>"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b xd:script = \"occurs ?; create from('/a/@b').toString() != ''\">\n"+
"      required string(); create from('/a/@b')\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b>bbb</b></a>", create(xp, "", "a" ,reporter, "<a b='bbb'/>"));
			assertEq("<a/>", create(xp, "", "a", reporter, "<a/>"));
			xdef = //test fromSource
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script = \"create newElement()\">\n"+
"    <b xd:script = \"create fromRoot('/a')\">\n"+
"      optional string(); create from('/a/@b')\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b>bbb</b></a>", create(xp ,null, "a",reporter, "<a b='bbb'/>"));
			assertEq("<a><b/></a>", create(xp, null,"a", reporter, "<a/>"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script = \"create newElement()\">\n"+
"    <b xd:script = \"create newElement()\">\n"+
"      optional string(); create fromRoot('/a/@b')\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			assertEq("<a><b>bbb</b></a>", create(xp, null,"a",reporter, "<a b = 'bbb'/>"));
			assertEq("<a><b/></a>", create(xp, null,"a",reporter, "<a/>"));
			xdef = //sequence
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='root'>\n"+
"  <root>\n"+
"   <xd:sequence xd:script=\"occurs *; create from('//a')\">\n"+
"     <a a = \"required string; create from('@A')\">\n"+
"       <b xd:script=\"occurs *; create from('B')\"\n"+
"         x = \"required string; create from('@c')\"\n"+
"         y = \"required string; create from('@d')\"/>\n"+
"     </a>\n"+
"   </xd:sequence>\n"+
"  </root>\n"+
"</xd:def>";
			assertEq("<root>" +
				"<a a=\"A\"><b x=\"c\" y=\"d\"/><b x=\"C\" y=\"D\"/></a>" +
				"<a a=\"B\"><b x=\"e\" y=\"f\"/></a>" +
				"</root>",
				create(xdef, "a", "root", reporter,
"<x>" +
"<a A=\"A\"><B c=\"c\" d=\"d\"/><B c=\"C\" d=\"D\"/></a>" +
"<a A=\"B\"><B c=\"e\" d=\"f\"/></a>" +
"</x>"));
			xdef = //test create from parsedXml
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name = 'a' root = 'a'>\n"+
"  <xd:declaration scope='global'>\n"+
"    external int $x;\n"+
"    int $y = $x + 1;\n"+
"  </xd:declaration>\n"+
"  <a>required int(); finally $y = $y + 1;</a>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'b'>\n"+
"  <xd:declaration scope='global'>\n"+
"    external String $s;\n"+
"    int $z;\n"+
"  </xd:declaration>\n"+
"  <b xd:script=\"create xparse($s, 'a'); finally $z = $y + 1;\">\n"+
"    required int()\n"+
"  </b>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xd = compile(xdef).createXDDocument("b");
			xml = "<a>123</a>";
			xd.setVariable("$s", xml);
			xd.setVariable("$x", 10);
			assertEq("<b>123</b>", create(xd, "b", reporter));
			assertEq(xd.getVariable("$s").stringValue(), xml);
			assertEq(xd.getVariable("$x").intValue(), 10);
			assertEq(xd.getVariable("$y").intValue(), 12);
			assertEq(xd.getVariable("$z").intValue(), 13);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:declaration scope='global'>\n"+
"    external String source; /*This must be set from calling program!*/\n"+
"    /** Check ID values.*/\n"+
"    boolean checkId() {\n"+
"      String s = getText(); /*get value to be checked*/\n"+
"      if (!string(10,11)) /*length must be 10 or 11 characters*/\n"+
"         return error('Incorrect length of PID');\n"+
"      if (s.substring(6,7) != '/') /* on 6th position must be '/'*/\n"+
"         return error('Missing slash character');\n"+
"      if (!isNumeric(s.substring(0,6)))/*first 6 characters numeric*/\n"+
"			return error('Second part is not numeric');\n"+
"      if (!isNumeric(s.substring(7))) /* atfer slash must be numeric */\n"+
"			return error('First part is not numeric');\n"+
"      return true;\n"+
"    }\n"+
"  </xd:declaration>\n"+
"  <Contract cId = 'required num(10)'\n"+
"            xd:script='create xparse(source)' >\n"+
"    <Owner Title=\"required string(1,30);create from('@title')\"\n"+
"        IC=\"required num(8); create from('@ic')\"\n"+
"        xd:script=\"occurs 1; create from('Client[@role=\\'1\\']')\"/>\n"+
"    <Holder Name=\"required string(1,30); create from('@name')\"\n"+
"        FamilyName=\"required string(1,30); create from('@familyname')\"\n"+
"        PersonalId=\"required checkId(); create from('@pid')\"\n"+
"        xd:script=\"occurs 1; create from('Client[@role=\\'2\\']')\"/>\n"+
"    <Policyholder Title=\"required string(1,30);\n"+
"                         create from('@name')+' '+from('@familyname')\"\n"+
"        IC=\"required num(8); create from('@ic')\"\n"+
"        xd:script=\"occurs 1; create from('Client[@role=\\'3\\']')\"/>\n"+
"  </Contract>\n"+
"</xd:def>";
			xml =
"<Contract cId = \"0123456789\">\n"+
"  <Client role = \"1\"\n"+
"          typ = \"P\"\n"+
"          title = \"Firma XYZ Ltd\"\n"+
"          ic = \"12345678\" />\n"+
"  <Client role = \"2\"\n"+
"          typ = \"O\"\n"+
"          typid = \"1\"\n"+
"          name = \"Frantisek\"\n"+
"          familyname = \"Novak\"\n"+
"          pid = \"311270/1234\" />\n"+
"  <Client role = \"3\"\n"+
"          typ = \"O\"\n"+
"          typid = \"2\"\n"+
"          name = \"Frantisek\"\n"+
"          familyname = \"Novak\"\n"+
"          pid = \"311270/1234\"\n"+
"          ic = \"87654321\" />\n"+
"</Contract>";
			xd = (compile(xdef)).createXDDocument();
			xd.setVariable("source", xml);
			el = create(xd, "Contract", reporter);
			assertNoErrorwarnings(reporter);
			assertTrue("Policyholder".equals(el.getChildNodes().item(2).getNodeName()));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <A xd:script=\"create [%a='a','b'].toElement()\" a='string'> string </A>\n"+
"</xd:def>";
			assertEq("<A a='a'>b</A>", create(xdef, "", "A", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <A xd:script=\"create [%a='a','b']\" a='string'> string </A>\n"+
"</xd:def>";
			assertEq("<A a='a'>b</A>", create(xdef, "", "A", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 3; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b xd:script=\"create $typ EQ 1\"/>\n"+
"      <c xd:script=\"create $typ EQ 2\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(create(xdef,"", "a", reporter, null), "<a/>");
			rep = reporter.getReport();
			assertEq(rep != null ? rep.getMsgID() : "?", "XDEF555");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 3; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b xd:script='match $typ EQ 1'/> <c xd:script='match $typ EQ 2'/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(create(xdef,"", "a", reporter, null), "<a/>");
			rep = reporter.getReport();
			assertEq(rep != null ? rep.getMsgID() : "?", "XDEF555");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2;'>\n"+
"      <b xd:script=\"*;create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create $typ EQ 2; finally $typ = 3;\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a><b/><c/></a>", create(xdef,"", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2..3'>\n"+ //creates min
"      <b xd:script=\"create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create $typ EQ 2;\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a><b/><c/></a>", create(xdef,"", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2..3'>\n"+ //creates min
"      <b xd:script=\"create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create $typ EQ 2;\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(create(xdef,"", "a", reporter, null), "<a><b/><c/></a>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2..3; create true'>\n"+  //creates max
"      <b xd:script=\"create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create $typ EQ 2;\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a><b/><c/><c/></a>", create(xdef,"","a",reporter,null));
			assertNoErrorwarnings(reporter);
			xdef = // conainer to root, named values with maps
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <xd:declaration>\n"+
"    int i = 0;\n"+
"    Container c = [[%a = 'a', %b = 'b'], [%a = 'c', %b = 'd']];\n"+
"  </xd:declaration>\n"+
"  <a xd:script='create c'>\n"+
"    <b xd:script='occurs +;' a='string' b='string'/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b a='a' b='b'/><b a='c' b='d'/></a>";
			assertEq(xml, create(xdef, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <xd:declaration>\n"+
"    int i = 0;\n"+
"    Container c = [[%b=[%a = 'a', %b = 'b']], [%b=[%a = 'c', %b = 'd']]];\n"+
"  </xd:declaration>\n"+
"  <a xd:script='create c;'>\n"+
"    <b xd:script='occurs +;' a='string' b='string'/>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(xml, create(xdef, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // conainer to child, named values with maps
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <xd:declaration>\n"+
"    int i = 0;\n"+
"    Container c = [[%b=[%a = 'a', %b = 'b']], [%b=[%a = 'c', %b = 'd']]];\n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    <b xd:script='occurs +; create c;' a='string' b='string'/>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(xml, create(xdef, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // conainer to child, maps is child items
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <xd:declaration>\n"+
"    int i = 0;\n"+
"    Container c = [[%a = 'a', %b = 'b'], [%a = 'c', %b = 'd']];\n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    <b xd:script='occurs +; create c;' a='string' b='string'/>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq(xml, create(xdef, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"  <xd:declaration>Container c=[[%a='A',[%b='B','C'],'D']];</xd:declaration>\n"+
  "<A xd:script= \"create c\" a= \"string\">\n" +
"    <B b = \"string\">string</B>\n" +
"    string\n" +
"  </A>\n" +
"</xd:def>";
			assertEq("<A a=\"A\"><B b=\"B\">C</B>D</A>", create(xdef, "", "A", reporter, null));
			assertNoErrorwarnings(reporter);
////////////////////////////////////////////////////////////////////////////////
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b xd:script = \"occurs 0..; create from('b');\n"+
"       onStartElement out('S=' + from('@x'));\n"+
"       init out('I=' + from('@x')); finally out('F=' + from('@x'));\"/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b x='1'/><b x='2'/></a>";
			swr = new StringWriter();
			assertEq("<a><b/><b/></a>", create(xdef, null, "a", reporter, xml, swr, null));
			assertEq("I=1S=1F=1I=2S=2F=2", swr.toString());
			xdef = // test moreAttributes and recursive reference
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='create from(\"/a\"); options moreAttributes, moreText'>\n"+
"    <a xd:script='*; ref a; create from(\"a\")'> ? string </a>\n"+
"  </a>\n"+
"</xd:def>";
			xml =
"<a>\n"+
"  <a>\n"+
"    <a a='1'>\n"+
"      <a/>\n"+
"      <a/>\n"+
"      one\n"+
"    </a>\n"+
"    <a a='2'>\n"+
"      <a/>\n"+
"      <a/>\n"+
"      two\n"+
"    </a>\n"+
"  </a>\n"+
"  <a>\n"+
"    <a a='3'>\n"+
"     three\n"+
"    </a>\n"+
"    <a a='4'/>\n"+
"  </a>\n"+
"</a>";
			el = parse((xp = compile(xdef)), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			(xd = xp.createXDDocument()).setXDContext(el);
			assertEq("<a><a><a a='1'><a/><a/>one</a><a a='2'>"+
				"<a/><a/>two</a></a><a><a a='3'>three</a><a a='4'/></a></a>",
				create(xd, "a",reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // array with one item array
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' xd:root='A'>\n" +
"  <xd:declaration scope='local'>\n" +
"    Container c = [ [ ['2007-01-01'], ['2007-01-02'], ['abc'] ] ];\n" +
"  </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script='create c'>\n" +
"      <C xd:script='occurs 1..*;'> <D>optional date();</D> </C>\n" +
"    </B>\n" +
"  </A>\n" +
"</xd:def>";
			assertEq("<A><B><C><D>2007-01-01</D></C></B></A>",
				compile(xdef).createXDDocument().xcreate("A", reporter));
			assertNoErrorsAndClear(reporter);
			xdef =  // array with two items array
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' xd:root='A'>\n" +
"  <xd:declaration scope='local'>\n" +
"    Container c = [\n" +
"      [ ['2007-01-01'], ['2007-01-02'], ['abc'] ],\n" +
"      [ ['2007-01-03'], ['2007-01-04'], ['def'] ]\n" +
"    ];\n" +
"  </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script='create c'>\n" +
"      <C xd:script='occurs 1..*;'> <D>optional date();</D> </C>\n" +
"    </B>\n" +
"  </A>\n" +
"</xd:def>";
			assertEq(compile(xdef).createXDDocument().xcreate("A", reporter),
"<A><B><C><D>2007-01-01</D></C><C><D>2007-01-03</D></C></B></A>");
			assertNoErrorsAndClear(reporter);
			xdef = //external method with context - default, see <b>
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDContainer test.xdef.TestCompose.getDataDoc(\n"+
"      XXElement, XDContainer, String); \n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"   <b xd:script='occurs *'>\n"+
"    <xd:choice xd:script=\"occurs 0..1;"+
"        create getDataDoc(from('x | y'), getAttr('a'))\">\n"+
"      <c xd:script=\"create from('self::x'); ref z\"/>\n"+
"      <d xd:script=\"create from('self::y'); ref z\"/>\n"+
"    </xd:choice>\n"+
"   </b>\n"+
"  </a>\n"+
"  <z k = \"string; create from('@c') \"/>\n"+
"</xd:def>";
			xml = "<r><b a='1'><x c='10'/></b>"
				+ "<b a='2'><x c='20'/></b>"
				+ "<b a='3'><y c='30'/></b>"
				+ "<b a='4'/>"
				+ "<b a='5'/></r>";
			assertEq("<a><b><c k=\"10\"/></b>"
				+ "<b><c k=\"20\"/></b>"
				+ "<b><d k=\"30\"/></b>"
				+ "<b><c k=\"88\"/></b>"
				+ "<b><d k=\"99\"/></b></a>",
				create(xdef, "", "a",reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //2 external method with context - specified, see <b>
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDContainer test.xdef.TestCompose.getDataDoc(\n"+
"      XXElement, XDContainer, String); \n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"   <b xd:script=\"occurs *; create from('b')\" >\n"+
"    <xd:choice xd:script=\"occurs 0..1; "
				+ "create getDataDoc(from('x | y'), getAttr('a'))\">\n"+
"      <c xd:script=\"create from('self::x'); ref z\"/>\n"+
"      <d xd:script=\"create from('self::y'); ref z\"/>\n"+
"    </xd:choice>\n"+
"   </b>\n"+
"  </a>\n"+
"  <z k = \"string; create from('@c') \"/>\n"+
"</xd:def>";
			xml = "<r><b a='1'><x c='10'/></b>"+
				"<b a='2'><x c='20'/></b>"+
				"<b a='3'><y c='30'/></b>"+
				"<b a='4'/>"+
				"<b a='5'/></r>";
			assertEq("<a><b><c k=\"10\"/></b>"+
				"<b><c k=\"20\"/></b>"+
				"<b><d k=\"30\"/></b>"+
				"<b><c k=\"88\"/></b>"+
				"<b><d k=\"99\"/></b></a>",
				create(xdef, "", "a",reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //3 method with context -  specified, see <b>
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDContainer test.xdef.TestCompose.getDataDoc(\n"+
"      XXElement, XDContainer, String); \n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    <b xd:script=\"occurs *; create from('b')\" a=\"create from('@a')\" >\n"+
"      <xd:choice xd:script=\"occurs 0..1;\n"+
"                   create getDataDoc(from('x | y'), getAttr('a'))\">\n"+
"        <c xd:script=\"create from('self::x'); ref z\"/>\n"+
"        <d xd:script=\"create from('self::y'); ref z\"/>\n"+
"      </xd:choice>\n"+
"    </b>\n"+
"  </a>\n"+
"  <z k = \"string; create from('@c') \"/>\n"+
"</xd:def>";
			xml = "<r><b a='1'><x c='10'/></b>"+
				"<b a='2'><x c='20'/></b>"+
				"<b a='3'><y c='30'/></b>"+
				"<b a='4'/>"+
				"<b a='5'/></r>";
			assertEq("<a><b a=\"1\"><c k=\"10\"/></b>"+
				"<b a=\"2\"><c k=\"20\"/></b>"+
				"<b a=\"3\"><d k=\"30\"/></b>"+
				"<b a=\"4\"><c k=\"88\"/></b>"+
				"<b a=\"5\"><d k=\"99\"/></b></a>",
				create(xdef, "", "a",reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration scope='global'> external Element e;</xd:declaration>\n"+
"  <a xd:script=\"+; create e\">\n"+
"    <b xd:script=\"+; create xpath('y[2]', e)\">\n"+
"      <c a='string'/>\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>\n";
			xml ="<x><y/><y>x<z/>x<c a='1'/></y><y/></x>";
			xd = compile(xdef).createXDDocument();
			xd.setVariable("e", xml);
			assertEq("<a><b><c a='1'/></b></a>", create(xd,"a",reporter,xml));
			assertNoErrorwarnings(reporter);
			xdef = //4 method with context - see <b>
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDContainer test.xdef.TestCompose.getDataDoc(\n"+
"      XXElement, XDContainer, String); \n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"   <b xd:script=\"occurs *; create from('b')\" >\n"+
"    <xd:choice xd:script=\"occurs 0..1;"+
"               create getDataDoc(from('*'), getAttr('a'))\">\n"+
"      <c xd:script=\"create from('self::x'); ref z\"/>\n"+
"      <d xd:script=\"create from('self::y'); ref z\"/>\n"+
"    </xd:choice>\n"+
"   </b>\n"+
"  </a>\n"+
"  <z k = \"string; create from('@c') \"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<r><b a='1'><x c='10'/></b>"
				+ "<b a='2'><x c='20'/></b>"
				+ "<b a='3'><y c='30'/></b>"
				+ "<b a='4'/>"
				+ "<b a='5'/></r>";
			xd.setXDContext(xml);
			assertEq("<a><b><c k=\"10\"/>"+
				"</b><b><c k=\"20\"/>"+
				"</b><b><d k=\"30\"/>"+
				"</b><b><c k=\"88\"/>"+
				"</b><b><d k=\"99\"/></b></a>", create(xd, "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //choice default variant
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice> <b /> <xx xd:script='?'/> </xd:choice>\n"+
"    <c/>\n"+
"    <d/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //choice explicite variant
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice xd:script=\"create from('/a/b')\">\n"+
"      <b xd:script=\"create from('.')\"/>\n"+
"      <xx xd:script='?'/>\n"+
"    </xd:choice>\n"+
"    <c xd:script=\"create from('/a/c')\"/>\n"+
"    <d xd:script=\"create from('/a/d')\"/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //mixed, default variant
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed> <b /> <x xd:script='?'/> </xd:mixed>\n"+
"    <c/>\n"+
"    <d/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><x/><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //mixed, explicite variant
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed xd:script=\"create from('/a')\">\n"+
"      <b /> <x xd:script='?'/>\n"+
"    </xd:mixed>\n"+
"    <c/>\n"+
"    <d/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><x/><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //sequence default create
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b xd:script=\"create from('b')\" />\n"+
"      <x xd:script=\"create from('x')\" />\n"+
"    </xd:sequence>\n"+
"    <c/>\n"+
"    <d/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><x/><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertTrue(reporter.errors(), "Error not reported");
			xdef = //sequence explicit create
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <a>\n"+
"    <xd:sequence xd:script=\"create from('/a')\">\n"+
"      <b xd:script=\"create from('b')\" />\n"+
"      <x xd:script=\"create from('x')\" />\n"+
"    </xd:sequence>\n"+
"    <c/>\n"+
"    <d/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><x/><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertTrue(reporter.errors(), "Error not reported");
			xdef = // element before choice and after choice
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b xd:script='+; create from(\"/a/b\")'/>\n"+
"    <xd:choice>\n"+
"       <c xd:script='1; create from(\"/a/c\")'/>\n"+
"    </xd:choice>\n"+
"    <d xd:script='1; create from(\"/a/d\")'/>\n"+
"    <e xd:script='?; create from(\"/a/e\")'/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b/><c/><d/><e/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><c/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertTrue(reporter.errors(), "Error not reported");
			xml = "<a><c/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertTrue(reporter.errors(), "Error not reported");
			xdef = //Test any in create mode
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:any xd:script=\"occurs *; finally out('fa'); create from('./*')\"/>\n" +
"  </a>\n"+
"</xd:def>\n";
			xml = "<a><b a = '1'><c/></b><b a = '2'><c/></b><x a = 's'/></a>";
			swr = new StringWriter();
			assertEq(create(xdef, null, "a", reporter, xml, swr, null), xml);
			assertNoErrorwarnings(reporter);
			assertEq("fafafa", swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a> <xd:any xd:script=\"occurs 1; finally out('fa');\" /> </a>\n"+
"</xd:def>\n";
			xml = "<a><b a = '1'><c/></b><b a = '2'><c/></b><x a = 's'/></a>";
			swr = new StringWriter();
			assertEq("<a><b a = '1'><c/></b></a>", create(xdef, null, "a", reporter, xml, swr, null));
			assertNoErrorwarnings(reporter);
			assertEq("fa", swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a><xd:any xd:script=\"occurs +;finally out(9);create from('x')\" /></a>\n" +
"</xd:def>\n";
			xml = "<a><b a = '1'><c/></b><b a = '2'><c/></b><x a = 's'/></a>";
			swr = new StringWriter();
			assertEq("<a><x a = 's'/></a>", create(xdef, null, "a", reporter, xml, swr, null));
			assertEq("9", swr.toString());
			xdef = //test of from()
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=''><b xd:script='occurs +; create from()'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			el = create(xp, null, "a", reporter, "<a/>");
			assertTrue(reporter.errors(), "Error not reported");
			assertEq(el, "<a/>");
			el = create(xp, null, "a", reporter, "<a><c/></a>");
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a><b/></a>");
			el = create(xp, null, "a", reporter, "<a><b/><c/></a>");
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a><b/></a>");
			el = create(xp, null, "a", reporter, "<a><c/><b/><b/></a>");
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a><b/><b/></a>");
			el = create(xp, null, "a", reporter, "<a><c/><d/><e/></a>");
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a><b/></a>");
			xdef = //forget in create mode
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    Container c = ['x','y'], d = [1,2,3]; int i = 0, j = 0;\n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    <a xd:script='*; create i != c.getLength();\n"+
"          finally {if (j == d.getLength()) {j = 0; i++;}} forget;'\n"+
"       c = '? string; create c.item(i)' d = '? int;  create d.item(j++)'/>\n"+
"  </a>\n"+
"</xd:def>";
			el = create(xdef, "#a",  "a", reporter, null);
			xml =
"<a>"+
	"<a c='x' d='1'/><a c='x' d='2'/><a c='x' d='3'/>"+
	"<a c='y' d='1'/><a c='y' d='2'/><a c='y' d='3'/>"+
"</a>";
			assertEq(xml, el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns='N' xmlns:sod='N' root='a'\n"+
"        script='options ignoreAttrWhiteSpaces, ignoreTextWhiteSpaces' >\n"+
"  <a>\n"+
"    <e f=\"optional string; create xpath('\\'\\'')\"/>\n"+
"    <e f=\"string; create xpath('\\'2\\'')\"/>\n"+
"    <f xd:script=\"occurs *; create xpath('../e')\"/>\n"+
"    <x xd:script=\"occurs *; create xpath('../sod:e')\"/>\n"+
"    <h xd:script=\"occurs *; create xpath('preceding-sibling::e')\"/>\n"+
"    <j xd:script=\"occurs *; create xpath('preceding-sibling::sod:e')\"/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			el = xp.createXDDocument().xcreate(new QName("N", "a"), reporter);
			assertEq("<a xmlns=\"N\"><e/><e f=\"2\"/><x/><x/><j/><j/></a>", el);
			assertNoErrorwarnings(reporter);
			xdef = // child node is xd:any
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <a xd:script='finally\n"+
"       returnElement((Element) getElement().getChidNodes().item(0));'>\n" +
"    <xd:any xd:script='options moreAttributes, moreElements, moreText;\n" +
"              create from(\"/a_/a/*\");' />\n" +
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xd.setXDContext("<a_><a><b d='abc'><c/></b></a></a_>");
			assertEq("<b d='abc'><c/></b>", xd.xcreate("a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = // create from element
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>\n"+
"  external method Element test.xdef.TestCompose.getChybyElement(XXElement);\n"+
"</xd:declaration>\n"+
"  <a xd:script=\"create getChybyElement();\" >\n"+
"    <b xd:script = \"occurs 1..\"\n"+
"       Kod       = \"required num(3)\" Typ = \"required string(1)\" />\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			el = create(xp, "", "a", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(el,"<a><b Kod='123' Typ='T'/><b Kod='456' Typ='T'/></a>");
			xdef = // check external method xx in create section
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <xd:declaration>\n"+
"   external method String test.xdef.TestCompose.xx(XXElement, XDContainer);\n"+
" </xd:declaration>\n"+
" <a><c f=\"? string; create xx(from('@f'))\"/></a>\n"+
"</xd:def>";
			xd = (xp = compile(xdef)).createXDDocument();
			xml = "<a><c f='xx'/></a>";
			xd.setXDContext(xml);
			assertEq(xml, create(xd, "a", reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><c/></a>";
			(xd = xp.createXDDocument()).setXDContext(xml);
			assertEq(xml, create(xd, "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = // test initialization of var section
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <a xd:script='var int i; init i=2;' b=\"optional int(); create ''+i;\"/>\n" +
"</xd:def>";
			xml = "<a b='2'/>";
			assertEq(xml, parse(xp = compile(xdef), "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // create from variable
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <a>\n" +
"    <a xd:script='var int i; init i=2;' b=\"optional int();create ''+i;\"/>\n"+
"  </a>\n" +
"</xd:def>";

			xml = "<a><a b='2'/></a>";
			assertEq(xml, parse(xp = compile(xdef), "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = // test create objects from null
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"  <xd:declaration>\n"+
"    external method String test.xdef.TestCompose.nulString(XXData);\n"+
"    external method Long test.xdef.TestCompose.nulLong(XXData);\n"+
"    external method Float test.xdef.TestCompose.nulFloat(XXData);\n"+
"    external method SDatetime test.xdef.TestCompose.nulDatetime(XXData);\n"+
"    external method SDuration test.xdef.TestCompose.nulDuration(XXData);\n"+
"    external method XDContainer test.xdef.TestCompose.nulContainer(XXData);\n"+
"  </xd:declaration>\n"+
"  <A b='optional string(); create nulString()'\n" +
"      c='optional long(); create nulLong()'\n"+
"      d='optional float(); create nulFloat()'\n"+
"      e='optional dateTime(); create nulDatetime()'\n"+
"      f='optional duration(); create nulDuration()'\n"+
"      g='optional string(); create nulContainer()' >\n"+
"    <B>optional string(); create nulString()</B>\n"+
"    <C>optional long(); create nulLong()</C>\n"+
"    <D>optional float(); create nulFloat()</D>\n"+
"    <E>optional dateTime(); create nulDatetime()</E>\n"+
"    <F>optional duration(); create nulDuration()</F>\n"+
"    <G>optional string(); create nulContainer()</G>\n"+
"    <X xd:script='*;; create nulString()'/>\n"+
"    <Y xd:script='*;; create nulLong()'/>\n"+
"    <Z xd:script='*;; create nulContainer()'/>\n"+
"  </A>\n"+
"</xd:def>";
			el = create(xdef, "", "A", reporter, null);
			assertNoErrorwarnings(reporter);
			assertEq(el,"<A><B/><C/><D/><E/><F/><G/></A>");
			// test recursive reference to model
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"   <A>\n" +
"     <B xd:script='ref B;' />\n" +
"   </A>\n" +
"   <B b='string()'>\n" +
"      <B xd:script='?; ref B;'/>\n" +
"   </B>\n" +
"</xd:def>";
			xml = "<A><B b=\"x\"><B b=\"y\"/></B></A>";
			assertEq(xml, parse(xp = compile(xdef), "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			(xd = xp.createXDDocument()).setXDContext(xml);
			assertEq(xml, create(xd, "A", reporter));
			assertNoErrorwarningsAndClear(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"   <A xd:script=\"1; create from('/A')\">\n" +
"     <B xd:script=\"0..; ref B; create from('B')\" />\n" +
"   </A>\n" +
"   <B b=\"string()\" xd:script=\"0..; create from('B')\">\n" +
"      <B xd:script=\"0..; ref B2;\"/>\n" +
"   </B>\n" +
"   <B2 b=\"string()\" xd:script=\"0..; create from('B')\">\n" +
"      <B xd:script=\"0..; ref B3;\"/>\n" +
"   </B2>\n" +
"   <B3 b=\"string()\" xd:script=\"0..; create from('B')\">\n" +
"      <B xd:script=\"0..; ref B4;\"/>\n" +
"   </B3>\n" +
"   <B4 xd:script=\"0..; create from('B')\"/>\n" +
"</xd:def>";
			xml =
"<A>\n" +
"   <B b=\"a1\">\n" +
"      <B b=\"a2\">\n" +
"        <B b=\"a3\">\n" +
"          <B/><B/><B/>\n" +
		 "</B>\n" +
"        <B b=\"a4\"/>\n" +
"     </B>\n" +
"   </B>\n" +
"   <B b=\"a9\"/>\n" +
"</A>";
			assertEq(xml, parse(xp = compile(xdef), "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			(xd = xp.createXDDocument()).setXDContext(xml);
			assertEq(xml, create(xd, "A", reporter));
			assertNoErrorwarningsAndClear(reporter);
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xdef = //test of exception in external method.
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> external method void test.xdef.TestCompose.throwExc(); </xd:declaration>" +
"  <a xd:script='finally throwExc()' />\n" +
"</xd:def>";
			create(compile(xdef), "", "a", reporter, null);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if(!reporter.errorWarnings()) {
				fail("error not reported");
			} else {
				rep = reporter.getReport();
				if (rep == null) {
					fail("report missing");
				} else {
					assertEq("XDEF569", rep.getMsgID());
				}
			}
		}
		try { //Test create mode with recursive reference.
			xd =  compile( // create section
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <X a=\"string()\">\n" +
"      <X xd:script=\"0..; create from('X'); ref X\"/>\n" +
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<X a=\"a1\"><X a=\"a2\"/><X a=\"a3\"/></X>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("X", null));
			xd =  compile( // no create section
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <X a=\"string()\">\n" +
"      <X xd:script=\"0..; ref X\"/>\n" +
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<X a=\"a1\"><X a=\"a2\"/><X a=\"a3\"/></X>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("X", null));
			xd = compile( // create section
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <X xd:script=\"ref Y\"/>\n" +
"   <Y a=\"string()\">\n" +
"      <X xd:script=\"0..; create from('X'); ref Y\"/>\n" +
"   </Y>\n" +
"</xd:def>").createXDDocument();
			xml = "<X a=\"a1\"><X a=\"a2\"/><X a=\"a3\"/></X>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("X", null));
			xd = compile( // no create section
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <X xd:script=\"ref Y\"/>\n" +
"   <Y a=\"string()\">\n" +
"      <X xd:script=\"0..; ref Y\"/>\n" +
"   </Y>\n" +
"</xd:def>").createXDDocument();
			xml = "<X a=\"a1\"><X a=\"a2\"/><X a=\"a3\"/></X>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("X", null));
			xd = compile( // create section
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"0..; ref X\"/>\n" +
"   </Vehicle>\n" +
"   <X name=\"string()\">\n" +
"      <Part xd:script=\"0..; create from('Part'); ref X\"/>\n" +
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<Vehicle><Part name=\"a1\"><Part name=\"a2\"/><Part name=\"a3\"/></Part></Vehicle>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("Vehicle", null));
			xd = compile( // no create section
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"0..; ref X\"/>\n" +
"   </Vehicle>\n" +
"   <X name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref X\"/>\n" +
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<Vehicle><Part name=\"a1\"><Part name=\"a2\"/><Part name=\"a3\"/></Part></Vehicle>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("Vehicle", null));
		} catch (RuntimeException ex) {fail(ex);}
		clearTempDir(); // delete temporary files.
		resetTester();
	}

////////////////////////////////////////////////////////////////////////////////
// implementation of external methods used in XDefinitions
////////////////////////////////////////////////////////////////////////////////

	private static String objToString(final Object obj) {
		if (obj == null) {
			return "null";
		} else if (obj instanceof ArrayList) {
			ArrayList<?> ar = (ArrayList) obj;
			if (ar.isEmpty()) {
				return "null";
			} else {
				Object o = ar.get(0);
				return o instanceof Node ? ((Node) o).getNodeValue() : o == null ? "null" : o.toString();
			}
		} else if (obj instanceof Node) {
			return ((Node) obj).getNodeValue();
		} else if (obj instanceof NodeList) {
			NodeList nl = (NodeList) obj;
			return nl.getLength() == 0 ? "null" : nl.item(0).getNodeValue();
		} else {
			return obj.toString();
		}
	}

	public static String MyProc(XXElement chkElem, XDValue[] params) {
		String s = "";
		XDValue val = chkElem.getXDContext();
		if (val != null && val.getItemId() == XDValueID.XD_ELEMENT) {
			Element el = val.getElement();
			for (XDValue param : params) {
				s+=objToString(KXpathExpr.evaluate(el,param.stringValue()))+' ';
			}
		}
		return s;
	}
	public static String myProc1(XXElement chkElem, XDValue[] params) {
		String s = "";
		XDValue val = chkElem.getXDContext();
		if (val != null && val.getItemId() == XDValueID.XD_ELEMENT) {
			Element el = val.getElement();
			for (XDValue param : params) {
				s+=objToString(KXpathExpr.evaluate(el,param.stringValue()))+' ';
			}
		}
		return s;
	}
	public static void myProc2(XXNode chkElem, XDValue[] params) {}
	public static void myOutput(XXNode chkElem, XDValue[] params) {
		String s;
		if (params.length == 0) {
			s = "null";
		} else {
			s = "";
			for (int i = 0; i < params.length; i++) {
				if (i > 0) {
					s += ", ";
				}
				s += (params[i].getItemId() == XDValueID.XD_STRING)
					? params[i].stringValue() : "?" + params[i].getItemId();
			}
		}
		chkElem.getElement().setAttribute("myOutput", s);
	}
	public static void hasElement(XXElement chkElem, XDValue[] params) {
		String subElemXpath = params[1].stringValue();
		String s = chkElem.getElement().getAttribute("E");
		if (s == null) {
			s = "";
		} else if (s.length() > 0) {
			s += ", ";
		}
		s += subElemXpath;
		chkElem.getElement().setAttribute("E",s);
	}
	public static void setUserResult(XXElement chkElem, XDValue[] params) {
		chkElem.getXDDocument().setUserObject(KXmlUtils.nodeToString(chkElem.getElement(),false));
		chkElem.forgetElement();
	}
	public static void mySetAttrFromXpath(XXData xdata, String xpath) {
		Element el = (Element) xdata.getUserObject();
		XDContainer dc = XDFactory.createXDContainer(DefXPathExpr.evaluate(el, xpath));
		for (int i = 0; i < dc.getXDItemsNumber(); i++) {
			XDValue v = dc.getXDItem(i);
			if (v.getItemId() == XDValueID.XD_ELEMENT) {
				Element e = v.getElement();
				e.setAttributeNS(xdata.getNodeURI(), xdata.getNodeName(), xdata.getTextValue());
				return;
			}
		}
	}
	public static XDContainer ctx(long l) {
		XDContainer c = XDFactory.createXDContainer();
		while (l-- > 0) {
			c.addXDItem(XDFactory.createXDElement(KXmlUtils.newDocument().createElementNS(null, "b")));
		}
		return c;
	}
	public static XDContainer ctx() {
		XDContainer c = XDFactory.createXDContainer();
		c.addXDItem(new DefElement(KXmlUtils.newDocument().createElementNS(	null, "c")));
		return c;
	}
	public static XDContainer getDataDoc(XXElement c,
		XDContainer odes, String id) {
		if(odes.getXDItemsNumber() > 0) {
			return odes;
		}
		String xm = "<x c='77'/>";
		if (id!=null && id.length() > 0) {
			xm = (Integer.parseInt(id) % 2) == 0 ? "<x c='88'/>" : "<y c='99'/>";
		}
		Element el = KXmlUtils.parseXml(xm).getDocumentElement();
		return XDFactory.createXDContainer(el);
	}
	/**External method just throws exception.
	 * @throws RuntimeException always throws.
	 */
	public static void throwExc() {throw new RuntimeException("thrown exception");}
	/** this is external method to be called
	 * @param param value to which is added constant 123456.
	 * @return value of argument to which is added constant 123456.
	 */
	public static long myMethod(long param) {return param + 123456;}
	public static Element getChybyElement(XXElement chkEl) {
		Document doc = chkEl.getElement().getOwnerDocument();
		Element elChyby = doc.createElement("a");
		Element elChyba = doc.createElement("b");
		elChyba.setAttribute("Kod", "123");
		elChyba.setAttribute("Typ", "T");
		elChyby.appendChild(elChyba);
		elChyba = doc.createElement("b");
		elChyba.setAttribute("Kod", "456");
		elChyba.setAttribute("Typ", "T");
		elChyby.appendChild(elChyba);
		return elChyby;
	}

	final public static String xx(final XXElement x, final XDContainer y) {
		return y.getXDText();
	}

	// testing different null in create data values
	public static String nulString(XXData xx) {return null;}
	public static Long nulLong(XXData xx) {return null;}
	public static Float nulFloat(XXData xx) {return null;}
	public static SDatetime nulDatetime(XXData xx) {return null;}
	public static SDuration nulDuration(XXData xx) {return null;}
	public static XDContainer nulContainer(XXData xx) {return null;}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}