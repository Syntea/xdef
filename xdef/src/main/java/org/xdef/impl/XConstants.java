package org.xdef.impl;

/** Internally used constants
 * @author Vaclav Trojan
 */
public interface XConstants {

	/** X-definition version 2.0 ID (deprecated). */
	public static final byte XD20 = 20;
	/** X-definition version 3.1 ID. */
	public static final byte XD31 = 31;
	/** X-definition version 3.2 ID. */
	public static final byte XD32 = 32;
	/** X-definition version 4.0 ID. */
	public static final byte XD40 = 40;

	/** JSON mode W3C format. */
	public static byte JSON_MODE_W3C = 1;
	/** JSON mode X-definition format. */
	public static byte JSON_MODE_XD = 2;

	/** JSON root element. */
	public static byte JSON_ROOT = 4;

	/** URI of X-definition 2.0 (deprecated). */
	public static final String XDEF20_NS_URI = "http://www.syntea.cz/xdef/2.0";
}