package test.xdef;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDEmailAddr;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDTelephone;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.xon.XonUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.CsvReader;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.chkCompoinentSerializable;

/** Test XON/JSON/INI/Properties/CSV data. */
public class TestXon extends XDTester {

	public TestXon() {super();}

	/** Test validation method and correct result in quoted/unquoted values.
	 * @param typ validation method.
	 */
	private ArrayReporter test(final String typ) {
		String json;
		XDDocument xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:declaration scope='local'> BNFGrammar base = new BNFGrammar('a::=[0-9a-zA-Z]+'); </xd:declaration>\n" +
"  <xd:json name='A'>\n" +
"   { \"a\": \"" + typ + "\" }\n" +
"  </xd:json>\n" +
"</xd:def>").createXDDocument();
		json = "{\"a\":\"unKNOWn\"}";
		ArrayReporter reporter = new ArrayReporter();
		String s = XonUtils.toJsonString(xd.jparse(json, reporter));
		if (!json.equals(s) || reporter.errors()) {
			reporter.add(Report.error("E", s));
		}
		json = "{\"a\":\"0\"}";
		ArrayReporter reporter2 = new ArrayReporter();
		s = XonUtils.toJsonString(xd.jparse(json, reporter2));
		if (reporter2.errors()) {
			Report r;
			while ((r = reporter2.getReport()) != null) {
				reporter.add(r);
			}
		} else if (!json.equals(s)) {
			reporter2.add(Report.error("E", s));
		}
		return reporter;
	}

	/** Run all tests. */
	@Override
	public void test() {
		if (!_xdNS.startsWith("http://www.xdef.org/xdef/4.")) {
			return; // do not test versions 2.0, 2.1, and 3.1
		}
		String s, ini, json, xon, xml;
		List list;
		Object o, x, y;
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XComponent xc;
		StringWriter swr;
		Map<String, Object> xi;
		String oldCodes = getProperty(XDConstants.XDPROPERTY_STRING_CODES);
		setProperty(XDConstants.XDPROPERTY_STRING_CODES, "");
		try {
			// Array
			assertNull(testA("byte", "[null, 1b ]"));
			assertNull(testA("short", "[null, 1s ]"));
			assertNull(testA("int", "[null, 1i ]"));
			assertNull(testA("long", "[null, 1 ]"));
			assertNull(testA("integer", "[null, 0N, -3N ]"));
			assertNull(testA("float", "[null, 1.0f ]"));
			assertNull(testA("double", "[null, 1.0 ]"));
			assertNull(testA("decimal", "[null, 0D, 1D, -1D, 1.5D,3.33e-5D ]"));
			assertNull(testA("date", "[null, d2021-01-12, d1999-01-05+01:01, d1998-12-21Z ]"));
			assertNull(testA("gYear", "[null,  d2021+01:00, d1999, d-0012Z ]"));
			assertNull(testA("gps", "[null,g(20.21,19.99), g(20.2,19.9,0), g(51.52, -0.09,0.0, xxx)]"));
			assertNull(testA("price", "[null, p(20.21 CZK), p(19.99 USD) ]"));
			assertNull(testA("char","[null, c\"a\", c\"'\", c\"\\\"\", c\"\\u0007\", c\"\\\\\" ]"));
			assertNull(testA("anyURI", "[null, u\"http://a.b\" ]"));
			assertNull(testA("emailAddr", "[null, e\"tro@volny.cz\", e\"a b<x@y.zz>\" ]"));
			assertNull(testA("file", "[null, \"temp/a.txt\" ]"));
			assertNull(testA("ipAddr", "[null, /::FFFF:129.144.52.38, /0.0.0]"));
			assertNull(testA("currency", "[null, C(USD), C(CZK)]"));
			assertNull(testA("telephone", "[null, t\"123456\",t\"+420 234 567 890\"]"));
			assertNull(testA("num", "[null, \"1\", \"0123456\"]"));
			assertNull(testA("empty", "[null, \"\"]"));
			assertNull(testA("jnull", "[ null, null ]"));
			assertNull(testA("jboolean", "[ null, true ]"));
			assertNull(testA("jnumber", "[ null, 1 ]"));
			assertNull(testA("jstring", "[ null, \"abc\", \"\" ]"));
			assertNull(testA("jvalue", "[ null, true, 1, \"abc\" ]"));
			assertNull(testA("jstring", "[null, \"1\", \"true\", \"null\", \"\", \"a b\", \" a \nb \"]"));
			assertNull(testX("<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='A'><xd:json name='A'>"+
"[\"* eq('ab')\"]</xd:json><xd:component>%class test.TestGJeq %link #A;</xd:component></xd:def>",
				"", "[ null, \"ab\" ]"));
			// Map
			assertNull(testM("string", "{}"));
			assertNull(testM("int", "{a:null, b:1}"));
			assertNull(testM("jvalue", "{a:true, b:null, c:\"a\\\"b\"}"));
		} catch (Exception ex) {fail(ex);}
		try {
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' name='M' root='y:X' xmlns:y='a.b'>\n" +
"  <xd:declaration>type gam xdatetime('yyyyMMddHHmmssSS');</xd:declaration>\n" +
"  <y:X a = '?date()' t='gam();' >? int() <y:Y xd:script='*'/>dec()</y:X>\n" +
"  <xd:component>%class " + _package + ".MGam %link y:X</xd:component>\n" +
"</xd:def>"));
			xml = "<n:X xmlns:n='a.b' a='2021-12-30' t='2020121101010101'>1<n:Y/><n:Y/>2.0</n:X>";
			xc = parseXC(xp,"M", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
			o = XonUtils.parseXON(
"{\"n:X\" : [\n" +
"    { a : d2021-12-30,\n" +
"      t : d2020-12-11T01:01:01.01,\n" +
"      \"xmlns:n\" : \"a.b\"\n" +
"    },\n" +
"    1i,\n" +
"    {\"n:Y\" : []},\n" +
"    {\"n:Y\" : []},\n" +
"    2.0D\n" +
"  ]\n" +
"}");
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' name=\"X\" root=\"a\">\n" +
"  <xd:component>%class " + _package + ".Csvxx %link a</xd:component>\n" +
" <xd:json name='a'>\n" +
"    [ [ \"%script: +\", \"int\", \"int\", \"string()\", \"boolean()\"] ]\n" +
" </xd:json>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json =
"[\n" +
"  [1, 2, \"a\", true],\n" +
"  [null, 1, \"a\t\n\\\"b\", false],\n" +
"  [6, null, null, true],\n" +
"  [null, null, null, null]\n" +
"]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(o, x = xc.toXon())) {
				fail(XonUtils.toXonString(o, true)
					+ "\n*****\n" + XonUtils.toXonString(x, true));
			}
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='A'>\n" +
"  <xd:json name='A'>\n" +
"    [\"hexBinary()\",\"base64Binary()\",\"hexBinary()\",\"base64Binary()\"]\n"+
"  </xd:json>\n" +
" <xd:component>%class "+_package+".MyTestX_Hexb64 %link #A;</xd:component>\n" +
"</xd:def>"));
			xon = "[ x(0FAE99), b(D66Z), x(), b() ]";
			o = XonUtils.parseXON(xon);
			y = jparse(xp, "", xon, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, y)) {
				fail(XonUtils.xonDiff(o, y)
					+ "\n***\n" + XonUtils.toXonString(y, true)
					+ "\n***\n" + XonUtils.toXonString(o, true));
			}
			xc = xp.createXDDocument().jparseXComponent(xon, null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, y=xc.toXon())) {
				fail(XonUtils.xonDiff(o, y)
					+ "\n***\n" + XonUtils.toXonString(y, true)
					+ "\n***\n" + XonUtils.toXonString(o, true));
			}
			el = xc.toXml();
			s = XonUtils.toXonString(XonUtils.xmlToXon(el), true);
			y = jparse(xp, "", s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, y)) {
				fail(XonUtils.xonDiff(o, y)
					+ "\n***\n" + KXmlUtils.nodeToString(el, true)
					+ "\n***\n" + XonUtils.toXonString(y, true)
					+ "\n***\n" + XonUtils.toXonString(o, true));
			}
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, y =xc.toXon())) {
				fail(XonUtils.xonDiff(o, y)
					+ "\n***\n" + XonUtils.toXonString(y, true)
					+ "\n***\n" + XonUtils.toXonString(o, true));
			}
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:json name='A'>\n" +
"    [\n" +
"      {\n" +
"        a : \"? short()\",\n" +
"        b : \"? jstring()\",\n" +
"        c : \"? double()\",\n" +
"        f : \"? boolean()\",\n" +
"        g : \"? duration()\",\n" +
"        h : \"? jnull()\",\n" +	/* null */
"        i : [],\n" +
"        Towns : [\n" +
"          \"* gps()\"\n" +
"        ],\n" +
"        j : \"? char()\",\n" +
"        k : \"? char()\",\n" +
"        l : \"? char()\",\n" +
"        m : \"? char()\",\n" +		/*char '\u0007' */
"        n : \"? char()\",\n" +
"        o : \"? char()\",\n" +
"        p : \"? char()\",\n" +
"        q : \"? char()\",\n" +
"        r : \"? char()\",\n" +  	/*char null */
"        t : \"? gYear()\",\n" +
"        u : \"? gYear()\",\n" +
"        v : \"? gYear()\",\n" +
"        w : \"? gYear()\",\n" +
"        \" name with space \": \"? jstring()\"\n" +
"      },\n" +
"      \"jnull()\",\n" +
"      \"float()\",\n" +
"      \"float()\",\n" +
"      \"decimal()\",\n" +
"      \"byte()\",\n" +
"      \"integer()\",\n" +
"      \"integer()\",\n" +
"      \"date()\",\n" +
"      \"gMonth()\",\n" +
"      \"gMonth()\",\n" +
"      \"gMonthDay()\",\n" +
"      \"gMonthDay()\",\n" +
"      \"time()\",\n" +
"      \"time()\",\n" +
"      \"time()\",\n" +
"      \"gYearMonth()\",\n" +
"      \"gYearMonth()\",\n" +
"      \"gYearMonth\",\n" +
"      \"dateTime()\",\n" +
"      \"gps()\",\n" +
"      \"base64Binary()\",\n" +
"      \"base64Binary()\",\n" +
"      \"price()\",\n" +
"      \"price()\",\n" +
"      \"currency()\",\n" +
"      \"ipAddr()\",\n" +
"      \"ipAddr()\"\n" +
"    ]\n" +
"  </xd:json>\n" +
"  <xd:component> %class "+_package+".Xon %link #A; </xd:component>\n" +
"</xd:def>"));
			xon =
