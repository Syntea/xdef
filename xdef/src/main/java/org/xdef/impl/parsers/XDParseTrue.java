package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;

/** Parser which any value sets as correct.
 * @author Vaclav Trojan
 */
public class XDParseTrue extends XDParserAbstract {
    private static final String ROOTBASENAME = "true_parser";

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p) {
        p.setParsedValue(p.getSourceBuffer());
        p.setEos();
    }

    @Override
    public String parserName() {return ROOTBASENAME;}
}