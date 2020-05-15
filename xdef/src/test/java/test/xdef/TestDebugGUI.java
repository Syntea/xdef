package test.xdef;

import test.XDTester;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import org.w3c.dom.Element;
import org.xdef.XDDebug;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMStatementInfo;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportPrinter;
import org.xdef.xml.KXmlUtils;

/** Provides testing of XDef debug mode and editing in display mode.
 * @author Trojan
 */
public final class TestDebugGUI extends XDTester {

	public TestDebugGUI() {super(); setChkSyntax(false);}

	@Override
	public void test() {
		String xdef;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		XDDocument xd;
		ByteArrayOutputStream baos;
		XDOutput out;
		Element el;
		String s;
		String json;
		// set external editor
//		setProperty(XDConstants.XDPROPERTY_XDEF_EDITOR,
//"xdplugin.XdPlugin; C:/Program Files/Oxygen XML Editor 20/oxygen20.1.exe");
		setProperty(XDConstants.XDPROPERTY_DISPLAY, //xdef_display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); //false
			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); //true
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS); //errors
		setProperty(XDConstants.XDPROPERTY_DEBUG, //xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE); //false
			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); //true
		setProperty(XDConstants.XDPROPERTY_WARNINGS, //xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true
//			XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE); //false
		try {
			// XScript breakpoints
			xdef =
"<x:collection xmlns:x='" + _xdNS + "'>\n"+                 	//01
"<x:def name = 'a' root = 'a'>\n"+								//02
"  <a a = \"required eq('a');\n"+								//03
"         create 'a';\n"+										//04
"         finally {\n"+											//04
"           int i = 1; int j = 2;\n"+							//05
"           s = x(1,'s');\n"+									//06
"           s += (i+j);\n"+										//08
"           i = 0;\n"+											//09
"           j = 0;\n"+											//10
"         }\" />\n"+											//11
"</x:def>\n"+													//12
"<x:declaration>\n"+											//13
"  String x(int n, String s){\n"+								//14
"    String result = s;\n"+										//15
"    for (int i = 0; i LT n; i++) {\n"+							//16
"      result += ',' + i;\n"+									//17
"    }\n"+														//18
"    return result;\n"+											//19
"  }\n"+														//20
"  String s = 'a';\n"+											//21
"</x:declaration>\n"+											//22
"</x:collection>";												//23
			xp = compile(xdef);
			XMDebugInfo xm = xp.getDebugInfo();
			// create mode
			xd = xp.createXDDocument("a");
			XMStatementInfo[] si = xm.getStatementInfo(4, "a");
			for (int i = 0; i < si.length; i++) {
				xd.getDebugger().setStopAddr(si[i].getAddr());
			}
			si = xm.getStatementInfo(6, "a");
			for (int i = 0; i < si.length; i++) {
				System.out.println(si[i]);
				xd.getDebugger().setStopAddr(si[i].getAddr());
			}
			xml = "<a a='a'/>";
			xd.setXDContext(xml);
			create(xd, "a", reporter);
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
			parse(xd, xml, reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			// XPos breakpoints
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"  <a a = \"required;\">\n"+
"    <b x:script='*'/>\n"+
"  </a>\n"+
"  <x:declaration>\n"+
"    String s = 'a';\n"+
"  </x:declaration>\n"+
"</x:def>";
			xp = compile(xdef);
			// create mode
			xd = xp.createXDDocument();
			xd.getDebugger().setXpos(XDDebug.CREATE + "/a");
			xd.getDebugger().setXpos(XDDebug.CREATE + "/a/@a");
			xd.getDebugger().setXpos(XDDebug.FINALLY + "/a/b[2]");
			xml = "<a a = 'a'><b/><b/><b/><b/><b/></a>";
			xd.setXDContext(xml);
			create(xd, "a", reporter);
			// processing mode
			parse(xd, xml, reporter);
		} catch (Exception ex) {fail(ex);}
		try { // trace, pause
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
" </a>\n"+
"</xd:def>";
			xml = "<a a='1'> <b/></a>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			baos = new ByteArrayOutputStream();
			out = XDFactory.createXDOutput(new OutputStreamWriter(baos), false);
			xd.setStdOut(out);
			xd.xparse(xml, null);
			out.close();
			assertEq("", baos.toString());
		} catch (Exception ex) {fail(ex);}
		try {
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
"		/*comment*/\n"+
"		pause(xpath('..'));\n"+
"		trace(xpath('..'));\n"+
"		outln('len=' + c.getLength()\n"+
"		+', typ=' + c.getItemType(0)\n"+
"		+', value=' + getText(c,0));\n"+
"      }\" /> \n"+
"   string();\n"+
" </SouborY1A>\n"+
"</xd:def>";
			xml =
"<SouborY1A Davka='davka' ZeDne='1.1.99'><a/>text</SouborY1A>";
			xd = compile(xdef).createXDDocument();
			baos = new ByteArrayOutputStream();
			out = XDFactory.createXDOutput(new OutputStreamWriter(baos), false);
			xd.setStdOut(out);
			xd.xparse(xml, null);
			out.close();
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<x:collection xmlns:x='" + _xdNS + "'>\n"+
"<x:def name         = 'a'\n"+
"       root         = 'a|*'\n"+
"       impl-version = '1.0.0'\n"+
"       impl-date    = '1.11.2000'\n"+
"       script       = 'options ignoreEmptyAttributes' >\n"+
"  <x:declaration>\n"+
"    String $verze = '1.23';\n"+
"    String $x = '???';\n"+
"    void $myPause(String i, int j, Datetime k) {\n"+
"      pause(i);\n"+
"    }\n"+
"  </x:declaration>\n"+
"  <a>\n"+
"    <b x:script = 'occurs 1..'\n"+
"       a = \"required eq('b'); onFalse" +
"           pause('Error setText to b!');\n"+
"           finally {\n"+
"             String s = getImplProperty('version');\n"+
"             trace('getImplProperty(\\'version\\'): ' + s);\n"+
	"       }\" />\n"+
"  </a>\n"+
"</x:def>\n"+
"<x:def name         = 'b'\n"+
"       impl-version = '2.0.0'\n"+
"       impl-date    = '2.11.2000' />\n"+
"</x:collection>";
			xp = compile(xdef, getClass());
			xml = "<a><b a = 'b' /><b a = 'c' /></a>";
			xd = xp.createXDDocument("a");
			parse(xd, xml, reporter);
			if (KXmlUtils.compareXML("<a><b a='b'/><b a='c'/></a>",
				KXmlUtils.nodeToString(xd.getElement())).errorWarnings()) {
				fail(KXmlUtils.nodeToString(xd.getElement()));
			}
		} catch (Exception ex) {fail(ex);}
		try {// check impl properties and "*" in the root selection
			xdef =
"<x:collection xmlns:x='" + _xdNS + "'>\n"+
"<x:def name = 'a' root = 'a|*'\n"+
"       impl-version = '1.0.0'\n"+
"       impl-date    = '1.11.2000'\n"+
"       script       = 'options ignoreEmptyAttributes' >\n"+
"  <a a = \"required eq('a');\n"+
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
"           s = 'abcd';\n"+
"           pause(s);\n"+
"         }\" />\n"+
"</x:def>\n"+
"<x:def name='b' impl-version='2.0.0' impl-date='2.11.2000'/>\n"+
"</x:collection>";
			xp = compile(xdef);
			xml = "<a a = 'b' />";
			xd = xp.createXDDocument("a");
			parse(xd, xml, reporter);
			if (reporter.errors()) {
				s = reporter.getReport().getMsgID();
				assertTrue("XDEF515".equals(s) || "XDEF809".equals(s), s);
			} else {
				fail();
			}
			// here runs as "*" - any
			xml = "<b a='b' b='a'>x<a/><a x='y'><a y='x'/></a>y</b>";
			xd = xp.createXDDocument("a");
			parse(xd, xml, reporter);
			if (reporter.errorWarnings()) {
				fail("\n" + ReportPrinter.printListing(xml, reporter));
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
		try {// JSON
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"JSON\" root=\"a\">\n"+
"<xd:json name=\"a\" >\n" +
"{ \"personnel\": { \"person\": \n" +
"      [ $script: \"occurs 1..*\",\n" +
"        { $script: \"occurs 1..*; ref B\" }\n" +
"      ]\n" +
"  }\n" +
"}\n" +
"</xd:json>\n" +
"<xd:json name=\"B\" >\n" +
"{ \"id\": \"string()\",\n" +
"   \"name\":{ \"family\":\"jstring()\", \"given\":\"optional jstring()\" },\n"+
"   \"email\": \"email();\",\n" +
"   \"link\": { $script: \"ref C\" }\n" +
"}\n" +
"</xd:json>\n" +
"<xd:json name=\"C\" >\n" +
"{  $oneOf: \"optional;\",\n" +
"   \"manager\": \"jstring()\",\n" +
"   \"subordinates\":[ \"* jstring();\" ]\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("JSON");
			json =
"{ \"personnel\": { \"person\":\n" +
"    [\n" +
"      { \"id\": \"Big.Boss\",\n" +
"        \"name\": { \"family\": \"Boss\",\n" +
"        \"given\": \"Big\" },\n" +
"        \"email\": \"chief@oxygenxml.com\",\n" +
"        \"link\": { \"subordinates\": [\"one.worker\", \"two.worker\" ] }\n" +
"      },\n" +
"      { \"id\": \"one.worker\",\n" +
"        \"name\": { \"family\": \"Worker\", \"given\": \"One\" },\n" +
"        \"email\": \"one@oxygenxml.com\",\n" +
"        \"link\": {\"manager\": \"Big.Boss\"}\n" +
"      },\n" +
"      { \"id\": \"two.worker\",\n" +
"        \"name\": { \"family\": \"Worker\", \"given\": \"Two\" },\n" +
"        \"email\": \"two@oxygenxml.com\",\n" +
"        \"link\": {\"manager\": \"Big.Boss\"}\n" +
"      }\n" +
"    ]\n" +
"  }\n" +
"}";
			xd.jparse(json, reporter);
			if (reporter.errors()) {
				System.out.println(reporter);
			}
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Start test from command line. Print results on system output.
	 * @param args The parameter is ignored.
	 */
	final public static void main(final String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}