"#**** Start of XON example ****\n" +
"[                                    # Array\n" +
"  {                                  # Map\n" +
"    a : 1s,                          # Short\n" +
"    b : \"ab cd\",                   # String\n" +
"    c : -123d,                       # Double\n" +
"    f:true,                          # Boolean\n" +
"    g : P1Y1M1DT1H1M1.12S,           # Duration\n" +
"    h : null,                        # null\n" +
"    i:[],                            # empty array\n" +
"    Towns : [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      null,\n" +
"      g(50.08, 14.42, 399, \"Prague old town\")\n" +
"    ],\n" +
"    j : c\"a\",                      # Character\n" +
"    k : c\"'\",                      # Character\n" +
"    l : c\"\\\"\",                   # Character\n" +
"    m : c\"\\u0007\",                # Character\n" +
"    n : c\"\\\\\",                   # Character\n" +
"    o : c\"\n\",                     # Character\n" +
"    p : c\"\\n\",                    # Character\n" +
"    q : c\" \",                      # Character\n" +
"    r : null,                        # Character (null)\n" +
"    t : d0001,                       # year (without zone)\n" +
"    u : d-0001,                      # year (without zone)\n" +
"    v : d123456789Z,                 # year zone\n" +
"    w : d-0001-01:00,                # year zone\n" +
"    \" name with space \": \"x\\ty\" # name with space is quoted!\n" +
"  },  #**** end of map ****\n" +
"  null,                              # null\n" +
"  3f,                                # Float\n" +
"  null,                              # null\n" +
"  -3.1d,                             # BigDecimal\n" +
"  -2b,                               # Byte\n" +
"  1N,                                # BigInteger\n" +
"  999999999999999999999999999999999, # big integer (authomatic)\n" +
"  d2021-01-11,                       # date\n" +
"  d--11,                             # month\n" +
"  d--02Z,                            # month zone\n" +
"  d--11-22,                          # month day\n" +
"  d--03-04-01:01,                    # month day zone\n" +
"  d19:23:01,                         # hours, minutes seconds\n" +
"  d19:23:01.012,                     # hours minutes seconds millis\n" +
"  d00:00:00.00001+00:00,             # time nanos zone\n" +
"  d2000-11Z,                         # year month zone\n" +
"  d2000-10-01:00,                    # year month zone\n" +
"  d2000-10,                          # year month; no zone\n" +
"  d2021-01-12T01:10:11.54012-00:01,  # date and time (nanos, zone)\n" +
"  g(-0, +1),                         # GPS\n" +
"  b(D66Z),                           # byte array (base64)\n" +
"  b(),                               # byte array (base64), length = 0\n" +
"  p(123.45 CZK),                     # price\n" +
"  p(12 USD),                         # price\n" +
"  C(USD),                            # currency\n" +
"  /129.144.52.38,                    # inetAddr (IPv4)\n" +
"  /1080:0:0:0:8:800:200C:417A,       # inetAddr (IPv6)\n" +
"] #*** end of array ****\n" +
"#**** End of XON example ****";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toXonString(x, true);
			y = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x,y));
			list = (List) ((Map) ((List) y).get(0)).get("Towns");
			assertEq("Wien",((GPSPosition) list.get(0)).name());
			assertEq("London",((GPSPosition) list.get(1)).name());
			assertEq("Prague old town",((GPSPosition) list.get(3)).name());
			assertEq(1233, Math.round(
				((GPSPosition) list.get(0)).distanceTo(((GPSPosition)list.get(1)))/1000));
			assertEq(252,Math.round(((GPSPosition)list.get(0)).distanceTo(((GPSPosition) list.get(3)))/1000));
			assertEq(1030,Math.round(((GPSPosition)list.get(1)).distanceTo(((GPSPosition)list.get(3)))/1000));
			assertNoErrorwarningsAndClear(reporter);
			json = XonUtils.toXonString(x, true);
			XonUtils.parseXON(json);
			y = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(xc.toXon(),y));
			el = xc.toXml();
			s = XonUtils.toXonString(XonUtils.xmlToXon(el), true);
			o = jparse(xp, "", s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, y)) {
				fail(XonUtils.xonDiff(XonUtils.xonToJson(o), XonUtils.xonToJson(y)));
				fail(KXmlUtils.nodeToString(el, true) + "\n***\n" + XonUtils.toXonString(y, true)
					+ "\n***\n" + XonUtils.toXonString(o, true));
			}
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' name=\"X\" root=\"a\">\n" +
"  <xd:component>%class " + _package + ".Xona %link a</xd:component>\n" +
"  <xd:json name='a'>\n" +
"    [\n" +
"      [ \"%script: optional\", \"boolean();\", \"optional int();\" ]\n" +
"    ]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json = "[ [ true, 123 ] ]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(o, (x = xc.toXon()))) {
				fail(XonUtils.toXonString(o) + "\n***\n" + XonUtils.toXonString(x));
			}
			json = "[\n]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(o, (x = xc.toXon()))) {
				fail(XonUtils.toXonString(o) + "\n***\n" + XonUtils.toXonString(x));
			}
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n" +
"  <xd:component>%class " + _package + ".Xonb %link X</xd:component>\n" +
"  <xd:json name=\"X\">\n" +
"[\n" +
"  [\"fixed 'Name'\",\"fixed 'Email'\",\"fixed 'Mobile Number'\"],\n" +
"  [\"%script:+\",\n" +
"    \"string()\",\n" +
"    \"union(%item=[emailAddr(), jnull])\",\n" +
"    \"union(%item=[telephone(), jnull])\"\n" +
"  ]\n" +
"]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			s =
"[\n" +
" [\"Name\", \"Email\", \"Mobile Number\"],\n" +
" [\"Hel \\\"\\\"Ova\",\"hka@vol.cz\",\"+420 123 345 678\"],\n" +
" [\"Eva Kuž, Epor \\\"Prix\\\"\", \"ep@ema.cz\", null],\n" +
" [\"Jivá\", null, null]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xd.getXDPool());
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(o, x = xc.toXon()));
			list = (List) ((List) x).get(0);
			assertEq("Name", list.get(0));
			list = (List) ((List) x).get(1);
			assertEq("Hel \"\"Ova", list.get(0));
			assertTrue(list.get(1) instanceof XDEmailAddr);
			assertEq("hka@vol.cz", list.get(1).toString());
			assertTrue(list.get(2) instanceof XDTelephone);
			assertEq("+420 123 345 678", list.get(2).toString());
			list = (List) ((List) x).get(2);
			assertNull(list.get(2));
			list = (List) ((List) x).get(3);
			assertNull(list.get(1));
			assertNull(list.get(2));
			xd = compile( // test Windows INI
"<xd:def xmlns:xd='"+_xdNS+"' root='test' name='A'>\n" +
"  <xd:ini xd:name = \"test\">\n" +
"    name = string();\n" +
"    date = date();\n" +
"    email = emailAddr();\n" +
"    [Server]\n" +
"      IPAddr = ipAddr();\n" +
"  </xd:ini>\n" +
"</xd:def>").createXDDocument("A");
			ini =
