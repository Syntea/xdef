package org.xdef.sys;

/** Interface for writing reports to streams of reports.
 * @author  Vaclav Trojan
 */
public interface ReportWriter {

	/** Maximal number of reports reported by checkAndThrow method. */
	public static final int MAX_REPORTS = 100;

	/** Set language (ISO-639 or ISO-639-2). This method takes an effect only if
	 * the reporter output is printed as a text to the output stream.
	 * @param language language id (ISO-639).
	 */
	public void setLanguage(final String language);

	/** Put the report to the list.
	 * @param report The report.
	 */
	public void putReport(Report report);

	/** Put fatal item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final String id, final String msg, final Object... mod);

	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void fatal(long registeredID, Object... mod);

	/** Put error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void error(final String id, final String msg, final Object... mod);

	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(long registeredID, Object... mod);

	/** Put light error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void lightError(long registeredID, Object... mod);

	/** Put light error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void lighterror(final String id,
		final String msg, final Object... mod);

	/** Put warning item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void warning(final String id, final String msg, final Object... mod);

	/** Put warning item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void warning(long registeredID, Object... mod);

	/** Put audit item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void audit(final String id, final String msg, final Object... mod);

	/** Put audit item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void audit(long registeredID, Object... mod);

	/** Put message item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void message(final String id, final String msg, final Object... mod);

	/** Put message item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void mesage(long registeredID, Object... mod);

	/** Put info item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void info(final String id, final String msg, final Object... mod);

	/** Put info item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void info(long registeredID, Object... mod);

	/** Put text item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void text(final String id, final String msg, final Object... mod);

	/** Put text item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void text(long registeredID, Object... mod);

	/** Put string item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void string(final String id, final String msg, final Object... mod);

	/** Put string item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void string(long registeredID, Object... mod);

	/** Get last error report.
	 * @return last error report (or <tt>null</tt> if last report is not
	 * available).
	 */
	public Report getLastErrorReport();

	/** Clear last error report. If last report has been available it will be
	 * erased (i.e. result of <tt>getLastReport()</tt> will be null. However,
	 * the report has already been written to the report file.
	 */
	public void clearLastErrorReport();

	/** Clear counters of fatal errors, errors and warnings. */
	public void clearCounters();

	/** Clear the report file. All report items will be erased from the file.
	 * Also last error report is cleared.
	 * throws KException if it is not possible to clear reports.
	 */
	public void clear();

	/** Get total number of reports.
	 * @return The number of generated reports.
	 */
	public int size();

	/** Check if fatal errors were generated.
	 * @return true is errors reports are present.
	 */
	public boolean fatals();

	/** Check if errors and/or fatal errors were generated.
	 * @return true is errors reports are present.
	 */
	public boolean errors();

	/** Check if warnings and/or errors and/or fatal errors were generated.
	 * @return true is warnings or errors reports are present.
	 */
	public boolean errorWarnings();

	/** Get number of fatal items.
	 * @return The number of generated fatal errors.
	 */
	public int getFatalErrorCount();

	/** Get number of error items.
	 * @return The number of errors.
	 */
	public int getErrorCount();

	/** Get number of light error items.
	 * @return The number of light errors.
	 */
	public int getLightErrorCount();

	/** Get number of warning items.
	 * @return The number of generated warnings.
	 */
	public int getWarningCount();

	/** Closes the reportWriter and creates report reader for reading created
	 * report data. If reader can't be created the SRuntimeException is thrown.
	 * @return report reader created from report writer.
	 */
	public ReportReader getReportReader();

	/** Close report writer. */
	public void close();

	/** flush report writer. */
	public void flush();

	/** Write string to reporter.
	 * @param str String to be written.
	 */
	public void writeString(String str);

	/** Check error reports stored in report writer. Return normally if
	 * in no errors are found, otherwise throw exception with list of
	 * error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public void checkAndThrowErrors() throws SRuntimeException;

	/** Check if error and warning reports were stored in report writer. Return
	 * normally if in no errors or warnings are found, otherwise throw
	 * exception with the  list of error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors or warnings has been generated.
	 */
	public void checkAndThrowErrorWarnings() throws SRuntimeException;

	/** Add to this reporter reports from report reader.
	 * @param reporter report reader with reports to be added.
	 */
	public void addReports(final ReportReader reporter);

}