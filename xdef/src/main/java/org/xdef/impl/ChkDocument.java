package org.xdef.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.XDDebug;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XX_DOCUMENT;
import static org.xdef.XDValueID.X_PARSEITEM;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.XXDOCUMENT;
import org.xdef.XDXmlOutStream;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import static org.xdef.impl.XConstants.JSON_ANYOBJECT;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefXmlWriter;
import org.xdef.impl.code.ParseItem;
import org.xdef.impl.xml.KNamespace;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.proc.XDLexicon;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SError;
import org.xdef.sys.SManager;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import static org.xdef.xon.XonNames.Q_ARRAY;
import static org.xdef.xon.XonNames.Q_MAP;
import static org.xdef.xon.XonNames.Q_VALUE;
import org.xdef.xon.XonUtils;

/** Provides root check object for generation of check tree and processing of the X-definition.
 * @author Vaclav Trojan
 */
final class ChkDocument extends ChkNode	implements XDDocument {
	////////////////////////////////////////////////////////////////////////////
	// Options
	////////////////////////////////////////////////////////////////////////////
	/** Flag for attributes "white spaces". */
	byte _attrWhiteSpaces; //0 not set, 'T' or 'F'
	/** Flag "ignore empty strings". */
	byte _ignoreEmptyAttributes; //0 not set, 'T', 'A', 'P', 'F'
	/** Flag set case of attribute values to upper(T) or lower(F). */
	byte _setAttrValuesCase; //0 not set, 'I' ignore, 'T' or 'F'
	/** Flag to trim/not trim attribute value. */
	byte _trimAttr; //0 not set, 'T' or 'F'
	/** Flag to trim/not trim text values. */
	byte _trimText; //0 not set 'T' or 'F'
	/** Flag set case of text node values to upper(T) or lower(F). */
	private byte _setTextValuesCase; //0 not set, 'I' ignore, 'T' or 'F'
	////////////////////////////////////////////////////////////////////////////
	/** Root definition. */
	XDefinition _xdef;
	/** Report generator. */
	SReporter _reporter;
	/** Actual w3c.dom.Document. */
	Document _doc;
	/** Root check element. */
	ChkElement _chkRoot;
	/** XDLexicon source language ID.*/
	int _sourceLanguageID = -1;
	/** XDLexicon destination language ID.*/
	int _destLanguageID = -1;
	/** XON object, result of XON/JSON parsing */
	Object _xon;
	////////////////////////////////////////////////////////////////////////////
	/** The list of child check elements. */
	private final List<ChkElement> _chkChildNodes;
	/** Reference number - max. 1 for root. */
	private int _refNum;
	/** true if we are running in create mode. */
	private boolean _createMode; //= false;
	/** Class of XComponent. */
	private Class<?> _xclass;
	/** XComponent if exists or null. */
	private XComponent _xComponent;
	/** Switch to generate XComponent instead of Element; */
	private boolean _genXComponent;
	////////////////////////////////////////////////////////////////////////////
	// valid date parameters
	/** Maximal accepted value of the year. */
	private int _maxYear = Integer.MIN_VALUE;
	/** Minimal accepted value of the year. */
	private int _minYear = Integer.MIN_VALUE;
	/** List of dates to be accepted out of interval _minYear.._maxYear. */
	private SDatetime _specialDates[];
	/** Switch to check if date is legal. */
	private boolean _stopCheckDateLegal;

	/** Create new instance of ChkDocument with ArrayReporter.
	 * @param xd XDefinition.
	 */
	private ChkDocument() {super("$root", null); _chkChildNodes = new ArrayList<>();}

	/** Create new instance of ChkDocument with ArrayReporter.
	 * @param xd XDefinition.
	 */
	public ChkDocument(final XDefinition xd) {
		this();
		SReporter reporter = new SReporter();
		setDateRestrictions(xd.getXDPool());
		init(xd, null, reporter, null, null);
		_scp = new XCodeProcessor(xd, reporter, null, null);
	}

	/** Create new instance of ChkDocument with ArrayReporter {this constructor is designed to parse source
	 * data containing reference to X-definition by attribute xdi:location).
	 * @param extObjects array of objects used to create DefPool.
	 * @param props Properties used to create DefPool.
	 */
	ChkDocument(final Properties props) {
		this();
		XBuilder xb = new XBuilder(props);
		xb.setSource("<xd:collection xmlns:xd='" + XDConstants.XDEF42_NS_URI+"'/>");
		XPool xp = (XPool) xb.compileXD();
		XDefinition xd = new XDefinition("#", xp, XDConstants.XDEF42_NS_URI, null, XConstants.XD41);
		xp._xdefs.put("#", xd);
		setDateRestrictions(xd.getXDPool());
		//create dummy X-definition - will be assigned from attribute
		init(xd, null, new SReporter(), props, null);
		_scp = new XCodeProcessor(xd, new SReporter(), null, null);
	}

	/** Create new instance of ChkDocument (this constructor is called only internally from ChkComposer).
	 * @param xd XDefinition.
	 * @param chkel ChkElement from which the object is created.
	 */
	ChkDocument(final XDefinition xd, final ChkElement chkel) {
		this();
		setDateRestrictions(xd.getXDPool());
		init(xd,
			chkel._rootChkDocument._doc,
			chkel._rootChkDocument._reporter,
			chkel._scp.getProperties(),
			chkel.getUserObject());
		_xComponent = chkel.getXComponent();
		_scp = new XCodeProcessor(xd, chkel);
	}

	/** Set date restrictions from XDPool.
	 * @param xd XCDPool.
	 */
	private void setDateRestrictions(final XDPool xp) {
		_minYear = xp.getMinYear();
		_maxYear = xp.getMaxYear();
		_specialDates = xp.getSpecialDates();
	}

	/** Initialize object. */
	final void init(final XDefinition xd,
		final Document doc,
		final SReporter reporter,
		final Properties props,
		final Object userObj) {
		setXPos("");
		if ((_xdef = xd) == null) {
			throw new SRuntimeException(XDEF.XDEF602); //The X-definition&{0}{ '}{'} is missing
		}
		_doc = doc == null ? KXmlUtils.newDocument() : doc;
		_reporter = reporter;
		_userObject = userObj;
		_setAttrValuesCase = _xdef._attrValuesCase;
		_setTextValuesCase = _xdef._textValuesCase;
		_trimText = _xdef._trimText;
		if (_scp != null) {
			_scp.setProperties(props!=null ? props : SManager.getProperties());
		}
	}

