package org.xdef;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.xdef.proc.XXNode;
import org.xdef.proc.XXElement;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import org.xdef.component.XComponent;
import org.xdef.model.XMElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.sys.ReportWriter;
import javax.xml.namespace.QName;
import org.xdef.impl.XDefinition;

/** Provides processing of given X-definition. For processing of X-definition you must create and instance
 * of XDDocument created from XDPool with given X-definition in which is defined root (starting point)
 * for next processing. Before starting of process you can set parameters of processing (variables,
 * properties, standard input and output streams, user objects). The document can be used for validation and
 * processing of the input XML data ("xparse" methods) or for construction of a XML object (methods
 * "xcreate"). Note that XDDocument is the root node of processing and it is the extension of the interface
 * {@link org.xdef.proc.XXNode}. You can read of or set to variables of a XDPool by methods of
 * {@link org.xdef.proc.XXNode}:
 * <p>{@link org.xdef.proc.XXNode#getVariable(String)},
 * {@link org.xdef.proc.XXNode#setVariable(String, Object)},
 * {@link org.xdef.proc.XXNode#setUserObject(Object)},
 * {@link org.xdef.proc.XXNode#getUserObject()},
 * {@link org.xdef.proc.XXNode#getUserObject()},
 * {@link org.xdef.proc.XXNode#setXDContext(org.w3c.dom.Node node)}.
 * {@link org.xdef.proc.XXNode#getXDContext()}.
 * <p>Typical example of validation:
 * <pre><code>
 * //get instance of XDDocument with X-definition given by name
 * XDDocument xDoc = xp.createXDDocument(name);
 * ArrayReporter reporter = new ArrayReporter(); // here will be written errors
 * ... set variables if necessary - see {@link org.xdef.proc.XXNode}
 * Element el = xd.xparse(sourceXml, reporter); //validate and process data
 * //now we have root element of parsed source data errors in variable el
 * //and list of errors in reporter
 * //test if an error was reported
 * if (reporter.errorWarnings()) {//error or warning reported?
 *     reporter.getReportReader().printReports(System.err);
 * } else {//no errors
 *     ... get variables if necessary - see {@link org.xdef.proc.XXNode}
 *     ....
 * }</code></pre>
 * <p>Typical example of construction:
 * <pre><code>
 * //get instance of XDDocument with X-definition given by name
 * XDDocument xDoc = xp.createXDDocument(name);
 * ArrayReporter reporter = new ArrayReporter(); // here will be written errors
 * ... set data source see {@link org.xdef.proc.XXNode}
 * //construct required element.
 * Element el = xd.xcreate(nsuri, //namespace of required model or null
 *   name, // name of required model (in given X-definition)
 *   reporter);
 * </code></pre>
 * @author Vaclav Trojan
 */
public interface XDDocument extends XXNode {

	/** Set properties.
	 * @param props Properties.
	 */
	public void setProperties(Properties props);

	/** Set property. If properties are null the new Properties object will be created.
	 * @param key name of property.
	 * @param value value of property or null. If the value is null the property is removed.
	 */
	public void setProperty(final String key, final String value);

	/** Get properties.
	 * @return assigned Properties.
	 */
	public Properties getProperties();

	/** Check if create mode is running.
	 * @return true if and only if create mode is running.
	 */
	public boolean isCreateMode();

	/** Get document.
	 * @return The Document object (may be null).
	 */
	public Document getDocument();

	/** Set root model for this document model.
	 * @param xmodel model to be set.
	 */
	public void setRootModel(XMElement xmodel);

	/** get StdOut output.
	 * @return std out XDOutput.
	 */
	public XDOutput getStdOut();

	/** get StdErr output.
	 * @return std err XDOutput.
	 */
	public XDOutput getStdErr();

	/** get StdIn intput.
	 * @return std in XDInput.
	 */
	public XDInput getStdIn();

	/** Set XML writer.
	 * @param out output stream.
	 * @param encoding encoding of output.
	 * @param writeHeader if true full document is written, otherwise only root element.
	 * @throws IOException if an error occurs.
	 */
	public void setStreamWriter(OutputStream out, String encoding, boolean writeHeader) throws IOException;

