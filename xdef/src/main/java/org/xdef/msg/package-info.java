/**
 * This package contains classes for registered reports of X-definitions.
 * <p>
 * The source text of messages is a property file. The file name starts with
 * prefix of messages followed by character "_" and by three letters of the
 * ISO 639-2 language ID. Other files in this package are Java files and they
 * are generated automatically - do not modify them!
 * See	{@link org.xdef.sys.RegisterReportTables}.
 * </p>
 * <p>
 * For each prefix of a report is in this package the interface Java file,
 * containing values of registered reports. The name of this interface is equal
 * to a prefix. For any language variant of messages there is also generated
 * the Java file with the name starting with the prefix of messages and
 * extended by "_" and by three letters of language code (same as in the XML
 * file). E.g. for the prefix "SYS" exists the interface
 * {@link org.xdef.msg.SYS}.
 * </p>
 */
package org.xdef.msg;
