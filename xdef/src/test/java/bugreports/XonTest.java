package bugreports;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test XON data.*/
public class XonTest extends XDTester {

	public XonTest() {super();}

	private static void display(final Object o) {
		if (o==null) {
			System.out.println("null");
		} else if (o instanceof Map) {
			Map m = (Map) o;
			for (Object x: m.entrySet()) {
				Map.Entry en = (Map.Entry) x;
				System.out.print(en.getKey() + " = ");
				display(en.getValue());
			}
		} else if (o instanceof List) {
			List list = (List) o;
			for (int i=0; i < list.size(); i++) {
				System.out.print("[" + i + "] ");
				display(list.get(i));
			}
		} else {
			System.out.println(o + "; " + o.getClass().getName());
		}
	}

	@Override
	public void test() {
		String s, json, xdef;
		Object x, y;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		XComponent xc;

		String tempDir = getTempDir();
		File f = new File(getTempDir());
		if (f.exists() && !f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()
				+ " is not directory");
		}
		f.mkdir();
		tempDir = f.getAbsolutePath().replace('\\', '/');
		if (!tempDir.endsWith("/")) {
			tempDir += '/';
		}
		if (!f.isDirectory()) {
			fail('\"' + tempDir + "\" is not directory");
			return;
		}
/*xx*
		try {
			s = "[d(2000),d(19:23:01.0)]";
			x = JsonUtil.parseXON(s);
			json = JsonUtil.toJsonString(JsonUtil.xonToJson(x), true);
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name='a'>\n" +
"[\"gYear()\", \"time()\"]\n" +
"</xd:json>\n" +
"<xd:component>\n"+
"  %class bugreports.data.Xon0 %link #a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			x = jparse(xp, "", json, reporter);
			genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			assertNoErrors(reporter);
			x = xc.toJson();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), x),
				json + "\n===\n" + JsonUtil.toJsonString(x, true));
		} catch (Exception ex) {fail(ex);}
if (true)return;
/*xx*/
		try {
			s =
"/* Start of XON test */\n" +
"[ /***** Array *****/\n" +
"  { /***** Map *****/\n" +
"    a = 1S,                          /* Short */\n" +
"    b = \"ab cd\",                     /* string */\n" +
"    c = -123.4e2,                    /* Double */\n" +
"    f = true,                        /* boolean */\n" +
"    g = p(P1Y1M1DT1H1M1.12S),        /* duration */\n" +
"    h = null,                        /* null */\n" +
"    i=[],                            /* empty array */\n" +
"    Towns = [ /* array with GPS locations of towns */\n" +
"      g(48.2,16.37,151, Wien),\n" +
"      g(51.52,-0.09,0,London),\n" +
"      g(50.08, 14.42, 399, \"Praha (centrum)\"),\n" +
"    ],\n" +
"    j = '?',                        /* character */\n" +
"    k = '\\u0007',                   /* character */\n" +
"    l = ''',                        /* character */\n" +
"    \"m\" : '\\n',                     /* character */\n" +
"    \"name with space\" : \"First\"     /* name with a space must be quoted! */\n" +
"  }, /**** end of map ****/\n" +
"  -3F,                               /* float */\n" +
"  -3d,                               /* decimal */\n" +
"  999999999999999999999999999999999, /* big integer */\n" +
"  d(--1),                            /* month */\n" +
"  d(--1Z),                           /* month zone*/\n" +
"  d(--1-2),                          /* month day */\n" +
"  d(--1-2-01:01),                    /* month day zone */\n" +
"  d(19:23:01),						/* hours, minutes seconds */\n" +
"  d(19:23:01.012),                   /* hours minutes seconds millis */\n" +
"  d(0:0:0.00001+00:00),              /* time nanos zone */\n" +
"  d(2000),                           /* year (without zone) */\n" +
"  d(-123456789),                     /* year (without zone) */\n" +
"  d(2000Z),                          /* year zone */\n" +
"  d(2000-01:00),                     /* year zone */\n" +
"  d(2000-1),                         /* year month */\n" +
"  d(2000-1Z),                        /* year month zone */\n" +
"  d(2000-1-01:00),                   /* year month zone */\n" +
"  d(2021-01-12T01:10:11.54012-00:01),/* date and time (nanos, zone) */\n" +
"  g(-0,+1),                          /* GPS */\n" +
"  b(HbRBHbRBHQw=),                   /* byte array (base64) */\n" +
"  #(123.45 CZK),                     /* currency ammount */ \n" +
"  #(12 USD),                         /* currency ammoun */\n" +
"] /**** end of array ****/\n" +
"/* End of XON test */";
//			System.out.println(s);
			System.out.println("=====================");
			x = JsonUtil.parseXON(s);
//			display(x);
			json = JsonUtil.toJsonString(JsonUtil.xonToJson(x), true);
			JsonUtil.parse(json);
			s = JsonUtil.toXonString(x, true);
			y = JsonUtil.parseXON(s);
			assertTrue(JsonUtil.jsonEqual(x,y));
			s = JsonUtil.toXonString(x, false);
			y = JsonUtil.parseXON(s);
			assertTrue(JsonUtil.jsonEqual(x,y));
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
"    \"a\" : \"int()\",\n" +		/* Short */
"    \"b\" : \"string()\",\n" +		/* string */
"    \"c\" : \"float()\",\n" +		/* Double */
"    \"f\" : \"boolean()\",\n" +	/* boolean */
"    \"g\" : \"duration()\",\n" +	/* duration */
"    \"h\" : \"jnull()\",\n" +		/* null */
"    \"i\" : [],\n" +				/* empty array */
"    \"Towns\" : [\n" +
"      \"gps()\",\n" +
"      \"gps()\",\n" +
"      \"gps()\"\n" +
"    ],\n" +
"    \"j\" : \"string()\",\n" +		/* char '?' */
"    \"k\" : \"string()\",\n" +		/* char '\\u0007' */
"    \"l\" : \"string()\",\n" +		/* char ''' */
"    \"m\" : \"string()\",\n" +		//char '\n' */
"    \"name with space\" : \"string()\"\n" +
"  },\n" +
"  \"float()\",\n" +
"  \"decimal()\",\n" +
"  \"integer()\",\n" +
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
			jparse(xp, "", json, reporter);
			genXComponent(xp, tempDir);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			assertNoErrors(reporter);
//			assertTrue(JsonUtil.jsonEqual(x, XComponentUtil.toXon(xc)));
			x = xc.toJson();
//			display(x);
//			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), x),
//				json + "\n===\n" + JsonUtil.toJsonString(xc.toJson(), true));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}