package mytest;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xml.KXmlUtils;
import buildtools.XDTester;
import org.xdef.component.GenXComponent;
import org.xdef.component.XComponent;
import org.xdef.model.XMData;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.sys.Report;

/** Various tests.
 * @author Vaclav Trojan
 */
public class MyTest_0 extends XDTester {

	public MyTest_0() {super(); setChkSyntax(true);}
	
	boolean T; //This flag is used to return after a test
	
	private static void printXMData(final XMData x) {
		System.out.println(x.getXDPosition()
			+ ", Parse: " + x.getParseMethod()
			+ ", Type: " + x.getValueTypeName()
			+ ", Ref: " + x.getRefTypeName()
			+ ", Default: " + x.getDefaultValue()
			+ ", Fixed: " + x.getFixedValue());
	}
	private static void printXMData(final XMElement xe) {
		for (XMData x: xe.getAttrs()) {
			printXMData(x);
		}
		for (XMNode x: xe.getChildNodeModels()) {
			if (x.getKind() == XMNode.XMTEXT) {
				printXMData((XMData) x);
			} else if (x.getKind() == XMNode.XMELEMENT) {
				printXMData((XMElement) x);
			}
		}
	}
	
	@Override
	/** Run test and print error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		T = false; // if false, all tests are invoked
		T = true; // if true, only first test is invoked
////////////////////////////////////////////////////////////////////////////////
		boolean chkSynteax = getChkSyntax();
//		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef.warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		String tempDir = getTempDir();
		try {
			if (new File(tempDir).exists()) {
				FUtils.deleteAll(tempDir, true);
				new File(tempDir).mkdir();
			}
		} catch (Exception ex) {fail(ex);}
		XDPool xp;
		String xdef;
		String xml;
		String s;
		String json;
		Object j;
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		Element el;
		StringWriter strw;
		Report rep;
		XComponent xc;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			json = "{\"\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			json = "{}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"[\"? jnull()\", \"int()\"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ1 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			GenXComponent.genXComponent(xp,
				"src/test/java", "UTF-8", false, true).checkAndThrowErrors();
			json = "[null, 12]";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			mytest.component.TJ1 TJ1 = (mytest.component.TJ1)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(TJ1.jgetjnull() != null);
			assertEq(12, TJ1.jgetnumber());
			assertTrue(TJ1.getjw$null() != null);
			assertEq(12, TJ1.getjw$number().get$value());
			json = "[12]";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			TJ1 = (mytest.component.TJ1)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(TJ1.jgetjnull() == null);
			assertEq(12, TJ1.jgetnumber());
			assertTrue(TJ1.getjw$null() == null);
			assertEq(12, TJ1.getjw$number().get$value());
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"[\"occurs * jnull()\", \"int()\"]\n"+
//"[\"? jnull()\", \"int()\"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ2 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			GenXComponent.genXComponent(xp,
				"src/test/java", "UTF-8", false, true).checkAndThrowErrors();
			json = "[null, 12]";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			mytest.component.TJ2 TJ2 = (mytest.component.TJ2)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(1, TJ2.jlistOfjnull().size());
			assertEq(12, TJ2.jgetnumber());
			assertEq(1, TJ2.listOfjw$null().size());
			assertEq(12, TJ2.getjw$number().get$value());
			json = "[12]";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			TJ2 = (mytest.component.TJ2)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(0, TJ2.jlistOfjnull().size());
			assertEq(12, TJ2.jgetnumber());
			assertEq(0, TJ2.listOfjw$null().size());
			assertEq(12, TJ2.getjw$number().get$value());
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"{\"a\":\"? jnull()\", \"b\":\"int()\"}\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ3 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			GenXComponent.genXComponent(xp,
				"src/test/java", "UTF-8", false, true).checkAndThrowErrors();
			json = "{\"a\":null, \"b\":12}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			mytest.component.TJ3 xx = (mytest.component.TJ3)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(xx.jgetjnull() != null
				&& "null".equals(xx.jgetjnull().toString()));
			assertEq(12, xx.jgetb());
			assertTrue(xx.getjw$null() != null
				&& "null".equals(xx.jgetjnull().toString()));
			assertEq(12, xx.getjw$number().get$value());
			json = "{\"b\":12}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xx = (mytest.component.TJ3)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertNull(xx.jgetjnull());
			assertEq(12, xx.jgetb());
			assertNull(xx.getjw$null());
			assertEq(12, xx.getjw$number().get$value());
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='X|Y|Z|jx:json'>\n"+
"<xd:json xd:name='X'>\n"+
"[\"int()\"]\n"+
"</xd:json>\n"+
"<xd:json xd:mode='xd' xd:name='Y'>\n"+
"[{\"a\":\"boolean\"},\"string()\",\"int()\"]\n" + 
"</xd:json>\n"+
"<xd:json xd:name='Z'>\n"+
"{\"a\":\"string()\"}\n" + 
"</xd:json>\n"+
"<xd:json xd:name='jx:json'>\n"+
"[\"date()\"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TX %link #X;\n"+
"  %class mytest.component.TY %link #Y;\n"+
"  %class mytest.component.TZ %link #Z;\n"+
"  %class mytest.component.TJson %link #jx:json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			GenXComponent.genXComponent(xp,
				"src/test/java", "UTF-8", false, true).checkAndThrowErrors();
			Class TJson, TX, TY, TZ;
			try {
				TX = Class.forName("mytest.component.TX");
				TY = Class.forName("mytest.component.TY");
				TZ = Class.forName("mytest.component.TZ");
				TJson = Class.forName("mytest.component.TJson");
				assertEq("X",
					(String) TX.getDeclaredField("XD_NAME").get(null));
				assertEq("Y",
					(String) TY.getDeclaredField("XD_NAME").get(null));
				assertEq("Z",
					(String) TZ.getDeclaredField("XD_NAME").get(null));
				assertEq("json",
					(String) TJson.getDeclaredField("XD_NAME").get(null));
			} catch (Exception ex) {
				fail("Compile package mytest.component");
				TJson = TX = TY = TZ = TJson = null;
			}
			if (TX != null) {
				json = "[\"2020-01-01\"]";
				j = xp.createXDDocument().jparse(json, "jx:json", reporter);
				assertNoErrors(reporter);
				reporter.clear();
				assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
					JsonUtil.toJsonString(j, true));
				assertNoErrors(reporter);
				reporter.clear();
				xc = xp.createXDDocument().jparseXComponent(json,
					TJson, reporter);
				assertNoErrors(reporter);
				reporter.clear();
				json = "[123]";
				j = xp.createXDDocument().jparse(json, "X", reporter);
				assertNoErrors(reporter);
				reporter.clear();
				assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
					JsonUtil.toJsonString(j, true));
				assertNoErrors(reporter);
				reporter.clear();
				xc = xp.createXDDocument().jparseXComponent(json,
					TX, reporter);
				assertNoErrors(reporter);
				reporter.clear();
				json = "[{\"a\":true},\"xxx\",125]";
				j = xp.createXDDocument().jparse(json, "Y", reporter);
				assertNoErrors(reporter);
				reporter.clear();
				assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
					JsonUtil.toJsonString(j, true));
				xc = xp.createXDDocument().jparseXComponent(json,
					TY, reporter);
				assertNoErrors(reporter);
				reporter.clear();
				json = "{\"a\":\"2020-01-01\"}";
				j = xp.createXDDocument().jparse(json, "Z", reporter);
				assertNoErrors(reporter);
				reporter.clear();
				assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
					JsonUtil.toJsonString(j, true));
				xc = xp.createXDDocument().jparseXComponent(json,
					TZ, reporter);
				assertNoErrors(reporter);
				reporter.clear();
			}			
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A|B|json'>\n"+
"<xd:json name='json'>\n"+
"[{\"a\":\"boolean\"},\"string()\",\"int()\"]\n" + 
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"{\"a\":\"int\"}\n"+
"</xd:json>\n"+
"  <A/>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			json = "[{\"a\":true},\"x\",-1]";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXmlW3C(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXmlW3C(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {// check mixed, include
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:mixed empty='false'>\n"+
"    <b xd:script = 'occurs 0..' />\n"+
"    optional string()\n"+
"    <c xd:script = 'occurs 0..'/>\n"+
"  </xd:mixed>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a>t1</a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a>t1<b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a>t1<b/>t2</a>\n", reporter);
			assertTrue(reporter.errorWarnings());
			parse(xp, null, "<a/>", reporter);
			rep = reporter.getReport();
			assertTrue(rep != null && ("XDEF520".equals(rep.getMsgID())),
				reporter.printToString());
			System.out.println(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef = //Incorrect fixed value
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A a='?float; fixed 2.0' b='? float; default 3.1' c='default \"3.1\"' />\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
//			xp.display();
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt; y : optional flt;}\n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: u.x}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID)'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
//			xp.display();
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  uniqueSet u{x: int();}\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <a xd:script='?' a='u.x()'/>\n"+
"  <b xd:script='+' a='u.x.ID()'/>\n"+
"  <c xd:script='?' a='u.x.IDREF()'/>\n"+
"  <d xd:script='?' a='u.x.CHKID()'/>\n"+
"  <e xd:script='?' a='u.x.SET()'/>\n"+
"</A>\n"+
"</xd:def>\n";
			xp = XDFactory.compileXD(null, xdef);
//			xp.display();
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A><a a='2'/><b a='1'/><c a='1'/><d a='1'/><e a='3'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>\n"+
"    uniqueSet r {a: int();};\n"+
"  </xd:declaration>\n"+
"  <A a = ''>\n"+
"    <B xd:script='*;' c='? r.a' a='r.a.SET()' b='? r.a.SET'/>\n"+
"    <C xd:script='*;' a='r.a.CHKIDS()'/>\n"+
"    ? r.a.CHKIDS\n"+
"  </A>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
//			xp.display();
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A a='x'><B a='1'/><C a='1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
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
"   xd:name=\"Test\" xd:root=\"json\">\n" +
"  <xd:json>{\"A\":\"int();\"}</xd:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef, 
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xmlns:js='" + XDConstants.JSON_NS_URI + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#json\"/>");
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
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "json", reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":\"1234\"}";
			xd.jparse(s, "json", reporter);
			assertErrors(reporter);
			reporter.clear();
/*xx*/
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"  xd:name=\"Test\" xd:root=\"json\">\n" +
"  <xd:json>{\"A\":\"int();\"}</xd:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef,
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#json\"/>");
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
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "json", reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			s = "{\"A\":\"1234\"}";
			assertTrue(!JsonUtil.jsonEqual(xd.jparse(s, "json", reporter),
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
"  <a xd:script='finally out(parseInt(\"123\"))'>\n" +
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
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("123", strw.toString());
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
			assertEq("stringdecxdatetime", strw.toString());
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524")
				&& s.contains("DatumNarozeni") && s.contains("Jmeno"),
				reporter);
		} catch (Exception ex) {fail(ex);}
	}
	
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
	
}
