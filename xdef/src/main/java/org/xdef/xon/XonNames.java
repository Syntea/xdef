package org.xdef.xon;

/** Names used in XON/JSON tools.
 * @author Vaclav Trojan
 */
public interface XonNames {

////////////////////////////////////////////////////////////////////////////////
// names used in XON DX and W3C format
////////////////////////////////////////////////////////////////////////////////
	/** XML element name of XON/JSON map. */
	public final static String X_MAP = "map";
	/** XML element name of XON/JSON array. */
	public final static String X_ARRAY = "array";
	/** XML attribute name of XON/JSON named item in a map. */
	public final static String X_KEYATTR = "key";
	/** XML element name XON/JSON value (value is in attribute. */
	public final static String X_ITEM = "item";
	/** XML attribute name of XON/JSON value (in the X_ITEM element). */
	public final static String X_VALATTR = "val";

	/** Charset directive name {used only in XON source data}. */
	public static final String CHARSET_DIRECTIVE = "%charset";

////////////////////////////////////////////////////////////////////////////////
// names used in X-definition moldels
////////////////////////////////////////////////////////////////////////////////
	/** This keyword used for the specification of script command in XON model. */
	public static final String SCRIPT_CMD = "%script";
	/** This keyword used for the specification of oneOf command in XON model. */
	public static final String ONEOF_CMD = "%oneOf";
	/** This keyword used for the specification of any object in XON model. */
	public static final String ANY_OBJ = "%anyObj";
	/** This keyword used for the specification of any name in XON map. */
	public static final String ANY_NAME = "%anyName";
}