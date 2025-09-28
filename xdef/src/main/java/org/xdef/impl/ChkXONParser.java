package org.xdef.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import java.util.Stack;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.impl.xml.KParsedAttr;
import org.xdef.impl.xml.KParsedElement;
import org.xdef.model.XMData;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SError;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUnsupportedOperationException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import org.xdef.xon.XonParser;
import org.xdef.xon.XonReader;
import org.xdef.xon.XonTools;

/** Parsing of the XML source with the X-definition.
 * @author Vaclav Trojan
 */
final class ChkXONParser implements XParser, XonParser {
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
	/** Input stream with source data.*/
	private Reader _in;
	/** Reporter for error messages. */
	private SReporter _sReporter;
	/** source position. */
	public SPosition _elemLocator;
	/** System id. */
	public String _sysId;
	/** Stack with kind types of item. */
	private final Stack<Integer> _kinds;
	/** Type of item: 0..value, 1..array, 2..map */
	private int _kind;
	/** Stack with names of items of map. */
	private Stack<SBuffer> _names;
	/** Stack with stack with names of items of map. */
	private final Stack<Stack<SBuffer>> _mapNames;
	/** flag if namespace was generated. */
	private boolean _nsGenerated;

	/** Creates a new instance of ChkParser and parses given string.
	 * @param reporter The reporter.
	 * @param source The string with source XML data.
	 */
	ChkXONParser(final ReportWriter reporter, final Object source) {this(reporter, source, null);}

	/** Creates a new instance of ChkParser and parses given data.
	 * @param reporter The reporter.
	 * @param source Object with source data (may be File, URL, string, InputStream, or Reader).
	 * @param sourceName the name of source data
	 */
	ChkXONParser(final ReportWriter reporter, final Object source, final String sourceName) {
		_kinds = new Stack<>();
		_mapNames = new Stack<>();
		_sReporter = new SReporter(reporter == null ?  new ArrayReporter() : reporter);
		if (source instanceof String) {
			String s = (String) source;
			try { // try if it is URL
				URL u = SUtils.getExtendedURL(s);
				_in = XonReader.getXonReader(u.openStream());
				_sysId = sourceName == null ? u.toExternalForm() : sourceName;
			} catch (IOException ex) {
				try { // try if it is a file name
					File f = new File(s);
					_in = XonReader.getXonReader(new FileInputStream(f));
					_sysId = f.getCanonicalPath();
				} catch (IOException exx) {//not file, try to parse it as string
					_sysId = sourceName == null ? "STRING_DATA" : sourceName;
					_in = new StringReader(s);
				}
			}
		} else if (source instanceof File) {
			File f = (File) source;
			try {
				_in = XonReader.getXonReader(new FileInputStream(f));
				_sysId = sourceName == null ? f.getCanonicalPath() : sourceName;
			} catch (IOException ex) {
				throw new SRuntimeException(SYS.SYS024,f.getAbsoluteFile()); //File doesn't exist:&{0}
			}
		} else if (source instanceof URL) {
			URL u = (URL) source;
			try {
				_in = XonReader.getXonReader(u.openStream());
				_sysId = sourceName == null ? u.toExternalForm() : sourceName;
			} catch (IOException ex) {
				throw new SRuntimeException(SYS.SYS028, u.toString()); //Can't read file: &{0}
			}
		} else if (source instanceof InputStream) {
			_in = XonReader.getXonReader((InputStream) source);
			_sysId = sourceName == null ? "READER" : sourceName;
		} else if (source instanceof Reader) {
			_in = (Reader) source;
			_sysId = sourceName == null ? "INPUTSTREAM" : sourceName;
		} else {
			//Unsupported type of argument &{0}: &{1}
			throw new SRuntimeException(SYS.SYS037, "source", source != null ? source.getClass() : null);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// private methods
////////////////////////////////////////////////////////////////////////////////

	/** Add attributes from parsedElem object to thew created element.
	 * @param parsedElem object with parsed attributes.
	 */
	private void addAttrs(final KParsedElement parsedElem) {
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			if (ka.getValue() != null) {
				_element.setAttributeNS(ka.getNamespaceURI(), ka.getName(), ka.getValue());
			}
		}
	}

	/** This method is called after all attributes of the current element attribute list was reached.
	 * The implementation may check the list of attributes and to invoke appropriate actions. The method
	 * is invoked when parser reaches the end of the attribute list.
	 */
	private void elementStart(final KParsedElement parsedElem) {
		if (++_level == 0) {
			if (_chkDoc == null) {
				throw new SRuntimeException(XDEF.XDEF550); //X-definition is not specified
			}
			_chkDoc.setDocument(
				KXmlUtils.newDocument(parsedElem.getParsedNSURI(), parsedElem.getParsedName(), null));
			_element = _chkDoc.getDocument().getDocumentElement();
			addAttrs(parsedElem);
			_chkEl = _chkDoc.createRootChkElement(_element, true);
		} else {
			Element el = _chkDoc.getDocument().createElementNS(
				parsedElem.getParsedNSURI(),parsedElem.getParsedName());
			_element.appendChild(el);
			_element = el;
			addAttrs(parsedElem);
			_chkEl = _chkEl.createChkElement(_element);
		}
		if (_level >= _chkElemStack.length) { //increase nodelist
			ChkElement[] newList = new ChkElement[_chkElemStack.length + NODELIST_ALLOC_UNIT];
			System.arraycopy(_chkElemStack, 0, newList, 0,_chkElemStack.length);
			_chkElemStack = newList;
		}
		_chkElemStack[_level] = _chkEl;
		for (XMData x: _chkEl.getXMElement().getAttrs()) {
			KParsedAttr ka = parsedElem.getAttrNS(x.getNSUri(),x.getName());
			if (ka != null) {
				_sReporter.setPosition(ka.getPosition());
				Attr att = _chkDoc.getDocument().createAttributeNS(ka.getNamespaceURI(), ka.getName());
				att.setValue(ka.getValue());
				_chkEl.newAttribute(att);
				parsedElem.remove(ka); // processed, remove from attr list
			}
		}
		// process not processed attributes
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			if (ka.getValue() != null) {
				_sReporter.setPosition(ka.getPosition());
				_chkEl.newAttribute(_element.getAttributeNode(ka.getName()));
			}
		}
		_sReporter.setPosition(parsedElem.getParsedNameSourcePosition());
		_chkEl.checkElement();
	}

