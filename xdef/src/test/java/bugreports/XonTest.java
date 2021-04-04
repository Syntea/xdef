package bugreports;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SUtils;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test XON data. */
public class XonTest extends XDTester {

	public XonTest() {super();}

	@Override
	public void test() {
		String s, json, xon, xdef;
		Object x, y;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XComponent xc;
		String tempDir = getTempDir();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"  <xd:json name='A'>\n"+
"    {\" \tspaces \\u0007\\n\\b in name\\r\": \"jstring();\"}\n"+
"  </xd:json>\n"+
"  <xd:component>\n"+
"    %class bugreports.data.GJson %link #A;\n"+
"  </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\" \tspaces \\u0007\n\\b in name\\r\": \" x \r\t y \"}";
			x = JsonUtil.parse(json);
			el = JsonUtil.jsonToXml(x);
			y = xp.createXDDocument().jparse(el, reporter);
			assertTrue(JsonUtil.jsonEqual(x,y));
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), x),
				JsonUtil.toJsonString(x, true));
			XDTester.genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			y = JsonUtil.xmlToJson(xc.toXml());
			assertTrue(JsonUtil.jsonEqual(x,y));
			y = JsonUtil.xonToJson(XComponentUtil.toXon(xc));
			assertTrue(JsonUtil.jsonEqual(x,y));
		} catch (Exception ex) {fail(ex);}
//if (true) return;
		try {
			xon =
"# Start of XON example\n" +
"[ #***** Array *****/\n" +
"  { #***** Map *****/\n" +
"    a : 1S,                          # Short\n" +
"    b : \"ab cd\",                     # String\n" +
"    c : -123.4e2D,                   # Double\n" +
"    f:true,                        # Boolean\n" +
"    g : P1Y1M1DT1H1M1.12S,           # Duration\n" +
"    h : null,                        # null\n" +
"    i:[],                            # empty array\n" +
"    Towns : [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),     # GPS\n" +
"      g(51.52, -0.09, 0, London),    # GPS\n" +
"      g(50.08, 14.42, 399, \"Praha (centrum)\"), # GPS\n" +
"    ],\n" +
"    j : '\\u0007',                    # Character\n" +
"    k : '\\n',                        # Character\n" +
"    l : '\"',                         # Character\n" +
"    m : ''',                         # Character\n" +
"    n : '\\\\',                        # Character\n" +
"    \" name with space \": \"x\\ty\" /* name with space is quoted! */\n" +
"  }, /**** end of map ****/\n" +
"  -3F,                               # Float\n" +
"  -3d,                               # BigDecimal\n" +
"  -2B,                               # Byte\n" +
"  0N,                                # BigInteger\n" +
"  999999999999999999999999999999999, /* big integer (authomatic)*/\n" +
"  D2021-01-11,                       /* date */\n" +
"  D--11,                             /* month */\n" +
"  D--02Z,                            /* month zone*/\n" +
"  D--11-22,                          /* month day */\n" +
"  D--03-04-01:01,                    /* month day zone */\n" +
"  D19:23:01,                         /* hours, minutes seconds */\n" +
"  D19:23:01.012,                     /* hours minutes seconds millis */\n" +
"  D00:00:00.00001+00:00,             /* time nanos zone */\n" +
"  D2000,                             /* year (without zone) */\n" +
"  D-123456789,                       /* year (without zone) */\n" +
"  D2000Z,                            /* year zone */\n" +
"  D2000-01:00,                       /* year zone */\n" +
"  D2000-10,                          /* year month */\n" +
"  D2000-11Z,                         /* year month zone */\n" +
"  D2000-10-01:00,                    /* year month zone */\n" +
"  D2021-01-12T01:10:11.54012-00:01,  /* date and time (nanos, zone) */\n" +
"  g(-0, +1),                         /* GPS */\n" +
"  b(HbRBHbRBHQw=),                   /* byte array (base64) */\n" +
"  p(123.45 CZK),                     /* price */ \n" +
"  p(12 USD),                         /* price */\n" +
"] /**** end of array ****/\n" +
"# End of XON example";
			x = JsonUtil.parseXON(xon);
			json = JsonUtil.toJsonString(JsonUtil.xonToJson(x), true);
//			json = JsonUtil.toJsonString(x, true);
			JsonUtil.parse(json);
			s = JsonUtil.toXonString(x, true);
			y = JsonUtil.parseXON(s);
			assertTrue(JsonUtil.jsonEqual(x,y));
//if (true) return;
			s = JsonUtil.toXonString(x, false);
			List list = (List) ((Map) ((List) x).get(0)).get("Towns");
			assertEq("Wien",((GPSPosition) list.get(0)).name());
			assertEq("London",((GPSPosition) list.get(1)).name());
			assertEq("Praha (centrum)",((GPSPosition) list.get(2)).name());
			assertEq(1234, Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(1)))/1000));
			assertEq(253,Math.round(((GPSPosition) list.get(0)).distanceTo(
				((GPSPosition) list.get(2)))/1000));
			assertEq(1031,Math.round(((GPSPosition) list.get(1)).distanceTo(
				((GPSPosition) list.get(2)))/1000));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name='a'>\n" +
"[\n" +
"  {\n" +
"    \"a\" : \"short()\",\n" +		/* Short */
"    \"b\" : \"jstring()\",\n" +	/* string */
"    \"c\" : \"double()\",\n" +		/* Double */
"    \"f\" : \"boolean()\",\n" +	/* boolean */
"    \"g\" : \"duration()\",\n" +	/* duration */
"    \"h\" : \"jnull()\",\n" +		/* null */
"    \"i\" : [],\n" +				/* empty array */
"    \"Towns\" : [\n" +
"      \"gps()\",\n" +
"      \"gps()\",\n" +
"      \"gps()\"\n" +
"    ],\n" +
"    \"j\" : \"char()\",\n" +		/* char '?' */
"    \"k\" : \"char()\",\n" +		/* char '\\u0007' */
"    \"l\" : \"char()\",\n" +		/* char ''' */
"    \"m\" : \"char()\",\n" +		//char '\n' */
"    \"n\" : \"char()\",\n" +		//char '\\' */
"    \" name with space \": \"jstring()\"\n" +
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
"  \"gYear()\",\n" +
"  \"gYear()\",\n" +
"  \"gYear()\",\n" +
"  \"gYear()\",\n" +
"  \"gYearMonth()\",\n" +
"  \"gYearMonth()\",\n" +
"  \"gYearMonth\",\n" +
"  \"dateTime()\",\n" +
"  \"gps()\",\n" +
"  \"base64Binary()\",\n" +
"  \"price()\",\n" +
"  \"price()\"\n" +
"]\n" +
"</xd:json>\n" +
"<xd:component>\n"+
"  %class bugreports.data.Xon %link #a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			y = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			genXComponent(xp, tempDir);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			y = XComponentUtil.toXon(xc);
			assertTrue(JsonUtil.jsonEqual(x,y));
			x = xc.toJson();
			y = JsonUtil.xonToJson(y);
				assertTrue(JsonUtil.jsonEqual(x,y));
		} catch (Exception ex) {fail(ex);}
		try {
			SUtils.deleteAll(tempDir, true);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}