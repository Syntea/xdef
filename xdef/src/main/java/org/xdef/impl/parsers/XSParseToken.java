package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of Schema "token" type.
 * @author Vaclav Trojan
 */
public class XSParseToken extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "token";

	public XSParseToken() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		String s = p.nextToken();
		if (s == null) {
			//Incorrect value of '&{0}'&{1}{: }
			p.error(XDEF.XDEF809, parserName(), s);
			return;
		}
		String token;
		p.isSpaces();
		while ((token = p.nextToken()) != null) {
			s += ' ' + token;
			p.isSpaces();
		}
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}