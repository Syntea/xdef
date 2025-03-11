package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of XML Schema (XSD) "NMTOKEN" type.
 * @author Vaclav Trojan
 */
public class XSParseNMTOKEN extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "NMTOKEN";

	public XSParseNMTOKEN() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		String token = p.nextToken();
		if (!StringParser.chkNMToken(token, StringParser.XMLVER1_0)) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
		checkCharset(xnode, p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}