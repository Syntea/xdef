package org.xdef.util;

import org.xdef.XDConstants;
import org.xdef.impl.xml.DomBaseHandler;
import org.xdef.impl.xml.XAbstractReader;
import org.xdef.impl.xml.XInputStream;
import org.xdef.impl.xml.XReader;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;
import java.io.File;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;

/** Generation of X-definition from DTD.
 * @author Vaclav Trojan
 */
public class GenDTD2XDEF extends DomBaseHandler implements DeclHandler {

	/** SAXParserFactory used for parsing. */
	private static final SAXParserFactory SPF = SAXParserFactory.newInstance();
	static {
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
			SPF.setFeature(
				"http://apache.org/xml/features/xinclude/fixup-base-uris",
				false); // do not create xml:base attributes
			SPF.setSchema(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/** Maximum number of references to a model. */
	private static final int MAFREF = 128;
	/** Maximum number of recurse call of setRefNumbers method. */
	private static final int MAXRECURSE = 15;

////////////////////////////////////////////////////////////////////////////////
// Generate X-definition
////////////////////////////////////////////////////////////////////////////////

	/** Internal reporter for messages. */
	private ReportWriter _reporter;
	/** Reporter set by user. */
	private ReportWriter _rw;
	/** Root element of generated X-definition. */
	private Element _xdef;
	/** Result X-definition */
	private Document _doc;

////////////////////////////////////////////////////////////////////////////////
//	private InputStream _in;
	private XReader _xReader;
	private byte[] _sourceBytes;
	/** Map with element declarations. */
	private final Map<String, ElemDecl> _elemDeclMap =
		new LinkedHashMap<String, ElemDecl>();
	/** Array with attribute declarations. */
	private List<AttrDecl> _attrDeclList = new ArrayList<AttrDecl>();
	/** true if xml parsing failed. */
	private boolean _xmlFailed;
	/** Creates new instance of XDGenerator. */
	public GenDTD2XDEF() {super(); _rw = null; _reporter = new ArrayReporter();}

	/** Creates a new instance of XDGenerator with reporter.
	 * @param rw reporter.
	 */
	public GenDTD2XDEF(final ReportWriter rw){
		super();
		_reporter = (_rw = rw) == null ? new ArrayReporter() : rw;
	}

	/** Creates a new instance of XDGenerator with source.
	 * @param s string with XML source or URL or file name.
	 */
	public GenDTD2XDEF(final String s) {
		this(s, null);
	}

	/** Creates a new instance of XDGenerator with source and reporter.
	 * @param s string with XML source or URL or file name.
	 * @param rw report handler.
	 */
	public GenDTD2XDEF(final String s, final ReportWriter rw) {
		this(rw);
		if (s.charAt(0) == '<') {
			try {
				_sysId = null;
				_sourceBytes = s.getBytes(Charset.forName("UTF-8"));
				prepareXReader();
			} catch (Exception ex) {
				throw new SRuntimeException();
			}
		} else {
			try {
				URL u = SUtils.getExtendedURL(s);
				_sysId = u.toExternalForm();
				prepareSourceBytes(u.openStream());
				prepareXReader();
			} catch (Exception ex) {
				File f = new File(s);
				try {
					_sysId = f.getCanonicalPath();
					prepareSourceBytes(new FileInputStream(s));
					prepareXReader();
				} catch (Exception exx) {
					_reporter.fatal(SYS.SYS024, s);
					_reporter.checkAndThrowErrors();
				}
			}
		}
	}

	/** Creates a new instance of XDGenerator.
	 * @param file file with source XML.
	 * @param rw reporter.
	 */
	public GenDTD2XDEF(final File file, final ReportWriter rw)	{
		this(rw);
		try {
			_sysId = file.getCanonicalPath();
			prepareSourceBytes(new FileInputStream(file));
			prepareXReader();
		} catch (Exception exx) {
			_reporter.fatal(SYS.SYS024, _sysId);
			_reporter.checkAndThrowErrors();
		}
	}

	/** Creates a new instance of XDGenerator.
	 * @param url URL pointing to source XML.
	 * @param rw report handler.
	 */
	public GenDTD2XDEF(final URL url, final ReportWriter rw) {
		this(rw);
		_sysId = url.toExternalForm();
		try {
			prepareSourceBytes(url.openStream());
			prepareXReader();
		} catch (Exception ex) {
			_reporter.fatal(SYS.SYS024, _sysId);
			_reporter.checkAndThrowErrors();
		}
	}

	/** Create X-definition.
	 * @param rootName name of root XML.
	 * @return created document.
	 */
	public Document genRootXd(final String rootName) {
		_reporter = new ArrayReporter();
		try {
			_isDTD = true;
			try {
				doParse(_xReader);
			} catch (Throwable ex) {
				if (!_xmlFailed) {
					try {
						_xmlFailed = true;
						_isDTD = true;
						String s = "<!DOCTYPE root SYSTEM 'x'>\n"+
							"<" + rootName + "/>";
						ByteArrayInputStream in = new ByteArrayInputStream(
							s.getBytes(Charset.forName("UTF-8")));
						doParse(in, "x.xxx");
					} catch (Exception exx) {
						throw new RuntimeException(ex);
					}
				} else {
					throw new RuntimeException(ex);
				}
			}
		} catch (Exception e) {
			if (e instanceof SRuntimeException) {
				throw (SRuntimeException) e;
			}
			throw new SRuntimeException(e);
		}
		prepareGen();
		genXDef(rootName);
		return _doc;
	}

	private void prepareSourceBytes(final InputStream in) throws Exception {
		_sourceBytes = SUtils.readBytes(in);
		in.close();
	}

	private void prepareXReader() throws Exception {
		XInputStream myInputStream =
			new XInputStream(new ByteArrayInputStream(_sourceBytes));
		_xReader = new XReader(myInputStream);
		_xReader.setHandler(this);
		_xReader.setSysId(_sysId);
	}

	public final ReportWriter getReporter() {return _reporter;}

	////////////////////////////////////////////////////////////////////////////
	// DomBaseHandler
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final void prepareParse(final InputSource is)
		throws IOException, SAXException {
		XMLReader xr;
		try {
			xr = SPF.newSAXParser().getXMLReader();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		setIgnoringComments(false);
		xr.setProperty(
			"http://xml.org/sax/properties/lexical-handler", this);
		xr.setProperty(
			"http://xml.org/sax/properties/declaration-handler", this);
		xr.setFeature( // continue after fatal error
			"http://apache.org/xml/features/continue-after-fatal-error", true);
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
		xr.setEntityResolver(this);
		setXMLReader(xr);
		_is = is;
		xr.parse(is);
		getReader().close();
		setReader(null);
		setXMLReader(null);
		_is = null;
		_locator = null;
	}

	////////////////////////////////////////////////////////////////////////////
	// ContentHandler
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final void startDocument() {}

	@Override
	public final void endDocument() {}

	@Override
	public final void startElement(final String uri,
		final String localName,
		final String qName,
		final Attributes atts) {}

	@Override
	public final void endElement(final String uri,
		final String localName,
		final String qName) {}

	@Override
	public final void characters(final char[] ch,final int off,final int len){}

	@Override
	public final void ignorableWhitespace(final char[] ch,
		final int beg, final int len) {}

	@Override
	public final void processingInstruction(final String target,
		final String data) {}

	////////////////////////////////////////////////////////////////////////////
	// EntityResolver
	////////////////////////////////////////////////////////////////////////////

	@Override
	public InputSource resolveEntity(final String pubID, final String sysID)
		throws IOException {
		if (_xmlFailed) {
			_xmlFailed = false;
			XInputStream xi =
				new XInputStream(new ByteArrayInputStream(_sourceBytes));
			XReader xReader = new XReader(xi);
			xReader.setHandler(this);
			xReader.setSysId(_sysId);
			InputSource is = new InputSource(xReader);
			is.setSystemId(sysID);
			return is;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	// ErrorHandler
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void warning(SAXParseException x) {
		_reporter.warning("", x.getMessage());
	}

	@Override
	public void error(SAXParseException x) {
		_reporter.error("", x.getMessage());
	}

	@Override
	public void fatalError(SAXParseException x) {
		_reporter.fatal("", x.getMessage());
	}

	////////////////////////////////////////////////////////////////////////////
	//XHandler
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final void popReader() {}

	@Override
	public final InputSource pushReader(final XAbstractReader mr) {return null;}

	/////////////////////////////////////////////////////////////
	// LexicalHandler
	/////////////////////////////////////////////////////////////
	@Override
	public final void startEntity(final String name) {}
	@Override
	public final void endEntity(final String name) {}
	@Override
	public final void startCDATA() {}
	@Override
	public final void endCDATA() {}
	@Override
	public final void startDTD(final String name,
		final String publicId,
		final String systemId) {_isDTD = true;}
	@Override
	public final void endDTD() {_isDTD = false;}

	@Override
	public final void comment(final char[] ch,
		final int start, final int length) {}

	////////////////////////////////////////////////////////////////////////////
	// DeclHandler
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final void elementDecl(final String name, final String model) {
		if (_isDTD) {
			_elemDeclMap.put(name, new ElemDecl(name, model));
		}
	}

	@Override
	public final void attributeDecl(final String eName,
		final String aName,
		final String type,
		final String mode,
		final String value) {
		if (_isDTD) {
			_attrDeclList.add(new AttrDecl(eName, aName, type, mode, value));
		}
	}

	@Override
	public final void internalEntityDecl(final String name,final String value){}

	@Override
	public final void externalEntityDecl(final String name,
		final String pubId,
		final String sysId) {}

	////////////////////////////////////////////////////////////////////////////

	private abstract static class SeqItem {
		private final static char MIXED = '*';
		private final static char CHOICE = '|';
		private final static char SEQUENCE = ',';
		final static char REF = 'R';
		char _type;
		char _occurs; // '*' | '+' | '?'
	}

	private static class SeqItemList extends SeqItem {
		final List<SeqItem> _list = new ArrayList<SeqItem>();
		final int size() {return _list.size();}
		final void add(final SeqItem item) {
			_list.add(item);
		}
		final SeqItem get(final int index) {
			return _list.get(index);
		}
		final int indexOf(final String name) {
			for (int i = 0; i < _list.size(); i++) {
				SeqItem item = _list.get(i);
				if (item._type == SeqItem.REF &&
					name.equals(((SeqItemRef) item)._name)) {
					return i;
				}
			}
			return -1;
		}
	}

	private static class SeqItemRef extends SeqItem {
		final String _name;
		SeqItemRef(final String name, final char rep) {
			_type = REF;
			_name = name;
			_occurs = rep;}
	}

	/** [69] PEReference::= '%' Name ';'  (Parameter entity reference). */
	private static boolean isPEReference(final StringParser p) {
		if (!p.isChar('%')) {
			return false;
		}
		p.isNCName((byte) 10);
		p.isChar(';');
		return true;
	}

	/** [28a] DeclSep::= PEReference | S */
	private static boolean isDeclSep(StringParser p) {
		if (p.isSpaces()) {
			if (!isPEReference(p)) {
				return true;
			}
		} else {
			if (!isPEReference(p)) {
				return false;
			}
		}
		p.skipSpaces();
		return true;
	}
	/** [48] cp::= (Name | choice | seq) ('?' | '*' | '+')?
		[49] choice::= '(' S? cp ( S? '|' S? cp )+ S? ')'
		[50] seq::= '(' S? cp ( S? ',' S? cp )* S? ')'
	 */
	private static boolean isCP(final SeqItemList seq, StringParser p) {
		char c;
		if (p.isXMLName((byte) 10)) {
			seq.add(new SeqItemRef(p.getParsedString(), p.isOneOfChars("?*+")));
			return true;
		} else if (p.isChar('(')) {//choice or seq
			SeqItemList seq1 = new SeqItemList();
			skipDeclSep(p);
			isCP(seq1, p);
			skipDeclSep(p);
			if (p.isChar(')')) {// one item - let's reduce DTD
				SeqItem item = seq1.get(0);
				if ((c = p.isOneOfChars("?*+")) != StringParser.NOCHAR) {
					seq1._occurs = c;
				}
				seq.add(seq1);
			} else {
				seq1._type = c = p.isOneOfChars("|,");
				do {
					isDeclSep(p);
					isCP(seq1, p);
					skipDeclSep(p);

				} while (p.isChar(c));
				p.isChar(')');
				if ((c = p.isOneOfChars("?*+")) != StringParser.NOCHAR) {
					seq1._occurs = c;
				}
				seq.add(seq1);
			}
		} else {
			return false;
		}
		return true;
	}

	private static void skipDeclSep(StringParser p) {
		p.skipSpaces();
		if (isPEReference(p)) {
			p.skipSpaces();
		}
	}

	private class ElemDecl {
		final String _name;
		boolean _any;
		int _numText;
		int _references;
		List<AttrDecl> _attList = new ArrayList<AttrDecl>();
		SeqItemList _childList;

		ElemDecl(final String name, final String model) {
			_name = name;
			StringParser p = new StringParser(model);
			if (p.isToken("EMPTY")) {
				return;
			}
			if (p.isToken("ANY")) {
				_any = true;
				return;
			} else if (!p.isChar('(')) {
				return ;
			}
			//mixed | children
			skipDeclSep(p);
			SeqItemList childList = new SeqItemList();
			if (p.isToken("#PCDATA")) {
				_numText++;
				//mixed
				skipDeclSep(p);
				if (p.isChar(')')) { //only #PCDATA
					if (p.isChar('*')) {
						//makes no difference
					}
				} else {
					childList._type = SeqItem.MIXED;
					while (p.isChar('|')) {
						isDeclSep(p);
						if (p.isXMLName((byte) 10)) {
							childList.add(
								new SeqItemRef(p.getParsedString(),'*'));
							skipDeclSep(p);
						}
					}
					if (p.isChar(')')) {
						p.isChar('*');
					}
					_childList = childList;
				}
			} else { //children
				// [47] children::= (choice | seq) ('?' | '*' | '+')?
				// [49] choice::= '(' S? cp ( S? '|' S? cp )+ S? ')'
				// [50] seq::= '(' S? cp ( S? ',' S? cp )* S? ')'
				_childList = new SeqItemList();
				skipDeclSep(p);
				isCP(childList, p);
				skipDeclSep(p);
				char c;
				if (!p.isChar(')')) {
					childList._type = c =  p.isOneOfChars("|,");
					//c == '|' => choice, c == ',' => seq
					do {
						skipDeclSep(p);
						isCP(childList, p);
						skipDeclSep(p);
					} while (p.isChar(c));
					skipDeclSep(p);
					p.isChar(')');
				}
				c = p.isOneOfChars("?*+");
				if (c != StringParser.NOCHAR) {
					childList._occurs = c;
				}
				_childList = childList;
			}
		}
	}
/*
AttType ::= StringType | TokenizedType | EnumeratedType
StringType ::= 'CDATA'
TokenizedType ::= 'ID' | 'IDREF' | 'ENTITY' | 'ENTITIES' | 'NMTOKEN' | 'NMTOKENS'
EnumeratedType ::= NotationType | Enumeration
NotationType ::= 'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'
Enumeration ::= '(' S? Nmtoken (S? '|' S? Nmtoken)* S? ')'
*/
	private static class AttrDecl {
		static final short DEFAULT = 0;
		static final short REQUIRED = 1;
		static final short IMPLIED = 2;
		static final short FIXED = 3;
		static final short IGNORE = 4;
		static final short ILLEGAL = 5;
		final String _name;
		String _type;
		final short _requirement;
		final String _attValue;

		String _eName;
		AttrDecl(final String eName,
			final String aName,
			final String type,
			final String mode,
			final String value) {
			_eName = eName;
			_name = aName;
			_attValue = value;
			if (mode != null) {
				StringParser p = new StringParser(mode.trim());
				int i = p.isOneOfTokens("#REQUIRED", "#IMPLIED", "#FIXED");
				_requirement = i >= 0 ? (short) (i + 1) : DEFAULT;
			} else {
				_requirement = DEFAULT;
			}
			String s;
			if (type != null && !(s = type.trim()).isEmpty()) {
				if ("CDATA".equals(s)) {
					_type = "string";
				} else if ("ID".equals(s) || "IDREF".equals(s)
					|| "IDREFS".equals(s) || "ENTITY".equals(s)
					|| "NMTOKEN".equals(s) || "NMTOKENS".equals(s)) {
					_type = s;
				} else if (s.startsWith("NOTATION")) {
				} else if (s.charAt(0) == '(') {
					_type = "enum(";
					StringTokenizer st = new StringTokenizer(
						s.substring(1, s.length() - 1), "| \t\n");
					if (st.hasMoreTokens()) {
						_type += '\'' +  st.nextToken() + '\'';
						while(st.hasMoreTokens()) {
							_type += ",'" +  st.nextToken() + '\'';
						}
						_type += ")";
					}
				}
			}
		}
	}

	private void prepareGen() {
		for (AttrDecl x : _attrDeclList) {
			ElemDecl y = _elemDeclMap.get(x._eName);
			if (y != null) {
				y._attList.add(x);
			}
		}
		_doc = KXmlUtils.newDocument(XDConstants.XDEF40_NS_URI,"xd:def",null);
		_xdef = _doc.getDocumentElement();
		_xdef.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
			"xmlns:xd", XDConstants.XDEF40_NS_URI);
	}

	private void genAttributes(Element model, ElemDecl elem) {
		if (elem._attList != null && !elem._attList.isEmpty()) {
			for (int i = 0, maxi = elem._attList.size(); i < maxi; i++) {
				AttrDecl att = elem._attList.get(i);
				if (att._name.startsWith("xml")) {
					continue;
				}
				StringBuilder sb = new StringBuilder();
				switch (att._requirement) {
					case AttrDecl.REQUIRED:
						sb.append("required ");
						break;
					case AttrDecl.IMPLIED:
						sb.append("optional ");
						break;
					case AttrDecl.FIXED:
						sb.append("required eq('")
							.append(att._attValue).append("')");
						model.setAttribute(att._name, sb.toString());
						continue;
					default:
						sb.append("required ");
						break;
				}
				sb.append(att._type);
				if (att._attValue != null) {
					sb.append("; onAbsence setText('")
						.append(att._attValue).append("')");
				}
				model.setAttribute(att._name, sb.toString());
			}
		}
	}

	private void genModel(Element xd, ElemDecl elem) {
		Element model = xd.getOwnerDocument().createElement(elem._name);
		xd.appendChild(model);
		genAttributes(model, elem);
		if (elem._any) {
			Element any = xd.getOwnerDocument().createElementNS(
				XDConstants.XDEF40_NS_URI, "xd:any");
			any.setAttributeNS(XDConstants.XDEF40_NS_URI,
				"xd:script", "*; options moreAttributes, moreElements");
			model.appendChild(any);
		} else if (elem._childList == null || elem._childList.size() == 0) {
			if (elem._numText > 0) {
				model.appendChild(xd.getOwnerDocument().createTextNode(
					"? string();"));
			}
		} else {
			if (elem._numText > 0) {
				model.setAttributeNS(XDConstants.XDEF40_NS_URI,
					"xd:text", "* string()");
			}
			char seqType;
			char seqOccurs;
			if (elem._childList != null && elem._childList.size() > 0) {
				seqType = elem._childList._type;
				seqOccurs = elem._childList._occurs;
			} else {
				seqType = SeqItem.SEQUENCE;
				seqOccurs = StringParser.NOCHAR;
			}
			genSequence(model, elem._childList, seqType, seqOccurs);
		}
	}

	private void genRefItem(Element p,
		SeqItemRef refItem,
		char seqOccurs) {
		ElemDecl elem = _elemDeclMap.get(refItem._name);
		Element x = p.getOwnerDocument().createElement(refItem._name);
		String ref = elem._references > 1 ? "ref " + refItem._name : "";
		String occurs;
		if (refItem._occurs == '*' || seqOccurs == '*') {
			occurs = "*";
		} else if (refItem._occurs == '+' || seqOccurs == '+') {
			occurs = "+";
		} else if (refItem._occurs == '?' || seqOccurs == '?') {
			occurs = "?";
		} else {
			occurs = "";
		}
		if (!"".equals(occurs) || !"".equals(ref)) {
			String s;
			if ("".equals(occurs)) {
				s = ref;
			} else if ("".equals(ref)) {
				s = occurs;
			} else {
				s = occurs + "; " + ref;
			}
			x.setAttributeNS(XDConstants.XDEF40_NS_URI, "xd:script", s);
		}
		if ("".equals(ref)) {
			if (elem._childList != null && elem._childList.size() > 0) {
				if (elem._numText > 0) {
					x.setAttributeNS(XDConstants.XDEF40_NS_URI,
						"xd:text", "* string()");
				}
				genAttributes(x, elem);
				char sqType;
				char sqOccurs;
				if (elem._childList != null && elem._childList.size() > 0) {
					sqType = elem._childList._type;
					sqOccurs = elem._childList._occurs;
				} else {
					sqType = SeqItem.SEQUENCE;
					sqOccurs = StringParser.NOCHAR;
				}
				genItem(x, elem._childList, sqType, sqOccurs);
			} else {
				genAttributes(x, elem);
				if (elem._numText > 0) {
					x.appendChild(x.getOwnerDocument()
						.createTextNode("* string()"));
				}
			}
		}
		p.appendChild(x);
	}

	private void genItem(Element p,
		SeqItem item,
		char seqType,
		char seqOccurs) {
		if (item == null) {
			return;
		}
		switch (item._occurs) {
			case '*':
				seqOccurs = '*';
				break;
			case '+':
				if (seqOccurs == '?' || seqOccurs == '*') {
					seqOccurs = '*';
				} else {
					seqOccurs = '+';
				}
				break;
			case '?':
				if (seqOccurs == '*' || seqOccurs == '+') {
					seqOccurs = '*';
				} else {
					seqOccurs = '?';
				}
				break;
			default:
		}
		if (item._type == SeqItem.REF) {
			genRefItem(p, (SeqItemRef) item, seqOccurs);
		} else {
			genSequence(p, (SeqItemList) item, seqType, seqOccurs);
		}
	}

	private void genSequence(final Element p,
		final SeqItemList seq,
		final char seqType,
		final char occurs) {
		char seqOccurs = occurs;
		if (seq == null) {
			return;
		}
		int len;
		if ((len = seq.size()) == 0) {
			return;
		}
		if (len == 1) {
			SeqItem item = seq.get(0);
			if (seq._occurs == seqOccurs || seqOccurs == 0) {
				genItem(p, item, seq._type, seq._occurs);
			} else {
				switch (seq._occurs) {
					case '*':
						seqOccurs = '*';
						break;
					case '+':
						if (item._occurs == '?' || item._occurs == '*') {
							seqOccurs = '*';
						}
						break;
					case '?':
						if (item._occurs == '*' || item._occurs == '+') {
							seqOccurs = '*';
						} else {
							seqOccurs = '?';
						}
						break;
					default:
				}
				genItem(p, item, seq._type, seqOccurs);
			}
			return;
		}
		Element x;
		if (seq._type == SeqItem.CHOICE) {
			x= p.getOwnerDocument().createElementNS(XDConstants.XDEF40_NS_URI,
				"xd:choice");
			if (seq._occurs != 0) {
				x.setAttribute("script", String.valueOf(seq._occurs));
			}
			p.appendChild(x);
		} else if (seq._type == SeqItem.MIXED) {
			x= p.getOwnerDocument().createElementNS(XDConstants.XDEF40_NS_URI,
				"xd:mixed");
			if (seq._occurs == '?') {
				x.setAttribute("script", "?");
			} else if (seq._occurs == '+') {//????????
				x.setAttribute("empty", "false");
			}
			p.appendChild(x);
		} else if (seq._occurs != 0 || (seqType != SeqItem.SEQUENCE
			&& seqType != '\0')) {
			x =p.getOwnerDocument().createElementNS(XDConstants.XDEF40_NS_URI,
				"xd:sequence");
			p.appendChild(x);
			if (seq._occurs != 0) {
				x.setAttribute("script", String.valueOf(seq._occurs));
			}
		} else {
			x = p;
		}
		for (int i = 0; i < len; i++) {
			genItem(x, seq.get(i), seq._type, seqOccurs);
		}
	}

	private void genXDef(final String rootName) {
		prepareGen();
		ElemDecl root = _elemDeclMap.get(rootName);
		if (root == null) {
			throw new SRuntimeException("Root not found: " + rootName);
		}
		_xdef.setAttribute("name", rootName);
		_xdef.setAttribute("root", rootName);
		setRefNumbers(root._childList, 0);
		genModel(_xdef, root);
		root._references = 1;
		for (ElemDecl elem : _elemDeclMap.values()) {
			if (elem == root) {
				continue;
			}
			if (elem._references > 1) {
				genModel(_xdef, elem);
			}
		}
	}

	private void setRefNumbers(final SeqItem item, final int recurse) {
		if (item == null || recurse > MAXRECURSE) {
			return;
		}
		if (item._type == SeqItem.REF) {
			ElemDecl elem = _elemDeclMap.get(((SeqItemRef) item)._name);
			if (++elem._references < MAFREF) {
				if (elem._childList != null && elem._childList.size() > 0) {
					setRefNumbers(elem._childList, recurse + 1);
				}
			}
		} else {
			SeqItemList list = (SeqItemList) item;
			for (int i = 0, maxi = list.size(); i < maxi; i++) {
				SeqItem si = list.get(i);
				if (item != si) {
					setRefNumbers(si, recurse + 1);
				}
			}
		}
	}
}