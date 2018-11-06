package org.xdef.impl.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Input stream used in SAX parser.
 * @author Vaclav Trojan
 */
public class XInputStream extends InputStream {
	private final InputStream _in;
	private int _hdrIndex;
	private int _len;
	private final String _encoding;
	private final String _version;
	private final boolean _standalone;
	private byte[] _buf;

	public XInputStream(final InputStream in, final String encoding)
		throws IOException {
		_in = in;
		_len = -1;
		_hdrIndex = 0;
		_buf = null;
		_encoding = encoding == null ? "UTF-8" : encoding;
		_standalone = false;
		_version = null;
	}

	public XInputStream(final InputStream in) throws IOException {
		_in = in;
		byte[] buf = new byte[4];
		String encoding = detectEncoding(_in, buf);
		_len = encoding.charAt(0) - '0'; // number of bytes read
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (_len > 0) {
			baos.write(buf, 0, _len);
		}
		int count = encoding.charAt(1) - '0'; // bytes nead to read next
		encoding = encoding.substring(2);
		String s = "";
		if (count > 0 && !"X-ISO-10646-UCS-4-2143".equals(encoding)
			&& !"X-ISO-10646-UCS-4-3412".equals(encoding)) {
			s = bytesToString(buf, 0, _len, encoding);
			int i;
			while (s.length() < 2
				&& (i = nextChar(_in, encoding, buf, count, baos)) != -1) {
				s += (char) i;
			}
			if (s.startsWith("<?")) {
				while (s.length() < 5
					&& (i=nextChar(_in, encoding,buf,count,baos)) != -1) {
					if (i == -1) {
						break;
					}
					s += (char) i;
					if (i == '>') {
						break;
					}
				}
			}
			if ("<?xml".equals(s)) {
				while (s.indexOf("?", 5) == -1) { // find "?"
					i = nextChar(_in, encoding, buf, count, baos);
					if (i == -1) {
						break;
					}
					s += (char) i;
					if(i == '?' || i == '>') {
						break;
					}
				}
			}
		}
		String val = getXMLDeclParam("encoding", s);
		_encoding = val != null ? val : encoding;
		val = getXMLDeclParam("version", s);
		_version = val != null ? val : "1.0";
		_standalone = "yes".equals(getXMLDeclParam("standalone", s));
		_buf = baos.toByteArray();
		_len = _buf.length;
	}

	public final String getXMLEncoding() {return _encoding;}
	public final String getXMLVersion() {return _version;}
	public final boolean getXMLStandalone() {return _standalone;}
	public final InputStream getInputStream() {return _len == 0 ? _in : this;}
	public final byte[] getparsedBytes() {return _buf;}

	/** Get XMLDecl parameter.
	 * @param paramName
	 * @param source
	 * @return value of required parameter or null.
	 */
	private static String getXMLDeclParam(String paramName, String source) {
		int ndx = source.indexOf(paramName);
		if (ndx > 0) {
			int ndx1 = source.indexOf('=', ndx + 8);
			if (ndx1 > 0) {
				int ndx2 = source.indexOf('"', ndx1 + 1);
				if (ndx2 == -1) {
					ndx2 = source.indexOf('\'', ndx1 + 1);
				}
				if (ndx2 >= 0) {
					char quote = source.charAt(ndx2++);
					int ndx3 = source.indexOf(quote, ndx2);
					if (ndx3 > 0) {
						return source.substring(ndx2, ndx3);
					}
				}
			}
		}
		return null;
	}

