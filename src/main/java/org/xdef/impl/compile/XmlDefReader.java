package org.xdef.impl.compile;

import org.xdef.impl.xml.DomBaseHandler;
import org.xdef.impl.xml.XAbstractReader;
import org.xdef.impl.xml.XInputStream;
import org.xdef.impl.xml.XReader;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.impl.xml.KParsedElement;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.LinkedHashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.impl.xml.KParsedAttr;
import org.xdef.sys.SUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;

/** Reads XML source data with XDefinitions, it provides data values with
 * line and position information (including attribute values).
 * @author trojan
 */
abstract class XmlDefReader extends DomBaseHandler implements DeclHandler {
	private int _level; // nesting level of a node
	private static final SAXParserFactory SPF = SAXParserFactory.newInstance();
	private ReportWriter _reporter;
	private final Stack<HandlerInfo> _stackReader=new Stack<HandlerInfo>();
	private Map<String, String> _entities;
	private SBuffer _text;
	public final StringBuilder _sb = new StringBuilder();
	static {
		try {
			// Set SAX parser parameters
			SPF.setNamespaceAware(true);
			SPF.setXIncludeAware(true);
			SPF.setValidating(false);
			SPF.setFeature("http://xml.org/sax/features/namespaces", true);
			SPF.setFeature("http://xml.org/sax/features/namespace-prefixes",
				false);
			SPF.setFeature(
				"http://apache.org/xml/features/allow-java-encodings", true);
			SPF.setFeature("http://xml.org/sax/features/string-interning",
				true);
			SPF.setFeature("http://apache.org/xml/features/xinclude", true);
			// do not validate document with DTD and Schema)
			SPF.setFeature(
				"http://apache.org/xml/features/validation/dynamic", false);
			SPF.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
				false);
			SPF.setFeature(
				"http://xml.org/sax/features/external-parameter-entities",
				false);
			SPF.setFeature(
				"http://xml.org/sax/features/external-general-entities", false);
			SPF.setFeature(
				"http://apache.org/xml/features/xinclude/fixup-base-uris",
				false);
			SPF.setFeature(
				"http://apache.org/xml/features/xinclude/fixup-language",false);
			SPF.setFeature( // do not create xml:base attributes
				"http://apache.org/xml/features/xinclude/fixup-base-uris",
				false);
			SPF.setFeature(
				"http://apache.org/xml/features/xinclude/fixup-language",false);
			SPF.setFeature(
				"http://xml.org/sax/features/external-general-entities", true);
			SPF.setFeature(
				"http://xml.org/sax/features/external-parameter-entities",true);
			SPF.setSchema(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static class HandlerInfo {
		private final XAbstractReader _mr;
		private final XMLReader _xr;
		private final InputSource _is;
		private final String _xmlVersion;
		private final String _xmlEncoding;
		private final String _pubId;
		private final String _sysId;

		private final boolean _isDTD;
		private final Locator _locator;
		private final Map<String, String> _entities;

		private HandlerInfo(final XmlDefReader h,
			final XAbstractReader mr) {
			_mr = h.getReader();
			_xr = h.getXMLReader();
			_is = h.getInputSource();
			_xmlVersion = h.getXmlVersion();
			_xmlEncoding = h.getXmlEncoding();
			_pubId = h.getPubId();
			_sysId = h.getSysId();

			_isDTD = h._isDTD;
			_locator = h._locator;
			_entities = h._entities;

			mr.setHandler((DomBaseHandler) h);

			h.setReader(mr);
			h.setXMLReader(null);
			h.setInputSource(null);
			h.setXmlVersion(null);
			h.setXmlEncoding(null);
			h.setPubId(null);
			h.setSysId(mr.getSysId());

			h._isDTD = false;
			h._entities = new LinkedHashMap<String, String>(_entities);
		}

		private void resetHandler(XmlDefReader h) {
			h.setReader(_mr);
			h.setXMLReader(_xr);
			h.setInputSource(_is);
			h.setXmlVersion(_xmlVersion);
			h.setXmlEncoding(_xmlEncoding);
			h.setSysId(_sysId);
			h.setPubId(_pubId);
			h._isDTD = _isDTD;
			h._locator = _locator;
			h._entities = _entities;
		}
	}

	/** Create the instance of XDefReader with the internal ArrayReporter. */
	public XmlDefReader() {this(new ArrayReporter());}

	/** Create the instance of XDefReader with reporter.
	 * @param reporter report writer.
	 */
	public XmlDefReader(ReportWriter reporter) {
		super();
		_reporter = reporter;
		prepareEnities();
		_entities = new LinkedHashMap<String, String>();
		XMLReader xr;
		try {
			SAXParser sp = SPF.newSAXParser();
			xr = sp.getXMLReader();
			xr.setProperty(
				"http://xml.org/sax/properties/declaration-handler", this);
		} catch (Exception ex) {
			throw new RuntimeException("Parse configuration error", ex);
		}
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
		xr.setEntityResolver(this);
		setXMLReader(xr);
	}

	/** Prepare entities with predefined items. */
	private void prepareEnities() {
		_entities = new LinkedHashMap<String, String>();
		// Set predefined entities
		_entities.put("gt", ">");
		_entities.put("lt", "<");
		_entities.put("amp", "&");
		_entities.put("apos", "'");
		_entities.put("quot", "\"");
	}

	////////////////////////////////////////////////////////////////////////////
	// XHandler
	////////////////////////////////////////////////////////////////////////////
	@Override
	public InputSource pushReader(XAbstractReader mr) {
		_stackReader.push(new HandlerInfo(this, mr));
		setReader(mr);
		return new InputSource(mr);
	}

	@Override
	public final void popReader() {
		if (!_stackReader.empty()) {
			_stackReader.pop().resetHandler(this);
		}
	}

	@Override
	/** Parse X-definition source.
	 * @param is InputSource with XML data.
	 */
	public void prepareParse(final InputSource is) {
		RuntimeException thwn = null;
		try {
			_is = is;
			_sysId = is.getSystemId();
			prepareEnities();
			XMLReader xr;
			try {
				SAXParser sp = SPF.newSAXParser();
				xr = sp.getXMLReader();
				xr.setProperty(
					"http://xml.org/sax/properties/declaration-handler",
					this);
			} catch (Exception ex) {
				throw new RuntimeException("Parse configuration error", ex);
			}
			xr.setContentHandler(this);
			xr.setErrorHandler(this);
			xr.setEntityResolver(this);
			setXMLReader(xr);

			xr.setFeature( // continue after fatal error
				"http://apache.org/xml/features/continue-after-fatal-error",
				true);
//			if (processComments()) { // process comments
//				xr.setProperty(
//					"http://xml.org/sax/properties/lexical-handler", this);
//			}
			xr.parse(is);
			getReader().close();
		} catch (Error ex) {
			thwn = new RuntimeException(ex);
		} catch (RuntimeException ex) {
			thwn = ex;
		} catch (Exception ex) {
			thwn = new RuntimeException(ex);
		}
		try {
			getReader().close();
		} catch (Exception exx) {}
		setReader(null);
		setXMLReader(null);
		_sb.setLength(0);
		_text = null;
		_is = null;
		_locator = null;
		_entities = null;
		_stackReader.clear();
		if (thwn != null) {
			throw thwn;
		}
	}

	/** New text value of current element parsed.
	 * @param text SBuffer with value of text node.
	 */
	abstract void text(final SBuffer text);

	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 * @param parsedElem contains name of the element, name space URI and
	 * the list of attributes.
	 */
	abstract void elementStart(final KParsedElement parsedElem);

	/** This method is invoked when parser reaches the end of element. */
	abstract void elementEnd();

	////////////////////////////////////////////////////////////////////////////
	//ErrorHandler
	////////////////////////////////////////////////////////////////////////////
	private SPosition getPosition(final SAXParseException x) {
		return new SPosition(x.getLineNumber(),x.getColumnNumber(),_sysId,null);
	}

	@Override
	public final void warning(final SAXParseException x) {
		String m = x.getMessage();
		if (m != null) {
			if (m.contains("Include operation failed, reverting to fallback.")){
				return; // we do not report it
			}
		}
		warning(getPosition(x), XML.XML075, x.getMessage());
	}

	@Override
	public final void error(final SAXParseException x) {
		error(getPosition(x), XML.XML075, x.getMessage());
	}

	@Override
	public final void fatalError(final SAXParseException x) {
		String m = x.getMessage();
		if (m != null) {
			if (m.contains("no fallback element was found")){
				error(getPosition(x), XML.XML308);
			} else if (m.contains("must not contain the '<' character")){
				error(getPosition(x), XML.XML041);
			} else if (m.contains("prefix ") && m.contains(" is not bound")) {
				error(getPosition(x), XML.XML047);
			} else if (m.contains("Premature end of file")) {
				error(getPosition(x), XML.XML005);
			} else {
				error(getPosition(x), XML.XML075, x.getMessage());
			}
		}
	}

	/////////////////////////////////////////////////////////////
	// EntityResolver
	/////////////////////////////////////////////////////////////
	@Override
	public InputSource resolveEntity(final String pubID, final String sysID)
		throws IOException {
		InputStream in;
		InputSource is;
		if (!_isDTD && sysID != null && pubID == null) {
			String id = sysID;
			XInputStream mi = null;
			XAbstractReader mr;
			if ((mr = getReader()) != null && mr != null) {
				SPosition spos = mr.getSPosition();
				boolean wasEndFlag = mr.wasEndTag();
				List<Object[]> list = mr.getElementPositions("*");
				boolean newWasEndFlag = mr.wasEndTag();
				mr.setWasEndTag(wasEndFlag);
				if (list.isEmpty()) {// unparsed entity ?
					mr.setSPosition(spos); // reset position before scanned
					return null; // will be resolved by SAX parser
				}
				String s = (String) list.get(0)[0];
				if (s.endsWith("include")) {
					mr.setXInclude(s);
					if (!newWasEndFlag) {
						// skip to end tag of include element
						do {
							mr.scanSpaces();
							mr.scanLiteral();
							mr.scanSpaces();
							if (mr.isToken("</" + s)) {
								mr.scanSpaces();
								mr.isChar('>');
								break;
							}
						} while (mr.nextChar() != 0);
					}
					try {
						URL u = SUtils.getExtendedURL(sysID);
						in = u.openStream();
						if (in == null) {
							mr.setUnresolved(true);
							return null;
						}
						String encoding;
						Map<String, String> atrs =
							new HashMap<String, String>();
						for (int i = 1; i < list.size(); i++) {
							Object[] x = list.get(i);
							atrs.put((String) x[0], (String) x[2]);
						}
						mr.releaseScanned();
						boolean includedText = false;
						if ("text".equals(atrs.get("parse"))) {
							includedText = true;
							encoding = atrs.get("encoding");
							if (encoding == null) {
								encoding = mr.getEncoding();
							}
							mi = new XInputStream(in, encoding);
						} else {
							mi = new XInputStream(in);
							encoding = mi.getXMLEncoding();
						}
						if (encoding != null) {
							mr.setIncludedText(includedText);
							mr = new XReader(mi);
						is = pushReader(mr);
						} else {
							is = new InputSource(mi);
						}
						setInputSource(is);
						mr.setSysId(id);
						is.setSystemId(id);
						is.setPublicId(pubID);
						return is;
					} catch (Exception ex) {
						if (mr != null) {
							popReader();
						}
						if (mi != null) {
							mi.close();
						}
					}
				} else {
					mr.setSPosition(spos);
				}
			}
		}
		if (_isDTD && sysID != null && pubID == null) {
			in = null;
			try {
				URL u = SUtils.getExtendedURL(sysID);
				in = u.openStream();
			} catch (Exception ex) {}
			if (in == null) {
				in = new ByteArrayInputStream(new byte[0]);
			}
			is = new InputSource(in);
			is.setSystemId(sysID);
			return is;
		}
		return null;
	}

	/////////////////////////////////////////////////////////////
	// ContentHandler
	/////////////////////////////////////////////////////////////
	// in the abstract class DomBaseHandler are implemented mdethods:
	//   - void setDocumentLocator(Locator x)
	//   - public void enDocument()
	//   - public void startPrefixMapping(final String prefix, final String uri)
	//   - public void endPrefixMapping(final String prefix)
	//   - public void skippedEntity(final String name)
	// Foolews methods to be implemented:
	@Override
	public void startDocument() throws SAXException {}

	/** Resolve char references, entities and CR,LF.
	 * @param sp buffer SPosition.
	 * @param src source value.
	 * @param type 0 .. CDATA section, 1 .. text, 2 .. attribute
	 * @return corrected buffer and created modifications in position.
	 */
	private String resolveReferences(final SPosition sp,
		final String src,
		final int type) {
		StringBuilder sb = new StringBuilder();
		int p = sp.getIndex();
		int len = src.length();
		long line = sp.getLineNumber();
		long startLine = sp.getStartLine();
		long filePos = sp.getFilePos();
		while (p < len) {
			char ch = src.charAt(p++);
			if (type != 0 && ch =='&') { // not CDATA and "&" => entity or char
				int start = p;
				int end = src.indexOf(';', start);
				if (end <= start) {
					continue;
				}
				p = end + 1;
				String s = src.substring(start, end); // value of reference
				String replacement;
				if (s.charAt(0) == '#') { // char spec.
					try {
						int x = (s.charAt(1)=='x' || s.charAt(1)=='X')
							? Integer.parseInt(s.substring(2), 16) // hexa
							: Integer.parseInt(s.substring(1)); // dec
						replacement = x < 65535
							? String.valueOf((char) x) // just a UTF-16 char
							: new String(new byte[] { // must be converted
								(byte) ((x >> 24) & 0xff),
								(byte) ((x >> 16) & 0xff),
								(byte) ((x >> 8) & 0xff),
								(byte) ((x) & 0xff)}, "UTF-32");
					} catch (Exception ex) {
						//shouldn't happen; checked in SAX parser
						replacement = "?";
					}
				} else { // entity reference
					replacement = _entities.get(s);
					if (replacement == null) {
						// not found macro: shouldn't happen; checked in SAX
						replacement = "";
					}
					if (type == 2) { // attribute value => replace with space
						replacement = replacement.replace('\n', ' ')
							.replace('\r', ' ')
							.replace('\f', ' ')
							.replace('\t', ' ');
					}
				}
				// Replace entity reference with result
				sp.addPos(sb.length() + 1,
					line,
					startLine,
					(int) (startLine - filePos) + replacement.length() - p,
					false);
				sp.addPos(sb.length() + replacement.length() + 1,
					line,
					startLine,
					(int) (startLine - filePos) - p - 1,
					false);
				sb.append(replacement);
			} else {
				if (ch=='\r' && p < len && src.charAt(p)=='\n') {// "\r\n"
					p++; // skip "\r" character
					sp.addPos(sb.length(),
						line,
						startLine,
						(int) (startLine - filePos) - p,
						false);
					ch = '\n';
				}
				if (type == 2 && (ch == '\r' || ch == '\n' || ch == '\t')) {
					sb.append(' ');  // attribute value
				} else {
					sb.append(ch);
				}
				if (ch == '\n') {
					startLine = filePos + p;
					line++;
					sp.addLine(sb.length(), line, startLine);//add new line info
				}
			}
		}
		return sb.toString();
	}

	private void appendText(SPosition sp, String s) {
		if (s != null && !s.isEmpty()) {
			if (_text == null) {
				_text = new SBuffer(s, sp);
			} else {
				if (_text.getStartLine() != sp.getFilePos()) {
					int col = (int) (_text.getStartLine() - sp.getFilePos())+1;
					_text.addPos(_text.getString().length() + 1,
						sp.getLineNumber(),
						sp.getStartLine(),
						col,
						false);
				}
				_text.appendToBuffer(new SBuffer(s, sp));
			}
		}
	}

	@Override
	public void startElement(final String uri,
		final String localName,
		final String qName,
		final Attributes atts) {
		setDocumentLocator(_locator);
		XAbstractReader mr = getReader();
		String nsuri = uri != null && uri.isEmpty() ? null : uri;
		if (_level++ == 0) { // root
			boolean wasDTD = _isDTD;
			Document doc;
			try {
				String s = mr.getProlog()  + "<" + qName;
				int ndx = qName.indexOf(':');
				if (ndx > 0) {
					s += " " + "xmlns:" + qName.substring(0, ndx)
						+ "=\"" + nsuri + '\"';
				} else if (nsuri != null && !nsuri.isEmpty()) {
					s += " " + "xmlns" + "=\"" + nsuri + '\"';
				}
				if (_entities.isEmpty()) {
					s += "/>";
				} else {
					s += ">";
					for (String n : _entities.keySet()) {
						s += "<" + n + ">&" + n + ";</" + n + ">";
					}
					s += "</" + qName + '>';
				}
				String encoding = mr.getEncoding();
				mr.releaseScanned();
				String sysId = _is.getSystemId();
				_isDTD = true;
				_docBuilder.setEntityResolver(this);
				doc = _docBuilder.parse(
					new ByteArrayInputStream(s.getBytes(encoding)), sysId);
				Element el = doc.getDocumentElement();
				if (!_entities.isEmpty()) {
					_entities.clear();
					NodeList nl = el.getElementsByTagName("*");
					for (int i = nl.getLength() - 1; i >= 0; i--) {
						Node n = nl.item(i);
						_entities.put(n.getNodeName(),
							n.getTextContent());
						el.removeChild(n);
					}
				}
			} catch (Exception ex) {}
			_isDTD = wasDTD;
		} else {
			if (!mr.prologParsed()) {
				mr.scanProlog();
			}
			parseText(mr);
			if (_text == null && _sb.length() != 0) {
				appendText(mr.getSPosition(), _sb.toString());
			}
			if (_text != null) {
				text(_text); /////////////////////////////////
			}
		}
		List<Object[]> list = mr.getElementPositions(qName);
		KParsedElement parsedElem = new KParsedElement();
		parsedElem.setParsedNameParams(nsuri, qName,
			(SPosition) ((Object[])list.get(0))[1]);
		mr.releaseScanned();
		for (Map.Entry<String, String> x: _prefixes.entrySet()) {
			String name = x.getKey();
			name = !name.isEmpty() ? "xmlns:" + name : "xmlns";
			Object[] item = findAttr(list, name);
			SPosition sp = item != null ? (SPosition) item[1] : null;
			KParsedAttr att = new KParsedAttr(
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI, name, x.getValue(),sp);
			parsedElem.addAttr(att);
		}
		for (int i = 1; i < list.size(); i++) {
			Object[] item = list.get(i);
			String name = (String) item[0];
			SPosition sp = item != null ? (SPosition) item[1] : null;
			int j = atts.getIndex(name);
			KParsedAttr att = new KParsedAttr(
				atts.getURI(j), name, atts.getValue(j), sp);
			if (item != null) {
				String s = (String) item[2];
				s = resolveReferences(sp, s, 2);
				att.setValue(s);
			}
			parsedElem.addAttr(att);
		}
		elementStart(parsedElem);
		_prefixes.clear();
		_sb.setLength(0);
		_text = null;
	}

	@Override
	public void endElement(final String uri,
		final String localName,
		final String qName) {
		_level--; // increase nesting level
		XAbstractReader mr = getReader();
		if (mr != null) {
			if (!mr.wasEndTag()) {
				SPosition spos = mr.getSPosition();
				if (mr.unresolved()) {
					mr.setUnresolved(false);
					parseText(mr);
				} else if (mr.includedText()) {
					mr.setIncludedText(false);
					parseText(mr);
				} else {
					parseText(mr);
					if (_text == null && _sb.length() > 0 ||  (_text != null
						&& !_sb.toString().equals(_text.getString()))) {
							if (mr.getXInclude() != null) { // XInclude fallback
								_text = new SBuffer(_sb.toString(),
								_text == null ? mr.getSPosition() : _text);
						}
					}
				}
				SPosition spos1 = mr.getSPosition();
				if (mr.scanEndElement() < 0) {
					if (_level == 0) { //root
						int i = spos1.getIndex() + qName.length() + 3;
						spos1.setIndex(i);
					}
				}
				mr.releaseScanned();
				if (_sb.length() > 0) {
					if (_text == null) {
						_text = new SBuffer(_sb.toString(), spos);
					} else if (!_sb.toString().equals(
						_text == null ? "" : _text.getString())) {
						_text = new SBuffer(_sb.toString(), spos);
					}
					text(_text); /////////////////////////////
				}
			} else {
				mr.setWasEndTag(false);
			}
		}
		_sb.setLength(0);
		_text = null;
		elementEnd();
	}

	@Override
	public void characters(final char[] ch,
		final int start,
		final int length) {
		_sb.append(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(final char[] ch,
		final int start,
		final int length) {
		_sb.append(ch, start, length);
	}

	@Override
	public void processingInstruction(final String target,final String data) {
//		if (_lev != 0) {
//			_el.appendChild(_doc.createProcessingInstruction(target, data));
//		} else if (_doc != null) {
//			_doc.appendChild(_doc.createProcessingInstruction(target, data));
//		}
	}

	@Override
	public void endDocument() {}

	/////////////////////////////////////////////////////////////
	// DeclHandler
	/////////////////////////////////////////////////////////////

	@Override
	public void elementDecl(String name, String model) {}

	@Override
	public void attributeDecl(String eName,
		String aName,
		String type,
		String mode,
		String value) {}

	@Override
	public void internalEntityDecl(String name, String value) {
		if (!name.startsWith("%")) {
			_entities.put(name, value);
		}
	}

	@Override
	public void externalEntityDecl(String name,
		String publicId,
		String systemId) {
		if (!name.startsWith("%")) {
			_entities.put(name, systemId);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Private methods:
////////////////////////////////////////////////////////////////////////////////

	private void parseText(final XAbstractReader mr) {
		for(;;) {
			if (mr.scanPI() >= 0 || mr.scanComment() >= 0) {
				continue;
			}
			SPosition sp = mr.getSPosition();
			int pos;
			if ((pos = mr.scanCDATA()) >= 0) {
				sp.setFilePos(sp.getFilePos() + 9);
				String s = mr.getBufPart(pos + 9, mr.getPos()-3);
				appendText(sp, resolveReferences(sp, s, 0));
			} else if ((pos = mr.scanEntity()) >= 0) {
				String en = mr.getBufPart(pos + 1, mr.getPos() - 1);
				String s = _entities.get(en);
				if (s != null) { // should be always
					appendText(sp, s);
					if (s.indexOf('\n') >= 0) {
						int len = en.length() + 2;
						int newLen = s.length();
//						int p = _text.getString().length() - newLen + 1;
						int p = _text.getString().length();
						_text.updatePositions(p, len, newLen, true);
					}
				}
			} else if ((pos = mr.scanText()) >= 0) {
				String s = mr.getBufPart(pos, mr.getPos());
				appendText(sp, resolveReferences(sp, s, 1));
			} else {
				break;
			}
		}
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

////////////////////////////////////////////////////////////////////////////////
// methods called from PreReaderXML
////////////////////////////////////////////////////////////////////////////////

	/** Get report writer.
	 * @return the report writer.
	 */
	final ReportWriter getReportWriter() {return _reporter;}

	/** Set report writer.
	 * @param x the report writer to be set.
	 */
	final void setReportWriter(final ReportWriter x) {_reporter = x;}

	/** Put fatal error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	final void fatal(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.fatal(registeredID, mod));
	}
	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	final void error(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.error(registeredID, mod));
	}

	/** Put ligthError message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	final void lightError(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.lightError(registeredID, mod));
	}

	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	final void warning(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.warning(registeredID, mod));
	}

	/** Put report to reporter.
	 * @param pos SPosition
	 * @param rep Report.
	 */
	final void putReport(final SPosition pos, final Report rep) {
		pos.putReport(rep, _reporter);
	}

	/** Put error to compiler reporter.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	final void error(final long registeredID, final Object... mod) {
		_reporter.error(registeredID, mod);
	}

	/** Put report to compiler reporter.
	 * @param rep Report.
	 */
	final void putReport(final Report rep) {
		_reporter.putReport(rep);
	}
}