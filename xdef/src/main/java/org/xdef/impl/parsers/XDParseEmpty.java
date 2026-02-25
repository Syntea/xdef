package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.WS_PRESERVE;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.impl.code.DefParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonTools;

/** Parser of X-script "empty" value type.
 * @author Vaclav Trojan
 */
public class XDParseEmpty extends XSAbstractParser {
    private static final String ROOTBASENAME = "empty";

    public XDParseEmpty() {super(); _whiteSpace = WS_PRESERVE;}

    @Override
    public int getLegalKeys() {return BASE;}

    @Override
    public void initParams() {_whiteSpace = WS_PRESERVE;}

    @Override
    public byte getDefaultWhiteSpace() {return WS_PRESERVE;}

    @Override
    public void parseObject(final XXNode xn, final XDParseResult p){
        boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
        String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
        if (!s.isEmpty()) {
            p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
        }
        p.setParsedValue(s);
        if (quoted) {
            p.setEos();
        }
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

    @Override
    public short parsedType() {return XD_STRING;}
}