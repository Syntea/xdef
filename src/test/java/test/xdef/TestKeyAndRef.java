/*
 * File: TestKeyAndRef.java
 * Copyright 2006 Syntea.
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

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.proc.XXData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Test of external utilities for key, keyRef and also sequence in choice.
 * @author Vaclav Trojan
 */
public final class TestKeyAndRef extends Tester {

	public TestKeyAndRef() {super();}

	public static String boundQName(final XXData data) {
		String s = data.getTextValue();
		byte xmlVersion =
			"1.1".equals(data.getElement().getOwnerDocument().getXmlVersion())
			? (byte) 11 : (byte) 10;
		if (!StringParser.chkNCName(s, xmlVersion)) {
			return null;
		}
		Element e = data.getElement();
		for (;;) {
			if (e.hasAttribute("targetNamespace")) {
				return e.getAttribute("targetNamespace");
			}
			Node n = e.getParentNode();
			if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
				break;
			}
			e = (Element) n;
		}
		return null;
	}

	@Override
	public void test() {
		String xdef;
		String s;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp;
		final String dataDir = getDataDir() + "test/";
		try {
/*VT6*/
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"<xd:declaration>uniqueSet u {x:int()}</xd:declaration>\n"+
"<a>\n"+
"  <b z='u.x.IDREFS'/>\n"+
"  <c x='u.x.ID' y='u.x.ID'/>\n"+
"</a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b z='1 2'/><c x='1' y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b z='1 3'/><c x='1' y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"<xd:declaration scope='local'>uniqueSet u {x: int()}</xd:declaration>\n"+
"<a>\n"+
"  <b x='u.x.ID' y='u.x.ID'/>\n"+
"  <c z='u.x.CHKIDS'/>\n"+
"</a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b x='1' y='2'/><c z='1 2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b x='1' y='2'/><c z='1 3'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			// uniqueSet declared as variable of model.
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<A>\n"+
"  <a xd:script='var uniqueSet v {x:int()}; occurs *'>\n"+
"    <b x='v.x.ID()' y='v.x.ID()'/>\n"+
"    <c z='v.x.IDREFS();'/>\n"+
"  </a>\n"+
"</A>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='3'/><c z='2 3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertEq(2, reporter.getErrorCount());
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt; y : optional flt;}\n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: u.x}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml ="<A><b a='3.1'/><c a='4.1'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt} \n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: flt()}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><b a='3.1'/><b a='3.1'/><c a='4.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 3, reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt; y : ? flt;}\n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: flt}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><b a='3.1'/><b a='3.1'/><c a='4.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 3, reporter.printToString());

			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"  <A>\n"+
