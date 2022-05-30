package org.xdef;

import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.ReportReader;

/** XD input stream/report reader in x-script.
 * @author Vaclav Trojan
 */
public interface XDInput extends XDValue {

	/** Reset input stream.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void reset() throws SRuntimeException;

	/** Get actual report.
	 * @return actual report.
	 */
	public Report getReport();

	/** Read actual report as string.
	 * @return read data or null.
	 */
	public String readString();

	/** Read reports from actual position as string.
	 * @return reports from actual position or an empty string.
	 */
	public String readStream();

	/** Close input. */
	public void close();

	/** Check if input is in the state open.
	 * @return true if this input is opened.
	 */
	public boolean isOpened();

	/** Get reader.
	 * @return report reader.
	 */
	public ReportReader getReader();
}