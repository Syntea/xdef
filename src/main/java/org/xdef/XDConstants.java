package org.xdef;

/** Important constants used by X-definition.
 * Note the constants with the name starting with prefix "XDPROPERTY"
 * are names of properties. To each such constant are also defined the constants
 * with the name starting with prefix "XDPROPERTYVALUE" and there are
 * defined the valid values of a respective property.
 * @author Vaclav Trojan
 */
public interface XDConstants {

	/** Property defines debug mode (default is false).*/
	public static final String XDPROPERTY_DEBUG = "xdef.debug";
	/** Value "true" of property "xdef.debug" */
	public static final String XDPROPERTYVALUE_DEBUG_TRUE = "true";
	/** Value "false" of property "xdef.debug" */
	public static final String XDPROPERTYVALUE_DEBUG_FALSE = "false";
	/** Value "showResult" of property "xdef.debug" */
	public static final String XDPROPERTYVALUE_DEBUG_SHOWRESULT = "showResult";
	/** Property defines stream used for debug output (default is stdOut). */
	public static final String XDPROPERTY_DEBUG_OUT = "xdef.debug.out";
	/** Property defines stream used for debug input (default is stdIn). */
	public static final String XDPROPERTY_DEBUG_IN = "xdef.debug.in";
	/** Property defines debug editor class name.*/
	public static final String XDPROPERTY_DEBUG_EDITOR = "xdef.debugeditor";
	/** Property defines X-definition editor class name.*/
	public static final String XDPROPERTY_XDEF_EDITOR = "xdef.editor";

	/** Property defines if XML DOCTYPE is permitted (default is "true"). */
	public static final String XDPROPERTY_DOCTYPE = "xdef.doctype";
	/** Value "false" of property "xdef.doctype" */
	public static final String XDPROPERTYVALUE_DOCTYPE_FALSE = "false";
	/** Value "true" of property "xdef.doctype" */
	public static final String XDPROPERTYVALUE_DOCTYPE_TRUE = "true";

	/** Set the XML parser will generate detailed location in reports. */
	public static final String XDPROPERTY_LOCATIONDETAILS =
		"xdef.locationsdetails";
	/** Value "false" of property "xdef.locationsdetails" (default). */
	public static final String XDPROPERTYVALUE_LOCATIONDETAILS_FALSE = "false";
	/** Value "true" of property "xdef.locationsdetails" */
	public static final String XDPROPERTYVALUE_LOCATIONDETAILS_TRUE = "true";

	/** Property defines if XML include is permitted (default is "true"). */
	public static final String XDPROPERTY_XINCLUDE = "xdef.xinclude";
	/** Value "false" of property "xdef.xinclude" */
	public static final String XDPROPERTYVALUE_XINCLUDE_FALSE = "false";
	/** Value "true" of property "xdef.xinclude (default)." */
	public static final String XDPROPERTYVALUE_XINCLUDE_TRUE = "true";

	/** Property defines if read from environmental variables is permitted
	 *  (default is "true"). */
	public static final String XDPROPERTY_ENV_GET = "xdef.envGet";
	/** Value "true" of property "xdef.envGet" */
	public static final String XDPROPERTYVALUE_ENV_GET_TRUE = "true";
	/** Value "false" of property "xdef.envGet" */
	public static final String XDPROPERTYVALUE_ENV_GET_FALSE = "false";

	/** Property warning messages are checked {thrown} (default is "false")*/
	public static final String XDPROPERTY_WARNINGS = "xdef.warnings";
	/** Value "true" of property "xdef.warnings" */
	public static final String XDPROPERTYVALUE_WARNINGS_TRUE = "true";
	/** Value "false" of property "xdef.debug" */
	public static final String XDPROPERTYVALUE_WARNINGS_FALSE = "false";

	/** Property defines debug mode (default is false).*/
	public static final String XDPROPERTY_DISPLAY = "xdef.display";
	/** Value "true" of property "xdef.display" */
	public static final String XDPROPERTYVALUE_DISPLAY_TRUE = "true";
	/** Value "errors" of property "xdef.display" */
	public static final String XDPROPERTYVALUE_DISPLAY_ERRORS = "errors";
	/** Value "false" of property "xdef.display" */
	public static final String XDPROPERTYVALUE_DISPLAY_FALSE = "false";

