package org.xdef.impl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.json.JParser;
import org.xdef.json.JsonNames;
import org.xdef.json.JsonParser;
import org.xdef.json.JsonTools;
import org.xdef.json.XONParsers;
import org.xdef.json.XONReader;
import org.xdef.msg.XDEF;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;

/** XML W3C parser of JSON/XON object from JSON/XON source.
 * @author Vaclav Trojan
 */
class XonSourceParser implements JParser, XParser {
	/** Allocation unit for node list. */
	private static final int NODELIST_ALLOC_UNIT = 8;
	/** instance of XONReader. */
	private final XONParsers _p;

	/** Nesting level. */
	private int _level = -1;
	private ChkDocument _chkDoc;
	private Document _doc;
	private Element _el;
	private ChkElement _chkEl;
	/** Stack of active node path. */
	private ChkElement[] _chkElemStack = new ChkElement[NODELIST_ALLOC_UNIT];
	/** Name of named item. */
	private SBuffer _name;
	/** simpleValue of item. */
	private JsonParser.JValue _value;

	XonSourceParser(final File f) {
		try {
			FileReader in = new FileReader(f);
			XONReader p = new XONReader(in, this);
			p.setXonMode();
			p.setJObjectsMode();
			p.setSysId(f.getCanonicalPath());
			_p = p;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	XonSourceParser(final URL url) {
		try {
			Reader in = new InputStreamReader(url.openStream(), "UTF-8");
			XONReader p = new XONReader(in, this);
			p.setXonMode();
			p.setJObjectsMode();
			p.setSysId(url.toExternalForm());
			_p = p;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	XonSourceParser(final Reader in, final String sysId) {
		XONReader p = new XONReader(in, this);
		p.setXonMode();
		p.setJObjectsMode();
		if (sysId != null) {
			p.setSysId(sysId);
		}
		_p = p;
	}

	XonSourceParser(final InputStream is, final String sysId) {
		try {
			Reader in = new InputStreamReader(is, "UTF-8");
			XONReader p = new XONReader(in, this);
			p.setXonMode();
			p.setJObjectsMode();
			if (sysId != null) {
				p.setSysId(sysId);
			}
			_p = p;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// validate object
	XonSourceParser(final Object x) {_p = new XonObjectParser(x, this);}

	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 */
	private void elementStart(final SBuffer elemName) {
		Element e = _doc.createElementNS(XDConstants.JSON_NS_URI_W3C,
			elemName.getString());
		String name = null;
		if (_name != null) {
			name = JsonTools.toXmlName(_name.getString());
			e.setAttribute(JsonNames.J_KEYATTR, name);
		}
		String value = null;
		if (_value != null) {
			value = JsonTools.genXMLValue(_value.getValue());
			e.setAttribute(JsonNames.J_VALUEATTR, value);
		}
		if (++_level == 0) {
			_el = e;
			_el.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns", XDConstants.JSON_NS_URI_W3C);
			QName qName = e.getNamespaceURI()==null ? new QName(e.getTagName())
				: new QName(e.getNamespaceURI(), e.getLocalName());
			_chkDoc._xElement = _chkDoc.findXElement(qName);
			if (_chkDoc._xElement == null) {
				//JSON root model&{0}{ of "}{" } is missing in X-definition
				throw new SRuntimeException(XDEF.XDEF315, e.getNodeName());
			}
			_chkEl = _chkDoc.createRootChkElement(_el, true);
		} else {
			_el = e;
			_chkEl = _chkEl.createChkElement(_el);
		}
		if (_level >= _chkElemStack.length) { //increase nodelist
			ChkElement[] newList =
				new ChkElement[_chkElemStack.length + NODELIST_ALLOC_UNIT];
			System.arraycopy(_chkElemStack, 0, newList, 0,_chkElemStack.length);
			_chkElemStack = newList;
		}
		_chkElemStack[_level] = _chkEl;
		if (_level == 0) {
			_chkDoc.getReporter().setPosition(new SPosition());
			_chkEl.newAttribute(e.getAttributeNode("xmlns"));
		}
		if (name != null) {
			_chkDoc.getReporter().setPosition(_name);
			_chkEl.addAttribute(JsonNames.J_KEYATTR, name);
		}
		if (value != null) {
			_chkDoc.getReporter().setPosition(_value.getPosition());
			_chkEl.addAttribute(JsonNames.J_VALUEATTR, value);
		}
		_chkDoc.getReporter().setPosition(elemName);
		_name = null;
		_value = null;
		_chkEl.checkElement();
	}

	private void elementEnd() {
		_chkDoc.getReporter().setPosition(_p.getPosition());
		_chkEl.addElement();
		if (_level > 0) {
			_chkElemStack[_level--] = null; //let's gc do the job
			_chkEl = _chkElemStack[_level];
		} else {
			_chkElemStack = null;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Interface JParser
////////////////////////////////////////////////////////////////////////////////

	@Override
	public void simpleValue(JsonParser.JValue value) {
		_value = value;
		elementStart(new SBuffer(JsonNames.J_ITEM, value.getPosition()));
		elementEnd();
	}

	@Override
	public void namedValue(SBuffer name) {_name = name;}

	@Override
	public void arrayStart(SPosition pos) {
		elementStart(new SBuffer(JsonNames.J_ARRAY, pos));
	}

	@Override
	public void arrayEnd(SPosition pos) {
		elementEnd();
	}

	@Override
	public void mapStart(SPosition pos) {
		elementStart(new SBuffer(JsonNames.J_MAP, pos));
	}

	@Override
	public void mapEnd(SPosition pos) {
		elementEnd();
	}

	@Override
	public void xdScript(SBuffer name, SBuffer value) {}

	@Override
	public void warning(SPosition pos, long ID, Object... params) {
		_chkDoc.getReporter().setPosition(pos);
		_chkDoc.warning(ID, params);
	}

	@Override
	public void error(SPosition pos, long ID, Object... params) {
		_chkDoc.getReporter().setPosition(pos);
		_chkDoc.error(ID, params);
	}

	@Override
	public void fatal(SPosition pos, long ID, Object... params) {
		_chkDoc.getReporter().setPosition(pos);
		_chkDoc.fatal(ID, params);
	}

////////////////////////////////////////////////////////////////////////////////
// Interface XParser
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Parse source.
	 * @param chkDoc The ChkDocument object.
	 */
	public void xparse(final ChkDocument chkDoc) {
		_chkDoc = chkDoc;
		_doc = _chkDoc._doc;
		chkDoc.getReporter();
		_p.parse();
		_chkDoc._xon = _chkEl._xonArray != null ? _chkEl._xonArray
			: _chkEl._xonMap != null ? _chkEl._xonMap : _chkEl._xonValue;
		_chkElemStack = null;
		_doc = null;
		_chkEl = null;
	}

	@Override
	/** Get connected reporter.
	 * @return connected SReporter.
	 */
	public SReporter getReporter() {return _chkDoc.getReporter();}
	@Override
	/** Close reader of parsed data. */
	public void closeReader() {
		try {
			_p.closeReader();
		} catch (Exception ex) {} // ignore it
	}
	@Override
	public void setSysId(final String sysId) {}
}

/** XML W3C parser of JSON/XON object from JSON/XON object.
 * @author Vaclav Trojan
 */
class XonObjectParser implements XONParsers {
	/** Empty position. */
	private static final SPosition NULPOS = new SPosition();
	private final Object _obj;
	private final JParser _jp;

	XonObjectParser(final Object obj, final JParser jp) {
		_obj = obj;
		_jp = jp;
	}
	private void parse(Object o) {
		if (o instanceof Map) {
			_jp.mapStart(NULPOS);
			for (Object x: ((Map) o).entrySet()) {
				Map.Entry en = (Map.Entry) x;
				_jp.namedValue(new SBuffer((String)en.getKey()));
				parse(en.getValue());
			}
			_jp.mapEnd(NULPOS);
		} else if (o instanceof List) {
			_jp.arrayStart(NULPOS);
			for (Object x: ((List) o)) {
				parse(x);
			}
			_jp.arrayEnd(NULPOS);
		} else {
			_jp.simpleValue(new JsonParser.JValue(NULPOS, o));
		}
	}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	@Override
	public SPosition getPosition() {return NULPOS;}
	@Override
	public void parse() {parse(_obj);}
	@Override
	public void closeReader() {}
}