package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.model.XMData;
import org.xdef.proc.XXData;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonUtils;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
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
	final public static void setResult(XXNode xnode, boolean result) {
		_xxx = result;
	}
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
				result += "** 2\n" +  o + "\n" + x + "\n";
			}
/**
			xd = xp.createXDDocument("A");
			XComponent xc = xd.jparseXComponent(s, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 3\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 4\n" +  o + "\n" + x + "\n";
			}
/**/
		} catch (Exception ex) {
			result += ex + "\n";
		}
		return result;
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
		System.setProperty("xdef-xon_debug", "showModel");
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		Element el;
		Object j;
		String json, s, xdef, xml;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"<xd:xon name=\"test\">\n" +
"  [%oneOf=\"ref A\"]\n"  +
"</xd:xon>\n"  +
"<xd:xon name=\"A\">\n" +
" [%oneOf,\n"+
"    \"jvalue();\",\n" +
"    [\"* jvalue();\" ],\n" +
"    {%anyName:\n" +
"       [%oneOf,\n" +
"         \"jvalue();\",\n" +
"         [\"* jvalue();\" ],\n" +
"         {%anyName: [%oneOf=\" ref test\"]}\n" +
"       ]\n" +
"    }\n" +
"  ]\n" +
"</xd:xon>\n"  +
//"<xd:xon name=\"B\">\n" +
//" [%oneOf,\n"+
//"    \"jvalue();\",\n" +
//"    [\"* jvalue();\" ],\n" +
//"    {%anyName:\n" +
//"       [%oneOf,\n" +
//"         \"jvalue();\",\n" +
//"         [\"* jvalue();\" ],\n" +
//"         {%anyName: [%oneOf=\" ref A\"]}\n" +
//"       ]\n" +
//"    }\n" +
//"  ]\n" +
//"</xd:xon>\n"  +
//"<xd:choice xd:name=\"test_ANY_\" xmlns:xd=\"http://www.xdef.org/xdef/4.0\">\n"+
//"  <jx:item key=\"? string();\" val=\"jvalue();\"\n" +
//"    xmlns:jx=\"http://www.xdef.org/xon/4.0/w\"/>\n" +
//"  <jx:array key=\"? string();\" xmlns:jx=\"http://www.xdef.org/xon/4.0/w\">\n"+
//"    <xd:choice xd:script=\"*; ref test_ANY_\"/>\n" +
//"  </jx:array>\n" +
//"  <jx:map key=\"? string();\" xmlns:jx=\"http://www.xdef.org/xon/4.0/w\">\n"+
//"    <xd:choice xd:script=\"*; ref test_ANY_\"/>\n" +
//"  </jx:map>\n" +
//"</xd:choice>\n"+
"<xd:component>\n"+
"  %class mytests.MyTestX_AnyX %link A#test;\n"+
//"  %class mytests.MyTestX_AnyX_A %link A#A\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
//			xp = compile(xdef);
			genXComponent(xp, clearTempDir());

			assertEq("", testAny(xp, "\"abc\""));
//			assertEq("", testAny(xp, "[]"));
//			assertEq("", testAny(xp, "[\"a\"]"));
//			assertEq("", testAny(xp, "{ }"));
//			assertEq("", testAny(xp, "{ \"a\":1 }"));
//			assertEq("", testAny(xp, "{ \"b\":[2,3] }"));
//			assertEq("", testAny(xp, "{ \"a\":1, \"b\":[2,3] }"));
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
if(true)return;
/**/
/*xx*/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='network'>\n" +
"<xd:xon name='network'>\n" +
"{\n" +
"  a: \"optionalstring();\"\n" +
"  b: {%script=\"optional\", a: \"optional string();\"}\n" +
"  c: [%script=\"optional\", \"int();\", { a: \"int();\"}]\n" +
"}\n" +
"</xd:xon>\n" +
"</xd:def>";
			System.setProperty("xdef-xon_debug", "showModel");
			xp = XDFactory.compileXD(null, xdef);
//			xp.display();
			xd = xp.createXDDocument();
			json =
//"{ }";
//"{ a:\"fda88\" }";
"{ b: {a: \"x\"} }";
//"{ c:[1, {a:2}] }";
//"{ a:\"fda88\", b: {a: \"xyz\"}, c:[1, {a:2}] }";
			Object o = XonUtils.parseXON(json);
			assertTrue(XonUtils.xonEqual(o, o = xd.jparse(json, reporter)),
				XonUtils.toXonString(o, true));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		System.setProperty("xdef-xon_debug", "");
