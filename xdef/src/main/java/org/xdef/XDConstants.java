package org.xdef;

import org.xdef.sys.config.PomInfo;

/** Important constants used by Xdefinition.
 * Note that constants with a name starting with the prefix "XDPROPERTY" are property names.  There are also
 * constants defined for each constant with a name starting with the prefix "XDPROPERTYVALUE", and there are
 * constants with a name starting with the prefix "XDPROPERTYVALUE" to which the corresponding property value
 * options are defined.
  @author Vaclav Trojan
 */
public interface XDConstants {
////////////////////////////////////////////////////////////////////////////////
// Properties
////////////////////////////////////////////////////////////////////////////////
	/** Property defines debug mode (default is false).*/
	public static final String XDPROPERTY_DEBUG = "xdef_debug";
	/** Value "true" of property "xdef_debug" */
	public static final String XDPROPERTYVALUE_DEBUG_TRUE = "true";
	/** Value "false" of property "xdef_debug" */
	public static final String XDPROPERTYVALUE_DEBUG_FALSE = "false";

	/** Property defines if XML DOCTYPE is permitted (default is "true"). */
	public static final String XDPROPERTY_DOCTYPE = "xdef_doctype";
	/** Value "false" of property "xdef_doctype" */
	public static final String XDPROPERTYVALUE_DOCTYPE_FALSE = "false";
	/** Value "true" of property "xdef_doctype" */
	public static final String XDPROPERTYVALUE_DOCTYPE_TRUE = "true";

	/** Set the XML parser will generate detailed location in reports. */
	public static final String XDPROPERTY_LOCATIONDETAILS = "xdef_locationsdetails";
	/** Value "false" of property "xdef_locationsdetails" (default). */
	public static final String XDPROPERTYVALUE_LOCATIONDETAILS_FALSE = "false";
	/** Value "true" of property "xdef_locationsdetails" */
	public static final String XDPROPERTYVALUE_LOCATIONDETAILS_TRUE = "true";

	/** Property defines if XML include is permitted (default is "true"). */
	public static final String XDPROPERTY_XINCLUDE = "xdef_xinclude";
	/** Value "false" of property "xdef_xinclude" */
	public static final String XDPROPERTYVALUE_XINCLUDE_FALSE = "false";
	/** Value "true" of property "xdef_xinclude (default)." */
	public static final String XDPROPERTYVALUE_XINCLUDE_TRUE = "true";

	/** Property warning messages are generated and checked, default is "true"*/
	public static final String XDPROPERTY_WARNINGS = "xdef_warnings";
	/** Value "true" of property "xdef_warnings" - warnings are generated- */
	public static final String XDPROPERTYVALUE_WARNINGS_TRUE = "true";
	/** Value "false" of property "xdef_warnings" - warnings not generated.*/
	public static final String XDPROPERTYVALUE_WARNINGS_FALSE = "false";

	/** Property defines if the actual reporter is cleared in the invoked
	 * action 'onFalse', 'onIllegalAttr', 'onIllegalText',
	 * 'onEllegalElement'. Default value is 'true'*/
	public static final String XDPROPERTY_CLEAR_REPORTS = "xdef_clearReports";
	/** Value "true" of property "xdef_clearReports" */
	public static final String XDPROPERTYVALUE_CLEAR_REPORTS_TRUE = "true";
	/** Value "false" of property "xdef_clearReports" */
	public static final String XDPROPERTYVALUE_CLEAR_REPORTS_FALSE = "false";

	/** Property defines if unresolved external methods are reported (used
	 * for syntax checking of Xdefinition (default is "false"). */
	public static final String XDPROPERTY_IGNORE_UNDEF_EXT = "xdef_ignoreUnresolvedExternals";
	/** Value "true" of property "xdef_ignoreUnresolvedExternals" */
	public static final String XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE = "true";
	/** Value "false" of property "xdef_ignoreUnresolvedExternals" */
	public static final String XDPROPERTYVALUE_IGNORE_UNDEF_EXT_FALSE = "false";

	/** Property defines debug mode (default is false).*/
	public static final String XDPROPERTY_DISPLAY = "xdef_display";
	/** Value "true" of property "xdef_display" */
	public static final String XDPROPERTYVALUE_DISPLAY_TRUE = "true";
	/** Value "false" of property "xdef_display" */
	public static final String XDPROPERTYVALUE_DISPLAY_FALSE = "false";
	/** Value "errors" of property "xdef_display" */
	public static final String XDPROPERTYVALUE_DISPLAY_ERRORS = "errors";

