package org.xdef;

import java.io.File;
import java.io.IOException;
import org.xdef.impl.XDSourceInfo;
import org.xdef.sys.SDatetime;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;
import org.xdef.model.XMVariableTable;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import org.xdef.sys.ArrayReporter;

/** Provides the interface to object containing compiled code of set of Xdefinitions.
 * You can create from XDPool the XDDocument.
 * @author Vaclav Trojan
 */
public interface XDPool extends Serializable {
	/** Display mode true (i.e. display always). */
	public static final byte DISPLAY_TRUE = 2;
	/** Display mode on errors (i.e. of an error occurs). */
	public static final byte DISPLAY_ERRORS = 1;
	/** Display mode false (do not display). */
	public static final byte DISPLAY_FALSE = 0;
	/** Debug mode true (i.e. display always). */
	public static final byte DEBUG_TRUE = 1;
	/** Debug mode false. */
	public static final byte DEBUG_FALSE = 0;

	/** Get version information.
	 * @return version information.
	 */
	public String getVersionInfo();

	/** Check compatibility of this instance of XDPool with given version.
	 * @param version the version to be checked.
	 * @return true if this instance of XDPool is compatible with given version. Otherwise return false.
	 */
	public boolean chkCompatibility(final String version);

	/** Create new XDDocument.
	 * @param name name of Xdefinition (or null) or path to model.
	 * @return Created XDDocument.
	 */
	public XDDocument createXDDocument(String name);

	/** Create new XDDocument with default Xdefinition.
	 * @return Created XDDocument.
	 */
	public XDDocument createXDDocument();

	/** Check if exists the Xdefinition of given name.
	 * @param name the name of Xdefinition (or either null or empty string if Xdefinition without name
	 * is checked).
	 * @return true if and only if the Xdefinition of given name exists in the XDPool.
	 */
	boolean exists(String name);

	/** Get array with all XMDefinitions from this XDPool.
	 * @return array with all XMDefinitions from this XDPool.
	 */
	XMDefinition[] getXMDefinitions();

	/** Get array with all XMDefinitions from this XDPool.
	 * @return array with all XMDefinitions from this XDPool.
	 */
	public String[] getXMDefinitionNames();

	/** Get XMDefinition from this XDPool.
	 * @param name name of XMDecinition.
	 * @return specified XMDefinition from this XDPool.
	 */
	public XMDefinition getXMDefinition(String name);

	/** Get XMDefinition without name from this XDPool.
	 * @return XMDefinition without name from this XDPool (or return null).
	 */
	public XMDefinition getXMDefinition();

	/** Find XModel in XDPool.
	 * @param xdpos position of XModel in XDPool.
	 * @return XMNode representing model or null if model was nod found.
	 */
	public XMNode findModel(String xdpos);

	/** Display XDPool.
	 * @param out PrintStream where pool is printed.
	 */
	public void display(final PrintStream out);

	/** Display XDPool on System.out. */
	public void display();

	/** Display code of XDPool.
	 * @param out PrintStream where pool is printed.
	 */
	public void displayCode(final PrintStream out);

	/** Display code of XDPool on System.out. */
	public void displayCode();

	/** Display debugging information of XDPool.
	 * @param out PrintStream where pool is printed.
	 */
	public void displayDebugInfo(PrintStream out);

	/** Display debugging information of XDPool on System.out. */
	public void displayDebugInfo();

	/** Get debug information or null.
	 * @return debug information object.
	 */
	public XMDebugInfo getDebugInfo();

	/** Get table of global variables.
	 * @return table of global variables.
	 */
	public XMVariableTable getVariableTable();

	/** Get display mode.
	 * @return display mode.
	 */
	public byte getDisplayMode();

	/** Get switch if the parser allows XML XInclude.
	 * @return true if the parser allows XInclude.
	 */
	public boolean isResolveIncludes();

	/** Check if debug mode is set on.
	 * @return value of debug mode.
	 */
	public boolean isDebugMode();

	/** Check if unresolved externals will be ignored.
	 * @return true if unresolved externals will be ignored.
	 */
	public boolean isIgnoreUnresolvedExternals();

	/** Get the switch if XML parser will generate detailed location reports.
	 * @return the location details switch.
	 */
	public boolean isLocationsdetails();

	/** Get switch if the parser do not allow DOCTYPE.
	 * @return true if the parser do not allow DOCTYPE or return false
	 * if DOCTYPE is processed.
	 */
	public boolean isIllegalDoctype();

	/** Get switch if the parser will check warnings as errors.
	 * @return true if the parser checks warnings as errors.
	 */
	public boolean isChkWarnings();

	/** Get switch if the actullal reporter is cleared in the executed code of the 'onFalse', 'onIllegalAttr',
	 * 'onIllegalText', 'onEllegalElement' sections. Default value is 'true'.
	 * @return true if reporter will be cleared.
	 */
	public boolean isClearReports();

	/** Get list of XComponents.
	 * @return list of XComponents.
	 */
	public Map<String, String> getXComponents();

	/** Get list of XComponent binds.
	 * @return list of XComponent binds.
	 */
	public Map<String, String> getXComponentBinds();

	/** Get list of XComponent enumerations.
	 * @return list of XComponent enumerations.
	 */
	public Map<String, String> getXComponentEnums();

	/** Get default TimeZone.
	 * @return default TimeZone.
	 */
	public TimeZone getDefaultZone();

	/** Get minimum valid year of date.
	 * @return minimum valid year (Integer.MIN if not set).
	 */
	public int getMinYear();

	/** Get maximum valid year of date (or Integer.MIN if not set).
	 * @return maximum valid year (Integer.MIN if not set).
	 */
	public int getMaxYear();

	/** Get array of dates to be accepted out of interval minYear..maxYear.
	 * @return array with special values of valid dates.
	 */
	public SDatetime[] getSpecialDates();

	/** Get the object with the map of source items of compiled Xdefinitions and with editing information.
	 * @return object with the map of source items of compiled Xdefinitions and with editing information.
	 */
	public XDSourceInfo getXDSourceInfo();

	/** Get properties from XDPool.
	 * @return properties from XDPool.
	 */
	public Properties getProperties();

	/** Get debug editor class name.
	 * @return debug editor class name (if null the default debug editor will be used).
	 */
	public String getDebugEditor();

	/** Get class name of the editor of Xdefinition.
	 * @return class name of the editor of Xdefinition which will be used).
	 */
	public String getXdefEditor();

	/** Generate XComponent Java source classes from XDPool.
	 * @param fdir directory where write the source code. The file names will be constructed from %class
	 * statements as "className.java".
	 * @param charset the character set name or null (if null then it is used the system character set name).
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param suppressPrintWarnings switch suppress print of warnings.
	 * @return ArrayReporter with errors and warnings
	 * @throws IOException if an error occurs.
	 */
	public ArrayReporter genXComponent(final File fdir,
		final String charset,
		final boolean genJavadoc,
		final boolean suppressPrintWarnings) throws IOException;
}