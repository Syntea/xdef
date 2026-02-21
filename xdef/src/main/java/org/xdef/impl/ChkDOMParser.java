package org.xdef.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import static org.xdef.XDConstants.XDEF_INSTANCE_NS_URI;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.impl.code.DefOutStream;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.msg.XDEF;
import org.xdef.msg.XML;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SException;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;

/** Provides processing of an XML document in the form of org.w3c.dom object.
 * @author  Vaclav Trojan
 */
class ChkDOMParser extends SReporter {

    /** The root ChkDocument. */
    private ChkDocument _chkDoc;
    /** Element to be processed. */
    private Element _elem;

    /** This prevents the user to create instance of ChkDOMParser. */
    private ChkDOMParser() {}

    /** Create instance of ChkDOMParser with source element.
     * @param elem source element.
     * @param reporter report writer.
     */
    ChkDOMParser(ReportWriter reporter, Element elem) {
        super(reporter);
        _elem = elem;
    }

    /* The class DOMValidate separates user from the public methods of
     * ReporterInterface.
     */
    private final class DOMValidate extends ChkDOMParser {
        /** The cumulated text. */
        private StringBuilder _text;
        /** Root document. */
        private  Document _doc;

        /** Creates a new empty instance of DOMValidate. */
        private DOMValidate() {}

        /** This method adds cumulated text nodes to the result. */
        private void addText(final ChkElement actNode) {
            if (_text.length() > 0) {
                actNode.addText(_text.toString());
                _text.setLength(0);
            }
        }

