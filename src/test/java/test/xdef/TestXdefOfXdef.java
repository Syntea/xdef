/*
 * File: TestXdefOfXdef.java
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
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.util.gencollection.XDGenCollection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.w3c.dom.Element;

/** Test of X-definitions by X-definition.
 * @author Vaclav Trojan
 */
public final class TestXdefOfXdef extends Tester {
	private static XDPool XP;

	public TestXdefOfXdef() {
		super();
	}

	final public Element parse(final String xml,
	final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		return XP.createXDDocument().xparse(xml, reporter);
	}

	private static String genCollection(final String... sources) {
		try {
			Element el = XDGenCollection.genCollection(sources,
				true, //resolvemacros
				false, //removeActions
				false);
			return KXmlUtils.nodeToString(el, true);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "";
		}
	}

	@Override
	/** Run test and print error information. */
	final public void test() {
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		final String dataDir = getDataDir() + "test/";
//		System.out.println(dataDir);
		if (XP == null) {
			XP = compile(dataDir + "TestXdefOfXdef*.xdef");
//			XP = compile(new String[] {
//				dataDir + "TestXdefOfXdef.xdef",
//				dataDir + "TestXdefOfXdef20.xdef",
//				dataDir + "TestXdefOfXdef31.xdef",
//			});
			ByteArrayOutputStream out;
			try {
				out = new ByteArrayOutputStream();
				XP.writeXDPool(out);
				XP = XDFactory.readXDPool(
					new ByteArrayInputStream(out.toByteArray()));
			} catch (Exception ex) {
				throw new RuntimeException("Error when serialized", ex);
			}

		}
		try { //check xdefinition of xdefinitions
/*xx*
			xml = genCollection(dataDir + "TestXComponentGen.xdef",
				dataDir + "TestXComponent_Z.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
if (true)return;
/*xx*/

			xml =
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/3.1' root = \"#A\">\n"+
"  <A/>\n"+
"</xd:def>";
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/3.1' root = \"#A\">\n"+
"  <A>\n"+
"    <B xd:script='occurs 2'/>\n"+
"  </A>\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/2.0' name = \"XDDecl\">\n"+
"  <xd:BNFGrammar name=\"xscript\"><![CDATA[L::='a'/*E*/]]></xd:BNFGrammar>\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml =
"<xd:def xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:declaration>\n"+
" uniqueSet id1 {t: string(); s: int;};\n"+
" uniqueSet id1 string;\n"+
"</xd:declaration>\n"+
"</xd:def>";
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml =
"<xd:def xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:declaration>\n"+
"  String t = ((String)1.5).substring(1);\n"+
"</xd:declaration>\n"+
"<a xd:script = \"*;\n"+
"    create {return (getElementName() == 'B') ? null : null;}\n"+
"    \"/>\n"+
"</xd:def>";
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml =
"<xd:def xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:declaration>\n"+
"  String t = ((String)1.5).substring(1);\n"+
"</xd:declaration>\n"+
"<a xd:script=\"*; create {return (getElementName()=='B')?null :null;}\"/>\n"+
"</xd:def>";
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xdef:def xmlns:xdef='http://www.syntea.cz/xdef/3.1' name='a' root='Field'>\n"+
"<Field Name='required an(); /* jmeno - jen alfanumericke znaky */'/>\n"+
"</xdef:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:def xmlns:xd = 'http://www.syntea.cz/xdef/3.1' root='A'>\n"+
"  <A xd:script=\"match @x==''; options acceptEmptyAttributes\" x=''/>\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test002_5.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:collection xmlns:xd='http://www.syntea.cz/xdef/3.1'>\n"+
"<xd:def xd:name = 'Example' xd:root = 'root'>\n"+
"  <root> required myType() </root>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration>\n"+
"     int x = 1;\n"+
"     type myType $rrr.check('intList');\n"+
"     int myProc(int i, Container c) {\n"+
"       i = myProc(1, %p1='zzz');\n"+
"       if (i == 1) return -1;\n"+
"       return i;\n"+
"     }\n"+
"  </xd:declaration>\n"+
"\n"+
"  <xd:BNFGrammar name = \"$base\">\n"+
"    integer  ::= [0-9]+\n"+
"    S    ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"\n"+
"  <xd:BNFGrammar name = \"$rrr\" extends = \"$base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"  </xd:BNFGrammar>\n"+
"\n"+
"</xd:def>\n"+
"</xd:collection>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' root=\"Objednavka\">\n"+
"<xd:component>\n"+
"  %bind Jedna %with test.xdef.TestXComponents_Y04 %link Y04#Part/@One;\n"+
"  %bind YYY %with test.xdef.TestXComponentsGen %link G#G;\n"+
"  %bind B2 %link J#A/B[2];\n"+
"  %class test.xdef.component.AZ\n"+
"     extends test.xdef.TestXComponentsGen %link A#A/Z;\n"+
"  %class test.xdef.component.Y04\n"+
"     extends test.xdef.TestXComponents_Y04\n"+
"       implements java.io.Serializable, Runnable %link Part;\n"+//380
"  %class test.xdef.component.Y06\n"+
"     extends test.xdef.TestXComponents_Y06Container\n"+ // template class
"        &lt;test.xdef.TestXComponents_Y06Domain&gt; %link A;\n"+
"  %class test.xdef.component.Y12_S %link Actions/$mixed/S;\n"+
"  %class test.xdef.component.J %link J#A;\n"+
"  %class test.xdef.component.N_Part\n"+
"     implements test.xdef.component.N_i, java.io.Serializable\n"+//270
"         %interface test.xdef.component.N_i %link Part;\n"+
"  %class test.xdef.component.N_Operation\n"+
"         implements test.xdef.component.N_i %link A/Operation;\n"+
"  %class test.xdef.component.L1 %link *;\n"+
"  %class test.xdef.component.L %link L;\n"+
"  %class test.xdef.component.C extends test.xdef.TestXComponents_C\n"+
"         %link C#Town;\n"+
"  %class test.xdef.component.C1 implements test.xdef.component.CI\n"+
"         %link C#Person;\n"+
"  %class test.xdef.component.C2 %link C#Town/Street/House;\n"+
"  %interface test.xdef.component.CI %link C#Person;\n"+
"  %enum test.xdef.component.Y21_enum eType;\n"+
"</xd:component>\n"+
"\n"+
"</xd:def>";
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
/*
			xml = genCollection(dataDir + "TestXComponentGen.xdef",
				dataDir + "TestXComponent_Z.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
*/
			xml = genCollection(
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' root=\"Objednavka\">\n"+
"<xd:declaration> \n"+
" Container $errors = [];\n"+
" void chyba(int code) {\n"+
"   Container c = new Container(); /*Vytvorime pracovni kontext.*/\n"+
"	 /* Nastaveni pojmenovanych hodnot. */\n"+
"	 c.setNamedItem(\"Zakaznik\", xpath(\"/Objednavka/@KodZakaznika\"));\n"+
"	 c.setNamedItem(\"KodChyby\", code);\n"+
"	 c.setNamedItem(\"Radek\", getSourceLine());\n"+
"	 c.setNamedItem(\"Sloupec\", getSourceColumn());\n"+
"	 /* Pridame do sekvencce kontextu vytvorenou polozku. */\n"+
"	 $errors.addItem(c); \n"+
"  }\n"+
" </xd:declaration>\n"+
"\n"+
"<Objednavka Cislo=\"int; onFalse chyba(1); onAbsence chyba(2);\"\n"+
"  KodZakaznika=\"string; onAbsence chyba(3);\">\n"+
"\n"+
"  <MistoDoruceni xd:script=\"onAbsence chyba(11);\">\n"+
"    <Adresa Ulice=\"string(2,100); onFalse chyba(12); onAbsence chyba(13);\"\n"+
"      CisloDomu=\"int(1,9999); onFalse chyba(14); onAbsence chyba(15);\"\n"+
"      Obec=\"string(2,100); onFalse chyba(16); onAbsence chyba(17);\"\n"+
"      PSC=\"num(5); onFalse chyba(18); onAbsence chyba(19);\"/>\n"+
"  </MistoDoruceni>\n"+
"\n"+
"  <Polozka xd:script=\"occurs 1..10; onAbsence chyba(21); onExcess chyba(22)\"\n"+
"    KodZbozi=\"num(4); onFalse chyba(23); onAbsence chyba(24)\"\n"+
"    Pocet=\"int(1,1000); onFalse chyba(25); onAbsence chyba(26)\"/>\n"+
"</Objednavka>\n"+
"\n"+
"<Chyby xd:script=\"create $errors\">\n"+
"    <Chyba xd:script=\"occurs 1..*\"\n"+
"      KodChyby=\"int\"\n"+
"      Zakaznik=\"string\"\n"+
"      Radek=\"int\"\n"+
"      Sloupec=\"int\"/>\n"+
"</Chyby>\n"+
"\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'>\n"+
"\n"+
"<xd:declaration>\n"+
"  /* Container, do nehoz budeme ukladat infpormace o chybach. */\n"+
"  Container $errors = [];\n"+
"  /* Promenna, v niz bude vysledny element s chybami. */\n"+
"  Element $chyby;\n"+
"  /* Ulozeny kod zakaznika ze zpracovavane objednavky - viz skript \"onStartElement\". */\n"+
"  String $zakaznik;\n"+
" \n"+
"  void chyba(int code) {\n"+
"    /* Vytvorime kontext pomoci konstruktoru. */\n"+
"    Container c = [ %Zakaznik = $zakaznik,\n"+
"      %KodChyby = code,\n"+
"      %Radek = getSourceLine(),\n"+
"      %Sloupec = getSourceColumn()];\n"+
"	 /* Pridame do sekvence kontextu vytvorenou polozku. */\n"+
"	 $errors.addItem(c);\n"+
"  }\n"+
" \n"+
"  void createChyby() {\n"+
"    /* Otestujeme, zda jsou generovany chyby. */\n"+
"    if (errors() GT 0 || $errors.getLength() GT 0) {\n"+
"	   /* Vytvorime element s chybami do promenne $chyby. */\n"+
"      $chyby = xcreate(\"Chyby\");\n"+
"    }\n"+
"  }\n"+
" </xd:declaration>\n"+
"\n"+
"<Objednavka\n"+
"  xd:script=\"onStartElement $zakaznik=(String)@KodZakaznika;finally createChyby();\"\n"+
"  Cislo=\"int; onFalse chyba(1); onAbsence chyba(2);\"\n"+
"  KodZakaznika=\"string; onAbsence {chyba(3); setText('Neuveden');}\">\n"+
"\n"+
"  <MistoDoruceni xd:script=\"onAbsence chyba(11);\">\n"+
"    <Adresa Ulice=\"string(2,100); onFalse chyba(12); onAbsence chyba(13);\"\n"+
"      CisloDomu=\"int(1,9999); onFalse chyba(14); onAbsence chyba(15);\"\n"+
"      Obec=\"string(2,100); onFalse chyba(16); onAbsence chyba(17);\"\n"+
"      PSC=\"num(5); onFalse chyba(18); onAbsence chyba(19);\"/>\n"+
"  </MistoDoruceni>\n"+
"\n"+
"  <Polozka xd:script=\"occurs 1..10; onAbsence chyba(21); onExcess chyba(22)\"\n"+
"    KodZbozi=\"num(4); onFalse chyba(23); onAbsence chyba(24)\"\n"+
"    Pocet=\"int(1,1000); onFalse chyba(25); onAbsence chyba(26)\"/>\n"+
"</Objednavka>\n"+
"\n"+
"<Chyby xd:script=\"create $errors\">\n"+
"    <Chyba xd:script=\"occurs +\"\n"+
"      KodChyby=\"int\"\n"+
"      Zakaznik=\"string\"\n"+
"      Radek=\"int\"\n"+
"      Sloupec=\"int\"/>\n"+
"</Chyby>\n"+
"\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:collection xmlns:xd='http://www.syntea.cz/xdef/3.1'>\n"
+"<xd:def name='a' root='a' script='options ignoreEmptyAttributes' >\n"
+"<a>\n"
+"  <p p = \"required datetime('ddMMyyyy')\"/>\n"
+"  <xd:list ref='b#dummy'/>\n"
+"  <z/>\n"
+"</a>\n"
+"</xd:def>\n"
+"<xd:def name='b' script='options ignoreEmptyAttributes'>\n"
+"<xd:list name='dummy'>\n"
+"  <xd:mixed>\n"
+"    <b/>\n"
+"    <c xd:script='occurs 0..' />\n"
+"    <d/>\n"
+"    <xd:list ref='dummy1'/>\n"
+"  </xd:mixed>\n"
+"</xd:list>\n"
+"<xd:list name='dummy1'>\n"
+"  <xd:list ref='dummy2'/>\n"
+"  <y/>\n"
+"</xd:list>\n"
+"<xd:list name='dummy2'>\n"
+"  <e xd:script='occurs 0..'/>\n"
+"  <f xd:script='occurs 0..'/>\n"
+"</xd:list>\n"
+"</xd:def>\n"
+"</xd:collection>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_00.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_01.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_01_1.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_01_2.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_02.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_03.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_04.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_05*.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_06*.xdef");
			parse(xml, reporter);
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_07.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(dataDir + "Test000_08*.xdef");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
////////////////////////////////////////////////////////////////////////////////
			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/3.1' name = \"XDDecl\">  \n"+
"  <xd:BNFGrammar name=\"xscript\"><![CDATA[L::='a'/*E*/]]></xd:BNFGrammar>\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xdef:def xmlns:xdef='http://www.syntea.cz/xdef/3.1' name='a' root='Field'>\n"+
"<Field Name='required an(); /* jmeno - jen alfanumericke znaky */'/>\n"+
"</xdef:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
			xml = genCollection(
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'>\n"+
"<xd:declaration>\n"+
" external method boolean a.b.ann(int);\n"+
" type annn ann(2 );\n"+
"</xd:declaration>\n"+
"<A a ='required annn();'/>\n"+
"</xd:def>");
			parse(xml, reporter);
			assertNoErrorwarnings(reporter, xml);
/// V teto Xdefinici je <xd:def xmlns:xd = "METAXDef" ...
//			xml = genCollection(dataDir + "TestXdefOfXdef*.xdef");
//			parse(xml, reporter);
//			assertNoErrorwarnings(reporter, xml);
		} catch (Exception ex) {fail(ex);}

	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
/*#if DEBUG*#/
		Tester.setGenObjFile(true);
/*#end*/
		if (runTest() != 0) {System.exit(1);}
	}

}
