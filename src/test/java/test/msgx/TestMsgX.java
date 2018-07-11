/*
 * File: TestMsgX.java
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
package test.msgx;

import cz.syntea.common.cfg.MsgX;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.STester;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXElement;
import java.io.StringWriter;
import java.util.Properties;
import org.w3c.dom.Element;


/** Testing class for MsgX.
 * @author Vaclav Trojan
 */
public final class TestMsgX extends STester {

	public TestMsgX() {super();}

////////////////////////////////////////////////////////////////////////////////
// methods and objects for Matej, Matej1
////////////////////////////////////////////////////////////////////////////////

	private static StringBuffer _outbf = new StringBuffer();

	private static String getSubXPath(final String path, final int startLevel) {
		if (path == null) {
			return "";
		}
		String xPath = path.trim();
		if (xPath.startsWith("/")) {
			xPath = xPath.substring(1);
		}
		xPath = xPath.trim();
		if (startLevel == 0) {
			return xPath;
		}
		int level = startLevel;
		if (level < 0) {
			if (xPath.endsWith("/")) {
				xPath = xPath.substring(0,xPath.length() - 1).trim();
			}
			while (level++ < 0) {
				int i = xPath.lastIndexOf('/');
				if (i < 0) {
					return xPath;
				}
				xPath = xPath.substring(0,i).trim();
			}
			return xPath;
		}
		while (level > 0) {
			int i = xPath.indexOf('/');
			if (i < 0) {
				return "";
			}
			xPath = xPath.substring(i + 1);
			xPath = xPath.trim();
			level--;
		}
		int i = xPath.indexOf('/');
		if (i < 0) {
			i = xPath.length();
		}
		int j = xPath.indexOf('[');
		if (j < 0 || j > i) {
			return xPath;
		}
		i = xPath.indexOf(']');
		if (i < 0) {
			throw new SRuntimeException("XML501",
				"XPath syntax error: ']' missing");
		}
		return xPath.substring(0,j).trim() + xPath.substring(i + 1).trim();
	}
	final public static void tst1(XXElement chkElem, XDValue[] params) {
			_outbf.append(getSubXPath(chkElem.getXPos(), 1)).append(" - tst1(");
			for (int i = 0; i < params.length; i++) {
				_outbf.append(i == 0 ? "" : ", ").append(params[i].toString());
			}
			_outbf.append(")");
	}
	final public static void tst2(XXElement chkElem, XDValue[] params) {
			_outbf.append(getSubXPath(chkElem.getXPos(), 1)).append(" - tst2(");
			for (int i = 0; i < params.length; i++) {
				_outbf.append(i == 0 ? "" : ", ").append(params[i].toString());
			}
			_outbf.append(")");
	}
	final public static void tst3(XXElement chkElem, XDValue[] params) {
			_outbf.append(getSubXPath(chkElem.getXPos(), 1)).append(" - tst3(");
			for (int i = 0; i < params.length; i++) {
				_outbf.append(i == 0 ? "" : ", ").append(params[i].toString());
			}
			_outbf.append(")");
	}

