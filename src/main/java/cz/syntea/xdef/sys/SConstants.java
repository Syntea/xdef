package cz.syntea.xdef.sys;

/** Constants (build date and version, java version, XML library).
 * @author Vaclav Trojan
 */
public interface SConstants {
	// Note that build version and build date are authomaticaly generated!
	/** Build version of software (3.1.004.007). */
	public static final String BUILD_VERSION = "3.1.004.007";
	/** Date of build version (2018-09-17). */
	public static final String BUILD_DATE = "2018-09-17";
	/** Prefix of property names for setting of message table files. */
	public static final String XDPROPERTY_MSGTABLE = "xdef.msg.";
	/** Name of property for setting language of messages. */
	public static final String XDPROPERTY_MSGLANGUAGE = "xdef.language";
	/** Compiler Java version. */
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
