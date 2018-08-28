/*
 * File: TestConstruct.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */

package test.xdef;

import test.utils.XDTester;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.proc.XXElement;

/** Test of construction of element from the program.
 *
 * @author  Vaclav Trojan
 */
public final class TestConstruct extends XDTester {

	public TestConstruct() {super();}

	@Override
	public void test() {
		Report.setLanguage("en"); //localize
		System.getProperties().setProperty("xdef.warnings", "true");
		System.getProperties().setProperty("xdef.extmode", "new");
		System.getProperties().setProperty("xdef.debug", "false");
		XDPool xp;
		XDDocument xd;
		ReportWriter rw;
		String xdef;
		XXElement rootChkel, chkel;
		try {
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='messages'>\n"+
"<messages>\n"+
"    <child/>\n"+
"  <xd:any xd:script = \"occurs 0..\"\n"+
"            ces     = \"optional; onTrue $stdErr.outln(getElementName()"+
"                        + '(ces) ' + getText())\"\n"+
"            eng     = \"optional; onTrue $stdErr.outln(getElementName()"+
"                        + '(eng) ' + getText())\"\n"+
"    />\n"+
"</messages>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			rootChkel = xd.prepareRootXXElement("messages", true);
			if (rootChkel.errors()) {
				fail(rootChkel.getXPos());
			}
			chkel = rootChkel.prepareXXElement("child");
			assertFalse(chkel.errors(), chkel.getXPos());
			assertTrue(chkel.addElement(), chkel.getXPos());
			chkel = rootChkel.prepareXXElement("A000");
			assertFalse(chkel.errors(), chkel.getXPos());
			assertTrue(chkel.addAttribute("ces", "Toto je zprava"),
				chkel.getXPos());
			assertTrue(chkel.addAttribute("eng", "This is message"),
				chkel.getXPos());
			assertFalse(chkel.addAttribute("rom", "messagos"),
				chkel.getXPos());
			assertTrue(chkel.addElement(), chkel.getXPos());
			assertTrue(rootChkel.addElement(), rootChkel.getXPos());
			assertEq(xd.getElement(),
				"<messages><child/><A000"
				+ " ces=\"Toto je zprava\" eng=\"This is message\"/>"
				+ "</messages>");
			xdef =
"<xd:def xmlns:xd = '" + XDEFNS + "'\n"+
"        xmlns:a  = \"a.a\"\n"+
"        xmlns:b  = \"b.b\"\n"+
"        xd:name  = \"test\"\n"+
"        xd:root  = \"a:root\">\n"+
"\n"+
"<a:root>\n"+
"  <b:child/>\n"+
"  <xd:any xd:script = \"occurs 0..\"\n"+
"      a:a         = \"optional string()\"\n"+
"      b:b         = \"optional string()\"\n"+
"    />\n"+
"</a:root>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument("test");
			rootChkel = xd.prepareRootXXElementNS(
				"a.a", "x:root", true);
			assertFalse(rootChkel.errors(), rootChkel.getXPos());
			chkel = rootChkel.prepareXXElementNS("b.b", "y:child");
			assertFalse(chkel.errors(), chkel.getXPos());
			assertTrue(chkel.addElement(), chkel.getXPos());
			chkel = rootChkel.prepareXXElementNS("b.b", "y:A000");
			assertFalse(chkel.errors(), chkel.getXPos());
			assertTrue(chkel.addAttributeNS("a.a", "x:a", "Toto je zprava"),
				chkel.getXPos());
			assertTrue(chkel.addAttributeNS("b.b", "y:b", "This is message"),
				chkel.getXPos());
			assertFalse(chkel.addAttribute("z0", "messagos"), chkel.getXPos());
			assertFalse(chkel.addAttribute("z1", "os"), chkel.getXPos());
			assertTrue(chkel.addElement(), chkel.getXPos());
			assertTrue(rootChkel.addElement(), rootChkel.getXPos());
			assertEq(xd.getElement(),
"<x:root xmlns:x=\"a.a\" xmlns:y=\"b.b\">" +
"<y:child/>" +
"<y:A000 x:a=\"Toto je zprava\" y:b=\"This is message\"/>"+
"</x:root>");
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
