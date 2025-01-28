package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of XML Schema (XSD) "Name" type.
 * @author Vaclav Trojan
 */
public class XSParseName extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "Name";

	public XSParseName() {super();}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parser.isXMLName(StringParser.XMLVER1_0)) {
			p.errorWithString(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'&{1}{: }
			return;
		}
		String s = parser.getParsedString();
		p.setIndex(parser.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}