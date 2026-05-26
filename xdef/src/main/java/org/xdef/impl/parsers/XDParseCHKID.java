package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDParseResult;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.proc.XXNode;
import org.xdef.sys.Report;
import org.xdef.sys.SReporter;

/** Parser of "CHKID" type.
 * @author Vaclav Trojan
 */
public class XDParseCHKID extends XSParseQName {
    private final static String ROOTBASENAME = "CHKID";

    public XDParseCHKID() {super();}

    @Override
    public void finalCheck(final XXNode xn, final XDParseResult result) {
        if (xn == null) {
            //The validation method &{0} can be called only from the X-script of attribute or text node
            result.error(XDEF.XDEF574, ROOTBASENAME);
            return;
        }
        CodeUniqueset tab = ((ChkNode) xn).getIdRefTable();
        tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
        ArrayReporter a = tab.chkId();
        if (a != null) {
            SReporter reporter = xn.getReporter();
            //Unique value "&{0}" was not set
            result.error(XDEF.XDEF522,
                result.getParsedString() + "&{xpath}" + xn.getXPos() + "&{xdpos}" + xn.getXDPosition());
            Report rep;
            while((rep = a.getReport()) != null) {
                reporter.putReport(rep);
            }
        }
    }
    @Override
    public String parserName() {return ROOTBASENAME;}
}