	/** Set XML writer.
	 * @param out stream writer.
	 * @param encoding encoding of output.
	 * @param writeDocumentHeader if true full document is written, otherwise only root element.
	 */
	public void setStreamWriter(Writer out, String encoding, boolean writeDocumentHeader);

	/** Set XML writer.
	 * @param xmlWriter XML writer.
	 */
	public void setStreamWriter(XDXmlOutStream xmlWriter);

	/** Set standard output stream.
	 * @param out PrintStream object.
	 */
	public void setStdOut(PrintStream out);

	/** Set standard output stream.
	 * @param out PrintStream object.
	 */
	public void setStdOut(Writer out);

	/** Set standard input stream.
	 * @param in InputStream object.
	 */
	public void setStdIn(InputStream in);

	/** Set standard output stream.
	 * @param out XDOutput object.
	 */
	public void setStdOut(XDOutput out);

	/** Set standard input stream.
	 * @param in XDInput object.
	 */
	public void setStdIn(XDInput in);

	/** Set debugger.
	 * @param debugger the debugger.
	 */
	public void setDebugger(XDDebug debugger);

	/** Get debugger.
	 * @return the debugger.
	 */
	public XDDebug getDebugger();

	/** Set debugging mode.
	 * @param debug debugging mode.
	 */
	public void setDebug(boolean debug);

	/** Check debugging mode is set ON.
	 * @return value of debugging mode.
	 */
	public boolean isDebug();

////////////////////////////////////////////////////////////////////////////////
// Generation of XML objects
////////////////////////////////////////////////////////////////////////////////

	/** Create root check element with namespace with given name.
	 * @param nsURI Namespace URI of the element.
	 * @param qname Qualified name of the element.
	 * @param checkRoot If value of this argument is true then the root element is checked* against the root
	 * list, otherwise it is found as XElement on the base level.
	 * @return ChkElement object.
	 */
	public XXElement prepareRootXXElementNS(String nsURI, String qname, boolean checkRoot);

	/** Create root check element with given name.
	 * @param name Tag name of the root element.
	 * @param checkRoot If value of this argument is true then the root element is checked against the root
	 * list, otherwise it is found as XElement on the base level.
	 * @return ChkElement object.
	 */
	public XXElement prepareRootXXElement(String name, boolean checkRoot);

	/** Get implementation properties of X-definition.
	 * @return the implementation properties of X-definition.
	 */
	public Properties getImplProperties();

	/** Get implementation property of X-definition.
	 * @param name The name of property.
	 * @return the value of implementation property from root X-definition.
	 */
	public String getImplProperty(String name);

	/** Check value of datetime. Check if the year of date in the interval (YEAR_MIN .. YEAR_MAX) or the value
	 * of date is one of UNDEF_YEAR[] values.
	 * @param date value to be checked.
	 * @return true if date is legal.
	 */
	public boolean isLegalDate(SDatetime date);

	/** Get minimum valid year of date.
	 * @return minimum valid year (Integer.MIN if not set).
	 */
	public int getMinYear();

	/** Set minimum valid year of date (or Integer.MIN is not set).
	 * @param x minimum valid year.
	 */
	public void setMinYear(int x);

	/** Get maximum valid year of date (or Integer.MIN if not set).
	 * @return maximum valid year (Integer.MIN if not set).
	 */
	public int getMaxYear();

	/** Set maximum valid year of date (or Integer.MIN is not set).
	 * @param x maximum valid year.
	 */
	public void setMaxYear(int x);

	/** Get array of dates to be accepted out of interval minYear..maxYear.
	 * @return array with special values of valid dates.
	 */
	public SDatetime[] getSpecialDates();

	/** Set array of dates to be accepted out of interval minYear..maxYear.
	 * @param x array with special values of valid dates.
	 */
	public void setSpecialDates(SDatetime[] x);

	/** Set if year of date will be checked for interval minYear..maxYear.
	 * @param x if true year of date will be checked.
	 */
	public void checkDateLegal(boolean x);

	/** Print reports to PrintStream.
	 * @param out PrintStream where reports are printed.
	 */
	public void printReports(PrintStream out);

