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
    private final boolean isXml;
    /** Language code (ISO-639-2) used for printing of reports. */
    private String _language;

    /** Create new empty instance of FileReportWriter. */
    private FileReportWriter() {this(true);}

    /** Create new empty instance of FileReportWriter.
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     */
    private FileReportWriter(boolean idXml) {isXml = idXml;}

    /** Create new FileReportWriter with the output file for reports.
     * @param out The PrintStream where reports are printed.
     */
    public FileReportWriter(final PrintStream out) {
        this();
        _out = new PrintWriter(new OutputStreamWriter(out), true);
    }

    /** Create new FileReportWriter with the output file for reports.
     * @param out The PrintStream where reports are printed.
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     */
    public FileReportWriter(final PrintStream out, final boolean isXml) {
        this(isXml);
        _out = new PrintWriter(new OutputStreamWriter(out), true);
    }

    /** Create new FileReportWriter with the output file for reports.
     * @param out The PrintWriter where reports are printed.
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     */
    public FileReportWriter(final PrintWriter out, final boolean isXml) {
        this(isXml);
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
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     */
    public FileReportWriter(final OutputStream out, final boolean isXml) {
        this(isXml);
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
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     */
    public FileReportWriter(final OutputStreamWriter out, final boolean isXml) {
        this(isXml);
        _out = new PrintWriter(out, true);
    }

    /** Create new FileReportWriter with the output file for reports.
     * @param fname The file for reports.
     */
    public FileReportWriter(final String fname) {this(new File(fname));}

    /** Create new FileReportWriter with the output file for reports.
     * @param fname The file for reports.
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     */
    public FileReportWriter(final String fname, final boolean isXml) {this(new File(fname), isXml);}

    /** Create new FileReportWriter with the output file for reports.
     * @param fname The file for reports.
     * @param code The character set name;
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     * @throws SRuntimeException
     * <ul>
     * <li>SYS023 Can't write to file
     * <li>SYS035 Unsupported character set name
     * <li>SYS077 Security violation
     * </ul>
     */
    public FileReportWriter(final String fname,final String code,final boolean isXml){this(new File(fname),code,isXml);}

    /** Create new KFileReportWriter with the output file for reports.
     * @param file The file for reports.
     */
    public FileReportWriter(final File file) {this(file, true);}

    /** Create new KFileReportWriter with the output file for reports.
     * @param file The file for reports.
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     * @throws SRuntimeException SYS023 Can't write to file.
     * @throws SRuntimeException SYS077 Security violation {0}
     */
    public FileReportWriter(final File file, final boolean isXml) {
        this(isXml);
        try {
            _file = file;
            _out = new PrintWriter(new FileOutputStream(file), true);
        } catch (IOException ex) {
            throw new SRuntimeException(SYS.SYS023, file); //Can't write to file: &{0}
        } catch (SecurityException ex) {
            throw new SRuntimeException(SYS.SYS077, file); //Security violation &{0}
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
     * @param isXml If true the output will be XML format, otherwise the output is the string.
     * @param encoding The character set name; the output be in the string format of reports.
     * @throws SRuntimeException
     * <ul>
     * <li>SYS023 Can't write to file
     * <li>SYS035 Unsupported character set name
     * <li>SYS077 Security violation
     * </ul>
     */
    public FileReportWriter(final File file, final String encoding, final boolean isXml) {
        this(isXml);
        try {
            _file = file;
            if (encoding == null || encoding.isEmpty()) {
                _out = new PrintWriter(new FileOutputStream(file), true);
            } else {
                _out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding), true);
            }
        } catch (SecurityException ex) {
            throw new SRuntimeException(SYS.SYS077, file); //Security violation &{0}
        } catch (UnsupportedEncodingException ex) {
            throw new SRuntimeException(SYS.SYS035, encoding); //Unsupported charset name: &{0}
        } catch (FileNotFoundException ex) {
            throw new SRuntimeException(SYS.SYS023, file); //Can't write to file: &{0}
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

    /** Set language (ISO-639 or ISO-639-2). This method takes an effect only if the reporter output
     * is printed as a text to the output stream.
     * @param language language id (ISO-639).
     */
    @Override
    public final void setLanguage(final String language) {_language = SUtils.getISO3Language(language);}
    /** Put the report to the list.
     * @param report The report.
     */
    @Override
    public final void putReport(final Report report) {
        _size++;
        if (isXml) {
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
    /** Write string to reporter.
     * @param str String to be written.
     */
    @Override
    public final void writeString(final String str) {putReport(Report.string(null, str));}
    /** Get last error report.
     * @return last error report (or null if last report is not
     * available).
     */
    @Override
    public final Report getLastErrorReport() {return _lastErrorReport;}
    /** Clear last error report. If last report has been available it will be erased (i.e. result
     * of getLastReport() will be null. However, the report has already been written to the report file.
     */
    @Override
    public void clearLastErrorReport() {_lastErrorReport = null;}
    /** Clear counters of fatal errors, errors and warnings. */
    @Override
    public final void clearCounters() {
        _errors = 0;
        _lightErrors = 0;
        _warnings = 0;
        _fatals = 0;
    }
    /** Clear the report file. All report items will be errased from the file. Also cleare last error report.
     * Throws an SRuntimeException if it is not possible to clear reports.
     */
    @Override
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
    /** Get size of the list of reports.
     * @return The number of items.
     */
    @Override
    public final int size() {return _size;}

    /** Put fatal item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void fatal(final String id, final String msg, final Object... mod) {
        putReport(Report.fatal(id, msg, mod));
    }
    /** Put error item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void fatal(final long id, final Object... mod) {putReport(Report.fatal(id, mod));}
    /** Put error item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void error(final String id,final String msg,final Object... mod) {putReport(Report.error(id,msg,mod));}
    /** Put error item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void error(final long id, final Object... mod) {putReport(Report.error(id, mod));}
    /** Put light error item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void lighterror(final String id, final String msg, final Object... mod) {
        putReport(Report.lightError(id, msg, mod));
    }
    /** Put light error item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void lightError(final long id, final Object... mod) {putReport(Report.lightError(id, mod));}
    /** Put warning item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void warning(final String id, final String msg, final Object... mod){
        putReport(Report.warning(id, msg, mod));
    }
    /** Put warning item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void warning(final long id, final Object... mod) {putReport(Report.warning(id, mod));}
    /** Put audit item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void audit(final String id,final String msg,final Object... mod) {putReport(Report.audit(id,msg,mod));}
    /** Put audit item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void audit(final long id, final Object... mod) {putReport(Report.audit(id, mod));}
    /** Put message item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void message(final String id, final String msg, final Object... mod) {
        putReport(Report.message(id, msg, mod));
    }
    /** Put message item.
     * @param id registered report id nubmber.
     * @param mod Message modification parameters.
     */
    @Override
    public final void mesage(final long id, final Object... mod) {putReport(Report.message(id, mod));}
    /** Put info item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void info(final String id, final String msg, final Object... mod) {putReport(Report.info(id,msg,mod));}
    /** Put info item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void info(final long id, final Object... mod) {putReport(Report.info(id, mod));}
    /** Put text item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void text(final String id, final String msg, final Object... mod) {putReport(Report.text(id,msg,mod));}
    /** Put text item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void text(final long id, final Object... mod) {putReport(Report.text(id, mod));}
    /** Put string item.
     * @param id The report id. If id is null the default text is used.
     * @param msg Default text of report. If id is not found in report files this text is used.
     * @param mod Message modification parameters.
     */
    @Override
    public final void string(final String id, final String msg, final Object... mod) {
        putReport(Report.string(id, msg, mod));
    }
    /** Put string item.
     * @param id registered report id number.
     * @param mod Message modification parameters.
     */
    @Override
    public final void string(final long id, final Object... mod) {putReport(Report.string(id, mod));}
    /** Get number of fatal items.
     * @return The number of fatal error items.
     */
    @Override
    public final int getFatalErrorCount() {return _fatals;}
    /** Get number of error items.
     * @return The number of error items.
     */
    @Override
    public final int getErrorCount() {return _lightErrors + _errors;}
    /** Get number of light error items.
     * @return The number of light errors.
     */
    @Override
    public final int getLightErrorCount() {return _lightErrors;}
    /** Get number of warning items.
     * @return The number of warning items.
     */
    @Override
    public final int getWarningCount() {return _warnings;}
    /** return true if fatal reports are present.
     * @return <i>true</i> if and only if exists fatal errors.
     */
    @Override
    public final boolean fatals() {return _fatals != 0;}
    /** Check if errors and/or fatal errors were generated.
     * @return <i>true</i> if fatal or error items are present.
     */
    @Override
    public final boolean errors() {return _fatals + _lightErrors + _errors!=0;}
    /** Check if warnings and/or errors and/or fatal errors were generated.
     * @return <i>true</i> if fatal or error or warning items are present.
     */
    @Override
    public final boolean errorWarnings() {return _fatals + _errors + _lightErrors + _warnings != 0;}
    /** Closes the reportWriter and creates report reader for reading createdreport data. If reader can't
     * be created the SRuntimeException is thrown.
     * @return report reader created from report writer.
     * @throws SRuntimeException SYS045 Can't get report reader from this report writer.
     */
    @Override
    public final ReportReader getReportReader() {
        close();
        if (_file == null || !isXml) {
            return null;
        }
        try {
            return new FileReportReader(new InputStreamReader(new FileInputStream(_file)), true);
        } catch (FileNotFoundException ex) {
            throw new SRuntimeException(SYS.SYS045); //Can't get report reader from this report writer
        }
    }
    /** Close report writer. */
    @Override
    public final void close() {
        _out.flush();
        if (_file != null) {
            _out.close();
        }
    }
    /** Flush report writer. */
    @Override
    public final void flush() {_out.flush();}
    /** Check error reports are present in the report writer. Return normally if no errors are found,
     * otherwise throw exception with list of error messages (max. MAX_REPORTS messages).
     * @throws SRuntimeException if errors has been generated.
     */
    @Override
    public final void checkAndThrowErrors() throws SRuntimeException {if (errors()) throwReports(false);}
    /** Check if error and/or warning reports  are present in the report writer. Return normally if no errors
     * or warnings are found, otherwise throw exception with the list of max. MAX_REPORTS messages.
     * @throws SRuntimeException if errors or warnings has been generated.
     */
    @Override
    public final void checkAndThrowErrorWarnings() throws SRuntimeException {
        if (errorWarnings()) {
            throwReports(true);
        }
    }
    @Override
    public final String toString() {return "FileReportWriter";}
    /** Add to this reporter reports from report reader.
     * @param reporter report reader with reports to be added.
     */
    @Override
    public final void addReports(final ReportReader reporter) {
        Report rep;
        while((rep = reporter.getReport()) != null) {
            putReport(rep);
        }
    }
}
