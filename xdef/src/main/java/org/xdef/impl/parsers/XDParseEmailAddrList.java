package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;

/** Parse list of email address (separator white space, ',' or ';')..
 * @author Vaclav Trojan
 */
public class XDParseEmailAddrList extends XDParserAbstract {

	private static final String ROOTBASENAME = "emailAddrList";

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		try {
			StringParser q = new StringParser(p.getSourceBuffer());
			q.setIndex(p.getIndex());
			XDContainer val = new DefContainer();
			for(;;) {
				val.addXDItem(new DefEmailAddr(q));
				if (q.eos() || q.isOneOfChars(",;") == SParser.NOCHAR) {
					break;
				}
			}
			if (q.eos()) {
				p.setEos();
				p.setParsedValue(val);
				return;
			}
		} catch (Exception ex) {}
		//Incorrect value of &{0}&{1}{: }
		p.errorWithString(XDEF.XDEF809, parserName());
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
}