	@Override
	final public void test() {
		Report.setLanguage("en"); //localize
		MsgX msg = null;
		String zprava;
		XDPool xp;
		String s;
		ReportWriter cmp; //reports from comparings
		//1. vytvorime nejdriv balik definic
		try {
			String xdef =
"<xd:collection xmlns:xd='http://www.syntea.cz/xdef/3.1'>\n"+
"<xd:def name = 'fronta'\n"+
"  root='emptynode#empty | abc#EndPrgInfo | abc#Complex | abc#Complex1|*' />\n"+
"\n"+
"<xd:def name=\"emptynode\">\n"+
"  <empty/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name=\"abc\">\n"+
"  <EndPrgInfo Verze=\"fixed '2.0'\"\n"+
"              Program=\"required string(1,4)\"\n"+
"              IdProces=\"required int()\"\n"+
"              Prg=\"required string(3,3)\"\n"+
"              Date=\"optional xdatetime('d.M.yyyy')\"\n"+
"              Vysledek=\"required enum('OK','ERR')\"\n"+
"              Kanal=\"optional num(2,2)\">\n"+
"  </EndPrgInfo>\n"+
"\n"+
"  <Complex ver=\"fixed '1.1'\">\n"+
"    <inside xd:script=\"occurs 1..2; ref EndPrgInfo\"\n"+
"            Kanal=\"required num(2,2)\"/>\n"+
"    <x xd:script=\"occurs 0..1; ref emptynode#empty\" />\n"+
"    <xd:any xd:script=\"occurs 2..;\n"+
"                            options moreAttributes,moreElements\">\n"+
//"      optional\n"+
"    </xd:any>\n"+
"    optional\n"+
"  </Complex>\n"+
"\n"+
"  <Complex1 ver=\"fixed '1.1'\">\n"+
"    <inside xd:script=\"occurs 1..2; ref EndPrgInfo\"\n"+
"            Kanal=\"required num(2,2)\"/>\n"+
"    <x xd:script=\"occurs 0..1; ref emptynode#empty\" />\n"+
"    optional\n"+
"  </Complex1>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xp = XDFactory.compileXD(null,xdef);
		} catch (Exception ex) {
			fail(ex);
			xp = null;
		}

		// a ted uz muzeme pracovat se zpravou
		//2. tvorba zpravy
		try {
			msg = new MsgX("EndPrgInfo", xp, "fronta" , MsgX.MODE_WRITE);
			msg.putRootAttribute("IdProces","1234");
			msg.putRootAttribute("Program","TEST");
			msg.putRootAttribute("Prg","DKN");
			msg.putRootAttribute("Vysledek","OK");
			msg.putRootAttribute("Date","07.03.1943");
			msg.putRootAttribute("Kanal","XX");
			fail("expected exception not thrown");
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("/EndPrgInfo/@Kanal") < 0 ||
				(ex.getMessage().indexOf("XDEF511") < 0 &&
				ex.getMessage().indexOf("XDEF515") < 0) &&
				ex.getMessage().indexOf("XDEF809") < 0) {
				fail(ex);
			}
		}
		try {
			zprava = msg.getMsg();
			cmp = KXmlUtils.compareXML(zprava,
				"<EndPrgInfo IdProces=\"1234\" Program=\"TEST\" Prg=\"DKN\"" +
				" Vysledek=\"OK\" Date=\"07.03.1943\" Verze=\"2.0\"/>");
			if (cmp.errorWarnings()) {
				fail("invalid result: " + zprava);
			}
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("/EndPrgInfo/@Kanal") < 0 ||
				(ex.getMessage().indexOf("XDEF511") < 0 &&
				ex.getMessage().indexOf("XDEF515") < 0) &&
				ex.getMessage().indexOf("XDEF809") < 0) {
				fail(ex);
			}
		}

		zprava = "<EndPrgInfo IdProces=\"1234\" Program=\"TEST\" Prg=\"DKN\"" +
			" Vysledek=\"OK\" Date=\"07.03.1943\" Verze=\"2.0\"/>";
		// 3. cteni
		try {
			msg = new MsgX(zprava, xp, "fronta", MsgX.MODE_READ);
			assertEq("EndPrgInfo", msg.getMsgName());
			assertEq("TEST", msg.getAttribute("Program"));
			assertEq("1234", msg.getAttribute("IdProces"));
			assertEq("DKN", msg.getAttribute("Prg"));
			assertEq("OK", msg.getAttribute("Vysledek"));
			assertTrue(null == msg.getAttribute("Kanal"));
		} catch (Exception ex) {fail(ex);}
