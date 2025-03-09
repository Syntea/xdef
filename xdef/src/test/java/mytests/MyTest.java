package mytests;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.impl.XConstants;
import org.xdef.XDFactory;
import org.xdef.model.XMDefinition;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xon.XonUtils;
import static org.xdef.xon.XonYaml.yamlToJsonXScript;
import org.yaml.snakeyaml.Yaml;
import test.XDTester;

/** Tests...
 * @author Vaclav Trojan
 */
public class MyTest extends XDTester {

	public MyTest() {super();}

	private static Object yamlToJson(final Object o) {
		if (o == null) return null;
		if (o instanceof Map) {
			Map m = (Map) o;
			Map<String, Object> result = new LinkedHashMap<>();
			for (Object x: m.entrySet()) {
				Map.Entry en = (Map.Entry) x;
				Object y = en.getKey();
				String key = (y instanceof byte[]) ? new String((byte[])y) : (String) y;
				Object z = yamlToJson(en.getValue());
				result.put(key, z);
			}
			return result;
		} else if (o instanceof List) {
			List list = (List) o;
			List<Object> result = new ArrayList<>();
			for (Object x: list) {
				result.add(yamlToJson(x));
			}
			return result;
		} else if (o instanceof byte[]) {
			return new String((byte[]) o);
		}
		return o;
	}

	private Object testExample(String xdef, String data) throws Exception {
		XDPool xp = XDFactory.compileXD(null, xdef);
		XDDocument xd = xp.createXDDocument("Example");
		ArrayReporter reporter = new ArrayReporter();
		org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
		String s;
		if (data.trim().startsWith("<") && data.trim().endsWith(">")) { // XML
			s = XonUtils.toJsonString(XonUtils.xmlToXon(data));
		} else {
			try { // try JSON
				XonUtils.parseJSON(data);
				s = data;
			} catch (RuntimeException ex) {
				try { // try XON
					XonUtils.parseXON(data);
					s = data;
				} catch (RuntimeException exx) {
					try {// try YAML
						StringReader sr = new StringReader(data);
						Object o = yaml.load(sr);
						s = XonUtils.toJsonString(yamlToJson(o), true);
					} catch (Exception exy) {
						s = data;
					}
				}
			}
		}
		xd.jparse(s, reporter);
		Object resultXon = xd.getXon();
		reporter.checkAndThrowErrorWarnings();
		return resultXon;
	}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false

		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, "");
//		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
//			XConstants.XDPROPERTYVALUE_DBG_SHOWXON);

////////////////////////////////////////////////////////////////////////////////

		ArrayReporter reporter = new ArrayReporter();
		Properties props;
		String s, xml;
		StringWriter swr;
		Object x;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		String xdef;
		Yaml yaml;
////////////////////////////////////////////////////////////////////////////////
/**/
		try {
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd     =\"http://www.xdef.org/xdef/4.0\" name=\"Vehicle\" root=\"Vehicle\"\n" +
"        impl-version =\"2024/06.0\" impl-date=\"2024-07-31\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"+;\" name=\"string()\" />\n" +
"   </Vehicle>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			XMDefinition xmd = xp.getXMDefinition("Vehicle");
			System.out.println(xmd.getImplProperty("version"));
			System.out.println(xmd.getImplProperty("date"));
		} catch (RuntimeException ex) {fail(ex);}