"date = 2021-02-03\n" +
"name = Jan Novak\n" +
"email = a@b.c\n" +
"[Server]\n" +
" IPAddr = 255.0.0.0\n";
			xi = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),XonUtils.parseINI(XonUtils.toIniString(xi))));
			xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"#this is INI file comment\n" +
"address=string(); options noTrimAttr\n" +
"dns = ipAddr()\n"  +
"name = string()\n"+
"parser.factor.1=string()\n" +
"servertool.up=string()\n" +
"  </xd:ini>\n" +
"</xd:def>").createXDDocument("A");
			ini =
"#this is INI file comment\n" +
"address=dhcp\1\n" +
"dns = 192.168.1.1\n"  +
"name = John E\\\n"+
" . \\\n"  +
" Smith\n"  +
"  parser.factor.1=')' \\u00E9 esperado.\n" +
"servertool.up=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
			xi = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),XonUtils.parseINI(XonUtils.toIniString(xi))));
			xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type=int(0,9)\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" + //
"port= ? int(0, 9999);\n" +
"[system] optional\n" +
"autolaunch=int()\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>").createXDDocument("A");
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"hostaddr= 123.45.6.7\n" +
"port= 0\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xi = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),XonUtils.parseINI(XonUtils.toIniString(xi))));
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xi = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),XonUtils.parseINI(XonUtils.toIniString(xi))));
			ini =
"proxy type=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xi = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),XonUtils.parseINI(XonUtils.toIniString(xi))));
			xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root=\"TRSconfig\">\n" +
"  <xd:ini xd:name=\"TRSconfig\">\n" +
"    TRSUser = string()\n" +
"    [User]\n" +
"      Home = file()\n" +
"      Authority=enum(\"SECURITY\",\"SOFTWARE\",\"CLIENT\",\"UNREGISTRED\")\n" +
"      ItemSize = int(10000, 15000000)\n" +
"      ReceiverSleep = int(1, 3600)\n" +
"    [Server] optional\n" +
"      RemoteServerURL = url()\n" +
"      SeverIP = ipAddr()\n" +
"      SendMailHost = domainAddr()\n" +
"      MailAddr = emailAddr()\n" +
"      Signature = SHA1()\n" +
"  </xd:ini>\n" +
"</xd:def>").createXDDocument();
			ini =
