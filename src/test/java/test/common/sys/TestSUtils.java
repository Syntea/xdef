package test.common.sys;

import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import buildtools.STester;

/** Test the class SUtils.
 * @author Vaclav Trojan
 */
public class TestSUtils extends STester {

	public TestSUtils() {super();}

	private static String displayDate(Calendar c) {
		int rawoffset = c.get(Calendar.ZONE_OFFSET);
		int offset = rawoffset >= 0 ? rawoffset : - rawoffset;
		int offmin = (offset / 60000) % 60;
		int offhour = (offset / 60000) / 60;
		return c.get(Calendar.DATE) + "." + (c.get(Calendar.MONTH) + 1) +
			"." + c.get(Calendar.YEAR) + " " + c.get(Calendar.HOUR_OF_DAY) +
			":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) +
			"." + c.get(Calendar.MILLISECOND) + (rawoffset >= 0 ? " +" : " -") +
			(offhour < 10 ? "0" : "") + offhour +
			(offmin < 10 ? ":0" : ":") + offmin;
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
			String s, s1, s2;
			StringParser p;
			Calendar c;
			s = (SUtils.getActualPath()+"aaa.aaa").replace('\\','/');
			if (!s.equalsIgnoreCase(new File("aaa.aaa").getAbsolutePath()
				.replace('\\','/'))) {
				fail("getAbsolutePath(): " + s + ", file= " +
					new File("aaa.aaa").getAbsolutePath().replace('\\','/'));
			}
			c = new java.util.GregorianCalendar(2004,8,16,17,5,43);
			c.set(Calendar.MILLISECOND,85);
			TimeZone tz = new SimpleTimeZone(-3600000*3, //rawOffset
				"XX", //String ID
				Calendar.APRIL, //startMonth
				-1, //int startDay
				-Calendar.SUNDAY, //startDayOfWeek
				7200000, //startTime (02:00)
				Calendar.OCTOBER, //int endMonth
				-1, //endDay
				Calendar.SUNDAY, //int endDayOfWeek
				7200000, //endTime (02:00)
				3600000); //dstSavings
			c.setTimeZone(tz);
			s = SDatetime.formatDate(c,"d-MM-yTH:mm:ss.SSSZ");
			assertEq("16-09-2004T17:05:43.085-02:00", s);
			s = SDatetime.toISO8601(c);
			assertEq("2004-09-16T17:05:43-02:00", s);
			s = SDatetime.toISO8601(c);
			p = new StringParser(s);
			assertTrue(p.isISO8601Date() &&
				s.equals(SDatetime.toISO8601(c)),
				"parsed date: " + s);
			assertEq("T17:05:43-02:00", p.getUnparsedBufferPart());
			s = "2004-09-16-02:00";
			p = new StringParser(s);
			assertTrue(p.isISO8601Date() && p.eos(),
				"parsed date: " + s);
			c = Calendar.getInstance();
			s = SDatetime.toISO8601(c);
			p = new StringParser(s);
			assertTrue(p.isISO8601Date() &&
				s.equals(SDatetime.toISO8601(c)),
				"parsed date now: " + s);
			s = "MMddHH2004xxxx0531235943+02:00aaaa Z";
			s1 = "'MMddHH'yyyyxxxxMMddHHmmssZ'aaaa Z'";
			p = new StringParser(s);
			if (!p.isDatetime(s1) || !p.eos() ||
				!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date 1: " +
					p.getParsedSDatetime().formatDate(s1));
			}
			s = "20040531235943+01:00";
			s1 = "yyyyMMddHHmmssZ";
			p = new StringParser(s);
			if (!p.isDatetime(s1) ||
				!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date 2: " +
					p.getParsedSDatetime().formatDate(s1));
			}
			s = "20040531235943+01:00";
			s1 = "yyyyMMddHHmmssZ";
			p = new StringParser(s);
			if (!p.isDatetime(s1) ||
				!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date 3: " +
					p.getParsedSDatetime().formatDate(s1));
			}

			//Test local setings
			assertEq("May", p.getParsedSDatetime().formatDate( "{L(en)}MMM"));
			assertEq("Mai", p.getParsedSDatetime().formatDate( "{L(de)}MMM"));
			s = p.getParsedSDatetime().formatDate("{L(cs,CZ)}MMM");
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
				assertEq("V",s);
			} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				assertEq("Kvě",s);
			} else {
				assertEq("kvě",s);
			}
			s1 = p.getParsedSDatetime().formatDate("{L(cs,CZ)}EEE");
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				assertEq("Po", s1);
			} else {
				assertEq("po",s1);
			}
			s1 = p.getParsedSDatetime().formatDate(
				"{L(es,ES,Traditional_WIN)}MMM");
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				assertEq("may", s1);
			} else {
				assertEq("may.", s1);
			}
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
				s = "Po, 2004 V. 31 235943+01:00";
			} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				s = "Po, 2004 Kvě. 31 235943+01:00";
			} else {
				s = "po, 2004 kvě. 31 235943+01:00";
			}
			s1 = "{L(cs,CZ)}E, yyyy MMM. dd HHmmssZ";
			p = new StringParser(s);
			if (!p.isDatetime(s1) ||
				!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date 3: " +
					p.getParsedSDatetime().formatDate(s1));
			}
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
				s = "Pondělí, 2004 květen 31 235943+01:00";
			} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				s = "Pondělí, 2004 května 31 235943+01:00";
			} else {
				s = "pondělí, 2004 května 31 235943+01:00";
			}
			s1 = "{L(cs,CZ)}EEEE, yyyy MMMM dd HHmmssZ";
			p = new StringParser(s);
			if (!p.isDatetime(s1) ||
				!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date 3: " +
					p.getParsedSDatetime().formatDate(s1));
			}
			s = "20040531235943Z";
			s1 = "yyyyMMddHHmmssZ";
			p = new StringParser(s);
			if (!p.isDatetime(s1)) {
				fail();
			} else if (!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date: " + s + " -> " +
					p.getParsedSDatetime().formatDate(s1));
			}
			s = "20040531235943Z";
			s1 = "yyyyMMddHHmmssZ";
			p = new StringParser(s);
			if (!p.isDatetime(s1) ||
				!s.equals(p.getParsedSDatetime().formatDate(s1))) {
				fail("parse/format date: " +
					p.getParsedSDatetime().formatDate(s1));
			}
			// test output format with optional sections
			s = "2055-07-15";
			s1 = "yyyy-MM-dd['T'HH:mm[:ss]][Z]";
			p = new StringParser(s);
			if (!p.isDatetime(s1) || !p.eos()) {
				fail();
			} else {
				assertEq("2055-07-15", s);
			}
			s = "2055-07-15+02:00";
			s1 = "yyyy-MM-dd['T'HH:mm[:ss]][Z]";
			p = new StringParser(s);
			if (!p.isDatetime(s1) || !p.eos()) {
				fail();
			} else {
				assertEq("2055-07-15+02:00", s);
			}
			s = "2055-07-15T23:50+02:00";
			s1 = "yyyy-MM-dd['T'HH:mm[:ss]][Z]";
			p = new StringParser(s);
			if (!p.isDatetime(s1) || !p.eos()) {
				fail();
			} else {
				s = p.getParsedSDatetime().formatDate(s1);
				assertEq("2055-07-15T23:50+02:00", s);
			}
			s = "2055-07-15T23:30:41+02:00";
			s1 = "yyyy-MM-dd['T'HH:mm[:ss]][Z]";
			p = new StringParser(s);
			if (!p.isDatetime(s1) || !p.eos()) {
				fail();
			} else {
				s = p.getParsedSDatetime().formatDate(s1);
				assertEq("2055-07-15T23:30:41+02:00", s);
			}

		} catch (Exception ex) {fail(ex);}
		//writeBytes,writeString, readBytes
		try {
			byte[] bytes = new byte[100];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte)i;
			}
			File f = new File(SUtils.getTempDir() + "readBytes.tst");
			f.delete();
			try {
				SUtils.readBytes(f);
				fail ("readBytes exception SYS24 expected");
			} catch (SException ex) {
				assertTrue("SYS024".equals(ex.getMsgID()), ex.toString());
			}
			try {
				SUtils.writeBytes(f, bytes);
			} catch (SException ex) {fail(ex);}
			try {
				byte[] bytes1 = SUtils.readBytes(f);
				assertTrue(Arrays.equals(bytes, bytes1),
					"readBytes - incorrect content.");
			} catch (SException ex) {fail(ex);}
			f.delete();
		} catch (Exception ex) {fail(ex);}
		//modifyString
		try {
			String orig = "abcdef";
			String result;
			result = SUtils.modifyString(orig, orig, orig);
			assertTrue(orig.equals(result), result);
			result = SUtils.modifyString(orig, orig, "");
			assertTrue("".equals(result), result);
			result = SUtils.modifyString(orig, orig, "x");
			assertTrue("x".equals(result), result);
			result = SUtils.modifyString(orig, orig, "xy");
			assertTrue("xy".equals(result), result);
			result = SUtils.modifyString(orig,"hij", "klm");
			assertTrue("abcdef".equals(result), result);
			result = SUtils.modifyString(orig,"a", "");
			assertTrue("bcdef".equals(result), result);
			result = SUtils.modifyString(orig,"f", "");
			assertTrue("abcde".equals(result), result);
			result = SUtils.modifyString(orig,"b", "");
			assertTrue("acdef".equals(result), result);
			result = SUtils.modifyString(orig,"a", "k");
			assertTrue("kbcdef".equals(result), result);
			result = SUtils.modifyString(orig,"f", "k");
			assertTrue("abcdek".equals(result), result);
			result = SUtils.modifyString(orig,"b", "k");
			assertTrue("akcdef".equals(result), result);
			result = SUtils.modifyString(orig,"a", "kl");
			assertTrue("klbcdef".equals(result), result);
			result = SUtils.modifyString(orig,"f", "kl");
			assertTrue("abcdekl".equals(result), result);
			result = SUtils.modifyString(orig,"b", "kl");
			assertTrue("aklcdef".equals(result), result);
			orig = "ababcabc";
			result = SUtils.modifyString(orig, orig, orig);
			assertTrue(orig.equals(result), result);
			result = SUtils.modifyString(orig, orig, "");
			assertTrue("".equals(result), result);
			result = SUtils.modifyString(orig, orig, "x");
			assertTrue("x".equals(result), result);
			result = SUtils.modifyString(orig, orig, "xy");
			assertTrue("xy".equals(result), result);
			result = SUtils.modifyString(orig, "a", "");
			assertTrue("bbcbc".equals(result), result);
			result = SUtils.modifyString(orig, "a", "x");
			assertTrue("xbxbcxbc".equals(result), result);
			result = SUtils.modifyString(orig, "a", "xy");
			assertTrue("xybxybcxybc".equals(result), result);
			result = SUtils.modifyString(orig, "c", "");
			assertTrue("ababab".equals(result), result);
			result = SUtils.modifyString(orig, "c", "x");
			assertTrue("ababxabx".equals(result), result);
			result = SUtils.modifyString(orig, "c", "xy");
			assertTrue("ababxyabxy".equals(result), result);
			orig = "cabcdc";
			result = SUtils.modifyString(orig,"c","xyz");
			assertTrue("xyzabxyzdxyz".equals(result), result);
		} catch (Exception ex) {fail(ex);}

		//modifyStringBuffer
		try {
			StringBuffer sb;
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "hij", "klm");
			assertTrue("abcdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "a", "");
			assertTrue("bcdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "f", "");
			assertTrue("abcde".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "b", "");
			assertTrue("acdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "a", "k");
			assertTrue("kbcdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "f", "k");
			assertTrue("abcdek".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "b", "k");
			assertTrue("akcdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "a", "kl");
			assertTrue("klbcdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "f", "kl");
			assertTrue("abcdekl".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("abcdef");
			SUtils.modifyStringBuffer(sb, "b", "kl");
			assertTrue("aklcdef".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("cabcdc");
			SUtils.modifyStringBuffer(sb,"c","xyz");
			assertTrue("xyzabxyzdxyz".equals(sb.toString()), sb.toString());
			sb = new StringBuffer("cabcdc");
			SUtils.modifyStringBuffer(sb,"c","");
			assertTrue("abd".equals(sb.toString()), sb.toString());
		} catch (Exception ex) {fail(ex);}
		//encodeHex, decodeHex
		try {
			byte[] b1 = new byte[0];
			byte[] b2;
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[1];
			b1[0] = 0;
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[2];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[3];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[4];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[300];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[0];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[1];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[35];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[36];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[37];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			b1 = new byte[300];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeHex(b1);
			assertTrue(Arrays.equals(b1, SUtils.decodeHex(b2)),
				"Hex encoding/decoding error");
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.decodeHex("\t\r 0F7f0001 \t\r ".getBytes());
			assertTrue(Arrays.equals(new byte[]{15, 127, 0, 1}, b2),
				"Hex encoding/decoding error");
			try {
				SUtils.decodeHex("0w7f0001".getBytes());
				fail("Error not thrown (not hexa digit)");
			} catch (SException ex) {
				assertEq("SYS047", ex.getMsgID(), ex.toString());
			}
			try {
				SUtils.decodeHex("0F70001".getBytes());
				fail("Error not thrown (odd number of digits)");
			} catch (SException ex) {
				assertEq("SYS047", ex.getMsgID(), ex.toString());
			}
		} catch (Exception ex) {fail(ex);}
		//encodeBase64, decodeBase64
		try {
			byte[] b1 = new byte[100];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			try {
				SUtils.decodeBase64(b1);
				fail("decodeBase64 - exception SYS48 not thrown");
			} catch (SException ex) {
				assertTrue("SYS048".equals(ex.getMsgID()),
					"decodeBase64 - exception SYS48 not thrown");
			}
			byte[] b2;
			b2 = SUtils.encodeBase64(b1, false);
			assertTrue(b2.length % 4 == 0, "Not dividable by 4:" +
				b2.length + "\n'" + new String(b2) + "'");
			b2 = SUtils.decodeBase64(b2);
			assertTrue(Arrays.equals(b1, b2),
				"Base64 encoding/decoding error");
			b1 = new byte[0];
			b2 = SUtils.encodeBase64(b1, false);
			assertTrue(b2.length % 4 == 0, "Not dividable by 4:" +
				b2.length + "\n'" + new String(b2) + "'");
			b2 = SUtils.decodeBase64(b2);
			assertTrue(Arrays.equals(b1, b2),
				"Base64 encoding/decoding error");
			b1 = new byte[1];
			b1[0] = 0;
			b2 = SUtils.encodeBase64(b1, false);
			assertTrue(b2.length % 4 == 0, "Not dividable by 4:" +
				b2.length + "\n'" + new String(b2) + "'");
			b2 = SUtils.decodeBase64(b2);
			assertTrue(Arrays.equals(b1, b2),
				"Base64 encoding/decoding error");
			b1 = new byte[2];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeBase64(b1, false);
			assertTrue(b2.length % 4 == 0, "Not dividable by 4:" +
				b2.length + "\n'" + new String(b2) + "'");
			b2 = SUtils.decodeBase64(b2);
			assertTrue(Arrays.equals(b1, b2),
				"Base64 encoding/decoding error");
			b1 = new byte[3];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeBase64(b1, false);
			assertTrue(b2.length % 4 == 0, "Not dividable by 4:" +
				b2.length + "\n'" + new String(b2) + "'");
			b2 = SUtils.decodeBase64(b2);
			assertTrue(Arrays.equals(b1, b2),
				"Base64 encoding/decoding error");
			b1 = new byte[4];
			for (int i = 0; i < b1.length; i++) {
				b1[i] = (byte)i;
			}
			b2 = SUtils.encodeBase64(b1, false);
			assertTrue(b2.length % 4 == 0, "Not dividable by 4:" +
				b2.length + "\n'" + new String(b2) + "'");
			b2 = SUtils.decodeBase64(b2);
			assertTrue(Arrays.equals(b1, b2),
				"Base64 encoding/decoding error");
			String s = "";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);

			assertEq(s, new String(b2));
			s = "A";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));

			s = "AB";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));

			s = "ABC";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));

			s = "ABCD";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));

			s = "ABCDE";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));

			s = "ABCDEF";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));

			s = "Ahoj Nazdar Dobry den Hi Good day Hello"
				+ " Gutten Tag Bon Jour Jo napot Shalom";
			b1 = SUtils.encodeBase64(s.getBytes(), false);
			assertTrue(b1.length % 4 == 0, "Not dividable by 4:" +
				b1.length + "\n'" + new String(b1) + "'");
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			s = "Ahoj Nazdar Dobry den Hi Good day Hello" +
				" Gutten Tag Bon Jour Jo napot Shalom.";
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			s += '.';
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			s += 'A';
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			s += 'h';
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			s += 'o';
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			assertEq(s, new String(b2));
			s += 'j';
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
			s += '!';
			b1 = SUtils.encodeBase64(s.getBytes(), true);
			b2 = SUtils.decodeBase64(b1);
			assertEq(s, new String(b2));
		} catch (Exception ex) {fail(ex);}
		try {//random test of encoding/decoding both base64 and hex
			Random rnd = new Random(System.currentTimeMillis());
			for (int i = 0; i < 1000; i++) {
				byte[] b1,b2;
				String en1, en2, en3;
				ByteArrayInputStream bis;
				ByteArrayOutputStream bos;
				CharArrayReader car;
				CharArrayWriter caw;
				int j;
				///////////////////////////////////////////////////////////////
				//hex
				///////////////////////////////////////////////////////////////
				b1 = new byte[rnd.nextInt(128)];
				rnd.nextBytes(b1);
				en1 = new String(SUtils.encodeHex(b1));
				b2 = SUtils.decodeHex(en1);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				en2 = new String(SUtils.encodeHex(b1));
				b2 = SUtils.decodeHex(en2);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				bis = new ByteArrayInputStream(b1);
				bos = new ByteArrayOutputStream();
				SUtils.encodeHex(bis, bos);
				bos.close();
				en3 = bos.toString();
				assertTrue(en1.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en1 + "'\n" + "s2 = '" + en3 + "'");
				bis = new ByteArrayInputStream(en3.getBytes());
				bos = new ByteArrayOutputStream();
				SUtils.decodeHex(bis, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				StringWriter sw = new StringWriter();
				bis = new ByteArrayInputStream(b1);
				SUtils.encodeHex(bis, sw);
				sw.close();
				en3 = sw.toString();
				assertTrue(en1.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en1 + "'\n" + "s2 = '" + en3 + "'");
				StringReader sr = new StringReader(en3);
				bos = new ByteArrayOutputStream();
				SUtils.decodeHex(sr, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				bis = new ByteArrayInputStream(b1);
				bos = new ByteArrayOutputStream();
				SUtils.encodeHex(bis, bos);
				bos.close();
				en3 = bos.toString();
				assertTrue(en2.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en2 + "'\n" + "s2 = '" + en3 + "'");
				bis = new ByteArrayInputStream(en3.getBytes());
				bos = new ByteArrayOutputStream();
				SUtils.decodeHex(bis, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				caw = new CharArrayWriter();
				bis = new ByteArrayInputStream(b1);
				SUtils.encodeHex(bis, caw);
				caw.close();
				en3 = caw.toString();
				assertTrue(en2.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en2 + "'\n" + "s2 = '" + en3 + "'");
				car = new CharArrayReader(en1.toCharArray());
				bos = new ByteArrayOutputStream();
				SUtils.decodeHex(car, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				///////////////////////////////////////////////////////////////
				//base64
				///////////////////////////////////////////////////////////////
				en1 = "YQB1AAAA";
				b1 = SUtils.decodeBase64(en1);
				en2 = new String(SUtils.encodeBase64(b1, true));
				b2 = SUtils.decodeBase64(en2);
				assertEq(en1, en2);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				en1 = "NwA1ADkAAAA=";
				b1 = SUtils.decodeBase64(en1);
				en2 = new String(SUtils.encodeBase64(b1, true));
				b2 = SUtils.decodeBase64(en2);
				assertEq(en1, en2);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				en1 = "VABSAFUARQAAAA==";
				b1 = SUtils.decodeBase64(en1);
				en2 = new String(SUtils.encodeBase64(b1, true));
				b2 = SUtils.decodeBase64(en2);
				assertEq(en1, en2);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				en1 = new String(SUtils.encodeBase64(b1, false));
				b2 = SUtils.decodeBase64(en1);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				en2 = new String(SUtils.encodeBase64(b1, true));
				b2 = SUtils.decodeBase64(en2);
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				bis = new ByteArrayInputStream(b1);
				bos = new ByteArrayOutputStream();
				SUtils.encodeBase64(bis, bos, false);
				bos.close();
				en3 = bos.toString();
				assertTrue(en1.equals(en3),
					"Error differs:\n" +
					"s1 = '" + en1 + "'\n" + "s2 = '" + en3 + "'");
				bis = new ByteArrayInputStream(en3.getBytes());
				bos = new ByteArrayOutputStream();
				SUtils.decodeBase64(bis, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				caw = new CharArrayWriter();
				bis = new ByteArrayInputStream(b1);
				SUtils.encodeBase64(bis, caw, false);
				caw.close();
				en3 = caw.toString();
				assertTrue(en1.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en1 + "'\n" + "s2 = '" + en3 + "'");
				car = new CharArrayReader(en3.toCharArray());
				bos = new ByteArrayOutputStream();
				SUtils.decodeBase64(car, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				bis = new ByteArrayInputStream(b1);
				bos = new ByteArrayOutputStream();
				SUtils.encodeBase64(bis, bos, true);
				bos.close();
				en3 = bos.toString();
				assertTrue(en2.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en2 + "'\n" + "s2 = '" + en3 + "'");
				bis = new ByteArrayInputStream(en3.getBytes());
				bos = new ByteArrayOutputStream();
				SUtils.decodeBase64(bis, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
				caw = new CharArrayWriter();
				bis = new ByteArrayInputStream(b1);
				SUtils.encodeBase64(bis, caw, true);
				caw.close();
				en3 = caw.toString();
				assertTrue(en2.equals(en3),
					"Error differs:\n"+
					"s1 = '" + en2 + "'\n" + "s2 = '" + en3 + "'");
				car = new CharArrayReader(en1.toCharArray());
				bos = new ByteArrayOutputStream();
				SUtils.decodeBase64(car, bos);
				bos.close();
				b2 = bos.toByteArray();
				assertTrue(Arrays.equals(b1, b2), "Decoded data not equal.");
			}
		} catch (Exception ex) {fail(ex);}
		//getActualPath
		try {
			File f1 = new File(SUtils.getActualPath());
			File f2 = new File(".");
			assertEq(f1.getCanonicalPath(), f2.getCanonicalPath(),
				"getActualPath error:\n"
					+ f1.getCanonicalPath() + "\n" + f2.getCanonicalPath());
		} catch (Exception ex) {fail(ex);}
		//getFileGroup
		try {
			String s = SUtils.getTempDir();
			if (s == null) {
				fail("SUtils.getTempDir() returns null!");
			} else {
				File f = new File(s + "getFileGroup");
				try {
					SUtils.deleteAll(f, true);
				} catch (SException ex) {
					if (!"SYS025".equals(ex.getMsgID())){
						fail(ex);
					}
				}
				try {
					SUtils.deleteAll(f, true); //here should be exception
					fail();
				} catch (SException ex) {
					if (!"SYS025".equals(ex.getMsgID())){
						fail(ex);
					}
				}
				f.mkdir();
				s = SUtils.getDirPath(f);
				SUtils.writeString(new File(s + "f1.1"), "f1.1");
				SUtils.writeString(new File(s + "f2.3"), "f2.2");
				SUtils.writeString(new File(s + "f3.2"), "f3.3");
				SUtils.writeString(new File(s + "f.1"), "f.1");
				SUtils.writeString(new File(s + "f.2"), "f.2");
				SUtils.writeString(new File(s + "f.3"), "f.3");
				SUtils.writeString(new File(s + "f.g.1"), "f.g.1");
				SUtils.writeString(new File(s + "f.g.h.1"), "f.g.h.1");
				File[] files;
				files = SUtils.getFileGroup(s + "*.*");
				assertTrue(files.length == 8,
					"getFileGroup - expected 8 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f*.*");
				assertTrue(files.length == 8,
					"getFileGroup - expected 8 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f*.1");
				assertTrue(files.length == 4,
					"getFileGroup - expected 4 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f1.*");
				assertTrue(files.length == 1,
					"getFileGroup - expected 1 file, found" + files.length);
				files = SUtils.getFileGroup(s + "*.1");
				assertTrue(files.length == 4,
					"getFileGroup - expected 4 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f.*");
				assertTrue(files.length == 5,
					"getFileGroup - expected 5 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f.*.*");
				assertTrue(files.length == 2,
					"getFileGroup - expected 2 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f.*.*.*");
				assertTrue(files.length == 1,
					"getFileGroup - expected 1 file, found" + files.length);
				files = SUtils.getFileGroup(s + "?.1");
				assertTrue(files.length == 1,
					"getFileGroup - expected 1 file, found" + files.length);
				files = SUtils.getFileGroup(s + "f?.1");
				assertTrue(files.length == 1,
					"getFileGroup - expected 1 file, found" + files.length);
				files = SUtils.getFileGroup(s + "f?.?");
				assertTrue(files.length == 3,
					"getFileGroup - expected 3 files, found" + files.length);
				files = SUtils.getFileGroup(s + "f.1");
				assertTrue(files.length == 1,
					"getFileGroup - expected 1 file, found" + files.length);
				try {
					SUtils.deleteAll(f, true);
					if (f.exists()) {
						fail();
					}
				} catch (Exception ex) {
					fail(ex);
				}
			}
		} catch (Exception ex) {fail(ex);}
		//formatDate
		try {
			Calendar c;
			String s;
			c = SDatetime.parseDatetime("28.2.1999 19:20:21.220 +01:00",
				"d.M.yyyy HH:mm:ss.S Z");
			if (c.get(Calendar.DATE) != 28 || c.get(Calendar.MONTH) != 1 ||
				c.get(Calendar.YEAR) != 1999 ||
				c.get(Calendar.HOUR_OF_DAY) != 19 ||
				c.get(Calendar.MINUTE) != 20 || c.get(Calendar.SECOND) != 21 ||
				c.get(Calendar.MILLISECOND) != 220 ||
				c.get(Calendar.ZONE_OFFSET) != 60*60*1000) {
				fail("parseDate error: " + displayDate(c));
			}
			s = SDatetime.formatDate(c, "dd/MM/yyyy HH:mm:ss.SSS Z");
			assertTrue("28/02/1999 19:20:21.220 +01:00".equals(s), s);
			c = SDatetime.parseDatetime("28.2.1999 19:20:21 +01:00",
				"d.M.yyyy HH:mm:ss.S Z");
			assertTrue(c == null, "parseDate error - null result expected: "+c);
		} catch (Exception ex) {fail(ex);}
		//parseDate - dateFromISO8601, dateToISO8601
		try {
			Calendar c;
			String s;
			c = SDatetime.parseDatetime("28.2.1999 19:20:21 +01:00",
				"d.M.yyyy HH:mm:ss Z");
			s = SDatetime.toISO8601(c);
			assertTrue("1999-02-28T19:20:21+01:00".equals(s), s);
			Calendar c1 = SDatetime.fromISO8601(s);
			assertTrue(c.equals(c1), "dateFromISO8601 error: "+displayDate(c1));
			c = SDatetime.parseISO8601("1999-365T13:14:15+01:00").getCalendar();
			s = SDatetime.toISO8601(c);
			c1 = SDatetime.fromISO8601(s);
			assertTrue(c.equals(c1), "dateFromISO8601 error: "+displayDate(c1));
			assertTrue(c1.get(Calendar.YEAR) == 1999,
				"dateFromISO8601 error: " + displayDate(c1));
			assertTrue(c1.get(Calendar.MONTH) == 11,
				"dateFromISO8601 error: " + displayDate(c1));
			assertTrue(c1.get(Calendar.DATE) == 31,
				"dateFromISO8601 error: " + displayDate(c1));
			assertTrue(c1.get(Calendar.HOUR_OF_DAY) == 13,
				"dateFromISO8601 error: " + displayDate(c1));
			assertTrue(c1.get(Calendar.MINUTE) == 14,
				"dateFromISO8601 error: " + displayDate(c1));
			assertTrue(c1.get(Calendar.SECOND) == 15,
				"dateFromISO8601 error: " + displayDate(c1));
//			assertTrue(c1.get(Calendar.MILLISECOND) == 16,
//				"dateFromISO8601 error: " + displayDate(c1));
			assertTrue(c1.get(Calendar.ZONE_OFFSET) == 3600000,
				"dateFromISO8601 error: " + displayDate(c1));
		} catch (Exception ex) {fail(ex);}

		try { // date format RFC822
			Calendar c;
			String s;
			c = SDatetime.fromRFC822("Tue, 27 Nov 2001 14:05:12 +0100 (CET)");
			s = SDatetime.toRFC822(c); // zone name is ignored!
			assertTrue("Tue, 27 Nov 2001 14:05:12 +0100".equals(s), s);
			c = SDatetime.fromRFC822("Fri, 23 Feb 2007 18:45:09 +0100");
			s = SDatetime.toRFC822(c);
			assertTrue("Fri, 23 Feb 2007 18:45:09 +0100".equals(s), s);
			s = SDatetime.formatDate(c,"yyyyMMdd, EEE, e, w, D");
			assertTrue("20070223, Fri, 5, 8, 54".equals(s), s);
			c = SDatetime.fromRFC822("Mon, 19 Feb 2007 18:45:09 +0100");
			s = SDatetime.toRFC822(c);
			assertTrue("Mon, 19 Feb 2007 18:45:09 +0100".equals(s), s);
			s = SDatetime.formatDate(c,"yyyyMMdd, EEE, e, w, D");
			assertTrue("20070219, Mon, 1, 8, 50".equals(s), s);
			c = SDatetime.fromRFC822("Sun, 18 Feb 2007 18:45:09 +0100");
			s = SDatetime.toRFC822(c);
			assertTrue("Sun, 18 Feb 2007 18:45:09 +0100".equals(s), s);
			s = SDatetime.formatDate(c,"yyyyMMdd, EEE, e, w, D");
			assertTrue("20070218, Sun, 7, 7, 49".equals(s), s);
			c = SDatetime.fromISO8601("2007-W07-7T18:45:09+01:00");
			s = SDatetime.toRFC822(c);
			assertTrue("Sun, 18 Feb 2007 18:45:09 +0100".equals(s), s);
		} catch (Exception ex) {fail(ex);}

		try { // date format RFC822
			StringParser p;
			String s;
			s = "Tue, 27 Nov 2001 14:05:12 +0100 (CET)";
			p = new StringParser(s);
			if (p.isDatetime("EEE, dd MMM yyyy HH:mm:ss ZZZZZ[ (z)]") &&
				p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				String s2 = SDatetime.formatDate(c,
					"EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
				assertTrue("Tue, 27 Nov 2001 14:05:12 +0100".equals(s2), s2);
			} else {
				fail();
			}
		} catch (Exception ex) {fail(ex);}

		try { // date format ISO 8601e
			Calendar c;
			StringParser p;
			String s;
			String s1;
			String s2;
			s = "1992-05-26 1:30:15AM-04:00";
			p = new StringParser(s);
			s1 = "yyyy-MM-dd h:m:saZ";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,s1);
				assertTrue(s.equals(s2), s2);
				s2 = SDatetime.formatDate(c,"yyyy-MM-dd H:m:sZ");
				assertTrue("1992-05-26 1:30:15-04:00".equals(s2), s2);
			} else {
				fail();
			}
			s = "1992-05-26 1:30:15PM-04:00";
			p = new StringParser(s);
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,s1);
				assertEq(s, s2);
				s2 = SDatetime.formatDate(c,"yyyy-MM-dd H:m:sZ");
				assertTrue("1992-05-26 13:30:15-04:00".equals(s2), s2);
			} else {
				fail();
			}
			s = "Monday, 23 January 2006";
			p = new StringParser(s);
			s1 = "{H10m11s13Z-08:30}EEEE, d MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,
					"EEE, dd MMM yyyy HH:mm:ss ZZZZZ (z)");
				assertTrue("Mon, 23 Jan 2006 10:11:13 -0830 (UTC)".
					equals(s2), s2);
			} else {
				fail();
			}
			p = new StringParser(s);
			s1 = "{H10m11s13Z-08:30}EEEE, d MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,
					"EEE, dd MMM yyyy HH:mm:ss ZZZZZ (z)");
				assertTrue("Mon, 23 Jan 2006 10:11:13 -0830 (UTC)".
					equals(s2), s2);
			} else {
				fail();
			}
			p = new StringParser(s);
			s1 = "{H10m11s13Z-08:30}EEEE, d MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,
					"EEE, dd MMM yyyy HH:mm:ss ZZZZZ (z)");
				assertTrue("Mon, 23 Jan 2006 10:11:13 -0830 (UTC)".
					equals(s2), s2);
			} else {
				fail();
			}
			p = new StringParser(s);
			s1 = "{L(en)H10m11s13z(Europe/Prague)}EEEE, d MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,
					"{L(en)}EEE, dd MMM yyyy HH:mm:ss ZZZZZ (z)");
				if (SUtils.JAVA_RUNTIME_VERSION_ID != 109) {
					assertTrue("Mon, 23 Jan 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else {
					assertTrue("Mon, 23 Jan 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);					
				}
			} else {
				fail();
			}
			p = new StringParser(s);
			s1 = "{L(en)H10m11s13z(Europe/Prague)}EEEE, d MMMM y|" +
				"{L(cs)H10m11s13z(Europe/Prague)}EEE, d MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c, s1);
				assertEq(s,s2);
				s2 = SDatetime.formatDate(c,
					"{L(fr)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
				if (SUtils.JAVA_RUNTIME_VERSION_ID == 109) {
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);
				} else {
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				}
				s2 = SDatetime.formatDate(c,
					"{L(cs)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
				if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
					assertTrue("Po, 23. leden 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
					assertTrue("Po, 23. ledna 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID == 109) {
					assertTrue("po, 23. ledna 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);
				} else {
					assertTrue("po, 23. ledna 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				}

			} else {
				fail();
			}
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
				s = "Po, 23. leden 2006";
			} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				s = "Po, 23. ledna 2006";
			} else {
				s = "po, 23. ledna 2006";
			}
			p = new StringParser(s);
			s1 = "{L(en)H10m11s13z(Europe/Prague)}EEEE, d MMMM y|" +
				"{L(cs)H10m11s13z(Europe/Prague)}EEE, d. MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				if (SUtils.JAVA_RUNTIME_VERSION_ID != 109) {
					s2 = SDatetime.formatDate(c,
						"{L(fr)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else {
					s2 = SDatetime.formatDate(c,
						"{L(fr)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);
				}
				s2 = SDatetime.formatDate(c,
					"{L(cs)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
				if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
					assertTrue("Po, 23. leden 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
					assertTrue("Po, 23. ledna 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID == 109) {
					assertTrue("po, 23. ledna 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);
				} else {
					assertTrue("po, 23. ledna 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				}
			} else {
				fail();
			}
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
				s = "Po, 23. leden 2006";
			} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				s = "Po, 23. ledna 2006";
			} else {
				s = "po, 23. ledna 2006";
			}
			p = new StringParser(s);
			s1 = "{L(cs)H10m11s13z(Europe/Prague)}EEE, d. MMMM y|" +
				"{L(en)H10m11s13z(Europe/Prague)}EEEE, d MMMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,
					"{L(fr)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
				if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID == 109) {
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);
				} else {
					assertTrue("lun., 23. janvier 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				}
				s2 = SDatetime.formatDate(c,
					"{L(cs)}EEE, dd. MMMM yyyy HH:mm:ss ZZZZZ (z)");
				if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
					assertTrue("Po, 23. leden 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
					assertTrue("Po, 23. ledna 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				} else if (SUtils.JAVA_RUNTIME_VERSION_ID == 109) {
					assertTrue("po, 23. ledna 2006 10:11:13 +0100 (SELČ)".
						equals(s2), s2);
				} else {
					assertTrue("po, 23. ledna 2006 10:11:13 +0100 (CEST)".
						equals(s2), s2);
				}
			} else {
				fail();
			}
			s = "Mon, 23 Jan 2006";
			p = new StringParser(s);
			s1 = "EEE, d MMM y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,s1);
				assertEq(s,s2);
				s2 = SDatetime.formatDate(c,"yyyy-MM-dd");
				assertTrue("2006-01-23".equals(s2), s2);
			} else {
				fail();
			}

			s = "12/6/1961";
			p = new StringParser(s);
			s1 = "d/M/y|{('cs)}d/MMM/y|{L(en)}d/MMM/y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,"d/M/y");
				assertEq(s, s2);
			} else {
				fail();
			}
			if (SUtils.JAVA_RUNTIME_VERSION_ID <= 107) {
				s = "12/VI/1961";
			} else if (SUtils.JAVA_RUNTIME_VERSION_ID <= 108) {
				s = "12/Čer/1961";
			} else {
				s = "12/čvn/1961";
			}
			p = new StringParser(s);
			s1 = "d/M/y|{L(cs)}d/MMM/y|{L(en)}d/MMM/y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,"d/M/y");
				if (!"12/6/1961".equals(s2)) {
					fail(s2);
				}
			} else {
				fail(p.getParsedBufferPart());
			}
			s = "12/Jun/1961";
			p = new StringParser(s);
			s1 = "d/M/y|{L(cs)}d/MMM/y|{L(en)}d/MMM/y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,"d/M/y");
				if (!"12/6/1961".equals(s2)) {
					fail(s2);
				}
			} else {
				fail();
			}
			s = "16/7/2015";
			p = new StringParser(s);
			s1 = "d?'./'M?'./'y";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,s1);
				if (!"16.7.2015".equals(s2)) {
					fail(s2);
				}
			} else {
				fail();
			}
			s = "1992-05-26-04:00";
			p = new StringParser(s);
			s1 = "yyyy-MM-ddZZZZZ|yyyy-MM-ddZ";
			if (p.isDatetime(s1) && p.eos()) {
				c = p.getParsedSDatetime().getCalendar();
				s2 = SDatetime.formatDate(c,s1);
				if (!"1992-05-26-0400".equals(s2)) {
					fail(s2);
				}
			} else {
				fail();
			}
		} catch (Exception ex) {fail(ex);}

		try { // date format ISO 8601e
			StringParser p;
			String s;
			s = "1992-05-26T13:30:15-04:00";
			p = new StringParser(s);
			if (p.isISO8601Datetime() && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				if (!s.equals(SDatetime.toISO8601(c))) {
					fail(SDatetime.toISO8601(c));
				}
			} else {
				fail();
			}
			p = new StringParser("1992-05-26T13:30:15-04:00");
			if (p.isISO8601Datetime() && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				if (!s.equals(SDatetime.toISO8601(c))) {
					fail(SDatetime.toISO8601(c));
				}
			} else {
				fail();
			}
			s = "1992-05-26T13:30:15Z";
			p = new StringParser(s);
			if (p.isISO8601Datetime() && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertTrue(s.equals(SDatetime.toISO8601(c)),
					SDatetime.toISO8601(c));
			} else {
				fail();
			}
			p = new StringParser("1992-05-26T13:30:15Z");
			if (p.isISO8601Datetime() && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertTrue(s.equals(SDatetime.toISO8601(c)),
					SDatetime.toISO8601(c));
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T14:48:59.956+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SZ");
				assertTrue("2005-03-01T14:48:59.956+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSZZ");
				assertTrue("2005-03-01T14:48:59.96+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSSZZZZZZ");
				assertTrue("2005-03-01T14:48:59.956+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSSSZZZZZ");
				assertTrue("2005-03-01T14:48:59.9560+0200".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSSSS");
				assertTrue("2005-03-01T14:48:59.95600".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T14:48:59.996+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SZ");
				assertTrue("2005-03-01T14:48:59.996+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSZZZZZZ");
				assertTrue("2005-03-01T14:49:00.00+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSSZZZZZ");
				assertTrue("2005-03-01T14:48:59.996+0200".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T23:59:59.996+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SZ");
				assertTrue("2005-03-01T23:59:59.996+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSZZZZZZ");
				assertTrue("2005-03-01T24:00:00.00+02:00".equals(s), s);
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ss.SSSZZZZZ");
				assertTrue("2005-03-01T23:59:59.996+0200".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T14:48:59.996+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ssZ");
				assertTrue("2005-03-01T14:49:00+02:00".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T23:59:59.996+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ssZ");
				assertTrue("2005-03-01T24:00:00+02:00".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T22:59:59.999+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ssZ");
				assertTrue("2005-03-01T23:00:00+02:00".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("2005-03-01T23:59:59.499+02:00");
			if (p.isDatetime("y-M-dTH:m:s.SZ") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				s = SDatetime.formatDate(c, "yyyy-MM-ddTHH:mm:ssZ");
				assertTrue("2005-03-01T23:59:59+02:00".equals(s), s);
			} else {
				fail();
			}
			p = new StringParser("05");
			if (p.isDatetime("yy") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertEq(2005,c.get(Calendar.YEAR));
			} else {
				fail();
			}
			p = new StringParser("95");
			if (p.isDatetime("Y") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertEq(95,c.get(Calendar.YEAR));
			} else {
				fail();
			}
			p = new StringParser("95");
			if (p.isDatetime("YY") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertEq(2095,c.get(Calendar.YEAR));
			} else {
				fail();
			}
			p = new StringParser("05");
			if (p.isDatetime("RR") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertEq(2005,c.get(Calendar.YEAR));
			} else {
				fail();
			}
			p = new StringParser("95");
			if (p.isDatetime("RR") && p.eos()) {
				Calendar c = p.getParsedSDatetime().getCalendar();
				assertEq(1995,c.get(Calendar.YEAR));
			} else {
				fail();
			}
		} catch (Exception ex) {fail(ex);}

		//SDatetime
		try {
			String s1 = "2005-03-01T23:59:59+02:00";
			String s2 = "2006-03-01T13:59:59+02:00";
			SDatetime d1 = SDatetime.parseISO8601(s1);
			SDatetime d2 = SDatetime.parseISO8601(s2);
			assertTrue(SDatetime.getCalendarDaysDifference(d1, d2) == 365,
				"Days: " + SDatetime.getCalendarDaysDifference(d1, d2));
			assertTrue(SDatetime.getCalendarMonthsDifference(d1, d2) == 12,
				"Months: " + SDatetime.getCalendarMonthsDifference(d1, d2));
			assertTrue(SDatetime.getCalendarYearsDifference(d1, d2) == 1,
				"Years: " + SDatetime.getCalendarYearsDifference(d1, d2));
			assertEq(d1.getDay(), 1);
			assertEq(d1.getMonth(), 3);
			assertEq(d1.getYear(), 2005);
			assertEq(d1.getHour(), 23);
			assertEq(d1.getDayOfYear(), 60);
			s1 = "2004-03-01T23:59:59+02:00"; //leap year
			s2 = "2006-03-01T13:59:59+02:00";
			d1 = SDatetime.parseISO8601(s1);
			d2 = SDatetime.parseISO8601(s2);
			assertTrue(SDatetime.getCalendarDaysDifference(d1, d2) == 731,
				"Days: " + SDatetime.getCalendarDaysDifference(d1, d2));
			assertTrue(SDatetime.getCalendarMonthsDifference(d1, d2) == 24,
				"Months: " + SDatetime.getCalendarMonthsDifference(d1, d2));
			assertTrue(SDatetime.getCalendarYearsDifference(d1, d2) == 2,
				"Years: " + SDatetime.getCalendarYearsDifference(d1, d2));
			assertEq(d1.getDay(), 1);
			assertEq(d1.getMonth(), 3);
			assertEq(d1.getYear(), 2004);
			assertEq(d1.getHour(), 23);
			assertEq(d1.getDayOfYear(), 61);
		} catch (Exception ex) {fail(ex);}

		try {//EasterMonday
			SDatetime d;
			d = SDatetime.getEasterMonday(1980);
			assertEq(d.getDay(), 7);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1981);
			assertEq(d.getDay(), 20);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1982);
			assertEq(d.getDay(), 12);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1983);
			assertEq(d.getDay(), 4);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1984);
			assertEq(d.getDay(), 23);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1985);
			assertEq(d.getDay(), 8);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1986);
			assertEq(d.getDay(), 31);
			assertEq(d.getMonth(), 3);
			d = SDatetime.getEasterMonday(1991);
			assertEq(d.getDay(), 1);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(1997);
			assertEq(d.getDay(), 31);
			assertEq(d.getMonth(), 3);
			d = SDatetime.getEasterMonday(2002);
			assertEq(d.getDay(), 1);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2006);
			assertEq(d.getDay(), 17);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2007);
			assertEq(d.getDay(), 9);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2008);
			assertEq(d.getDay(), 24);
			assertEq(d.getMonth(), 3);
			d = SDatetime.getEasterMonday(2010);
			assertEq(d.getDay(), 5);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2011);
			assertEq(d.getDay(), 25);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2013);
			assertEq(d.getDay(), 1);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2018);
			assertEq(d.getDay(), 2);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2021);
			assertEq(d.getDay(), 5);
			assertEq(d.getMonth(), 4);
			d = SDatetime.getEasterMonday(2024);
			assertEq(d.getDay(), 1);
			assertEq(d.getMonth(), 4);
		} catch (Exception ex) {fail(ex);}
		//deleteAll
		try {
			File f = new File(SUtils.getTempDir() + "deleteAll");
			if (f.exists()) {
				f.delete();
			}
			if (f.list() != null) {
				fail("deleteAll - can't make test");
			}
			f.mkdir();
			try {
				String s = SUtils.getDirPath(f);
				new File(s + "f1").createNewFile();
				new File(s + "f2").createNewFile();
			} catch (Exception ex) {
				fail("deleteAll - can't make test");
			}
			try {
				SUtils.deleteAll(f, true);
			} catch (SException ex) {}
			if (f.exists()) {
				fail("deleteAll - remains " + f.list().length + " files");
			}
		} catch (Exception ex) {fail(ex);}
		//filesToZip
		try {
			File f, f1, result;
			f = new File(SUtils.getTempDir() + "filesToZip");
			try {
				SUtils.deleteAll(f, true);
			} catch (SException ex) {}
			f.mkdir();
			f1 = new File(SUtils.getTempDir() + "filesToZip1");
			try {
				SUtils.deleteAll(f1, true);
			} catch (SException ex) {}
			f1.mkdir();
			String s = SUtils.getDirPath(f);
			SUtils.writeString(new File(s + "f1.1"), "f1.1 abcabcabcabcabcabc");
			SUtils.writeString(new File(s + "f2.2"), "f2.2 abcabcabcabcabcabc");
			SUtils.writeString(new File(s + "f3.3"), "f3.3 abcabcabcabcabcabc");
			SUtils.writeString(new File(s + "f.1"), "f.1 abcabcabcabcabcabc");
			SUtils.writeString(new File(s + "f.2"), "f.2 abcabcabcabcabcabc");
			SUtils.writeString(new File(s + "f.3"), "f.3 abcabcabcabcabcabc");
			result = new File(s + "f.zip");
			try {
				long len = SUtils.filesToZip(f.listFiles(), null, result);
				assertTrue(len == 135,
					"filesToZip - incorrect length = " + len);
				SUtils.filesFromZip(f1, result, null);
				assertTrue(f1.list().length == 6,
					"filesToZip - incorrect number of expanded files = " +
						f1.list().length);
			} catch (SException ex) {fail(ex);}

			result = new File(s + "f.zip");
			try {
				long len = SUtils.filesToZip(s + "f1.1;" + s + "f2.2;" +
					s + "f3.3;" + s + "f.1;" + s + "f.2;" + s + "f.3",
					null,
					result);
				assertTrue(len == 135,
					"filesToZip - incorrect length = " + len);
				SUtils.filesFromZip(f1, result, null);
				assertTrue(f1.list().length == 6,
					"filesToZip - incorrect number of expanded files = " +
					f1.list().length);
			} catch (SException ex) {fail(ex);}
			result = new File(s + "f.zip");
			try {
				long len = SUtils.filesToZip(s + "f*.*", "zip;1;2",	result);
				assertTrue(len == 45,
					"filesToZip - incorrect length = " + len);
				SUtils.filesFromZip(f1, result, null);
				assertTrue(f1.list().length == 6,
					"filesToZip - incorrect number of expanded files = " +
						f1.list().length);
			} catch (Exception ex) {fail(ex);}
			result = new File(s + "f.zip");
			try {
				long len = SUtils.filesToZip(s + "x.1", null, result);
				fail("filesToZip - exception SYS73 expected");
			} catch (SException ex) {
				if (!"SYS073".equals(ex.getMsgID())) {
					fail(ex);
				}
			}
			try {
				SUtils.deleteAll(f, true);
				SUtils.deleteAll(f1, true);
			} catch (Exception ex) {}
		} catch (Exception ex) {fail(ex);}
		try {//getCountry
			String s;
			s = SUtils.getCountry();
			if (s == null || s.length() < 2 || s.length() > 3) {
				fail("getCountry='" + s + "'");
			}
		} catch (Exception ex) {fail(ex);}
		try {//getLanguage
			String s;
			s = SUtils.getLanguage();
			if (s == null || s.length() < 2 || s.length() > 3) {
				fail("getLanguage='" + s + "'");
			}
		} catch (Exception ex) {fail(ex);}
		try {//getISO3language
			try {
				SUtils.getISO3Language("pqrs");
				fail("getISO3Language - expected exception SYS18 is missing");
			} catch (SRuntimeException ex) {
				if (!"SYS018".equals(ex.getMsgID())) {
					fail(ex);
				}
			}
			String s = SUtils.getISO3Language(SUtils.getLanguage());
			if (s != null && s.length() != 3) {
				fail("getISO3Language error '" + SUtils.getLanguage()
					+ "' -> '" + s + "'");
			}
			s = SUtils.getISO3Language();
			if (s != null && s.length() != 3) {
				fail("getISO3Language error '" + SUtils.getLanguage()
					+ "' -> '" + s + "'");
			}
		} catch (Exception ex) {fail(ex);}
		try {// test StringParser.checkDateFormat
			Report r;
			r = StringParser.checkDateFormat(
				"{L(de)}'Hello, today is 'EEEE, d. MMMM GG yyyy.");
			if (r != null) {
				fail(r);
			}
			r = StringParser.checkDateFormat("{L(de)Z+0100H1m99L(*)}" +
				"'Hello, today is 'EEEE, d. MMMM GG yyyy.");
			if (r == null) {
				fail("error not reported");
			} else {
				assertEq("SYS062", r.getMsgID());
			}
			r = StringParser.checkDateFormat("{L(cs,CZ,akjshdlf,g)" +
				"z(America/New_York)Z+01:00H1m59L(*)}" +
				"EEEE.d[.MMMM[.GG[.yyyy]][z]|{L(*)}yyyy");
			if (r == null) {
				fail("error not reported");
			} else {
				assertEq("SYS064", r.getMsgID());
			}
			r = StringParser.checkDateFormat("{M30000}");
			if (r == null) {
				fail("Error not reported");
			}
			r = StringParser.checkDateFormat("yyy");
			if (r != null) {
				assertEq("SYS059", r.getMsgID());
			} else {
				fail("Error not reported");
			}
			r = StringParser.checkDateFormat("'abc'yyy");
			if (r != null) {
				assertEq("SYS059", r.getMsgID());
			} else {
				fail("Error not reported");
			}
			r = StringParser.checkDateFormat("'abcyyy");
			if (r != null) {
				assertEq("SYS049", r.getMsgID());
			} else {
				fail("Error not reported");
			}
			r = StringParser.checkDateFormat("''yyyy");
			assertEq(r, null);
			r = StringParser.checkDateFormat("yyyy''");
			assertEq(r, null);
			r = StringParser.checkDateFormat("''''yyyy");
			assertEq(r, null);
			r = StringParser.checkDateFormat("''''''yyyy");
			assertEq(r, null);
			r = StringParser.checkDateFormat("'a'''yyyy");
			assertEq(r, null);
			r = StringParser.checkDateFormat("'''a'yyyy");
			assertEq(r, null);
			r = StringParser.checkDateFormat("'a''b'yyyy");
			assertEq(r, null);
			r = StringParser.checkDateFormat("''RR");
			assertEq(r, null);
			r = StringParser.checkDateFormat("''R");
			assertEq(r.getMsgID(), "SYS059");
			r = StringParser.checkDateFormat("''RRR");
			assertEq(r.getMsgID(), "SYS059");
			r = StringParser.checkDateFormat("''Y");
			assertEq(r, null);
			r = StringParser.checkDateFormat("''YY");
			assertEq(r, null);
			r = StringParser.checkDateFormat("''YYY");
			assertEq(r.getMsgID(), "SYS059");
			r = StringParser.checkDateFormat("{z(Asia/Tel_Aviv)L(he,CZ,Win)" +
				"Z+01:00m59H1}EEEE.d[|.MMMM[.GG[.yyyy]]][z]|{L(*)}yyyy");
			if (r == null) {
				fail("error not reported");
			} else {
				assertEq("SYS078", r.getMsgID());
			}
			r = StringParser.checkDateFormat("{z(America/New_York)L(he,CZ,Win)"+
				"Z+01:00m59H1}EEEE.d[.MMMM[.GG[.yyyy]]][z]|{L(*)}yyyy");
			assertEq(r, null);
		} catch (Exception ex) {fail(ex);}
		try {
			final String s1 = "asjh kjhf kldjah ;aoihfo;weihf" +
				"awuioehf awileufh ilawuchilaweubncilawe bufail"+
//				"weubfwilaeubfailwebukbiwfw";
				"weubfwilaeubfailwebukbiwfwe";
			InputStream is = new InputStream() {
				private int i = 0;
				final private byte[] buf = s1.getBytes();
				@Override
				public int read() throws IOException {
					if (i >= buf.length) {
						return -1;
					}
					return (int) buf[i++];
				}
				@Override
				public int read(byte[] b) throws IOException {
					return read(b, 0, b.length);
				}
				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if (i>= buf.length) {
						return -1;
					}
					int result = i + len >= buf.length ? buf.length - i : len;
					System.arraycopy(buf, i, b, off, result);
					i+= result;
					return result;
				}
			};
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(bos);
			SUtils.encodeBase64(is, osw, true);
			is.close();
			osw.close();
			final String s2 = bos.toString();
			if (s2.indexOf(' ') > 0) {
				fail();
			}
			is = new InputStream() {
				private int i = 0;
				final private byte[] buf = s2.getBytes();
				@Override
				public int read() throws IOException {
					if (i >= buf.length) {
						return -1;
					}
					return (int) buf[i++];
				}
				@Override
				public int read(byte[] b) throws IOException {
					return read(b, 0, b.length);
				}
				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if (i>= buf.length) {
						return -1;
					}
					int result = i + len >= buf.length ? buf.length - i : len;
					System.arraycopy(buf, i, b, off, result);
					i+= result;
					return result;
				}
			};
			bos = new ByteArrayOutputStream();
			SUtils.decodeBase64(is, bos);
			is.close();
			bos.close();
			String s = bos.toString();
			assertEq(s1, s);
		} catch (Exception ex) {fail(ex);}
		try {
			SDatetime sd = new SDatetime("2015-12-06");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date d = df.parse("2015-12-06");
			assertTrue(sd.getCalendar().getTime().equals(d));
			sd = new SDatetime("2015-12-06T01:02:03");
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			d = df.parse("2015-12-06T01:02:03");
			assertTrue(sd.getCalendar().getTime().equals(d));
		} catch (Exception ex) {fail(ex);}
		try {
			// isLeapYear
			assertTrue(SDatetime.isLeapYear(2000));
			assertTrue(SDatetime.isLeapYear(2004));
			assertTrue(SDatetime.isLeapYear(2400));
			assertTrue(SDatetime.isLeapYear(2400));
			assertFalse(SDatetime.isLeapYear(2005));
			assertFalse(SDatetime.isLeapYear(1900));
			assertFalse(SDatetime.isLeapYear(2100));
			assertFalse(SDatetime.isLeapYear(2200));
			assertFalse(SDatetime.isLeapYear(2300));
			// getEasterMonday
			assertTrue(SDatetime.getEasterMonday(2000).equals(
				new SDatetime("2000-04-24")));
			assertTrue(SDatetime.getEasterMonday(2002).equals(
				new SDatetime("2002-04-01")), SDatetime.getEasterMonday(2002));
			// Timestamp
			SDatetime sd = new SDatetime("2000-01-01T01:01:02.999");
			java.sql.Timestamp tstamp = new java.sql.Timestamp(sd.getTimeInMillis());
			assertTrue(sd.getTimeInMillis() == tstamp.getTime());
			assertTrue(sd.getNanos() == tstamp.getNanos());
			assertTrue(sd.equals(tstamp));
			assertTrue(new SDatetime(tstamp).equals(tstamp));
			tstamp.setNanos(999);
			sd.setNanos(999);
			assertTrue(sd.getTimeInMillis() == tstamp.getTime());
			assertTrue(sd.getNanos() == tstamp.getNanos());
			assertTrue(sd.equals(tstamp));
			assertTrue(new SDatetime(tstamp).equals(tstamp));
			tstamp.setNanos(100000999);
			sd.setNanos(100000999);
			assertTrue(sd.getTimeInMillis() == tstamp.getTime());
			assertTrue(sd.getNanos() == tstamp.getNanos());
			assertTrue(sd.equals(tstamp));
			assertTrue(new SDatetime(tstamp).equals(tstamp));
			tstamp.setNanos(999499999);
			sd.setNanos(999499999);
			assertTrue(sd.getTimeInMillis() == tstamp.getTime());
			assertTrue(sd.getNanos() == tstamp.getNanos());
			assertTrue(sd.equals(tstamp));
			assertTrue(new SDatetime(tstamp).equals(tstamp));
			tstamp.setNanos(999999999);
			sd.setNanos(999999999);
			assertTrue(sd.getTimeInMillis() == ((Date)tstamp).getTime());
			assertTrue(sd.getNanos() == tstamp.getNanos());
			assertTrue(sd.equals(tstamp));
			assertTrue(new SDatetime(tstamp).equals(tstamp));
		} catch (Exception ex) {fail(ex);}
	}

	/** Start test from command line. Print results on system output.
	 * @param args The parameter is ignored.
	 */
	public static void main(String[] args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
