package org.xdef.xml;

import org.xdef.msg.SYS;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUnsupportedOperationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xdef.sys.SUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Provides creating and parsing XML data.
 * @author Vaclav Trojan
 */
public class KDOMBuilder extends DocumentBuilder {
	private boolean _coalescing;
	private boolean _namespaceAware;
	private boolean _ignoreComments;
	private boolean _expandEntityReferences;
	/** If this flag is <tt>true</tt> DTD validation is provided. */
	private boolean _validate;
	private boolean _ignoreElementContentWhitespace;
	/** If true the parser will resolve XInclude nodes. By default the value of
	 * this is set to false. */
	private boolean _resolveIncludes;
	/** If true ignore unresolved entities (e.g. file not found). */
	private boolean _ignoreUnresolvedEntities;
	private ArrayReporter _reporter;

	private static final DocumentBuilderFactory BUILDER_FACTORY =
		DocumentBuilderFactory.newInstance();
	private DocumentBuilder _xBuilder;

	/** Creates a new instance of KDomBuilder with KDOMParser and S_Document.*/
	public KDOMBuilder() {
		_coalescing = true;
		_namespaceAware = true;
		_expandEntityReferences = true;
		_ignoreComments = true;
//		_validate = false;
		_resolveIncludes = true;
//		_ignoreElementContentWhitespace = false;
//		_ignoreUnresolvedEntities = false;
	}

	private void throwMsg(final SAXParseException ex, final byte type) {
		if (ex instanceof SThrowable) {
			putReport(((SThrowable) ex).getReport());
			return;
		}
		String modification = "" + ex + "; ";
		String s;
		if ((s = ex.getMessage())!= null) {
			modification += s;
		}
		int i;
		if ((i = ex.getLineNumber()) > 0) {
			modification += "&{line}" + i;
		}
		if ((i = ex.getColumnNumber()) > 0) {
			modification += "&{column}" + i;
		}
		String source = ex.getSystemId();
		if ((s = ex.getPublicId()) != null) {
			s = "(pubid: " + s + ")";
			source = source == null ? s : source + ' ' + s;
		}
		if (source != null) {
			modification += "&{sysId}" + source;
		}
		//DomBuilder report&{0}{: }&{#SYS000}
		putReport(new Report(type, XML.XML404,	modification));
	}

	private void putReport(final Report report) {
		if (_reporter == null) {
			_reporter = new ArrayReporter();
		}
		_reporter.putReport(report);
	}

	/** Get internal reporter.
	 * @return internal reporter or <tt>null</tt>.
	 */
	public final ArrayReporter getReporter() {
		if (_reporter == null) {
			return null;
		}
		return _reporter;
	}

