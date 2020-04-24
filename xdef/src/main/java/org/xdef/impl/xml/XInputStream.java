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
		String encoding = XAbstractReader.detectBOM(_in, buf);
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