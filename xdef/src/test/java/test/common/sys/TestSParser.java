package test.common.sys;

import java.math.BigInteger;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/** Test of simple parser and SDatetime.
 * @author Vaclav Trojan
 */
public class TestSParser extends STester {

	public TestSParser() {super();}

	/** Check if both arguments with XMLGregorianCalendar values are equal.
	 * @param d1 first XMLGregorianCalendar value.
	 * @param d2 SECOND XMLGregorianCalendar value.
	 * @return empty string if both arguments are equal. Otherwise returns
	 * the string with information about differences.
	 */
	private static String checkDateEQ(final XMLGregorianCalendar d1,
		final XMLGregorianCalendar d2) {
		String result = "";
		if (d1.getYear() != d2.getYear()) {
			result += "Year: " + d1.getYear() + "/" +  d2.getYear();
		}
		if (d1.getMonth() != d2.getMonth()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Month: " + d1.getMonth() + "/" +  d2.getMonth();
		}
		if (d1.getDay() != d2.getDay()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Day: " + d1.getDay() + "/" +  d2.getDay();
		}
		if (d1.getHour() != d2.getHour()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Hour: " + d1.getHour() + "/" +  d2.getHour();
		}
		if (d1.getMinute() != d2.getMinute()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Minute: " + d1.getMinute() + "/" +  d2.getMinute();
		}
		if (d1.getSecond() != d2.getSecond()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Second: " + d1.getSecond() + "/" +  d2.getSecond();
		}
		if (d1.getMillisecond() != d2.getMillisecond()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Millisecond: " + d1.getMillisecond()+"/"+ d2.getMillisecond();
		}
		if (d1.getTimezone() != d2.getTimezone()) {
			result += (result.isEmpty() ? "" : "\n") +
				"Timezone: " + d1.getTimezone() + "/" +  d2.getTimezone();
		}
		if (d1.getEon() == null) {
			if (d2.getEon() != null) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Eon: null/" +  d2.getEon();
			}
		} else if (!d1.getEon().equals(d2.getEon())) {
			result += (result.isEmpty() ? "" : "\n")
				+ "Eon: " + d1.getEon() + "/" +  d2.getEon();
		}
		if (d1.getEonAndYear()== null) {
			if (d2.getEonAndYear() != null) {
				result +=  (result.isEmpty() ? "" : "\n")
					+ "EonAndYear: null/" +  d2.getEonAndYear();
			}
		} else if (!d1.getEonAndYear().equals(d2.getEonAndYear())) {
			result +=  (result.isEmpty() ? "" : "\n")
				 + "EonAndYear: " + d1.getEonAndYear()
				+ "/" +  d2.getEonAndYear();
		}
		return result;
	}

	/** Check if both arguments with XMLGregorianCalendar values are equal.
	 * (before and after normalization).
	 * @param d1 first XMLGregorianCalendar value.
	 * @param d2 SECOND XMLGregorianCalendar value.
	 * @return empty string if both arguments are equal. Otherwise returns
	 * the string with information about differences.
	 */
	private static String checkDateEQ2(final XMLGregorianCalendar d1,
		final XMLGregorianCalendar d2) {
		String r1 = checkDateEQ(d1, d2);
		String r2 = checkDateEQ(d1.normalize(), d2.normalize());
		r2 = r2.isEmpty() ? "" : ("normalized\n" + r2);
		return r1.isEmpty() ? r2 : r1 + (r2.isEmpty() ? "" : ("\n" + r2));
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		SDatetime d;
		StringParser p;
		String s;
		int ce;
		Calendar c;
		try {
			//test intervals
			p = new StringParser("abcQ");
			assertTrue(p.isInInterval('a', 'c') == 'a' &&
				p.isInInterval('a', 'c') == 'b' &&
				p.isInInterval('a', 'c') == 'c' &&
				p.isInInterval('a', 'c') == StringParser.NOCHAR &&
				p.isInInterval('A', 'Z') == 'Q' &&
				p.eos());
		} catch (Exception ex) {fail(ex);}
		try {
			//datetime format literals in mask
			p = new StringParser("11.2009");
			if (p.isDatetime("M'-'y|M'.'y")) {
				assertEq("11/2009", p.getParsedSDatetime().formatDate("M'/'y"));
			} else {
				fail();
			}
			p = new StringParser("11'2009");
			if (p.isDatetime("M''''y|M'.'y")) {
				assertEq("11'2009",p.getParsedSDatetime().formatDate("M''''y"));
			} else {
				fail();
			}
			p = new StringParser("11'2009");
			if (p.isDatetime("M'.'y|M''''y")) {
				assertEq("11'2009",
					p.getParsedSDatetime().formatDate("M''''y"));
			} else {
				fail();
			}
			p = new StringParser("11'a2009");
			if (p.isDatetime("M'.'y|M'''a'y")) {
				assertEq("11'a2009",
					p.getParsedSDatetime().formatDate("M'''a'y"));
			} else {
				fail();
			}
			p = new StringParser("11a'2009");
			if (p.isDatetime("M'.'y|M'a'''y")) {
				assertEq("11a'2009",
					p.getParsedSDatetime().formatDate("M'a'''y"));
			} else {
				fail();
			}
			p = new StringParser("11'a'2009");
			if (p.isDatetime("M'.'y|M'''a'''y")) {
				assertEq("11'a'2009",
					p.getParsedSDatetime().formatDate("M'''a'''y"));
			} else {
				fail();
			}
			p = new StringParser("11a''b2009");
			if (p.isDatetime("M'.'y|M'a''''b'y")) {
				assertEq("11a''b2009",
					p.getParsedSDatetime().formatDate("M'a''''b'y"));
			} else {
				fail();
			}
			p = new StringParser("11a'b'c2009");
			if (p.isDatetime("M'.'y|M'a''b''c'y")) {
				assertEq("11a'b'c2009",
					p.getParsedSDatetime().formatDate("M'a''b''c'y"));
			} else {
				fail();
			}
			p = new StringParser("11a2009");
			if (p.isDatetime("M'.'y|M?'a''b''c'y")) {
				assertEq("11a2009",
					p.getParsedSDatetime().formatDate("M'a'y"));
			} else {
				fail();
			}
			p = new StringParser("11/2009");
			if (p.isDatetime("M'.'y|M?'a''b''c'y|M'/'y")) {
				assertEq("11/2009",
					p.getParsedSDatetime().formatDate("M'/'y"));
			} else {
				fail();
			}
			p = new StringParser("11'2009");
			if (p.isDatetime("M'.'y|M?'a''b''c'y")) {
				assertEq("11'2009",p.getParsedSDatetime().formatDate("M''''y"));
			} else {
				fail();
			}
			p = new StringParser("11b2009");
			if (p.isDatetime("M'.'y|M?'a''b''c'y")) {
				assertEq("11b2009", p.getParsedSDatetime().formatDate("M'b'y"));
			} else {
				fail();
			}
			p = new StringParser("11c2009");
			if (p.isDatetime("M'.'y|M?'a''b''c'y")) {
				assertEq("11c2009", p.getParsedSDatetime().formatDate("M'c'y"));
			} else {
				fail();
			}
			p = new StringParser("11/2009");
			if (p.isDatetime("M'.'y|M?'a''b''c'y|M'/'y")) {
				assertEq("11/2009", p.getParsedSDatetime().formatDate("M'/'y"));
			} else {
				fail();
			}
			p = new StringParser("1.1.2000");
			if (p.isDatetime("d.M.y")) {
				assertEq("00:00:00.0",
					p.getParsedSDatetime().formatDate("HH:mm:ss.S"));
			} else {
				fail();
			}
			//datetime format ISO
			p = new StringParser("2009-11-09");
			if (p.isDatetime("y-MM-dd")) {
				assertEq("2009-11-09",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
			} else {
				fail();
			}
			p  = new StringParser("-2009-11-09");
			if (p.isDatetime("y-MM-dd")) {
				assertEq("-2009-11-09",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
			} else {
				fail();
			}
			p  = new StringParser("0009-11-09");
			if (p.isDatetime("y-MM-dd")) {
				assertEq("0009-11-09",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
			} else {
				fail();
			}
			p  = new StringParser("-0009-11-09");
			if (p.isDatetime("y-MM-dd")) {
				assertEq("-0009-11-09",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
			} else {
				fail();
			}
			p  = new StringParser("1996-12-31");
			if (p.isISO8601Date()) {
				assertEq("1996-12-31",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
				assertEq("1996W532",
					p.getParsedSDatetime().formatDate("y'W'wwe"));
			} else {
				fail();
			}
			p  = new StringParser("1997-W1-2");
			if (p.isISO8601Date()) {
				assertEq("1996-12-31",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
			} else {
				fail();
			}
			p  = new StringParser("1997W012");
			if (p.isISO8601Date()) {
				assertEq("1996-12-31",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
				assertEq("1996W532",
					p.getParsedSDatetime().formatDate("y'W'wwe"));
			} else {
				fail();
			}
			p = new StringParser("1996W532");
			if (p.isISO8601Date()) {
				assertEq("1996-12-31",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
				assertEq("1996W532",
					p.getParsedSDatetime().formatDate("y'W'wwe"));
			} else {
				fail();
			}
			p = new StringParser("19950204");
			if (p.isDatetime("yyyyMMdd[Z]")) {
				assertEq("1995-02-04",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
				assertEq("19950204",
					p.getParsedSDatetime().formatDate("yMMdd"));
			} else {
				fail();
			}
			p  = new StringParser("1995-035");
			if (p.isISO8601Date()) {
				assertEq("1995-02-04",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
				assertEq("1995-035",
					p.getParsedSDatetime().formatDate("y-DDD"));
			} else {
				fail();
			}
			p  = new StringParser("1995035");
			if (p.isDatetime("yyyyDDD[Z]")) {
				assertEq("1995-02-04",
					p.getParsedSDatetime().formatDate("y-MM-dd"));
				assertEq("1995035", p.getParsedSDatetime().formatDate("yDDD"));
			} else {
				fail();
			}
			p  = new StringParser("23:59:59.1234567");
			if (p.isISO8601Time()) {
				assertEq("23:59:59",
					p.getParsedSDatetime().formatDate("HH:mm:ss"));
				assertEq("23:59:59.1234567", p.getParsedSDatetime().toString());
			} else {
				fail();
			}
			p  = new StringParser("23:59");
			if (p.isISO8601Time()) {
				assertEq("23:59:00",
					p.getParsedSDatetime().formatDate("HH:mm:ss"));
				assertEq("23:59", p.getParsedSDatetime().formatDate("HH:mm"));
				assertEq("23:59", p.getParsedSDatetime().toString());
			} else {
				fail();
			}
			p  = new StringParser("1995-2-4T24:00:00+01:30");
			if (p.isISO8601Datetime()) {
				assertEq("1995-02-05T00:00:00+01:30",
					p.getParsedSDatetime().toISO8601());
			} else {
				fail();
			}
			p = new StringParser("TEXT?\n" +
				" 123 \n" + //integer
				" +123 \n" + //signed integer
				" -123 \n" + //signed integer
				" 12e3\n" + //float
				" -12e3\n" + //float
				" 120e-1\n" + //float
				" 12.50e+1\n" + //float
				" .5\n" + //float
				" .5e1\n" + //float
				" -.5\n" + //float
				" -.5e1\n" + //float
				" 5.\n" + //float
				" 5.e1\n" + //float
				" -5.\n" + //float
				" -5.e1\n" + //float
				" 29.2.2000\n" + //date
				" 13:23\n" + //time
				" 13:23:59.987\n" + //time
				" 13:23:59.987\n" + //time
				" 13:23:59.9876\n" + //time
				" 13:23:59.98\n" + //time
				" 13:23:59.9\n" + //time
				" 2004-02-18T23:15:06-01:30\n" + //ISO8601 datetime
				" 2004-02-18T23:15:06Z\n" + //ISO8601 datetime
				" 2004-02-18T23:15-01:30\n" + //ISO8601 datetime
				" 2004-02-18T23-01:30\n" + //ISO8601 datetime
				" END" +
				"");
			p.skipSpaces();
			assertTrue(p.isToken("TEXT"));
			p.skipSpaces();
			assertTrue(p.isChar('?'));
			p.skipSpaces();
			assertTrue(p.isInteger() && (p.getParsedInt() == 123),
				"b) isInteger() fails: " + p.getParsedInt());
			p.skipSpaces();
			assertTrue(p.isSignedInteger() && (p.getParsedInt() == 123),
				"a) isSignedInteger() fails: " + p.getParsedInt());
			p.skipSpaces();
			assertTrue(p.isSignedInteger() && (p.getParsedInt() == -123),
				"b) isSignedInteger() fails: " + p.getParsedInt());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == 12e3),
				"a) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && p.getParsedFloat() == -12e3,
				"b) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == 120e-1),
				"c) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == 12.50e+1),
				"d) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == .5),
				"e) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == .5e1),
				"f) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == -.5),
				"g) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == -.5e1),
				"h) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == 5.),
				"i) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == 5.e1),
				"j) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == -5.),
				"k) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			assertTrue(p.isSignedFloat() && (p.getParsedFloat() == -5.e1),
				"l) isFloat() fails: " + p.getParsedFloat());
			p.skipSpaces();
			if (p.isDatetime("d.M.yyyy")) {
				c = p.getParsedCalendar();
				assertEq(29, c.get(Calendar.DAY_OF_MONTH));
				assertEq(1, c.get(Calendar.MONTH));
				assertEq(2000, c.get(Calendar.YEAR));
				assertEq(0, c.get(Calendar.HOUR));
			} else {
				fail("isDate(\"d.M.yyyy\")");
			}
			p.skipSpaces();
			assertTrue(p.isDatetime("HH:mm") &&
				(((13*60+23)*60)+0)*1000+0 ==
				p.getParsedSDatetime().getDaytimeInMillis(),
				"a) isTime() fails: " + p.getParsedSDatetime());
			p.skipSpaces();
			assertTrue(p.isDatetime("H:mm:ss.SSS") &&
				(((13*60+23)*60)+59)*1000+987 ==
				p.getParsedSDatetime().getDaytimeInMillis(),
				"b) isTime() fails: " + p.getParsedSDatetime());
			p.skipSpaces();
			assertTrue(p.isDatetime("H:mm:ss.S") &&
				(((13*60+23)*60)+59)*1000+987 ==
				p.getParsedSDatetime().getDaytimeInMillis(),
				"c) isTime() fails: " + p.getParsedSDatetime());
			p.skipSpaces();
			assertTrue(p.isDatetime("H:mm:ss.S") &&
				(((13*60+23)*60)+59)*1000+988 ==
				p.getParsedSDatetime().getDaytimeInMillis(),
				"d) isTime() fails: " + p.getParsedSDatetime());
			p.skipSpaces();
			assertTrue(p.isDatetime("H:mm:ss.S") &&
				(((13*60+23)*60)+59)*1000+980 ==
				p.getParsedSDatetime().getDaytimeInMillis(),
				"e) isTime() fails: " + p.getParsedSDatetime());
			p.skipSpaces();
			assertTrue(p.isDatetime("H:mm:ss.S") &&
				(((13*60+23)*60)+59)*1000+900 ==
				p.getParsedSDatetime().getDaytimeInMillis(),
				"f) isTime() fails: " + p.getParsedSDatetime());
			p.skipSpaces();
			if (!p.isDatetime("yyyy-MM-ddTHH[:mm[:ss]][Z]")) {
				fail("a) Error of date parser at position: " + p.getIndex());
			} else {
				assertEq("2004-02-18T23:15:06-01:30",
					p.getParsedSDatetime().toISO8601());
			}
			p.skipSpaces();
			if (!p.isDatetime("yyyy-MM-ddTHH[:mm[:ss]][Z]")) {
				fail("b) Error of date parser at position: " + p.getIndex());
			} else {
				assertEq("2004-02-18T23:15:06Z",
					p.getParsedSDatetime().toISO8601());
			}
			p.skipSpaces();
			if (!p.isDatetime("yyyy-MM-ddT[HH:mm[:ss]][Z]")) {
				fail("c) Error of date parser at position: " + p.getIndex());
			} else {
				assertEq("2004-02-18T23:15:00-01:30",
					p.getParsedSDatetime().toISO8601());
			}
			p.skipSpaces();
			if (!p.isDatetime("yyyy-MM-ddTHH[:mm[:ss]][Z]")) {
				fail("c) Error of date parser at position: " + p.getIndex());
			} else {
				assertEq("2004-02-18T23:00:00-01:30",
					p.getParsedSDatetime().toISO8601());
			}
			p.skipSpaces();
			assertTrue(p.isToken("END"), "Not recognized token");
			assertTrue(p.eos(), "End of source expected");
			p = new StringParser("19731100");
			if (p.isDatetime("yyyyMMdd")) {
				assertTrue(p.eos());
				d = p.getParsedSDatetime();
				assertFalse(d.chkDatetime(), "Error not reported, day = 0!");
			}
			p = new StringParser("2005-11-20T18:49");
			assertTrue(p.isDatetime("yyyy-MM-ddTHH:mm[:ss][Z]|dd.MM.yyyy"));
			p = new StringParser("20.11.2005");
			assertTrue(p.isDatetime("yyyy-MM-ddTHH:mm[:ss][Z]|dd.MM.yyyy"));
			p = new StringParser("20/11/2005");
			assertFalse(p.isDatetime("yyyy-MM-ddTHH:mm[:ss][Z]|dd.MM.yyyy"),
				"Error not reported");
			p = new StringParser("20/11/2005");
			assertTrue(p.isDatetime(
				"yyyy-MM-ddTHH:mm[:ss][Z]|dd.MM.yyyy|dd/MM/yyyy"));
			p = new StringParser("20/11/2005");
			if (!p.isDatetime("{H23m59s58}d/M/yyyy[ HH:mm:ss]|d.M.yyyy")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 23);
				assertEq(c.get(Calendar.MINUTE), 59);
				assertEq(c.get(Calendar.SECOND), 58);
			}
			p = new StringParser("20.11.2005");
			if (!p.isDatetime("{H23m59s58}d/M/yyyy[ HH:mm:ss]|d.M.yyyy")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 23);
				assertEq(c.get(Calendar.MINUTE), 59);
				assertEq(c.get(Calendar.SECOND), 58);
			}
			p = new StringParser("20.11.2005");
			if (!p.isDatetime(
				"{H23m59s58}d/M/yyyy[ HH:mm:ss]|{H22m33s44}d.M.yyyy")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 22);
				assertEq(c.get(Calendar.MINUTE), 33);
				assertEq(c.get(Calendar.SECOND), 44);
			}
			p = new StringParser("20/11/2005");
			if (!p.isDatetime("{H23m59s58S543}d/M/y[[ H:m[:s[?',.'S]]]")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 23);
				assertEq(c.get(Calendar.MINUTE), 59);
				assertEq(c.get(Calendar.SECOND), 58);
				assertEq(c.get(Calendar.MILLISECOND), 543);
			}
			p = new StringParser("20/11/2005 17:40");
			if (!p.isDatetime("{H23m59s58S543}d/M/y[[ H:m[:s[?',.'S]]]")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 17);
				assertEq(c.get(Calendar.MINUTE), 40);
				assertEq(c.get(Calendar.SECOND), 58);
				assertEq(c.get(Calendar.MILLISECOND), 543);
			}
			p = new StringParser("20/11/2005 17:40:05");
			if (!p.isDatetime("{H23m59s58S543}d/M/y[[ H:m[:s[?',.'S]]]")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 17);
				assertEq(c.get(Calendar.MINUTE), 40);
				assertEq(c.get(Calendar.SECOND), 5);
				assertEq(c.get(Calendar.MILLISECOND), 543);
			}
			p = new StringParser("20/11/2005 17:40:05.123");
			if (!p.isDatetime("{H23m59s58S543}d/M/y[[ H:m[:s[?',.'S]]]")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 17);
				assertEq(c.get(Calendar.MINUTE), 40);
				assertEq(c.get(Calendar.SECOND), 5);
				assertEq(c.get(Calendar.MILLISECOND), 123);
			}
			p = new StringParser("20/11/2005 17:40");
			if (!p.isDatetime("{H23m59s58S543}d/M/y[[ H:m[:s[?',.'S]]]")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 17);
				assertEq(c.get(Calendar.MINUTE), 40);
				assertEq(c.get(Calendar.SECOND), 58);
				assertEq(c.get(Calendar.MILLISECOND), 543);
			}
			p = new StringParser("20/11/2005 17:");
			if (!p.isDatetime("{H23m59s58S543}d/M/y[[ H:m[:s[?',.'S]]]")) {
				fail();
			} else {
				assertTrue(p.isToken(" 17:"));
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.HOUR_OF_DAY), 23);
				assertEq(c.get(Calendar.MINUTE), 59);
				assertEq(c.get(Calendar.SECOND), 58);
				assertEq(c.get(Calendar.MILLISECOND), 543);
			}
			s = "29/10/1975 11:55:23.345 CET";
			p = new StringParser(s);
			if (!p.isDatetime("d?'/.'M?'./'yyyy HH:mm:ss?',.'S z")) {
				fail(s);
			} else {
				assertTrue(p.eos());
				c = p.getParsedCalendar();
				assertEq(c.getTimeZone().getRawOffset(), 3600000);
			}
			s = s.replace('.', ',').replace('/', '.');
			p = new StringParser(s);
			if (!p.isDatetime("d?'/.'M?'./'yyyy HH:mm:ss?',.'S z")) {
				fail(s);
			} else {
				assertTrue(p.eos());
				c = p.getParsedCalendar();
				assertEq(c.getTimeZone().getRawOffset(), 3600000);
			}
			s = s.replace('.', '?');
			p = new StringParser(s);
			if (!p.isDatetime("d?M?yyyy HH:mm:ss?',.'S z")) {
				fail(s);
			} else {
				assertTrue(p.eos());
				c = p.getParsedCalendar();
				assertEq(c.getTimeZone().getRawOffset(), 3600000);
			}
			SimpleDateFormat sdf =
				new SimpleDateFormat("d/M/yyyy HH:mm:ss.S z");
			s = "11/10/2005 17:56:46.395 CESTXYZ";
			p = new StringParser(s);
			if (!p.isDatetime("d/M/yyyy HH:mm:ss?',.'S z")) {
				fail();
			} else {
				assertTrue(p.isToken("XYZ"), p.getUnparsedBufferPart());
				assertTrue(p.eos());
				c = p.getParsedCalendar();
				TimeZone tz = c.getTimeZone();
				assertEq("CEST",
					tz.getDisplayName(tz.useDaylightTime(), TimeZone.SHORT));
				assertEq(c.getTimeZone().getRawOffset(), 3600000);
			}
			s = "11/10/2005 17:56:46.395 GMT+01:00";
			p = new StringParser(s);
			if (!p.isDatetime("d/M/yyyy HH:mm:ss?',.'S z")) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertTrue(p.eos());
				assertEq("UTC", c.getTimeZone().getID());
				assertEq(c.getTimeZone().getRawOffset(), 3600000);
			}
			s = "2005-10-11T17:56:46+01:00";
			p = new StringParser(s);
			if (!p.isISO8601Datetime()) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertTrue(p.eos());
				assertEq("UTC", c.getTimeZone().getID());
				assertEq(c.getTimeZone().getRawOffset(), 3600000);
			}
			//Example: 2006W023 represents Wednesday (1 .. Monday)
			//of the second week of 2006 (=> 2006-01-10).
			s = "2006-W02-3 Z";
			p = new StringParser(s);
			if (!p.isISO8601Date()) {
				fail();
			} else {
				assertTrue(p.isToken(" Z"));
				assertTrue(p.eos());
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 2006);
				assertEq(c.get(Calendar.MONTH), 0);
				assertEq(c.get(Calendar.DATE), 11);
			}
			s = "2006W101";
			p = new StringParser(s);
			if (!p.isISO8601Date()) {
				fail();
			} else {
				assertTrue(p.eos());
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 2006);
				assertEq(c.get(Calendar.MONTH), 2);
				assertEq(c.get(Calendar.DATE), 6);
			}
			s = "2006-010T13:30:01Z";
			p = new StringParser(s);
			if (!p.isISO8601Datetime()) {
				fail();
			} else {
				assertTrue(p.eos(), s.substring(0, p.getIndex()));
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 2006);
				assertEq(c.get(Calendar.MONTH), 0);
				assertEq(c.get(Calendar.DATE), 10);
				assertEq(c.get(Calendar.HOUR_OF_DAY), 13);
				assertEq(c.get(Calendar.SECOND), 1);
				assertEq(c.get(Calendar.ZONE_OFFSET), 0);
			}
			s = "2006010T133001Z"; //tenth day of 2006
			p = new StringParser(s);
			if (p.isDatetime("yyyyDDDTHHmmss[.S][Z]")) {
				assertTrue(p.eos(), s.substring(0, p.getIndex()));
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 2006);
				assertEq(c.get(Calendar.MONTH), 0);
				assertEq(c.get(Calendar.DATE), 10);
				assertEq(c.get(Calendar.HOUR_OF_DAY), 13);
				assertEq(c.get(Calendar.SECOND), 1);
				assertEq(c.get(Calendar.ZONE_OFFSET), 0);
			} else {
				fail();
			}
			s = "1993-02-14";
			p = new StringParser(s);
			if (!p.isISO8601Datetime()) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 1993);
				assertEq(c.get(Calendar.MONTH), 1);
				assertEq(c.get(Calendar.DATE), 14);
			}
			s = "19930214";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMdd[Z]")) {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 1993);
				assertEq(c.get(Calendar.MONTH), 1);
				assertEq(c.get(Calendar.DATE), 14);
			} else {
				fail();
			}
			s = "1993-045";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 1993);
				assertEq(c.get(Calendar.MONTH), 1);
				assertEq(c.get(Calendar.DATE), 14);
			} else {
				fail();
			}
			s = "1993045";
			p = new StringParser(s);
			if (p.isDatetime("yyyyDDD[Z]")) {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 1993);
				assertEq(c.get(Calendar.MONTH), 1);
				assertEq(c.get(Calendar.DATE), 14);
			} else {
				fail();
			}
			s = "1993-W7-7";
			p = new StringParser(s);
			if (!p.isISO8601Datetime()) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 1993);
				assertEq(c.get(Calendar.MONTH), 1);
				assertEq(c.get(Calendar.DATE), 14);
			}
			s = "1993W077";
			p = new StringParser(s);
			if (!p.isISO8601Datetime()) {
				fail();
			} else {
				c = p.getParsedCalendar();
				assertEq(c.get(Calendar.YEAR), 1993);
				assertEq(c.get(Calendar.MONTH), 1);
				assertEq(c.get(Calendar.DATE), 14);
			}
			p = new StringParser("abc");
			assertTrue(p.isToken("abc"));
			assertTrue(p.eos());
			p = new StringParser("");
			assertTrue(p.eos());
			p = new StringParser("abc  d");
			assertTrue(p.isToken("abc"));
			assertTrue(p.isSpaces());
			assertTrue(p.isChar('d'));
			assertTrue(p.eos());
			s = "\n\t2005-03-11T13:46:05+01:00\n\t;";
			p = new StringParser(s);
			assertEq(p.getLineNumber(), 1);
			assertEq(p.getColumnNumber(), 1);
			assertEq(p.getSourcePosition(), 0);
			p.skipSpaces();
			assertEq(p.getLineNumber(), 2);
			assertEq(p.getColumnNumber(), 2);
			assertEq(p.getSourcePosition(), 2);
			if (p.isISO8601Datetime()) {
				c = p.getParsedCalendar();
				if (c.get(Calendar.DAY_OF_MONTH) != 11 ||
					c.get(Calendar.MONTH) != 2 ||
					c.get(Calendar.YEAR) != 2005 ||
					c.get(Calendar.HOUR_OF_DAY) != 13 ||
					c.get(Calendar.MINUTE) != 46 ||
					c.get(Calendar.SECOND) != 5 ||
					c.get(Calendar.ZONE_OFFSET) != 3600000) {
					fail("getParsedDate; " +
						"d: " + c.get(Calendar.DAY_OF_MONTH) +
						", M: " + c.get(Calendar.MONTH) +
						", y: " + c.get(Calendar.YEAR) +
						", H: " + c.get(Calendar.HOUR_OF_DAY) +
						", m: " + c.get(Calendar.MINUTE) +
						", s: " + c.get(Calendar.SECOND) +
						", o: " + c.get(Calendar.ZONE_OFFSET)
						);
				}
				assertEq(p.getLineNumber(), 2);
				assertEq(p.getSourcePosition(), 27);
				if (!p.isSpaces()) {
					fail("isBlanks " + p.getIndex() + ":'" +
						p.getUnparsedBufferPart()+ "'");
				} else {
					assertEq(p.getLineNumber(), 3);
					assertEq(p.getColumnNumber(), 2);
					assertEq(p.getSourcePosition(), 29);
					assertTrue(p.isChar(';'),
						"isChar(';')" + p.getIndex() + ":'" +
						p.getUnparsedBufferPart() + "'");
					assertTrue(p.eos());
				}
			} else {
				fail("!isISO8601Date");
			}
			s = "2005-03-11T13:46:05.123+01:00";
			p = new StringParser(s);
			p.findCharAndSkip('-');
			assertEq(p.getLineNumber(), 1);
			assertEq(p.getColumnNumber(), 6);
			assertEq(p.getSourcePosition(), 5);
			p = new StringParser(s);
			p.findChar('-');
			assertEq(p.getLineNumber(), 1);
			assertEq(p.getColumnNumber(), 5);
			assertEq(p.getSourcePosition(), 4);
			s = "\tabc\n2005-03-11T13:46:05.123+01:00";
			p = new StringParser(s);
			p.findCharAndSkip('-');
			assertEq(p.getLineNumber(), 2);
			assertEq(p.getColumnNumber(), 6);
			assertEq(p.getSourcePosition(), 10);
			p = new StringParser(s);
			p.findChar('c');
			assertEq(p.getLineNumber(), 1);
			assertEq(p.getColumnNumber(), 4);
			assertEq(p.getSourcePosition(), 3);
			p = new StringParser(s);
			p.findCharAndSkip('c');
			assertEq(p.getLineNumber(), 1);
			assertEq(p.getColumnNumber(), 5);
			assertEq(p.getSourcePosition(), 4);
			s = "2005-03-11T13:46:05+01:00";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				c = p.getParsedCalendar();
				if (c.get(Calendar.DAY_OF_MONTH) != 11 ||
					c.get(Calendar.MONTH) != 2 ||
					c.get(Calendar.YEAR) != 2005 ||
					c.get(Calendar.HOUR_OF_DAY) != 13 ||
					c.get(Calendar.MINUTE) != 46 ||
					c.get(Calendar.SECOND) != 5 ||
					c.get(Calendar.ZONE_OFFSET) != 3600000) {
					fail("getParsedDate; " +
						"d: " + c.get(Calendar.DAY_OF_MONTH) +
						", M: " + c.get(Calendar.MONTH) +
						", y: " + c.get(Calendar.YEAR) +
						", H: " + c.get(Calendar.HOUR_OF_DAY) +
						", m: " + c.get(Calendar.MINUTE) +
						", s: " + c.get(Calendar.SECOND) +
						", o: " + c.get(Calendar.ZONE_OFFSET)
						);
				}
			} else {
				fail("!isISO8601Date");
			}
			p = new StringParser("Content-Type: multipart/mixed");
			if (p.isTokenIgnoreCase("content-type")) {
				if (p.isChar(':')) {
					p.skipSpaces();
					if (p.isTokenIgnoreCase("Multipart")) {
						if (p.isChar('/')) {
							if (!p.isTokenIgnoreCase("miXed")) {
								fail();
							}
						} else {
							fail();
						}
					} else {
						fail();
					}
				} else {
					fail();
				}
			} else {
				fail();
			}
			s = "20050311T134605+03:00";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050311T104605Z", s);
			} else {
				fail();
			}
			s = "20050311T134605+13:00";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050311T004605Z", s);
			} else {
				fail();
			}
			s = "20050311T134605-13:00";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050312T024605Z", s);
			} else {
				fail();
			}
			s = "20050311T134605+13:59";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050310T234705Z", s);
			} else {
				fail();
			}
			s = "20050311T134605-13:59";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050312T034505Z", s);
			} else {
				fail();
			}
			s = "20050311T134605+14:00";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050310T234605Z", s);
			} else {
				fail();
			}
			s = "20050311T134605-14:00";
			p = new StringParser(s);
			if (p.isDatetime("yyyyMMddTHHmmssZ")) {
				d = p.getParsedSDatetime();
				d.toTimeZone(TimeZone.getTimeZone("GMT"));
				s = d.formatDate("yyyyMMddTHHmmssZ");
				assertEq("20050312T034605Z", s);
			} else {
				fail();
			}
			s = "---2";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("---02", s);
			} else {
				fail();
			}
			s = "--2";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("--02", s);
			} else {
				fail();
			}
			s = "--02-03";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("--02-03", s);
			} else {
				fail();
			}
			s = "1234";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("1234", s);
			} else {
				fail();
			}
			s = "-1234";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("-1234", s);
			} else {
				fail();
			}
			s = "-1234-5";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("-1234-05", s);
			} else {
				fail();
			}
			s = "23:05";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("23:05", s);
			} else {
				fail();
			}
			s = "23:15:01.3456-01:00";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("23:15:01.3456-01:00", s);
			} else {
				fail();
			}
			s = "-0001-01-01";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("-0001-01-01", s);
			} else {
				fail();
			}
			s = "1999-5-1T20:43:09.876+01:00";
			p = new StringParser(s);
			if (p.isISO8601Datetime()) {
				d = p.getParsedSDatetime();
				s = d.toString();
				assertEq("1999-05-01T20:43:09.876+01:00", s);
			} else {
				fail();
			}
			d = SDatetime.parseISO8601("2009-01-01");
			d.addWeek(6);
			s = d.toISO8601();
			assertEq("2009-02-12", s);
			s = "05-03-11";
			p = new StringParser(s);
			if (p.isDatetime("RR-M-d")) {
				d = p.getParsedSDatetime();
				assertTrue(d.getDay() == 11, d.toISO8601());
				assertTrue(d.getMonth() == 3, d.toISO8601());
				int y = new GregorianCalendar().get(Calendar.YEAR);
				ce = y /100; //actual century
				y = y % 100; //last two digitsd of the actual year
				int i = d.getYear() % 100;
				int r =	y < 50 ? ce * 100 + i : ((ce + 1) * 100 + i);
				assertTrue(d.getYear() == r, d.toISO8601());
				assertTrue(s.equals(d.formatDate("RR-MM-dd")),
					d.formatDate("RR-MM-dd"));
			} else {
				fail();
			}
			s = "55-03-11";
			p = new StringParser(s);
			if (p.isDatetime("RR-MM-dd")) {
				d = p.getParsedSDatetime();
				assertTrue(d.getDay() == 11, d.toISO8601());
				assertTrue(d.getMonth() == 3, d.toISO8601());
				int y = new GregorianCalendar().get(Calendar.YEAR);
				ce = y /100; //actual century
				y = y % 100; //last two digits of the actual year
				int i = d.getYear() % 100;
				int r =	y < 50 ? (ce - 1) * 100 + i : (ce * 100 + i);
				assertTrue(d.getYear() == r, d.toISO8601());
				assertTrue(s.equals(d.formatDate("RR-MM-dd")),
					d.formatDate("RR-MM-dd"));
			} else {
				fail();
			}
			s = "05-03-11";
			p = new StringParser(s);
			if (p.isDatetime("YY-M-d")) {
				d = p.getParsedSDatetime();
				assertTrue(d.getDay() == 11, d.toISO8601());
				assertTrue(d.getMonth() == 3, d.toISO8601());
				 //actual century
				ce = new GregorianCalendar().get(Calendar.YEAR) /100;
				int i = d.getYear() % 100;
				int r =	ce * 100 + i;
				assertTrue(d.getYear() == r, d.toISO8601());
				assertTrue(s.equals(d.formatDate("YY-MM-dd")),
					d.formatDate("YY-MM-dd"));
			} else {
				fail();
			}
			s = "05-03-11";
			p = new StringParser(s);
			if (p.isDatetime("yy-M-d")) {
				d = p.getParsedSDatetime();
				assertTrue(d.getDay() == 11, d.toISO8601());
				assertTrue(d.getMonth() == 3, d.toISO8601());
				 //actual century
				ce = new GregorianCalendar().get(Calendar.YEAR) /100;
				int i = d.getYear() % 100;
				int r =	ce * 100 + i;
				assertTrue(d.getYear() == r, d.toISO8601());
				assertTrue(s.equals(d.formatDate("yy-MM-dd")),
					d.formatDate("yy-MM-dd"));
			} else {
				fail();
			}
			p = new StringParser("7/6");
			assertTrue(p.isDatetime("d/M|d.M|d.M.yyyy|d/M/yyyy") && p.eos());
			p = new StringParser("7/6/2020");
			assertTrue(p.isDatetime("d/M|d.M|d.M.yyyy|d/M/yyyy") && p.eos());
			p = new StringParser("7.6");
			assertTrue(p.isDatetime("d/M|d.M|d.M.yyyy|d/M/yyyy") && p.eos());
			p = new StringParser("7.6.2020");
			assertTrue(p.isDatetime("d/M|d.M|d.M.yyyy|d/M/yyyy") && p.eos());
			p = new StringParser("7.6.2020");
			assertTrue(p.isDatetime("d/M|d.M|d.M.yyyy[:H]|d/M/yyyy")&&p.eos());
			p = new StringParser("7.6.2020:16");
			assertTrue(p.isDatetime("d/M|d.M|d.M.yyyy[:H]|d/M/yyyy")&&p.eos());
			p = new StringParser("Sun Jun 07 18:00:00 CEST 2020");
			assertTrue(p.isPrintableDatetime()&&p.eos());
			p = new StringParser("12/6/1961");
			assertTrue(p.isDatetime("d/M/y|{L(cs)}d/MMM/y|{L(en)}d/MMM/y")
				&& p.eos());
			p = new StringParser(
				p.getParsedSDatetime().formatDate("{L(cs)}d/MMM/y"));
			assertTrue(p.isDatetime("d/M/y|{L(cs)}d/MMM/y|{L(en)}d/MMM/y")
				&& p.eos());
			p = new StringParser(
				p.getParsedSDatetime().formatDate("{L(en)}d/MMM/y"));
			assertTrue(p.isDatetime("d/M/y|{L(cs)}d/MMM/y|{L(en)}d/MMM/y")
				&& p.eos());
			p = new StringParser("20.11.");
			assertTrue(!p.isDatetime("d.M.yyyy[ HH:mm]"));
			p = new StringParser("20.11.2005 23:");
			assertTrue(!(p.isDatetime("d.M.yyyy[ HH:mm]") && p.eos()));
			assertEq(" 23:", p.getUnparsedBufferPart());
			p = new StringParser("20.11.");
			assertTrue(!p.isDatetime("d/M/yyyy[ HH:mm]|d.M.yyyy[ HH:mm]"));
			//isLeapYear method
			assertTrue(!SDatetime.isLeapYear(1970));
			assertTrue(SDatetime.isLeapYear(1972));
			assertTrue(SDatetime.isLeapYear(2000));
			assertFalse(SDatetime.isLeapYear(1900));
			// test if XMLGregorianCalendar methods in SDatatiome are the same
			SDatetime y = new SDatetime("2010-08-11T21:11:01.123CEST");  //DST
			GregorianCalendar g = y.toGregorianCalendar();
			DatatypeFactory df = DatatypeFactory.newInstance();
			XMLGregorianCalendar x = df.newXMLGregorianCalendar(g);
			y = new SDatetime(g);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(180); y.setTimezone(180);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(-180); y.setTimezone(-180);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(10); y.setTimezone(10);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(-210); y.setTimezone(-210);
			assertEq("", checkDateEQ2(x,y));
			x.setYear(1999); y.setYear(1999);
			assertEq("",checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
			x.setYear(Integer.MIN_VALUE); y.setYear(Integer.MIN_VALUE);
			assertEq("", checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
			x.setYear(new BigInteger("20000000000100"));
			y.setYear(new BigInteger("20000000000100"));
			assertEq("", checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
			x.clear(); y.clear();
			assertEq("", checkDateEQ2(x,y));

			y = new SDatetime("2010-01-11T21:11:01.123CEST"); //No DST
			g = y.toGregorianCalendar();
			x = df.newXMLGregorianCalendar(g);
			y = new SDatetime(g);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(180); y.setTimezone(180);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(-180); y.setTimezone(-180);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(10); y.setTimezone(10);
			assertEq("", checkDateEQ2(x,y));
			x.setTimezone(-210); y.setTimezone(-210);
			assertEq("", checkDateEQ2(x,y));
			x.setYear(1999); y.setYear(1999);
			assertEq("", checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
			x.setYear(Integer.MIN_VALUE); y.setYear(Integer.MIN_VALUE);
			assertEq("", checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
			x.setYear(new BigInteger("20000000000100"));
			y.setYear(new BigInteger("20000000000100"));
			assertEq("", checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
			y = SDatetime.parse("2010-08-11T21:11:01", "yyyy-MM-ddTHH:mm:ss");
			g = y.toGregorianCalendar();
			x = df.newXMLGregorianCalendar(g);
			y = new SDatetime(g);
			assertEq("", checkDateEQ2(x,y));
			x.setMillisecond(123); y.setMillisecond(123);
			assertEq("", checkDateEQ2(x,y));
			x.setYear(1999); y.setYear(1999);
			assertEq("", checkDateEQ2(x,y));
			x.setMillisecond(Integer.MIN_VALUE);
			y.setMillisecond(Integer.MIN_VALUE);
			assertEq("", checkDateEQ2(x,y));
			if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
				x.reset(); y.reset();
				assertEq("", checkDateEQ2(x,y));
			}
		} catch (Error ex) {
			fail(ex);
		} catch (Exception ex) {fail(ex);}
		try {//SDuration
			SDuration du;
			du = new SDuration("1999-11-05T23:11:05/P2Y1M3DT11H");
			assertEq("1999-11-05T23:11:05/P2Y1M3DT11H", du.toString());
			du = new SDuration("1999-11-05T23:11:05/P2Y1M3D");
			assertEq("1999-11-05T23:11:05/P2Y1M3D", du.toString());
			du = new SDuration("P2Y1M3DT11H");
			assertEq("P2Y1M3DT11H", du.toString());
			du = new SDuration("P6W/2009-09-08");
			assertEq("P42D/2009-09-08", du.toString());
			du = new SDuration("P6W");
			d = du.getNextTime(SDatetime.parseISO8601("2009-01-01"));
			assertEq("20090212", d.formatDate("yyyyMMdd"));
			du = new SDuration("P3M");
			d = du.getNextTime(SDatetime.parseISO8601("2009-01-01"));
			assertEq("20090401", d.formatDate("yyyyMMdd"));
			du = new SDuration("P3M/2009-04-02");
			d = du.getNextTime(SDatetime.parseISO8601("2009-01-01"));
			assertEq("20090401", d.formatDate("yyyyMMdd"));
			du = new SDuration("P3M/2009-04-01");
			d = du.getNextTime(SDatetime.parseISO8601("2009-01-01"));
			assertEq(null, d);
			du = new SDuration("-P1Y"); // negative
			assertEq("-P1Y", du.toString());
			du = new SDuration("-P2M10D");  // negative
			assertEq("-P2M10D", du.toString());
			du = new SDuration("P5Y");
			assertEq("P5Y", du.toString());
			du = new SDuration("P1DT2S");
			assertEq("P1DT2S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.001S");
			assertEq("P1Y2M3DT5H20M30.001S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.01S");
			assertEq("P1Y2M3DT5H20M30.01S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.1S");
			assertEq("P1Y2M3DT5H20M30.1S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.12S");
			assertEq("P1Y2M3DT5H20M30.12S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.123S");
			assertEq("P1Y2M3DT5H20M30.123S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.1234S");
			assertEq("P1Y2M3DT5H20M30.1234S", du.toString());
			du = new SDuration("P1Y2M3DT5H20M30.1236S");
			assertEq("P1Y2M3DT5H20M30.1236S", du.toString());
			du = new SDuration("P5Y2M10D");
			assertEq("P5Y2M10D", du.toString());
			du = new SDuration("P5Y2M10DT15H");
			assertEq("P5Y2M10DT15H", du.toString());
			du = new SDuration("PT15H");
			assertEq("PT15H", du.toString());
			du = new SDuration("PT1004199059S");
			assertEq("PT1004199059S", du.toString());
			du = new SDuration("PT130S");
			assertEq("PT130S", du.toString());
			du = new SDuration("PT2M10S");
			assertEq("PT2M10S", du.toString());
			du = new SDuration("P2DT3H20M");
			assertEq("P2DT3H20M", du.toString());
			du = new SDuration("R12/P2DT3H20M");
			assertEq("R12/P2DT3H20M", du.toString());
			du = new SDuration("P0001-10-11T23:01:55/2009-11-05T23:11:05");
			assertEq("P1Y10M11DT23H1M55S/2009-11-05T23:11:05", du.toString());
			du = new SDuration("2009-11-05T23:11:05/P0001-10-11T23:01:55");
			assertEq("2009-11-05T23:11:05/P1Y10M11DT23H1M55S", du.toString());
			du = new SDuration("1999-11-05T23:11:05/P0001-10-11T23:01:55/" +
				"2009-11-05T23:11:05");
			assertEq(
				"1999-11-05T23:11:05/P1Y10M11DT23H1M55S/2009-11-05T23:11:05",
				du.toString());
			try {
				if (new SDuration("P1M2Y") != null) {}
				fail("parts order - Y must precede M");
			} catch (Exception ex) {}
			try {
				if (new SDuration("P1Y-1M") != null) {}
				fail("all parts must be positive, error not thrown");
			} catch (Exception ex) {
				String msg = "" + ex.getMessage();
				if (msg.indexOf("SYS056") < 0) {
					fail(ex);
				}
			}
			try {
				new SDuration("1999-111-5T23:11:05/P2Y3D1MT11H");
				fail("Incorrect format of time period: not thrown");
			} catch (Exception ex) {
				String msg = "" + ex.getMessage();
				if (msg.indexOf("SYS056") < 0) {
					fail(ex);
				}
			}
			//2000-01-12T12:13:14Z, P1Y3M5DT7H10M3.3S => 2001-04-17T19:23:17.3Z
			SDuration sd = new SDuration("P1Y3M5DT7H10M3.3S");
			SDatetime st = new SDatetime("2000-01-12T12:13:14Z");
			SDatetime st1 = sd.getNextTime(st);
			s = st1.formatDate("yyyy-MM-ddTHH-mm-ss.SZ");
			assertEq("2001-04-17T19-23-17.3Z", s);
			//2000-01  -P3M => 1999-10
			sd = new SDuration("-P3M");
			st = SDatetime.parse("2000-01", "yyyy-MM");
			st1 = sd.getNextTime(st);
			s = st1.toISO8601();
			assertEq("1999-10-01T00:00:00", s);
			//2000-01-12  PT33H => 2000-01-13
			sd = new SDuration("PT33H");
			st = SDatetime.parse("2000-01-12", "yyyy-MM-dd");
			st1 = sd.getNextTime(st);
			s = st1.toISO8601();
			assertEq("2000-01-13T09:00:00", s);
			//2000-03-30 + P1D) + P1M = 2000-03-31 + P1M = 2000-04-30
			sd = new SDuration("P1D");
			st = SDatetime.parse("2000-03-30", "yyyy-MM-dd");
			st1 = sd.getNextTime(st);
			s = st1.toISO8601();
			assertEq("2000-03-31T00:00:00", s);
			sd = new SDuration("P1M");
			st1 = sd.getNextTime(st1);
			s = st1.toISO8601();
			assertEq("2000-04-30T00:00:00", s);
			//(2000-03-30 + P1M) + P1D = 2000-04-30 + P1D = 2000-05-01
			sd = new SDuration("P1M");
			st = SDatetime.parse("2000-03-30", "yyyy-MM-dd");
			st1 = sd.getNextTime(st);
			s = st1.toISO8601();
			assertEq("2000-04-30T00:00:00", s);
			sd = new SDuration("P1D");
			st1 = sd.getNextTime(st1);
			s = st1.toISO8601();
			assertEq("2000-05-01T00:00:00", s);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}