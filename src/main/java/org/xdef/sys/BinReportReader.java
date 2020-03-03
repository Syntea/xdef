package org.xdef.sys;

import java.io.InputStream;
import java.io.PrintStream;

/** Reader of binary form of report data.
 * @author Vaclav Trojan
 */
public class BinReportReader implements ReportReader {

	final private SObjectReader _in;

	public BinReportReader(final InputStream in) {_in = new SObjectReader(in);}

	@Override
	public final Report getReport() {
		try {
			return Report.readObj(_in);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return null;
		}
	}

	@Override
	public final void close() {
		try {_in.getStream().close();} catch (Exception ex) {}
	}

	@Override
	public final void printReports(final PrintStream out) {}

	@Override
	public final void printReports(final PrintStream out,
		final String language) {}

	@Override
	public final String printToString() {return null;}

	@Override
	public final String printToString(final String language) {return null;}

	@Override
	/** Write reports from this reporter reader to report writer.
	 * @param reporter OutputStreamWriter where to write,
	 */
	public void writeReports(final ReportWriter reporter) {
		Report rep;
		while((rep = getReport()) != null) {
			reporter.putReport(rep);
		}
	}
}