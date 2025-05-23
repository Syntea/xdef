/**
 * Prepares building of X-definition project.
 * <UL>
 *
 * <li>
 * <h2><tt>Canonize</tt></h2>
 * provides canonization of sources and generates registered message tables.
 * </li>
 *
 * <li>
 * <h2><tt>CanonizeSource</tt></h2>
 * provides canonization of sources (in the Java source files. It replaces the leading 4 spaces with one tab
 * and removes all trailing white spaces.
 * </li>
 *
 * <li>
 * <h2><tt>GenReportTables</tt></h2>
 * generates registered message tables from org.xdef.msg repository.
 * </li>
 *
 * <li>
 * <h2><tt>JavaPreprocessor</tt></h2>
 * you can run preprocessor with switches and arguments on command line.
 * </li>
 *
 * <li>
 * <h2><tt>GenConstants</tt></h2>
 * defines constants used in canonizing.
 * </li>
 *
 * <li>
 * <h2><tt>GenPluginInfo</tt></h2>
 * generates XML document with the information about implemented methods used in GUI plugins.
 *  </li>
 *
 * </UL>
 */
package buildtools;