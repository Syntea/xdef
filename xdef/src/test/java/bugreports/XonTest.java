package bugreports;

import java.util.List;
import java.util.Map;
import org.xdef.json.JsonUtil;
import org.xdef.sys.GPSPosition;
import test.XDTester;

/** Test XON data.*/
public class XonTest extends XDTester {

	public XonTest() {super();}

	@Override
	public void test() {
		String s;
		Object x, y;
		try {
			s =
"/* Start of XON test */\n" +
"[ /***** Array *****/\n" +
"  { /***** Map *****/\n" +
"    a = 1S,                          /* Long */\n" +
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
"  -0d3,                              /* decimal */\n" +
"  999999999999999999999999999999999, /* big integer */\n" +
"  d(--1),                            /* month */\n" +
"  d(--1Z),                           /* month zone*/\n" +
"  d(--1-2),                          /* month day */\n" +
"  d(--1-2-01:01),                    /* month day zone */\n" +
"  d(19:23),                          /* hours minutes */\n" +
"  d(19:23:01),                       /* hours, minutes seconds */\n" +
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
			System.out.println(s);
			System.out.println("=====================");
			x = JsonUtil.parseXON(s);
//if(true)return;
			s = JsonUtil.toJsonString(JsonUtil.xonToJson(x), true);
//			System.out.println(s);
			JsonUtil.parse(s);
//			System.out.println("=======================");
			s = JsonUtil.toXonString(x, true);
			System.out.println(s);
//			System.out.println("=======================");
			y = JsonUtil.parseXON(s);
			assertTrue(JsonUtil.jsonEqual(x,y));
			s = JsonUtil.toXonString(x, false);
//			System.out.println(s);
//			System.out.println("=======================");
			y = JsonUtil.parseXON(s);
			assertTrue(JsonUtil.jsonEqual(x,y));
//if(true)return;
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
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}