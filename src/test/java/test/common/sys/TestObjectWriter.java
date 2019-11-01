package test.common.sys;

import org.xdef.sys.SObjectReader;
import org.xdef.sys.SObjectWriter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import builtools.STester;

/** Test SObjectWriter.
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
				w.writeBoolean((Boolean) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readBoolean() ? Boolean.TRUE : Boolean.FALSE;
			} else if (obj instanceof Character) {
				w.writeChar((Character) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readChar();
			} else if (obj instanceof Byte) {
				w.writeByte((Byte) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readByte();
			} else if (obj instanceof Short) {
				w.writeShort((Short) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readShort();
			} else if (obj instanceof Integer) {
				w.writeInt((Integer) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readInt();
			} else if (obj instanceof Long) {
				w.writeLong((Long) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readLong();
			} else if (obj instanceof Float) {
				w.writeFloat((Float) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readFloat();
			} else if (obj instanceof Double) {
				w.writeDouble((Double) obj);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				result = r.readDouble();
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
				w.writeLength(i);
				out.close();
				in = new ByteArrayInputStream(out.toByteArray());
				SObjectReader r = new SObjectReader(in);
				int j = r.readLength();
				result = j;
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
		assertTrue("".equals(s = test((byte) 0)), s);
		assertTrue("".equals(s = test((byte) 1)), s);
		assertTrue("".equals(s = test((byte) -1)), s);
		assertTrue("".equals(s = test((byte) 2)), s);
		assertTrue("".equals(s = test((byte) -2)), s);
		assertTrue("".equals(s = test(Byte.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Byte.MIN_VALUE)), s);
		assertTrue("".equals(s = test((short) 0)), s);
		assertTrue("".equals(s = test((short) 1)), s);
		assertTrue("".equals(s = test((short) -1)), s);
		assertTrue("".equals(s = test((short) 2)), s);
		assertTrue("".equals(s = test((short) -2)), s);
		assertTrue("".equals(s = test(Byte.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Byte.MIN_VALUE)), s);
		assertTrue("".equals(s = test(Short.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Short.MIN_VALUE)), s);
		assertTrue("".equals(s = test(0)), s);
		assertTrue("".equals(s = test(1)), s);
		assertTrue("".equals(s = test(-1)), s);
		assertTrue("".equals(s = test(2)), s);
		assertTrue("".equals(s = test(-2)), s);
		assertTrue("".equals(s = test((int) Byte.MAX_VALUE)), s);
		assertTrue("".equals(s = test((int) Byte.MIN_VALUE)), s);
		assertTrue("".equals(s = test((int) Short.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Short.MIN_VALUE)), s);
		assertTrue("".equals(s = test(Integer.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Integer.MIN_VALUE)), s);
		assertTrue("".equals(s = test(0L)), s);
		assertTrue("".equals(s = test(1L)), s);
		assertTrue("".equals(s = test(-1L)), s);
		assertTrue("".equals(s = test(2L)), s);
		assertTrue("".equals(s = test(-2L)), s);
		assertTrue("".equals(s = test((long) Byte.MAX_VALUE)), s);
		assertTrue("".equals(s = test((long) Byte.MIN_VALUE)), s);
		assertTrue("".equals(s = test((long) Short.MAX_VALUE)), s);
		assertTrue("".equals(s = test((long) Short.MIN_VALUE)), s);
		assertTrue("".equals(s = test(Integer.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Integer.MIN_VALUE)), s);
		assertTrue("".equals(s = test(Long.MAX_VALUE)), s);
		assertTrue("".equals(s = test(Long.MIN_VALUE)), s);
		assertTrue("".equals(s = test((char) 0)), s);
		assertTrue("".equals(s = test('a')), s);
		assertTrue("".equals(s = test((char) 65535)), s);
		assertTrue("".equals(s = test(0F)), s);
		assertTrue("".equals(s = test(1F)), s);
		assertTrue("".equals(s = test(-1F)), s);
		assertTrue("".equals(s = test(65535F)), s);
		assertTrue("".equals(s = test(-65535F)), s);
		assertTrue("".equals(s = test(3.141592F)), s);
		assertTrue("".equals(s = test(0.000000001F)), s);
		assertTrue("".equals(s = test(0.999999999F)), s);
		assertTrue("".equals(s = test(Float.NaN)), s);
		assertTrue("".equals(s = test(Float.POSITIVE_INFINITY)), s);
		assertTrue("".equals(s = test(Float.NEGATIVE_INFINITY)), s);
		assertTrue("".equals(s = test(0D)), s);
		assertTrue("".equals(s = test(1D)), s);
		assertTrue("".equals(s = test(-1D)), s);
		assertTrue("".equals(s = test(65535D)), s);
		assertTrue("".equals(s = test(-65535D)), s);
		assertTrue("".equals(s = test(3.141592D)), s);
		assertTrue("".equals(s = test(0.000000000000000D)), s);
		assertTrue("".equals(s = test(0.9999999999999999D)), s);
		assertTrue("".equals(s = test(Double.NaN)), s);
		assertTrue("".equals(s = test(Double.POSITIVE_INFINITY)),s);
		assertTrue("".equals(s = test(Double.NEGATIVE_INFINITY)),s);
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
