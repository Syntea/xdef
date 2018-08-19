/*
 * Copyright 2015 Syntea software group a.s.
 *
 * File: TestDebugGUI.java, created 2015-04-20.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package test.xdef;

import cz.syntea.xdef.XDBuilder;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDOutput;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ReportWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import org.w3c.dom.Element;

/** Provides testing of XDef debug mode and editing in display mode.
 * @author Trojan
 */
public final class TestDebugGUI extends Tester {

	public TestDebugGUI() {super(); setChkSyntax(false);}

	@Override
	public void test() {
		Report.setLanguage("en"); //localize
		String xdef;
		XDPool xp;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		ByteArrayOutputStream baos;
		XDOutput out;
		String s;
		Element el;

//		// set external editor
//		setProperty(XDConstants.XDPROPERTY_XDEF_EDITOR, "xdplugin.XdPlugin");

		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef.warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false

//		setProperty(XDConstants.XDPROPERTY_DEBUG_EDITOR, // debug editor
//			"cz.syntea.xdef.impl.debug.ChkGUIDebug"); // class name
//		setProperty(XDConstants.XDPROPERTY_XDEF_EDITOR, // xdef editor
//			"cz.syntea.xdef.impl.debug.ChkGUIDisplay"); // class name
//		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);
//		try {
//			// XScript breakpoints
//			xdef =
//"<x:collection xmlns:x='" + XDEFNS + "'>\n"+                 	//01
//"<x:def name = 'a' root = 'a'>\n"+								//02
//"<a a = \"required eq('a');\n"+									//03
//"         create 'a';\n"+										//04
//"         finally {\n"+											//04
//"           int i = 1; int j = 2;\n"+							//05
//"           s = x(1,'s');\n"+									//06
//"           s += (i+j);\n"+										//08
//"           i = 0;\n"+											//09
//"           j = 0;\n"+											//10
//"         }\" />\n"+											//11
//"</x:def>\n"+													//12
//"<x:def>\n"+													//13
//" <x:declaration>\n"+											//14
//"  String x(int n, String s){\n"+								//15
//"    String result = s;\n"+										//16
//"    for (int i = 0; i LT n; i++) {\n"+							//17
//"      result += ',' + i;\n"+									//18
//"    }\n"+														//19
//"    return result;\n"+											//20
//"  }\n"+														//21
//"  String s = 'a';\n"+											//22
//" </x:declaration>\n"+											//23
//"</x:def>\n"+													//24
//"</x:collection>";												//25
//			xp = compile(xdef);
//			XMDebugInfo xm = xp.getDebugInfo();
//			// create mode
//			xd = xp.createXDDocument("a");
//			XMStatementInfo[] si = xm.getStatementInfo(4, "a");
//			for (int i = 0; i < si.length; i++) {
//				xd.getDebugger().setStopAddr(si[i].getAddr());
//			}
//			xml = "<a a='a'/>";
//			xd.setXDContext(xml);
//			create(xd, "a", reporter);
//			// processing mode
//			xd = xp.createXDDocument("a");
//			xm = xp.getDebugInfo();
//			si = xm.getStatementInfo(3, "a");
//			for (int i = 0; i < si.length; i++) {
//				xd.getDebugger().setStopAddr(si[i].getAddr());
//			}
//			si = xm.getStatementInfo(16, "");
//			for (int i = 0; i < si.length; i++) {
//				xd.getDebugger().setStopAddr(si[i].getAddr());
//			}
//			si = xm.getStatementInfo(18, "");
//			for (int i = 0; i < si.length; i++) {
//				xd.getDebugger().setStopAddr(si[i].getAddr());
//			}
//			parse(xd, xml, reporter);
//			// XPos breakpoints
//			xdef =
//"<x:def xmlns:x='" + XDEFNS + "' root='a'>\n"+
//"<a a = \"required;\">\n"+
//"  <b x:script='*'/>\n"+
//"</a>\n"+
//" <x:declaration>\n"+
//"  String s = 'a';\n"+
//" </x:declaration>\n"+
//"</x:def>";
//			xp = compile(xdef);
//			// create mode
//			xd = xp.createXDDocument();
//			xd.getDebugger().setXpos(XDDebug.CREATE + "/a");
//			xd.getDebugger().setXpos(XDDebug.CREATE + "/a/@a");
//			xd.getDebugger().setXpos(XDDebug.FINALLY + "/a/b[2]");
//			xml = "<a a = 'a'><b/><b/><b/><b/><b/></a>";
//			xd.setXDContext(xml);
//			create(xd, "a", reporter);
////			// processing mode
//			parse(xd, xml, reporter);
//		} catch (Exception ex) {fail(ex);}
		try { // trace, pause
			xdef =
"<xd:def xmlns:xd = '" + XDEFNS + "' root = 'a'>\n"+
" <a a='required'>\n"+
"   <b xd:script = \"finally {\n"+
"      trace(xpath('../@a').toString());\n"+
"      trace(now().toString());\n"+
"      trace('this'.toString());\n"+
"      Regex x = compilePattern('[A-Z]');\n"+
"      trace(x.toString());\n"+
"      RegexResult r = x.getMatcher('DCBA');\n"+
"      trace(r.toString());\n"+
"      }\" />\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a a='1'> <b/></a>";
			Properties props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
				XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
			props.setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
				XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
//			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef.warnings
//				XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
			XDBuilder xb = XDFactory.getXDBuilder(props);
//			ReportWriter frw = new FileReportWriter("C:/temp/rep.rep");
			ReportWriter frw = new ArrayReporter();
			xb.setReporter(frw);
			xb.setSource(xdef, "Test1");
			xp = xb.compileXD();
System.out.println(frw.getReportReader().printToString());
			xd = xp.createXDDocument();
			baos = new ByteArrayOutputStream();
			out = XDFactory.createXDOutput(new OutputStreamWriter(baos), false);
			xd.setStdOut(out);
			xd.xparse(xml, null);
			out.close();
			assertEq("", baos.toString());
		} catch (Exception ex) {fail(ex);}
//		try {
//			xdef =
//"<xd:def xmlns:xd='" + XDEFNS + "' root='SouborY1A'>\n"+
//" <SouborY1A\n"+
//"    Davka        = \"required\"\n"+
//"    ZeDne        = \"required\" > \n"+
//"    <a  xd:script = \"finally {\n"+
//"      Container c = xpath('../@Davka');\n"+
//"		String error; \n"+
//"		error = '._OK._';\n"+
//"		trace(error);\n"+
//"		trace(replace(error, '._', '???'));\n"+
//"		trace(replaceFirst(error, '._', '???'));\n"+
//"		int i = 1; if (2*i EQ 1) trace('ERROR');\n"+
//"		/*comment*/\n"+
//"		pause(xpath('..'));\n"+
//"		trace(xpath('..'));\n"+
//"		outln('len=' + c.getLength()\n"+
//"		+', typ=' + c.getItemType(0)\n"+
//"		+', value=' + getText(c,0));\n"+
//"      }\" /> \n"+
//"   string();\n"+
//" </SouborY1A>\n"+
//"</xd:def>";
//			xml =
//"<SouborY1A Davka='davka' ZeDne='1.1.99'><a/>text</SouborY1A>";
//			xd = compile(xdef).createXDDocument();
//			baos = new ByteArrayOutputStream();
//			out = XDFactory.createXDOutput(new OutputStreamWriter(baos), false);
//			xd.setStdOut(out);
//			xd.xparse(xml, null);
//			out.close();
//		} catch (Exception ex) {fail(ex);}
//		try {
//			xdef =
//"<x:collection xmlns:x='" + XDEFNS + "'>\n"+
//"<x:def name         = 'a'\n"+
//"       root         = 'a|*'\n"+
//"       impl-version = '1.0.0'\n"+
//"       impl-date    = '1.11.2000'\n"+
//"       script       = 'options ignoreEmptyAttributes' >\n"+
//"<x:declaration>\n"+
//"   String $verze = '1.23';\n"+
//"   String $x = '???';\n"+
//"  void $myPause(String i, int j, Datetime k) {\n"+
//"    pause(i);\n"+
//"  }\n"+
//"</x:declaration>\n"+
//"<a>\n"+
//"<b x:script = 'occurs 1..'\n"+
//"   a = \"required eq('b'); onFalse" +
//	" pause('Error setText to b!');\n"+
//"       finally {\n"+
//"           String s = getImplProperty('version');\n"+
//"           trace('getImplProperty(\\'version\\'): ' + s);\n"+
//	"       }\" />\n"+
//"</a>\n"+
//"</x:def>\n"+
//"<x:def name         = 'b'\n"+
//"       impl-version = '2.0.0'\n"+
//"       impl-date    = '2.11.2000' />\n"+
//"</x:collection>";
//			xp = compile(xdef, getClass());
//			xml = "<a><b a = 'b' /><b a = 'c' /></a>";
//			xd = xp.createXDDocument("a");
//			parse(xd, xml, reporter);
//			if (KXmlUtils.compareXML("<a><b a='b'/><b a='c'/></a>",
//				KXmlUtils.nodeToString(xd.getElement())).errorWarnings()) {
//				fail(KXmlUtils.nodeToString(xd.getElement()));
//			}
//		} catch (Exception ex) {fail(ex);}
//		try {// check impl properties and "*" in the root selection
//			xdef =
//"<x:collection xmlns:x='" + XDEFNS + "'>\n"+
//"<x:def name = 'a' root = 'a|*'\n"+
//"       impl-version = '1.0.0'\n"+
//"       impl-date    = '1.11.2000'\n"+
//"       script       = 'options ignoreEmptyAttributes' >\n"+
//"<a a = \"required eq('a');\n"+
//"         finally {\n"+
//"           String s = getImplProperty('version');\n"+
//"           trace(s);\n"+
//"           if (s NE '1.0.0') {\n"+
//"             throw new Exception('Error1: ' + s);\n"+
//"           }\n"+
//"           s = getImplProperty('x');\n"+
//"           if (s NE '') {\n"+
//"             throw new Exception('Error2: ' + s);\n"+
//"           }\n"+
//"           s = getImplProperty('b','version');\n"+
//"           trace(s);\n"+
//"           if (s NE '2.0.0') {\n"+
//"             throw new Exception('Error3: ' + s);\n"+
//"           }\n"+
//"           s = getImplProperty('x','version');\n"+
//"           if (s NE '') {\n"+
//"             throw new Exception('Error4: ' + s);\n"+
//"           }\n"+
//"           s = getImplProperty('b','x');\n"+
//"           if (s NE '') {\n"+
//"             throw new Exception('Error5: ' + s);\n"+
//"           }\n"+
//"           s = 'abcd';\n"+
//"           pause(s);\n"+
//"         }\" />\n"+
//"</x:def>\n"+
//"<x:def name='b' impl-version='2.0.0' impl-date='2.11.2000'/>\n"+
//"</x:collection>";
//			xp = compile(xdef);
//			xml = "<a a = 'b' />";
//			xd = xp.createXDDocument("a");
//			parse(xd, xml, reporter);
//			if (reporter.errors()) {
//				s = reporter.getReport().getMsgID();
//				assertTrue("XDEF515".equals(s) || "XDEF809".equals(s), s);
//			} else {
//				fail();
//			}
//			// here runs as "*" - any
//			xml = "<b a='b' b='a'>x<a/><a x='y'><a y='x'/></a>y</b>";
//			xd = xp.createXDDocument("a");
//			parse(xd, xml, reporter);
//			if (reporter.errorWarnings()) {
//				fail("\n" + ReportPrinter.printListing(xml, reporter));
//				reporter.checkAndThrowErrorWarnings();
//			} else {
//				el = xd.getDocument().getDocumentElement();
//				if (KXmlUtils.compareXML(xml,
//					KXmlUtils.nodeToString(el)).errorWarnings()) {
//					fail(KXmlUtils.nodeToString(
//						xd.getDocument().getDocumentElement()));
//				}
//			}
//		} catch (Exception ex) {fail(ex);}
		resetTester();
	}

	/** Start test from command line. Print results on system output.
	 * @param args The parameter is ignored.
	 */
	final public static void main(final String[] args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}