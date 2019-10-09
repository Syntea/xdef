package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

/** Collection of useful methods.
 * @author Vaclav Trojan
 */
public class SUtils extends FUtils {

	/** Version of Java VM as an integer composed from the string where
	 * the version part is multiplied by 100 and subversion part is added.
	 * E.g. "1.6" is converted to 106. The build version is ignored.
	 */
	public static final int JAVA_RUNTIME_VERSION_ID;

	/** The string with the last part of Java VM version information.
	 * E.g. if version information is "1.6.0_45" it will be "0_45".
	 */
	public static final String JAVA_RUNTIME_BUILD;

	static {
		String s;
		try {
			s = Runtime.class.getPackage().getImplementationVersion();
			if (s == null) {
				Class<?> cls = Runtime.class;
				java.lang.reflect.Method m = cls.getDeclaredMethod("version");
				Object o = m.invoke(null);
				cls = o.getClass();
				m = cls.getDeclaredMethod("version");
				s = m.invoke(null).toString();
			}
		} catch (Exception ex) {
			s = System.getProperty("java.version");
		}
		String[] ss = s.split("\\.");
		JAVA_RUNTIME_VERSION_ID = Integer.parseInt(ss[0]) * 100
			+ Integer.parseInt(ss[1]);
		s = "";
		for (int i = 2; i < ss.length; i++) {
			if (!s.isEmpty()) {
				s += '.';
			}
			s += ss[i];
		}
		JAVA_RUNTIME_BUILD = s;
	}

	/** Don't allow user to instantiate this class. */
	private SUtils() {}

