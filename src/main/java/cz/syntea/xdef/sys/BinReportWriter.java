/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: BinReportWriter.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.sys;

import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.OutputStream;

/**
 * Writer of binary form of report data.
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
	public void setLanguage(String language) {}

	@Override
	public void putReport(Report report) {
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
	public Report getLastErrorReport() {return _lastErrorReport;}

	@Override
	public void clearLastErrorReport() {
		_lastErrorReport = null;
	}

	@Override
	public void clearCounters() {
		_errors = 0;
		_lightErrors = 0;
		_warnings = 0;
		_fatals = 0;
		_size = 0;
	}

	@Override
	public void clear() {clearCounters();}

	@Override
	public int size() {return _size;}

	@Override
	/** Put fatal item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final String id, final String msg, final Object... mod) {
		putReport(Report.fatal(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void fatal(long registeredID, Object... mod) {
		putReport(Report.fatal(registeredID, mod));
	}

	@Override
	/** Put error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void error(final String id, final String msg, final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(long registeredID, Object... mod) {
		putReport(Report.error(registeredID, mod));
	}

	@Override
	/** Put light error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void lighterror(final String id,
		final String msg, final Object... mod) {
		putReport(Report.lightError(id, msg, mod));
	}

	@Override
	/** Put light error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void lightError(long registeredID, Object... mod) {
		putReport(Report.lightError(registeredID, mod));
	}

	@Override
	/** Put warning item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void warning(final String id, final String msg, final Object... mod){
		putReport(Report.warning(id, msg, mod));
	}

	@Override
	/** Put warning item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void warning(long registeredID, Object... mod) {
		putReport(Report.warning(registeredID, mod));
	}

	@Override
	/** Put audit item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void audit(final String id, final String msg, final Object... mod) {
		putReport(Report.audit(id, msg, mod));
	}

	@Override
	/** Put audit item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void audit(long registeredID, Object... mod) {
		putReport(Report.audit(registeredID, mod));
	}

	@Override
	/** Put message item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void message(final String id, final String msg, final Object... mod){
		putReport(Report.message(id, msg, mod));
	}

	@Override
	/** Put message item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void mesage(long registeredID, Object... mod) {
		putReport(Report.message(registeredID, mod));
	}

	@Override
	/** Put info item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void info(final String id, final String msg, final Object... mod) {
		putReport(Report.info(id, msg, mod));
	}

	@Override
	/** Put info item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void info(long registeredID, Object... mod) {
		putReport(Report.info(registeredID, mod));
	}

	@Override
	/** Put text item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void text(final String id, final String msg, final Object... mod) {
		putReport(Report.text(id, msg, mod));
	}

	@Override
	/** Put text item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void text(long registeredID, Object... mod) {
		putReport(Report.text(registeredID, mod));
	}

	@Override
	/** Put string item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void string(final String id, final String msg, final Object... mod) {
		putReport(Report.string(id, msg, mod));
	}

	@Override
	/** Put string item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void string(long registeredID, Object... mod) {
		putReport(Report.string(registeredID, mod));
	}

	@Override
	public boolean fatals() {return _fatals != 0;}

	@Override
	public boolean errors() {
		return _fatals + _errors + _lightErrors != 0;}

	@Override
	public boolean errorWarnings() {
		return _fatals + _errors + _lightErrors + _warnings != 0;
	}

	@Override
	public int getFatalErrorCount() {return _fatals;}

	@Override
	public int getErrorCount() {return _errors + _lightErrors;}

	@Override
	/** Get number of light error items.
	 * @return The number of light errors.
	 */
	public int getLightErrorCount() {return _lightErrors;}

	@Override
	public int getWarningCount() {return _warnings;}

	@Override
	public ReportReader getReportReader() {return null;}

	@Override
	public void close() {
		try {
			_out.getStream().close();
		} catch (Exception ex) {}
	}

	@Override
	/** Flush report writer. */
	public void flush() {
		try {
			_out.getStream().flush();
		} catch (Exception ex) {}
	}

	@Override
	public void writeString(String str) {
		putReport(Report.string(null, str));
	}

	@Override
	/** Check error reports are present in the report writer. Return normally if
	 * in no errors are found, otherwise throw exception with list of
	 * error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public void checkAndThrowErrors() throws SRuntimeException {
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
	public void checkAndThrowErrorWarnings() throws SRuntimeException {
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
	private void throwReports(boolean warnings) {
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

}