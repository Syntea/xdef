package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;


/** Parser which any value sets as incorrect ("false_parser").
 * @author Vaclav Trojan
 */
public class XDParseFalse extends XDParserAbstract {
    private static final String ROOTBASENAME = "false_parser";

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p) {
        p.error(XDEF.XDEF809, "false_parser"); //Incorrect value&{0}{ of '}{'}&{1}{: '}{'}
    }
    @Override

    public String parserName() {return ROOTBASENAME;}
}