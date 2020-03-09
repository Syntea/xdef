package org.xdef;

import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.ReportWriter;

/** XD output stream/report writer  in x-script.
 * @author Vaclav Trojan
 */
public interface XDOutput extends XDValue {

	/** Write a string to the output stream.
	 * @param s String to be written.
	 */
	public void writeString(final String s);

	/** Write a report to the output stream.
	 * @param rep Report to be written.
	 */
	public void putReport(final Report rep);

	/** Get last error report.
	 * @return last error report (or <tt>null</tt> if last report is not
	 * available).
	 */
	public Report getLastErrorReport();

	/** Close output stream. */
	public void close();

	/** Flush buffer of the output stream. */
	public void flush();

	/** Get writer.
	 * @return report writer.
	 */
	public ReportWriter getWriter();

	/** Get XDInput from this XDOutput.
	 * @return XDInput created from this XDOutput (if it is possible).
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDInput getXDInput() throws SRuntimeException;

}