"    <a xd:script='occurs *; ref a;'/>\n"+
"  </A>\n"+
"  <a xd:script='var uniqueSet v {x: int(1,3); y: int()}'>\n"+
"    <b xd:script='+; finally v.ID' x='v.x' y='v.y'/>\n"+
"    <c x='v.x'> v.y.IDREFS </c>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><b x='1' y='3'/><c x='1'>2 3</c></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c x='1'>1 3</c></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
/*VT6*/
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"  <A xd:script='var {uniqueSet v {p: int(1,3); q: optional string};}'>\n"+
"  <a xd:script='occurs *;'>\n"+
"    <b xd:script='finally v.ID()' x='v.p()' y='v.q()'/>\n"+
"    <c xd:script='finally v.IDREF()' x='v.p()' y='v.q()'/>\n"+
"  </a>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='3' y='a'/><c x='3' y='a'/></a>\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
/*VT6*/
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<xd:declaration scope='local'>uniqueSet v {x: int(1,3)}</xd:declaration>\n"+
"  <A>\n"+
"    <a xd:script='occurs *; ref a;'/>\n"+
"  </A>\n"+
"  <a xd:script='finally v.CLEAR()'>\n"+
"    <b x='v.x.ID' y='v.x.ID'/>\n"+
"    <c z='v.x.IDREFS'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"  <A>\n"+
"    <a xd:script='occurs *; ref a;'/>\n"+
"  </A>\n"+
"  <a xd:script='var uniqueSet v {x: int(1,3)}'>\n"+
"    <b x='v.x.ID' y='v.x.ID'/>\n"+
"    <c z='v.x.IDREFS'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<xd:declaration scope='local'>uniqueSet v {x:int(1,3)}</xd:declaration>\n"+
"  <A>\n"+
"    <a xd:script='occurs *; ref a;'/>\n"+
"  </A>\n"+
"  <a xd:script='var uniqueSet v {x: int(1,3)}'>\n"+
"    <b x='v.x.ID' y='v.x.ID()'/>\n"+
"    <c z='v.x.IDREFS'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
/*VT6*/
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<A xd:script='var {type i int(1,3);uniqueSet v {p:i;q:? string(1,9)};}'>\n"+
"  <a xd:script='occurs *;'>\n"+
"    <b xd:script='finally v.ID' x='v.p()' y='v.q()'/>\n"+
"    <c xd:script='finally v.IDREF' x='v.p()' y='v.q()'/>\n"+
"  </a>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+
"<a><b x='3' y='a'/><c x='3' y='a'/></a>"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<A xd:script='var{type i int(1,3);uniqueSet v{p:i;q:? string(1,9)};}'>\n"+
"  <a xd:script='occurs *;'>\n"+
"    <b xd:script='finally v.ID' x='v.p()' y='v.q()'/>\n"+
"    <c xd:script='finally v.IDREF' x='v.p()' y='v.q()'/>\n"+
"  </a>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='3' y='a'/><c x='3' y='a'/></a>\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type code string(3);\n"+
"  uniqueSet FileCode {parse: code();}\n"+
"  uniqueSet FileParam {Code: FileCode.parse.ID(); Param: string(1,10);}\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <FileType xd:script='occurs 1..'\n"+
"      FileCode='required FileParam.Code()'>\n"+
"    <Param xd:script='occurs 1..'\n"+
"      ParamName='required FileParam.Param.ID()'/>\n"+
"  </FileType>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <FileType FileCode='XYZ'>\n"+
"    <Param ParamName='v1'/>\n"+
"    <Param ParamName='v2'/>\n"+
"  </FileType>\n"+
"  <FileType FileCode='ABC'>\n"+
"    <Param ParamName='v1'/>\n"+
"    <Param ParamName='v2'/>\n"+
"  </FileType>\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>\n"+
"  <FileType FileCode='XYZ'>\n"+
"    <Param ParamName='v1'/>\n"+
"    <Param ParamName='v2'/>\n"+
"  </FileType>" +
"  <FileType FileCode='XYZ'>\n"+
"    <Param ParamName='v3'/>\n"+
"    <Param ParamName='v4'/>\n"+
"  </FileType>\n"+
"</A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.printToString().indexOf("XDEF523") > 0,
				"Error not recognized; " + reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='Zeme'>\n"+
