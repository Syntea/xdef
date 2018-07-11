/*
 * File: TestReport.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */

package test.common.sys;

import cz.syntea.xdef.sys.STester;
import cz.syntea.xdef.sys.SObjectReader;
import cz.syntea.xdef.sys.SObjectWriter;
import cz.syntea.xdef.sys.SDatetime;
import cz.syntea.xdef.sys.SDuration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/** TestReport
 *
 * @author  Vaclav Trojan
 */
public class TestObjectWriter extends STester {

	public TestObjectWriter() {super();}

	String test(Object obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			SObjectWriter w = new SObjectWriter(out);
			Object result;
			ByteArrayInputStream in;
			if (obj instanceof Boolean) {
				w.writeBoolean(((Boolean) obj).booleanValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readBoolean() ? Boolean.TRUE : Boolean.FALSE;
			} else if (obj instanceof Character) {
				w.writeChar(((Character) obj).charValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Character(r.readChar());
			} else if (obj instanceof Byte) {
				w.writeByte(((Byte) obj).byteValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Byte(r.readByte());
			} else if (obj instanceof Short) {
				w.writeShort(((Short) obj).shortValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Short(r.readShort());
			} else if (obj instanceof Integer) {
				w.writeInt(((Integer) obj).intValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Integer(r.readInt());
			} else if (obj instanceof Long) {
				w.writeLong(((Long) obj).longValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Long(r.readLong());
			} else if (obj instanceof Float) {
				w.writeFloat(((Float) obj).floatValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Float(r.readFloat());
			} else if (obj instanceof Double) {
				w.writeDouble(((Double) obj).doubleValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = new Double(r.readDouble());
			} else if (obj instanceof String) {
				w.writeString(obj.toString());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readString();
			} else if (obj instanceof SDatetime) {
				SDatetime y = (SDatetime) obj;
				w.writeSDatetime(y);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readSDatetime();
			} else if (obj instanceof SDuration) {
				SDuration y = (SDuration) obj;
				w.writeSDuration(y);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readSDuration();
			} else if (obj instanceof byte[]) {
				byte[] bytes = (byte[]) obj;
				w.writeBytes(bytes);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				byte[] bytes1 = r.readBytes();
				result = Arrays.equals(bytes, bytes) ? obj :
					("Error bytes, len=" + bytes1.length);
			} else if (obj instanceof StringBuffer) {//len
				Integer i = Integer.valueOf(obj.toString());
				obj = i;
				w.writeLength(i.intValue());
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				int j = r.readLength();
				result = new Integer(j);
			} else {
				return "Unsupported type: " + obj;
			}
			return "" + (in.read() >= 0 ? "Not eof" : "") +
				(obj.equals(result) ? "" : ("error:" + result.toString()));
		} catch (Exception e) { //return string with the exception
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.close();
			return pw.toString();
		}
	}
	@Override
	/** Run test and print error information. */
	public void test() {
		String s;
		assertTrue("".equals(s = test(new Byte((byte) 0))), s);
		assertTrue("".equals(s = test(new Byte((byte) 1))), s);
		assertTrue("".equals(s = test(new Byte((byte) -1))), s);
		assertTrue("".equals(s = test(new Byte((byte) 2))), s);
		assertTrue("".equals(s = test(new Byte((byte) -2))), s);
		assertTrue("".equals(s = test(new Byte(Byte.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Byte(Byte.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Short((short) 0))), s);
		assertTrue("".equals(s = test(new Short((short) 1))), s);
		assertTrue("".equals(s = test(new Short((short) -1))), s);
		assertTrue("".equals(s = test(new Short((short) 2))), s);
		assertTrue("".equals(s = test(new Short((short) -2))), s);
		assertTrue("".equals(s = test(new Short(Byte.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Short(Byte.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Short(Short.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Short(Short.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Integer(0))), s);
		assertTrue("".equals(s = test(new Integer(1))), s);
		assertTrue("".equals(s = test(new Integer(-1))), s);
		assertTrue("".equals(s = test(new Integer(2))), s);
		assertTrue("".equals(s = test(new Integer(-2))), s);
		assertTrue("".equals(s = test(new Integer(Byte.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Integer(Byte.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Integer(Short.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Integer(Short.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Integer(Integer.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Integer(Integer.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Long(0))), s);
		assertTrue("".equals(s = test(new Long(1))), s);
		assertTrue("".equals(s = test(new Long(-1))), s);
		assertTrue("".equals(s = test(new Long(2))), s);
		assertTrue("".equals(s = test(new Long(-2))), s);
		assertTrue("".equals(s = test(new Long(Byte.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Byte.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Short.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Short.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Integer.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Integer.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Long.MAX_VALUE))), s);
		assertTrue("".equals(s = test(new Long(Long.MIN_VALUE))), s);
		assertTrue("".equals(s = test(new Character((char) 0))), s);
		assertTrue("".equals(s = test(new Character('a'))), s);
		assertTrue("".equals(s = test(new Character((char) 65535))), s);
		assertTrue("".equals(s = test(new Float(0))), s);
		assertTrue("".equals(s = test(new Float(1))), s);
		assertTrue("".equals(s = test(new Float(-1))), s);
		assertTrue("".equals(s = test(new Float(65535))), s);
		assertTrue("".equals(s = test(new Float(-65535))), s);
		assertTrue("".equals(s = test(new Float(3.141592))), s);
		assertTrue("".equals(s = test(new Float(0.000000001))), s);
		assertTrue("".equals(s = test(new Float(0.999999999))), s);
		assertTrue("".equals(s = test(new Float(Float.NaN))), s);
		assertTrue("".equals(s = test(new Float(Float.POSITIVE_INFINITY))), s);
		assertTrue("".equals(s = test(new Float(Float.NEGATIVE_INFINITY))), s);
		assertTrue("".equals(s = test(new Double(0))), s);
		assertTrue("".equals(s = test(new Double(1))), s);
		assertTrue("".equals(s = test(new Double(-1))), s);
		assertTrue("".equals(s = test(new Double(65535))), s);
		assertTrue("".equals(s = test(new Double(-65535))), s);
		assertTrue("".equals(s = test(new Double(3.141592))), s);
		assertTrue("".equals(s = test(new Double(0.0000000000000001))), s);
		assertTrue("".equals(s = test(new Double(0.9999999999999999))), s);
		assertTrue("".equals(s = test(new Double(Double.NaN))), s);
		assertTrue("".equals(s = test(new Double(Double.POSITIVE_INFINITY))),s);
		assertTrue("".equals(s = test(new Double(Double.NEGATIVE_INFINITY))),s);
		assertTrue("".equals(s = test(new StringBuffer("0"))), s);
		assertTrue("".equals(s = test(new StringBuffer("1"))), s);
		assertTrue("".equals(s = test(new StringBuffer("255"))), s);
		assertTrue("".equals(s = test(new StringBuffer("256"))), s);
		assertTrue("".equals(s = test(new StringBuffer("65535"))), s);
		assertTrue("".equals(s = test(new StringBuffer("65536"))), s);
		assertTrue("".equals(s = test(new StringBuffer("99999999"))), s);
		assertTrue("".equals(s = test("")), s);
		assertTrue("".equals(s = test("\u0000xxx\uffff")), s);
		char[] chars = new char[66000];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) 1234;
		}
		s = String.valueOf(chars);
		assertTrue("".equals(s = test(s)), s);
		byte[] bytes = new byte[66000];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) 123;
		}
		assertTrue("".equals(s = test(bytes)), s);
		assertTrue("".equals(s = test(new SDatetime("2015-11-15"))), s);
		assertTrue("".equals(s = test(new SDatetime("2015-11-15-05:00"))), s);
		assertTrue(
			"".equals(s = test(new SDatetime("2015-11-15T11:31:01-05:00"))), s);
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
