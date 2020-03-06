package org.xdef.sys;


import java.io.PrintStream;

/** Interface for processing tables of source report messages.
 * @author  Vaclav Trojan
 */

public interface ReportReader {

	/** Get next report from the list or null.
	 * @return The report or null.
	 */
	public Report getReport();


	/** Close the report stream. */
	public void close();

	/** Write reports to output stream.
	 * @param out The PrintStream where reports are printed.
	 */
	public void printReports(PrintStream out);

	/** Write reports to output stream.
	 * @param language language id (ISO-639).
	 * @param out The PrintStream where reports are printed.
	 */
	public void printReports(PrintStream out, String language);

	/** Write reports to String (in actual language).
	 * @return the String with reports.
	 */
	public String printToString();

	/** Write reports to String in specified language.
	 * @param language language id (ISO-639).
	 * @return the String with reports.
	 */
	public String printToString(String language);

	/** Write reports from this reporter reader to report writer.
	 * @param reporter OutputStreamWriter where to write,
	 */
	public void writeReports(final ReportWriter reporter);

}