package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-script "starts" type.
 * @author Vaclav Trojan
 */
public class XDParseStarts extends XDParseEq {
    private static final String ROOTBASENAME = "starts";

    public XDParseStarts() {super();}

    @Override
    public void parseObject(final XXNode xn, final XDParseResult p){
        XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
            ? new DefParseResult(XonTools.readJString(p)) : p;
        if (q.isToken(_param)) {
            p.setEos();
            checkCharset(xn, p);
        } else {
            p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
        }
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

    @Override
    public boolean equals(final XDValue o) {
        if (!super.equals(o) || !(o instanceof XDParseStarts) ) {
            return false;
        }
        return _param.equals(((XDParseStarts) o)._param);
    }
}