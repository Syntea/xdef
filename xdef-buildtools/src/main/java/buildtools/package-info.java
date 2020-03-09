/**
 * Prepares building of X-definition project.
 * <UL>
 * <li>
 * <h2><tt>GenXComponent</tt></h2>
 * generates X-components for the tests (into the test resources).
 * </li>
 *
 * <li>
 * <h2><tt>CanonizeSource</tt></h2>
 * provides canonization of sources (in the Java source files it replaces the
 * leading spaces with tabs - 4 spaces are replaced by one tab)
 * and it removes all trailing white spaces.
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
 *  </li>
 * </UL>
 */
package buildtools;