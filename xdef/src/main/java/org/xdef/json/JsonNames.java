package org.xdef.json;

/** Names used in conversion s of JSON/XON to/from XML
 * @author Vaclav Trojan
 */
public interface JsonNames {
	/** XML element name of JSON map. */
	public final static String J_MAP = "map";
	/** XML element name of JSON array. */
	public final static String J_ARRAY = "array";
	/** XML attribute name of JSON named item in a map. */
	public final static String J_KEYATTR = "key";
	/** XML element name JSON value (value value is in attribute. */
	public final static String J_ITEM = "item";
	/** XML attribute name of JSON value (in a J_ITEM element). */
	public final static String J_VALUEATTR = "value";

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