"<xd:declaration scope='local'>\n"+
" /* tabulka slozenych klicu */\n"+
" uniqueSet adresa {zeme: string; mesto: string; ulice: string; dum: int()};\n"+
"</xd:declaration>\n"+
"<Zeme jmeno=\"adresa.zeme()\">\n"+
"  <Mesto xd:script = \"occurs +\" nazev=\" adresa.mesto()\">\n"+
"    <Ulice xd:script = \"occurs +\" nazev=\" adresa.ulice()\">\n"+
"      <Dum xd:script = \"occurs +\" cislo=\"adresa.dum.ID()\"/> \n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa xd:script = \"occurs *; finally adresa.IDREF();\"\n"+
"    Zeme=\"adresa.zeme()\"\n"+
"    Mesto=\"adresa.mesto()\"\n"+
"    Ulice=\"adresa.ulice()\"\n"+
"    Dum=\"? adresa.dum()\" />\n"+
"</Zeme>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Zeme jmeno=\"CS\">\n"+
"  <Mesto nazev=\"Praha\">\n"+
"    <Ulice nazev=\"Dlouhá\">\n"+
"      <Dum cislo=\"1\"/> \n"+
"      <Dum cislo=\"3\"/> \n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouhá\" Dum=\"1\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouhá\" Dum=\"3\" />\n"+
"</Zeme>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<Zeme jmeno=\"CS\">\n"+
"  <Mesto nazev=\"Praha\">\n"+
"    <Ulice nazev=\"Dlouhá\">\n"+
"      <Dum cislo=\"1\"/> \n"+
"      <Dum cislo=\"3\"/> \n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouhá\" Dum=\"1\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouhá\" Dum=\"3\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouhá\" Dum=\"5\" />\n"+
"</Zeme>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1
				&& s.indexOf("/Zeme/Adresa[3]") > 1, s);
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='Zeme'>\n"+
"<xd:declaration scope='local'>\n"+
" /* tabulka slozenych klicu */\n"+
" uniqueSet mesto {zeme: string; jmeno: string};\n"+
" uniqueSet adresa {zeme: mesto.zeme;\n"+
"					mesto: mesto.jmeno;\n"+
"					ulice: string;\n"+
"					dum:?int()};\n"+
"</xd:declaration>\n"+
"<Zeme jmeno=\"adresa.zeme(mesto.zeme())\">\n"+
"  <Mesto xd:script=\"occurs +\" nazev=\"adresa.mesto(mesto.jmeno.SET());\">\n"+
"    <Ulice xd:script = \"occurs +\" nazev=\"adresa.ulice.SET()\">\n"+
"      <Dum xd:script = \"occurs *\" cislo=\"adresa.dum.ID()\"/> \n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa xd:script = \"occurs *; finally adresa.IDREF();\"\n"+
"    Zeme=\"adresa.zeme()\"\n"+
"    Mesto=\"adresa.mesto()\"\n"+
"    Ulice=\"adresa.ulice()\"\n"+
"    Dum=\"? adresa.dum()\" />\n"+
"  <Lokalita xd:script=\"occurs *; finally mesto.IDREF();\"\n"+
"    Zeme=\"mesto.zeme()\"\n"+
"    Mesto=\"mesto.jmeno()\"/>\n"+
"</Zeme>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Zeme jmeno=\"CS\">\n"+
"  <Mesto nazev=\"Praha\">\n"+
"    <Ulice nazev=\"Dlouha\">\n"+
"      <Dum cislo=\"1\"/> \n"+
"      <Dum cislo=\"3\"/> \n"+
"    </Ulice>\n"+
"    <Ulice nazev=\"Kratka\"/>\n"+
"  </Mesto>\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Kratka\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"1\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"3\" />\n"+
"  <Lokalita Zeme=\"CS\" Mesto=\"Praha\" />\n"+
"</Zeme>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<Zeme jmeno=\"CS\">\n"+
"  <Mesto nazev=\"Praha\">\n"+
"    <Ulice nazev=\"Dlouha\">\n"+
"      <Dum cislo=\"1\"/> \n"+
"      <Dum cislo=\"3\"/> \n"+
"    </Ulice>\n"+
"    <Ulice nazev=\"Kratka\"/>\n"+
"  </Mesto>\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Kratka\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"1\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"3\" />\n"+
"  <Lokalita Zeme=\"CS\" Mesto=\"Olomouc\" />\n"+
"</Zeme>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1
				&& s.indexOf("/Zeme/Lokalita[1]") > 1, s);
			xml =
