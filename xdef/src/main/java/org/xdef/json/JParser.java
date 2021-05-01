package org.xdef.json;

import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Interface for parsers of JSON/XON data.
 * @author Vaclav Trojan
 */
public interface JParser {
	public void simpleValue(XONReader.JValue value);
	public void namedValue(SBuffer name);
	public void arrayStart(SPosition pos);
	public void arrayEnd(SPosition pos);
	public void mapStart(SPosition pos);
	public void mapEnd(SPosition pos);
	public void xdScript(SBuffer name, SBuffer value);
	public void warning(SPosition pos, long ID, Object... params);
	public void error(SPosition pos, long ID, Object... params);
	public void fatal(SPosition pos, long ID, Object... params);
	public void setSysId(String sysId);
}