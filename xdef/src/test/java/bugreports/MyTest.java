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
import org.xdef.json.JsonUtil;
import org.xdef.model.XMData;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

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

	private static String testj(String xml, String json) {
		Object j = JsonUtil.parse(json);
		Element el = JsonUtil.jsonToXmlXD(j);
		if (KXmlUtils.compareElements(xml, el, true, null).errorWarnings()) {
			return "xml != el"+
				"\njson: "+json+
				"\nxml:  "+xml+
				"\nel:   "+KXmlUtils.nodeToString(el);
		}
		Object j1 = JsonUtil.xmlToJson(el);
		if (!JsonUtil.jsonEqual(j, j1)) {
			return "j != j1"+
				"\nxml: "+xml+
				"\nj:   " + JsonUtil.toJsonString(j)+
				"\nj1:  " + JsonUtil.toJsonString(j1);
		}
		return "";
	}
	private static Object toJson(final XComponent xc) {
		return JsonUtil.xmlToJson(xc.toXml());
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

		Element el;
		Object j;
		String json, s, xdef, xml;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  email(); onTrue {\n"+
"              Email e = (Email) getParsedValue();\n"+
"              outln(getEmailUserName(e));\n"+
"              outln(getEmailLocalPart(e));\n"+
"              outln(getEmailDomain(e));\n"+
"              outln(getEmailAddr(e));\n"+
"            }\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>(T. tr) a@b</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:json name='a'>\n"+
"  [\"gps();\", \"gps();\"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.MyTesta %link a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			genXComponent(xp, clearTempDir());
			json = "[ g(12.50, 1.2), g(2.5, 3.5, -0.1, xxx) ]";
			xd = xp.createXDDocument("");
			jparse(xd, json, reporter);
			assertNoErrors(reporter);
			System.out.println(JsonUtil.toXonString(xd.getXon(), true));
			xd = xp.createXDDocument("");
			xc = xd.jparseXComponent(json, null, reporter);
			System.out.println(KXmlUtils.nodeToString(xc.toXml(), true));
		} catch (Exception ex) {fail(ex);}
if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='x|y|y1|y2'>\n"+
"<xd:json name='y'>\n"+
"  \"int();\"\n"+
"</xd:json>\n"+
"<xd:json name='y1'>\n"+
"  {\"a\":\"int();\"}\n"+
"</xd:json>\n"+
"<xd:json name='y2'>\n"+
"  [\"occurs 2 int();\", \"optional jnumber();\", \"optional string();\"]\n"+
"</xd:json>\n"+
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
"     %bind js$xxx %link #y2/js:array/js:item[2];\n"+
"     %bind js$yyy %link #y2/js:array/js:item[3];\n"+
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
			assertNoErrors(reporter);
			xc = parseXC(xp, "", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());

			xd = xp.createXDDocument("");
			s = "123";
			j = JsonUtil.parse(s);
			assertTrue(JsonUtil.jsonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY"), reporter);
			assertTrue(JsonUtil.jsonEqual(j, toJson(xc)),
				JsonUtil.toJsonString(toJson(xc), true));

			xd = xp.createXDDocument("");
			s = "{\"a\": 123}";
			j = JsonUtil.parse(s);
			assertTrue(JsonUtil.jsonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY1"), reporter);
			assertTrue(JsonUtil.jsonEqual(j, toJson(xc)),
				JsonUtil.toJsonString(toJson(xc), true));

			xd = xp.createXDDocument("");
			s = "[123, 123]";
			j = JsonUtil.parse(s);
			assertTrue(JsonUtil.jsonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY2"), reporter);
			assertTrue(JsonUtil.jsonEqual(j, toJson(xc)),
				JsonUtil.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123, -1.23e3]";
			j = JsonUtil.parse(s);
			assertTrue(JsonUtil.jsonEqual(j, xd.jparse(s, reporter)));
				xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY2"), reporter);
			assertTrue(JsonUtil.jsonEqual(j, toJson(xc)),
				JsonUtil.toJsonString(toJson(xc), true));
			xd = xp.createXDDocument("");
			s = "[123, 123, -1.23e3, \"abc\"]";
			j = JsonUtil.parse(s);
			assertTrue(JsonUtil.jsonEqual(j, xd.jparse(s, reporter)));
			xc = xd.jparseXComponent(s,
				Class.forName("bugreports.MyTestY2"), reporter);
			assertTrue(JsonUtil.jsonEqual(j, toJson(xc)),
				JsonUtil.toJsonString(toJson(xc), true));
		} catch (Exception ex) {fail(ex);}
if(true)return;
		try {
			assertEq("", testj(
//"<a>[ 1, 2 ][ 3, 4 ]</a>",
"<a xmlns:js='http://www.xdef.org/json/4.0'>"+
"<js:array>[ 1, 2 ]</js:array>"+
"<js:array>[ 3, 4 ]</js:array>"+
"</a>",
				"{\"a\":[{}, [1,2], [3,4]]}"));
if(true)return;
			xml = "<a ax='1'><b bx='2'>xxx</b></a>";
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			j = JsonUtil.xmlToJson(el);
//			assertEq(xml, JsonUtil.jsonToXmlXD(j));
//			System.out.println(JsonUtil.toJsonString(j));
//			assertEq(el, JsonUtil.jsonToXmlXD(j));
			assertEq("", testj("<a/>", "{\"a\": {} }"));
			assertEq("", testj("<a>aaa</a>", "{\"a\":[{},\"aaa\"]}"));
//			assertEq("", testj("<a>aaa</a>", "{\"a\": \"aaa\" }"));
			assertEq("", testj("<a b='1' c='2'/>",
				"{\"a\": {\"b\": 1, \"c\": 2} }"));
			assertEq("", testj("<a><b/>aaa<c/></a>",
				"{\"a\": [ {},  {\"b\": {} }, \"aaa\", {\"c\": {} } ] }"));
			assertEq("", testj("<a ax='1'><b bx='2'>xxx</b></a>",
				"{\"a\":[{\"ax\":1},{\"b\":[{\"bx\":2},\"xxx\"]}]}"));
			assertEq("", testj("<a>[ 1, 2 ]</a>", "{\"a\":[{},[1,2]]}"));
			assertEq("", testj(
//"<a>[ 1, 2 ][ 3, 4 ]</a>",
"<a xmlns:js='http://www.xdef.org/json/4.0'>"+
"<js:array>[ 1, 2 ]</js:array>"+
"<js:array>[ 3, 4 ]</js:array>"+
"</a>",
				"{\"a\":[{}, [1,2], [3,4]]}"));
		} catch (Exception ex) {fail(ex);}
if(true)return;
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
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
//if(true)return;
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
			assertNoErrors(reporter);
			xml = "<a a='P. Novák'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='Č.'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='Č. Ž.'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a a='F. X. Šalda'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method XDParseResult bugreports.MyTest_0.kp(XXNode, XDValue[]);\n"+
"</xd:declaration>\n"+
"<a a='kp(1,5,%totalDigits=1,%enumeration=[1,3],%pattern=[\"\\\\d\"])'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='3'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}

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