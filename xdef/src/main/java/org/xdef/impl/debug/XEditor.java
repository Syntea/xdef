package org.xdef.impl.debug;

import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;

/** Interface for X-definition editor.
 * @author Vaclav Trojan
 */
public interface XEditor {

	/** Open the GUI.
	 * @param xp XDPool.
	 * @param err error reporter.
	 * @return if true the GUI was finished else recompile is supposed.
	 */
	public boolean setXEditor(final XDPool xp, final ArrayReporter err);

	/** Close XEditor.
	 * @param msg text of message to be shown. If null no message is shown.
	 */
	public void closeXEditor(String msg);

}