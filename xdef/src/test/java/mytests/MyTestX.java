package mytests;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import javax.xml.XMLConstants;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.impl.XConstants;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefString;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;
import test.xdef.TestXComponents_Y21enum;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTestX extends XDTester {
	public MyTestX() {
		super();
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
	}

	public static boolean chk1(String s)
	{System.out.println("i1=" + s); return true;}
	public static boolean chk2(int i, XDContainer c)
	{System.out.println("i2="+i + ", " + c);return true;}
	public static void chk3() {System.out.println("x3");}
	public static boolean chk4(XXNode x,int i)
	{System.out.println("i4="+i + ", " + x);return true;}
	public static boolean chk5(XXNode x,int i)
	{System.out.println("i5="+i + ", " + x);return true;}
	public static void chk6(XXNode x, XDValue[] y) {
		System.out.print("x6 " + x.getXPos() + ",");
		for(XDValue z: y) System.out.print(" " + z);
		System.out.println();
	}

	@SuppressWarnings({"unchecked", "unchecked"})
	@Override
	/** Run test and display error information. */
	public void test() {
/**/
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, "");
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
////////////////////////////////////////////////////////////////////////////////
		Element el;
		File file;
		String json, s, xml, xon ;
		Object o,x,y;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr;
		XComponent xc;
		XDDocument xd;
		List list;
		String xdef;
		XDPool xp;
/**
		s = "_x61_hoj"; //ahoj
		System.out.print(s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x69_tem";  //item
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x6b_ey";  //key
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x76_alue";  //value
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x78_ml"; //xml
		System.out.print("; " + s + " ");
		System.out.println(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x6d_ap"; //map
		System.out.print("; " + s + " ");
		System.out.println(org.xdef.xon.XonTools.xmlToJName(s));
//if(true)return;
/**
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:component>%class mytests.MytestX_Hex0 %link #A;</xd:component>\n" +
"<xd:json name='A'>\n" +
"[\n" +
"  \"base64Binary()\",\n" +
"  \"hexBinary()\",\n" +
"  \"? hexBinary()\"\n" +
"]\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			genXComponent(xp, clearTempDir());
			xon = "[b(true), x(0FAE99), x()]";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toJsonString(x, true);
			XonUtils.parseXON(s);
			y = XonUtils.parseXON(XonUtils.toXonString(x, true));
			assertTrue(XonUtils.xonEqual(x,y));
			json = XonUtils.toXonString(x, true);
			y = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xc.toXon(),y));
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
		} catch (Exception ex) {fail(ex);}
if(true) return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='a'>\n"+
"  <a a=\"ydatetime('yyyy-MM-ddTHH:mm:ss[ZZ]', 'yyyy-MM-ddTHH:mm:ssZ');\"/>\n" +
"</xd:def>";
			Properties props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props, xdef);
			xml = "<a a='2024-10-22T11:55:30Europe/Prague'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T11:55:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30-03:30'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T17:25:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30Etc/GMT-14'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-21T23:55:30+02:00\"/>", xd.xparse(xml, null));
		} catch (RuntimeException ex) {fail(ex);}
//if(true) return;
if(T) return;
/**/
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"root\" >\n" +
"<xd:declaration>\n" +
"   type x boolean() CHECK false.equals(getParsedValue());\n" +
"   type y int AAND '123' == getText();\n" +
"</xd:declaration>\n" +
"  <root a=\"x();\" b=\"y;\" />\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
//			xp = compile(xdef);
			xml ="<root a='false' b='123' />";
			xp.display();
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			XDValue[] stack = new XDValue[] {
				new DefParseResult("???"),
				new DefString("2"),
			};
			System.out.println(((XDParseResult) stack[0]).getSourceBuffer() + ", " + stack[1]);
			int sp = 1;
//			s = stack[sp--].toString();
			((XDParseResult) stack[--sp]).setSourceBuffer(stack[sp+1].toString());
			System.out.println(((XDParseResult) stack[0]).getSourceBuffer()+ ", " + stack[1]);
		} catch (RuntimeException ex) {fail(ex);}
