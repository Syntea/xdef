package org.xdef.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUnsupportedOperationException;
import org.xdef.xml.KXmlUtils;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import static org.xdef.xon.XonNames.X_VALUE;
import org.xdef.xon.XonParser;
import org.xdef.xon.XonParsers;
import org.xdef.xon.XonReader;
import org.xdef.xon.XonTools;

/** Parse  XON/JSON object from XON/JSON source and generate XML (W3C format).
 * Reads source with XON/JSON and generates W3C XML methods invoked in
 * CHKDocument and CHKElement.
 * @author Vaclav Trojan
 */
public final class XonSourceParser implements XonParser, XParser {
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
    private Stack<SBuffer> _names;
    private final Stack<Stack<SBuffer>> _mapNames = new Stack<>();

    XonSourceParser(final File f) {
        try {
            XonReader p = new XonReader(XonReader.getXonReader(new FileInputStream(f)), this);
            p.setXonMode();
            p.setSysId(f.getCanonicalPath());
            _p = p;
            return;
        } catch (IOException ex) {}
        throw new SRuntimeException(SYS.SYS028, f); //Can't read file: &{0}
    }

    XonSourceParser(final URL url) {
        String id = url.toExternalForm();
        try {
            XonReader p = new XonReader(XonReader.getXonReader(url.openStream()), this);
            p.setXonMode();
            p.setSysId(id);
            _p = p;
        } catch (IOException ex) {
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
        XonReader p = new XonReader(XonReader.getXonReader(is), this);
        p.setXonMode();
        if (sysId != null) {
            p.setSysId(sysId);
        }
        _p = p;
    }

    // validate object
    XonSourceParser(final Object x) {_p = new XonObjectParser(x, this);}

    /** Set reporter to the parser.
     * @param reporter where to write reports.
     */
    public final void setReporter(ReportWriter reporter) {_p.setReportWriter(reporter);}


    /** This method is called after all attributes of the current element attribute list was reached.
     * The implementation may check the list of attributes and to invoke appropriate actions. The method
     * is invoked when parser reaches the end of the attribute list.
     * @param elemName name of element.
     */
    private void elementStart(final SBuffer elemName) {
        Element e = _doc.createElementNS(XDConstants.XON_NS_URI_W, elemName.getString());
        String name = null;
        if (_name != null) {
            name = XonTools.toXmlName(_name.getString());
            e.setAttribute(X_KEYATTR, name);
        }
        String value = null;
        if (_value != null) {
            value = XonTools.genXMLValue(_value.getValue());
            e.setAttribute(X_VALATTR, value);
        }
        if (++_level == 0) {
            _el = e;
            _el.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", XDConstants.XON_NS_URI_W);
            QName qName = KXmlUtils.getQName(e);
            _chkDoc._xElement = _chkDoc.findXElement(qName);
            if (_chkDoc._xElement == null) {
                //Text with &{0} model&{1}{ of "}{" } is missing in X-definition
                throw new SRuntimeException(XDEF.XDEF315, "json", e.getNodeName());
            }
            _chkEl = _chkDoc.createRootChkElement(_el, true);
        } else {
            _el = e;
            _chkEl = _chkEl.createChkElement(_el);
        }
        if (_level >= _chkElemStack.length) { //increase nodelist
            ChkElement[] newList = new ChkElement[_chkElemStack.length + NODELIST_ALLOC_UNIT];
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
            _chkEl.addAttribute(X_KEYATTR, name);
        }
        if (value != null) {
            _chkDoc.getReporter().setPosition(_value.getPosition());
            _chkEl.addAttribute(X_VALATTR, value);
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

    /** Put value to result.
     * @param value JValue to be added to result object.
     */
    @Override
    public void putValue(final XonTools.JValue value) {
        _value = value;
        elementStart(new SBuffer(X_VALUE, value.getPosition()));
        elementEnd();
    }

    /** Set name of value pair.
     * @param name value name.
     * @return true if the name of pair already exists otherwise return false.
     */
    @Override
    public boolean namedValue(final SBuffer name) {
        String s = name.getString();
        boolean result = false;
        for (SBuffer x : _names) {
            if (s.equals(x.getString())) { //Value pair &{0} already exists (error(JSON.JSON022, name)
                result = true;
                break;
            }
        }
        _names.push(_name = name);
        return result;
    }

    /** Array started.
     * @param pos source position.
     */
    @Override
    public void arrayStart(final SPosition pos) {elementStart(new SBuffer(X_ARRAY, pos));}

    /** Array ended.
     * @param pos source position.
     */
    @Override
    public void arrayEnd(final SPosition pos) {elementEnd();}

    /** Map started.
     * @param pos source position.
     */
    @Override
    public void mapStart(final SPosition pos) {
        elementStart(new SBuffer(X_MAP, pos));
        _mapNames.push(_names = new Stack<>());
    }

    /** Map ended.
     * @param pos source position.
     */
    @Override
    public void mapEnd(final SPosition pos) {elementEnd();_names = _mapNames.pop();}


    /** Processed comment.
     * @param value SBuffer with the value of comment.
     */
    @Override
    public void comment(SBuffer value){/*we ingore it here*/}

    /** X-script item parsed, not used methods for XON/JSON parsing (used in X-definition compiler).
     * @param name name of item.
     * @param value value value of item.
     */
    @Override
    public void xdScript(final SBuffer name, SBuffer value) {}

    /** Get result of parser (not supported here). */
    @Override
    public final Object getResult(){throw new SUnsupportedOperationException();}

////////////////////////////////////////////////////////////////////////////////
// Interface XParser
////////////////////////////////////////////////////////////////////////////////


    /** Parse source.
     * @param chkDoc The ChkDocument object.
     */
    @Override
    public void xparse(final XDDocument chkDoc) {
        _chkDoc = (ChkDocument) chkDoc;
        _doc = _chkDoc.getDocument();
        chkDoc.getReporter();
        _p.parse();
        _chkDoc._xon = _chkEl._xonArray != null ? _chkEl._xonArray
            : _chkEl._xonMap != null ? _chkEl._xonMap : _chkEl._xonValue;
        _chkElemStack = null;
        _doc = null;
        _chkEl = null;
    }

    /** Get connected reporter.
     * @return connected SReporter.
     */
    @Override
    public SReporter getReporter() {return _chkDoc.getReporter();}

    /** Close reader of parsed data. */
    @Override
    public void closeReader() {try {_p.closeReader();} catch (Exception ex) {}} // ignore exception

    ////////////////////////////////////////////////////////////////////////////
    /** XML W3C parser of XON/JSON object from XON/JSON object.
     * @author Vaclav Trojan
     */
    private static class XonObjectParser implements XonParsers {
        private static final SPosition NULPOS = new SPosition(); /** Empty position. */
        private final Object _obj;
        private final XonParser _jp;

        XonObjectParser(final Object obj, final XonParser jp) {_obj = obj; _jp = jp;}

        /** Parsing of an XON/JSON object.
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
        public SPosition getPosition() {return NULPOS;} // no position here

        @Override
        public void parse() {parse(_obj);}

        @Override
        public void closeReader() {} // ignored here

        @Override
        public void setXdefMode() {} // ignored here

        @Override
        public void setReportWriter(ReportWriter reporter) {} // ignored here

        @Override
        public void setXonMode() {}

        @Override
        public void setJsonMode() {}
    }
}