	/** Detect encoding without External Encoding Information
	 * (see http://www.w3.org/TR/REC-xml/#charsets).
	 * <UL>
	 * <LI>With a Byte Order Mark (BOM):
	 * <p>EF BB BF: UTF-8</p>
	 * <p>00 00 FE FF: UCS-4, big-endian machine (1234 order)</p>
	 * <p>FF FE 00 00: UCS-4, little-endian machine (4321 order)</p>
	 * <p>00 00 FF FE: UCS-4, unusual octet order (2143)</p>
	 * <p>FE FF 00 00: UCS-4, unusual octet order (3412)</p>
	 * <p>FE FF ## ##: UTF-16, big-endian</p>
	 * <p>FF FE ## ##: UTF-16, little-endian</p>
	 * </LI>
	 * <LI><p>Without a Byte Order Mark:</p>
	 * <b>00 00 00 3C, 3C 00 00 00, 00 00 3C 00, 00 3C 00 00:</b>
	 * <p>UCS-4 or other encoding with a 32-bit code unit and ASCII
	 * characters encoded as ASCII values, in respectively big-endian(1234),
	 * little-endian(4321) and two unusual byte orders (2143 and 3412).
	 * The encoding declaration must be read to determine which of UCS-4 or
	 * other supported 32-bit encodings applies.</p>
	 * <b>00 3C 00 3F</b>
	 * <p>UTF-16BE or big-endian ISO-10646-UCS-2 or other encoding with a
	 * 16-bit code unit in big-endian order and ASCII characters encoded as
	 * ASCII values (the encoding declaration must be read to determine
	 * which)</p>
	 * <b>3C 00 3F 00:</b>
	 * <p>UTF-16LE or little-endian ISO-10646-UCS-2 or other encoding with a
	 * 16-bit code unit in little-endian order and ASCII characters encoded
	 * as ASCII values (the encoding declaration must be read to determine
	 * which)</p>
	 * <b>3C 3F 78 6D:</b>
	 * <p>UTF-8, ISO 646, ASCII, some part of ISO 8859, Shift-JIS, EUC, or
	 * any other 7-bit, 8-bit, or mixed-width encoding which ensures that
	 * the characters of ASCII have their normal positions, width, and
	 * values; the actual encoding declaration must be read to detect which
	 * of these applies, but since all of these encodings use the same bit
	 * patterns for the relevant ASCII characters, the encoding declaration
	 * itself may be read reliably</p>
	 * <b>4C 6F A7 94:</b>
	 * <p>EBCDIC (in some flavor; the full encoding declaration must be read
	 * to tell which code page is in use)</p>
	 * <b>Other:</b>
	 * <p>UTF-8 without an encoding declaration, or else the data stream
	 * is mislabeled (lacking a required encoding declaration), corrupt,
	 * fragmentary, or enclosed in a wrapper of some kind.</p>
	 * </LI>
	 * </UL>
	 * @param in InputStream where to read.
	 * @param buf array of four bytes to which first bytes are read.
	 * @return character set name. First two characters have a special mesaning.
	 * The first character is number of read bytes and the second character
	 * is the number of bytes to read next character.
	 * @throws IOException if an IO error occurs.
	 */
	private static String detectEncoding(final InputStream in,
		final byte[] buf) throws IOException {
		int i1, i2, i3, i4;
		buf[0] = (byte) (i1 = in.read());
		if (i1 == -1) {
			return ((char)('0' - 1)) + "0UTF-8"; // first character - '0' = -1!
		}
		buf[1] = (byte) (i2 = in.read());
		if (i2 == -1) {
			return "11UTF-8";
		}
		buf[2] = (byte) (i3 = in.read());
		if (i3 == -1) {
			return  (i1 == 0xFE && i2 == 0xFF) ? "02UTF-16BE" //BOM
				: (i1 == 0xFF && i2 == 0xFE) ?   "02UTF-16LE" //BOM
				: "22UTF-8";
		}
		if (i1 == 0xEF && i2 == 0xBB && i3 == 0xBF) {
			return "01UTF-8"; //BOM
		}
		buf[3] = (byte) (i4 = in.read());
		if (i4 == -1) {
			return "31UTF-8";
		} else if (i1 == 0xFF && i2 == 0xFE) { // BOM
			if (i3 == 0 && i4 == 0) { // FF FE 00 00
				return "04UTF-32LE"; // BOM 4321
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
			return (i3 == 0 && i4 == 0) ? "44X-ISO-10646-UCS-4-3412" //0<00
				: "42UTF-16BE";
		} else if (i1 != 0 && i2 == 0) {
			return (i3 == 0 && i4 == 0) ? "44UTF-32LE" : "42UTF-16LE";
		} else if (i1 == 0x4C && i2 == 0x6F && i3 == 0xA7 && i4 == 0x94) {
			return "41CP037"; // EBCDIC ("<?xm")
		}
		return "41UTF-8"; // other
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
	private static int nextChar(final InputStream in,
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

	/** Convert bytes from buffer to string.
	 * @param buf array of bytes to be converted.
	 * @param off offset where to start converting.
	 * @param len number of bytes to be converted.
	 * @param encoding character set name.
	 * @return converted string.
	 * @throws IOException if an error occurs.
	 */
	private static String bytesToString(byte[] buf,
		int off,
		int len,
		String encoding) throws IOException {
		if (len == 0) {
			return "";
		}
		if ("X-ISO-10646-UCS-4-2143".equals(encoding)
			|| "X-ISO-10646-UCS-4-3412".equals(encoding)) {
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

////////////////////////////////////////////////////////////////////////////////
// Implementation of InputStream methods
////////////////////////////////////////////////////////////////////////////////
//	@Override
//	public boolean markSupported() {return _in.markSupported();}
//
//	@Override
//	public void mark(int readlimit) {_in.mark(readlimit);}
//
//	@Override
//	public void reset() throws IOException {_in.reset();}
//
//	@Override
//	public long skip(final long n) throws IOException {return _in.skip(n);}

	@Override
	public int read() throws IOException {
		if (_hdrIndex <= _len) {
			if (_hdrIndex < _len) {
				return _buf[_hdrIndex++];
			}
			_buf = null;
		}
		return _in.read();
	}

	@Override
	public int read(final byte[] b) throws IOException {
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
	public int read(final byte[] b,
		final int off,
		final int len) throws IOException {
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
	public void close() throws IOException {
		if (_in != null) {
			_in.close();
		}
		_buf = null;
		_hdrIndex = _len = 0;
	}

}
