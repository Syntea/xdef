package bugreports;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.json.JNull;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import static test.XDTester.genXComponent;

/** Test XON data.*/
public class XonTest extends XDTester {

	public XonTest() {super();}

	private static void display(final Object x,
		final Object y, final String id) {
		if (id != null) {
			System.err.println("***** BEG " + id + " *****");
		}
		if (x==null || x instanceof JNull) {
			System.err.print("null");
			if (y != null && !(y instanceof JNull)) {
				System.err.print("; Par y: " + y.getClass().getName());
			}
			System.err.println();
		} else if (x instanceof Map) {
			System.err.println("\nMap");
			Map m = (Map) x;
			if (!(y instanceof Map)) {
				System.err.println("Par y: " + y.getClass().getName());
			} else {
				Map n = (Map) y;
				for (Object z: m.entrySet()) {
					Map.Entry en = (Map.Entry) z;
					System.err.print(en.getKey() + " = ");
					display(en.getValue(), n.get(en.getKey()), null);
				}
				System.err.println("Map END");
			}
		} else if (x instanceof List) {
			System.err.println("\nArray");
			List a = (List) x;
			if (!(y instanceof List)) {
				System.err.println("Par y: " + y.getClass().getName());
			} else {
				List b = (List) y;
				for (int i=0; i < a.size(); i++) {
					System.err.print("[" + i + "] ");
					display(a.get(i), b.get(i), null);
				}
				System.err.println("Array END");
			}
		} else if (x instanceof Number) {
			System.err.print(x + "; " + x.getClass().getName());
			if (!(y instanceof Number)) {
				System.err.print("; Par y: "
					+ (y == null ? "null" : y.getClass().getName()));
			} else if (((Number)x).doubleValue() != ((Number)y).doubleValue()) {
				System.err.print("; Par2: " + y + "; "+x.getClass().getName());
			}
			System.err.println();
		} else {
			System.err.print(x + "; " + x.getClass().getName());
			try {
				byte[] b1 = (byte[]) x;
				byte[] b2 = (byte[]) y;
				if (!Arrays.equals(b1, b2)) {
					System.err.print("; Par2: " + y + "; [B");
				}
				System.err.println();
				return;
			} catch (Exception ex) {}
			if (!x.equals(y)) {
				try {
					System.err.print("; Par y: "
						+ (y == null ? "null" : y+"; "+y.getClass().getName()));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			System.err.println();
		}
		if (id != null) {
			System.err.println("***** END " + id + " *****");
		}
	}

	@Override
	public void test() {
		String s, json, xon, xdef;
		Object x, y;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
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
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"  <xd:json name='A'>\n"+
"    {\"b\": \"char();\"}\n"+
"  </xd:json>\n"+
"  <xd:component>\n"+
"    %class bugreports.data.GJson %link #A;\n"+
"  </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
//			json = "{\"a\":[\"2021-03-10\", \"1999-01-01-01:00\"], \"b\": \"\n\"}";
//			json = "{\"b\": \"x\"}";
//			json = "{\"b\": \"x\"}";
//			json = "{\"b\": \"\n\"}";
//			json = "{\"b\": \"\\\\\"}";
			json = "{\"b\": \"\\u0007\"}";
//			json = "{\"b\": \"\\\"\"}";
			x = JsonUtil.parse(json);
			el = JsonUtil.jsonToXml(x);
			y = xp.createXDDocument().jparse(json, reporter);
			if (!JsonUtil.jsonEqual(x,y)) {
				display(x, y, "-1");
				fail();
			}
			if (reporter.errors()) {
				System.err.println(KXmlUtils.nodeToString(el, true));
				fail(reporter);
			}
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), x),
				JsonUtil.toJsonString(x, true));
			XDTester.genXComponent(xp, tempDir);
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			y = JsonUtil.xmlToJson(xc.toXml());
			if (!JsonUtil.jsonEqual(x,y)) {
				System.err.println(KXmlUtils.nodeToString(el, true));
				System.err.println(KXmlUtils.nodeToString(xc.toXml(), true));
				System.err.println(JsonUtil.toJsonString(x, true));
				System.err.println(JsonUtil.toJsonString(y, true));
				display(x, y, "-2");
				fail();
			}
//			assertTrue(JsonUtil.jsonEqual(x,y));
			y = JsonUtil.xonToJson(XComponentUtil.toXon(xc));
			if (!JsonUtil.jsonEqual(x,y)) {
				display(x, y, "-3");
				fail();
			}
//			assertTrue(JsonUtil.jsonEqual(x,y));
		} catch (Exception ex) {fail(ex);}
