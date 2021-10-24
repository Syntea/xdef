package test.xdef;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.xon.XonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

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
		String tempDir = getTempDir();
		try {
			String xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:json name='A'>\n"+
"    [\"* " + type + "()\"]\n"+
"  </xd:json>\n"+
"  <xd:component>\n"+
"    %class bugreports.data.GJ"+ type + " %link #A;\n"+
"  </xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			x = XonUtil.parseXON(xon);
			el = XonUtil.xonToXml(x);
			xd = xp.createXDDocument();
			y = xd.jvalidate(el, reporter);
			if (reporter.errorWarnings()) {
				return "" + KXmlUtils.nodeToString(el, true) + "\n" + reporter;
			}
			if (!XonUtil.xonEqual(x,y)) {
				return "1\n" + XonUtil.toXonString(x)
					+ "\n" +  XonUtil.toXonString(y);
			}
			o = xd.getXon();
			if (!XonUtil.xonEqual(x, o)) {
				return "2\n" + xon + "\n" +  XonUtil.toXonString(xd.getXon());
			}
			reporter.clear();
			if (!XonUtil.xonEqual(XonUtil.parseXON(xon), x)) {
				return "3\n" + xon + "\n" + XonUtil.toXonString(x);
			}
			assertTrue(XonUtil.xonEqual(XonUtil.parseXON(xon), x),
				XonUtil.toJsonString(x, true));
			XDTester.genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(xon, null, reporter);
			y = XonUtil.xmlToJson(xc.toXml());
			if (!XonUtil.xonEqual(XonUtil.xonToJson(x),y)) {
				return "4\n" + XonUtil.toJsonString(XonUtil.xonToJson(x))
					+ "\n" +  XonUtil.toJsonString(y);
			}
			y = XComponentUtil.toXon(xc);
			if (!XonUtil.xonEqual(x,y)) {
				return "5\n" + xon + "\n" +  XonUtil.toXonString(y);
			}
			return null;
		} catch (Exception ex) {return printThrowable(ex);}
	}

	@Override
	public void test() {
//		assertNull(testx("decimal", "[ 0d, 1d, -1d, 1.5d, 3.33e-5d ]"));
//if (true) return;
		assertNull(testx("int", "[ ]"));
		assertNull(testx("byte", "[ 1, -3 ]"));
		assertNull(testx("short", "[ 1 ]"));
		assertNull(testx("int", "[ 1 ]"));
		assertNull(testx("long", "[ 1 ]"));
		assertNull(testx("integer", "[ 0N, -3N ]"));
		assertNull(testx("float", "[ 1.0 ]"));
		assertNull(testx("double", "[ 1.0 ]"));
//		assertNull(testx("decimal", "[ 0d, 1d, -1d, 1.5d, 3.33e-5d ]"));
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
		String s, json, xon, xdef;
		Object x, y;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		XComponent xc;
		File tempDir = clearTempDir();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='a'>\n"+
"<xd:json name='a'>\n" +
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
"    r = \"? char()\",\n" +
"    s = \"? char()\",\n" +
"    t = \"? gYear()\",\n" +
"    u = \"? gYear()\",\n" +
"    v = \"? gYear()\",\n" +
"    w = \"? gYear()\",\n" +
"    x = \"? gYear()\",\n" +
"    y = \"? gYear()\",\n" +
"    z = \"? gYear()\",\n" +
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
"  \"? price()\"\n" +
"]\n" +
"</xd:json>\n" +
"<xd:component>\n"+
"  %class bugreports.data.Xon %link #a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			xon =
"# Start of XON example\n" +
"[ #***** Array *****/\n" +
"  { #***** Map *****/\n" +
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
"    j = c\"a\",                        # Character\n" +
"    k = c\"'\",                        # Character\n" +
"    l = c\"\\\"\",                     # Character\n" +
"    m = c\"\\u0007\",                  # Character\n" +
"    n = c\"\\\\\",                     # Character\n" +
"    o = c\"\n\",                       # Character\n" +
"    p = c\"\\n\",                      # Character\n" +
"    q = c\" \",                        # Character\n" +
"    t = D0001,                       # year (without zone)\n" +
"    u = D-0001,                      # year (without zone)\n" +
"    v = D123456789Z,                 # year zone\n" +
"    w = D-0001-01:00,                # year zone\n" +
"    \" name with space \": \"x\\ty\" # name with space is quoted!\n" +
"  }, /**** end of map ****/\n" +
"  -3F,                               # Float\n" +
"  -3.1d,                             # BigDecimal\n" +
"  -2B,                               # Byte\n" +
"  1N,                                # BigInteger\n" +
"  999999999999999999999999999999999, /* big integer (authomatic)*/\n" +
"  D2021-01-11,                       /* date */\n" +
"  D--11,                             /* month */\n" +
"  D--02Z,                            /* month zone*/\n" +
"  D--11-22,                          /* month day */\n" +
"  D--03-04-01:01,                    /* month day zone */\n" +
"  D19:23:01,                         /* hours, minutes seconds */\n" +
"  D19:23:01.012,                     /* hours minutes seconds millis */\n" +
"  D00:00:00.00001+00:00,             /* time nanos zone */\n" +
"  D2000-11Z,                         /* year month zone */\n" +
"  D2000-10-01:00,                    /* year month zone */\n" +
"  D2000-10,                          /* year month; no zone */\n" +
"  D2021-01-12T01:10:11.54012-00:01,  /* date and time (nanos, zone) */\n" +
"  g(-0, +1),                         /* GPS */\n" +
"  b(HbRBHbRBHQw=),                   /* byte array (base64) */\n" +
"  p(123.45 CZK),                     /* price */ \n" +
"  p(12 USD),                         /* price */\n" +
"] /**** end of array ****/\n" +
"# End of XON example";
			x = XonUtil.parseXON(xon);
			s = XonUtil.toJsonString(x, true);
			XonUtil.parseJSON(s);
			s = XonUtil.toXonString(x, true);
			y = XonUtil.parseXON(s);
			assertTrue(XonUtil.xonEqual(x,y));
			s = XonUtil.toXonString(x, false);
			List list = (List) ((Map) ((List) x).get(0)).get("Towns");
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
			json = XonUtil.toXonString(x, true);
			XonUtil.parseXON(json);
			y = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			y = XComponentUtil.toXon(xc);
			assertTrue(XonUtil.xonEqual(x,y));
			x = xc.toJson();
			y = XonUtil.xonToJson(y);
			assertTrue(XonUtil.xonEqual(x,y));
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