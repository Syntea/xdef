package test.xdef;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDEmailAddr;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDTelephone;
import org.xdef.component.XComponent;
import org.xdef.xon.XonUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.CsvReader;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.genXComponent;

/** Test XON/JSON/INI/Properties/CSV data. */
public class TestXon extends XDTester {

	public TestXon() {super();}

	/** Test simple type in Array
	 * @param type type
	 * @param xon xon to be tested.
	 * @return string with errors or null.
	 */
	private String testA(final String type, final String xon) {
		return testX(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:xon name='A'>\n"+
"    [\"* " + type + "()\"]\n"+
"  </xd:xon>\n"+
"  <xd:component>\n"+
"    %class test.xdef.GJ"+ type + " %link #A;\n"+
"  </xd:component>\n"+
"</xd:def>", "", xon);
	}

	/** Test simple type in Map.
	 * @param type type
	 * @param xon xon to be tested.
	 * @return string with errors or null.
	 */
	private String testM(final String type, final String xon) {
		return testX(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"{a: \"" + type + "();\"}\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestEmptyMap %link A</xd:component>\n"+
"</xd:def>", "", xon);
	}

	/** Test given data with Xdefinition.
	 * @param xdef Xdefinition source.
	 * @param xdName name of XDefinition.
	 * @param data data.
	 * @return null or string with error.
	 */
	private String testX(String xdef, String xdName, String data) {
		try {
			XDPool xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			return testX(xp, xdName, data);
		} catch (Exception ex) {return printThrowable(ex);}
	}

	/** Test given data with XDPool
	 * @param xdef Xdefinition source.
	 * @param xdName name of XDefinition.
	 * @param data data.
	 * @return empty string or error.
	 */
	private String testX(XDPool xp, String xdName, String data) {
		return testX(xp, xdName, null, data);
	}

