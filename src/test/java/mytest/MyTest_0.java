package mytest;

import java.io.File;
import java.io.StringWriter;
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
import org.xdef.json.JNull;
import org.xdef.model.XMData;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.sys.Report;

/** Various tests.
 * @author Vaclav Trojan
 */
public class MyTest_0 extends XDTester {

	public MyTest_0() {super();}
		
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
		boolean T = false; // if false, all tests are invoked
		T = true; // if true, only first test is invoked

		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS); // true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
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
		final String xcomponentDir = "src/test/java";
/**
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> Element e;</xd:declaration>\n"+
"<a xd:script=\"+; create xpath('y[2]', e)\">\n"+
"</a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
		} catch (Exception ex) {ex.printStackTrace();}
if(T){return;}
/**/
		try {
/* *
  public Object jgeta() {
	if (_jw$null != null) return jgeta$null();
	if (_jw$boolean != null) return jgeta$boolean();
	if (_jw$number != null) return jgeta$number();
	if (_jw$string != null) return jgeta$string();
	return null;
  }
  public void jseta(Object x) {
	  if (x==null || x instanceof cz.syntea.xdef.json.JNull) 
        jseta$null((cz.syntea.xdef.json.JNull) x);
	  else if (x instanceof Boolean) jseta$boolean((Boolean)x);
	  else if (x instanceof Number) jseta$number((Number)x);
	  else if (x instanceof String) jseta$string((String)x);
	  else throw new cz.syntea.xdef.sys.SRuntimeException(//Incorrect type &{0}
        cz.syntea.xdef.msg.XDEF.XDEF377, x.getClass().getName());
  }
/* */
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.XAA %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			mytest.component.XAA XAA = (mytest.component.XAA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XAA.toJson()),
				JsonUtil.toJsonString(XAA.toJson(), true));
			reporter.checkAndThrowErrors();
			assertEq("aaa", XAA.jgeta$string());
			XAA.jseta$number(123);
			assertEq(123, XAA.jgeta$number());
			assertNull(XAA.jgeta$string());
			assertEq(123, XAA.jgeta$number());
			XAA.jseta$null(null);
			assertNull(XAA.jgeta$null());
			assertNull(XAA.jgeta$boolean());
			assertNull(XAA.jgeta$number());
			assertNull(XAA.jgeta$string());

			XAA.jseta$string(" a b \t");
			assertEq(" a b \t", XAA.jgeta$string());
//			XAA.jseta(" a b \t");
//			assertEq(" a b \t", XAA.jgeta());

			assertNull(XAA.jgeta$null());
			assertNull(XAA.jgeta$boolean());
			assertNull(XAA.jgeta$number());

			XAA.jseta$null(null);
			assertNull(XAA.jgeta$null());
//			XAA.jseta(null);
//			assertNull(XAA.jgeta());

			assertNull(XAA.jgeta$null());
			assertNull(XAA.jgeta$boolean());
			assertNull(XAA.jgeta$number());
			assertNull(XAA.jgeta$string());
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XAA = (mytest.component.XAA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XAA.toJson()),
				JsonUtil.toJsonString(XAA.toJson(), true));
			assertEq(123, XAA.jgeta$number());
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XAA = (mytest.component.XAA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XAA.toJson()),
				JsonUtil.toJsonString(XAA.toJson(), true));
			assertEq(false, XAA.jgeta$boolean());
			XAA.jseta$number(123);
			assertEq(123, XAA.jgeta$number());
			assertNull(XAA.jgeta$boolean());
			assertNull(XAA.jgeta$string());
			assertNull(XAA.jgeta$null());
			XAA = (mytest.component.XAA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XAA.toJson()),
				JsonUtil.toJsonString(XAA.toJson(), true));

			assertTrue(XAA.jgeta$boolean()!= null && !(XAA.jgeta$boolean()));
			XAA.jseta$null(JNull.JNULL);
			assertNull(XAA.jgeta$boolean());
			assertNull(XAA.jgeta$number());
			assertNull(XAA.jgeta$string());
			assertEq("null", XAA.jgeta$null().toString());
//			assertTrue(XAA.jgeta() != null && !((Boolean)XAA.jgeta()));
//			XAA.jseta(null);
//			assertNull(XAA.jgeta());

			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XAA = (mytest.component.XAA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XAA.toJson()),
				JsonUtil.toJsonString(XAA.toJson(), true));
			assertEq(JNull.JNULL, XAA.jgeta$null());
			json = "{}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XAA = (mytest.component.XAA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XAA.toJson()),
				JsonUtil.toJsonString(XAA.toJson(), true));
			assertNull(XAA.jgeta$null());
