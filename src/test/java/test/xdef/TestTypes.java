/*
 * File: TestTypes.java
 * Copyright 2011 Syntea.
 *
 * This file may be copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt kopirovan, modifikovan a siren pouze v souladu
 * s textem prilozeneho souboru LICENCE.TXT, ktery obsahuje specifikaci
 * prislusnych prav.
 */
package test.xdef;

import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.model.XMData;
import java.io.StringWriter;
import org.w3c.dom.Element;
import cz.syntea.xdef.XDContainer;

/** Test of types, AnyValue and null in X-script.
 * @author Vaclav Trojan
 */
public final class TestTypes extends Tester {

	public TestTypes() {
		super();
/*#if DEBUG*/
		setChkSyntax(true);
		setGenObjFile(true);
/*#end*/
	}

	@Override
	/** Run test and print error information. */
	final public void test() {
		XDPool xp;
		XDDocument xd;
		Element el;
		String xdef, xml, s;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root = 'a'>\n"+
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
"<xd:def xmlns:xd='" + XDEFNS + "' root = 'a'>\n"+
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
"<xd:def xmlns:xd='" + XDEFNS + "' root = 'a'>\n"+
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
			assertEq("nullnullnullnullnull",
				xd.getVariable("t").toString());
			xdef =  //test limits in datetime
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
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
			assertTrue(reporter.errorWarnings(), "Error not recognized");
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
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			setProperty(XDConstants.XDPROPERTY_MINYEAR,
				String.valueOf(Integer.MIN_VALUE));
			setProperty(XDConstants.XDPROPERTY_MAXYEAR,
				String.valueOf(Integer.MAX_VALUE));
			setProperty(XDConstants.XDPROPERTY_SPECDATES, "");
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
		} catch (Exception ex) {fail(ex);}
		resetProperties();
		try { //element type
			xdef =
"<xd:def root='a' xmlns:xd='" + XDEFNS + "'>\n"+
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
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
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
"<xd:def xmlns:xd='" + XDEFNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"  void x() {\n"+
"    ParseResult t;\n"+
"    Parser s = xs:decimal; \n"+
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
"    t = xs:boolean().parse('false');\n"+
"    if (!t) error('E11');\n"+
"    b = t.getValue();\n"+
"    if (b) error('E11');\n"+
"    t = xs:int().parse('9');\n"+
"    int k = t.getValue();\n"+
"    if (9 != k) error('E12');\n"+
"    t = xs:float().parse('3.14');\n"+
"    float f = t.getValue();\n"+
"    if (f != 3.14) error('E15');\n"+
"    t = xs:dateTime().parse('2000-01-02T11:30:00Z');\n"+
"    Datetime dt = t.getValue();\n"+
"    if (dt != new Datetime('2000-01-02T11:30:00Z')) error('E16');\n"+
"    t = xs:duration().parse('PT9999H');\n"+
"    Duration du = t.getValue();\n"+
"    if (du != new Duration('PT9999H')) error('E18');\n"+
"  }\n"+
"</xd:declaration>\n"+
"<A xd:script='finally x()'/>\n"+
"</xd:def>";
			parse(xdef, "", "<A/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef = //uniqueSet
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
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
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a a='string(2,3)' b='int(2,3)' c='xs:date(\"2000-01-01\",\"2010-01-01\")'>\n"+
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
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest() != 0) {System.exit(1);}
	}

}
