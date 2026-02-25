package org.xdef.sys;

import java.io.PrintStream;

/** Reporting with source position.
 * @author Vaclav Trojan
 */
public class SReporter extends SPosition {

    /** Assigned reporter.*/
    private ReportWriter _reportWriter;

    /** Creates a new instance of SReporter. Connected reporter will be ArrayReporter. */
    public SReporter() {this (new ArrayReporter());}

    /** Creates a new instance of SReporter.
     * @param reportWriter Report writer connected to report generator.
     */
    public SReporter(final ReportWriter reportWriter) {_reportWriter = reportWriter;}

    /** Creates a new instance of SReporter with reporter and given parameters taken from another reporter.
     * @param reporter The reporter.
     */
    public SReporter(final SReporter reporter) {super(reporter); _reportWriter = reporter._reportWriter;}

    /** Creates a new instance of SReporter with reporter and given parameters.
     * @param reportWriter Report Writer.
     * @param position The source position.
     */
    public SReporter(final ReportWriter reportWriter, final SPosition position) {
        super(position);
        _reportWriter = reportWriter;
    }

////////////////////////////////////////////////////////////////////////////////

    /** Get report writer.
     * @return Report writer associated with this reporter or null.
     */
    public final ReportWriter getReportWriter() {return _reportWriter;}

    /** Set report writer.
     * @param reporter SReporter to be associated with this generator.
     */
    public final void setReportWriter(final ReportWriter reporter) {_reportWriter = reporter;}

////////////////////////////////////////////////////////////////////////////////
// Methods for reporting
////////////////////////////////////////////////////////////////////////////////

    /** Put fatal error message with modification parameters.
     * @param id Message id (may be null).
     * @param msg Message text.
     * @param mod Message modification parameters.
     */
    public void fatal(final String id, final String msg, final Object... mod) {
        putReport(Report.fatal(id, msg, mod), _reportWriter);
    }

    /** Put error message with modification parameters.
     * @param id Message id (may be null).
     */
    public void error(final String id) {putReport(Report.error(id, null), _reportWriter);}

    /** Put error message with modification parameters.
     * @param id Message id (may be null).
     * @param msg Message text.
     * @param mod Message modification parameters.
     */
    public void error(final String id, final String msg, final Object... mod) {
        putReport(Report.error(id, msg, mod), _reportWriter);
    }

    /** Put light error message with modification parameters.
     * @param id The message id (may be null).
     * @param msg The message text.
     * @param mod Message modification parameters.
     */
    public void lightError(final String id, final String msg, final Object... mod) {
        putReport(Report.lightError(id, msg, mod), _reportWriter);
    }

    /** Put warning message with modification parameters.
     * @param id Message id (may be null).
     * @param msg The message text.
     * @param mod Message modification parameters.
     */
    public void warning(final String id, final String msg, final Object... mod) {
        putReport(Report.warning(id, msg, mod), _reportWriter);
    }

    /** Put fatal error message with modification parameters.
     * @param registeredID registered message ID.
     * @param mod Message modification parameters.
     */
    public void fatal(final long registeredID, final Object... mod) {
        putReport(Report.fatal(registeredID, mod), _reportWriter);
    }

    /** Put error message with modification parameters.
     * @param ID registered message ID.
     * @param mod Message modification parameters.
     */
    public void error(final long ID, final Object... mod) {
        putReport(Report.error(ID, mod), _reportWriter);
    }

    /** Put light error message with modification parameters.
     * @param ID registered message ID.
     * @param mod Message modification parameters.
     * @throws SRuntimeException if reporter is null.
     */
    public void lightError(final long ID, final Object... mod) {
        putReport(Report.lightError(ID, mod), _reportWriter);
    }

    /** Put warning message with modification parameters.
     * @param ID registered message ID.
     * @param mod Message modification parameters.
     */
    public void warning(final long ID, final Object... mod) {
        putReport(Report.warning(ID,mod),_reportWriter);
    }

    /** Put report. Type of report may be WARNING, ERROR or FATAL; see {@link org.xdef.sys.Report#getMsgID()}.
     * @param report The report.
     * @throws SRuntimeException if reporter is null and if report type is FATAL, ERROR or LIGHTERROR .
     */
    public void putReport(final Report report) {putReport(report, _reportWriter);}

    /** Put report at position.
     * @param pos Source position.
     * @param report The report.
     * @throws SRuntimeException if reporter is null and if report type is FATAL, ERROR or LIGHTERROR .
     */
    public void putReport(final SPosition pos, final Report report) {
        pos.putReport(report, _reportWriter);
    }

    /** Get number of errors.
     * @return Number of errors.
     */
    public final int getErrorCount() {return _reportWriter == null ? 0 : _reportWriter.getErrorCount();}

    /** Get number of warnings.
     * @return Number of warnings.
     */
    public final int getWarningCount() {return _reportWriter == null ? 0 : _reportWriter.getWarningCount();}

    /** Return true if and only if errors or fatal errors or light
     * errors were generated.
     * @return true if an error occurred.
     */
    public final boolean errors() {return _reportWriter == null ? false : _reportWriter.errors();}

    /** Check if errors, light errors or warnings were generated.
     * @return true if errors or warnings occurred.
     */
    public final boolean errorWarnings() {
        return _reportWriter == null ? false : _reportWriter.errorWarnings();
    }

    /** Check error reports in the reporter. Return normally if no error was reported, otherwise throws
     * the exception with the list of error messages (max. MAX_REPORTS messages).
     * @throws SRuntimeException if an error was reported.
     */
    public final void checkAndThrowErrors() throws SRuntimeException {
        if (_reportWriter != null) {
            _reportWriter.checkAndThrowErrors();
        }
    }

    /** Check error or warning reports in the reporter. Return normally if no error or warning was reported,
     * otherwise throws the exception with the list of error messages (max. MAX_REPORTS messages).
     * @throws SRuntimeException if an error or warning was reported.
     */
    public final void checkAndThrowErrorWarnings() throws SRuntimeException {
        if (_reportWriter != null) {
            _reportWriter.checkAndThrowErrorWarnings();
        }
    }

    /** Write reports to output stream.
     * Please use getReportReader().printReports(...).
     * @param out The output stream.
     */
    public final void printReports(final PrintStream out) {_reportWriter.getReportReader().printReports(out);}

    /** Write reports to String.
     * @return the String with reports.
     */
    public final String printToString() {return _reportWriter.getReportReader().printToString();}
}