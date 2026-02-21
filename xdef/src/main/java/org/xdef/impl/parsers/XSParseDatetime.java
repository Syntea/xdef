package org.xdef.impl.parsers;

import java.util.TimeZone;
import org.xdef.XDParseResult;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.MAXEXCLUSIVE;
import static org.xdef.XDParser.MAXINCLUSIVE;
import static org.xdef.XDParser.MINEXCLUSIVE;
import static org.xdef.XDParser.MININCLUSIVE;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDValueID.XD_DATETIME;
import org.xdef.impl.code.DefDate;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.StringParser;

/** Parser of XML Schema (XSD) "dateTime" type.
 * @author Vaclav Trojan
 */
public class XSParseDatetime extends XSAbstractParseComparable {
    private static final String ROOTBASENAME = "dateTime";

    public XSParseDatetime() {super();}

    @Override
    public int getLegalKeys() {
        return PATTERN +
            ENUMERATION +
            WHITESPACE + //fixed to collapse
            MAXINCLUSIVE +
            MAXEXCLUSIVE +
            MININCLUSIVE +
            MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
            BASE +
            0;
    }

    @Override
    public void parseObject(final XXNode xnode, final XDParseResult p){
        int pos0 = p.getIndex();
        p.isSpaces();
        int pos = p.getIndex();
        StringParser parser = new StringParser(p.getSourceBuffer(), pos);
        if (!parse(parser)) {
            p.errorWithString(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'&{1}{: }
            return;
        }
        SDatetime d = parser.getParsedSDatetime();
        TimeZone defaulttz;
        if (d.getTZ() == null && xnode != null && (defaulttz = xnode.getDefaultZone()) != null) {
            d.setTZ(defaulttz);
        }
        p.setParsedValue(new DefDate(d));
        p.setIndex(parser.getIndex());
        String s = p.getParsedBufferPartFrom(pos);
        p.isSpaces();
        p.replaceParsedBufferFrom(pos0, s);
        p.addReports((ArrayReporter) parser.getReportWriter());//datetime errors
        checkDate(xnode, p);
    }

    // This method is overwritten in different date/time parsers
    boolean parse(final StringParser parser) {return parser.isXMLDatetime();}

    @Override
    public short parsedType() {return XD_DATETIME;}

    @Override
    public String parserName() {return ROOTBASENAME;}
}