package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDate;

/** Parser of Schema "date" type.
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
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parse(parser)) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		p.setBufIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		SDatetime d = parser.getParsedSDatetime();
//		if (d.getTZ() == null && (d.getYear() >= 0 || d.getMonth() >= 0
//			|| d.getDay() >= 0)) {
//			d.setTZ(TimeZone.getTimeZone("Z"));
//		}
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(new DefDate(d));
		p.addReports((ArrayReporter) parser.getReportWriter());//datetime errors
		if (!d.chkDatetime()) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		} else if (xnode != null && !xnode.getXDDocument().isLegalDate(d)) {
			//Range of values of year of date must be from &{0} to &{1}'
			p.error(XDEF.XDEF818, xnode.getXDDocument().getMinYear(),
				xnode.getXDDocument().getMaxYear());
			return;
		}
		checkPatterns(p);
		checkComparable(p);
	}
	boolean parse(final StringParser parser) {
		return parser.isXMLDate();
	}
	@Override
	public short parsedType() {return XD_DATETIME;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}