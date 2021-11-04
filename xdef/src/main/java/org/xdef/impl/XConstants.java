package org.xdef.impl;

/** Internally used constants
 * @author Vaclav Trojan
 */
public interface XConstants {
	/** X-definition version 3.1 ID. */
	public static final byte XD31 = 31;
	/** X-definition version 3.2 ID. */
	public static final byte XD32 = 32;
	/** X-definition version 4.0 ID. */
	public static final byte XD40 = 40;
	/** X-definition version 4.1 ID. */
	public static final byte XD41 = 41;

	/** JSON mode W3C format. */
	public static byte JSON_MODE_W = 1;
	/** JSON mode X-definition format. */
	public static byte JSON_MODE_XD = 2;

	/** JSON root element. */
	public static byte JSON_ROOT = 4;
}