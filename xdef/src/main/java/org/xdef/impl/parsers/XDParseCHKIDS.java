package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefContainer;
import org.xdef.proc.XXNode;

/** Parser of "CHKIDS" type.
 * @author Vaclav Trojan
 */
public class XDParseCHKIDS extends XSParseENTITIES {
    private static final String ROOTBASENAME = "CHKIDS";

    public XDParseCHKIDS() {super();}

    @Override
    public void finalCheck(final XXNode xnode, final XDParseResult result) {
        if (xnode == null) {
            //The validation method &{0} can be called only from the X-script of attribute or text node
            result.error(XDEF.XDEF574, ROOTBASENAME);
            return;
        }
        CodeUniqueset tab = ((ChkNode) xnode).getIdRefTable();
        DefContainer val = (DefContainer) result.getParsedValue();
        for (int i = 0; i < val.getXDItemsNumber(); i++) {
            XDValue id = val.getXDItem(i);
            tab.getParsedItems()[0].setParsedObject(id);
            if (!tab.hasId()) {
                //Unique value "&{0}" was not set
                result.error(XDEF.XDEF522, id +"&{xpath}"+xnode.getXPos()+"&{xdpos}"+xnode.getXDPosition());
            }
        }
    }

    @Override
    public short parsedType() {return XD_CONTAINER;}

    @Override
    public short getAlltemsType() {return XD_OBJECT;}

    @Override
    public String parserName() {return ROOTBASENAME;}
}