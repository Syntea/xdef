package org.xdef.impl.parsers;

import java.net.MalformedURLException;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import java.net.URL;
import static org.xdef.XDParserAbstract.checkCharset;

/** Parse email address.
 * @author Vaclav Trojan
 */
public class XDParseUrl extends XDParserAbstract {

    private static final String ROOTBASENAME = "url";

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p) {
        p.isSpaces();
        String s = p.getUnparsedBufferPart().trim();
        if (chkUrl(p, s, ROOTBASENAME)) {
            p.setParsedValue(s);
            checkCharset(xnode, p);
            p.setEos();
        }
    }
    /** Check if the argument contains correct URL.
     * @param p XDParseResult where to set en error information.
     * @param s string with URL.
     * @return true if the string contains correct URL.
     */
    @SuppressWarnings("deprecation")
    final static boolean chkUrl(final XDParseResult p,
        final String s,
        final String paserName) {
        try {
            if (!s.isEmpty()) {
                new URL(s); // just to check
                return true;
            }
        } catch (MalformedURLException ex) {}
        p.errorWithString(XDEF.XDEF809, ROOTBASENAME); //Incorrect value of '&{0}'&{1}{: }
        return false;
    }

    @Override
    public String parserName() {return ROOTBASENAME;}
}