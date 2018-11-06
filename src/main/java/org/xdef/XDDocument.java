package org.xdef;

import org.xdef.proc.XXNode;
import org.xdef.proc.XXElement;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import org.xdef.component.XComponent;
import org.xdef.model.XMElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.ReportWriter;
import javax.xml.namespace.QName;

/** Provides processing of given X-definition. For processing of X-definition you
 * must create and instance of XDDocument created from XDPool with given
 * X-definition in which is defined root (starting point) for next processing.
 * Before starting of process you can set parameters of processing (variables,
 * properties, standard input and output streams, user objects). The document
 * can be used for validation and processing of the input XML data ("xparse"
 * methods) or for construction of a XML object (methods "xcreate"). Note that
 * XDDocument is the root node of processing and it is the extension of
 * the interface {@link org.xdef.proc.XXNode}. You can read of or set to
 *  variables of a XDPool by methods of {@link org.xdef.proc.XXNode}:
 * <p>{@link org.xdef.xdef.proc.XXNode#getVariable(String)},
 * {@link org.xdef.proc.XXNode#setVariable(String, Object)},
 * {@link org.xdef.proc.XXNode#setUserObject(Object)},
 * {@link org.xdef.proc.XXNode#getUserObject()},
 * {@link org.xdef.proc.XXNode#getUserObject()},
 * {@link org.xdef.proc.XXNode#setXDContext(org.w3c.dom.Node node)}.
 * {@link org.xdef.proc.XXNode#getXDContext()}.</p>
 * <p>Typical example of validation:</p>
 * <pre><tt>
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
 * }</tt></pre>
 * <p>Typical example of construction:</p>
 * <pre><tt>
 * //get instance of XDDocument with X-definition given by name
 * XDDocument xDoc = xp.createXDDocument(name);
 * ArrayReporter reporter = new ArrayReporter(); // here will be written errors
 * ... set data source see {@link org.xdef.proc.XXNode}
 * //construct required element.
 * Element el = xd.xcreate(nsuri, //namespace of required model or null
 *   name, // name of required model (in given X-definition)
 *   reporter);
 * </tt></pre>
 * @author Vaclav Trojan
 */
public interface XDDocument extends XXNode {

	/** Set properties.
	 * @param props Properties.
	 */
	public void setProperties(Properties props);

	/** Set property. If properties are null the new Properties object
	 * will be created.
	 * @param key name of property.
	 * @param value value of property or null. If the value is null the property
	 * is removed from properties.
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
	 * @return The Document object (may be <tt>null</tt>).
	 */
	public Document getDocument();

	/** Set root model for this document model.
	 * @param xmodel model to be set.
	 */
	public void setRootModel(XMElement xmodel);

	/** Parse and process XML source element.
	 * @param xmlData string with pathname of XML file or XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(String xmlData, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param xmlData string with pathname of XML file or XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent parseXComponent(String xmlData,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source element.
	 * @param xmlData string with pathname of XML file or XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @param sourceId name of source or <tt>null</tt>.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(String xmlData,
		String sourceId,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param xmlData file with XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent parseXComponent(File xmlData,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source element.
	 * @param xmlData file with XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(File xmlData, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param xmlData URL pointing to XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent parseXComponent(URL xmlData,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source element.
	 * @param xmlData URL pointing to XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(URL xmlData, ReportWriter reporter)
		throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param xmlData input stream with XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent parseXComponent(InputStream xmlData,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source element.
	 * @param xmlData input stream with XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(InputStream xmlData,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param xmlData input stream with XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param sourceId name of source or <tt>null</tt>.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent parseXComponent(InputStream xmlData,
		Class<?> xClass,
		String sourceId,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source element.
	 * @param xmlData input stream with XML source data.
	 * @param sourceId name of source or <tt>null</tt>.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(InputStream xmlData,
		String sourceId,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse source XML and return XComponent as result.
	 * @param xmlData XML <tt>org.w3c.dom.Node</tt>.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent parseXComponent(Node xmlData,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException;

	/** Parse and process XML source element.
	 * @param xmlData XML <tt>org.w3c.dom.Node</tt>.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(Node xmlData,
		ReportWriter reporter) throws SRuntimeException;


	/** Run create mode - create element according to the X-definition model.
	 * If the parameter nsUri is not <tt>null</tt> then its assigned the model
	 * with given namespaceURI; in this case the parameter name may be
	 * qualified with a prefix.
	 * @param nsUri the namespace URI of result element (may be <tt>null</tt>).
	 * @param name the name of model of required element (may contain prefix).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 * @deprecated Please use method xcreate(QName qname, ReportWriter reporter)
	 */
	public Element xcreate(String nsUri,
		String name,
		ReportWriter reporter) throws SRuntimeException;