//if (true) return;
/**
		try {
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd     =\"http://www.xdef.org/xdef/4.0\" root=\"Vehicle\"\n" +
"        impl-version =\"2024/06.0\" impl-date=\"2024-07-31\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"1..; ref Part\" />\n" +
"   </Vehicle>\n" +
"   <Part name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part2\"/>\n" +
"   </Part>\n" +
"   <Part2 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part3\"/>\n" +
"   </Part2>\n" +
"   <Part3 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part4\"/>\n" +
"   </Part3>\n" +
"   <Part4 name=\"string()\"/>\n" +
"</xd:def>";
			xml =
"<Vehicle>\n" +
"   <Part name=\"a1\">\n" +
"      <Part name=\"a2\" />\n" +
"      <Part name=\"a3\" />\n" +
"   </Part>  \n" +
"</Vehicle>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			assertEq(xml, xd.xparse(xml, null));
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			s = // result ???
"<Vehicle><Part name='a1'>\n"+
"  <Part name='a2'/><Part name='a3'/>\n"+
  "<Part name='a2'/><Part name='a3'/>\n"+
  "</Part>\n"+
"</Vehicle>";
			assertEq(s, xd.xcreate("Vehicle", null));
			xdef =
"<xd:def xmlns:xd     =\"http://www.xdef.org/xdef/4.0\" root=\"Vehicle\"\n" +
"        impl-version =\"2024/06.0\" impl-date=\"2024-07-31\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"1..; ref Part\" />\n" +
"   </Vehicle>\n" +
"   \n" +
"   <Part name=\"string()\">\n" +
"      <Part xd:script=\"1; ref Part2\"/>\n" +
"   </Part>\n" +
"   <Part2 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part3\"/>\n" +
"   </Part2>\n" +
"   <Part3 name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref Part4\"/>\n" +
"   </Part3>\n" +
"   <Part4 />\n" +
"</xd:def>";
			xp = compile(xdef);
			assertEq(xml, xd.xparse(xml, null));
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			s = "<Vehicle><Part name='a1'><Part name='a2'/></Part></Vehicle>";
			assertEq(s, xd.xcreate("Vehicle", null));
		} catch (Exception ex) {fail(ex);}
if (T) return;
/**
		try {
			x = XonUtils.parseYAML("% 1\n +- 2");
			System.out.println(x);
		} catch (Exception ex) {fail(ex.toString());}
if (true) return;
if (T) return;
/**
		try {
			System.out.println(parseXon("[d1945+00:00]"));
		} catch (Exception ex) {fail(ex.toString());}
if (true) return;
if (T) return;
/**
		try {
			for (String t: new String[] {
				"\"true\"",
				"null",
				"1l",
				"-1.25e-3f",
				"0N",
				"-3e-5D",
				// string
				"\"\"",
				"\"\\u0045\"", // string with UTFChar
				"\"\\n\\t\\\\\\\"\\b\"", // esccaped chars
				"d1949-11-07", // date
				"d1949-11-07T15:59:00Z",  // dateTime
				"d1949-11-07T15:59:01.123",
				"d1949-11-07T15:59:01.123-02:00",
				"d1945 ", //* GYear *
				"d1945Z",
				"d1945-02:00",
				"d1945+00:00",
				"d---29", //* GDay *
				"d---29-02:00",
				"d--05", //* GMonth *
				"d--12-02-02:00",
				"d--12-29 ", //* GMonthDay *
				"d--12-29-02:00",
				"d11:20:00 ", //* time *
				"d11:20:31",
				"d11:20:31.123",
				"d11:20:31.123Z",
				"d11:20:31.123-02:00",
				"d11:20:31+02:00",
				// Duration
				"P2Y6M5DT12H35M30S",
				"P1Y2M3DT10H30M123.123456S",
				"-P1DT2H",
				"P1Y1M1DT1H1M1.1234567S",
				// Email address
				"e\"a@b\"",
				"e\"a@b (A. Bc)\"",
				"e\"A. Bc <x-y.z@cd.ef>\"",
				// bytes
				"b()",
				"b( a b\n\tc = )",
				// GPS
				"g(1,0)",
				"g(1.5, -3, -5)",
				"g(1.5, -3, -5, Lon)",
				"g(1.5, -3, -5, \"a b\")",
				"g(0, 0, Lon)",
				"g(-0, 0, \"a b\")",
				"g(0, 0, Lon3, city/Center)",
				// Currency
				"C(CZK)",
				"C(USD)",
				// Price
				"p(12 CZK)",
				"p(0.0 USD)",
				// Character
				"c\" \"",
				"c\"\\u0045\"",
				// URI
				"u\"https://org.xdef/ver1\"",
				// InetAddr
				"/0.00.000.038",
				"/129.255.0.99",
				"/0:0:0:0:0:0:0:0",
				"/FFFF:0:0:0:8:800:000C:417A",
				"/ffff:0:0:0:8:800:000C:417a",
				// Complex values
				"{}",
				"{\"\":\"\"}",
				"{\"A B\":{\"a b\":\"\"}}",
				"[]",
				"[[]]",
				"[[{}]]",
				"[[[]]]",
				"[[[1b]]]",
				"[[],[]]",
				"[[1d,2D],[null,4s]]",
				"[{a:{b:1}}]",
				"[{a:[]}]",
				"[{a:[1]}]",
				"[{a:[[]]}]",
				"[{a:[{}]}]",
				"[{a:[{a:1}]}]",
				"[1, -1.25e-3, true, \"abc\", null]",
				"{ a:{}, _b:[], c_:null }",
				"[[3,null,false],[3.14,\"\",false],[\"aa\",true,false]]",
				"[1, { _x69_:1, _x5f_map:null, \"a_x\tb\":[null], item:{}}]",
				"[1, {_a._b:1,b-c:\"a\",\"\":false,array:[],map:{}},\"abc\"]",
				"[{a:[{},[1,2],{},[3,4]]}]",
				"[{a:[{a:1},[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{},[3,4]]}]",
				"[{a:[[1,2],{a:1},[30,4]]}]",
				"[{a:[[1,2],[3,4],{}]}]",
				"[{a:[[1,2],[3,4],{a:1,b:2}]}]"}) {
				assertEq(t, parseXon(t));
			}
		} catch (Exception ex) {fail(ex.toString());}
if (true) return;
if (T) return;
/**/
		try {
			int _hour = 24;
			int _minute = 0;
			int _second = 0;
			double _fraction = Double.NEGATIVE_INFINITY;
			System.out.println(_fraction != Double.NEGATIVE_INFINITY);
			if (_hour > 24 || _minute > 0 || _second > 0 ||	_fraction > 0.0D || _fraction != Double.NEGATIVE_INFINITY) {
				System.out.println("XXX " + (_fraction > 0.0D) + ", " + (_fraction != Double.NEGATIVE_INFINITY));
			}
		} catch (RuntimeException ex) {fail(ex);}
