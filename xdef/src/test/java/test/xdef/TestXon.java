package test.xdef;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.json.JsonUtil;
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
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"  <xd:json name='A'>\n"+
"    [\"* " + type + "()\"]\n"+
"  </xd:json>\n"+
"  <xd:component>\n"+
"    %class bugreports.data.GJ"+ type + " %link #A;\n"+
"  </xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			x = JsonUtil.parseXON(xon);
			el = JsonUtil.jsonToXml(x);
			xd = xp.createXDDocument();
			y = xd.jvalidate(el, reporter);
			if (reporter.errorWarnings()) {
				return "" + KXmlUtils.nodeToString(el, true) + "\n" + reporter;
			}
			if (!JsonUtil.jsonEqual(x,y)) {
				return "1\n" + JsonUtil.toXonString(x)
					+ "\n" +  JsonUtil.toXonString(y);
			}
			o = xd.getXon();
			if (!JsonUtil.jsonEqual(x, o)) {
				return "2\n" + xon + "\n" +  JsonUtil.toXonString(xd.getXon());
			}
			reporter.clear();
			if (!JsonUtil.jsonEqual(JsonUtil.parseXON(xon), x)) {
				return "3\n" + xon + "\n" + JsonUtil.toXonString(x);
			}
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parseXON(xon), x),
				JsonUtil.toJsonString(x, true));
			XDTester.genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(xon, null, reporter);
			y = JsonUtil.xmlToJson(xc.toXml());
			if (!JsonUtil.jsonEqual(JsonUtil.xonToJson(x),y)) {
				return "4\n" + JsonUtil.toJsonString(JsonUtil.xonToJson(x))
					+ "\n" +  JsonUtil.toJsonString(y);
			}
			y = XComponentUtil.toXon(xc);
			if (!JsonUtil.jsonEqual(x,y)) {
				return "5\n" + xon + "\n" +  JsonUtil.toXonString(y);
			}
			return null;
		} catch (Exception ex) {
			StringWriter swr = new StringWriter();
			ex.printStackTrace(new PrintWriter(swr));
			return swr.toString();
		}
	}

	@Override
	public void test() {
/*xx*/
		assertNull(testx("int", "[ ]"));
		assertNull(testx("byte", "[ 1 ]"));
		assertNull(testx("short", "[ 1 ]"));
		assertNull(testx("int", "[ 1 ]"));
		assertNull(testx("long", "[ 1 ]"));
		assertNull(testx("integer", "[ 0i0, -0i3 ]"));
		assertNull(testx("float", "[ 1.0 ]"));
		assertNull(testx("double", "[ 1.0 ]"));
		assertNull(testx("decimal", "[ 0d0, 0d1, -0d1, 0d1.5, 0d3.33e-5 ]"));
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
//if (true) return;
/*xx*/
		String s, json, xon, xdef;
		Object x, y;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		XComponent xc;
		File tempDir = clearTempDir();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name='a'>\n" +
"[\n" +
"  {\n" +
"    a = \"? short()\",\n" +	/* Short */
"    b = \"? jstring()\",\n" +	/* string */
"    c = \"? double()\",\n" +		/* Double */
"    f = \"? boolean()\",\n" +	/* boolean */
"    g = \"? duration()\",\n" +	/* duration */
"    h = \"? jnull()\",\n" +	/* null */
"    i = [],\n" +				/* empty array */
"    Towns = [\n" +
"      \"* gps()\"\n" +
"    ],\n" +
"    j = \"? char()\",\n" +		/* char 'a' */
"    k = \"? char()\",\n" +		/* char "'" */
"    l = \"? char()\",\n" +		/* char "\\"" */
"    m = \"? char()\",\n" +		/*char '\u0007' */
"    n = \"? char()\",\n" +		/*char "\\\"" */
"    o = \"? char()\",\n" +		/*char '\n' */
"    p = \"? char()\",\n" +		/*char '\\n' */
"    q = \"? char()\",\n" +		/*char ' ' */
"    r = \"? char()\",\n" +		/*char not exists */
"    s = \"? char()\",\n" +		/*char not exists */
"    t = \"? gYear()\",\n" +	/*gYear*/
"    u = \"? gYear()\",\n" +	/*gYear*/
"    v = \"? gYear()\",\n" +	/*gYear*/
"    w = \"? gYear()\",\n" +	/*gYear*/
"    x = \"? gYear()\",\n" +	/*gYear*/
"    y = \"? gYear()\",\n" +	/*gYear*/
"    z = \"? gYear()\",\n" +	/*gYear*/
"    \" name with space \": \"? jstring()\"\n" +
"  },\n" +
"  \"float()\",\n" +
"  \"decimal()\",\n" +
"  \"byte()\",\n" +
"  \"integer()\",\n" +
"  \"integer()\",\n" +
"  \"date()\",\n" +				    /* month d(--1) */
"  \"gMonth()\",\n" +				/* month d(--1) */
"  \"gMonth()\",\n" +				/* month d(--1Z) */
"  \"gMonthDay()\",\n" +			/* --1-2 */
"  \"gMonthDay()\",\n" +			/* --1-2-01:01 */
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
"    c = -123.4e2D,                   # Double\n" +
"    f=true,                          # Boolean\n" +
"    g = P1Y1M1DT1H1M1.12S,           # Duration\n" +
"    h = null,                        # null\n" +
"    i=[],                            # empty array\n" +
"    Towns = [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),     # GPS\n" +
"      g(51.52, -0.09, 0, London),    # GPS\n" +
"      g(50.08, 14.42, 399, \"Praha (centrum)\") # GPS\n" +
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
"  }, /**** end of map ****/\n" +
"  -3F,                               # Float\n" +
"  -0d3.1,                            # BigDecimal\n" +
"  -2B,                               # Byte\n" +
"  0i1,                               # BigInteger\n" +
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
			x = JsonUtil.parseXON(xon);
			s = JsonUtil.toJsonString(x, true);
			JsonUtil.parse(s);
			s = JsonUtil.toXonString(x, true);
			y = JsonUtil.parseXON(s);
			assertTrue(JsonUtil.jsonEqual(x,y));
			s = JsonUtil.toXonString(x, false);
			List list = (List) ((Map) ((List) x).get(0)).get("Towns");
			assertEq("Wien",((GPSPosition) list.get(0)).name());
			assertEq("London",((GPSPosition) list.get(1)).name());
			assertEq("Praha (centrum)",((GPSPosition) list.get(2)).name());
			assertEq(1233, Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(1)))/1000));
			assertEq(252,Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(2)))/1000));
			assertEq(1030,Math.round(((GPSPosition) list.get(1)).distanceTo(
				((GPSPosition) list.get(2)))/1000));
			assertNoErrors(reporter);
			json = JsonUtil.toXonString(x, true);
			JsonUtil.parseXON(json);
			y = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			y = XComponentUtil.toXon(xc);
			assertTrue(JsonUtil.jsonEqual(x,y));
			x = xc.toJson();
			y = JsonUtil.xonToJson(y);
			assertTrue(JsonUtil.jsonEqual(x,y));
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