	/** Select root element.
	 * @param name name of element.
	 * @param namespaceURI namespace URI or null.
	 * @param languageID actual lexicon language or null.
	 * @return X-element or null if not found.
	 */
	private XElement selectRoot(final String name, final String namespaceURI, final int languageID) {
		String nm = name;
		XDLexicon t = languageID >= 0 ? ((XPool) getXDPool())._lexicon : null;
		if (namespaceURI != null && !namespaceURI.isEmpty()) { // has NS URI
			int i = name.indexOf(':');
			nm = name.substring(i + 1);
			QName qn = new QName(namespaceURI, nm);
			for (String xName: _xdef._rootSelection.keySet()) {
				XElement xe = (XElement) _xdef._rootSelection.get(xName);
				if (xe._xon > 0) {
					QName qxe = xe.getQName();
					if (qn.equals(qxe)) {
						return xe;
					}
					if (!Q_ARRAY.equals(qxe) && !Q_MAP.equals(qxe)
						&& !Q_VALUE.equals(qxe)) {
						for (XNode x: xe._childNodes) { // find in childNodes
							if (qn.equals(x.getQName())) {
								return (XElement) x;
							}
						}
					}
					continue;
				}
				i = xName.indexOf(':');
				if (i >= 0) {
					xName = xName.substring(i + 1); // XElement local name
				}
				if (t != null) {
					String s = t.findText(xName, languageID);
					if (s != null) {
						xName = s;
					}
				}
				if (nm.equals(xName) && namespaceURI.equals(xe.getNSUri())) {
					return (XElement) xe;
				}
			}
		} else if (t != null) { // not NS URI, lexicon
			for (XNode xe: _xdef._rootSelection.values()) {
				// get translated name
				String newName = t.findText(xe.getXDPosition(), languageID);
				if (nm.equals(newName) && xe.getNSUri() == null) {
					return (XElement) xe;
				}
			}
		} else {  // not NS URI, not lexicon
			for (XNode xe: _xdef._rootSelection.values()) {
				if (xe.getKind() == XMELEMENT && nm.equals(xe.getName()) && xe.getNSUri() == null) {
					return (XElement) xe;
				}
			}
		}
		// not found, now try model renerences of xd:choice
		for (XNode x: _xdef._rootSelection.values()) {
			if (x.getName().endsWith("$choice")) {
				for (XNode xe: ((XElement)x)._childNodes) {
					if (xe.getKind() == XMELEMENT && nm.equals(xe.getName()) && xe.getNSUri() == null) {
						XElement xel = (XElement) xe;
						if (_element != null && xel._match >= 0) {
							ChkElement chkEl = new ChkElement(this, _element, xel, false);
							chkEl.setXXType((byte) 'E');
							chkEl.setElemValue(_element);
							if (_scp.exec(xel._match, chkEl).booleanValue()) {
								return xel;
							}
						} else {
							return xel;
						}
					}
				}
				// try to find xd:any
				for (XNode xe: ((XElement)x)._childNodes) {
					if (xe.getKind() == XMELEMENT && "$any".equals(xe.getName())){
						XElement xel = (XElement) xe;
						if (_element != null && xel._match >= 0) {
							ChkElement chkEl = new ChkElement(this, _element, xel, false);
							chkEl.setXXType((byte) 'E');
							chkEl.setElemValue(_element);
							if (_scp.exec(xel._match, chkEl).booleanValue()) {
								return xel;
							}
						} else {
							return xel;
						}
					}
				}
			}
		}
		// not found, now try model renerence to an xd:any
		for (String xName: _xdef._rootSelection.keySet()) {
			XNode xe = _xdef._rootSelection.get(xName);
			if (xe == null) {
				int i = xName.indexOf(':');
				if (i >= 0) {
					xName = xName.substring(i + 1); // XElement local name
				}
			} else {
				String lockey = xe.getName();
				if (lockey.endsWith("$any") && lockey.length() > 4) {
					// reference of the named any
					return ((XElement) xe)._childNodes.length == 0
						? null : (XElement) ((XElement) xe)._childNodes[0];
				}
			}
		}
		return (XElement) _xdef._rootSelection.get("*"); // not found, try if there is "*"
	}

	/** Returns available element model represented by given name or null if definition item is not available.
	 * @param key name of definition item used for search.
	 * @param nsURI namespace URI.
	 * @param languageID actual lexicon language or null.
	 * @return required XElement or null.
	 */
	public final XElement getXElement(final String key, final String nsURI, final int languageID) {
		String lockey;
		XDefinition def;
		XDLexicon t = languageID >= 0 ? ((XPool) getXDPool())._lexicon : null;
		int ndx = key.lastIndexOf('#');
		if (ndx < 0) { //reference to this set, element with the name from key.
			lockey = key;
			def = _xdef;
		} else {
			def=(XDefinition) getXDPool().getXMDefinition(key.substring(0,ndx));
			if (def == null) {
				return null;
			}
			lockey = key.substring(ndx + 1);
		}
		if (nsURI == null || nsURI.isEmpty()) {
			for (XMElement xel: def.getModels()) {
				if (xel.getNSUri() == null && key.equals(xel.getName())) {
					return (XElement) xel;
				}
			}
			if (t != null) { // lexicon
				for (XMElement xel: def.getModels()) {
					String lname = t.findText(xel.getXDPosition(),languageID);
					if (xel.getNSUri() == null && lockey.equals(lname)) {
						return (XElement) xel;
					}
				}
			}
		} else {
			ndx = lockey.indexOf(':');
			lockey = ndx >= 0 ? lockey.substring(ndx + 1) : lockey;
			for (XMElement xel: _xdef.getModels()) {
				if (nsURI.equals(xel.getNSUri()) && lockey.equals(xel.getLocalName())) {
					return (XElement) xel;
				}
			}
			if (t != null) { // lexicon
				for (XMElement xel: _xdef.getModels()) {
					String lname = t.findText(xel.getXDPosition(),languageID);
					ndx = lname.indexOf(':');
					if (ndx >= 0) {
						lname = lname.substring(ndx + 1);
					}
					if (nsURI.equals(xel.getNSUri()) && lockey.equals(lname)){
						return (XElement) xel;
					}
				}
			}
		}
		return null;
	}

	/** Set create mode.
	 * @param createMode true if create mode is running.
	 */
	final void setCreateMode(boolean createMode) {_createMode = createMode;}