///*#if !JAVAX*#/
//		// 4. test get atribute z elementu
//		try {
//			msg = new MsgX(zprava, xp, "fronta", MsgX.MODE_READ);
//			Element el = msg.getRootElement();
//			assertEq("1234", el.getAttribute("IdProces"));
//			//toto musi hodit SRuntimeException!
//			el.getAttribute("YdProces");
//			fail("Exception not thrown");
//		} catch (Exception ex) {
//			if (ex instanceof SRuntimeException) {
//				SRuntimeException exx = (SRuntimeException) ex;
//				if (!"XDEF581".equals(exx.getMsgID())) {
//					fail(ex);
//				}
//			} else {
//				fail(ex);
//			}
//		}
///*#end*/
		// 5. cteni z hotoveho elementu
		try {
			Element el = msg.getRootElement();
			msg = new MsgX(el, xp, "fronta");
			assertEq("EndPrgInfo", msg.getMsgName());
			assertEq("TEST", msg.getAttribute("Program"));
			assertEq("1234", msg.getAttribute("IdProces"));
			assertEq("DKN", msg.getAttribute("Prg"));
			assertEq("OK", msg.getAttribute("Vysledek"));
			assertTrue(null == msg.getAttribute("Kanal"));
		} catch (Exception ex) {fail(ex);}

		// 6. cteni  check = true - pokus o cteni atributu xxx da exception
		try {
			msg = new MsgX(zprava, xp, "fronta", MsgX.MODE_READ);
			assertEq("EndPrgInfo", msg.getMsgName());
			assertEq("TEST", msg.getAttribute("Program"));
			s = msg.getAttribute("xxx");
			fail("expected exception not thrown: " + "xxx=" + s);
		} catch (Exception ex) {
			if (!ex.getMessage().startsWith("E XDEF581")
				|| ex.getMessage().indexOf("@xxx") < 0) {
				fail(ex);
			}
		}

		// 7. vytvoreni polozky se slozitejsi strukturou:
		try {
			StringWriter sw = new StringWriter();
			msg = new MsgX("Complex", xp, "fronta", MsgX.MODE_WRITE,
				sw, "windows-1250", true);
			msg.newElement("inside");
			msg.putAttribute("Program","TEST");
			msg.putAttribute("IdProces","1234");
			msg.putAttribute("Prg","DKN");
			msg.putAttribute("Vysledek","OK");
			msg.putAttribute("Kanal","12");
			msg.addElement();
			msg.newElement("x");
			msg.addElement();
			msg.newElement("any");
			msg.putAttribute("attr","attr1");
			msg.newElement("xx");
			msg.newElement("yy");
			msg.addText("text");
			msg.addElement();
			msg.addElement();
			msg.addElement();
			msg.newElement("any");
			msg.putAttribute("attr","attr2");
			msg.addElement();
			msg.addText("test");
			cmp = KXmlUtils.compareXML((zprava = msg.getMsg()),
				"<Complex ver=\"1.1\">"+
				"<inside Program=\"TEST\" IdProces=\"1234\" Prg=\"DKN\""+
				" Vysledek=\"OK\" Kanal=\"12\" Verze=\"2.0\"/>"+
				"<x/>"+
				"<any attr=\"attr1\"><xx><yy>text</yy></xx></any>"+
				"<any attr=\"attr2\"/>"+
				"test"+
				"</Complex>");
			assertFalse(cmp.errorWarnings(), "MSG=" + zprava);
			sw.close();
			cmp = KXmlUtils.compareElements(
				KXmlUtils.parseXml(sw.toString()).getDocumentElement(),
				KXmlUtils.parseXml(
				"<Complex ver=\"1.1\">"
				+ "<inside Program=\"TEST\" IdProces=\"1234\" Prg=\"DKN\""
				+ " Vysledek=\"OK\" Kanal=\"12\" Verze=\"2.0\"/>"
				+ "<x/>"
				+ "<any attr=\"attr1\"><xx><yy>text</yy></xx></any>"
				+ "<any attr=\"attr2\"/>"
				+ "test"
				+ "</Complex>").getDocumentElement());
			assertFalse(cmp.errorWarnings(), "RESULT=" + sw.toString());
		} catch (Exception ex) {fail(ex);}

		// 8. Cteni slozitejsi struktury
		try {
			msg = new MsgX(zprava, xp, "fronta", MsgX.MODE_READ);
			assertEq("Complex", msg.getMsgName());
			assertEq("1.1", msg.getAttribute("ver"));
			assertEq("1.1", (String) msg.getValue("@ver"));
			assertEq("TEST", (String) msg.getValue("/Complex/inside@Program"));
			assertEq("TEST", (String) msg.getValue("/Complex/inside/@Program"));
			assertEq("TEST", (String) msg.getValue("inside@Program"));
			assertEq("TEST", (String) msg.getValue("inside/@Program"));
			assertEq("1234", (String)msg.getValue("/Complex/inside/@IdProces"));
			assertEq("DKN", (String) msg.getValue("/Complex/inside/@Prg"));
			assertEq("OK", (String) msg.getValue("/Complex/inside/@Vysledek"));
			assertEq("12", (String) msg.getValue("/Complex/inside/@Kanal"));
			assertEq("attr1", (String) msg.getValue("/Complex/any[1]@attr"));
			assertEq("attr2",
				(String) msg.getValue("/Complex/any[last()]@attr"));
			assertEq("text",
				(String) msg.getValue("/Complex/any[1]/xx/yy.text()"));
			assertEq("text",
				(String) msg.getValue("/Complex/any[1]/xx/yy/text()"));
			assertEq("text", msg.getText("/Complex/any[1]/xx/yy"));
			assertEq("test", (String) msg.getValue("/Complex.text()"));
			assertEq("test", (String) msg.getValue("/Complex/text()"));
			assertEq("test", msg.getRootText());
		} catch (Exception ex) {fail(ex);}

		// 9. Cteni nepovoleneho atributu uvnitr slozitejsi struktury.
		try {
			msg = new MsgX(zprava, xp, "fronta", MsgX.MODE_READ);
			s = (String)msg.getValue("/Complex/inside@xxx");
			fail("expected exception not thrown: "+ "xxx=" + s);
		} catch (Exception ex) {
			if (!ex.getMessage().startsWith("E XDEF581")
				|| ex.getMessage().indexOf("@xxx") < 0) {
				fail(ex);
			}
		}

		// 10. chyba pri pridani nedefinovaneho elementu
		try {
			msg = new MsgX("EndPrgInfo", xp, "fronta", MsgX.MODE_WRITE);
			msg.putAttribute("IdProces","1234");
			msg.putAttribute("Program","TEST");
			msg.putAttribute("Prg","DKN");
			msg.putAttribute("Vysledek","OK");
			msg.newElement("foo"); //here should be thrown the exception
			msg.addElement();
			fail("expected exception not thrown\n MSG= "	+ msg.getMsg());
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("'foo'") < 0 ||
				ex.getMessage().indexOf("/EndPrgInfo") < 0) {
				fail(ex);
			}
		}

		// 11. vytvoreni polozky se slozitejsi strukturou, hlaseni chybi
		try {
			msg = new MsgX("Complex", xp, "fronta", MsgX.MODE_WRITE);
			msg.newElement("inside");
			msg.putAttribute("Program","TEST");
			msg.putAttribute("IdProces","1234");
			msg.putAttribute("Prg","DKN");
			msg.putAttribute("Vysledek","OK");
			msg.putAttribute("Kanal","12");
			msg.addElement();
			msg.newElement("x");
			msg.addElement();
			msg.newElement("any");
			msg.putAttribute("attr","attr1");
			msg.newElement("xx");
			msg.newElement("yy");
			msg.addText("text");
			msg.addElement();
			msg.addElement();
			msg.addElement();
			msg.newElement("any");
			msg.putAttribute("attr","attr2");
			msg.addElement();
			msg.addText("test");
			cmp = KXmlUtils.compareXML((zprava = msg.getMsg()),
				"<Complex ver=\"1.1\">"+
				"<inside Program=\"TEST\" IdProces=\"1234\" Prg=\"DKN\""+
				" Vysledek=\"OK\" Kanal=\"12\" Verze=\"2.0\"/>"+
				"<x/>"+
				"<any attr=\"attr1\"><xx><yy>text</yy></xx></any>"+
				"<any attr=\"attr2\"/>"+
				"test"+
				"</Complex>");
			assertFalse(cmp.errorWarnings(), "MSG=" + zprava);
		} catch (Exception ex) {fail(ex);}

		// 12. vytvoreni polozky se slozitejsi strukturou, hlaseni chyby:
		try {
			msg = new MsgX("Complex1", xp, "fronta", MsgX.MODE_WRITE);
			msg.newElement("inside");
			msg.putAttribute("Program","TEST");
			msg.putAttribute("IdProces","1234");
			msg.putAttribute("Prg","DKN");
			msg.putAttribute("Vysledek","OK");
			msg.putAttribute("Kanal","12");
			msg.addElement();
			msg.newElement("x");
			msg.addElement();
			try {
				msg.addElementToRoot(msg.createElement("any"));
				fail("a) unreported error 'undef. element'");
			} catch (Exception ex) {
				String m = ex.getLocalizedMessage();
				if (m.indexOf("'any'") < 0) {
					fail(ex);
				}
			}
			try {
				msg.newElement("any");
				fail("b) unreported error undef. element");
			} catch (Exception ex) {
				String m = ex.getLocalizedMessage();
				if (m.indexOf("'any'") < 0) {
					fail(ex);
				}
			}
			try {
				msg.putAttribute("attr","attr2");
				fail("unreported error undef. attr");
			} catch (Exception ex) {
				String m = ex.getLocalizedMessage();
				if (m.indexOf("'any'") < 0) {
					fail(ex);
				}
			}
			try {
				msg.addElement();
				fail("unreported error undef. attr");
			} catch (Exception ex) {
				String m = ex.getLocalizedMessage();
				if (m.indexOf("'any'") < 0) {
					fail(ex);
				}
			}
		} catch (Exception ex) {fail(ex);}

		// 13. vytvoreni polozky se slozitejsi strukturou: chybi element
		try {
			xp = XDFactory.compileXD(null,
"<xd:def xmlns:xd = 'http://www.syntea.cz/xdef/3.1'\n"+
"        xd:name = \"test\" xd:root = \"root\" >\n"+
"  <root>\n"+
"    <a>\n"+
"      <b xd:script = \"occurs 0..;\"/>\n"+
"      <c/>\n"+
"    </a>\n"+
"  </root>\n"+
"</xd:def>");
			msg = new MsgX("root", xp, "test", MsgX.MODE_WRITE);
			msg.newElement("a");
			msg.newElement("b");
			msg.addElement();
			msg.newElement("b");
			msg.addElement();
			msg.addElement();
			msg.getMsg();
			fail("unreported error: missing c");
		} catch (Exception ex) {
			String m = ex.getLocalizedMessage();
			if (m.indexOf("XDEF539") < 0) {
				fail(ex);
			}
		}

		// 14. Test hodnoty
		try {
			xp = XDFactory.compileXD(null,
"<xd:def xmlns:xd = 'http://www.syntea.cz/xdef/3.1'\n"+
"        xd:root = \"Test\" xd:name = \"test\">\n"+
"  <Test att1 = \"optional int()\"\n"+
"        att2 = \"optional string(3,4)\" />\n"+
"</xd:def>");
			msg = new MsgX("Test", xp, "test", MsgX.MODE_WRITE);
			msg.putRootAttribute("att1", "a");
			fail("unreported error: Integer format error");
		} catch (Exception ex) {
			String m = ex.getLocalizedMessage();
			if (m.indexOf("XDEF508") < 0 && m.indexOf("XDEF809") < 0 &&
				m.indexOf("XDEF515") < 0) {
				fail(ex);
			}
		}

		try { // 14.a Test hodnoty
			msg = new MsgX("Test", xp, "test", MsgX.MODE_WRITE);
			msg.putRootAttribute("att2", "a");
			fail("unreported error: String is too short");
		} catch (Exception ex) {
			String m = ex.getLocalizedMessage();
			if (m.indexOf("XDEF506") < 0 && m.indexOf("XDEF814") < 0 &&
				m.indexOf("XDEF515") < 0) {
				fail(ex);
			}
		}
		try { // 14.b Test hodnoty - OK
			msg = new MsgX("Test", xp, "test", MsgX.MODE_WRITE);
			msg.putRootAttribute("att2", "abc");
		} catch (Exception ex) {
			fail("Unexpected exception: " + ex);
		}
		testMatej();
		testMatej1();
	}

	private void testMatej() {
		Report.setLanguage("en"); //localize
		final String xdef1 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'\n"+
"  script='options setAttrUpperCase' root='MyTest' name='MyTest'>\n"+
"   <MyTest>\n"+
"      <yTest xd:script=\"occurs 1..; ref xTest\"/>\n"+
"   </MyTest>\n"+
"   <xTest xd:script=\"init tst1('aa');\"\n"+
"         Ver=\"fixed '1.2'\"\n"+
"         Time=\"optional\">\n"+
"     <inside xd:script=\"init {tst1('bb');tst2('bb');}finally tst3('bb');\"\n"+
"          attr=\"optional\">\n"+
"     </inside>\n"+
"   </xTest>\n"+
"</xd:def>";
		final String xdef2 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'\n"+
"     script='options setAttrUpperCase' name='Log'>\n"+
"   <Log xd:script=\"init {tst1();tst2();}\"\n"+
"      Ver=\"fixed '1.2'\"\n"+
"      IdProces=\"required num()\"\n"+
"      Time=\"required\"\n"+
"      Program=\"required\"\n"+
"      Verze=\"required\"\n"+
"      Misto=\"required\"\n"+
"      Code=\"required\"\n"+
"      Subject=\"required\"\n"+
"      Body=\"required\" />\n"+
"</xd:def>";
		final String xdef3 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' name='DN'>\n"+
"  <AnnulMsg xd:script=\"init {tst1();tst2();} ref AnnulMsg#AnnulMsg\" />\n"+
"  <Log xd:script=\"init {tst1();tst2();} ref Log#Log\" />\n"+
"  <TimeMsg xd:script=\"init {tst1();tst2();} ref TimeMsg#TimeMsg\" />\n"+
"</xd:def>";
		final String xdef4 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'\n"+
" script='options setAttrLowerCase' root='DN#Log|MyTest#MyTest' name='Q_LOG'/>";
		final String xdef5 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1'\n"+
"     root='DN#TimeMsg | DN#AnnulMsg' name='Q_CTM' />";
		final String xdef6 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' name='AnnulMsg'>\n"+
"   <AnnulMsg\n"+
"      xd:script=\"init {tst1();tst2();} finally tst2();\"\n"+
"      Ver=\"fixed '1.2'\"\n"+
"      Class=\"required\"\n"+
"      Element=\"required\"\n"+
"      Time=\"optional\" />\n"+
"</xd:def>";
		final String xdef7 =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' name='TimeMsg'>\n"+
"   <TimeMsg xd:script=\"init {tst1();tst2();}\"\n"+
"      Ver=\"fixed '1.2'\"\n"+
"      Class=\"required\"\n"+
"      Element=\"required\"\n"+
"      Time=\"required\"\n"+
"      Queue=\"required\"\n"+
"      Recurr=\"optional\" >\n"+
"     <xd:any xd:script=\"options moreAttributes,moreText,moreElements\"/>\n"+
"   </TimeMsg>\n"+
"</xd:def>";
		XDPool xp;
		try {
			Properties props = (Properties) System.getProperties().clone();
			props.setProperty("xdef.warnings", "true");
			props.setProperty("xdef.externalmode", "both");
			xp = XDFactory.compileXD(props,
				new String[]{xdef1, xdef2, xdef3, xdef4, xdef5, xdef6, xdef7},
				getClass());
		} catch (Exception ex) {
			fail(ex);
			return;
		}
		MsgX msg;
		String zprava = null;

		//mame defpool, muzeme pracovat se zpravou
		//1. tvorba "Log" zpravy
		try {
			msg = new MsgX("Log", xp, "Q_LOG", MsgX.MODE_WRITE);
			msg.putRootAttribute("IdProces","1234");
			msg.putRootAttribute("Time","10:30");
			msg.putRootAttribute("Program","TEST");
			msg.putRootAttribute("Verze","2.0");
			msg.putRootAttribute("Misto","Praha");
			msg.putRootAttribute("Code","1A");
			msg.putRootAttribute("Subject","subj");
			msg.putRootAttribute("Body","body");
			zprava = msg.getMsg();
			if (KXmlUtils.compareXML(
				"<Log IdProces=\"1234\" Time=\"10:30\" Program=\"TEST\""+
				" Verze=\"2.0\" Misto=\"PRAHA\" Code=\"1A\""+
				" Subject=\"SUBJ\" Body=\"BODY\" Ver=\"1.2\"/>",
				zprava).errorWarnings()) {
				fail(zprava);
			}
		} catch (Exception ex) {
			fail("MSG=" + zprava + "\n"	+ ex);
		}
		//2. tvorba "TimeMsg" zpravy
		try {
			msg = new MsgX("TimeMsg", xp, "Q_CTM", MsgX.MODE_WRITE);
			msg.putRootAttribute("Class", "1");
			msg.putRootAttribute("Element", "elem");
			msg.putRootAttribute("Time", "10:30");
			msg.putRootAttribute("Queue", "OUEUE");
			//pridame vnitrni element:
			msg.newElement("xxx");
			msg.putAttribute("attr","TEST");
			msg.addText("text");
			msg.addElement();

			zprava = msg.getMsg();
			if (KXmlUtils.compareXML(
				"<TimeMsg Class=\"1\" Element=\"elem\" Time=\"10:30\""
				+ " Queue=\"OUEUE\" Ver=\"1.2\">"
				+ "<xxx attr=\"TEST\">text</xxx></TimeMsg>",
				zprava).errorWarnings()) {
				fail(zprava);
			}
			if (new MsgX(zprava, xp, "Q_CTM", MsgX.MODE_READ) == null) {
				fail();
			}
		} catch (Exception ex) {
			fail("MSG=" + zprava + "\n"+ ex);
		}
		//3. tvorba "MyMsg" zpravy
		try {
			msg = new MsgX("MyTest", xp, "MyTest", MsgX.MODE_WRITE);
			msg.newElement("yTest");
			msg.putAttribute("Time", "10:30");
			//pridame vnitrni element:
			msg.newElement("inside");
			msg.putAttribute("attr","TEST");
//			msg.addText("text");
			msg.addElement();
			msg.addElement();

			msg.newElement("yTest");
			msg.putAttribute("Time", "10:30");
			//pridame vnitrni element:
			msg.newElement("inside");
			msg.putAttribute("attr","TEST");
//			msg.addText("text");
			msg.addElement();
			msg.addElement();

			zprava = msg.getMsg();
			if (!("<MyTest><yTest Time=\"10:30\" Ver=\"1.2\">"
				+ "<inside attr=\"TEST\"/>"
				+ "</yTest>"
				+ "<yTest Time=\"10:30\" Ver=\"1.2\">"
				+ "<inside attr=\"TEST\"/></yTest></MyTest>").equals(zprava)) {
				fail(zprava);
			}
			if (new MsgX(zprava, xp, "MyTest", MsgX.MODE_READ) == null) {
				fail();
			}
		} catch (Exception ex) {
			fail("MSG=" + zprava + "\n"+ ex);
		}
		if (!(" - tst1()"
			+ " - tst2()"
			+ " - tst1()"
			+ " - tst2()"
			+ " - tst1()"
			+ " - tst2()"
			+ "yTest - tst1(aa)"
			+ "yTest/inside[1] - tst1(bb)"
			+ "yTest/inside[1] - tst2(bb)"
			+ "yTest/inside[1] - tst3(bb)"
			+ "yTest - tst1(aa)"
			+ "yTest/inside[1] - tst1(bb)"
			+ "yTest/inside[1] - tst2(bb)"
			+ "yTest/inside[1] - tst3(bb)"
			+ "yTest - tst1(aa)"
			+ "yTest/inside[1] - tst1(bb)"
			+ "yTest/inside[1] - tst2(bb)"
			+ "yTest/inside[1] - tst3(bb)"
			+ "yTest - tst1(aa)"
			+ "yTest/inside[1] - tst1(bb)"
			+ "yTest/inside[1] - tst2(bb)"
			+ "yTest/inside[1] - tst3(bb)").equals(_outbf.toString())) {
			fail(_outbf.toString());
		}
	}

	private void testMatej1() {
		String xdef;
		XDPool xp;
		try {
			xdef =
  "<xd:collection xmlns:xd='http://www.syntea.cz/xdef/3.1' >\n"
+ "<xd:def xd:script=\"options setAttrUpperCase\"\n"
+ "     xd:root=\"MyTest\" xd:name=\"MyTest\">\n"
+ "   <MyTest>\n"
+ "      <yTest xd:script=\"occurs 1..; ref xTest\"/>\n"
+ "   </MyTest>\n"
+ "   <xTest\n"
+ "         xd:script=\"init tst1('aa');\"\n"
+ "         Ver=\"fixed '1.2'\"\n"
+ "         Time=\"optional\">\n"
+ "     <inside\n"
+ "            xd:script=\"init {tst1('bb');tst2('bb');} finally tst3('bb')\"\n"
+ "            attr=\"optional\">\n"
+ "     </inside>\n"
+ "   </xTest>\n"
+ "</xd:def>\n"
+ "\n"
+ "<xd:def xd:script=\"options setAttrUpperCase\" xd:name=\"Log\">\n"
+ "   <Log\n"
+ "      xd:script=\"init {tst1();tst2();}\"\n"
+ "      Ver=\"fixed '1.2'\"\n"
+ "      IdProces=\"required num()\"\n"
+ "      Time=\"required\"\n"
+ "      Program=\"required\"\n"
+ "      Verze=\"required\"\n"
+ "      Misto=\"required\"\n"
+ "      Code=\"required\"\n"
+ "      Subject=\"required\"\n"
+ "      Body=\"required\"\n"
+ "   />\n"
+ "</xd:def>\n"
+ "\n"
+ "<xd:def xd:name=\"DN\">\n"
+ "  <AnnulMsg xd:script=\"init {tst1();tst2();} ref AnnulMsg#AnnulMsg\" />\n"
+ "  <Log xd:script=\"init {tst1();tst2();} ref Log#Log\" />\n"
+ "  <TimeMsg xd:script=\"init {tst1();tst2();} ref TimeMsg#TimeMsg\" />\n"
+ "</xd:def>\n"
+ "\n"
+ "<xd:def \n"
//+ "     xd:script=\"options setAttrValuesUpper\"\n"
+ "     xd:script=\"options setAttrLowerCase\"\n"
+ "     xd:root=\"DN#Log | MyTest#MyTest\"\n"
//+ "     xd:script=\"init {tst1(),tst2()}\"\n"
+ "     xd:name=\"Q_LOG\">\n"
+ "</xd:def>\n"
+ "\n"
+ "<xd:def xd:root=\"DN#TimeMsg | DN#AnnulMsg\" xd:name=\"Q_CTM\"/>\n"
+ "\n"
+ "<xd:def xd:name=\"AnnulMsg\">\n"
+ "   <AnnulMsg\n"
+ "      xd:script=\"init {tst1();tst2();} finally tst2();\"\n"
+ "      Ver=\"fixed '1.2'\"\n"
+ "      Class=\"required\"\n"
+ "      Element=\"required\"\n"
+ "      Time=\"optional\"\n"
+ "   />\n"
+ "</xd:def>\n"
+ "\n"
+ "<xd:def xd:name=\"TimeMsg\">\n"
+ "   <TimeMsg\n"
+ "      xd:script=\"init {tst1();tst2();}\"\n"
+ "      Ver=\"fixed '1.2'\"\n"
+ "      Class=\"required\"\n"
+ "      Element=\"required\"\n"
+ "      Time=\"required\"\n"
+ "      Queue=\"required\"\n"
+ "      Recurr=\"optional\"\n"
+ "   >\n"
+ "     <xd:any xd:script=\"occurs 0..1\" attr=\"required\">\n"
+ "       required\n"
+ "     </xd:any>\n"
+ "   </TimeMsg>\n"
+ "</xd:def>\n"
+ "</xd:collection>";
			xp = XDFactory.compileXD(null, xdef, getClass());
		} catch (Exception ex) {
			fail(ex);
			return;
		}
		MsgX msg;
		String zprava;

		//mame defpool, muzeme pracovat se zpravou
		//1. tvorba "Log" zpravy
		try {
			msg = new MsgX("Log", xp, "Q_LOG", MsgX.MODE_WRITE);
			msg.putRootAttribute("IdProces","1234");
			msg.putRootAttribute("Time","10:30");
			msg.putRootAttribute("Program","TEST");
			msg.putRootAttribute("Verze","2.0");
			msg.putRootAttribute("Misto","Praha");
			msg.putRootAttribute("Code","1A");
			msg.putRootAttribute("Subject","subj");
			msg.putRootAttribute("Body","body");
			zprava = msg.getMsg();
			if (KXmlUtils.compareXML(
				"<Log IdProces=\"1234\" Time=\"10:30\" Program=\"TEST\""+
				" Verze=\"2.0\" Misto=\"PRAHA\" Code=\"1A\""+
				" Subject=\"SUBJ\" Body=\"BODY\" Ver=\"1.2\"/>",
				zprava).errorWarnings()) {
				fail(zprava);
			}
			StringWriter baos = new StringWriter();
			msg = new MsgX("Log", xp, "Q_LOG", MsgX.MODE_WRITE,
				baos, "windows-1250", false);
			msg.putRootAttribute("IdProces","1234");
			msg.putRootAttribute("Time","10:30");
			msg.putRootAttribute("Program","Test");
			msg.putRootAttribute("Verze","2.0");
			msg.putRootAttribute("Misto","Praha");
			msg.putRootAttribute("Code","1a");
			msg.putRootAttribute("Subject","Subj");
			msg.putRootAttribute("Body","Body");
			msg.getRootElement();
			baos.close();
			zprava = baos.toString();
			if (KXmlUtils.compareXML(
				"<Log IdProces=\"1234\" Time=\"10:30\" Program=\"TEST\""+
				" Verze=\"2.0\" Misto=\"PRAHA\" Code=\"1A\""+
				" Subject=\"SUBJ\" Body=\"BODY\" Ver=\"1.2\"/>",
				zprava).errorWarnings()) {
				fail(zprava);
			}
		} catch (Exception ex) {fail(ex);}
		//2. tvorba "TimeMsg" zpravy
		try {
			msg = new MsgX("TimeMsg", xp, "Q_CTM", MsgX.MODE_WRITE);
			msg.putRootAttribute("Class", "1");
			msg.putRootAttribute("Element", "elem");
			msg.putRootAttribute("Time", "10:30");
			msg.putRootAttribute("Queue", "OUEUE");
			//pridame vnitrni element:
			msg.newElement("xxx");
			msg.putAttribute("attr","TEST");
			msg.addText("text");
//			msg.putAttribute("attr","TEST");
//			msg.putAttribute("att","TEST");
			msg.addElement();
			zprava = msg.getMsg();
			if (KXmlUtils.compareXML(
				"<TimeMsg Class=\"1\" Element=\"elem\" Time=\"10:30\""
				+ " Queue=\"OUEUE\" Ver=\"1.2\">"
				+ "<xxx attr=\"TEST\">text</xxx></TimeMsg>",
				zprava).errorWarnings()) {
				fail(zprava);
			}
			if (new MsgX(zprava, xp, "Q_CTM",MsgX.MODE_READ) == null) {
				fail();
			}
		} catch (Exception ex) {fail(ex);}

		//3. tvorba "MyMsg" zpravy
		try {
			msg = new MsgX("MyTest", xp, "MyTest", MsgX.MODE_WRITE);
			msg.newElement("yTest");
			msg.putAttribute("Time", "10:30");
			//pridame vnitrni element:
			msg.newElement("inside");
			msg.putAttribute("attr","TEST");
//			msg.addText("text");
			msg.addElement();
			msg.addElement();

			msg.newElement("yTest");
			msg.putAttribute("Time", "10:30");
			//pridame vnitrni element:
			msg.newElement("inside");
			msg.putAttribute("attr","TEST");
//			msg.addText("text");
			msg.addElement();
			msg.addElement();
			zprava = msg.getMsg();
			if (!("<MyTest><yTest Time=\"10:30\" Ver=\"1.2\">"
				+ "<inside attr=\"TEST\"/>"
				+ "</yTest>"
				+ "<yTest Time=\"10:30\" Ver=\"1.2\">"
				+ "<inside attr=\"TEST\"/></yTest></MyTest>").equals(zprava)) {
				fail(zprava);
			}
			if (new MsgX(zprava, xp, "MyTest", MsgX.MODE_READ) == null) {
				fail();
			}
		} catch (Exception ex) {fail(ex);}

		//3. tvorba "MyMsg" zpravy
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' root='a' >" +
"  <a b='? num(4); onFalse removeText();' />" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			msg = new MsgX("a", xp, "" , MsgX.MODE_WRITE);
			msg.putRootAttribute("b","0");
			assertEq("<a/>", msg.getMsg());
		} catch (Exception ex) {fail(ex);}
		_outbf = null;
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