"############# TRS configuration #############\n" +
"# TRS user name\n" +
"TRSUser = John Smith\n" +
"[User]\n" +
"# user directory\n" +
"Home = D:/TRS_Client/usr/Smith\n" +
"# authority(SECURITY | SOFTWARE | CLIENT | UNREGISTRED)\n" +
"Authority=CLIENT\n" +
"# Maximal item size (10000 .. 15000000)\n" +
"ItemSize=4000000\n" +
"# Receiver sleep time in seconds (1 .. 3600).\n" +
"ReceiverSleep=1\n" +
"[Server]\n" +
"# Remote server\n" +
"RemoteServerURL=http://localhost:8080/TRS/TRSServer\n" +
"SeverIP = 123.45.67.8\n" +
"SendMailHost = smtp.synth.cz\n" +
"MailAddr = jira@synth.cz\n" +
"Signature = 12afe0c1d246895a990ab2dd13ce684f012b339c\n";
			xi = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xi, xd.iparse(XonUtils.toIniString(xi), reporter)));
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp = compile( //test CSV data with head line (column names)
"<xd:def xmlns:xd='"+_xdNS+"' root='CSV'>\n" +
"  <xd:component>%class " + _package + ".CsvTest %link CSV</xd:component>\n" +
"  <xd:json name=\"CSV\">\n" +
"[\n" +
"  [\"3 string();\"],\n" + // head
"  [\"%script:+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n" +
"]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			s =
"Name, Email, Mobile Number\n" +
"abc, a@b.c, +1 2345 67 89 01\n" +
"\n" +
"xyz, d@e.f,\n" +
"xyz,,\n" +
",,\n" +
"xyz, , 123 456 789";
			x = xd.cparse(new StringReader(s), null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"Name | Email | Mobile Number\n" +
"abc | a@b.c | +1 2345 67 89 01\n" +
"\n" +
"xyz | d@e.f |\n" +
"xyz | |\n" +
" | |\n" +
"xyz | | 123 456 789";
			o = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n" +
" [\"Name\",\"Email\",\"Mobile Number\"],\n" +
" [\"abc\", e\"a@b.c\", \"+1 2345 67 89 01\"],\n" +
" [],\n" +
" [\"xyz\", e\"d@e.f\",null],\n" +
" [\"xyz\", null, null],\n" +
" [null, null, null],\n" +
" [\"xyz\", null, \"123 456 789\"]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n"
					+ "*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root = \"test\">\n" +
"  <xd:json name = \"test\">\n" +
"[\n" +
"  [\"occurs 2.. string();\"], # header line\n" +
"  # CSV lines:\n" +
"  [ \"%script: +\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n" +
"]\n" +
"  </xd:json>\n" +
"</xd:def>").createXDDocument();
			s =
"Name, Email, Mobile Number\n" +
"John Smith, john.smith@smith.com, +1 2345 67 89 01\n" +
", nobody@somewhere.org, +000 987 654 321\n" +
"aaa, ,\n" +
", , +090 98 76 54 12\n" +
"";
			o = xd.cparse(new StringReader(s), ',', true, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(4, ((List) o).size());
			assertEq("aaa", ((List) ((List) o).get(2)).get(0));
			assertEq(null, ((List) ((List) o).get(2)).get(1));
			assertEq(null, ((List) ((List) o).get(2)).get(2));
			assertEq(3, ((List) ((List) o).get(2)).size());
			genXComponent(xp = compile( // no CSV head line with bames;
"<xd:def xmlns:xd='"+_xdNS+"' root='CSV'>\n" +
"  <xd:component>%class " + _package + ".CsvTest1 %link CSV</xd:component>\n" +
"  <xd:json name=\"CSV\">\n" +
"[\n"+
"  [\"%script: +\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n" +
"]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			s =
"Name, Email, Mobile Number\n" +
"abc, a@b.c, +1 2345 67 89 01\n" +
"xyz, d@e.f,\n" +
"xyz,,\n" +
",,\n" +
"xyz, , 123 456 789\n";
			o = xd.cparse(new StringReader(s), ',', true, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"abc | a@b.c | +1 2345 67 89 01\n" +
"xyz | d@e.f |\n" +
"xyz | |\n" +
" | |\n" +
"xyz | | 123 456 789\n";
			x = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n"+
" [\"abc\", e\"a@b.c\", \"+1 2345 67 89 01\"],\n" +
" [\"xyz\", e\"d@e.f\",null],\n" +
" [\"xyz\", null, null],\n" +
" [null, null, null],\n" +
" [\"xyz\", null, \"123 456 789\"]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n" + "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			genXComponent(xp = compile( // test %oneOf
"<xd:def xmlns:xd='"+_xdNS+"' root=\"test\">\n" +
"  <xd:component>%class "+_package+".MyTestX_OneOf %link test</xd:component>\n"+
"  <xd:json name=\"test\">\n" +
"{ a:[ \"%oneOf\",\n" +
"       \"date(); finally outln('date')\", \n" +
"       \"ipAddr(); finally outln('ipAddr')\", \n" +
"       [\"%script:finally outln('[...]')\",\"*int()\"], \n" +
"       \"string(); finally outln('string')\" \n" +
"  ]\n" +
"}\n" +
"  </xd:json>\n" +
"</xd:def>"));
			s = "{a:\"2022-04-10\"}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("date\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("date\n", swr.toString());
			assertEq(o, xc.toXon());
			s = "{a:\"202.204.1.0\"}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("ipAddr\n", swr.toString());
			assertEq(o, xc.toXon());
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
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("[...]\n", swr.toString());
			assertEq(o, xc.toXon());
			s = "{a:\"a\tb\n\"}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("string\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq("string\n", swr.toString());
			assertEq(o, xc.toXon());
			genXComponent(xp = compile( // test forget in XON
"<xd:def xmlns:xd='"+_xdNS+"' root=\"test\">\n" +
"  <xd:component>\n" +
"    %class "+_package+".data.TestXonForget %link test\n"+
"  </xd:component>\n" +
"  <xd:json name=\"test\">\n" +
"    {date: \"date()\",\n" +
"      cities: [\n" +
"        { \"%script\": \"occurs 1..*; finally outln(); forget\",\n" +
"          \"from\": [\n" +
"            \"string(); finally out('From ' + getText());\",\n" +
"            { \"%script\": \"occurs 1..*;\",\n" +
"              \"to\": \"jstring();finally out(' to '+getText()+' is ');\",\n" +
"              \"distance\": \"int(); finally out(getText() + ' km');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }"+
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>"));
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
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			x = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(swr.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(swr.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertTrue(((List)((Map)(x = xc.toXon())).get("cities")).isEmpty());
			assertEq(((Map) x).get("date"), new SDatetime("2020-02-22"));
			genXComponent(xp = compile( // CSV with head
"<xd:def xmlns:xd='"+_xdNS+"' root='CSV'>\n" +
"  <xd:component>%class "+_package+".CsvTest %link CSV</xd:component>\n" +
"  <xd:json name=\"CSV\">\n" +
"[\n" +
"  [\"3..3 string();\"],\n" + // head
"  [\"%script :+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n" +
"]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			s =
"Name, Email, Mobile Number\n" +
"abc, a@b.c, +1 2345 67 89 01\n" +
"\n" +
"xyz, d@e.f,\n" +
"xyz,,\n" +
",,\n" +
"xyz, , 123 456 789";
			xd = xp.createXDDocument();
			x = xd.cparse(new StringReader(s), null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"Name | Email | Mobile Number\n" +
"abc | a@b.c | +1 2345 67 89 01\n" +
"\n" +
"xyz | d@e.f |\n" +
"xyz | |\n" +
" | |\n" +
"xyz | | 123 456 789";
			o = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n" +
" [\"Name\",\"Email\",\"Mobile Number\"],\n" +
" [\"abc\", e\"a@b.c\", \"+1 2345 67 89 01\"],\n" +
" [],\n" +
" [\"xyz\", e\"d@e.f\",null],\n" +
" [\"xyz\", null, null],\n" +
" [null, null, null],\n" +
" [\"xyz\", null, \"123 456 789\"]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n" + "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			genXComponent(xp = compile( // no head;
"<xd:def xmlns:xd='"+_xdNS+"' root='CSV'>\n" +
"  <xd:component>%class "+_package+".CsvTest1 %link CSV</xd:component>\n" +
"  <xd:json name=\"CSV\">\n" +
"    [\n" +
"      [\"%script:+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n" +
"    ]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			s =
"Name, Email, Mobile Number\n" +
"abc, a@b.c, +1 2345 67 89 01\n" +
"xyz, d@e.f,\n" +
"xyz,,\n" +
",,\n" +
"xyz, , 123 456 789\n";
			xd = xp.createXDDocument();
			o = xd.cparse(new StringReader(s), ',', true, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"abc | a@b.c | +1 2345 67 89 01\n" +
"xyz | d@e.f |\n" +
"xyz | |\n" +
" | |\n" +
"xyz | | 123 456 789\n";
			x = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n" +
" [\"abc\", e\"a@b.c\", \"+1 2345 67 89 01\"],\n" +
" [\"xyz\", e\"d@e.f\",null],\n" +
" [\"xyz\", null, null],\n" +
" [null, null, null],\n" +
" [\"xyz\", null, \"123 456 789\"]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x) + "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n" + "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			genXComponent(xp = compile( // test forget
"<xd:def xmlns:xd='"+_xdNS+"' root=\"test\">\n" +
"  <xd:component>\n" +
"    %class "+_package+".data.TestXonForget %link test\n" +
"  </xd:component>\n"+
"  <xd:json name=\"test\">\n" +
"    {date: \"date()\",\n" +
"      cities: [\n" +
"        { \"%script\": \"occurs 1..*; finally outln(); forget\",\n" +
"          \"from\": [\n" +
"            \"string(); finally out('From ' + getText());\",\n" +
"            { \"%script\": \"occurs 1..*;\",\n" +
"              \"to\": \"jstring();finally out(' to '+getText()+' is ');\",\n" +
"              \"distance\": \"int(); finally out(getText() + ' km');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }"+
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>"));
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
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			x = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(swr.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(swr.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertTrue(((List)((Map)(x = xc.toXon())).get("cities")).isEmpty());
			assertEq(((Map) x).get("date"), new SDatetime("2020-02-22"));
			genXComponent(xp = compile( // declaration of "%anyName" item in map
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'> { \"\\u0025anyName\": \"string();\" } </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_0_ %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			xd.jvalidate("{ \"%anyName\": \"xxx\" }", reporter); // item accepted
			assertNoErrorsAndClear(reporter);
			xd.jvalidate("{ \"x\": \"xxx\" }", reporter);  // illegal item
			assertErrorsAndClear(reporter);
			genXComponent(xp = compile( // declaration of both "%anyName" item and %anyName in map
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"    { \"%anyName\": \"string();\", \"\\u0025anyName\": \"int();\" }\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_0 %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			xd.jvalidate("{ \"%anyName\": 1, \"x\": \"x\" }", reporter);
			assertNoErrorsAndClear(reporter);
			genXComponent(xp = compile( // test %anyName
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"    { \"%anyName\": \"? int();\", x: \"? int();\" }\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_1 %link A</xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTestAny_1"; // c2lass name
			assertNull(testX(xp, "", s, "{}"));
			assertNull(testX(xp, "", s, "{\"x\" : 1, \"xxx\" : 2}"));
			genXComponent(xp = compile(// %anyName items and one named item
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"    { \"%anyName\": \"* int(); finally out('X'+getText())\", x: \"? int();\" }\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_2 %link A</xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTestAny_2"; // c2lass name
			assertNull(testX(xp, "", s, "{}", ""));
			assertNull(testX(xp, "", s, "{ a: 1, b: 2, x: 999 }", "X1X2"));
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'> { \"%anyName\": [\"* int();\"] } </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_3 %link A</xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTestAny_3"; // class name
			assertNull(testX(xp, "", s, "{ \"x\": [] }"));
			assertNull(testX(xp, "", s, "{ \"x\": [1,2i] }"));
			xd = (xp = compile( // %anyObj
"<xd:def xmlns:xd='"+_xdNS+"' root='Any'>\n" +
"  <xd:json name=\"Any\"> \"%anyObj\" </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_4 %link Any</xd:component>\n" +
"</xd:def>")).createXDDocument();
			o = xd.jparse(json = "[\n" +
"  [6, 1, \"a\t\n\\\"b\", false],\n" +
"  [6, null, null, true],\n" +
"  [null, null, null, null]\n" +
"]", reporter);
			assertNoErrors(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(XonUtils.toJsonString(o)), o));
			genXComponent(xp);
			s = _package+".MyTestAny_4"; // class name
			assertNull(testX(xp, "", s, "true"));
			assertNull(testX(xp, "", s, "1"));
			assertNull(testX(xp, "", s, "null"));
			assertNull(testX(xp, "", s, "{}"));
			assertNull(testX(xp, "", s, "[0]"));
			assertNull(testX(xp, "", s, json));
			genXComponent(xp = compile( // %anyName in map with %anyObj items
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:json name='A'> { \"%anyName\": \"%anyObj:*;\"} </xd:json>\n"+
"  <xd:component>%class "+_package+".MyTestAny_5 %link A</xd:component>\n"+
"</xd:def>"));
			s = _package+".MyTestAny_5"; // class name
			assertNull(testX(xp, "", s, "{}"));
			assertNull(testX(xp, "", s, "{\"\": {} }"));
			assertNull(testX(xp, "", s, "{ a: [] }"));
			assertNull(testX(xp, "", s, "{ \" \": [\"\",null, false] }"));
			assertNull(testX(xp, "", s, "{ p:[8d,9D],q:[],r:{},s:\"\" }"));
			genXComponent(xp = compile( //test num, eq, gYear, int in array
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_numeq %link #A;</xd:component>\n" +
"  <xd:json name='A'>\n" +
"    [ \"num()\", \"eq('2021')\", \"gYear()\", \"int(2021,2021)\" ]\n" +
"  </xd:json>\n" +
"</xd:def>"));
			xon = "[\"2021\", \"2021\", d2021, 2021]";
			s = _package+".MytestX_numeq";
			assertNull(testX(xp,"", s, xon));
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"{ \"cities\" : [\n" +
"    \"date(); finally outln('Measurements taken on: '+getText()+'\\n');\",\n" +
"    { \"%script\": \"occurs 1..*;\",\n" +
"      \"%anyName\": [\"%script: occurs 1..*;\n"+
"                init outln('Distance from ' + getXonKey() + '\nto:');\",\n" +
"        { \"%script\": \"occurs 1..*; finally outln();\",\n" +
"          \"to\" : \"jstring();finally out(' - ' + getText() + ' = ');\",\n" +
"          \"distance\" : \"int(); finally out(getText() + '(km)');\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_6 %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json =
"{ \"cities\" : [\n" +
"    \"2020-02-22\",\n" +
"    { \"Brussels\": [\n" +
"        {\"to\": \"London\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 265}\n" +
"      ]\n" +
"    },\n" +
"    { \"London\": [\n" +
"        {\"to\": \"Brussels\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 344}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}";
			x = XonUtils.parseJSON(json);
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			s =
"Measurements taken on: 2020-02-22\n" +
"\n" +
"Distance from Brussels\n" +
"to:\n" +
" - London = 322(km)\n" +
" - Paris = 265(km)\n" +
"Distance from London\n" +
"to:\n" +
" - Brussels = 322(km)\n" +
" - Paris = 344(km)\n";
			assertEq(swr.toString(), s);
			if (!XonUtils.xonEqual(x,y =  XonUtils.xonToJson(y))) {
				fail("** 1 **\n"+XonUtils.toXonString(x, true)
					+ "\n" +  XonUtils.toXonString(y, true));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, XonUtils.xonToJson(o))) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(swr.toString(), s);
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"{ \"cities\" : [\n" +
"    \"date(); finally outln('Measurements taken on: '+getText()+'\\n');\",\n"+
"    { \"%script\": \"occurs 1..*;\",\n" +
"      \"%anyName\": [\"%script: occurs 1..*;\n"+
"                init outln('Distance from ' + getXonKey() + '\nto:');\",\n" +
"        { \"%script\": \"occurs 1..*; finally outln();\",\n" +
"          \"to\" : \"jstring();finally out(' - ' + getText() + ' = ');\",\n" +
"          \"distance\" : \"int(); finally out(getText() + '(km)');\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_6 %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json =
"{ \"cities\" : [\n" +
"    \"2020-02-22\",\n" +
"    { \"Brussels\": [\n" +
"        {\"to\": \"London\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 265}\n" +
"      ]\n" +
"    },\n" +
"    { \"London\": [\n" +
"        {\"to\": \"Brussels\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 344}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}";
			x = XonUtils.parseJSON(json);
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			s =
"Measurements taken on: 2020-02-22\n" +
"\n" +
"Distance from Brussels\n" +
"to:\n" +
" - London = 322(km)\n" +
" - Paris = 265(km)\n" +
"Distance from London\n" +
"to:\n" +
" - Brussels = 322(km)\n" +
" - Paris = 344(km)\n";
			assertEq(swr.toString(), s);
			if (!XonUtils.xonEqual(x,y =  XonUtils.xonToJson(y))) {
				fail("** 1 **\n"+XonUtils.toXonString(x, true)
					+ "\n" +  XonUtils.toXonString(y, true));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, XonUtils.xonToJson(o))) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(swr.toString(), s);
			genXComponent(xp = compile( // test %anyName, %oneOf, ref
"<xd:def xmlns:xd='"+_xdNS+"' root=\"a\">\n" +
"  <xd:json name=\"a\">[ { \"%script\": \"occurs *; ref B\" } ]</xd:json>\n" +
"  <xd:json name=\"B\">\n" +
"    { \"%anyName\": [\"%oneOf\",\n" +
"        \"string()\",\n" +
"        [\"occurs *; string()\"]\n" +
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".TestAnyRef %link #a</xd:component>\n" +
"</xd:def>"));
			json =
"[ { \"A\": [\"classic\"] },\n" +
"  { \"B\": [ \"Rock\", \"pop\" ] },\n" +
"  { \"C\": \"Country\" },\n" +
"  { \"D\": [] }\n" +
"]";
			assertNull(testX(xp, "", _package+".TestAnyRef", json));
			genXComponent(xp = compile( //test anyName, oneOf
"<xd:def xmlns:xd='"+_xdNS+"' name=\"a\" root=\"test\">\n" +
"<xd:json name=\"test\">[ \"%script:ref A\" ]</xd:json>\n" +
"  <xd:json name=\"A\">\n" +
"    [\"%oneOf\",\n"+
"      \"jvalue();\",\n" +
"      [\"*; jvalue();\" ],\n" +
"      {\"%anyName\":\n" +
"        [\"%oneOf\",\n" +
"          \"jvalue();\",\n" +
"          [\"*; jvalue();\"],\n" +
"          {\"%anyName\":\n" +
"            [\"%oneOf\",\n" +
"              \"jvalue();\",\n" +
"              [\"*; jvalue();\"],\n" +
"            ]\n" +
"          }\n" +
"        ]\n" +
"      }\n" +
"    ]\n" +
"  </xd:json>\n"  +
"<xd:component>\n" +
"  %class "+_package+".TestAnyRef1 %link a#test;\n" +
"</xd:component>\n" +
"</xd:def>"));
			s = _package+".TestAnyRef1"; // class name
			assertNull(testX(xp, "a", s, "[ [] ]"));
			assertNull(testX(xp, "a", s, "[ {} ]"));
			assertNull(testX(xp, "a", s, "[ { a:1 } ]"));
			assertNull(testX(xp, "a", s, "[ { a: [1,2] } ]"));
			assertNull(testX(xp, "a", s, "[true]"));
			assertNull(testX(xp, "a", s, "[[1, true]]"));
			assertNull(testX(xp, "a",s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			genXComponent(xp = compile( // test reference to %oneOf
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
" <xd:json name=\"A\">\n" +
"[ \"?; jvalue()\",\n" +
"  [ \"%oneOf: ?; ref X\" ]\n" +
"]\n" +
" </xd:json>\n" +
" <xd:json name=\"X\">\n" +
"[\"%oneOf\",\n" +
" [\"* jvalue();\"],\n" +
" { \"a\": \"? date()\", \"b\": \"? jvalue()\" }\n" +
"]\n" +
" </xd:json>\n" +
" <xd:component> %class "+_package+".TestRef0 %link #A; </xd:component>\n" +
"</xd:def>"));
			s = _package+".TestRef0";
			assertNull(testX(xp,"",s, "[true,[0, 1]]"));
			assertNull(testX(xp,"",s,"[{a:d1991-01-01}]"));
			assertNull(testX(xp,"",s, "[{b:1}]"));
			assertNull(testX(xp,"",s,"[{}]"));
			assertNull(testX(xp,"",s,"[null]"));
			assertNull(testX(xp,"",s,"[]"));
			assertNotNull(testX(xp,"",s,"[1,2]"));
			assertNotNull(testX(xp,"",s,"[[],[]]"));
			assertNotNull(testX(xp,"",s,"[{},{}]"));
			genXComponent(xp = compile(// %anyName, name of item is an empty string
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'> {\"%anyName\": \"? int()\"} </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_x1 %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json = "{ \"\": 1}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x,y));
			assertTrue(XonUtils.xonEqual(x, xd.getXon()));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(x, XonUtils.xonToJson(xc.toXon())));
			assertTrue(XonUtils.xonEqual(x, XComponentUtil.getMap(xc)));
			genXComponent(xp = compile( // test %anyObj
"<xd:def xmlns:xd='"+_xdNS+"' name=\"a\" root=\"testX\">\n" +
"  <xd:json name=\"testX\">%anyObj</xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".MyTestX_any %link a#testX;\n" +
"  </xd:component>\n"+
"</xd:def>"));
			s = _package+".MyTestX_any";
			// (simple value)
			assertNull(testX(xp,"a", s, "true"));
			assertNull(testX(xp,"a", s, "1"));
			assertNull(testX(xp,"a", s, "null"));
			assertNull(testX(xp,"a", s, "\"abc\""));
			// (array)
			assertNull(testX(xp,"a", s, "[]"));
			assertNull(testX(xp,"a", s, "[1]"));
			assertNull(testX(xp,"a", s, "[ [] ]"));
			assertNull(testX(xp,"a", s, "[ [\"1\",[]] ]"));
			assertNull(testX(xp,"a", s, "[ [{}] ]"));
			assertNull(testX(xp,"a", s, "[ [ { a:[1, 2]} ] ]"));//
			assertNull(testX(xp,"a", s, "[0,{a:[1,true],b:null},false,null]"));
			assertNull(testX(xp,"a", s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			// (map)
			assertNull(testX(xp,"a", s, "{}"));
			assertNull(testX(xp,"a", s, "{a:1}"));
			assertNull(testX(xp,"a", s,"{a:1,b:[],c:{},d:{e:5,f:[2]},g:null}"));
			assertNull(testX(xp,"a", s, "{a:[1, true], b:null}"));
			genXComponent(xp = compile( // test occurrence 1 for %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"  <xd:json name=\"A\"> [ \"%anyObj:occurs 1;\" ] </xd:json>\n" +
"  <xd:component>\n" +
	 "%class "+_package+".TestArrayAnyObj1 %link #A;\n" +
"  </xd:component>\n" +
"</xd:def>"));
			s = _package+".TestArrayAnyObj1";
			assertNull(testX(xp,"",s, "[true]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[[1]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1}]")); // OK
			assertNull(testX(xp,"",s, "[[1,2]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1,b:2}]")); // OK
			assertNotNull(testX(xp,"",s, "[]")); // error empty
			assertNotNull(testX(xp,"",s, "[1,2]")); // error more then one
			assertNotNull(testX(xp,"",s, "{a:1,b:2}")); // error more then one
			assertNotNull(testX(xp,"",s, "[[],[]]")); // error more then one
			assertNotNull(testX(xp,"",s, "[{},{}]")); // error more then one
			genXComponent(xp = compile( // test occurrence 2 for %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"  <xd:json name=\"A\"> [ \"%anyObj:occurs 2;\" ] </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".TestArrayAnyObj2 %link #A;\n" +
"  </xd:component>\n" +
"</xd:def>"));
			s = _package+".TestArrayAnyObj2";
			assertNull(testX(xp,"",s, "[true,null]")); // OK
			assertNull(testX(xp,"",s, "[1,3.14]")); // OK
			assertNull(testX(xp,"",s, "[[1],[]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1}, 1]")); // OK
			assertNull(testX(xp,"",s, "[[1,2],[3]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1,b:2},{a:1}]")); // OK
			assertNotNull(testX(xp,"",s, "[]")); // error empty
			assertNotNull(testX(xp,"",s, "[1]"));// error only one
			assertNotNull(testX(xp,"",s, "[[]]"));// error only one
			assertNotNull(testX(xp,"",s, "[[1,2]]"));// error only one
			assertNotNull(testX(xp,"",s, "[{}]"));// error only one
			assertNotNull(testX(xp,"",s, "[{a:1,b:2}]"));// error only one
			assertNotNull(testX(xp,"",s, "{a:1,b:2,c:3}"));//error not array
			assertNotNull(testX(xp,"",s, "true"));//error not array
			assertNotNull(testX(xp,"",s, "[1,2,3]")); // error more then two
			assertNotNull(testX(xp,"",s, "[[],[],[]]"));//error more then two
			assertNotNull(testX(xp,"",s, "[{},{},{}]"));//error more then two
			assertNotNull(testX(xp,"",s, "[1,[],{}]"));//error more then two
			genXComponent(xp = compile( // test %anyObj in different X-definitions
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name=\"a\" root=\"testX\">\n" +
"  <xd:json name=\"testX\"> [ \"%anyObj:?;\" ] </xd:json>\n" +
"</xd:def>\n" +
"<xd:def name=\"b\" root=\"testX\">\n" + // map
"  <xd:json name=\"testX\"> { \"%anyName\": \"%anyObj:?;\" } </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".MyTestX_AnyXX1 %link a#testX;\n" +
"    %class "+_package+".MyTestX_AnyXX2 %link b#testX;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>"));
			// xdef A; (array)
			s = _package+".MyTestX_AnyXX1";
			assertNull(testX(xp,"a", s, "[]"));
			assertNull(testX(xp,"a", s, "[1]"));
			assertNull(testX(xp,"a", s, "[ [] ]"));
			assertNull(testX(xp,"a", s, "[ [1, true, \"x\"] ]"));
			assertNull(testX(xp,"a", s, "[ [ { a:1 } ] ]"));
			assertNull(testX(xp,"a", s, "[ [ { a:1, b:[] } ] ]"));
			assertNull(testX(xp,"a", s,"[{a:1,b:[3.4],c:{d:5,e:[6,7]},f:{}}]"));
			assertNotNull(testX(xp,"a", s, "{}")); // must be error!
			assertNotNull(testX(xp,"a", s, "true")); // must be error!
			assertNotNull(testX(xp,"a", s, "[ 1, true ]")); // must be error!
			// xdef B; (map)
			s = _package+".MyTestX_AnyXX2";
			assertNull(testX(xp,"b", s, "{}"));
			assertNull(testX(xp,"b", s, "{a:1}"));
			assertNotNull(testX(xp,"b", s, "[]")); // must be error!
			assertNotNull(testX(xp,"b", s, "true")); // must be error!
			assertNotNull(testX(xp,"b", s, "{a:1, b:null}")); // must be error!
			genXComponent(xp = compile( // test XON models in different X-definitions
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name=\"a\" root=\"testX\">\n" +
"  <xd:json name=\"testX\"> [\"%anyObj:*\" ] </xd:json>\n" + // array
"</xd:def>\n" +
"<xd:def name=\"m\" root=\"testX\">\n" + // map
"  <xd:json name=\"testX\"> { \"%anyName\": \"%anyObj:*;\" } </xd:json>\n"  +
"</xd:def>\n" +
"<xd:def name=\"x\" root=\"testX\">\n" + // any object
"  <xd:json name=\"testX\"> \"%anyObj\" </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".MyTestX_AnyXXa %link a#testX;\n" +
"    %class "+_package+".MyTestX_AnyXXm %link m#testX;\n" +
"    %class "+_package+".MyTestX_AnyXXx %link x#testX;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>"));
			// xdef a; root is array with %anyObj items
			s = _package+".MyTestX_AnyXXa";
			assertNull(testX(xp,"a", s, "[]"));
			assertNull(testX(xp,"a", s, "[1]"));
			assertNull(testX(xp,"a", s, "[ \"\\\"\"]"));
			assertNull(testX(xp,"a", s, "[ [ { a:[1, \"\", \"\\\"\"]} ] ]"));//
			assertNull(testX(xp,"a", s, "[0,{a:[1,true],b:null},false,null]"));
			assertNull(testX(xp,"a", s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			assertNotNull(testX(xp,"a", s, "true")); // must be error
			assertNotNull(testX(xp,"a", s, "1")); // must be error
			assertNotNull(testX(xp,"a", s, "{}")); // must be error!
			// xdef m; root is map with %anyObj items
			s = _package+".MyTestX_AnyXXm";
			assertNull(testX(xp,"m", s, "{}"));
			assertNull(testX(xp,"m", s, "{a:1}"));
			assertNull(testX(xp,"m", s, "{a:\"\"}"));
			assertNull(testX(xp,"m", s, "{a:\"a b\"}"));
			assertNull(testX(xp,"m", s, "{a:\" a \"}"));
			assertNull(testX(xp,"m", s,"{a:1,b:[],c:{},d:{e:5,f:[2]},g:null}"));
			assertNull(testX(xp,"m", s, "{a:[1, true], b:null}"));
			assertNotNull(testX(xp,"m", s, "true")); // must be error!
			assertNotNull(testX(xp,"m", s, "[]")); // must be error!
			// 1. array as direct %anyObj
			s = _package+".MyTestX_AnyXXx";
			assertNull(testX(xp,"x", s, "[]"));
			assertNull(testX(xp,"x", s, "[]"));
			assertNull(testX(xp,"x", s, "[1]"));
			assertNull(testX(xp,"x", s, "[ [] ]"));
			assertNull(testX(xp,"x", s, "[ [1, true, [], {}] ]"));
			assertNull(testX(xp,"x", s, "[ [\"a\"] ]"));
			assertNull(testX(xp,"x", s, "[ [\"a\", \"\\\"\"] ]"));
			assertNull(testX(xp,"x", s, "[ [1] ]"));
			assertNull(testX(xp,"x", s, "[ [1, 2] ]"));
			assertNull(testX(xp,"x", s, "[ [1], [true], [\"x\"] ]"));
			assertNull(testX(xp,"x", s, "[ [ { a:1 } ] ]"));
			assertNull(testX(xp,"x", s, "[ [ { a:1, b:[] } ] ]"));
			assertNull(testX(xp,"x", s, "[ [{}], [ { a:1, b:[1,2,3] } ] ]"));
			assertNull(testX(xp,"x", s, "[ [{}] ]"));
			assertNull(testX(xp,"x", s, "[ [ { a:[1, 2]} ] ]"));//
			assertNull(testX(xp,"x", s, "[0,{a:[1,true],b:null},false,null]"));
			assertNull(testX(xp,"x", s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			// 2. map as direct %anyObj
			assertNull(testX(xp,"x", s, "{}"));
			assertNull(testX(xp,"x", s, "{a:1}"));
			assertNull(testX(xp,"x", s, "{\"\\\"\":\"1\"}"));
			assertNull(testX(xp,"x", s,"{a:1,b:[],c:{},d:{e:5,f:[2]},g:null}"));
			assertNull(testX(xp,"x", s, "{a:[1,true],b:null,c:1,d:false}"));
			// 3. simple value as direct %anyObj
			assertNull(testX(xp,"x", s, "null"));
			assertNull(testX(xp,"x", s, "true"));
			assertNull(testX(xp,"x", s, "1"));
			assertNull(testX(xp,"x", s, "-0.5e+2"));
			assertNull(testX(xp,"x", s, "\"\""));
			assertNull(testX(xp,"x", s, "\"x\""));
			assertNull(testX(xp,"x", s, "\"ab\tcd\""));
			assertNull(testX(xp,"x", s, "\" ab\tcd \""));
			assertNull(testX(xp,"x", s, "\" ab\\tcd \""));
			assertNull(testX(xp,"x", s, "\" ab\\u0020tcd \""));
			assertNull(testX(xp,"x", s, "\" \t\n \""));
			assertNull(testX(xp,"x", s, "\" \\t\\n \""));
			assertNull(testX(xp,"x", s, "\"\\\"\""));
			assertNull(testX(xp,"x", s, "\"\\\"\\\"\""));
			genXComponent(xp = compile( // test XON reference to %any in %oneOf
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='Any'>\n" +
"  <xd:json name=\"Any\">\n" +
"    [ \"%oneOf\", \"jvalue();\",\n" +
"      [ \"%script:*; ref anyA;\" ],\n" +
"      { \"%script\": \"*; ref anyM;\" }\n" +
"    ]\n" +
"  </xd:json>\n" +
"  <xd:json name=\"anyA\"> [ \"%anyObj:*;\" ] </xd:json>\n" +
"<xd:json name=\"anyM\"> { \"%anyName\": \"%anyObj:*;\" } </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".MyTest_xxx %link X#Any;\n" +
"  </xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTest_xxx";
			// value
			assertNull(testX(xp,"X", s, "true"));
			assertNull(testX(xp,"X", s, "1"));
			assertNull(testX(xp,"X", s, "null"));
			assertNull(testX(xp,"X", s, "\"a b\""));
			assertNull(testX(xp,"X", s, "\" \""));
			assertNull(testX(xp,"X", s, "\" a b \""));
			assertNull(testX(xp,"X", s, "\"1\""));
			// array
			assertNull(testX(xp,"X", s, "[]"));
			assertNull(testX(xp,"X", s, "[null]"));
			assertNull(testX(xp,"X", s, "[1]"));
			assertNull(testX(xp,"X", s, "[true]"));
			assertNull(testX(xp,"X", s, "[\"1\"]"));
			assertNull(testX(xp,"X", s, "[\"true\"]"));
			assertNull(testX(xp,"X", s, "[\" ab cd \"]"));
			assertNull(testX(xp,"X", s, "[1, true]"));
			assertNull(testX(xp,"X", s, "[[]]"));
			assertNull(testX(xp,"X", s, "[{}]"));
			// map
			assertNull(testX(xp,"X", s, "{}"));
			assertNull(testX(xp,"X", s, "{a:1}"));
			assertNull(testX(xp,"X", s, "{a:\"1\"}"));
			assertNull(testX(xp,"X", s, "{a:1, b:[],c:null,d:[], e:{}}"));
			assertNull(testX(xp,"X", s, "{a:1, b:[],c:null,d:[], e:{}}"));
			genXComponent(xp = compile( //jvalue
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"  <xd:json name='a'> \"jvalue()\" </xd:json>\n" +
"  <xd:component> %class "+_package+".MyTestX_jval %link #a; </xd:component>\n"+
"</xd:def>"));
			s = _package+".MyTestX_jval";
			assertNull(testX(xp, "", s, "null"));
			assertNull(testX(xp, "", s, "-1f"));
			assertNull(testX(xp, "", s, "3.14e+3D"));
			assertNull(testX(xp, "", s, "true"));
			assertNull(testX(xp, "", s, "\"a\""));
			assertNull(testX(xp, "", s, "\"a b\""));
			genXComponent(xp = compile( //jstring
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"  <xd:json name='a'> \"jstring()\" </xd:json>\n" +
"  <xd:component> %class "+_package+".MyTestX_jstr %link #a; </xd:component>\n"+
"</xd:def>"));
			s = _package+".MyTestX_jstr";
			assertNull(testX(xp, "", s, "\"\""));
			assertNull(testX(xp, "", s, "\"x\""));
			assertNull(testX(xp, "", s, "\" \""));
			assertNull(testX(xp, "", s, "\" \\\" \""));
			genXComponent(xp = compile( //int
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"  <xd:json name='a'> \"int()\" </xd:json>\n" +
"  <xd:component> %class "+_package+".MyTestX_int %link #a; </xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTestX_int";
			assertNull(testX(xp, "", s, "0"));
			assertNull(testX(xp, "", s, "-3"));
			assertNull(testX(xp, "", s, "123456"));
			genXComponent(xp = compile( // num
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_num %link #A;</xd:component>\n" +
"  <xd:json name='A'> \"num()\" </xd:json>\n" +
"</xd:def>"));
			s = _package+".MytestX_num";
			assertNull(testX(xp,"", s, "\"0\""));
			assertNull(testX(xp,"", s, "\"00001\""));
			assertNull(testX(xp,"", s, "\"2021\""));
			genXComponent(xp = compile( //boolean
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"  <xd:json name='a'> \"boolean()\" </xd:json>\n" +
"  <xd:component> %class "+_package+".MyTestX_bool %link #a; </xd:component>\n"+
"</xd:def>"));
			s = _package+".MyTestX_bool";
			assertNull(testX(xp, "", s, "true"));
			assertNull(testX(xp, "", s, "false"));
			genXComponent(xp = compile( //double
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"  <xd:json name='a'> \"double()\" </xd:json>\n" +
"  <xd:component> %class "+_package+".MyTestX_dbl %link #a; </xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTestX_dbl";
			assertNull(testX(xp, "", s, "-12.34"));
			assertNull(testX(xp, "", s, "1234"));
			genXComponent(xp = compile( //jvalue in map
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"  <xd:json name='a'> { a:\"jvalue()\" } </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestX_jvalM %link #a;</xd:component>\n" +
"</xd:def>"));
			s = _package+".MyTestX_jvalM";
			assertNull(testX(xp, "", s, "{\"a\":true}"));
			assertNull(testX(xp, "", s, "{\"a\":false}"));
			assertNull(testX(xp, "", s, "{\"a\":0}"));
			assertNull(testX(xp, "", s, "{\"a\":-3}"));
			assertNull(testX(xp, "", s, "{\"a\": -12.34}"));
			assertNull(testX(xp, "", s, "{\"a\": 1234}"));
			assertNull(testX(xp, "", s, "{\"a\": null}"));
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'> {\"%anyName\": \"* int()\", a:\"? boolean()\"} </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_x2 %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json = "{ \"\": 1, x: -99}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x,y));
			assertTrue(XonUtils.xonEqual(x, xd.getXon()));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(x, XonUtils.xonToJson(xc.toXon())));
			assertTrue(XonUtils.xonEqual(x, XComponentUtil.getMap(xc)));
			json = "{ \"\": 1, x: -99, a: true}";
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json),y));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json), xc.toXon()));
			genXComponent(xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>\n" +
"    { \"%anyName\": [\"%script:?\", \"%anyObj:*\"], a: \"%anyObj:?\" }\n" +
"  </xd:json>\n" +
"  <xd:component>%class "+_package+".MyTestAny_x4 %link A</xd:component>\n" +
"</xd:def>"));
			xd = xp.createXDDocument();
			json = "{ x: [0]}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x,y));
			assertTrue(XonUtils.xonEqual(x, xd.getXon()));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
			assertEq(1,XComponentUtil.getMap(xc).size());
			assertNull(XComponentUtil.get(xc, "$a"));
			json = "{ a:0, x: [1,2], y: [] }";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x,y));
			assertTrue(XonUtils.xonEqual(x, xd.getXon()));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
			assertEq(2, XComponentUtil.getMap(xc).size());
			assertEq(0, XComponentUtil.get(xc, "$a"));
		} catch (RuntimeException ex) {fail(ex);}
		try { // test YAML
			xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='test' name='A'>\n"+
"  <xd:json name=\"test\">\n" +
"    { \"cities\": [\n" +
"        \"date();\",\n" +
"        { \"%script\": \"occurs 1..*;\",\n" +
"          \"from\": [\n" +
"            \"string();\",\n" +
"            { \"%script\": \"occurs 1..*; \",\n" +
"              \"to\": \"jstring();\",\n" +
"              \"distance\": \"int();\"\n" +
"            }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }"+
"  </xd:json>\n" +
"</xd:def>").createXDDocument("A");
			s =
"cities:\n" +
"- '2020-02-22'\n" +
"- from:\n" +
"  - Brussels\n" +
"  - {to: London, distance: 322}\n" +
"  - {to: Paris, distance: 265}\n" +
"- from:\n" +
"  - London\n" +
"  - {to: Brussels, distance: 322}\n" +
"  - {to: Paris, distance: 344}\n";
			x = xd.yparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xd.yparse(XonUtils.toYamlString(x), reporter)));
			assertNoErrorwarningsAndClear(reporter);
		} catch (SRuntimeException ex) {
			if ("JSON101".equals(ex.getMsgID())) {
				setResultInfo("YAML tests skipped; org.yaml.snakeyaml is not available");
			} else {
				fail(ex);
			}
		}
		try { // test jlist
			xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='x'>\n"+
"  <x> <a xd:script='*'> jlist(%item=jvalue()) </a> </x>\n"+
"  <xd:component> %class "+_package+".TestJList %link x; </xd:component>\n"+
"</xd:def>");
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
			xc = parseXC(xp, "", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml, xc.toXml());
//			x = XComponentUtil.get(XComponentUtil.getList(xc, "a").get(0), "$value");
//			assertEq(new ArrayList(), XComponentUtil.jlistToList(x));
//			assertEq(new ArrayList(), x);
//			x = XComponentUtil.get(XComponentUtil.getList(xc, "a").get(1), "$value");
//			assertEq("false", XComponentUtil.jlistToList(x).get(0));
//			assertEq("false", XComponentUtil.jlistToList(x).get(0));
//			x = XComponentUtil.get(XComponentUtil.getList(xc, "listOfa").get(2), "$value");
//			assertEq(null, XComponentUtil.jlistToList(x).get(0));
//			x = XComponentUtil.get(XComponentUtil.getList(xc,"a").get(3),"$value");
//			assertEq(-9, XComponentUtil.jlistToList(x).get(0));
//			assertEq("", XComponentUtil.jlistToList(x).get(1));
//			assertEq("\"", XComponentUtil.jlistToList(x).get(2));
//			ArrayList<Object> alist = new ArrayList<>();
//			alist.add(2);
//			alist.add(new ArrayList<>());
//			alist.add("ab\tc");
//			assertTrue(XonUtils.xonEqual(alist, XComponentUtil.jlistToList(x).get(3)));
//			assertEq("-3.5", XComponentUtil.jlistToList(x).get(4));

			assertNoErrors(test("an();"));
			assertNoErrors(test("string();"));
			assertNoErrors(test("enum('0', 'unKNOWn');"));
			assertNoErrors(test("enumi('0', 'unknown');"));
			assertNoErrors(test("base.rule('a');"));
			assertNoErrors(test("BNF(base,'a');"));
		} catch (RuntimeException ex) {fail(ex);}
		if (oldCodes != null) {
			setProperty(XDConstants.XDPROPERTY_STRING_CODES, oldCodes);
		}

		clearTempDir(); // clear temporary directory
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}