	/** Test given data with XDPool
	 * @param xdef Xdefinition source.
	 * @param xdName name of XDefinition.
	 * @param cls XDefinition class name or null.
	 * @param data data.
	 * @return empty string or error.
	 */
	private String testX(XDPool xp, String xdName, String cls, String data){
		String result = "";
		try {
			ArrayReporter reporter = new ArrayReporter();
			Object o = XonUtils.parseXON(data);
			XDDocument xd = xp.createXDDocument(xdName);
			Object x = xd.jparse(data, reporter);
			if (reporter.errorWarnings()) {
				result += "** 1\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 2\n" +  data + "\n" +
					XonUtils.toXonString(x, true) + "\n";
			}
			xd = xp.createXDDocument(xdName);
			XComponent xc = xd.jparseXComponent(data, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 3\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 4\n" +  data + "\n" +
					XonUtils.toXonString(x, true) + "\n";
			}
			xd = xp.createXDDocument(xdName);
			xc = xd.jparseXComponent(o, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 5\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 6\n" +  data + "\n" +
					XonUtils.toXonString(x, true) + "\n";
			}
			if (cls != null) {
				Class<?> clazz = Class.forName(cls);
				xc = xd.jparseXComponent(data, clazz, reporter);
				if (reporter.errorWarnings()) {
					result += "** 3\n" + reporter.printToString() + "\n";
					reporter.clear();
				}
				x = xc.toXon();
				if (!XonUtils.xonEqual(o, x)) {
					result += "** 4\n" +  data + "\n" +
						XonUtils.toXonString(x, true) + "\n";
				}
				xd = xp.createXDDocument(xdName);
				xc = xd.jparseXComponent(o, clazz, reporter);
				if (reporter.errorWarnings()) {
					result += "** 5\n" + reporter.printToString() + "\n";
					reporter.clear();
				}
				x = xc.toXon();
				if (!XonUtils.xonEqual(o, x)) {
					result += "** 4\n" +  data + "\n" +
						XonUtils.toXonString(x, true) + "\n";
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			result += ex + "\n";
		}
		return result.isEmpty() ? null : result;
	}

	/** Run all tests. */
	@Override
	public void test() {
		if (!_xdNS.startsWith("http://www.xdef.org/xdef/4.")) {
			return;
		}
		assertNull(testA("byte", "[null, 1b, -3b ]"));
		assertNull(testA("short", "[null, 1s ]"));
		assertNull(testA("int", "[null, 1i ]"));
		assertNull(testA("long", "[null, 1 ]"));
		assertNull(testA("integer", "[null, 0N, -3N ]"));
		assertNull(testA("float", "[null, 1.0f ]"));
		assertNull(testA("double", "[null, 1.0 ]"));
		assertNull(testA("decimal", "[null, 0D, 1D, -1D, 1.5D, 3.33e-5D ]"));
		assertNull(testA("date",
			"[null, d2021-01-12, d1999-01-05+01:01, d1998-12-21Z ]"));
		assertNull(testA("gYear", "[null,  d2021+01:00, d1999, d-0012Z ]"));
		assertNull(testA("gps",
			"[null, g(20.21,19.99),g(20.21, 19.99,0.1),g(51.52,-0.09,0,xxx) ]"));
		assertNull(testA("price", "[null, p(20.21 CZK), p(19.99 USD) ]"));
		assertNull(testA("char",
			"[null, c\"a\", c\"'\", c\"\\\"\", c\"\\u0007\", c\"\\\\\" ]"));
		assertNull(testA("anyURI", "[null, u\"http://a.b\" ]"));
		assertNull(testA("emailAddr",
			"[null, e\"tro@volny.cz\",e\"a b<x@y.zz>\" ]"));
		assertNull(testA("file", "[null, \"temp/a.txt\" ]"));
		assertNull(testA("ipAddr", "[null, /::FFFF:129.144.52.38,/0.0.0]\n"));
		assertNull(testA("currency", "[null, C(USD), C(CZK)]\n"));
		assertNull(testA("telephone",
			"[null, t\"123456\",t\"+420 234 567 890\"]\n"));
		assertNull(testA("jnull", "[ null, null ]"));
		assertNull(testA("jboolean", "[ null, true ]"));
		assertNull(testA("jnumber", "[ null, 1 ]"));
		assertNull(testA("jstring", "[ null, \"abc\" ]"));
		assertNull(testA("jvalue", "[ null, true, 1, \"abc\" ]"));

		assertNull(testM("? int", "{a:1}"));
		assertNull(testM("? int", "{ }"));

		String s, json, xon, xdef, xml;
		List list;
		Object o, x, y;
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XComponent xc;
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' name='M' root='y:X'\n"+
"        xmlns:y='a.b'>\n"+
"<xd:declaration>type gam xdatetime('yyyyMMddHHmmssSS');</xd:declaration>\n"+
"  <y:X a = '?date()' t='gam();' >? int() <y:Y xd:script='*'/>dec()</y:X>\n"+
"<xd:component>%class test.xdef.MGam %link y:X</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml =
"<n:X xmlns:n='a.b' a='2021-12-30' t='2020121101010101'>1<n:Y/><n:Y/>2.0</n:X>";
			xc = parseXC(xp,"M", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
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
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
"<xd:component>%class test.xdef.Csvxx %link a</xd:component>\n"+
" <xd:xon name='a'>\n"+
"    [ [ %script =\"+\", \"int\", \"int\", \"string()\", \"boolean()\"] ]\n"+
" </xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef); // no property
			genXComponent(xp, clearTempDir());
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
			if (!XonUtils.xonEqual(o, x = xc.toXon())) {
				fail(XonUtils.toXonString(o, true)
					+ "\n*****\n" + XonUtils.toXonString(x, true));
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"[\n" +
"  {\n" +
"    a : \"? short()\",\n" +
"    b : \"? jstring()\",\n" +
"    c : \"? double()\",\n" +
"    f : \"? boolean()\",\n" +
"    g : \"? duration()\",\n" +
"    h : \"? jnull()\",\n" +	/* null */
"    i : [],\n" +
"    Towns : [\n" +
"      \"* gps()\"\n" +
"    ],\n" +
"    j : \"? char()\",\n" +
"    k : \"? char()\",\n" +
"    l : \"? char()\",\n" +
"    m : \"? char()\",\n" +		/*char '\u0007' */
"    n : \"? char()\",\n" +
"    o : \"? char()\",\n" +
"    p : \"? char()\",\n" +
"    q : \"? char()\",\n" +
"    r : \"? char()\",\n" +  	/*char null */
"    t : \"? gYear()\",\n" +
"    u : \"? gYear()\",\n" +
"    v : \"? gYear()\",\n" +
"    w : \"? gYear()\",\n" +
"    \" name with space \": \"? jstring()\"\n" +
"  },\n" +
"  \"jnull()\",\n" +
"  \"float()\",\n" +
"  \"float()\",\n" +
"  \"decimal()\",\n" +
"  \"byte()\",\n" +
"  \"integer()\",\n" +
"  \"integer()\",\n" +
"  \"date()\",\n" +
"  \"gMonth()\",\n" +
"  \"gMonth()\",\n" +
"  \"gMonthDay()\",\n" +
"  \"gMonthDay()\",\n" +
"  \"time()\",\n" +
"  \"time()\",\n" +
"  \"time()\",\n" +
"  \"gYearMonth()\",\n" +
"  \"gYearMonth()\",\n" +
"  \"gYearMonth\",\n" +
"  \"dateTime()\",\n" +
"  \"gps()\",\n" +
"  \"base64Binary()\",\n" +
"  \"price()\",\n" +
"  \"price()\",\n" +
"  \"currency()\",\n" +
"  \"ipAddr()\",\n" +
"  \"ipAddr()\"\n" +
"]\n" +
"</xd:xon>\n" +
"<xd:component>\n"+
"  %class test.xdef.Xon %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			xon =
"/**** Start of XON example ****/\n" +
"[                                    # Array\n" +
"  {                                  # Map\n" +
"    a : 1s,                          # Short\n" +
"    b : \"ab cd\",                     # String\n" +
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
"  },  /**** end of map ****/\n" +
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
"  b(HbRBHbRBHQw=),                   # byte array (base64)\n" +
"  p(123.45 CZK),                     # price\n" +
"  p(12 USD),                         # price\n" +
"  C(USD),                            # currency\n" +
"  /129.144.52.38,                    # inetAddr (IPv4)\n" +
"  /1080:0:0:0:8:800:200C:417A,       # inetAddr (IPv6)\n" +
"] /**** end of array ****/\n" +
"/**** End of XON example ****/";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toJsonString(x, true);
			XonUtils.parseXON(s);
			s = XonUtils.toXonString(x, true);
			y = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x,y));
			s = XonUtils.toXonString(x, false);
			list = (List) ((Map) ((List) x).get(0)).get("Towns");
			assertEq("Wien",((GPSPosition) list.get(0)).name());
			assertEq("London",((GPSPosition) list.get(1)).name());
			assertEq("Prague old town",((GPSPosition) list.get(3)).name());
			assertEq(1233, Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(1)))/1000));
			assertEq(252,Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(3)))/1000));
			assertEq(1030,Math.round(((GPSPosition) list.get(1)).distanceTo(
				((GPSPosition) list.get(3)))/1000));
			assertNoErrorwarningsAndClear(reporter);
			json = XonUtils.toXonString(x, true);
			XonUtils.parseXON(json);
			y = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			o = xc.toXon();
			assertTrue(XonUtils.xonEqual(x,y));
			x = XonUtils.xonToJson(XonUtils.xmlToXon(el = xc.toXml()));
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail(KXmlUtils.nodeToString(el, true)
					+ "\n***\n" + XonUtils.toXonString(y, true)
					+ "\n***\n" + XonUtils.toXonString(x, true));
			}
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, y = xc.toXon()));
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
"<xd:component>%class test.xdef.Xona %link a</xd:component>\n"+
" <xd:xon name='a'>\n"+
"[\n" +
"  [ %script= \"optional\", \"boolean();\", \"optional int();\" ]\n" +
"]\n" +
" </xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json = "[ [ true, 123 ] ]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, (x = xc.toXon()))) {
				fail(XonUtils.toXonString(o)
					+ "\n***\n" + XonUtils.toXonString(x));
			}
			json = "[\n]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, (x = xc.toXon()))) {
				fail(XonUtils.toXonString(o)
					+ "\n***\n" + XonUtils.toXonString(x));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class test.xdef.Xonb %link X</xd:component>\n"+