//if (true) return;
if (T) return;
/**/
		try { // test property  "xdef_defaultZone"
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"root\">\n" +
"  <root datum=\"xdatetime('yyyy-MM-ddTHH:mm:ss[Z]','yyyy-MM-ddTHH:mm:ss');\" />\n" +
"</xd:def>";
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			XDFactory.compileXD(props, xdef);
		} catch (RuntimeException ex) {fail(ex);}
//if (true) return;
if (T) return;
/**/
		try { // test property  "xdef_defaultZone"
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a=\"ydatetime('yyyy-MM-ddTHH:mm:ss[Z]', 'yyyy-MM-ddTHH:mm:ssZ');\"/>\n" +
"  <xd:component> %class test.xdef.TestTXX%link a; </xd:component>\n"+
"</xd:def>";
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props, xdef);
			xml = "<a a='2024-10-22T11:55:30'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			assertEq("<a a='2024-10-22T11:55:30+02:00'/>", parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T11:55:30+02:00'/>", xc.toXml());
			xml = "<a a='2024-10-22T11:55:30-03:33'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			assertEq(TimeZone.getTimeZone("CET"), xd.getDefaultZone());
			assertEq("<a a='2024-10-22T17:28:30+02:00'/>", parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T17:28:30+02:00'/>", xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if (true) return;
if (T) return;
/**/
		try { // test property  "xdef_defaultZone"
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a='dateTime();\n" +
"        onTrue {\n" +
"          Datetime d = (Datetime) getParsedValue();\n" +
"          outln(d.getZoneName());\n" +
"          outln(d.toString());\n" +
"          d.setZoneName(\"+03:01\");\n" +
"          outln(d.toString());\n" +
"        }'/>\n" +
"  <xd:component> %class test.xdef.TestTZ%link a; </xd:component>\n"+
"</xd:def>";
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props, xdef);
			xml = "<a a='2024-10-22T11:55:30'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			assertEq(TimeZone.getTimeZone("CET"), xd.getDefaultZone());
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(swr.toString(), "CET\n2024-10-22T11:55:30+02:00\n2024-10-22T12:56:30.0+03:01\n");
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T12:56:30.0+03:01'/>", xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
//if (true) return;
if (T) return;

/**/
		try { // test property  "xdef_defaultZone"
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a='dateTime();\n" +
"        onTrue {\n" +
"          Datetime d = (Datetime) getParsedValue();\n" +
"          outln(d.getZoneName());\n" +
"          outln(d.toString());\n" +
"          d.setZoneName(\"GMT\");\n" +
"          outln(d.toString());\n" +
"          outln(d.getZoneName());\n" +
"          d.setZoneOffset(-3600000);\n" +
"          outln(d.toString());\n" +
"        }'/>\n" +
"  <xd:component> %class test.xdef.TestTZ%link a; </xd:component>\n"+
"</xd:def>";
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props, xdef);
			xml = "<a a='2024-10-22T11:55:30'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			assertEq(TimeZone.getTimeZone("CET"), xd.getDefaultZone());
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			assertEq(swr.toString(),
				"CET\n2024-10-22T11:55:30+02:00\n2024-10-22T09:55:30.0Z\nGMT\n2024-10-22T08:55:30.0-01:00\n");
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T08:55:30.0-01:00'/>", xc.toXml());
			xml = "<a a='2024-10-22T11:55:30+03:30'/>"; // zone SPECIFIED
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			assertEq(TimeZone.getTimeZone("CET"), xd.getDefaultZone());
			assertNoErrorsAndClear(reporter);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq(swr.toString(),
				"GMT\n2024-10-22T11:55:30+03:30\n2024-10-22T08:25:30.0Z\nGMT\n2024-10-22T07:25:30.0-01:00\n");
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T07:25:30.0-01:00'/>", xc.toXml());

			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a=\"xdatetime('yyyy-MM-ddTHH:mm[:ss][Z]', 'yyyy-MM-ddTHH:mmZ'); /* date and time, no seconds */\"/>\n" +
"  <xd:component> %class test.xdef.TestTZ1%link a; </xd:component>\n"+
"</xd:def>";
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props, xdef);
			genXComponent(xp, clearTempDir());
			xml = "<a a='2024-10-22T11:55:15'/>";
			assertEq("<a a='2024-10-22T11:55+02:00'/>", parse(xp, "", xml));
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T11:55:15+02:00'/>", xc.toXml());
			xml = "<a a='2024-10-22T11:55Z'/>";
			assertEq("<a a='2024-10-22T11:55Z'/>", parse(xp, "", xml));
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T11:55Z'/>", xc.toXml());
			assertEq(null, compile("<def xmlns='"+_xdNS+"'/>").getDefaultZone());
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a='dateYMDhms();'/>\n" +
"  <xd:component> %class test.xdef.TestTZ2%link a; </xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef);
			genXComponent(xp, clearTempDir());
			xml = "<a a='20241022115530'/>";
			assertEq("<a a='20241022115530'/>", parse(xp, "", xml));
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='20241022115530'/>", xc.toXml());
			assertEq("2024-10-22T11:55:30+02:00",
				((SDatetime) SUtils.getValueFromGetter(xc,"geta")).toString());
		} catch (RuntimeException ex) {fail(ex);}
	for (String t : TimeZone.getAvailableIDs()) {
		TimeZone tz = TimeZone.getTimeZone(t);
//		if (tz.getID().length() <= 3 && tz.getDSTSavings() != 0 || tz.getRawOffset() >= 43200000)
//		if (tz.getRawOffset() >= 46800000 || tz.getRawOffset() <= -46800000)
//		System.out.println(t + ";" + tz.getDisplayName() + ", " + tz.getID()
//			+ " (" + (tz.getRawOffset()/1000) + ", " + (tz.getDSTSavings()/1000) + ")");
	}
