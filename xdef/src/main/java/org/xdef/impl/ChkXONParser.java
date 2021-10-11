package org.xdef.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Stack;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.impl.xml.KParsedAttr;
import org.xdef.impl.xml.KParsedElement;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonReader;
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
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonParser;
import org.xdef.xon.XonNames;

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
	/** Input stream with XML data.*/
	private InputStream _in;
	/** Reporter for error messages. */
	private SReporter _sReporter;
	/** XML source position. */
	public SPosition _elemLocator;
	/** System id. */
	public String _sysId;

	private final Stack<Integer> _kinds = new Stack<Integer>();
	private int _kind; // 0..value, 1..array, 2..map
	private final Stack<SBuffer> _names = new Stack<SBuffer>();
	private boolean _nsGenerated;


	private ChkXONParser(final ReportWriter reporter) {
		super();
		_sReporter = new SReporter(
			reporter == null ?  new ArrayReporter() : reporter);
	}

	/** Creates a new instance of ChkParser and parses given string.
	 * @param reporter The reporter.
	 * @param source The string with source XML data.
	 */
	ChkXONParser(final ReportWriter reporter, final String source) {
		this(reporter);
		if (source == null || source.trim().isEmpty()) {
			throw new SRuntimeException(SYS.SYS024, "null");
		}
		String s = source.trim();
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
				if (s.length() > 1) {
					_sysId = "STRING";
					_in = new ByteArrayInputStream(
						source.getBytes(Charset.forName("UTF-8")));
				} else {
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
	ChkXONParser(final ReportWriter reporter, final File source) {
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
	ChkXONParser(final ReportWriter reporter,
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
	ChkXONParser(final ReportWriter reporter, final URL source) {
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

////////////////////////////////////////////////////////////////////////////////
// Implementation of XParser
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Close reader of parsed data. */
	public final void closeReader() {
		if (_in != null) {
			try {
				_in.close();
			} catch (IOException ex) {
				throw new SRuntimeException(ex);
			}
		}
	}

	@Override
	/** Get connected reporter.
	 * @return connected SReporter.
	 */
	public final SReporter getReporter() {return _sReporter;}

	@Override
	/** Parse XML generated from XON source.
	 * @param chkDoc The ChkDocument object.
	 */
	public final void xparse(final ChkDocument chkDoc) {
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
			_chkDoc._doc = _chkDoc._rootChkDocument._doc = null;
			XPool xdp = (XPool) chkDoc._xdef.getXDPool();
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
////////////////////////////////////////////////////////////////////////////////

	/** Add attributes from parsedElem object to thew created element.
	 * @param parsedElem object with parsed attributes.
	 */
	private void addAttrs(final KParsedElement parsedElem) {
		for (int i = 0, max = parsedElem.getLength(); i < max; i++) {
			KParsedAttr ka = parsedElem.getAttr(i);
			if (ka.getValue() != null) {
				_element.setAttributeNS(
					ka.getNamespaceURI(), ka.getName(), ka.getValue());
			}
		}
	}

	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 */
	private void elementStart(final KParsedElement parsedElem) {
		if (++_level == 0) {
			if (_chkDoc == null) {
				//X-definition is not specified
				throw new SRuntimeException(XDEF.XDEF550);
			}
			_chkDoc._doc=KXmlUtils.newDocument(parsedElem.getParsedNSURI(),
				parsedElem.getParsedName(), null);
			_element = _chkDoc._doc.getDocumentElement();
			addAttrs(parsedElem);
			_chkEl = _chkDoc.createRootChkElement(_element, true);
		} else {
			Element el = _chkDoc._doc.createElementNS(
				parsedElem.getParsedNSURI(), parsedElem.getParsedName());
			_element.appendChild(el);
			_element = el;
			addAttrs(parsedElem);
			_chkEl = _chkEl.createChkElement(_element);
		}
		if (_level >= _chkElemStack.length) { //increase nodelist
			ChkElement[] newList =
				new ChkElement[_chkElemStack.length + NODELIST_ALLOC_UNIT];
			System.arraycopy(_chkElemStack, 0, newList, 0,_chkElemStack.length);
			_chkElemStack = newList;
		}
		_chkElemStack[_level] = _chkEl;
		for (XMData x: _chkEl.getXMElement().getAttrs()) {
			KParsedAttr ka = parsedElem.getAttrNS(x.getNSUri(),x.getName());
			if (ka != null) {
				_sReporter.setPosition(ka.getPosition());
				Attr att = _chkDoc._doc.createAttributeNS(ka.getNamespaceURI(),
					ka.getName());
				att.setValue(ka.getValue());
				_chkEl.newAttribute(att);
			}
		}
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
		}
	}

	private KParsedElement genKElem(final String qname, final SPosition spos) {
		KParsedElement kelem = new KParsedElement();
		kelem.setParsedNameParams(XDConstants.JSON_NS_URI_W3C, qname, spos);
		if (!_nsGenerated) {
			kelem.addAttr(new KParsedAttr(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns", XDConstants.JSON_NS_URI_W3C, spos));
			_nsGenerated = true;
		}
		return kelem;
	}

	private void genItem(final XonReader.X_Value value, final SBuffer name) {
		KParsedElement kelem = genKElem(XonNames.X_ITEM,
			name == null ? value.getPosition() : name);
		if (name != null) {
			kelem.addAttr(new KParsedAttr(XonNames.X_KEYATTR,
				XonTools.toXmlName(name.getString()), name));
		}
		kelem.addAttr(new KParsedAttr(XonNames.X_VALUEATTR,
			XonTools.genXMLValue(value.getValue()), value.getPosition()));
		elementStart(kelem);
		elementEnd();
	}

	private void doParse() {
		_kinds.push(_kind = 0);
		XonReader xr = new XonReader(
			new InputStreamReader(_in, Charset.forName("UTF-8")), this);
		xr.setSysId(_sysId);
		xr.setReportWriter(_sReporter.getReportWriter());
		xr.setXonMode();
		xr.parse();
	}

	@Override
	/** Put value to result.
	 * @param value JValue to be added to result object.
	 * @return null or name of pair if value pair already exists in
	 * the currently processed map.
	 */
	public String putValue(final XonReader.X_Value value) {
		if (_kind == 2) { // map
			SBuffer name = _names.pop();
			genItem(value, name);
			for (SBuffer x : _names) {
				if (x.getString().equals(name.getString())) {
					return name.getString(); //stack contains this name
				}
			}
		} else {// simple value or array
			genItem(value, null);
		}
		return null;
	}
	@Override
	/** Set name of value pair.
	 * @param name value name.
	 */
	public final void namedValue(final SBuffer name) {_names.push(name);}
	@Override
	/** Array started.
	 * @param pos source position.
	 */
	public final void arrayStart(final SPosition pos) {
		KParsedElement kelem = genKElem(XonNames.X_ARRAY, pos);
		if (_kind == 2) { // map
			SBuffer name = _names.peek();
			kelem.addAttr(new KParsedAttr(XonNames.X_KEYATTR,
				XonTools.toXmlName(name.getString()), name));
		}
		elementStart(kelem);
		_kinds.push(_kind = 1);
	}
	@Override
	/** Array ended.
	 * @param pos source position.
	 */
	public final void arrayEnd(final SPosition pos) {
		_kinds.pop();
		_kind = _kinds.peek();
		elementEnd();
	}

	@Override
	/** Map started.
	 * @param pos source position.
	 */
	public final void mapStart(final SPosition pos) {
		KParsedElement kelem = genKElem(XonNames.X_MAP, pos);
		if (_kind == 2) { // map
			SBuffer name = _names.peek();
			kelem.addAttr(new KParsedAttr(XonNames.X_KEYATTR,
				XonTools.toXmlName(name.getString()), name));
		}
		elementStart(kelem);
		_kinds.push(_kind = 2);
	}
	@Override
	/** Map ended.
	 * @param pos source position.
	 */
	public final void mapEnd(final SPosition pos) {
		_kinds.pop();
		_kind = _kinds.peek();
		elementEnd();
	}
	@Override
	/** Processed comment.
	 * @param value SBuffer with the value of comment.
	 */
	public final void comment(final SBuffer value){} // we ingore it here
	@Override
	/** X-script item parsed, not used methods for JSON/XON parsing
	 * (used in X-definition compiler).
	 * @param name name of item.
	 * @param value value of item.
	 */
	public final void xdScript(final SBuffer name, final SBuffer value) {}
}