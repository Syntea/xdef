package org.xdef.sys;

import org.xdef.msg.SYS;
import org.xdef.xml.KXmlUtils;
import java.io.OutputStream;

/** Writer of binary form of report data.
 * @author Vaclav Trojan
 */
public class BinReportWriter implements ReportWriter {

	private final SObjectWriter _out;

	private Report _lastErrorReport;
	private int _lightErrors;
	private int _errors;
	private int _fatals;
	private int _warnings;
	private int _size;

	public BinReportWriter(OutputStream out) {_out = new SObjectWriter(out);}

	@Override
	public final void setLanguage(final String language) {}

	@Override
	public final void putReport(final Report report) {
		try {
			report.writeObj(_out);
			_size++;
			switch (report.getType()) {
				case Report.ERROR:
					_errors++;
					_lastErrorReport = report;
					return;
				case Report.LIGHTERROR:
					_lightErrors++;
					_lastErrorReport = report;
					return;
				case Report.FATAL:
					_fatals++;
					_lastErrorReport = report;
					return;
				case Report.WARNING:
					_warnings++;
			}
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS066, ex); //Internal error&{}{: }
		}
	}

	@Override
	public final Report getLastErrorReport() {return _lastErrorReport;}

	@Override
	public final void clearLastErrorReport() {
		_lastErrorReport = null;
	}

	@Override
	public final void clearCounters() {
		_errors = 0;
		_lightErrors = 0;
		_warnings = 0;
		_fatals = 0;
		_size = 0;
	}

	@Override
	public final void clear() {clearCounters();}

	@Override
	public final int size() {return _size;}

	@Override
	/** Put fatal item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void fatal(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.fatal(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void fatal(final long registeredID, final Object... mod) {
		putReport(Report.fatal(registeredID, mod));
	}

	@Override
	/** Put error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void error(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void error(final long registeredID, final Object... mod) {
		putReport(Report.error(registeredID, mod));
	}

	@Override
	/** Put light error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void lighterror(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.lightError(id, msg, mod));
	}

	@Override
	/** Put light error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void lightError(final long registeredID, final Object... mod) {
		putReport(Report.lightError(registeredID, mod));
	}

	@Override
	/** Put warning item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.warning(id, msg, mod));
	}

	@Override
	/** Put warning item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final long registeredID, final Object... mod) {
		putReport(Report.warning(registeredID, mod));
	}

	@Override
	/** Put audit item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void audit(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.audit(id, msg, mod));
	}

	@Override
	/** Put audit item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void audit(final long registeredID, final Object... mod) {
		putReport(Report.audit(registeredID, mod));
	}

	@Override
	/** Put message item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void message(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.message(id, msg, mod));
	}

	@Override
	/** Put message item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void mesage(final long registeredID, final Object... mod) {
		putReport(Report.message(registeredID, mod));
	}

	@Override
	/** Put info item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void info(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.info(id, msg, mod));
	}

	@Override
	/** Put info item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void info(final long registeredID, final Object... mod) {
		putReport(Report.info(registeredID, mod));
	}

	@Override
	/** Put text item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void text(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.text(id, msg, mod));
	}

	@Override
	/** Put text item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void text(final long registeredID, final Object... mod) {
		putReport(Report.text(registeredID, mod));
	}

	@Override
	/** Put string item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public final void string(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.string(id, msg, mod));
	}

	@Override
	/** Put string item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void string(final long registeredID, final Object... mod) {
		putReport(Report.string(registeredID, mod));
	}

	@Override
	public final boolean fatals() {return _fatals != 0;}

	@Override
	public final boolean errors() {
		return _fatals + _errors + _lightErrors != 0;}

	@Override
	public final boolean errorWarnings() {
		return _fatals + _errors + _lightErrors + _warnings != 0;
	}

	@Override
	public final int getFatalErrorCount() {return _fatals;}

	@Override
	public final int getErrorCount() {return _errors + _lightErrors;}

	@Override
	/** Get number of light error items.
	 * @return The number of light errors.
	 */
	public final int getLightErrorCount() {return _lightErrors;}

	@Override
	public final int getWarningCount() {return _warnings;}

	@Override
	public final ReportReader getReportReader() {return null;}

	@Override
	public void close() {
		try {
			_out.getStream().close();
		} catch (Exception ex) {}
	}

	@Override
	/** Flush report writer. */
	public final void flush() {
		try {
			_out.getStream().flush();
		} catch (Exception ex) {}
	}

	@Override
	public final void writeString(final String str) {
		putReport(Report.string(null, str));
	}

	@Override
	/** Check error reports are present in the report writer. Return normally if
	 * in no errors are found, otherwise throw exception with list of
	 * error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public final void checkAndThrowErrors() throws SRuntimeException {
		if (errors()) {
			throwReports(false);
		}
	}

	@Override
	/** Check if error and/or warning reports  are present in the report writer.
	 * Return normally if in no errors or warnings are found, otherwise throw
	 * exception with the  list of error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors or warnings has been generated.
	 */
	public final void checkAndThrowErrorWarnings() throws SRuntimeException {
		if (errorWarnings()) {
			throwReports(true);
		}
	}

	/** Throw runtime exception if reports with errors and (or even warnings)
	 * are present in the report writer.
	 * @param warnings display all warnings messages if this argument is true,
	 * otherwise display only errors.
	 * @throws SRuntimeException with reports.
	 */
	private void throwReports(final boolean warnings) {
		ReportReader reader = null;
		try {
			reader = getReportReader();
		} catch (Exception ex) {}
		if (reader == null) {
			ArrayReporter r = new ArrayReporter();
			r.putReport(getLastErrorReport());
			// Can't get report reader from this report writer
			r.putReport(Report.error(SYS.SYS045));
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
				sb.append(KXmlUtils.toXmlText(
					//Too many errors
					Report.error(SYS.SYS013).toXmlString(),'"',true));
				sb.append("&}");
				break;
			} else if (warnings || rep.getType() == Report.ERROR ||
				rep.getType() == Report.LIGHTERROR ||
				rep.getType() == Report.FATAL) {
				i++;
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(rep.toXmlString(),'"',true));
				sb.append("&}");
			}
		}
		//Errors detected: &{0}
		throw new SRuntimeException(SYS.SYS012, sb.toString());
	}

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