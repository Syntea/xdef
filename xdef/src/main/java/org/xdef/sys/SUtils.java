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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
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
	/** String with the last part of Java VM version information.
	 * E.g. if version information is "1.6.0_45" it will be "0_45".
	 */
	public static final String JAVA_RUNTIME_BUILD;

	/** Length of line of encoded hex format and base64 format. */
	private static final int ENCODED_LINE_LENGTH = 72;
	/** ength of input buffer in the line of hexadecimal encoded data. */
	private static final int INPUT_HEXBUFFER_LENGTH = ENCODED_LINE_LENGTH/2;
	/** Hexadecimal digits. */
	private static final byte[] HEXDIGITS = new byte[] {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/** Cache to accelerate getISO3Language methods.*/
	private static final Map<String, String> LANGUAGES =
		new HashMap<String, String>();
	/**  Cache to accelerate getISO3Country methods.*/
	private static final Map<String, Locale> COUNTRYMAP =
		new HashMap<String, Locale>();

////////////////////////////////////////////////////////////////////////////////
// initialize static variables
////////////////////////////////////////////////////////////////////////////////
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
		if (s.startsWith("9.")) {
			s = "1." + s;
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

////////////////////////////////////////////////////////////////////////////////
// enconding/decoding of hexadecimal format
////////////////////////////////////////////////////////////////////////////////

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
	 * @return byte array with the encoded data to hexadecimal digits.
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

	/** Encodes binary input byte stream <b>in</b> to the output text writer
	 * <b>out</b> as a stream of hexadecimal digits.
	 * @param in InputStream with binary origin bytes.
	 * @param out OutputStream where hexadecimal digits are written.
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeHex(final InputStream in,
		final OutputStream out) throws SException {
		final byte[] ibuf = new byte[INPUT_HEXBUFFER_LENGTH];
		final byte[] obuf;
		obuf = new byte[ENCODED_LINE_LENGTH];
		int len;
		try {
			if ((len = in.read(ibuf)) == INPUT_HEXBUFFER_LENGTH) {
				for (;;) {
					encodeHex(obuf, 0, ibuf, 0, INPUT_HEXBUFFER_LENGTH);
					if ((len = in.read(ibuf)) > 0) {
						out.write(obuf);
						if (len < INPUT_HEXBUFFER_LENGTH) {
							break;
						}
					} else {
						out.write(obuf, 0, ENCODED_LINE_LENGTH);
						return;
					}
				}
			}
			if (len > 0) {
				len = encodeHex(obuf, 0, ibuf, 0, len);
				out.write(obuf, 0, len);
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Encodes binary input byte stream <b>in</b> to the output writer
	 * <b>out</b> as a stream of hexadecimal digits.
	 * @param in InputStream with binary origin bytes.
	 * @param out Writer where hexadecimal digits are written.
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeHex(final InputStream in,
		final Writer out) throws SException {
		final byte[] ibuf = new byte[INPUT_HEXBUFFER_LENGTH];
		final char[] obuf;
		obuf = new char[ENCODED_LINE_LENGTH];
		int len;
		try {
			if ((len = in.read(ibuf)) == INPUT_HEXBUFFER_LENGTH) {
				for (;;) {
					encodeHex(obuf, 0, ibuf, 0, INPUT_HEXBUFFER_LENGTH);
					if ((len = in.read(ibuf)) > 0) {
						out.write(obuf);
						if (len < INPUT_HEXBUFFER_LENGTH) {
							break;
						}
					} else {
						out.write(obuf, 0, ENCODED_LINE_LENGTH);
						return;
					}
				}
			}
			if (len > 0) {
				len = encodeHex(obuf, 0, ibuf, 0, len);
				out.write(obuf, 0, len);
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes stream of hexadecimal format <b>in</b> to the stream of bytes.
	 * @param in InputStream with hexadecimal digits.
	 * @param out decoded byte stream.
	 * @throws SException SYS047 HEX format error.
	 */
	public static final void decodeHex(final InputStream in,
		final OutputStream out) throws SException {
		try {
			int i;
			while ((i = in.read())==' ' || i=='\n' || i=='\r' || i=='\t') {}
			while (i > 0) {
				int b = "0123456789abcdefABCDEF".indexOf(i);
				if (b < 0) {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				} else if (b > 15) {
					b -= 6;
				}
				i = "0123456789abcdefABCDEF".indexOf(in.read());
				if (i < 0) {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				} else if (i > 15) {
					i -= 6;
				}
				out.write((b << 4) + i);
				if ((i = in.read())==' ' || i=='\n' || i=='\r' || i=='\t') {
					break;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes stream of hexadecimal format <b>in</b> to the stream of bytes.
	 * @param in Reader with hexadecimal digits.
	 * @param out decoded byte stream.
	 * @throws SException SYS047 HEX format error.
	 */
	public static final void decodeHex(final Reader in,
		final OutputStream out) throws SException {
		try {
			int i;
			while ((i = in.read())==' ' || i=='\n' || i=='\r' || i=='\t') {}
			while (i > 0) {
				if (i == ' ' || i == '\n' || i == '\r' || i == '\t') {
					continue; //skip white spaces
				}
				int b = "0123456789abcdefABCDEF".indexOf(i);
				if (b < 0) {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				} else if (b > 15) {
					b -= 6;
				}
				i = "0123456789abcdefABCDEF".indexOf(in.read());
				if (i < 0) {
					throw new SException(SYS.SYS047); //Hexadecimal format error
				} else if (i > 15) {
					i -= 6;
				}
				out.write((b << 4) + i);
				if ((i = in.read())==' ' || i=='\n' || i=='\r' || i=='\t') {
					break;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes array of bytes with hexadecimal digits to the array of bytes.
	 * @param bytes byte array with hexadecimal data.
	 * @return byte array decoded from the source.
	 * @throws SException SYS047 HEX format error.
	 */
	public static final byte[] decodeHex(final byte[] bytes) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeHex(new ByteArrayInputStream(bytes), out);
		return out.toByteArray();
	}

	/** Decodes array of hexadedimal digits to the array of bytes.
	 * @param chars array with hexadecimal data.
	 * @return byte array decoded from the source.
	 * @throws SException SYS047 HEX format error.
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
	 * @throws SException SYS047 HEX format error.
	 */
	public static final byte[] decodeHex(final String src) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeHex(new StringReader(src), out);
		return out.toByteArray();
	}

////////////////////////////////////////////////////////////////////////////////
// enconding/decoding of base64 format
////////////////////////////////////////////////////////////////////////////////

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

	/** Encodes binary input byte stream <b>in</b> to the output stream
	 * <b>fo</b> in the form of MIME/BASE64.
	 * @param in InputStream with binary origin bytes.
	 * @param out OutputStream for encoded Base64 result.
	 * @param lines if true the output is break to lines (72 bytes).
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeBase64(final InputStream in,
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
			if ((len = in.read(ibuf)) == INPUT_B64BUFFER_LENGTH) {
				for (;;) {
					encodeBase64(obuf, 0, ibuf, 0, INPUT_B64BUFFER_LENGTH);
					if ((len = in.read(ibuf)) > 0) {
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

	/** Encodes binary input byte stream <b>in</b> to the output stream
	 * <b>fo</b> in the form of MIME/BASE64.
	 * @param in InputStream with binary origin bytes.
	 * @param out Writer for encoded Base64 resulting character stream.
	 * @param lines if true the output is broken into lines (72 bytes).
	 * @throws SException when I/O error occurs.
	 */
	public static final void encodeBase64(final InputStream in,
		final Writer out,
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
			if ((len = in.read(ibuf)) == INPUT_B64BUFFER_LENGTH) {
				for (;;) {
					encodeBase64(obuf, 0, ibuf, 0, INPUT_B64BUFFER_LENGTH);
					if ((len = in.read(ibuf)) > 0) {
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

	/** Encodes binary input byte array to the output array in the form
	 * of MIME/BASE64.
	 * @param bytes array of bytes to be encoded.
	 * @param lines if true the output is broken into lines (72 bytes).
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
	 * @param lines if true the output is break to lines (72 bytes).
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
	 * <li>decoding values 0..63
	 * <li>64 represents white blanks
	 * <li>65 padding character ('=')
	 * <li>66-70 incorrect characters
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

	/** Decode binary input MIME/BASE64 from SReader <b>ii</b> to output stream.
	 * @param in java.io.Reader with Base64 character stream.
	 * @param out Output stream for decoded byte stream.
	 * @throws SException
	 * <br>SYS036 .. Program exception: {msg}.
	 * <br>SYS048 .. Base64 format error.
	 */
	public static final void decodeBase64(final SReader in,
		final OutputStream out) throws SException {
		final int bufmax = 960; //must be multiple of 3!
		int bx = 0;
		byte[] buf = new byte[bufmax];
		byte b1,b2,b3,b4;
		try {
			for (;;) {
				//skip white blanks
				int i;
				while ((b1 = DECODE_BASE64[(i = in.read()) & 127]) == 64) {}
				if (b1 >= 65) {
					out.write(buf,0,bx);
					if (i >= 0 && b1 != 64) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					return;
				}
				//skip white blanks
				while ((b2 = DECODE_BASE64[(i = in.read()) & 127]) == 64) {}
				if (b2 >= 65) {
					out.write(buf,0,bx);
					if (b1 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					out.write(b1 << 2);
					return; //last 6 bites filled by 0 (error???)
				}
				buf[bx++] = (byte)((b1 << 2) + (b2 >> 4));
				//skip white blanks
				while ((b3 = DECODE_BASE64[(i = in.read()) & 127]) == 64) {}
				if (b3 >= 65) {
					out.write(buf,0,bx);
					if (b3 != 65 || i < 0) {
						throw new SException(SYS.SYS048); //Base64 format error
					}
					while (DECODE_BASE64[(i = in.read()) & 127] == 64) {}
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
				while ((b4 = DECODE_BASE64[(i = in.read()) & 127]) == 64) {}
				if (b4 >= 65) {
					out.write(buf,0,bx);
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
					out.write(buf);
					bx = 0;
				}
			}
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Decodes binary input MIME/BASE64 stream <b>in</b> to the output stream
	 * of bytes.
	 * @param in java.io.Reader with Base64 character stream.
	 * @param out Output stream for decoded byte stream.
	 * @throws SException
	 * <br>SYS036 .. Program exception: {msg}.
	 * <br>SYS048 .. Base64 format error.
	 */
	public static final void decodeBase64(final InputStream in,
		final OutputStream out) throws SException {
		decodeBase64(new SReader() {
			@Override
			public int read() throws IOException {return in.read();}}, out);
	}

	/** Decodes input MIME/BASE64 Reader <b>in</b> to output stream.
	 * @param in java.io.Reader with Base64 data.
		 * @param out Output stream for decoded byte stream.
	 * @throws SException
	 * <br>SYS036 .. Program exception {msg}.
	 * <br>SYS048 .. Base64 format error.
	 */
	public static final void decodeBase64(final Reader in,
		final OutputStream out) throws SException {
		decodeBase64(new SReader() {
			@Override
			public int read() throws IOException {return in.read();}}, out);
	}

	/** Decodes input MIME/BASE64 from SPaser <b>in</b> to the output stream.
	 * @param in SAParser with Base64 data.
	 * @param out Writer for decoded byte stream.
	 * @throws SException
	 * <br>SYS036 .. Program exception: {msg}.
	 * <br>SYS048 .. Base64 format error.
	 */
	public static final void decodeBase64(final SParser in,
		final OutputStream out) throws SException {
		decodeBase64(new SReader() {
			@Override
			public int read() {return in.eos() ? -1 : in.peekChar();}}, out);
	}

	/** Decodes input MIME/BASE64 from SPaser <b>in</b> to byte array.
	 * @param in SAParser with Base64 data.
	 * @return byte array decoded from source.
	 * @throws SException
	 * <br>SYS036 .. Program exception: {msg}.
	 * <br>SYS048 .. Base64 format error.
	 */
	public static final byte[] decodeBase64(final SParser in) throws SException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(in, out);
		return out.toByteArray();
	}

	/** Decodes input MIME/BASE64 from byte array <b>in</b> to byte array.
	 * @param in byte array with base64 data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeBase64(final byte[] in) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(new ByteArrayInputStream(in), out);
		return out.toByteArray();
	}

	/** Decodes input MIME/BASE64 from character array <b>in</b> to byte array.
	 * @param in char array with base64 encoded data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeBase64(final char[] in) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(new CharArrayReader(in), out);
		return out.toByteArray();
	}

	/** Decodes binary input MIME/BASE64 string <b>in</b> to byte array.
	 * @param in string with base64 encoded data.
	 * @return byte array decoded from source.
	 * @throws SException SYS048 Base64 format error.
	 */
	public static final byte[] decodeBase64(final String in) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decodeBase64(new StringReader(in), out);
		return out.toByteArray();
	}

////////////////////////////////////////////////////////////////////////////////
// String tools
////////////////////////////////////////////////////////////////////////////////

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
	 * @param replacement string which replaces the first key occurrence.
	 * @return modified string.
	 */
	public static final String modifyFirst(final String source,
		final String key,
		final String replacement) {
		int ndx, keylen;
		if (source == null ||
			(keylen = key.length()) == 0 ||
			(ndx = source.indexOf(key)) < 0) {
			return source;
		}
		return ndx == 0 ? replacement + source.substring(keylen)
			: source.substring(0,ndx)+replacement+source.substring(ndx+keylen);
	}

	/** Replace all occurrences of the key in the source by the value.
	 * @param source string source.
	 * @param key string to be replaced.
	 * @param replacement replaces all key occurrences in the source.
	 * @return modified string.
	 */
	public static final String modifyString(final String source,
		final String key,
		final String replacement) {
		int pos, keylen, strlen;
		if (source == null || (strlen = source.length()) == 0 ||
			(keylen = key.length()) == 0 ||
			(pos = source.indexOf(key)) < 0) {
			return source;
		}
		StringBuilder result = pos == 0 ? new StringBuilder(replacement) :
			new StringBuilder(source.substring(0, pos)).append(replacement);
		int lastpos = pos += keylen;
		while (lastpos < strlen && (pos = source.indexOf(key, lastpos)) > 0) {
			result.append(source.substring(lastpos, pos)).append(replacement);
			lastpos = pos += keylen;
		}
		if (lastpos < strlen) {
			result.append(source.substring(lastpos));
		}
		return result.toString();
	}

	/** Replace all occurrences of the argument "key" in the StringBuffer
	 * by value from the argument "rep".
	 * @param s StringBuffer with source data.
	 * @param key string to be replaced.
	 * @param replacement replaces all key occurrences in the source.
	 */
	public static final void modifyStringBuffer(final StringBuffer s,
		final String key,
		final String replacement) {
		int keylen;
		if ((keylen = key.length()) == 0) {
			return;
		}
		int replen = replacement.length();
		int ndx = s.indexOf(key); //java 1.4 and higher
		if (ndx < 0) {
			return;
		}
		do {
			s.replace(ndx, ndx + keylen, replacement);
		} while ((ndx = s.indexOf(key, ndx + replen)) >= 0); //java 1.4, ...
	}

	/** Replace all occurrences of the argument "key" in the StringBuilder "s"
	 * by value from the argument "rep".
	 * @param s StringBuilder with source data.
	 * @param key string to be replaced.
	 * @param replacement replaces all key occurrences in the source.
	 */
	public static final void modifyStringBuilder(final StringBuilder s,
		final String key,
		final String replacement) {
		int keylen;
		if ((keylen = key.length()) == 0) {
			return;
		}
		int replen = replacement.length();
		int ndx = s.indexOf(key); //java 1.4 and higher
		if (ndx < 0) {
			return;
		}
		do {
			s.replace(ndx, ndx + keylen, replacement);
		} while ((ndx = s.indexOf(key, ndx + replen)) >= 0);
	}

	/**	Replace in the string from argument s all occurrences of characters in
	 * the argument p by the character on the same position in the argument q.
	 * If no character is on the position of q then this character is removed.
	 * <p>Examples:
	 * <br>translate(“bcr”,“abc”,”ABa”) returns “Bar”.
	 * <br>translate("-abc-","ab-","BA") returns "BAc".
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

////////////////////////////////////////////////////////////////////////////////
// Localization
////////////////////////////////////////////////////////////////////////////////
	/** Get user country setting id.
	 * @return The country id.
	 */
	public static final String getCountry() {
		return System.getProperties().getProperty("user.country");
	}

	/** Check if given argument is supported country code.
	 * @param country 2-letter country code defined in ISO 3166.
	 * @return true if  country code is supported.
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

	/** Get ISO 639-2 (3 letters) user language ID.
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

	/** Get ISO 3166-1 alpha-2 country code.
	 * @param code The country code ISO 3166-1 alpha-2 or
	 * ISO 3166-1 alpha-2-3 or display name.
	 * @return ISO 3166-1 alpha-2 code (two letters).
	 * @throws SRuntimeException code SYS018 if language code is not found.
	 */
	public final static String getISO2Country(final String code) {
		String s = code.toUpperCase();
		if (s.length() == 3) {
			Locale loc = COUNTRYMAP.get(s);
			if (loc != null) {
				return loc.getCountry();
			}
		}
		try {
			for (String country : Locale.getISOCountries()) {
				Locale loc = new Locale("", country);
				if (s.equals(loc.getCountry()) || s.equals(loc.getISO3Country())
					|| s.equals(loc.getDisplayCountry().toUpperCase())) {
					COUNTRYMAP.put(loc.getISO3Country(), loc);
					return country;
				}
			}
		} catch (Exception ex) {}
		if ("CZECHIA".equals(s)) {  // "Czechia not registered yet"
			return "CZ";
		}
		//Unsupported country code: &{0}
		throw new SRuntimeException(SYS.SYS017, code);
	}

	/** Get ISO 3166-1 alpha-3 country code.
	 * @param code The country code ISO 3166-1 alpha-2 or
	 * ISO 3166-1 alpha-3 or display name.
	 * @return ISO 3166-1 alpha-3 code (three letters).
	 * @throws SRuntimeException code SYS018 if language code is not found.
	 */
	public final static String getISO3Country(final String code) {
		String s = code.toUpperCase();
		try {
			Locale loc = new Locale("", s);
			String t = loc.getISO3Country();
			if (t != null) {
				if (!COUNTRYMAP.containsKey(t)) {
					COUNTRYMAP.put(loc.getISO3Country(), loc);
				}
				return t;
			}
		} catch (Exception ex) {}
		try {
			for (String country : Locale.getISOCountries()) {
				Locale loc = new Locale("", country);
				if (s.equals(country) || s.equals(loc.getISO3Country())
					|| s.equals(loc.getDisplayCountry().toUpperCase())) {
					s = loc.getISO3Country();
					if (s != null) {
						COUNTRYMAP.put(s, loc);
					}
					return loc.getISO3Country();
				}
			}
		} catch (Exception ex) {}
		if ("CZECHIA".equals(s)) {  // "Czechia not registered yet"
			return "CZE";
		}
		//Unsupported country code: &{0}
		throw new SRuntimeException(SYS.SYS017, code);
	}

////////////////////////////////////////////////////////////////////////////////
// Access objects from a class.
////////////////////////////////////////////////////////////////////////////////

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

	/** Create new instance of object.
	 * @param className name of class.
	 * @param pars Object where is the filed.
	 * @return new instance of object.
	 * @throws SRuntimeException if the constructor was not found in the class.
	 */
	public final static Object getNewInstance(String className, Object... pars){
		try {
			Class<?> cls = Class.forName(className);
			Class<?>[] paramTypes = new Class<?>[pars.length];
			for (int i = 0; i < pars.length; i++) {
				paramTypes[i] = pars[i].getClass();
			}
			Constructor<?> constructor = cls.getConstructor(paramTypes);
			constructor.setAccessible(true);
			return constructor.newInstance(pars);
		} catch (Exception ex) {
			//Constructor in the class &{0} not found
			throw new SRuntimeException(SYS.SYS101, className);
		}
	}

	/** Get value of the field of the class of an object.
	 * @param className name of class.
	 * @param name name of filed.
	 * @return value of field.
	 * @throws SRuntimeException if the class or field was not found in
	 * given class.
	 */
	public final static Object getObjectField(String className, String name) {
		Class<?> cls;
		try {
			cls = Class.forName(className);
		} catch (Exception ex) {
			//Class &{0} not found
			throw new SRuntimeException(SYS.SYS102, className);
		}
		for (;;) {
			try {
				Field f = cls.getDeclaredField(name);
				f.setAccessible(true);
				return f.get(null); //static
			} catch (Exception ex) {
				cls = cls.getSuperclass();
				if (cls == null) {
					break;
				}
			}
		}
		//Field &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS103, name, className);
	}

	/** Get value of the field of the class of an object.
	 * @param o Object where is the filed.
	 * @param name name of filed.
	 * @return value of field.
	 * @throws SRuntimeException if the field was not found in the object's
	 * class.
	 */
	public final static Object getObjectField(Object o, String name) {
		Class<?> cls = o.getClass();
		for (;;) {
			try {
				Field f = cls.getDeclaredField(name);
				f.setAccessible(true);
				try {
					return f.get(o);
				} catch (Exception ex) {
					return f.get(null); //static
				}
			} catch (Exception ex) {
				cls = cls.getSuperclass();
				if (cls == null) {
					break;
				}
			}
		}
		//Field &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS103, name, cls.getName());
	}

	/** Set to the field of the class of an object.
	 * @param o Object where is the filed.
	 * @param name name of filed.
	 * @param v the value to be set.
	 * @throws SRuntimeException if the field was not found in the object's
	 * class or it is not accessible.
	 */
	public final static void setObjectField(Object o, String name, Object v) {
		Class<?> cls = o.getClass();
		for (;;) {
			try {
				Field f = cls.getDeclaredField(name);
				f.setAccessible(true);
				try {
					f.set(o, v);
					return;
				} catch (Exception ex) {
					f.set(null, v); // static
					return;
				}
			} catch (Exception ex) {
				cls = cls.getSuperclass();
				if (cls == null) {
					break;
				}
			}
		}
		//Field &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS103, name, cls.getName());
	}

	/** Invoke a getter on the object.
	 * @param o object where is getter.
	 * @param name name of setter.
	 * @return value of getter.
	 * @throws SRuntimeException if the getter was not found.
	 */
	public final static Object getValueFromGetter(Object o, String name) {
		Class<?> cls = o.getClass();
		for (;;) {
			try {
				Method m = cls.getDeclaredMethod(name);
				m.setAccessible(true);
				try {
					return m.invoke(o);
				} catch (Exception ex) {
					return m.invoke(null); //static
				}
			} catch (Exception ex) {
				cls = cls.getSuperclass();
				if (cls == null) {
					break;
				}
			}
		}
		//Getter &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS104, name, cls.getName());
	}

	/** Invoke a setter on the object.
	 * @param o the object where is setter.
	 * @param name name of setter.
	 * @param v value to be set.
	 * @throws SRuntimeException if the setter was not found or it is not
	 * accessible.
	 */
	public final static void setValueToSetter(Object o, String name, Object v) {
		Class<?> cls = o.getClass();
		for (;;) {
			for (Method m: cls.getDeclaredMethods()) {
				Class<?>[] params = m.getParameterTypes();
				if (name.equals(m.getName()) && params!=null && params.length==1) {
					try {
						m.setAccessible(true);
						try {
							m.invoke(o, v);
							return;
						} catch (Exception ex) {
							m.invoke(null, v); // static
							return;
						}
					} catch (Exception ex) {}
				}
			}
			cls = cls.getSuperclass();
			if (cls == null) {
				break;
			}
		}
		//SYS105=Setter &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS105, name, cls.getName());
	}

////////////////////////////////////////////////////////////////////////////////
// Execute a process.
////////////////////////////////////////////////////////////////////////////////

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
				throw new RuntimeException(ex);
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
	 * If waitFlag is true the current thread waits until the executed
	 * process has been terminated.
	 * @param command array containing a command and its arguments.
	 * @param envVars array of strings, each element of which has environment
	 * variable settings in format name=value.
	 * @param actDir the working directory of the subprocess, or null if the
	 * subprocess should inherit the working directory of the current process.
	 * @param stdOut output stream for standard output stream or null.
	 * @param stdErr output stream for standard error stream or null.
	 * @param stdIn input stream with standard input data or null.
	 * @param waitFlag if true the current thread waits until
	 * the executed process has been terminated.
	 * @return Process object for managing the executed subprocess.
	 * @throws Exception if an error occurs.
	 */
	public static final Process execute(final String [] command,
		final String [] envVars,
		final File actDir,
		final PrintStream stdOut,
		final PrintStream stdErr,
		final InputStream stdIn,
		final boolean waitFlag) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command, envVars, actDir);
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
	 * @param command array containing a command and its arguments.
	 * @return Process object for managing the executed subprocess.
	 * @throws Exception if an error occurs.
	 */
	public static final Process execute(final String... command)
		throws Exception {
		return execute(command, null, null, System.out, System.err, null, true);
	}
}