package test.xdef;

import test.XDTester;
import org.xdef.XDConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDebug;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMStatementInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import org.w3c.dom.Element;
import org.xdef.XDValueID;

/** TestDebug provides testing of debug mode.
 * @author Trojan
 */
public final class TestDebug extends XDTester {

	public TestDebug() {super();}

	@Override
	public void test() {
		String xdef;
		XDPool xp;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		ByteArrayOutputStream baos, bos1;
		XDOutput out;
		String s;
		PrintStream ps;
		Element el;
		setProperty(XDConstants.XDPROPERTY_XDEF_EDITOR, // debug editor
			"org.xdef.impl.debug.ChkGUIDebug"); // class name
		setProperty(XDConstants.XDPROPERTY_DEBUG_EDITOR, // debug editor
			"org.xdef.impl.debug.ChkGUIDebug"); // class name
//		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		try {
			// Xscript breakpoints
			xdef =
"<x:collection xmlns:x='" + _xdNS + "'>\n"+ 	                //01
"<x:def name = 'a' root = 'a'>\n"+								//02
"<a a = \"required eq('a');\n"+									//03
"         create 'a';\n"+										//04
"         finally {\n"+											//04
"           int i = 1; int j = 2;\n"+							//05
"           s = x(1,'s');\n"+									//06
"           s += (i+j);\n"+										//08
"           i = 0;\n"+											//09
"           j = 0;\n"+											//10
"         }\" />\n"+											//11
"</x:def>\n"+													//12
"<x:def>\n"+													//13
" <x:declaration scope='global'>\n"+							//14
"  String x(int n, String s){\n"+								//15
"    String result = s;\n"+										//16
"    for (int i = 0; i LT n; i++) {\n"+							//17
"      result += ',' + i;\n"+									//18
"    }\n"+														//19
"    return result;\n"+											//20
"  }\n"+														//21
"  String s = 'a';\n"+											//22
" </x:declaration>\n"+											//23
"</x:def>\n"+													//24
"</x:collection>";												//25
			xp = compile(xdef);
			XMDebugInfo xm = xp.getDebugInfo();
			XMStatementInfo[] si = xm.getStatementInfo(4, "a");
			// create mode
			xd = xp.createXDDocument("a");
			for (int i = 0; i < si.length; i++) {
				xd.getDebugger().setStopAddr(si[i].getAddr());
			}
			xd.getDebugger().setInDebug(
				new ByteArrayInputStream("context\ngo".getBytes()));
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			xml = "<a a='a'/>";
			xd.setXDContext(xml);
			xd.xcreate("a", reporter);
			ps.close();
			assertNoErrors(reporter);
			s = baos.toString();
			assertTrue(s.indexOf("a/a/@a") >= 0 &&
				s.indexOf("<a a=\"a\"/>") >= 0, s);
			// processing mode
			xd = xp.createXDDocument("a");
			xm = xp.getDebugInfo();
			si = xm.getStatementInfo(3, "a");
			for (int i = 0; i < si.length; i++) {
				xd.getDebugger().setStopAddr(si[i].getAddr());
			}
			si = xm.getStatementInfo(16, "");
			for (int i = 0; i < si.length; i++) {
				xd.getDebugger().setStopAddr(si[i].getAddr());
			}
			si = xm.getStatementInfo(18, "");
			for (int i = 0; i < si.length; i++) {
				xd.getDebugger().setStopAddr(si[i].getAddr());
			}
			xd.getDebugger().setInDebug(new ByteArrayInputStream(
				"pl\ngo\npl\ngo\npl\ngo\npl\ngo".getBytes()));
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			ps.close();
			s = baos.toString();
			assertTrue(s.indexOf("a/@a;") > 0 &&
				s.indexOf("No local variables") > 0, s);
			// XPos breakpoints
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a a = \"required;\">\n"+
"  <b x:script='*'/>\n"+
"</a>\n"+
" <x:declaration>\n"+
"  String s = 'a';\n"+
" </x:declaration>\n"+
"</x:def>";
			xp = compile(xdef);
			byte[] dbgInput = "go\ngo\ngo\ngo\ngo\ngo\ngo".getBytes();
			// create mode
			xd = xp.createXDDocument();
			xd.getDebugger().setXpos(XDDebug.CREATE+"/a");
			xd.getDebugger().setXpos(XDDebug.CREATE+"/a/@a");
			xd.getDebugger().setXpos(XDDebug.FINALLY+"/a/b[2]");
			xd.getDebugger().setInDebug(new ByteArrayInputStream(dbgInput));
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			xml = "<a a = 'a'><b/><b/><b/><b/><b/></a>";
			xd.setXDContext(xml);
			create(xd, "a", reporter);
			ps.close();
			assertNoErrors(reporter);
			s = baos.toString();
			assertTrue(s.indexOf("PAUSE /a") >= 0 &&
				s.indexOf("PAUSE /a/@a") >= 0 &&
				s.indexOf("PAUSE /a/b[2]") >= 0, s);
			xd.getDebugger().setInDebug(new ByteArrayInputStream(dbgInput));
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			xd.setXDContext(xml);
			create(xd, "a", reporter);
			ps.close();
			assertNoErrors(reporter);
			s = baos.toString();
			assertTrue(s.indexOf("PAUSE /a") >= 0 &&
				s.indexOf("PAUSE /a/@a") >= 0 &&
				s.indexOf("PAUSE /a/b[2]") >= 0, s);
//			// processing mode
			xd.getDebugger().setInDebug(new ByteArrayInputStream(dbgInput));
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			parse(xd, xml, reporter);
			ps.close();
			s = SUtils.modifyString(baos.toString(), "\r\n", "\n");
			assertNoErrors(reporter);
			assertTrue(s.indexOf(" /a/b[2]\n") >= 0, s);
			assertFalse(s.indexOf(" /a\n") >= 0 ||
				s.indexOf(" /a/@a\n") >= 0, s);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root = 'a'>\n"+
" <a a='required'>\n"+
"   <b xd:script = \"finally {\n"+
"      trace(xpath('../@a').toString());\n"+
"      trace(now().toString());\n"+
"      trace('this'.toString());\n"+
"      Regex x = new Regex('[A-Z]');\n"+
"      trace(x.toString());\n"+
"      RegexResult r = x.getMatcher('DCBA');\n"+
"      trace(r.toString());\n"+
"      }\" />\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a a='1'> <b/></a>";
			xd = compile(xdef).createXDDocument();
			baos = new ByteArrayOutputStream();
			out = XDFactory.createXDOutput(new OutputStreamWriter(baos), false);
			xd.setStdOut(out);
			bos1 = new ByteArrayOutputStream();
			ps = new PrintStream(bos1);
			xd.getDebugger().setOutDebug(ps);
			xd.xparse(xml, null);
			ps.close();
			assertTrue(bos1.toString().startsWith("TRACE /a/b[1];"),
				bos1.toString());
			out.close();
			assertEq("", baos.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='SouborY1A'>\n"+
" <SouborY1A\n"+
"    Davka        = \"required\"\n"+
"    ZeDne        = \"required\" > \n"+
"    <a  xd:script = \"finally {\n"+
"      Container c = xpath('../@Davka');\n"+
"		String error; \n"+
"		error = '._OK._';\n"+
"		trace(error);\n"+
"		trace(replace(error, '._', '???'));\n"+
"		trace(replaceFirst(error, '._', '???'));\n"+
"		int i = 1; if (2*i EQ 1) trace('ERROR');\n"+
//"		//not allowed comment\n"+
//"		pause(xpath('..'));\n"+
"		trace(xpath('..'));\n"+
"		outln('len=' + c.getLength()\n"+
"		+', typ=' + c.getItemType(0)\n"+
"		+', value=' + getText(c,0));\n"+
"      }\" /> \n"+
" required\n"+
" </SouborY1A>\n"+
"</xd:def>";
			xml =
"<SouborY1A Davka='davka' ZeDne='1.1.99'><a/>text</SouborY1A>";
			xd = compile(xdef).createXDDocument();
			baos = new ByteArrayOutputStream();
			out = XDFactory.createXDOutput(new OutputStreamWriter(baos), false);
			xd.setStdOut(out);
			bos1 = new ByteArrayOutputStream();
			ps = new PrintStream(bos1);
			xd.getDebugger().setOutDebug(ps);
			xd.xparse(xml, null);
			ps.close();
			s = bos1.toString();
			assertTrue(s.startsWith("TRACE /SouborY1A/a[1];"), s);
			out.close();
			s = baos.toString();
			assertTrue(("len=1, typ=" + XDValueID.XD_ATTR +
				", value=davka\n").equals(s), s);
			xdef =
"<x:collection xmlns:x='" + _xdNS + "'>\n"+
"<x:def name         = \"a\"\n"+
"       root         = \"a|*\"\n"+
"       impl-version = \"1.0.0\"\n"+
"       impl-date    = \"1.11.2000\"\n"+
"       script=\"options ignoreEmptyAttributes\" >\n"+
"<x:declaration>\n"+
"   String $verze = '1.23';\n"+
"   String $x = '???';\n"+
"   void $myPause(String i, int j, Datetime k) {\n"+
"     pause(i);\n"+
"   }\n"+
"</x:declaration>\n"+
"<a>\n"+
"<b x:script = \"occurs 1..\"\n"+
"   a = \"required eq('b'); onFalse" +
	" pause('Error setText to b!');\n"+
"       finally {\n"+
"           String s = getImplProperty('version');\n"+
"           trace('getImplProperty(\\'version\\'): ' + s);\n"+
	"       }\" />\n"+
"</a>\n"+
"</x:def>\n"+
"<x:def name         = \"b\"\n"+
"       impl-version = \"2.0.0\"\n"+
"       impl-date    = \"2.11.2000\" />\n"+
"</x:collection>";
			xp = compile(xdef);
			xml = "<a><b a = 'b' /><b a = 'c' /></a>";
			xd = xp.createXDDocument("a");
			xd.getDebugger().setInDebug(
				new ByteArrayInputStream("go".getBytes()));
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			parse(xd, xml, reporter);
			ps.close();
			s = baos.toString();
			s = SUtils.modifyString(s, "\r", "");
			assertTrue(s.startsWith("TRACE /a/b[1]/@a; pc=")
				&& s.indexOf("; \"getImplProperty('version'): 1.0.0\"; \n") > 0
				&& s.indexOf("PAUSE /a/b[2]/@a; pc=") > 0
				&& s.indexOf("; \"Error setText to b!\"; \n") > 0
				&& s.indexOf("command ('?' for help): \n") > 0
				&& s.indexOf("TRACE /a/b[2]/@a; pc=") > 0
				&& s.indexOf("; \"getImplProperty('version'): 1.0.0\"; \n") > 0,
				s);
			if (reporter.errors()) {
				printReports(reporter.getReportReader(), xml);
			}
			if (KXmlUtils.compareXML(
				"<a><b a=\"b\"/><b a=\"c\"/></a>",
				KXmlUtils.nodeToString(xd.getElement())).errorWarnings()) {
				fail(KXmlUtils.nodeToString(xd.getElement()));
			}
			// check impl properties and "*" in the root selection
			xdef =
"<x:collection xmlns:x='" + _xdNS + "'>\n"+
"<x:def name = \"a\" root = \"a|*\"\n"+
"       impl-version = \"1.0.0\"\n"+
"       impl-date    = \"1.11.2000\"\n"+
"       script=\"options ignoreEmptyAttributes\" >\n"+
"<a a = \"required eq('a');\n"+
"         finally {\n"+
"           String s = getImplProperty('version');\n"+
"           trace(s);\n"+
"           if (s NE '1.0.0') {\n"+
"             throw new Exception('Error1: ' + s);\n"+
"           }\n"+
"           s = getImplProperty('x');\n"+
"           if (s NE '') {\n"+
"             throw new Exception('Error2: ' + s);\n"+
"           }\n"+
"           s = getImplProperty('b','version');\n"+
"           trace(s);\n"+
"           if (s NE '2.0.0') {\n"+
"             throw new Exception('Error3: ' + s);\n"+
"           }\n"+
"           s = getImplProperty('x','version');\n"+
"           if (s NE '') {\n"+
"             throw new Exception('Error4: ' + s);\n"+
"           }\n"+
"           s = getImplProperty('b','x');\n"+
"           if (s NE '') {\n"+
"             throw new Exception('Error5: ' + s);\n"+
"           }\n"+
"         }\" />\n"+
"</x:def>\n"+
"<x:def name = \"b\" impl-version = \"2.0.0\" impl-date = \"2.11.2000\" />\n"+
"</x:collection>";
			xp = compile(xdef);
/*
			xd = xp.getDefinition("b");
			assertEq("2.0.0", xd.getImplProperties().getProperty("version"));
			assertEq("2.11.2000", xd.getImplProperty("date"));
			xd = xp.getDefinition("a");
			assertEq("1.0.0", xd.getImplProperties().getProperty("version"));
			assertEq("1.11.2000", xd.getImplProperty("date"));
			if (xd.getImplProperty("abc") != null) {
				fail(xd.getImplProperty("abc"));
			}

 */
			xml = "<a a = 'b' />";
			xd = xp.createXDDocument("a");
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			parse(xd, xml, reporter);
			ps.close();
			s = baos.toString();
			s = SUtils.modifyString(s, "\r", "");
			assertTrue(s.indexOf("TRACE /a/@a;") == 0
				&& s.indexOf("TRACE /a/@a;", 1) > 0
				&& s.indexOf("; \"1.0.0\";") > 0
				&& s.indexOf("; \"2.0.0\";") > 0, s);
			if (reporter.errors()) {
				s = reporter.getReport().getMsgID();
				assertTrue("XDEF515".equals(s) || "XDEF809".equals(s), s);
			} else {
				fail();
			}
			// here runs as "*" - any
			xml = "<b a='b' b='a'>x<a/><a x='y'><a y='x'/></a>y</b>";
			xd = xp.createXDDocument("a");
			baos = new ByteArrayOutputStream();
			ps = new PrintStream(baos);
			xd.getDebugger().setOutDebug(ps);
			parse(xd, xml, reporter);
			if (reporter.errorWarnings()) {
				fail("\n"+ ReportPrinter.printListing(xml, reporter));
				reporter.checkAndThrowErrorWarnings();
			} else {
				el = xd.getDocument().getDocumentElement();
				if (KXmlUtils.compareXML(xml,
					KXmlUtils.nodeToString(el)).errorWarnings()) {
					fail(KXmlUtils.nodeToString(
						xd.getDocument().getDocumentElement()));
				}
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