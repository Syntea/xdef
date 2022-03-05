package test.xdef;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
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
import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test XON data. */
public class TestXon extends XDTester {

	public TestXon() {super();}

	private String testx(final String type, final String xon) {
		Object o, x, y;
		XDDocument xd;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XComponent xc;
		try {
			String xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:xon name='A'>\n"+
"    [\"* " + type + "()\"]\n"+
"  </xd:xon>\n"+
"  <xd:component>\n"+
"    %class test.xdef.GJ"+ type + " %link #A;\n"+
"  </xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			x = XonUtils.parseXON(xon);
			el = XonUtils.xonToXml(x);
			xd = xp.createXDDocument();
			y = xd.jvalidate(el, reporter);
			if (reporter.errorWarnings()) {
				return "" + KXmlUtils.nodeToString(el, true) + "\n" + reporter;
			}
			if (!XonUtils.xonEqual(x,y)) {
				return "** 1 **\n"+XonUtils.toXonString(x)
					+ "\n" +  XonUtils.toXonString(y);
			}
			o = xd.getXon();
			if (!XonUtils.xonEqual(x, o)) {
				return "** 2 **\n"+xon+"\n" + XonUtils.toXonString(xd.getXon());
			}
			reporter.clear();
			if (!XonUtils.xonEqual(XonUtils.parseXON(xon), x)) {
				return "** 3 **\n" + xon + "\n" + XonUtils.toXonString(x);
			}
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(xon), x),
				XonUtils.toJsonString(x, true));
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xc = xp.createXDDocument().jparseXComponent(xon, null, reporter);
			y = XonUtils.xmlToXon(xc.toXml());
			if (!XonUtils.xonEqual(XonUtils.xonToJson(x),XonUtils.xonToJson(y))) {
				return "** 4 **\n" + XonUtils.toJsonString(XonUtils.xonToJson(x))
					+ "\n" +  XonUtils.toJsonString(XonUtils.xonToJson(y));
			}
			y = XComponentUtil.toXon(xc);
			if (!XonUtils.xonEqual(x,y)) {
				return "** 5 **\n" + xon + "\n" +  XonUtils.toXonString(y);
			}
			xd = xp.createXDDocument();
			xd.setXONContext(x);
			xc = xd.jcreateXComponent("A", null, reporter);
			y = XComponentUtil.toXon(xc);
			if (!XonUtils.xonEqual(x,y)) {
				return "** 6 **\n" + xon + "\n" +  XonUtils.toXonString(y);
			}
			return null;
		} catch (Exception ex) {return printThrowable(ex);}
	}
	private String testy(final String type, final String xon) {
		XComponent xc;
		XDPool xp;
		Object o, x, y;
		try {
			String xdef = // Test map
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"{ a=\"" + type + "();\" }\n" +
"</xd:xon>\n" +
"<xd:component>%class test.xdef.TestEmptyMap %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			o = XonUtils.parseXON(xon);
			x = xp.createXDDocument().jparse(xon, null);
			if (!XonUtils.xonEqual(o,x)) {
				return "1\n" + xon + "\n" +  XonUtils.toXonString(x);
			}
			XDDocument xd = xp.createXDDocument();
			xd.setXONContext(o);
			xc = xd.jcreateXComponent("A", null, null);
			y = XComponentUtil.toXon(xc);
			if (!XonUtils.xonEqual(o,y)) {
				return "1\n" + xon + "\n" +  XonUtils.toXonString(y);
			}
			return null;
		} catch (Exception ex) {return printThrowable(ex);}
	}

	@Override
	public void test() {
		assertNull(testx("int", "[ ]"));
		assertNull(testx("jnull", "[ null ]"));
		assertNull(testx("byte", "[ 1, -3 ]"));
		assertNull(testx("short", "[ 1 ]"));
		assertNull(testx("int", "[ 1 ]"));
		assertNull(testx("long", "[ 1 ]"));
		assertNull(testx("integer", "[ 0N, -3N ]"));
		assertNull(testx("float", "[ 1.0 ]"));
		assertNull(testx("double", "[ 1.0 ]"));
		assertNull(testx("decimal", "[ 0d, 1d, -1d, 1.5d, 3.33e-5d ]"));
		assertNull(testx("date",
			"[ D2021-01-12, D1999-01-05+01:01, D1998-12-21Z ]"));
		assertNull(testx("gYear", "[ D2021+01:00, D1999, D-0012Z ]"));
		assertNull(testx("gps",
			"[ g(20.21,19.99),g(20.21, 19.99,0.1),g(51.52,-0.09,0,London) ]"));
		assertNull(testx("price", "[ p(20.21 CZK), p(19.99 USD) ]"));
		assertNull(testx("char",
			"[ c\"a\", c\"'\", c\"\\\"\", c\"\\u0007\", c\"\\\\\" ]"));
		assertNull(testx("anyURI", "[ u\"http://a.b\" ]"));
		assertNull(testx("emailAddr","[ e\"tro@volny.cz\",e\"a b<x@y.zz>\" ]"));
		assertNull(testx("file", "[ \"temp/a.txt\" ]"));
		assertNull(testx("ipAddr", "[/::FFFF:129.144.52.38,/0.0.0]\n"));
		assertNull(testx("currency", "[C(USD), C(CZK)]\n"));
//		assertNull(testx("telephone", "[T\"123 456\",T\"+420 234 567 890\"]\n"));

		assertNull(testy("? int", "{a=1}"));
		assertNull(testy("? int", "{ }"));

		String s, json, xon, xdef, xml;
		List list;
		Object o, x, y;
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		XComponent xc;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' name='M' root='y:X'\n"+
"        xmlns:y='a.b'>\n"+
"<xd:declaration>type gam xdatetime('yyyyMMddHHmmssSS');</xd:declaration>\n"+
"  <y:X a = '?date()' t='gam();' >? int() <y:Y xd:script='*'/>dec()</y:X>\n"+
"<xd:component>%class test.xdef.MGam %link y:X</xd:component>\n"+
//?????????
//"<xd:component>%class test.xdef.Mgam %link y:X</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xml =
"<n:X xmlns:n='a.b' a='2021-12-30' t='2020121101010101'>1<n:Y/><n:Y/>2.0</n:X>";
			xc = parseXC(xp,"M", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			o = XonUtils.parseXON(
"{n:X = [\n" +
"    { a = D2021-12-30,\n" +
"      t = D2020-12-11T01:01:01.01,\n" +
"      xmlns:n = \"a.b\"\n" +
"    },\n" +
"    1I,\n" +
"    {n:Y = []},\n" +
"    {n:Y = []},\n" +
"    2.0d\n" +
"  ]\n" +
"}");
			assertTrue(XonUtils.xonEqual(o, XComponentUtil.toXon(xc)));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"[\n" +
"  {\n" +
"    a = \"? short()\",\n" +
"    b = \"? jstring()\",\n" +
"    c = \"? double()\",\n" +
"    f = \"? boolean()\",\n" +
"    g = \"? duration()\",\n" +
"    h = \"? jnull()\",\n" +	/* null */
"    i = [],\n" +
"    Towns = [\n" +
"      \"* gps()\"\n" +
"    ],\n" +
"    j = \"? char()\",\n" +
"    k = \"? char()\",\n" +
"    l = \"? char()\",\n" +
"    m = \"? char()\",\n" +		/*char '\u0007' */
"    n = \"? char()\",\n" +
"    o = \"? char()\",\n" +
"    p = \"? char()\",\n" +
"    q = \"? char()\",\n" +
"    t = \"? gYear()\",\n" +
"    u = \"? gYear()\",\n" +
"    v = \"? gYear()\",\n" +
"    w = \"? gYear()\",\n" +
"    \" name with space \": \"? jstring()\"\n" +
"  },\n" +
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
"    a = 1S,                          # Short\n" +
"    b = \"ab cd\",                     # String\n" +
"    c = -123.4e2d,                   # Double\n" +
"    f=true,                          # Boolean\n" +
"    g = P1Y1M1DT1H1M1.12S,           # Duration\n" +
"    h = null,                        # null\n" +
"    i=[],                            # empty array\n" +
"    Towns = [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      g(50.08, 14.42, 399, \"Prague old town\")\n" +
"    ],\n" +
"    j = c\"a\",                      # Character\n" +
"    k = c\"'\",                      # Character\n" +
"    l = c\"\\\"\",                   # Character\n" +
"    m = c\"\\u0007\",                # Character\n" +
"    n = c\"\\\\\",                   # Character\n" +
"    o = c\"\n\",                     # Character\n" +
"    p = c\"\\n\",                    # Character\n" +
"    q = c\" \",                      # Character\n" +
"    t = D0001,                       # year (without zone)\n" +
"    u = D-0001,                      # year (without zone)\n" +
"    v = D123456789Z,                 # year zone\n" +
"    w = D-0001-01:00,                # year zone\n" +
"    \" name with space \": \"x\\ty\" # name with space is quoted!\n" +
"  },  /**** end of map ****/\n" +
"  -3F,                               # Float\n" +
"  -3.1d,                             # BigDecimal\n" +
"  -2B,                               # Byte\n" +
"  1N,                                # BigInteger\n" +
"  999999999999999999999999999999999, # big integer (authomatic)\n" +
"  D2021-01-11,                       # date\n" +
"  D--11,                             # month\n" +
"  D--02Z,                            # month zone\n" +
"  D--11-22,                          # month day\n" +
"  D--03-04-01:01,                    # month day zone\n" +
"  D19:23:01,                         # hours, minutes seconds\n" +
"  D19:23:01.012,                     # hours minutes seconds millis\n" +
"  D00:00:00.00001+00:00,             # time nanos zone\n" +
"  D2000-11Z,                         # year month zone\n" +
"  D2000-10-01:00,                    # year month zone\n" +
"  D2000-10,                          # year month; no zone\n" +
"  D2021-01-12T01:10:11.54012-00:01,  # date and time (nanos, zone)\n" +
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
			assertEq("Prague old town",((GPSPosition) list.get(2)).name());
			assertEq(1233, Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(1)))/1000));
			assertEq(252,Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(2)))/1000));
			assertEq(1030,Math.round(((GPSPosition) list.get(1)).distanceTo(
				((GPSPosition) list.get(2)))/1000));
			assertNoErrors(reporter);
			json = XonUtils.toXonString(x, true);
			XonUtils.parseXON(json);
			y = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			o = y = XComponentUtil.toXon(xc);
			assertTrue(XonUtils.xonEqual(x,y));
			x = XonUtils.xmlToXon(xc.toXml());
			assertTrue(XonUtils.xonEqual(x, XonUtils.xonToJson(y)));
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			y = XComponentUtil.toXon(xc);
			assertTrue(XonUtils.xonEqual(o,y));
		} catch (Exception ex) {fail(ex);}
		try { // XON from CSV
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='CSV'>\n"+
"<xd:component>%class test.xdef.testXON.XonFromCsv %link CSV</xd:component>\n"+
"<xd:xon name=\"CSV\">\n"+
"[\n" +
"  [\"fixed 'Name'\",\"fixed 'Email'\",\"fixed 'Mobile Number'\"],\n"+
"  [$script=\"+\",\n"+
"    \"string()\",\n"+
"    \"union(%item=[emailAddr(), jnull])\",\n"+
"    \"union(%item=[telephone(), jnull])\"\n"+
"  ]\n" +
"]\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = compile(xdef);
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp, clearTempDir()).checkAndThrowErrors();
			xd = xp.createXDDocument();
			s =
