package org.xdef.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/** Contains information about X-definition source items about
 * the position of the screen if some item was displayed (window rectangle).
 * @author Vaclav Trojan
 */
public final class XDSourceInfo {
	/** X position of screen left corner. */
	public int _xpos;
	/** Y position of screen left corner. */
	public int _ypos;
	/** Screen width. */
	public int _width;
	/** Screen height. */
	public int _height;
	/** Map with X-definition source items. */
	private final Map<String, XDSourceItem> _sourcesMap;

	/** Get map with the description of X-definition sources.
	 * @return map with the description of X-definition sources.
	 */
	public final Map<String, XDSourceItem> getMap() {return _sourcesMap;}

	/** Create the new empty instance. */
	public XDSourceInfo() {
		_xpos = -1;
		_ypos = -1;
		_width = -1;
		_height = -1;
		_sourcesMap = new LinkedHashMap<>();
	}

	/** Copy values from the other XDSourceInfo object.
	 * @param x other XDSourceInfo object to be copied to this object.
	 */
	public final void copyFrom(XDSourceInfo x) {
		_xpos = x._xpos;
		_ypos = x._ypos;
		_width = x._width;
		_height = x._height;
		_sourcesMap.clear();
		_sourcesMap.putAll(x.getMap());

	}

	/** Write this XDSourceIinfo object (screen position is ignored).
	 * @param xw Writer where to write
	 * @throws IOException if an error occurs.
	 */
	public final void writeXDSourceInfo(final XDWriter xw) throws IOException {
		xw.writeInt(_xpos);
		xw.writeInt(_ypos);
		xw.writeInt(_width);
		xw.writeInt(_height);
		int size = _sourcesMap.size();
		xw.writeLength(size);
		for (Entry<String, XDSourceItem> e: _sourcesMap.entrySet()) {
			xw.writeString(e.getKey());
			e.getValue().writeXDSourceItem(xw);
		}
	}

	/** Read XDSourceInfo object from XDReader (screen position is ignored).
	 * @param xr XDReader containing XDSourceInfo object
	 * @return XDSourceInfo object
	 * @throws IOException if an error occurs.
	 */
	public static final XDSourceInfo readXDSourceInfo(final XDReader xr) throws IOException {
		XDSourceInfo result = new XDSourceInfo();
		result._xpos = xr.readInt();
		result._ypos = xr.readInt();
		result._width = xr.readInt();
		result._height = xr.readInt();
		int size = xr.readLength();
		for (int i = 0; i < size; i++) {
			String key = xr.readString();
			XDSourceItem value = XDSourceItem.readXDSourceItem(xr);
			result._sourcesMap.put(key, value);
		}
		return result;
	}
}