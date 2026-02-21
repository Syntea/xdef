package org.xdef.impl.parsers;

import java.io.File;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonTools;

/** Parse filename.
 * @author Vaclav Trojan
 */
public class XDParseFile extends XDParserAbstract {
    private static final String ROOTBASENAME = "file";

    @Override
    public void parseObject(XXNode xn, XDParseResult p) {
        p.isSpaces();
        String s = xn != null && xn.getXonMode() > 0 && p.isChar('"')
            ? XonTools.readJString(p) :  p.getUnparsedBufferPart().trim();
        if (chkFile(p, s, ROOTBASENAME)) {
            checkCharset(xn, p);
            p.setEos();
        }
    }
    /** Check if the argument contains correct filename.
     * @param p XDParseResult where to set en error information.
     * @param s string with filename.
     * @param parserName name of parser.
     * @return true if the string contains correct filename.
     */
    final static boolean chkFile(final XDParseResult p, final String s, final String parserName) {
        if (!s.isEmpty()) {
            try {
                new File(s).getCanonicalFile();
                return true;
            } catch (Exception ex) {}
        }
        p.errorWithString(XDEF.XDEF809, ROOTBASENAME); //Incorrect value of '&{0}'&{1}{: }
        return false;
    }

    @Override
    public short parsedType() {return XD_STRING;}

    @Override
    public String parserName() {return ROOTBASENAME;}
}