/*
 * File: CompileXdefPool.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.impl.code.CodeTable;
import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.msg.XML;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.SError;
import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.SThrowable;
import cz.syntea.xdef.sys.SUtils;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.xml.KParsedAttr;
import cz.syntea.xdef.xml.KParsedElement;
import cz.syntea.xdef.xml.KXmlConstants;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDParser;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.impl.XChoice;
import cz.syntea.xdef.impl.XComment;
import cz.syntea.xdef.impl.XData;
import cz.syntea.xdef.impl.XDebugInfo;
import cz.syntea.xdef.impl.XDefinition;
import cz.syntea.xdef.impl.XElement;
import cz.syntea.xdef.impl.XMixed;
import cz.syntea.xdef.impl.XNode;
import cz.syntea.xdef.impl.XOccurrence;
import cz.syntea.xdef.impl.XSelector;
import cz.syntea.xdef.impl.XSelectorEnd;
import cz.syntea.xdef.impl.XSequence;
import cz.syntea.xdef.impl.XThesaurusImpl;
import cz.syntea.xdef.impl.XVariableTable;
import cz.syntea.xdef.impl.ext.XExtUtils;
import cz.syntea.xdef.impl.parsers.XDParseEnum;
import cz.syntea.xdef.model.XMElement;
import cz.syntea.xdef.model.XMNode;
import cz.syntea.xdef.model.XMVariable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.XDContainer;
import cz.syntea.xdef.impl.XPool;
import cz.syntea.xdef.XDValueID;
import static cz.syntea.xdef.sys.SParser.NOCHAR;
import cz.syntea.xdef.xml.KXmlUtils;
import java.util.HashMap;

/** Parse and compile X-definitions.
 * @author Vaclav Trojan
 */
