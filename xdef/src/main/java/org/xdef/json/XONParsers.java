package org.xdef.json;

import org.xdef.sys.SPosition;

/** Interface for JSON/XON parsers.
 * @author Vaclav Trojan
 */
public interface XONParsers {
	public SPosition getPosition();
	/** Parse JSON or XON source data.*/
	public void parse();
	public void closeReader();
}