//if (true) return;
		try {
			xon =
"/* Start of XON test */\n" +
"[ /***** Array *****/\n" +
"  { /***** Map *****/\n" +
"    a = 1S,                          /* Short */\n" +
"    b = \"ab cd\",                     /* string */\n" +
"    c = -123.4e2D,                    /* Double */\n" +
"    f = true,                        /* boolean */\n" +
"    g = P1Y1M1DT1H1M1.12S,        /* duration */\n" +
"    h = null,                        /* null */\n" +
"    i=[],                            /* empty array */\n" +
"    Towns = [ /* array with GPS locations of towns */\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      g(50.08, 14.42, 399, \"Praha (centrum)\"),\n" +
"    ],\n" +
"    j = '\\u0007',                    /* character */\n" +
"    k = '\n',                         /* character '\n'*/\n" +
"    l = '\"',                         /* character '\"' */\n" +
"    \"m\" : '\'',                     /* character '\' */\n" +
"    \"n\" : '\\\\',                   /* character '\\' */\n" +
//"    \"name with space\" : \"    x \t y \"   /* name with space is quoted! */\n" +
"    \"name with space\" : \"x \t y\"   /* name with space is quoted! */\n" +
"  }, /**** end of map ****/\n" +
"  -3F,                               /* float */\n" +
"  -3d,                               /* decimal */\n" +
"  999999999999999999999999999999999, /* big integer */\n" +
"  D--1,                              /* month */\n" +
"  D--1Z,                             /* month zone*/\n" +
"  D--1-2,                            /* month day */\n" +
"  D--3-4-01:01,                      /* month day zone */\n" +
"  D19:23:01,						  /* hours, minutes seconds */\n" +
"  D19:23:01.012,                     /* hours minutes seconds millis */\n" +
"  D0:0:0.00001+00:00,                /* time nanos zone */\n" +
"  D2000,                             /* year (without zone) */\n" +
"  D-123456789,                       /* year (without zone) */\n" +
"  D2000Z,                            /* year zone */\n" +
"  D2000-01:00,                       /* year zone */\n" +
"  D2000-1,                           /* year month */\n" +
"  D2000-1Z,                          /* year month zone */\n" +
"  D2000-1-01:00,                     /* year month zone */\n" +
"  D2021-01-12T01:10:11.54012-00:01,  /* date and time (nanos, zone) */\n" +
"  g(-0, +1),                          /* GPS */\n" +
"  b(HbRBHbRBHQw=),                   /* byte array (base64) */\n" +
"  #(123.45 CZK),                     /* currency ammount */ \n" +
"  #(12 USD),                         /* currency ammoun */\n" +
"] /**** end of array ****/\n" +
"/* End of XON test */";
			x = JsonUtil.parseXON(xon);
			json = JsonUtil.toJsonString(JsonUtil.xonToJson(x), true);
			JsonUtil.parse(json);
			s = JsonUtil.toXonString(x, true);
			y = JsonUtil.parseXON(s);
			if (!JsonUtil.jsonEqual(x,y)) {
				System.err.flush();
				fail(s);
				System.err.flush();
				System.err.println(xon);
				System.err.println("+++");
				System.err.println(s);
				System.err.println("+++");
				display(y, x, "0");
			}
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
"    \"b\" : \"string()\",\n" +		/* string */
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
			y = jparse(xp, "", json, reporter);
			if (reporter.errorWarnings()) {
				System.err.println(
					KXmlUtils.nodeToString(JsonUtil.jsonToXml(json), true));
				assertNoErrors(reporter);
				return;
			}
			genXComponent(xp, tempDir);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			y = XComponentUtil.toXon(xc);
			if (!JsonUtil.jsonEqual(x,y)) {
				display(x, y, "1");
			}
			x = xc.toJson();
			y = JsonUtil.xonToJson(y);
			if (!JsonUtil.jsonEqual(x,y)) {
				display(x, y, "2");
			}
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