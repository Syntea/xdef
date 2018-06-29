/*
 * File: XSourceItem.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.impl.xml.XInputStream;
import cz.syntea.xdef.impl.xml.XReader;
import cz.syntea.xdef.sys.SUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** XDefEditor data.
 * @author Vaclav Trojan
 */
final class XSourceItem {
	/** The URL if source item is file (may be null. */
	URL _url;
	/** Encoding of data of this item. */
	String _encoding;
	/** String containing source item text (may be null). */
	String _source;
	/** True if the text of this source item was changed. */
	boolean _changed;
	/** True if the text of this source item was saved. */
	boolean _saved;
	/** True if this source item is active. */
	boolean _active;
	/** Actual position to text of this item when the window is changed. */
	int _pos = -1;

	/** Create new empty instance. */
	private XSourceItem() {}

	/** Create new instance of this object from argument.
	 * @param o may be file, URL or string with the text.
	 */
	XSourceItem(Object o) throws Exception {
		if (o instanceof File) {
			_url = ((File) o).toURI().toURL();
		} else if (o instanceof URL) {
			_url = (URL) o;
		} else if ((o instanceof InputStream)) {
			InputStream in = (InputStream) o;
			try {
				XInputStream myInputStream = new XInputStream(in);
				_encoding = myInputStream.getXMLEncoding();
				_source = SUtils.readString(myInputStream , _encoding);
			} finally {
				in.close();
			}
		} else { // should be String
			_source = (String) o;
		}
	}

	void writeXSourceItem(XDWriter xw) throws IOException {
		xw.writeString(_url == null ? null : _url.toExternalForm());
		xw.writeString(_encoding);
		xw.writeString(_source);
	}

	static XSourceItem readXSourceItem(XDReader xr) throws IOException {
		XSourceItem result = new XSourceItem();
		String s = xr.readString();
		if (s != null) {
			result._url = new URL(s);
		}
		result._encoding = xr.readString();
		result._source = xr.readString();
		return result;
	}

}