if(T) return;
/**/
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='X' root='A' >\n" +
"  <xd:declaration>\n"+
"  final BNFGrammar rr = new BNFGrammar('\n"+
"    WS     ::= [#9#10#13 ]*\n"+ // skip white spaces
"    SEP    ::= WS \",\" WS\n"+ // separator of values
"    LnPrd  ::= [1-9] | [1-4][0-9]\n"+
"    Month  ::= [1-9] | [1][0-2]\n"+
"    Months ::= Month ( SEP Month )*\n"+
"    YPrd   ::= LnPrd? \"Y\" \"(\" Months \")\"\n"+
"    MDay   ::= [1-9] | [1-2][0-9] | [3][0-1] | \"-1\"\n"+
"    MDays  ::= MDay (SEP MDay)*\n"+
"    MPrd   ::= LnPrd? \"M\" \"(\" MDays \")\"\n"+
"    WDay   ::= [0-7] | \"-1\"\n"+
"    WDays  ::= WDay (SEP WDay)*\n"+
"    WPrd   ::= LnPrd? \"W\" \"(\" WDays \")\"\n"+
"    TimeH  ::= [0-1][0-9] | [2][0-3]\n"+
"    TimeM  ::= [0-5] [0-9]\n"+
"    Time   ::= TimeH \":\" TimeM\n"+
"    Times  ::= Time (SEP Time)*\n"+
"    DPrd   ::= LnPrd? \"D\" \"(\" Times \")\"\n"+
"    HPrd   ::= LnPrd \"H\"\n"+
"    MinPrd ::= LnPrd \"Min\"\n"+
"    reccur ::= MinPrd? HPrd? DPrd? WPrd? MPrd? YPrd?'\n"+
");\n"+
"  </xd:declaration>\n"+
"  <A><xd:text>required BNF(rr, 'reccur');</xd:text></A>\n"+
"</xd:def>";
			xp = compile(xdef);
//			xp.displayCode();
			org.xdef.model.XMDefinition xmd = xp.getXMDefinition("X");
			org.xdef.model.XMElement xme = xmd.getModel(null, "A");
			for (org.xdef.model.XMNode xmn : xme.getChildNodeModels()) {
				if (xmn instanceof org.xdef.model.XMData) {
					System.out.println(
						((org.xdef.model.XMData) xmn).getParseMethod());
				}
			}
			xml ="<A>D(10:00, 11:55)W(1, 2, 3, 4, 5, 6)</A>";
			assertEq(xml, parse(xp, "X", xml, reporter));
		} catch (RuntimeException ex) {fail(ex);}
if(T) return;
		try {
			reporter.clear();
			xdef =
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.2'>\n" +
"<xd:def name='A' root='A|B#B'>\n" +
"<A xd:script='option setAttrUpperCase' a='string()'>\n" +
"  <B xd:script='*; ref B'/>\n" +
"</A>\n" +
"<B xd:script='option preserveAttrCase' b='? string();'/>\n" +
"</xd:def>\n" +
"<xd:def name='B'>\n" +
"<B a='string(); option setAttrUpperCase'>\n" +
"  <C xd:script='option preserveAttrCase; *; ref C'/>\n" +
"</B>\n" +
"<C b='? string();'/>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			assertEq("<A a='A'><B b='b'/></A>", parse(xp, "A", "<A a='a'><B b='b'/></A>", reporter));
			assertEq("<B a='A'><C b='b'/></B>", parse(xp, "A", "<B a='a'><C b='b'/></B>", reporter));
		} catch (Exception ex) {fail(ex);}
if(T) return;
		try {
			reporter.clear();
			xdef =
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.2'>\n" +
"<xd:def name='A' root='A|B#B'>\n" +
"<A xd:script='options setAttrUpperCase' a='string()'>\n" +
"  <B xd:script='*; ref B'/>\n" +
"</A>\n" +
"<B xd:script='options preserveAttrCase' b='? string();'/>\n" +
"</xd:def>\n" +
"<xd:def name='B'>\n" +
"<B xd:script='options setAttrUpperCase' a='string()'>\n" +
"  <C xd:script='options preserveAttrCase; *; ref C'/>\n" +
"</B>\n" +
"<C b='? string();'/>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = XDFactory.compileXD(null, xdef);
			assertEq("<A a='A'><B b='b'/></A>", parse(xp, "A", "<A a='a'><B b='b'/></A>", reporter));
			assertEq("<B a='A'><C b='b'/></B>", parse(xp, "A", "<B a='a'><C b='b'/></B>", reporter));
		} catch (RuntimeException ex) {fail(ex);}
