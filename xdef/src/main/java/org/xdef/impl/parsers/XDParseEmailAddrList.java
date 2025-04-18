package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_EMAIL;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefNull;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;

/** Parse "emailAddrList" (separator white space, ',' or ';')..
 * @author Vaclav Trojan
 */
public class XDParseEmailAddrList extends XDParserAbstract {
	private static final String ROOTBASENAME = "emailAddrList";

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
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
		p.setParsedValue(DefNull.genNullValue(parsedType()));
		p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of &{0}&{1}{: }
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_CONTAINER;}

	@Override
	public short getAlltemsType() {return XD_EMAIL;}
}