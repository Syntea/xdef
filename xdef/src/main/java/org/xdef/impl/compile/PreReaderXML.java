package org.xdef.impl.compile;

import org.xdef.XDConstants;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.msg.XML;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.StringParser;
import org.xdef.impl.xml.KParsedElement;
import org.xdef.xml.KXmlUtils;
import java.io.InputStream;
import java.net.URI;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.impl.XConstants;
import org.xdef.impl.xml.KParsedAttr;

/** Reads source X-definitions and prepares list of PNodes with X-definitions
 * from XML source data.
 * @author Trojan
 */
class PreReaderXML extends XmlDefReader implements PreReader {
	/** Actual node */
	private PNode _actPNode;
	/** includes. */
	private Element _includeElement;
	/** Nesting level of XML node. */
	private int _level;
	/** Instance of PreCompiler. */
	private final XPreCompiler _pcomp;

	/** Creates a new instance of XPreCompiler.
	 * @param pcomp XPreCompiler.
	 */
	PreReaderXML(final XPreCompiler pcomp) {super(); _pcomp = pcomp;}

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
				"1.1".equals(getXmlVersion())
					? XConstants.XML11 : XConstants.XML10);
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
			_actPNode._xpathPos = qName + "[1]";
			String uri = parsedElem.getParsedNSURI();
			if ("def".equals(elemLocalName)
				|| "lexicon".equals(elemLocalName)
				|| "thesaurus".equals(elemLocalName)//&&_actPNode._xdVersion==31
				|| "declaration".equals(elemLocalName)
				|| "component".equals(elemLocalName)
				|| "BNFGrammar".equals(elemLocalName)
				|| "collection".equals(elemLocalName))  {
				String projectNS; // = XDConstants.XDEF20_NS_URI;
				KParsedAttr ka;
				byte ver;
				if ((ka = parsedElem.getAttrNS(
					XDConstants.XDEF20_NS_URI, "metaNamespace")) != null
					|| (ka = parsedElem.getAttrNS(
						XDConstants.XDEF31_NS_URI, "metaNamespace")) != null
					|| (ka = parsedElem.getAttrNS(
						XDConstants.XDEF32_NS_URI, "metaNamespace")) != null
					|| (ka = parsedElem.getAttrNS(
						XDConstants.XDEF40_NS_URI, "metaNamespace")) != null) {
					projectNS = ka.getValue().trim();
					ver = XDConstants.XDEF20_NS_URI.equals(ka.getNamespaceURI())
						? XConstants.XD20
						: XDConstants.XDEF31_NS_URI.equals(ka.getNamespaceURI())
						? XConstants.XD31
						: XDConstants.XDEF32_NS_URI.equals(ka.getNamespaceURI())
						? XConstants.XD32 : XConstants.XD40;
					try {
						if (projectNS.isEmpty()) {
							throw new RuntimeException();
						}
						new URI(projectNS);
					} catch (Exception ex) {
						//Attribute 'metaNamespace' must contain a valid URI
						error(ka.getPosition(), XDEF.XDEF253);
					}
					parsedElem.remove(ka);
				} else {
					if (XDConstants.XDEF20_NS_URI.equals(uri)
						|| XDConstants.XDEF31_NS_URI.equals(uri)
						|| XDConstants.XDEF32_NS_URI.equals(uri)
						|| XDConstants.XDEF40_NS_URI.equals(uri)) {
						ver = XDConstants.XDEF20_NS_URI.equals(uri)
							? XConstants.XD20
							: XDConstants.XDEF31_NS_URI.equals(uri)
							? XConstants.XD31
							: XDConstants.XDEF32_NS_URI.equals(uri)
							? XConstants.XD32 : XConstants.XD40;
						projectNS = uri;
					} else {
						//Namespace of X-definitions is required
						error(_actPNode._name, XDEF.XDEF256);
						projectNS = XDConstants.XDEF40_NS_URI;
						ver = XConstants.XD40;
					}
				}
				if (_pcomp.isChkWarnings()&&"thesaurus".equals(elemLocalName)) {
					warning(_actPNode._name,XDEF.XDEF998,"thesaurus","lexicon");
				}
				_actPNode._xdVersion = ver;
				_pcomp.setURIOnIndex(0, projectNS);
			} else {
				_pcomp.setURIOnIndex(0, uri);
				//X-definition or X-collection expected
				error(_actPNode._name, XDEF.XDEF255);
			}
		} else {
			_actPNode._parent.addChildNode(_actPNode);
		}
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			String key = ka.getName();
			String value = ka.getValue();
			if (key.startsWith("xmlns")) { //addAttr namespace URI to the list.
				int nsndx = _pcomp.setNSURI(value.trim());
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
				_actPNode.setAttr(item);
				if ((ndx = key.indexOf(':')) >= 0) {
					String prefix = key.substring(0, ndx);
					item._localName = key.substring(ndx + 1);
					Integer nsndx = _actPNode._nsPrefixes.get(prefix);
					if (nsndx == null) {
						String u = ka.getNamespaceURI();
						if (u != null) {
							nsndx = _pcomp.setNSURI(u);
							_actPNode._nsPrefixes.put(prefix, nsndx);
						}
					}
					if (nsndx != null) {
						item._nsURI = _pcomp.getNSURI(nsndx);
						if ((item._nsindex=nsndx) == XPreCompiler.NS_XDEF_INDEX
							&& "script".equals(item._localName)) {
							XScriptParser xp =
								new XScriptParser(_actPNode._xmlVersion);
							xp.setSource(new SBuffer(value, ka.getPosition()),
								_actPNode._xdef == null ? null
									: _actPNode._xdef.getName(),
								null,_actPNode._xmlVersion, item._xpathPos);
							xp.skipBlanksAndComments();
							if (xp.isToken("template")) {
								xp.skipBlanksAndComments();
								if (!xp.eos() && !xp.isChar(';')) {
									xp.error(XDEF.XDEF425);//Script error
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
			}
		}
		Integer nsuriIndex = _actPNode._nsPrefixes.get(elemPrefix);
		if (nsuriIndex != null) {
			int urindx;
			if (((urindx = nsuriIndex) == XPreCompiler.NS_XINCLUDE_INDEX)) {
				String nsuri = _pcomp.getNSURI(urindx);
				Element el;
				if (_includeElement == null) {
					el = _includeElement = KXmlUtils.newDocument(nsuri,
						_actPNode._name.getString(), null).getDocumentElement();
				} else {
					el = _includeElement.getOwnerDocument().createElementNS(
						nsuri, _actPNode._name.getString());
					_includeElement.appendChild(el);
				}
				for (PAttr aval: _actPNode.getAttrs()) {
					if (aval._nsindex < 0) {
						el.setAttribute(aval._name, aval._value.getString());
					} else {
						el.setAttributeNS(_pcomp.getNSURI(aval._nsindex),
							aval._name,
							aval._value.getString());
					}
				}
				return;
			} else {
				_actPNode._nsindex = urindx;
			}
		}
		if (_level++ == -1) { // root level
			if ("collection".equals(elemLocalName)) {
				_pcomp.processIncludeList(_actPNode);
				_pcomp.reportNotAllowedAttrs(_actPNode);
				_pcomp.getPCollections().add(_actPNode);
				_level = -1; // collection must be -1!
			} else if ("BNFGrammar".equals(elemLocalName)) {
				 _pcomp.getPBNFs().add(_actPNode);
			} else if ("lexicon".equals(elemLocalName)
				|| ("thesaurus".equals(elemLocalName)
					&&_actPNode._xdVersion <= 31)) {
				_pcomp.getPLexiconList().add(_actPNode);
			} else if ("declaration".equals(elemLocalName)) {
				_pcomp.getPDeclarations().add(0, _actPNode);
			} else if ("component".equals(elemLocalName)) {
				_pcomp.getPComponents().add(0, _actPNode);
			} else { // def
				String defName = getNameAttr(_actPNode, false, true);
				if (defName == null) {
					defName = "";
				}
				// Because there is not yet connected an X-definition to
				// the PNode we create a dumy one in fact just to store
				// the X-definition name (we nead it to be able to compile
				// internal declarations, BNGGrammars, components and
				// lexicon items).
				_actPNode._xdef = new XDefinition(defName,
					null, _actPNode._nsURI, null, _actPNode._xmlVersion);
				_pcomp.processIncludeList(_actPNode);
				if (defName != null) {
					// check duplicate of X-definition
					for (PNode p: _pcomp.getPXDefs()) {
						if (defName.equals(p._xdef.getName())) {
							XScriptParser xp =
								new XScriptParser(_actPNode._xmlVersion);
							xp.setSource(_actPNode._name,
								_actPNode._xdef == null ? null
									: _actPNode._xdef.getName(),
								null,_actPNode._xmlVersion,_actPNode._xpathPos);
							if (defName.length() == 0) {
								//Only one X-definition in the compiled XDPool
								// may be without name
								xp.error(_actPNode._name, XDEF.XDEF212);
							} else {
								//X-definition '&{0}' already exists
								xp.error(_actPNode._name,XDEF.XDEF303, defName);
							}
							defName = null;
							break;
						}
					}
					if (defName != null) {
						_pcomp.getPXDefs().add(_actPNode);
					}
				}
			}
		}
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
		PAttr pa = _pcomp.getXdefAttr(pnode, "name", required, remove);
		if (pa == null) {
			// if required do not return null!
			return required ? ("_UNKNOWN_REQUIRED_NAME_") : null;
		}
		String name = pa._value.getString();
		if (name.length() == 0) {
			//Incorrect name
			error(pa._value, XDEF.XDEF258);
			return "__UNKNOWN_ATTRIBUTE_NAME_";
		}
		if (!PreReaderXML.chkDefName(name, pnode._xmlVersion)) {
			error(pa._value, XDEF.XDEF258); //Incorrect name
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
		p._nsURI = _pcomp.getNSURI(XPreCompiler.NS_XDEF_INDEX);
		p._nsindex = XPreCompiler.NS_XDEF_INDEX;
		p._localName = "text";
		p._value = _actPNode._value;
		_actPNode._value = null;
		_level++;
		_actPNode.addChildNode(p);
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
			if ("json".equals(_actPNode._localName)) {
				if (_level != 1) {
					//JSON model can be declared only as a child of X-definition
					error(_actPNode._value, XDEF.XDEF310);
					_actPNode._value = null;
				}
				return;
			} else if ("text".equals(_actPNode._localName)
				|| "BNFGrammar".equals(_actPNode._localName)
				|| "lexicon".equals(_actPNode._localName)
				|| "thesaurus".equals(_actPNode._localName)
//					&& _actPNode._xdVersion == 31
				|| "declaration".equals(_actPNode._localName)
				|| "component".equals(_actPNode._localName)
				|| "macro".equals(_actPNode._localName)) {
				return; //text is processed in the pnode
			} else if (!"mixed".equals(_actPNode._localName)
				&& !"choice".equals(_actPNode._localName)
				&& !"list".equals(_actPNode._localName)
//				&& !"PI".equals(_actPNode._localName) //TODO
//				&& !"comment".equals(_actPNode._localName) //TODO
//				&& !"document".equals(_actPNode._localName) //TODO
//				&& !"value".equals(_actPNode._localName) //TODO
//				&&!"attlist".equals(_actPNode._localName) //TODO
				&& !"sequence".equals(_actPNode._localName)
				&& !"any".equals(_actPNode._localName)) {
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
			setReportWriter(_pcomp.getReportWriter());
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
}