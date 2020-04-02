package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.XDPool;

/** Test of attribute processing and match expression; test DTD types.
 * @author Vaclav Trojan
 */
public final class TestDTDTypes extends XDTester {

	public TestDTDTypes() {super();}

	@Override
	public void test() {
		String xdef, xml;
		XDPool	xp;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		try {
//CDATA
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required CDATA'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF814".equals(rep.getMsgID()), rep.toString());
			}
//ID
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><b a='a1'/><b a='a2'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF523", rep.getMsgID(), rep.toString());
			}
//ID
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><b a='a1'/><b a='a2'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF523", rep.getMsgID(), rep.toString());
			}
//IDREF
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <a xd:script='*' a='required IDREF'/>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><a a='a1'/><b a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><a a='a2'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
//IDREF
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <a xd:script='*' a='required IDREF'/>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><a a='a1'/><b a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><a a='a2'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
//CHKID
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"    <a xd:script='*' a='required CHKID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><b a='a1'/><a a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><a a='a2'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
//IDREFS
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <a xd:script='*' a='required IDREFS'/>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><a a='a1'/><b a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><a a='a1 a1'/><b a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><a a='a2'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a><a a='a1 a2'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <a xd:script='*' a='required IDREFS'/>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><a a='a1'/><b a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><a a='a1 a1'/><b a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><a a='a2'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a><a a='a1 a2'/><b a='a1'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
//CHKIDS
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"    <a xd:script='*' a='required CHKIDS'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><b a='a1'/><a a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><a a='a1 a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><a a='a2'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a><b a='a1'/><a a='a1 a2'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"    <b xd:script='*' a='required ID'/>\n"+
"    <a xd:script='*' a='required CHKIDS'/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a><b a='a1'/><a a='a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><a a='a1 a1'/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b a='a1'/><a a='a2'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a><b a='a1'/><a a='a1 a2'/></a>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF522", rep.getMsgID(), rep.toString());
			}
//NMTOKEN
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required NMTOKEN'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF804".equals(rep.getMsgID()), rep.toString());
			}
//NMTOKEN
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required NMTOKEN'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF809", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF809", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF804", rep.getMsgID(), rep.toString());
			}
//NMTOKENS
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required NMTOKENS'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
//NMTOKENS
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required NMTOKENS'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF809", rep.getMsgID(), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertEq("XDEF809", rep.getMsgID(), rep.toString());
			}
//ENTITY
//			if (hasDatatype) {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required ENTITY'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!ELEMENT a ANY>\n"+
"<!ATTLIST a a CDATA #IMPLIED>\n"+
"<!ENTITY X SYSTEM '003-2.ent' NDATA nota>\n"+
"]>\n"+
"<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = 'X X'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF804".equals(rep.getMsgID()), rep.toString());
			}
//			}
//ENTITY
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required ENTITY'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!ELEMENT a ANY>\n"+
"<!ATTLIST a a CDATA #IMPLIED>\n"+
"<!ENTITY X SYSTEM '003-2.ent' NDATA nota>\n"+
"]>\n"+
"<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = 'X X'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = 'X Y'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF804".equals(rep.getMsgID()), rep.toString());
			}
//ENTITIES
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required ENTITIES'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!ELEMENT a ANY>\n"+
"<!ATTLIST a a CDATA #IMPLIED>\n"+
"<!ENTITY X '003-2.ent'>\n"+
"]>\n"+
"<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!ELEMENT a ANY>\n"+
"<!ATTLIST a a CDATA #IMPLIED>\n"+
"<!ENTITY X SYSTEM '003-2.ent' NDATA nota>\n"+
"<!ENTITY Y SYSTEM '003-3.ent' NDATA nota>\n"+
"]>\n"+
"<a a = 'X Y'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
//ENTITIES
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required ENTITIES'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!ELEMENT a ANY>\n"+
"<!ATTLIST a a CDATA #IMPLIED>\n"+
"<!ENTITY X '003-2.ent'>\n"+
"]>\n"+
"<a a = 'X'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!ELEMENT a ANY>\n"+
"<!ATTLIST a a CDATA #IMPLIED>\n"+
"<!ENTITY X SYSTEM '003-2.ent' NDATA nota>\n"+
"<!ENTITY Y SYSTEM '003-3.ent' NDATA nota>\n"+
"]>\n"+
"<a a = 'X Y'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a = '?'/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
			parse(xp, "", "<a a = ''/>", reporter);
			rep = reporter.getReport();
			if (rep == null) {
				fail("Error not reported");
			} else {
				assertTrue("XDEF809".equals(rep.getMsgID()), rep.toString());
			}
//Test NOTATION
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a a = 'required NOTATION'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!NOTATION n PUBLIC 'http://xdef.syntea.cz/x'>\n"+
"]>\n"+
"<a a = 'n'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "",
"<!DOCTYPE a [\n"+
"<!NOTATION n PUBLIC 'http://xdef.syntea.cz/x'>\n"+
"]>\n"+
"<a a = 'x'/>", reporter);
			assertErrors(reporter);