//			try {
//				XA.jseta(XA); // the must throw SRuntimeException
//				fail("error not recognized");
//			} catch (SRuntimeException ex) {
//				assertTrue("XDEF377".equals(ex.getMsgID()));
//			}
/**/
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A | B'>\n"+
"<xd:json name='A'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"[\"? jvalue()\"]\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.XA %link #A;\n"+
"  %class mytest.component.XB %link #B;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			mytest.component.XA XA = (mytest.component.XA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XA.toJson()),
				JsonUtil.toJsonString(XA.toJson(), true));
			reporter.checkAndThrowErrors();
			assertEq("aaa", XA.jgeta$string());
			XA.jseta$number(123);
			assertEq(123, XA.jgeta$number());
			assertNull(XA.jgeta$string());
			assertEq(123, XA.jgeta$number());
			XA.jseta$null(null);
			assertNull(XA.jgeta$null());
			assertNull(XA.jgeta$boolean());
			assertNull(XA.jgeta$number());
			assertNull(XA.jgeta$string());
//			XA.jseta("xyz");
//			assertEq("xyz", XA.jgeta());
			assertNull(XA.jgeta$null());
			assertNull(XA.jgeta$boolean());
			assertNull(XA.jgeta$number());
//			XA.jseta(null);
//			assertNull(XA.jgeta());
			assertNull(XA.jgeta$null());
			assertNull(XA.jgeta$boolean());
			assertNull(XA.jgeta$number());
			assertNull(XA.jgeta$string());
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XA = (mytest.component.XA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XA.toJson()),
				JsonUtil.toJsonString(XA.toJson(), true));
			assertEq(123, XA.jgeta$number());
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XA = (mytest.component.XA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XA.toJson()),
				JsonUtil.toJsonString(XA.toJson(), true));
			assertEq(false, XA.jgeta$boolean());
			XA.jseta$number(123);
			assertEq(123, XA.jgeta$number());
			assertNull(XA.jgeta$boolean());
			assertNull(XA.jgeta$string());
			assertNull(XA.jgeta$null());
			XA = (mytest.component.XA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XA.toJson()),
				JsonUtil.toJsonString(XA.toJson(), true));
//			assertTrue(XA.jgeta() != null && !((Boolean)XA.jgeta()));
//			XA.jseta(null);
//			assertNull(XA.jgeta());
			XA.jseta$boolean(null);
			assertNull(XA.jgeta$boolean());
			assertNull(XA.jgeta$number());
			assertNull(XA.jgeta$string());
			assertNull(XA.jgeta$null());
			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XA = (mytest.component.XA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XA.toJson()),
				JsonUtil.toJsonString(XA.toJson(), true));
			assertEq(JNull.JNULL, XA.jgeta$null());
			json = "{}";
			j = xp.createXDDocument().jparse(json, "A", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XA = (mytest.component.XA)
				 xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XA.toJson()),
				JsonUtil.toJsonString(XA.toJson(), true));
			assertNull(XA.jgeta$null());
//			try {
//				XA.jseta(XA); // the must throw SRuntimeException
//				fail("error not recognized");
//			} catch (SRuntimeException ex) {
//				assertTrue("XDEF377".equals(ex.getMsgID()));
//			}
			
			json = "[null]";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			mytest.component.XB XB = (mytest.component.XB)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XB.class, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XB.toJson()),
				JsonUtil.toJsonString(XB.toJson(), true));
			assertEq(JNull.JNULL, XB.jgetnull());
			json = "[123]";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XB = (mytest.component.XB)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XB.class, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XB.toJson()),
				JsonUtil.toJsonString(XB.toJson(), true));
			assertNull(XB.jgetnull());
			assertEq(123, XB.jgetnumber());
			assertNull(XB.jgetboolean());
			assertNull(XB.jgetstring());
			json = "[true]";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XB = (mytest.component.XB)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XB.class, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XB.toJson()),
				JsonUtil.toJsonString(XB.toJson(), true));
			assertNull(XB.jgetnull());
			assertNull(XB.jgetnumber());
			assertTrue(XB.jgetboolean());
			assertNull(XB.jgetstring());
			json = "[]";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XB = (mytest.component.XB)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XB.class, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XB.toJson()),
				JsonUtil.toJsonString(XB.toJson(), true));
			assertNull(XB.jgetnull());
			assertNull(XB.jgetnumber());
			assertNull(XB.jgetboolean());
			assertNull(XB.jgetstring());
