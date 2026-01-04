package test.xdef;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.xon.XonUtils;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Price;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.chkCompoinentSerializable;
import static test.XDTester.parseXC;

/** Test XComponents.
 * @author Vaclav Trojan
 */
public final class TestXComponents extends XDTester {

	public TestXComponents() {super();}

	private static void genXPosList(final XComponent xc, final StringBuilder sb) {
		sb.append(xc.xGetXPos()).append('\n');
		java.util.List<XComponent> childNodes = xc.xGetNodeList();
		if (childNodes != null && !childNodes.isEmpty()) {
			for (int i = 0; i < childNodes.size(); i++) {
				genXPosList(childNodes.get(i), sb);
			}
		}
	}

	private static String checkXPos(final XComponent xc) {
		StringBuilder sb = new StringBuilder();
		genXPosList(xc,sb);
		String before = sb.toString();
		sb.setLength(0);
		XComponentUtil.updateXPos(xc);
		genXPosList(xc, sb);
		String after = sb.toString();
		return before.equals(after)? "" : ("Before:\n"+before+"After:\n"+after);
	}

	/** Run test and print error information. */
	@Override
	@SuppressWarnings("unchecked")
	public void test() {
		// just ensure following classes are compiled!
		TestXComponents_C.class.getClass();
		TestXComponents_G.class.getClass();
		TestXComponents_Y04.class.getClass();
		TestXComponents_Y06Container.class.getClass();
		TestXComponents_Y06Domain.class.getClass();
		TestXComponents_Y06DomainContainer.class.getClass();
		TestXComponents_Y06XCDomain.class.getClass();
		TestXComponents_Y07Operation.class.getClass();
		TestXComponents_Y08.class.getClass();
		TestXComponents_Y21enum.class.getClass();
		TestXComponents_bindAbstract.class.getClass();
		TestXComponents_bindEnum.class.getClass();
		TestXComponents_bindInterface.class.getClass();
		///////////////////////////////////////////////////
		Element el;
		Class cls;
		List list, list1;
		Object o, xon;
		ArrayReporter reporter = new ArrayReporter();
		String s, xml, xdef, json;
		SDatetime sd;
		StringWriter swr;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		try {
			xdef = // test datetime with milliseconds = 0
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:declaration>type gam xdatetime('yyyyMMddHHmmssSSS');</xd:declaration>\n"+
"  <X a='gam()'>int()<Y xd:script='*' a='int()'/>? date()</X>\n"+
"  <xd:component>%class "+_package+".Mgam %link X</xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml =
"<X a='20201211010101333'>3<Y a='1'/><Y a='2'/>2021-12-30</X>";//millis == 333
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml =
"<X a='20201211010101000'>3<Y a='1'/><Y a='2'/></X>";//millis == 000
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <A> <B b='string'/> <C> <B b='string'/> </C> </A>\n" +
"  <xd:component> %class "+_package+".MichalTest %link #A; </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A><B b=\"1\"/><C><B b=\"2\"/></C></A>";
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"),"b"));
			assertEq("2", XComponentUtil.get((XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(
				xc, "C"),"B"), "b"));
			xdef = // GPSPosition, Price
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:declaration\n>\n"+
"    Price a;\n"+
"    GPSPosition p = new GPSPosition(50.08, 14.42, 399, 'Prague'), q;\n"+
"    int d; /* distance in km */\n"+
"  </xd:declaration>\n"+
"  <A xd:script='finally d = round(p.distanceTo(q)/1000); /* km */'\n"+
"     a='? price(); onTrue a= getParsedValue();'\n"+
"     q='gps(); onTrue q=getParsedValue();'/>\n"+
"  <xd:component>%class "+_package+".TY_GPS %link #A;</xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A a='1.25 CZK' q='48.2, 16.37, 151, Vienna'/>"; //
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("1.25 CZK", xd.getVariable("a").stringValue());
			assertEq(252, xd.getVariable("d").intValue());
			assertEq(new GPSPosition(48.2, 16.37, 151, null), XComponentUtil.get(xc, "q"));
			XComponentUtil.set(xc, "a", new Price(new BigDecimal("456.001"), "USD"));
			assertEq("456.001 USD", xc.toXml().getAttribute("a"));
			xml = "<A q='51.52,-0.09,0,\"London\"'/>"; //,
			el = parse(xd, xml, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, el);
			assertEq(1030, xd.getVariable("d").intValue());
			assertEq("51.52, -0.09, 0.0, London", xd.getVariable("q").toString());
			xp = compile(new String[] { // nested declaration of type
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_xc'>\n" +
"  <xd:component>\n" +
"    %class "+_package+".IdentDN %link D7_#A;\n" +
"    %class "+_package+".VymazDN extends "+_package+".IdentDN %link D7_#B;\n" +
"  </xd:component>\n" +
"</xd:def>",
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_' root='A | B'>\n" +
"  <xd:declaration scope=\"global\">\n" +
"    type  cisloDN num(5);\n" +
"    type  cj      string(1,50);\n" +
"    type  plan    gamDate();\n" +
"    type  rokDN   gamYear();\n" +
"  </xd:declaration>\n" +
"  <A RokDN=\"rokDN()\" CisloDN=\"cisloDN()\"/>\n" +
"  <B xd:script=\"ref A\" C=\"cj()\" P=\"? plan()\"/>\n" +
"</xd:def>",
"<xd:def xmlns:xd='"+_xdNS+"' name='D7_decl'>\n" +
"  <xd:declaration scope=\"global\">\n" +
"    type  gamYear  long(1800, 2200);\n" +
"    type  gamDate  xdatetime('yyyyMMdd');\n" +
"  </xd:declaration>\n" +
"</xd:def>"});
			genXComponent(xp);
			xml = "<A RokDN=\"2021\" CisloDN=\"12345\"/>";
			assertEq(xml, parse(xp, "D7_", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "D7_", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(2021L, XComponentUtil.get(xc,"RokDN"));
			assertEq("12345", XComponentUtil.get(xc,"CisloDN"));
			xml ="<B RokDN=\"2021\" CisloDN=\"12345\" C=\"x\" P=\"20210524\"/>";
			assertEq(xml, parse(xp, "D7_", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "D7_", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(2021L, XComponentUtil.get(xc,"RokDN"));
			assertEq("12345", XComponentUtil.get(xc, "CisloDN"));
			assertEq("x", XComponentUtil.get(xc,"C"));
			assertEq(new SDatetime("2021-05-24"), XComponentUtil.get(xc,"P"));
			xdef = // test base64/hex
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <X a='hex()' b='base64Binary()' c='SHA1()'/>\n"+
"  <xd:component> %class "+_package+".TestXexBase64 %link X; </xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<X a='1FA0' b='ahgkjfd01Q==' c='12AFE0C1D246895A990AB2DD13CE684F012B339C'/>";
			xd = xp.createXDDocument("");
			el = parse(xd, xml, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument("");
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(el,parse(xd, KXmlUtils.nodeToString(xc.toXml()),reporter));
			assertNoErrorwarningsAndClear(reporter);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='Person' root='Person'>\n"+
"  <Person Name  = \"string()\"\n" +
"          Birth = \"xdatetime('dd.MM.yyyy')\"\n" +
"          Sex   = \"enum('M','W', 'X')\"/>\n" +
"  <xd:component>\n" +
"    %class "+_package+".xcomp.XCPerson\n" +
"        extends "+_package+".TestXComponents_bindAbstract\n" +
"        implements "+_package+".TestXComponents_bindInterface\n" +
"        %link Person#Person;\n" +
"    %bind Name %with "+_package+".obj.Person %link Person#Person/@Name;\n" +
"    %bind SBirth %with "+_package+".obj.Person %link Person#Person/@Birth;\n" +
"    %bind SexString %with "+_package+".obj.Person %link Person#Person/@Sex;\n" +
"  </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xc = (XComponent) SUtils.getNewInstance(_package+".xcomp.XCPerson");
			XComponentUtil.set(xc,"Name", "John Brown");
			XComponentUtil.set(xc,"Birth", new Timestamp(new Date(0).getTime()));
			XComponentUtil.set(xc,"Sex", TestXComponents_bindEnum.M);
			xml = "<Person Birth='01.01.1970' Name='John Brown' Sex='M'/>";
			assertEq(xml, xc.toXml());
			xd = xp.createXDDocument("Person");
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent(null, "Person", null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='XdPoolCfg'>\n" +
"  <Resource xd:script=\"occurs 0..;\">string();</Resource>\n" +
"  <IncludeXDPoolCfg>\n" +
"    <PoolCfg xd:script=\"occurs 0..; ref Resource\"/>\n" +
"  </IncludeXDPoolCfg>\n" +
"  <XdPoolCfg>\n" +
"    <IncludeExternals xd:script=\"occurs 0..1; ref IncludeXDPoolCfg\"/>\n" +
"    <Externals xd:script=\"occurs 0..1\">\n" +
"      <ClassPath xd:script=\"occurs 0..; ref Resource\"/>\n" +
"    </Externals>\n" +
"    <XDefs xd:script=\"occurs 0..1\">\n" +
"      <Resource xd:script=\"occurs 0..; ref Resource\"/>\n" +
"    </XDefs>\n" +
"  </XdPoolCfg>\n" +
"  <xd:component>\n" +
"    %class bugreports.data.XCIncludeXDPoolCfg %link X#IncludeXDPoolCfg;\n" +
"    %class bugreports.data.XCXdPoolCfg  %link X#XdPoolCfg;\n" +
"    %class bugreports.data.XCExternals %link X#XdPoolCfg/Externals;\n" +
"    %class bugreports.data.XCClass %link X#XdPoolCfg/Externals/ClassPath;\n" +
"    %class bugreports.data.XCXDefs %link X#XdPoolCfg/XDefs;\n" +
"    %class bugreports.data.XCResource %link X#Resource;\n" +
"  </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument("X");
			xml =
"<XdPoolCfg>\n"+
"  <IncludeExternals/>\n"+
"  <Externals>\n"+
"    <ClassPath>abc</ClassPath><ClassPath>def</ClassPath></Externals>\n"+
"  <XDefs><Resource>ghi</Resource></XDefs>\n" +
"</XdPoolCfg>";
			assertEq(xml, parse(xdef, "X", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xd = xp.createXDDocument("X");
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent(null, "XdPoolCfg", null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name = \"X\" root = \"a\">\n" +
"  <a>\n" +
"    <b xd:script = \"occurs 0..\" Name = \"string(1,20)\">\n" +
"      <Param xd:script = \"occurs 0..\" Name = \"string(1,20)\">\n" +
"        <xd:choice>\n" +
"          <ScriptValue xd:script = \"occurs 1;   ref Script\"/>\n" +
"          <EnumValue   xd:script = \"occurs 1..; ref Enum\"/>\n" +
"          <TabValue    xd:script = \"occurs 1;   ref Tab\"/>\n" +
"        </xd:choice>\n" +
"      </Param>\n" +
"    </b>\n" +
"  </a>\n" +
"  <Script Script = \"string(1,255)\" />\n" +
"  <Enum Value = \"string(1,255)\" Label = \"string(1,255)\" />\n" +
"  <Tab Value = \"string(1,255)\" Where = \"string(1,255)\" />\n" +
"  <xd:component>\n" +
"    %class bugreports.data.DefCommands %link X#a;\n" +
"    %class bugreports.data.UserDefCommand %link X#a/b;\n" +
"  </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml =
"<a>\n"+
"  <b Name='X1'>\n"+
"    <Param Name='XP1'><ScriptValue Script='scr' /></Param>\n" +
"  </b>\n"+
"  <b Name='X2'>\n"+
"    <Param Name='XP2'>\n" +
"      <EnumValue Value='V1' Label= 'L1'/><EnumValue Value='V2' Label='L2'/>\n" +
"    </Param>\n" +
"  </b>\n"+
"  <b Name='X3'>\n"+
"    <Param Name='XP3'><TabValue Value='T1' Where='W1' /></Param>\n" +
"  </b>\n"+
"  <b Name='X4'/>\n"+
"</a>";
			assertEq(xml, parse(xdef, "X", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument("X");
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xd = xp.createXDDocument("X");
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent(null, "a", null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xdef = // test jcreateXComponent
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:json name = 'X'>{a:\"int();\", b:[\"boolean();\"]}</xd:json>\n"+
"  <xd:component> %class bugreports.data.JCreateX1 %link X </xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			s = "{a:1, b:[true]}";
			xon = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xon, XonUtils.parseXON(s)));
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xd = xp.createXDDocument();
			xd.setXDContext(xc.toXml());
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:component> %class bugreports.data.JCreateX2 %link X </xd:component>\n"+
"  <xd:json name = 'X'>[\"2 boolean()\", \"boolean()\"]</xd:json>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			s = "[true, false, true]";
			xon = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(xon, xd.jparse(s, reporter)));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xd.setXONContext(XonUtils.xonToJson(xon));
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:component>%class "+_package+".JCreateX3 %link X</xd:component>\n"+
"  <xd:json name = 'X'>[\"2 boolean()\", \"boolean()\"]</xd:json>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			s = "[true, false, true]";
			xon = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(xon, xd.jparse(s, reporter)));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xd.setXONContext(XonUtils.xonToJson(xon));
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:json name=\"X\"> {b:[ \"int();\",[\"int();\"],\"string();\"]}</xd:json>\n"+
"  <xd:component> %class "+_package+".JCreateX4 %link X </xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			s = "{b:[1, [2], \"x\"]}";
			xon = XonUtils.parseXON(s);
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xdef = // jcreate with create section
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:json name = 'X'>\n"+
"   [ \"boolean(); create 'true'\", \"int(); create '2'\" ]\n"+
"  </xd:json>\n"+
"  <xd:component>%class "+_package+".JCreateX5 %link X</xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			xon = XonUtils.parseXON("[true, 2]");
			assertTrue(XonUtils.xonEqual(xon, xd.jcreate("X", reporter)));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:json name = 'X'>\n"+
"    { a:\"int(); create '1'\",\n"+
"      b:[ \"boolean(); create 'true'\", \"int(); create '2'\" ]\n"+
"    }\n"+
"  </xd:json>\n"+
"  <xd:component>%class "+_package+".JCreateX6 %link X</xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			xon = XonUtils.parseXON("{a:1, b:[true, 2]}");
			assertTrue(XonUtils.xonEqual(xon, xd.jcreate("X", reporter)));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xon, xc.toXon()));
			xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='x'>\n"+
"<x>\n"+
"  <a xd:script='*'>\n"+
"    jlist(%item=jvalue())\n"+
"  </a>\n"+
"  <b xd:script='*'>\n"+
"    jlist(%item=union(%item=[jnull,boolean()]))\n"+
"  </b>\n"+
"</x>\n"+
"<xd:component> %class bugreports.JCreateX7 %link x; </xd:component>\n"+
"</xd:def>");
			genXComponent(xp, clearTempDir());
			xml =
"<x>\n"+
"  <a>[1,\"a\\\"\\nbc\"]</a>\n"+
"  <a>[\"\",[-3.5,null,\"\\\"\"]]</a>\n"+
"  <b>[null,true,false,null]</b>\n"+
"</x>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp, "", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			list = (List)((List) XComponentUtil.get(xc, "$a")).get(0);
			assertEq(1, list.get(0));
			assertEq("a\"\nbc", list.get(1));
			list = (List)((List)XComponentUtil.get(xc,"$a")).get(1);
			assertEq("", list.get(0));
			list = (List) list.get(1);
			assertEq(-3.5, list.get(0));
			assertNull(list.get(1));
			assertEq("\"",list.get(2));
			list = (List)((List) XComponentUtil.get(xc,"$b")).get(0);
			assertNull(list.get(0));
			assertTrue((boolean)list.get(1));
			assertFalse((boolean)list.get(2));
			assertNull(list.get(3));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"<xd:component>%class "+_package+".TestX_OneOfa %link a</xd:component>\n"+
"  <xd:json name='a'>\n" +
"    {\n" +
"      \"%oneOf\": [\"manager\", \"subordinates\"],\n" +
"      \"manager\": \"? string()\",\n" +
"      \"subordinates\":[\"%script: ?;\", \"* string();\" ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			s = "{\"manager\": \"BigBoss\"}";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			assertEq("BigBoss", XComponentUtil.get(xc,"$manager"));
			assertNull(XComponentUtil.get(xc, "$subordinates"));
			s = "{\"subordinates\": []}";
			xd = xp.createXDDocument();
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			assertNull(XComponentUtil.get(xc, "$manager"));
			xon = XComponentUtil.get(xc, "$subordinates");
			assertEq(((Map)o).get("subordinates"), ((XComponent) xon).toXon());
			s = "{\"subordinates\": [\"first\", \"second\"]}";
			xd = xp.createXDDocument();
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			assertNull(XComponentUtil.get(xc, "$manager"));
			xon = XComponentUtil.get(xc, "$subordinates");
			assertEq(((Map)o).get("subordinates"), ((XComponent) xon).toXon());
			s = "{}";
			xd = xp.createXDDocument();
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			assertNull(XComponentUtil.get(xc, "$manager"));
			assertNull(XComponentUtil.get(xc, "$subordinates"));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root=\"test\">\n" +
"<xd:component>%class "+_package+".MyTestX_OneOfb %link test</xd:component>\n"+
"  <xd:json name=\"test\">\n" +
"    { a:[ \"%oneOf: ?\",\n" +
"        \"jnull(); finally outln('null')\", \n" + // must be first
"        \"date(); finally outln('date')\", \n" +
"        \"ipAddr(); finally outln('ipAddr')\", \n" +
"        [\"%script: finally outln('[...]')\",\"*int()\"], \n" +
"        {\"%script\": \"finally outln('{ . }')\",x:\"? int()\",y:\"?string()\"},\n"+
"        \"string(); finally outln('string')\" \n" +
"      ]\n" +
"    }\n" +
"</xd:json>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			s = "{a:\"2022-04-10\"}";
			o = XonUtils.parseXON(s);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xon = xd.jparse(s, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.xonToJson(xon), o));
			assertEq("date\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("date\n", swr.toString());
			o = XComponentUtil.get(xc, "jx$"+XonNames.X_VALUE+"_1"); // jx$val_1
			XComponentUtil.set((XComponent) o, XonNames.X_VALATTR, new SDatetime("2022-04-15"));
			assertEq(new SDatetime("2022-04-15"), ((Map)xc.toXon()).get("a"));
			s = "{a:\"202.2.4.10\"}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("ipAddr\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:{x:1, y:\" ab\tcd \"}}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			assertEq(1,((Map)((Map) o).get("a")).get("x"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("{ . }\n", swr.toString());
			assertEq(" ab\tcd ",((Map)((Map) xc.toXon()).get("a")).get("y"));
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:[1,2]}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("[...]\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:{}}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("{ . }\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:null}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("null\n", swr.toString()); //????, however it is OK
			assertNull(((Map) o).get("a"));
			assertTrue(((Map) o).containsKey("a"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("null\n", swr.toString());
			assertNull(XComponentUtil.get(xc, "$a"));
			assertNull(((Map) xc.toXon()).get("a"));
			assertTrue(((Map) xc.toXon()).containsKey("a"));
			s = "{}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", swr.toString()); //????
			assertNull(((Map) o).get("a"));
			assertFalse(((Map) o).containsKey("a"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("", swr.toString());
			assertNull(XComponentUtil.get(xc, "$a"));
			assertNull(((Map) xc.toXon()).get("a"));
			assertFalse(((Map) xc.toXon()).containsKey("a"));
			xdef = // sequence with separator
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:component>%class "+_package+".MytestX_SQ %link #a;</xd:component>\n" +
"  <xd:declaration>\n"+
"    type s sequence(%separator=',', %item=[int, long, long]);\n"+
"  </xd:declaration>\n"+
"  <a a='? s'> ? s; <b xd:script='?'> s; </b> </a>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNull(XComponentUtil.get(xc, "a"));
			xml = "<a a='1,2,3'>4,5,6</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, (xc = parseXC(xp,"", xml , null, reporter)).toXml());
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if ((o = XComponentUtil.get(xc, "a")) instanceof List) {
				assertTrue(((List) o).get(0) instanceof Long);
				assertEq(1, ((List) o).get(0));
				assertEq(2, ((List) o).get(1));
				assertEq(3, ((List) o).get(2));
			} else {
				fail("incorrect type: " + o.getClass() + "; " + o);
			}
			if ((o = XComponentUtil.get(xc, "$value")) instanceof List) {
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
			assertEq(xml, (xc = parseXC(xp,"", xml , null, reporter)).toXml());
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNull(XComponentUtil.get(xc, "a"));
			assertNull(XComponentUtil.get(xc, "$value"));
			if ((o = XComponentUtil.get(xc, "$b")) instanceof List) {
				assertEq(5, ((List) o).get(0));
				assertEq(6, ((List) o).get(1));
				assertEq(7, ((List) o).get(2));
			} else {
				fail("incorrect type: " + o.getClass() + "; " + o);
			}
			xdef = //Names of getters of A/B and A/C/B must be same
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <A> <B b='string'/> <C> <B b='string'/> </C> </A>\n" +
"  <xd:component> %class "+_package+".TestB %link #A; </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A><B b=\"1\"/><C><B b=\"2\"/></C></A>";
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "b"));
			assertEq("2", XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get(xc, "C"), "B"), "b"));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A0'>\n" +
"  <A0>\n" +
"    <B><D xd:script=\"ref D\" /><C c=\"string(1)\"/></B>\n" +
"  </A0>\n"+
"  <D><C c=\"string(3)\"/></D>\n"+
"  <xd:component> %class "+_package+".A0 %link #A0; </xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A0><B><D><C c=\"d/c\"/></D><C c=\"c\"/></B></A0>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("c", XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get(xc, "B"), "C"), "c"));
			assertEq("d/c", XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "D"), "C"), "c"));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A1'>\n" +
"  <A1>\n" +
"    <B><C c=\"string(1)\"/><D xd:script=\"ref D\" /></B>\n" +
"  </A1>\n"+
"  <D><C c=\"string(3)\"/></D>\n"+
"  <xd:component> %class "+_package+".A1 %link #A1; </xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A1><B><C c=\"c\"/><D><C c=\"d/c\"/></D></B></A1>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("c", XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get(xc, "B"), "C"), "c"));
			assertEq("d/c", XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "D"), "C"), "c"));
			xdef = //test any, moreAttributes, moreElements, moreText
"<xd:def  xmlns:xd='"+_xdNS+"' root='X'>\n"+
"  <xd:component>%class bugreports.data.M %link X</xd:component>\n"+
"  <xd:any xd:name='X'\n"+
"     xd:script='options moreAttributes, moreElements, moreText'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml = "<A><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></A>";
			assertEq(xml, parse(xp, "", xml , reporter));
			assertNoErrorwarnings(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xp = compile(new String[] { // any, create mode
"<xd:def  xmlns:xd='"+_xdNS+"' name='A' root='A'>\n" +
"  <A><xd:any xd:script='options moreElements,moreText,moreAttributes'/></A>\n"+
"  <xd:component> %class "+_package+".Kalcik %link A#A; </xd:component>\n" +
"</xd:def>",
"<xd:def  xmlns:xd='"+_xdNS+"' name='B' root='X'>\n" +
"  <X xd:script='create from(\"/*\")' a=\"string()\" b=\"date()\" />\n" +
"</xd:def>"});
			genXComponent(xp);
			xd = xp.getXMDefinition("A").createXDDocument();
			xml = "<A><B a='x' b='2000-01-21'/></A>";
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			el = ((XComponent) XComponentUtil.get(xc, "$any")).toXml();
			xd = xp.getXMDefinition("B").createXDDocument();
			xd.setXDContext(el);
			el = xd.xcreate("X", reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("<X b='2000-01-21' a='x' />", el);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"  <A a='int(-1) || int(0, 100);'/>\n" +
"  <xd:component> %class "+_package+".TestXKoci1 %link #A; </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A a='20'/>";
			parse(xp,"", xml, reporter);
			assertNoErrors(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrors(reporter);
			o = XComponentUtil.get(xc, "a");
			assertTrue(o instanceof String && "20".equals(o));
			assertEq(xml, xc.toXml());
			xdef = // test union
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"  <xd:declaration>\n"+
"    type s union(%item=[byte(-1), int(1, 100)]);\n"+
"  </xd:declaration>\n"+
"  <A a='s'>s;</A>\n"+
"  <xd:component> %class "+_package+".TestXKoci2 %link #A; </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<A a='-1'>20</A>";
			parse(xp,"", xml, reporter);
			assertNoErrors(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrors(reporter);
			o = XComponentUtil.get(xc, "a");
			assertTrue(o instanceof Integer && (-1 == (Integer) o));
			o = XComponentUtil.get(xc, "$value");
			assertTrue(o instanceof Integer && (20 == (Integer) o));
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
////////////////////////////////////////////////////////////////////////////////
		try {// generate XCDPool from sources used in next tests
			xp = compile(new String[] {
				getDataDir()+"test/TestXComponents.xdef", getDataDir() + "test/TestXComponent_Z.xdef"});
			// generate and compile XComponents from xp
			assertNoErrors(genXComponent(xp));
		} catch (Exception ex) {
			fail(ex);
			return;
		}
		try {
			xml = "<A a='a' dec='123.45'><W w='wwwwwwww'/></A>";
			parseXC(xp, "A", xml, null, reporter);
			assertTrue(reporter.errors());
			reporter.clear();
			xml = "<A a='a' dec='123.45'><W w='w'>wwwwwwww</W></A>";
			parseXC(xp, "A", xml, null, reporter);
			assertTrue(reporter.errors());
			reporter.clear();
			xml =
"<A a='a' dec='123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>1</i><Y>3</Y>"+
"<d>2013-09-14</d><t>10:20:30</t><s>Franta</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>";
			xc = parseXC(xp, "A", xml, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			assertEq("/A/@a", XComponentUtil.getx(xc, "xposOfa"));
			assertEq("/A/d[1]/$text", XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "d"),
				"xposOf$value"));
			list = (List) XComponentUtil.get(xc,"$Y");
			assertEq("1", list.get(0));
			assertEq("2", list.get(1));
			assertEq("3", XComponentUtil.get(xc, "$Y_1"));
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "d"), "$value").toString(),
				"2013-09-14");
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "d"), "$value"),
				XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "d"), "dateOf$value"));
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "d"), "$value"),
				XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "d"), "timestampOf$value"));
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "d"), "$value"),
				XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "d"), "calendarOf$value"));
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "i"), "$value").toString(), "1");
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "t"), "$value").toString(),
				"10:20:30");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "i"),"$value", BigInteger.valueOf(2));
			assertEq("Franta", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "s"), "$value"));
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "s"), "$value", "Pepik");
			assertTrue(BigInteger.valueOf(2).equals(
				XComponentUtil.get((XComponent) XComponentUtil.get(xc,"i"),"$value")));
			assertEq("Pepik", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "s"), "$value"));
			list = (List) XComponentUtil.getx(xc, "listOfd2");
			assertEq("/A/d2[1]", ((XComponent)list.get(0)).xGetXPos());
			assertEq("/A/d2[2]", ((XComponent)list.get(1)).xGetXPos());
			assertEq(xc.toXml(),
"<A a='a' dec = '123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>2</i><Y>3</Y>"+
"<d>2013-09-14</d><t>10:20:30</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "i"), "$value", BigInteger.valueOf(3));
			SDatetime date =  new SDatetime("2013-09-01");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "d"), "$value", date);
			SDatetime time =  new SDatetime("11:21:31");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "t"), "$value", time);
			assertTrue(BigInteger.valueOf(3).equals(
				XComponentUtil.get((XComponent) XComponentUtil.get(xc,"i"),"$value")));
			assertEq(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "d"), "$value"), date);
			el = xc.toXml();
			assertEq(el,
"<A a='a' dec = '123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>3</i><Y>3</Y>"+
"<d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			assertEq("", checkXPos(xc));
			xc = parseXC(xp, "A", el, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			el = xc.toXml();
			assertEq(el,
"<A a='a' dec = '123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>3</i><Y>3</Y>"+
"<d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			XComponentUtil.set(xc, "dec", new BigDecimal("456.01"));
			assertEq(xc.toXml(),
"<A a='a' dec='456.01'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>3</i><Y>3</Y>"+
"<d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			XComponentUtil.set(xc, "dec", new BigDecimal("123.45"));
			list = (List) XComponentUtil.getx(xc, "listOfW");
			assertNull(XComponentUtil.get((XComponent) list.get(0), "$value"));
			assertEq("blabla", XComponentUtil.get((XComponent) list.get(1),"$value"));
			list.clear();
			((List) XComponentUtil.getx(xc, "listOfY")).clear();
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "i"), "$value",BigInteger.valueOf(99));
			assertEq(xc.toXml(), //clone
"<A a='a' dec='123.45'>"+
"<i>99</i><Y>3</Y><d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			el = XComponentUtil.toXml(xc, xp.createXDDocument("B"), "A");
			xml = "<A id='99' date='2013-09-01' time='11:21:31' name='Pepik'/>";
			assertEq(xml, el);
			try {
				XComponentUtil.set(xc, "dec",new BigDecimal("456.001"));
				el = xc.toXml();
				xp.createXDDocument("A").xparse(el, null);
				fail("Error not reported" + el);
			} catch (RuntimeException ex) {
				if (!ex.getMessage().contains("path=/A/@dec")) {
					fail(ex);
				}
			}
			assertEq("", checkXPos(xc));
			el = XComponentUtil.toXml(xc, xp.createXDDocument("B"), "A");
			xc = parseXC(xp, "B", el, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xc.toXml(), xml);
			xd = xp.createXDDocument("B");
			assertEq(xd.xparse(el, null), xml);
			xc = parseXC(xd, el, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xc.toXml(), xml);
			try {
				XComponentUtil.getVariable(xc, "nazdar");
				fail("Error not reported");
			} catch (NoSuchMethodException ex) {
				if (!ex.getMessage().contains("nazdar")) {
					fail(ex);
				}
			}
			xml = "<Z z='z'/>";
			XMElement xe = xp.getXMDefinition("A").getModel(null, "A");
			assertEq("", chkCompoinentSerializable(xc));
			xd = null;
			for (XMNode xm: xe.getChildNodeModels()) {
				if ("Z".equals(xm.getName())) {
					xd = ((XMElement) xm).createXDDocument();
					break;
				}
			}
			if (xd != null) {
				xc = parseXC(xd, xml, null, null);
				assertEq("", chkCompoinentSerializable(xc));
				assertEq("z", XComponentUtil.getVariable(xc, "z"));
				assertEq("z", XComponentUtil.get(xc, "z"));
				assertEq(xc.toXml(), xml);
				XComponentUtil.set(xc, "z", "Z");
				assertEq("<Z z='Z'/>", xc.toXml());
				XComponentUtil.set(xc, "z", null);
				assertEq("<Z/>", xc.toXml());
			} else {
				fail("Model not found");
			}
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml =
"<Town Name='Praha'>" +
	"<Street Name='Dlouha'>" +
		"<House Num='1'>"+
			"<Person FirstName='Jan' LastName='Novak'></Person>"+
			"<Person FirstName='Jana' LastName='Novakova'></Person>"+
		"</House>"+
		"<House Num='2'/>"+
		"<House Num='3'>"+
			"<Person FirstName='Josef' LastName='Novak'></Person>"+
		"</House>"+
	"</Street>"+
	"<Street Name='Kratka'>"+
		"<House Num='1'>"+
			"<Person FirstName='Pavel' LastName='Novak'></Person>"+
		"</House>"+
	"</Street>"+
"</Town>";
			xc = parseXC(xp, "C", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(3, XComponentUtil.get(xc, "Test"));
			assertEq(xml, xc.toXml());
			o = ((List) XComponentUtil.getx(xc, "listOfStreet")).get(0);
			assertEq("Dlouha", XComponentUtil.get((XComponent) o, "Name"));
			o = ((List) XComponentUtil.getx((XComponent) o, "listOfHouse")).get(0);
			assertEq(1, XComponentUtil.get((XComponent) o, "Num"));
			o = ((List) XComponentUtil.getx((XComponent) o, "listOfPerson")).get(0);
			assertEq("Jan", XComponentUtil.get((XComponent) o, "FirstName"));
			assertEq("Novak", XComponentUtil.get((XComponent) o, "LastName"));
			XMElement xe = xp.getXMDefinition("C").getModel(null, "Town");
			for (XMNode xn: xe.getChildNodeModels()) {
				if ("Street".equals(xn.getName())) {
					xe = (XMElement) xn;
					for (XMNode xn1: xe.getChildNodeModels()) {
						if ("House".equals(xn1.getName())) {
							xe = (XMElement) xn1;
							break;
						}
					}
				}
			}
			el = XComponentUtil.toXml(xc, xp, "C#Persons");
			xml =
"<Persons>"+
	"<Person>Jan Novak; Dlouha 1, Praha</Person>"+
	"<Person>Jana Novakova; Dlouha 1, Praha</Person>"+
	"<Person>Josef Novak; Dlouha 3, Praha</Person>"+
	"<Person>Pavel Novak; Kratka 1, Praha</Person>"+
"</Persons>";
			assertEq(xml, el);
			xml =
"<House Num='3'>"+
	"<Person FirstName='Jan' LastName='Novak'/>"+
	"<Person FirstName='Jana' LastName='Novakova'/>"+
"</House>";
			xc = parseXC(xe.createXDDocument(), xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(3, XComponentUtil.get(xc, "Num"));
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<a:A xmlns:a='a.b'>"+
"<_/>"+
	"<B A='true'>1<X>true</X><X>false</X>2<Y>true</Y></B>"+
	"<I A='1'><X>2</X><X>3</X><Y>4</Y></I>"+
	"<F A='3.14'><X>-3.14</X><X>-3.15</X><Y>NaN</Y></F>"+
	"<G A='ahgkjfd01Q=='>"+
		"<X>bhgkjfd01Q==</X><X>chgkjfd01Q==</X><Y>dhgkjfd01Q==</Y>"+
	"</G>"+
	"<H A='0123456789ABCDEF'>"+
		"<X>ABCDEF03456789</X><X>89ABCDE34567</X><Y>6789</Y>"+
	"</H>"+
	"<P A='1.15'><X>0.1</X><X>123.0</X><Y>-12.0</Y></P>"+
	"<Q A='2013-09-25'><X>2013-09-26</X><X>2013-09-27</X><Y>2013-09-28</Y></Q>"+
	"<R A='P2Y1M3DT11H'><X>P2Y1M3DT12H</X><X>P2Y1M3DT13H</X><Y>P2Y1M3DT14H</Y>"+
	"</R>"+
	"<S A='abc'><X>abc</X><X>def</X><Y>ghi</Y></S>"+
	"<E/>"+
	"<T xmlns='x.y' t='s'><I/></T>"+
	"<a:T xmlns:a='a.b' a:t='t'><a:I/></a:T>"+
"</a:A>";
			xc = parseXC(xp, "D", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			el = xc.toXml();
			assertEq(el, xml);
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "$value"));
			assertEq("/a:A/B[1]/$text", XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "B"),
				"xposOf$value"));
			assertEq("2", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "$value1"));
			assertEq("/a:A/B[1]/$text", XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "B"),
				"xposOf$value1"));
			byte[] bytes;
			bytes = SUtils.decodeBase64("ahgkjfd01Q==");
			assertEq(bytes, XComponentUtil.get((XComponent) XComponentUtil.get(xc, "G"), "A"));
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "G"), "listOfX");
			assertEq(2,list.size());
			bytes = SUtils.decodeBase64("bhgkjfd01Q==");
			assertEq(bytes, XComponentUtil.get((XComponent) list.get(0),"$value"));
			bytes = SUtils.decodeBase64("dhgkjfd01Q==");
			assertEq(bytes, XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent)XComponentUtil.get(xc,"G"),"Y"),
				"$value"));
			bytes = SUtils.decodeHex("0123456789ABCDEF");
			assertEq(bytes, XComponentUtil.get((XComponent) XComponentUtil.get(xc, "H"), "A"));
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "H"), "listOfX");
			assertEq(2, list.size());
			bytes = SUtils.decodeHex("ABCDEF03456789");
			assertEq(bytes, XComponentUtil.get((XComponent) list.get(0), "$value"));
			bytes = SUtils.decodeHex("89ABCDE34567");
			assertEq(bytes, XComponentUtil.get((XComponent) list.get(1),"$value"));
			bytes = SUtils.decodeHex("6789");
			assertEq(bytes,
				XComponentUtil.get((XComponent) XComponentUtil.get(
					(XComponent)XComponentUtil.get(xc,"H"),"Y"),
				"$value"));
			assertEq("t", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "a$T"), "a$t"));
			assertTrue(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "a$T"), "a$I") != null);
		} catch (SException ex) {fail(ex);}
		try {
			xml = "<if><class try='t'/></if>";
			xc = parseXC(xp, "E", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xc.toXml(), xml);
			assertEq("t", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "Clazz"), "try"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<X><A/></X>";
			xc = parseXC(xp, "F", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xc.toXml(), xml);
			xc = (XComponent) SUtils.getNewInstance(_package+".component.F");
			assertEq("", chkCompoinentSerializable(xc));
			XComponentUtil.set(xc, "B", null);
			XComponentUtil.set(xc, "A", SUtils.getNewInstance(_package+".component.F$A"));
			assertEq(xc.toXml(), "<X><A/></X>");
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Y><B/><A/></Y>";
			xc = parseXC(xp, "F", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			o = (XComponent) SUtils.getNewInstance(_package+".component.F1");
			XComponentUtil.set((XComponent) o, "A", XComponentUtil.get(xc, "A"));
			XComponentUtil.setVariable((XComponent) o, "B", XComponentUtil.get(xc, "B"));
			assertEq(((XComponent) o).toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<G g='g'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>";
			xd = xp.createXDDocument("G");
			o = SUtils.getNewInstance(_package+".component.G");
			xd.setUserObject(o);
			xc = parseXC(xd, xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("<G g='g_'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>", xc.toXml());
			assertEq("x", XComponentUtil.get((XComponent) SUtils.getObjectField(o, "_X"), "x"));
			assertEq("z", XComponentUtil.getVariable((XComponent) SUtils.getObjectField(o, "_Y"),"y"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<s:H xmlns:s='soap' s:encodingStyle='encoding'>"+
	"<s:Header>"+
		"<b:User xmlns:b='request' s:understand='true' IdentUser='Novak'/>"+
		"<b:Request xmlns:b='request' IdentZpravy='xx' s:understand='true'/>"+
	"</s:Header>"+
	"<s:Body><b:PingFlow xmlns:b='request' Flow='B1B'/></s:Body>"+
"</s:H>\n";
			xc = parseXC(xp, "H", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xc.toXml(), xml);
			xd = xp.createXDDocument("H");
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent(new QName("soap", "s:H"), null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xml = "<Ping/>";
			xc = parseXC(xp, "I", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xc.toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<A><B/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<A><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<A><B/><C/><C/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<A><B/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><X x='x'/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><C/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<C>a<D/>b<D/>c</C>\n";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml()); // ? <C>a<D/>c<D/>b</C> => poradi textu
			xml = "<C>a<D/>b<D/>c</C>\n";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml()); // ? <C>a<D/>c<D/>b</C> => poradi textu
			xml = "<C>a<D/>b<D/><D/>c</C>\n";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml()); //  <C>a<D/>c<D/><D/>b</C> =>poradi textu
		} catch (Exception ex) {fail(ex);}
		try {
			int century = new GregorianCalendar().get(Calendar.YEAR);
			int y = century % 100; // actual year in century;
			century /= 100; // actual century
			DecimalFormat df = new DecimalFormat("00");
			xml =
"<A>" +
"<c Kod='1' Cislo='1' Rok='00'/>" +
"<c Kod='1' Cislo='2' Rok='01'/>" +
"<c Kod='1' Cislo='3' Rok='10'/>" +
"<c Kod='1' Cislo='4' Rok='" + df.format(y-1)+ "'/>" +
"<c Kod='1' Cislo='5' Rok='" + df.format(y)+ "'/>" +
"<c Kod='1' Cislo='6' Rok='" + df.format(y+1)+ "'/>" +
"<c Kod='1' Cislo='7' Rok='99'/>" +
"123456" +
"<d a='23.6.2015'/>" +
"</A>";
			xc = parseXC(xp, "K", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			list = (List) XComponentUtil.getx(xc, "listOfc");
			assertEq(7, list.size());
			int x;
			assertEq("1", XComponentUtil.get((XComponent) list.get(0), "Cislo"));
			assertEq(String.valueOf((x=00)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(0), "Rok").toString());
			assertEq("2", XComponentUtil.get((XComponent) list.get(1), "Cislo"));
			assertEq(String.valueOf((x=01)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(1), "Rok").toString());
			assertEq("3", XComponentUtil.get((XComponent) list.get(2), "Cislo"));
			assertEq(String.valueOf((x=10)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(2), "Rok").toString());
			assertEq("4", XComponentUtil.get((XComponent) list.get(3), "Cislo"));
			assertEq(String.valueOf((x=y-1)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(3), "Rok").toString());
			assertEq("5", XComponentUtil.get((XComponent) list.get(4), "Cislo"));
			assertEq(String.valueOf((x=y)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(4), "Rok").toString());
			assertEq("6", XComponentUtil.get((XComponent) list.get(5), "Cislo"));
			assertEq(String.valueOf((x=y+1)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(5), "Rok").toString());
			assertEq("7", XComponentUtil.get((XComponent) list.get(6), "Cislo"));
			assertEq(String.valueOf((x=99)+(y<x ? century-1 : century)*100),
				XComponentUtil.get((XComponent) list.get(6), "Rok").toString());
			assertEq("123456", "" + XComponentUtil.get(xc, "$value"));
			assertEq("2015-06-23", "" + XComponentUtil.get((XComponent) XComponentUtil.get(xc, "d"), "a"));
		} catch (Exception ex) {fail(ex);}
		try {
			//just force compilation
			xml = "<L><D></D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<L><D a='a'>a</D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<L><D><E/></D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<L><D a='a'>a<E><F>b</F>c</E>d</D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<xx><D a='a'>a<E><F>b</F>c</E>d</D></xx>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "M", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><Operation One='9' Two='a'/></A>";
			xc = parseXC(xp, "N", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			o = XComponentUtil.get(xc, "Operation");
			assertEq(9, XComponentUtil.get((XComponent) o, "One"));
			assertEq("a", XComponentUtil.get((XComponent) o, "Two"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			xc = parseXC(xp, "O", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<A>" +
"  <a a='3.1' b='3.3.1999'/>\n"+
"  <b a='4.1' b='4.3.1999'/>\n"+
"  <b a='5.1' b='5.3.1999'/>\n"+
"  <c a='4.1' b='4.3.1999'/>\n"+
"  <d a='4.1' b='4.3.1999'/>\n"+
"</A>";
			xc = parseXC(xp, "P", xml, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xc.toXml(), parse(xp.createXDDocument("P"), xml,reporter));
			assertNoErrorwarningsAndClear(reporter);
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml =
"<X><A><B><E>1</E></B><C/></A><A><B/><B><E>2</E></B><C/><C/></A><A/></X>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			list = (List) XComponentUtil.getx(xc, "listOfA");
			assertEq(3, list.size());
			assertEq(1, ((List) XComponentUtil.getx((XComponent) list.get(0), "listOfC")).size());
			assertEq(1, ((List) XComponentUtil.getx((XComponent) list.get(0), "listOfB")).size());
			xml = "<Y><A V='2'/>xx<B/></Y>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			el = xc.toXml();
			xc = parseXC(xp, "X", el, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(el, xc.toXml());
			xml = "<Y><A V='2'/>xx<B/></Y>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			el = xc.toXml();
			xc = parseXC(xp, "X", el, null, null);
			assertEq(el, xc.toXml());
			assertEq("abc", XComponentUtil.get(xc, "XX"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Test><Operation One='prvni' Two='druhy' x='X'/></Test>";
			xc = parseXC(xp, "Y01", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			o = XComponentUtil.get(xc, "Operation");
			assertEq("prvni", XComponentUtil.get((XComponent) o, "One"));
			XComponentUtil.set((XComponent) o, "One", "first");
			assertEq("first", XComponentUtil.get((XComponent) o, "One"));
			assertEq("X", XComponentUtil.get((XComponent) o, "x"));
			XComponentUtil.set((XComponent) o, "x", "Y");
			assertEq("Y", XComponentUtil.get((XComponent) o, "x"));
			xml = "<Test One='prvni' Two= 'druhy'/>";
			xc = parseXC(xp, "Y02", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("prvni", XComponentUtil.get(xc, "One"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Part><PartOne One='one'/><PartTwo One='1'/></Part>";
			xc = parseXC(xp, "Y03", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("one", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "PartOne"), "One"));
			assertEq(1, XComponentUtil.get((XComponent) XComponentUtil.get(xc, "PartTwo"), "One"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Part One='1' Two='Two'/>";
			xc = parseXC(xp, "Y04", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("One", XComponentUtil.get(xc, "Jedna"));
			assertEq("Two", XComponentUtil.get(xc, "Two"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A One='Jedna' Two='Dva'/>";
			xc = parseXC(xp, "Y05a", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("Jedna", XComponentUtil.get(xc, "One"));
			assertEq("Dva", XComponentUtil.get(xc, "Two"));
			xml = "<B One='Jedna' Two='Dva'/>";
			xc = parseXC(xp, "Y05", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("Jedna", XComponentUtil.get(xc, "One"));
			assertEq("Dva", XComponentUtil.get(xc, "Two"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B One='Jedna' Two='Dve'/></A>";
			xc = parseXC(xp, "Y06", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("<B One='Jedna' Two='Dve'/>", ((XComponent) XComponentUtil.get(xc, "Domain")).toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A Id='123'><Domain One='Jedna' Two='Dve'/></A>";
			xc = parseXC(xp, "Y08", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(124, XComponentUtil.get(xc, "IdFlow"));
			XComponentUtil.set(xc, "IdFlow", 456);
			xml = "<A Id='457'><Domain One='Jedna' Two='Dve'/></A>";
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A x='X' y='Y'><a b='x'><b/></a></A>";
			xc = parseXC(xp, "Y09", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("<a b='x'><b/></a>", ((XComponent) XComponentUtil.get(xc, "$any")).toXml());
			assertEq("<a b='x'><b/></a>",
				((XComponent) XComponentUtil.get(xc, "$any")).toXml().getOwnerDocument().getDocumentElement());
			assertEq("/A", xc.xGetXPos());
			assertEq("/A/a[1]", ((XComponent) XComponentUtil.get(xc, "$any")).xGetXPos());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A a='A' b='B' c='XX'><a x='x'/><b x='xx'/></A>";
			xc = parseXC(xp, "Y10", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("x",XComponentUtil.get((XComponent) XComponentUtil.get(xc, "p"), "x"));
			assertEq("xx",XComponentUtil.get((XComponent) XComponentUtil.get(xc, "q"), "x"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<A><B N='p' I='P'>" +
"<Ev N='J' M='O_'><Co C='M' I='No'><X/></Co><Y Y='X'/></Ev>" +
"<Ev N='N' M='B_'><Co C='B' I='No'><X/></Co><Y Y='Y'/></Ev>" +
"<Op N='D' M='D_'><Co C='M' I='Yes'><X/></Co>"+
"<Co C='B' I='Yes'><X/></Co><Y Y='Z'/></Op>" +
"</B></A>";
			xc = parseXC(xp, "Y11", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			list = (List) XComponentUtil.getx(xc, "listOfB");
			list = (List) XComponentUtil.getx((XComponent) list.get(0), "listOfOp");
			assertEq("D", XComponentUtil.get((XComponent) list.get(0), "N"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B><P O='oo'/><S V='s'/><Q O='q'/><P O='o'/></B></A>";
			xc = parseXC(xp, "Y12", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			xml = "<a><b/><c/>1<b/><c/>2</a>";
			xc = parseXC(xp, "Y12", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			try {
				Class.forName(_package+".component.Y13$A$B");
				fail("Error Y13: class "+_package+".component.Y13.A.B was generated.");
			} catch (ClassNotFoundException ex) {}
			xml = "<A><B a='1'/></A>";
			xc = parseXC(xp, "Y13", xml,null,null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "a"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<C><X a='1'/></C>";
			xc = parseXC(xp, "Y14", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "X"), "a"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<a>1<b/>2</a>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get(xc, "$value"));
			assertEq("2", XComponentUtil.get(xc, "$value1"));
			assertEq("/a/$text", XComponentUtil.getx(xc,"xposOf$value1"));
			xml = "<a>1</a>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get(xc, "$value"));
			assertNull(XComponentUtil.get(xc, "$value1"));
			assertEq("/a/$text", XComponentUtil.getx(xc, "xposOf$value"));
			assertEq("/a/$text", XComponentUtil.getx(xc,"xposOf$value1"));
			xml = "<b>1</b>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			assertTrue(reporter.errorWarnings());
			reporter.clear();
			if (xc != null) {
				fail("XComponent shlould be null");
			}
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml = "<A><B><B_1><C><B b='1'/></C></B_1></B></A>";
			xc = parseXC(xp, "Y19", xml, null, null);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(1, XComponentUtil.get((XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "B_1"), "C"), "B"), "b"));
			cls = (Class) XComponentUtil.get((XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(
					xc, "B"), "B_1"), "C"), "B"), "Class");
			s = cls.getName();
			assertTrue(s.endsWith("B_2"), s);
		} catch (Exception ex) {fail(ex);}
		try { // construction of XComponent
			xml = "<a><x:b xmlns:x='x.int' y='1'/></a>";
			Object x = SUtils.getNewInstance(_package+".component.Y16");
			Object y = SUtils.getNewInstance(_package+".component.Y16a");
			XComponentUtil.set((XComponent) y, "y", 1);
			XComponentUtil.set((XComponent) x, "x$b", y);
			xon = XonUtils.xmlToXon(KXmlUtils.parseXml(xml).getDocumentElement());
			el = ((XComponent)x).toXml();
			assertEq(xml, el);
			if (!XonUtils.xonEqual(xon, XonUtils.xmlToXon(el))) {
				fail();
			}
			x = SUtils.getNewInstance(_package+".component.Y16c");
			y = SUtils.getNewInstance(_package+".component.Y16d");
			XComponentUtil.set((XComponent) y, "y", 1);
			XComponentUtil.add((XComponent) x, "d", y); //???
			xml = "<c><d xmlns='y.int' y='1'/></c>";
			assertEq(xml, ((XComponent) x).toXml());
			x = SUtils.getNewInstance(_package+".component.Y16e");
			y = SUtils.getNewInstance(_package+".component.Y16f");
			XComponentUtil.set((XComponent) y, "y", 1);
			XComponentUtil.set((XComponent) x, "f", y);
			xml = "<e><f y='1'/></e>";
			assertEq(xml, ((XComponent) x).toXml());
		} catch (Exception ex) {fail(ex);}
		try { // construction of XComponent
			xml = "<a><b a='1'/><c/><b a='x'/></a>";
			xc = parseXC(xp, "Y17", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			xon = XonUtils.xmlToXon(KXmlUtils.parseXml(xml).getDocumentElement());
			el = xc.toXml();
			assertEq(xml, el);
			assertEq(1, XComponentUtil.get((XComponent) XComponentUtil.get(xc, "b_1"), "a"));
			assertEq("x", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "b_2"), "a"));
			if (!XonUtils.xonEqual(xon, XonUtils.xmlToXon(el))) {fail();}
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml = "<A a='a' b='b'><C c='c' d='d' e='e'>f</C></A>";
			xc = parseXC(xp, "Y18", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("e", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "C"), "e"));
			assertEq("f", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "C"), "x"));
			assertEq(xml, xc.toXml());
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "C"), "e", "x");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "C"), "x", null);
			assertEq("<A a='a' b='b'><C c='c' d='d' e='x'/></A>", xc.toXml());
		} catch (Exception ex) {fail(ex);reporter.clear();}
		try {
			xml = "<A><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></A>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "X"), "b"));
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "X"), "listOfX");
			assertEq(2, list.size());
			assertEq("2", XComponentUtil.get((XComponent) list.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) list.get(0), "listOfX");
			assertEq(1, list1.size());
			assertEq("3", XComponentUtil.get((XComponent) list1.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) list.get(1), "listOfX");
			assertEq(0, list1.size());
			assertEq("4", XComponentUtil.get((XComponent) list.get(1), "b"));
			xml = "<B><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></B>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "X"), "b"));
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "X"), "listOfX");
			assertEq(2, list.size());
			assertEq("2", XComponentUtil.get((XComponent) list.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) list.get(0), "listOfX");
			assertEq(1, list1.size());
			assertEq("3", XComponentUtil.get((XComponent) list1.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) list.get(1), "listOfX");
			assertEq(0, list1.size());
			assertEq("4", XComponentUtil.get((XComponent) list.get(1), "b"));
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml = "<C><B b='1'><Y b='2'><Y b='3'/></Y><Y b='4'/></B></C>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "B"), "b"));
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "B"), "listOfY");
			assertEq(2, list.size());
			assertEq("2", XComponentUtil.get((XComponent) list.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) list.get(0), "listOfY");
			assertEq(1, list1.size());
			assertEq("3", XComponentUtil.get((XComponent) list1.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) list.get(1), "listOfY");
			assertEq(0, list1.size());
			assertEq("4", XComponentUtil.get((XComponent) list.get(1), "b"));
			xml = "<D><Z b='1'><C><Z b='2'><C><Z b='3'><C/></Z></C></Z></C></Z></D>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq("1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "Z"), "b"));
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(
				(XComponent) XComponentUtil.get(xc, "Z"), "C"), "listOfZ");
			assertEq(1, list.size());
			assertEq("2", XComponentUtil.get((XComponent) list.get(0), "b"));
			list1 = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(
				(XComponent) (XComponent) list.get(0),"C"), "listOfZ");
			assertEq(1, list1.size());
			assertEq("3", XComponentUtil.get((XComponent) list1.get(0), "b"));
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml = "<A b='x'>z<B c='a'>x</B><B c='c'>y</B>x</A>";
			xc = parseXC(xp,"Y21", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			XComponentUtil.set(xc, "$value", null);
			o = SUtils.getObjectField(_package+".component.Y21_enum", "y");
			XComponentUtil.set(xc, "b", o);
			list = (List) XComponentUtil.getx(xc, "listOfB");
			o = SUtils.getObjectField(_package+".TestXComponents_Y21enum", "b");
			XComponentUtil.set((XComponent) list.get(1), "c", o);
			assertEq("<A b='y'><B c='a'>x</B><B c='b'>y</B>x</A>", xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try {
			xml =
"<A Creator='DK'\n" +
"   NumBlocks='Not in Xdefinition'>\n" +
"  Not in Xdefinition: <a x='y'>x<y/></a>xxx<b/>\n" +
"  <Transfer Date='Not in Xdefinition'\n" +
"    Sender='0012'\n" +
"    Recipient='Not in Xdefinition'>\n" +
"    Not in Xdefinition: <a x='y'>x<y/></a>xxx<b/>\n" +
"    <DataFiles>\n" +
"      <Directory Path='q:/Ckp-2.6/VstupTest_SK/KOOP_P1_163/'>\n" +
"        <File Name='7P19998163.ctl'/><File Name='7P19998163A.xml'/>\n" +
"      </Directory>\n" +
"    </DataFiles>\n" +
"    Not in Xdefinition: <a x='y'>x<y/></a>xxx<b/>\n" +
"  </Transfer>\n" +
" Not in Xdefinition: <a x='y'>x<y/></a>xxx<b/>\n" +
"</A>";
			el = parse(xp, "Y22", xml , reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp, "Y22", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(el, xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
		try { // test vlaue setters/getters
			xml = "<a><s k='p'>t1</s><s k='q'>t2</s></a>";
			xc = parseXC(xp,"Y23",xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			assertEq("p", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "s"), "k"));
			assertEq("t1", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "s"), "$value"));
			assertEq("q", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "s_1"), "k"));
			assertEq("t2", XComponentUtil.get((XComponent) XComponentUtil.get(xc, "s_1"), "$value"));
			xml = "<b><c>xx</c></b>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "c"), "$value", "yy");
			assertEq("<b><c>yy</c></b>", xc.toXml());
			xml = "<d><e>2019-04-01+02:00</e></d>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			assertEq(new SDatetime("2019-04-01+02:00"),
				XComponentUtil.get((XComponent) XComponentUtil.get(xc, "e"), "$value"));
			sd = new SDatetime("2019-04-02+02:00");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "e"), "$value", sd);
			assertTrue(XComponentUtil.get((XComponent) XComponentUtil.get(xc, "e"), "$value").equals(sd));
			assertTrue(new SDatetime((java.sql.Timestamp) XComponentUtil.getx(
				(XComponent) XComponentUtil.get(xc, "e"), "timestampOf$value")).equals(sd));
			assertTrue(new SDatetime((Calendar) XComponentUtil.getx(
				(XComponent) XComponentUtil.get(xc, "e"),"calendarOf$value")).equals(sd));
			assertEq("<d><e>2019-04-02+02:00</e></d>", xc.toXml());
			sd = new SDatetime("2019-04-03+02:00");
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "e"), "$value", sd);
			assertEq(sd, XComponentUtil.get((XComponent) XComponentUtil.get(xc, "e"), "$value"));
			assertEq("<d><e>2019-04-03+02:00</e></d>", xc.toXml());
			XComponentUtil.set((XComponent) XComponentUtil.get(xc, "e"), "$value", sd.getCalendar());
			assertEq(sd, XComponentUtil.get((XComponent) XComponentUtil.get(xc, "e"), "$value"));
			assertEq("<d><e>2019-04-03+02:00</e></d>", xc.toXml());
			xml = "<e>2019-04-01+02:00</e>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			sd = new SDatetime("2019-04-01+02:00");
			assertEq(sd, XComponentUtil.get(xc, "$value"));
			assertEq(xml, xc.toXml());
			sd = new SDatetime("2019-04-02+02:00");
			XComponentUtil.set(xc, "$value", sd);
			assertEq(sd, XComponentUtil.get(xc,"$value"));
			assertEq("<e>2019-04-02+02:00</e>", xc.toXml());
			xml = "<f><g>2019-04-02+02:00</g></f>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			sd = new SDatetime("2019-04-03+02:00");
			list = (List) XComponentUtil.getx(xc, "listOfg");
			o = SUtils.getNewInstance(_package+".component.XCf$g");
			XComponentUtil.set((XComponent) o, "$value", sd);
			XComponentUtil.add(xc, "g", o);
			assertEq(2, list.size());
			assertEq(sd, XComponentUtil.get((XComponent) list.get(1), "$value"));
			assertEq("<f><g>2019-04-02+02:00</g><g>2019-04-03+02:00</g></f>", xc.toXml());
			list.clear();
			assertEq("<f/>", xc.toXml());
			o = SUtils.getNewInstance(_package+".component.XCf$g");
			XComponentUtil.set((XComponent) o, "$value", sd);
			list.add(o);
			assertEq("<f><g>2019-04-03+02:00</g></f>", xc.toXml());
			list = (List) XComponentUtil.getx(xc, "listOfg");
			XComponentUtil.set((XComponent) list.get(0), "$value", new SDatetime("2019-04-01+02:00"));
			assertEq("<f><g>2019-04-01+02:00</g></f>", xc.toXml());
			list.clear();
			assertEq("<f/>", xc.toXml());
			xml = "<a><d/></a>";
			xc = parseXC(xp,"Y24", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			o = XComponentUtil.get(xc, "d");
			assertTrue(o !=null && (_package+".component.Y24$d").equals(o.getClass().getName()));
			xml = "<c><d/></c>";
			xc = parseXC(xp,"Y24", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			o = XComponentUtil.get(xc, "d");
			assertTrue(o !=null && (_package+".component.Y24$d").equals(o.getClass().getName()));
			xml = "<Y24d Y24d='Y24d'><Y24d/></Y24d>";
			assertEq(xml, parseXC(xp,"Y24",xml,null,reporter).toXml());
			assertNoErrorwarningsAndClear(reporter);
			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"decimal()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"date()\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"1987-02-01\"/>\n" +
"  </Params>\n" +
"</a>";
			xc = parseXC(xp,"Y25", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			list = (List) XComponentUtil.getx((XComponent) XComponentUtil.get(xc, "DefParams"),"listOfParam");
			assertEq("decimal()", XComponentUtil.get((XComponent) list.get(1), "Type"));
			list = (List) XComponentUtil.getx(xc, "listOfParams");
			list = (List) XComponentUtil.getx((XComponent) list.get(0), "listOfParam");
			assertEq("14.8",XComponentUtil.get((XComponent) list.get(1), "Value"));
			xml = "<C>123</C>";
			xc = parseXC(xp,"Y26", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xml = "<D><DD>123</DD><DD>456</DD></D>";
			xc = parseXC(xp,"Y26", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			list = (List) XComponentUtil.get(xc, "$DD");
			assertEq(xml, xc.toXml());
			assertEq(2, list.size());
			assertEq(123, list.get(0));
			assertEq(456, list.get(1));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
		try { // test lexicon
			xd = xp.createXDDocument("LEX");
			xml = "<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>";
			assertEq(xml, xd.xtranslate(xml, "eng", "eng", reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument("LEX");
			assertEq("<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>",
				 xd.xtranslate(xml, "eng", "ces", reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument("LEX");
			xd.setLexiconLanguage("eng");
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument("LEX");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			xd.setLexiconLanguage("ces");
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument("LEX");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			assertEq("<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>",
				xd.xtranslate(xml, "ces", "eng", reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument("LEX");
			assertEq("<S s=\"x\"><T t=\"1\"/><T t=\"2\"/><T t=\"3\"/></S>",
				xd.xtranslate(xml, "ces", "deu", reporter));
			assertNoErrorwarningsAndClear(reporter);
			// test lexicon with X-component
			xd = xp.createXDDocument("LEX");
			xd.setLexiconLanguage("ces");
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			list = (List) XComponentUtil.getx(xc, "listOfY");
			assertEq(list.size(), 3);
			assertEq(1, XComponentUtil.get((XComponent) list.get(0), "y"));
			assertEq(3, XComponentUtil.get((XComponent) list.get(2), "y"));
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
		try {
			xc = parseXC(xp, "SouborD1A", getDataDir() + "test/TestXComponent_Z.xml", null, null);
			assertEq("", chkCompoinentSerializable(xc));
			list = (List) XComponentUtil.getx(xc, "listOfZaznamPDN");
			list1 = (List) XComponentUtil.getx((XComponent) list.get(1), "listOfVozidlo");
			assertEq(2, list1.size());
			el = xc.toXml();
			xc = parseXC(xp, "SouborD1A", el, null,null);
			assertEq("", chkCompoinentSerializable(xc));
			list = (List) XComponentUtil.getx(xc, "listOfZaznamPDN");
			list1 = (List) XComponentUtil.getx((XComponent) list.get(1), "listOfVozidlo");
			assertEq(2, list1.size());
			assertEq(xc.toXml(), el);
			assertEq("", checkXPos(xc));
			assertEq("", chkCompoinentSerializable(xc));
		} catch (Exception ex) {fail(ex);}
		try { // test Serializable
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:component> %class "+_package+".Ser_1 %link #a; </xd:component>\n"+
"  <xd:json name='a'>\n" +
"    [\n" +
"      {\n" +
"        a : \"? short()\",\n" +
"        i : [],\n" +
"        Towns : [\n" +
"          \"* gps()\"\n" +
"        ],\n" +
"        j : \"? char()\"\n" +
"      },\n" +
"      \"base64Binary()\",\n" +
"      \"base64Binary()\",\n" +
"      \"base64Binary()\",\n" +
"      \"price()\",\n" +
"      \"currency()\",\n" +
"      \"* ipAddr()\"\n" +
"    ]\n" +
"  </xd:json>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			json =
"# Start of XON example\n" +
"[ #***** Array *****\n" +
"  { #***** Map *****\n" +
"    a : 1s,                          # Short\n" +
"    i:[],                            # empty array\n" +
"    Towns : [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      g(50.08, 14.42, 399, \"Prague old town\")\n" +
"    ],\n" +
"    j : c\"a\",                        # Character\n" +
"  }, /**** end of map ****/\n" +
"  b(),                               /* byte array (base64) */\n"+
"  b(AA==),                           /* byte array (base64) */\n"+
"  b(true),                           /* byte array (base64) */\n" +
"  p(123.45 CZK),                     /* price */ \n" +
"  C(USD),                            /* currency */\n" +
"  /1080:0:0:0:8:800:200C:417A,        /* inetAddr (IPv6)  */\n" +
"  /::FFFF:129.144.52.38,\n" +
"  \"127.0.0.0\"\n" +
"] /**** end of array ****/\n" +
"# End of XON example";
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
		try {
			xdef = // test IDREFS, CHKIDS, ENTITIES, NMTOKENS
"<xd:def xmlns:xd='"+_xdNS+"' root='A|B|C|D'>\n"+
"  <xd:component>\n" +
"	  %class " + _package + ".D_idrefA %link A;\n" +
"	  %class " + _package + ".D_idrefB %link B;\n" +
"	  %class " + _package + ".D_idrefC %link C;\n" +
"	  %class " + _package + ".D_idrefD %link D;\n" +
"  </xd:component>\n" +
"  <xd:declaration scope='local'>\n"+
"    uniqueSet u{t: string()};\n"+
"    uniqueSet v{t: int()};\n"+
"  </xd:declaration>\n"+
"  <A> <B xd:script='*' b='? u.t.IDREFS();' a='? u.t.ID'/> </A>\n"+
"  <B> <B xd:script='*' b='? v.t.CHKIDS();' a='? v.t.ID'/> </B>\n"+
"  <C a = 'required ENTITIES'/>\n"+
"  <D a = 'required NMTOKENS'/>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument();
			xml = "<A><B b=\"'a b' 'a''b' c\"/><B a=\"a'b\"/><B a='a b'/><B a='c'/></A>";
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xml = "<B><B a=\"1\"/><B a='2'/><B b=\"1 2\"/></B>";
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xml =
"<!DOCTYPE C [\n"+
"<!ELEMENT C ANY>\n"+
"<!ATTLIST C a CDATA #IMPLIED>\n"+
"<!ENTITY X '003-2.ent'>\n"+
"<!ENTITY Y '003-3.ent'>\n"+
"]>\n"+
"<C a = 'X Y'/>";
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xml = "<D a = 'X Y'/>";
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
		try { // the command %interface in fererence
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root=\"test\">\n" +
"<xd:component>%class "+_package+".MyTestX_OneOfb %link test</xd:component>\n"+
"  <xd:json name=\"test\">\n" +
"    { a:[ \"%oneOf: ?\",\n" +
"        \"jnull(); finally outln('null')\", \n" + // must be first
"        \"date(); finally outln('date')\", \n" +
"        \"ipAddr(); finally outln('ipAddr')\", \n" +
"        [\"%script: finally outln('[...]')\",\"*int()\"], \n" +
"        {\"%script\": \"finally outln('{ . }')\",x:\"? int()\",y:\"?string()\"},\n"+
"        \"string(); finally outln('string')\" \n" +
"      ]\n" +
"    }\n" +
"</xd:json>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			s = "{a:\"2022-04-10\"}";
			o = XonUtils.parseXON(s);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xon = xd.jparse(s, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.xonToJson(xon), o));
			assertEq("date\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("date\n", swr.toString());
			o = XComponentUtil.get(xc, "jx$"+XonNames.X_VALUE+"_1"); // jx$val_1
			XComponentUtil.set((XComponent) o, XonNames.X_VALATTR, new SDatetime("2022-04-15"));
			assertEq(new SDatetime("2022-04-15"), ((Map)xc.toXon()).get("a"));
			s = "{a:\"202.2.4.10\"}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("ipAddr\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:{x:1, y:\" ab\tcd \"}}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			assertEq(1,((Map)((Map) o).get("a")).get("x"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("{ . }\n", swr.toString());
			assertEq(" ab\tcd ",((Map)((Map) xc.toXon()).get("a")).get("y"));
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:[1,2]}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("[...]\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:{}}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("{ . }\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			s = "{a:null}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("null\n", swr.toString()); //????, however it is OK
			assertNull(((Map) o).get("a"));
			assertTrue(((Map) o).containsKey("a"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("null\n", swr.toString());
			assertNull(XComponentUtil.get(xc, "$a"));
			assertNull(((Map) xc.toXon()).get("a"));
			assertTrue(((Map) xc.toXon()).containsKey("a"));
			s = "{}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", swr.toString()); //????
			assertNull(((Map) o).get("a"));
			assertFalse(((Map) o).containsKey("a"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("", swr.toString());
			assertNull(XComponentUtil.get(xc, "$a"));
			assertNull(((Map) xc.toXon()).get("a"));
			assertFalse(((Map) xc.toXon()).containsKey("a"));
		} catch (RuntimeException ex) {fail(ex);}
		try { // the command %interface with extension
			genXComponent(xp=compile(
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"A | B\">\n" +
"  <A a = \"int()\" />\n" +
"  <B xd:script =\"ref A\" b=\"string()\" />\n" +
"<xd:component>\n" +
"%class "+_package+".L_X_1 %link #A;\n" +
"%class "+_package+".L_XX_1 %link #B;\n" +
"%interface "+_package+".L_I_1 %link #A;\n" +
"%interface "+_package+".L_II_1 extends "+_package+".L_I_1 %link #B;\n" +
"</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument("");
			xml = "<A a=\"1\" />";
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			assertEq(1, XComponentUtil.get(xc, "a")); //	assertEq(1, ((L_I_1) xc).geta());
			xml = "<B a=\"2\" b=\"c d\" />";
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
//			assertEq(2, ((L_II_1) xc).geta());
//			assertEq("c d", ((L_II_1) xc).getb());
		} catch (RuntimeException ex) {fail(ex);}
		try {// test reference to map
			xp = compile(
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"  <xd:json name=\"A\">\n" +
"    {\"A\":\n" +
"      { \"Name\": \"string()\", \"End\": [ {\"%script\": \"+; ref E#E\", \"x\": \"?; string()\"} ] }\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>\n" +
"<xd:def name='E' root='E'>\n" +
"  <xd:json name='E'> { \"E\": \"string()\", \"L\": \"? string()\" } </xd:json>\n" +
"</xd:def>\n" +
"<xd:component>\n" +
"  %class "+_package+".Matesx_A %link A#A; %interface "+_package+".Matesx_A_I %link A#A;\n" +
"  %class "+_package+".Matesx_EndS %link E#E; %interface "+_package+".Matesx_EndS_I %link E#E;\n" +
"</xd:component>\n" +
"</xd:collection>");
			genXComponent(xp);
			json =
"{\"A\":\n" +
"  { \"Name\": \"xxx\",\n" +
"    \"End\": [\n" +
"      { \"L\": \"Y\", \"E\": \"aa\", \"x\": \"xx\" }, { \"L\": \"N\", \"x\": \"xy\", \"E\": \"ab\" }\n" +
"    ]\n" +
"  }\n" +
"}";
			xd = xp.createXDDocument("A");
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
		try {
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"<A>\n" +
"  <A/>\n" +
"  <A>\n" +
"      <A/>\n" +
"      <A/>\n" +
"  </A>\n" +
"</A>\n" +
"  <xd:component>%class "+_package+".Mates1_A %link A;</xd:component>\n" +
"</xd:def>"));
			parse(xp, "", xml = "<A><A/><A><A/><A/></A></A>", reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xp.createXDDocument().xparseXComponent(xml, null, reporter).toXml());
			assertNoErrorsAndClear(reporter);
			xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"{ \"A\": [\"occurs 0..* string();\"],\n" +
"  \"B\": [\n" +
"    { \"C\": [\"occurs 0..* string();\"], \"D\": [\"occurs 0..* string();\"] }\n" +
"  ],\n" +
"}\n" +
"  </xd:json>\n" +
"  <xd:component> %class "+_package+".XCA %link A;</xd:component>\n" +
"</xd:def>");
			json = "{ \"A\": [], \"B\": [ { \"C\": [\"b 2\"], \"D\": [] } ] }";
			o = jparse(xp, "", json, reporter);
			assertNoErrorsAndClear(reporter);
			genXComponent(xp);
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
		resetTester();
	}

	/** Run test.
	 * @param args the command line arguments.
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}