	/** If builderFactory is set then create builder. */
	private void checkBuilder() {
		try {
			if (BUILDER_FACTORY.isCoalescing() != _coalescing) {
				BUILDER_FACTORY.setCoalescing(_coalescing);
				_xBuilder = null;
			}
			if (BUILDER_FACTORY.isExpandEntityReferences() !=
				_expandEntityReferences) {
				BUILDER_FACTORY.setExpandEntityReferences(
					_expandEntityReferences);
				_xBuilder = null;
			}
			if (BUILDER_FACTORY.isIgnoringComments() != _ignoreComments) {
				BUILDER_FACTORY.setIgnoringComments(_ignoreComments);
				_xBuilder = null;
			}
			if (BUILDER_FACTORY.isIgnoringElementContentWhitespace() !=
				_ignoreElementContentWhitespace) {
				BUILDER_FACTORY.setIgnoringElementContentWhitespace(
					_ignoreElementContentWhitespace);
				_xBuilder = null;
			}
			if (BUILDER_FACTORY.isNamespaceAware() != _namespaceAware) {
				BUILDER_FACTORY.setNamespaceAware(_namespaceAware);
				_xBuilder = null;
			}
			if (BUILDER_FACTORY.isValidating() != _validate) {
				BUILDER_FACTORY.setValidating(_validate);
				_xBuilder = null;
			}
			try {
				if (BUILDER_FACTORY.isXIncludeAware() != _resolveIncludes) {
					BUILDER_FACTORY.setXIncludeAware(_resolveIncludes);
					_xBuilder = null;
				}
			} catch (UnsupportedOperationException ex) {
				if (_resolveIncludes) {
					//Unsupported feature of DomBuilder&{0}{: }&{#SYS000}
					throw new SUnsupportedOperationException(XML.XML405,
						"setXIncludeAware");
				}
			}
			if (_xBuilder == null) {
				_xBuilder = BUILDER_FACTORY.newDocumentBuilder();
			} else {
				_xBuilder.reset();
			}
			_xBuilder.setErrorHandler(new ErrorHandler(){
				@Override
				public void warning(SAXParseException ex) {
					throwMsg(ex, Report.WARNING);
				}
				@Override
				public void error(SAXParseException ex) {
					throwMsg(ex, Report.ERROR);
				}
				@Override
				public void fatalError(SAXParseException ex) {
					throwMsg(ex, Report.FATAL);
				}
			});
			if (_ignoreUnresolvedEntities) {
				_xBuilder.setEntityResolver(new EntityResolver() {
					@Override
					public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
						if (publicId == null && systemId != null) { //
							InputStream in;
							try {
								in = SUtils.getExtendedURL(
									systemId).openStream();
							} catch (Exception ex) {
								// if error occurs set the empty InputStream
								in = new ByteArrayInputStream(new byte[0]);
							}
							InputSource is = new InputSource(in);
							is.setSystemId(systemId);
							return is;
						}
						return null;
					}
				});
			}
		} catch (ParserConfigurationException ex) {
			//Parser configuration error: &{0}
			throw new SRuntimeException(XML.XML402, ex);
		}
	}

	@Override
	/** Get DomImplementation from this builder.
	 * @return DomImplementation from this builder.
	 */
	public final DOMImplementation getDOMImplementation() {
		checkBuilder();
		return _xBuilder.getDOMImplementation();
	}

	@Override
	/** Create new empty document.
	 * @return new empty document.
	 */
	public final Document newDocument() {
		checkBuilder();
		return _xBuilder.newDocument();
	}

	/** Creates an XML <tt>Document</tt> object with empty root element
	 * created by document builder (see SetDOMImplementation).
	 * @param nsURI namespace of created root element (or null).
	 * @param qname qualified name of root element.
	 * @param docType DocumentType object or null.
	 * @return new <tt>Document</tt> object with empty root element.
	 */
	public final Document newDocument(final String nsURI,
		final String qname,
		final DocumentType docType) {
		checkBuilder();
		return _xBuilder.getDOMImplementation()
			.createDocument(nsURI, qname, docType);
	}

	/** Specifies that the DOM parser code will convert CDATA nodes
	 * to Text nodes and append it to the adjacent (if any) text node.
	 * By default the value of this is set to true.
	 * @param coalescing true if the DOM parser will convert CDATA nodes
	 * to Text nodes and append it to the adjacent (if any) text node;
	 * false otherwise.
	 */
	public final void setCoalescing(final boolean coalescing) {
		_coalescing = coalescing;
	}

	/** Get value of the coalescing switch.
	 * @return the coalescing switch.
	 */
	public final boolean isCoalescing() {return _coalescing;}

	/** Specifies that the DOM parser will provide support
	 * for XML namespaces. By default the value of this is set to true.
	 * @param namespaceAware true if the DOM parser will provide
	 * support for XML namespaces; false otherwise.
	 */
	public final void setNamespaceAware(final boolean namespaceAware) {
		_namespaceAware = namespaceAware;
	}

	@Override
	/** Get switch if the DOM parser will provide support for XML namespaces.
	 * @return true if the DOM parser will provide support for XML namespaces.
	 */
	public final boolean isNamespaceAware() {return _namespaceAware;}

	/* Specifies that the DOM parser must eliminate whitespace in element
	 * content (sometimes known loosely as 'ignorable whitespace') when parsing
	 * XML documents (see XML Rec 2.10). Note that only whitespace which is
	 * directly contained within element content that has an element only
	 * content model (see XML Rec 3.2.1) will be eliminated. Due to reliance
	 * on the content model this setting requires the parser to be in validating
	 * mode. By default the value of this is set to true.
	 * @param ignore true if the DOM parser must eliminate whitespace
	 * in the element content when parsing XML documents; false otherwise.
	 */
	public final void setIgnoringElementContentWhitespace(final boolean ignore){
		_ignoreElementContentWhitespace = ignore;
	}

	/** Get value of ignoreWhitespace switch.
	 * @return  value of ignoreWhitespace switch.
	 */
	public final boolean isIgnoringElementContentWhitespace() {
		return _ignoreElementContentWhitespace;
	}

	/** Set the XML parser will expand entity reference nodes. By default the
	 * value of this is set to true.
	 * @param expandEntityReferences set the expandEntityReferences switch.
	 */
	public final void setExpandEntityReferences(
		final boolean expandEntityReferences){
		_expandEntityReferences = expandEntityReferences;
	}

	/** Get the expand Entity references switch.
	 * @return the expand Entity references switch.
	 */
	public final boolean isExpandEntityReferences() {
		return _expandEntityReferences;
	}

	/** Set the parser will ignore comments. By default
	 * the value of this is set to true.
	 * @param ignoreComents set the ignoreComments switch.
	 */
	public final void setIgnoringComments(final boolean ignoreComents) {
		_ignoreComments = ignoreComents;
	}

	/** Get the parser will expand entity references switch.
	 * @return the ignoreComments switch.
	 */
	public final boolean isIgnoringComments() {return _ignoreComments;}

	/** Set the parser to resolve include nodes. By default
	 * the value of this is set to false.
	 * @param resolveIncludes set the resolveIncludes switch.
	 */
	public final void setXIncludeAware(final boolean resolveIncludes) {
		_resolveIncludes = resolveIncludes;
	}

	@Override
	/** Get the resolveIncludes switch.
	 * @return the resolveIncludes switch.
	 */
	public final boolean isXIncludeAware() {return _resolveIncludes;}

	/** Set the parser to provide DTD validating. By default
	 * the value of this is set to false.
	 * @param validate set the validate switch.
	 */
	public final void setValidating(final boolean validate) {
		_validate = validate;
	}

	@Override
	/** Get DTD validating switch.
	 * @return the validate switch.
	 */
	public final boolean isValidating() {return _validate;}

	/** Set the parser to ignore unresolved entities. By default
	 * the value of this is set to false.
	 * @param x set the ignoreUnresolvedEntities switch.
	 */
	public final void setIgnoreUnresolvedEntities(final boolean x) {
		_ignoreUnresolvedEntities = x;
	}

	/** Get the ignoreUnresolvedEntities switch.
	 * @return the resolveIncludes switch.
	 */
	public final boolean isIgnoreUnresolvedEntities() {
		return _ignoreUnresolvedEntities;
	}

	@Override
	/** Parse XML file.
	 * @param file the file with XML document
	 * @return object org.w3c.dom.Document with parsed XML document.
	 * @throws SRuntimeException if reporter was not specified and when an
	 * error occurs.
	 */
	public final Document parse(final File file) {
		Document doc = null;
		if (_reporter != null) {
			_reporter.clear();
		}
		checkBuilder();
		try {
			doc = _xBuilder.parse(file);
		} catch (Exception ex) {
			//Error while reading XML document: &{0}
			putReport(Report.fatal(XML.XML403, ex));
		}
		if (_reporter != null) {
			_reporter.checkAndThrowErrors();
		}
		return doc;
	}

	/** Parse XML from URL.
	 * @param url URL pointing to XML document
	 * @return object org.w3c.dom.Document with parsed XML document.
	 * @throws SRuntimeException if reporter was not specified and when an
	 * error occurs.
	 */
	public final Document parse(final URL url) {
		Document doc = null;
		if (_reporter != null) {
			_reporter.clear();
		}
		checkBuilder();
		try {
			doc = _xBuilder.parse(url.openStream());
		} catch (Exception ex) {
			//Error while reading XML document: &{0}
			putReport(Report.fatal(XML.XML403, ex));
		}
		if (_reporter != null) {
			_reporter.checkAndThrowErrors();
		}
		return doc;
	}

	@Override
	/** Parse XML from InputStream.
	 * @param stream the InputStream with XML document
	 * @return object org.w3c.dom.Document with parsed XML document.
	 * @throws SRuntimeException if reporter was not specified and when an
	 * error occurs.
	 */
	public final Document parse(final InputStream stream) {
		return parse(stream, true);
	}

	/** Parse XML from InputStream.
	 * @param stream the InputStream with XML document
	 * @param closeStream if true the input stream is closed after parsing.
	 * @return object org.w3c.dom.Document with parsed XML document.
	 * @throws SRuntimeException if reporter was not specified and when an
	 * error occurs.
	 */
	public final Document parse(final InputStream stream,
		final boolean closeStream) {
		Document doc = null;
		if (_reporter != null) {
			_reporter.clear();
		}
		checkBuilder();
		try {
			doc = _xBuilder.parse(stream);
			if (closeStream) {
				stream.close();
			}
		} catch (Exception ex) {
			//Error while reading XML document: &{0}
			putReport(Report.fatal(XML.XML403, ex));
		}
		if (_reporter != null) {
			_reporter.checkAndThrowErrors();
		}
		return doc;
	}

	@Override
	/** Parse XML from data from argument.
	 * @param source If this argument starts with "&lt;" then it is parsed as
	 * XML data. Otherwise it is interpreted as a pathname of the file with
	 * XML data.
	 * @return object org.w3c.dom.Document with parsed XML document.
	 */
	public final Document parse(final String source) {
		if (_reporter != null) {
			_reporter.clear();
		}
		if (source == null || source.length() == 0) {
			//Invalid file name: '&{0}'
			throw new SRuntimeException(SYS.SYS065, source);
		}
		Document doc = null;
		if (source.charAt(0) == '<') {
			checkBuilder();
			try {
				doc =_xBuilder.parse(new InputSource(new StringReader(source)));
			} catch (Exception ex) {
				//Error while reading XML document: &{0}
				putReport(Report.fatal(XML.XML403, ex));
			}
		} else {
			if (source.startsWith("//") ||
				(source.indexOf(":/") > 2 && source.indexOf(":/") < 11)) {
				try { // try URL
					return parse(SUtils.getExtendedURL(source));
				} catch (Exception ex) {}
			}
			doc = parse(new File(source));
		}
		if (_reporter != null) {
			_reporter.checkAndThrowErrors();
		}
		return doc;
	}

