package org.xdef.sys;

/** Interface for reporters.
 * @author Vaclav Trojan
 */
public interface Reporter {

	/** Get report writer.
	 * @return Report writer associated with this reporter or <tt>null</tt>.
	 */
	public ReportWriter getReportWriter();

	/** Set report writer.
	 * @param reportWriter SReporter to be associated with this generator.
	 */
	public void setReportWriter(final ReportWriter reportWriter);

	/** Get report reader.
	 * @return Report reader from associated report writer or <tt>null</tt>.
	 */
	public ReportReader getReportReader();

////////////////////////////////////////////////////////////////////////////////
// Methods for reporting
////////////////////////////////////////////////////////////////////////////////

	/** Put fatal error message.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void fatal(final String id, final String msg);

	/** Put fatal error message with modification parameters.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void fatal(final String id, final String msg, final String modif);

	/** Put error message.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void error(final String id, final String msg);

	/** Put error message with modification parameters.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void error(final String id, final String msg, final String modif);

	/** Put light error message.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void lightError(final String id, final String msg);

	/** Put light error message with modification parameters.
	 * @param id The message id (may be <tt>null</tt>).
	 * @param msg The message text.
	 * @param modif The modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void lightError(final String id,
		final String msg,
		final String modif);

	/** Put warning message.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 */
	public void warning(final String id, final String msg);

	/** Put warning message with modification parameters.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 */
	public void warning(final String id, final String msg, final String modif);

	/** Put fatal error message.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void fatal(final SPosition pos,
		final String id,
		final String msg);

	/** Put fatal error message with modification parameters.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void fatal(final SPosition pos,
		final String id,
		final String msg,
		final String modif);

	/** Put error message.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void error(final SPosition pos,
		final String id,
		final String msg);

	/** Put error message with modification parameters.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void error(final SPosition pos,
		final String id,
		final String msg,
		final String modif);

	/** Put light error message.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void lightError(final SPosition pos,
		final String id,
		final String msg);

	/** Put light error message with modification parameters.
	 * @param pos Source position.
	 * @param id The message id (may be <tt>null</tt>).
	 * @param msg The message text.
	 * @param modif The modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> .
	 */
	public void lightError(final SPosition pos,
		final String id,
		final String msg,
		final String modif);

	/** Put warning message.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 */
	public void warning(final SPosition pos,
		final String id, final String msg);

	/** Put warning message with modification parameters.
	 * @param pos Source position.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 */
	public void warning(final SPosition pos,
		final String id,
		final String msg,
		final String modif);

	/** Put report. Type of report may be WARNING, ERROR or FATAL
	 * (see {@link org.xdef.sys.Report#getMsgID()}).
	 * @param report The report.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt>.
	 */
	public void putReport(final Report report);

	/** Put report at position.
	 * @param pos Source position.
	 * @param report The report.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt>.
	 */
	public void putReport(final SPosition pos, final Report report);

	/** Put report. Type of report may be WARNING, ERROR or FATAL
	 * (see {@link org.xdef.sys.Report#getMsgID()}).
	 * @param type Type of report.
	 * @param id Identifier of message (may be <tt>null</tt>).
	 * @param msg Default text of report.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt> .
	 */
	public void putReport(final char type, final String id, final String msg);

	/** Put report. Type of report may be WARNING, ERROR or FATAL
	 * (see {@link org.xdef.sys.Report#getMsgID()}).
	 * @param type Type of report.
	 * @param id Identifier of message (may be <tt>null</tt>).
	 * @param msg Default text of report.
	 * @param modification Modification string or <tt>null</tt>.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt>.
	 */
	public void putReport(final char type,
		final String id,
		final String msg,
		final String modification);

	/** Put message with modification parameters.
	 * @param pos Source position.
	 * @param type Type of report.
	 * @param id Message id (may be <tt>null</tt>).
	 * @param msg Message text.
	 * @param modif Modification string (may be <tt>null</tt>).
	 * @throws SRuntimeException if reporter is <tt>null</tt> and if report
	 * type is <tt>FATAL</tt>, <tt>ERROR</tt> or <tt>LIGHTERROR</tt>.
	 */
	public void putReport(final SPosition pos,
		final char type,
		final String id,
		final String msg,
		final String modif);

	/** Check error reports stored in reporter specified by argument. Return
	 * in no errors are found, otherwise throw exception with list of
	 * error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public void checkAndThrowErrors();

	/** Get number of errors.
	 * @return Number of errors.
	 */
	public int getErrorCount();

	/** Return <tt>true</tt> if and only if errors or fatal errors or light
	 * errors were generated.
	 * @return <tt>true</tt> if an error occurred.
	 */
	public boolean errors();

	/** Check if errors, light errors or warnings were generated.
	 * @return true if errors or warnings occurred.
	 */
	public boolean errorWarnings();

}