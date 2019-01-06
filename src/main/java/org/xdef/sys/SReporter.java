package org.xdef.sys;

import java.io.PrintStream;

/** Reporting with source position.
 * @author Vaclav Trojan
 */
public class SReporter extends SPosition {

	/** Assigned reporter.*/
	private ReportWriter _reportWriter;

	/** Creates a new instance of SReporter. Connected reporter
	 * will be ArrayReporter.
	 */
	public SReporter() {
		this (new ArrayReporter());
	}

	/** Creates a new instance of SReporter.
	 * @param reportWriter Report writer connected to report generator.
	 */
	public SReporter(final ReportWriter reportWriter) {
		_reportWriter = reportWriter;
	}

	/** Creates a new instance of SReporter with reporter
	 * and given parameters taken from another report generator.
	 * @param reporter The reporter.
	 */
	public SReporter(final SReporter reporter) {
		super(reporter);
		_reportWriter = reporter._reportWriter;
	}

	/** Creates a new instance of SReporter with reporter
	 * and given parameters.
	 * @param reportWriter Report Writer.
	 * @param position The source position.
	 */
	public SReporter(final ReportWriter reportWriter,
		final SPosition position) {
		super(position);
		_reportWriter = reportWriter;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Get report writer.
	 * @return Report writer associated with this reporter or <tt>null</tt>.
	 */
	public final ReportWriter getReportWriter() {return _reportWriter;}

	/** Set report writer.
	 * @param reporter SReporter to be associated with this generator.
	 */
	public final void setReportWriter(final ReportWriter reporter) {
		_reportWriter = reporter;
	}

////////////////////////////////////////////////////////////////////////////////
// Methods for reporting
////////////////////////////////////////////////////////////////////////////////

	/** Put fatal error message with modification parameters.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param mod Message modification parameters.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void fatal(final String id, final String msg, final Object... mod) {
		putReport(Report.fatal(id, msg, mod), _reportWriter);
	}

	/** Put error message with modification parameters.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param mod Message modification parameters.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void error(final String id, final String msg, final Object... mod) {
		putReport(Report.error(id, msg, mod), _reportWriter);
	}

	/** Put light error message with modification parameters.
	 * @param id The message id (may be <tt>null</tt>).
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void lightError(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.lightError(id, msg, mod), _reportWriter);
	}

	/** Put warning message with modification parameters.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public void warning(final String id, final String msg, final Object... mod){
		putReport(Report.warning(id, msg, mod), _reportWriter);
	}

	/** Put fatal error message with modification parameters.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void fatal(final long registeredID, final Object... mod) {
		putReport(Report.fatal(registeredID, mod), _reportWriter);
	}

	/** Put error message with modification parameters.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void error(final long registeredID, final Object... mod) {
		putReport(Report.error(registeredID, mod), _reportWriter);
	}

	/** Put light error message with modification parameters.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void lightError(final long registeredID, final Object... mod) {
		putReport(Report.lightError(registeredID, mod), _reportWriter);
	}

	/** Put warning message with modification parameters.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public void warning(final long registeredID, final Object... mod) {
		putReport(Report.warning(registeredID, mod), _reportWriter);
	}

	/** Put report. Type of report may be WARNING, ERROR or FATAL
	 * (see {@link org.xdef.sys.Report#getMsgID()}).
	 * @param report The report.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt> .
	 */
	public void putReport(final Report report) {
		putReport(report, _reportWriter);
	}

	/** Put report at position.
	 * @param pos Source position.
	 * @param report The report.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt> .
	 */
	public void putReport(final SPosition pos, final Report report) {
		pos.putReport(report, _reportWriter);
	}

	/** Get number of errors.
	 * @return Number of errors.
	 */
	public int getErrorCount() {
		return _reportWriter == null ? 0 : _reportWriter.getErrorCount();
	}

	/** Get number of warnings.
	 * @return Number of warnings.
	 */
	public int getWarningCount() {
		return _reportWriter == null ? 0 : _reportWriter.getWarningCount();
	}

	/** Return <tt>true</tt> if and only if errors or fatal errors or light
	 * errors were generated.
	 * @return <tt>true</tt> if an error occurred.
	 */
	public boolean errors() {
		return _reportWriter == null ? false : _reportWriter.errors();
	}

	/** Check if errors, light errors or warnings were generated.
	 * @return true if errors or warnings occurred.
	 */
	public boolean errorWarnings() {
		return _reportWriter == null ? false : _reportWriter.errorWarnings();
	}

	/** Check error reports in the reporter. Return normally if no error was
	 * reported, otherwise throws the exception with the list of error messages
	 * (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if an error was reported.
	 */
	public void checkAndThrowErrors() throws SRuntimeException {
		if (_reportWriter != null) {
			_reportWriter.checkAndThrowErrors();
		}
	}

	/** Check error or warning reports in the reporter. Return normally if
	 * no error or warning was reported, otherwise throws the exception with
	 * the list of error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if an error or warning was reported.
	 */
	public void checkAndThrowErrorWarnings() throws SRuntimeException {
		if (_reportWriter != null) {
			_reportWriter.checkAndThrowErrorWarnings();
		}
	}

	/** Write reports to output stream.
	 * Please use getReportReader().printReports(...).
	 * @param out The output stream.
	 */
	public void printReports(final PrintStream out) {
		_reportWriter.getReportReader().printReports(out);
	}

	/** Write reports to String.
	 * @return the String with reports.
	 */
	public String printToString() {
		return _reportWriter.getReportReader().printToString();
	}

}