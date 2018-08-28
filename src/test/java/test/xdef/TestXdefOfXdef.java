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

import test.utils.XDTester;
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
public final class TestXdefOfXdef extends XDTester {
	private static XDPool XP;

	public TestXdefOfXdef() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	final public ArrayReporter parse(final String xml) {
		ArrayReporter reporter = new ArrayReporter();
		XP.createXDDocument().xparse(xml, reporter);
		return reporter;
	}

	private static String genCollection(final String... sources) {
		try {
			Element el = XDGenCollection.genCollection(sources,
				true, //resolvemacros
				false, // do not removeActions
				false);
			return KXmlUtils.nodeToString(el, true);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "";
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String xml;
		final String dataDir = getDataDir() + "test/";
		if (XP == null) {
			XP = compile(dataDir + "TestXdefOfXdef*.xdef");
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
//			xml = genCollection(
//"<xd:def name='a' root='macTest' xmlns:xd='" + XDEFNS + "'>\n"+
//"<macTest xd:script=\"finally {${text}; x();} options trimText;\"/>\n"+
//"<xd:macro name=\"text\">\n"+
//"outln('Macro call is:\\n{text}')</xd:macro>\n"+
//"<xd:declaration>\n"+
//"   void x() {${text};}\n"+
//"</xd:declaration>\n"+
//"</xd:def>");
//			assertNoErrorwarnings(parse(xml), xml);
//			assertNoErrorwarnings(parse(xml), genCollection(xml));
//			XDPool xd = compile(xml);
//			xd.createXDDocument("a").xparse("<macTest/>", null);
//if (true) return;
			xml = genCollection(
"<xd:def xmlns:xd = 'http://www.syntea.cz/xdef/2.0' name = 'a' root = 'foo'"+
"   xd:include = \"" + dataDir +"TestInclude_1.xdef\">\n"+
"  <foo xd:script = \"finally out('f')\">\n"+
"    <bar xd:script = '*; ref b#bar'/>\n"+ // b is xdefinition from include
"  </foo>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));

			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/3.1' root='a'>\n"+
"<xd:declaration>\n"+
" external method String x.b(XXNode, XDValue[]);\n"+
"</xd:declaration>\n"+
" <a xd:script = \"\n" +
"    var{\n" +
"        int iii = 1;\n" +
"        uniqueSet id1 {t: string()};\n" +
"        int jjj = 2;\n" +
"        type cislo int();\n" +
"        uniqueSet id2 {t: cislo;}\n" +
"        type datum xdatetime('d. M. yyyy[ HH:mm[:ss]]');\n" +
"        uniqueSet id3 {t: xdatetime('yyyyMMddHHmmss')}\n" +
"    }\n" +
"    finally {\n" +
"      id1.CLEAR();\n" +
"      for (int i = 0; i LT b.size(); i++) {\n" +
"        b.setAt(i,i);\n" +
"      }\n" +
"    }\"\n" +
"    rc = \"required rodneCislo()\" >\n" +
"  <b a = \"optional id1.t.IDREF()\"\n" +
"      b = \"optional cislo();\"\n" +
"      c = \"optional datum();\"/>\n" +
"  <c xd:script = \"occurs 1..; finally id2.CLEAR()\"\n" +
"     stamp = \"required id3.t.ID()\" >\n" +
"     <d xd:script = \"occurs 1..\"\n" +
"        a1 = \"required id1.t.ID()\" \n" +
"        a2 = \"optional id2.t.ID()\"/>\n" +
"     <e a3 = \"required id2.t.IDREF()\"/>\n" +
"  </c>\n" +
"  <f a4 = \"required id1.t.IDREF()\" a5 = \"optional id3.t.IDREF()\"/>\n" +
" </a>\n" +
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/3.1' root = \"#A\">\n"+
"  <A>\n"+
"    <B xd:script='occurs 2 /*intentionaly no parse method*/'/>\n"+
"  </A>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/2.0' name = \"XDDecl\">\n"+
"  <xd:BNFGrammar name=\"xscript\"><![CDATA[L::='a'/*E*/]]></xd:BNFGrammar>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:declaration>\n"+
"  uniqueSet id1 {t: string(); s: int;};\n"+
"  uniqueSet id2 string\n"+
"</xd:declaration>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:declaration>\n"+
"  String t = ((String)1.5).substring(1);\n"+
"</xd:declaration>\n"+
"<a xd:script = \"*;\n"+
"    create {return (getElementName() == 'B') ? null : null;}\n"+
"    \"/>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
		xml = genCollection(
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' root='a'>\n" +
"<a b=\"\n" +
"   optional\n" +
"   {\n" +
"      int i=1;\n" +
"      switch(i) {\n" +
"        case 1: {i=2;}\n" +
"        default: {return true;}\n" +
"      }\n" +
"     return true;\n" +
"   }\n" +
"	default 'abc';\n" +
"   finally outln();\n" +
" \"/>\n" +
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml =
"<xd:def xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:declaration>\n"+
"  String t = ((String)1.5).substring(1);\n"+
"</xd:declaration>\n"+
"<a xd:script=\"*; create {return (getElementName()=='B')?null :null;}\"/>\n"+
"</xd:def>";
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xdef:def xmlns:xdef='http://www.syntea.cz/xdef/3.1' name='a' root='Field'>\n"+
"<Field Name='required an() /* no semicolon*/'/>\n"+
"</xdef:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd = 'http://www.syntea.cz/xdef/3.1' root='A'>\n"+
"  <A xd:script=\"match @x==''; options acceptEmptyAttributes\" x=''/>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
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
"    S ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
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
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd ='http://www.syntea.cz/xdef/3.1' name = \"XDDecl\">  \n"+
"  <xd:BNFGrammar name=\"xscript\"><![CDATA[L::='a'/*E*/]]></xd:BNFGrammar>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'>\n"+
"<xd:declaration>\n"+
" external method boolean a.b.a ( int ) ;\n"+
" type an a( 2 )\n"+ // here is intentionaly missing the semicolon
"</xd:declaration>\n"+
"<A a ='required an();'/>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
////////////////////////////////////////////////////////////////////////////////
			if (getFulltestMode()) {
//				xml = genCollection(
//					dataDir+ "../../../../mytest/xdef/data/SouborD1A.xdef");
//				assertNoErrorwarnings(parse(xml), xml);
//				assertNoErrorwarnings(parse(xml), xml);
				xml = genCollection(
"<xd:def xmlns:xd='" + XDEFNS + "' root ='a'>\n"+
"  <a a=\"fixed {return 'abc';}\" />\n"+
"</xd:def>");
				assertNoErrorwarnings(parse(xml), xml);
				assertNoErrorwarnings(parse(xml), genCollection(xml));
				xml = genCollection(
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method boolean test.xdef.TestXSTypes.kp(XXNode, XDValue[]);"+
"</xd:declaration>\n"+
"<a a='kp(1,5,%totalDigits=1,%enumeration=1,%pattern=\"\\\\d\")'/>\n"+
"</xd:def>");
				assertNoErrorwarnings(parse(xml), xml);
				assertNoErrorwarnings(parse(xml), genCollection(xml));
				xml = genCollection(
"<xd:def xmlns:xd='" + XDEFNS + "' root='a | b | m/n | x'>\n"+
"   <xd:any xd:name='x' b='int()' />\n"+
"   <xd:mixed xd:name='m'> <n/> <o/> </xd:mixed>\n"+
"   <a> <xd:mixed xd:script='ref m' /> </a> <b>\n"+
"     <xd:any xd:script='ref x' b='int()' />\n"+
"   </b>\n"+
"</xd:def>");
				assertNoErrorwarnings(parse(xml), xml);
				assertNoErrorwarnings(parse(xml), genCollection(xml));

//// V teto Xdefinici je <xd:def xmlns:xd = "METAXDef" ...
//				xml = dataDir + "TestXdefOfXdef*.xdef";
//				assertNoErrorwarnings(parse(xml), xml);
			}
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest() != 0) {System.exit(1);}
	}

}