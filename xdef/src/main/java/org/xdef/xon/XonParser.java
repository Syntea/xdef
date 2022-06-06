package org.xdef.xon;

import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Interface for parsers of XON/JSON/Properties/INI/CSV data.
 * @author Vaclav Trojan
 */
public interface XonParser {
	/** Put value to result.
	 * @param value X_Value to be added to result object.
	 */
	public void putValue(XonTools.JValue value);
	/** Set name of value pair.
	 * @param name value name.
	 * @return true if the name of pair already exists, otherwise return false.
	 */
	public boolean namedValue(SBuffer name);
	/** Array started.
	 * @param pos source position.
	 */
	public void arrayStart(SPosition pos);
	/** Array ended.
	 * @param pos source position.
	 */
	public void arrayEnd(SPosition pos);
	/** Map started.
	 * @param pos source position.
	 */
	public void mapStart(SPosition pos);
	/** Map ended.
	 * @param pos source position.
	 */
	public void mapEnd(SPosition pos);
	/** Processed comment.
	 * @param value SBuffer with the value of comment.
	 */
	public void comment(SBuffer value);
	/** X-script item parsed.
	 * @param name name of item.
	 * @param value value of item.
	 */
	public void xdScript(SBuffer name, SBuffer value);
	/** Get result of parser.
	 * @return parsed object.
	 */
	public Object getResult();
}