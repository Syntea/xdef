package mytest;

import java.lang.reflect.Method;
import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.GenXComponent;
import org.xdef.component.XComponent;
import org.xdef.json.*;
import org.xdef.sys.ArrayReporter;
import test.utils.XDTester;

/** Test names of getter/setter.
 * @author Vaclav Trojan
 */
public class JSONTest3 extends XDTester {

	public JSONTest3() {super();}

	/** Run getter on the X-component.
	 * @param xc X-component.
	 * @param name name of X-component getter.
	 * @return value of getter.
	 */
    private static Object getValue(XComponent xc, String name) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("get" + name);
			return m.invoke(xc);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/** Run setter on the X-component.
	 * @param xc X-component.
	 * @param name name XC-component setter.
	 * @param val value to be set.
	 */
    private static void setValue(XComponent xc, String name, Object val) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("set" + name, val.getClass());
			m.invoke(xc, val);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Run a JSON getter on the X-component.
	 * @param xc X-component.
	 * @param name name of JSON getter.
	 * @return value of getter.
	 */
    private static Object getJValue(XComponent xc, String name) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("jget" + name);
			return m.invoke(xc);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/** Run a JSON setter on the X-component.
	 * @param xc X-component.
	 * @param name name of JSON setter.
	 * @param val value to be set.
	 */
    private static void setJValue(XComponent xc, String name, Object val) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("jset" + name, val.getClass());
			m.invoke(xc, val);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		XComponent xc;
		XDPool xp;
		XDDocument xd;
		String json;
		Object js, js1;
		ArrayReporter reporter = new ArrayReporter();
		Properties props = new Properties();
//		props.setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS); // errors
//		props.setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		String knma, knmb;
//		knma = "";
		knma = "a.b";
//		knma = " a, b ";
//		knmb = "";
		knmb = "c.d";
//		knmb = "a b";
/*ss*/
		String jnamea = GenXComponent.javaName(JsonUtil.toXmlName(knma));
		String jnameb = GenXComponent.javaName(JsonUtil.toXmlName(knmb));
		String testDir = "src/test/java/";
		try {
			xp = XDFactory.compileXD(props, 
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\": \"optional union(%item=[jnull, int, jstring])\"}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\": \"optional jstring()\"}\n" +
"</jw:json>\n" +
"</xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' name='B' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\": \"optional int()\"}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\": \"optional int()\"}\n" +
"</jw:json>\n" +
"  <xd:component>\n" +
"    %class mytest.xdef.component.Js3 %link A#js:json;\n" +
"    %class mytest.xdef.component.Js3w %link A#jw:json;\n" +
"    %class mytest.xdef.component.Js3i %link B#js:json;\n" +
"    %class mytest.xdef.component.Js3wi %link B#jw:json;\n" +
"  </xd:component>\n" +
"</xd:def>");
			assertNoErrors(
				GenXComponent.genXComponent(xp, testDir, "UTF-8", false, true));
//		System.exit(0);	
//			json = "{\""+knm1+"\":null}";
			json = "{\""+knma+"\":\"Music Library\"}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("A");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq("Music Library", getJValue(xc, jnamea));
			setJValue(xc, jnamea, "123");
			assertEq("123", getJValue(xc, jnamea));
			setJValue(xc, jnamea, "a\nb");
			assertEq("a\nb", getJValue(xc, jnamea));
			
			xd = xp.createXDDocument("A");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3w.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq("Music Library", getJValue(xc, jnamea));
			setJValue(xc, jnamea, "123");
			assertEq("123", getJValue(xc, jnamea));
			setJValue(xc, jnamea, "a\nb");
			assertEq("a\nb", getJValue(xc, jnamea));

			json = "{\""+knma+"\":123}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("B");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3i.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq(123, getJValue(xc, jnamea));
			setJValue(xc, jnamea, 1);
			assertEq(1, getJValue(xc, jnamea));
			xd = xp.createXDDocument("B");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3wi.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(js1, js));
			assertEq(123, getJValue(xc, jnamea));
			setJValue(xc, jnamea, 1);
			assertEq(1, getJValue(xc, jnamea));
		} catch (Exception ex) {fail(ex);}
