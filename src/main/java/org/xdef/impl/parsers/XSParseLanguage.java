package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of Schema "language" type.
 * @author Vaclav Trojan
 */
public class XSParseLanguage extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "language";

	public XSParseLanguage() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		int count = 0;
		boolean fits = true;
		while (p.isInInterval('a', 'z') != SParser.NOCHAR ||
			p.isInInterval('A', 'Z') != SParser.NOCHAR) {
			if (++count > 8) {
				//Incorrect value of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF809, parserName());
				return;
			}
		}
		if (count == 0) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		} else {
			while (fits && p.isChar('-')) {
				count = 0;
				while (p.isInInterval('a', 'z') != SParser.NOCHAR ||
					p.isInInterval('A', 'Z') != SParser.NOCHAR ||
					p.isInInterval('0', '9') != SParser.NOCHAR) {
					if (++count > 8) {
						//Incorrect value of '&{0}'&{1}{: }
						p.errorWithString(XDEF.XDEF809, parserName());
						return;
					}
				}
				if (count == 0) {
					//Incorrect value of '&{0}'&{1}{: }
					p.errorWithString(XDEF.XDEF809, parserName());
					return;
				}
			}
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}