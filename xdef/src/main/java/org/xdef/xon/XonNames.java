package org.xdef.xon;

/** Names used in conversions of XON/JSON to XML and from XML.
 * @author Vaclav Trojan
 */
public interface XonNames {
	/** XML element name of XON/JSON map. */
	public final static String X_MAP = "map";
	/** XML element name of XON/JSON array. */
	public final static String X_ARRAY = "array";
	/** XML attribute name of XON/JSON named item in a map. */
	public final static String X_KEYATTR = "key";
	/** XML element name XON/JSON value (value value is in attribute. */
	public final static String X_ITEM = "item";
	/** XML attribute name of XON/JSON value (in the X_ITEM element). */
	public final static String X_VALUEATTR = "value";

////////////////////////////////////////////////////////////////////////////////
// names used in X-definition
////////////////////////////////////////////////////////////////////////////////
	/** This keyword used for the $script specification in X-definition. */
	public static final String SCRIPT_NAME = "$script";
	/** This keyword used for the $oneOf specification in X-definition. */
	public static final String ONEOF_NAME = "$oneOf";
	/** This keyword used for $any specification in X-definition. */
	public static final String ANY_NAME = "$any";
}