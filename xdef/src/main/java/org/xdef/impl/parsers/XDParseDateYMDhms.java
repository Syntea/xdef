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
public class XDParseDateYMDhms extends XSParseDatetime {
	private static final String ROOTBASENAME = "dateYMDhms";

	public XDParseDateYMDhms() {
		super();
	}
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
		SDatetime d = parser.getParsedSDatetime();
		p.setParsedValue(new DefDate(d));
		p.addReports((ArrayReporter) parser.getReportWriter());//datetime errors
		p.setBufIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
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
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isDatetime("yyyyMMddHHmmss");
	}
}