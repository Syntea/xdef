package org.xdef.impl;

import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefXmlWriter;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SError;
import org.xdef.sys.SManager;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.impl.xml.KNamespace;
import org.xdef.xml.KXmlUtils;
import org.xdef.component.XComponent;
import org.xdef.XDConstants;
import org.xdef.XDDebug;
import org.xdef.XDDocument;
import org.xdef.XDValue;
import org.xdef.XDXmlOutStream;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.ReportWriter;
import org.xdef.XDValueType;
import javax.xml.namespace.QName;
import org.xdef.json.JsonUtil;

/** Provides root check object for generation of check tree and processing
 * of the X-definition.
 * @author Vaclav Trojan
 */
//@SuppressWarnings("deprecation") // xcreate(String,String,ReportWriter)
final class ChkDocument extends ChkNode	implements XDDocument {
	////////////////////////////////////////////////////////////////////////////
	// Options
	////////////////////////////////////////////////////////////////////////////
	/** Flag "ignore comments". */
	byte _ignoreComments; //0 not set, 'T' or 'F'
	/** Flag for attributes "white spaces". */
	byte _attrWhiteSpaces; //0 not set, 'T' or 'F'
	/** Flag for text nodes "white spaces". */
	byte _textWhiteSpaces; //0 not set, 'T' or 'F'
	/** Flag "ignore empty strings". */
	byte _ignoreEmptyAttributes; //0 not set, 'T', 'A', 'P', 'F'
	/** Flag set case of attribute values to upper(T) or lower(F). */
	byte _setAttrValuesCase; //0 not set, 'I' ignore, 'T' or 'F'
	/** Flag set case of text node values to upper(T) or lower(F). */
	byte _setTextValuesCase; //0 not set, 'I' ignore, 'T' or 'F'
	/** Flag to trim/not trim attribute value. */
	byte _trimAttr; //0 not set, 'T' or 'F'
	/** Flag to trim/not trim text values. */
	byte _trimText; //0 not set 'T' or 'F'
	/** Flag to ignore entities resolving. */
	byte _resolveEntities;
	/** Flag to accept qualified attributes for elements with namespace URI. */
	byte _acceptQualifiedAttr; //0 not set 'T' or 'F'
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
	/** The list of child check elements. */
	final ArrayList<ChkElement> _chkChildNodes = new ArrayList<ChkElement>();

	// valid date parameters
	/** Maximal accepted value of the year. */
	private int _maxYear = Integer.MIN_VALUE;
	/** Minimal accepted value of the year. */
	private int _minYear = Integer.MIN_VALUE;
	/** List of dates to be accepted out of interval _minYear.._maxYear. */
	private SDatetime _specialDates[];
	/** Switch to check if date is legal. */
	private boolean _stopCheckDateLegal; // = false;

	/** Creates a new instance of ChkDocument with ArrayReporter.
	 * @param xd XDefinition.
	 */
	public ChkDocument(final XDefinition xd) {
		super("$root", null);
		SReporter reporter = new SReporter();
		init(xd, null, reporter, null, null);
		_scp = new XCodeProcessor(xd, reporter, null, null);
	}

	/** Creates a new instance of ChkDocument with ArrayReporter.
	 * Note this constructor is designed to parse source data containing
	 * reference to X-definition by attribute xdi:location.
	 * @param extObjects array of objects used to create DefPool.
	 * @param props Properties used to create DefPool.
	 */
	ChkDocument(final Class<?>[] extObjects, final Properties props) {
		super("$root", null);
		XPool xp = (XPool) new XBuilder(props).setExternals(extObjects)
			.setSource(
				"<xd:collection xmlns:xd='"+XDConstants.XDEF32_NS_URI+"'/>")
			.compileXD();
		XDefinition xd = new XDefinition("#",
			xp, XDConstants.XDEF32_NS_URI, null, XConstants.XD32);
		xp._xdefs.put("#", xd);
		//create dummy X-definition - will be assigned from attribute
		init(xd, null, new SReporter(), props, null);
		_scp = new XCodeProcessor(xd, new SReporter(), null, null);
	}

	/** Creates a new instance of ChkDocument. This constructor is called
	 * only internally from ChkComposer.
	 * @param xd XDefinition.
	 * @param chkel ChkElement from which the object is created.
	 */
	ChkDocument(final XDefinition xd, final ChkElement chkel) {
		super("#root", null);
		init(xd, chkel._rootChkDocument._doc,
			chkel._rootChkDocument._reporter,
			chkel._scp.getProperties(), //Properties props,
			chkel.getUserObject());
		_xComponent = chkel.getXComponent();
		_scp = new XCodeProcessor(xd, chkel);
	}

