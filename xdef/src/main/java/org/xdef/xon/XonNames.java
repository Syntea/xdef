package org.xdef.xon;

import javax.xml.namespace.QName;
import org.xdef.XDConstants;

/** Names used in XON/JSON tools.
 * @author Vaclav Trojan
 */
public interface XonNames {
////////////////////////////////////////////////////////////////////////////////
// names used in XON DX and W3C format
////////////////////////////////////////////////////////////////////////////////
	/** XML element name of XON/JSON map. */
	public final static String X_MAP = "map";
	/** XML attribute name of XON/JSON named item in a map. */
	public final static String X_KEYATTR = "key";
	/** XML element name of XON/JSON array. */
	public final static String X_ARRAY = "array";
	/** XML element name XON/JSON value (value is in attribute. */
	public final static String X_ITEM = "item";
	/** XML attribute name of XON/JSON value (in the X_ITEM element). */
	public final static String X_VALATTR = "val";

	/** QName of of XON/JSON map. */
	public final static QName Q_MAP =
		new QName(XDConstants.XON_NS_URI_W, X_MAP);
	/** QName of of XON/JSON array. */
	public final static QName Q_ARRAY =
		new QName(XDConstants.XON_NS_URI_W, X_ARRAY);
	/** QName of of XON/JSON item. */
	public final static QName Q_ITEM =
		new QName(XDConstants.XON_NS_URI_W, X_ITEM);

////////////////////////////////////////////////////////////////////////////////
// Names of XON directives
////////////////////////////////////////////////////////////////////////////////
	/** Encoding directive {used only in first line of XON source data}. */
	public static final String ENCODING_DIRECTIVE = "%encoding";
	/** Directive with script in XON model. */
	public static final String SCRIPT_DIRECTIVE = "%script";
	/** Directive oneOf in XON model. */
	public static final String ONEOF_DIRECTIVE = "%oneOf";
	/** Directive any name in XON map model. */
	public static final String ANY_NAME = "%anyName";
	/** Directive any object in XON model. */
	public static final String ANY_OBJ = "%anyObj";
}