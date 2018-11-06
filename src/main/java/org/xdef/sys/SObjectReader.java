package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/** Provides reading of SObjects to output stream.
 * @author Vaclav Trojan
 */
public class SObjectReader {

	private final InputStream _in;

	public SObjectReader(InputStream in) {_in = in;}

	private byte[] readBytes(final int len) throws IOException {
		byte[] result = new byte[len];
		if (len > 0) {
			int i;
			if ((i = _in.read(result)) < 0) {
				throw new SIOException(SYS.SYS091); //Read after eof
			}
			while (i < len) {
				int j;
				if ((j = _in.read(result, i, len - i)) < 0) {
					throw new SIOException(SYS.SYS091); //Read after eof
				}
				i += j;
			}
		}
		return result;
	}

	public final byte[] readBytes() throws IOException {
		int len;
		return (len=readLength()) == Integer.MAX_VALUE ? null : readBytes(len);
	}

	/** Read length of an object (returns the positive integer or 0).
	 * @return positive integer or 0.
	 * @throws IOException if an error occurs.
	 */
	public final int readLength() throws IOException {
		int i;
		if ((i = _in.read()) < 0) {
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, "readLength - unexpected EOF");
		}
		return i == 255 ? readInt() : i;
	}

	public final boolean readBoolean() throws IOException {
		int i;
		if ((i = _in.read()) < 0) {
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, "readLength - unexpected EOF");
		}
		return i != 0;
	}

	public final byte readByte() throws IOException {
		int i;
		if ((i = _in.read()) < 0) {
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, "readByte = unexpected EOF");
		}
		return (byte) (i > Byte.MAX_VALUE ? i - 256 : i);
	}

	public final short readShort() throws IOException {
		byte[] x = readBytes(2);
		return (short) ((0xff & x[0]) << 8 | 0xff & x[1]);
	}

	public final char readChar() throws IOException {
		return (char) readShort();
	}

	public final int readInt() throws IOException {
		byte[] x = readBytes(4);
		return (0xff & x[0]) << 24 | (0xff & x[1]) << 16
			| (0xff & x[2]) << 8 | 0xff & x[3];
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final long readLong() throws IOException {
		byte[] x = readBytes(8);
		return ((long) (0xff & x[0])) << 56
			| ((long) (0xff & x[1])) << 48
			| ((long) (0xff & x[2])) << 40
			| ((long) (0xff & x[3])) << 32
			| ((long) (0xff & x[4])) << 24
			| ((long) (0xff & x[5])) << 16
			| ((long) (0xff & x[6])) << 8
			| (long) 0xff & x[7];
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public final BigDecimal readBigDecimal() throws IOException {
		String s = readString();
		return (s == null) ? null : new BigDecimal(s);
	}

	public final String readString() throws IOException {
		byte[] bytes = readBytes();
		return bytes == null
			? null : new String(bytes, Charset.forName("UTF-8"));
	}

	public final SPosition readSPosition() throws IOException {
		return (readBoolean()) ? SPosition.readObj(this) : null;
	}

	public final SDatetime readSDatetime() throws IOException {
		return (readBoolean()) ? SDatetime.readObj(this) : null;
	}

	public final SDuration readSDuration() throws IOException {
		return (readBoolean()) ? SDuration.readObj(this) : null;
	}

	public final Report readReport() throws IOException {
		return (readBoolean()) ? Report.readObj(this) : null;
	}

	public final BNFGrammar readBNFGrammar() throws IOException {
		return (readBoolean()) ? BNFGrammar.readObj(this) : null;
	}

	public final InputStream getStream() {return _in;}

	public final void close() throws IOException {_in.close();}

}