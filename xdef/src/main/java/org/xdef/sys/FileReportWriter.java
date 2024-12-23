package org.xdef.sys;

import org.xdef.msg.SYS;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/** Implements the ReportWriter interface for output streams and/or files. The format of file can be composed
 * from either Report objects transformed to XML format or from Reports objects transformed to strings.
 * @see org.xdef.sys.Report
 * @author Vaclav Trojan
 */

public class FileReportWriter implements ReportWriter {
	/** number of warning messages. */
	private int _warnings;
	/** Number of light errors reports. */
	private int _lightErrors;
	/** number of error messages. */
	private int _errors;
	/** number of fatal error messages. */
	private int _fatals;
	/** total number of messages. */
	private int _size;
	/** The output file. */
	private File _file;
	/** The output writer. */
	private PrintWriter _out;
	/** Last error report which has been written by put method. */
	private Report _lastErrorReport;
	/** true if output should be in xml format. */
	private final boolean _xmlFormat;
	/** Language code (ISO-639-2) used for printing of reports. */
	private String _language;

	/** Create new empty instance of FileReportWriter. */
	private FileReportWriter() {this(true);}

	/** Create new empty instance of FileReportWriter. */
	private FileReportWriter(boolean xmlFormat) {_xmlFormat = xmlFormat;}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The PrintStream where reports are printed.
	 */
	public FileReportWriter(final PrintStream out) {
		this();
		_out = new PrintWriter(new OutputStreamWriter(out), true);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The PrintStream where reports are printed.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 */
	public FileReportWriter(final PrintStream out, final boolean xmlFormat) {
		this(xmlFormat);
		_out = new PrintWriter(new OutputStreamWriter(out), true);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The PrintWriter where reports are printed.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 */
	public FileReportWriter(final PrintWriter out, final boolean xmlFormat) {
		this(xmlFormat);
		_out = out;
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The Output stream.
	 */
	public FileReportWriter(final OutputStream out) {
		this();
		_out = new PrintWriter(new OutputStreamWriter(out), true);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The Output stream.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 */
	public FileReportWriter(final OutputStream out, final boolean xmlFormat) {
		this(xmlFormat);
		_out = new PrintWriter(new OutputStreamWriter(out), true);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The Output stream.
	 */
	public FileReportWriter(final OutputStreamWriter out) {
		this();
		_out = new PrintWriter(out, true);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param out The Output stream.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 */
	public FileReportWriter(final OutputStreamWriter out,
		final boolean xmlFormat) {
		this(xmlFormat);
		_out = new PrintWriter(out, true);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param fileName The file for reports.
	 */
	public FileReportWriter(final String fileName) {
		this(new File(fileName));
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param fileName The file for reports.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 */
	public FileReportWriter(final String fileName, final boolean xmlFormat) {
		this(new File(fileName), xmlFormat);
	}

	/** Create new FileReportWriter with the output file for reports.
	 * @param fileName The file for reports.
	 * @param encoding The character set name;
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 * @throws SRuntimeException
	 * <ul>
	 * <li>SYS023 Can't write to file
	 * <li>SYS035 Unsupported character set name
	 * <li>SYS077 Security violation
	 * </ul>
	 */
	public FileReportWriter(final String fileName, final String encoding, final boolean xmlFormat) {
		this(new File(fileName), encoding, xmlFormat);
	}

	/** Create new KFileReportWriter with the output file for reports.
	 * @param file The file for reports.
	 */
	public FileReportWriter(final File file) {
		this(file, true);
	}

	/** Create new KFileReportWriter with the output file for reports.
	 * @param file The file for reports.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 * @throws SRuntimeException SYS023 Can't write to file.
	 * @throws SError SYS077 Security violation {0}
	 */
	public FileReportWriter(final File file, final boolean xmlFormat) {
		this(xmlFormat);
		try {
			_file = file;
			_out = new PrintWriter(new FileOutputStream(file), true);
		} catch (IOException ex) {
			//Can't write to file: &{0}
			throw new SRuntimeException(SYS.SYS023, file);
		} catch (SecurityException ex) {
			//Security violation &{0}
			throw new SRuntimeException(SYS.SYS077, file);
		}
	}

	/** Create new KFileReportWriter with the output file for reports.
	 * @param file The file for reports.
	 * @param encoding The character set name;
	 * @throws SRuntimeException if an error occurs:
	 * <ul>
	 * <li>SYS023 Can't write to file
	 * <li>SYS035 Unsupported character set name
	 * <li>SYS077 Security violation
	 * </ul>
	 */
	public FileReportWriter(final File file, final String encoding) {this(file, encoding, true);}

	/** Create new KFileReportWriter with the output file for reports.
	 * @param file The file for reports.
	 * @param xmlFormat If true the output will be XML format, otherwise the output is the string.
	 * @param encoding The character set name;
	 * the output be in the string format of reports.
	 * @throws SRuntimeException
	 * <ul>
	 * <li>SYS023 Can't write to file
	 * <li>SYS035 Unsupported character set name
	 * <li>SYS077 Security violation
	 * </ul>
	 */
	public FileReportWriter(final File file, final String encoding, final boolean xmlFormat) {
		this(xmlFormat);
		try {
			_file = file;
			if (encoding == null || encoding.isEmpty()) {
				_out = new PrintWriter(new FileOutputStream(file), true);
			} else {
				_out = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(file),
					encoding), true);
			}
		} catch (SecurityException ex) {
			//Security violation &{0}
			throw new SRuntimeException(SYS.SYS077, file);
		} catch (UnsupportedEncodingException ex) {
			//Unsupported charset name: &{0}
			throw new SRuntimeException(SYS.SYS035, encoding);
		} catch (FileNotFoundException ex) {
			//Can't write to file: &{0}
			throw new SRuntimeException(SYS.SYS023, file);
		}
	}

	@Override
	/** Set language (ISO-639 or ISO-639-2). This method takes an effect only if the reporter output
	 * is printed as a text to the output stream.
	 * @param language language id (ISO-639).
	 */
	public final void setLanguage(final String language) {_language = SUtils.getISO3Language(language);}

	@Override
	/** Put the report to the list.
	 * @param report The report.
	 */
	public final void putReport(final Report report) {
		_size++;
		if (_xmlFormat) {
			_out.write(report.toXmlString());
		} else {
			_out.write(_language == null ? report.toString() : report.toString(_language));
			if (report.getType() == Report.STRING) {
				_out.flush();
				return;
			}
		}
		_out.write('\n');
		_out.flush();
		switch (report.getType()) {
			case Report.ERROR: _errors++; _lastErrorReport = report; return;
			case Report.LIGHTERROR: _lightErrors++; _lastErrorReport = report; return;
			case Report.FATAL: _fatals++; _lastErrorReport = report; return;
			case Report.WARNING: _warnings++;
		}
	}

	@Override
	/** Write string to reporter.
	 * @param str String to be written.
	 */
	public final void writeString(final String str) {putReport(Report.string(null, str));}

	@Override
	/** Get last error report.
	 * @return last error report (or null if last report is not
	 * available).
	 */
	public final Report getLastErrorReport() {return _lastErrorReport;}

	@Override
	/** Clear last error report. If last report has been available it will be erased (i.e. result
	 * of getLastReport() will be null. However, the report has already been written to the report file.
	 */
	public void clearLastErrorReport() {_lastErrorReport = null;}

	@Override
	/** Clear counters of fatal errors, errors and warnings. */
	public final void clearCounters() {
		_errors = 0;
		_lightErrors = 0;
		_warnings = 0;
		_fatals = 0;
	}

	@Override
	/** Clear the report file. All report items will be errased from the file. Also cleare last error report.
	 * throws SRuntimeException if it is not possible to clear reports.
	 */
	public final void clear() {
		if (_file == null) {
			throw new SRuntimeException(SYS.SYS046); //Report writer: report file can't be cleared.
		}
		_out.close();
		try {
			_out = new PrintWriter(new FileOutputStream(_file), true);
		} catch (IOException ex) {
			throw new SRuntimeException(SYS.SYS046); //Report writer: report file can't be cleared.
		}
		clearCounters();
		_size = 0;
		_lastErrorReport = null;
	}

	@Override
	/** Get size of the list of reports.
	 * @return The number of items.
	 */
	public final int size() {return _size;}

	@Override
	/** Put fatal item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void fatal(final String id, final String msg, final Object... mod) {
		putReport(Report.fatal(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void fatal(final long ID, final Object... mod) {putReport(Report.fatal(ID, mod));}

	@Override
	/** Put error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void error(final String id, final String msg, final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void error(final long ID, final Object... mod) {putReport(Report.error(ID, mod));}

	@Override
	/** Put light error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void lighterror(final String id, final String msg, final Object... mod) {
		putReport(Report.lightError(id, msg, mod));
	}

	@Override
	/** Put light error item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void lightError(final long ID, final Object... mod) {putReport(Report.lightError(ID, mod));}

	@Override
	/** Put warning item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final String id, final String msg, final Object... mod){
		putReport(Report.warning(id, msg, mod));
	}

	@Override
	/** Put warning item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final long ID, final Object... mod) {putReport(Report.warning(ID, mod));}

	@Override
	/** Put audit item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void audit(final String id, final String msg, final Object... mod) {
		putReport(Report.audit(id, msg, mod));
	}

	@Override
	/** Put audit item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void audit(final long ID, final Object... mod) {putReport(Report.audit(ID, mod));}

	@Override
	/** Put message item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void message(final String id, final String msg, final Object... mod) {
		putReport(Report.message(id, msg, mod));
	}

	@Override
	/** Put message item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void mesage(final long ID, final Object... mod) {putReport(Report.message(ID, mod));}

	@Override
	/** Put info item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void info(final String id, final String msg, final Object... mod) {
		putReport(Report.info(id, msg, mod));
	}

	@Override
	/** Put info item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void info(final long ID, final Object... mod) {putReport(Report.info(ID, mod));}

	@Override
	/** Put text item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void text(final String id, final String msg, final Object... mod) {
		putReport(Report.text(id, msg, mod));
	}

	@Override
	/** Put text item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void text(final long ID, final Object... mod) {putReport(Report.text(ID, mod));}

	@Override
	/** Put string item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void string(final String id, final String msg, final Object... mod) {
		putReport(Report.string(id, msg, mod));
	}

	@Override
	/** Put string item.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void string(final long ID, final Object... mod) {putReport(Report.string(ID, mod));}

	@Override
	/** Get number of fatal items.
	 * @return The number of fatal error items.
	 */
	public final int getFatalErrorCount() {return _fatals;}

	@Override
	/** Get number of error items.
	 * @return The number of error items.
	 */
	public final int getErrorCount() {return _lightErrors + _errors;}

	@Override
	/** Get number of light error items.
	 * @return The number of light errors.
	 */
	public final int getLightErrorCount() {return _lightErrors;}

	@Override
	/** Get number of warning items.
	 * @return The number of warning items.
	 */
	public final int getWarningCount() {return _warnings;}

	@Override
	/** return true if fatal reports are present.
	 * @return <i>true</i> if and only if exists fatal errors.
	 */
	public final boolean fatals() {return _fatals != 0;}

	@Override
	/** Check if errors and/or fatal errors were generated.
	 * @return <i>true</i> if fatal or error items are present.
	 */
	public final boolean errors() {return _fatals + _lightErrors + _errors!=0;}

	@Override
	/** Check if warnings and/or errors and/or fatal errors were generated.
	 * @return <i>true</i> if fatal or error or warning items are present.
	 */
	public final boolean errorWarnings() {return _fatals + _errors + _lightErrors + _warnings != 0;}

	@Override
	/** Closes the reportWriter and creates report reader for reading createdreport data. If reader can't
	 * be created the SRuntimeException is thrown.
	 * @return report reader created from report writer.
	 * @throws SRuntimeException SYS045 Can't get report reader from this report writer.
	 */
	public final ReportReader getReportReader() {
		close();
		if (_file == null || !_xmlFormat) {
			return null;
		}
		try {
			return new FileReportReader(new InputStreamReader(new FileInputStream(_file)), true);
		} catch (FileNotFoundException ex) {
			throw new SRuntimeException(SYS.SYS045); //Can't get report reader from this report writer
		}
	}

	@Override
	/** Close report writer. */
	public final void close() {
		_out.flush();
		if (_file != null) {
			_out.close();
		}
	}

	@Override
	/** Flush report writer. */
	public final void flush() {_out.flush();}

	@Override
	/** Check error reports are present in the report writer. Return normally if no errors are found,
	 * otherwise throw exception with list of error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public final void checkAndThrowErrors() throws SRuntimeException {if (errors()) throwReports(false);}

	@Override
	/** Check if error and/or warning reports  are present in the report writer. Return normally if no errors
	 * or warnings are found, otherwise throw exception with the list of max. MAX_REPORTS messages.
	 * @throws SRuntimeException if errors or warnings has been generated.
	 */
	public final void checkAndThrowErrorWarnings() throws SRuntimeException {
		if (errorWarnings()) {
			throwReports(true);
		}
	}

	/** Throw runtime exception if reports with errors and (or even warnings) are present in report writer.
	 * @param warnings display all warnings messages if this argument is true, otherwise display only errors.
	 * @throws SRuntimeException with reports.
	 */
	private void throwReports(final boolean warnings) throws SRuntimeException {
		ReportReader reader = null;
		try {
			reader = getReportReader();
		} catch (Exception ex) {}
		if (reader == null) {
			ArrayReporter r = new ArrayReporter();
			r.putReport(getLastErrorReport());
			r.putReport(Report.error(SYS.SYS045)); //Can't get report reader from this report writer
			reader = r.getReportReader();
		}
		StringBuilder sb = new StringBuilder();
		Report rep;
		for (int i = 0; (rep=reader.getReport()) != null;) {
			if (i >= MAX_REPORTS) {
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(
					Report.text(null, "...").toXmlString(),'"',true));
				sb.append("&}");
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(Report.error(SYS.SYS013).toXmlString(), //Too many errors
					'"',true));
				sb.append("&}");
				break;
			} else if (warnings || rep.getType() == Report.ERROR || rep.getType() == Report.LIGHTERROR
				|| rep.getType() == Report.FATAL) {
				i++;
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(rep.toXmlString(),'"',true));
				sb.append("&}");
			}
		}
		throw new SRuntimeException(SYS.SYS012, sb.toString()); //Errors detected: &{0}
	}

	@Override
	public final String toString() {return "FileReportWriter";}

	@Override
	/** Add to this reporter reports from report reader.
	 * @param reporter report reader with reports to be added.
	 */
	public final void addReports(final ReportReader reporter) {
		Report rep;
		while((rep = reporter.getReport()) != null) {
			putReport(rep);
		}
	}
}