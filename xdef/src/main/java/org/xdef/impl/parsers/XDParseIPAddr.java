package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_IPADDR;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SParser;

/** Parse Internet IP address.
 * @author Vaclav Trojan
 */
public class XDParseIPAddr extends XDParserAbstract {
	private static final String ROOTBASENAME = "ipAddr";

	public XDParseIPAddr() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		p.isSpaces();
		int pos = p.getIndex();
		p.isChar('/'); // xon format may start with '/'
		int parts = 0;
		while ("0123456789abcdefABCDEF".indexOf(p.getCurrentChar()) >= 0) {
			p.nextChar();
			char ch = p.isOneOfChars(":.");
			if (ch != SParser.NOCHAR) {
				parts++;
				if(ch == ':') {
					while(p.isChar(':')){}
				}
			}
		}
		String s = p.getBufferPart(pos, p.getIndex());
		p.isSpaces();
		if (parts > 1) {
			try {
				p.setParsedValue(new DefIPAddr(s));
				return;
			} catch (RuntimeException ex) {}
		}
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,parserName(), s);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_IPADDR;}
}