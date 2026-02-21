package org.xdef.impl.xml;

import java.io.InputStream;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/** Reader used in SAX parser.
 * @author Vaclav Trojan
 */
public abstract class DomBaseHandler implements ContentHandler, EntityResolver, ErrorHandler,
	LexicalHandler, XHandler {
	private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
	public final DocumentBuilder _docBuilder;
	public InputSource _is;
	private XMLReader _xr;
	public boolean _isDTD;
	public Locator _locator;
	public final Map<String, String> _prefixes = new LinkedHashMap<>(); // used in ContentHandler
	private boolean _ignoreComments = true; //default
	private XAbstractReader _mr ;
	public String _sysId;
	private String _pubId;
	private String _xmlVersion;
	private String _xmlEncoding;

	static {
		try { // Set parameters of DocumentBuilderFactory used in the handler of the parser..
			DBF.setNamespaceAware(true);
			DBF.setCoalescing(true);
			DBF.setExpandEntityReferences(true);
			DBF.setValidating(false);
			DBF.setXIncludeAware(true);
			DBF.setIgnoringElementContentWhitespace(false);
			DBF.setIgnoringComments(false);
			// no xml:base attributes
			DBF.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Create new instance of DomBaseHandler. */
	public DomBaseHandler() {
		try {
			_docBuilder = DBF.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/////////////////////////////////////////////////////////////
	// DomBaseHandler
	/////////////////////////////////////////////////////////////

	/** Prepare parser with the given input data.
	 * @param in input stream with data to be used in parser.
	 * @param sysId data sysId..
	 * @throws Exception if an error occurs.
	 */
	public final void doParse(final InputStream in, final String sysId) throws Exception {
		XInputStream myInputStream = new XInputStream(in);
		try (XReader myReader = new XReader(myInputStream)) {
			myReader.setHandler(this);
			myReader.setSysId(sysId);
			doParse(myReader);
		}
	}

	/** Prepare parser with the given XReader.
	 * @param myReader set reader to be used in parser and prepare parsing.
	 * @throws Exception if an error occurs.
	 */
	public final void doParse(final XReader myReader) throws Exception {
		InputSource is = new InputSource();
		is.setSystemId(myReader.getSysId());
		is.setCharacterStream(myReader);
		setReader(myReader);
		prepareParse(is);
	}

	/** Prepare parser with this DomBaseHandler and the given InputSource.
	 * @param is InpusSource to be used.
	 * @throws Exception if an I/O error occurs.
	 */
	public abstract void prepareParse(final InputSource is) throws Exception;

	/** Get XML reader.
	 * @return XML reader.
	 */
	public final XMLReader getXMLReader() {return _xr;}

	/** Set XML reader.
	 * @param x XML reader.
	 */
	public final void setXMLReader(final XMLReader x) {_xr = x;}

	/** Set XML encoding.
	 * @param x name of encoding.
	 */
	public final void setXmlEncoding(final String x) {_xmlEncoding = x;}

	/** Set XML version.
	 * @param x version of XML.
	 */
	public final void setXmlVersion(final String x) {_xmlVersion = x;}

	/** Get InputSource of this parser.
	 * @return InputSource of this parser.
	 */
	public final InputSource getInputSource() {return _is;}

	/** Set InputSource to this parser.
	 * @param x InputSource to be set to this parser.
	 */
	public final void setInputSource(final InputSource x) {_is = x;}

	/** Get SysId of the source data.
	 * @return SysId of the ssource data.
	 */
	public final String getSysId() {return _sysId;}

	/** Set SysId of the parsed source data.
	 * @param x SysId of the parsed source data.
	 */
	public final void setSysId(final String x) {_sysId = x;}

	/** Get PubId of the source data.
	 * @return PubId of the ssource data.
	 */
	public final String getPubId() {return _pubId;}

	/** Set PubId of the parsed source data.
	 * @param x PubId of the parsed source data.
	 */
	public final void setPubId(final String x) {_sysId = x;}

	/** Get XML encoding of the source data.
	 * @return  XML encoding of the ssource data.
	 */
	public String getXmlEncoding() {return _xmlEncoding;}

	/** Get XML version of the source data.
	 * @return  XML version of the ssource data.
	 */
	public String getXmlVersion() {return _xmlVersion;}

	/** Get implementation of XML reader.
	 * @return implementation of XML reader.
	 */
	public final XAbstractReader getReader() {return _mr;}

	/** Set implementation of XML readers.
	 * @param x implementation of XML readers.
	 */
	public final void setReader(final XAbstractReader x) {_mr = x;}

	/** Get value of switch to ignore XML commjents.
	 * @return value of switch to ignore XML commjents.
	 */
	public final boolean isIgnoringComments() {return _ignoreComments;}

	/** Get switch to ignore XML commjents.
	 * @param x value of switch to ignore XML commjents.
	 */
	public final void setIgnoringComments(final boolean x) {_ignoreComments=x;}

	/////////////////////////////////////////////////////////////
	// ContentHandler implementation
	/////////////////////////////////////////////////////////////

	@Override
	public void setDocumentLocator(final Locator locator) {_locator = locator;}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) {_prefixes.put(prefix, uri);}

	@Override
	public void endPrefixMapping(final String prefix){}

	@Override
	public void skippedEntity(final String name) {}

	/////////////////////////////////////////////////////////////
	// LexicalHandlerr implementation
	/////////////////////////////////////////////////////////////

	@Override
	public void startEntity(final String name) {}

	@Override
	public void endEntity(final String name) {}

	@Override
	public void startCDATA() {}

	@Override
	public void endCDATA() {}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId) {_isDTD=true;}

	@Override
	public void endDTD() {_isDTD = false;}

	@Override
	public void comment(final char[] ch, final int start, final int length) {}
}