	/** This method is invoked when parser reached the end of element. */
	private void elementEnd() {
		_chkElemStack[_level--] = null; //let's gc do the job
		_chkEl.addElement();
		if (_level >= 0) {
			_chkEl = _chkElemStack[_level];
			_element = _chkEl._element;
			if (_element == null) { // ???
				throw new SRuntimeException(_sReporter.printToString());
			}
		}
	}

	private KParsedElement genKElem(final String qname, final String nsuri, final SPosition spos) {
		KParsedElement kelem = new KParsedElement();
		kelem.setParsedNameParams(nsuri, qname, spos);
		if (!_nsGenerated && nsuri != null) {
			kelem.addAttr(new KParsedAttr(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns", XDConstants.XON_NS_URI_W, spos));
			_nsGenerated = true;
		}
		return kelem;
	}

	private void genItem(final XonTools.JValue value, final SBuffer name) {
		KParsedElement kelem = genKElem(XonNames.X_VALUE,
			XDConstants.XON_NS_URI_W, name == null ? value.getPosition() : name);
		if (name != null) {
			kelem.addAttr(new KParsedAttr(XonNames.X_KEYATTR, XonTools.toXmlName(name.getString()), name));
		}
		kelem.addAttr(new KParsedAttr(XonNames.X_VALATTR,
			XonTools.genXMLValue(value.getValue()), value.getPosition()));
		elementStart(kelem);
		elementEnd();
	}

	private void doParse() {
		_kinds.push(_kind = 0);
		XonReader xr = new XonReader(_in, this);
		xr.setSysId(_sysId);
		xr.setReportWriter(_sReporter.getReportWriter());
		xr.setXonMode();
		xr.parse();
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XParser
////////////////////////////////////////////////////////////////////////////////

	/** Parse XML generated from XON source.
	 * @param chkDoc The ChkDocument object.
	 */
	@Override
	public final void xparse(final XDDocument chkDoc) {
		try {
			_level = -1;
			_chkElemStack = new ChkElement[NODELIST_ALLOC_UNIT];
			_chkEl = null;
			_chkDoc = (ChkDocument) chkDoc;
			_chkDoc._node = null;
			_chkDoc._element = null;
			XCodeProcessor scp = _chkDoc._scp;
			Properties props = scp.getProperties();
			_chkDoc._scp = null;
			_chkDoc.init((XDefinition) chkDoc.getXMDefinition(),
				(Document) _chkDoc.getDocument().cloneNode(false),
				chkDoc.getReporter(),
				scp.getProperties(),
				chkDoc.getUserObject());
			_chkDoc._scp = scp;
			_chkDoc._rootChkDocument.setDocument(null);
			_chkDoc.setDocument(null);
			XPool xdp = (XPool) ((ChkDocument)chkDoc).getXDPool();
			if (_chkDoc.isDebug() && _chkDoc.getDebugger() != null) {
				 // open debugger
				_chkDoc.getDebugger().openDebugger(props, xdp);
			}
			_chkDoc._scp.initscript();//Initialize variables and methods
			try {
				doParse();
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

	/** Get connected reporter.
	 * @return connected SReporter.
	 */
	@Override
	public final SReporter getReporter() {return _sReporter;}

	/** Close reader of parsed data. */
	@Override
	public final void closeReader() {
		if (_in != null) {
			try {_in.close();} catch (IOException ex) {} // ignore mexception
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XonParser
////////////////////////////////////////////////////////////////////////////////

	/** Put value to result.
	 * @param value JValue to be added to result object.
	 */
	@Override
	public void putValue(final XonTools.JValue value) {
		if (_kind == 2) { // map
			SBuffer name = _names.pop();
			genItem(value, name);
		} else {// simple value or array
			genItem(value, null);
		}
	}

	/** Set name of value pair.
	 * @param name value name.
	 * @return true if the name of pair already exists otherwise return false.
	 */
	@Override
	public final boolean namedValue(final SBuffer name) {
		boolean result = false;
		String s = name.getString();
		for (SBuffer x : _names) {
			if (s.equals(x.getString())) {
				result = true;
				break;
			}
		}
		_names.push(name);
		return result;
	}

	/** Array started.
	 * @param pos source position.
	 */
	@Override
	public final void arrayStart(final SPosition pos) {
		KParsedElement kelem = genKElem(XonNames.X_ARRAY, XDConstants.XON_NS_URI_W, pos);
		if (_kind == 2) { // map
			SBuffer name = _names.peek();
			kelem.addAttr(new KParsedAttr(XonNames.X_KEYATTR, XonTools.toXmlName(name.getString()), name));
		}
		elementStart(kelem);
		_kinds.push(_kind = 1);
	}

	/** Array ended.
	 * @param pos source position.
	 */
	@Override
	public final void arrayEnd(final SPosition pos) {
		_kinds.pop();
		_kind = _kinds.peek();
		elementEnd();
	}

	/** Map started.
	 * @param pos source position.
	 */
	@Override
	public final void mapStart(final SPosition pos) {
		KParsedElement kelem = genKElem(XonNames.X_MAP, XDConstants.XON_NS_URI_W, pos);
		if (_kind == 2) { // map
			SBuffer name = _names.peek();
			kelem.addAttr(new KParsedAttr(XonNames.X_KEYATTR, XonTools.toXmlName(name.getString()), name));
		}
		elementStart(kelem);
		_mapNames.push(_names = new Stack<>());
		_kinds.push(_kind = 2);
	}

	/** Map ended.
	 * @param pos source position.
	 */
	@Override
	public final void mapEnd(final SPosition pos) {
		_kinds.pop();
		_kind = _kinds.peek();
		elementEnd();
		_names = _mapNames.pop();
	}

	/** Processed comment.
	 * @param value SBuffer with the value of comment.
	 */
	@Override
	public final void comment(final SBuffer value){} // we ingore it here

	/** X-script item parsed (not used methods for XON/JSON parsing, used in X-definition compiler).
	 * @param name name of item.
	 * @param value value of item.
	 */
	@Override
	public final void xdScript(final SBuffer name, final SBuffer value) {}

	/** Get result of parser (not supported here). */
	@Override
	public final Object getResult(){throw new SUnsupportedOperationException();}
}