if(T) return;
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Example\" root=\"root\" >\n" +
"  <root>\n" +
"    <b value=\"optional regex('[A-Z]{3}[0-9]{6}');\"/>\n" +
"  </root>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<root>\n" +
"  <b value=\"ABC123456\" />\n" +
"</root>";
			assertEq(xml, parse(xp,"",xml,reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Example\" root=\"root\" >\n" +
"  <xd:declaration>\n" +
"    ParseResult pole() {\n" +
"      ParseResult p = string();\n" +
"      if (!'12'.equals(p.getValue())) {p.error(); } \n" +
"      return p; \n" +
"    }\n" +
"  </xd:declaration>\n" +
"  <root a=\"pole(); onTrue outln('root a: ');\" >\n" +
"    <b xd:script=\"occurs *\" >\n" +
"      optional string(); finally outln(\"b: \");\n" +
"    </b>\n" +
"  </root>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<root a=\"12\" ><b>Lorem ipsum dolor amet.</b><b/></root>";
			parse(xp, "Example", xml, reporter);
			assertNoErrorsAndClear(reporter);
			xml = "<root a=\"123\" ><b>Lorem ipsum dolor amet.</b><b/></root>";
			parse(xp, "Example", xml, reporter);
			assertErrorsAndClear(reporter);
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef =
"<xd:collection xmlns:xd=\"http://www.syntea.cz/xdef/3.1\">\n" +
"    <xd:def xd:name  =\"multiXdefTest2\"\n" +
"            xd:root  =\"FirmWorkers\"\n" +
"            xmlns:ext=\"A.A\" >\n" +
"        <FirmWorkers>\n" +
"            <Worker xd:script=\"ref ext2_1#ext:Person; occurs +\"/>\n" +
"        </FirmWorkers>\n" +
"    </xd:def>\n" +
"    <xd:def xd:name  =\"ext2_1\"\n" +
"            xd:root  =\"hr:Person\"\n" +
"            xmlns:hr =\"A.A\"\n" +
"            xmlns:ext=\"b.b\" >\n" +
"         <xd:declaration>\n" +
"           type xxx xdatetime('dd.MM.yyyy', 'yyyy-MM-yy'); \n" +
"         </xd:declaration>\n" +
"        <hr:Person>\n" +
"            <hr:Name>required string(1,30)</hr:Name>\n" +
"            <hr:Surname>required string(1,50)</hr:Surname>\n" +
"            <hr:DateOfBirth>required xxx()</hr:DateOfBirth>\n" +
"            <ext:Position xd:script=\"ref ext2_2#ext:Position; occurs +\" />\n" +
"            <ext:Salary xd:script=\"ref ext2_2#ext:Salary; occurs 1\" />\n" +
"        </hr:Person>\n" +
"    </xd:def>\n" +
"    <xd:def xd:name =\"ext2_2\"\n" +
"            xd:root =\"fh:Position | fh:Salary\"\n" +
"            xmlns:fh=\"b.b\" >\n" +
"        <fh:Position fh:place=\"optional string\">\n" +
"            <fh:Name>required string</fh:Name>\n" +
"        </fh:Position>\n" +
"        <fh:Salary fh:currency=\"string\">required int</fh:Salary>\n" +
"    </xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			xml =
"<FirmWorkers xmlns:xsi=\""+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"\"\n" +
"    xmlns:hr=\"A.A\"\n" +
"    xmlns:fh=\"b.b\" >\n" +
"    <Worker>\n" +
"        <hr:Name>John</hr:Name>\n" +
"        <hr:Surname>Doe</hr:Surname>\n" +
"        <hr:DateOfBirth>05.05.1972</hr:DateOfBirth>\n" +
"        <fh:Position fh:place=\"external\">\n" +
"            <fh:Name>Programmer</fh:Name>\n" +
"        </fh:Position>\n" +
"        <fh:Position>\n" +
"            <fh:Name>Analyst</fh:Name>\n" +
"        </fh:Position>\n" +
"        <fh:Salary fh:currency=\"EUR\">35000</fh:Salary>\n" +
"    </Worker>\n" +
"    <Worker>\n" +
"        <hr:Name>Joe</hr:Name>\n" +
"        <hr:Surname>Smith</hr:Surname>\n" +
"        <hr:DateOfBirth>12.01.1969</hr:DateOfBirth>\n" +
"        <fh:Position>\n" +
"            <fh:Name>CEO</fh:Name>\n" +
"        </fh:Position>\n" +
"        <fh:Salary fh:currency=\"EUR\">5000</fh:Salary>\n" +
"    </Worker>\n" +
"</FirmWorkers>";
			parse(xp, "multiXdefTest2", xml, reporter);
			assertNoErrorsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef =
"<xd:collection xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"  <xd:def xd:name=\"t018\"\n" +
"    xmlns:tns=\"http://b\"\n" +
"    xd:root=\"tns:elem\"\n" +
"    xmlns:a=\"http://a\">\n" +
"    <xd:declaration>\n" +
"      type attr2_Type1 long(0, 20);\n" +
"    </xd:declaration>\n" +
"    <xd:declaration scope=\"global\">\n" +
"      type attr2_Type long(2, 10);\n" +
"    </xd:declaration>\n" +
"    <tns:elem>\n" +
"      <xd:sequence xd:script=\"occurs 1\">\n" +
"        <a:a1 xd:script=\"occurs 1; ref t018_1#a:a1\"/>\n" +
"        <a:a2 xd:script=\"occurs 1; ref t018_1#a:a2\"/>\n" +
"        <a3 xd:script=\"occurs 1\"\n" +
"          attr1=\"optional string()\"\n" +
"          tns:attr2=\"optional attr2_Type1()\"/>\n" +
"      </xd:sequence>\n" +
"    </tns:elem>\n" +
"  </xd:def>\n" +
"  <xd:def xd:name=\"t018_1\"\n" +
"    xmlns:tns=\"http://a\"\n" +
"    xd:root=\"tns:a1 | tns:a2\"\n" +
"    xmlns:a=\"http://b\">\n" +
"    <tns:a1 attr1=\"optional string()\"\n" +
"      a:attr2=\"optional attr2_Type()\"/>\n" +
"    <tns:a2 tns:attr1=\"optional string()\"\n" +
"      a:attr2=\"optional attr2_Type()\"/>\n" +
"  </xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			xml =
"<pb:elem xsi:schemaLocation = \"http://b t018.xsd\"\n" +
"	 xmlns:xsi =\""+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"\"\n" +
"	 xmlns:pb  = \"http://b\">\n" +
"    <pa:a1 xmlns:pa  = \"http://a\"\n" +
"	   attr1 =\"aaa\"\n" +
"	   pb:attr2 =\"3\"/>\n" +
"    <pa:a2 xmlns:pa  = \"http://a\"\n" +
"	   pb:attr2 =\"5\"/>\n" +
"    <a3 attr1 =\"aaab\"\n" +
"	 pb:attr2 =\"15\"/>\n" +
"</pb:elem>";
			parse(xp, "t018", xml, reporter);
			assertNoErrorsAndClear(reporter); //XDEF822
		} catch (Exception ex) {fail(ex);}
//if(true)return;
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Example\" root=\"root\" >\n" +
"  <xd:declaration>\n" +
"    type pole string(1,3) CHECK regex('[0-9]{3}');\n" +
//"    type pole string(1,2) CHECK regex('[0-9]{3}').parse().matches();\n" +
//"    type pole string(1,2) CHECK chk();\n" +
//"    type r regex('[0-9]{3}');\n" +
//"    boolean chk() {\n"+
//"      return regex('[0-9]{3}').parse().matches();\n" +
//"   }\n" +
"  </xd:declaration>\n" +
"  <root a=\"pole(); onTrue outln('root a: ' + getText());\" >\n" +
"    <b xd:script=\"occurs *\" >\n" +
"      optional string();\n" +
"    </b>\n" +
"  </root>\n" +
"</xd:def>";
			xp = compile(xdef);
			xp.displayCode();
			xml = "<root a=\"12\" ><b>Lorem ipsum dolor amet.</b><b/></root>";
			parse(xp, "Example", xml, reporter);
			assertErrorsAndClear(reporter);
			xml = "<root a=\"123\" ><b>Lorem ipsum dolor amet.</b><b/></root>";
			parse(xp, "Example", xml, reporter);
			assertNoErrorsAndClear(reporter);
			xml = "<root a=\"1234\" ><b>Lorem ipsum dolor amet.</b><b/></root>";
			parse(xp, "Example", xml, reporter);
			assertErrorsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:component>%class mytests.MytestX_Str %link #A;</xd:component>\n" +
"<xd:declaration>boolean x(){outln(getText());return true;}</xd:declaration>\n"+
"<xd:json name='A'>\n" +
//"{\"\": \"jstring()\", \"a\": \"jstring()\", \"b\":\"jstring()\"}\n"+
"[ \"eq('2021')\", \"num()\", \"gYear()\", \"jstring()\", \"jstring()\" ]\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			file = clearTempDir();
			assertNoErrors(genXComponent(xp, file));
//			copyToSourceDir(file, "mytests", "MytestX_Str");
//			xon = "{\"\":\"nul\", \"a\": \"a1\", \"b\": \"a-1\"}";
//			xon = "{\"\":\"null\", \"a\": \"1\", \"b\": \"-1\"}";
			xon = "[\"2021\", \"2021\", \"2021\", \"null\", \"-1\"]";
//			xon = "[\"-1\"]";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toXonString(x, true);
System.out.println(s);
			XonUtils.parseXON(s);
			y = XonUtils.parseXON(XonUtils.toXonString(x, true));
			assertTrue(XonUtils.xonEqual(x,y));
			json = XonUtils.toXonString(x, true);
System.out.println(json);
			y = jparse(xp, "", json, reporter);
System.out.println(XonUtils.toXonString(y, true));
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
if (!XonUtils.xonEqual(xc.toXon(),y)) {
	System.out.println("errorB\n" + ((List)xc.toXon()).get(0).getClass());
	System.out.println("errorB\n" + XonUtils.toXonString(xc.toXon(), true));
}
			xd = xp.createXDDocument();
			xd.jparseXComponent(xc.toXon(), null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
if (!XonUtils.xonEqual(xc.toXon(),y)) {
	System.out.println("errorC\n" + XonUtils.toXonString(xc.toXon(), true));
}
			el = XonUtils.xonToXml(xc.toXon());
			s = XonUtils.toJsonString(XonUtils.xmlToXon(el), true);
System.out.println(s);
			o = XonUtils.xonToJson(jparse(xp, "", s, reporter));
if (!XonUtils.xonEqual(o,y)) {
	System.out.println("errorD\n" + KXmlUtils.nodeToString(el, true));
	System.out.println("errorD\n" + s);
	System.out.println("errorD\n" + XonUtils.toXonString(o, true));
}
		} catch (RuntimeException ex) {fail(ex);}
if(T) return;
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = \"A\">\n" +
"  <xd:component>%class mytests.MytestX_Hex %link #A;</xd:component>\n" +
"<xd:json name=\"A\">\n" +
"  [ \"base64Binary()\", \"hexBinary()\" ]\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			// generate and compile XComponents from xp
			file = clearTempDir();
			assertNoErrors(genXComponent(xp, file));
//			copyToSourceDir(file, "mytests", "MytestX_Hex");
			xon = "[ b(FF00), x(FF00) ]";
			x = XonUtils.parseXON(xon);
			list = (List) x;
			assertEq(2, list.size());
System.out.println(list.get(0).getClass() + "," + list.get(1).getClass());
System.out.println(XonUtils.toXonString(x, true));
			xd = xp.createXDDocument();
			xd.jparse(xon, reporter);
			assertNoErrorsAndClear(reporter);
			y = xd.getXon();
list = (List) y;
System.out.println(list.get(0).getClass() + "," + list.get(1).getClass());
System.out.println(XonUtils.toXonString(y, true));
			assertTrue(XonUtils.xonEqual(x,y));
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent(xon, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
list = (List) y;
System.out.println(list.get(0).getClass() + "," + list.get(1).getClass());
System.out.println(XonUtils.toXonString(y, true));
			assertTrue(XonUtils.xonEqual(x,y));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name = 'O' root = \"A\">\n" +
"  <xd:component>%class mytests.MytestX_O %link O#A;</xd:component>\n" +
"  <A>\n" +
"    <xd:choice xd:script='+'>\n" +
"      <B/>\n" +
"      <C/>\n" +
"      int\n" +
"    </xd:choice>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			// generate and compile XComponents from xp
			file = clearTempDir();
			assertNoErrors(genXComponent(xp, file));
//			copyToSourceDir(file, "mytests", "MytestX_O");
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			xc = parseXC(xp, "O", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			xc = parseXC(xp, "O", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // sequence witn separatoritem (compatible item types)
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:component>%class mytests.MytestX_SQ %link #a;</xd:component>\n" +
"<xd:declaration>\n"+
"  type s sequence(%separator=',', %item=[int,long]);\n"+
"</xd:declaration>\n"+
" <a a='?s'>\n"+
"  ? s;\n"+
"  <b xd:script='?'>s;</b>\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MytestX_SQ");
			xml = "<a a=' 1,2 '>   3,4    </a>";
			assertEq("<a a='1,2'>3,4</a>", parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if ((o = SUtils.getValueFromGetter(xc, "geta")) instanceof List) {
				x = ((List) o).get(0);
				assertTrue(x instanceof Long);
				assertEq(1, x);
				x = ((List) o).get(1);
				assertTrue(x instanceof Long);
				assertEq(2, x);
			} else {
				fail("incorrect type");
			}
			if ((o=SUtils.getValueFromGetter(xc,"get$value")) instanceof List) {
				x = ((List) o).get(0);
				assertTrue(x instanceof Long);
				assertEq(3, x);
				x = ((List) o).get(1);
				assertTrue(x instanceof Long);
				assertEq(4, x);
			} else {
				fail("incorrect type");
			}
			assertNull(SUtils.getValueFromGetter(xc, "get$b"));
			assertEq(xml, xc.toXml());
			xml = "<a><b>5,6</b></a>";
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertNull(SUtils.getValueFromGetter(xc, "geta"));
			assertNull(SUtils.getValueFromGetter(xc, "get$value"));
			if ((o = SUtils.getValueFromGetter(xc, "get$b")) instanceof List) {
				x = ((List) o).get(0);
				assertTrue(x instanceof Long);
				assertEq(5, x);
				x = ((List) o).get(1);
				assertTrue(x instanceof Long);
				assertEq(6, x);
			} else {
				fail("incorrect type");
			}
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			TestXComponents_Y21enum.class.getClass(); // force to compile
			xdef = // test enum and ref
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='A'>\n" +
"  <xd:declaration scope=\"local\" >\n" +
"    type Test_int int(1, 10);\n" +
"  </xd:declaration>\n" +
"  <xd:component>%class mytests.MytestX_A %link #A;</xd:component>\n" +
"  <A d='? list(%item=Test_int);'>\n" +
"      ? list(%item=int(), %length=3)\n" +
"  </A>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MytestX_A");
			xml = "<A>1\n2\n3</A>";
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			el = xc.toXml();
			assertEq("<A>1 2 3</A>", el);
			xc = parseXC(xp,"", el , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
			xml = "<A d='1 2'/>";
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			el = xc.toXml();
			assertEq(xml, el);
			xc = parseXC(xp,"", el , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			TestXComponents_Y21enum.class.getClass(); // force to compile
			xdef = // test enum and ref
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='A' name='Y21'>\n" +
"  <xd:declaration scope=\"local\" >\n" +
"    type eType enum('x', 'y', 'A1_b', 'z', '_1', 'A1_b2', '$');\n" +
"    type myType eType;\n" +
"    type eType1 enum('a', 'b', 'c');\n" +
"    type extType eType1;\n" +
"    type Test_int int(1, 10);\n" +
"  </xd:declaration>\n" +
"  <xd:component>\n" +
"    %class mytests.Y21 %link Y21#A;\n" +
"    %enum mytests.Y21_enum eType;\n" +
"    %ref %enum test.xdef.TestXComponents_Y21enum eType1;\n" +
"  </xd:component>\n" +
"  <A b='myType;' >\n" +
"    ? myType;\n" +
"    <B xd:script='*' c='eType1;' d='? list(%item=Test_int);'>\n" +
"      myType;\n" +
"    </B>\n" +
"    ? myType;\n" +
"  </A>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml = "<A b='x'>z<B c='a'>x</B><B c='c' d='1 2'>y</B>x</A>";
			xc = parseXC(xp,"Y21", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			SUtils.setValueToSetter(xc, "set$value", null);
			o = SUtils.getObjectField("mytests.Y21_enum", "y");
			SUtils.setValueToSetter(xc, "setb", o);
			list = (List) SUtils.getValueFromGetter(xc, "listOfB");
			o = SUtils.getObjectField("test.xdef.TestXComponents_Y21enum", "b");
			SUtils.setValueToSetter(list.get(1), "setc", o);
			assertEq("<A b='y'><B c='a'>x</B><B c='b' d='1 2'>y</B>x</A>",
				xc.toXml());
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"D\">\n" +
"<D a='union(%item=[byte(), unsignedByte()]);'/>\n" +
"<xd:component> %class mytests.MyTestXUnion %link #D; </xd:component>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXUnion");
			xml = "<D a='111'/>";
			assertEq(xml, parse(xp,"", xml, reporter));
			assertNoErrors(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<A xd:script=\"finally printf('Pi = %f', 3.141592)\"/>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, "<A/>", reporter);
			System.out.println(swr);
			System.out.println(String.join(", ", "a", "b")+".");
			System.out.println(String.format("a, %s.", "b"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"D\">\n" +
"<xd:declaration>\n"+
"  external method boolean mytests.MyTestX.chk1(String s);\n"+
"  external method boolean mytests.MyTestX.chk2(int i, XDContainer c);\n"+
"  external method void mytests.MyTestX.chk3();\n"+
"  external method boolean mytests.MyTestX.chk4(XXNode x, int i);\n"+
"  external method boolean mytests.MyTestX.chk5(XXNode x,int i);\n"+
"  external method void mytests.MyTestX.chk6(XXNode x, XDValue[] y);\n"+
"</xd:declaration>\n"+
"<D a='chk1(getText());' xd:script='finally chk3();'>\n" +
" <E b='chk4(4);' xd:script='finally chk6(1,2,%t=9);'>\n" +
"   chk5(5);\n" +
" </E>\n" +
" chk2(2, %a = 2);\n" +
"</D>\n" +
"<xd:component> %class mytests.MyTestXExt %link #D; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXExt");
			xml = "<D a='111'><E b='444'>555</E>222</D>";
			parse(xp,"", xml, reporter);
			assertNoErrors(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"D\">\n" +
"<xd:json name=\"D\">\n" +
"  [ \"* int();\"]\n" +
"</xd:json>\n" +
"<xd:component> %class mytests.MyTestXonD %link #D; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXonD");
			json = "[ 1, 2 ]";
			s = "mytests.MyTestXonD";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			System.out.println(xc.toXon());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"C\">\n" +
"<xd:json name=\"C\">\n" +
"  [ %anyObj, \"int();\"]\n" +
"</xd:json>\n" +
"<xd:component> %class mytests.MyTestXonC %link #C; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXonC");
			json = "[ true, 2 ]";
			s = "mytests.MyTestXonC";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			System.out.println(xc.toXon());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\">{ %anyName: %anyObj=\"occurs *;\" }</xd:json>\n" +
"<xd:component> %class mytests.MyTestXX00M %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX00M");
			s = "mytests.MyTestXX00M";
			json = "{}";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json), SUtils.getValueFromGetter(xc, "getMap$")));
			json = "{ a:1, b:true }";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json), SUtils.getValueFromGetter(xc, "getMap$")));
