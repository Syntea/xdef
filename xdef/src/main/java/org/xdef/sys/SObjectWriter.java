package org.xdef.sys;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/** Provides writing of SObjects to output stream.
 * @author Vaclav Trojan
 */
public class SObjectWriter {

	/** OutputStream used by this SObjectWriter. */
	private final OutputStream _out;

	/** Create new instance of SObjectWriter.
	 * @param out OutputStream used by this SObjectWriter,
	 */
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

	/** Write boolean value.
	 * @param x boolean value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeBoolean(final boolean x) throws IOException {
		writeByte((byte) (x ? 1 : 0));
	}

	/** Write byte value.
	 * @param x byte value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeByte(final byte x) throws IOException {
		_out.write(x);
	}

	/** Write short value.
	 * @param x short value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeShort(final short x) throws IOException{
		_out.write(x >> 8);
		_out.write(x);
	}

	/** Write char value.
	 * @param x char value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeChar(final char x) throws IOException {
		writeShort((short) x);
	}

	/** Write integer value.
	 * @param x integer value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeInt(final int x) throws IOException {
		_out.write(x >> 24);
		_out.write(x >> 16);
		_out.write(x >> 8);
		_out.write(x);
	}

	/** Write float value.
	 * @param x float value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeFloat(final float x) throws IOException{
		writeInt(Float.floatToRawIntBits(x));
	}

	/** Write long value.
	 * @param x long value to be written.
	 * @throws IOException if an error occurs.
	 */
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

	/** Write double value.
	 * @param x double value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeDouble(final double x) throws IOException {
		writeLong(Double.doubleToRawLongBits(x));
	}

	/** Write byte array value.
	 * @param x byte array value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeBytes(final byte[] x) throws IOException {
		if (x == null) {
			writeLength(Integer.MAX_VALUE);
		} else {
			int len = x.length;
			writeLength(len);
			_out.write(x);
		}
	}

	/** Write BigDecimal value.
	 * @param x BigDecimal value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeBigDecimal(final BigDecimal x) throws IOException {
		writeString(x == null ? null : x.toString());
	}

	/** Write BigInteger value.
	 * @param x BigInteger value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeBigInteger(final BigInteger x) throws IOException {
		writeBytes(x == null ? null : x.toByteArray());
	}

	/** Write String value.
	 * @param x String value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeString(final String x) throws IOException {
		writeBytes((x == null) ? null : x.getBytes(StandardCharsets.UTF_8));
	}

	/** Write SPosition value.
	 * @param x SPosition value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeSPosition(final SPosition x) throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	/** Write SDatetime value.
	 * @param x SDatetime value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeSDatetime(final SDatetime x) throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	/** Write SDuration value.
	 * @param x SDuration value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeSDuration(final SDuration x) throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	/** Write Report value.
	 * @param x Report value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeReport(final Report x) throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	/** Write BNFGrammar value.
	 * @param x BNFGrammar value to be written.
	 * @throws IOException if an error occurs.
	 */
	synchronized public final void writeBNFGrammar(final BNFGrammar x) throws IOException {
		if (x == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			x.writeObj(this);
		}
	}

	/** Get OutputStream of this SObjectWriter.
	 * @return OutputStream of this SObjectWriter.
	 */
	public final OutputStream getStream() {return _out;}

	/** Close this SObjectWriter.
	 * @throws IOException if an error occurs.
	 */
	public final void close() throws IOException {_out.close();}
}