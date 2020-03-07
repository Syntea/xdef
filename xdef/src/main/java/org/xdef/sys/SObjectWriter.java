package org.xdef.sys;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/** Provides writing of SObjects to output stream.
 * @author Vaclav Trojan
 */
public class SObjectWriter {

	private final OutputStream _out;

	public SObjectWriter(OutputStream out) {_out = out;}

	/** Write length of an object (a positive integer or 0).
	 * @param x length to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeLength(final int x) throws IOException {
		if (x < 255) {
			_out.write(x);
		} else {
			_out.write(255);
			writeInt(x);
		}
	}

	synchronized public final void writeBoolean(final boolean x)
		throws IOException {
		writeByte((byte) (x ? 1 : 0));
	}

	synchronized public final void writeByte(final byte x) throws IOException {
		_out.write(x);
	}

	synchronized public final void writeShort(final short x) throws IOException{
		_out.write(x >> 8);
		_out.write(x);
	}

	synchronized public final void writeChar(final char x) throws IOException {
		writeShort((short) x);
	}

	synchronized public final void writeInt(final int x) throws IOException {
		_out.write(x >> 24);
		_out.write(x >> 16);
		_out.write(x >> 8);
		_out.write(x);
	}

	synchronized public final void writeFloat(final float x) throws IOException{
		writeInt(Float.floatToRawIntBits(x));
	}

	synchronized public final void writeLong(final long x) throws IOException {
		_out.write((int)((x >> 56)));
		_out.write((int)((x >> 48)));
		_out.write((int)((x >> 40)));
		_out.write((int)((x >> 32)));
		_out.write((int)((x >> 24)));
		_out.write((int)((x >> 16)));
		_out.write((int)((x >> 8)));
		_out.write((int)(x));
	}

	synchronized public final void writeDouble(final double x)
		throws IOException {
		writeLong(Double.doubleToRawLongBits(x));
	}

	synchronized public final void writeBytes(final byte[] x)
		throws IOException {
		if (x == null) {
			writeLength(Integer.MAX_VALUE);
		} else {
			int len = x.length;
			writeLength(len);
			_out.write(x);
		}
	}

	synchronized public final void writeBigDecimal(final BigDecimal x)
		throws IOException {
		writeString(x == null ? null : x.toString());
	}

	synchronized public final void writeString(final String x)
		throws IOException {
		writeBytes((x == null) ? null : x.getBytes(Charset.forName("UTF-8")));
	}

	synchronized public final void writeSPosition(final SPosition x)
		throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	synchronized public final void writeSDatetime(final SDatetime x)
		throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	synchronized public final void writeSDuration(final SDuration x)
		throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	synchronized public final void writeReport(final Report x)
		throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	synchronized public final void writeBNFGrammar(final BNFGrammar x)
		throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	public final OutputStream getStream() {return _out;}

	public final void close() throws IOException {_out.close();}

}