"<Zeme jmeno=\"CS\">\n"+
"  <Mesto nazev=\"Praha\">\n"+
"    <Ulice nazev=\"Dlouha\">\n"+
"      <Dum cislo=\"1\"/> \n"+
"      <Dum cislo=\"3\"/> \n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"1\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"3\" />\n"+
"  <Adresa Zeme=\"CS\" Mesto=\"Praha\" Ulice=\"Dlouha\" Dum=\"5\" />\n"+
"</Zeme>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1
				&& s.indexOf("/Zeme/Adresa[3]") > 1, s);
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='Zeme'>\n"+
"<xd:declaration scope='local'>\n"+
"   /* tabulka slozenych klicu */\n"+
"   uniqueSet lokalita {zeme:string; mesto: ? string;};\n"+
"   uniqueSet adresa {zeme: lokalita.zeme.SET;\n"+
"                     mesto: lokalita.mesto.SET;\n"+
"                     ulice:string; dum:int()};\n"+
"</xd:declaration>\n"+
"<Zeme nazev=\"adresa.zeme()\">\n"+
"  <Mesto xd:script = \"occurs +\" nazev=\" adresa.mesto()\">\n"+
"    <Ulice xd:script = \"occurs +\" nazev=\" adresa.ulice()\">\n"+
"      <Dum xd:script = \"occurs +\" cislo=\"adresa.dum.ID()\"/>\n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa xd:script = \"occurs *; finally adresa.IDREF();\"\n"+
"    Zeme=\"adresa.zeme()\"\n"+
"    Mesto=\"adresa.mesto()\"\n"+
"    Ulice=\"adresa.ulice()\"\n"+
"    Dum=\"adresa.dum()\"/>\n"+
"  <Lokalita xd:script = \"occurs *; finally lokalita.IDREF();\"\n"+
"    Zeme=\"lokalita.zeme()\"\n"+
"    Mesto=\"? lokalita.mesto()\"/>\n"+
"</Zeme>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Zeme nazev=\"CR\">\n"+
"  <Mesto nazev='Praha'>\n"+
"    <Ulice nazev='Dlouha'>\n"+
"      <Dum cislo='1'/>\n"+
"      <Dum cislo='3'/>\n"+
"    </Ulice>\n"+
"    <Ulice nazev='Kratka'>\n"+
"      <Dum cislo='2'/>\n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa Zeme='CR' Mesto='Praha' Ulice='Dlouha' Dum='1'/>\n"+
"  <Adresa Zeme='CR' Mesto='Praha' Ulice='Dlouha' Dum='3'/>\n"+
"  <Adresa Zeme='CR' Mesto='Praha' Ulice='Kratka' Dum='2'/>\n"+
"  <Lokalita Zeme='CR'/>\n"+
"  <Lokalita Zeme='CR' Mesto='Praha'/>\n"+
"</Zeme>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try { // test a.d.NEWKEY()
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a xd:script='finally a.ID()' a='a.d(); finally a.d.NEWKEY()' >\n"+
"    <b b='a.d.IDREF();' />\n"+
"  </a>\n"+
"  <xd:declaration>\n"+
"    uniqueSet a {d: int()}\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a a='5'><b b='5'/></a>";
			parse(xp, "", xml, reporter);
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF522") > 0);

			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <Mesto xd:script='occurs 0..*;'\n"+
"        jmeno='adresa.mesto()'>\n"+
"        <Ulice xd:script='occurs 0..*'\n"+
"          jmeno='adresa.ulice()'>\n"+
"          <Dum xd:script='occurs 0..*'\n"+
"            cislo='adresa.dum.ID()'/>\n"+
"        </Ulice>\n"+
"      </Mesto>\n"+
"      <Vesnice xd:script='occurs 0..*;'\n"+
"        jmeno='adresa.mesto()'>\n"+
"        <Dum xd:script='occurs 0..*; init adresa.ulice.NEWKEY();'\n"+
"          cislo='adresa.dum.ID();'/>\n"+
"      </Vesnice>\n"+
"    </xd:mixed>\n"+
"    <Adresa xd:script='occurs 0..*; finally adresa.IDREF()'\n"+
"       mesto='adresa.mesto()'\n"+
"       ulice='optional adresa.ulice(); onAbsence adresa.ulice.NEWKEY();'\n"+
"       cislo='adresa.dum()' />\n"+
"  </a>\n"+
"\n"+
"	<xd:declaration scope='local'>\n"+
"		uniqueSet adresa { mesto: string(); ulice: string(); dum: int(); }\n"+
"	</xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
//			xp.displayCode();
			xml =
