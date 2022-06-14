package org.xdef.xon;

/** Names used in conversions of XON/JSON to XML and from XML.
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

////////////////////////////////////////////////////////////////////////////////
// names used in X-definition moldels
////////////////////////////////////////////////////////////////////////////////
	/** This keyword used for the specification of script in X-definition. */
	public static final String SCRIPT_NAME = "$:script";
	/** This keyword used for the specification of oneOf in X-definition. */
	public static final String ONEOF_NAME = "$:oneOf";
	/** This keyword used for the specification of any in X-definition. */
	public static final String ANY_NAME = "$:any";
}