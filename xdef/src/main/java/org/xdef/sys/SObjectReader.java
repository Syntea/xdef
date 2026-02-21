package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/** Provides reading of SObjects to output stream.
 * @author Vaclav Trojan
 */
public class SObjectReader extends InputStream {

    /** InputStream used by this SObjectRader. */
    private final InputStream _in;

    /** Create new instance of SObjectReader.
     * @param in InputStream used by this SObjectReader,
     */
    public SObjectReader(InputStream in) {_in = in;}

    /** Read array of bytes; length is given by argument.
     * @param len number of bytes to read.
     * @return array of bytes; length is given by argument.
     * @throws IOException if an error occurs.
     */
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

    /** Read array of bytes.
     * @return array of bytes.
     * @throws IOException if an error occurs.
     */
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

    /** Read boolean value.
     * @return boolean value.
     * @throws IOException if an error occurs.
     */
    public final boolean readBoolean() throws IOException {
        int i;
        if ((i = _in.read()) < 0) {
            //SObject reader: incorrect format of data&{0}{: }
            throw new SIOException(SYS.SYS039, "readLength - unexpected EOF");
        }
        return i != 0;
    }

    /** Read byte value.
     * @return byte value.
     * @throws IOException if an error occurs.
     */
    public final byte readByte() throws IOException {
        int i;
        if ((i = _in.read()) < 0) {
            //SObject reader: incorrect format of data&{0}{: }
            throw new SIOException(SYS.SYS039, "readByte = unexpected EOF");
        }
        return (byte) (i > Byte.MAX_VALUE ? i - 256 : i);
    }

    /** Read short value.
     * @return short value.
     * @throws IOException if an error occurs.
     */
    public final short readShort() throws IOException {
        byte[] x = readBytes(2);
        return (short) ((0xff & x[0]) << 8 | 0xff & x[1]);
    }

    /** Read char value.
     * @return char value.
     * @throws IOException if an error occurs.
     */
    public final char readChar() throws IOException {return (char) readShort();}

    /** Read integer value.
     * @return integer value.
     * @throws IOException if an error occurs.
     */
    public final int readInt() throws IOException {
        byte[] x = readBytes(4);
        return (0xff & x[0]) << 24 | (0xff & x[1]) << 16 | (0xff & x[2]) << 8 | 0xff & x[3];
    }

    /** Read float value.
     * @return float value.
     * @throws IOException if an error occurs.
     */
    public final float readFloat() throws IOException {return Float.intBitsToFloat(readInt());}

    /** Read long value.
     * @return long value.
     * @throws IOException if an error occurs.
     */
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

    /** Read double value.
     * @return double value.
     * @throws IOException if an error occurs.
     */
    public final double readDouble() throws IOException {return Double.longBitsToDouble(readLong());}

    /** Read BigDecimal value.
     * @return BigDecimal value.
     * @throws IOException if an error occurs.
     */
    public final BigDecimal readBigDecimal() throws IOException {
        String s = readString();
        return (s == null) ? null : new BigDecimal(s);
    }

    /** Read BigInteger value.
     * @return BigInteger value.
     * @throws IOException if an error occurs.
     */
    public final BigInteger readBigInteger() throws IOException {
        byte[] bytes = readBytes();
        return (bytes == null) ? null : new BigInteger(bytes);
    }

    /** Read String value.
     * @return String value.
     * @throws IOException if an error occurs.
     */
    public final String readString() throws IOException {
        byte[] bytes = readBytes();
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    /** Read SPosition value.
     * @return SPosition value.
     * @throws IOException if an error occurs.
     */
    public final SPosition readSPosition() throws IOException {
        return (readBoolean()) ? SPosition.readObj(this) : null;
    }

    /** Read SDatetime value.
     * @return SDatetime value.
     * @throws IOException if an error occurs.
     */
    public final SDatetime readSDatetime() throws IOException {
        return (readBoolean()) ? SDatetime.readObj(this) : null;
    }

    /** Read SDuration value.
     * @return SDuration value.
     * @throws IOException if an error occurs.
     */
    public final SDuration readSDuration() throws IOException {
        return (readBoolean()) ? SDuration.readObj(this) : null;
    }

    /** Read Report value.
     * @return Report value.
     * @throws IOException if an error occurs.
     */
    public final Report readReport() throws IOException {return (readBoolean())? Report.readObj(this) : null;}

    /** Read BNFGrammar value.
     * @return BNFGrammar value.
     * @throws IOException if an error occurs.
     */
    public final BNFGrammar readBNFGrammar() throws IOException {
        return (readBoolean()) ? BNFGrammar.readObj(this) : null;
    }

    /** Get InputStream assigned to this SObjectReader.
     * @return InputStream assigned to this SObjectReader.
     */
    public final InputStream getStream() {return _in;}

    /** Close this SObjectReader.
     * @throws IOException if an error occurs.
     */
    @Override
    public final void close() throws IOException {_in.close();}

    /** read byte from this SObjectReader.
     * @return the next byte of data, or -1 if the end of the stream is reached.
     * @throws IOException if an error occurs.
     */
    @Override
    public int read() throws IOException {return _in.read();}
}