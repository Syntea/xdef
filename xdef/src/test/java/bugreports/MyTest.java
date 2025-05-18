package bugreports;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.impl.XConstants;
import org.xdef.model.XMData;
import org.xdef.proc.XXData;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTest extends XDTester {

	public MyTest() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	public static boolean a(boolean a, String b) {return true;}
	public static int b(String b) {return 0;}
	public static void c() {}
	private static Object toJson(final XComponent xc) {
		return XonUtils.xmlToXon(xc.toXml());
	}
	public static String xxx(XXNode xn) {return "" + xn.getXComponent();}
	private static boolean _xxx;
	final public static void setResult(XXNode xnode, boolean x) {_xxx = x;}
	final public static void setResult(XXData xnode, XDParser parser) {
		setResult(xnode, parser.check(null, xnode.getTextValue()));
	}
	final public static void setResult(XXNode xnode, XDParseResult result) {
		setResult(xnode, !result.errors());
	}
	private static String testAny(XDPool xp, String s) {
		String result = "";
		try {
			XDDocument xd = xp.createXDDocument("A");
			ArrayReporter reporter = new ArrayReporter();
			Object o = XonUtils.parseXON(s);
			Object x = xd.jparse(s, reporter);
			if (reporter.errorWarnings()) {
				result += "** 1\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 2\n" + o + "\n" + x + "\n";
			}
		} catch (RuntimeException ex) {
			result += ex + "\n";
		}
		return result;
	}

	/** Run test and display error information. */
	@Override
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////
		Object o, x, j;
		String json, s, xdef, xml;
		List list;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"<xd:declaration>\n" +
"  void x() {\n" +
"    Currency c = new Currency('USD');\n" +
"    out(c.currencyCode());\n" +
"  }\n" +
"</xd:declaration>\n"+
"  <A xd:script='finally x();' a='currency();' />\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			parse(xp, "", "<A a='USD'/>", reporter, swr=new StringWriter(), null, null);
			assertNoErrors(reporter);
			assertEq("USD", swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		try {
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"<xd:declaration>\n" +
"  void testLoop() {\n"+
"    for (int i=0; ;) {\n"+
"      if (i GT 4) break;\n"+
"      if (i++ LE 1) continue;\n"+
"      if (i NE 3) out('ERR1: i = ' + i);\n"+
"      break;\n"+
"    }\n"+
"    out('1');\n"+
"    for (int i=0; i LT 5;) {\n"+
"      if (i GT 4) break;\n"+
"      if (i++ LE 1) continue;\n"+
"      if (i NE 3) out('ERR2: i = ' + i);\n"+
"      break;\n"+
"    }\n"+
"    out('2');\n"+
"    for (int i=0; ; i++) {\n"+
"      if (i GT 4) break;\n"+
"      if (i LE 1) {continue;}\n"+
"      if (i != 3) out('ERR3: i = ' + i);\n"+
"      break;\n"+
"    }\n"+
"    out('3');\n"+
"    for (int i = 0; i LT 6; i++) {\n"+
"      if (i GT 4) break;\n"+
"      if (i == 0) continue;\n"+
"      if (i != 1) out('ERR4: i = ' + i);\n"+
"      break;\n"+
"    }\n"+
"    out('4');\n"+
"    int j= 0;\n"+
"    while (j LT 6) {\n"+
"      if (j GE 4) break;\n"+
"      if (j++ LE 1) continue;\n"+
"      if (j NE 3) out('ERR5: j = ' + j);\n"+
"      break;\n"+
"    }\n"+
"    out('5');\n"+
"    j = 0;\n"+
"    do {\n"+
"      if (j GE 4) break;\n"+
"      if (j++ LE 2) continue;\n"+
"      if (j NE 4) out('ERR6: j = ' + j);\n"+
"      break;\n"+
"    } while (j LT 6);\n"+
"    out('6');\n"+
"  }\n"+
"</xd:declaration>\n"+
"  <A xd:script='finally testLoop();' />\n" +
"</xd:def>");
			parse(xp, "", "<A/>", reporter, swr = new StringWriter(), null, null);
			assertNoErrors(reporter);
			assertEq("123456", swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		try {
			xdef = // sequence with separator
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:component>%class " + _package + ".MytestX_SQ %link #a;</xd:component>\n"+
"  <xd:declaration>\n"+
"    type s sequence(%separator=',', %item=[int, long, long]);\n"+
"  </xd:declaration>\n"+
"  <a a='? s'> ? s; <b xd:script='?'> s; </b> </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertNull(XComponentUtil.get(xc, "a"));
			xml = "<a a='1,2,3'>4,5,6</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, (xc = parseXC(xp, "", xml, null, reporter)).toXml());
			assertNoErrorwarningsAndClear(reporter);
			list = (List) XComponentUtil.get(xc, "a");
			assertTrue(list.get(0) instanceof Long);
			assertEq(1, list.get(0));
			assertEq(2, list.get(1));
			assertEq(3, list.get(2));
			list = (List) XComponentUtil.get(xc, "$value");
			assertTrue(list.get(0) instanceof Long);
			assertEq(4, list.get(0));
			assertEq(5, list.get(1));
			assertEq(6, list.get(2));
			assertNull(XComponentUtil.get(xc, "$b"));
			assertNoErrorwarningsAndClear(reporter);
			xml = "<a><b>5,6,7</b></a>";
			assertEq(xml, (xc = parseXC(xp, "", xml, null, reporter)).toXml());
			assertNoErrorwarningsAndClear(reporter);
			assertNull(XComponentUtil.get(xc, "a"));
			assertNull(XComponentUtil.get(xc, "$value"));
			list = (List) XComponentUtil.get(xc, "$b");
			assertEq(5, list.get(0));
			assertEq(6, list.get(1));
			assertEq(7, list.get(2));
			xdef = // sequence with separator
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:component>%class " + _package + ".MytestX_SQ %link #a;</xd:component>\n"+
"  <xd:declaration>\n"+
"    type s sequence(%separator=',', %item=[int, long, long]);\n"+
"  </xd:declaration>\n"+
"  <a a='? s'> ? s; <b xd:script='?'> s; </b> </a>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertNull(XComponentUtil.get(xc, "a"));
			xml = "<a a='1,2,3'>4,5,6</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, (xc = parseXC(xp, "", xml, null, reporter)).toXml());
			assertNoErrorwarningsAndClear(reporter);
			if ((o = XComponentUtil.get(xc, "a")) instanceof List) {
				assertTrue(((List) o).get(0) instanceof Long);
				assertEq(1, ((List) o).get(0));
				assertEq(2, ((List) o).get(1));
				assertEq(3, ((List) o).get(2));
			} else {
				fail("incorrect type: " + o.getClass() + "; " + o);
			}
			if ((o=XComponentUtil.get(xc, "$value")) instanceof List){
				assertTrue(((List) o).get(0) instanceof Long);
				assertEq(4, ((List) o).get(0));
				assertEq(5, ((List) o).get(1));
				assertEq(6, ((List) o).get(2));
			} else {
				fail("incorrect type: " + o.getClass() + "; " + o);
			}
			assertNull(XComponentUtil.get(xc, "$b"));
			assertNoErrorwarningsAndClear(reporter);
			xml = "<a><b>5,6,7</b></a>";
			assertEq(xml, (xc = parseXC(xp, "", xml, null, reporter)).toXml());
			assertNoErrorwarningsAndClear(reporter);
			assertNull(XComponentUtil.get(xc, "a"));
			assertNull(XComponentUtil.get(xc, "$value"));
			if ((o = XComponentUtil.get(xc, "$b")) instanceof List) {
				assertEq(5, ((List) o).get(0));
				assertEq(6, ((List) o).get(1));
				assertEq(7, ((List) o).get(2));
			} else {
				fail("incorrect type: " + o.getClass() + "; " + o);
			}
//if(true)return;
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='x'>\n"+
"  <x>\n"+
"    <a xd:script='*'> jlist(%item=jvalue()) </a>\n"+
"  </x>\n"+
"  <xd:component> %class " + _package + ".TestJList %link x; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml =
"<x>\n"+
"  <a>[]</a>\n"+
"  <a>[\"false\"]</a>\n"+
"  <a>[null]</a>\n"+
"  <a>[-9,\"\",\"\\\"\",[2,[],\"ab\\tc\"],\"-3.5\",-3.5,null,false]</a>\n"+
"</x>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			x = XComponentUtil.get(XComponentUtil.getList(xc, "a").get(0), "$value");
			assertEq(new ArrayList(), XComponentUtil.jlistToList(x));
			assertEq(new ArrayList(), x);
			x = XComponentUtil.get(XComponentUtil.getList(xc, "a").get(1), "$value");
			assertEq("false", XComponentUtil.jlistToList(x).get(0));
			assertEq("false", XComponentUtil.jlistToList(x).get(0));
			x = XComponentUtil.get(XComponentUtil.getList(xc, "a").get(2), "$value");
			assertEq(null, XComponentUtil.jlistToList(x).get(0));
			x = XComponentUtil.get(XComponentUtil.getList(xc, "a").get(3), "$value");
			assertEq(-9, XComponentUtil.jlistToList(x).get(0));
			assertEq("", XComponentUtil.jlistToList(x).get(1));
			assertEq("\"", XComponentUtil.jlistToList(x).get(2));
			ArrayList<Object> alist = new ArrayList<>();
			alist.add(2);
			alist.add(new ArrayList<>());
			alist.add("ab\tc");
			assertTrue(XonUtils.xonEqual(alist, XComponentUtil.jlistToList(x).get(3)));
			assertEq("-3.5", XComponentUtil.jlistToList(x).get(4));
//if(true)return;
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='X'>\n" +
"  <xd:json name = 'X'>\n" +
"     [\"* jvalue();\"]\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTest_jvalue %link X</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xd = xp.createXDDocument();
			s = "[2, null, \"abc\", 4.5]";
			o = XonUtils.parseXON(s);
			xd.setXONContext(s);
			x = xd.jcreate("X", reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, x));
			xd = xp.createXDDocument();
			xd.setXONContext(s);
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"   <A><B xd:script='ref B;' /></A>\n" +
"   <B b='string()'><B xd:script='?; ref B;'/></B>\n" +
"</xd:def>";
			xml = "<A><B b=\"x\"><B b=\"y\"/></B></A>";
			assertEq(xml, parse(xp = compile(xdef), "", xml, reporter));
//			xp.displayCode();
			assertNoErrorwarningsAndClear(reporter);
			(xd = xp.createXDDocument()).setXDContext(xml);
			assertEq(xml, create(xd, "A", reporter));
			assertNoErrorwarningsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
/**/
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"  <a>\n" +
"    <b/>\n" +
"    <xd:choice>\n" +
"      <c a = 'optional string();' xd:script= 'create from(\"//c\")'/>\n" +
"      <d a = 'optional string();' xd:script= 'create from(\"//d\")'/>\n" +
"    </xd:choice>\n" +
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			assertEq(create(xp, null, "a", reporter, "<a><b><c a='x'/></b></a>"),
				"<a><b/><c a='x'/></a>");
			assertNoErrorwarnings(reporter);
			assertEq(create(xp, null, reporter, "<a><b><d a='y'/></b></a>"),
				"<a><b/><d a=\"y\"/></a>");
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
//if (true) return;
		try {// test %anyName in map
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
"<xd:component> %class mytests.MyTestX_AnyX %link A#test; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());

			assertEq("", testAny(xp, "\"abc\""));
			assertEq("", testAny(xp, "[]"));
			assertEq("", testAny(xp, "[\"a\"]"));
			assertEq("", testAny(xp, "{ }"));
			assertEq("", testAny(xp, "{ \"a\":1 }"));
			assertEq("", testAny(xp, "{ \"b\":[2,3] }"));
			assertEq("", testAny(xp, "{ \"a\":1, \"b\":[2,3] }"));
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
//if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='network'>\n" +
"<xd:json name='network'>\n" +
"{\n" +
"  a: \"optional string();\"\n" +
"  b: {%script=\"optional\", a: \"optional string();\"}\n" +
"  c: [%script=\"optional\", \"int();\", { a: \"int();\"}]\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>";
			System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
				XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
			xp = compile(xdef);
			xd = xp.createXDDocument();
			json = "{ b: {a: \"x\"} }";
			o = XonUtils.parseXON(json);
			assertTrue(XonUtils.xonEqual(o, o = xd.jparse(json, reporter)),
				XonUtils.toXonString(o, true));
			assertNoErrors(reporter);
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
//if(T)return;
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, "");
		try {
			xdef =
"<xd:def xmlns:xd ='" + _xdNS + "' name='a' root='a'\n" +
"   script='options preserveEmptyAttributes,\n" +
"           preserveAttrWhiteSpaces, noTrimAttr'>\n" +
"<xd:declaration>\n" +
"  external method {\n" +
"     void bugreports.MyTest.setResult(XXNode, boolean);\n" +
"     void bugreports.MyTest.setResult(XXData, XDParser);\n" +
"     void bugreports.MyTest.setResult(XXNode, XDParseResult);\n" +
"  }\n" +
"</xd:declaration>\n" +
"  <a a=\"optional; finally {setResult(int(1,3,%enumeration=[1,3]));}\"/>\n" +
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a a='3'></a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			assertTrue(_xxx);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"<xd:declaration>\n" +
"    type y list(%length=3, %item=xdatetime('y-M-d', 'yyyyMMdd'));\n" +
"</xd:declaration>\n" +
"  <A a='? y();'></A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<A a=' 2022-5-8   2022-5-11 2020-1-2 '></A>";
			assertEq("<A a='20220508 20220511 20200102'></A>",
				parse(xd, xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) { fail(ex); }
//if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"<xd:declaration>\n" +
"  external method String bugreports.MyTest.xxx(XXNode);\n" +
"</xd:declaration>\n" +
"<xd:json name='a'>\n" +
"  [ %script=\"finally outln(xxx());\", \"gps();\", \"gps();\"]\n" +
"</xd:json>\n" +
"<xd:component>\n" +
"  %class bugreports.MyTesta %link a;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			json = "[ g(12.50, 1.2), g(2.5, 3.5, -0.1, xxx) ]";
			xd = xp.createXDDocument("");
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			jparse(xd, json, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("null\n", swr.toString());
			xd = xp.createXDDocument("");
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xd.jparseXComponent(json, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("XComponent: #a/jx:array\n", swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='x|y|y1|y2'>\n" +
"<x>\n" +
"  <a xd:script='*'>\n" +
"    jlist(%item=jvalue())\n" +
"  </a>\n" +
"  <b xd:script='*'>\n" +
"    jlist(%item=union(%item=[jnull,boolean()]))\n" +
"  </b>\n" +
"  <c xd:script='*'>\n" +
"    jlist(%item=jnumber())\n" +
"  </c>\n" +
"  <d xd:script='*'>\n" +
"    jlist(%item=int())\n" +
"  </d>\n" +
"  <e xd:script='*'>\n" +
"    <s:array xmlns:s='abc' xd:script='*'>\n" +
"      jlist(2, %item=union(%item=[jnull,int()]))\n" +
"    </s:array>\n" +
"  </e>\n" +
"</x>\n" +
"<xd:json name='y'>\n" +
"  \"int();\"\n" +
"</xd:json>\n" +
"<xd:json name='y1'>\n" +
"  {\"a\":\"int();\"}\n" +
"</xd:json>\n" +
"<xd:json name='y2'>\n" +
"  [\"occurs 2 int();\", \"optional jnumber();\", \"optional string();\"]\n" +
"</xd:json>\n" +
"<xd:component>\n" +
"  %class bugreports.MyTest_X %link x;\n" +
"  %class bugreports.MyTest_Y %link y;\n" +
"  %class bugreports.MyTest_Y1 %link y1;\n" +
"  %class bugreports.MyTest_Y2 %link y2;\n" +
"     %bind js$xxx %link #y2/jx:array/jx:item[2];\n" +
"     %bind js$yyy %link #y2/jx:array/jx:item[3];\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml =
"<x>\n" +
"  <a>[]</a>\n" + // empty array
"  <a>[\"false\"]</a>\n" +
"  <a>[123,null,false]</a>\n" +
"  <a>[123]</a>\n" +
"  <a>[3.14E+3]</a>\n" +
"  <a>[false]</a>\n" +
"  <a>[abc]</a>\n" +
"  <a>[1,\"a\\\"\\nbc\"]</a>\n" +
"  <a>[1,false,abc]</a>\n" +
"  <a>[null,123,1,false]</a>\n" +
"  <a>[null,123,false,abc]</a>\n" +
"  <a>[\"\",\"a\\nc\",[2],[-3.5,null,\"\",\"a\\nc\"],false]</a>\n" +
"  <b>[null]</b>\n" +
"  <b>[true]</b>\n" +
"  <b>[true,null]</b>\n" +
"  <b>[null,true]</b>\n" +
"  <b>[null,true,false,null]</b>\n" +
"  <e><s:array xmlns:s='abc'>[1,-2]</s:array>\n" +
"     <s:array xmlns:s='abc'>[null,99]</s:array>\n" +
"  </e>\n" +
"</x>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			xd = xp.createXDDocument("");
			s = "123";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTest_Y"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));

			xd = xp.createXDDocument("");
			s = "{\"a\": 123}";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTest_Y1"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123]";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTest_Y2"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123, -1.23e3]";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTest_Y2"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123, -1.23e3, \"abc\"]";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTest_Y2"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='z'>\n" +
"<xd:json name='z'>\n" +
"  [\"* int();\"]\n" +
"</xd:json>\n" +
"<xd:component>%class bugreports.MyTest_Z %link z;</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			s = "[1,2,3]";
			xd = xp.createXDDocument("");
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s, null, reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));

		} catch (RuntimeException ex) {fail(ex);}
//if (true) return;
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:BNFGrammar name=\"g\" scope=\"local\">\n"+
"    prvek      ::= 'a' | 'a' | 'b' | 'c'\n"+
"    prvekS     ::=  prvek ',' \n"+
"    seznam     ::= '(' prvekS* prvek ')'\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:declaration>\n"+
"     type mujtyp1 int(1,10);\n"+
"     BNFRule r = g.rule('seznam');\n"+
"     type rr r;\n"+
"     type mujtyp2 g.rule('seznam');\n"+
"  </xd:declaration>\n"+
"  <a x='mujtyp1()' y='mujtyp2()' z='r'/>\n"+
"</xd:def>";
			xp = compile(xdef);
//			xp.displayCode();
			XMData xmd;
			XDValue xdv;
			XDParser xdp;
			XDParseResult xdr;
			xmd = (XMData) xp.findModel("#a/@x");
			assertEq("int", xmd.getParserName());
			xdv = xmd.getParseMethod();
			xdp = (XDParser) xdv;
			xdr = xdp.check(null, "11");
			assertErrors(xdr.getReporter());
			xmd = (XMData) xp.findModel("#a/@y");
			assertEq("string", xmd.getParserName());
			xml = "<a x='9' y='(a,b)' z='(c)'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
//if(true)return;
		try {
			// \p{Lu} capital letters
			// \p{Ll} small letters
			// \.     dot
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
//"  <a a='string(%pattern=[\"\\\\p{Lu}(\\\\.|\\\\p{Ll}+)( \\\\p{Lu}(\\\\p{Ll}*|\\\\.))*\"]);'/>\n" +
"  <a a='string(%pattern=[\"[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ](\\\\.|[a-záčďéěíňóřšťúůýž]+)"+
	"( [A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]([a-záčďéěíňóřšťúůýž]*|\\\\.))*\"]);'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='Novák'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='P. Novák'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='Č.'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='Č. Ž.'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='F. X. Šalda'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
//if(true)return;

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}
}
