package org.xdef.impl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/** Implementation of a reader which supports connection with MyHandler.
 * @author Vaclav Trojan
 */
public class XReader extends XAbstractReader {

	/** The reader. */
	private final Reader _in;
	private boolean _notScanning;

	public XReader(final XInputStream mi) throws IOException {
		this(mi.getInputStream(), mi.getXMLEncoding());
	}

	public XReader(final InputStream in, final String encoding)
		throws IOException {
		this("X-ISO-10646-UCS-4-2143".equals(encoding)
			? new Reader_UCS_4_2143(in)
			: "X-ISO-10646-UCS-4-3412".equals(encoding)
				? new Reader_UCS_4_3412(in)
				: new InputStreamReader(in,encoding));
	}

	public XReader(final Reader in) {
		super();
		_in = in;
		setEncoding((in instanceof InputStreamReader)
			? ((InputStreamReader) in).getEncoding() : null);
	}

	@Override
	public int read() throws IOException {
		if (_notScanning) {
			return _in.read();
		}
		int result = _in.read();
		if (result != -1) {
			addBuf((char) result);
		}
		return result;
	}

	@Override
	public int read(final char[] cbuf) throws IOException {
		if (_notScanning) {
			return _in.read(cbuf);
		}
		int result = _in.read(cbuf);
		if (result > 0) {
			addBuf(cbuf, 0, result);
		}
		return result;
	}

	@Override
	public int read(final char[] cbuf,
		final int off, final int len) throws IOException {
		if (_notScanning) {
			return _in.read(cbuf, off, len);
		}
		int result = _in.read(cbuf, off, len);
		if (result > 0) {
			addBuf(cbuf, off, result);
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		if (!_closed) {
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