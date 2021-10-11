package org.xdef.impl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonReader;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xon.XonParsers;
import org.xdef.xon.XonParser;
import org.xdef.xon.XonNames;

/** Parse  JSON/XON object from JSON/XON source and generate XML (W3C format).
 * Reads source with JSON/XON and generates W3C XML methods invoked in
 * CHKDocument and CHKElement.
 * @author Vaclav Trojan
 */
public class XonSourceParser implements XonParser, XParser {
	/** Allocation unit for node list. */
	private static final int NODELIST_ALLOC_UNIT = 8;
	/** instance of XONReader. */
	private final XonParsers _p;

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
	private XonTools.JValue _value;

	XonSourceParser(final File f) {
		try {
			FileReader in = new FileReader(f);
			XonReader p = new XonReader(in, this);
			p.setXonMode();
			p.setSysId(f.getCanonicalPath());
			_p = p;
			return;
		} catch (Exception ex) {}
		throw new SRuntimeException(SYS.SYS028, f); //Can't read file: &{0}
	}

	XonSourceParser(final URL url) {
		String id = url.toExternalForm();
		try {
			Reader in = new InputStreamReader(
				url.openStream(), Charset.forName("UTF-8"));
			XonReader p = new XonReader(in, this);
			p.setXonMode();
			p.setSysId(id);
			_p = p;
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS028, id); //Can't read file: &{0}
		}
	}

	XonSourceParser(final Reader in, final String sysId) {
		XonReader p = new XonReader(in, this);
		p.setXonMode();
		if (sysId != null) {
			p.setSysId(sysId);
		}
		_p = p;
	}

	XonSourceParser(final InputStream is, final String sysId) {
		Reader in = new InputStreamReader(is, Charset.forName("UTF-8"));
		XonReader p = new XonReader(in, this);
		p.setXonMode();
		if (sysId != null) {
			p.setSysId(sysId);
		}
		_p = p;
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
			name = XonTools.toXmlName(_name.getString());
			e.setAttribute(XonNames.X_KEYATTR, name);
		}
		String value = null;
		if (_value != null) {
			value = XonTools.genXMLValue(_value.getValue());
			e.setAttribute(XonNames.X_VALUEATTR, value);
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
			_chkEl.addAttribute(XonNames.X_KEYATTR, name);
		}
		if (value != null) {
			_chkDoc.getReporter().setPosition(_value.getPosition());
			_chkEl.addAttribute(XonNames.X_VALUEATTR, value);
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
	/** Put value to result.
	 * @param value JValue to be added to result object.
	 * @return null or name of pair if value pair already exists in
	 * the currently processed map.
	 */
	public String putValue(final XonTools.JValue value) {
		_value = value;
		elementStart(new SBuffer(XonNames.X_ITEM, value.getPosition()));
		elementEnd();
		return null;
	}
	@Override
	/** Set name of value pair.
	 * @param name value name.
	 */
	public void namedValue(final SBuffer name) {_name = name;}
	@Override
	/** Array started.
	 * @param pos source position.
	 */
	public void arrayStart(final SPosition pos) {
		elementStart(new SBuffer(XonNames.X_ARRAY, pos));
	}
	@Override
	/** Array ended.
	 * @param pos source position.
	 */
	public void arrayEnd(final SPosition pos) {elementEnd();}
	@Override
	/** Map started.
	 * @param pos source position.
	 */
	public void mapStart(final SPosition pos) {
		elementStart(new SBuffer(XonNames.X_MAP, pos));
	}
	@Override
	/** Map ended.
	 * @param pos source position.
	 */
	public void mapEnd(final SPosition pos) {elementEnd();}

	@Override
	/** Processed comment.
	 * @param value SBuffer with the value of comment.
	 */
	public void comment(SBuffer value){/*we ingore it here*/}

	@Override
	/** X-script item parsed, not used methods for JSON/XON parsing
	 * (used in X-definition compiler).
	 * @param name name of item.
	 * @param value value value of item.
	 */
	public void xdScript(final SBuffer name, SBuffer value) {}

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

	////////////////////////////////////////////////////////////////////////////
	/** XML W3C parser of JSON/XON object from JSON/XON object.
	 * @author Vaclav Trojan
	 */
	private static class XonObjectParser implements XonParsers {
		/** Empty position. */
		private static final SPosition NULPOS = new SPosition();
		private final Object _obj;
		private final XonParser _jp;

		XonObjectParser(final Object obj, final XonParser jp) {
			_obj = obj;
			_jp = jp;
		}

		/** Parsing of an JSON/XON object.
		 * @param o the object to parse.
		 */
		private void parse(final Object o) {
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
				_jp.putValue(new XonTools.JValue(NULPOS, o));
			}
		}

////////////////////////////////////////////////////////////////////////////////
// implementation of the interface XONParsers
////////////////////////////////////////////////////////////////////////////////
		@Override
		public SPosition getPosition() {return NULPOS;} // no position
		@Override
		public void parse() {parse(_obj);}
		@Override
		public void closeReader() {}
	}
}