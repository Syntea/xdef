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

	public Report getReport();

	public String readString();

	public String readStream();

	public void close();

	public boolean isOpened();

	/** Get reader.
	 * @return report reader.
	 */
	public ReportReader getReader();

}