//			setValueToSetter(xc, "setval", 2);
			json = "null";
			assertNotNull(testX(xp, "", s, json)); // error: not map
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> \"int()\" </xd:json>\n" +
"<xd:component> %class mytests.MyTestXX00 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX00");
			s = "mytests.MyTestXX00";
			json = "1";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(1, SUtils.getValueFromGetter(xc, "getval"));
			SUtils.setValueToSetter(xc, "setval", 2);
			assertEq(2, SUtils.getValueFromGetter(xc, "getval"));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> [\"int()\"] </xd:json>\n" +
"<xd:component> %class mytests.MyTest_xxy %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTest_xxy");
			s = "mytests.MyTest_xxy";
			json = "[1]";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getArray$"));
			SUtils.setValueToSetter(xc, "set$item", 2);
			assertEq(1, ((List) xc.toXon()).size());
			assertEq(2, ((List) xc.toXon()).get(0));
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getArray$"));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> { a:\"? int()\" }</xd:json>\n" +
"<xd:component> %class mytests.MyTestXX02 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX02");
			s = "mytests.MyTestXX02";
			json = "{}";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getMap$"));
			assertNull(SUtils.getValueFromGetter(xc, "get$a"));
			json = "{a:123}";
			assertNull(testX(xp,"",s, json)); // OK
			SUtils.setValueToSetter(xc, "set$a", 9);
			assertEq(9, SUtils.getValueFromGetter(xc, "get$a"));
			SUtils.setValueToSetter(xc, "set$a", null);
			assertNull(SUtils.getValueFromGetter(xc, "get$a"));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getMap$"));
			assertEq(123, SUtils.getValueFromGetter(xc, "get$a"));
			SUtils.setValueToSetter(xc, "set$a", 9);
			assertEq(9, SUtils.getValueFromGetter(xc, "get$a"));
			SUtils.setValueToSetter(xc, "set$a", null);
			assertNull(SUtils.getValueFromGetter(xc, "get$a"));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> [ \"occurs *; int()\" ] </xd:json>\n" +
"<xd:component> %class mytests.MyTestXX03 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX03");
			s = "mytests.MyTestXX03";
			assertNull(testX(xp,"",s, "[]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[1,2]")); // OK
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> { a:\"? int()\" } </xd:json>\n" +
"<xd:component> %class mytests.MyTestXX04 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX04");
			s = "mytests.MyTestXX04";
			assertNull(testX(xp,"",s, "{}")); // OK
			assertNull(testX(xp,"",s, "{a:1}")); // OK
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> [ %anyObj] </xd:json>\n" +
//"<xd:json name=\"A\"> [ %anyObj=\"occurs 1;\", %anyObj=\"occurs ?;\" ] </xd:json>\n" +
"<xd:component> %class mytests.MyTestXX05 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX05");
			s = "mytests.MyTestXX05";
			assertNull(testX(xp,"",s, "[true]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[[1]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1}]")); // OK
			assertNull(testX(xp,"",s, "[[]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1,b:2}]")); // OK
			assertNotNull(testX(xp,"",s, "[1,2]")); // error more then two
			assertNotNull(testX(xp,"",s, "[[],{}]")); // error more then two
			assertNotNull(testX(xp,"",s, "[1,2,3]")); // error more then two
			assertNotNull(testX(xp,"",s, "[]")); // error empty
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\"> [ %anyObj=\"occurs +;\" ] </xd:json>\n" +
"<xd:component> %class mytests.MyTestXX06 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX06");
			s = "mytests.MyTestXX06";
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent("[[123, true]]", null, reporter);
			System.out.println(((List) xc.toXon()).get(0));
			assertNull(testX(xp,"",s, "[true]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[[1]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1}]")); // OK
			assertNull(testX(xp,"",s, "[[]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1,b:2}]")); // OK
			assertNull(testX(xp,"",s, "[1,2]")); // OK
			assertNull(testX(xp,"",s, "[[],{}]")); // OK
			assertNotNull(testX(xp,"",s, "[]")); // error empty
			assertNotNull(testX(xp,"",s, "{a:1,b:2}")); // error not array
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\">\n" +
"{ %anyName: %anyObj=\"occurs 2;\" }\n" +
"</xd:json>\n" +
"<xd:component> %class mytests.MyTestXX07 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX07");
			s = "mytests.MyTestXX07";
			assertNull(testX(xp,"",s, "{x:1, y:2}"));
			assertNotNull(testX(xp,"",s, "{}")); // empty
			assertNotNull(testX(xp,"",s, "{x:1}")); // only one item
