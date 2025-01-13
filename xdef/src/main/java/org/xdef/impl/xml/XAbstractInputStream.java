package org.xdef.impl.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Abstract class which enables to create Reader from InputStream.
 * @author Vaclav Trojan
 */
public class XAbstractInputStream extends InputStream {
	private final InputStream _in;
	private int _hdrIndex;
	private int _len;
	private byte[] _buf;

	/** Create new instance of XAbstractInputStream.
	 * @param in Input stream with data.
	 * @throws IOException if an error occurs.
	 */
	public XAbstractInputStream(final InputStream in) throws IOException {_in = in; _len = -1;}

	public final void setHdrIndex(final int hdrIndex) {_hdrIndex = hdrIndex;}
	public final InputStream getInputStream() {return _len == 0 ? _in : this;}
	public final byte[] getparsedBytes() {return _buf;}
	public final void setBuffer(final byte[] buf) {
		_buf = buf;
		_len = _buf == null ? -1 : _buf.length;
	}
////////////////////////////////////////////////////////////////////////////////
	/** Detect encoding from Byte Order Mark (BOM)
	 * (see http://www.w3.org/TR/REC-xml/#charsets).
	 * <UL>
	 * <li>With a Byte Order Mark (BOM):
	 * <p>EF BB BF: UTF-8
	 * <p>00 00 FE FF: UCS-4, big-endian machine (1234 order)
	 * <p>FF FE 00 00: UCS-4, little-endian machine (4321 order)
	 * <p>00 00 FF FE: UCS-4, unusual octet order (2143)
	 * <p>FE FF 00 00: UCS-4, unusual octet order (3412)
	 * <p>FE FF ## ##: UTF-16, big-endian
	 * <p>FF FE ## ##: UTF-16, little-endian
	 * <li><p>Without a Byte Order Mark:
	 * <b>00 00 00 3C, 3C 00 00 00, 00 00 3C 00, 00 3C 00 00:</b>
	 * <p>UCS-4 or other encoding with a 32-bit code unit and ASCII characters encoded as ASCII values,
	 * in respectively big-endian(1234), little-endian(4321) and two unusual byte orders (2143 and 3412).
	 * The encoding declaration must be read to determine which of UCS-4 or other supported 32-bit encodings
	 * applies.
	 * <b>00 3C 00 3F</b>
	 * <p>UTF-16BE or big-endian ISO-10646-UCS-2 or other encoding with a 16-bit code unit in big-endian order
	 * and ASCII characters encoded as ASCII values (the encoding declaration must be read to determine which)
	 * <b>3C 00 3F 00:</b>
	 * <p>UTF-16LE or little-endian ISO-10646-UCS-2 or other encoding with a 16-bit code unit in little-endian
	 * order and ASCII characters encoded as ASCII values (the encoding declaration must be read to determine
	 * which)
	 * <b>3C 3F 78 6D:</b>
	 * <p>UTF-8, ISO 646, ASCII, some part of ISO 8859, Shift-JIS, EUC, or any other 7-bit, 8-bit, or
	 * mixed-width encoding which ensures that the characters of ASCII have their normal positions, width, and
	 * values; the actual encoding declaration must be read to detect which of these applies, but since all
	 * of these encodings use the same bit patterns for the relevant ASCII characters, the encoding
	 * declaration itself may be read reliably
	 * <b>4C 6F A7 94:</b>
	 * <p>EBCDIC (in some flavor; the full encoding declaration must be read
	 * to tell which code page is in use)
	 * <b>Other:</b>
	 * <p>UTF-8 without an encoding declaration, or else the data stream is mislabeled (lacking a required
	 * encoding declaration), corrupt, fragmentary, or enclosed in a wrapper of some kind.
	 * </UL>
	 * @param in InputStream where to read.
	 * @param buf array of four bytes to which first bytes are read.
	 * @return character set name. First two characters have a special mesaging. The first character is number
	 * of read bytes and the second character is the number of bytes to read next character.
	 * @throws IOException if an IO error occurs.
	 */
	public static final String detectBOM(final InputStream in, final byte[] buf) throws IOException {
		int i1, i2, i3, i4;
		buf[0] = (byte) (i1 = in.read());
		if (i1 == -1) {
			return ((char)('0' - 1)) + "0UTF-8"; // first character - '0' = -1!
		}
		buf[1] = (byte) (i2 = in.read());
		if (i2 == -1) {
			return "11UTF-8"; // only one byte in the input stream -> 1
		}
		buf[2] = (byte) (i3 = in.read());
		if (i3 == -1) {
			return  (i1 == 0xFE && i2 == 0xFF) ? "02UTF-16BE" //BOM
				: (i1 == 0xFF && i2 == 0xFE) ?   "02UTF-16LE" //BOM
				: "22UTF-8";
		}
		if (i1 == 0xEF && i2 == 0xBB && i3 == 0xBF) {
			return "01UTF-8"; //BOM UTF-8 (3 bytes)
		}
		buf[3] = (byte) (i4 = in.read());
		if (i4 == -1) {
			return "31UTF-8";
		} else if (i1 == 0xFF && i2 == 0xFE) { // BOM
			if (i3 == 0 && i4 == 0) { // FF FE 00 00
				return "04UTF-32LE"; // BOM 4321, 4 bytes
			}
			buf[0] = buf[2]; // FF FE ## ## => UTF-16LE and skip two bytes
			buf[1] = buf[3];
			return "22UTF-16LE"; // BOM + '<'
		} else if (i1 == 0xFE && i2 == 0xFF) { // BOM
			if (i3 == 0 && i4 == 0) { // FE FF 00 00
				return "04X-ISO-10646-UCS-4-3412"; //BOM 3412
			}
			buf[0] = buf[2]; // FE FF ## ## => UTF-16BE and skip two bytes
			buf[1] = buf[3];
			return "22UTF-16BE"; // BOM + '<'
		} else if (i1 == 0 && i2 == 0) {
			return (i3 == 0xFE && i4 == 0xFF) ? "04UTF-32BE"  //BOM 1234
				: (i3 == 0xFF && i4 == 0xFE) ? "04X-ISO-10646-UCS-4-2143" //BOM
				: (i3 != 0 && i4 == 0) ? "44X-ISO-10646-UCS-4-2143" //00<0
				: (i3 == 0 && i4 != 0) ? "44UTF-32BE" // 000<
				: "41UTF-8"; // other
		} else if (i1 == 0 && i2 != 0) {
			return (i3 == 0 && i4 == 0) ? "44X-ISO-10646-UCS-4-3412" /*0<00*/ : "42UTF-16BE";
		} else if (i1 != 0 && i2 == 0) {
			return (i3 == 0 && i4 == 0) ? "44UTF-32LE" : "42UTF-16LE";
		} else if (i1 == 0x4C && i2 == 0x6F && i3 == 0xA7 && i4 == 0x94) {
			return "41CP037"; // EBCDIC ("<?xm")
		}
		return "41UTF-8"; // other
	}