        /** This method process element and all child nodes.
         * @param parentNode The parent node.
         * @param sourceElem The element with source data.
         */
        private void processElement(final ChkElement parentNode,
            final Element sourceElem) {
            String ns = sourceElem.getNamespaceURI();
            NamedNodeMap atrs = sourceElem.getAttributes();
            int maxAttr = atrs.getLength();
            String elementName = sourceElem.getNodeName();
            String xdefInstancePrefix = "";
            ChkElement chkEl;
            if (parentNode == null) {
                for (int i=0; i < maxAttr; i++) {
                    String name = atrs.item(i).getNodeName();
                    String val = atrs.item(i).getNodeValue();
                    if (name.startsWith("xmlns:")) {
                        if (XDEF_INSTANCE_NS_URI.equals(val)) {
                            xdefInstancePrefix = name.substring(6);
                            break;
                        }
                    }
                }
                if (!xdefInstancePrefix.isEmpty() && _chkDoc == null) {
                    //xdi:location
                    String key = xdefInstancePrefix + ":location";
                    String val = sourceElem.getAttribute(key);
                    if (val != null && !val.isEmpty()) {
                        StringParser p = new StringParser(val);
                        p.isSpaces();
                        String systemLiteral;
                        int i = p.isOneOfTokens("SYSTEM", "PUBLIC");
                        switch (i) {
                            case 0:
                                //SYSTEM
                                if (!p.isSpaces()) {
                                    error(XML.XML014, "PUBLIC"); //Whitespace expected after '&{0}'
                                }
                                if ((systemLiteral = p.readString())==null){
                                    error(XDEF.XDEF504); //Quoted string declaration expected"
                                    return;
                                }
                                break;
                            case 1:
                                //PUBLIC
                                if (!p.isSpaces()) {
                                    error(XML.XML014, "PUBLIC"); //Whitespace expected after '&{0}'
                                }	if (p.readString() != null) {
                                    if (!p.isSpaces()) {
                                        error(XML.XML014, "PUBLIC"); //Whitespace expected after '&{0}'
                                        return;
                                    }
                                    if ((systemLiteral = p.readString())==null){
                                        error(XDEF.XDEF504); //Quoted string declaration expected"
                                        return;
                                    }
                                } else {
                                    error(XDEF.XDEF504); //Quoted string declaration expected"
                                    return;
                                }
                                break;
                            default:
                                systemLiteral = p.getSourceBuffer().trim();
                        }
                        URL u;
                        try {
                            u = SUtils.resolveSystemID(systemLiteral, SUtils.getActualPath());
                        } catch (SException ex) {
                            Report rep = ex.getReport();
                            fatal(rep.getMsgID(), rep.getText(), rep.getModification());
                            return;
                        }
                        XDPool xdp;
                        try {
                            xdp = new XBuilder(null).setSource(u).compileXD();
                        } catch (Exception ex) {
                            fatal(XDEF.XDEF543, ex); //In X-definition are errors&{0}{: }
                            return;
                        }
                        key = xdefInstancePrefix+":xdefName"; // xdi:definition
                        val = sourceElem.getAttribute(key).trim();
                        String value = null;
                        if (!val.isEmpty()) {
                            if (xdp != null) {
                                value = val;
                            }
                        } else {
                            value = sourceElem.getTagName();
                        }
                        if (xdp != null) {
                            XMDefinition def = xdp.getXMDefinition(value);
                            if (def != null) {
                                XDOutput stdOut = null;
                                key = xdefInstancePrefix+":stdOut"; // xd:stdOut
                                val = sourceElem.getAttribute(key).trim();
                                if (!val.isEmpty() && _chkDoc != null) {
                                    value = val.trim();
                                    int index = value.indexOf(',');
                                    if (index >= 0) {
                                        String encoding = value.substring(index + 1).trim();
                                        value = value.substring(0,index).trim();
                                        stdOut = new DefOutStream(value, encoding);
                                    } else {
                                        stdOut = new DefOutStream(value);
                                    }
                                }
                                key = xdefInstancePrefix+":stdErr"; // xd:stdErr
                                val = sourceElem.getAttribute(key).trim();
                                if (!val.isEmpty() && _chkDoc != null) {
                                    value = val;
                                    int index = value.indexOf(',');
                                    if (index >= 0) {
                                        String encoding = value.substring(index + 1).trim();
                                        value = value.substring(0,index).trim();
                                        setReportWriter(new FileReportWriter(value,encoding,true));
                                    } else {
                                        setReportWriter(new FileReportWriter(value));
                                    }
                                }
                                _chkDoc = (ChkDocument) def.createXDDocument();
                                _chkDoc.getReporter().setReportWriter(getReportWriter());
                                if (stdOut != null) {
                                    _chkDoc.setStdOut(stdOut);
                                }
                            } else {
                                fatal(XDEF.XDEF530, value); //Missing X-definition &{0}{: }
                                return;
                            }
                        }
                    }
                }
                Element el = _doc.createElementNS(ns, elementName);
                for (int i = 0; i < maxAttr; i++) {
                    Node n = atrs.item(i);
                    el.setAttributeNS(n.getNamespaceURI(), n.getNodeName(), n.getNodeValue());
                }
                chkEl = _chkDoc.createRootChkElement(el, true);
            } else {
                Element el = _doc.createElementNS(ns, elementName);
                for (int i = 0; i < maxAttr; i++) {
                    Node n = atrs.item(i);
                    el.setAttributeNS(n.getNamespaceURI(), n.getNodeName(), n.getNodeValue());
                }
                chkEl = parentNode.createChkElement(el);
            }
            List<Attr> atrs1 = new ArrayList<>(); // list of processed atrs
            // Process atrributes which have model
            for (XMData x: chkEl.getXMElement().getAttrs()) {
                String uri = x.getNSUri();
                Attr att = uri == null ? (Attr) atrs.getNamedItem(x.getName())
                    : (Attr) atrs.getNamedItemNS(uri, x.getQName().getLocalPart());
                if (att != null) {
                    chkEl.newAttribute(att);
                    atrs1.add(att);
                }
            }
            // Process remaining atrributes
            for (int i = 0, max = atrs.getLength(); i < max; i++) {
                Attr attr = (Attr) atrs.item(i);
                if (atrs1.contains(attr)) {
                    continue; // already processed
                }
                chkEl.newAttribute(attr);
            }
            chkEl.checkElement();
            NodeList nl = sourceElem.getChildNodes();
            for (int i = 0, nodeMax = nl.getLength(); i < nodeMax; i++) {
                Node item = nl.item(i);
                switch (item.getNodeType()) {
                    case Node.COMMENT_NODE:
                        addText(chkEl);
                        chkEl.addComment(item.getNodeValue());
                        continue;
                    case Node.CDATA_SECTION_NODE:
                    case Node.TEXT_NODE:
                        _text.append(item.getNodeValue());
                        continue;
                    case Node.ENTITY_REFERENCE_NODE:
                        _text.append(KXmlUtils.nodeToString(item, false, false, false));
                        continue;
                    case Node.ELEMENT_NODE:
                        addText(chkEl);
                        processElement(chkEl, (Element) item);
                        continue;
                    case Node.PROCESSING_INSTRUCTION_NODE:
                        addText(chkEl);
                        chkEl.addPI(((ProcessingInstruction) item).getTarget(),
                            ((ProcessingInstruction) item).getData());
                }
            }
            addText(chkEl);
            chkEl.addElement();
            if (parentNode == null) { //root element finished!
                _chkDoc.endDocument();
            }
        }