//?			assertNotNull(testX(xp,"",s, "{x:1,y:2,z:3}"));//more items
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test %anyObj in array
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\">\n" +
"[ %anyObj = \"0..1;\" ]\n" +
"</xd:json>\n" +
"<xd:component> %class mytests.MyTestXX08 %link #A; </xd:component>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX08");
			s = "mytests.MyTestXX08";
			assertNull(testX(xp,"",s, "[]"));
			assertNull(testX(xp,"",s, "[true]"));
			assertNull(testX(xp,"",s, "[ [true] ]"));
			assertNull(testX(xp,"",s,"[{a:1,b:2}]"));
			assertNotNull(testX(xp,"",s,"[1,2]")); // more then one item
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test %anyObj in array
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:json name=\"A\">%anyObj</xd:json>\n" +
"<xd:component> %class mytests.MyTestXX09 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX09");
			s = "mytests.MyTestXX09";
			assertNull(testX(xp,"",s, "123"));
			assertNull(testX(xp,"",s, "[]"));
			assertNull(testX(xp,"",s, "[true]"));
			assertNull(testX(xp,"",s, "[ [true] ]"));
			assertNull(testX(xp,"",s,"{a:1,b:2}"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			reporter.clear();
			xdef = // test XON models
"<xd:def xmlns:xd='" + _xdNS + "' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" +
" [ %oneOf, \"jvalue(); finally out('V')\",\n" +
"   [ %script=\"*; ref anyA; finally out('A')\" ],\n" +
"   { %script=\"*; ref anyM; finally out('M')\" }\n" +
" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyA\">\n" +
" [ %anyObj=\"*;\" ]\n" +
//" [ [%script=\"*; finally outln('AA');\", %anyObj ] ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyM\">\n" +
//" { %anyName: %anyObj }\n"+
" {%anyName:\n" +
"   [%oneOf, \"jvalue()\",\n" +
"     [%script=\"ref Any; finally outln('MM')\"],\n" +
"   ]\n" +
" }\n" +
"</xd:json>\n" +
"<xd:component> %class mytests.MyTestXX10 %link X#Any; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
//			copyToSourceDir(file, "mytests", "MyTestXX10");
			s = "mytests.MyTestXX10";
			assertNull(testX(xp,"X", s, "true", "V"));
			assertNull(testX(xp,"X", s, "[]", "A"));
			assertNull(testX(xp,"X", s, "[[1,2]]", "A"));
			assertNull(testX(xp,"X", s, "{}", "M"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
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
