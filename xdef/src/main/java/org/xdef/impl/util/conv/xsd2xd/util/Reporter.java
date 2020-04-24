package org.xdef.impl.util.conv.xsd2xd.util;

import org.xdef.sys.ReportWriter;
import org.xdef.sys.SReporter;

/** Message reporter.
 *
 * @author Alexandrov
 */
public class Reporter extends SReporter {

	/** Debug mode switch. */
	private boolean _debugMode = false;

	/** Create new instance.
	 * @param repWriterInt Report writer.
	 * @param debugMode true if debug mode.
	 */
	public Reporter(ReportWriter repWriterInt, boolean debugMode) {
		super(repWriterInt);
		_debugMode = debugMode;
	}

	/** Debug mode switch setter.
	 * @param debugMode debug mode switch.
	 */
	public void setDebugMode(boolean debugMode) {_debugMode = debugMode;}

	/** Put warning message.
	 * @param id message id.
	 * @param msg message string.
	 */
	public void warning(String id, String msg) {
		if (_debugMode) {
			super.warning(id, msg);
		}
	}
}