/**/
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json mode='xd'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.XD %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			mytest.component.XD XD = (mytest.component.XD)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XD.class, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XD.toJson()),
				JsonUtil.toJsonString(XD.toJson(), true));
			reporter.checkAndThrowErrors();
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XD = (mytest.component.XD)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XD.class, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XD.toJson()),
				JsonUtil.toJsonString(XD.toJson(), true));
			reporter.checkAndThrowErrors();
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XD = (mytest.component.XD)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XD.class, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XD.toJson()),
				JsonUtil.toJsonString(XD.toJson(), true));
			reporter.checkAndThrowErrors();
			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			json = "{}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			XD = (mytest.component.XD)
				 xp.createXDDocument().jparseXComponent(json,
					 mytest.component.XD.class, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), XD.toJson()),
				JsonUtil.toJsonString(XD.toJson(), true));
			reporter.checkAndThrowErrors();
/* */
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"[\n"+
"    \"? jnull\",\n"+
"    \"int()\",\n"+
"    \"? jvalue()\"\n"+
"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ1 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
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
			assertTrue(TJ1.jgetnull() != null);
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
			assertTrue(TJ1.jgetnull() == null);
			assertEq(12, TJ1.jgetnumber());
			assertTrue(TJ1.getjw$null() == null);
			assertEq(12, TJ1.getjw$number().get$value());
/* */
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"[\"occurs * jnull()\", \"int()\"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ2 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
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
			assertEq(1, TJ2.jlistOfnull().size());
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
			assertEq(0, TJ2.jlistOfnull().size());
			assertEq(12, TJ2.jgetnumber());
			assertEq(0, TJ2.listOfjw$null().size());
			assertEq(12, TJ2.getjw$number().get$value());
/* */
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json>\n"+
"{\"a\":\"? jnull()\", \"b\":\"int()\"}\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ3 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
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
			assertTrue(xx.jgeta$null()!= null
				&& "null".equals(xx.jgeta$null().toString()));
			assertTrue(xx.jgeta$null()!= null
				&& "null".equals(xx.jgeta$null().toString()));
			assertEq(12, xx.jgetb$number());
			assertTrue(xx.getjw$null() != null
				&& "null".equals(xx.jgeta$null().toString()));
//			assertTrue(xx.jgeta() != null
//				&& "null".equals(xx.jgeta().toString()));
//			assertEq(12, xx.jgetb());
//			assertTrue(xx.getjw$null() != null
//				&& "null".equals(xx.jgeta().toString()));
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
			assertNull(xx.jgeta$null());
			assertEq(12, xx.jgetb$number());
//			assertNull(xx.jgeta());
//			assertEq(12, xx.jgetb());
			assertNull(xx.getjw$null());
			assertEq(12, xx.getjw$number().get$value());
/* */
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='X|Y|Z|jx:json'>\n"+
"<xd:json xd:name='X'>\n"+
"[\"int()\"]\n"+
"</xd:json>\n"+
"<xd:json xd:mode='xd' name='Y'>\n"+
"[{\"a\":\"jboolean\"},\"jstring()\",\"jnumber()\",\"? jboolean()\"]\n" + 
"</xd:json>\n"+
"<xd:json name='Z'>\n"+
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
			xp = compile(xdef);
			GenXComponent.genXComponent(xp,
				xcomponentDir, "UTF-8", false, true).checkAndThrowErrors();
/* */
			Class<?> TX = mytest.component.TX.class;
			Class<?> TY = mytest.component.TY.class;
			Class<?> TZ = mytest.component.TZ.class;
			Class<?> TJson = mytest.component.TJson.class;
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
			assertEq(123, ((mytest.component.TX) xc).jgetnumber());
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
			assertTrue(((mytest.component.TY) xc).geta().get$value());
			assertEq("xxx", ((mytest.component.TY) xc).jgetitem());
			assertEq(125, ((mytest.component.TY) xc).jgetitem_1());
			assertNull(((mytest.component.TY) xc).jgetitem_2());
			((mytest.component.TY) xc).jsetitem_2(true);
			assertTrue(((mytest.component.TY) xc).jgetitem_2());
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
			assertEq("2020-01-01", ((mytest.component.TZ) xc).jgeta$string());
/**/
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
			xp = compile(xdef);
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
			xp = compile(xdef);
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
			xp = compile(xdef);
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
			xp = compile(xdef);
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
			xp = compile(xdef);
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
			xp = XDFactory.compileXD(null, s);//with wildcards
			xp = XDFactory.compileXD(null, 
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='" + s + "'/>");
			xp = XDFactory.compileXD(null, 
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='" + s + "'/>");
			// URL (file:/filepath)
			xp = XDFactory.compileXD(null, "file:/" + s);
			xp = XDFactory.compileXD(null, 
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='file:/" + s + "'/>");
			xp = XDFactory.compileXD(null, 
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
			xp = compile(xdef);
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
			xp = compile(xdef);
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
