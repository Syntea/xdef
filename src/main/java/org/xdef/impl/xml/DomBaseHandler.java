package org.xdef.impl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/** Reader used in SAX parser.
 * @author Vaclav Trojan
 */
public abstract class DomBaseHandler
	implements ContentHandler, EntityResolver, ErrorHandler,
	LexicalHandler, XHandler{
	private static final DocumentBuilderFactory DBF =
		DocumentBuilderFactory.newInstance();
	public final DocumentBuilder _docBuilder;
	static {
		try {
			DBF.setNamespaceAware(true);
			DBF.setCoalescing(true);
//			DBF.setExpandEntityReferences(false);
			DBF.setExpandEntityReferences(true);
			DBF.setValidating(false);
			DBF.setXIncludeAware(true);
			DBF.setIgnoringElementContentWhitespace(false);
			DBF.setIgnoringComments(false);
			DBF.setFeature( // no xml:base attributes
			  "http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public InputSource _is;
	private XMLReader _xr;
	public boolean _isDTD;
	public Locator _locator;
	public final Map<String, String> _prefixes =
		new LinkedHashMap<String, String>();
	private boolean _ignoreComments = true; //default
	private XAbstractReader _mr ;
	public String _sysId;
	private String _pubId;
	private String _xmlVersion;
	private String _xmlEncoding;

	public DomBaseHandler() {
		try {
			_docBuilder = DBF.newDocumentBuilder();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/////////////////////////////////////////////////////////////
	// DomBaseHandler
	/////////////////////////////////////////////////////////////

	public final void doParse(final InputStream in, final String sysId)
		throws Exception {
		XInputStream myInputStream = new XInputStream(in);
		XReader myReader = new XReader(myInputStream);
		myReader.setHandler(this);
		myReader.setSysId(sysId);
		doParse(myReader);
		myReader.close();
	}

	public final void doParse(final XReader myReader)
		throws Exception {
		InputSource is = new InputSource();
		is.setSystemId(myReader.getSysId());
		is.setCharacterStream(myReader);
		setReader(myReader);
		prepareParse(is);
	}

	public abstract void prepareParse(final InputSource is)
		throws IOException, SAXException;
	public final XMLReader getXMLReader() { return _xr; }
	public final void setXMLReader(final XMLReader x) {_xr = x;}
	public final void setXmlEncoding(final String x) {_xmlEncoding = x;}
	public final void setXmlVersion(final String x) {_xmlVersion = x;}
	public final InputSource getInputSource() {return _is;}
	public final void setInputSource(final InputSource x) {_is = x;}
	public final String getSysId() {return _sysId;}
	public final void setSysId(final String x) {_sysId = x;}
	public final String getPubId() {return _pubId;}
	public final void setPubId(final String x) {_sysId = x;}
	public String getXmlEncoding() {return _xmlEncoding;}
	public String getXmlVersion() {return _xmlVersion;}

	public final XAbstractReader getReader() {return _mr;}
	public final void setReader(final XAbstractReader x) {_mr = x;}
	public final boolean isIgnoringComments() {return _ignoreComments;}
	public final void setIgnoringComments(final boolean x) {_ignoreComments=x;}

	/////////////////////////////////////////////////////////////
	// ContentHandler
	/////////////////////////////////////////////////////////////
	@Override
	public void setDocumentLocator(final Locator locator) {_locator = locator;}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) {
		_prefixes.put(prefix, uri);
	}

	@Override
	public void endPrefixMapping(final String prefix){}

	@Override
	public void skippedEntity(final String name) {}

	/////////////////////////////////////////////////////////////
	// LexicalHandler
	/////////////////////////////////////////////////////////////
	@Override
	public void startEntity(String name) {}
	@Override
	public void endEntity(String name) {}
	@Override
	public void startCDATA() {}
	@Override
	public void endCDATA() {}

	@Override
	public void startDTD(String name, String publicId, String systemId)
	{_isDTD = true;}

	@Override
	public void endDTD() {_isDTD = false;}

	@Override
	public void comment(char[] ch, int start, int length) {}

}