"<a>\n"+
"  <Mesto jmeno='Praha'>\n"+
"    <Ulice jmeno='Dlouha'>\n"+
"      <Dum cislo='1'/>\n"+
"      <Dum cislo='3'/>\n"+
"    </Ulice>\n"+
"    <Ulice jmeno='Kratka'>\n"+
"      <Dum cislo='2'/>\n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Vesnice jmeno='Lhota'>\n"+
"    <Dum cislo='1'/>\n"+
"    <Dum cislo='2'/>\n"+
"  </Vesnice>\n"+
"  <Adresa mesto='Praha' ulice='Kratka' cislo='2'/>\n"+
"  <Adresa mesto='Praha' ulice='Dlouha' cislo='3'/>\n"+
"  <Adresa mesto='Praha' ulice='Dlouha' cislo='1'/>\n"+
"  <Adresa mesto='Lhota' cislo='2'/>\n"+
"  <Adresa mesto='Lhota' cislo='1'/>\n"+
"</a>";
//System.out.println(xml);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<a>\n"+
"  <Mesto jmeno='Praha'>\n"+
"    <Ulice jmeno='Kratka'>\n"+
"      <Dum cislo='1'/>\n"+
"    </Ulice>\n"+
"  </Mesto>\n"+
"  <Adresa mesto='Praha' ulice='Kratka' cislo='1'/>\n"+
"  <Adresa mesto='Praha' ulice='Kratka' cislo='2'/>\n"+
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1 &&
				s.indexOf("xpath=/a/Adresa[2]") > 0, s);
		} catch (Exception ex) {fail(ex);}
		try {
/*#if EXTKEY*#/
			xp = compile(dataDir + "TestKeyAndRef1.xdef");
			assertEq(dataDir + "TestKeyAndRef1.xml",
				parse(xp, "", dataDir + "TestKeyAndRef1.xml", reporter));
			assertNoErrors(reporter);
/*#end*/

			xp = compile(dataDir + "TestKeyAndRef2.xdef");
			assertEq(dataDir + "TestKeyAndRef2.xml",
				parse(xp, "", dataDir + "TestKeyAndRef2.xml",reporter));
			assertNoErrors(reporter);

			xp = compile(dataDir + "TestKeyAndRef3.xdef");
			assertEq(dataDir + "TestKeyAndRef3.xml",
				parse(xp, "", dataDir + "TestKeyAndRef3.xml", reporter));
			assertNoErrors(reporter);
			assertEq(dataDir + "TestKeyAndRef3_1.xml",
				parse(xp, "", dataDir + "TestKeyAndRef3_1.xml", reporter));
			assertNoErrors(reporter);

			xp = compile(dataDir + "TestKeyAndRef4.xdef");
			assertEq(dataDir + "TestKeyAndRef4.xml",
				parse(xp, "",dataDir + "TestKeyAndRef4.xml", reporter));
			assertNoErrors(reporter);
			parse(xp, "", dataDir + "TestKeyAndRef4_1.xml" , reporter);
			assertTrue(reporter.getErrorCount()==1
				&& reporter.printToString().indexOf("XDEF522")>0,
				"Error Not recognized; " + reporter.printToString());
			parse(xp, "", dataDir + "TestKeyAndRef4_2.xml", reporter);
			assertTrue(reporter.getErrorCount()==1
				&& reporter.printToString().indexOf("XDEF522")>0,
				"Error Not recognized; " + reporter.printToString());
			xp = compile(dataDir + "TestKeyAndRef5.xdef");
			assertEq(dataDir + "TestKeyAndRef5.xml",
				parse(xp, "", dataDir + "TestKeyAndRef5.xml",reporter));
			assertNoErrors(reporter);

			xp = compile(dataDir + "TestKeyAndRef6.xdef");
			assertEq(dataDir + "TestKeyAndRef6.xml",
				parse(xp, "" , dataDir + "TestKeyAndRef6.xml", reporter));
			assertNoErrors(reporter);

			setProperty(XDConstants.XDPROPERTY_MINYEAR, null);
			setProperty(XDConstants.XDPROPERTY_MAXYEAR, null);
			setProperty(XDConstants.XDPROPERTY_SPECDATES, null);
			xp = compile(dataDir + "TestKeyAndRef7.xdef");
			assertEq(dataDir + "TestKeyAndRef7.xml",
				parse(xp, "Mondial" , dataDir + "TestKeyAndRef7.xml",reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}