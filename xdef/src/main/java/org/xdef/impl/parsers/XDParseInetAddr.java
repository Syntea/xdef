package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefInetAddr;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SParser;

/** Parse Internet IP address.
 * @author Vaclav Trojan
 */
public class XDParseInetAddr extends XDParserAbstract {
	private static final String ROOTBASENAME = "inetAddr";

	public XDParseInetAddr() {super();}

	private static boolean isHexNumber(XDParseResult p) {
		boolean result = false;
		while(p.isOneOfChars("0123456789ABCDEFabcdef") != SParser.NOCHAR)
			result = true;
		return result;
	}
	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		int pos = p.getIndex();
		p.isSpaces();
		boolean xon;
		if (xon = p.isToken("i(")) {
			p.isSpaces();
		}
		int pos1 = p.getIndex();
		int numParts = 0;
		while (isHexNumber(p) || p.isOneOfChars(".:") > SParser.NOCHAR) {
			numParts++;
		}
		int pos2 = p.getIndex();
		if (numParts > 1) {
			String s = p.getBufferPart(pos1, pos2);
			if (!xon || ((p.isSpaces()||true) && p.isChar(')'))) {
				try {
					p.setParsedValue(new DefInetAddr(s));
					return;
				} catch (Exception ex) {} //inet addr error
				p.setIndex(pos1);
			}
		}
		p.setParsedValue(new DefInetAddr()); //null InetAddr
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,parserName(),p.getBufferPart(pos1,pos2));
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_INETADDR;}
}