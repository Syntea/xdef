/**
 * Contains interfaces for processing of X-definitions and
 * the factory for compilation of X-definitions and creation of some important
 * objects used in X-definition.
 * <p>
 * For any kind of usage it is necessary first to compile X-definition to
 * the object{@link org.xdef.XDPool}. The XDPool object can be declared
 * as static and it can be used by more parallel threads.
 * XDPool contains	compiled code generated form of all X-definitions.
 * To compile X-definition use the {@link org.xdef.XDFactory} - see
 * static methods. To execute a X-definition you must first create an object
 * {@link org.xdef.XDDocument}. This object contains pointer to the root
 * X-definition where processing will start and it contains also all instances
 * of objects needed for processing (variables etc). This object can be used
 * for validation processing and/or for construction of XML data. In the
 * multi-thread environment you must create this object for each thread.
 * </p>
 * <p>There are several typical modes of usage of X-definition tools:</p>
 *
 * <UL>
 * <li>
 * The most frequented mode is the "parsing" mode. In this mode the processor
 * validates and processes input data represented from the input XML document.
 * The important property of this mode is the ability to process XML files of
 * unlimited size. If it is used the option "forget" in an X-definition the size
 * of input data may be many gigabytes and it is not limited by the size of
 * internal memory of the Java virtual machine. The speed of processor is
 * typically over 1 megabyte per second. The method which provides this mode
 * is {@link org.xdef.XDDocument#xparse(Object,
 * org.xdef.sys.ReportWriter)}.
 * The Object in the first parameter can be a string with pathname, file, URL,
 * input stream or XML or.w3v.dom.Node with data to be parsed.
 * </li>
 *
 * <li>
 * The construction mode is designed to construct XML documents
 * according to X-definition. In this mode the source data is generated by the
 * X-definition itself (i.e. XDPool): the data can be taken from XML object or
 * from an external database, or by setting a value. The output data are created
 * according to given X-definition (see
 * {@link org.xdef.XDDocument#xcreate(String,
 * org.xdef.sys.ReportWriter)} or
 * {@link org.xdef.XDDocument#xcreate(javax.xml.namespace.QName,
 * org.xdef.sys.ReportWriter)}.
 * The first parameter contains the name of model according which the result
 * is constructed.
 * </li>
 * </UL>
 *
 * In the case you want to know properties of X-definition models of
 * processed objects you can use methods:
 * <p>{@link org.xdef.XDDocument#getXMDefinition()}</p>
 * <p>{@link org.xdef.proc.XXNode#getXMElement()}</p>
 * <p>{@link org.xdef.proc.XXData#getXMData()}</p>
 * <p>
 * Those methods return an object with properties of processed data.
 * The interfaces of such objects see the package <i><b>org.xdef.model</b></i>.
 * </p>
 *
 * <UL>
 * <li>
 * <h2><code>XDPool</code></h2>
 * Contains pool of X-definitions compiled from the source (see
 * {@link org.xdef.XDFactory}).
 * <p><b>Example:</b></p>
 * <pre><code>
 * // Prepare X-definition file
 * File xdef = new File("./src/Example.xdef");
 *
 * // 1. Create DefPool and XDDocument.
 * XDPool xpool = XDFactory.genXDPool(null, xdef); //creation of XDPool
 * XDDocument xdoc = xp.createXDDocument(name); //create of XDDocument
 *    ...
 * </code></pre>
 * </li>
 *
 * <li>
 * <h2><code>XDDocument</code></h2>
 * Validates XML source data (see
 * {@link org.xdef.XDDocument#xparse(Object,
 * org.xdef.sys.ReportWriter)}).
 * <p><b>Example:</b></p>
 * <pre><code>
 * // 2. Validate and process source XML data with X-definition.
 * ArrayReporter reporter = new ArrayReporter(); // here will be written errors
 * Element el = xd.xparse(sourceXml, reporter); //validate and process data
 * // now we have root element of parsed source data errors in variable el
 * // and list of errors in reporter
 * // 3. test if an error was reported
 * if (xd.errorWarnings()) {//error or warning reported?
 *    reporter.getReportReader().printReports(System.err);
 * } else {//ok, no error or warniong messages
 *     ...
 * }
 * </code></pre>
 * </li>
 *
 * <li>Generates XML data from source data (see
 * {@link org.xdef.XDDocument#xcreate(String,
 * org.xdef.sys.ReportWriter)} or
 * {@link org.xdef.XDDocument#xcreate(javax.xml.namespace.QName,
 * org.xdef.sys.ReportWriter)} and
 * {@link org.xdef.XDDocument#getElement()}).
 * <p><b>Example:</b></p>
 * <pre><code>
 * // Set to ChkDocument parsed source element.
 * xdoc.setSourceContext(sourcedata); //element with source data
 *
 * // construction of XML document
 * ArrayReporter reporter = new ArrayReporter();
 * Element el = xdoc.xcreate("element namespace URI", "element name", reporter);
 * Element element = chkDoc.getElement();
 * // test errors
 * if (xdoc.errorWarnings()) {
 *     reporter.printReports(System.err); // print error messages
 * } else {
 *     System.out.println(KXmlUtils.nodeToString(element, true)); //print result
 * }
 * </code></pre>
 * </li>
 * </UL>
 *
 * <UL>
 * <li>
 * <h2><code>GenXDefinition</code></h2>
 * You can create the X-definition from a XML document when you
 * call the class GenXDefinition from command line: see:
 * {@link org.xdef.util.GenXDefinition#main(String[])}.
 * </li>
 * </UL>
*/
package org.xdef;