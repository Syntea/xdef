package cz.syntea.xdef;

/** Constants used by X-definition builder. The constants with the name with the
 * prefix "XDPROPERTY" are names of properties. The constants with the name with
 * the prefix XDPROPERTYVALUE are valid values of respective property.
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
	/** This property is not designed for public, it sets XML parser
	 * to ignore unresolved entities. */
	public static final String XDPROPERTY_IGNOREUNRESOLVEDENTITIES =
		"xdef.ignoreunresovedentities";
	/** This is not designed for public; it resets to normal use
	 * process of unresolved entities (default value). */
	public static final String XDPROPERTYVALUE_IGNOREUNRESOLVEDENTITIES_FALSE =
		"false";
	/** This is not designed for public; it resets ignore unresolved entities.*/
	public static final String XDPROPERTYVALUE_IGNOREUNRESOLVEDENTITIES_TRUE =
		"true";

	////////////////////////////////////////////////////////////////////////////
	/** X-definition version 2.0 ID. */
	public static final byte XD20_ID = 20;
	/** X-definition version 3.1 ID. */
	public static final byte XD31_ID = 31;

	////////////////////////////////////////////////////////////////////////////
	// Build version information. Do not modify following part of the code!
	// Note that the fields BUILD_VERSION, BUILD_DATE and JAVA_VERSION
	// are generated authomaticaly!
	////////////////////////////////////////////////////////////////////////////

	/** Build version of software (3.1.004.011). */
	public static final String BUILD_VERSION = "3.1.004.011";
	/** Date of build version (2018-10-31). */
	public static final String BUILD_DATE = "2018-10-31";
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
