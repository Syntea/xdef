package org.xdef.xon;

import org.xdef.sys.ReportWriter;
import org.xdef.sys.SPosition;

/** Interface for XON/JSON parsers.
 * @author Vaclav Trojan
 */
public interface XonParsers {
	/** get current source3 position.
	 * @return current source3 position.
	 */
	public SPosition getPosition();
	/** Parse XON/JSON source data.*/
	public void parse();
	/** close the reader. */
	public void closeReader();
	/** Set mode the parser is called from X-definition compiler. */
	public void setXdefMode();
	/** Set report writer.
	 * @param reporter SReporter to be associated with this generator.
	 */
	public void setReportWriter(ReportWriter reporter);
}