//Test of ID, IDREF, IDREFS
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name=\"test1\">\n"+
" <xd:declaration scope='global'>\n"+
"   uniqueSet id2 {c: cislo};\n"+
" </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:root='a' name='test'>\n"+
" <xd:declaration scope='global'>\n"+
"   int i = 1;\n"+
"   uniqueSet id1 {s: string()};" +
"   int j = 2;\n"+
"   ParseResult cislo() { return int(); }\n"+
"   type datum xdatetime('d. M. yyyy[ HH:mm[:ss]]');\n"+
"   uniqueSet id3 {t: xdatetime('yyyyMMddHHmmss')};\n"+
" </xd:declaration>\n"+
" <a xd:script = \"finally id1.CLEAR()\">\n"+
"   <b a = \"optional id1.s.IDREF()\"\n"+
"      b = \"optional cislo();\"\n"+
"      c = \"optional datum();\"/>\n"+
"   <c xd:script = \"occurs 1..; finally id2.CLEAR()\"\n"+
"     a = \"optional id2.c.IDREF()\"\n"+
"     stamp = \"required id3.t.ID()\" >\n"+
"     <d xd:script = \"occurs 1..\"\n"+
"        a1 = \"required id1.s.ID()\" \n"+
"        a2 = \"optional id2.c.ID()\"/>\n"+
"     <e a3 = \"required id2.c.IDREF()\"/>\n"+
"   </c>\n"+
"   <f a4 = \"required id1.s.IDREF()\" a5 = \"optional id3.t.IDREF()\"/>\n"+
" </a>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml =
"<a>\n"+                                                    // 01
"  <b a = \"b1\" b = \"123\" c = \"15. 3. 2007\"/>\n"+      // 02
//"  <b a = \"xxx\" b = \"123\" c = \"15. 3. 2007\"/>\n"+     // 02
"  <c stamp = \"20070101000000\" a = \"3\">\n"+             // 03
"    <d a1 = \"a1\" a2 = \"1\" />\n"+                       // 04
"    <d a1 = \"b1\" a2 = \"2\" />\n"+                       // 05
"    <d a1 = \"c1\" a2 = \"3\" />\n"+                       // 06
"    <e a3 = \"3\"/>\n"+                                    // 07
"  </c>\n"+                                                 // 08
"  <c stamp = \"20070101000001\">\n"+                       // 09
"    <d a1 = \"d1\" a2 = \"1\" />\n"+                       // 10
"    <d a1 = \"e1\" a2 = \"2\" />\n"+                       // 11
"    <d a1 = \"f1\" a2 = \"3\" />\n"+                       // 12
"    <e a3 = \"3\"/>\n"+                                    // 13
"  </c>\n"+                                                 // 14
"  <c stamp = \"20070101000002\">\n"+                       // 15
"    <d a1 = \"g1\" a2 = \"1\" />\n"+                       // 16
"    <d a1 = \"h1\" a2 = \"2\" />\n"+                       // 17
"    <d a1 = \"i1\" a2 = \"3\" />\n"+                       // 18
"    <e a3 = \"3\"/>\n"+                                    // 19
"  </c>\n"+                                                 // 20
"   <f a4 = \"h1\" a5 = \"20070101000001\"/>\n"+            // 21
"</a>";                                                      // 22
			parse(xp, "test", xml, reporter);
			assertNoErrors(reporter);
// Test error reporting
			xml =
"<a>\n"+                                                    // 01
"  <b a = \"c1\" b = \"123\" c = \"15. 3. 2007\"/>\n"+      // 02
"  <c stamp = \"20070101000000\">\n"+                       // 03
"    <d a1 = \"a1\" a2 = \"1\" />\n"+                       // 04
"    <d a1 = \"b1\" a2 = \"2\" />\n"+                       // 05
"    <d a1 = \"b1\" a2 = \"3\" />\n"+                       // 06
"    <e a3 = \"3\"/>\n"+                                    // 07
"  </c>\n"+                                                 // 08
"  <c stamp = \"20070101000001\" a = \"3\">\n"+             // 09
"    <d a1 = \"d1\" a2 = \"1\" />\n"+                       // 10
"    <d a1 = \"e1\" a2 = \"2\" />\n"+                       // 11
"    <d a1 = \"f1\" a2 = \"2\" />\n"+                       // 12
"    <e a3 = \"3\"/>\n"+                                    // 13
"  </c>\n"+                                                 // 14
"  <c stamp = \"20070101000002\">\n"+                       // 15
"    <d a1 = \"g1\" a2 = \"1\" />\n"+                       // 16
"    <d a1 = \"h1\" a2 = \"2\" />\n"+                       // 17
"    <d a1 = \"i1\" a2 = \"3\" />\n"+                       // 18
"    <e a3 = \"1\"/>\n"+                                    // 19
"  </c>\n"+                                                 // 20
"   <f a4 = \"xx\" a5 = \"20070101000001\"/>\n"+            // 21
"</a>";                                                      // 22
			parse(xp, "test", xml, reporter);
			if (reporter.errors()) {
				String[] reports = new String[] {
				"XDEF523",
				"&{xpath}/a/c[1]/d[3]/@a1&{pos}150&{line}6&{column}14",
				"XDEF523",
				"&{xpath}/a/c[2]/d[3]/@a2&{pos}311&{line}12&{column}24",
				"XDEF522",
				"&{id}3&{xpath}/a/c[2]/@a&{pos}226&{line}9&{column}36",
				"XDEF522",
				"&{id}3&{xpath}/a/c[2]/e[1]/@a3&{pos}330&{line}13&{column}14",
				"XDEF522",
				"&{id}xx&{xpath}/a/f[1]/@a4&{pos}497&{line}21&{column}13",
				"XDEF522",
				"&{id}c1&{xpath}/a/b[1]/@a&{pos}14&{line}2&{column}11"
				};
			loop: for (int i = 0; i < reports.length; i+=2) {
					if ((rep = reporter.getReport()) != null) {
						for (int j = 0; j < reports.length; j+=2) {
							if (reports[j].equals(rep.getMsgID()) &&
								reports[j + 1].equals(rep.getModification())) {
								continue loop;
							}
						}
					} else {
						fail("Missing report " + (1 + i/2));
						break;
					}
				}
				while((rep = reporter.getReport()) != null) {
					fail(rep.toString());
				}
			} else {
				fail("Error not reported");
			}
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}