/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: SConstants.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.sys;

/** Constants (build date and version, java version, XML library).
 * @author Vaclav Trojan
 */
public interface SConstants {
	// Note that build version and build date are authomaticaly generated!
	/** Build version of software (3.1.004.001). */
	public static final String BUILD_VERSION = "3.1.004.001";
	/** Date of build version (2018-06-14). */
	public static final String BUILD_DATE = "2018-06-14";
	/** Prefix of property names for setting of message table files. */
	public static final String REPORTTABLE_FILE = "syn.msg.";
	/** Name of property for setting language of messages. */
	public static final String REPORT_LANGUAGE = "syn.language";
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
