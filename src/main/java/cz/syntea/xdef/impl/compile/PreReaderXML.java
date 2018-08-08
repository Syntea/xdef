/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: PreReaderXML.java, created 2018-07-21.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.impl.XDefinition;
import cz.syntea.xdef.impl.ext.XExtUtils;
import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.msg.XML;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.SThrowable;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.xml.KParsedAttr;
import cz.syntea.xdef.xml.KParsedElement;
import cz.syntea.xdef.xml.KXmlConstants;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Reads source X-definitions and prepares list of PNodes with X-definitions
 * from XML source data.
 * @author Trojan
 */
class PreReaderXML extends XmlDefReader {

	/** Actual node */
	private PNode _actPNode;
	/** includes. */
	private Element _includeElement;
	/** The nesting level of XML node. */
	private int _level;

	private final XPreCompiler _pcomp;

	/** Creates a new instance of XDefCompiler
	 * @param reporter reporter.
	 * @param pcomp pre compiler.
	 */
	PreReaderXML(final ReportWriter reporter,
		XPreCompiler pcomp) {
		super(reporter);
		_pcomp = pcomp;
	}

	@Override
	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 * @param parsedElem contains name of the element, name space URI and
	 * the list of attributes.
	 */
	public final void elementStart(final KParsedElement parsedElem) {
		String qName = parsedElem.getParsedName();
		if (_includeElement == null) {
			if (_actPNode != null && _actPNode._value != null &&
				_actPNode._value.getString().length() > 0) {
				processText();
			}
			_actPNode = new PNode(qName,
				parsedElem.getParsedNameSourcePosition(),
				_actPNode,
				_actPNode==null? (byte) 0 : _actPNode._xdVersion,
				"1.1".equals(getXmlVersion()) ? (byte) 11 : (byte) 10);
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
			_pcomp.getCodeGenerator()._namespaceURIs.remove(0);
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
				_pcomp.getCodeGenerator()._namespaceURIs.add(0, projectNS);
			} else {
				_pcomp.getCodeGenerator()._namespaceURIs.add(0, uri);
				//X-definition or X-collection expected
				error(_actPNode._name, XDEF.XDEF255);
			}
		}
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			String key = ka.getName();
			String value = ka.getValue();
			if (key.startsWith("xmlns")) { //addAttr namespace URI to the list.
				int nsndx = _pcomp.getCodeGenerator()._namespaceURIs.indexOf(
					value.trim());
				if (nsndx < 0) {
					nsndx = _pcomp.getCodeGenerator()._namespaceURIs.size();
					_pcomp.getCodeGenerator()._namespaceURIs.add(value.trim());
				}
				if (key.length() == 5) { //default prefix
					_actPNode._nsPrefixes.put("", nsndx);
				} else if (key.charAt(5) == ':') { //prefix name
					_actPNode._nsPrefixes.put(key.substring(6), nsndx);
				}
			} else if ("collection".equals(elemLocalName) &&
				key.startsWith("impl-")) {//continue; ignore, just documentation
			} else {
				PAttr item = new PAttr(key,
					new SBuffer(value, ka.getPosition()), null, -1);
				if ((ndx = key.indexOf(':')) >= 0) {
					String prefix = key.substring(0, ndx);
					item._localName = key.substring(ndx + 1);
					Integer nsndx = _actPNode._nsPrefixes.get(prefix);
					if (nsndx == null) {
						String u;
						if ((u = ka.getNamespaceURI()) != null) {
							int x = _pcomp.getCodeGenerator()
								._namespaceURIs.indexOf(u);
							if (x < 0) {
								nsndx = _pcomp.getCodeGenerator()
									._namespaceURIs.size();
								_pcomp.getCodeGenerator()
									._namespaceURIs.add(u);
							} else {
								nsndx = x;
							}
							_actPNode._nsPrefixes.put(prefix, nsndx);
						}
					}
					if (nsndx != null) {
						item._nsURI =
							_pcomp.getCodeGenerator()._namespaceURIs.get(nsndx);
						if ((item._nsindex=nsndx) == XPreCompiler.NS_XDEF_INDEX
							&& "script".equals(item._localName)) {
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
			if (((urindx = nsuriIndex) == XPreCompiler.NS_XINCLUDE_INDEX)) {
				String nsuri =
					_pcomp.getCodeGenerator()._namespaceURIs.get(urindx);
				Element el;
				if (_includeElement == null) {
					el = _includeElement = KXmlUtils.newDocument(nsuri,
						_actPNode._name.getString(), null).getDocumentElement();
				} else {
					el = _includeElement.getOwnerDocument().createElementNS(
						nsuri, _actPNode._name.getString());
					_includeElement.appendChild(el);
				}
				for (PAttr aval: _actPNode._attrs) {
					if (aval._nsindex < 0) {
						el.setAttribute(aval._name, aval._value.getString());
					} else {
						el.setAttributeNS(
							_pcomp.getCodeGenerator()._namespaceURIs.get(
								aval._nsindex),
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
				_pcomp.processIncludeList(_actPNode);
				_pcomp.reportNotAllowedAttrs(_actPNode);
				_pcomp.getPCollections().add(_actPNode);
			} else if ("BNFGrammar".equals(elemLocalName)) {
				_level++;
				 _pcomp.getPBNFs().add(_actPNode);
			} else if ("thesaurus".equals(elemLocalName)) {
				_level++;
				_pcomp.getPThesaurusList().add(_actPNode);
			} else if ("declaration".equals(elemLocalName)) {
				_level++;
				_pcomp.getPDeclarations().add(0, _actPNode);
			} else if ("component".equals(elemLocalName)) {
				_level++;
				_pcomp.getPComponents().add(0, _actPNode);
			} else {
				if (!"def".equals(elemLocalName)) {
					error(_actPNode._name, XDEF.XDEF259);//X-definition expected
				}
				_level++;
				String defName = getNameAttr(_actPNode, false, true);
				if (defName == null) {
					defName = "";
				}
				// Because there is not yet connected an X-definition to
				// the PNode we create a dumy one in fact just to store
				// the X-definition name (we nead it to be able to compile
				// internal declarations, BNGGrammars, components and
				// thesaurus items).
				_actPNode._xdef = new XDefinition(defName,
					null, null, null, _actPNode._xmlVersion);
				_pcomp.processIncludeList(_actPNode);
				// check duplicate of X-definition
				for (PNode p: _pcomp.getPXDefs()) {
					if (defName.equals(p._xdef.getName())) {
						if (defName.length() == 0) {
							//Only one X-definition in the compiled XDPool
							// may be without name
							error(_actPNode._name, XDEF.XDEF212);
						} else {
							//X-definition '&{0}' already exists
							error(_actPNode._name, XDEF.XDEF303, defName);
						}
						defName = null;
//						String s = null;
//						for (int count = 1; s == null; count++) {
//							s = defName + "_DUPLICATED_NAME_" + count;
//							for (PNode q: _xdefPNodes) {
//								if (s.equals(q. _xdef.getName())) {
//									s = null;
//									break;
//								}
//							}
//						}
//						defName = s;
					}
				}
				if (defName != null) {
//					_xdefNames.add(defName);
					_pcomp.getPXDefs().add(_actPNode);
				}
			}
		} else {
			_level++;
			_actPNode._parent._childNodes.add(_actPNode);
		}
//		if (_level == 1 && "declaration".equals(elemLocalName)) {
//			_listDecl.add(0, _actPNode);
//		}
	}

	/** Get "name" (or "prefix:name") of node.
	 * If the argument required is set to true put error message that
	 * required attribute is missing.
	 * @param pnode PNode where the attribute "name" is required.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the name or null.
	 */
	private String getNameAttr(final PNode pnode,
		final boolean required,
		final boolean remove) {
		SBuffer sval = _pcomp.getXdefAttr(pnode, "name", required, remove);
		if (sval == null) {
			// if required do not return null!
			return required ? ("_UNKNOWN_REQUIRED_NAME_") : null;
		}
		String name = sval.getString();
		if (name.length() == 0) {
			//Incorrect name
			error(sval, XDEF.XDEF258);
			return "__UNKNOWN_ATTRIBUTE_NAME_";
		}
		if (!PreReaderXML.chkDefName(name, pnode._xmlVersion)) {
			error(sval, XDEF.XDEF258); //Incorrect name
			return "__UNKNOWN_INCORRECT_NAME_";
		}
		return name;
	}

	@Override
	/** This method is invoked when parser reaches the end of element. */
	public final void elementEnd() {
		if (_includeElement != null) {
			String ns = _includeElement.getPrefix();
			ns = ns == null || ns.length() == 0 ? "xmlns" : "xmlns:" + ns;
			NamedNodeMap nm = _includeElement.getAttributes();
			if (nm.getLength() > 0) { //other attributes
				for (int i = 0; i < nm.getLength(); i++) {
					Node n = nm.item(i);
					String name = n.getNodeName();
					if (!"href".equals(name) && !"parse".equals(name)
						&& !ns.equals(name)) {
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
			_actPNode._nsindex != XPreCompiler.NS_XDEF_INDEX) {
			if (text != null && !text.getString().isEmpty()) {
				SBuffer sval = new SBuffer(text.getString(), text);
				if (_actPNode._value == null) {
					_actPNode._value = sval;
				} else {
					_actPNode._value.appendToBuffer(sval);
				}
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
		if (_actPNode._nsindex == XPreCompiler.NS_XDEF_INDEX &&
			"def".equals(_actPNode._localName)) {
			XScriptParser xparser = new XScriptParser(_actPNode._xmlVersion);
			xparser.setLineInfoFlag(true);
			xparser.setReportWriter(getReportWriter());
			xparser.setSourceBuffer(_actPNode._value);
			// it still may be a comment
			if (xparser.nextSymbol() != XScriptParser.NOCHAR) {
				//Text value is not allowed here
				lightError(_actPNode._value, XDEF.XDEF260);
			}
			_actPNode._value = null;//prevent repeated message, remove this text
		}
	}

	/** Generate text node */
	private void genTextNode() {
		String name = "";
		for (String prefix : _actPNode._nsPrefixes.keySet()) {
			if (_actPNode._nsPrefixes.get(prefix)==XPreCompiler.NS_XDEF_INDEX) {
				name = prefix + ":text";
				break;
			}
		}
		PNode p = new PNode(name,
			new SPosition(_actPNode._value),
			_actPNode,
			_actPNode._xdVersion,
			_actPNode._xmlVersion);
		p._nsURI = _pcomp.getCodeGenerator()._namespaceURIs.get(
			XPreCompiler.NS_XDEF_INDEX);
		p._nsindex = XPreCompiler.NS_XDEF_INDEX;
		p._localName = "text";
		p._value = _actPNode._value;
		_actPNode._value = null;
		_level++;
		_actPNode._childNodes.add(p);
		_level--;
	}

	private void processText() {
		if (_actPNode._template && _level > 0
			&& _actPNode._nsindex != XPreCompiler.NS_XDEF_INDEX) {
			genTextNode();
			return;
		}
		XScriptParser xparser = new XScriptParser(_actPNode._xmlVersion);
		xparser.setLineInfoFlag(true);
		xparser.setReportWriter(getReportWriter());
		xparser.setSourceBuffer(_actPNode._value);
		if (xparser.nextSymbol() == XScriptParser.NOCHAR) {
			_actPNode._value = null; // remove this text
			return;
		}
		if (_actPNode._nsindex == XPreCompiler.NS_XDEF_INDEX) {
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

	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param fileName pathname of file with with X-definitions.
	 */
	public final void parseFile(final String fileName) {
		parseFile(new File(fileName));
	}

	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param file The file with with X-definitions.
	 */
	public final void parseFile(final File file) {
		try {
			URL url = file.toURI().toURL();
			for (Object o: _pcomp.getSources()) {
				if (o instanceof URL && url.equals(o)) {
					return; //found in list
				}
			}
			_pcomp.getSources().add(url);
			parseStream(new FileInputStream(file), url.toExternalForm());
		} catch (RuntimeException ex) {
			throw ex;
		} catch (IOException ex) {
			//Can't read X-definition from the file &{0}
			throw new SRuntimeException(XDEF.XDEF902,
				(file == null ? (String) null : file.getAbsolutePath()));
		}
	}

	/** Parse InputStream source X-definition and addAttr it to the set
	 * of definitions.
	 * @param in input stream with the X-definition.
	 * @param srcName name of source data used in reporting (SysId) or
	 * <tt>null</tt>.
	 */
	public final void parseStream(final InputStream in, final String srcName) {
		if (_pcomp.getSources().contains(in)) {
			return;
		}
		_pcomp.getSources().add(in);
		_sysId = srcName;
		_level = -1;
		_actPNode = null;
		try {
			doParse(in, srcName);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			_actPNode = null; //just let gc to do the job
			if (_pcomp.getDispalyMode() > XDPool.DISPLAY_FALSE) {
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

	/** Parse data with source X-definition given by URL and addAttr it
	 * to the set of X-definitions.
	 * @param url URL of the file with the X-definition.
	 */
	public final void parseURL(final URL url) {
		if (url == null) {
			//Can't read X-definition from the file &{0}
			getReportWriter().error(XDEF.XDEF902, "null");
			return;
		}
		for (Object o: _pcomp.getSources()) {
			if (o instanceof URL && url.equals((URL) o)) {
				return; //prevents to doParse the source twice.
			}
		}
		String srcName = url.toExternalForm();
		_pcomp.getSources().add(srcName);
		try {
			parseStream(url.openStream(), srcName);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			//Can't read X-definition from the file &{0}
			throw new SRuntimeException(XDEF.XDEF902, srcName);
		}
	}

	/** Check if the name of X-definition is OK.
	 * @param name name of X-definition
	 * @return true if the name of X-definition is OK.
	 */
	private static boolean chkDefName(final String name, byte xmlVersion) {
		if (name.length() == 0) {
			return true; //nameless is also name
		}
		if (StringParser.getXmlCharType(name.charAt(0),  xmlVersion) !=
			StringParser.XML_CHAR_NAME_START) {
			return false;
		}
		char c;
		boolean wasColon = false;
		for (int i = 1; i < name.length(); i++) {
			if (StringParser.getXmlCharType(c = name.charAt(i),  xmlVersion) !=
				StringParser.XML_CHAR_NAME_START && (c  < '0' && c > '9')) {
				if (!wasColon && c == ':') { // we allow one colon inside name
					wasColon = true;
					if (i + 1 < name.length()
						&& StringParser.getXmlCharType(
							name.charAt(++i), xmlVersion)
						!= StringParser.XML_CHAR_NAME_START){//must follow name
						continue;
					}
				}
				return false;
			}
		}
		return true;
	}

	/** Parse string and addAttr it to the set of definitions.
	 * @param source The source string with definitions.
	 */
	public final void parseString(final String source) {
		if (_pcomp.getSources().indexOf(source) >= 0 || source.length() == 0) {
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

	/** Parse string and addAttr it to the set of X-definitions.
	 * @param source source string with X-definitions.
	 * @param srcName pathname of source (URL or an identifying name or null).
	 */
	public final void parseString(final String source, final String srcName) {
		if (_pcomp.getSources().indexOf(source) >= 0
			|| source.length() == 0) {
			return;  //we ignore already declared or empty strings
		}
		char c;
		if ((c = source.charAt(0)) <= ' ' || c == '<') {
			_pcomp.getSources().add(source);
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

}