//if(T)return;
/*xx*/
		try {
			xdef =
"<xd:def xmlns:xd ='" + _xdNS + "' name='a' root='a'\n"+
"   script='options preserveEmptyAttributes," +
"           preserveAttrWhiteSpaces, noTrimAttr'>\n"+
"<xd:declaration>\n"+
"  external method {\n"+
"     void bugreports.MyTest.setResult(XXNode, boolean);\n"+
"     void bugreports.MyTest.setResult(XXData, XDParser);\n"+
"     void bugreports.MyTest.setResult(XXNode, XDParseResult);\n"+
"  }\n"+
"</xd:declaration>\n"+
"  <a a=\"optional; finally {setResult(int(1,3,%enumeration=[1,3]));}\"/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a a='3'></a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			assertTrue(_xxx);
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:declaration>\n"+
"    type x list(xdatetime('y-M-d'));\n" +
"    type y list(xdatetime('y-M-d', 'yyyyMMdd'));\n" +
"</xd:declaration>\n"+
"  <A a='? y();'></A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<A a=' 2022-5-8   2022-5-11 '></A>";
			assertEq("<A a='20220508 20220511'></A>", parse(xd, xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method String bugreports.MyTest.xxx(XXNode);\n"+
"</xd:declaration>\n"+
"<xd:xon name='a'>\n"+
"  [ %script=\"finally outln(xxx());\", \"gps();\", \"gps();\"]\n"+
"</xd:xon>\n"+
"<xd:component>\n"+
"  %class bugreports.MyTesta %link a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			json = "[ g(12.50, 1.2), g(2.5, 3.5, -0.1, xxx) ]";
			xd = xp.createXDDocument("");
			jparse(xd, json, reporter);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xc = xd.jparseXComponent(json, null, reporter);
		} catch (Exception ex) {fail(ex);}
//if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='x|y|y1|y2'>\n"+
"<xd:xon name='y'>\n"+
"  \"int();\"\n"+
"</xd:xon>\n"+
"<xd:xon name='y1'>\n"+
"  {\"a\":\"int();\"}\n"+
"</xd:xon>\n"+
"<xd:xon name='y2'>\n"+
"  [\"occurs 2 int();\", \"optional jnumber();\", \"optional string();\"]\n"+
"</xd:xon>\n"+
"<x>\n"+
"  <a xd:script='*'>\n"+
//"    jlist(%item=union(%item=[jnull,boolean(), int, string]))\n"+
"    jlist(%item=jvalue())\n"+
"  </a>\n"+
"  <b xd:script='*'>\n"+
"    jlist(%item=union(%item=[jnull,boolean()]))\n"+
"  </b>\n"+
"  <c xd:script='*'>\n"+
"    jlist(%item=jnumber())\n"+
"  </c>\n"+
"  <d xd:script='*'>\n"+
"    jlist(%item=int())\n"+
"  </d>\n"+
"  <e xd:script='*'>\n"+
"    <s:array xmlns:s='abc' xd:script='*'>\n"+
"		jlist(2, %item=union(%item=[jnull,int()]))\n"+
"    </s:array>\n"+
"  </e>\n"+
"</x>\n"+
"<xd:component>\n"+
"  %class bugreports.MyTestX %link x;\n"+
"  %class bugreports.MyTestY %link y;\n"+
"  %class bugreports.MyTestY1 %link y1;\n"+
"  %class bugreports.MyTestY2 %link y2;\n"+
"     %bind js$xxx %link #y2/jx:array/jx:item[2];\n"+
"     %bind js$yyy %link #y2/jx:array/jx:item[3];\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			genXComponent(xp, clearTempDir());
			xml =
"<x xmlns:s='abc'>\n"+
"  <a>[ \"false\" ]</a>\n"+
"  <a>[ 123, null, false ]</a>\n"+
"  <a>[ 123 ]</a>\n"+
"  <a>[ 3.14E+3 ]</a>\n"+
"  <a>[ false ]</a>\n"+
"  <a>[ \"abc\" ]</a>\n"+
"  <a>[ 1, \"a\\\"\\nbc\" ]</a>\n"+
"  <a>[ 1, false, \"abc\" ]</a>\n"+
"  <a>[ null, 123, 1, false ]</a>\n"+
"  <a>[ null, 123, false, \"abc\" ]</a>\n"+
//"  <a>[ null, 123, false, \"abc\", \"\" ]</a>\n"+
//"  <a>[ ]</a>\n"+ // empty array
"  <b>[ null ]</b>\n"+
"  <b>[ true ]</b>\n"+
"  <b>[ true, null ]</b>\n"+
"  <b>[ null, true ]</b>\n"+
"  <b>[ null, true, false, null ]</b>\n"+
"  <e><s:array>[ 1, -2 ]</s:array>\n"+
"     <s:array>[ null, 99 ]</s:array>\n"+
"  </e>\n"+
"</x>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());

			xd = xp.createXDDocument("");
			s = "123";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));

			xd = xp.createXDDocument("");
			s = "{\"a\": 123}";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY1"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));

			xd = xp.createXDDocument("");
			s = "[123, 123]";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY2"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123, -1.23e3]";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
				xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY2"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123, -1.23e3, \"abc\"]";
			j = XonUtils.parseJSON(s);
			assertTrue(XonUtils.xonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY2"), reporter);
			assertTrue(XonUtils.xonEqual(j, toJson(xc)),
				XonUtils.toJsonString(toJson(xc), true));
		} catch (Exception ex) {fail(ex);}
//if(true)return;
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:BNFGrammar name=\"g\" scope=\"local\">\n" +
"   prvek      ::= 'a' | 'a' | 'b' | 'c'\n" +
"   prvekS     ::=  prvek ',' \n" +
"   seznam     ::= '(' prvekS* prvek ')'\n" +
" </xd:BNFGrammar>\n"+
"<xd:declaration>\n"+
" type mujtyp1 int(1,10);\n"+
" BNFRule r = g.rule('seznam');\n"+
" type rr r;\n"+
" type mujtyp2 g.rule('seznam');\n"+
"</xd:declaration>\n"+
"<a x='mujtyp1()' y='mujtyp2()' z='r'/>\n"+
"</xd:def>";
			xp = compile(xdef);
//			xp.displayCode();
			xd = xp.createXDDocument();
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
			xdv = xmd.getParseMethod();
			xml = "<a x='9' y='(a,b)' z='(c)'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(true)return;
		try {
			// \p{Lu} capital letters
			// \p{Ll} small letters
			// \.     dot
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='string(%pattern=[\"\\\\p{Lu}(\\\\.|\\\\p{Ll}+)( \\\\p{Lu}(\\\\p{Ll}*|\\\\.))*\"]);'/>\n"+
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
		if (runTest(args) > 0) {System.exit(1);}
	}
}