"<xd:xon name=\"X\">\n"+
"[\n" +
"  [\"fixed 'Name'\",\"fixed 'Email'\",\"fixed 'Mobile Number'\"],\n"+
"  [%script=\"+\",\n"+
"    \"string()\",\n"+
"    \"union(%item=[emailAddr(), jnull])\",\n"+
"    \"union(%item=[telephone(), jnull])\"\n"+
"  ]\n" +
"]\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			s =
"[\n" +
" [\"Name\", \"Email\", \"Mobile Number\"],\n"+
" [\"Hel \\\"\\\"Ova\",\"hka@vol.cz\",\"+420 123 345 678\"],\n" +
" [\"Eva Kuž, Epor \\\"Prix\\\"\", \"ep@ema.cz\", null],\n" +
" [\"Jivá\", null, null]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xd.getXDPool(), clearTempDir());
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
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
		} catch (Exception ex) {fail(ex);}
		try { // test YAML
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"  <xd:xon name=\"test\">\n" +
"    { \"cities\": [\n" +
"        \"date();\",\n" +
"        { %script = \"occurs 1..*;\",\n" +
"          \"from\": [\n" +
"            \"string();\",\n" +
"            { %script = \"occurs 1..*; \",\n" +
"              \"to\": \"jstring();\",\n" +
"              \"distance\": \"int();\"\n" +
"            }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }"+
"  </xd:xon>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument("A");
			String yaml =
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
			x = xd.yparse(yaml, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x,
				xd.yparse(XonUtils.toYamlString(x), reporter)));
			assertNoErrorwarningsAndClear(reporter);
		} catch (SRuntimeException ex) {
			if ("JSON101".equals(ex.getMsgID())) {
				setResultInfo("YAML tests skipped: package "
					+ "org.yaml.snakeyaml not available");
			} else {
				fail(ex);
			}
		} catch (Exception ex) {fail(ex);}
		try { // test Windows INI
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"<xd:ini xd:name = \"test\">\n" +
" name = string();\n" +
" date = date();\n" +
" email = emailAddr();\n" +
" [Server]\n" +
"   IPAddr = ipAddr();\n" +
"</xd:ini>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument("A");
			String ini =
"date = 2021-02-03\n"+
"name = Jan Novak\n"+
"email = a@b.c\n"+
"[Server]\n"+
" IPAddr = 255.0.0.0\n";
			Map<String, Object> xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"#this is INI file comment\n" +
"address=string(); options noTrimAttr\n" +
"dns = ipAddr()\n"  +
"name = string()\n"+
"parser.factor.1=string()\n" +
"servertool.up=string()\n"+
"  </xd:ini>\n"  +
"</xd:def>";
			xd = XDFactory.compileXD(null,xdef).createXDDocument("A");
			ini =
"#this is INI file comment\n" +
"address=dhcp\1\n" +
"dns = 192.168.1.1\n"  +
"name = John E\\\n"+
" . \\\n"  +
" Smith\n"  +
"  parser.factor.1=')' \\u00E9 esperado.\n" +
"servertool.up=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type=int(0,9)\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" + //
"port= ? int(0, 9999);\n" +
"[system] %script = optional\n" +
"autolaunch=int()\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>";
//			xp = compile(xdef);
			xp = XDFactory.compileXD(null,xdef);
			xd = xp.createXDDocument("A");
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
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			ini =
"proxy type=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"TRSconfig\">\n" +
"  <xd:ini xd:name=\"TRSconfig\">\n" +
"    TRSUser = string()\n" +
"    [User]\n" +
"      Home = file()\n" +
"      Authority = enum(\"SECURITY\", \"SOFTWARE\", \"CLIENT\", \"UNREGISTRED\")\n" +
"      ItemSize = int(10000, 15000000)\n" +
"      ReceiverSleep = int(1, 3600)\n" +
"    [Server] %script = optional\n" +
"      RemoteServerURL = url()\n" +
"      SeverIP = ipAddr()\n" +
"      SendMailHost = domainAddr()\n" +
"      MailAddr = emailAddr()\n" +
"      Signature = SHA1()\n" +
"  </xd:ini>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
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
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xini,
				xd.iparse(XonUtils.toIniString(xini), reporter)));
			assertNoErrorwarningsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			//test CSV data with head line (column names)
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class test.xdef.CsvTest %link CSV</xd:component>\n"+
"<xd:xon name=\"CSV\">\n"+
"[\n"+
"  [\"3..3 string();\"],\n"+ // head
"  [%script=\"+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n"+
"]\n"+
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			genXComponent(xp, clearTempDir());
			s =
