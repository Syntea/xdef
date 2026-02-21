package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.ext.XExtUtils;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/** Parser of XML Schema (XSD) "NOTATION" type.
 * @author Vaclav Trojan
 */
public class XSParseNOTATION extends XSAbstractParseString {
    private static final String ROOTBASENAME = "NOTATION";

    public XSParseNOTATION() {super(); _minLength = _maxLength = -1;}

    @Override
    public  void initParams() {
        _patterns = null;
        _enumeration = null;
        _minLength = _maxLength = -1;
        _whiteSpace = WS_REPLACE;
    }

    @Override
    public byte getDefaultWhiteSpace() {return WS_REPLACE;}

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p){
        if (_whiteSpace == WS_COLLAPSE) {
            p.isSpaces();
        }
        if (_enumeration != null) {
            checkEnumeration(p, xnode);
        } else if (_whiteSpace == WS_COLLAPSE) {//collapse
            StringBuilder sb = new StringBuilder();
            int pos = p.getIndex();
            StringParser parser = new StringParser(p.getSourceBuffer(), pos);
            while(!parser.eos()) {
                char c;
                while ((c=parser.notOneOfChars("\n\r\t ")) != SParser.NOCHAR) {
                    sb.append(c);
                }
                int pos1 = parser.getIndex();
                if (parser.isSpaces()) {
                    if (!parser.eos()) {
                        sb.append(' ');
                    } else {
                        parser.setIndex(pos1);
                        break;
                    }
                }
            }
            String s = sb.toString();
            p.setSourceBuffer(s);
            p.setParsedValue(s);
            if (!XSParseENTITY.chkEntity(s, xnode.getElement())) {
                p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
            }
        } else {//preserve or replace
            String s = p.getUnparsedBufferPart().trim();
            if (_whiteSpace == WS_REPLACE) { //replace
                s = s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
            }
            p.setSourceBuffer(s);
            p.setParsedValue(s);
        }
        p.setEos();
        checkPatterns(p);
        checkLength(p);
        checkCharset(xnode, p);
    }
    @Override
    public void finalCheck(final XXNode xnode, final XDParseResult p) {
        if (xnode == null) {
            //The validation method &{0} can be called only from the X-script of attribute or text node
            p.error(XDEF.XDEF574, ROOTBASENAME);
            return;
        }
        Element el = xnode.getElement();
        DocumentType dt = el.getOwnerDocument().getDoctype();
        String id = p.getSourceBuffer();
        NamedNodeMap nm = dt == null ? null : dt.getNotations();
        boolean notationFound;
        if (nm == null || nm.getLength() == 0) {
            notationFound = false;
        } else {
            String nsURI = XExtUtils.getQnameNSUri(id, el);
            if (nsURI.length() > 0) {
                int ndx = id.indexOf(':');
                String localName = ndx < 0 ? id : id.substring(ndx + 1);
                notationFound = nm.getNamedItemNS(nsURI, localName) != null;
            } else {
                notationFound =  nm.getNamedItem(id) != null;
            }
        }
        if (!notationFound) {
            p.error(XDEF.XDEF809, parserName(), id); //Incorrect value of '&{0}'&{1}{: }
        }
    }

    @Override
    /** Get name of value.
     * @return The name.
     */
    public String parserName() {return ROOTBASENAME;}
}