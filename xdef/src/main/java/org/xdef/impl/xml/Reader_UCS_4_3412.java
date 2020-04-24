package org.xdef.impl.xml;

import java.io.InputStream;

/** Reader of X-ISO-10646-UCS-4-2143 or X-ISO-10646-UCS-4-3412 charset.
 * @author Vaclav Trojan
 */
class Reader_UCS_4_3412 extends Reader_UCS_4_xxxx {

	/** four bytes. */
	private final byte[] _b4 = new byte[4];

	Reader_UCS_4_3412(final InputStream in) {
		super(in);
		setEncoding("X-ISO-10646-UCS-4-3412");
//		setEncoding("UTF-32");
	}

	@Override
	final void changeBuffer(final byte[] byteBuf, final int len) {
		for (int i = 0; i < len; i += 4) {
			System.arraycopy(byteBuf, i, _b4, 0, 4);
			byteBuf[i + 2] = _b4[0];
			byteBuf[i + 3] = _b4[1];
			byteBuf[i] = _b4[2];
			byteBuf[i + 1] = _b4[3];
		}
	}
}