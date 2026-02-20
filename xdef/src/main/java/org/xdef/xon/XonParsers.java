package org.xdef.xon;

import org.xdef.sys.ReportWriter;
import org.xdef.sys.SPosition;

/** Interface for all XON parsers.
 * @author Vaclav Trojan
 */
public interface XonParsers {
    /** Parse XON/JSON source data.*/
    public void parse();

    /** close the reader. */
    public void closeReader();

    /** Set mode the parser is called from X-definition compiler. */
    public void setXdefMode();

    /** Set parser accepts XON format and not Xdef mode). */
    public void setXonMode();

    /** Set parser accepts JSON format (not Xdef mode and not XON mode). */
    public void setJsonMode();

    /** Get actual SPosition from parser.
     * @return actual SPosition from parser.
     */
    public SPosition getPosition();

    /** Set report writer.
     * @param reporter SReporter to be associated with this generator.
     */
    public void setReportWriter(ReportWriter reporter);
}