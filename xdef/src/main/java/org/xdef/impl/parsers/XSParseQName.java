package org.xdef.impl.parsers;

import org.w3c.dom.Element;
import org.xdef.XDParseResult;
import static org.xdef.XDParserAbstract.checkCharset;
import org.xdef.impl.code.DefQName;
import org.xdef.impl.ext.XExtUtils;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.StringParser;

/** Parser of XML Schema (XSD) "QName" type.
 * @author Vaclav Trojan
 */
public class XSParseQName  extends XSAbstractParseToken {
    private static final String ROOTBASENAME = "QName";
    DefQName _value;

    @Override
    public void parseObject(XXNode xnode, XDParseResult p) {
        int pos0 = p.getIndex();
        p.isSpaces();
        int pos = p.getIndex();
        StringParser parser = new StringParser(p.getSourceBuffer(), pos);
        if (!parser.isXMLQName(StringParser.XMLVER1_0)) {
            p.errorWithString(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'&{1}{: }
            return;
        }
        String s = parser.getParsedString();
        p.setIndex(parser.getIndex());
        p.isSpaces();
        p.replaceParsedBufferFrom(pos0, s);

        p.setParsedValue(_value = new DefQName(s));
        p.setParsedValue(s);
        checkItem(p);
        checkCharset(xnode, p);
    }

    public XSParseQName() {super();}

    @Override
    public void finalCheck(final XXNode xnode, XDParseResult p) {
        Element el;
        if (xnode == null || (el = xnode.getElement()) == null) {
            return; // If the context element is not acv available ignore test of namexpace
        }
        String prefix = _value.getPrefix();
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        String nsURI = XExtUtils.getQnameNSUri(prefix, el);
        if (nsURI == null || nsURI.isEmpty()) {
            p.error(XDEF.XDEF809,parserName(),"Missing value of 'xmlns:"+prefix+"'");//Incorrect value of '&{0}'&{1}{: }
        } else {
            _value = new DefQName(nsURI, _value.getLocalName() + ":" + _value.getPrefix(), _value.getPrefix());
        }
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

}
