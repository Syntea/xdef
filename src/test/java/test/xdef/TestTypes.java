package test.xdef;

import builtools.XDTester;
import org.xdef.XDConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.model.XMData;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.w3c.dom.Element;
import org.xdef.XDContainer;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDParserAbstract;
import org.xdef.XDValue;
import org.xdef.impl.parsers.XSAbstractParser;
import org.xdef.impl.parsers.XSParseDecimal;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.xml.KXmlUtils;

/** Test of types, AnyValue and null in X-script.
 * @author Vaclav Trojan
 */
public final class TestTypes extends XDTester {

	public TestTypes() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		Element el;
		String xdef, xml, s;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
"  String s = null;\n"+
"  String t = null;\n"+
"  String u = null;\n"+
"  Element v = null;\n"+
"  Container w = null;\n"+
"  void x(Container c) {\n"+
"    if (t == null) {\n"+
"      t += 't';\n"+
"    }\n"+
"    if (v == null) {\n"+
"      t += 'u';\n"+
"    }\n"+
"    AnyValue a = c.getNamedItem('a');\n"+
"    if (a.valueType() == $INT) {\n"+
"      t += 'ID' + (int) a;\n"+
"    } else {\n"+
"      t += 'ERRID';\n"+
"    }\n"+
"    t+=c.getNamedItem('a').toString();\n"+
"    if (s == null) {\n"+
"      t += 'OK1';\n"+
"    } else {\n"+
"      t += 'ERR1';\n"+
"    }\n"+
"    if (s != null) {\n"+
"      t += 'ERR2';\n"+
"    } else {\n"+
"      t += 'OK2';\n"+
"    }\n"+
"    if (null == s) {\n"+
"      t += 'OK3';\n"+
"    } else {\n"+
"      t += 'ERR3';\n"+
"    }\n"+
"    if (null != s) {\n"+
"      t += 'ERR4';\n"+
"    } else {\n"+
"      t += 'OK4';\n"+
"    }\n"+
"    t += v != null ? null : '!=';\n"+
"    t += v == null ? '==' : null;\n"+
"    while (true) {\n"+
"      t += s;\n"+
"      break;\n"+
"    }\n"+
"  }\n"+
"  void y(Container c) {\n"+
"    AnyValue v = c.item(0);\n"+
"    t+= c.getItemType(0) == $STRING;\n"+
"    t+= (v.valueType() == $STRING);\n"+
"    t+= (String) v;\n"+
"  }\n"+
"</xd:declaration>\n"+
"<a xd:script='finally {x(%a=11); y([\"A\"])}'/>"+
"</xd:def>\n";
			xd = compile(xdef).createXDDocument();
			xd.xparse("<a/>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq("tuID1111OK1OK2OK3OK4!===truetrueA",
				xd.getVariable("t").toString());
			assertTrue(xd.getVariable("u").isNull());
			assertTrue(xd.getVariable("u").stringValue() == null);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
"  Decimal dc = null;\n"+
"  Datetime dt = null;\n"+
"  Duration du = null;\n"+
"  Element el = null;\n"+
"  String t = null;\n"+
"  void x() {\n"+
"      t += dc == null;\n"+
"      t += dt == null;\n"+
"      t += du == null;\n"+
"      t += el == null;\n"+
"      AnyValue a = el;\n"+
"      t += a.typeName();\n"+
"      a = 1;\n"+
"      t += a.typeName();\n"+
"      a = null;\n"+
"      t += a.typeName();\n"+
"      t += ((AnyValue) 1.5).typeName();\n"+
"  }\n"+
"</xd:declaration>\n"+
"<a xd:script='finally x();'/>"+
"</xd:def>\n";
			xd = compile(xdef).createXDDocument();
			xd.xparse("<a/>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq("truetruetruetrueElementintfloat",
				xd.getVariable("t").toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
"  AnyValue a = null;\n"+
"  Decimal dc = null;\n"+
"  Datetime dt = null;\n"+
"  Duration du = null;\n"+
"  Element el = null;\n"+
"  String t = null;\n"+
"  void x() {\n"+
"      t += a;\n"+
"      t += dc;\n"+
"      t += dt;\n"+
"      t += du;\n"+
"      t += el;\n"+
"  }\n"+
"</xd:declaration>\n"+
"<a xd:script='finally x();'/>"+
"</xd:def>\n";
			xd = compile(xdef).createXDDocument();
			xd.xparse("<a/>", reporter);
			assertNoErrorwarnings(reporter);
//			assertEq("nullnullnullnullnull", xd.getVariable("t").toString());
			assertEq("", xd.getVariable("t").toString());

///////////// Check date limits ////////////////////////////////////////////////
			setProperty(XDConstants.XDPROPERTY_MINYEAR, "1916");
			setProperty(XDConstants.XDPROPERTY_MAXYEAR, "2216");
			setProperty(XDConstants.XDPROPERTY_SPECDATES,
				"3000-12-31,3000-12-31T00:00:00,3000-12-31T23:59:59");
			xdef =  //test limits in datetime
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
" <b xd:script='+'\n"+
"  a=\"xdatetime('yyyyMMdd', 'd.M.y')\" b=\"xdatetime('yyyyMMddHHmmss')\" />\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b a='22151201' b='22151201235959'/></a>";
			assertEq("<a><b a=\"1.12.2215\" b=\"22151201235959\"/></a>",
				parse(xp, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b a='25001231' b='25001231235959'/></a>";
			parse(xp, null, xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a><b a='30001231' b='30001231235959'/></a>";
			el = parse(xp, null, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a><b a=\"31.12.3000\" b=\"30001231235959\"/></a>");
			xml = "<a><b a='30001231' b='30001231000000'/></a>";
			assertEq("<a><b a=\"31.12.3000\" b=\"30001231000000\"/></a>",
				parse(xp, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b a='30001231' b='30001231100000'/></a>";
			assertEq("<a><b a=\"31.12.3000\" b=\"30001231100000\"/></a>",
				parse(xp, null, xml, reporter));
			assertTrue(reporter.errorWarnings(), "Error not reported");
			resetProperties();
////////////////////////////////////////////////////////////////////////////////

			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.checkDateLegal(false);
			xml = "<a><b a='20151201' b='20151201235959'/></a>";
			assertEq("<a><b a=\"1.12.2015\" b=\"20151201235959\"/></a>",
				parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b a='25001231' b='25001231235959'/></a>";
			xd = xp.createXDDocument();
			xd.checkDateLegal(false);
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a><b a='30001231' b='30001231235959'/></a>";
			xd = xp.createXDDocument();
			xd.checkDateLegal(false);
			assertEq("<a><b a=\"31.12.3000\" b=\"30001231235959\"/></a>",
				parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(xdef);
			xml = "<a><b a='20151201' b='20151201235959'/></a>";
			assertEq("<a><b a=\"1.12.2015\" b=\"20151201235959\"/></a>",
				parse(xp, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b a='30001231' b='30001231235959'/></a>";
			assertEq("<a><b a=\"31.12.3000\" b=\"30001231235959\"/></a>",
				parse(xp, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			// name of type equals to name of internal type method
			xdef = 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <xd:declaration scope='local'> type string string(1,2); </xd:declaration>\n"+
"<a x='required string'>\n"+
" required string;\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='x'>x</a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='xxx'>xxx</a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <xd:declaration>\n"+
"  type int string(1,2);\n"+
"  int i;\n"+
" </xd:declaration>\n"+
"<a x='required int'>\n"+
" required int;\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='x'>x</a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='xxx'>xxx</a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);			
		} catch (Exception ex) {fail(ex);}
		try { //element type
			xdef =
"<xd:def root='a' xmlns:xd='" + _xdNS + "'>\n"+
"\n"+
"  <xd:declaration><![CDATA[\n"+
"    void x() {\n"+
"	   Element e = xparse(\"<e xmlns:x='x' a='A' x:a='xA'><b/>t1</e>\");\n"+
"	   Element f = new Element('f');\n"+
"	   f.setAttr('f', 'F');\n"+
"	   f.setAttr('x','x:f', 'xF');\n"+
"	   e.addElement(f);\n"+
"	   e.addText('t2');\n"+
//"      outln(e.toString());\n"+
"      outln(e.getAttr('a'));\n"+
"      outln(e.getAttr('x','a'));\n"+
"      outln(e.getAttr('x','x:a'));\n"+
"      outln(e.getText());\n"+
"    }\n"+
"  ]]></xd:declaration>\n"+
"\n"+
"  <a xd:script='finally x();'/>\n"+
"</xd:def>";
			strw = new StringWriter();
			parse(xdef, "", "<a/>", reporter, strw, null, null);
			strw.close();
			assertNoErrorwarnings(reporter);
			s = "A\nxA\nxA\nt1t2\n";
			assertEq(s, strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"   <![CDATA[\n"+
"    Element $a;\n"+
"    Element e = xparse(\"<x a='A\nB'><y/>text<z/></x>\");\n"+
"    Element e1 = new Element('e1');\n"+
"    Element e2 = new Element('nsuri', 'e2');\n"+
"    Element e3 = new Element('nsuri', 'x:e2');\n"+
"    void test() {\n"+
"      outln(e.toString());\n"+
"      outln(e.toString(true));\n"+
"      outln(e.getText());\n"+
"      outln(e1.toString());\n"+
"      outln(e2.toString());\n"+
"      outln(e3.toString());\n"+
"    }\n"+
"   ]]>\n"+
"  </xd:declaration>\n"+
"  <a xd:script='onStartElement {$a=getElement(); outln($a.toString());}\n"+
"                finally test()'>\n"+
"    <b/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef); //vytvořeni ze zdroju
			strw = new StringWriter();
			parse(xp, "", "<a><b/></a>", reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq(strw.toString(),
				"<a/>\n"+
				"<x a=\"A B\"><y/>text<z/></x>\n"+
				"<x a=\"A B\">\n"+
				"  <y/>\n"+
				"  text\n"+
				"  <z/>\n"+
				"</x>\n"+
				"text\n"+
				"<e1/>\n"+
				"<e2 xmlns=\"nsuri\"/>\n"+
				"<x:e2 xmlns:x=\"nsuri\"/>\n");
			xdef = // Parse, ParseResult
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"  void x() {\n"+
"    ParseResult t;\n"+
"    Parser s = decimal; \n"+
"    t = s.parse('2');\n"+
"    Decimal i = t.getValue();\n"+
"    if (2 != i) error('E01');\n"+
"    if (i != 2) error('E02');\n"+
"    if (i != (Decimal) 2) error('E03');\n"+
"    if ((Decimal) 2 != i) error('E04');\n"+
"    if ((Decimal) 2.0 != i) error('E05');\n"+
"    if (i != (Decimal) 2.0) error('E06');\n"+
"    Decimal j = 2;\n"+
"    if (i != j) error('E07');\n"+
"    j = 2.0;\n"+
"    if (i != j) error('E08');\n"+
"    if (!t.matches()) error('E09');\n"+
"    j = 2.0;\n"+
"    boolean b = t;\n"+
"    if (!b) error('E10');\n"+
"    t = boolean().parse('false');\n"+
"    if (!t) error('E11');\n"+
"    b = t.getValue();\n"+
"    if (b) error('E11');\n"+
"    t = int().parse('9');\n"+
"    int k = t.getValue();\n"+
"    if (9 != k) error('E12');\n"+
"    t = float().parse('3.14');\n"+
"    float f = t.getValue();\n"+
"    if (f != 3.14) error('E15');\n"+
"    t = dateTime().parse('2000-01-02T11:30:00Z');\n"+
"    Datetime dt = t.getValue();\n"+
"    if (dt != new Datetime('2000-01-02T11:30:00Z')) error('E16');\n"+
"    t = duration().parse('PT9999H');\n"+
"    Duration du = t.getValue();\n"+
"    if (du != new Duration('PT9999H')) error('E18');\n"+
"  }\n"+
"</xd:declaration>\n"+
"<A xd:script='finally x()'/>\n"+
"</xd:def>";
			parse(xdef, "", "<A/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef = //uniqueSet
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"   uniqueSet id1 {key:int()};" +
"  </xd:declaration>\n"+
"  <a x='id1.key.ID' y='id1.key.ID'>\n"+
"    <b xd:script='+'>required id1.key.IDREF</b>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a x='1' y='23'><b>1</b><b>23</b></a>";
			parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			xdef = // test getParserParams
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='string(2,3)' b='int(2,3)' c='date(\"2000-01-01\",\"2010-01-01\")'>\n"+
"    xdatetime('y-M-d','d.M.y')\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			XDContainer c = ((XMData) xp.findModel("#a/@a")).getParseParams();
			assertEq(2, c.getXDNamedItemValue("minLength").intValue());
			assertEq(3, c.getXDNamedItemValue("maxLength").intValue());
			c = ((XMData) xp.findModel("#a/@b")).getParseParams();
			assertEq(2, c.getXDNamedItemValue("minInclusive").intValue());
			assertEq(3, c.getXDNamedItemValue("maxInclusive").intValue());
			c = ((XMData) xp.findModel("#a/@c")).getParseParams();
			assertEq("2000-01-01",
				c.getXDNamedItemValue("minInclusive").toString());
			assertEq("2010-01-01",
				c.getXDNamedItemValue("maxInclusive").toString());
			c = ((XMData) xp.findModel("#a/text()")).getParseParams();
			assertEq("y-M-d", c.getXDNamedItemValue("format").stringValue());
			assertEq("d.M.y", c.getXDNamedItemValue("outFormat").stringValue());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = // external method with key params
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method boolean test.xdef.TestTypes.kp(XXNode, XDValue[]);"+
"</xd:declaration>\n"+
"<a a='kp(1,5,%totalDigits=1,%enumeration=1,%pattern=\"\\\\d\")'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='1'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // test combine seq and key params
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='string(2,%maxLength=3)'/>\n"+
"</xd:def>";
			parse(xdef, "", "<a a='abc'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a a='abcd'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='string(3,%maxLength=3)' b='int(3, %maxInclusive=3)' />\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a a='a12' b='3'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a a='a1' b ='2'/>", reporter);
			assertFalse(reporter.getErrorCount() != 2,reporter.printToString());
			parse(xp, null, "<a a='a124' b ='4'/>", reporter);
			assertFalse(reporter.getErrorCount() != 2,reporter.printToString());
			xdef = //decimal
"<xd:def xmlns:xd='" + _xdNS + "' root='a'><a a='decimal(0,1)'/></xd:def>";
			compile(xdef).createXDDocument();
			xml = "<a a='1'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2'/>";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // decimal, base decimal
"<xd:def xmlns:xd='" + _xdNS + "' name = 'test' root='a'>\n"+
"<a a='decimal(%base=decimal(%minInclusive=0),%minInclusive=1," +
"      %maxInclusive=5,%totalDigits=1,%fractionDigits=0," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])' />\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='1'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union - declared type parser
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> type t union(%item=[decimal,boolean]);</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xml = "<a a='true' />";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2' />";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='xyz' />";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' 1 2' />";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union - declared simplified version
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  type x union(%item = [int(1, 2), boolean]);\n"+
"  type t x;\n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xml = "<a a='true'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='3'/>";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a='xyz'/>";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' 1 2'/>";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union - declared parser
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  boolean x() {if (int(1, 2)) return true; return boolean();}\n"+
"  type t x;\n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xml = "<a a='true'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='3'/>";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union - declared parser
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  boolean t() { if (int(1, 2)) return true; return boolean(); }\n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xml = "<a a='true'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2'/>";
			parse(compile(xdef), "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='3'/>";
			parse(compile(xdef), "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union, declared items, base.
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = string(%enumeration=['true', '2', 'xyz']); \n"+
"  Parser t = union(%base=s, %item=[decimal,boolean]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xml = "<a a='true' />";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='xyz' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' 1 2' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' 1 2' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union with base
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = string(%enumeration='xyz'); \n"+
"  Parser t = union(%item=[decimal, boolean, s ]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='true' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='xyz' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' 1 2' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // model variable.
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A xd:script='var type p union(%item = [int(1, 3), boolean]);'>\n"+
"  <a xd:script='occurs *;'><b x='p'/></a>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><a><b x='1'/></a><a><b x='3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef = // model variable.
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A xd:script='var {Parser p = union(%item = [int(1, 3), boolean]);}'>\n"+
"  <a xd:script='occurs *;'><b x='p'/></a>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><a><b x='1'/></a><a><b x='3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);		
			xdef = // union with the a list of same items
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = list(%item=eq('abc'), %minLength=1, %maxLength=2); \n"+
"</xd:declaration>\n"+
" <a a='required s'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='abc' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' abc' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' abc ' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' abc abc ' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' abc abc abc' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), reporter.printToString());
			xml = "<a a=' efg ' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a='' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = //union with the a list item
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = list(%item=decimal, %enumeration=[1,2,[3,4]]); \n"+
"  Parser t = union(%item=[boolean, s]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a=' true' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' 1' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' 2 ' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='3 4' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' 7 ' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' true 1 ' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // union with the sequence item
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"    %enumeration=['true 1', 'false 2']); \n"+
"  Parser t = union(%item=[decimal,boolean, s]); \n"+
"</xd:declaration>\n"+
" <a a='required t; options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xml = "<a a=' true ' />";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=' 2 ' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='   true        1    ' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='   true        2    ' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' xyz ' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' 1 2' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // gYear - invoked in if command (see method "check")
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a = \"required {return gYear(%minInclusive='1999',\n"+
"   %maxInclusive='2000');}\"/>\n"+
"</xd:def>";
			xml = "<a a='1999'/>";
			parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2000'/>";
			parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='2010'/>";
			parse(xdef, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a=' sequence ( %item = [ decimal ( %maxInclusive = 5 ) ] ) '/>\n"+
"</xd:def>";
			xml = "<a a=' 1' />";
			parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xdef = // sequence
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
" xmlns:xs='" + _xdNS + "' root='a'>\n"+
" <a a='sequence(%item=[decimal(%maxInclusive=5), int(%minInclusive=0)])'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a=' 1 2 3' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xml = "<a a=' 1 2' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xdef = // sequence with enumeration
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"    %enumeration=['true 1', 'false 2']); \n"+
"</xd:declaration>\n"+
" <a a='required s'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='true 1' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='false 2' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='false 1' />";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");		
			xdef =  // string whiteSpace=preserve (option trimAttr)
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser p = string(%minLength=1, %whiteSpace='preserve');\n"+
"</xd:declaration>\n"+
"<a a=\"optional p;\" b=\"optional string(0,100);\"/>\n"+
"</xd:def>";
			xml = "<a a=' ' b=' '/>";
			el = parse(xdef, "", xml, reporter);
			assertErrors(reporter);
			assertEq(el, "<a a='' b=''/>");
			xdef = // string whiteSpace=preserve (option noTrimAttr)
"<xd:def xmlns:xd='" + _xdNS + "' root='a' xd:script='options noTrimAttr'>\n"+
"<xd:declaration>\n"+
"  Parser p = string(%minLength=1, %whiteSpace='preserve');\n"+
"</xd:declaration>\n"+
"<a a=\"optional p;\" b=\"optional string(0,100);\"/>\n"+
"</xd:def>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a a=' ' b=' '/>");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' xd:script='options noTrimAttr'>\n"+
"<xd:declaration>\n"+
"  Parser p = string(%minLength=0, %whiteSpace='collapse');\n"+
"</xd:declaration>\n"+
"<a a=\"optional p;\" b=\"optional string(0,100);\"/>\n"+
"</xd:def>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a a='' b=' '/>");
			
		xdef = // Test of methods NCname(), QName(), QnameURI()
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:ws='abc' root='ws:message'>\n"+
"  <xd:declaration>\n"+
"    external method String test.xdef.TestTypes.tst(XXElement, String);\n"+
"  </xd:declaration>\n"+
"  <ws:message xd:script='occurs 0..'\n"+
"     name='required NCName()'>" +
"     <xd:choice>" +
"       <ws:part xd:script=\"occurs 0..; match @element\"" +
"          name =\"required NCName();" +
"          finally {String t, s=getText(); t=getQnamePrefix(s); out(t+'/' +" +
"          getQnameLocalpart(s) + '/' + getNamespaceURI(t) + ';' +" +
"          getQnameURI(s) + ';' + tst(s));}\"" +
"          element             =\"required QNameURI();" +
"          finally {String t, s=getText(); t=getQnamePrefix(s); out(t+'/' +" +
"          getQnameLocalpart(s) + '/' + getNamespaceURI(t) + ';' +" +
"          getQnameURI(s) + ';' + tst(s));}\"/>" +
"       <ws:part xd:script=\"occurs 0..; match @type\"" +
"          name=\"required NCName();\"" +
"          type=\"required QName();\"/>" +
"     </xd:choice>" +
"  </ws:message>" +
"</xd:def>\n";
		xp = compile(xdef);
		strw = new StringWriter();
		xml =
"<message name=\"GetEndorsingBoarderRequest\"\n"+
"  xmlns='abc'\n"+
"  xmlns:esxsd =\"http://schemas.snowboard-info.com/EndorsementSearch.xsd\">" +
"  <part name=\"body\" element=\"esxsdX:GetEndorsingBoarder\"/>" +
"</message>";
		parse(xp, "", xml, reporter, strw, null, null);
		s = strw.toString();
		assertFalse(s.indexOf("/body/abc;abc;abc") < 0 ||
			s.indexOf("/abc;abc;abc") < 0, s);
		if (reporter.getErrorCount() == 0) {
			fail("error not reported");
		} else if (reporter.getErrorCount() != 1) {
			fail(reporter.printToString());
		} else if (!"XDEF554".equals(
			reporter.getReport().getMsgID()) &&
			!"XDEF515".equals(reporter.getReport().getMsgID())) {
			fail(reporter.getReport().toString());
		}
		strw = new StringWriter();
		xml =
"<message name=\"GetEndorsingBoarderRequest\"" +
"  xmlns='abc'" +
"  xmlns:esxsd =\"http://schemas.snowboard-info.com/EndorsementSearch.xsd\">" +
"  <part   name=\"body\" element=\"esxsd:GetEndorsingBoarder\"/>" +
"</message>";
		parse(xp, "", xml, reporter, strw, null, null);
		s = strw.toString();
		assertFalse(s.indexOf("/body/abc;abc;abc") < 0 ||
			s.indexOf("esxsd/GetEndorsingBoarder/" +
			"http://schemas.snowboard-info.com/EndorsementSearch.xsd;" +
			"http://schemas.snowboard-info.com/EndorsementSearch.xsd;" +
			"http://schemas.snowboard-info.com/EndorsementSearch.xsd") < 0,
			s);
			assertNoErrorwarnings(reporter);
			setChkSyntax(false);
			xdef = // expressions
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='int | string; finally out(int | string)'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='1'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("true", strw.toString());
			assertNoErrors(reporter);
			xml = "<a a='x'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("true", strw.toString());
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='int || string; finally out(int || string)'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='1'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("true", strw.toString());
			assertNoErrors(reporter);
			xml = "<a a='x'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("true", strw.toString());
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='int AND string; finally out(int AND string)'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='1'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("true", strw.toString());
			assertNoErrors(reporter);
			xml = "<a a='x'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("false", strw.toString());
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='int AAND string; finally out(int AAND string)'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='1'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("true", strw.toString());
			assertNoErrors(reporter);
			xml = "<a a='x'/>";
			strw = new StringWriter();
			parse(xp, null, xml, reporter, strw, null, null);
			assertEq("false", strw.toString());
			assertErrors(reporter);
			xdef = // check Parser - combination of sequential and key parameters
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a='decimal(0,2,%totalDigits=3,%fractionDigits=2,%enumeration=[1.21])'\n"+
"     b='decimal(-2,2,%totalDigits=3,%fractionDigits=2)' c='dec(3,2)'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a a='+3.21' b='-1.21' c='+0.00'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			xml = "<a a='+1.21' b='-3.21' c='+0.00'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			xml = "<a a='+1.21' b='-1.21' c='+12.45'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			xml = "<a a='+1.21' b='-1.21' c='+0.0'/>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a a='+1.21' b='-1.21' c='+1.21'/>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a a='+1.21' b='-1.21' c='-1.21'/>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+
"  <a x=\"list(string, %length = 2);\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='1 2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			xml = "<a x='1 2 3'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> Parser p = string; </xd:declaration>\n"+
"  <a x=\"list(p, %minLength = 2, %maxLength = 3);\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='1 2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>int i = 1, j = 2; Parser p = int(i,j);</xd:declaration>\n"+
"  <a x=\"list(p, %minLength = 2, %maxLength = 3);\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='1 2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			xml = "<a x='1 9'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			xdef = // check element variable
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+
" <a xd:script=\"var {String x;}\">\n"+
"   <b xd:script='+'"+
"     x=\"empty();\n"+
"       onTrue x = '1999';\n"+
"       finally {\n"+
"         Parser p = gYear(%minInclusive=1999);\n"+
"         ParseResult r = p.parse(x);\n"+
"         if (!r.matches()) {\n"+
"           error('E001','Check failed: &amp;{p}', '&amp;{p}' + x);\n"+
"         }\n"+
"       }\">\n"+
"   </b>\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a><b x=''></b></a>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a xd:script=\"var final String z='1999';\">\n"+
"  <b>\n"+
"   <c xd:script='+'\n"+
"     x=\"empty();\n"+
"       finally {\n"+
"         Parser p = gYear(%minInclusive=1999);\n"+
"         ParseResult r = p.parse(z);\n"+
"         if (!r.check()) {\n"+
"           error('E001','Check failed: &amp;{p}', '&amp;{p}' + z);\n"+
"         }\n"+
"       }\">\n"+
"   </c>\n"+
"  </b>\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a><b><c x=''/></b></a>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = // parser type
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <xd:declaration>\n"+
"    external method XDParser test.xdef.TestTypes.getMyParser();\n"+
"  </xd:declaration>\n"+
"  <a a='getMyParser()'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a a='abc'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='cde'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <xd:declaration>\n"+
"    external method XDParser test.xdef.TestTypes.getMyParser();\n"+
"    Parser p = getMyParser();\n"+
"  </xd:declaration>\n"+
"  <a a='p'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a a='abc'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a='cde'/>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			xdef = // external variable
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"<xd:declaration> external Parser p; </xd:declaration>\n"+
"  <a a='p'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a a='abc'/>";
			xd = xp.createXDDocument();
			xd.setVariable("p", getMyParser());
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument();
			xd.setVariable("p", getMyParser());
			xml = "<a a='cde'/>";
			parse(xd, xml, reporter);
			assertTrue(reporter.errorWarnings());
			int year = new GregorianCalendar().get(Calendar.YEAR);
			setProperty("xdef.minyear", String.valueOf(year - 200));
			setProperty("xdef.maxyear", String.valueOf(year + 200));
			setProperty("xdef.specdates",
				"3000-12-31,3000-12-31T00:00:00,3000-12-31T23:59:59");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'><a a='int()' b='int()'/></xd:def>";
			xp = compile(xdef);
			xml ="<a a='2147483647' b='-2147483648' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml ="<a a='2147483648' b='-2147483649' />";
			parse(xp, "", xml, reporter);
			assertEq(2, reporter.getErrorCount());

			xdef = // check xml schema types
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name='SchemaTypes'>\n"+
" <xd:declaration>\n"+
"   type ID ID();\n"+
"   type normalizedString normalizedString();\n"+
"   type tokens NMTOKENS();\n"+
"   type language language();\n"+
"   type Qname QName();\n"+
"   type NCname NCName();\n"+
"   type duration duration();\n"+
"   type dateTime xdatetime('yyyy-M-dTH:m[:s][ Z]');\n"+
"   type date xdatetime('yyyy-M-d');\n"+
"   type time xdatetime('H:m[:s]');\n"+
"   type gYearMonth xdatetime('yyyy-M');\n"+
"   type gYear xdatetime('yyyy');\n"+
"   type gMonthDay xdatetime('M-d');\n"+
"   type gDay xdatetime('d');\n"+
"   type gMonth xdatetime('M');\n"+
"   type base64Binary base64Binary(4);\n"+
"   type hexBinary hexBinary(3);\n"+
" </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:script='options preserveAttrWhiteSpaces,noTrimAttr'\n"+
" xd:name='a' xd:root='a'>\n"+
"<a string = \"required string(); onFalse out('string');\"\n"+
" normalizedString = \"required normalizedString();\n"+
"                      onFalse out('normalizedString');\"\n"+
" tokens = \"required tokens(); onFalse out('token');\"\n"+
" language = \"required language(); onFalse out('language');\"\n"+
" Qname = \"required Qname(); onFalse out('name');\"\n"+
" NCName = \"required NCname(); onFalse out('NCName');\"\n"+
" ID = \"required ID(); onFalse out('ID');\"\n"+
" ID1 = \"required ID(); onFalse out('ID1');\"\n"+
" IDREF = \"required IDREF(); onFalse out('IDREF');\"\n"+
" IDREFS = \"required IDREFS(); onFalse out('IDREFS');\"\n"+
" duration = \"required duration(); onFalse out('duration');\"\n"+
" dateTime = \"required dateTime(); onFalse out('dateTime');\"\n"+
" date = \"required date(); onFalse out('date');\"\n"+
" time = \"required time(); onFalse out('time');\"\n"+
" gYearMonth = \"required gYearMonth(); onFalse out('gYearMonth');\"\n"+
" gYear = \"required gYear(); onFalse out('gYear');\"\n"+
" gMonthDay = \"required gMonthDay(); onFalse out('gMonthDay');\"\n"+
" gDay = \"required gDay(); onFalse out('gDay');\"\n"+
" gMonth = \"required gMonth(); onFalse out('gMonth');\"\n"+
" boolean = \"required boolean(); onFalse out('boolean');\"\n"+
" base64Binary = \"required base64Binary(); onFalse out('base64Binary');\"\n"+
" hexBinary = \"required hexBinary(); onFalse out('hexBinary');\"\n"+
" float = \"required float(); onFalse out('float');\"\n"+
"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml =
"<a\n"+
" string = ' a		 b c   '\n"+
" normalizedString = ' a		 b c   '\n"+
" tokens = ' a		 b c '\n"+
" language = ' cs   '\n"+
" Qname = ' cs   '\n"+
" NCName = ' cs   '\n"+
" ID = ' cs   '\n"+
" ID1 = ' cs1   '\n"+
" IDREF = ' cs   '\n"+
" IDREFS = ' cs    cs1   '\n"+
" duration = 'T1H'\n"+
" dateTime = '1998-1-1T19:30'\n"+
" date = '1998-1-1'\n"+
" time = '19:30:1'\n"+
" gYearMonth = '1998-1'\n"+
" gYear = '1998'\n"+
" gMonthDay = '1-1'\n"+
" gDay = '1'\n"+
" gMonth = '1'\n"+
" boolean = '0'\n"+
" base64Binary = '01abcQ=='\n"+
" hexBinary = '01abcd'\n"+
" float = '1.5e-7'\n"+
"/>\n";
			xp = compile(xdef);
			el = parse(xp, "a", xml, reporter, null);
			assertNoErrors(reporter);
			assertEq(el.getAttribute("string"), " a   b c   ");
			assertEq(el.getAttribute("normalizedString"), " a   b c   ");
			assertEq(el.getAttribute("tokens"), "a b c");
			assertEq(el.getAttribute("language"), "cs");
			assertEq(el.getAttribute("Qname"), "cs");
			assertEq(el.getAttribute("NCName"), "cs");
			assertEq(el.getAttribute("ID"), "cs");
			assertEq(el.getAttribute("ID1"), "cs1");
			assertEq(el.getAttribute("IDREF"), "cs");
			assertEq("cs cs1", el.getAttribute("IDREFS"));
			assertEq("1998-1-1T19:30", el.getAttribute("dateTime"));
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	public static String tst(XXElement xe, String s) {
		return KXmlUtils.getNSURI(KXmlUtils.getQNamePrefix(s), xe.getElement());
	}

	public static boolean kp(XXNode chkel, XDValue[] params) {
		XDContainer c = XDFactory.createXDContainer((XDContainer) params[2]);
		c.setXDNamedItem("minInclusive", params[0]);
		c.setXDNamedItem("maxInclusive", params[1]);
		try {
			XSAbstractParser d = new XSParseDecimal();
			d.setNamedParams(null, c);
			return !d.check(null, chkel.getXMLNode().getNodeValue()).errors();
		} catch (Exception ex) {
			chkel.error("", ex.getMessage());
			return false;
		}
	}

	public static XDParser getMyParser() {
		return new XDParserAbstract() {
			@Override
			public int getLegalKeys() {return 0;}
			@Override
			public void parseObject(XXNode xnode, XDParseResult p) {
				p.isSpaces();
				if (!p.isToken("abc")) {
					p.error("E000", "Chyba");
				}
				p.isSpaces();
				if (!p.eos()) {
					p.error("E000", "Chyba");
				}
			}
			@Override
			public String parserName() {return "myParser";}
		};
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}

}