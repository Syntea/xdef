package org.xdef.impl.compile;

import java.io.InputStream;

/** Interface for readers of source X-definition (SML, JSON).
 * @author Vaclav Trojan
 */
public interface PreReader {

	/** Parse source input stream.
	 * @param in input stream with source data.
	 * @param sysId system ID of source data.
	 * @throws Exception if an error occurs.
	 */
	public void doParse(final InputStream in, final String sysId)
		throws Exception;
}