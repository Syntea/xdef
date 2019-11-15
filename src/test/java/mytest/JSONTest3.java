/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import buildtools.XDTester;

/** Test names of getter/setter.
 * @author Vaclav Trojan
 */
public class JSONTest3 extends XDTester {

	public JSONTest3() {super();}

	/** Run a JSON getter on the X-component.
	 * @param xc X-component.
	 * @param name name of setter.
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
	 * @param name name of setter.
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
		String jname;
		Object js, js1;
		ArrayReporter reporter = new ArrayReporter();
		Properties props = new Properties();
//		props.setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS); // errors
//		props.setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		String knm = "";
//		String knm = " a,\n\t?:b ";
		try {
			xp = XDFactory.compileXD(props, 
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knm+"\": \"optional jstring()\"}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knm+"\": \"optional jstring()\"}\n" +
"</jw:json>\n" +
"</xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' name='B' root='js:json | jw:json'\n" +
"        xmlns:js='"+XDConstants.JSON_NS_URI+"'\n" +
"        xmlns:jw='"+XDConstants.JSON_NS_URI_W3C+"'>\n" +
"<js:json>\n" +
"{\""+knm+"\": \"optional int()\"}\n" +
"</js:json>\n" +
"<jw:json>\n" +
"{\""+knm+"\": \"optional int()\"}\n" +
"</jw:json>\n" +
"  <xd:component>\n" +
"    %class mytest.component.Js3 %link A#js:json;\n" +
"    %class mytest.component.Js3w %link A#jw:json;\n" +
"    %class mytest.component.Js3i %link B#js:json;\n" +
"    %class mytest.component.Js3wi %link B#jw:json;\n" +
"  </xd:component>\n" +
"</xd:def>");
			String dir = "src/test/java/";
			assertNoErrors(
				GenXComponent.genXComponent(xp, dir, "UTF-8", false, true));
		} catch (Exception ex) {
			fail(ex);
			return;
		}
		try {
			json = "{\""+knm+"\":\"Music Library\"}";
			jname = GenXComponent.javaName(JsonUtil.toXmlName(knm));
//System.out.println(jname);
			js1 = JsonUtil.parse(json);
			xd = xp.createXDDocument("A");
			js = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.component.Js3.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq("Music Library", getJValue(xc, jname));
			setJValue(xc, jname, "123");
			assertEq("123", getJValue(xc, jname));
			setJValue(xc, jname, "a\nb");
			assertEq("a\nb", getJValue(xc, jname));
			xd = xp.createXDDocument("A");
			js = xd.jparse(json, "jw:json", reporter);
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.component.Js3w.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq("Music Library", getJValue(xc, jname));
			setJValue(xc, jname, "123");
			assertEq("123", getJValue(xc, jname));
			setJValue(xc, jname, "a\nb");
			assertEq("a\nb", getJValue(xc, jname));
			
			json = "{\""+knm+"\":123}";
			js1 = JsonUtil.parse(json);
			xd = xp.createXDDocument("B");
			js = xd.jparse(json, "js:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			xc = xd.jparseXComponent(json,
				mytest.component.Js3i.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertEq(123, getJValue(xc, jname));
			setJValue(xc, jname, 1);
			assertEq(1, getJValue(xc, jname));
			xd = xp.createXDDocument("B");
			js = xd.jparse(json, "jw:json", reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(js1, js));
			xc = xd.jparseXComponent(json,
				mytest.component.Js3wi.class, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(xc.toJson(), js));
			assertTrue(JsonUtil.jsonEqual(js1, js));
			assertEq(123, getJValue(xc, jname));
			setJValue(xc, jname, 1);
			assertEq(1, getJValue(xc, jname));
		} catch (Exception ex) {fail(ex);}
	}
	
	public static void main(String[] args) {
		XDTester.setFulltestMode(false);
		if (runTest(args) > 0) {System.exit(1);}
	}
	
}
