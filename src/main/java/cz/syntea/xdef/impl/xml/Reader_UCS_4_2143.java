/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: MyReader.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.xml;

import java.io.InputStream;

/** Reader of X-ISO-10646-UCS-4-2143 or X-ISO-10646-UCS-4-3412 charset.
 * @author trojan
 */
class Reader_UCS_4_2143 extends Reader_UCS_4_xxxx {
	/** four bytes. */
	private final byte[] _b4 = new byte[4];
	/** Gen detailed position flag. */
	private boolean _genPositions;

	Reader_UCS_4_2143(final InputStream in) {
		super(in);
		setEncoding("X-ISO-10646-UCS-4-2143");
//		setEncoding("UTF-32");
	}

	@Override
	final void changeBuffer(final byte[] byteBuf, final int len) {
		for (int i = 0; i < len; i += 4) {
			System.arraycopy(byteBuf, i, _b4, 0, 4);
			byteBuf[i + 1] = _b4[0];
			byteBuf[i] = _b4[1];
			byteBuf[i + 3] = _b4[2];
			byteBuf[i + 2] = _b4[3];
		}
	}
}