	/** Property defines validation of attribute names (default is false). */
	public static final String XDPROPERTY_VALIDATE = "xdef.validate";
	/** Value "true" of property "validate.debug" */
	public static final String XDPROPERTYVALUE_VALIDATE_TRUE = "true";
	/** Value "false" of property "validate.debug" */
	public static final String XDPROPERTYVALUE_VALIDATE_FALSE = "false";

	/** Property defines minimal valid year of date (default is no minimum). */
	public static final String XDPROPERTY_MINYEAR = "xdef.minyear";
	/** Property defines maximal valid year of date (default is no maximum). */
	public static final String XDPROPERTY_MAXYEAR = "xdef.maxyear";
	/** Property defines legal values of dates if year is out of range.*/
	public static final String XDPROPERTY_SPECDATES = "xdef.specdates";

	/** Property defines if unresolved external methods are reported (used
	 * for syntax checking of X-definition (default is "false"). */
	public static final String XDPROPERTY_IGNORE_UNDEF_EXT =
		"xdef.ignoreUnresolvedExternals";
	/** Value "true" of property "xdef.ignoreUnresolvedExternals" */
	public static final String XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE = "true";
	/** Value "false" of property "xdef.ignoreUnresolvedExternals" */
	public static final String XDPROPERTYVALUE_IGNORE_UNDEF_EXT_FALSE = "false";

	/** Prefix of property names for setting of message table files. */
	public static final String XDPROPERTY_MESSAGES = "xdef.msg.";
	/** Name of property for setting language of messages. */
	public static final String XDPROPERTY_MSGLANGUAGE = "xdef.language";

////////////////////////////////////////////////////////////////////////////////
// XML constants
////////////////////////////////////////////////////////////////////////////////
	
	/** URI of xmlns:xlink */
	public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
	/** URI of w3c Xinclude */
	public static final String XINCLUDE_NS_URI =
		"http://www.w3.org/2001/XInclude";
	/** URI of JSON/XML conversion nodes. */
	public static final String JSON_NS_URI = "http://www.syntea.cz/json/1.0";
	/** Recommended namespace prefix used for JSON/XML conversion nodes. */
	public static final String JSON_NS_PREFIX = "js";
	/** Recommended namespace prefix used for X-definition nodes. */
	public static final String XDEF_NS_PREFIX = "xd";
	/** URI of X-definition 2.0. */
	public static final String XDEF20_NS_URI = "http://www.syntea.cz/xdef/2.0";
	/** URI of X-definition 3.1. */
	public static final String XDEF31_NS_URI = "http://www.syntea.cz/xdef/3.1";
	/** URI of X-definition 3.2. */
	public static final String XDEF32_NS_URI = "http://www.org.xdef/3.2";
	/** The namespace URI for X-definition instance */
	public static final String XDEF_INSTANCE_NS_URI =
		"http://www.syntea.cz/xdef/instance";
	/** The namespace URI for X-definition instance */
	public static final String XDEF32_INSTANCE_NS_URI = 
		"http://www.org.xdef/instance/3.2";

////////////////////////////////////////////////////////////////////////////////
// Build version information. Do not modify following part of the code!
// Note that the fields BUILD_VERSION, BUILD_DATE and JAVA_VERSION
// are generated authomaticaly!
////////////////////////////////////////////////////////////////////////////////

	/** Build version of software (3.2.001.001). */
	public static final String BUILD_VERSION = "3.2.001.001";
	/** Date of build version (2018-11-14). */
	public static final String BUILD_DATE = "2018-11-14";
	/** Java version of compiler when the X-definition code was compiled. */
/*#if JAVA_1.6*/
	public static final String JAVA_VERSION = "Java 1.6";
/*#elseif JAVA_1.7*#/
	public static final String JAVA_VERSION = "Java 1.7";
/*#elseif JAVA_1.8*#/
	public static final String JAVA_VERSION = "Java 1.8";
/*#elseif JAVA_1.9*#/
	public static final String JAVA_VERSION = "Java 1.9";
/*#elseif JAVA_1.10*#/
	public static final String JAVA_VERSION = "Java 1.10";
/*#end*/


}