	/** Get actual source language used for lexicon.
	 * @return string with actual language or return null if lexicon is not specified  or if language is
	 * not specified.
	 */
	public String getLexiconLanguage();

	/** Set actual source language used for lexicon.
	 * @param language string with language or null.
	 * @throws SRuntimeException if lexicon not specified or if language is not specified.
	 */
	public void setLexiconLanguage(String language) throws SRuntimeException;

	/** Translate the input element from the source language to the destination language according to lexicon.
	 * @param elem path to the source element or the string with element.
	 * @param sourceLanguage name of source language.
	 * @param destLanguage name of destination language.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return element converted to the destination language.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Element xtranslate(String elem,
		String sourceLanguage,
		String destLanguage,
		ReportWriter reporter) throws SRuntimeException;

	/** Translate the input element from the source language to the destination language according to lexicon.
	 * @param elem the element in the source language.
	 * @param sourceLanguage name of source language.
	 * @param destLanguage name of destination language.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return element converted to the destination language.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Element xtranslate(Element elem,
		String sourceLanguage,
		String destLanguage,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse a string with a type declared in X-definition.
	 * @param typeName name of type in X-definition.
	 * @param data string with data to be parsed.
	 * @return XDParseResult object with parsed data.
	 */
	public XDParseResult parseXDType(String typeName, String data);

////////////////////////////////////////////////////////////////////////////////
	/** Parse and process CSV data and return processed object. If separator is comma header is not skipped.
	 * @param data string with pathname of CSV file or Reader with CSV data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return List with processed data.
	 * @throws RuntimeException if reporter is null and an error is reported.
	 */
	public List<Object> cparse(Object data, ReportWriter reporter) throws RuntimeException;

