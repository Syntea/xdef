package org.xdef.impl.parsers;

import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDParserAbstract;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.XPool;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.STOP_OP;
import static org.xdef.impl.code.CodeTable.ST_GLOBAL;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parse string with a value type specification (e.g. string(%maxLength=10)).
 * @author Vaclav Trojan
 */
public class XDParseXDType extends XDParserAbstract {
    private static final String ROOTBASENAME = "xdType";

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p) {
        p.isSpaces();
        String s = p.getUnparsedBufferPart().trim();
        try {
            String xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>" +
"<xd:declaration>Parser x=" + s + ";</xd:declaration>" +
"<A/>" +
"</xd:def>";
            Properties props = new Properties();
            if (xnode.getXDPool().isChkWarnings()) {
                props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
            }
            XDPool xp = XDFactory.compileXD(props, xdef);
            XDValue[] code  =((XPool) xp).getCode();
            if (code.length == 3 && code[0].getCode() == LD_CONST && code[0].getItemId() == XD_PARSER
                && code[1].getCode() == ST_GLOBAL && code[2].getCode() == STOP_OP) {
                p.setParsedValue(code[0]);
            } else {
                XDDocument xd = xp.createXDDocument();
                xd.xparse("<A/>", null);
                XDParser x = (XDParser) xd.getVariable("x");
                p.setParsedValue(x);
            }
            p.setEos();
        } catch (Exception ex) {
            // Value "&{0}" is not a valid value type specification (in &{1})
            p.error(XDEF.XDEF817, s, parserName());
        }
    }

    @Override
    public String parserName() {return ROOTBASENAME;}

    @Override
    public short parsedType() {return XD_PARSER;}
}