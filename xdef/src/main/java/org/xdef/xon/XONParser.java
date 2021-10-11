package org.xdef.json;

import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Interface for parsers of JSON/XON data.
 * @author Vaclav Trojan
 */
public interface JParser {
	/** Put value to result.
	 * @param value JValue to be added to result object.
	 * @return null or name of pair if value pair already exists in
	 * the currently processed map.
	 */
	public String putValue(XONReader.JValue value);
	/** Set name of value pair.
	 * @param name value name.
	 */
	public void namedValue(SBuffer name);
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
}