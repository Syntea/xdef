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

	public XDParseDateYMDhms() {super();}
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
		p.setIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkDate(xnode, p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isDatetime("yyyyMMddHHmmss");
	}
}