	/** Property defines minimal valid year of date (default is no minimum). */
	public static final String XDPROPERTY_MINYEAR = "xdef_minyear";
	/** Property defines maximal valid year of date (default is no maximum). */
	public static final String XDPROPERTY_MAXYEAR = "xdef_maxyear";
	/** Property defines legal values of dates if year is out of range.*/
	public static final String XDPROPERTY_SPECDATES = "xdef_specdates";
	/** Property defines code table name for parsed string values (default is do not check).*/
	public static final String XDPROPERTY_STRING_CODES = "xdef_string_code";
	/** Property defines default time zone.*/
	public static final String XDPROPERTY_DEFAULTZONE = "xdef_defaultZone";

	/** Prefix of property names for setting of message table files. */
	public static final String XDPROPERTY_MESSAGES = "xdef_msg_";
	/** Name of property for setting language of messages. */
	public static final String XDPROPERTY_MSGLANGUAGE = "xdef_language";

	/** Property defines debug editor class name.*/
	public static final String XDPROPERTY_DEBUG_EDITOR = "xdef_debugeditor";
	/** Property defines Xdefinition editor class name.*/
	public static final String XDPROPERTY_XDEF_EDITOR = "xdef_editor";
	/** Property defines Xdefinition external editor class name.*/
	public static final String XDPROPERTY_XDEF_EXTEDITOR = "xdef_exteditor";

	/** Property defines stream used for debug output (default is stdOut). */
	public static final String XDPROPERTY_DEBUG_OUT = "xdef_debug_out";
	/** Property defines stream used for debug input (default is stdIn). */
	public static final String XDPROPERTY_DEBUG_IN = "xdef_debug_in";

//////////////////////////////////////////////////////////////////////////////////
// XML constants
////////////////////////////////////////////////////////////////////////////////
	/** URI of w3c XLink */
	public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
	/** URI of w3c XInclude */
	public static final String XINCLUDE_NS_URI = "http://www.w3.org/2001/XInclude";

////////////////////////////////////////////////////////////////////////////////
// Xdefinition
////////////////////////////////////////////////////////////////////////////////
	/** Recommended namespace prefix used for Xdefinition nodes. */
	public static final String XDEF_NS_PREFIX = "xd";
	/** URI of Xdefinition 3.1. */
	public static final String XDEF31_NS_URI = "http://www.syntea.cz/xdef/3.1";
	/** URI of Xdefinition 3.2. */
	public static final String XDEF32_NS_URI = "http://www.xdef.org/xdef/3.2";
	/** URI of Xdefinition 4.0. */
	public static final String XDEF40_NS_URI = "http://www.xdef.org/xdef/4.0";
	/** URI of Xdefinition 4.1. */
	public static final String XDEF41_NS_URI = "http://www.xdef.org/xdef/4.1";
	/** URI of Xdefinition 4.1. */
	public static final String XDEF42_NS_URI = "http://www.xdef.org/xdef/4.2";

	/** The namespace URI for Xdefinition instance. */
	public static final String XDEF_INSTANCE_NS_URI = "http://www.xdef.org/xdef/instance";

////////////////////////////////////////////////////////////////////////////////
// XON/JSON/INI
////////////////////////////////////////////////////////////////////////////////
	/** Recommended namespace prefix used for XON/JSON/INI XML format. */
	public static final String XON_NS_PREFIX = "jx";
	/** URI of XON/JSON/INI XML Xdefinition conversion. */
	public static final String XON_NS_URI_XD = "http://www.xdef.org/xon/4.0";
	/** URI of XON/JSON/INI XML W3C like conversion*/
	public static final String XON_NS_URI_W="http://www.xdef.org/xon/4.0/w";

////////////////////////////////////////////////////////////////////////////////
// Build version information.
////////////////////////////////////////////////////////////////////////////////
	/** Build version of software build from pom.xml */
	public static final String BUILD_VERSION = PomInfo.POMINFO.getVersion();
	/** Datetime of build version from pom.xml */
	public static final String BUILD_DATETIME = PomInfo.POMINFO.getBuildTimestamp();

////////////////////////////////////////////////////////////////////////////////
// Platform dependent constants.
////////////////////////////////////////////////////////////////////////////////
	/** Platform-dependent line separator (newline characters: LF, CR LF, etc.*/
	public static final String LINE_SEPARATOR = String.format("%n");
}