	/** Initialize object. */
	final void init(final XDefinition xd,
		final Document doc,
		final SReporter reporter,
		final Properties props,
		final Object userObj) {
//		_refNum = 0; _xElement = null; //Java makes it!
		setXPos("");
		if ((_xdef = xd) == null) {
			//The X-definition&{0}{ '}{'} is missing
			throw new SRuntimeException(XDEF.XDEF602);
		}
		_minYear = _xdef.getXDPool().getMinYear();
		_maxYear = _xdef.getXDPool().getMaxYear();
		_specialDates = _xdef.getXDPool().getSpecialDates();
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

	/** Set create mode.
	 * @param createMode true if create mode is running.
	 */
	final void setCreateMode(boolean createMode) {_createMode = createMode;}

	/** Create root check element for given name.
	 * @param element The element
	 * @param checkRoot if true then the root check element is checked against
	 * the root list otherwise it is found the XElement model.
	 * @return The ChkElement object.
	 */
	final ChkElement createRootChkElement(final Element element,
		final boolean checkRoot) {
		String name = element.getNodeName();
		String nsURI = element.getNamespaceURI();
		if (_xElement == null) {
			int languageId = isCreateMode() ?_destLanguageID:_sourceLanguageID;
			_xElement = checkRoot
				? _xdef.selectRoot(name, nsURI, languageId)
				: _xdef.getXElement(name, nsURI, languageId);
		}
		_element = element;
		boolean ignore;
		if (_xElement == null) {
			ignore = true;
			_xElement = _xdef.createAnyDefElement();
			_chkRoot = new ChkElement(this, _element, _xElement, ignore);
			String s= nsURI!=null&&nsURI.length()>0?" (xmlns=\""+nsURI+"\")":"";
			_xPos = "/" + element.getNodeName();
			if (_xdef._onIllegalRoot >= 0) {
				//Element &{0} is not defined as root&{1}{ in X-definition }
				putTemporaryReport(Report.error(XDEF.XDEF502,
					element.getNodeName() + s, _xdef.getName()));
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
					//Element doesn't fit to match condition", null
					putTemporaryReport(Report.error(XDEF.XDEF503));
					_chkRoot.setXXType((byte) 'D');
					_scp.exec(_xdef._onIllegalRoot, _chkRoot);
					copyTemporaryReports();
					_chkRoot.setElemValue(null);
					if (_chkRoot.getElemValue() == null) {
						return _chkRoot;
					}
				} else {
					//Element doesn't fit to match condition
					error(XDEF.XDEF503);
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
			String s;
			int ndx;
			XElement xe = _xElement;
			s = xe.getXDPosition();
			ndx = s.indexOf('$');
			if (ndx > 0) {
				s = s.substring(0, ndx);
			}
			String className = _xclass == null ?
				getXDPool().getXComponents().get(s)
				: _xclass.getName();
			while (className == null) {
				if (!xe.isReference()) {
					ndx = s.indexOf('/');
					if (ndx > 0) { // separate root model name
						s = s.substring(0, ndx);
						className = getXDPool().getXComponents().get(s);
						if (className == null) { // extract namespace prefix
							ndx = s.indexOf('#') + 1;
							int ndx1 = s.indexOf(':');
							s = s.substring(0, ndx) + s.substring(ndx1 + 1);
							className = getXDPool().getXComponents().get(s);
						}
					}
					break;
				}
				s = xe.getReferencePos();
				ndx = s.indexOf('!');
				if (ndx > 0) {
					s = s.substring(0, ndx);
				}
				xe = (XElement) getXDPool().findModel(s);
				className = getXDPool().getXComponents().get(s);
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
						_xclass = Class.forName(className, false,
							Thread.currentThread().getContextClassLoader());
					}
					Constructor<?> c = _xclass.getDeclaredConstructor(
						XComponent.class, XXNode.class);
					c.setAccessible(true);
					_xComponent = (XComponent) c.newInstance(
						(XComponent)null, _chkRoot);
					_chkRoot.setXComponent(_xComponent);
				} catch (Exception ex) {
					Throwable cause = ex.getCause();
					if (cause != null && cause instanceof SRuntimeException) {
						throw (SRuntimeException) cause;
					}
					//Error in Java XComponent class for element &{0}: &{1}
					throw new SRuntimeException(XDEF.XDEF506,
						ex, element.getNodeName(), className);
				}
			} else {
				//Java XComponent class for element &{0} is not defined
				// for root &{1}{ in X-definition }
				throw new SRuntimeException(XDEF.XDEF505,
					_element.getNodeName(), _xdef.getName());
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

	/** Parse and process XML source element.
	 * @param pasrser ChkParser object.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	private Element xparse(final ChkParser parser,
		final ReportWriter reporter) {
		Element result;
		try {
			_reporter = parser.getReporter();
			_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
			_refNum = 0; // we must clear counter!
			parser.xparse(this);
			_xElement = null;
			result = chkAndGetRootElement(parser.getReporter(), reporter==null);
			parser.closeReader();
			return result;
		} catch (Exception ex) {
			XDDebug debugger = getDebugger();
			if (debugger != null) {
				debugger.closeDebugger("Process finished with exception:\n"+ex);
			}
			if (parser != null) {
				parser.closeReader();
			}
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		} catch (SError e) {
			Report rep = e.getReport();
			 //X-definition canceled
			if (rep == null || !"XDEF906".equals(rep.getMsgID())) {
				throw e;
			}
			if (parser != null) {
				parser.closeReader();
			}
			error(rep.getMsgID(), rep.getText(), rep.getModification());
			return getElement();
		}
	}

	private XComponent getPparsedComponent() {
		_genXComponent = false;
		_element = null;
		_chkRoot.setElemValue(null);
		XComponent result = _xComponent;
		_xComponent = null;
		return result;
	}

	private Element chkAndGetRootElement(final SReporter reporter,
		boolean noreporter) throws SRuntimeException {
		if (noreporter) {
			if (XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE.equals(
				SManager.getProperty(getProperties(),
					XDConstants.XDPROPERTY_WARNINGS))) {
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
		return (_sourceLanguageID < 0 || xp._lexicon == null)
			? null : xp._lexicon.findText(key, _sourceLanguageID);
	}

////////////////////////////////////////////////////////////////////////////////
// implementation of XDDocument
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get create mode/process mode.
	 * @return true if and only if create mode is running.
	 */
	public final boolean isCreateMode() {return _createMode;}

	@Override
	/** Create root check element for given name.
	 * @param nsURI NameSpace URI of the element.
	 * @param qname Qualified name of the element (with prefix).
	 * @param checkRoot if true, the root check element is checked against
	 * the root list, otherwise it is found as XElement on the base level.
	 * @return The ChkElement object.
	 */
	public final XXElement prepareRootXXElementNS(final String nsURI,
		final String qname,
		final boolean checkRoot) {
		String uri = nsURI == null || nsURI.length() == 0 ? null : nsURI;
		try {
			Element root = _doc.createElementNS(uri, qname);
			if (uri != null) {
				String s = root.getPrefix();
				root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
					(s != null && s.length()>0) ? "xmlns:"+s : "xmlns", nsURI);
			}
			return createRootChkElement(root, checkRoot);
		} catch (Exception ex) {
			 //Can'create root element
			throw new SRuntimeException(XDEF.XDEF103, ex);
		}
	}

	@Override
	/** Create root check element for given name.
	 * @param name Tag name of the root element (with prefix).
	 * @param checkRoot if true, the root check element is checked against
	 * the root list, otherwise it is found as XElement on the base level.
	 * @return The ChkElement object.
	 */
	public final XXElement prepareRootXXElement(final String name,
		final boolean checkRoot) {
		return prepareRootXXElementNS(null, name, checkRoot);
	}

	@Override
	public final short getItemId() {return XX_DOCUMENT;}

	@Override
	public final XDValueType getItemType() {return XDValueType.XXDOCUMENT;}

	@Override
	/** Set properties.
	 * @param props Properties.
	 */
	public final void setProperties(final Properties props) {
		_scp.setProperties(props);
	}

	@Override
	/** Set property. If properties are null the new Properties object
	 * will be created.
	 * @param key name of property.
	 * @param value value of property or null. If the value is null the property
	 * is removed from properties.
	 */
	public final void setProperty(final String key, final String value) {
		String newKey = key.startsWith("xdef.") ? key.replace('.','_'): key;
		if (!newKey.equals(key)) {
			_scp.setProperty(key, null); // remve odl version of property
		}
		_scp.setProperty(newKey, value);
	}

	@Override
	/** Get properties.
	 * @return assigned Properties.
	 */
	public final Properties getProperties() {return _scp.getProperties();}

	@Override
	/** Set root model for this document model.
	 * @param xmel model to be set.
	 */
	public final void setRootModel(final XMElement xmel) {
		_xElement = (XElement) xmel;
	}

	@Override
	/** Parse source XML and return XCpomonent as result.
	 * @param xmlData string with pathname of XML file or XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent parseXComponent(final String xmlData,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		xparse(xmlData, reporter);
		return getPparsedComponent();
	}

	@Override
	/** Parse source XML and return XComponent as result.
	 * @param xmlData file with XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent parseXComponent(final File xmlData,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		xparse(xmlData, reporter);
		return getPparsedComponent();
	}

	@Override
	/** Parse source XML and return XComponent as result.
	 * @param xmlData URL pointing to XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent parseXComponent(final URL xmlData,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		xparse(xmlData, reporter);
		return getPparsedComponent();
	}

	@Override
	/** Parse source XML and return XComponent as result.
	 * @param xmlData input stream with XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent parseXComponent(final InputStream xmlData,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		return parseXComponent(xmlData, xClass, null, reporter);
	}

	@Override
	/** Parse source XML and return XComponent as result.
	 * @param xmlData input stream with XML source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param sourceId name of source or <tt>null</tt>.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent parseXComponent(final InputStream xmlData,
		final Class<?> xClass,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		xparse(xmlData, sourceId, reporter);
		return getPparsedComponent();
	}

	@Override
	/** Parse source XML and return XComponent as result.
	 * @param xmlData XML <tt>org.w3c.dom.Node</tt>.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent parseXComponent(final Node xmlData,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		_genXComponent = true;
		_xclass = xClass;
		if (xClass != null) {
			try {
				String s =(String) xClass.getDeclaredField("XD_NAME").get(null);
				XNode xn = _xdef._rootSelection.get(s);
				if (xn == null) {
					for (String key: _xdef._rootSelection.keySet()) {
						int ndx = key.indexOf(':');
						if (ndx > 0 && s.equals(key.substring(ndx + 1))) {
							xn = _xdef._rootSelection.get(key);
							break;
						}
					}
				}
				if (xn != null && xn.getKind() == XMNode.XMELEMENT) {
					XElement xe = (XElement) xn;
					if (xe._json != 0) {
						_xElement = xe;
						xparse(xmlData, reporter);
						return getPparsedComponent();
					}
				}
			} catch (Exception ex) {}
		}
		xparse(xmlData, reporter);
		return getPparsedComponent();
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData string with pathname of XML file or XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xparse(final String xmlData,
		final ReportWriter reporter) throws SRuntimeException {
		return xparse(xmlData, null, reporter);
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData string with pathname of XML file or XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @param sourceId name of source or <tt>null</tt>.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xparse(final String xmlData,
		final String sourceId,
		final ReportWriter reporter) throws SRuntimeException {
		if(xmlData == null || xmlData.length() == 0) {
			// Input XML source is empty or doesn't exist.
			throw new SRuntimeException(Report.error(XDEF.XDEF578));
		}
		ChkParser parser = new ChkParser(reporter, xmlData);
		if (sourceId != null) {
			parser._sysId = sourceId;
		}
		return xparse(parser, reporter);
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData file with XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xparse(final File xmlData,
		final ReportWriter reporter) throws SRuntimeException {
		if(xmlData == null || !xmlData.canRead()|| xmlData.length() == 0) {
			// Input XML source is empty or doesn't exist.
			throw new SRuntimeException(Report.error(XDEF.XDEF578));
		}
		return xparse(new ChkParser(reporter, xmlData), reporter);
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData URL pointing to XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xparse(final URL xmlData,
		final ReportWriter reporter) throws SRuntimeException {
		if(xmlData == null) {
			// Input XML source is empty or doesn't exist.
			throw new SRuntimeException(Report.error(XDEF.XDEF578));
		}
		return xparse(new ChkParser(reporter, xmlData), reporter);
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData input stream with XML source data.
	 * @param name name of source or <tt>null</tt>.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xparse(final InputStream xmlData,
		final String name,
		final ReportWriter reporter) throws SRuntimeException {
		if(xmlData == null) {
			// Input XML source is empty or doesn't exist.
			throw new SRuntimeException(Report.error(XDEF.XDEF578));
		}
		return xparse(new ChkParser(reporter, xmlData, name), reporter);
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData XML <tt>org.w3c.dom.Node</tt>.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xparse(final Node xmlData,
		final ReportWriter reporter) throws SRuntimeException {
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(reporter));
		Element el = xmlData.getNodeType() == Node.ELEMENT_NODE ?
			(Element) xmlData : xmlData.getNodeType() == Node.DOCUMENT_NODE ?
			((Document) xmlData).getDocumentElement() :
			xmlData.getOwnerDocument().getDocumentElement();
		ChkDOMParser parser = new ChkDOMParser(
			reporter == null ? new ArrayReporter() : reporter, el);
		_reporter = parser;
		_refNum = 0; // we must clear counter!
		parser.xparse(this);
		return chkAndGetRootElement(parser, reporter == null);
	}

	@Override
	/** Parse and process XML source element.
	 * @param xmlData input stream with XML source data.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public Element xparse(final InputStream xmlData,
		final ReportWriter reporter) throws SRuntimeException {
		return xparse(xmlData, null, reporter);
	}

	@Override
	/** Parse and process JSON source and return JSON object.
	 * @param jsonData string with pathname of JSON file or JSON source data.
	 * @param model qualified name of JSON root model.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return JSON object with processed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Object jparse(final String jsonData,
		final String model,
		final ReportWriter reporter) throws SRuntimeException {
		return jparse(JsonUtil.parse(jsonData), model, reporter);
	}

	@Override
	/** Parse and process JSON source and return JSON object.
	 * @param jsonData file with JSON source data.
	 * @param model qualified name of JSON root model.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return JSON object with processed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Object jparse(final File jsonData,
		final String model,
		final ReportWriter reporter) throws SRuntimeException {
		return jparse(JsonUtil.parse(jsonData), model, reporter);
	}

	@Override
	/** Parse and process JSON source and return JSON object.
	 * @param jsonData URL with JSON source data.
	 * @param model qualified name of JSON root model.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return JSON object with processed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Object jparse(final URL jsonData,
		final String model,
		final ReportWriter reporter) throws SRuntimeException {
		return jparse(JsonUtil.parse(jsonData), model, reporter);
	}

	@Override
	/** Parse and process JSON data and return processed JSON object.
	 * @param jsonData input stream with JSON data.
	 * @param sourceId name of source or <tt>null</tt>.
	 * @param model qualified name of JSON root model.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return JSON object with processed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Object jparse(final InputStream jsonData,
		final String sourceId,
		final String model,
		final ReportWriter reporter) throws SRuntimeException {
		return jparse(JsonUtil.parse(jsonData, sourceId), model, reporter);
	}

	@Override
	/** Parse and process XML data with JSON model.
	 * @param xmlData org.w3c.dom.Document or org.w3c.dom.Element.
	 * @param model qualified name of JSON root model.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return JSON object with processed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Object jparse(final Node xmlData,
		final String model,
		final ReportWriter reporter) throws SRuntimeException {
		Element e;
		if (xmlData instanceof Document) {
			e = ((Document) xmlData).getDocumentElement();
		} else {
			e = (Element) xmlData;
		}
		return jparse(JsonUtil.xmlToJson(e), model, reporter);
	}

	@Override
	/** Parse and process JSON data and return processed JSON object.
	 * @param jsonData JSON data.
	 * @param model qualified name of JSON root model.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return JSON object with processed data.
	 * @throws SRuntimeExcception if model is not found.
	 */
	public final Object jparse(final Object jsonData,
		final String model,
		final ReportWriter reporter) throws SRuntimeException {
		// Check if exists JSON root model and set xdVersion (xe._json != 0)
		XNode xn = _xdef._rootSelection.get(model);
		if (xn != null && xn.getKind() == XMNode.XMELEMENT) {
			XElement xe = (XElement) xn;
			if (xe._json != 0) {
				_xElement = xe;
				Element e = xe._json == XConstants.JSON_MODE
					? JsonUtil.jsonToXml(jsonData)
					: JsonUtil.jsonToXmlXdef(jsonData);
				if (xe._childNodes.length > 0
					&& xe._childNodes[0].getKind() == XMNode.XMCHOICE) {
					// XMChoice is root of JSON model of XML
					XChoice xch = (XChoice) xe._childNodes[0];
					String url = e.getNamespaceURI();
					QName eQname = url == null ? new QName(e.getTagName())
						: new QName(url, e.getLocalName());
					for (int i = xch.getBegIndex()+1; i<xch.getEndIndex(); i++){
						XNode y = xe._childNodes[i];
						if (y.getKind() == XMNode.XMELEMENT) {
							if (y.getQName().equals(eQname)) {
								_xElement = (XElement) y;
								break;
							}
						}
					}
				}
				xparse(e, reporter);
				return JsonUtil.xmlToJson(_element);
			}
		}
		//JSON model &{0}{"}{" }is missing in X-definition
		throw new SRuntimeException(XDEF.XDEF315, model);
	}

	@Override
	/** Parse source JSON and return XComponent as result.
	 * @param json string with pathname of JSON file or JSON source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent jparseXComponent(String json,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException {
		return jparseXComponent(JsonUtil.parse(json), xClass, reporter);
	}

	@Override
	/** Parse URL with JSON source and return XComponent as result.
	 * @param json URL with JSON source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent jparseXComponent(URL json,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException {
		return jparseXComponent(JsonUtil.parse(json), xClass, reporter);
	}

	@Override
	/** Parse URL with JSON source and return XComponent as result.
	 * @param json InputStream with JSON source data.
	 * @param sourceId name of source or <tt>null</tt>.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent jparseXComponent(InputStream json,
		String sourceId,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException {
		return jparseXComponent(JsonUtil.parse(json,sourceId),xClass,reporter);
	}

	@Override
	/** Parse file with JSON source and return XComponent as result.
	 * @param json file with JSON source data.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent jparseXComponent(File json,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException {
		return jparseXComponent(JsonUtil.parse(json), xClass, reporter);
	}

	@Override
	/** Parse XML data with JSON model and return XComponent as result.
	 * @param xml org.w3c.dom.Document or org.w3c.dom.Element
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final XComponent jparseXComponent(final Node xmlData,
		final Class<?> xClass,
		final ReportWriter reporter) throws SRuntimeException {
		Element e;
		if (xmlData instanceof Document) {
			e = ((Document) xmlData).getDocumentElement();
		} else {
			e = (Element) xmlData;
		}
		return jparseXComponent(JsonUtil.xmlToJson(e), xClass, reporter);
	}

	@Override
	/** Parse JSON data and return XComponent as result.
	 * @param json XML <tt>org.w3c.dom.Node</tt>.
	 * @param xClass XCompomnent class (if <tt>null</tt>, then XComponent class
	 * is searched in XDPool).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of parsed data.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public XComponent jparseXComponent(Object json,
		Class<?> xClass,
		ReportWriter reporter) throws SRuntimeException {
		Element e;
		Class<?> yClass = xClass;
		if (yClass == null) {
			int ndx;
			for (String s: getXDPool().getXComponents().keySet()) {
				String className = getXDPool().getXComponents().get(s);
				try {
					yClass = Class.forName(className);
					String jmodel =
						(String) yClass.getDeclaredField("XD_NAME").get(null);
					byte jVersion =
						(Byte) yClass.getDeclaredField("JSON").get(null);
					if (jVersion > 0) {
						XElement xe = _xdef.selectRoot(jmodel,
							jVersion == 1 ? XDConstants.JSON_NS_URI_W3C
								: XDConstants.JSON_NS_URI,
							-1);
						if (xe != null && xe._json != 0) {
							break;
						}
					}
				} catch (Exception ex) {}
				yClass = null;
			}
		}
		try {
			byte jVersion = (Byte) yClass.getDeclaredField("JSON").get(null);
			e = jVersion == XConstants.JSON_MODE ?
				JsonUtil.jsonToXml(json) : JsonUtil.jsonToXmlXdef(json);
		} catch (Exception ex) {
			e = JsonUtil.jsonToXml(json);
		}
		return parseXComponent(e, yClass, reporter);
	}

	@Override
	/** Run create mode - create element according to the X-definition model.
	 * If the parameter nsUri is not <tt>null</tt> then its assigned the model
	 * with given namespaceURI; in this case the parameter qname may be
	 * qualified with a prefix.
	 * @param nsUri the namespace URI of result element (may be <tt>null</tt>).
	 * @param name the name of model of required element (may contain prefix).
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xcreate(final String nsUri,
		final String name,
		final ReportWriter reporter) throws SRuntimeException {
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
		_refNum = 0; // we must clear counter!
		ChkComposer chkp =
			new ChkComposer(reporter == null ? new ArrayReporter() : reporter);
		chkp.xcreate(this, nsUri, name);
		return chkAndGetRootElement(chkp, reporter == null);
	}

	@Override
	/** Run create mode - create element according to the X-definition model.
	 * @param qname the QName of model of required element.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xcreate(final QName qname, final ReportWriter reporter)
		throws SRuntimeException {
		return xcreate(qname.getNamespaceURI(), qname.getLocalPart(), reporter);
	}

	@Override
	/** Run create mode - create element according to the X-definition model.
	 * @param name the name of model of required element.
	 * @param reporter report writer or <tt>null</tt>. If this argument is
	 * <tt>null</tt> and error reports occurs then SRuntimeException is thrown.
	 * @return root element of created XML document.
	 * @throws SRuntimeException if reporter is <tt>null</tt> and an error
	 * was reported.
	 */
	public final Element xcreate(final String name,
		final ReportWriter reporter) throws SRuntimeException {
		return xcreate(null, name, reporter);
	}

	@Override
	/** Decrease reference counter by one.
	 * @return The increased reference number.
	 */
	final int decRefNum() {return --_refNum;}

	@Override
	/** Increase reference counter by one.
	 * @return The increased reference number.
	 */
	final int incRefNum() {return ++_refNum;}

	@Override
	/** Get reference counter of actual definition
	 * @return The reference number.
	 */
	public final int getRefNum() {return _refNum;}

	@Override
	/** Get occurrence of this node
	 * @return The reference number.
	 */
	public final int getOccurrence() {return 1;}

	@Override
	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	final ChkElement getChkElement() {return _chkRoot;}

	@Override
	/** Get Element value assigned to this node.
	 * @return Element value assigned to this node.
	 */
	final Element getElemValue() {return _chkRoot.getElemValue();}

	@Override
	/** Assign Element value to this node.
	 * @param elem Element value to be assigned to this node.
	 */
	final void setElemValue(final Element elem) {_chkRoot.setElemValue(elem);}

	@Override
	/** Set output stream writer.
	 * @param out output stream.
	 * @param encoding encoding of output.
	 * @param writeDocumentHeader if true full document is written, otherwise
	 * only root element.
	 * @throws IOException if an error occurs.
	 */
	public final void setStreamWriter(final OutputStream out,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		_scp.setXmlStreamWriter(
			new DefXmlWriter(out,encoding,writeDocumentHeader));
	}

	@Override
	/** Set writer.
	 * @param out stream writer.
	 * @param encoding encoding of output.
	 * @param writeDocumentHeader if true full document is written, otherwise
	 * only root element.
	 */
	public final void setStreamWriter(final Writer out,
		final String encoding,
		final boolean writeDocumentHeader) {
		_scp.setXmlStreamWriter(
			new DefXmlWriter(out,encoding,writeDocumentHeader));
	}

	@Override
	/** Set XML writer.
	 * @param xmlWriter XML writer.
	 */
	public final void setStreamWriter(final XDXmlOutStream xmlWriter) {
		_scp.setXmlStreamWriter(xmlWriter);
	}

	@Override
	/** Get namespace context - for document it contains just XD prefix.
	 * @return namespace context of the parent element.
	 */
	public final KNamespace getXXNamespaceContext() {
		return new KNamespace();
	}

	@Override
	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	public final XXElement getRootXXElement() {
		return _rootChkDocument._chkRoot;
	}

	@Override
	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	public final XXElement getXXElement() {return _rootChkDocument._chkRoot;}

	@Override
	/** Get associated XML node.
	 * @return the associated XML node.
	 */
	public final Node getXMLNode() {return _doc;}

	@Override
	/** Get implementation properties of X-definition.
	 * @return the implementation properties of X-definition.
	 */
	public final Properties getImplProperties() {return _xdef._properties;}

	@Override
	/** Get implementation property of X-definition.
	 * @param name The name of property.
	 * @return the value implementation property of X-definition.
	 */
	public final String getImplProperty(final String name) {
		return _xdef._properties.getProperty(name);
	}

	@Override
	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	public final XXNode[] getChildXXNodes() {
		XXNode[] result = new XXNode[_chkChildNodes.size()];
		_chkChildNodes.toArray(result);
		return result;
	}

	@Override
	final ArrayList<ChkElement> getChkChildNodes() {return _chkChildNodes;}

	@Override
	/** Set debugging mode.
	 * @param debug debugging mode.
	 */
	public final void setDebug(final boolean debug) { _scp.setDebug(debug); }

	@Override
	/** Check debugging mode is set ON.
	 * @return value of debugging mode.
	 */
	public final boolean isDebug() {return _scp.isDebugMode();}

	@Override
	/** Get name of actual node.
	 * @return The name of node.
	 */
	public final String getNodeName() {return "#document";}

	@Override
	/** Get namespace URI of actual node.
	 * @return namespace URI or <tt>null</tt>.
	 */
	public final String getNodeURI() {return null;}

	/** Get text value of this node.
	 * @return The name of node.
	 */
	public final String getTextValue() {return null;}

	/** Set text value to this node.
	 * @param text the text value to be set.
	 */
	public final void setTextValue(final String text) {
		//Illegal use of method: &{0}
		throw new SRuntimeException(SYS.SYS083, "setTextValue");
	}

	@Override
	/** set debugger.
	 * @param debugger the debugger.
	 */
	public final void setDebugger(final XDDebug debugger) {
		_scp.setDebugger(debugger);
	}

	@Override
	/** Get debugger.
	 * @return the debugger.
	 */
	public final XDDebug getDebugger() {return _scp.getDebugger();}

	@Override
	/** Get actual model.
	 * @return actual model.
	 */
	public final XMNode getXMNode() {return _xdef;}

	@Override
	/** Get XComponent.
	 * @return The XComponent object (may be <tt>null</tt>).
	 */
	public final XComponent getXComponent() {return _xComponent;}

	@Override
	/** Set XComponent.
	 * @param x XComponent object.
	 */
	public final void setXComponent(final XComponent x) {_xComponent = x;}

	@Override
	/** Check value of datetime.
	 * Check if the year of date in the interval
	 * (YEAR_MIN .. YEAR_MAX) or the value of date is
	 * one of UNDEF_YEAR[] values.
	 * @param date value to be checked.
	 * @return true if date is legal.
	 */
	public final boolean isLegalDate(final SDatetime date) {
		int i;
		if (_stopCheckDateLegal || date == null
			|| (i = date.getYear()) == Integer.MIN_VALUE
			|| ((_minYear == Integer.MIN_VALUE || i >= _minYear)
			&& (_maxYear == Integer.MIN_VALUE || i <= _maxYear))) {
			return true; // not illlegal value or year not defined
		}
		if (_specialDates != null) { // check special dates
			for (SDatetime x: _specialDates) {
				if (date.equals(x)) { // Check special legal values of date
					return true; // is one of legal values
				}
			}
		}
		return false;
	}
	@Override
	/** Get minimum valid year of date.
	 * @return minimum valid year (Integer.MIN if not set).
	 */
	public final int getMinYear() {return _minYear;}
	@Override
	/** Set minimum valid year of date (or Integer.MIN is not set).
	 * @param x minimum valid year.
	 */
	public final void setMinYear(final int x) {_minYear = x;}
	@Override
	/** Get maximum valid year of date (or Integer.MIN if not set).
	 * @return maximum valid year (Integer.MIN if not set).
	 */
	public final int getMaxYear()  {return _maxYear;}
	@Override
	/** Set maximum valid year of date (or Integer.MIN is not set).
	 * @param x maximum valid year.
	 */
	public final void setMaxYear(final int x) {_maxYear = x;}
	@Override
	/** Get array of dates to be accepted out of interval minYear..maxYear.
	 * @return array with special values of valid dates.
	 */
	public final SDatetime[] getSpecialDates() {return _specialDates;}
	@Override
	/** Set array of dates to be accepted out of interval minYear..maxYear.
	 * @param x array with special values of valid dates.
	 */
	public final void setSpecialDates(final SDatetime[] x) {_specialDates = x;}
	@Override
	/** Set if year of date will be checked for interval minYear..maxYear.
	 * @param x if true year of date will be checked.
	 */
	public final void checkDateLegal(final boolean x){_stopCheckDateLegal = !x;}
	@Override
	/** Print reports to PrintStream.
	 * @param out PrintStream where reports are printed.
	 */
	public final void printReports(final java.io.PrintStream out) {
		getReportWriter().getReportReader().printReports(out);
	}
	@Override
	/** Get actual source language used for lexicon.
	 * @return string with actual language or return null if lexicon is not
	 * specified  or if language is not specified.
	 */
	public final String getLexiconLanguage() {
		return _sourceLanguageID < 0 ? null
		: ((XPool) getXDPool())._lexicon.getLanguages()[_sourceLanguageID];
	}
	@Override
	/** Set actual source language used for lexicon.
	 * @param language string with language or null.
	 * @throws SRuntimeException if lexicon is not specified or if
	 * language is not specified.
	 */
	public final void setLexiconLanguage(final String language) {
		XPool xp = (XPool) getXDPool();
		if (xp._lexicon == null) {
			//Can't set language &{0} because lexicon is not declared
			throw new SRuntimeException(XDEF.XDEF141, language);
		}
		try {
			_sourceLanguageID = language == null
				? -1 : xp._lexicon.getLanguageID(language);
		} catch (Exception ex) {
			//Can't set language &{0} because this language is not
			//specified in lexicon
			throw new SRuntimeException(XDEF.XDEF142, language);
		}
	}

	@Override
	/** Store  model variable.
	 * @param name name of variable.
	 * @return loaded value.
	 */
	final XDValue loadModelVariable(final String name) {
		throw new SRuntimeException(SYS.SYS066, //Internal error&{0}{: }
			"Unknown 'model' variable "+name);
	}

	@Override
	/** Store model variable.
	 * @param name name of variable.
	 * @param val value to be stored.
	 */
	final void storeModelVariable(final String name, final XDValue val) {
		throw new SRuntimeException(SYS.SYS066, //Internal error&{0}{: }
			"Unknown variable "+name);
	}

	@Override
	/** Translate the input element from the source language to the destination
	 * language according to lexicon.
	 * @param elem the element in the source language.
	 * @param sourceLanguage name of source language.
	 * @param destLanguage name of destination language.
	 * @param reporter the reporter where to write errors or null.
	 * @return element converted to the destination language.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Element xtranslate(final Element elem,
		final String sourceLanguage,
		final String destLanguage,
		final ReportWriter reporter) throws SRuntimeException {
		_reporter = new SReporter(reporter);
		_scp.setStdErr(new DefOutStream(_reporter.getReportWriter()));
		_refNum = 0; // we must clear counter!
		ChkTranslate chTranlsate =
			new ChkTranslate(reporter == null ? new ArrayReporter() : reporter);
		chTranlsate.xtranslate(this, elem, sourceLanguage, destLanguage);
		return chkAndGetRootElement(chTranlsate, reporter == null);
	}

	@Override
	/** Translate the input element from the source language to the destination
	 * language according to lexicon.
	 * @param elem path to the source element or the string
	 * with element.
	 * @param sourceLanguage name of source language.
	 * @param destLanguage name of destination language.
	 * @param reporter the reporter where to write errors or null.
	 * @return element converted to the destination language.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Element xtranslate(String elem,
		String sourceLanguage,
		String destLanguage,
		ReportWriter reporter) throws SRuntimeException {
		return xtranslate(KXmlUtils.parseXml(elem).getDocumentElement(),
			sourceLanguage, destLanguage, reporter);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Get actual destination language used for lexicon.
	 * @return string with actual language.
	 */
	final String getDestLexiconLanguage() {
		return (_destLanguageID < 0) ? null
			: ((XPool) getXDPool())._lexicon.getLanguages()[_destLanguageID];
	}


	/** Set actual destination language used for lexicon.
	 * @param language string with language or null.
	 * @throws SRuntimeException if lexicon is not specified or if
	 * language is not specified.
	 */
	final void setDestLexiconLanguage(final String language) {
		XPool xp = (XPool) getXDPool();
		if (xp._lexicon == null) {
			//Can't set language of output &{0} because lexicon is not
			//declared
			throw new SRuntimeException(XDEF.XDEF141, language);
		}
		_destLanguageID =
			language == null ? -1 : xp._lexicon.getLanguageID(language);
	}

	@Override
	public String toString() {return "ChkDocument: " + _xElement;}
}