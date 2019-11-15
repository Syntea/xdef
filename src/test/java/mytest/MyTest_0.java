/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytest;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xml.KXmlUtils;
import buildtools.XDTester;

/** Various tests.
 * @author Vaclav Trojan
 */
public class MyTest_0 extends XDTester {

	public MyTest_0() {super(); setChkSyntax(true);}
	
	@Override
	/** Run test and print error information. */
	public void test() {
		String tempDir = getTempDir();
		XDPool xp;
		String xdef;
		String xml;
		String s;
		try {
			if (new File(tempDir).exists()) {
				FUtils.deleteAll(tempDir, true);
				new File(tempDir).mkdir();
			}
		} catch (Exception ex) {fail(ex);}

		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		Element el;
		XDOutput out;
		StringWriter strw;
		boolean chkSynteax = getChkSyntax();
//		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef.warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
		final boolean T = true; //This flag is used to return from a test
//		final boolean T = false; //This flag is used to return from a test
		try {
			s = "D:/cvs/DEV/java/xdef/src/main/resources/"
				+ "org/xdef/impl/compile/XdefOfXdef*.xdef";
			// filepath
			xp = XDFactory.compileXD((Properties) null, s);//with wildcards
			xp = XDFactory.compileXD((Properties) null,
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='" + s + "'/>");
			xp = XDFactory.compileXD((Properties) null,
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='" + s + "'/>");
			// URL (file:/filepath)
			xp = XDFactory.compileXD((Properties) null, "file:/" + s);
			xp = XDFactory.compileXD((Properties) null,
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='file:/" + s + "'/>");
			xp = XDFactory.compileXD((Properties) null,
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='file:/" + s + "'/>");
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:name=\"Test\" xd:root=\"A\">\n" +
"  <A a=''>\n" +
"    <a:a xmlns:a='a.a' a=''></a:a>\n" +
"    <a:a xmlns:a='a.b' a=''></a:a>\n" +
"  </A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD((Properties) null, xdef);
			xml =
"<A a='a'>\n" +
"    <a:a xmlns:a='a.a' a='b'></a:a>\n" +
"    <a:a xmlns:a='a.b' a='c'></a:a>\n" +
"</A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
boolean xxx;
xxx = false;
xxx = true;
			java.io.ByteArrayOutputStream baos;
			java.io.ObjectOutput outx;
			java.io.ObjectInput in;
/*xx*/
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xmlns:js='" + XDConstants.JSON_NS_URI + "'\n" +
"   xd:name=\"Test\" xd:root=\"js:json\">\n" +
"  <js:json>{\"A\":\"int();\"}</js:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef, 
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xmlns:js='" + XDConstants.JSON_NS_URI + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#js:json\"/>");
if (xxx) {
baos = new java.io.ByteArrayOutputStream();
outx = new java.io.ObjectOutputStream(baos);
outx.writeObject(xp);
outx.close();
in = new java.io.ObjectInputStream(
	new java.io.ByteArrayInputStream(baos.toByteArray()));
xp = (XDPool) in.readObject();
}
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":1234}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			s = "{\"A\":\"1234\"}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertErrors(reporter);
			reporter.clear();
/*xx*/
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"  xmlns:js='" + XDConstants.JSON_NS_URI_W3C + "'\n" +
"  xd:name=\"Test\" xd:root=\"js:json\">\n" +
"  <js:json>{\"A\":\"int();\"}</js:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef,
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"  xmlns:js='" + XDConstants.JSON_NS_URI_W3C + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#js:json\"/>");
if (xxx) {
baos = new java.io.ByteArrayOutputStream();
outx = new java.io.ObjectOutputStream(baos);
outx.writeObject(xp);
outx.close();
in = new java.io.ObjectInputStream(
	new java.io.ByteArrayInputStream(baos.toByteArray()));
xp = (XDPool) in.readObject();
}
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":1234}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			s = "{\"A\":\"1234\"}";
			assertTrue(!JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertErrors(reporter);
			reporter.clear();
/*xx*/			
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def root='A'\n" +
"  xmlns:xd='"+XDConstants.XDEF32_NS_URI+"'>\n" +
"    <A a='optional jstring()'>optional jstring();</A>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
/*xx*
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
//"<A>a\\\"</A>";
//"<A a='a\\\"'></A>";
"<A a='a\\\"\\tb'>a\\\"\\tb</A>";
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			assertEq(el, xd.xparse(xml, reporter));
			assertNoErrors(reporter);
			Object o = XmlToJson.toJson(el);
			assertTrue(JsonUtil.jsonEqual(o,
				JsonUtil.parse(JsonUtil.toJSONString(o))));
			System.out.println(JsonUtil.toJSONString(o));
			el = JsonToXml.toXmlXD(o);
			System.out.println(KXmlUtils.nodeToString(el, true));
			assertTrue(JsonUtil.jsonEqual(o, XmlToJson.toJson(el)));
/*xx*/
			s = "{\"\":\"\\\\\\\"\\t\"}";
			el = JsonUtil.jsonToXmlW3C(JsonUtil.parse(s));
			xml = KXmlUtils.nodeToString(el, true);
			System.out.println(xml);
			System.out.println(s);
			System.out.println(JsonUtil.toJsonString(JsonUtil.xmlToJson(el)));
/*xx*/
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally out(parseInt(\"12x3\"))'>\n" +
"<xd:mixed>\n"+
"    match getOccurrence() == 0; ? string(); \n" +
"    <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"    match getOccurrence() == 0; string(); \n" +
"</xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("0", strw.toString());
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='a'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet u {var Parser x; var int y; a: string(); var String z}\n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <DefParams>\n" +
"       <Param xd:script='*;'\n" +
"          Name='u.a.ID();'\n" +
"          Type='xdType(); onTrue u.x=getParsedValue();\n" +
"                          onFalse u.y=99;\n" +
"                          finally out(u.y)'/>\n" +
"    </DefParams>\n" +
"    <Params xd:script=\"*; init u.checkUnref()\">\n" +
"       <Param xd:script='*;'\n" +
"              Name='u.a.CHKID();'\n" +
"              Value='u.x; onTrue out(u.x); '/>\n" +
"    </Params>\n" +
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"xdatetime('dd.MM.yyyy')\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"01.02.1987\"/>\n" +
"  </Params>\n" +
"  <Params>\n" +
"    <Param Value=\"14.8a\" Name=\"Vyska\"/>\n" +
"  </Params>\n" +
"</a>";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("nullnullnullCDATAdecxdatetime", strw.toString());
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524")
				&& s.contains("DatumNarozeni") && s.contains("Jmeno"),
				reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
	}
	
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
	
}
