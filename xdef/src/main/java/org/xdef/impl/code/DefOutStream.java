package org.xdef.impl.code;

import org.xdef.sys.FileReportWriter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDInput;
import org.xdef.XDOutput;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import org.xdef.sys.ReportWriter;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.OUTPUT;

/** Implementation of output stream.
 * @author Vaclav Trojan
 */
public final class DefOutStream extends XDValueAbstract implements XDOutput {
	/** Report Writer. */
	final private ReportWriter _out;

	/** Creates a new "null" instance of DefStream.*/
	public DefOutStream() {_out = null;}

	/** Creates a new instance of DefStream.
	 * @param fname the file name.
	 * @param encoding the name of an encoding table.
	 * @param xmlFormat if true the stream will be formated as XML.
	 */
	public DefOutStream(final String fname, final String encoding, final boolean xmlFormat) {
		_out = new FileReportWriter(fname, encoding, xmlFormat);
	}

	/** Creates a new instance of DefStream (XML format).
	 * @param fname the file name.
	 * @param encoding the name of an encoding table.
	 */
	public DefOutStream(final String fname, final String encoding) {
		_out = new FileReportWriter(fname, encoding, true);
	}

	/** Creates a new instance of DefStream (XML format).
	 * @param writer the writer where data will be written.
	 */
	public DefOutStream(final OutputStreamWriter writer) {_out = new FileReportWriter(writer, true);}

	/** Creates a new instance of DefStream from OutputStreamWriter.
	 * The output format will be in the XML format or string format according to argument xmlFormat.
	 * @param writer the writer where data will be written.
	 * @param xmlFormat if true the output will be in XML format, otherwise in string format.
	 */
	public DefOutStream(final OutputStreamWriter writer, final boolean xmlFormat) {
		_out = new FileReportWriter(writer, xmlFormat);
	}

	/** Creates a new instance of DefStream
	 * @param printer a printer.
	 */
	public DefOutStream(final PrintStream printer) {_out = new FileReportWriter(printer, false);}

	/** Creates a new instance of DefStream
	 * @param printer a printer.
	 * @param xmlFormat if true the output will be in XML format, otherwise in string format.
	 */
	public DefOutStream(final PrintStream printer, final boolean xmlFormat) {
		_out = new FileReportWriter(printer, xmlFormat);
	}

	/** Creates a new instance of DefStream
	 * @param printer a printer.
	 */
	public DefOutStream(final PrintWriter printer) {_out = new FileReportWriter(printer, false);}

	/** Creates a new instance of DefStream
	 * @param printer a printer.
	 * @param xmlFormat if true the output will be in XML format, otherwise in string format.
	 */
	public DefOutStream(final PrintWriter printer, final boolean xmlFormat) {
		_out = new FileReportWriter(printer, xmlFormat);
	}

	/** Creates a new instance of DefStream
	 * @param printer writer as a printer.
	 * otherwise in string format.
	 */
	public DefOutStream(final Writer printer) {_out = new FileReportWriter(new PrintWriter(printer), false);}

	/** Creates a new instance of DefStream
	 * @param printer writer as a printer.
	 * @param xmlFormat if true the output will be in XML format, otherwise in string format.
	 */
	public DefOutStream(final Writer printer, final boolean xmlFormat) {
		_out = new FileReportWriter(new PrintWriter(printer), xmlFormat);
	}

	/** Creates a new instance of DefStream.
	 * @param name The name of stream: either filename or "#System.out" or "#System.err".
	 */
	public DefOutStream(final String name) {
		_out = name == null || name.length() == 0 || name.equalsIgnoreCase("#System.out") ?
			new FileReportWriter(System.out) : name.equalsIgnoreCase("#System.err") ?
			new FileReportWriter(System.err) : new FileReportWriter(name);
	}

	/** Creates a new instance of DefStream from OutputStream. The output will be in XML format.
	 * @param os The OutputStream.
	 */
	public DefOutStream(final OutputStream os) {_out = new FileReportWriter(os, true);}

	/** Creates a new instance of DefStream from OutputStream. The output format will be in the XML format
	 * or string format according to argument xmlFormat.
	 * @param os The OutputStream.
	 * @param xmlFormat if true the output will be in XML format, otherwise in string format.
	 */
	public DefOutStream(final OutputStream os, final boolean xmlFormat) {
		_out = new FileReportWriter(os, xmlFormat);
	}

	/** Creates a new instance of DefStream from reporter. The output format depends
	 * on the format set in reporter.
	 * @param reporter The reporter.
	 */
	public DefOutStream(final ReportWriter reporter) {_out = reporter;}

	/** Write a string to the output stream.
	 * @param s String to be written.
	 */
	@Override
	public void writeString(final String s) {_out.writeString(s);}

	/** Write a report to the output stream.
	 * @param rep Report to be written.
	 */
	@Override
	public void putReport(final Report rep) {_out.putReport(rep);}

	/** Close output stream. */
	@Override
	public void close() {if (_out != null) _out.close();}

	/** Flush buffer of the output stream. */
	@Override
	public void flush() {_out.flush();}

	/** Get writer. */
	@Override
	public ReportWriter getWriter() {return _out;}

	/** Get last error report.
	 * @return last error report (or null if last report is not available).
	 */
	@Override
	public Report getLastErrorReport() {
		if (_out == null) {
			return null;
		}
		Report rep = _out.getLastErrorReport();
		_out.clearLastErrorReport();
		return rep;
	}

	/** Get XDInput from this XDOutput.
	 * @return XDInput created from this XDOutput (if it is possible).
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public XDInput getXDInput() throws SRuntimeException {return new DefInStream(_out.getReportReader());}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public short getItemId() {return XD_OUTPUT;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return OUTPUT;}

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public boolean isNull() {return _out == null;}

	/** Get value as String.
	 * @return The string from value.
	 */
	@Override
	public String toString() {return "org.xdef.impl.code.DefOutStream(" + _out + ")";}

	/** Get string value of this object.
	 * @return string value of this object.
	 */
	@Override
	public String stringValue() {return toString();}

	/** Clone the item (get this object here).
	 * @return this object.
	 */
	@Override
	public XDValue cloneItem() {return this;}
}