        /** Parse XML source and process check and processing instructions.
         * @param chkDoc the ChkDocument object.
         * @param elem the element to be validated.
         * @return The ChkDocument object.
         */
        private void xvalidate(final ChkDocument chkDoc, final Element elem) {
            _chkDoc = chkDoc;
            _chkDoc._node = null;
            _chkDoc._element = null;
            XCodeProcessor scp = _chkDoc._scp;
            Properties props = scp.getProperties();
            _chkDoc._scp = null;
            _chkDoc.init((XDefinition) chkDoc.getXMDefinition(),
                (Document) _chkDoc.getDocument().cloneNode(false),
                chkDoc.getReporter(),
                props,
                chkDoc._userObject);
            _chkDoc.startDocument();
            _chkDoc._scp = scp;
            _doc = _chkDoc.getDocument();
            setReportWriter(chkDoc.getReporter().getReportWriter());
            setIndex(-1);
            setLineNumber(-1);
            if (_chkDoc.isDebug() && _chkDoc.getDebugger() != null) { // open debugger
                _chkDoc.getDebugger().openDebugger(props, _chkDoc.getXDPool());
            }
            _chkDoc._scp.initscript(); //Initialize variables and methods
            _text = new StringBuilder();
            processElement(null, elem);
        }

        @Override
        public final void warning(final String id, final String msg, final Object... mod) {
            putReport(Report.warning(id, msg, mod));
        }
        @Override
        public final void lightError(final String id, final String msg, final Object... mod) {
            putReport(Report.lightError(id, msg, mod));
        }
        @Override
        public final void error(final String id, final String msg, final Object... mod) {
            putReport(Report.error(id, msg, mod));
        }
        @Override
        public final void fatal(final String id, final String msg, final Object... mod) {
            putReport(Report.fatal(id, msg, mod));
        }
        public final void putReport(final byte type, final String id, final String msg, final Object... mod) {
            if (getReportWriter() == null) {
                if (type != Report.WARNING) {
                    throw new SRuntimeException(id, msg, mod);
                }
            } else {
                putReport(new Report(type, id, msg, mod));
            }
        }
    }

    /** Parse XML source element and process check and processing instructions.
     * @param chkDoc The ChkDocument object.
     */
    final void xparse(final ChkDocument chkDoc) {
        setReportWriter(chkDoc.getReportWriter());
        chkDoc.startDocument();
        new DOMValidate().xvalidate(chkDoc, _elem);
        chkDoc.endDocument();
    }
}