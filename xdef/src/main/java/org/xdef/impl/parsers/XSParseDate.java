package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.StringParser;
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
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDate;

/** Parser of XML Schema (XSD) "date" type.
 * @author Vaclav Trojan
 */
public class XSParseDate extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "date";

	public XSParseDate() {super();}

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
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parse(parser)) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		p.setIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		SDatetime d = parser.getParsedSDatetime();
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(new DefDate(d));
		p.addReports((ArrayReporter) parser.getReportWriter());//datetime errors
		checkDate(xnode, p);
	}
	boolean parse(final StringParser parser) {return parser.isXMLDate();}
	@Override
	public short parsedType() {return XD_DATETIME;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