	/** Convert bytes from buffer to string.
	 * @param buf array of bytes to be converted.
	 * @param off offset where to start converting.
	 * @param len number of bytes to be converted.
	 * @param encoding character set name.
	 * @return converted string.
	 * @throws IOException if an error occurs.
	 */
	public static final String bytesToString(final byte[] buf, final int off, final int len, String encoding)
		throws IOException {
		if (len == 0) {
			return "";
		}
		if ("X-ISO-10646-UCS-4-2143".equals(encoding) || "X-ISO-10646-UCS-4-3412".equals(encoding)) {
			byte[] b = new byte[len];
			for (int i = 0; i < len / 4; i += 4) {
				if (encoding.endsWith("2143")) {
					b[i + 1] = buf[i + off + 0];
					b[i + 0] = buf[i + off + 1];
					b[i + 3] = buf[i + off + 2];
					b[i + 2] = buf[i + off + 3];
				} else {
					b[i + 2] = buf[i + off + 0];
					b[i + 3] = buf[i + off + 1];
					b[i + 0] = buf[i + off + 2];
					b[i + 1] = buf[i + off + 3];
				}
			}
			return new String(b, "UTF32");
		} else {
			return new String(buf, off, len, encoding);
		}
	}

	/** Read next character from input stream.
	 * @param in input stream.
	 * @param encoding character encoding.
	 * @param buf working buffer (4 bytes)
	 * @param count number of bytes to read from the input stream.
	 * @param baos ByteArrayOutputStream where are written bytes read.
	 * @return decoded character.
	 * @throws IOException if an error occurs.
	 */
	public static final int readChar(final InputStream in,
		final String encoding,
		final byte[] buf,
		final int count,
		final ByteArrayOutputStream baos) throws IOException {
		int len = in.read(buf, 0, count);
		if (len == -1) {
			return -1;
		}
		while (len < count) {
			int i = in.read(buf, len, count - len);
			if (i == -1) {
				break;
			}
			len += i;
		}
		baos.write(buf, 0, len);
		if (len < count) {
			return -1; // error, premature EOF
		}
		return bytesToString(buf, 0, count, encoding).charAt(0);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of necessary InputStream methods
////////////////////////////////////////////////////////////////////////////////

	@Override
	public final int read() throws IOException {
		if (_hdrIndex <= _len) {
			if (_hdrIndex < _len) {
				return _buf[_hdrIndex++];
			}
			_buf = null;
		}
		return _in.read();
	}

	@Override
	public final int read(final byte[] b) throws IOException {
		if (_hdrIndex <= _len) {
			int i = 0;
			for (; i < b.length && _hdrIndex < _len; i++) {
				b[i] = _buf[_hdrIndex++];
			}
			if (_hdrIndex == _len) {
				_buf = null;
			}
			if (i > 0) {
				return i;
			}
		}
		return _in.read(b);
	}

	@Override
	public final int read(final byte[] b, final int off, final int len) throws IOException {
		if (_hdrIndex <= _len) {
			int i = off;
			for (; i < b.length && i < off + len && _hdrIndex < _len; i++){
				b[i] = _buf[_hdrIndex++];
			}
			if (_hdrIndex == _len) {
				_buf = null;
			}
			if (i > off) {
				return i - off;
			}
		}
		return _in.read(b, off, len);
	}

	@Override
	public final void close() throws IOException {
		if (_in != null) {
			_in.close();
		}
		_buf = null;
		_hdrIndex = _len = 0;
	}
}
