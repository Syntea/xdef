package bugreports;

import java.util.List;
import java.util.Map;
import org.xdef.json.JsonUtil;
import org.xdef.sys.GPSPosition;
import test.XDTester;

/** Test XON data.*/
public class TestXON extends XDTester {

	public TestXON() {super();}

	private static void printDistance(List list, int i, int j) {
		GPSPosition p1 = (GPSPosition) list.get(i);
		GPSPosition p2 = (GPSPosition) list.get(j);
		System.out.printf("%s - %s: %.2f km.\n",
			p1.name(), p2.name(), p1.distanceTo(p2)/1000.0D);
	}

	@Override
	public void test() {
		String s;
		Object o;
		try {
			s =
"/*This is XON array */\n"+
"[\n"+
"  /*This is XON map */\n"+
"  {\n"+
"    a /* comment */ = 1S,              /* comment, integer*/\n"+
"    b=\"a b\",                        /* string */\n"+
"    c=-123.4e2,                      /* float*/\n"+
"    d=d(1999-01-12-01:00),            /* date */\n"+
"    e= d(13:55:00.123456+02:00),      /* time */\n"+
"    f=true,                           /* boolean */\n"+
"    g= p(P1Y1M1DT1H1M1.12S),          /* duration */\n"+
"    h=null,                           /* null */\n"+
"    i=[-3F,                           /* decimal */\n"+
"      999999999999999999999999999999999999999,  /* big integer */\n"+
"      d(--1),                          /* month */\n"+
"      d(--1Z),                         /* month zone*/\n"+
"      d(--1-2),                        /* month day */\n"+
"      d(--1-2-01:01),                  /* month day zone*/\n"+
"      d(19:23),                        /* hours minutes*/\n"+
"      d(19:23:01),                     /* hours, minutes seconds*/\n"+
"      d(19:23:01.012),                 /* hours minutes seconds millis*/\n"+
"      d(0:0:0.00001+00:00),            /* time nanos zone*/\n"+
"      d(2000),                         /* year (without zone) */\n"+
"      d(-123456789),                   /* year (without zone) */\n"+
"      d(2000Z),                        /* year zone */\n"+
"      d(2000-01:00),                   /* year zone */\n"+
"      d(2000-1),                       /* year month */\n"+
"      d(2000-1Z),                      /* year month zone*/\n"+
"      d(2000-1-01:00),                 /* year month zone*/\n"+
"      d(2000-1-1T0:0:0.00001-00:01)    /* date and time */\n"+
"    ]\n"+
"    /* Towns and BPS locations */\n"+
"    Towns = [\n"+
"      gps(50.08, 14.42, 399, Praha),\n"+
"      gps(48.2,16.37,151, Wien),\n"+
"      gps(51.52,-0.09,0,London)\n"+
"    ]\n"+
"  },\n"+
"  gps(-0,+1),                       /* GPS */\n"+
"  gps(90,180,xyz),                    /* GPS */\n"+
"  b(HbRBHbRBHQw=),                    /* byte aarray */\n"+
"  #(123.45 CZK),                      /* currency ammount */ \n"+
"  #(12 USD),\n"+
"  '?',                                /* character */\n"+
"  '\\u0007',                          /* character */\n"+
"  '\'',                               /* character */\n"+
"  '\n'                                /* character */\n"+
"]\n"+
"/* End of XON test */";
//			System.out.println(s);
			o = JsonUtil.parseXON(s);
			System.out.println(
				JsonUtil.toJsonString(JsonUtil.xonToJson(o), true));
			System.out.println("=======================");
			s = JsonUtil.toXonString(o, true);
			System.out.println(s);
			System.out.println("=======================");
			JsonUtil.parseXON(s);
			s = JsonUtil.toXonString(o, false);
			System.out.println(s);
			System.out.println("=======================");
			o = JsonUtil.parseXON(s);
//if(true)return;
			List list = (List) ((Map) ((List) o).get(0)).get("Towns");
			printDistance(list, 0, 1);
			printDistance(list, 0, 2);
			printDistance(list, 1, 2);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}