////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Specify the {@link ErrorHandler} to be used by the parser.
	 * Setting this to <code>null</code> will result in the underlying
	 * implementation using it's own default implementation and
	 * behavior.
	 * @param errHandler The <code>ErrorHandler</code> to be used by the parser.
	 */
	 public final void setErrorHandler(final ErrorHandler errHandler) {
		 if (_xBuilder != null) {
			 _xBuilder.setErrorHandler(errHandler);
		 } else {
			throw new UnsupportedClassVersionError();
		 }
	 }

	@Override
	/** Specify the {@link EntityResolver} to be used to resolve
	 * entities present in the XML document to be parsed. Setting
	 * this to <code>null</code> will result in the underlying
	 * implementation using it's own default implementation and
	 * behavior.
	 * @param entResolver The <code>EntityResolver</code> to be used to resolve
	 * entities present in the XML document to be parsed.
	 */
	public final void setEntityResolver(final EntityResolver entResolver) {
		 if (_xBuilder != null) {
			 _xBuilder.setEntityResolver(entResolver);
		 } else {
			throw new UnsupportedClassVersionError();
		 }
	}

	@Override
	/** Parse the content of the given input source as an XML document
	 * and return a new DOM {@link Document} object.
	 * An <code>IllegalArgumentException</code> is thrown if the
	 * <code>InputSource</code> is <code>null</code> null.
	 *
	 * @param is InputSource containing the content to be parsed.
	 * @exception IOException If any IO errors occur.
	 * @exception SAXException If any parse errors occur.
	 * @see org.xml.sax.DocumentHandler
	 * @return A new DOM Document object.
	 */
	public final Document parse(final InputSource is)
		throws  SAXException, IOException {
		return parse(is.getByteStream(), false);
	}

	/** Parse the content of the given input source as an XML document
	 * and return a new DOM {@link Document} object.
	 * An <code>IllegalArgumentException</code> is thrown if the
	 * <code>InputSource</code> is <code>null</code> null.
	 * @param is InputSource containing the content to be parsed.
	 * @param closeStream if true the input stream is closed after parsing.
	 * @exception IOException If any IO errors occur.
	 * @exception SAXException If any parse errors occur.
	 * @see org.xml.sax.DocumentHandler
	 * @return A new DOM Document object.
	 */
	public final Document parse(final InputSource is, final boolean closeStream)
		throws  SAXException, IOException {
		return parse(is.getByteStream(), closeStream);
	}
}