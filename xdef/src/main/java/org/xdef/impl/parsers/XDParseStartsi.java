package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;

/** Parser of X-script "startsi" type.
 * @author Vaclav Trojan
 */
public class XDParseStartsi extends XDParseEqi {
    private static final String ROOTBASENAME = "startsi";

    public XDParseStartsi() {super();}

    @Override
    public void parseObject(final XXNode xn, final XDParseResult p) {
        XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
            ? new DefParseResult(XonTools.readJString(p)) : p;
        int i = q.getIndex();
        if (q.isTokenIgnoreCase(_param)) {
            p.setParsedValue(p.getSourceBuffer().substring(i));
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
        if (!super.equals(o) || !(o instanceof XDParseStartsi) ) {
            return false;
        }
        return _param.equals(((XDParseStartsi) o)._param);
    }
}