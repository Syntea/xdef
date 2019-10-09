package org.xdef.impl;

import org.xdef.XDConstants;
import org.xdef.XDOutput;
import org.xdef.impl.code.DefOutStream;
import org.xdef.msg.XDEF;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SError;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.impl.xml.KParsedElement;
import org.xdef.impl.xml.DomBaseHandler;
import org.xdef.impl.xml.XAbstractReader;
import org.xdef.model.XMData;
import org.xdef.msg.SYS;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.Report;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SException;
import org.xdef.sys.SUtils;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.LinkedHashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.xdef.impl.xml.KParsedAttr;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/** Parsing of the XML source with the X-definition.
 * @author Vaclav Trojan
 */
final class ChkParser extends DomBaseHandler {
	/** SAXParserFactory used in this class. */
	private static final SAXParserFactory SPF = SAXParserFactory.newInstance();
	static { // set features to SAXParserFactory.
		try {
			SPF.setNamespaceAware(true);
			SPF.setXIncludeAware(true);
			SPF.setValidating(false);
			SPF.setFeature("http://xml.org/sax/features/namespaces", true);
			SPF.setFeature("http://xml.org/sax/features/namespace-prefixes",
				false);
			SPF.setFeature("http://apache.org/xml/features/allow-java-encodings",
				true);
			SPF.setFeature("http://xml.org/sax/features/string-interning",
				true);
			SPF.setFeature("http://apache.org/xml/features/xinclude", true);
			SPF.setFeature( // do not create xml:base attributes
				"http://apache.org/xml/features/xinclude/fixup-base-uris",
				false);
			SPF.setSchema(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/** Allocation unit for node list. */
	private static final int NODELIST_ALLOC_UNIT = 8;
	/** Nested level of parsed object.*/
	private int _level;
	/** Root ChkDocument. */
	private ChkDocument _chkDoc;
	/** Actual ChkElement node. */
	private ChkElement _chkEl;
	/** Actual w3c.dom.Element. */
	private Element _element;
	/** Stack of active node path. */
	private ChkElement[] _chkElemStack;
	/** Root document. */
	private Document _doc;
	/** Cumulated text. */
	private SBuffer _text;
	/** Input stream with XML data.*/
	private InputStream _in;
	/** Reporter for error messages. */
	private SReporter _sReporter;
	/** XML source position. */
	public SPosition _elemLocator;

	private final Stack<HandlerInfo> _stackReader = new Stack<HandlerInfo>();
	private Map<String, String> _entities;
	private boolean _genPositionsX;
	private boolean _illegalDoctype;
	private boolean _resolveIncludes;
	private boolean _locationDetails;

	private static class HandlerInfo {
		private final XAbstractReader _mr;
		private final XMLReader _xr;
		private final InputSource _is;
		private final String _xmlVersion;
		private final String _xmlEncoding;
		private final String _pubId;
		private final String _sysId;

		private final boolean _isDTD;
		private final SPosition _elemLocator;

		private HandlerInfo(final ChkParser h, final XAbstractReader mr) {
			_mr = h.getReader();
			_xr = h.getXMLReader();
			_is = h.getInputSource();
			_xmlVersion = h.getXmlVersion();
			_xmlEncoding = h.getXmlEncoding();
			_pubId = h.getPubId();
			_sysId = h.getSysId();

			_elemLocator = h._elemLocator;
			_isDTD = h._isDTD;
			mr.setHandler((DomBaseHandler) h);

			h.setReader(mr);
			h.setXMLReader(null);
			h.setInputSource(null);
			h.setXmlVersion(null);
			h.setXmlEncoding(null);
			h.setPubId(null);
			h.setSysId(mr.getSysId());
			h._isDTD = _isDTD;
			h._elemLocator = null;
		}

		private void resetHandler(ChkParser h) {
			h.setReader(_mr);
			h.setXMLReader(_xr);
			h.setInputSource(_is);
			h.setXmlVersion(_xmlVersion);
			h.setXmlEncoding(_xmlEncoding);
			h.setSysId(_sysId);
			h.setPubId(_pubId);

			h._isDTD = _isDTD;
			h._elemLocator = _elemLocator;
		}
	}

	private ChkParser(final ReportWriter reporter) {
		super();
		_sReporter = new SReporter(
			reporter == null ?  new ArrayReporter() : reporter);
		_entities = new LinkedHashMap<String, String>();
		_entities.put("gt", ">");
		_entities.put("lt", "<");
		_entities.put("amp", "&");
		_entities.put("apos", "'");
		_entities.put("quot", "\"");
		_resolveIncludes = true;
//		_illegalDoctype = false; // java makes it
		XMLReader xr;
		try {
			SAXParser sp = SPF.newSAXParser();
			xr = sp.getXMLReader();
		} catch (Exception ex) {//shouldn't happen
			throw new RuntimeException("Parse configuration error", ex);
		}
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
		xr.setEntityResolver(this);
		setXMLReader(xr);
	}

	/** Creates a new instance of ChkParser and parses given string.
	 * @param reporter The reporter.
	 * @param source The string with source XML data.
	 */
	ChkParser(final ReportWriter reporter, final String source) {
		this(reporter);
		if (source == null || source.trim().isEmpty()) {
			throw new SRuntimeException(SYS.SYS024, "null");
		}
		String s = source.trim();
		if (s.length() > 0 && s.charAt(0) == '<') {
			try {
				_sysId = "STRING";
				_in = new ByteArrayInputStream(source.getBytes("UTF8"));
			} catch (Exception ex) {}//never happens
		} else {
			try {
				URL u = SUtils.getExtendedURL(s);
				_sysId = u.toExternalForm();
				_in = u.openStream();
			} catch (Exception ex) {
				File f = new File(source);
				try {
					_sysId = f.getCanonicalPath();
					_in = new FileInputStream(s);
				} catch (Exception exx) {
					//File doesn't exist: &{0}
					throw new SRuntimeException(SYS.SYS024, s);
				}
			}
		}
	}

	/** Creates a new instance of ChkParser and parses given string.
	 * @param reporter The reporter.
	 * @param source The file with source XML data.
	 */
	ChkParser(final ReportWriter reporter, final File source) {
		this(reporter);
		if (source == null) {
			//File doesn't exist: &{0}
			throw new SRuntimeException(SYS.SYS024, "null");
		}
		try {
			_sysId = source.getCanonicalPath();
			_in = new FileInputStream(source);
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS024,//File doesn't exist: &{0}
				source != null ? source.getAbsoluteFile() : "null");
		}
	}

	/** Creates a new instance of ChkParser
	 * @param reporter The reporter
	 * @param in The source input stream.
	 * @param sourceName the name of source data
	 */
	ChkParser(final ReportWriter reporter,
		final InputStream in,
		final String sourceName) {
		this(reporter);
		if (in == null) {
			throw new SRuntimeException(SYS.SYS024, "null");
		}
		_sysId = sourceName;
		_in = in;
	}

	/** Creates a new instance of ChkParser and parses given string.
	 * @param reporter The reporter.
	 * @param source URL with source XML data.
	 */
	ChkParser(final ReportWriter reporter, final URL source) {
		this(reporter);
		if (source == null) {
			throw new SRuntimeException(SYS.SYS024, "null");
		}
		_sysId = source.toExternalForm();
		try {
			_in = source.openStream();
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS024, _sysId);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// implementation of XHandler
	////////////////////////////////////////////////////////////////////////////
	@Override
	public InputSource pushReader(XAbstractReader mr) {
		_stackReader.push(new HandlerInfo(this, mr));
		return new InputSource(mr);
	}

	@Override
	public final void popReader() {
		if (!_stackReader.empty()) {
			_stackReader.pop().resetHandler(this);
		}
	}

	/////////////////////////////////////////////////////////////
	// Implementation of DomBaseHandler methods
	/////////////////////////////////////////////////////////////
	@Override
	public final void prepareParse(final InputSource is)
		throws IOException, SAXException {
		XMLReader xr = getXMLReader();
		_is = is;
		xr.parse(is);
		getReader().close();
		setReader(null);
		setXMLReader(null);
		_element = null;
		_text = null;
		_is = null;
		_locator = null;
		_elemLocator = null;
		_stackReader.clear();
	}

	/////////////////////////////////////////////////////////////
	// Implementation of EntityResolver
	/////////////////////////////////////////////////////////////

	@Override
	public InputSource resolveEntity(final String pubID, final String sysID)
		throws SAXException, IOException {
		InputSource is;
		if (_isDTD && pubID != null) {
			InputStream in = new ByteArrayInputStream(new byte[0]);
			is = new InputSource(in);
			is.setSystemId(sysID);
			if (_illegalDoctype) {
				_sReporter.fatal(XML.XML099);//DOCTYPE is set as not allowed
			}
			return is;
		}
		if (sysID != null && pubID == null) {
			InputStream in = new ByteArrayInputStream(new byte[0]);
			if (_isDTD && _illegalDoctype) {
				_sReporter.fatal(XML.XML099);//DOCTYPE is set as not allowed
			} else if ((_isDTD && _chkDoc._xdef._resolveEntities != 'F')
				|| !_isDTD) {
				try {
					URL u = SUtils.getExtendedURL(sysID);
					in = u.openStream();
				} catch (Exception ex) {
					//URL &{0} error: &{1}{; }
					_sReporter.fatal(SYS.SYS076, sysID);
				}
				if  (!_resolveIncludes) {
					_sReporter.error(XML.XML309); //XInclude forbidden
				}
			}
			is = new InputSource(in);
			is.setSystemId(sysID);
			return is;
		}
		return null;
	}

	/////////////////////////////////////////////////////////////
	// Implementation of ContentHandler
	/////////////////////////////////////////////////////////////

	// implemented DomBaseHandler:
	// void setDocumentLocator(Locator x)
	// public void startPrefixMapping(final String prefix, final String uri)
	// public void endPrefixMapping(final String prefix)
	// public void skippedEntity(final String name)

	private void updateLocator() {
		if (_locator != null) {
			_elemLocator = new SPosition(_locator.getLineNumber(),
				_locator.getColumnNumber(), _sysId,
				_locator.getPublicId());
		}
	}

	@Override
	public void startDocument() throws SAXException {
		updateLocator();
		_doc = null;
		_element = null;
		_text = null;
	}

	/** Find attribute in the list.
	 * @param list list of parsed attributes.
	 * @param n name of attribute.
	 * @return found item or null.
	 */
	private static Object[] findAttr(final List<Object[]> list, final String n){
		if (list != null) {
			for (int i = 1; i < list.size(); i++) {
				Object[] item = list.get(i);
				if (n.equals(item[0])) {
					return item;
				}
			}
		}
		return null;
	}

	@Override
	public void startElement(final String uri,
		final String localName,
		final String qName,
		final Attributes atts) throws SAXException {
		_elemLocator.setIndex(_elemLocator.getIndex() + qName.length() + 1);
		_sReporter.setPosition(_elemLocator);
		_isDTD = false;
		XAbstractReader mr = getReader();
		if (_level == -1) {
			try {
				String s = mr.getProlog() + "<" + qName;
				if (uri != null && !uri.isEmpty()) {
					int ndx = qName.indexOf(':');
					String attrName = "xmlns"
						+ (ndx > 0 ? ':' + qName.substring(0, ndx) : "");
					s += " " + attrName + "=\"" + uri + '\"';
				}
				s += "/>";
				InputStream in = new ByteArrayInputStream(
					s.getBytes(getReader().getEncoding()));
				String sysId = _is.getSystemId();
				_docBuilder.setEntityResolver(this);
				boolean isDTD = _isDTD;
				_isDTD = true;
				_doc = _docBuilder.parse(in, sysId);
				_isDTD = isDTD;
				_chkDoc._doc = _chkDoc._rootChkDocument._doc = _doc;
				_element = _doc.getDocumentElement();
			} catch (Exception ex) { // never should happen!
				throw new RuntimeException(ex);
			}
			if (!_locationDetails) {
				mr.stopGenPositions();// now scanning will be stopped
			} else {
				if (!mr.prologParsed()) {
					mr.scanProlog();
				}
				mr.releaseScanned();
			}
		} else {
			processText(mr);
			Element el = _doc.createElementNS(uri, qName);
			_element.appendChild(el);
			_element = el;
		}
		_text = null;
		KParsedElement parsedElem = new KParsedElement();
		List<Object[]> list = null;
		if (_locationDetails
			&& !(list = mr.getElementPositions(qName)).isEmpty()) {
			SPosition ep = (SPosition) ((Object[])list.get(0))[1];
			parsedElem.setParsedNameParams(uri, qName, ep);
			mr.releaseScanned();
			for (Map.Entry<String, String> x: _prefixes.entrySet()) {
				String name = x.getKey();
				name = !name.isEmpty() ? "xmlns:" + name : "xmlns";
				Object[] item = findAttr(list, name);
				SPosition sp = item != null ? (SPosition) item[1] : ep;
				KParsedAttr att = new KParsedAttr(
					XMLConstants.XMLNS_ATTRIBUTE_NS_URI, name, x.getValue(),sp);
				parsedElem.addAttr(att);
			}
			for (int i = 0; i < atts.getLength(); i++) {
				String name = atts.getQName(i);
				Object[] item = findAttr(list, name);
				SPosition sp = item != null
					? (SPosition) ((SPosition) item[1]).correctPosition() : ep;
				KParsedAttr att = new KParsedAttr(
					atts.getURI(i), name, atts.getValue(i), sp);
				parsedElem.addAttr(att);
			}
		} else {
			SPosition ePos = new SPosition(_elemLocator);
			parsedElem.setParsedNameParams(uri, qName, ePos);
			ePos = new SPosition(ePos);
			ePos.setIndex(ePos.getIndex() + 1);
			for (Map.Entry<String, String> x: _prefixes.entrySet()) {
				String name = x.getKey();
				name = !name.isEmpty() ? "xmlns:" + name : "xmlns";
				KParsedAttr item = new KParsedAttr(
					XMLConstants.XMLNS_ATTRIBUTE_NS_URI,name,x.getValue(),ePos);
				parsedElem.addAttr(item);
			}
			for (int i = 0; i < atts.getLength(); i++) {
				KParsedAttr item = new KParsedAttr(
					atts.getURI(i), atts.getQName(i), atts.getValue(i), null);
				parsedElem.addAttr(item);
			}
		}
		int len = parsedElem.getLength();
		for (int i = 0; i < len; i++) {
			KParsedAttr att = parsedElem.getAttr(i);
			_element.setAttributeNS(att.getNamespaceURI(),
				att.getName(), att.getValue());
		}
		_prefixes.clear();
		updateLocator();
		elementStart(parsedElem);
	}

	@Override
	public void endElement(final String uri,
		final String localName,
		final String qName) throws SAXException {
		_sReporter.setPosition(_elemLocator);
		XAbstractReader mr = getReader();
		processText(mr);
		elementEnd();
		if (_genPositionsX) {
			mr.scanEndElement();
		}
		updateLocator();
	}

	private void appendText(final String s) {
		if (_text == null) {
			_sReporter.setPosition(_elemLocator);
			_text = new SBuffer(s, _elemLocator);
		} else {
			_text.addString(s);
		}
	}

	@Override
	public void characters(final char[] ch,
		final int start,
		final int length) throws SAXException {
		appendText(String.valueOf(ch, start, length));
		updateLocator();
	}

	@Override
	public void ignorableWhitespace(final char[] ch,
		final int start,
		final int length) throws SAXException {
		appendText(String.valueOf(ch, start, length));
		updateLocator();
	}

	@Override
	public void processingInstruction(final String target, final String data)
		throws SAXException {
		if (_level >= 0) {
			_element.appendChild(_doc.createProcessingInstruction(target,data));
		} else if (_doc != null) {
			_doc.appendChild(_doc.createProcessingInstruction(target, data));
		}
		updateLocator();
	}

	@Override
	public void endDocument() throws SAXException {
		_text = null;
		_chkDoc.endDocument();
	}

	/////////////////////////////////////////////////////////////
	// Implementation of LexicalHandler
	/////////////////////////////////////////////////////////////
	@Override
	public void startDTD(String name, String publicId, String systemId) {
		_isDTD = true;
		if (_illegalDoctype) {
			_sReporter.fatal(XML.XML099); //DOCTYPE is set as not allowed
		}
		updateLocator();
	}

	@Override
	public void endDTD() {
		_isDTD = false;
		updateLocator();
	}

	@Override
	public void startEntity(String name) {
		updateLocator();
	}

	@Override
	public void endEntity(String name) {
		updateLocator();
	}

	@Override
	public void startCDATA() {updateLocator();}

	@Override
	public void endCDATA() {updateLocator();}

	@Override
	public void comment(char[] ch, int start, int length) {
		if (!isIgnoringComments() && _doc != null) {
			Node n = _element == null ? _doc : _element;
			Comment x = _doc.createComment(new String(ch, start, length));
			n.appendChild(x);
		}
		updateLocator();
	}

	/////////////////////////////////////////////////////////////
	// Implementation of ErrorHandler
	/////////////////////////////////////////////////////////////

	private SPosition getPos(final SAXParseException x) {
		SPosition result = new SPosition(x.getLineNumber(),
			x.getColumnNumber(), _sysId, x.getPublicId());
		return result;
	}

	@Override
	public final void warning(final SAXParseException x) {
		String m = x.getMessage();
		if (m != null) {
			if (m.contains("Include operation failed, reverting to fallback.")){
				return;
			}
		}
		_sReporter.setPosition(getPos(x));
		_sReporter.warning(XML.XML075, m);
	}

	@Override
	public final void error(final SAXParseException x) {
		String m = x.getMessage();
		_sReporter.setPosition(getPos(x));
		_sReporter.error(XML.XML075, m);
	}

	@Override
	public final void fatalError(final SAXParseException x) throws SAXException{
		String m = x.getMessage();
		if (m != null) {
			if (m.contains("no fallback element was found")){
				return;
			}
			if (m.contains("must not contain the '<' character")) {
				//Character "<" can't be used here
				_sReporter.setPosition(getPos(x));
				_sReporter.error(XML.XML041);
				return;
			}
			if (m.contains("--")) {
				_sReporter.setPosition(getPos(x));
				_sReporter.error(XML.XML030);//XML030=Comment can't contain "--"
				return;
			}
			if (m.contains("character sequence \"]]>\" must not")) {
				_sReporter.setPosition(getPos(x));
				//Character sequence "]]>" is not allowed in element content
				_sReporter.error(XML.XML053);
				return;
			}
			if (m.contains("must be terminated by the matching end-tag")) {
				String s = null;
				int ndx = m.indexOf('"');
				if (ndx >= 0) {
					int ndx1 = m.indexOf('"',++ndx);
					if (ndx1 > 0) {
						s = m.substring(ndx, ndx1);
					}
				}
				_sReporter.setPosition(getPos(x));
				_sReporter.error(XML.XML024, s);
				return;
			}
			if (m.contains("Recursive include detected.")) {
				//XInclude - recursive include &{0}
				int ndx1 = m.indexOf("Document");
				if (ndx1 >= 0) {
					ndx1 += 9;
					int ndx2 = m.indexOf(" was already processed.", ndx1);
					String fname = m.substring(ndx1 + 1, ndx2 - 1);
					_sReporter.setPosition(getPos(x));
					_sReporter.error(XML.XML306, fname);
					return;
				}
			}
			if (m.contains("The prefix") &&
				m.contains("for attribute")
				&& m.contains("associated with an element")) {
				int ndx = m.indexOf('"', 10);
				if (ndx > 0) {
					int ndx2 = m.indexOf('"', ndx + 1);
					if (ndx2 > 0) {
						ndx = m.indexOf('"', ndx2 + 1);
						if (ndx > 0) {
							ndx2 = m.indexOf('"', ndx + 1);
							String attName = m.substring(ndx+1, ndx2);
							_sReporter.setPosition(getPos(x));
							_sReporter.error(XML.XML047, attName);
							return;
						}
					}
				}
			}
		}
		_sReporter.setPosition(getPos(x));
		_sReporter.error(XML.XML075); // XML error
	}

	/////////////////////////////////////////////////////////////

	final void closeReader() {} // needs the old version with KXmlParser

	/** Get connected reporter.
	 * @return connected SReporter.
	 */
	SReporter getReporter() {return _sReporter;}

	/** Parse XML source and process check and processing instructions.
	 * @param chkDoc The ChkDocument object.
	 */
	void xparse(ChkDocument chkDoc) {
		try {
			_level = -1;
			_chkElemStack = new ChkElement[NODELIST_ALLOC_UNIT];
			_chkEl = null;
			_chkDoc = chkDoc;
			_chkDoc._node = null;
			_chkDoc._element = null;
			XCodeProcessor scp = _chkDoc._scp;
			Properties props = scp.getProperties();
			_chkDoc._scp = null;
			_chkDoc.init(chkDoc._xdef,
				(Document) _chkDoc.getDocument().cloneNode(false),
				chkDoc._reporter,
				scp.getProperties(),
				chkDoc._userObject);
			_chkDoc._scp = scp;
			_doc = _chkDoc._doc = _chkDoc._rootChkDocument._doc = null;
			XPool xdp = (XPool) chkDoc._xdef.getXDPool();
			setIgnoringComments(true); // ????
			_illegalDoctype = !getBooleanProperty(xdp.isIllegalDoctype(),
				XDConstants.XDPROPERTY_DOCTYPE, props);
			_resolveIncludes = getBooleanProperty(xdp.isResolveIncludes(),
				XDConstants.XDPROPERTY_XINCLUDE, props);
			_locationDetails = getBooleanProperty(xdp.isLocationsdetails(),
				XDConstants.XDPROPERTY_LOCATIONDETAILS, props);
			if (_chkDoc.isDebug() && _chkDoc.getDebugger() != null) {
				 // open debugger
				_chkDoc.getDebugger().openDebugger(props, xdp);
			}
			_chkDoc._scp.initscript(); //Initialize variables and methods
			_isDTD = true;
			try {
				doParse(_in, _sysId);
			} catch (SRuntimeException ex) {
				String x = ex.getMsgID();
				if (x != null && x.startsWith("XML")) {
					Report r = ex.getReport();
					_sReporter.error(x, r.getText(), r.getModification());
				} else {
					throw ex;
				}
			} catch (RuntimeException ex) {
				throw ex;
			} catch (SAXException ex) {
				//XML parser was canceled by exception: &{0}
				_sReporter.error(XML.XML080, "SAXException; "+ex.getMessage());
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			_chkEl = null;
			_chkElemStack = null;
		} catch (SError e) {
			if ("XDEF906".equals(e.getMsgID())) {
				throw e; //X-definition canceled
			}
			throw new SRuntimeException(e.getReport(), e.getCause());
		}
	}

	private static boolean getBooleanProperty(final boolean x,
		final String name,
		final Properties props) {
		String val = props != null ? props.getProperty(name) : null;
		return "true".equals(val) ? true : "false".equals(val) ? false : x;
	}

	////////////////////////////////////////////////////////////////////////////

	/** This method adds cumulated text nodes to the result. */
	private void processText(XAbstractReader mr) {
		if (_text != null && _text.getString().length() > 0) {
			SPosition myPos = new SPosition(_sReporter); //save position
			//set position of text
			_sReporter.setPosition(_text);
			_chkEl.addText(_text.getString());
			//set saved position
			_sReporter.setPosition(myPos);
			_text = null;
		}
		if (_locationDetails && mr != null) {
			while (mr.scanCDATA()>=0 || mr.scanPI()>=0
				|| mr.scanEntity()>=0 || mr.scanComment()>= 0
				 || mr.scanText()>=0) {}
		}
	}

	/** Read string.
	 * string::=  S ('"' ^['"']* '"' | "'" ^["'"]* "'")
	 * @param p string parser
	 * @return string.
	 */
	static String readString(final StringParser p) {
		p.skipSpaces();
		char delimiter;
		if ((delimiter = p.isOneOfChars("'\"")) == StringParser.NOCHAR) {
			return null;
		}
		int pos = p.getIndex();
		char c;
		while ((c = p.peekChar()) != StringParser.NOCHAR) {
			if (c == delimiter) {
				return p.getBufferPart(pos, p.getIndex() -1);
			}
		}
		return null;
	}

	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 */
	private void elementStart(final KParsedElement parsedElem) {
		_level++;
		if (_level == 0) {
			int ndx = parsedElem.indexOfNS(XDConstants.XDEF_INSTANCE_NS_URI,
				"location");
			if (ndx < 0) { // deprecated instance specification
				ndx = parsedElem.indexOfNS(XPool.XDEF31_INSTANCE_NS_URI,
					"location");
			}
			if (ndx < 0) { // deprecated instance specification
				ndx = parsedElem.indexOfNS(XPool.XDEF20_INSTANCE_NS_URI,
					"location");
			}
			KParsedAttr ka;
			String s;
			if (ndx >= 0 &&	(ndx =
				(s=(ka=parsedElem.getAttr(ndx)).getName()).indexOf(':')) > 0) {
				String xPrefix = s.substring(0, ndx + 1);
				s = "xmlns:" + s.substring(0, ndx);
				_element.removeAttribute(s);
				parsedElem.remove(s);
				parsedElem.remove(xPrefix + "location"); //xdi:location
				_element.removeAttribute(xPrefix + "location");
				if ((_chkDoc == null || ka != null &&
					"#".equals(_chkDoc._xdef.getName()))){
					XPool xdp;
					String systemLiteral = getExternalId(
						new SBuffer(ka.getValue(), ka.getPosition()));
					URL u;
					try {
						s = _sysId.replace('\\', '/');
						if (s.startsWith("file:")) {
							s = s.substring(5);
						}
						int i = s.lastIndexOf("/");
						s = i > 0 ? s.substring(0, i + 1): s;
						u = SUtils.resolveSystemID(systemLiteral, s);
					} catch (SException ex) {
						Report rep = ex.getReport();
						_sReporter.putReport(ka.getPosition(),
							Report.fatal(rep.getMsgID(),
							rep.getText(),
							rep.getModification()));
						return;
					}
					try {
						xdp =(XPool)new XBuilder(null).setSource(u).compileXD();
					} catch (Exception ex) {
						//In X-definition are errors&{0}{: }
						_sReporter.putReport(ka.getPosition(),
							Report.fatal(XDEF.XDEF543, ex));
						return;
					}
					//xdi:definition
					ka = parsedElem.getAttr(xPrefix + "xdefName");
					String value;
					if (ka != null) {
						value = xdp != null ? ka.getValue().trim() : null;
					} else {
						value = parsedElem.getParsedName();
					}
					if (xdp != null) {
						XDefinition def = xdp.getDefinition(value);
						if (def != null) {
							XDOutput stdOut = null;
							//xdi:stdOut
							ka = parsedElem.getAttr(xPrefix + "stdOut");
							if (ka != null && _chkDoc != null) {
								value = ka.getValue().trim();
								int index = value.indexOf(',');
								if (index >= 0) {
									String encoding =
										value.substring(index + 1).trim();
									value = value.substring(0,index).trim();
									stdOut =
										new DefOutStream(value, encoding);
								} else {
									stdOut = new DefOutStream(value);
								}
							}
							// xdi:stdErr
							ka = parsedElem.getAttr(xPrefix + "stdErr");
							if (ka != null && _chkDoc != null) {
								value = ka.getValue().trim();
								int index = value.indexOf(',');
								if (index >= 0) {
									String encoding =
										value.substring(index + 1).trim();
									value = value.substring(0,index).trim();
									_sReporter.setReportWriter(
										new FileReportWriter(
											value, encoding, true));
								} else {
									_sReporter.setReportWriter(
										new FileReportWriter(value));
								}
							}
							if (_chkDoc == null) {
								_chkDoc = new ChkDocument(def);
								_chkDoc._reporter.setReportWriter(
									_sReporter.getReportWriter());
								_chkDoc._doc = _element.getOwnerDocument();
								_chkDoc.setStdOut(stdOut);
							} else {
								XCodeProcessor scp = _chkDoc._scp;
								ChkDocument cd = new ChkDocument(def);
								_chkDoc._reporter.setReportWriter(
									_sReporter.getReportWriter());
								_chkDoc._scp = cd._scp;
								_chkDoc._scp.setDebugger(scp.getDebugger());
								_chkDoc._scp.setProperties(scp.getProperties());
								_chkDoc._scp.setStdErr(scp.getStdErr());
								_chkDoc._xdef = cd._xdef;
							}
						} else {
							//Missing X-definition &{0}{:}
							_sReporter.fatal(XDEF.XDEF530, value);
							return;
						}
					}
				}
				//remove attributes with X-definition instance namespace
				parsedElem.remove(xPrefix + "xdefName");
				parsedElem.remove(xPrefix + "stdOut");
				parsedElem.remove(xPrefix + "stdErr");
				parsedElem.remove(xPrefix + "stdIn");
				_element.removeAttribute(xPrefix + "xdefName");
				_element.removeAttribute(xPrefix + "stdOut");
				_element.removeAttribute(xPrefix + "stdErr");
				_element.removeAttribute(xPrefix + "stdIn");
			}
			if (_chkDoc == null) {
				//X-definition is not specified
				throw new SRuntimeException(XDEF.XDEF550);
			}
			_chkEl = _chkDoc.createRootChkElement(_element, true);
		} else {
			_chkEl = _chkEl.createChkElement(_element);
		}
		if (_level >= _chkElemStack.length) { //increase nodelist
			ChkElement[] newList =
				new ChkElement[_chkElemStack.length + NODELIST_ALLOC_UNIT];
			System.arraycopy(_chkElemStack, 0, newList, 0, _chkElemStack.length);
			_chkElemStack = newList;
		}
		_chkElemStack[_level] = _chkEl;
		for (XMData x: _chkEl.getXMElement().getAttrs()) {
			KParsedAttr ka = parsedElem.getAttrNS(x.getNSUri(),x.getName());
			if (ka != null) {
				if (_locationDetails) {
					_sReporter.setPosition(ka.getPosition());
				}
				_chkEl.newAttribute(
					_element.getAttributeNode(ka.getName()));
				ka.setValue(null);
			}
		}
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			if (ka.getValue() != null) {
				if (_locationDetails) {
					_sReporter.setPosition(ka.getPosition());
				}
				_chkEl.newAttribute(
					_element.getAttributeNode(ka.getName()));
			}
		}
		if (_locationDetails) {
			_sReporter.setPosition(_elemLocator);
		}
		_chkEl.checkElement();
	}

	private String getExternalId(final SBuffer val) {
		StringParser p;
		if (val != null) {
			p = new StringParser(val);
			p.skipSpaces();
		} else {
			return "";
		}
		int i = p.isOneOfTokens("SYSTEM", "PUBLIC");
		if (i < 0) {
			return p.getSourceBuffer().trim(); //we allow direct specification??
		}
		if (i == 1) { //PUBLIC
			if (!p.isSpaces()) {
				_sReporter.putReport(val, //Whitespace expected after '&{0}'
					Report.lightError(XML.XML014, "PUBLIC"));
			}
			if (readString(p) == null) {
				//Quoted string declaration expected
				_sReporter.error(XDEF.XDEF504);
			}
			if (!p.isSpaces()) {
				//Whitespace expected after '&{0}'
				_sReporter.putReport(val, Report.lightError(XML.XML014,
					"PUBLIC identifier"));
			}
		} else if (!p.isSpaces()) {
			//Whitespace expected after '&{0}'
			_sReporter.putReport(val, Report.lightError(XML.XML014, "SYSTEM"));
		}
		String sourceURL;
		if ((sourceURL = readString(p)) != null) {
			return sourceURL.trim();
		}
		_sReporter.error(XDEF.XDEF504); //Quoted string declaration expected
		return "";
	}

	/** This method is invoked when parser reached the end of element. */
	private void elementEnd() {
		_chkElemStack[_level--] = null; //let's gc do the job
		_chkEl.addElement();
		if (_level >= 0) {
			_chkEl = _chkElemStack[_level];
		}
	}
}