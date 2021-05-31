package org.xdef.json;

import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Interface for parsers of JSON/XON data.
 * @author Vaclav Trojan
 */
public interface JParser {
	/** Add value to result.
	 * @param value JValue to be added.
	 * @return null or name of pair if value pair already exists in
	 * the currently processed map.
	 */
	public String addValue(XONReader.JValue value);
	/** Set name  value pair.
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
	/** X-script item parsed.
	 * @param name name of item.
	 * @param value value of item.
	 */
	public void xdScript(SBuffer name, SBuffer value);
}