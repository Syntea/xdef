package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDInput;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.xdef.sys.ReportReader;
import org.xdef.XDValueType;

/** Implementation of input stream.
 * @author Vaclav Trojan
 */
public final class DefInStream extends XDValueAbstract implements XDInput {

	/** Reader. */
	private ReportReader _in;
	/** The file name. */
	private String _fname;
	/** Char set name */
	private String _encoding;
	/** flag if opened. */
	private boolean _opened;
	/** Flag if format of input data is XML.*/
	private boolean _xmlFormat;

	/** Creates new empty instance of DefStream.*/
	public DefInStream() {}

	/** Creates a new instance of DefStream
	 * @param in the ReportReader.
	 */
	public DefInStream(ReportReader in) {
		_fname = null;
		_encoding = null;
		_xmlFormat = false;
		_opened = true;
		_in = in;
	}

	/** Creates a new instance of DefStream
	 * @param fname the name of the file with the input data.
	 * @param encoding the name of encoding table (<tt>null</tt> =&gt; default
	 * encoding).
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SRuntimeException if an error occurs.
	 */
	public DefInStream(final String fname,
		final String encoding,
		final boolean xmlFormat) throws SRuntimeException {
		_fname = fname;
		_encoding = encoding;
		_xmlFormat = xmlFormat;
		open();
	}

	/** Creates a new instance of DefStream.
	 * @param reader the input stream.
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SRuntimeException if an error occurs.
	 */
	public DefInStream(final InputStreamReader reader,
		final boolean xmlFormat) throws SRuntimeException {
		_in = new FileReportReader(reader, xmlFormat);
		_encoding = reader.getEncoding();
		_fname = "#";
		_opened = true;
		_xmlFormat = xmlFormat;
		canonizeEncoding();
	}

	/** Creates a new instance of DefStream.
	 * @param stream the input stream.
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data strem is processed as a stream of source lines.
	 * @throws SRuntimeException if an error occurs.
	 */
	public DefInStream(final InputStream stream, final boolean xmlFormat)
	throws SRuntimeException{
		_xmlFormat = xmlFormat;
		InputStreamReader isr = new InputStreamReader(stream);
		_in = new FileReportReader(isr, xmlFormat);
		_encoding = isr.getEncoding();
		_fname = stream == System.in ? "#System.in" : "#";
		_opened = true;
		canonizeEncoding();
	}

	/** Creates a new instance of DefStream.
	 * @param name file pathname or "in" (System.in).
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SRuntimeException if an error occurs.
	 */
	public DefInStream(final String name, final boolean xmlFormat)
	throws SRuntimeException {
		InputStreamReader isr;
		_xmlFormat = xmlFormat;
		if (name == null || name.length() == 0 || name.equalsIgnoreCase("in")) {
			isr = new InputStreamReader(System.in);
			_in = new FileReportReader(isr, xmlFormat);
			_encoding = isr.getEncoding();
			_fname = "#System.in";
			_opened = true;
			canonizeEncoding();
		} else {
			_fname = name;
			_encoding = null;
			open();
		}
	}

	private void open() throws SRuntimeException {
		_opened = false;
		InputStreamReader isr;
		try {
			if (_encoding == null || _encoding.length() == 0) {
				isr = new InputStreamReader(new FileInputStream(_fname));
				_in = new FileReportReader(isr, _xmlFormat);
			} else {
				isr = new InputStreamReader(
					new FileInputStream(_fname), _encoding);
				_in = new FileReportReader(isr, _xmlFormat);
			}
			canonizeEncoding();
			_opened = true;
		} catch (FileNotFoundException ex) {
			//Can't read file: &{0}
			throw new SRuntimeException(SYS.SYS028, ex, _fname+" ("+ex+")");
		} catch (UnsupportedEncodingException ex) {
			//Unsupported encoding table: &{0}
			throw new SRuntimeException(SYS.SYS035, ex, _encoding);
		}
	}

	private void canonizeEncoding() {
		if(!_opened) {
			return;
		}
		if (_encoding!= null && _encoding.startsWith("Cp")) {
			_encoding = "windows-" + _encoding.substring(2);
		}
	}

	@Override
	/** Reset input stream.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void reset() throws SRuntimeException {
		if (_fname.charAt(0) == '#') {
			return;
		}
		close();
		open();
	}

	public String getEncoding() {
		return _encoding;
	}

	@Override
	public String readString() {
		if(!_opened) {
			return null;
		}
		Report rep;
		return (rep = _in.getReport()) == null ? "" : rep.toString();
	}

	@Override
	public String readStream() {
		if(!_opened) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Report rep;
		while ((rep = _in.getReport()) != null) {
			sb.append(rep.toString());
		}
		return sb.toString();
	}

	@Override
	public Report getReport() {
		return _in.getReport();
	}

	@Override
	public void close() {
		if(!_opened) {
			return;
		}
		_in.close();
		_opened = false;
	}

	@Override
	public boolean isOpened() {return _opened;}

	@Override
	/** Get object representing value */
	public ReportReader getReader() {return _in;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_INPUT;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.INPUT;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		return "org.xdef.impl.code.DefInStream(" + _fname + ")";
	}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return _fname;}

	@Override
	/** Clone the item (returns this object here).
	 * @return this object.
	 */
	public XDValue cloneItem() {return this;}
}