if (true) return;
if (T) return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='a' root='a'>\n"+
"<xd:json name=\"a\">\n" +
"{\n" +
"  A: {a:\"string()\", b:\"string()\"},\n" +
"  B: {a:\"string()\", b:\"string()\"}\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			s = "{ A: { a: \"x\", b: \"y\"}, B: { a: \"x\", b: \"y\"} }";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='a' root='a'>\n"+
"<xd:json name=\"a\">\n" +
"[ \"? jvalue()\",\n" +
"  {%script=\"occurs 1..*; ref person\"}\n" +
"]\n" +
"</xd:json>\n" +
"<xd:json name=\"person\">\n" +
"{ jmeno: \"string()\",\n" +
"  plat: \"? decimal()\",\n" +
"  nar: \"? date()\",\n" +
"  IP: [%script=\"?\", [%script=\"*\", \"* ipAddr()\"]]\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			s =
"[  true,\n" +
"   { jmeno: \"first\",\n" +
"     plat: 123.45D,\n" +
"     nar: d1960-01-01,\n" +
"     IP: [[/127.0.0.1],\n" +
"          [/129.144.52.38, /170.144.52.0, /170.144.52.1, /170.144.52.2,\n" +
"           /170.144.52.3, /170.144.52.4, /170.144.52.5],\n" +
"          [],\n" +
"          [/180.144.11.2]]\n" +
"   },\n" +
"   { jmeno: \"second\",\n" +
"     plat: 678.90D,\n" +
"     nar: d1950-10-12,\n" +
"     IP: [[/129.144.52.48, /129.144.52.49],\n" +
"       [/129.144.52.50],\n" +
"       [/129.144.52.51],\n" +
"       [/129.144.52.52],\n" +
"       [/129.144.52.53]]\n" +
"   },\n" +
"   { jmeno: \"third\" }\n" +
"]";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='a' root='a'>\n"+
"  <xd:json name=\"a\">\n" + yamlToJsonXScript(
"- \"? jvalue()\"\n" +
"- \"%script\": \"occurs 1..*;\"\n" +
"  jmeno: \"string();\"\n" +
"  plat: \"? decimal();\"\n" +
"  nar: \"? date();\"\n" +
"  IP: [\"%script=?\",[\"%script=*\",\"* ipAddr();\"]]") +
"  </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='a' root='a'>\n"+
"  <xd:json name=\"a\">\n" + yamlToJsonXScript(
"- \"? jvalue()\"\n" +
"- \"%script\": \"occurs 1..*; ref person\"") +
"  </xd:json>\n" +
"  <xd:json name=\"person\">\n" + yamlToJsonXScript(
"jmeno: \"string();\"\n" +
"plat: \"? decimal();\"\n" +
"nar: \"? date();\"\n" +
"IP: [\"%script=?\",[\"%script=*\",\"* ipAddr();\"]]") +
"  </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n"+
"<xd:json name=\"test\">\n"+
"  [%oneOf=\"ref A\"]\n"+
"</xd:json>\n"+
"<xd:json name=\"A\">\n"+
" [%oneOf,\n"+
"    \"jvalue();\",\n"+
"    [\"* jvalue();\" ],\n"+
"    {%anyName:\n"+
"       [%oneOf,\n"+
"         \"jvalue();\",\n"+
"         [\"* jvalue();\" ],\n"+
"         {%anyName: [%oneOf=\" ref test\"]}\n"+
"       ]\n"+
"    }\n"+
"  ]\n"+
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			s = "{ \"a\":1, \"b\":[2,3] }";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n"+
"<xd:json name=\"test\">\n" + yamlToJsonXScript(
"  - '%oneOf=ref A'\n") +
"</xd:json>\n"+
"<xd:json name=\"A\">\n" + yamlToJsonXScript(
"- '%oneOf'\n" +
"- jvalue();\n" +
"- ['* jvalue();']\n" +
"- '%anyName':\n" +
"  - '%oneOf'\n" +
"  - jvalue();\n" +
"  - ['* jvalue();']\n" +
"  - '%anyName': ['%oneOf=ref test']") +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" +
" [ %oneOf,  \"jvalue();\",\n" +
"   [ %script=\"*; ref anyA;\" ],\n" +
"   { %script=\"*; ref anyM;\" }\n" +
" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyA\">\n" +
" [ %anyObj=\"*\" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyM\">\n" +
" { %anyName: %anyObj=\"*;\" }\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			s = "{a:1, b:[],c:null,d:[], e:{}}";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" + yamlToJsonXScript(
"- '%oneOf'\n" +
"- jvalue();\n" +
"- ['%script=*; ref anyA;']\n" +
"- {'%script': '*; ref anyM;'}\n") +
"</xd:json>\n" +
"<xd:json name=\"anyA\">\n" + yamlToJsonXScript(
" [\"%anyObj=*\"]") +
"</xd:json>\n" +
"<xd:json name=\"anyM\">\n" + yamlToJsonXScript(
" { \"%anyName\": \"%anyObj=*;\" }") +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			s = "\"abcd\"";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			s = "[1, \"true\"]";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			s = "{a:1, b:[],c:null,d:[], e:{}}";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			s = "[{}]";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='X' root='A'>\n" +
"  <xd:json name='A'>\n" +
				yamlToJsonXScript(
"  \"%anyName\":\n"+
"    - \"%script=?\"\n"+
"    - \"%anyObj=*\"\n"+
"  a: \"%anyObj=?\"") +
"  </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			s = "{ a:0, x: [1,2], y: [] }";
			x = XonUtils.parseXON(s);
			assertEq("", XonUtils.xonDiff(x, jparse(xp, "", s, reporter)));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='Example' root='root'>\n"+
"  <xd:declaration scope='local'>\n" +
"    boolean b = false;\n" +
"    type t1 int();\n" +
"    ParseResult x() {\n" +
"       ParseResult p = t1();\n"+
"       if (p.matches()) {\n"+
"			b = true;\n"+
"       }\n"+
"       return p;\n" +
"    }\n" +
"  </xd:declaration>\n" +
"  <root a='x(); finally if (!b) error(b);' />\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<root a='123'/>";
			assertEq(xml, parse(xp, "Example", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<root a='a'/>";
			assertEq(xml, parse(xp, "Example", xml, reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <xd:declaration>\n"+
"    int i = 0;\n"+
"    Container c = [[%b=[%a = 'a', %b = 'b']], [%b=[%a = 'c', %b = 'd']]];\n"+
"  </xd:declaration>\n"+
"  <a xd:script='create c;'>\n"+
"    <b xd:script='occurs +;' a='string(1)' b='string(1)'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xp.displayCode();
			xml = "<a><b a='a' b='b'/><b a='c' b='d'/></a>";
			assertEq(xml, create(xp, "", "a", reporter));
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument();
			assertEq(xml, xd.xparse(xml, null));
		} catch (RuntimeException ex) {fail(ex);}
if (T) return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A|B'>\n" +
"<xd:declaration scope='global'>\n" +
"     type  skodaTextDN string(1,4000);\n" +
"     type  castkaKcDN  long(0,999_999_999);\n" +
"</xd:declaration>\n"+
"  <A>\n" +
"    <B xd:script=\"*\"><C xd:script=\"0..1; ref C\"/></B>\n" +
"    <D xd:script=\"*\"><C xd:script=\"0..1; ref C\"/></D>\n" +
"  </A>\n" +
"  <B>\n" +
"    <Q xd:script=\"*\"><E xd:script=\"0..1; ref E\"/></Q>\n" +
"    <R xd:script=\"*\"><E xd:script=\"0..1; ref E\"/></R>\n" +
"  </B>\n"+
"  <C c='castkaKcDN'/>\n" +
"  <E e='skodaTextDN'>? string() </E>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xd = xp.createXDDocument("");
			if (reporter.errorWarnings())System.err.println(reporter);
			xml = "<B><Q><E e='1'>x</E></Q><R><E e='2'>y</E></R></B>";
			xd.xparse(xml, reporter);
			if (reporter.errorWarnings())System.err.println(reporter);
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try { // test forget
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"Example\" root=\"test\">\n" +
"  <xd:component>\n"+
"    %class test.xdef.data.TestXonForget%link test;\n"+
"  </xd:component>\n"+
"  <xd:json name=\"test\">\n" +
"    {date: \"date()\",\n" +
"      cities: [\n" +
"        { %script = \"occurs 1..*;finally outln(); forget\",\n" +
"          \"from\": [\n" +
"            \"string(); finally out('From ' + getText());\",\n" +
"            { %script = \"occurs 1..*;\",\n" +
"              \"to\": \"jstring();finally out(' to '+getText()+' is ');\",\n"+
"              \"distance\": \"int(); finally out(getText() + ' km');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }"+
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
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
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument("Example");
			xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			x = xd.jparse(s, reporter);
			swr.close();
			assertEq(swr.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
			xd = xp.createXDDocument("Example");
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			swr.close();
			assertEq(swr.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertNoErrors(reporter);
			x = xc.toXon();
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
		} catch (IOException | SRuntimeException ex) {fail(ex);}
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, "");
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"Example\" root=\"test\">\n" +
"  <xd:json name=\"test\">\n" +
"    { date: \"date()\",\n" +
"      cities: [\n" +
"        { %script= \"occurs 1..*\",\n" +
"          from: [\n" +
"            \"string()\",\n" +
"            { %script= \"occurs 1..*\", to: \"jstring()\", distance: \"int()\" }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  </xd:json> \n" +
"</xd:def>";
			s =
"{ \"date\" : \"2020-02-22\",\n" +
"  \"cities\" : [ \n" +
"    { \"from\": [\"Brussels\",\n" +
"        {\"to\": \"London\", \"distance\": 322}, {\"to\": \"Paris\", \"distance\": 265}\n" +
"      ]\n" +
"    },\n" +
"    { \"from\": [\"London\",\n" +
"        {\"to\": \"Brussels\", \"distance\": 322}, {\"to\": \"Paris\", \"distance\": 344}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}";
			x = testExample(xdef, s);
			s = XonUtils.toJsonString(x, true);
			x = testExample(xdef, s);
			s = XonUtils.toXonString(x, true);
			x = testExample(xdef, s);
			swr = new StringWriter();
			yaml = new org.yaml.snakeyaml.Yaml();
			yaml.dump(XonUtils.xonToJson(x), swr);
			swr.close();
			s = swr.toString();
			testExample(xdef, s);
		} catch (Exception ex) {fail(ex);}
		try {
			s =
"%YAML 1.2\n" +
"---\n" +
"!!seq [\n" +
"  !!str \"literal\\n\",\n" +
"  !!str \"·folded\\n\",\n" +
"  !!str \"keep\\n\\n\",\n" +
"  !!str \"·strip\",\n" +
"]";
			yaml = new org.yaml.snakeyaml.Yaml();
			x = yaml.load(s);
			System.out.println(XonUtils.toJsonString(x, true));
			swr = new StringWriter();
			yaml.dump(XonUtils.xonToJson(x), swr);
//			swr.close();
			System.out.println(swr.toString());
			s =
"%YAML 1.2\n" +
"---\n" +
"!!seq [\n" +
"  !!str \"a\",\n" +
"  !!str \"b\",\n" +
"  &A !!str \"c\",\n" +
"  *A,\n" +
"  !!str \"\",\n" +
"]";
			yaml = new org.yaml.snakeyaml.Yaml();
			x = yaml.load(s);
			System.out.println(XonUtils.toJsonString(x, true));
			swr = new StringWriter();
			yaml.dump(XonUtils.xonToJson(x), swr);
//			swr.close();
			System.out.println(swr.toString());
			s =
"%YAML 1.2\n" +
"---\n" +
"!!seq [\n" +
"  !!seq [ !!float \"1.25e0\", !!bool \"true\" ],\n" +
"  !!map { ? !!str \"a\" : !!str 'b' },\n" +
"  !!int \"-2\",\n" +
"  !!str \"b\",\n" +
"  !!str \"c\",\n" +
"]";
			yaml = new org.yaml.snakeyaml.Yaml();
			x = yaml.load(s);
			System.out.println(XonUtils.toJsonString(x, true));
			yaml = new org.yaml.snakeyaml.Yaml();
			swr = new StringWriter();
			yaml.dump(x, swr);
			System.out.println(swr.toString());
			swr = new StringWriter();
			yaml.dump(XonUtils.xonToJson(x), swr);
			System.out.println(swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

	public static XDParseResult ydatetime(XXNode xnode, String m1) {
		return ydatetime(xnode, m1, null);
	}

	public static XDParseResult ydatetime(XXNode xnode, String m1, String m2) {
		return null;
	}
}
/**
s ::= [#9#10#13 ]+
IntNumber ::= [0-9]+
SignedInteger ::= "-"? IntNumber
Exponent ::= ('e' | 'E') ("+" | "-")? Integer
FloatPart ::= "." Integer Exponent? | Exponent
FloatNumber ::= SignedInteger FloatPart
Byte ::= SignedInteger "b"
Short ::= SignedInteger "s"
Integer ::= SignedInteger "i"
Long ::= SignedInteger "l" ?
Decimal ::= SignedInteger "D" | FloatNumber "D"
Float ::= SignedInteger "f" | FloatNumber "f" | "NaNf" | "-" ? "INFf"
Double ::= SignedInteger "d" | FloatNumber "f"? | "NaN" | "-" ? "INF"

yearFrag ::= '-'? [1-9] [0-9]* | [0-9]+
monthFrag ::= ('0' [1-9]) | ('1' [0-2])
dayFrag ::= ('0' [1-9]) | ([12] digit) | ('3' [01])
hourFrag ::= ([01] digit) | ('2' [0-3])
minuteFrag ::= [0-5] digit
secondFrag ::= ([0-5] digit) ('.' digit+)?
endOfDayFrag ::= '24:00:00' ('.' '0'+)?
timezoneFrag ::= 'Z' | ('+' | '-') (('0' digit | '1' [0-3]) ':' minuteFrag | '14:00')

dateFrag ::= yearFrag '-' monthFrag '-' dayFrag
date ::= 'd' dateFrag  timezoneFrag?
timeFrag ::= (hourFrag ':' minuteFrag ':' secondFrag | '24:00:00' ) ('.' [0-9]+)
time ::= 'd' timeFrag timezoneFrag?
dateTime ::= 'd' dateFrag 'T' timeFrag timezoneFrag?
			 | endOfDayFrag) timezoneFrag?
Date ::= 'd' yearFrag '-' monthFrag '-' dayFrag ( 'T' timeFrag ) ? timezoneFrag?

duYearFrag ::= [0-9]+ 'Y'
duMonthFrag ::= [0-9]+ 'M'
duDayFrag ::= [0-9]+ 'D'
duHourFrag ::= [0-9]+ 'H'
duMinuteFrag ::= [0-9]+ 'M'
duSecondFrag ::= [0-9]+ ('.' [0-9]+)? 'S'

duYearMonthFrag ::= (duYearFrag duMonthFrag?) | duMonthFrag
duTimeFrag ::= 'T' ((duHourFrag duMinuteFrag? duSecondFrag?)
  | (duMinuteFrag duSecondFrag?) | duSecondFrag)
duDayTimeFrag ::= (duDayFrag duTimeFrag?) | duTimeFrag

Duration ::= '-'? 'P' ((duYearMonthFrag duDayTimeFrag?) | duDayTimeFrag)

Null ::= "null"
Boolean ::= "true" | "false"
StringPart ::= "\\" | '\"' | "\n" | "\r" | "\t" | "\f" | "\b" | OtherChar
UTFChar ::= "\\u" (JDEDEN backslash!) HexDigit HexDigit HexDigit HexDigit
HexDigit ::= $digit | "a" | "b" | "c" | "d" | "e" | "f"
  | "A" | "B" | "C" | "D" | "E" | "F"
AnyChar ::= $UTFChar - "\" - '"'  // $UTFChar is any valid UTF-16 character
OtherChar ::= UTFChar | AnyChar
String ::= '"' StringPart '"'
SimpleValue ::= S ( Float | Double | Byte | Short | Integer| Long | Decimal
  | DateValues | Duration | String | Boolean | Null )
Array ::= S "[" Value* S ]
Identifier ::= '_' | $letter ("_" | $letter | $digit)*
NamedValue ::= S Identifier S "=" Value
Map ::=  S"{" NamedValue* S "}"
Value ::= Array | Map | SimpleValue

Result type of values in Java:
String		String
Boolean		Boolean
Float		Double
SignedInteger	Long
Dewcimal	BigDecimal
DateValues	org.xdef.sys.SDatetime (interface javax.xml.datatype.XMLGregorianCalendar)
Duration	org.xdef.sys.SDuration (extension of javax.xml.datatype.Duration)
Null		null
*/
//https://www.w3.org/TR/xmlschema11-2/#dt-dt-7PropMod