public final class CompileXdefPool extends XDefReader
	implements CodeTable, XDValueID {

////////////////////////////////////////////////////////////////////////////////

	/** MAX_REFERENCE max level of nested references */
	private static final int MAX_REFERENCE = 4096;
	/** index of NameSpace of X-definitions. */
	private static final int NS_XDEF_INDEX = 0;
	/** index of NameSpace of XML. */
	private static final int NS_XML_INDEX = NS_XDEF_INDEX + 1; //1
	/** index of NameSpace of XML NameSpace. */
	private static final int NS_XMLNS_INDEX = NS_XML_INDEX + 1; //2
	/** index of NameSpace of XLink. */
	private static final int NS_XLINK_INDEX = NS_XMLNS_INDEX + 1; //3
	/** index of NameSpace of XInclude. */
	private static final int NS_XINCLUDE_INDEX = NS_XLINK_INDEX + 1; //4
	/** index of NameSpace of XML Schema. */
	private static final int NS_XMLSCHEMA_INDEX = NS_XINCLUDE_INDEX + 1; //5
//	/** index of NameSpace of X-definition include. */
//	private static final int NS_XDEF_INCLUDE_INDEX = NS_XMLSCHEMA_INDEX + 1; //6
	/** The nesting level of XML node. */
	private int _level;
	/** Actual node */
	private PNode _actPNode;
	/** Code generator. */
	private CompileCode _codeGenerator;
	/** The script compiler. */
	private CompileXScript _scriptCompiler;
	/** counter of unknown objects. */
	private int _unknownCounter;
	private Element _includeElement;
	/** True if and only if version of XML document is 1.0.*/
	private boolean _xmlVersion1;
	/** Table of names of parsed X-definitions. */
	private ArrayList<String> _xdefNames;
	/** Created nodes. */
	private ArrayList<PNode> _xdefNodes;
	/** Source files table - to prevent to doParse the source twice. */
	private ArrayList<Object> _sourceFiles;
	/** Include list of URL's. */
	private ArrayList<Object> _includeList;
	/** List of macro definitions. */
	private Map<String, XScriptMacro> _macros;
	/** Actual node stack. */
	private ArrayList<XNode> _nodeList;
	/** Table of NameSpace prefixes. */
	private final Map<String, Integer> _predefinedNSPrefixes;
	/** Table of definitions */
	private final Map<String, XDefinition> _xdefs;
	/** Display mode */
	private final byte _displayMode;
	private final ArrayList<PNode> _listBNF = new ArrayList<PNode>();
	private final ArrayList<PNode> _listDecl = new ArrayList<PNode>();
	private final ArrayList<PNode> _listComponent = new ArrayList<PNode>();
	/** Array of thesaurus sources. */
	private final ArrayList<PNode> _thesaurus = new ArrayList<PNode>();
	/** External classes. */
	private Class<?>[] _extClasses;
	/** Creates a new instance of XDefCompiler
	 * @param xp The XDefPool object.
	 * @param reporter The reporter.
	 * @param extClasses The external classes.
	 * @param xdefs Table of X-definitions.
	 */
	public CompileXdefPool(final XDPool xp,
		final ReportWriter reporter,
		final Class<?>[] extClasses,
		final Map<String, XDefinition> xdefs) {
		super(reporter);
		ClassLoader classLoader =
			Thread.currentThread().getContextClassLoader();
		_xdefs = xdefs;
//		setLineInfoFlag(true); // generate line information
		_xdefNames = new ArrayList<String>();
		_xdefNodes = new ArrayList<PNode>();
		_macros = new TreeMap<String, XScriptMacro>();
		_sourceFiles = new ArrayList<Object>();
		_includeList = new ArrayList<Object>();
		_nodeList = new ArrayList<XNode>();
		_predefinedNSPrefixes = new TreeMap<String, Integer>();
		/** DisplayMode. */
		_displayMode = xp.getDisplayMode();
		_codeGenerator = new CompileCode(extClasses,
			2, xp.isDebugMode(), xp.isIgnoreUnresolvedExternals());
		_codeGenerator._namespaceURIs.add("."); //dummy namespace
		_codeGenerator._namespaceURIs.add(XMLConstants.XML_NS_URI);
		_codeGenerator._namespaceURIs.add(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		_codeGenerator._namespaceURIs.add(KXmlConstants.XLINK_NS_URI);
		_codeGenerator._namespaceURIs.add(KXmlConstants.XINCLUDE_NS_URI);
		_codeGenerator._namespaceURIs.add(//schema
			XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		_predefinedNSPrefixes.put(XMLConstants.XML_NS_PREFIX, //"xml"
			NS_XML_INDEX);
		_predefinedNSPrefixes.put(XMLConstants.XMLNS_ATTRIBUTE, //"xmlns",
			NS_XMLNS_INDEX);
		_scriptCompiler = new CompileXScript(_codeGenerator, _xmlVersion1,
			_predefinedNSPrefixes, _macros, classLoader);
		_scriptCompiler.setReportWriter(getReportWriter());
		_unknownCounter = 0;
	}

	/** Get external classes used in x-definition methods.
	 * @return array of objects.
	 */
	public Class<?>[] getExternals() {return _extClasses;}

	/** Set User objects. This method is just to keep compatibility with
	 * previous versions.
	 * @param extObjects array of objects.
	 */
	public void setExternals(final Class<?>... extObjects) {
		_codeGenerator.setExternals(extObjects);
		_extClasses = _codeGenerator.getExternals();
	}

	/** Set class loader. The class loader must be set before setting sources.
	 * @param loader the ClassLoader.
	 */
	public final void setClassLoader(final ClassLoader loader) {
		_scriptCompiler.setClassLoader(loader);
	}

	/** Get the ClassLoader used to load Java classes.
	 * @return ClassLoader used to load Java classes.
	 */
	public final ClassLoader getClassLoader() {
		return _scriptCompiler.getClassLoader();
	}

	/** Process include list from header of X-definition. */
	private void processIncludeList() {
		/** let's check some attributes of X-definition.*/
		SBuffer include = _actPNode.getXdefAttr("include", false, true);
		processIncludeList(include, _includeList,
			_actPNode._name.getSysId(), getReportWriter());
	}

	/** Process list of file specifications and/or URLs. Result of list is added
	 * to the includeList (if the includeList already contains an item the
	 * item is skipped. If the argument reporter is not <tt>null</tt> and an
	 * error occurs then the error is written to reporter. If reporter is
	 * <tt>null</tt> then an SRuntimeException is thrown.
	 * @param include SBuffer with list of items, separator is ",". Wildcard
	 * characters are permitted.
	 * @param includeArray ArrayList with items.
	 * @param sysId actual path.
	 * @param reporter report writer or <tt>null</tt>.
	 * @throws SRuntimeException if list contains error and reporter is null.
	 */
	private static void processIncludeList(final SBuffer include,
		final ArrayList<Object> includeArray,
		final String sysId,
		final ReportWriter reporter) {
		if (include == null) {
			return;
		}
		ReportWriter myreporter =
			reporter == null ? new ArrayReporter() : reporter;
		StringTokenizer st =
			new StringTokenizer(include.getString(), " \t\n\r\f,");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.startsWith("https:") || s.startsWith("http:") ||
				s.startsWith("ftp:") || s.startsWith("file:")) {
				try {
					URL u = new URL(URLDecoder.decode(s, "UTF-8"));
					if (includeArray.contains(u)) {
						continue;
					}
					includeArray.add(u);
				} catch (Exception ex) {
					myreporter.error(SYS.SYS024, s); //File doesn't exist: &{0}
				}
			} else {
				if (s.indexOf(':') < 0 &&
					!s.startsWith("/") && !s.startsWith("\\")) {//no path
					if (sysId != null) {//take path from sysId
						try {
							URL u = new URL(URLDecoder.decode(sysId, "UTF-8"));
							if (!"file".equals(u.getProtocol())) {
								String v =u.toExternalForm().replace('\\', '/');
								int i = v.lastIndexOf('/');
								if (i >= 0) {
									v = v.substring(0, i + 1);
								}
								u = new URL(URLDecoder.decode(v + s, "UTF-8"));
								if (includeArray.contains(u)) {
									continue;
								}
								includeArray.add(u);
								continue;
							} else {
								String p = new File(u.getFile()).
									getCanonicalPath().replace('\\', '/');
								int i = p.lastIndexOf('/');
								s = i>0 ? p.substring(0, i + 1) + s : ('/' + s);
							}
						} catch (Exception ex) {
							s = ""; // no file
						}
					}
				}
				File[] list = SUtils.getFileGroup(s);
				if (list.length == 0) {
					myreporter.error(SYS.SYS024, s); //File doesn't exist: &{0}
				} else {
					for (File f: list) {
						try {
							if (f.canRead()) {
								if (includeArray.contains(
									f.getCanonicalPath())) {
									continue; //file already exists
								}
								includeArray.add(f);
								continue;
							}
						} catch (IOException ex) {}
						//File doesn't exist: &{0}
						myreporter.error(SYS.SYS024, s);
					}
				}
			}
		}
		if (reporter == null && myreporter.errors()) {
			myreporter.checkAndThrowErrors();
		}
	}

	/** Report about not legal attributes. All allowed attributes should be
	 * processed and removed.
	 * @param pnode node to be checked.
	 */
	private void reportNotAllowedAttrs(final PNode pnode) {
		for (AttrValue attr: pnode._attrs) {
			//Attribute '&{0}' not allowed here
			error(attr._value, XDEF.XDEF254, attr._name);
		}
		pnode._attrs.clear();
	}

	/** Report deprecated symbol.
	 * @param spos position where to report.
	 * @param symbol deprecated symbol.
	 * @param replace what should be done.
	 */
	private void reportDeprecated(final SPosition spos,
		final String symbol,
		final String replace) {
		//&{0} is deprecated. Please use &{1} instead
		warning(spos, XDEF.XDEF998, symbol, replace);
	}

	@Override
	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 * @param parsedElem contains name of the element, name space URI and
	 * the list of attributes.
	 */
	void elementStart(final KParsedElement parsedElem) {
		_xmlVersion1 = "1.1".equals(getXmlVersion());
		String qName = parsedElem.getParsedName();
		if (_includeElement == null) {
			if (_actPNode != null && _actPNode._value != null &&
				_actPNode._value.getString().length() > 0) {
				processText();
			}
			_actPNode = new PNode(qName,
				parsedElem.getParsedNameSourcePosition(),
				_actPNode,
				_actPNode==null? (byte) 0 : _actPNode._xdVersion);
		}
		String elemPrefix;
		String elemLocalName;
		int ndx;
		if ((ndx = qName.indexOf(':')) >= 0) {
			elemPrefix = qName.substring(0, ndx);
			_actPNode._localName = elemLocalName = qName.substring(ndx + 1);
		} else {
			elemPrefix = "";
			_actPNode._localName = elemLocalName = qName;
		}
		_actPNode._nsURI = parsedElem.getParsedNSURI();
		if (_level == -1) {
			_codeGenerator._namespaceURIs.remove(0);
			String uri = parsedElem.getParsedNSURI();
			if ("def".equals(elemLocalName)
				|| "thesaurus".equals(elemLocalName)
				|| "declaration".equals(elemLocalName)
				|| "BNFGrammar".equals(elemLocalName)
				|| "collection".equals(elemLocalName))  {
				String projectNS; // = XDConstants.XDEF20_NS_URI;
				KParsedAttr ka;
				byte ver;
				if ((ka = parsedElem.getAttrNS(
					KXmlConstants.XDEF20_NS_URI, "metaNamespace")) != null
					|| (ka = parsedElem.getAttrNS(
						KXmlConstants.XDEF31_NS_URI, "metaNamespace")) != null){
					projectNS = ka.getValue().trim();
					ver=KXmlConstants.XDEF31_NS_URI.equals(ka.getNamespaceURI())
						 ? XDConstants.XD31_ID : XDConstants.XD20_ID;
					if (XExtUtils.uri(projectNS).errors()) {
						//Attribute 'metaNamespace' must contain a valid URI
						error(ka.getPosition(), XDEF.XDEF253);
					}
					parsedElem.remove(ka);
				} else {
					if (KXmlConstants.XDEF20_NS_URI.equals(uri)
						|| KXmlConstants.XDEF31_NS_URI.equals(uri)) {
						ver = KXmlConstants.XDEF31_NS_URI.equals(uri)
							? XDConstants.XD31_ID : XDConstants.XD20_ID;
						projectNS = uri;
					} else {
						//Namespace of X-definitions is required
						error(_actPNode._name, XDEF.XDEF256);
						projectNS = KXmlConstants.XDEF31_NS_URI;
						ver = XDConstants.XD31_ID;
					}
				}
				_actPNode._xdVersion = ver;
				_codeGenerator._parser._xdVersion = ver;
				_codeGenerator._namespaceURIs.add(0, projectNS);
			} else {
				_codeGenerator._namespaceURIs.add(0, uri);
				//X-definition or X-collection excpected
				error(_actPNode._name, XDEF.XDEF255);
			}
		}
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			String key = ka.getName();
			String value = ka.getValue();
			if (key.startsWith("xmlns")) { //addAttr namespace URI to the list.
				int nsndx = _codeGenerator._namespaceURIs.indexOf(value.trim());
				if (nsndx < 0) {
					nsndx = _codeGenerator._namespaceURIs.size();
					_codeGenerator._namespaceURIs.add(value.trim());
				}
				if (key.length() == 5) { //default prefix
					_actPNode._nsPrefixes.put("", nsndx);
				} else if (key.charAt(5) == ':') { //prefix name
					_actPNode._nsPrefixes.put(key.substring(6), nsndx);
				}
			} else if ("collection".equals(elemLocalName) &&
				key.startsWith("impl-")) {//continue; ignore, just documentation
			} else {
				AttrValue item = new AttrValue(key,
					new SBuffer(value, ka.getPosition()), null, -1);
				if ((ndx = key.indexOf(':')) >= 0) {
					String prefix = key.substring(0, ndx);
					item._localName = key.substring(ndx + 1);
					Integer nsndx = _actPNode._nsPrefixes.get(prefix);
					if (nsndx == null) {
						String u;
						if ((u = ka.getNamespaceURI()) != null) {
							int x = _codeGenerator._namespaceURIs.indexOf(u);
							if (x < 0) {
								nsndx = _codeGenerator._namespaceURIs.size();
								_codeGenerator._namespaceURIs.add(u);
							} else {
								nsndx = x;
							}
							_actPNode._nsPrefixes.put(prefix, nsndx);
						}
					}
					if (nsndx != null) {
						item._nsURI = _codeGenerator._namespaceURIs.get(nsndx);
						if ((item._nsindex=nsndx) == NS_XDEF_INDEX &&
							"script".equals(item._localName)) {
							StringParser p = new StringParser(
								new SBuffer(value, ka.getPosition()));
							p.skipSpaces();
							if (p.isToken("template")) {
								p.skipSpaces();
								if (!p.eos() && !p.isChar(';')) {
									error(p, XDEF.XDEF425);//Script error
								}
								_actPNode._template = true;
							}
						}
					} else {
						item._nsindex = -1;
					}
				} else {
					item._localName = key;
					item._nsindex = -1;
				}
				_actPNode._attrs.add(item);
			}
		}
		Integer nsuriIndex = _actPNode._nsPrefixes.get(elemPrefix);
		if (nsuriIndex != null) {
			int urindx;
			if (((urindx = nsuriIndex) == NS_XINCLUDE_INDEX)) {
				String nsuri = _codeGenerator._namespaceURIs.get(urindx);
				Element el;
				if (_includeElement == null) {
					el = _includeElement = KXmlUtils.newDocument(nsuri,
						_actPNode._name.getString(), null).getDocumentElement();
				} else {
					el = _includeElement.getOwnerDocument().createElementNS(
						nsuri, _actPNode._name.getString());
					_includeElement.appendChild(el);
				}
				for (AttrValue aval: _actPNode._attrs) {
					if (aval._nsindex < 0) {
						el.setAttribute(aval._name, aval._value.getString());
					} else {
						el.setAttributeNS(_codeGenerator.
							_namespaceURIs.get(aval._nsindex),
							aval._name,
							aval._value.getString());
					}
				}
				return;
			} else {
				_actPNode._nsindex = urindx;
			}
		}
		if (_level == -1) {
			if ("collection".equals(elemLocalName)) {
				processIncludeList();
				reportNotAllowedAttrs(_actPNode);
			} else if ("BNFGrammar".equals(elemLocalName)) {
				_level++;
				 _listBNF.add(_actPNode);
			} else if ("thesaurus".equals(elemLocalName)) {
				_level++;
				_thesaurus.add(_actPNode);
			} else if ("declaration".equals(elemLocalName)) {
				_level++;
				_listDecl.add(0, _actPNode);
			} else {
				if (!"def".equals(elemLocalName)) {
					error(_actPNode._name, XDEF.XDEF259);//X-definition expected
				}
				_level++;
				String defName = _actPNode.getNameAttr(false, true);
				if (defName == null) {
					defName = "";
				}
				processIncludeList();
				if (_xdefNames.contains(defName)) {
					if (defName.length() == 0) {
						//Only one X-definition in a collection may be
						//without name
						error(_actPNode._name, XDEF.XDEF212);
					} else {
						//X-definition '&{0}' already exists
						error(_actPNode._name, XDEF.XDEF303, defName);
					}
				} else {
					_xdefNames.add(defName);
					_xdefNodes.add(_actPNode);
				}
			}
		} else {
			_level++;
			_actPNode._parent._childNodes.add(_actPNode);
		}
	}

	@Override
	/** This method is invoked when parser reaches the end of element. */
	final void elementEnd() {
		if (_includeElement != null) {
			String href = null;
			String parse = null;
			String ns = _includeElement.getPrefix();
			String uri = _includeElement.getNamespaceURI();
			ns = ns == null || ns.length() == 0 ? "xmlns" : "xmlns:" + ns;
			NamedNodeMap nm = _includeElement.getAttributes();
			if (nm.getLength() > 0) { //other attributes
				for (int i = 0; i < nm.getLength(); i++) {
					Node n = nm.item(i);
					String name = n.getNodeName();
					if ("href".equals(name)) {
						href = _includeElement.getAttribute("href");
					} else if ("parse".equals(name)) {
						parse = _includeElement.getAttribute("parse");
					} else if (!ns.equals(name)) {
						//Xinclude - unknown attribute &{0}
						error(_actPNode._name, XML.XML305, n.getNodeName());
					}
				}
			}
			_includeElement = null;
			_actPNode = _actPNode._parent;
			return;
		}
		if (_actPNode._value != null &&
			_actPNode._value.getString().length() > 0) {
			processText();
		}
		_level--;
		_actPNode = _actPNode._parent;
	}

	@Override
	/** New text value of current element parsed.
	 * @param text SBuffer with value of text node.
	 */
	public final void text(final SBuffer text) {
		if (_includeElement != null) {
			return;
		}
		if (_actPNode._template && _level > 0 &&
			_actPNode._nsindex != NS_XDEF_INDEX) {
			SBuffer sval = null;
			if (text != null) {
				sval = new SBuffer(text.getString(), text);
			}
			if (_actPNode._value == null) {
				_actPNode._value = sval;
			} else {
				_actPNode._value.appendToBuffer(sval);
			}
			return;
		}
		if (text == null) {
			return; // no string
		}
		String s = text.getString();
		int len = s.length() - 1;
		while (len >= 0 && s.charAt(len) <= ' ') {
			len--;
		}
		if (len < 0) {
			return; // empty string
		}
		SBuffer sb =new SBuffer(s, text);
		if (_actPNode._value == null) {
			_actPNode._value = sb;
		} else {
			if (_actPNode._value == null) {
				_actPNode._value = sb;
			} else {
				_actPNode._value.appendToBuffer(sb);
			}
		}
		if (_actPNode._nsindex == NS_XDEF_INDEX &&
			"def".equals(_actPNode._localName)) {
			_scriptCompiler.setSourceBuffer(_actPNode._value);
			_scriptCompiler._xdVersion = _actPNode._xdVersion;
			// it still may be a comment
			if (_scriptCompiler.nextSymbol() != XScriptParser.NOCHAR) {
				//Text value is not allowed here
				lightError(_actPNode._value, XDEF.XDEF260);
			}
			_actPNode._value = null; //prevent repeated message
		}
	}

	/** Generate text node */
	private void genTextNode() {
		String name = "";
		for (String prefix : _actPNode._nsPrefixes.keySet()) {
			if (_actPNode._nsPrefixes.get(prefix) == NS_XDEF_INDEX) {
				name = prefix + ":text";
				break;
			}
		}
		PNode p = new PNode(name,
			new SPosition(_actPNode._value), _actPNode,  _actPNode._xdVersion);
		p._nsURI = _codeGenerator._namespaceURIs.get(NS_XDEF_INDEX);
		p._nsindex = NS_XDEF_INDEX;
		p._localName = "text";
		p._value = _actPNode._value;
		_actPNode._value = null;
		_level++;
		_actPNode._childNodes.add(p);
		_level--;
	}

	private void processText() {
		if (_actPNode._template && _level > 0 &&
			_actPNode._nsindex != NS_XDEF_INDEX) {
			genTextNode();
			return;
		}
		_scriptCompiler.setSourceBuffer(_actPNode._value);
		if (_scriptCompiler.nextSymbol() == XScriptParser.NOCHAR) {
			_actPNode._value = null;
			return;
		}
		if (_actPNode._nsindex == NS_XDEF_INDEX) {
			if ("text".equals(_actPNode._localName) ||
				"BNFGrammar".equals(_actPNode._localName) ||
				"thesaurus".equals(_actPNode._localName) ||
				"declaration".equals(_actPNode._localName) ||
				"component".equals(_actPNode._localName) ||
				"macro".equals(_actPNode._localName)) {
				return; //text is processed in the pnode
			} else if (!"mixed".equals(_actPNode._localName) &&
				!"choice".equals(_actPNode._localName) &&
				!"list".equals(_actPNode._localName) &&
//				!"PI".equals(_actPNode._localName) && //TODO
//				!"comment".equals(_actPNode._localName) && //TODO
//				!"document".equals(_actPNode._localName) && //TODO
//				!"value".equals(_actPNode._localName) && //TODO
//				!"attlist".equals(_actPNode._localName) && //TODO
				!"sequence".equals(_actPNode._localName) &&
				!"any".equals(_actPNode._localName)) {
				//Text value is not allowed here
				lightError(_actPNode._value, XDEF.XDEF260);
				_actPNode._value = null; //prevent repeated message
				return;
			}
		}
		if (_level == 0) {
			//Text value not allowed here
			lightError(_actPNode._value, XDEF.XDEF260);
			_actPNode._value = null; //prevent repeated message
		} else {
			genTextNode(); //generate text node
		}
	}

	/** Parse string and addAttr it to the set of definitions.
	 * @param source The source string with definitions.
	 */
	public final void parseString(final String source) {
		if (_sourceFiles.indexOf(source) >= 0 || source.length() == 0) {
			return;  //we ignore already declared or empty strings
		}
		if (source.charAt(0) == '<') {
			parseString(source, "STRING");
		} else {
			try {
				URL u = new URL(source);
				parseURL(u);
			} catch (Exception ex) {
				parseFile(new File(source));
			}
		}
	}

	/** Parse string and addAttr it to the set of definitions.
	 * @param source source string with definitions.
	 * @param srcName name of source (URL).
	 */
	public final void parseString(final String source, final String srcName) {
		if (_sourceFiles.indexOf(source) >= 0 || source.length() == 0) {
			return;  //we ignore already declared or empty strings
		}
		char c;
		if ((c = source.charAt(0)) <= ' ' || c == '<') {
			_sourceFiles.add(source);
			try {
				parseStream(new ByteArrayInputStream(source.getBytes("UTF-8")),
					srcName);
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			parseFile(source);
		}
	}

	/** Parse file with source definition and addAttr it to the set
	 * of definitions.
	 * @param fileName The name of the file with with definitions.
	 */
	public final void parseFile(final String fileName) {
		parseFile(new File(fileName));
	}

	/** Parse file with source definition and addAttr it to the set
	 * of definitions.
	 * @param file The file with with definitions.
	 */
	public final void parseFile(final File file) {
		try {
			URL url = file.toURI().toURL();
			for (Object o: _sourceFiles) {
				if (o instanceof URL && url.equals(o)) {
					return; //found in list
				}
			}
			_sourceFiles.add(url);
			parseStream(new FileInputStream(file), url.toExternalForm());
		} catch (RuntimeException ex) {
			throw ex;
		} catch (IOException ex) {
			//Can't read X-definition from the file &{0}
			throw new SRuntimeException(XDEF.XDEF902,
				(file == null ? (String) null : file.getAbsolutePath()));
		}
	}

	/** Parse file with source definition and addAttr it to the set
	 * of definitions.
	 * @param in The input stream with the definition.
	 * @param srcName name of source data used in reporting (SysId) or
	 * <tt>null</tt>.
	 */
	public final void parseStream(final InputStream in, final String srcName) {
		if (_sourceFiles.contains(in)) {
			return;
		}
		_sourceFiles.add(in);
		_sysId = srcName;
		_level = -1;
		_actPNode = null;
		try {
			doParse(in, srcName);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			_actPNode = null; //just let gc to do the job
			if (_displayMode > XDPool.DISPLAY_FALSE) {
				if (!(ex instanceof SThrowable)) {
					getReportWriter().error(SYS.SYS066, //Internal error&{0}{: }
						"when parsing document\n" + ex);
				}
			} else {
				if (ex instanceof SThrowable &&
					"SYS012".equals(((SThrowable) ex).getMsgID())) {
					throw (SRuntimeException) ex; //Errors detected&{0}{: }
				} else {
					//Internal error: &{0}
					throw new SRuntimeException(SYS.SYS066,
						ex, "when parsing document\n" + ex);
				}
			}
		}
		_actPNode = null; //just let gc to do the job
	}

	/** Parse data with source definition given by URL and addAttr it
	 * to the set of definitions.
	 * @param url The URL pointing to file with the definition.
	 */
	public final void parseURL(final URL url) {
		if (url == null) {
			//Can't read X-definition from the file &{0}
			getReportWriter().error(XDEF.XDEF902, "null");
			return;
		}
		for (Object o: _sourceFiles) {
			if (o instanceof URL && url.equals((URL) o)) {
				return; //prevents to doParse the source twice.
			}
		}
		String srcName = url.toExternalForm();
		_sourceFiles.add(srcName);
		try {
			parseStream(url.openStream(), srcName);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			//Can't read X-definition from the file &{0}
			throw new SRuntimeException(XDEF.XDEF902, srcName);
		}
	}

	/** Add node to parent.
	 * @param parentNode The node where the new node will be added.
	 * @param xNode The node to be added.
	 * @param level The nesting level of new node.
	 */
	private void addNode(final XNode parentNode,
		final XNode xNode,
		final int level,
		final SPosition pos) {
		short parentKind = parentNode.getKind();
		short nodeKind = xNode.getKind();
		if (parentKind == XNode.XMDEFINITION) { //"model" level
			if (nodeKind == XNode.XMELEMENT) {
				((XElement) xNode)._definition = (XDefinition) parentNode;
				((XElement) xNode).setXDNamespaceContext(
					_scriptCompiler._g.getXDNamespaceContext());
			} else if (nodeKind == XNode.XMTEXT) {
				error(pos, XDEF.XDEF260); //Text value not allowed here
			}
		} else {
			XElement parentXel;
			if (parentKind == XNode.XMELEMENT) {
				parentXel = (XElement) parentNode;
			} else {
				// here it can be only group, we find the parent element model
				parentXel = null;
				int lev = level - 1;
				while (lev >= 0) {
					XNode xnode = _nodeList.get(lev);
					if (xnode.getKind()!= XNode.XMELEMENT) {
						lev--;
						continue;
					}
					parentXel = (XElement) xnode;
					break;
				}
				if (parentXel == null) {
					//Internal error: &{0}
					throw new SError(XDEF.XDEF315, "No XElement");
				}
			}
			if (nodeKind == XNode.XMELEMENT) {
				((XElement) xNode)._definition = parentXel._definition;
				((XElement) xNode).setXDNamespaceContext(
					_scriptCompiler._g.getXDNamespaceContext());
			}
			parentXel.addNode(xNode);
		}
		_nodeList.add(level, xNode);
	}

	/** Check if the node has no nested child nodes. */
	private void chkNestedElements(final PNode pnode) {
		for (PNode p: pnode._childNodes) {
			//Nested child elements are not allowed here
			error(p._name, XDEF.XDEF219);
		}
	}

	void compileComponentDeclaration() {
		for (;;) {
			_scriptCompiler.skipBlanksAndComments();
			if (_scriptCompiler.eos()) {
				break;
			}
			int ndx;
			if (!_scriptCompiler.isChar('%')) {
				error(XDEF.XDEF356, //'&{0}' expected
					"\"%class\",\"%interface\",\"%bind\",\"%ref\",\"%enum\"");
				if (_scriptCompiler.findCharAndSkip(';')) {
					continue;
				}
				break;
			}
			SPosition spos = _scriptCompiler.getPosition();
			String result = _scriptCompiler.parseComponent(true);
			if (result == null) {
				if (_scriptCompiler.findCharAndSkip(';')) {
					continue;
				}
				break;
			}
			if (result.indexOf("[1]") >= 0) {
				// remove all occurrences of "[1]"
				result = SUtils.modifyString(result, "[1]", "");
			}
			_scriptCompiler.skipBlanksAndComments();
			if (result.startsWith("%enum ")) {
				ndx = result.indexOf(' ', 6);
				String enumClass = result.substring(6, ndx);
				SBuffer sbf = new SBuffer(enumClass, spos);
				String enumTypeName = result.substring(ndx+1);
				if (_codeGenerator._enums.put(enumTypeName, sbf) != null) {
					_scriptCompiler.error(//Duplicity of enumeration &{0}
						spos, XDEF.XDEF379, enumTypeName);
				}
			} else if (result.startsWith("%bind")) {
				ndx = result.indexOf(" %link ");
				StringTokenizer st =
					new StringTokenizer(result.substring(ndx+7), "\t\n\r ,");
				SBuffer sbf = new SBuffer(result.substring(6, ndx), spos);
				while(st.hasMoreTokens()) {
					if (_codeGenerator._binds.put(st.nextToken(), sbf)!=null) {
						_scriptCompiler.error(spos,//Duplicity of reference &{0}
							XDEF.XDEF355, sbf.getString());
					}
				}
			} else if (result.startsWith("%ref ")) {
				ndx = result.indexOf(" %link ");
				String className = result.substring(0, ndx);
				String modelName = result.substring(ndx + 7);
				ndx = modelName.indexOf('#');
				String xdName = modelName.substring(0, ndx);
				modelName = modelName.substring(ndx + 1);
				String model = xdName + '#' + modelName;
				SBuffer s = _codeGenerator._components.put(
					model, new SBuffer(className, spos));
				if (s != null) {
					//Duplicate declaration of &{0}
					error(spos, XDEF.XDEF351, "interface;"+model);
				}
				_scriptCompiler.skipBlanksAndComments();
				_scriptCompiler.isChar(';');
				continue;
			} else {
				ndx = result.indexOf(" %link ");
				String modelName = result.substring(ndx + 7);
				result = result.substring(0, ndx);
				ndx = modelName.indexOf('#');
				String xdName = modelName.substring(0, ndx);
				modelName = modelName.substring(ndx + 1);
				_scriptCompiler.skipBlanksAndComments();
				String className, extension;
				if (result.startsWith("%interface ")) {
					extension = result.substring(1);
					className = "";
				} else { // %class
					ndx = result.indexOf(' ', 7);
					if (ndx < 0) {
						extension = "";
						className = result.substring(7);
					} else {
						extension = result.substring(ndx); //with space!
						int ndx1;
						if ((ndx1 = extension.indexOf("%interface ")) > 0) {
							extension = extension.substring(0, ndx1)
								+ extension.substring(ndx1+1);
						}
						className = result.substring(7, ndx);
					}
				}
				String model = xdName + '#' + modelName;
				SBuffer s = _codeGenerator._components.get(model);
				if (s != null) {
					String t = s.getString();
					if (result.startsWith("%class ")
						&& t.startsWith("interface ")) {
						if (result.indexOf(" %interface ") > 0) {
							//Duplicate declaration of &{0}
							error(spos, XDEF.XDEF351,
								"interface;"+model);
						} else {
							t = result.substring(7) + ' ' + t;
							s = new SBuffer(t, spos);
							_codeGenerator._components.put(model, s);
						}
					} else if (result.startsWith("%interface ")) {
						if (t.contains(" interface ")) {
							//Duplicate declaration of &{0}
							error(spos, XDEF.XDEF351,
								"interface;"+model);
						} else {
							t += ' ' + result.substring(1);
							s = new SBuffer(t, s);
							_codeGenerator._components.put(model, s);
						}
					} else {
						s = _codeGenerator._components.get(model);
						if (s != null) {
							if (!s.getString().startsWith("interface ")) {
								//Duplicate declaration of &{0}
								error(s, XDEF.XDEF351, model);
								//Duplicate declaration of &{0}
								error(spos, XDEF.XDEF351, model);
								// do not repeat this message
								_codeGenerator._components.remove(model);
							}
						}
					}
				} else {
					for (Entry<String, SBuffer> e:
						_codeGenerator._components.entrySet()) {
						String cn = e.getValue().getString();
						ndx = cn.indexOf(" extends ");
						if (ndx > 0) {
							cn = cn.substring(0, ndx);
						}
						ndx = cn.indexOf(" implements ");
						if (ndx > 0) {
							cn = cn.substring(0, ndx);
						}
						ndx = cn.indexOf(" interface ");
						if (ndx > 0) {
							cn = cn.substring(0, ndx);
						}
						if (className.equals(cn) && !model.equals(e.getKey())) {
							//Duplicate declaration of class &{0}
							// for XComponent &{1}
							error(spos, XDEF.XDEF352, className, model);
						}
					}
					if (!className.contains(" implements ")
						|| !extension.startsWith(" implements ")) {
						className += extension;
					} else {
						className += extension.substring(12);
					}
					s = _codeGenerator._components.put(
						model, new SBuffer(className, spos));
					if (s != null && !className.equals(s.getString())) {
						//Duplicate declaration of class &{0}
						// for XComponent &{1}
						error(spos, XDEF.XDEF352, className, model);
					}
				}
			}
			if (!_scriptCompiler.isChar(';')) {
				spos = _scriptCompiler.getPosition();
				if (!_scriptCompiler.eos()) {
					//'&{0}' expected
					_scriptCompiler.error(spos, XDEF.XDEF356, ";");
					if (!_scriptCompiler.findCharAndSkip(';')) {
						break;
					}
				}
			}
		}
	}

	private void compileMehodsAndClassesAttrs() {
		for (int i = 0; i < _xdefNames.size(); i++) {
			PNode pnode = _xdefNodes.get(i);
			SBuffer sval;
			if ((sval = pnode.getXdefAttr("methods", false, true)) != null
				&& !_codeGenerator._ignoreUnresolvedExternals) {
				if (pnode._xdVersion >= XDConstants.XD31_ID) {
					reportDeprecated(sval,
					"Attribute \"methods\"",
					"<xd:declaration> external method { ... } ...");
				}
				_scriptCompiler.setSource(sval,
					_scriptCompiler._actDefName,
					pnode._xdVersion,
					pnode._nsPrefixes);
				_scriptCompiler.compileExtMethods(
					_scriptCompiler._actDefName, false);
			}
			if ((sval = pnode.getXdefAttr("classes", false, true)) != null
				&& !_codeGenerator._ignoreUnresolvedExternals) {
				if (pnode._xdVersion >= XDConstants.XD31_ID) {
					reportDeprecated(sval,
					"Attribute \"classes\"",
					"<xd:declaration> external method { ... } ...");
				}
				String value = sval.getString();
				Map<String, Class<?>> ht = new TreeMap<String, Class<?>>();
				for (Class<?> clazz : _codeGenerator._extClasses) {
					ht.put(clazz.getName(), clazz);
				}
				StringTokenizer st = new StringTokenizer(value," \t\r\n,;");
				while (st.hasMoreTokens()) {
					String clsname = st.nextToken();
					if (!ht.containsKey(clsname)) {
						Class<?> clazz;
						try {
							clazz = Class.forName(clsname,
								false, _scriptCompiler.getClassLoader());
						} catch (Exception ex) {
							clazz = null;
						}
						if (clazz != null) {
							ht.put(clazz.getName(), clazz);
						} else {
							//Class &{0} is not available
							error(sval, XDEF.XDEF267, clsname);
						}
					}
				}
				if (_codeGenerator._extClasses.length == 0) {
					Class<?>[] exts = new Class<?>[ht.values().size()];
					ht.values().toArray(exts);
					_codeGenerator.setExternals(exts);
				}
			}
			if ((sval = pnode.getXdefAttr("component", false, true)) != null) {
				_scriptCompiler.setSource(sval,
					_xdefNames.get(i),
					pnode._xdVersion,
					pnode._nsPrefixes);
				compileComponentDeclaration();
			}
		}
	}

	/** Check if declaration section has an attribute "scope".
	 * @param nodei node with declaration section.
	 * @return true if the attribute "scope" is "local".
	 */
	private boolean isLocalScope(final PNode nodei, final boolean removeAttr) {
		SBuffer scope = nodei.getXdefAttr("scope", false, removeAttr);
		boolean local = false;
		if (scope != null) {
			String s = scope.getString();
			local = ("local".equals(s));
			if (local && nodei._xdef == null) {
				//Attribute "scope" in selfstanding declaration section
				//can be only "global"
				error(scope, XDEF.XDEF221);
				nodei.getXdefAttr("scope", false, true);
				return false;
			} else if (!local  && !"global".equals(s)) {
				//Incorrect attribute "scope" in declaration section: &{0}
				// (must be "local" or "global")
				error(scope, XDEF.XDEF215);
			}
		}
		return local;
	}

	/** Precompile list of BNF declarations and then the list of variable
	 * declarations. If there is an undefined object in an item of the list
	 * then put this item to the end of list and try to recompile it again.
	 * This nasty trick ensures the declarations on object to preceed object
	 * references. However, it should be resolved with a reference list
	 * connected to the variable declaration.
	 */
	private void preCompileDeclarations() {
		// now not generate code of external methods, make them undefined
		_codeGenerator.setIgnoreExternalMethods(true);
		// first process BNFGrammar declarations, then declaration sections.
		for (ArrayList<PNode> list = _listBNF; list != null;
			list = list == _listBNF ? _listDecl : null) {
			int len;
			if ((len = list.size()) > 0) {
				int size = _codeGenerator._globalVariables.size();
				int lastOffset =_codeGenerator._globalVariables.getLastOffset();
				for (int n = 0; ; n++) {
					int errndx = -1;
					// now we check items from the list. We break the cycle when
					// we find an item with an undefined variable referrence.
					// We put this item to the end of the list and we
					// try it again
					for (int i = 0; i < len; i++) {
						PNode nodei = list.get(i);
						// name of X-definition
						if (list == _listBNF) { //BNFs
							compileBNFGrammar(nodei, false);
						} else { //declarations
							compileDeclaration(nodei, false);
						}
						if (_scriptCompiler.errors()) { //if errors reported
							ArrayReporter reporter = (ArrayReporter)
								_scriptCompiler.getReportWriter();
							Report report;
							//check if there is undefined variable report
							while((report = reporter.getReport()) != null) {
								//XDEF424 ... Undefined variable &{0}
								//XDEF443 ... Unknown method &{0}
								if ("XDEF424".equals(report.getMsgID())
									|| ("XDEF443".equals(report.getMsgID())
									&& list == _listDecl)) {
									errndx = i; //set index of this item
									break;
								}
							}
							reporter.clear();
							if (errndx == i) {
								break;
							}
						}
					}
					if (errndx == -1) {
						break;  // finished, => OK
					}
					if (errndx == len - 1 || n >= len) {
						//we must now compile all items not compiled yet
						for (int i = errndx + 1; i < len; i++) {
							PNode nodei = list.get(i);
							if (list == _listBNF) { //BNFs
								compileBNFGrammar(nodei, false);
							} else { //declarations
								compileDeclaration(nodei, false);
							}
						}
						break;
					}
					//initRecompilation
					_codeGenerator._globalVariables.resetTo(size, lastOffset);
					_codeGenerator.reInit(); //clear the generated code
					_scriptCompiler.initCompilation(
						CompileBase.GLOBAL_MODE, XD_VOID);
					//move the node with error to the end of list and try again
					list.add(list.remove(errndx));
				}
			}
		}
		// set normal generation of code of external methods
		_codeGenerator.setIgnoreExternalMethods(false);
	}

	/** Compile thesaurus.
	 * @param thesaurus list of thesaurus declarations.
	 * @param xp XDPool object.
	 */
	private void compileThesaurus(final ArrayList<PNode> thesaurus,
		final XDPool xp) {
		if (!thesaurus.isEmpty()) { //Compile thesaurus section
			/** Array of properties for thesaurus languages. */
			ArrayList<Map<String,String>> languages =
				new ArrayList<Map<String,String>>();
			for (PNode nodei: thesaurus) {
				_scriptCompiler.compileThesaurus(nodei._value,
					nodei._xdef == null ? null : nodei._xdef.getName(),
					nodei.getXdefAttr("language", true, true),
					nodei.getXdefAttr("default", false, true),
					xp,
					languages);
				reportNotAllowedAttrs(nodei);
			}
			if (!languages .isEmpty()) {
				String[] langs = new String[languages .size()];
				boolean deflt = false;
				for (int i = 0; i < langs.length; i++) {
					Map<String,String> p = languages.get(i);
					if ("!".equals(p.get("%{default}"))) {
						deflt = true;
						p.remove("%{default}");
						if (i != 0) {
							Map<String,String> pp = languages.get(0);
							languages.set(0, p);
							languages.set(i, pp);
							PNode nodei = thesaurus.get(0);
							thesaurus.set(0, thesaurus.get(i));
							thesaurus.set(i, nodei);
						}
						break;
					}
				}
				for (int i = 0; i < langs.length; i++) {
					Map<String,String> p = languages .get(i);
					langs[i] = p.get("%{language}");
					p.remove("%{language}");
				}
				XThesaurusImpl t = new XThesaurusImpl(langs);
				for (int i = deflt ? 1 : 0; i < langs.length; i++) {
					Map<String,String> p = languages .get(i);
					for (Object o: p.keySet()) {
						String s = (String) o;
						t.setItem(s, i, p.get(s));
					}
				}
				boolean[] badIndexes = new boolean[languages.size()];
				for (String key: t.getKeys()) {
					String[] texts = t.findTexts(key);
					for (int i = deflt ? 1 : 0; i < texts.length; i++) {
						if (texts[i] == null) {
							badIndexes[i] = false;
							//Thesaurus item "&{0}" is missing for language &{1}
							error(thesaurus.get(i)._name, XDEF.XDEF149,
								key, t.getLanguages()[i]);
						}
					}
				}
				if (deflt) {
					Map<String,String> pp = languages.get(0);
					int okIndex = 1;
					for (int i = 1; i < badIndexes.length; i++) {
						if (!badIndexes[i]) {
							okIndex = i;
							break;
						}
					}
					Map<String,String> p = languages .get(okIndex);
					for (String s: p.keySet()) {
						String v = s;
						int ndx = v.indexOf('#');
						if (ndx >= 0) {
							v = v.substring(ndx+1);
						}
						ndx = v.lastIndexOf('/');
						if (ndx >= 0) {
							v = v.substring(ndx+1);
						}
						if (v.startsWith("@")) {
							v = v.substring(1);
						}
						t.setItem(s, 0, v);
					}
				}
				_scriptCompiler._g._thesaurus = t;
			}
			thesaurus.clear();
		}
	}

	private void compileDeclaration(final PNode nodei, final boolean remove) {
		boolean local = isLocalScope(nodei, remove);
		String defName = nodei._xdef == null ? null : nodei._xdef.getName();
		_scriptCompiler.setSource(nodei._value,
			defName,
			nodei._xdVersion,
			nodei._nsPrefixes);
		_scriptCompiler.compileDeclaration(local);
	}

	private void compileBNFGrammar(final PNode nodei, final boolean remove) {
		String defName = nodei._xdef == null ? null : nodei._xdef.getName();
		SBuffer sName = nodei.getXdefAttr("name", true, remove);
		if (sName == null) {
			return; //required name is missing.
		}
		_scriptCompiler.compileBNFGrammar(sName,
			nodei.getXdefAttr("extends", false, remove),
			nodei._value,
			defName,
			isLocalScope(nodei, remove),
			nodei._nsPrefixes);
	}

	/** Compile list of BNFs declaration and of variables/methods declaration.
	 * @param listBNF list of BNF declarations.
	 * @param listDecl list of variable declarations.
	 */
	private void compileDeclarations(final ArrayList<PNode> listBNF,
		final ArrayList<PNode> listDecl,
		final ArrayList<PNode> listComponent) {
		for (ArrayList<PNode> list = listBNF; list != null;
			list = list == listBNF ? listDecl : null) {
			if (list.size() > 0) {
				for (PNode nodei: list) {
					if (list == listBNF) { //BNFs
						compileBNFGrammar(nodei, true);
					} else { //declarations
						compileDeclaration(nodei, true);
					}
					reportNotAllowedAttrs(nodei);
				}
			}
		}
		for (PNode nodei: listComponent) {
			String defName = nodei._xdef.getName();
			_scriptCompiler.setSource(nodei._value, defName,
				nodei._xdVersion, nodei._nsPrefixes);
			compileComponentDeclaration();
		}
	}

	/** Prepare list of declared macros and expand macros. */
	private void prepareMacros() {
		// doParse of definitions from include list
		for (int i = 0; i < _includeList.size(); i++) {
			Object o = _includeList.get(i);
			if (o instanceof URL) {
				parseURL((URL) o);
			} else {
				parseFile((File) o);
			}
		}
		for (int i = 0;  i < _xdefNames.size(); i++) {
			String defName = _xdefNames.get(i);
			PNode def = _xdefNodes.get(i);
			ArrayList<PNode> macros = def.getXDefChildNodes("macro");
			for (PNode macro : macros) {
				Map<String, String> params = new TreeMap<String, String>();
				chkNestedElements(macro);
				for (AttrValue val : macro._attrs) {
					params.put(val._name, val._value.getString());
				}
				XScriptMacro m = new XScriptMacro(
					macro.getNameAttr(true, true),
					defName,
					params,
					macro._value,
					_scriptCompiler.getReportWriter());
				if (_macros.containsKey(m.getName())) {
					//Macro '&{0}' redefinition
					_scriptCompiler.error(XDEF.XDEF482, m.getName());
				} else {
					_macros.put(m.getName(), m);
				}
			}
			def.removeChildNodes(macros); // remove all macros
		}
		// expand macros
		ReportWriter reporter = getReportWriter();
		for (int i = 0;  i < _xdefNames.size(); i++) {
			PNode p = _xdefNodes.get(i);
			p.expandMacros(reporter, _xdefNames.get(i));
		}		
	}
	
	/** First step: prepare and expand macros and compile declarations. */
	private void precompile() {
		prepareMacros();
		ReportWriter reporter = getReportWriter();
		// set reporter to the compiler of script
		_scriptCompiler.setReportWriter(reporter);		
		_scriptCompiler.initCompilation(CompileBase.GLOBAL_MODE, XD_VOID);
		compileMehodsAndClassesAttrs();
		// Move all declarations of BNF grammars and variables from the
		// list of X-definitions to the separated lists of variable declarations
		// and of BNF grammar declarations.
		for (int i = 0; i < _xdefNodes.size(); i++) {
			PNode def = _xdefNodes.get(i);
			// since we are removing childnodes from X-definition we must
			// process the childnodes list downwards!
			// However, we insert the item to the first position of the created
			// list to assure the original sequence of items in the X-definition.
			for (int j = def._childNodes.size() - 1;  j >= 0; j--) {
				PNode nodei = def._childNodes.get(j);
				if (nodei._nsindex != NS_XDEF_INDEX) {
					continue;
				}
				String nodeName = nodei._localName;
				if ("declaration".equals(nodeName) ||
					"thesaurus".equals(nodeName) ||
					"component".equals(nodeName) ||
					"BNFGrammar".equals(nodeName)) {
					chkNestedElements(nodei);
					if ("thesaurus".equals(nodeName)) {
						_thesaurus.add(nodei);
					} else if ("BNFGrammar".equals(nodeName)) {
						_listBNF.add(0, nodei);
					} else if ("component".equals(nodeName)) {
						if (nodei._value != null &&
							nodei._value.getString().length() > 0) // not empty
						_listComponent.add(0, nodei);
					} else {
						_listDecl.add(0, nodei);
					}
					// This is the nasty trick! Because there is not yet
					// connected the X-definition to the PNode we create a
					// dumy one just to store a X-definition name (we nead it
					// to be able to compile declaration blocks).
					nodei._xdef =
						new XDefinition(_xdefNames.get(i), null, null, null);
					// remove this node from the X-definition PNode
					def._childNodes.remove(j);
				}
			}
		}
		//we set a temporary reporter which we throw out.
		_scriptCompiler.setReportWriter(new ArrayReporter());
		//Compile declarations and we throw errors out. Due to this trick after
		//this step there will be resolved postdefines and we'll know all
		//types of declared variables and methods.

		// precompile declarations and BNF gramars - just to prepare
		// variable list and to sort item to resolve cross references.
		preCompileDeclarations();

		//Now forget the generated code and compile declatations again with
		//known types of declared objects and with the original error reporter.
		//After compilation the nodes containing declarations are removed
		//from the tree.
		setReportWriter(reporter);//reset original reporter
		_codeGenerator._debugInfo = new XDebugInfo();
		_scriptCompiler.setReportWriter(reporter); //reset original reporter
		_codeGenerator.reInit(); //clear the generated code
		_scriptCompiler.initCompilation(CompileBase.GLOBAL_MODE, XD_VOID);
		// now compile all: BNF gramars, declarations, components, thesaurus
		compileDeclarations(_listBNF, _listDecl, _listComponent);
		// clear all postdefines (should be already dleared, but who knows?)
		_codeGenerator.clearPostdefines();
	}

	/** Check if the name of X-definition is OK.
	 * @param name name of X-definition
	 * @return true if the name of X-definition is OK.
	 */
	private static boolean chkDefName(final String name) {
		if (name.length() == 0) {
			return true; //nameless is also name
		}
		if (StringParser.getXmlCharType(name.charAt(0), false) !=
			StringParser.XML_CHAR_NAME_START) {
			return false;
		}
		char c;
		boolean wasColon = false;
		for (int i = 1; i < name.length(); i++) {
			if (StringParser.getXmlCharType(c = name.charAt(i), false) !=
				StringParser.XML_CHAR_NAME_START && (c  < '0' && c > '9')) {
				if (!wasColon && c == ':') { // we allow one colon inside name
					wasColon = true;
					if (i + 1 < name.length() &&
						(StringParser.getXmlCharType(name.charAt(++i), false)
						!= StringParser.XML_CHAR_NAME_START)){//must follow name
						continue;
					}
				}
				return false;
			}
		}
		return true;
	}

	/** Add the name of X-definition if it is not yet there. */
	private String canonizeReferenceName(final String refName,
		final XDefinition xdef) {
		String name = refName;
		String defName = xdef.getName();
		int i = name.indexOf('#');
		if (i == 0) {
			name = name.substring(1);
		} else if (i > 0) {
			defName = name.substring(0,i);
			name = name.substring(i+1);
		}
		if (!chkDefName(defName)) {
			return null;
		}
		if (!StringParser.chkNCName(name, _xmlVersion1)) {
			return null;
		}
		return defName + '#' + name;
	}

	/** check boolean value. */
	final boolean chkeckTrueFalse(final SBuffer sval, final boolean dflt)  {
		if (sval == null) {
			return dflt;
		}
		String s = sval.getString();
		if ("no".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
			if ("no".equalsIgnoreCase(s)) {
				reportDeprecated(sval, "no", "false");
			}
			return false;
		} else if (!"yes".equalsIgnoreCase(s) && !"true".equalsIgnoreCase(s)) {
			error(sval, XDEF.XDEF410, "false"); //'&{0}' expected
			return dflt;
		} else {
			if ("yes".equalsIgnoreCase(s)) {
				reportDeprecated(sval, "yes", "true");
			}
			return true;
		}
	}

	/** Create copy of SPosition (without modifications). */
	private SPosition copySPosition(final SPosition sval) {
		return new SPosition(sval.getIndex(),
			sval.getLineNumber(),
			sval.getStartLine(),
			sval.getFilePos(),
			sval.getSystemId());
	}

	private void compileAttrs(final PNode pnode,
		final String defName,
		final XNode xNode,
		final boolean isAttlist) {
		XElement xel;
		XData xtxt;
		short newKind;
		if ((newKind = xNode.getKind()) == XNode.XMELEMENT) {
			xel = (XElement) xNode;
			xtxt = null;
			//compile first script - we must recognize template!
			AttrValue pattr = pnode.getAttrNS("script", NS_XDEF_INDEX);
			if (pattr != null) {
				_scriptCompiler.setSource(pattr._value,
					defName, pnode._xdVersion, pnode._nsPrefixes);
				if (xel._template) {
					_scriptCompiler.skipSpaces();
					if (_scriptCompiler.isToken("$$$script:")) {
						_scriptCompiler.skipSpaces();
						_scriptCompiler.isChar(';');
						xel._template = false;
					}
				}
				_scriptCompiler.compileElementScript(xel);
			}
		} else if (newKind == XNode.XMTEXT) {
			xel = null;
			xtxt = (XData) xNode;
		} else {
			return;
		}
		for (AttrValue pattr: pnode._attrs) {
			String key = pattr._name;
			SBuffer sval = pattr._value;
			_scriptCompiler.setSource(sval,
				_scriptCompiler._actDefName,
				pnode._xdVersion,
				pnode._nsPrefixes);
			if (pattr._nsindex == NS_XDEF_INDEX) {
				String localName = pattr._localName;
				if ("script".equals(localName)) {
					if (isAttlist) {
						//Attribute '&{0}' not allowed here
						error(sval, XDEF.XDEF254, key);
					} else if (newKind == XNode.XMTEXT) {
						_scriptCompiler.compileDataScript(xtxt);
					}
				} else if ("attr".equals(localName)) {
					//any Attribute - script for "moreAtttributes"
					if (newKind == XNode.XMELEMENT && xel != null/*must be!*/) {
						//any attributes in Element
						XData xattr = new XData("$attr",
							null, xel.getXDPool(), XNode.XMATTRIBUTE);
						xattr.setSPosition(copySPosition(sval));
						xattr.setXDPosition(xel.getXDPosition()+"/$attr");
						_scriptCompiler.compileDataScript(xattr);
						xel.setDefAttr(xattr);
					} else {
						//Attribute '&{0}' not allowed here
						error(sval, XDEF.XDEF254, key);
					}
				} else if ("text".equals(localName) ||
					"textcontent".equals(localName)) {
					if (newKind == XNode.XMELEMENT && xel != null/*must be!*/
						&& !isAttlist) {
						//here is "textcontent"
						XData xdata = new XData('$' + localName,
							null, xel.getXDPool(), XNode.XMTEXT);
						xdata.setSPosition(copySPosition(sval));
						xdata.setXDPosition(
							xel.getXDPosition() + "/$" + localName);
						_scriptCompiler.compileDataScript(xdata);
						xel.setDefAttr(xdata);
						xel._moreText = 'T';
					} else {
						//Attribute '&{0}' not allowed here
						error(sval, XDEF.XDEF254, key);
					}
//				} else if ("PI".equals(localName)) {//TODO
//				} else if ("comment".equals(localName)) {//TODO
//				} else if ("document".equals(localName)) {//TODO
//				} else if ("value".equals(localName)) {//TODO
//				} else if ("attlist".equals(localName)) {//TODO
				} else if (newKind == XNode.XMELEMENT) {
					//Attribute '&{0}' not allowed here
					error(sval, XDEF.XDEF254, key);
				}
			} else {
				// attributes which are not from our namespace
				if (newKind == XNode.XMELEMENT && xel != null /*must be!*/) {
					XData xattr = new XData(key,
						pattr._nsURI, xel.getXDPool(), XNode.XMATTRIBUTE);
					xattr.setSPosition(copySPosition(sval));
					xattr.setXDPosition(xel.getXDPosition()+ "/@" + key);
					boolean template;
					_scriptCompiler.skipSpaces();
					if (template = xel._template) {
						_scriptCompiler.skipSpaces();
						if (_scriptCompiler.isToken("$$$script:")) {
							_scriptCompiler.skipSpaces();
							_scriptCompiler.isChar(';');
							template = false;
						}
					}
					if (template) {
						_scriptCompiler.genTemplateData(xattr, xNode);
					} else {//"normal" attributes
						_scriptCompiler.compileDataScript(xattr);
					}
					xel.setDefAttr(xattr);
				} else {
					//Attribute '&{0}' not allowed here
					error(sval, XDEF.XDEF254,key);
				}
			}
		}
	}

	/** Create reference node.
	 * @param pnode source node.
	 * @param refName Local name of p-node.
	 * @param xdef X-definition.
	 * @return generated ParsedReference object.
	 */
	private XNode createReference(final PNode pnode,
		final String refName,
		final XDefinition xdef) {
		XSelector newNode;
		XOccurrence defaultOcc = new XOccurrence(1,1); //required as default
		if ("mixed".equals(refName)) {
			newNode = new XMixed();
			defaultOcc.setUnspecified();
		} else if ("choice".equals(refName)) {
			newNode = new XChoice(); //min=1; max=1
		} else if ("sequence".equals(refName)) {
			newNode = new XSequence(); //min=1;max=1
		} else {//include
			newNode = new XSequence();
			defaultOcc.setUnspecified();
		}
		newNode.setUnspecified();
		newNode.setSPosition(copySPosition(pnode._name));
		SBuffer ref = pnode.getXdefAttr("ref", false, true);
		newNode.setXDPosition(xdef.getXDPosition()+'$'+pnode._name.getString()+
			(ref != null ? "("+ref.getString()+")" : ""));
		short kind = newNode.getKind();
		if (kind==XNode.XMCHOICE||kind==XNode.XMMIXED||kind==XNode.XMSEQUENCE) {
			SBuffer sval;
			if ((sval = pnode.getXdefAttr("script", false, true)) != null) {
				_scriptCompiler.setSource(sval,
					_scriptCompiler._actDefName,
					pnode._xdVersion,
					pnode._nsPrefixes);
				SBuffer s = _scriptCompiler.compileGroupScript(newNode);
				if (s != null) {
					if (ref != null) {
						//Reference ca'nt be specified both in attributes
						//'ref' and 'script'
						error(ref, XDEF.XDEF117);
					}
					ref = s;
				}
			} else {
				if ((sval = pnode.getXdefAttr("init", false, true)) != null) {
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					_scriptCompiler.nextSymbol();
					newNode.setInitCode(_scriptCompiler.compileSection(CompileBase.ELEMENT_MODE,
						XD_VOID,
						XScriptParser.INIT_SYM));
				}
				if ((sval = pnode.getXdefAttr("occurs", false, true)) != null) {
					if (kind == XNode.XMMIXED) {
						reportDeprecated(pnode._name, "occurs","script");
					}
					XOccurrence occ = new XOccurrence();
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					_scriptCompiler.nextSymbol();
					if (!_scriptCompiler.isOccurrenceInterval(occ)) {
						//After 'occurs' is expected the interval
						error(sval, XDEF.XDEF429);
					}
					newNode.setOccurrence(occ);
					if (!_scriptCompiler.eos()) {
						 error(sval, XDEF.XDEF425); //Script error
					}
				}
				if ((sval = pnode.getXdefAttr("finally", false, true)) != null){
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					_scriptCompiler.nextSymbol();
					newNode.setFinallyCode(_scriptCompiler.compileSection(CompileBase.ELEMENT_MODE,
						XD_VOID,
						XScriptParser.FINALLY_SYM));
				}
				if ((sval = pnode.getXdefAttr("create", false, true)) != null) {
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					_scriptCompiler.nextSymbol();
					newNode.setComposeCode(_scriptCompiler.compileSection(CompileBase.ELEMENT_MODE,
						XD_ANY,
						XScriptParser.CREATE_SYM));
				}
				if ((sval = pnode.getXdefAttr("match", false, true)) != null) {
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					_scriptCompiler.nextSymbol();
					newNode.setMatchCode(_scriptCompiler.compileSection(CompileBase.ELEMENT_MODE,
						XD_BOOLEAN,
						XScriptParser.MATCH_SYM));
				}
			}
			if ((sval = pnode.getXdefAttr("empty", false, true)) != null) {
				String s = sval.getString().trim();
				if (s.length() > 0) {
					if (newNode.isSpecified()) {//specified
						//If occurrence is specified it can't be changed
						//by specification of 'empty'"
						error(sval, XDEF.XDEF264);
					}
				}
				if ("true".equals(s)) {
					if (kind != XNode.XMMIXED) {
						//Attribute 'empty' is allowed only in the group
						//'xd:mixed'
						lightError(pnode._name, XDEF.XDEF263);
						newNode.setOccurrence(0, 1);
					} else {
						newNode.setEmptyDeclared(true);
						newNode.setMinOccur(0);
					}
				} else if ("false".equals(s)) {
					if (kind != XNode.XMMIXED) {
						//Attribute 'empty' is allowed only in the group
						//'xd:mixed'
						lightError(pnode._name, XDEF.XDEF263);
						newNode.setOccurrence(1, 1);
					} else {
						newNode.setEmptyDeclared(false);
						newNode.setMinOccur(1);
					}
				} else {
					//Value of type '&{0}' expected
					error(sval, XDEF.XDEF423, "boolean");
				}
			}
		}
		reportNotAllowedAttrs(pnode);
		if (!newNode.isSpecified()) {
			if (ref != null) {
				newNode.setUnspecified();
			} else {
				newNode.setOccurrence(defaultOcc);
			}
		}
		String name;
		if ("includeChildNodes".equals(refName)) {
			if (ref == null) {
				//Incorrect or missing attribute 'ref'
				error(pnode._name, XDEF.XDEF218);
				return null;
			}
			if (pnode._childNodes.size() > 0) {
				//Child nodes of the element 'xd:includeChildNodes'
				//are not allowed
				error(pnode._name, XDEF.XDEF232);
			}
			name = canonizeReferenceName(ref.getString(), xdef);
			if (name == null) {
				error(ref, XDEF.XDEF258); //Incorrect name
				name = ref.getString();
			}
		} else {
			if (ref == null) {
				return "list".equals(refName) ? null : newNode;
			}
			name = ref.getString() + '$' + refName;
		}
		return new CompileReference(
			CompileReference.XMINCLUDE, xdef, null, name, ref, newNode);
	}

	private static void setXDPosition(
		final XNode parent,
		final XElement parentElement,
		final XNode xn) {
		String name = xn.getName();
		if (parent.getKind() == XNode.XMDEFINITION) {
			String nsUri = xn.getNSUri();
			if (nsUri != null) {
				XDefinition xd = (XDefinition) parent;
				for (Entry<String, String> e: xd._namespaces.entrySet()) {
					if (nsUri.equals(e.getValue())) {
						String pfx = e.getKey();
						int ndx = name.indexOf(':');
						if (ndx > 0) {
							if (pfx.isEmpty()) {
								name = name.substring(ndx + 1);
							} else {
								name = pfx + name.substring(ndx);
							}
						} else {
							if (!pfx.isEmpty()) {
								name = pfx + ':' + name;
							}
						}
						break;
					}
				}
			}
			xn.setXDPosition(parent.getXDPosition() + name);
			return;
		}
		String xdPos = parent.getXDPosition();
		if (!xdPos.endsWith("#")) {
			xdPos += "/";
		}
		xdPos += (xn.getKind() == XNode.XMTEXT) ? "$text" : name;
		int n = 1;
		for (int i = 0; i < parentElement._childNodes.length; i++) {
			XNode x = parentElement._childNodes[i];
			String xpos = x.getXDPosition();
			if (xpos != null) { // not #selector_end
				if (xpos.endsWith("]")) {
					xpos = xpos.substring(0, xpos.lastIndexOf('['));
				}
				if (xdPos.equals(xpos)) {
					n++;
				}
			}
		}
		if (n > 1) {
			xdPos += "[" + n + "]";
		}
		xn.setXDPosition(xdPos);
	}

	private void compileXChild(final XNode parentNode,
		final XElement lastElement,
		final PNode pnode,
		final XDefinition xdef,
		final int level) {
		String xchildName = pnode._name.getString();
		XNode newNode;
		SBuffer sval;
		short parentKind = parentNode.getKind();
		XElement xel;
		if (parentKind == XNode.XMELEMENT &&
			(xel = (XElement) parentNode)._template) {
			if (pnode._nsindex == NS_XDEF_INDEX) {
				if ("text".equals(pnode._localName)) {
					chkNestedElements(pnode);
					sval = pnode._value;
					if (xel._trimText == 'T' || xel._textWhiteSpaces == 'T') {
						if (sval.getString().trim().length() == 0) {
							return;
						}
					}
					XData xtxt = new XData("$text",
						null, xdef.getXDPool(), XNode.XMTEXT);
					xtxt.setXDPosition(parentNode.getXDPosition()+"/$text");
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes); //???
					_scriptCompiler.skipSpaces();
					if (_scriptCompiler.isToken("$$$script:")) {
						_scriptCompiler.skipSpaces();
						_scriptCompiler.isChar(';');
						_scriptCompiler.compileDataScript(xtxt);
					} else {
						_scriptCompiler.genTemplateData(xtxt, xel);
					}
					pnode._value = null;
					newNode = xtxt;
//				} else if ("PI".equals(_actPNode._localName)) { //TODO
//					sval = pnode._value;
//					XPI newPI = new XPI(xchildName, xdef.getDefPool());
//					newPI.setSPosition(new SPosition(sval, false));
//					_scriptCompiler.setSource(sval,
//						_scriptCompiler._actDefName, pnode._nsPrefixes); //???=
//					_scriptCompiler.skipSpaces();
//					if (_scriptCompiler.isToken("$$$script:")) {
//						_scriptCompiler.skipSpaces();
//						_scriptCompiler.isChar(';');
//						_scriptCompiler.compileDataScript(newPI);
//					} else {
//						_scriptCompiler.genTemplateData(newPI, xel);
//					}
//					newNode = newPI;
				} else if ("comment".equals(_actPNode._localName)) { //TODO
					sval = pnode._value;
					XComment xcomment = new XComment(xdef.getXDPool());
					xcomment.setSPosition(copySPosition(sval));
//					xcomment.setXDPosition(parentNode.getXDPosition() +
//						"/#comment");
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes); //???=
					_scriptCompiler.skipSpaces();
					if (_scriptCompiler.isToken("$$$script:")) {
						_scriptCompiler.skipSpaces();
						_scriptCompiler.isChar(';');
						_scriptCompiler.compileDataScript(xcomment);
					} else {
						_scriptCompiler.genTemplateData(xcomment, xel);
					}
					newNode = xcomment;
//				} else if ("document".equals(_actPNode._localName)) { //TODO
//					newNode = new XDocument("$document", null, xdef);
//					newNode = createReference(pnode, pnode._localName, xdef);
//				} else if ("value".equals(_actPNode._localName)) { //TODO
//					newNode = new XData("$text",
//						null, xdef.getDefPool(), XNode.XMTEXT);
//				} else if ("attlist".equals(_actPNode._localName)) { //TODO
//					newNode = createReference(pnode, pnode._localName, xdef);
				} else {
					//Element from namespace of XDefinitions is not allowed here
					error(pnode._name, XDEF.XDEF322);
					return;
				}
			} else {//XElement
				xel = new XElement(xchildName, pnode._nsURI, xdef);
				_scriptCompiler._g._varBlock =
					new XVariableTable(_scriptCompiler._g._varBlock,
						xel.getSqId());
				xel.setSPosition(copySPosition(pnode._name));
				_scriptCompiler.genTemplateElement(xel, parentNode);
				newNode = xel;
			}
		} else if (pnode._nsindex == NS_XDEF_INDEX) {
			String name = pnode._localName;
			if ("data".equals(name) || "text".equals(name)) {
				chkNestedElements(pnode);
				if ("data".equals(name)) {
					reportDeprecated(pnode._name, "data", "text");
				}
				XData xtext =
					new XData("$text", null, xdef.getXDPool(), XNode.XMTEXT);
				xtext.setSPosition(copySPosition(pnode._name));
				newNode = xtext;
				sval = pnode.getXdefAttr("script", false, true);
				if (sval != null) {
					reportDeprecated(sval, "<xd:text xd:script=...",
						"script declared as a text value of model");
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					_scriptCompiler.compileDataScript(xtext);
				} else if (pnode._value != null) {
					_scriptCompiler.setSource(pnode._value,
						_scriptCompiler._actDefName,
						pnode._xdVersion,
						pnode._nsPrefixes);
					pnode._value = null;
					_scriptCompiler.compileDataScript(xtext);
				} else { //default text script
					_scriptCompiler.setSourceBuffer("optional string()");
					_scriptCompiler.compileDataScript(xtext);
				}
				reportNotAllowedAttrs(pnode);
			} else if ("list".equals(name) || "includeChildNodes".equals(name)){
				if ("includeChildNodes".equals(name)) {
					reportDeprecated(_actPNode._name,
						"includeChildNodes", "list");
				}
				chkNestedElements(pnode);
				if (level == 1) {
					//Node '&{0}' from '&{1}' namespace is not allowed here
					error(pnode._name, XDEF.XDEF265, name, "xdefinitons");
					return;
				}
				newNode = createReference(pnode, name, xdef);
			} else if ("mixed".equals(name) ||
				"choice".equals(name) || "sequence".equals(name)) {
				newNode = createReference(pnode, name, xdef);
				newNode.setSPosition(copySPosition(pnode._name));
			} else if ("any".equals(name)) {
				newNode = new XElement("$any", null, xdef);
				_scriptCompiler._g._varBlock =
					new XVariableTable(_scriptCompiler._g._varBlock,
					((XElement)newNode).getSqId());
				((XElement) newNode).setSPosition(copySPosition(pnode._name));
				if (level == 1) {
					//Node '&{0}' from '&{1}' namespace is not allowed here
					error(pnode._name, XDEF.XDEF265, name, "xdefinitons");
					return;
				}
//			} else if ("PI".equals(_actPNode._localName)) { //TODO
//				newNode = new XPI(pnode._name._source, xdef.getDefPool());
//			} else if ("comment".equals(name)) { //TODO
//				newNode = new XComment(xdef.getXDPool());
//			} else if ("document".equals(_actPNode._localName)) { //TODO
//				newNode = new XDocument("$document", null, xdef);
//				newNode = createReference(pnode, pnode._localName, xdef);
//			} else if ("value".equals(_actPNode._localName)) { //TODO
//				newNode = new XData("$text",
//					null, xdef.getDefPool(), XNode.XMTEXT);
//			} else if ("attlist".equals(_actPNode._localName)) { //TODO
//				newNode = createReference(pnode, pnode._localName, xdef);
			} else {
				//Node '&{0}' from '&{1}' namespace is not allowed here
				error(pnode._name, XDEF.XDEF265, xchildName, "X-definitions");
				return;
			}
		} else {
			XElement x = new XElement(xchildName, pnode._nsURI, xdef);
			newNode = x;
			_scriptCompiler._g._varBlock =
				new XVariableTable(_scriptCompiler._g._varBlock,
					((XElement)newNode).getSqId());
			x.setSPosition(copySPosition(pnode._name));
			if (parentKind != XNode.XMDEFINITION) {
				x.setRequired();
			}
		}
		if (newNode == null) {
			//Unknown node '&{0}'
			error(pnode._name, XDEF.XDEF217, pnode._name.getString());
			return;
		}
		setXDPosition(parentNode, lastElement, newNode);
		//process attributes
		compileAttrs(pnode, _scriptCompiler._actDefName, newNode, false);
		addNode(parentNode, newNode, level, pnode._name);
		//compile child nodes
		for (PNode nodei: pnode._childNodes) {
			XElement x = newNode.getKind() == XMNode.XMELEMENT ?
				(XElement) newNode : lastElement;
			compileXChild(newNode, x, nodei, xdef, level + 1);
		}
		short newKind = newNode.getKind();
		if (level == 1) {
			if (newKind == XNode.XMELEMENT) {
				if (!xdef.addModel((XElement) newNode)) {
					//Repeated specification of element '&{0}'
					error(pnode._name,XDEF.XDEF236, newNode.getName());
				}
			}
		}
		if (newKind == XNode.XMCHOICE || newKind == XNode.XMSEQUENCE ||
			newKind == XNode.XMMIXED) {
			addNode(parentNode, new XSelectorEnd(), level, pnode._name);
		}
		if (pnode._value != null && pnode._value.getString() != null) {
			_scriptCompiler.setSourceBuffer(pnode._value);
			_scriptCompiler.skipSpaces();
			if (!_scriptCompiler.eos()) {
				//Text value not allowed here
				_scriptCompiler.lightError(XDEF.XDEF260);
			}
			pnode._value = null; //prevent repeated message
		}
		if (newKind == XNode.XMELEMENT) {
			if (_scriptCompiler._g._varBlock != null) {
				_scriptCompiler._g._varBlock =
					_scriptCompiler._g._varBlock.getParent();
			}
		}
	}

	/** Compile header attributes of xd:def and xd:collection.
	 * @param ii index of item.
	 * @param xdp defPool.
	 */
	private void compileXdefHeader(final int ii, final XDPool xdp) {
		String defName = _xdefNames.get(ii);
		PNode pnode = _xdefNodes.get(ii);
		_scriptCompiler._actDefName = defName;
		XDefinition def =
			new XDefinition(defName, xdp, pnode._nsURI, pnode._name);
		pnode._xdef = def;
		for (Entry<String, Integer> e: pnode._nsPrefixes.entrySet()) {
			def._namespaces.put(e.getKey(),
				_codeGenerator._namespaceURIs.get(e.getValue()));
		}
		SBuffer sval = pnode.getXdefAttr("script", false, true);
		if (sval != null) {
			_scriptCompiler.setSource(sval, defName, def.getXDVersion());
			_scriptCompiler.compileXDHeader(def);
		}
		sval = pnode.getXdefAttr("root", false, true);
		if (sval != null) {
			_scriptCompiler.setSource(sval,
				defName, pnode._xdVersion, pnode._nsPrefixes);
			while (true) {
				_scriptCompiler.skipSpaces();
				SPosition pos = new SPosition(_scriptCompiler);
				String refName;
				String nsURI = null;
				if (_scriptCompiler.isChar('*')) {
					refName = "*"; //any
				} else if (_scriptCompiler.isXModelPosition()) { /*xx*/
					refName = _scriptCompiler.getParsedString();
					//get NSUri of the reference identifier.
					int ndx = refName.indexOf('#') + 1;
					int ndx1 = refName.indexOf(':', ndx);
					Object obj;
					if (ndx1 > 0) {// get nsURI assigned to the prefix
						String prefix = refName.substring(ndx, ndx1);
						if ((obj = pnode._nsPrefixes.get(prefix)) == null) {
							//Namespace for prefix '&{0}' is undefined
							sval.putReport(Report.error(XDEF.XDEF257, prefix),
								_scriptCompiler.getReportWriter());
						}
					} else {
						obj = pnode._nsPrefixes.get("");
					}
					if (obj != null) {
						nsURI = _scriptCompiler._g._namespaceURIs.get(
							((Integer) obj));
					}
				} else {
					//Reference to element model expected
					_scriptCompiler.error(pos, XDEF.XDEF213);
					break;
				}
				XNode xn = new CompileReference(
					CompileReference.XMREFERENCE, def, nsURI, refName, pos);
				if (def._rootSelection.containsKey(xn.getName())) {
					//Repeated root selection &{0}
					_scriptCompiler.error(pos, XDEF.XDEF231, refName);
				} else {
					def._rootSelection.put(xn.getName(), xn);
				}
				_scriptCompiler.skipBlanksAndComments();
				if (!_scriptCompiler.isChar('|')) {
					break;
				}
			}
			_scriptCompiler.skipBlanksAndComments();
			if (!_scriptCompiler.eos()) {
				_scriptCompiler.error(sval,XDEF.XDEF216); //Unexpected character
			}
		}
		//process attributes of XDefinition
		for (AttrValue pattr:  pnode._attrs) {
			if (pattr._name.startsWith("impl-") && pattr._localName.length()>5){
				def._properties.put(pattr._name.substring(5),
					pattr._value.getString());
			} else {// unknown name
				//Attribute '&{0}' not allowed here
				error(pattr._value, XDEF.XDEF254, pattr._name);
			}
		}
		if (_xdefs.containsKey(def.getName())) {
			//XDefinition '&{0}' already exists
			error(XDEF.XDEF303, def.getName());
		}
		_xdefs.put(def.getName(), def);
	}

	private void compileXDefinition(final int ii) {
		PNode pnode = _xdefNodes.get(ii);
		String defName = _xdefNames.get(ii);
		String actDefName = _scriptCompiler._actDefName;
		_scriptCompiler._actDefName = defName;
		XDefinition def = pnode._xdef;
		_nodeList.add(0,def);
		//compile xmodels
		for (PNode nodei: pnode._childNodes) {
			String name = nodei._localName;
			if (nodei._nsindex == NS_XDEF_INDEX && ("choice".equals(name) ||
				"mixed".equals(name) || "sequence".equals(name) ||
//				"text".equals(name) ||
//				"PI".equals(name) || //TODO
//				"comment".equals(name) || //TODO
//				"value".equals(name) || //TODO
//				"document".equals(name) || //TODO
//				"attlist".equals(name) || //TODO
				"list".equals(name) || "any".equals(name))) {
//				if ("text".equals() &&
//					nodei.getXdefAttr("name", true, true) == null) {
//					// Text not allowed here
//					error(nodei._value, XDEF.XDEF260);
//					continue;
//				}
				SBuffer gname;
				if ("any".equals(name)) {//any MUST use prefixed name attribute!
					AttrValue v = nodei.getAttrNS("name", NS_XDEF_INDEX);
					if (v == null) {
						gname = null;
						//Required attribute '&{0}' is missing
						error(nodei._name, XDEF.XDEF323, "xd:name");
					} else {
						gname = v._value;
						nodei._attrs.remove(v);
					}
				} else {
					gname = nodei.getXdefAttr("name", true, true);
				}
				if (gname != null) { //we create dummy element
					String dname = gname.getString() + '$' + name;
					XElement dummy = new XElement(dname, null, def);
					dummy.setSPosition(copySPosition(pnode._name));
					dummy.setXDPosition(def.getXDPosition() + dname);
					addNode(def, dummy, 1, nodei._name);
					if (!def.addModel(dummy)) {
						//Repeated specification of element '&{0}'
						error(gname, XDEF.XDEF236, gname.getString());
					} else {
						if ("list".equals(name)) {
							for (PNode pn: nodei._childNodes){
								compileXChild(dummy, dummy, pn, def, 2);
							}
						} else if (name.startsWith("att")) {
							compileAttrs(nodei, defName, dummy, true);
						} else if (name.startsWith("any")) {
							compileXChild(dummy, dummy, nodei, def, 2);
						} else {
							compileXChild(dummy, dummy, nodei, def, 2);
						}
					}
				}
				continue;
			}
			compileXChild(def, null, nodei, def, 1);
		}
		_nodeList.clear();
		_scriptCompiler._actDefName = actDefName;
	}

	private static short getTypeId(XMNode xn) {
		if (xn.getKind() == XMNode.XMELEMENT) {
			// all elements have same type (i.e XComponent or List<XComponent>)
			return (short) (250 + (((XMElement) xn).maxOccurs() > 1 ? 0 : 1));
		} else {
			XDValue p = ((XData)xn).getParseMethod();
			return p.getItemId() == XDValueID.XD_PARSER ?
				((XDParser) p).parsedType() : ((XData)xn).getBaseType();
		}
	}

	/** Compile parsed definitions to the XPool.
	 * @param xdp the XPool.
	 */
	public final void compileXPool(final XDPool xdp) {
		if (_scriptCompiler == null) {
			//Attempt to recompile compiled pool
			throw new SRuntimeException(XDEF.XDEF203);
		}
		precompile(); //compile definitions and groups.
		for (int ii = 0;  ii < _xdefNames.size(); ii++) {
			compileXdefHeader(ii, xdp);
		}
		for (int ii = 0;  ii < _xdefNames.size(); ii++) {
			compileXDefinition(ii);
		}
		//just let GC do the job;
		_xdefNames = null;
		_xdefNodes = null;
		_sourceFiles = null;
		_includeList = null;
		_nodeList = null;
		_macros = null;
		boolean result = true;
		//check integrity of all XDefinitions
		HashSet<XNode> hs = new HashSet<XNode>();
		for (XDefinition x : _xdefs.values()) {
			for (XElement xel: x.getXElements()) {
				result &= checkIntegrity(xel, 0, hs);
			}
		}
		//process clearing of adopted forgets
		hs.clear();
		for (XDefinition x: _xdefs.values()) {
			for (XElement xel: x.getXElements()) {
				clearAdoptedForgets(xel, false, hs);
			}
		}
		hs.clear();
		//update selectors
		for (XDefinition x : _xdefs.values()) {
			XElement[] elems = x.getXElements();
			for (XElement xe: elems) {
				updateSelectors(xe, 0, null, false, false, hs);
			}
		}
		hs.clear(); //let's gc do the job
		//resolve root references for all XDefinitions
		for (XDefinition d : _xdefs.values()) {
			Map<String, XNode> rootSelection = new TreeMap<String, XNode>();
			for (Map.Entry<String,XNode> entry: d._rootSelection.entrySet()) {
				try {
					XNode xnode = entry.getValue();
					if (xnode.getKind() == CompileReference.XMREFERENCE) {
						CompileReference xref = (CompileReference) xnode;
						XElement xel = xref.getTarget();
						if (xel == null) { //Unresolved reference
							xref.putTargetError(getReportWriter());
						} else {
							if (rootSelection.put(xref.getName(), xel) != null){
								//Internal error: &{0} XXXX
								error(XDEF.XDEF315,
									"reference to element model expected");
							}
						}
					} else {
						//Internal error: &{0}
						error(XDEF.XDEF315,
							"reference to element model expected");
						result = false;
					}
				} catch(SRuntimeException ex) {
					putReport(ex.getReport());
					result = false;
					break;
				}
				d._rootSelection = rootSelection;
			}
		}
		compileThesaurus(_thesaurus, xdp); // compile thesaurus
		if (!result) {
			error(XDEF.XDEF201); //Error of XDefinitions integrity
		} else {
			try {
				// set code to xdp
				((XPool) xdp).setCode(_codeGenerator._code,
					_codeGenerator._globalVariables.getLastOffset() + 1,
					_codeGenerator._localVariablesMaxIndex + 1,
					_codeGenerator._spMax + 1,
					_codeGenerator._init,
					_codeGenerator._parser._xdVersion,
					_codeGenerator._thesaurus);
				XVariableTable variables = new XVariableTable(0);

				// set variables to xdp
				for (XMVariable xv: _codeGenerator._globalVariables.toArray()) {
					CompileVariable v = (CompileVariable) xv;
					v.setValue(null); //Clear assigned value
					v.clearPostdefs();//Clear postdefs (should be already clear)
					variables.addVariable(v);
				}
				((XPool) xdp).setVariables(variables);

				// set debug info to xdp
				if (_codeGenerator._debugInfo != null) {
					((XPool) xdp).setDebugInfo(_codeGenerator._debugInfo);
				}

				// set X-components to xdp
				HashSet<String> classNames = new HashSet<String>();
				// create map of components
				Map<String, String> x = new TreeMap<String, String>();
				for (Map.Entry<String, SBuffer> e:
					_codeGenerator._components.entrySet()) {
					XMNode xn = (XMElement) xdp.findModel(e.getKey());
					if (xn == null || xn.getKind() != XMNode.XMELEMENT) {
						SBuffer sbf = e.getValue();
						//Unresolved reference &{0}
						error(sbf, XDEF.XDEF353, e.getKey());
					} else {
						String s = e.getValue().getString();
						x.put(e.getKey(), s);
						if (!s.startsWith("%ref ")) {
							// Extract qualified class name to be generated
							if (s.startsWith("interface ")) {
								s = s.substring(10); //remove inteface
							}
							int ndx = s.indexOf(' ');
							if (ndx >= 0) { //remove rest of command
								s = s.substring(0, ndx);
							}
							if (!classNames.add(s)) {
								//Class name &{0} is used in other command
								error(e.getValue(), XDEF.XDEF383, s);
							}
						}
					}
				}
				((XPool) xdp).setXComponents(x);

				x = new TreeMap<String, String>();
				for (Map.Entry<String, SBuffer> e:
					_codeGenerator._binds.entrySet()) {
					XMNode xn = xdp.findModel(e.getKey());
					if (xn == null || xn.getKind() != XMNode.XMELEMENT
						&& xn.getKind() != XMNode.XMATTRIBUTE
						&& xn.getKind() != XMNode.XMTEXT) {
						SBuffer sbf = e.getValue();
						//Unresolved reference &{0}
						error(sbf, XDEF.XDEF353, e.getKey());
						continue;
					}
					// if this bind item is connected to a class (and extends
					// a component)
					String s = e.getValue().getString();
					int ndx = s.indexOf(" %with ");
					if (ndx > 0) {
						short typ = getTypeId(xn);
						// Check if all binds conneted to the same class
						// have the same type.
						for (Map.Entry<String, SBuffer> f:
							_codeGenerator._binds.entrySet()) {
							if (!e.getKey().equals(f.getKey()) &&
								e.getValue().getString().equals(
									f.getValue().getString())) {
								XMNode xm = xdp.findModel(f.getKey());
								if (xm == null) {
									//Unresolved reference &{0}
									error(f.getValue(),
										XDEF.XDEF353, e.getKey());
								} else if (typ != getTypeId(xdp.findModel(
									f.getKey()))) {
									// same name in same class must have
									// same typ
									s = s.substring(ndx + 7, s.indexOf(' '));
									//Types of items &{0},&{1} bound
									//to class &{2} differs
									error(f.getValue(),XDEF.XDEF358,
										e.getKey(), f.getKey(), s);
								}
							}
						}
					}
					x.put(e.getKey(), e.getValue().getString());
				}
				((XPool) xdp).setXComponentBinds(x);

				x = new TreeMap<String, String>();
				for (Map.Entry<String, SBuffer> e:
					_codeGenerator._enums.entrySet()) {
					String name = e.getKey();
					int ndx;
					if ((ndx = name.indexOf(' ')) >= 0) {
						name.substring(0, ndx);
					}
					CompileVariable var = _codeGenerator.getVariable(name);
					SBuffer cls = e.getValue(); // name of class
					if (var == null) {
						//Enumeration &{0} is not declared as typ&
						error(cls, XDEF.XDEF380, name);
					} else {
						XDValue xv =
							_codeGenerator._code.get(var.getParseMethodAddr());
						if (xv.getItemId() == XDValueID.XD_PARSER) {
							XDParser p = (XDParser) xv;
							if (!name.equals(p.getDeclaredName())) {
								//Enumeration &{0} is not declared as a type
								error(cls,XDEF.XDEF380, e.getKey());
							} else {
								XDContainer xc = p.getNamedParams();
								if (xc != null && (p instanceof XDParseEnum
									&& (xv = xc.getXDNamedItemValue("argument"))
									!= null)) {
									xc = (XDContainer) xv;
								} else {
									//Type &{0} can't be converted to enum
									error(cls, XDEF.XDEF381, name);
									continue;
								}
								XDValue[] names = xc.getXDItems();
								boolean wasError =
									names == null || names.length == 0;
								if (!wasError) {
									for (XDValue item: names) {
										String s = item == null
											? null : item.stringValue();
										if (!StringParser.isJavaName(s)) {
											wasError = true;
											//Type &{0} can't be converted to
											//enumeration &{1} because value
											//"&{2}" is not Java identifier
											error(cls, XDEF.XDEF382,
												name, cls.getString(), s);
										}
									}
								}
								String s = cls.getString(); // get as string
								if (!classNames.add(s)) {
									//Class name &{0} is used in other command
									error(cls, XDEF.XDEF383, s);
								}
								if (!wasError) {
									for (XDValue item: names) {
										s += " " + item.stringValue();
									}
									x.put(e.getKey(), s);
								}
							}
						} else {
							//Enumeration &{0} is not declared as a type
							error(cls, XDEF.XDEF380, name);
						}
					}
				}
				((XPool) xdp).setXComponentEnums(x);
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				//Internal error: &{0}
				throw new SRuntimeException(SYS.SYS066,ex,ex);
			}
			// finally check "implements" and "uses" requests
			// Note this must be done after all referrences are resolved
			boolean errs = getReportWriter().errors();
			for (CompileReference xref : _scriptCompiler._implList) {
				XElement xel2 = xref.getTarget();
				if (xel2 == null) { //Unresolved reference
					xref.putTargetError(getReportWriter());
				} else {
					SPosition spos = xref.getSPosition();
					_sysId = spos.getSysId();
					if (errs) { // previous errors were reported
						//Comparing of models is skipped due to previous errors
						putReport(Report.lightError(XDEF.XDEF229));
					} else {
						ArrayReporter rp = xel2.compareModel(
							(XElement) xref._parent, xref.getKind()==1);
						if (rp != null) {
							Report rep;
							while((rep = rp.getReport()) != null) {
								putReport(rep);
							}
						}
					}
				}
			}
		}
		_scriptCompiler = null; //just let GC do the job;
		_codeGenerator = null;
	}

	/** Modify new selector and return null or new selector. */
	private XSelector modifyReferredSelector(final CompileReference xref,
		final XSelector oldSelector) {
		XSelector newSelector = new XSelector(oldSelector);
		if (xref._empty != -1) {//was specified
			newSelector.setEmptyFlag(xref._empty == 1);
		}
		if (xref.isSpecified()) {//was specified
			newSelector.setOccurrence(xref.minOccurs(), xref.maxOccurs());
		}
		if (xref._matchMethod != -1) {
			newSelector.setMatchCode(xref._matchMethod);
		}
		if (xref._initMethod != -1) {
			newSelector.setInitCode(xref._initMethod);
		}
		if (xref._absenceMethod != -1) {
			newSelector.setOnAbsenceCode(xref._absenceMethod);
		}
		if (xref._excessMethod != -1) {
			newSelector.setOnExcessCode(xref._excessMethod);
		}
		if (xref._setSourceMethod != -1) {
			newSelector.setComposeCode(xref._setSourceMethod);
		}
		if (xref._finallyMethod != -1) {
			newSelector.setFinallyCode(xref._finallyMethod);
		}
		return newSelector;
	}

	/** Copy child nodes, if position in the source and destination differs then
	 * replace selectors by a clone.
	 * @param fromList - the source array.
	 * @param fromIndex - start position in the source array.
	 * @param toList - the destination array.
	 * @param toIndex - start position in the destination data.
	 * @param length - the number of array elements to be copied.
	 */
	private void copyChildNodes(final XNode[] fromList,
		final int fromIndex,
		final XNode[] toList,
		final int toIndex,
		final int length) {
		if (length <= 0) {
			return;
		}
		System.arraycopy(fromList, fromIndex, toList, toIndex, length);
		if (fromIndex == toIndex) {
			return;
		}
		for (int i = toIndex, endIndex = toIndex + length; i < endIndex; i++) {
			XNode xn;
			if ((xn = toList[i]) != null) {
				switch (xn.getKind()) {
					case XNode.XMCHOICE:
					case XNode.XMSEQUENCE:
					case XNode.XMMIXED:
						toList[i] = new XSelector((XSelector) xn);
				}
			}
		}
	}

	/** Resolve references.
	 * @param xel the XElement.
	 * @param level The recursivity level.
	 * @param ingnoreOccurrence if <tt>true</tt> the occurrence specification
	 * from the referred object is ignored.
	 * @param ar node list.
	 * @return true if reverence was resolved.
	 */
	private boolean resolveReference(final XElement xel,
		final int level,
		final boolean ingnoreOccurrence,
		final HashSet<XNode> hs) {
		boolean result = true;
		int lenx;
		if ((lenx = xel._childNodes.length) > 0 &&
			(xel._childNodes[0].getKind() == CompileReference.XMREFERENCE)) {
			CompileReference xref = (CompileReference) xel._childNodes[0];
			XElement y = xref.getTarget();
			if (y == null) {
				xref.putTargetError(getReportWriter()); //Unresolved reference
				xel._childNodes = new XNode[0];
				return false;
			} else if (y == xel &&  //self reference
				xel._childNodes.length==1 && xel.getAttrs().length==0) {
				//Self reference is not allowed: &{0}
				error(xref.getSPosition(), XDEF.XDEF321, xref.getXDPosition());
				XNode[] childNodes = xel._childNodes;
				int newLen = childNodes.length -1;
				xel._childNodes = new XNode[newLen];
				if (newLen > 0) {
					copyChildNodes(childNodes, 1, xel._childNodes, 0, newLen);
				}
				return true;
			} else if (level > MAX_REFERENCE) {
				//Too many nested references or reference loop in &{0}
				error(xref.getSPosition(), XDEF.XDEF320, xref.getXDPosition());
				return false;
			} else if (!resolveReference(
				y, level+1, ingnoreOccurrence && xel.isSpecified(), hs)) {
				return false;
			} else if (!checkIntegrity(y, level+1, hs)) {
				return false;
			} else {
				if (y.getName().indexOf('$') > 0) {
					y = (XElement) y.getChildNodeModels()[0];
				}
			}
			xel.setSqId(y.getSqId());
			xel._vartable = y._vartable;
			xel._varsize = y._varsize;
			xel._varinit = y._varinit;
			//copy specified options from target to unspecified options
			if (xel._trimAttr == 0) {// _trimAttr not set
				if (y._trimAttr != 0) {
					xel._trimAttr = y._trimAttr;
				} else if (xel._definition._trimAttr != 0) {
					xel._trimAttr = xel._definition._trimAttr;
				}
			}
			if (xel._trimText == 0) {// _trimText not set
				if (y._trimText != 0) {
					xel._trimText = y._trimText;
				} else if (xel._definition._trimText != 0) {
					xel._trimText = xel._definition._trimText;
				}
			}
			if (xel._attrWhiteSpaces == 0) { //not _attrWhiteSpaces
				if (y._attrWhiteSpaces != 0) {
					xel._attrWhiteSpaces = y._attrWhiteSpaces;
				} else if (xel._definition._attrWhiteSpaces != 0) {
					xel._attrWhiteSpaces = xel._definition._attrWhiteSpaces;
				}
			}
			if (xel._textWhiteSpaces == 0) { //TextWhiteSpaces
				if (y._textWhiteSpaces != 0) {
					xel._textWhiteSpaces = y._textWhiteSpaces;
				} else if (xel._definition._textWhiteSpaces != 0) {
					xel._textWhiteSpaces = xel._definition._textWhiteSpaces;
				}
			}
			if (xel._ignoreEmptyAttributes == 0) { //not _ignoreEmptyAttributes
				if (y._ignoreEmptyAttributes != 0) {
					xel._ignoreEmptyAttributes = y._ignoreEmptyAttributes;
				} else if (xel._definition._ignoreEmptyAttributes != 0) {
					xel._ignoreEmptyAttributes =
						xel._definition._ignoreEmptyAttributes;
				}
			}
			if (xel._attrValuesCase == 0) { // _attrValuesCase not set
				if (y._attrValuesCase != 0) {
					xel._attrValuesCase = y._attrValuesCase;
				} else if (xel._definition._attrValuesCase != 0) {
					xel._attrValuesCase = xel._definition._attrValuesCase;
				}
			}
			if (xel._textValuesCase == 0) {//_setTextValuesCase not set
				if (y._textValuesCase != 0) {
					xel._textValuesCase = y._textValuesCase;
				} else if (xel._definition._textValuesCase != 0) {
					xel._textValuesCase = xel._definition._textValuesCase;
				}
			}
			if (xel._acceptQualifiedAttr == 0) {//_acceptQualifiedAttr not set
				if (y._acceptQualifiedAttr != 0) {
					xel._acceptQualifiedAttr = y._acceptQualifiedAttr;
				} else if (xel._definition._acceptQualifiedAttr != 0) {
					xel._acceptQualifiedAttr =
						xel._definition._acceptQualifiedAttr;
				}
			}
			if (xel._ignoreComments == 0 && y._ignoreComments != 0) {
				xel._ignoreComments = y._ignoreComments;
			}
			if (xel._moreAttributes == 0 && y._moreAttributes != 0) {
				xel._moreAttributes = y._moreAttributes;
			}
			if (xel._moreElements == 0 && y._moreElements != 0) {
				xel._moreElements = y._moreElements;
			}
			if (xel._moreText == 0 && y._moreText != 0) {
				xel._moreText = y._moreText;
			}
			if (xel._nillable == 0 && y._nillable != 0) {
				xel._nillable = y._nillable;
			}
			if (xel._varinit == -1 && y._varinit != 0) {
				xel._varinit = y._varinit;
			}
			if (xel._varsize == 0 && y._varsize != 0) {// varsize not set
				xel._varsize = y._varsize;
			}
			if (xel._finaly == -1 && y._finaly != -1) {
				xel._finaly = y._finaly;
			}
			if (xel._compose == -1 && y._compose != -1) {
				xel._compose = y._compose;
			}
			if (xel._init == -1 && y._init != -1) {
				xel._init = y._init;
			}
			if (xel._onAbsence == -1 && y._onAbsence != -1) {
				xel._onAbsence = y._onAbsence;
			}
			if (xel._onExcess == -1 && y._onExcess != -1) {
				xel. _onExcess = y._onExcess;
			}
			if (xel._onIllegalText == -1 && y._onIllegalText != -1) {
				xel._onIllegalText = y._onIllegalText;
			}
			if (xel._onIllegalElement == -1 && y._onIllegalElement != -1) {
				xel._onIllegalElement = y._onIllegalElement;
			}
			if (xel._onStartElement == -1 && y._onStartElement != -1) {
				xel._onStartElement = y._onStartElement;
			}
			if (xel._onIllegalAttr == -1 && y._onIllegalAttr != -1) {
				xel._onIllegalAttr = y._onIllegalAttr;
			}
			if (xel._match == -1 && y._match != -1) {
				xel._match = y._match;
			}
			if (xel._forget == 0 && xel._forget != 0
				&& xel._clearAdoptedForgets == 0) {// forget
				xel._forget = y._forget;
			}
			if (xel._deflt == -1 && y._deflt != -1) {
				xel._deflt = y._deflt;
			}
			if (xel._vartable == null && y._vartable != null) {
				xel._vartable = y._vartable;
			}
			if (xel._varinit == -1 && y._varinit != -1) {
				xel._varinit = y._varinit;
			}
			if (xel._varsize == -1 && y._varsize != -1) {
				xel._varsize = y._varsize;
			}
			if (!xel.isSpecified() && y.isSpecified()) {
				xel.setOccurrence(y);
			}
			int leny = y._childNodes.length;
			xel.setReferencePos(y.getXDPosition());
			if (xel._childNodes.length == 1 && xel.getAttrs().length == 0) {
				xel._attrs = y._attrs;
				xel._childNodes = y._childNodes;
				xel.setReference(true);
				return true;
			} else {
				xel.setReference(false);
				xel.setReferencePos(null);
				//copy old attributes and update XDPositions
				String basePos = xel.getXDPosition();
				for (String name: y.getXDAttrNames()) {
					if (!xel.hasDefAttr(name)) {// the (new) declared we leave
						// update XDPosition
						XData attr = new XData(y.getDefAttr(name, -1));
						attr.setXDPosition(basePos + "/@" + name);
						xel.setDefAttr(attr);
					}
				}
				//replace reference with child nodes of the referred node
				lenx--; //reference itself we remove
				XNode[] childNodes = new XNode[lenx + leny];
				if (leny > 0) {
					copyChildNodes(y._childNodes, 0, childNodes, 0, leny);
				}
				copyChildNodes(xel._childNodes, 1, childNodes, leny, lenx);
				lenx += leny;
				xel._childNodes = childNodes;
			}
		}
		if (!xel.isSpecified()) {
			xel.setRequired(); //interval not set, let's set defaults
		}
		int i = 0;
		while(i < lenx) {//resolve include references
			if (xel._childNodes[i].getKind() != CompileReference.XMINCLUDE) {
				i++;
				continue;
			}
			CompileReference xref = (CompileReference) xel._childNodes[i];
			if (level > MAX_REFERENCE) {
				//Too many nested references or reference loop in &{0}
				error(xref.getSPosition(), XDEF.XDEF320, xref.getXDPosition());
				return false;
			}
			XElement y = xref.getTarget();
			if (y == null) {
				xref.putTargetError(getReportWriter());//Unresolved reference
				XElement xe = new XElement("?", null, xel._definition);
				xe.setSPosition(xref.getSPosition());
				xe.setXDPosition(xel.getXDPosition() + "/?");
				xel._childNodes[i] = xe;
				i++;
				result = false;
				continue;
			}
			result &= resolveReference(y, level+1,
				ingnoreOccurrence && xel.minOccurs() != XOccurrence.UNDEFINED,
				hs);
			int leny = y._childNodes.length;
			boolean isList = y.getName().endsWith("!list");
			y = new XElement(y); //create clone of an element
			y.setXDPosition(xel.getXDPosition());
			XNode[] childNodes = new XNode[y._childNodes.length];
			int nestedSelectors = 0;
			for (int j = 0; j < childNodes.length; j++) {//modify min, max
				XNode xn = y._childNodes[j];
				switch (xn.getKind()) {
					case XNode.XMELEMENT:
						if (isList && xref.isSpecified()) {
							XElement xe = (XElement) xn;
							if (xe.minOccurs() != xref.minOccurs() ||
								xe.maxOccurs() != xref.maxOccurs()) {
								xe = new XElement(xe);
								xe.setOccurrence(xref);
								xn = xe;
							}
						}
						break;
					case XNode.XMTEXT:
						if (isList && xref.isSpecified()) {
							XData xa = (XData) xn;
							if (xa.minOccurs() != xref.minOccurs() ||
								xa.maxOccurs() != xref.maxOccurs()) {
								xa = new XData(xa);
								xa.setOccurrence(xref);
								xn = xa;
							}
						}
						break;
					case XNode.XMCHOICE:
					case XNode.XMMIXED:
					case XNode.XMSEQUENCE: {
						if (nestedSelectors == 0) {
							XNode xs = modifyReferredSelector(
								xref, (XSelector) xn);
							if (xs != null) {
								xn = xs;
							}
						} else { //we just clone nested selectors
							xn = new XSelector((XSelector) xn);
						}
						nestedSelectors++;
						break;
					}
					case XNode.XMSELECTOR_END:
						nestedSelectors--;
						break;
					default:
						break;
				}
				childNodes[j] = xn;
			}
			y._childNodes = childNodes;
			childNodes = xel._childNodes;
			if (leny == 1) {
				xel._childNodes[i] = y._childNodes[0];
			}
			int newLen = lenx + leny - 1;
			xel._childNodes = new XNode[newLen];
			if (i > 0) {
				copyChildNodes(childNodes, 0, xel._childNodes, 0, i);
			}
			if (leny > 0) {
				copyChildNodes(y._childNodes, 0, xel._childNodes, i, leny);
			}
			if (i < lenx - 1) {
				copyChildNodes(childNodes,i+1,xel._childNodes,i+leny,lenx-i-1);
			}
			lenx = newLen;
		}
		return result;
	}

	/** Check integrity of the node and resolve references.
	 * @param xel the XElement.
	 * @param level The recursivity level.
	 * @param ar node list.
	 * @return true if check was successful.
	 */
	private boolean checkIntegrity(final XElement xel,
		final int level,
		final HashSet<XNode> hs) {
		if (!hs.add(xel)) {
			return true; //already done
		}
		boolean result = resolveReference(xel, level+1, xel.isSpecified(), hs);
		if (result) {
			for (XNode dn: xel._childNodes) {
				if (dn.getKind() == XNode.XMELEMENT && !hs.contains(dn)) {
					XElement xe = (XElement) dn;
					result &= checkIntegrity(xe, level+1, hs);
				} else if (dn.getKind() == XNode.XMTEXT) {
					if (!dn.isSpecified()) {
						dn.setOptional();
					}
				}
			}
		}
		if (!xel.isSpecified()) {
			xel.setRequired(); //interval not set, let's set defaults
		}
		return result;
	}

	/** Update selectors.
	 * @param xel the XElement.
	 * @param index index of item where updating starts.
	 * @param selector selector or <tt>null</tt>.
	 * @param ignorableFlag flag if the item can be ignored.
	 * @param selectiveFlag flag if the item is selective in choice section.
	 * @param hs hash map with processed X-nodes.
	 * @return index of last processed item.
	 */
	private int updateSelectors(final XElement xel,
		final int index,
		final XSelector selector,
		final boolean ignorableFlag,
		final boolean selectiveFlag,
		final HashSet<XNode> hs) {
		hs.add(xel);
		HashMap<String, Integer> groupItems = new HashMap<String, Integer>();
		boolean ignorable = ignorableFlag;
		boolean selective = selectiveFlag;
		boolean notReported = true;
		boolean empty = true;
		short selectorKind = selector == null ?
			XNode.XMSEQUENCE : selector.getKind();
		for (int i = index; i < xel._childNodes.length; i++) {
			XNode xn = xel._childNodes[i];
			short kind;
			switch (kind = xn.getKind()) {
				case XNode.XMTEXT: {
					XData x;
					int min;
					min = (x = (XData) xn).minOccurs();
					if (selectorKind==XNode.XMCHOICE) {
						ignorable |= min <= 0;
					} else {
						ignorable &= min <= 0;
					}
					empty &= min <= 0;
					selective = false;
					if ((selectorKind==XNode.XMCHOICE ||
						selectorKind==XNode.XMMIXED)
						&& x._match < 0) { //we igore items with match
						String s = "$text";
						Integer j;
						if ((j = groupItems.get(s)) != null && notReported) {
							XData y = (XData) xel._childNodes[j];
							if (y._match == -1) { // we accept items with match
								//Ambiguous group '&{0}' (equal items)
								// in XDefinition '&{1}'
								error(x.getSPosition(), XDEF.XDEF234,
									(selectorKind==XNode.XMCHOICE ?
									"choice" : "mixed"),
									xel._definition.getName());
								notReported = false;
							}
						}
						groupItems.put(s, i);
					} else if (selectorKind==XNode.XMSEQUENCE && i > 0
						&& xel._childNodes[i-1].getKind() == XNode.XMTEXT
						&& ((XData) xel._childNodes[i-1])._match < 0) {
						//Ambiguous X-definition: text node can´t follow
						// previous text node
						error(x.getSPosition(), XDEF.XDEF239);
						notReported = false;
					}
					continue;
				}
				case XNode.XMELEMENT: {
					int min;
					XElement x;
					min = (x = (XElement) xn).minOccurs();
					if (!hs.contains(x)) {
						updateSelectors(
							x, 0, null, false, false, hs);
					}
					if (selectorKind== XNode.XMCHOICE) {
						ignorable |= min == 0;
					} else {
						ignorable &= min <= 0;
					}
					empty &= min <= 0;
					selective = false;
					if (selectorKind==XNode.XMCHOICE ||
						selectorKind==XNode.XMMIXED) {
						String s = x.getNSUri() == null ? x.getName() :
							('{' + x.getNSUri() + '}' + x.getName());
						Integer j;
						if ((j = groupItems.get(s)) != null && notReported) {
							XElement y = (XElement) xel._childNodes[j];
							if (y._match == -1) {// we accept items with match
								//Ambiguous group '&{0}' (equal items)
								// in XDefinition '&{1}'
								error(x.getSPosition(), XDEF.XDEF234,
									(selectorKind==XNode.XMCHOICE ?
									"choice" : "mixed"),
									xel._definition.getName());
								notReported = false;
							}
						}
						groupItems.put(s, i);
					} else if (selectorKind==XNode.XMSEQUENCE && i > 0
						&& xel._childNodes[i-1].getKind() == XNode.XMELEMENT
						&& x.getName().equals(xel._childNodes[i-1].getName())) {
						// get previous node (we know it is XElement)
						XElement y = (XElement) xel._childNodes[i-1];
						if (y.isSpecified() // (occurrence)
							&& y.maxOccurs()!=y.minOccurs() && y._match==-1) {
							if (y.maxOccurs()==Integer.MAX_VALUE) {
								//Ambiguous X-definition: previous element
								// with same name has unlimited occurrence
								error(x.getSPosition(), XDEF.XDEF238);
							} else if (!x.isSpecified()  // (occurrence)
								|| x.minOccurs() > 0){
								//Ambiguous X-definition: minimum occurrence
								// must be zero
								error(x.getSPosition(),XDEF.XDEF235);
								notReported = false;
							}
						}
					}
					continue;
				}
				case XNode.XMCHOICE:
				case XNode.XMSEQUENCE:
				case XNode.XMMIXED: {
					XSelector xs = (XSelector) xn;
					if (hs.add(xs)) { //not processed yet
						xs.setBegIndex(i);
						selective = kind == XNode.XMCHOICE ||
							selective && kind == XNode.XMSEQUENCE;
						xs.setSelective(selective);
						i = updateSelectors(xel,
							i + 1,
							xs,
							kind==XNode.XMCHOICE || ignorable,
							selective,
							hs);
						xs.setEndIndex(i);
						if (i - xs.getBegIndex() <= 1) {
							//Empty group '&{0}' in XDefinition '&{1}'
							error(xs.getSPosition(), XDEF.XDEF325,
								xs.getName().substring(1),
								xel._definition.getName());
							xs.setIgnorable(ignorable = true);
						}
						if (xs.getKind() == XNode.XMCHOICE &&
							xs.minOccurs() <= 0) {
							xs.setIgnorable(ignorable);
						} else {
							xs.setIgnorable(ignorable || xs.minOccurs() <= 0);
						}
						if (xs.isEmptyDeclared()) {
							xs.setEmptyFlag(!xs.isEmptyFlag());
						}
						ignorable &= xs.isIgnorable();
						empty &= xs.minOccurs() <= 0;
						continue;
					} else {//already processed
						empty &= xs.minOccurs() <= 0;
						return xs.getEndIndex();
					}
				}
				case XNode.XMSELECTOR_END:
					if (selector != null && selectorKind == XNode.XMMIXED) {
						if (!selector.isSpecified()) {
							selector.setOccurrence(empty ? 0 : 1, 1);
						} else if (selector.minOccurs() > 1
							|| selector.maxOccurs() > 1) {
							error(selector.getSPosition(), XDEF.XDEF115);
						}
					}
					return i;
//				case XNode.XMCOMMENT:
//				case XNode.XMPI:
				default:
			}
		}
		return xel._childNodes.length;
	}

	/** Clear adopted forgets.
	 * @param xel XElement.
	 * @param clear adopted clear flag.
	 * @param hs hashset with nodes.
	 */
	private void clearAdoptedForgets(final XElement xel,
		final boolean clear,
		final HashSet<XNode> hs) {
		hs.add(xel);
		boolean clr = clear | xel._clearAdoptedForgets == 'T';
		boolean newChildNodes = false;
		for (int i = 0; i < xel._childNodes.length; i++) {
			XNode dn = xel._childNodes[i];
			if (dn.getKind() == XNode.XMELEMENT) {
				XElement xe = (XElement) dn;
				if (clr) {
					if (xe._forget == 'T') {
						if (!newChildNodes) {
							XNode[] oldNodes = xel._childNodes;
							copyChildNodes(oldNodes,
								0, xel._childNodes, 0, oldNodes.length);
							newChildNodes = true;
						}
						xe = new XElement(xe);
						xe._forget = 0;
						xel._childNodes[i] = xe;
					}
				}
				if (!hs.contains(xe)) {
					clearAdoptedForgets(xe, clr, hs);
				}
			}
		}
	}

	/** The object value of attribute.*/
	private final class AttrValue {
		final String _name; //qualified name of the attribute
		String _localName; //Local name of the attribute
		final SBuffer _value; //Value of attribute
		int _nsindex; //Index to the namespace id (-1 in no namespace)
		String _nsURI;  //namespace URI

		/** Create new instance of AttrValue.
		 * @param name the quoted name of attribute.
		 * @param value the SBuffer object with the value of attribute.
		 */
		private AttrValue(final String name,
			final SBuffer value,
			final String nsURI,
			final int nsindex) {
			_name = name;
			_localName = null;
			_nsURI = nsURI;
			_nsindex = nsindex;
			_value = value;
		}

		@Override
		/** Check another attribute if it is equal to this one. We consider
		 * two attributes equal if both local names and name spaces are equal.
		 * @param o The object to be compared.
		 */
		public boolean equals(final Object o) {
			if (o == null || !(o instanceof AttrValue)) {
				return false;
			}
			AttrValue attr = (AttrValue) o;
			return _localName.equals(attr._localName) &&
				_nsindex == attr._nsindex;
		}
		@Override
		/** Returns hash code of the object. */
		public int hashCode() {
			int hash = 89 * 7 + _localName.hashCode();
			return 89 * hash + _nsindex;
		}
		@Override
		public String toString() {
			return _name + "=" + _value;
		}
	}

	/** Object with the parsed node. */
	private final class PNode {
		final List<AttrValue> _attrs; //attributes
		final List<PNode> _childNodes; //child nodes
		final Map<String, Integer> _nsPrefixes; // namespace prefixes
		final SBuffer _name; //qualified name of node
		String _localName;  //local name of node
		String _nsURI;  //namespace URI
		final PNode _parent; //parent node
		int _level; //nesting level of this node
		SBuffer _value; //String node assigned to this node
		int _nsindex; //namespace index of this node
		XDefinition _xdef;  //XDefinition associated with this node
		byte _xdVersion;  // version of X-definion
		boolean _template;  //template

		/** Creates a new instance of PNode.
		 * @param name The node name.
		 * @param position The position in the source text.
		 * @param parent The parent node.
		 */
		private PNode(final String name,
			final SPosition position,
			final PNode parent,
			final byte xdversion) {
			_name = new SBuffer(name, position);
			_childNodes = new ArrayList<PNode>();
			_attrs = new ArrayList<AttrValue>();
			_nsPrefixes = new TreeMap<String, Integer>();
			_xdVersion = xdversion;
			if (parent == null) {
				_nsPrefixes.putAll(_predefinedNSPrefixes);
				_template = false;
			} else {
				_template = parent._template;
				_nsPrefixes.putAll(parent._nsPrefixes);
			}
			_parent = parent;
			_nsindex = -1;
//			_level = 0; // java makes it
//			_value = null; // java makes it
//			_def = null; // java makes it
		}

		/** Get list of child nodes of given name (not recursive).
		 * @param name The name.
		 * @return the list of child nodes of given name.
		 */
		private ArrayList<PNode> getXDefChildNodes(final String name) {
			ArrayList<PNode> result = new ArrayList<PNode>();
			for (PNode node : _childNodes) {
				if (node._nsindex == 0 && node._localName.equals(name)) {
					result.add(node);
				}
			}
			return result;
		}

		/** Remove child nodes.
		 * @param list The list of nodes to be removed.
		 */
		private void removeChildNodes(final ArrayList<PNode> list) {
			_childNodes.removeAll(list);
		}

		/** Get attribute of given name with X=definition name space.
		 * If required attribute doesn't exist return null.
		 * @param localName key name of attribute.
		 * @param nsIndex The index of name space (0 == XDEF).
		 * @return the object SParsedData with the attribute value or null.
		 */
		private AttrValue getAttrNS(final String localName, final int nsIndex) {
			AttrValue xattr = null;
			for (AttrValue a : _attrs) {
				if (localName.equals(a._localName) && a._nsindex == nsIndex) {
					xattr = a;
				}
			}
			return xattr;
		}

		/** Get attribute of given name with or without name space prefix from
		 * node. The attribute is removed from the list. If the argument
		 * required is set to true put error message that required attribute
		 * is missing.
		 * @param localName The local name of attribute.
		 * @param required if true the attribute is required.
		 * @param remove if true the attribute is removed.
		 * @return the object SParsedData with the attribute value or null.
		 */
		private SBuffer getXdefAttr(final String localName,
			final boolean required,
			final boolean remove) {
			AttrValue attr = null;
			AttrValue xattr = null;
			for (AttrValue a : _attrs) {
				if (localName.equals(a._localName) && a._nsindex <= 0) {
					if (a._nsindex == 0) {
						xattr = a;
					} else {
						attr = a;
					}
				}
			}
			if (xattr != null && attr != null) {
				//The attribute '&{0}' can't be specified simultanously
				//with and without namespace
				error(attr._value, XDEF.XDEF230, localName);
			} else if (xattr == null) {
				xattr = attr;
			}
			if (xattr == null) {
				if (required) {
					//Required attribute '&{0}' is missing
					error(_name, XDEF.XDEF323, "xd:" + localName);
				}
				return null;
			} else {
				if (remove) {
					_attrs.remove(xattr);
				}
				return xattr._value;
			}
		}

		/** Get "name" (or "prefix:name") of node.
		 * If the argument required is set to true put error message that
		 * required attribute is missing.
		 * @param required if true the attribute is required.
		 * @param remove if true the attribute is removed.
		 * @return the name or null.
		 */
		private String getNameAttr(final boolean required,
			final boolean remove) {
			SBuffer sval = getXdefAttr("name", required, remove);
			if (sval == null) {
				return required ? ("__UNKNOWN__" + _unknownCounter++) : null;
			}
			String name = sval.getString().trim();
			if (name == null || name.length() == 0) {
				error(sval, XDEF.XDEF258); //Incorrect name
				return "__UNKNOWN__" + _unknownCounter++;
			}
			if (!chkDefName(name)) {
				error(sval, XDEF.XDEF258); //Incorrect name
				return "__UNKNOWN__" + _unknownCounter++;
			}
			return name;
		}
		
		private boolean isXScriptName(StringParser p) {
			if (p.getXmlCharType(_xmlVersion1) != StringParser.XML_CHAR_NAME_START) {
				return false;
			}
			StringBuilder sb = new StringBuilder(String.valueOf(p.peekChar()));
			char c;
			boolean wasColon = false;
			while (StringParser.getXmlCharType(c = p.getCurrentChar(), _xmlVersion1) ==
				StringParser.XML_CHAR_NAME_START ||
				(c >= '0' && c <= '9') || (!wasColon && c == ':')) {
				if (c == ':') { // we allow one colon inside the name
					wasColon = true;
					c = p.nextChar();
					if (p.getXmlCharType(_xmlVersion1) != StringParser.XML_CHAR_NAME_START) {
						p.setBufIndex(p.getIndex() - 1);  // must follow name, ignore ':'
						break;
					}
					sb.append(':');
				}
				sb.append(c);
				p.nextChar();
			}
			p.setParsedString(sb.toString());
			return true;
		}
		
		private static final int MAX_NESTED_MACRO = 100;
		
		/** Parse macro references in the source text.
		 * @param nestingLevel level of nested reference (to prevent cycle).
		 * @param p String parser.
		 * @return string with expanded macro references.
		 */
		private String parseMacro(int nestingLevel,
			final StringParser p,
			final String actDefName) throws SRuntimeException {
			if (nestingLevel >= MAX_NESTED_MACRO) {
				macError(0, XDEF.XDEF486); //Too many nested macros
				return null;
			}
			if (!isXScriptName(p)) {
				 //Macro name error
				macError(nestingLevel, XDEF.XDEF484);
				return null;
			}
			String macName = p.getParsedString();
			if (p.isChar('#')) {
				if (!isXScriptName(p)) {
					//Macro name error
					macError(nestingLevel, XDEF.XDEF484);
					return null;
				}
				macName += '#' + p.getParsedString();
			} else {
				macName = actDefName + '#' + macName;
			}
			XScriptMacro macro = _macros.get(macName);
			if (macro == null) {
				//Macro '&{0}' doesn't exist
				macError(nestingLevel, XDEF.XDEF483,macName);
				return null;
			}
			String[] params = null;
			StringBuilder sb;
			String s, s1;
			StringParser p1;
			int ndx;
			int level;
			p.skipSpaces();
			if (p.isChar('(')) { // parameters
				p.skipSpaces();
				while (isXScriptName(p)) {
					String parName = p.getParsedString();
					int index = macro.getParamNames().indexOf(parName);
					if (index < 0) {
						//Unknown parameter '&{0}' of macro '&{1}'
						macError(nestingLevel, XDEF.XDEF497, parName, macName);
						return null;
					}
					p.skipSpaces();
					if (!p.isChar('=')) {
						 //'&{0}' expected
						macError(nestingLevel, XDEF.XDEF410, "=");
						return null;
					}
					p.skipSpaces();
					char delimiter;
					if ((delimiter = p.isOneOfChars("'\"")) == NOCHAR) {
						//String specification expected
						macError(nestingLevel, XDEF.XDEF493);
						return null;
					}
					// parse the string constant, but we copy all escapes
					sb = new StringBuilder();
					for(;;) {
						if (!p.chkBufferIndex()) {
							//Unclosed string specification
							macError(nestingLevel, XDEF.XDEF403);
							return null;
						}
						char c;
						if ((c = p.peekChar()) == delimiter) {
							break;
						}
						if (c == '\\') {
							if (!p.chkBufferIndex()) {
								//Unclosed string specification
								macError(nestingLevel,XDEF.XDEF403);
								return null;
							}
							c = p.peekChar();
						}
						sb.append(c);
					}
					if (index >= 0) {
						if (params == null) {
							params = new String[macro.getParamValues().length];
							System.arraycopy(macro.getParamValues(),
								0, params, 0, params.length);
						}
						s = sb.toString();
						level = nestingLevel;
						while ((ndx = s.indexOf("${")) >= 0) {
							p1 = new StringParser(s.substring(ndx));
							p1.nextChar(); p1.nextChar(); //pos + 2
							s1 = parseMacro(++level, p1, actDefName);
							if (s1 == null) {
								return null;
							}
							sb = new StringBuilder(s.substring(0,ndx));
							sb.append(s1);
							if (!p1.eos()) {
								sb.append(p1.getBufferPartFrom(p1.getIndex()));
							}
							s = sb.toString();
							sb.setLength(0);
						}
						params[index] = s;
					}
					p.skipSpaces();
					if (p.isChar(',')) {
						p.skipSpaces();
					} else {
						break;
					}
				}
				if (!p.isChar(')')) {
					 //'&{0}' expected
					macError(nestingLevel, XDEF.XDEF410, ")");
					return null;
				}
			}
			if (!p.isChar('}')) {
				//'&{0}' expected
				macError(nestingLevel, XDEF.XDEF410, "}");
				return null;
			}
			if (params == null) {
				params = macro.getParamValues();
			}
			s = macro.expand(params);
			level = nestingLevel;
			while ((ndx = s.indexOf("${")) >= 0) {
				p1 = new StringParser(s.substring(ndx));
				p1.nextChar(); p1.nextChar(); //pos + 2
				s1 = parseMacro(++level, p1, actDefName);
				if (s1 == null) {
					return null;
				}
				sb = new StringBuilder(s.substring(0,ndx));
				sb.append(s1);
				if (!p1.eos()) {
					sb.append(p1.getBufferPartFrom(p1.getIndex()));
				}
				s = sb.toString();
			}
			return s;
		}

		/** Report macro error with modification.
		 * @param level nesting level.
		 * @param id registered message ID.
		 * @param mod Message modification parameters.
		 */
		private void macError(final int level,
			final long id,
			final Object... mod) {
			if (level > 1) {
				return;
			}
			if (level == 1) {
				//Error in nested macro
				throw new SRuntimeException(XDEF.XDEF498);
			} else {
				throw new SRuntimeException(id, mod);
			}
		}

		/** Expands all macro references in the source buffer. */
		private void expandMacros(final SBuffer sb,
			final StringParser p,
			final String actDefName) {
			// Save original position
			int ndx;
			if ((ndx = sb.getString().lastIndexOf("${")) < 0 ||
				ndx + 3 >= sb.getString().length()) {
				return; //no macro
			}
			p.setSourceBuffer(sb);
			int savedPos = p.getIndex();
			long savedLine = p.getLineNumber();
			long savedStartLine = p.getStartLine();
			long savedFilePos = p.getFilePos();
			//Expand macro references (backward from the end of buffer).
			try {
			do {
				p.setBufIndex(ndx + 2);
				// Parse macro
				String replacement = parseMacro(0, p, actDefName);
				if (replacement == null) {
					break; //an error detected, finish
				}
				// Replace the macro reference with result
				p.changeBuffer(ndx, p.getIndex() - ndx, replacement, true);
				// check if the new macro occurred after this replacement
				int level = 1;
				while (--ndx >= 0 && p.getCharAtPos(ndx) == '$' &&
					replacement.startsWith("{")) {
					// A new nested macro occurred after this replacement
					// Set parser positinon at the macro
					if (ndx + 2 < p.getEndBufferIndex()) {
						p.setBufIndex(ndx + 2);
					} else {
						p.setEos();
					}
					// Parse macro
					replacement = parseMacro(level++, p, actDefName);
					if (replacement == null) {
						break; //an error detected, finish
					}
					// Replace the macro reference with result
					p.changeBuffer(ndx, p.getIndex() - ndx, replacement, true);
					if (!p.chkBufferIndex()) {
						p.setEos();
					}
				}
			} while ((ndx = p.getSourceBuffer().lastIndexOf("${")) >= 0 &&
				ndx + 3 < p.getEndBufferIndex());
			} catch (SRuntimeException ex) {
				p.putReport(ex.getReport());				
			}
			//reset parser to original position
			p.setLineNumber(savedLine);
			p.setStartLine(savedStartLine);
			p.setFilePos(savedFilePos);
			p.setBufIndex(savedPos);		
			sb.setPosition(p);
			sb.setString(p.getSourceBuffer());
 		}

		private void expandMacros(final ReportWriter reporter,
			final String actDefName) {
			StringParser p = new StringParser();
			p.setLineInfoFlag(true);
			p.setReportWriter(reporter);
			for (AttrValue x: _attrs) {
				if (x._value.getString().indexOf("${") >= 0) {
					expandMacros(x._value, p, actDefName);
				}
			}
			if (_value != null) {
				String s = _value.getString();
				int ndx = s.lastIndexOf("${");
				if (ndx >= 0) {
					expandMacros(_value, p, actDefName);
				}
			}
			for (PNode x: _childNodes) {
				x.expandMacros(reporter, actDefName);
			}
		}
		
		@Override
		public String toString() {
			return "PNode: " + _name.getString();
		}
	}
}