	/** Run create mode - create element according to the X-definition model.
	 * @param name the name of model of required element.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xcreate(String name, ReportWriter reporter)
		throws SRuntimeException;

	/** Run create mode - create element according to the X-definition model.
	 * @param qname the QName of model of required element.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xcreate(QName qname, ReportWriter reporter)
		throws SRuntimeException;

	/** get StdOut.
	 * @return std out XDOutput.
	 */
	public XDOutput getStdOut();

	/** get StdErr.
	 * @return std err XDOutput.
	 */
	public XDOutput getStdErr();

	/** get StdIn.
	 * @return std in XDInput.
	 */
	public XDInput getStdIn();

	/** Set XML writer.
	 * @param out output stream.
	 * @param encoding encoding of output.
	 * @param writeDocumentHeader if true full document is written, otherwise
	 * only root element.
	 * @throws IOException if an error occurs.
	 */
	public void setStreamWriter(OutputStream out,
		String encoding,
		boolean writeDocumentHeader) throws IOException;

	/** Set XML writer.
	 * @param out stream writer.
	 * @param encoding encoding of output.
	 * @param writeDocumentHeader if true full document is written, otherwise
	 * only root element.
	 */
	public void setStreamWriter(Writer out,
		String encoding,
		boolean writeDocumentHeader);

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

	/** Create root check element with namespace for given name.
	 * @param nsURI Namespace URI of the element.
	 * @param qname Qualified name of the element.
	 * @param checkRoot If value of this argument is true then the root check
	 * element is checked against the root list, otherwise it is found as
	 * XElement on the base level.
	 * @return The ChkElement object.
	 */
	public XXElement prepareRootXXElementNS(final String nsURI,
		final String qname,
		final boolean checkRoot);

	/** Create root check element for given name.
	 * @param name Tag name of the root element.
	 * @param checkRoot If value of this argument is true then the root check
	 * element is checked against the root list, otherwise it is found as
	 * XElement on the base level.
	 * @return The ChkElement object.
	 */
	public XXElement prepareRootXXElement(final String name, boolean checkRoot);

	/** Get implementation properties of X-definition.
	 * @return the implementation properties of X-definition.
	 */
	public Properties getImplProperties();

	/** Get implementation property of X-definition.
	 * @param name The name of property.
	 * @return the value of implementation property from root X-definition.
	 */
	public String getImplProperty(String name);

	/** Check value of datetime.
	 * Check if the year of date in the interval
	 * (YEAR_MIN .. YEAR_MAX) or the value of date is
	 * one of UNDEF_YEAR[] values.
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

	/** Get actual source language used for thesaurus.
	 * @return string with actual language.
	 */
	public String getSourceThesaurusLanguage();

	/** Set actual source language used for thesaurus.
	 * @param language string with language or null.
	 * @throws SRuntimeException if thesaurus is not specified or if
	 * language is not specified.
	 */
	public void setSourceThesaurusLanguage(String language)
		throws SRuntimeException;

	/** Get actual destination language used for thesaurus.
	 * @return string with actual language.
	 */
	public String getDestThesaurusLanguage();

	/** Set actual destination language used for thesaurus.
	 * @param language string with language or null.
	 * @throws SRuntimeException if thesaurus is not specified or if
	 * language is not specified.
	 */
	public void setDestThesaurusLanguage(String language)
		throws SRuntimeException;

}