	/** Create instance of XComponent object from the class given by name.
	 * @param className name of class to create the XComponent object.
	 * @return true if XComponent object was created.
	 */
	private boolean createXComponent(final String className) {
		try {
			return createXComponent(
				Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	/** Create instance of XComponent object from the class.
	 * @param y the class from which to create the XComponent object.
	 * @return true if XComponent object was created.
	 */
	private boolean createXComponent(final Class<?> y) {
		try {
			Constructor<?> c = y.getDeclaredConstructor(XComponent.class, XXNode.class);
			c.setAccessible(true);
			XComponent xc = (XComponent) c.newInstance((XComponent)null, _chkRoot);
			_xclass = y;
			_chkRoot.setXComponent(_xComponent=xc);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException
			| NoSuchMethodException | SecurityException | InvocationTargetException ex) {
			_xComponent = null;
			return false;
		}
	}

	/** Create root check element for given name.
	 * @param element element
	 * @param checkRoot if true then the root check element is checked against the root list otherwise
	 * it is found the XElement model.
	 * @return The ChkElement object.
	 */
	final ChkElement createRootChkElement(final Element element, final boolean checkRoot) {
		String uri = element.getNamespaceURI();
		String name = element.getNodeName();
		_element = element;
		if (_xElement == null) {
			int languageId = isCreateMode() ? _destLanguageID:_sourceLanguageID;
			_xElement = checkRoot ? selectRoot(name, uri, languageId) : getXElement(name, uri, languageId);
		}
		boolean ignore;
		if (_xElement == null) {
			ignore = true;
			_xElement = _xdef.createAnyDefElement();
			_chkRoot = new ChkElement(this, _element, _xElement, ignore);
			String s = uri!=null && !uri.isEmpty()? " (xmlns=\""+uri+"\")" : "";
			_xPos = "/" + element.getNodeName();
			if (_xdef._onIllegalRoot >= 0) {
				//Element &{0} is not defined as root&{1}{ in X-definition }
				putTemporaryReport(Report.error(XDEF.XDEF502, element.getNodeName() + s, _xdef.getName()));
				_chkRoot.setXXType((byte) 'D');
				_scp.exec(_xdef._onIllegalRoot, _chkRoot);
				copyTemporaryReports();
			} else {
				debugXPos(XDDebug.ONILLEGALROOT);
				//Element &{0} is not defined as root&{1}{ in X-definition }
				error(XDEF.XDEF502, element.getNodeName() + s, _xdef.getName());
				_chkRoot.setElemValue(null);
				return _chkRoot;
			}
			if (_reporter.errors()) {
				return _chkRoot;
			}
		} else {
			 ignore = _xElement.isIgnore();
			_chkRoot = new ChkElement(this, _element, _xElement, ignore);
		}
		if (_xElement._match >= 0 && !isCreateMode()) {
			 //Execute "match" action on element
			_chkRoot.setElemValue(element);
			_chkRoot.setXXType((byte) 'E');
			XDValue result = _scp.exec(_xElement._match, _chkRoot);
			copyTemporaryReports();
			if (result == null || !result.booleanValue()) {
				if (_xdef._onIllegalRoot >= 0) {
					putTemporaryReport(Report.error(XDEF.XDEF503)); //Element doesn't fit to match condition
					_chkRoot.setXXType((byte) 'D');
					_scp.exec(_xdef._onIllegalRoot, _chkRoot);
					copyTemporaryReports();
					_chkRoot.setElemValue(null);
					if (_chkRoot.getElemValue() == null) {
						return _chkRoot;
					}
				} else {
					error(XDEF.XDEF503); //Element doesn't fit to match condition
					_chkRoot.setElemValue(null);
					return _chkRoot;
				}
			}
			_chkRoot.setElemValue(null);
		}
		if (_xdef != null && _xdef._init >= 0) { //init
			_chkRoot.setElemValue(element);
			_chkRoot.setXXType((byte) 'D');
			_scp.exec(_xdef._init, _chkRoot);
			copyTemporaryReports();
			_chkRoot.setElemValue(null);
		} else {
			if (_xdef != null) {
				debugXPos(XDDebug.INIT);
			}
		}
		_chkChildNodes.add(_chkRoot);
		if (_xElement.isIgnore()) {
			_element = null;
			_doc = null;
		} else if (_genXComponent) {
			_xComponent = null;
			_genXComponent = false;
			XElement xe = _xElement;
			String s = xe.getXDPosition();
			Map<String, String> components = getXDPool().getXComponents();
			int ndx = s.indexOf('$');
			if (ndx > 0) {
				s = s.substring(0, ndx);
			}
			String className = _xclass == null ? components.get(s) : _xclass.getName();
			while (className == null) {
				ndx = s.indexOf('/');
				if (ndx > 0) { // separate root model name
					String t = s.substring(0, ndx);
					className = components.get(t);
					if (className == null) { // extract namespace prefix
						ndx = s.indexOf('#') + 1;
						int ndx1 = t.indexOf(':');
						t = t.substring(0, ndx) + (ndx1 > 0 ? s.substring(ndx1 + 1) : "");
						className = components.get(t);
						if (className == null) {
							for (String key : components.keySet()) {
								if (key.startsWith(t)) {
									className = components.get(key);
									break;
								}
							}
						}
					}
					if (className != null) {
						break;
					}
				}
				if (!xe.isReference()) {
					break;
				}
				s = xe.getReferencePos();
				if ((ndx = s.indexOf('!')) > 0) {
					s = s.substring(0, ndx);
				}
				className = getXDPool().getXComponents().get(s);
				xe = (XElement) getXDPool().findModel(s);
			}
			if (className != null) {
				if (className.startsWith("%ref ")) {
					className = className.substring(5);
				}
				ndx = className.indexOf(' ');
				if (ndx > 0) { // remove extension and interfaces!
					className = className.substring(0, ndx).trim();
				}
				try {
					if (_xclass == null) {
						_xclass = Class.forName(className,
							false, Thread.currentThread().getContextClassLoader());
					}
					if (xe.getXDPosition().contains("#" + JSON_ANYOBJECT + "/$choice")) {
						String y = xe.getName();
						y = y.substring(y.indexOf(':') + 1);
						for (Class<?> x: _xclass.getDeclaredClasses()) {
							// jx$item jx$array jx$map
							String z = x.getName();
							if (y.equals(z.substring(x.getName().lastIndexOf('$') + 1))) {
								_xclass = x;
								break;
							} else {
								for (Class<?> xx: x.getDeclaredClasses()) {
									z = xx.getName();
									if (y.equals(z.substring(xx.getName().lastIndexOf('$') + 1))) {
										_xclass = xx;
										break;
									}
								}
							}
						}
					}
					if (!createXComponent(_xclass)) {
						String x =
							XComponentUtil.xmlToJavaName(_chkRoot.getChkElement().getXMElement().getName());
						if (!createXComponent(className + '$' + x)) {
							Class<?>[] classes = _xclass.getDeclaredClasses();
							for (Class<?> y : classes) {
								if (createXComponent(y)) {
									break;
								}
							}
						}
					}
					if (_xComponent == null) {
						//Error in Java XComponent class for element&{0} :&{1}
						throw new SRuntimeException(XDEF.XDEF506, element.getNodeName(), className);
					}
				} catch (ClassNotFoundException | SecurityException | SRuntimeException ex) {
					Throwable cause = ex.getCause();
					if (cause != null && cause instanceof SRuntimeException) {
						throw (SRuntimeException) cause;
					}
					//Error in Java XComponent class for element &{0}: &{1}
					throw new SRuntimeException(XDEF.XDEF506, ex, element.getNodeName(), className);
				}
			} else {
				//Java XComponent class for element &{0} is not defined for root &{1}{ in X-definition }
				throw new SRuntimeException(XDEF.XDEF505, _element.getNodeName(), _xdef.getName());
			}
		}
		_chkRoot.initElem();
		return _chkRoot;
	}

	final void endDocument() {
		if (_xdef != null) {
			if (_xdef._finaly >= 0) {
				_chkRoot.setXXType((byte) 'D');
				_scp.exec(_xdef._finaly, _chkRoot);
			} else {
				debugXPos(XDDebug.FINALLY);
			}
		}
		if (_scp.getXmlStreamWriter() != null) {
			_scp.getXmlStreamWriter().flushStream();
		}
		// Check unresolved IdRefs (print errors) and clear memory.
		_scp.endXDProcessing(); //TODO???
		copyTemporaryReports();
	}

	private Element chkAndGetRootElement(final SReporter reporter, boolean noreporter) {
		if (noreporter) {
			if (XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE.equals(
				SManager.getProperty(getProperties(), XDConstants.XDPROPERTY_WARNINGS))) {
				reporter.checkAndThrowErrorWarnings();
			} else {
				reporter.checkAndThrowErrors();
			}
		}
		return _element;
	}

	/** Find text in lexicon.
	 * @param key in lexicon.
	 * @return text from lexicon.
	 */
	final String findInLexicon(final String key) {
		XPool xp = (XPool) getXDPool();
		return _sourceLanguageID<0 || xp._lexicon==null ? null : xp._lexicon.findText(key, _sourceLanguageID);
	}

////////////////////////////////////////////////////////////////////////////////
// implementation of XDDocument
////////////////////////////////////////////////////////////////////////////////

	/** Get create mode/process mode.
	 * @return true if and only if create mode is running.
	 */
	@Override
	public final boolean isCreateMode() {return _createMode;}

	/** Create root check element with given name.
	 * @param nsURI NameSpace URI of the element.
	 * @param qn Qualified name of the element (with prefix).
	 * @param chkRoot if true, the root check element is checked against the root list, otherwise it is found
	 * as XElement on the base level.
	 * @return ChkElement object.
	 */
	@Override
	public final XXElement prepareRootXXElementNS(final String nsURI, final String qn, final boolean chkRoot){
		String uri = nsURI == null || nsURI.isEmpty() ? null : nsURI;
		try {
			Element root = _doc.createElementNS(uri, qn);
			if (uri != null) {
				String s = root.getPrefix();
				root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
					(s != null && !s.isEmpty()) ? "xmlns:"+s : "xmlns", nsURI);
			}
			return createRootChkElement(root, chkRoot);
		} catch (DOMException ex) {
			throw new SRuntimeException(XDEF.XDEF103, ex); //XDEF103 = Can'create root element
		}
	}

	/** Create root check element with given name.
	 * @param name Tag name of the root element (with prefix).
	 * @param chkRoot if true, the root check element is checked against the root list, otherwise it is found
	 * as XElement on the base level.
	 * @return ChkElement object.
	 */
	@Override
	public final XXElement prepareRootXXElement(final String name, final boolean chkRoot) {
		return prepareRootXXElementNS(null, name, chkRoot);
	}

	/** Get type ID of this item (i.e. XX_DOCUMENT),
	 * @return type ID of this item (i.e. XX_DOCUMENT),
	 */
	@Override
	public final short getItemId() {return XX_DOCUMENT;}

	/** Get XDValueType of this item (i.e. XXDOCUMENT),
	 * @return XDValueType of this item (i.e. XXDOCUMENT),
	 */
	@Override
	public final XDValueType getItemType() {return XXDOCUMENT;}

	/** Set properties. */
	@Override
	public final void setProperties(final Properties props) {_scp.setProperties(props);}

	/** Set property. If properties are null the new Properties object will be created.
	 * @param key name of property.
	 * @param value value of property or null. If the value is null the property is removed from properties.
	 */
	@Override
	public final void setProperty(final String key, final String value) {
		String newKey = key.startsWith("xdef.") ? key.replace('.','_'): key;
		if (!newKey.equals(key)) {
			_scp.setProperty(key, null); // remove old version of property
		}
		_scp.setProperty(newKey, value);
	}

	/** Get properties.
	 * @return assigned Properties.
	 */
	@Override
	public final Properties getProperties() {return _scp.getProperties();}

	/** Set root model for this document model.
	 * @param xmel model to be set.
	 */
	@Override
	public final void setRootModel(final XMElement xmel) { _xElement = (XElement) xmel; }

	/** Get the URL, file or string from the argument.
	 * @param s the string to be checked.
	 * @return the source of data,
	 */
	private Object getSource(final String s) {
		try {
			return SUtils.getExtendedURL(s);
		} catch (MalformedURLException ex) {
			File f = new File(s);
			return (f.exists()) ? f : s;
		}
	}

	/** Create parser for XON/JSON objects.
	 * @param x Object from which parser will be created.
	 * @param reporter reporter for error messages.
	 * @param sysId system id of error messages.
	 * @return created XPaorser.
	 */
	private XParser createXonParser(final Object x, final ReportWriter reporter, final String sysId) {
		XonSourceParser result;
		if (x instanceof String) {
			Object y = getSource((String) x);
			if (y == x) { // string
				result = new XonSourceParser(new StringReader((String) x), sysId == null ? "STRING" : sysId);
			} else {
				result = new XonSourceParser(y);
			}
		} else if (x instanceof File) {
			result = new XonSourceParser((File) x);
		} else if (x instanceof URL) {
			result = new XonSourceParser((URL) x);
		} else if (x instanceof Reader) {
			result = new XonSourceParser((Reader) x, sysId);
		} else if (x instanceof InputStream) {
			result = new XonSourceParser((InputStream) x, sysId);
		} else {// XON object: Map, List, simplevalue...
			result = new XonSourceParser(x);
		}
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(reporter));
		_refNum = 0; // we must clear counter!
		result.setReporter(reporter);
		return result;
	}

	/** Find model with QName in rootSelection.
	 * @param qname QName of model.
	 * @return XElement of the model.
	 */
	final XElement findXElement(final QName qname) {
		for (XNode x: _xdef._rootSelection.values()) {
			if (x.getKind() == XMELEMENT) {
				XElement xe = (XElement) x;
				if (xe.getXonMode() != 0) {
					if (qname.equals(xe.getQName())) {
						return xe;
					}
				} else if (xe._childNodes.length > 0 && xe._childNodes[0].getKind() == XMCHOICE) {
					// XMChoice is root of XON/JSON model of XML
					XChoice xch = (XChoice) xe._childNodes[0];
					for (int i = xch.getBegIndex()+1;
						i<xch.getEndIndex(); i++){
						XNode y = xe._childNodes[i];
						if (y.getKind() == XMELEMENT) {
							if (y.getQName().equals(qname)) {
								return (XElement) y;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/** Decrease reference counter by one.
	 * @return The increased reference number.
	 */
	@Override
	final int decRefNum() {return --_refNum;}

	/** Increase reference counter by one.
	 * @return The increased reference number.
	 */
	@Override
	final int incRefNum() {return ++_refNum;}

	/** Get reference counter of actual definition
	 * @return The reference number.
	 */
	@Override
	public final int getRefNum() {return _refNum;}

	/** Get occurrence of this node
	 * @return The reference number.
	 */
	@Override
	public final int getOccurrence() {return 1;}

	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	@Override
	final ChkElement getChkElement() {return _chkRoot;}

	/** Get Element value assigned to this node.
	 * @return Element value assigned to this node.
	 */
	@Override
	public final Element getElemValue() {return _chkRoot.getElemValue();}

	/** Assign Element value to this node.
	 * @param elem Element value to be assigned to this node.
	 */
	@Override
	final void setElemValue(final Element elem) {_chkRoot.setElemValue(elem);}

	/** Set output stream writer.
	 * @param out output stream.
	 * @param encoding encoding of output.
	 * @param wrHeader if true full document is written, otherwise only root element.
	 * @throws IOException if an error occurs.
	 */
	@Override
	public final void setStreamWriter(final OutputStream out, final String encoding, final boolean wrHeader)
		throws IOException {
		_scp.setXmlStreamWriter( new DefXmlWriter(out,encoding,wrHeader));
	}

	/** Set XML writer.
	 * @param out stream writer.
	 * @param encoding encoding of output.
	 * @param wrHeader if true full document is written, otherwise only root element.
	 */
	@Override
	public final void setStreamWriter(final Writer out, final String encoding, final boolean wrHeader) {
		_scp.setXmlStreamWriter(new DefXmlWriter(out,encoding,wrHeader));
	}

	/** Set XML writer.
	 * @param xmlWriter XML writer.
	 */
	@Override
	public final void setStreamWriter(final XDXmlOutStream xmlWriter) {_scp.setXmlStreamWriter(xmlWriter);}

	/** Get namespace context - for document it contains just XD prefix.
	 * @return namespace context of the parent element.
	 */
	@Override
	public final KNamespace getXXNamespaceContext() {return new KNamespace();}

	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	@Override
	public final XXElement getRootXXElement(){return _rootChkDocument._chkRoot;}

	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	@Override
	public final XXElement getXXElement() {return _rootChkDocument._chkRoot;}

	/** Get associated XML node.
	 * @return the associated XML node.
	 */
	@Override
	public final Node getXMLNode() {return _doc;}

	/** Get implementation properties of X-definition.
	 * @return the implementation properties of X-definition.
	 */
	@Override
	public final Properties getImplProperties() {return _xdef._properties;}

	/** Get implementation property of X-definition.
	 * @param name name of property.
	 * @return value implementation property of X-definition.
	 */
	@Override
	public final String getImplProperty(final String name) {return _xdef._properties.getProperty(name);}

	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	@Override
	public final XXNode[] getChildXXNodes() {
		XXNode[] result = new XXNode[_chkChildNodes.size()];
		_chkChildNodes.toArray(result);
		return result;
	}

	/** Get list with models of child nodes.
	 * @return list with models of child nodes..
	 */
	@Override
	final List<ChkElement> getChkChildNodes() {return _chkChildNodes;}

	/** Set debugging mode.
	 * @param debug debugging mode.
	 */
	@Override
	public final void setDebug(final boolean debug) {_scp.setDebug(debug);}

	/** Check debugging mode is set ON.
	 * @return value of debugging mode.
	 */
	@Override
	public final boolean isDebug() {return _scp.isDebugMode();}

	/** Get name of actual node.
	 * @return The name of node.
	 */
	@Override
	public final String getNodeName() {return "#document";}

	/** Get namespace URI of actual node.
	 * @return namespace URI or null.
	 */
	@Override
	public final String getNodeURI() {return null;}
	/** Get text value of this node.
	 * @return The name of node.
	 */
	public final String getTextValue() {return null;}

	/** Set text value to this node.
	 * @param text the text value to be set.
	 */
	public final void setTextValue(final String text) {
		throw new SRuntimeException(SYS.SYS083, "setTextValue"); //Illegal use of method: &{0}
	}

	/** set debugger.
	 * @param debugger the debugger.
	 */
	@Override
	public final void setDebugger(final XDDebug debugger) {_scp.setDebugger(debugger);}

	/** Get debugger.
	 * @return the debugger.
	 */
	@Override
	public final XDDebug getDebugger() {return _scp.getDebugger();}

	/** Get actual model.
	 * @return actual model.
	 */
	@Override
	public final XMNode getXMNode() {return _xdef;}

	/** Get XComponent.
	 * @return XComponent object (may be null).
	 */
	@Override
	public final XComponent getXComponent() {return _xComponent;}

	/** Set XComponent.
	 * @param x XComponent object.
	 */
	@Override
	public final void setXComponent(final XComponent x) {_xComponent = x;}

	/** Check value of datetime. Check if the year of date in the interval (YEAR_MIN .. YEAR_MAX) or the value
	 * of date is one of UNDEF_YEAR[] values.
	 * @param date value to be checked.
	 * @return true if date is legal.
	 */
	@Override
	public final boolean isLegalDate(final SDatetime date) {
		int i;
		if (_stopCheckDateLegal || date == null || (i = date.getYear()) == Integer.MIN_VALUE
			|| ((_minYear == Integer.MIN_VALUE || i >= _minYear)
			&& (_maxYear == Integer.MIN_VALUE || i <= _maxYear))) {
			return true; // not illlegal value or year not defined
		}
		if (_specialDates != null) { // check special dates
			for (SDatetime x: _specialDates) {
				if (x.equals(date) && x.getHour() == date.getHour() && x.getMinute() == date.getMinute()
					&& x.getSecond() == date.getSecond() && x.getFraction() == date.getFraction()) {
					return true; // time have both specified or not specified
				}
			}
		}
		return false;
	}

	/** Get minimum valid year of date.
	 * @return minimum valid year (Integer.MIN if not set).
	 */
	@Override
	public final int getMinYear() {return _minYear;}

	/** Set minimum valid year of date (or Integer.MIN is not set).
	 * @param x minimum valid year.
	 */
	@Override
	public final void setMinYear(final int x) {_minYear = x;}

	/** Get maximum valid year of date (or Integer.MIN if not set).
	 * @return maximum valid year (Integer.MIN if not set).
	 */
	@Override
	public final int getMaxYear()  {return _maxYear;}

	/** Set maximum valid year of date (or Integer.MIN is not set).
	 * @param x maximum valid year.
	 */
	@Override
	public final void setMaxYear(final int x) {_maxYear = x;}

	/** Get array of dates to be accepted out of interval minYear..maxYear.
	 * @return array with special values of valid dates.
	 */
	@Override
	public final SDatetime[] getSpecialDates() {return _specialDates;}

	/** Set array of dates to be accepted out of interval minYear..maxYear.
	 * @param x array with special values of valid dates.
	 */
	@Override
	public final void setSpecialDates(final SDatetime[] x) {_specialDates = x;}

	/** Set if year of date will be checked for interval minYear..maxYear.
	 * @param x if true year of date will be checked.
	 */
	@Override
	public final void checkDateLegal(final boolean x){_stopCheckDateLegal = !x;}

	/** Print reports to PrintStream.
	 * @param out PrintStream where reports are printed.
	 */
	@Override
	public final void printReports(final java.io.PrintStream out) {
		getReportWriter().getReportReader().printReports(out);
	}

	/** Get actual source language used for lexicon.
	 * @return string with actual language or return null if lexicon is not specified
	 * or if language is not specified.
	 */
	@Override
	public final String getLexiconLanguage() {
		return _sourceLanguageID<0 ? null : ((XPool) getXDPool())._lexicon.getLanguages()[_sourceLanguageID];
	}

	/** Set actual source language used for lexicon.
	 * @param language string with language or null.
	 * @throws SRuntimeException if lexicon is not specified or if language is not specified.
	 */
	@Override
	public final void setLexiconLanguage(final String language) {
		XPool xp = (XPool) getXDPool();
		if (xp._lexicon == null) {
			//Can't set language &{0} because lexicon is not declared
			throw new SRuntimeException(XDEF.XDEF141, language);
		}
		try {
			_sourceLanguageID = language == null ? -1 : xp._lexicon.getLanguageID(language);
		} catch (Exception ex) {
			//Can't set language &{0} because this language is not specified in lexicon
			throw new SRuntimeException(XDEF.XDEF142, language);
		}
	}

	/** Store  model variable.
	 * @param name name of variable.
	 * @return loaded value.
	 */
	@Override
	final XDValue loadModelVariable(final String name) {
		throw new SRuntimeException(SYS.SYS066, "Unknown 'model' variable " + name);//Internal error&{0}{: }
	}

	/** Store model variable.
	 * @param name name of variable.
	 * @param val value to be stored.
	 */
	@Override
	final void storeModelVariable(final String name, final XDValue val) {
		throw new SRuntimeException(SYS.SYS066, "Unknown variable " + name);//Internal error&{0}{: }
	}

	/** Translate the input element from the source language to the destination language according to lexicon.
	 * @param elem the element in the source language.
	 * @param sourceLanguage name of source language.
	 * @param destLanguage name of destination language.
	 * @param reporter the reporter where to write errors or null.
	 * @return element converted to the destination language.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public final Element xtranslate(final Element elem,
		final String sourceLanguage,
		final String destLanguage,
		final ReportWriter reporter) throws SRuntimeException {
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
		_refNum = 0; // we must clear counter!
		ChkTranslate chTranlsate = new ChkTranslate(reporter == null ? new ArrayReporter() : reporter);
		chTranlsate.xtranslate(this, elem, sourceLanguage, destLanguage);
		return chkAndGetRootElement(chTranlsate, reporter == null);
	}

	/** Translate the input element from the source language to the destination language according to lexicon.
	 * @param elem path to the source element or the string with element.
	 * @param sourceLanguage name of source language.
	 * @param destLanguage name of destination language.
	 * @param reporter the reporter where to write errors or null.
	 * @return element converted to the destination language.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public final Element xtranslate(final String elem,
		final String sourceLanguage,
		final String destLanguage,
		final ReportWriter reporter) throws SRuntimeException {
		return xtranslate(KXmlUtils.parseXml(elem).getDocumentElement(),sourceLanguage,destLanguage,reporter);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Get actual destination language used for lexicon.
	 * @return string with actual language.
	 */
	public final String getDestLexiconLanguage() {
		return (_destLanguageID < 0) ? null : ((XPool) getXDPool())._lexicon.getLanguages()[_destLanguageID];
	}


	/** Set actual destination language used for lexicon.
	 * @param language string with language or null.
	 * @throws SRuntimeException if lexicon is not specified or if language is not specified.
	 */
	public final void setDestLexiconLanguage(final String language) {
		XPool xp = (XPool) getXDPool();
		if (xp._lexicon == null) {
			//Can't set language of output &{0} because lexicon is not declared
			throw new SRuntimeException(XDEF.XDEF141, language);
		}
		_destLanguageID = language == null ? -1 : xp._lexicon.getLanguageID(language);
	}

	/** Parse a string with a type declared in X-definition.
	 * @param typeName name of type in X-definition.
	 * @param data string with data to be parsed.
	 * @return XDParseResult object with parsed data.
	 */
	@Override
	public final XDParseResult parseXDType(final String typeName, final String data) {
		_scp.initscript();
		XDValue xv = getVariable(typeName);
		if (xv == null) {
			throw new SRuntimeException("Typ " + typeName + " not found");
		}
		int addr;
		switch (xv.getItemId()) {
			case X_PARSEITEM: addr = ((ParseItem) xv).getParseMethodAddr(); break;
			case X_UNIQUESET_M:
				ParseItem keyItem = ((CodeUniqueset) xv).getParseKeyItem(typeName);
				addr = keyItem == null ? -1 : keyItem.getParseMethodAddr();
				break;
			default: addr = -1;
		}
		if (addr < 0) {
			throw new SRuntimeException("Name " + typeName + " is not parser");
		}
		XElement xel = new XElement("parseXDType", null, _xdef);
		ChkElement chkel = new ChkElement(this, null, xel, true);
		chkel.setXXType((byte) 'T');
		chkel.setTextValue(data);
		XDValue x = _scp.exec(addr, chkel);
		if (x.getItemId() == XD_PARSERESULT) {
			return (XDParseResult) x;
		}
		DefParseResult result = new DefParseResult(data);
		if (x.booleanValue()) { // OK
			result.setParsedValue(data);
		} else { // error
			result.addReports(chkel.getTemporaryReporter());
			if (!result.errors()) {
				result.putDefaultParseError(); //Incorrect value&amp;{0}{: }
			}
		}
		return result;
	}

	/** Get XON result.
	 * @return XON  result.
	 */
	@Override
	public final Object getXon() {
		return _xon != null ? _xon : _element != null ? XonUtils.xmlToXon(_element) : null;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse and process CSV data and return processed object. If separator is comma header is not skipped.
	 * @param source reader with CSV data
	 * @param sourceId name of source or null.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	@Override
	public final List<Object> cparse(final Reader source, final String sourceId, final ReportWriter reporter){
		return cparse(source, ',', false, sourceId, reporter);
	}

	/** Parse and process CSV data and return processed object.
	 * @param source reader with CSV data
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @param sourceId name of source or null.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return List with processed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final List<Object> cparse(final Reader source,
		final char separator,
		final boolean skipHeader,
		final String sourceId,
		ReportWriter reporter) {
		return (List<Object>) jvalidate(XonUtils.parseCSV(source,separator,skipHeader,sourceId),reporter);
	}

	/** Parse and process INI/Properties data and return processed object.
	 * @param source INI/Properties data or file pathname
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return Map with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Map<String, Object> iparse(final String source, final ReportWriter reporter)
		throws SRuntimeException {
		return ivalidate(XonUtils.iniToXml(source), reporter);
	}

	/** Parse and process INI/Properties data and return processed object.
	 * @param data File with INI/Properties data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return Map with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Map<String, Object> iparse(final File data, final ReportWriter reporter)
		throws SRuntimeException {
		return ivalidate(XonUtils.iniToXml(data), reporter);
	}

	/** Parse and process INI/Properties data and return processed object.
	 * @param source URL pointing to INI/Properties data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return Map with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Map<String, Object> iparse(final URL source, final ReportWriter reporter)
		throws SRuntimeException {
		return ivalidate(XonUtils.iniToXml(source), reporter);
	}

	/** Parse and process INI/Properties data and return processed object.
	 * @param source InputStream with INI/Properties data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return Map with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Map<String, Object> iparse(final InputStream source, final ReportWriter reporter)
		throws SRuntimeException {
		return ivalidate(XonUtils.iniToXml(source), reporter);
	}

	/** Parse source INI/Properties and return XComponent as result.
	 * @param source string with pathname of XON/JSON source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	@Override
	public final XComponent iparseXComponent(final Object source,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		return iparseXComponent(source, xClass, null, reporter);
	}

	/** Parse source INI/Properties and return XComponent as result.
	 * @param source string with pathname of INI/Properties file or INI/Properties source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error is reported.
	 */
	@Override
	public final XComponent iparseXComponent(final Object source,
		final Class<?> xClass,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		Class<?> yClass  = xClass;
		if (source == null || source instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) source;
			if (yClass == null) {
				for (String s: getXDPool().getXComponents().keySet()) {
					String className = getXDPool().getXComponents().get(s);
					try {
						yClass = Class.forName(className);
						String jmodel = (String) yClass.getDeclaredField("XD_NAME").get(null);
						byte jVersion = (Byte) yClass.getDeclaredField("XON").get(null);
						if (jVersion > 0) {
							XElement xe = selectRoot(jmodel, XDConstants.XON_NS_URI_W, -1);
							if (xe != null && xe._xon != 0) {
								break;
							}
						}
					} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
						| NoSuchFieldException | SecurityException ex) {}
					yClass = null;
				}
			}
			return xparseXComponent(XonUtils.iniToXml(map), yClass, reporter);
		} else if (source instanceof String) {
			return iparseXComponent(XonUtils.parseINI((String) source), yClass, reporter);
		} else if (source instanceof File) {
			return iparseXComponent(XonUtils.parseINI((File) source), yClass,reporter);
		} else if (source instanceof URL) {
			return iparseXComponent(XonUtils.parseINI((URL) source), yClass,reporter);
		} else if (source instanceof InputStream) {
			return iparseXComponent((InputStream)source, yClass,sourceId,reporter);
		} else if (source instanceof Document) {
			return iparseXComponent(XonUtils.xmlToXon(((Document) source).getDocumentElement()),
				yClass, reporter);
		} else if (source instanceof Element) {
			return iparseXComponent(XonUtils.xmlToXon((Element) source), yClass, reporter);
		}
		//Unsupported type of argument &{0}: &{1}
		throw new SRuntimeException(SYS.SYS037,"source",source.getClass());
	}

	/** Validate and process INI/Properties data and return processed XON.
	 * @param source INI/Properties object or XML representation of object to validate.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final Map<String, Object> ivalidate(final Object source, final ReportWriter reporter)
		throws SRuntimeException {
		if (source instanceof Map || source instanceof String) {
			_reporter = new SReporter(reporter);
			_scp.setStdErr(new DefOutStream(reporter));
			_refNum = 0; // we must clear counter!
			new XonSourceParser(source).xparse(this);
			return (Map<String, Object>) getXon();
		} else if (source instanceof File || source instanceof URL || source instanceof InputStream) {
			createXonParser(source, reporter, null).xparse(this);
			return (Map<String, Object>) getXon();
		}
		Element e;
		if (source instanceof Element) {
			e = (Element) source;
		} else if (source instanceof Document) {
			e = ((Document)source).getDocumentElement();
		} else {
			//Unsupported type of argument &{0}: &{1}
			throw new SRuntimeException(SYS.SYS037,"source", source.getClass());
		}
		QName qName = KXmlUtils.getQName(e);
		if ((_xElement = findXElement(qName)) != null) {
			xparse(e, reporter);
			return (Map<String, Object>) (_xon=_chkRoot.getXon());//prepare XON;
		}
		//Text with &{0} model&{1}{ of "}{" } is missing in X-definition
		throw new SRuntimeException(XDEF.XDEF315, "json", e.getNodeName());
	}

	/** Find in current X-definition the model to be created.
	 * @param name name of XON/JSON/CSV model.
	 * @param typ name of type of model ("JSON, INI, CSV etc").
	 * @return the XElemnt with model or throw SRuntimeException
	 * @throws SRuntimeException model wan not found.
	 */
	private XElement findXonModel(final String name, final String typ) throws SRuntimeException {
		for (XMElement x : _scp.getXDefinition().getModels()) {
			if (name.equals(x.getName())) {
				XMNode[] models = x.getChildNodeModels();
				if (models != null && models.length == 1 && ((XElement) x)._xon > 0) {
					return (XElement) models[0];
				}
				break;
			}
		}
		//Text with &{0} model&{1}{ of "}{" } is missing in X-definition
		throw new SRuntimeException(XDEF.XDEF315, typ, name);
	}

	/** Run create XON/JSON according to the X-definition XON/JSON model.
	 * @param name name of XON/JSON model.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return Object with the constructed XON/JSON data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final Object jcreate(final String name, final ReportWriter reporter) throws SRuntimeException {
		_xElement = findXonModel(name, "JSON");
		xcreate(_xElement.getQName(), reporter);
		return (_xon = _chkRoot.getXon()); //prepare XON
	}

	/** Create XComponent from XON/JSON according to the X-definition model.
	 * NOTE this method is experimental.
	 * @param name the name of required model.
	 * @param xClass XComponent class (if null, then XComponent class
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public XComponent jcreateXComponent(final String name, final Class xClass, final ReportWriter reporter)
		throws SRuntimeException {
		_xElement = findXonModel(name, "JSON");
		_genXComponent = true;
		_xclass = xClass;
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
		_refNum = 0; // we must clear counter!
		new ChkComposer(reporter == null ? new ArrayReporter() : reporter)
			.xcreate(this, _xElement.getNSUri(), _xElement.getName());
		return getParsedComponent();
	}

	/** Parse and process XON/JSON data and return processed XON object.
	 * @param source XON/JSON data or pathname
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object jparse(final String source, final ReportWriter reporter) throws SRuntimeException {
		xparse(new ChkXONParser(reporter, source), reporter);
		return getXon();
	}

	/** Parse and process XON/JSON data and return processed XON object.
	 * @param data File with XON/JSON data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object jparse(final File data, final ReportWriter reporter) throws SRuntimeException {
		createXonParser(data, reporter, null).xparse(this);
		return getXon();
	}

	/** Parse and process XON/JSON data and return processed XON object.
	 * @param source URL pointing to XON/JSON data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object jparse(final URL source, final ReportWriter reporter) throws SRuntimeException {
		xparse(new ChkXONParser(reporter, source), reporter);
		return getXon();
	}

	/** Parse and process XON/JSON data and return processed XON object.
	 * @param source InputStream with XON/JSON data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object jparse(final InputStream source, final ReportWriter reporter)throws SRuntimeException{
		xparse(new ChkXONParser(reporter, source, null), reporter);
		return getXon();
	}

	/** Parse source XON/JSON and return XComponent as result.
	 * @param source string with pathname of XON/JSON source data, file name, URL, InputStream, Reader,
	 * or JSON object.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent jparseXComponent(final Object source,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		return jparseXComponent(source, xClass, null, reporter);
	}

	/** Parse URL with XON/JSON source and return XComponent as result.
	 * @param source InputStream with XON/JSON source data.
	 * @param sourceId name of source or null.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent jparseXComponent(final Object source,
		final Class<?> xClass,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		if (source instanceof File || source instanceof InputStream || source instanceof Reader
			|| source instanceof URL) {
			_genXComponent = true;
			_xclass = xClass;
			xparse(new ChkXONParser(reporter, source, sourceId), reporter);
			return getParsedComponent();
		}
		if (source instanceof String) {
			return jparseXComponent(new StringReader((String) source), xClass, sourceId, reporter);
		}
		if (source == null || source instanceof Map || source instanceof List
			|| source instanceof Number || source instanceof Boolean) {
			return jparseXComponent(XonUtils.xonToXmlW(source), xClass, sourceId, reporter);
		}
		if (source instanceof Document) {
			return xparseXComponent(((Document) source).getDocumentElement(), xClass, reporter);
		}
		if (source instanceof Element) {
			return xparseXComponent((Element) source, xClass, reporter);
		}
		//Unsupported type of argument &{0}: &{1}
		throw new SRuntimeException(SYS.SYS037,"source",source.getClass());
	}

	/** Parse and process XON/JSON data and return processed XON object.
	 * @param source XON/JSON object, or either File, URL, InputStream. or XML node wit XON/JSON data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object jvalidate(final Object source, final ReportWriter reporter) throws SRuntimeException {
		if (source instanceof String) {
			String s = (String) source;
			File f = new File(s);
			Object x = f;
			if (!f.exists()) {
				try {
					URL u = new URL(s);
					u.openConnection();
					x = u;
				} catch (IOException ex) {
					x = new StringReader(s);
				}
			}
			createXonParser(x, reporter, null).xparse(this);
			return getXon();
		}
		if (source == null || source instanceof Map || source instanceof List || source instanceof Number
			|| source instanceof Boolean || source instanceof SDatetime || source instanceof SDuration
			|| source instanceof GPSPosition || source instanceof Price) {
			_reporter = new SReporter(reporter);
			_scp.setStdErr(new DefOutStream(reporter));
			_refNum = 0; // we must clear counter!
			new XonSourceParser(source).xparse(this);
			return getXon();
		} else if (source instanceof File || source instanceof URL || source instanceof InputStream
			|| source instanceof Reader) {
			createXonParser(source, reporter, null).xparse(this);
			return getXon();
		}
		Element e = null;
		if (source instanceof Document) {
			e = ((Document) source).getDocumentElement();
		} else if (source instanceof Element) {
			e = (Element) source;
		} else {
			throw new SRuntimeException(XDEF.XDEF318); //Incorrect XON/JSON data
		}
		QName qName = KXmlUtils.getQName(e);
		if ((_xElement = findXElement(qName)) != null) {
			xparse(e, reporter);
			return (_xon = _chkRoot.getXon());//prepare XON
		}
		//Text with &{0} model&{1}{ of "}{" } is missing in X-definition
		throw new SRuntimeException(XDEF.XDEF315, "json", e.getNodeName());
	}

////////////////////////////////////////////////////////////////////////////////

	/** Run create mode - create element according to the X-definition model. If the parameter nsUri is not
	 * null then its assigned the model with given namespaceURI; in this case the parameter qname may be
	 * qualified with a prefix.
	 * @param nsUri namespace URI of result element (may be null).
	 * @param name name of model of required element (may contain prefix).
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final Element xcreate(final String nsUri, final String name, final ReportWriter reporter)
		throws SRuntimeException {
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
		_refNum = 0; // we must clear counter!
		ChkComposer chkp = new ChkComposer(reporter == null ? new ArrayReporter() : reporter);
		chkp.xcreate(this, nsUri, name);
		Element result = chkAndGetRootElement(chkp, reporter == null);
		KXmlUtils.removeRedundantXmlnsAttrs(result);
		return result;
	}

	/** Run create mode - create element according to the X-definition model.
	 * @param qname the QName of model of required element.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final Element xcreate(final QName qname, final ReportWriter reporter) throws SRuntimeException {
		String s = qname.getPrefix();
		s = (s != null && !s.isEmpty() ? s + ':' : "") + qname.getLocalPart();
		return xcreate(qname.getNamespaceURI(), s, reporter);
	}

	/** Run create mode - create element according to the X-definition model.
	 * @param name the name of model of required element.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final Element xcreate(final String name, final ReportWriter reporter) throws SRuntimeException {
		for (XMElement x : _scp.getXDefinition().getModels()) {
			if (name.equals(x.getName())) {
				return xcreate(x.getQName(), reporter); // model found
			}
		}
		return xcreate(null, name, reporter);
	}

	/** Run create mode - create element according to the X-definition model. If the parameter nsUri is not
	 * null then its assigned the model with given namespaceURI; in this case the parameter qname may be
	 * qualified with a prefix.
	 * @param nsUri the namespace URI of result element (may be null).
	 * @param name the name of model of required element (may contain prefix).
	 * @param xClass XComponent class (if null, then XComponent class
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent xcreateXComponent(final String nsUri,
		final String name,
		Class xClass,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
		_refNum = 0; // we must clear counter!
		new ChkComposer(reporter == null ? new ArrayReporter() : reporter)
			.xcreate(this, nsUri, name);
		return getParsedComponent();
	}

	/** Run create mode - create XComponent according to the X-definition model.
	 * @param qname the QName of model of required element.
	 * @param cls XComponent class (if null, then XComponent class
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent xcreateXComponent(final QName qname, final Class cls, final ReportWriter reporter)
		throws SRuntimeException {
		String s = qname.getPrefix();
		s = (s != null && !s.isEmpty() ? s + ':' : "") + qname.getLocalPart();
		return xcreateXComponent(qname.getNamespaceURI(), s, cls, reporter);
	}

	/** Run create mode - create XComponent according to the X-definition model.
	 * @param name the name of model of required element.
	 * @param xClass XComponent class (if null, then XComponent class
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with created data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent xcreateXComponent(final String name,
		final Class xClass,
		final ReportWriter reporter) throws SRuntimeException {
		return xcreateXComponent(null, name, xClass, reporter);
	}

	/** Parse and process XML source element.
	 * @param pasrser XParser object.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	private Element xparse(final XParser parser, final ReportWriter reporter) {
		Element result;
		try {
			_reporter = parser.getReporter();
			_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
			_refNum = 0; // we must clear counter!
			parser.xparse(this);
			_xElement = null;
			result = chkAndGetRootElement(parser.getReporter(), reporter==null);
			parser.closeReader();
			_xon = _chkRoot.getXon();//prepare XON
			return result;
		} catch (SRuntimeException ex) {
			XDDebug debugger = getDebugger();
			if (debugger != null) {
				if (ex instanceof SThrowable) {
					if (!_reporter.errorWarnings()) {
						SThrowable st = (SThrowable) ex;
						debugger.closeDebugger(st.getReport().getLocalizedText());
					}
				} else {
					debugger.closeDebugger("Process finished with exception:\n" + ex);
				}
				if (parser != null) {
					parser.closeReader();
				}
				return null;
			}
			if (parser != null) {
				parser.closeReader();
			}
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new SRuntimeException(SYS.SYS036, STester.printThrowable(ex)); //Program exception&{0}{: }
		} catch (SError e) {
			Report rep = e.getReport();
			if (rep == null || !"XDEF906".equals(rep.getMsgID())) { //X-definition canceled
				throw e;
			}
			if (parser != null) {
				parser.closeReader();
			}
			fatal(rep.getMsgID(), rep.getText(), rep.getModification());
			return null;
		}
	}

	/** Parse and process XML source element.
	 * @param source string with pathname of XML file or XML source data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final Element xparse(final Object source, final ReportWriter reporter) throws SRuntimeException {
		if (source != null && source instanceof Node) {
			_reporter = new SReporter(reporter);
			_scp.setStdErr(new DefOutStream(reporter));
			Node node = (Node) source;
			Element el = node.getNodeType() == Node.ELEMENT_NODE
				? (Element) node : node.getNodeType() == Node.DOCUMENT_NODE
				? ((Document) node).getDocumentElement() : node.getOwnerDocument().getDocumentElement();
			ChkDOMParser parser = new ChkDOMParser(reporter == null ? new ArrayReporter() : reporter, el);
			_reporter = parser;
			_refNum = 0; // we must clear counter!
			parser.xparse(this);
			_xon = _chkRoot.getXon();//prepare XON
			return chkAndGetRootElement(parser, reporter == null);
		}
		return xparse(source, null, reporter);
	}

	/** Parse and process XML source element.
	 * @param source string with pathname of XML file or XML source data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @param sourceId name of source or null.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final Element xparse(final Object source, final String sourceId, final ReportWriter reporter)
		throws SRuntimeException {
		ChkParser parser =
			(source instanceof String) ? new ChkParser(reporter, (String) source)
			: (source instanceof File) ? new ChkParser(reporter, (File) source)
			: (source instanceof URL) ? new ChkParser(reporter, (URL) source)
			: (source instanceof InputStream) ? new ChkParser(reporter,(InputStream) source,sourceId) : null;
		if (parser == null) {
			// Input XML source is empty or doesn't exist.
			throw new SRuntimeException(Report.error(XDEF.XDEF578));
		}
		if (sourceId != null && !sourceId.isEmpty()) {
			parser._sysId = sourceId;
		}
		return xparse(parser, reporter);
	}

	/** Get XComponent from parsed document.
	 * @return XComponent from parsed document.
	 */
	private XComponent getParsedComponent() {
		_genXComponent = false;
		_element = null;
		_chkRoot.setElemValue(null);
		XComponent result = _xComponent;
		_xComponent = null;
		return result;
	}

	/** Parse source XML and return XCpomonent as result.
	 * @param src string with pathname of XML file or XML source data.
	 * @param cls XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param rep report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent xparseXComponent(final Object src, final Class<?> cls, final ReportWriter rep)
		throws SRuntimeException {
		_genXComponent = true;
		_xclass = cls;
		xparse(src, rep);
		return getParsedComponent();
	}

	/** Parse source XML and return XComponent as result.
	 * @param source input stream with XML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param sourceId name of source or null.
	 * @param reporter report writer or null. If this argument is  null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent xparseXComponent(final Object source,
		final Class<?> xClass,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		xparse(source, sourceId, reporter);
		return getParsedComponent();
	}

	/** Run create XAML according to the X-definition XON/JSON model.
	 * @param name name of XON/JSON model.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return Object with the constructed source data.
	 * @throws SRuntimeException if an error was reported.
	 */
	@Override
	public Object ycreate(String name, ReportWriter reporter) throws SRuntimeException {
		return XonUtils.xonToJson(jcreate(name, reporter));
	}

	/** Parse and process YAML data and return processed XON object.
	 * @param source YAML data or pathname
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object yparse(final String source, final ReportWriter reporter) throws SRuntimeException {
		Object o = getSource(source);
		return (o instanceof String) ? XonUtils.parseYAML((String) o)
			: (o instanceof URL) ? yparse(((URL) o), reporter)
			: (o instanceof File) ? yparse(((File) o), reporter)
			: jvalidate(XonUtils.parseYAML(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))),
				reporter);
	}

	/** Parse and process YAML data and return processed XON object.
	 * @param data File with YAML data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws RuntimeException if an was reported.
	 */
	@Override
	public final Object yparse(final File data, final ReportWriter reporter) throws SRuntimeException {
		try {
			return jvalidate(XonUtils.parseYAML(new FileInputStream(data)), reporter);
		} catch (FileNotFoundException ex) { throw new RuntimeException(ex); }
	}

	/** Parse and process YAML data and return processed XON object.
	 * @param source URL pointing to YAML data.
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object yparse(final URL source, final ReportWriter reporter) throws SRuntimeException {
		try {
			return jvalidate(XonUtils.parseYAML(source.openStream()), reporter);
		} catch (IOException ex) { throw new RuntimeException(ex); }
	}

	/** Parse and process YAML data and return processed XON object.
	 * @param source InputStream with YAML data.
	 * @param reporter report writer or null. If this argument is
	 * null and error reports occurs then SRuntimeException is thrown.
	 * @return XON object with processed data.
	 * @throws SRuntimeException if an was reported.
	 */
	@Override
	public final Object yparse(final InputStream source, final ReportWriter reporter)
		throws SRuntimeException {
		return jvalidate(XonUtils.parseYAML(source), reporter);
	}

	/** Parse source YAML and return XComponent as result.
	 * @param source string with pathname of YAML file or YAML source data.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return XComponent with parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent yparseXComponent(final Object source,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		return jparseXComponent(source, xClass, null, reporter);
	}

	/** Parse URL with YAML source and return XComponent as result.
	 * @param source InputStream with YAML source data.
	 * @param sourceId name of source or null.
	 * @param xClass XComponent class (if null, then XComponent class is searched in XDPool).
	 * @param reporter report writer or null. If this argument is null and error reports occurs then
	 * SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is null and an error was reported.
	 */
	@Override
	public final XComponent yparseXComponent(final Object source,
		final Class<?> xClass,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		Class<?> yClass  = xClass;
		if (source == null || source instanceof Map || source instanceof List || source instanceof Number
			|| source instanceof Boolean) {
			if (yClass == null) {
				for (String s: getXDPool().getXComponents().keySet()) {
					String className = getXDPool().getXComponents().get(s);
					try {
						yClass = Class.forName(className);
						String jmodel = (String) yClass.getDeclaredField("XD_NAME").get(null);
						byte jVersion = (Byte) yClass.getDeclaredField("XON").get(null);
						if (jVersion > 0) {
							XElement xe = selectRoot(jmodel, XDConstants.XON_NS_URI_W, -1);
							if (xe != null && xe._xon != 0) {
								break;
							}
						}
					} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
						| NoSuchFieldException | SecurityException ex) {}
					yClass = null;
				}
			}
			Element e;
			try {
				// version of XON/JSON to XML transormation
				byte xonVer = (Byte) yClass.getDeclaredField("XON").get(null);
				e = xonVer == XConstants.XON_MODE_W ? XonUtils.xonToXmlW(source) : XonUtils.xonToXml(source);
			} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException
				| SecurityException ex) {
				e = XonUtils.xonToXmlW(source); // X-definition transormation
			}
			return xparseXComponent(e, yClass, reporter);
		} else if (source instanceof String) {
			return jparseXComponent(XonUtils.parseXON((String) source), yClass, reporter);
		} else if (source instanceof File) {
			return jparseXComponent(XonUtils.parseXON((File) source), yClass,reporter);
		} else if (source instanceof InputStream) {
			return jparseXComponent((InputStream)source, yClass, sourceId, reporter);
		} else if (source instanceof Document) {
			return jparseXComponent(XonUtils.xmlToXon(((Document) source).getDocumentElement()),
				yClass, reporter);
		} else if (source instanceof Element) {
			return jparseXComponent(XonUtils.xmlToXon((Element) source),yClass,reporter);
		}
		//Unsupported type of argument &{0}: &{1}
		throw new SRuntimeException(SYS.SYS037, "source", source.getClass());
	}
	/** Get printable string from this object. */
	@Override
	public final String toString() {return "ChkDocument: " + _xElement;}

////////////////////////////////////////////////////////////////////////////////

	/** Parse source XML and return XComponent as result.
	 * @deprecated please use xparseXComponent instead
	 */
	@Override
	@SuppressWarnings("deprecation")
	public final XComponent parseXComponent(final Object data, final Class<?> cls, final ReportWriter rep)
		throws SRuntimeException {
		return xparseXComponent(data, cls, rep);
	}

	/** Parse source XML and return XComponent as result.
	 * @deprecated please use xparseXComponent instead
	 */
	@Override
	@SuppressWarnings("deprecation")
	public final XComponent parseXComponent(final Object xmlData,
		final Class<?> xClass,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		return xparseXComponent(xmlData, xClass, sourceId, reporter);
	}
}
