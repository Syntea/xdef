package org.xdef.impl;

/** Internally used constants in this implementation.
 * @author Vaclav Trojan
 */
public interface XConstants {
	/** Xdefinition version 3.1 ID. */
	public static final byte XD31 = 31;
	/** Xdefinition version 3.2 ID. */
	public static final byte XD32 = 32;
	/** Xdefinition version 4.0 ID. */
	public static final byte XD40 = 40;
	/** Xdefinition version 4.1 ID. */
	public static final byte XD41 = 41;
	/** Xdefinition version 4.1 ID. */
	public static final byte XD42 = 42;

	/** XON/JSON mode W3C format. */
	public static byte XON_MODE_W = 1;
	/** XON/JSON mode Xdefinition format. */
	public static byte XON_MODE_XD = 2;

	/** XON/JSON root element. */
	public static byte XON_ROOT = 64;

	/** Name of model of any XON object. */
	public static final String JSON_ANYOBJECT = "JSON.ANYOBJECT";

////////////////////////////////////////////////////////////////////////////////
// Xdefinition gebugging
////////////////////////////////////////////////////////////////////////////////
	/** Xdefinition debugger property. */
	public static final String XDPROPERTY_XDEF_DBGSWITCHES = "JSON.ANYOBJECT";
	/** Xdefinition debugger property value - show generated XON models. */
	public static final String XDPROPERTYVALUE_DBG_SHOWXON = "showXon";
}