package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_IPADDR;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.impl.code.DefParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SParser;
import org.xdef.xon.XonTools;

/** Parse "ipAddr".
 * @author Vaclav Trojan
 */
public class XDParseIPAddr extends XDParserAbstract {
    private static final String ROOTBASENAME = "ipAddr";

    public XDParseIPAddr() {super();}

    @Override
    public void parseObject(final XXNode xn, final XDParseResult p) {
        int pos = p.getIndex();
        p.isSpaces();
        boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
        XDParseResult q = quoted?new DefParseResult(XonTools.readJString(p)):p;
        q.isChar('/');
        int pos1 = q.getIndex();
        int parts = 0;
        while ("0123456789abcdefABCDEF".indexOf(p.getCurrentChar()) >= 0) {
            q.nextChar();
            char ch = q.isOneOfChars(":.");
            if (ch != SParser.NOCHAR) {
                parts++;
                if(ch == ':') {
                    while(q.isChar(':')){}
                }
            }
        }
        String s = q.getBufferPart(pos1, q.getIndex());
        q.isSpaces();
        if (q.eos()) {
            if (parts > 1) {
                try {
                    p.setParsedValue(new DefIPAddr(s));
                    p.setEos();
                    return;
                } catch (Exception ex) {} //inet addr error
            }
        }
        p.setIndex(pos);
        p.setParsedValue(new DefIPAddr()); //null IPAddr
        p.errorWithString(XDEF.XDEF809,parserName(), s); //Incorrect value of '&{0}'&{1}{: }
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

    @Override
    public short parsedType() {return XD_IPADDR;}
}