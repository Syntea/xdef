package org.xdef.impl;

import org.xdef.XDDocument;
import org.xdef.sys.SReporter;

/** Interface for parsers of input data.
 * @author Vaclav Trojan
 */
interface XParser {

    /** Parse source.
     * @param chkDoc The ChkDocument object.
     */
    public void xparse(final XDDocument chkDoc);

    /** Get connected reporter.
     * @return connected SReporter.
     */
    public SReporter getReporter();

    /** Close reader of parsed data. */
    public void closeReader();
}