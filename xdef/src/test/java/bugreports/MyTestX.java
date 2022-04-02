package bugreports;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.genXComponent;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTestX extends XDTester {
	public MyTestX() {super();}

	private void test(String xdef, String json) {
		XDPool xp = XDFactory.compileXD(null, xdef);
		genXComponent(xp, clearTempDir()).checkAndThrowErrors();
		XDDocument xd = xp.createXDDocument();
		Object x = XonUtils.parseXON(json);
		Element e1 = XonUtils.xonToXmlXD(x);
		Element e2 = XonUtils.xonToXml(x);
		Object o1 = XonUtils.xmlToXon(e1);
		Object o2 = XonUtils.xmlToXon(e2);
		if (!XonUtils.xonEqual(o1, o2)) {
			fail(XonUtils.toXonString(o1, true) + "\n"
				+ XonUtils.toXonString(o2, true) + "\n"
				+ KXmlUtils.nodeToString(e1,true) + "\n"
				+ KXmlUtils.nodeToString(e2,true) + "\n");
		}
		ArrayReporter reporter = new ArrayReporter();
//System.out.println(KXmlUtils.nodeToString(XonUtils.xonToXml(x), true));
		o1 = xd.jparse(json, reporter);
		if (!XonUtils.xonEqual(x, o1)) {
			fail(XonUtils.toXonString(x, true) + "\n"
				+ XonUtils.toXonString(o1, true) + "\n");
		}
		assertNoErrors(reporter);
		reporter.clear();
		XComponent xc = xd.jparseXComponent(json, null, reporter);
		assertNoErrors(reporter);
		if (!XonUtils.xonEqual(x, o1 = XComponentUtil.toXon(xc))) {
			fail(XonUtils.toXonString(x, true) + "\n"
				+ XonUtils.toXonString(o1, true) + "\n");
		}
		Object xon = XonUtils.xonToJson(XonUtils.parseXON(json));
		o1 = XonUtils.xmlToXon(XonUtils.xonToXml(xon));
		o2 = XonUtils.xmlToXon(XonUtils.xonToXmlXD(xon));
		if (!XonUtils.xonEqual(o1,o2)) {
			fail(XonUtils.toXonString(o1, true) + "\n"
				+ XonUtils.toXonString(o2, true) + "\n"
				+ "***\n"
				+ KXmlUtils.nodeToString(XonUtils.xonToXml(xon), true)
				+ "\n"+KXmlUtils.nodeToString(XonUtils.xonToXmlXD(xon), true));
		}
		try {
			Object o = xd.jvalidate(x, null);
			if (!XonUtils.xonEqual(x, XonUtils.xonToJson(o))) {
				fail(XonUtils.toJsonString(x) + "\n"
					+ XonUtils.toJsonString(XonUtils.xonToJson(o)));
			}
		} catch (Exception ex) {
			fail(ex);
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		ArrayReporter reporter = new ArrayReporter();
		String s;
		StringWriter strw;
		Object o,x;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		String xdef;

////////////////////////////////////////////////////////////////////////////////
/**
		s = "_x61_hoj";
		System.out.print(s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x69_tem";
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x6b_ey";
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x76_alue";
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x78_ml";
		System.out.print("; " + s + " ");
		System.out.println(org.xdef.xon.XonTools.xmlToJName(s));
if (true) return;
/**/
		try { // test forget
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Skladby'>\n"+
"<xd:xon name=\"Skladby\">\n"+
"  [\n" +
"    { $script= \"occurs 1..*;\",\n" +
"       \"Style\": [ $oneOf,\n" +
"         \"string()\",\n" +
"         [ \"occurs 2..* string()\" ]\n" +
"       ]\n" +
"    }\n" +
"  ]\n" +
"</xd:xon>\n"+
"<xd:component>%class test.xdef.TestX0xx %link Skladby</xd:component>\n"+
"</xd:def>";
			s =
"[\n" +
"  {\n" +
"    \"Style\": \"Classic\"\n" +
"  },\n" +
"  {\n" +
"    \"Style\": [\"jazz\", \"pop\" ]\n" +
"  }\n" +
"]";
			test(xdef, s);
			xp = compile(xdef);
			xd = xp.createXDDocument();
			x = jparse(xd, s, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument("");
//System.out.println(KXmlUtils.nodeToString(XonUtils.xonToXml(x),true));
			xd.setXONContext(x);
			o = jcreate(xd, "Skladby", reporter);
			assertNoErrors(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
//if (true) return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"[\n" +
"  { $script= \"occurs 1..*\",\n" +
"    \"Genre\": [ $oneOf,\n" +
"      \"string()\",\n" +
"      [\"occurs 1..* string()\"]\n" +
"    ]\n" +
"  }\n" +
"]\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestX017 %link a</xd:component>\n"+
"</xd:def>";
			s =
"[\n" +
//"  {\n" +
//"    \"Genre\": [\"A1\"]\n" +
//"  },\n" +
//"  {\n" +
//"    \"Genre\": [\"B1\", \"B2\"]\n" +
//"  },\n" +
"  {\n" +
"    \"Genre\": \"C1\"\n" +
"  }\n" +
"]";
			test(xdef, s);
if (true) return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"[\n" +
"  { $script= \"occurs 1..*\",\n" +
"    \"Name\": \"string()\",\n" +
"    \"Genre\": [ $oneOf,\n" +
"      \"string()\",\n" +
"      [\"occurs 1..* string()\"]\n" +
"    ]\n" +
"  }\n" +
"]\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestX017 %link a</xd:component>\n"+
"</xd:def>";
			s =
"[\n" +
"  {\n" +
"    \"Name\": \"A\",\n" +
"    \"Genre\": [\"A1\"]\n" +
"  },\n" +
"  {\n" +
"    \"Name\": \"B\",\n" +
"    \"Genre\": [\"B1\", \"B2\"]\n" +
"  },\n" +
"  {\n" +
"    \"Name\": \" cc dd \",\n" +
"    \"Genre\": \"C1\"\n" +
"  }\n" +
"]";
			test(xdef, s);
if (true) return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"{ a=\" int();\" }\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestX0 %link A</xd:component>\n"+
"</xd:def>";
			s = "{ a=1 }";
			test(xdef, s);
//if (true) return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"{ \"\": \"jstring()\" }\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestX1 %link a</xd:component>\n"+
"</xd:def>";
			s = "{\n" +
"	\"\":\"\"\n" +
"}";
			test(xdef, s);
//if (true) return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"{ \"\": \"jstring()\" }\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestX2 %link a</xd:component>\n"+
"</xd:def>";
			s =
"{\n" +
"	\"\":\"\"\n" +
"}";
			test(xdef, s);
		} catch (Exception ex) {fail(ex);}
//if (true) return;
		try { // test forget

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"Example\" root=\"test\">\n" +
"<xd:component>%class test.xdef.data.TestXonForget %link test</xd:component>\n"+
"  <xd:xon name=\"test\">\n" +
"    {date= \"date()\",\n" +
"      cities= [\n" +
"        { $script = \"occurs 1..*; finally outln(); forget\",\n" +
"          \"from\": [\n" +
"            \"string(); finally out('From ' + getText());\",\n" +
"            { $script = \"occurs 1..*;\",\n" +
"              \"to\": \"jstring();finally out(' to '+getText()+' is ');\",\n"+
"              \"distance\": \"int(); finally out(getText() + ' km');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }"+
"      ]\n" +
"    }\n" +
"  </xd:xon>\n" +
"</xd:def>";
			s =
"{ \"date\" : \"2020-02-22\",\n" +
"  \"cities\" : [ \n" +
"    { \"from\": [\"Brussels\",\n" +
"        {\"to\": \"London\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 265}\n" +
"      ]\n" +
"    },\n" +
"    { \"from\": [\"London\",\n" +
"        {\"to\": \"Brussels\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 344}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}";
			test(xdef, s);
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument("Example");
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			x = xd.jparse(s, reporter);
			strw.close();
			assertEq(strw.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
			xd = xp.createXDDocument("Example");
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null, reporter);
			strw.close();
			assertEq(strw.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertNoErrors(reporter);
			x = XComponentUtil.toXon(xc);
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
		} catch (Exception ex) {fail(ex);}
if (true) return;
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}