//if(true)return;
/*ss*/
		try {
			xp = XDFactory.compileXD(props, 
"<xd:def xmlns:xd='" + _xdNS + "' name='C' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\":{\""+knmb+"\": \"optional jstring()\"}}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\":{\""+knmb+"\": \"optional jstring()\"}}\n" +
"</jw:json>\n" +
"</xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' name='D' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\":{\""+knmb+"\": \"optional int()\"}}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\":{\""+knmb+"\": \"optional int()\"}}\n" +
"</jw:json>\n" +
"  <xd:component>\n" +
"    %class mytest.xdef.component.Js3_1 %link C#js:json;\n" +
"    %class mytest.xdef.component.Js3_1w %link C#jw:json;\n" +
"    %class mytest.xdef.component.Js3_1i %link D#js:json;\n" +
"    %class mytest.xdef.component.Js3_1wi %link D#jw:json;\n" +
"  </xd:component>\n" +
"</xd:def>");
//melo by byt: jgetA+X20_B().jgeta_x20_b();  jgetA+X20_B().jseta_x20_b(xxx)
			assertNoErrors(
				GenXComponent.genXComponent(xp, testDir, "UTF-8", false, true));
			json = "{\""+knma+"\":{\""+knmb+"\":\"Music Library\"}}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("C");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_1.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq("Music Library", getValue(xc, jnameb));
			setValue(xc, jnameb, "123");
			assertEq("123",	getValue(xc, jnameb));
			setValue(xc, jnameb, "a\nb");
			assertEq("a\nb", getValue(xc, jnameb));
			xd = xp.createXDDocument("C");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_1w.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq("Music Library", 
				getJValue(((mytest.xdef.component.Js3_1w)xc).getjw$map(),
				jnameb));
			setJValue(((mytest.xdef.component.Js3_1w)xc)
				.getjw$map(), jnameb, "123");
			assertEq("123", getJValue(((mytest.xdef.component.Js3_1w)xc)
				.getjw$map(), jnameb));
			setJValue(((mytest.xdef.component.Js3_1w)xc)
				.getjw$map(), jnameb, "a\nb");
			assertEq("a\nb", getJValue(((mytest.xdef.component.Js3_1w)xc)
				.getjw$map(), jnameb));

			json = "{\""+knma+"\":{\""+knmb+"\":123}}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("D");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_1i.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq(123, getValue(xc, jnameb));
			setValue(xc, jnameb, 1);
			assertEq(1, getValue(xc, jnameb));
			
			xd = xp.createXDDocument("D");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_1wi.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq(123, getJValue(((mytest.xdef.component.Js3_1wi)xc)
				.getjw$map(), jnameb));
			setJValue(((mytest.xdef.component.Js3_1wi)xc)
				.getjw$map(), jnameb, 1);
			assertEq(1, getJValue(((mytest.xdef.component.Js3_1wi)xc)
				.getjw$map(), jnameb));
		} catch (Exception ex) {fail(ex);}