"[\n" +
" [\"Name\", \"Email\", \"Mobile Number\"],\n"+
" [\"Helena \\\"\\\"Klímová\",\"hklimova@volny.cz\",\"+420 602 345 678\"],\n" +
" [\"Eva Kuželová, Epor \\\"Prix\\\"\", \"epor@email.cz\", null],\n" +
" [\"Jirová\", null, null]\n" +
"]";
			o = xd.jparse(s, reporter);
			assertNoErrorsAndClear(reporter);
			genXComponent(xd.getXDPool(), clearTempDir()).checkAndThrowErrors();
			xc = xd.jparseXComponent(s, null, reporter);
			x = XComponentUtil.toXon(xc);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, x));
			list = (List) ((List) x).get(0);
			assertEq("Name", list.get(0));
			list = (List) ((List) x).get(1);
			assertEq("Helena \"\"Klímová", list.get(0));
			assertTrue(list.get(1) instanceof XDEmailAddr);
			assertEq("hklimova@volny.cz", list.get(1).toString());
			assertTrue(list.get(2) instanceof XDTelephone);
			assertEq("+420 602 345 678", list.get(2).toString());
			list = (List) ((List) x).get(2);
			assertNull(list.get(2));
			list = (List) ((List) x).get(3);
			assertNull(list.get(1));
			assertNull(list.get(2));
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