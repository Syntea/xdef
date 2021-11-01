package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import static org.xdef.sys.SParser.NOCHAR;

/** Parse Internet IP address.
 * @author Vaclav Trojan
 */
public class XDParseIPAddr extends XDParserAbstract {
	private static final String ROOTBASENAME = "ipAddr";

	public XDParseIPAddr() {super();}

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		int pos = p.getIndex();
		p.isSpaces();
		p.isChar('/');
		int pos1 = p.getIndex();
		int parts = 0;
		while ("0123456789abcdefABCDEF".indexOf(p.getCurrentChar()) >= 0) {
			p.nextChar();
			char ch = p.isOneOfChars(":.");
			if (ch != NOCHAR) {
				parts++;
				if(ch == ':') {
					while(p.isChar(':')){}
				}
			}
		}
		int pos2 = p.getIndex();
		String s = "";
		if (parts > 1) {
			s = p.getBufferPart(pos1, pos2);
			try {
				p.setParsedValue(new DefIPAddr(s));
				return;
			} catch (Exception ex) {} //inet addr error
		}
		p.setIndex(pos);
		p.setParsedValue(new DefIPAddr()); //null IPAddr
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,parserName(), s);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_IPADDR;}
}