	/** length of line of encoded hex format and base64 format. */
	private static final int ENCODED_LINE_LENGTH = 72;
	/** length of input buffer in the line of hexadecimal encoded data. */
	private static final int INPUT_HEXBUFFER_LENGTH = ENCODED_LINE_LENGTH/2;
	/** Hexadecimal digits. */
	private static final byte[] HEXDIGITS = new byte[] {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/** Cache to accelerate 2 letters/3 letter (getISO3Language method).*/
	private static final Map<String, String> LANGUAGES =
		 new LinkedHashMap<String, String>();

	/** Encodes a byte array to hexadecimal format, no blanks or line breaks
	 * are inserted.
	 * @param bytes The array of bytes to be encoded.
	 * @return the array of hexadecimal digits.
	 */
	public static final byte[] encodeHex(final byte[] bytes) {
		return encodeHex(bytes, 0, bytes.length);
	}

	/** Encodes a byte array to hexadecimal format, no blanks or line breaks
	 * are inserted.
	 * @param bytes array of bytes to be encoded.
	 * @param offset offset in the buffer of the first byte to encode.
	 * @param length number of bytes to read from the buffer.
	 * @return byte array with the Base64 encoded data.
	 * @throws SRuntimeException when an error occurs.
	 */
	public static final byte[] encodeHex(final byte[] bytes,
		final int offset,
		final int length) {
		if (offset + length > bytes.length) {
			throw new SRuntimeException(SYS.SYS080); //Index out of array
		}
		//allocate buffer of size of endoded data including padding characters
		byte[] buf = new byte[length*2];
		encodeHex(buf, 0, bytes, 0, length);
		return buf;
	}

	/** Encodes a byte array to character array hexadecimal format.
	 * @param outbuf output buffer where encoded data will be stored.
	 * @param outoff offset to output buffer where to start.
	 * @param inbuf input buffer with bytes to be encoded.
	 * @param inoff offset to input buffer where to start.
	 * @param length number of bytes to read from the input buffer.
	 * @return new offset to output buffer.
	 */
	private static int encodeHex(final byte[] outbuf,
		final int outoff,
		final byte[] inbuf,
		final int inoff,
		final int length) {
		//allocate buffer of size of endoded data including padding characters
		int lx = outoff;
		for(int i = inoff; i < length; i++) {
			int b;
			outbuf[lx++] = HEXDIGITS[(b = inbuf[i] & 0xff) >> 4];
			outbuf[lx++] = HEXDIGITS[b & 15];
		}
		return lx;
	}

	/** Encodes a byte array to character array hexadecimal format.
	 * @param outbuf output buffer where encoded data will be stored.
	 * @param outoff offset to output buffer where to start.
	 * @param inbuf input buffer with bytes to be encoded.
	 * @param inoff offset to input buffer where to start.
	 * @param length number of bytes to read from the input buffer.
	 * @return new offset to output buffer.
	 */
	private static int encodeHex(final char[] outbuf,
		final int outoff,
		final byte[] inbuf,
		final int inoff,
		final int length) {
		//allocate buffer of size of endoded data including padding characters
		int lx = outoff;
		for(int i = inoff; i < length; i++) {
			int b;
			outbuf[lx++] = (char) HEXDIGITS[(b = inbuf[i] & 0xff) >> 4];
			outbuf[lx++] = (char) HEXDIGITS[b & 15];
		}
		return lx;
	}

	/** Encodes binary input byte stream <b>fi</b> to the output text writer
	 * <b>fo</b> as stream of hexadecimal digits.
	 * @param fi InputStream with binary origin bytes.
	 * @param fo OutputStream for encoded Base64 result.
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeHex(final InputStream fi,
		final OutputStream fo) throws SException {
		final byte[] ibuf = new byte[INPUT_HEXBUFFER_LENGTH];
		final byte[] obuf;
		obuf = new byte[ENCODED_LINE_LENGTH];
		int len;
		try {
			if ((len = fi.read(ibuf)) == INPUT_HEXBUFFER_LENGTH) {
				for (;;) {
					encodeHex(obuf, 0, ibuf, 0, INPUT_HEXBUFFER_LENGTH);
					if ((len = fi.read(ibuf)) > 0) {
						fo.write(obuf);
						if (len < INPUT_HEXBUFFER_LENGTH) {
							break;
						}
					} else {
						fo.write(obuf, 0, ENCODED_LINE_LENGTH);
						return;
					}
				}
			}
			if (len > 0) {
				len = encodeHex(obuf, 0, ibuf, 0, len);
				fo.write(obuf, 0, len);
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Encodes binary input byte stream <b>fi</b> to the output text writer
	 * <b>fo</b> as stream of hexadecimal digits.
	 * @param fi InputStream with binary origin bytes.
	 * @param fo Writer for encoded Base64 result.
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeHex(final InputStream fi,
		final Writer fo) throws SException {
		final byte[] ibuf = new byte[INPUT_HEXBUFFER_LENGTH];
		final char[] obuf;
		obuf = new char[ENCODED_LINE_LENGTH];
		int len;
		try {
			if ((len = fi.read(ibuf)) == INPUT_HEXBUFFER_LENGTH) {
				for (;;) {
					encodeHex(obuf, 0, ibuf, 0, INPUT_HEXBUFFER_LENGTH);
					if ((len = fi.read(ibuf)) > 0) {
						fo.write(obuf);
						if (len < INPUT_HEXBUFFER_LENGTH) {
							break;
						}
					} else {
						fo.write(obuf, 0, ENCODED_LINE_LENGTH);
						return;
					}
				}
			}
			if (len > 0) {
				len = encodeHex(obuf, 0, ibuf, 0, len);
				fo.write(obuf, 0, len);
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes input stream in hexadecimal format <b>fi</b> to the output
	 * stream of bytes.
	 * @param fi InputStream with hexadecimal digits.
	 * @param fo decoded byte stream.
	 * @throws SException
	 * <ul>
	 * <li>SYS036 Program exception {msg}.</li>
	 * <li>SYS047 HEX format error.</li>
	 * </ul>
	 */
	public static final void decodeHex(final InputStream fi,
		final OutputStream fo) throws SException {
		try {
			int i;
			byte b = 0;
			while ((i = fi.read())==' ' || i=='\n' || i=='\r' || i=='\t') {}
			while (i > 0) {
				if (i >= '0' && i <= '9')  {
					b = (byte) (i - '0');
				} else if (i >= 'a' && i <= 'f') {
					b = (byte) (i - 'a' + 10);
				} else if (i >= 'A' && i <= 'F') {
					b = (byte) (i - 'A' + 10);
				} else {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				}
				i = fi.read();
				if (i >= '0' && i <= '9')  {
					fo.write((b << 4) + (i - '0'));
				} else if (i >= 'a' && i <= 'f') {
					fo.write((b << 4) + (i - 'a' + 10));
				} else if (i >= 'A' && i <= 'F') {
					fo.write((b << 4) + (i - 'A' + 10));
				} else {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				}
				if ((i = fi.read())==' ' || i=='\n' || i=='\r' || i=='\t') {
					break;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes reader in hexadecimal format <b>fi</b> to the output stream
	 * of bytes.
	 * @param fi Reader with hexadecimal digits.
	 * @param fo decoded byte stream.
	 * @throws SException
	 * <ul>
	 * <li>SYS036 Program exception </li>
	 * <li>SYS047 HEX format error.</li>
	 * </ul>
	 */
	public static final void decodeHex(final Reader fi,
		final OutputStream fo) throws SException {
		try {
			int i;
			byte b = 0;
			while ((i = fi.read())==' ' || i=='\n' || i=='\r' || i=='\t') {}
			while (i > 0) {
				if (i == ' ' || i == '\n' || i == '\r' || i == '\t') {
					continue; //skip white spaces
				}
				if (i >= '0' && i <= '9')  {
					b = (byte) (i - '0');
				} else if (i >= 'a' && i <= 'f') {
					b = (byte) (i - 'a' + 10);
				} else if (i >= 'A' && i <= 'F') {
					b = (byte) (i - 'A' + 10);
				} else {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				}
				i = fi.read();
				if (i >= '0' && i <= '9')  {
					fo.write((b << 4) + (i - '0'));
				} else if (i >= 'a' && i <= 'f') {
					fo.write((b << 4) + (i - 'a' + 10));
				} else if (i >= 'A' && i <= 'F') {
					fo.write((b << 4) + (i - 'A' + 10));
				} else {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				}
				if ((i = fi.read())==' ' || i=='\n' || i=='\r' || i=='\t') {
					break;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes binary input HEX stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param bytes byte array with hexadecimal data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeHex(final byte[] bytes) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeHex(new ByteArrayInputStream(bytes), out);
		return out.toByteArray();
	}

	/** Decodes binary input HEX stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param chars The char array with hexadecimal data.
	 * @return The byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeHex(final char[] chars) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeHex(new CharArrayReader(chars), out);
		return out.toByteArray();
	}

	/** Decodes binary input HEX stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param src The string with hexadecimal data.
	 * @return The byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeHex(final String src) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeHex(new StringReader(src), out);
		return out.toByteArray();
	}

	/** length of input buffer in the line of base64 encoded data. */
	private static final int INPUT_B64BUFFER_LENGTH = (ENCODED_LINE_LENGTH/4)*3;
	/** Code table for values 0..63 for BASE64 encoding. */
	private static final byte[] ENCODE_BASE64 = new byte[] {
		'A','B','C','D','E','F','G','H','I','J','K','L','M', //0..12
		'N','O','P','Q','R','S','T','U','V','W','X','Y','Z', //13..25
		'a','b','c','d','e','f','g','h','i','j','k','l','m', //26..38
		'n','o','p','q','r','s','t','u','v','w','x','y','z', //39..51
		'0','1','2','3','4','5','6','7','8','9',             //52..61
		'+','/'};                                            //62,63

	/** Encodes a byte array to Base64 format, no blanks or line breaks
	 * are inserted.
	 * @param bytes array of bytes to be encoded.
	 * @return byte array with the Base64 encoded data.
	 * @throws SRuntimeException when an error occurs.
	 */
	public static final byte[] encodeBase64(final byte[] bytes) {
		return encodeBase64(bytes, 0, bytes.length);
	}

	/** Encodes a byte array to Base64 format, no blanks or line breaks
	 * are inserted.
	 * @param bytes array of bytes to be encoded.
	 * @param offset offset in the buffer of the first byte to encode.
	 * @param length number of bytes to read from the buffer.
	 * @return byte array with the Base64 encoded data.
	 * @throws SRuntimeException when error occurs:
	 */
	public static final byte[] encodeBase64(final byte[] bytes,
		final int offset,
		final int length) {
		if (offset + length > bytes.length) {
			throw new SRuntimeException(SYS.SYS080); //Index out of array
		}
		//allocate buffer of size of endoded data including padding characters
		byte[] outbuf = new byte[((length + 2) / 3) * 4];
		encodeBase64(outbuf, 0, bytes,	offset, length);
		return outbuf;
	}

	/** Encodes a byte array to byte array in the Base64 format, no blanks
	 * or line breaks are inserted.
	 * @param outbuf output buffer where encoded data will be stored.
	 * @param outoff offset to output buffer where to start.
	 * @param inbuf input buffer with bytes to be encoded.
	 * @param inoff offset to input buffer where to start.
	 * @param length number of bytes to read from the input buffer.
	 * @return new offset to output buffer.
	 * @throws SRuntimeException when error occurs.
	 */
	private static int encodeBase64(final byte[] outbuf,
		final int outoff,
		final byte[] inbuf,
		final int inoff,
		final int length) {
		int maxIndex = inoff + length; // maximal index to input buffer
		//allocate buffer of size of endoded data including padding characters
		int i = inoff, lx = outoff;
		for(int max = maxIndex - 2; i < max;) {//encode all tripples of bytes
			int b1 = inbuf[i++] & 0xff;
			outbuf[lx++] = ENCODE_BASE64[b1 >> 2];
			int b2 = inbuf[i++] & 0xff;
			outbuf[lx++] = ENCODE_BASE64[((b1 << 4) | (b2 >> 4)) & 0x3F];
			int b3 = inbuf[i++] & 0xff;
			outbuf[lx++] = ENCODE_BASE64[((b2 << 2) | (b3 >> 6)) & 0x3F];
			outbuf[lx++] = ENCODE_BASE64[b3 & 0x3F];
		}
		if (i < maxIndex) {//one or two bytes remains to be encodeed
			int b1 = inbuf[i++] & 0xff;
			outbuf[lx++] = ENCODE_BASE64[b1 >> 2];
			if (i == maxIndex) { // one byte
				outbuf[lx++] = ENCODE_BASE64[(b1 << 4) & 0x3F];
				outbuf[lx++] = '=';
			} else {// two bytes
				int b2 = inbuf[i] & 0xff;
				outbuf[lx++] = ENCODE_BASE64[((b1 << 4) | (b2 >> 4)) & 0x3F];
				outbuf[lx++] = ENCODE_BASE64[((b2 & 0xf) << 2)];
			}
			outbuf[lx++] = '=';
		}
		return lx;
	}

	/** Encodes a byte array to char array in the Base64 format, no blanks
	 * or line breaks are inserted.
	 * @param outbuf output buffer where encoded data will be stored.
	 * @param outoff offset to output buffer where to start.
	 * @param inbuf input buffer with bytes to be encoded.
	 * @param inoff offset to input buffer where to start.
	 * @param length number of bytes to read from the input buffer.
	 * @return new offset to output buffer.
	 * @throws SRuntimeException when error occurs.
	 */
	private static int encodeBase64(final char[] outbuf,
		final int outoff,
		final byte[] inbuf,
		final int inoff,
		final int length) {
		int maxIndex = inoff + length; // maximal index to input buffer
		//allocate buffer of size of endoded data including padding characters
		int i = inoff, lx = outoff;
		for(int max = maxIndex - 2; i < max;) {//encode all tripples of bytes
			int b1 = inbuf[i++] & 0xff;
			outbuf[lx++] = (char) ENCODE_BASE64[b1 >> 2];
			int b2 = inbuf[i++] & 0xff;
			outbuf[lx++] = (char) ENCODE_BASE64[((b1 << 4) | (b2 >> 4)) & 0x3F];
			int b3 = inbuf[i++] & 0xff;
			outbuf[lx++] = (char) ENCODE_BASE64[((b2 << 2) | (b3 >> 6)) & 0x3F];
			outbuf[lx++] = (char) ENCODE_BASE64[b3 & 0x3F];
		}
		if (i < maxIndex) {//one or two bytes remains to be encodeed
			int b1 = inbuf[i++] & 0xff;
			outbuf[lx++] = (char) ENCODE_BASE64[b1 >> 2];
			if (i == maxIndex) { // one byte
				outbuf[lx++] = (char) ENCODE_BASE64[(b1 << 4) & 0x3F];
				outbuf[lx++] = '=';
			} else {// two bytes
				int b2 = inbuf[i] & 0xff;
				outbuf[lx++] =
					(char) ENCODE_BASE64[((b1 << 4) | (b2 >> 4)) & 0x3F];
				outbuf[lx++] = (char) ENCODE_BASE64[((b2 & 0xf) << 2)];
			}
			outbuf[lx++] = '=';
		}
		return lx;
	}

	/** Encodes binary input byte stream <b>fi</b> to the output stream
	 * <b>fo</b> in the form of MIME/BASE64.
	 * @param fi InputStream with binary origin bytes.
	 * @param out OutputStream for encoded Base64 result.
	 * @param lines if <tt>true</tt> the output is break to lines (72 bytes).
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeBase64(final InputStream fi,
		final OutputStream out,
		final boolean lines) throws SException {
		byte[] ibuf = new byte[INPUT_B64BUFFER_LENGTH];
		byte[] obuf;
		if (lines) {
			obuf = new byte[ENCODED_LINE_LENGTH + 1];
			obuf[ENCODED_LINE_LENGTH] = '\n';
		} else {
			obuf = new byte[ENCODED_LINE_LENGTH];
		}
		int len;
		try {
			if ((len = fi.read(ibuf)) == INPUT_B64BUFFER_LENGTH) {
				for (;;) {
					encodeBase64(obuf, 0, ibuf, 0, INPUT_B64BUFFER_LENGTH);
					if ((len = fi.read(ibuf)) > 0) {
						out.write(obuf);
						if (len < INPUT_B64BUFFER_LENGTH) {
							break;
						}
					} else {
						out.write(obuf, 0, ENCODED_LINE_LENGTH);
						return;
					}
				}
			}
			if (len > 0) {
				len = encodeBase64(obuf, 0, ibuf, 0, len);
				out.write(obuf, 0, len);
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Encodes binary input byte stream <b>fi</b> to the output stream
	 * <b>fo</b> in the form of MIME/BASE64.
	 * @param fi InputStream with binary origin bytes.
	 * @param fo Writer for encoded Base64 resulting character stream.
	 * @param lines if <tt>true</tt> the output is breaked to lines (72 bytes).
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeBase64(final InputStream fi,
		final Writer fo,
		final boolean lines) throws SException {
		byte[] ibuf = new byte[INPUT_B64BUFFER_LENGTH];
		char[] obuf;
		if (lines) {
			obuf = new char[ENCODED_LINE_LENGTH + 1];
			obuf[ENCODED_LINE_LENGTH] = '\n';
		} else {
			obuf = new char[ENCODED_LINE_LENGTH];
		}
		int len;
		try {
			if ((len = fi.read(ibuf)) == INPUT_B64BUFFER_LENGTH) {
				for (;;) {
					encodeBase64(obuf, 0, ibuf, 0, INPUT_B64BUFFER_LENGTH);
					if ((len = fi.read(ibuf)) > 0) {
						fo.write(obuf);
						if (len < INPUT_B64BUFFER_LENGTH) {
							break;
						}
					} else {
						fo.write(obuf, 0, ENCODED_LINE_LENGTH);
						return;
					}
				}
			}
			if (len > 0) {
				len = encodeBase64(obuf, 0, ibuf, 0, len);
				fo.write(obuf, 0, len);
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Encodes binary input byte array to the output array in the form
	 * of MIME/BASE64.
	 * @param bytes array of bytes to be encoded.
	 * @param lines if <tt>true</tt> the output is break to lines (72 bytes).
	 * @return string with encoded Base64.
	 */
	public static final byte[] encodeBase64(final byte[] bytes,
		final boolean lines) {
		if (lines) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				encodeBase64(new ByteArrayInputStream(bytes), out, lines);
				return out.toByteArray();
			} catch (Exception ex) {// never should happen
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else {
			return encodeBase64(bytes);
		}
	}

	/** Encodes binary input byte array to the output array in the form
	 * of MIME/BASE64.
	 * @param bytes array of bytes to be encoded.
	 * @param offset offset in the buffer of the first byte to encode.
	 * @param len maximum number of bytes to read from the buffer.
	 * @param lines if <tt>true</tt> the output is break to lines (72 bytes).
	 * @return string with encoded Base64.
	 */
	public static final byte[] encodeBase64(final byte[] bytes,
		final int offset,
		final int len,
		final boolean lines) {
		if (lines) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				encodeBase64(
					new ByteArrayInputStream(bytes, offset, len), out, lines);
				return out.toByteArray();
			} catch (Exception ex) {// never should happen
				throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
			}
		} else {
			return encodeBase64(bytes, offset, len);
		}
	}

	/** Table for Base64 decoding:
	 * <LI>decoding values 0..63</LI>
	 * <LI>64 represents white blanks</LI>
	 * <LI>65 padding character ('=')</LI>
	 * <LI>66-70 incorrect characters</LI>
	 * Syntax of input:
	 * Base64Binary  ::=  ((B64S B64S B64S B64S)*
	 *                    ((B64S B64S B64S B64) |
	 *                    (B64S B64S B16S '=') |
	 *                    (B64S B04S '=' #x20? '=')))?
	 *
	 * B64S         ::= B64 #x20?
	 * B16S         ::= B16 #x20?
	 * B04S         ::= B04 #x20?
	 * B04          ::=  [AQgw]
	 * B16          ::=  [AEIMQUYcgkosw048]
	 * B64          ::=  [A-Za-z0-9+/]
	 */
	private static final byte[] DECODE_BASE64 = new byte[] {
		70,70,70,70,70,70,70,70,70, //00..08
		64, //09 (TAB) - ignorable
		64, //10 (CR) - ignorable
		70, //11
		64, //12 (NP)
		64, //13 (LF) - ignorable
		70,70,70,70,70,70,70,70,70,70,70,70, //14..25
		67, //26 (EOF)
		70,70,70,70,70,
		64, //32 ' ' - ignorable
		70,70,70,70,70,70,70,70,70,70, //33..42
		62, //43 '+'
		70, //44
		66, //45 '-'
		70, //46
		63, //47 '/'
		52,53,54,55,56,57,58,59,60,61, //48..57 '0'..'9'
		70,70,70, //58..60
		65, //61 '=' - padding character
		70,70,70, //62..64
		0,1,2,3,4,5,6,7,8,9,           //65..74 'A'..'J'
		10,11,12,13,14,15,16,17,18,19, //75..84 'K'..'T'
		20,21,22,23,24,25,             //85..90 'U'..'Z'
		70,70,70,70,70,70,             //91..96
		26,27,28,29,30,31,32,33,34,35, //97..106 'a'..'j'
		36,37,38,39,40,41,42,43,44,45, //107..116 'k'..'t'
		46,47,48,49,50,51,             //117..122 'u'..'z'
		70,70,70,70,                   //123 .. 127
		65,                            //127 .. may be EOF
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //128..143
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //144..159
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //160..175
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //176..191
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //192..207
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //208..223
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70, //224..239
		70,70,70,70,70,70,70,70,70,70,70,70,70,70,70,70};//240..255

	/** Decodes binary input MIME/BASE64 stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param fi java.io.Reader with Base64 character stream.
	 * @param fo Writer for decoded byte stream.
	 * @throws SException
	 * <ul>
	 * <li>SYS036 .. Program exception: {msg}.</li>
	 * <li>SYS048 .. Base64 format error.</li>
	 * </ul>
	 */
	public static final void decodeBase64(final InputStream fi,
		final OutputStream fo) throws SException {
		final int bufmax = 960; //must be multiple of 3!
		int bx = 0;
		byte[] buf = new byte[bufmax];
		byte b1,b2,b3,b4;
		try {
			for (;;) {
				//skip white blanks
				int i;
				while ((b1 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b1 >= 65) {
					fo.write(buf,0,bx);
					if (i >= 0 && b1 != 64) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				//skip white blanks
				while ((b2 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b2 >= 65) {
					fo.write(buf,0,bx);
					if (b1 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					fo.write(b1 << 2);
					return; //last 6 bites filled by 0 (error???)
				}
				buf[bx++] = (byte)((b1 << 2) + (b2 >> 4));
				//skip white blanks
				while ((b3 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b3 >= 65) {
					fo.write(buf,0,bx);
					if (b3 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					while (DECODE_BASE64[(i = fi.read()) & 127] == 64) {}
					if (i != '=') {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					if ((b2 & (byte) 0x0F) != 0) { // last four bits not zero
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				buf[bx++] = (byte)((b2 << 4)+(b3 >> 2)); //2
				//skip white blanks
				while ((b4 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b4 >= 65) {
					fo.write(buf,0,bx);
					if (b4 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					if ((b3 & (byte) 0x03) != 0) { // last two bits not zero
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				buf[bx++] = (byte)((b3 << 6) + b4); //3
				if (bx >= bufmax) {
					fo.write(buf);
					bx = 0;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes binary input MIME/BASE64 stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param fi java.io.Reader with Base64 character stream.
	 * @param fo OutputStream for decoded byte stream.
	 * @throws SException
	 * <ul>
	 * <li>SYS036 .. Program exception {msg}.</li>
	 * <li>SYS048 .. Base64 format error.</li>
	 * </ul>
	 */
	public static final void decodeBase64(final Reader fi,
		final OutputStream fo) throws SException {
		final int bufmax = 960; //must be multiple of 3!
		int bx = 0;
		byte[] buf = new byte[bufmax];
		byte b1,b2,b3,b4;
		try {
			for (;;) {
				//skip white blanks
				int i;
				while ((b1 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b1 >= 65) {
					fo.write(buf,0,bx);
					if (i >= 0 && b1 != 64) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				//skip white blanks
				while ((b2 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b2 >= 65) {
					fo.write(buf,0,bx);
					if (b1 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					fo.write(b1 << 2);
					return; //last 6 bites filled by 0 (error???)
				}
				buf[bx++] = (byte)((b1 << 2) + (b2 >> 4));
				//skip white blanks
				while ((b3 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b3 >= 65) {
					fo.write(buf,0,bx);
					if (b3 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					while (DECODE_BASE64[(i = fi.read()) & 127] == 64) {}
					if (i != '=') {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					if ((b2 & (byte) 0x0F) != 0) { // last four bits not zero
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				buf[bx++] = (byte)((b2 << 4)+(b3 >> 2)); //2
				//skip white blanks
				while ((b4 = DECODE_BASE64[(i = fi.read()) & 127]) == 64) {}
				if (b4 >= 65) {
					fo.write(buf,0,bx);
					if (b4 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					if ((b3 & (byte) 0x03) != 0) { // last two bits not zero
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				buf[bx++] = (byte)((b3 << 6) + b4); //3
				if (bx >= bufmax) {
					fo.write(buf);
					bx = 0;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes binary input MIME/BASE64 stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param source byte array with base64 encoded data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeBase64(final byte[] source)
	throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(new ByteArrayInputStream(source), out);
		return out.toByteArray();
	}

	/** Decodes binary input MIME/BASE64 stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param source char array with base64 encoded data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeBase64(final char[] source)
	throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(new CharArrayReader(source), out);
		return out.toByteArray();
	}

	/** Decodes binary input MIME/BASE64 stream <b>fi</b> to the output stream
	 * of bytes.
	 * @param source string with base64 encoded data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeBase64(final String source)
	throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(new StringReader(source), out);
		return out.toByteArray();
	}

	/** Returns string of given length created from given characters.
	 * If argument "length" is less or equal zero then returns the empty string.
	 * @param length required length of result.
	 * @param ch character used for creation of the result.
	 * @return string of required length created from characters from
	 * the argument "ch".
	 */
	public static final String makeStringOfChars(final int length,
		final char ch) {
		if (length <= 0) {
			return "";
		}
		char[] buf = new char[length];
		Arrays.fill(buf, ch);
		return new String(buf);
	}

	/** Replace the first occurrence of the key in the string by the value.
	 * @param source string source.
	 * @param key string to be replaced.
	 * @param value string which replaces key occurrence.
	 * @return modified string.
	 */
	public static final String modifyFirst(final String source,
		final String key,
		final String value) {
		int ndx, keylen;
		if (source == null ||
			(keylen = key.length()) == 0 ||
			(ndx = source.indexOf(key)) < 0) {
			return source;
		}
		return ndx == 0 ?
			value + source.substring(keylen) :
			source.substring(0,ndx) + value + source.substring(ndx + keylen);
	}

	/** Replace all occurrences of the key in the source by the value.
	 * @param source string source.
	 * @param key string to be replaced.
	 * @param rep string which replaces all key occurrences in the source.
	 * @return modified string.
	 */
	public static final String modifyString(final String source,
		final String key,
		final String rep) {
		int pos, keylen, strlen;
		if (source == null || (strlen = source.length()) == 0 ||
			(keylen = key.length()) == 0 ||
			(pos = source.indexOf(key)) < 0) {
			return source;
		}
		StringBuilder result = pos == 0 ? new StringBuilder(rep) :
			new StringBuilder(source.substring(0, pos)).append(rep);
		int lastpos = pos += keylen;
		while (lastpos < strlen && (pos = source.indexOf(key, lastpos)) > 0) {
			result.append(source.substring(lastpos, pos)).append(rep);
			lastpos = pos += keylen;
		}
		if (lastpos < strlen) {
			result.append(source.substring(lastpos));
		}
		return result.toString();
	}

	/** Replace all occurrences of the argument "key" in the StringBuffer
	 * by value from the argument "rep".
	 * @param source StringBuffer with source data.
	 * @param key string to be replaced.
	 * @param rep string which replaces all key occurrences in the source.
	 */
	public static final void modifyStringBuffer(final StringBuffer source,
		final String key,
		final String rep) {
		int keylen;
		if ((keylen = key.length()) == 0) {
			return;
		}
		int replen = rep.length();
		int ndx = source.indexOf(key); //java 1.4 and higher
		if (ndx < 0) {
			return;
		}
		do {
			source.replace(ndx, ndx + keylen, rep);
		} while ((ndx = source.indexOf(key, ndx + replen)) >= 0); //java 1.4, ...
	}

	/** Replace all occurrences of the argument "key" in the StringBuilder
	 * by value from the argument "rep".
	 * @param source StringBuilder with source data.
	 * @param key string to be replaced.
	 * @param rep string which replaces all key occurrences in the source.
	 */
	public static final void modifyStringBuilder(final StringBuilder source,
		final String key,
		final String rep) {
		int keylen;
		if ((keylen = key.length()) == 0) {
			return;
		}
		int replen = rep.length();
		int ndx = source.indexOf(key); //java 1.4 and higher
		if (ndx < 0) {
			return;
		}
		do {
			source.replace(ndx, ndx + keylen, rep);
		} while ((ndx = source.indexOf(key, ndx + replen)) >= 0);
	}

	/**	Replace in the string from argument s all occurrences of characters in
	 * the argument p by the character on the same position in the argument q.
	 * If no character is on the position of q then this character is removed.
	 * <p>Examples:</p>
	 * <p>translate(“bcr”,“abc”,”ABa”) returns “Bar”.</p>
	 * <p>translate("-abc-","ab-","BA") returns "BAc".</p>
	 * @param s source string.
	 * @param p string with characters to be replaced.
	 * @param q string with characters which will be in the result.
	 * @return translated string.
	 */
	public static final String translate(final String s,
		final String p,
		final String q) {
		if (s == null) {
			return null;
		}
		int k = q.length();
		StringBuilder sb = new StringBuilder(s);
		for (int i = sb.length() - 1; i >= 0; i--) {
			int j;
			if ((j = p.indexOf(sb.charAt(i))) >= 0) {//found in p
				if (j >= k) {//not in q, => delete
					sb.deleteCharAt(i);
				} else {//replace with q[j]
					sb.setCharAt(i, q.charAt(j));
				}
			}
		}
		return sb.toString();
	}

	/** Return trimmed string from argument and replace there all multiple
	 * sequences of white spaces by one regular space character.
	 * @param value input string.
	 * @return trimmed string with multiple white spaces replaced by the
	 * space character.
	 */
	public static final String trimAndRemoveMultipleWhiteSpaces(
		final String value) {
		StringBuilder sb = new StringBuilder(value.trim());
		int len = sb.length();
		for (int i = 0; i < len; i++) {
			char c;
			// \t\n\r\f - space, tab, carriage-return, form-feed character
			if ((c = sb.charAt(i)) <= ' ' && (c == ' ' || c == '\r' ||
				c == '\n' || c == '\t' || c == '\f')) {
				int j = i;
				while (++j < len && ((c = sb.charAt(j)) <= ' ' && (c == ' ' ||
					c == '\r' || c == '\n' || c == '\t' || c == '\f'))) {}
				if (j > i + 1) {
					sb.replace(i, j, " ");
					len -= j - i - 1;
				} else if (c != ' ') {
					sb.setCharAt(i, ' ');
				}
			}
		}
		return sb.toString();
	}

	/** Get user name.
	 * @return The user name.
	 */
	public static final String getUserName() {
		return System.getProperties().getProperty("user.name");
	}

	/** Get user country setting id.
	 * @return The country id.
	 */
	public static final String getCountry() {
		return System.getProperties().getProperty("user.country");
	}

	/** Check if given argument is supported country code.
	 * @param country 2-letter country code defined in ISO 3166.
	 * @return <tt>true</tt> if  country code is supported.
	 */
	public static final boolean isCountryCode(final String country) {
		for (String x: Locale.getISOCountries()) {
			if (x.equals(country)) {
				return true;
			}
		}
		return false;
	}

	/** Get user language id.
	 * @return The language id.
	 */
	public static final String getLanguage() {
		return System.getProperties().getProperty("user.language");
	}

	/** get ISO 639-2 (3 letters) user language ID.
	 * @return ISO 639-2 (3 letters) language ID.
	 * @throws SRuntimeException code SYS018 if language code is not found.
	 */
	public static final String getISO3Language() throws SRuntimeException {
		return getISO3Language(getLanguage());
	}

	/** Get ISO 639-2 (3 letters) language ID.
	 * @param language The language code (ISO 639 2 letters) or
	 * (ISO 639-2 3 letters).
	 * @return the ISO 639-2 language ID (three letters).
	 * @throws SRuntimeException code SYS018 if language code is not found.
	 */
	public static final String getISO3Language(final String language)
	throws SRuntimeException {
		String result;
		if ((result = LANGUAGES.get(language)) != null) {
			return result;
		}
		if (language.length() == 3) {
			for (Locale x: Locale.getAvailableLocales()) {
				if (language.equals(x.getISO3Language())) {
					result = x.getISO3Language();
					LANGUAGES.put(language, result);
					return result;
				}
			}
		} else {
			try {
				result = new Locale(language,"").getISO3Language();
				if (result != null && result.length() == 3) {
					LANGUAGES.put(language, result);
					LANGUAGES.put(result, result);
					return result;
				}
			} catch (Exception ex) {}
		}
		//Unsupported language code: &{0}
		throw new SRuntimeException(SYS.SYS018, language);
	}

	/** Check if a class implements given interface.
	 * @param clazz the class to be checked.
	 * @param interfaceName the qualified name of interface (including package
	 * specification).
	 * @return true if the class implements interface from argument.
	 */
	public static final boolean implementsInterface(final Class<?> clazz,
		String interfaceName) {
		for (Class<?> x: clazz.getInterfaces()) {
			if (x.getName().equals(interfaceName)) {
				return true;
			}
			if (implementsInterface(x, interfaceName)) {
				return true;
			}
		}
		return false;
	}

	/** Check if a class implements given interface.
	 * @param clazz the class to be checked.
	 * @param interfaceClass the class with an interface.
	 * @return true if the class implements the interface from argument.
	 */
	public static final boolean implementsInterface(final Class<?> clazz,
		Class<?> interfaceClass) {
		for (Class<?> x: clazz.getInterfaces()) {
			if (x.getName().equals(interfaceClass.getName())) {
				return true;
			}
			if (implementsInterface(x, interfaceClass)) {
				return true;
			}
		}
		return false;
	}

	/** This is the auxiliary thread for piping of output streams of method
	 * execute (stdout, stderr).*/
	private static final class PipedOutStream extends Thread {
		private final BufferedReader _in;
		private final PrintStream _out;
		PipedOutStream(InputStream in, PrintStream out, boolean wait) {
			_in = new BufferedReader(new InputStreamReader(in));
			_out = out;
			if (!wait) {
				setDaemon(true);
			}
		}
		@Override
		public final void run() {
			try {
				if (_out != null) {// user stream is specified
					int i;
					while ((i = _in.read()) >= 0) {
						_out.print((char) i);
					}
				}
				_in.close();
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	/** Executes the specified command and arguments in a separate process with
	 * the specified environment and working directory.
	 * If there is a security manager, its checkExec method is called with the
	 * first component of the array cmdArray as its argument. This may result
	 * in a security exception.
	 * Given an array of strings cmdArray, representing the tokens of a command
	 * line, and an array of strings envVars, representing "environment"
	 * variable settings, this method creates a new process in which to execute
	 * the specified command.
	 * If envVars is null, the subprocess inherits the environment settings of
	 * the current process.
	 * The working directory of the new subprocess is specified by actDir. If
	 * actDir is null, the subprocess inherits the current working directory
	 * of the current process.
	 * If out is null the output stream is printed to System.out, otherwise it
	 * is written to given stream out.
	 * If err is null the output stream is printed to System.err, otherwise it
	 * is written to given stream err.
	 * If stdIn is null the input stream passed to program is empty, otherwise
	 * it is passed from the stream from the parameter.
	 * If waitFlag is <tt>true</tt> the current thread waits until the executed
	 * process has been terminated.
	 * @param cmdArray array containing the command to call and its arguments.
	 * @param envVars array of strings, each element of which has environment
	 * variable settings in format name=value.
	 * @param actDir the working directory of the subprocess, or null if the
	 * subprocess should inherit the working directory of the current process.
	 * @param stdOut output stream for standard output stream or <tt>null</tt>.
	 * @param stdErr output stream for standard error stream or <tt>null</tt>.
	 * @param stdIn input stream with standard input data or <tt>null</tt>.
	 * @param waitFlag if <tt>true</tt> the current thread waits until
	 * the executed process has been terminated.
	 * @return a <tt>Process</tt> object for managing the executed subprocess.
	 * @throws Exception if an error occurs.
	 */
	public static final Process execute(final String [] cmdArray,
		final String [] envVars,
		final File actDir,
		final PrintStream stdOut,
		final PrintStream stdErr,
		final InputStream stdIn,
		final boolean waitFlag) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(cmdArray, envVars, actDir);
		new PipedOutStream(process.getErrorStream(), stdErr, waitFlag).start();
		new PipedOutStream(process.getInputStream(), stdOut, waitFlag).start();
		OutputStream os = process.getOutputStream();  //piped input
		if (stdIn != null) {//system input is specified
			int i;
			while ((i = stdIn.read()) >= 0) {
				os.write(i);
			}
		}
		os.close();
		if (waitFlag) {
			process.waitFor();
		}
		runtime.gc();
		return process;
	}

	/** Executes a separate process with arguments.
	 * @param cmdarray array of strings with command line arguments.
	 * @return a <tt>Process</tt> object for managing the executed subprocess.
	 * @throws Exception if an error occurs.
	 */
	public static final Process execute(final String... cmdarray)
		throws Exception {
		return execute(cmdarray,
			null, null, System.out, System.err, null, true);
	}
}