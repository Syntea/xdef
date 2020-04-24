package org.xdef.impl.xml;

import java.io.IOException;
import java.io.InputStream;

/** Reader of X-ISO-10646-UCS-4-2143 or X-ISO-10646-UCS-4-3412 charset.
 * @author Vaclav Trojan
 */
abstract class Reader_UCS_4_xxxx extends XAbstractReader {
	/** byte input stream. */
	private final InputStream _in;
	/** byte buffer. */
	private final byte[] _byteBuf = new byte[4096];
	/** char buffer. */
	private final char[] _charBuf = new char[2048];
	/** char buffer index. */
	private int _charBufIndex = 0;
	/** number of characters in char buffer. */
	private int _numChars = 0;
	/** flag if buteBuf is not multiple of 4. */
	private boolean _unexpectedEOF;
	/** Flag scanning is stopped. */
	private boolean _notScanning;

	Reader_UCS_4_xxxx(final InputStream in) {
		super();
		_in = in;
	}

	abstract void changeBuffer(final byte[] byteBuf, final int len);

	private void readChars() throws IOException {
		_charBufIndex = 0;
		int len = _in.read(_byteBuf);
		if (len <= 0) {
			_numChars = -1;
			if (len == -1) {
				if (_unexpectedEOF) {
					throw new IOException("Unexpected end of file");
				}
				return;
			}
			// this perhaps never happens
			int i = _in.read(); // length was 0, so we try to read a byte.
			if (i == -1) {
				return;
			}
			_byteBuf[0] = (byte) i;
			_numChars = 1;
			_numChars = 1;
		}
		while ((len & 3) != 0) { // we need multiple of 4 bytes
			int i = _in.read();
			if (i == -1) {
				if (len < 4) {
					throw new IOException("Unexpected end of file");
				} else {
					_unexpectedEOF = true;
					len = len & 3;
					break;
				}
			}
			_byteBuf[len++] = (byte) i;
		}
		changeBuffer(_byteBuf, len);
		String s = new String(_byteBuf, 0, len, "UTF32");
		s.getChars(0, s.length(), _charBuf, 0);
		_numChars  = s.length();
		if (_numChars == 0) {
			_numChars = -1;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementat ion of methods or Reader
////////////////////////////////////////////////////////////////////////////////

	@Override
	public int read() throws IOException {
		if (_charBufIndex >= _numChars) {
			readChars();
			if (_numChars <= 0) {
				return -1;
			}
		}
		if (_notScanning) {
			return _charBuf[_charBufIndex++];
		} else {
			int result = _charBuf[_charBufIndex++];
			if (result != -1) {
				addBuf((char) result);
			}
			return result;
		}
	}

	@Override
	public int read(final char[] cbuf) throws IOException {
		int i = 0;
		int len = cbuf.length;
		while (i < len) {
			int numBytes = _numChars - _charBufIndex;
			if (numBytes <= 0) {
				readChars();
				if (_numChars <= 0) {
					break;
				}
				numBytes = _numChars - _charBufIndex;
			}
			if (i + numBytes > len) {
				numBytes = len - i;
			}
			System.arraycopy(_charBuf, _charBufIndex, cbuf, i, numBytes);
			i+= numBytes;
			_charBufIndex += numBytes;
		}
		if (i == 0) {
			return -1;
		}
		if (!_notScanning) {
			addBuf(cbuf, 0, i);
		}
		return i;
	}

	@Override
	public int read(final char[] cbuf,
		final int off, final int len) throws IOException {
		int i = off;
		while (i < len) {
			int numBytes = _numChars - _charBufIndex;
			if (numBytes <= 0) {
				readChars();
				if (_numChars <= 0) {
					break;
				}
				numBytes = _numChars - _charBufIndex;
			}
			if (i + numBytes > len) {
				numBytes = len - i;
			}
			System.arraycopy(_charBuf, _charBufIndex, cbuf, i, numBytes);
			i+= numBytes;
			_charBufIndex += numBytes;
		}
		if (i == 0) {
			return -1;
		}
		if (!_notScanning) {
			addBuf(cbuf, off, i);
		}
		return i;
	}

	@Override
	public void close() throws IOException {
		if (!isClosed()) {
			_in.close();
			_closed = true;
			if (_handler!=null) {
				_handler.popReader();
			}
		}
	}
	@Override
	void stopScanning() {
		_notScanning = true;
	}

}