/*ss*
		try {
			knma = "";
			jnamea = GenXComponent.javaName(JsonUtil.toXmlName(knma));
			knmb = "_";
			jnameb = GenXComponent.javaName(JsonUtil.toXmlName(knmb));
			xp = XDFactory.compileXD(props, 
"<xd:def xmlns:xd='" + _xdNS + "' name='E' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\":\"string()\", \""+knmb+"\": \"optional jstring()\"}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\":\"string()\", \""+knmb+"\": \"optional jstring()\"}\n" +
"</jw:json>\n" +
"</xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' name='F' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\":\"int()\", \""+knmb+"\": \"optional int()\"}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\":\"int()\", \""+knmb+"\": \"optional int()\"}\n" +
"</jw:json>\n" +
"  <xd:component>\n" +
"    %class mytest.xdef.component.Js3_2 %link E#js:json;\n" +
"    %class mytest.xdef.component.Js3_2w %link E#jw:json;\n" +
"    %class mytest.xdef.component.Js3_2i %link F#js:json;\n" +
"    %class mytest.xdef.component.Js3_2wi %link F#jw:json;\n" +
"  </xd:component>\n" +
"</xd:def>");
//		System.exit(0);	
//melo by byt: jgetA+X20_B().jgeta_x20_b();  jgetA+X20_B().jseta_x20_b(xxx)
			assertNoErrors(
				GenXComponent.genXComponent(xp, "test/", "UTF-8", false, true));
			
			jnamea = GenXComponent.javaName(JsonUtil.toXmlName(knma));
			jnameb = GenXComponent.javaName(JsonUtil.toXmlName(knmb));
			json = "{\""+knma+"\":\"A B\", \""+knmb+"\": \"C D\"}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("E");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_2.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq("A B", ((mytest.xdef.component.Js3_2)xc).get$_());
			assertEq("C D", ((mytest.xdef.component.Js3_2)xc).get_x5f_());
			((mytest.xdef.component.Js3_2)xc).set$_("a");
			((mytest.xdef.component.Js3_2)xc).set_x5f_("b");
			assertEq("a",((mytest.xdef.component.Js3_2)xc).get$_());
			assertEq("b",((mytest.xdef.component.Js3_2)xc).get_x5f_());
			
			xd = xp.createXDDocument("E");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_2w.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq("A B", ((mytest.xdef.component.Js3_2w)xc).jget$_());
			assertEq("C D", ((mytest.xdef.component.Js3_2w)xc).jget_x5f_());
			((mytest.xdef.component.Js3_2w)xc).jset$_("a");
			((mytest.xdef.component.Js3_2w)xc).jset_x5f_("b");
			assertEq("a",((mytest.xdef.component.Js3_2w)xc).jget$_());
			assertEq("b",((mytest.xdef.component.Js3_2w)xc).jget_x5f_());

			json = "{\""+knma+"\": 11, \""+knmb+"\": -22}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("F");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_2i.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq(11, ((mytest.xdef.component.Js3_2i)xc).get$_());
			assertEq(-22, ((mytest.xdef.component.Js3_2i)xc).get_x5f_());
			((mytest.xdef.component.Js3_2i)xc).set$_(0);
			((mytest.xdef.component.Js3_2i)xc).set_x5f_(1);
			assertEq(0,((mytest.xdef.component.Js3_2i)xc).get$_());
			assertEq(1,((mytest.xdef.component.Js3_2i)xc).get_x5f_());
			
			xd = xp.createXDDocument("F");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_2wi.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq(11, ((mytest.xdef.component.Js3_2wi)xc).jget$_());
			assertEq(-22, ((mytest.xdef.component.Js3_2wi)xc).jget_x5f_());
			((mytest.xdef.component.Js3_2wi)xc).jset$_(0);
			((mytest.xdef.component.Js3_2wi)xc).jset_x5f_(1);
			assertEq(0,((mytest.xdef.component.Js3_2wi)xc).jget$_());
			assertEq(1,((mytest.xdef.component.Js3_2wi)xc).jget_x5f_());
		} catch (Exception ex) {fail(ex);}
/*ss*
		try {
			knma = "a";
			jnamea = GenXComponent.javaName(JsonUtil.toXmlName(knma));
			knmb = "b";
			jnameb = GenXComponent.javaName(JsonUtil.toXmlName(knmb));
			xp = XDFactory.compileXD(props, 
"<xd:def xmlns:xd='" + _xdNS + "' name='G' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\":{\""+knma+"\":\"string()\",\""+knmb+"\":\"jstring()\"}}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\":{\""+knma+"\":\"string()\",\""+knmb+"\":\"jstring()\"}}\n" +
"</jw:json>\n" +
"</xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' name='H' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knma+"\":{\""+knma+"\":\"int()\", \""+knmb+"\": \"optional int()\"}}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knma+"\":{\""+knma+"\":\"int()\", \""+knmb+"\": \"optional int()\"}}\n" +
"</jw:json>\n" +
"  <xd:component>\n" +
"    %class mytest.xdef.component.Js3_3 %link G#js:json;\n" +
"    %class mytest.xdef.component.Js3_3w %link G#jw:json;\n" +
"    %class mytest.xdef.component.Js3_3i %link H#js:json;\n" +
"    %class mytest.xdef.component.Js3_3wi %link H#jw:json;\n" +
"  </xd:component>\n" +
"</xd:def>");
			assertNoErrors(
				GenXComponent.genXComponent(xp, "test/", "UTF-8", false, true));
			jnamea = GenXComponent.javaName(JsonUtil.toXmlName(knma));
			jnameb = GenXComponent.javaName(JsonUtil.toXmlName(knmb));
			json = "{\""+knma+"\":{\""+knma+"\":\"a b\",\""+knmb+"\":\"b c\"}}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("G");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_3.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq("a b", ((mytest.xdef.component.Js3_3)xc).geta());
			assertEq("b c", ((mytest.xdef.component.Js3_3)xc).getb());
			((mytest.xdef.component.Js3_3)xc).seta("a");
			((mytest.xdef.component.Js3_3)xc).setb("b");
			assertEq("a",((mytest.xdef.component.Js3_3)xc).geta());
			assertEq("b",((mytest.xdef.component.Js3_3)xc).getb());
			
			xd = xp.createXDDocument("G");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_3w.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq("a b", ((mytest.xdef.component.Js3_3w)xc).getjw$map().jgeta());
			assertEq("b c", ((mytest.xdef.component.Js3_3w)xc).getjw$map().jgetb());
			((mytest.xdef.component.Js3_3w)xc).getjw$map().jseta("a");
			((mytest.xdef.component.Js3_3w)xc).getjw$map().jsetb("b");
			assertEq("a",((mytest.xdef.component.Js3_3w)xc).getjw$map().jgeta());
			assertEq("b",((mytest.xdef.component.Js3_3w)xc).getjw$map().jgetb());

			json = "{\""+knma+"\":{\""+knma+"\": 11,\""+knmb+"\": 22}}";
			js = JsonUtil.parse(json);
			xd = xp.createXDDocument("H");
			js1 = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_3i.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq(11, ((mytest.xdef.component.Js3_3i)xc).geta());
			assertEq(22, ((mytest.xdef.component.Js3_3i)xc).getb());
			((mytest.xdef.component.Js3_3i)xc).seta(0);
			((mytest.xdef.component.Js3_3i)xc).setb(1);
			assertEq(0,((mytest.xdef.component.Js3_3i)xc).geta());
			assertEq(1,((mytest.xdef.component.Js3_3i)xc).getb());
			
			xd = xp.createXDDocument("H");
			js1 = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.xdef.component.Js3_3wi.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.xmlToJson(xc.toXml()), js));
			assertEq(11, ((mytest.xdef.component.Js3_3wi)xc).getjw$map().jgeta());
			assertEq(22, ((mytest.xdef.component.Js3_3wi)xc).getjw$map().jgetb());
			((mytest.xdef.component.Js3_3wi)xc).getjw$map().jseta(0);
			((mytest.xdef.component.Js3_3wi)xc).getjw$map().jsetb(1);
			assertEq(0,((mytest.xdef.component.Js3_3wi)xc).getjw$map().jgeta());
			assertEq(1,((mytest.xdef.component.Js3_3wi)xc).getjw$map().jgetb());
		} catch (Exception ex) {fail(ex);}
/*XX*			
		try {
			xdef = 
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/3.2\" name='A' root='js:map'\n" +
"  xmlns:js=\"http://www.w3.org/2005/xpath-functions\">\n" +
"    <js:map>\n" +
"      <js:string key=\"string();\"\n" +
"        xd:script=\"optional; match @key=='';\">\n" +
"        <xd:text>\n" +
"          optional jstring();\n" +
"        </xd:text>\n" +
"      </js:string>\n" +
"    </js:map>\n" +
"</xd:def>";
			el = XDGenCollection.genCollection(new String[]{xdef},
				true, true, true);
			xdef = KXmlUtils.nodeToString(el, true);
			System.out.println(xdef);
			xp = XDFactory.compileXD(null, xdef);
		} catch (Exception ex) {
			fail(ex);
			System.err.println(xdef);
		}
/*XX*/
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(false);
		if (runTest(args) > 0) {System.exit(1);}
	}

}