	/** Parse and process CSV data and return processed object. If separator is comma header is not skipped.
	 * @param data Reader with CSV data.
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return List with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public List<Object> cparse(Reader data, String sourceId, ReportWriter reporter) throws SRuntimeException;

	/** Parse and process CSV data and return processed object.
	 * @param data reader with CSV data
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return List with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public List<Object> cparse(Reader data,
		char separator,
		boolean skipHeader,
		String sourceId,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process INI/Properties data and return processed object.
	 * @param data string with INI/Propertie data or pathname or file or a stream INI/Propertie source data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return Map with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Map<String, Object> iparse(Object data, ReportWriter reporter) throws RuntimeException;

	/** Parse source INI/Properties and return XComponent as result.
	 * @param data string with INI/Propertie data or pathname or file or a stream INI/Propertie source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent iparseXComponent(Object data, Class<?> xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source INI/Properties and return XComponent as result.
	 * @param ini string with pathname of INI/Properties file or INI/Properties source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent iparseXComponent(Object ini, Class<?> xClass, String sourceId, ReportWriter reporter)
		throws SRuntimeException;

	/** Validate and process INI/Properties data and return processed object.
	 * @param data string with INI/Propertie data or pathname or file or a stream INI/Propertie source data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return Map with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Map<String, Object> ivalidate(Object data, ReportWriter reporter) throws SRuntimeException;

	/** Run create XON/JSON according to the X-definition JSON model.
	 * @param name name of XON/JSON model.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return Object with the constructed XON/JSON data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Object jcreate(String name, ReportWriter reporter) throws SRuntimeException;

	/** Create XComponent from XON/JSON according to the X-definition model. NOTE this method is experimental.
	 * @param name the name of required model.
	 * @param xClass XComponent class (if <i>null</i>, then XComponent class.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent jcreateXComponent(String name, Class xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse and process XON/JSON data and return processed XON object.
	 * @param data string with JSON/XON data or pathname or file or a stream JSON/XON source data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Object jparse(Object data, ReportWriter reporter) throws SRuntimeException;

	/** Parse source XON/JSON and return XComponent as result.
	 * @param data string with pathname of XON/JSON source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent jparseXComponent(Object data, Class<?> xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source JSON/XON and return XComponent as result.
	 * @param data string with pathname of XON/JSON source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent jparseXComponent(Object data, Class<?> xClass, String sourceId, ReportWriter reporter)
		throws SRuntimeException;

	/** Validate and process JSON/XON data and return processed XON object.
	 * @param data JSON/XON object or XML representation of object to validate.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Object jvalidate(Object data, ReportWriter reporter) throws SRuntimeException;

	/** Run create mode - create element according to the X-definition model.
	 * @param name the name of model of required element.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Element xcreate(String name, ReportWriter reporter) throws SRuntimeException;

	/** Run create mode - create element according to the X-definition model. If the parameter nsUri is not
	 * null then its assigned the model with given namespaceURI; in this case the parameter name may be
	 * qualified with a prefix.
	 * @param nsUri the namespace URI of result element (may be null).
	 * @param name the name of model of required element (may contain prefix).
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Element xcreate(String nsUri, String name, ReportWriter reporter) throws SRuntimeException;

	/** Run create mode - create element according to the X-definition model.
	 * @param qname the QName of model of required element.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Element xcreate(QName qname, ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source and return org.w3c.dom.Element.
	 * @param data string with XML data or pathname or file or a stream XML source data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Element xparse(Object data, ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source and return org.w3c.dom.Element.
	 * @param data string with XML data or pathname or file or a stream XML source data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @param sourceId name of source or null.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Element xparse(Object data, String sourceId, ReportWriter reporter) throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param data string with pathname of XML file or XML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent xparseXComponent(Object data, Class<?> xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param data string with pathname of XML file or a stream with XML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent xparseXComponent(Object data, Class<?> xClass, String sourceId, ReportWriter reporter)
		throws SRuntimeException;

	/** Run create mode - create element according to the X-definition model. If the parameter nsUri is not
	 * null then its assigned the model* with given namespaceURI (in this case the parameter qname may be
	 * qualified with a prefix).
	 * @param nsUri the namespace URI of result element (may be <i>null</i>).
	 * @param name the name of model of required element (may contain prefix).
	 * @param xClass XComponent class (if <i>null</i>, then XComponent class
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent xcreateXComponent(String nsUri, String name, Class xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Run create mode - create XComponent according to the X-definition model.
	 * @param name the name of model of required element.
	 * @param xClass XComponent class (if <i>null</i>, then XComponent class.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent xcreateXComponent(String name, Class xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Run create mode - create XComponent according to the X-definition model.
	 * @param qname the QName of model of required element.
	 * @param xClass XComponent class (if <i>null</i>, then XComponent class.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is <i>null</i> and an error was reported.
	 */
	public XComponent xcreateXComponent(QName qname, Class xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Run create XAML according to the X-definition XON model.
	 * @param name name of XON model.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Object ycreate(String name, ReportWriter reporter) throws SRuntimeException;

	/** Parse and process YAML data and return processed XON object.
	 * @param data string with YAML data or pathname or file or a stream YAML source data.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public Object yparse(Object data, ReportWriter reporter) throws SRuntimeException;

	/** Parse source YAML and return XComponent as result.
	 * @param data string with pathname of YAML file or YAML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent yparseXComponent(Object data, Class<?> xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source YAML and return XComponent as result.
	 * @param data string with pathname of YAML file or YAML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	public XComponent yparseXComponent(Object data, Class<?> xClass, String sourceId, ReportWriter reporter)
		throws SRuntimeException;

////////////////////////////////////////////////////////////////////////////////
	/** Set w3c.dom.Document to this object.
	 * @param doc w3c.dom.Document to set.
	 */
	public void setDocument(final Document doc);

	/** Set XDefinition to this object.
	 * @param xdef XDefinition to be set.
	 */
	public void setXDefinition(final XDefinition xdef);

////////////////////////////////////////////////////////////////////////////////
// deprecated
////////////////////////////////////////////////////////////////////////////////
	/** Parse source XML and return XComponent as result.
	 * @deprecated please use xparseXComponent instead
	 * @param data string with pathname of XML file or XML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	@Deprecated
	public XComponent parseXComponent(Object data, Class<?> xClass, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @deprecated please use xparseXComponent instead
	 * @param data string with pathname of XML file or XML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer. If it is null and error reports occurs then throw a SRuntimeException.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	@Deprecated
	public XComponent parseXComponent(Object data, Class<?> xClass, String sourceId, ReportWriter reporter)
		throws SRuntimeException;
}
