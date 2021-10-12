package org.xdef.xon;

import org.xdef.sys.SPosition;

/** Interface for JSON/XON parsers.
 * @author Vaclav Trojan
 */
public interface XonParsers {
	public SPosition getPosition();
	/** Parse JSON or XON source data.*/
	public void parse();
	public void closeReader();
}
