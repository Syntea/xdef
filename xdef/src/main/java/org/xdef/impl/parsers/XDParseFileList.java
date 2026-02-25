package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefContainer;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import java.util.StringTokenizer;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_STRING;

/** Parse list of filenames (separators are white space, ';' or ':')..
 * @author Vaclav Trojan
 */
public class XDParseFileList extends XDParserAbstract {

    private static final String ROOTBASENAME = "fileList";

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p) {
        String s = p.getUnparsedBufferPart().trim();
        StringTokenizer st = new StringTokenizer(s, ";: \n\t\r");
        if (!st.hasMoreTokens()) {
            p.errorWithString(XDEF.XDEF809, ROOTBASENAME); //Incorrect value of '&{0}'&{1}{: }
            return;
        }
        XDContainer val = new DefContainer();
        String t = null;
        do {
            String x = st.nextToken().trim();
            if (!XDParseFile.chkFile(p, x, ROOTBASENAME)) {
                return;
            }
            if (t == null) {
                t = x;
            } else {
                t += ' ' + x;
            }
            val.addXDItem(x);
        } while (st.hasMoreTokens());
        p.setParsedValue(val);
        p.setEos();
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

    @Override
    public short parsedType() {return XD_CONTAINER;}

    @Override
    public short getAlltemsType() {return XD_STRING;}
}