"Name, Email, Mobile Number\n"+
"abc, a@b.c, +420 601 349 889\n"+
"\n"+
"xyz, d@e.f,\n"+
"xyz,,\n"+
",,\n"+
"xyz, , 123 456 789";
			x = xd.cparse(new StringReader(s), null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"Name | Email | Mobile Number\n"+
"abc | a@b.c | +420 601 349 889\n"+
"\n"+
"xyz | d@e.f |\n"+
"xyz | |\n"+
" | |\n"+
"xyz | | 123 456 789";
			o = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n"+
" [\"Name\",\"Email\",\"Mobile Number\"],\n"+
" [\"abc\", e\"a@b.c\", \"+420 601 349 889\"],\n"+
" [],\n"+
" [\"xyz\", e\"d@e.f\",null],\n"+
" [\"xyz\", null, null],\n"+
" [null, null, null],\n"+
" [\"xyz\", null, \"123 456 789\"]\n"+
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n"
					+ "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			// no CSV head line with bames;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class test.xdef.CsvTest1 %link CSV</xd:component>\n"+
"<xd:xon name=\"CSV\">\n"+
"[\n"+
"  [%script=\"+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n"+
"]\n"+
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			genXComponent(xp, clearTempDir());
			s =
"Name, Email, Mobile Number\n"+
"abc, a@b.c, +420 601 349 889\n"+
"xyz, d@e.f,\n"+
"xyz,,\n"+
",,\n"+
"xyz, , 123 456 789\n";
			o = xd.cparse(new StringReader(s), ',', true, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"abc | a@b.c | +420 601 349 889\n"+
"xyz | d@e.f |\n"+
"xyz | |\n"+
" | |\n"+
"xyz | | 123 456 789\n";
			x = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n"+
" [\"abc\", e\"a@b.c\", \"+420 601 349 889\"],\n"+
" [\"xyz\", e\"d@e.f\",null],\n"+
" [\"xyz\", null, null],\n"+
" [null, null, null],\n"+
" [\"xyz\", null, \"123 456 789\"]\n"+
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n"
					+ "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
		} catch (Exception ex) {fail(ex);}
		try { // test $oneOf
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"test\">\n" +
"<xd:component>%class test.xdef.MyTestX_OneOf %link test</xd:component>\n"+
"<xd:xon name=\"test\">\n" +
"{ a:[ %oneOf,\n" +
"       \"date(); finally outln('date')\", \n" +
"       \"ipAddr(); finally outln('ipAddr')\", \n" +
"       [%script=\"finally outln('[...]')\",\"*int()\"], \n" +
"       \"string(); finally outln('string')\" \n" +
"  ]\n" +
"}\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, clearTempDir());
			s = "{a:\"2022-04-10\"}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("date\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("date\n", strw.toString());
			assertEq(o, xc.toXon());
			s = "{a:\"202.204.1.0\"}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", strw.toString());
			assertEq(o, xc.toXon());
			s = "{a:[1,2]}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", strw.toString());
			assertEq(o, xc.toXon());
			s = "{a:\"a\tb\n\"}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("string\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("string\n", strw.toString());
			assertEq(o, xc.toXon());
		} catch (Exception ex) {fail(ex);}
		try { // test forget in XON
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"test\">\n" +
"<xd:component>%class test.xdef.data.TestXonForget %link test</xd:component>\n"+
"  <xd:xon name=\"test\">\n" +
"    {date: \"date()\",\n" +
"      cities: [\n" +
"        { %script = \"occurs 1..*; finally outln(); forget\",\n" +
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
"  </xd:xon>\n" +
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
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			x = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			strw.close();
			assertEq(strw.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			strw.close();
			assertEq(strw.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertTrue(((List)((Map)(x = xc.toXon())).get("cities")).isEmpty());
			assertEq(((Map) x).get("date"), new SDatetime("2020-02-22"));
		} catch (Exception ex) {fail(ex);}
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:xon name='A'>\n"+
"{ %anyName: \"? int();\", x: \"? int();\" }\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_1 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json = "{}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			json = "{\"x\" : 1, \"xxx\" : 2}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:xon name='A'>\n"+
"{ %anyName: \"* int();\", x: \"? int();\" }\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_2 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json = "{}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			json = "{ a: 1, b: 2, x: 999 }";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n"+
"{ %anyName: [\"* int();\"]}\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_3 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			json = "{ \"x\": [1,2] }";
			x = XonUtils.parseJSON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			genXComponent(xp, clearTempDir());
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n"+
"{ %anyName: [%script=\"*\", \"* int();\"]}\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_4 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			json = "{ \"x\": [1,2] }";
			x = XonUtils.parseJSON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			genXComponent(xp, clearTempDir());
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			json = "{ \"p\": [8,9], \"q\": [] }";
			x = XonUtils.parseJSON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n"+
"{ \"cities\" : [\n" +
"    \"date(); finally outln('Measurements taken on: '+getText()+'\\n');\",\n"+
"    { %script = \"occurs 1..*;\",\n" +
"      %anyName: [%script = \"occurs 1..*;\n"+
"                init outln('Distance from ' + getXonKey() + '\nto:');\",\n" +
"        { %script = \"occurs 1..*; finally outln();\",\n" +
"          \"to\" : \"jstring();finally out(' - ' + getText() + ' = ');\",\n" +
"          \"distance\" : \"int(); finally out(getText() + '(km)');\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_6 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
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
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			strw.close();
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
			assertEq(strw.toString(), s);
			if (!XonUtils.xonEqual(x,y =  XonUtils.xonToJson(y))) {
				fail("** 1 **\n"+XonUtils.toXonString(x, true)
					+ "\n" +  XonUtils.toXonString(y, true));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, XonUtils.xonToJson(o))) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			strw.close();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(strw.toString(), s);
		} catch (Exception ex) {fail(ex);}
		try { // test Windows INI
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"<xd:ini xd:name = \"test\">\n" +
" name = string();\n" +
" date = date();\n" +
" email = emailAddr();\n" +
" [Server]\n" +
"   IPAddr = ipAddr();\n" +
"</xd:ini>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument("A");
			String ini =
"date = 2021-02-03\n"+
"name = Jan Novak\n"+
"email = a@b.c\n"+
"[Server]\n"+
" IPAddr = 255.0.0.0\n";
			Map<String, Object> xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"#this is INI file comment\n" +
"address=string(); options noTrimAttr\n" +
"dns = ipAddr()\n"  +
"name = string()\n"+
"parser.factor.1=string()\n" +
"servertool.up=string()\n"+
"  </xd:ini>\n"  +
"</xd:def>";
			xd = XDFactory.compileXD(null,xdef).createXDDocument("A");
			ini =
"#this is INI file comment\n" +
"address=dhcp\1\n" +
"dns = 192.168.1.1\n"  +
"name = John E\\\n"+
" . \\\n"  +
" Smith\n"  +
"  parser.factor.1=')' \\u00E9 esperado.\n" +
"servertool.up=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type=int(0,9)\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" + //
"port= ? int(0, 9999);\n" +
"[system] %script = optional\n" +
"autolaunch=int()\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>";
//			xp = compile(xdef);
			xp = XDFactory.compileXD(null,xdef);
			xd = xp.createXDDocument("A");
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
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			ini =
"proxy type=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"TRSconfig\">\n" +
"  <xd:ini xd:name=\"TRSconfig\">\n" +
"    TRSUser = string()\n" +
"    [User]\n" +
"      Home = file()\n" +
"      Authority = enum(\"SECURITY\", \"SOFTWARE\", \"CLIENT\", \"UNREGISTRED\")\n" +
"      ItemSize = int(10000, 15000000)\n" +
"      ReceiverSleep = int(1, 3600)\n" +
"    [Server] %script = optional\n" +
"      RemoteServerURL = url()\n" +
"      SeverIP = ipAddr()\n" +
"      SendMailHost = domainAddr()\n" +
"      MailAddr = emailAddr()\n" +
"      Signature = SHA1()\n" +
"  </xd:ini>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
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
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xini,
				xd.iparse(XonUtils.toIniString(xini), reporter)));
			assertNoErrorwarningsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
		try { //test CSV data
			// with head
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class test.xdef.CsvTest %link CSV</xd:component>\n"+
"<xd:xon name=\"CSV\">\n"+
"[\n"+
"  [\"3..3 string();\"],\n"+ // head
"  [%script=\"+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n"+
"]\n"+
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			genXComponent(xp, clearTempDir());
			s =
"Name, Email, Mobile Number\n"+
"abc, a@b.c, +420 601 349 889\n"+
"\n"+
"xyz, d@e.f,\n"+
"xyz,,\n"+
",,\n"+
"xyz, , 123 456 789";
			x = xd.cparse(new StringReader(s), null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"Name | Email | Mobile Number\n"+
"abc | a@b.c | +420 601 349 889\n"+
"\n"+
"xyz | d@e.f |\n"+
"xyz | |\n"+
" | |\n"+
"xyz | | 123 456 789";
			o = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n"+
" [\"Name\",\"Email\",\"Mobile Number\"],\n"+
" [\"abc\", e\"a@b.c\", \"+420 601 349 889\"],\n"+
" [],\n"+
" [\"xyz\", e\"d@e.f\",null],\n"+
" [\"xyz\", null, null],\n"+
" [null, null, null],\n"+
" [\"xyz\", null, \"123 456 789\"]\n"+
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n"
					+ "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			// no head;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class test.xdef.CsvTest1 %link CSV</xd:component>\n"+
"<xd:xon name=\"CSV\">\n"+
"[\n"+
"  [%script=\"+\", \"? string()\", \"? emailAddr\", \"? telephone()\"]\n"+
"]\n"+
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			genXComponent(xp, clearTempDir());
			s =
"Name, Email, Mobile Number\n"+
"abc, a@b.c, +420 601 349 889\n"+
"xyz, d@e.f,\n"+
"xyz,,\n"+
",,\n"+
"xyz, , 123 456 789\n";
			o = xd.cparse(new StringReader(s), ',', true, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			s =
"abc | a@b.c | +420 601 349 889\n"+
"xyz | d@e.f |\n"+
"xyz | |\n"+
" | |\n"+
"xyz | | 123 456 789\n";
			x = xd.cparse(new StringReader(s), '|', false, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			xc = xd.jparseXComponent(o, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(x = xc.toXon(), o)) {
				fail("*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			s =
"[\n"+
" [\"abc\", e\"a@b.c\", \"+420 601 349 889\"],\n"+
" [\"xyz\", e\"d@e.f\",null],\n"+
" [\"xyz\", null, null],\n"+
" [null, null, null],\n"+
" [\"xyz\", null, \"123 456 789\"]\n"+
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail( "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
			el = CsvReader.csvToXml((List) o);
			x = CsvReader.xmlToCsv(el);
			if (!XonUtils.xonEqual(o, x)) {
				fail(KXmlUtils.nodeToString(el, true) + "\n"
					+ "*** A *\n" + XonUtils.toXonString(x)
					+ "\n*** B *\n" + XonUtils.toXonString(o));
			}
		} catch (Exception ex) {fail(ex);}
		try { // test $oneOf
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"test\">\n" +
"<xd:component>%class test.xdef.MyTestX_OneOf %link test</xd:component>\n"+
"<xd:xon name=\"test\">\n" +
"{ a:[ %oneOf,\n" +
"       \"date(); finally outln('date')\", \n" +
"       \"ipAddr(); finally outln('ipAddr')\", \n" +
"       [%script=\"finally outln('[...]')\",\"*int()\"], \n" +
"       \"string(); finally outln('string')\" \n" +
"  ]\n" +
"}\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, clearTempDir());
			s = "{a:\"2022-04-10\"}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("date\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("date\n", strw.toString());
			assertEq(o, xc.toXon());
			s = "{a:\"202.204.1.0\"}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", strw.toString());
			assertEq(o, xc.toXon());
			s = "{a:[1,2]}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", strw.toString());
			assertEq(o, xc.toXon());
			s = "{a:\"a\tb\n\"}";
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("string\n", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null,reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("string\n", strw.toString());
			assertEq(o, xc.toXon());
		} catch (Exception ex) {fail(ex);}
		try { // test forget
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"test\">\n" +
"<xd:component>%class test.xdef.data.TestXonForget %link test</xd:component>\n"+
"  <xd:xon name=\"test\">\n" +
"    {date: \"date()\",\n" +
"      cities: [\n" +
"        { %script = \"occurs 1..*; finally outln(); forget\",\n" +
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
"  </xd:xon>\n" +
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
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			x = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			strw.close();
			assertEq(strw.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			assertTrue(((List)((Map)x).get("cities")).isEmpty());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			strw.close();
			assertEq(strw.toString(),
"From Brussels to London is 322 km to Paris is 265 km\n" +
"From London to Brussels is 322 km to Paris is 344 km\n");
			assertTrue(((List)((Map)(x = xc.toXon())).get("cities")).isEmpty());
			assertEq(((Map) x).get("date"), new SDatetime("2020-02-22"));
		} catch (Exception ex) {fail(ex);}
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:xon name='A'>\n"+
"{ %anyName: \"? int();\", x: \"? int();\" }\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_1 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json = "{}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			json = "{\"x\" : 1, \"xxx\" : 2}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:xon name='A'>\n"+
"{ %anyName: \"* int();\", x: \"? int();\" }\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_2 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json = "{}";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			json = "{ a: 1, b: 2, x: 999 }";
			x = XonUtils.parseXON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n"+
"{ %anyName: [\"* int();\"]}\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_3 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			json = "{ \"x\": [1,2] }";
			x = XonUtils.parseJSON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			genXComponent(xp, clearTempDir());
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n"+
"{ %anyName: [%script=\"*\", \"* int();\"]}\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_4 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			json = "{ \"x\": [1,2] }";
			x = XonUtils.parseJSON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			genXComponent(xp, clearTempDir());
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			json = "{ \"p\": [8,9], \"q\": [] }";
			x = XonUtils.parseJSON(json);
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			if (!XonUtils.xonEqual(x,y)) {
				fail("** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
			if (!XonUtils.xonEqual(x, y = XonUtils.xonToJson(y))) {
				fail("** 3 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y));
			}
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n"+
"{ \"cities\" : [\n" +
"    \"date(); finally outln('Measurements taken on: '+getText()+'\\n');\",\n"+
"    { %script = \"occurs 1..*;\",\n" +
"      %anyName: [%script = \"occurs 1..*;\n"+
"                init outln('Distance from ' + getXonKey() + '\nto:');\",\n" +
"        { %script = \"occurs 1..*; finally outln();\",\n" +
"          \"to\" : \"jstring();finally out(' - ' + getText() + ' = ');\",\n" +
"          \"distance\" : \"int(); finally out(getText() + '(km)');\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}\n"+
"</xd:xon>\n"+
"<xd:component>%class test.xdef.MyTestAny_6 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
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
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			y = xd.jvalidate(json, reporter);
			assertNoErrorsAndClear(reporter);
			strw.close();
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
			assertEq(strw.toString(), s);
			if (!XonUtils.xonEqual(x,y =  XonUtils.xonToJson(y))) {
				fail("** 1 **\n"+XonUtils.toXonString(x, true)
					+ "\n" +  XonUtils.toXonString(y, true));
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, XonUtils.xonToJson(o))) {
				fail("** 2 **\n"+json+"\n" + XonUtils.toXonString(xd.getXon()));
			}
			strw.close();
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(strw.toString(), s);
			xdef = // test %anyName, %oneOf, ref
"<xd:def xmlns:xd='" + _xdNS + "' root=\"a\">\n" +
"<xd:xon name=\"a\">[ { %script=\"occurs *; ref B\" } ]</xd:xon>\n" +
"<xd:xon name=\"B\">\n" +
"  { %anyName: [%oneOf,\n" +
"      \"string()\",\n" +
"       [\"occurs *; string()\"]\n" +
"    ]\n" +
"  }\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestAnyRef %link #a</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			json =
"[ { \"A\": [\"classic\"] },\n" +
"  { \"B\": [ \"Rock\", \"pop\" ] },\n" +
"  { \"C\": \"Country\" },\n" +
"  { \"D\": [] }\n" +
"]\n" +
"";
			assertNull(testX(xp, "", "test.xdef.TestAnyRef", json));
			xdef = //test ref to oneOf
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"<xd:xon name=\"test\">[ %script=\"ref A\" ]</xd:xon>\n" +
"<xd:xon name=\"A\">\n" +
" [%oneOf,\n"+
"    \"jvalue();\",\n" +
"    [\"*; jvalue();\" ],\n" +
"    {%anyName:\n" +
"       [%oneOf,\n" +
"         \"jvalue();\",\n" +
"         [\"*; jvalue();\"],\n" +
"         {%anyName:\n" +
"           [%oneOf,\n" +
"             \"jvalue();\",\n" +
"             [\"*; jvalue();\"],\n" +
"           ]\n" +
"         }\n" +
"       ]\n" +
"    }\n" +
"  ]\n" +
"</xd:xon>\n"  +
"<xd:component>\n"+
"  %class test.xdef.TestAnyRef1 %link A#test;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			s = "test.xdef.TestAnyRef1"; // class name
			assertNull(testX(xp, "A", s, "[ [] ]"));
			assertNull(testX(xp, "A", s, "[ {} ]"));
			assertNull(testX(xp, "A", s, "[ { a:1 } ]"));
			assertNull(testX(xp, "A", s, "[ { a: [1,2] } ]"));
			assertNull(testX(xp, "A", s, "[true]"));
			assertNull(testX(xp, "A", s, "[[1, true]]"));
			assertNull(testX(xp, "A",s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			xdef =  // test %anyObj in different X-definitions
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name=\"A\" root=\"testX\">\n" +
"  <xd:xon name=\"testX\"> [ %anyObj ] </xd:xon>\n"  +
"</xd:def>\n"+
"<xd:def name=\"B\" root=\"testX\">\n" +
"  <xd:xon name=\"testX\"> { %anyObj } </xd:xon>\n"  +
"  <xd:component>\n"+
"    %class test.xdef.MyTestX_AnyXX1 %link A#testX;\n"+
"    %class test.xdef.MyTestX_AnyXX2 %link B#testX;\n"+
"  </xd:component>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			// xdef A; (array)
			s = "test.xdef.MyTestX_AnyXX1";
			assertNull(testX(xp,"A", s, "[]"));
			assertNull(testX(xp,"A", s, "[1]"));
			assertNull(testX(xp,"A", s, "[ [] ]"));
			assertNull(testX(xp,"A", s, "[ [1, true] ]"));
			assertNull(testX(xp,"A", s, "[ [\"a\"] ]"));
			assertNull(testX(xp,"A", s, "[ [\"a\"] ]"));
			assertNull(testX(xp,"A", s, "[ [1] ]"));
			assertNull(testX(xp,"A", s, "[ [1, 2] ]"));
			assertNull(testX(xp,"A", s, "[ [1], [true], [\"x\"] ]"));
			assertNull(testX(xp,"A", s, "[ [ { a:1 } ] ]"));
			assertNull(testX(xp,"A", s, "[ [ { a:1, b:[] } ] ]"));
			assertNull(testX(xp,"A", s, "[ [{}], [ { a:1, b:[1,2,3] } ] ]"));
			assertNull(testX(xp,"A", s, "[ [{}] ]"));
			assertNull(testX(xp,"A", s, "[ [ { a:[1, 2]} ] ]"));//
			assertNull(testX(xp,"A", s, "[0,{a:[1,true],b:null},false,null]"));
			assertNull(testX(xp,"A", s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			// xdef B; (map)
			s = "test.xdef.MyTestX_AnyXX2";
			assertNull(testX(xp,"B", s, "{}"));
			assertNull(testX(xp,"B", s, "{a:1}"));
			assertNull(testX(xp,"B", s, "{a:[1, true], b:null}"));
			assertNull(testX(xp,"B", s,"{a:1,b:[],c:{},d:{e:5,f:[2]},g:null}"));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = // test %anyObj
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"testX\">\n" +
"<xd:xon name=\"testX\">%anyObj</xd:xon>\n"  +
"<xd:component>%class test.xdef.MyTestX_AnyObj %link A#testX;</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			s = "test.xdef.MyTestX_AnyObj";
			genXComponent(xp, clearTempDir());
			// %anyObj
			assertNull(testX(xp,"A", s, "true"));
			assertNull(testX(xp,"A", s, "1"));
			assertNull(testX(xp,"A", s, "null"));
//TODO string ???
//			assertNull(testX(xp,"A", s, "\"\""));
//			assertNull(testX(xp,"A", s, "\"abc\""));
			// (array)
			assertNull(testX(xp,"A", s, "[]"));
			assertNull(testX(xp,"A", s, "[1]"));
			assertNull(testX(xp,"A", s, "[ [] ]"));
			assertNull(testX(xp,"A", s, "[ [1, true] ]"));
			assertNull(testX(xp,"A", s, "[ [\"a\"] ]"));
			assertNull(testX(xp,"A", s, "[ [\"a\"] ]"));
			assertNull(testX(xp,"A", s, "[ [1] ]"));
			assertNull(testX(xp,"A", s, "[ [1, 2] ]"));
			assertNull(testX(xp,"A", s, "[ [1], [true], [\"x\"] ]"));
			assertNull(testX(xp,"A", s, "[ [ { a:1 } ] ]"));
			assertNull(testX(xp,"A", s, "[ [ { a:1, b:[] } ] ]"));
			assertNull(testX(xp,"A", s, "[ [{}], [ { a:1, b:[1,2,3] } ] ]"));
			assertNull(testX(xp,"A", s, "[ [{}] ]"));
			assertNull(testX(xp,"A", s, "[ [ { a:[1, 2]} ] ]"));//
			assertNull(testX(xp,"A", s, "[0,{a:[1,true],b:null},false,null]"));
			assertNull(testX(xp,"A", s,"[{a:1,b:[3,4],c:{d:5,e:[6,7]},f:{}}]"));
			// (map)
			assertNull(testX(xp,"A", s, "{}"));
			assertNull(testX(xp,"A", s, "{a:1}"));
			assertNull(testX(xp,"A", s,"{a:1,b:[],c:{},d:{e:5,f:[2]},g:null}"));
			assertNull(testX(xp,"A", s, "{a:[1, true], b:null}"));
		} catch (Exception ex) {fail(ex);}
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