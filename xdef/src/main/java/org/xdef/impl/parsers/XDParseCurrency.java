package org.xdef.impl.parsers;

import org.xdef.XDCurrency;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_CURRENCY;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parse "currency" (see ISO 4217).
 * @author Vaclav Trojan
 */
public class XDParseCurrency extends XDParserAbstract {
    private static final String ROOTBASENAME = "currency";

    public XDParseCurrency() {super();}

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p) {
        p.isSpaces();
        boolean xon;
        if (xon = p.isToken("C(")) {
            p.isSpaces();
        }
        int pos1 = p.getIndex();
        char ch;
        while ((ch = p.getCurrentChar()) > ' ' && ch != ')') {
            p.nextChar();
        }
        int pos2 = p.getIndex();
        if (pos2 == pos1 + 3) {
            String s = p.getBufferPart(pos1, pos2);
            if (!xon || ((p.isSpaces()||true) && p.isChar(')'))) {
                try {
                    p.setParsedValue(new XDCurrency(s));
                    return;
                } catch (Exception ex) {} //inet addr error
            }
        }
        p.setParsedValue(new XDCurrency()); //null Currency
        //Incorrect value of '&{0}'&{1}{: }
        p.errorWithString(XDEF.XDEF809,parserName(),p.getBufferPart(pos1,pos2));
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

    @Override
    public short parsedType() {return XD_CURRENCY;}
}