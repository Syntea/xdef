package cz.syntea.xdef.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/** Contains information about X-definition source items and ABOUT
 * the position of the screen if some item was displayed.
 * @author Vaclav Trojan
 */
public final class XDSourceInfo {
	/** X position of sreen left corner. */
	public int _xpos = -1;
	/** Y position of sreen left corner. */
	public int _ypos = -1;
	/** Sreen width. */
	public int _width = -1;
	/** Sreen height. */
	public int _height = -1;
	/** Map with X-definition source items. */
	public final Map<String, XDSourceItem> _sourcesMap =
		new TreeMap<String, XDSourceItem>();

	/** Create new empty instance. */
	XDSourceInfo() {}

	public final void writeXDSources(final XDWriter xw) throws IOException {
		int size = _sourcesMap.size();
		xw.writeLength(size);
		for (Entry<String, XDSourceItem> e: _sourcesMap.entrySet()) {
			xw.writeString(e.getKey());
			e.getValue().writeXDSourceItem(xw);
		}
	}

	public final static XDSourceInfo readXDSources(final XDReader xr)
		throws IOException {
		XDSourceInfo result = new XDSourceInfo();
		int size = xr.readLength();
		for (int i = 0; i < size; i++) {
			String key = xr.readString();
			XDSourceItem value = XDSourceItem.readXDSourceItem(xr);
			result._sourcesMap.put(key, value);
		}
		return result;
	}

}