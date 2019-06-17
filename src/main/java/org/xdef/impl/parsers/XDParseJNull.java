package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of X-Script "jnull" type.
 * @author Vaclav Trojan
 */
public class XDParseJNull  extends XSAbstractParser {
	private static final String ROOTBASENAME = "jnull";

	public XDParseJNull() {
		super();
		_whiteSpace = 'c';
	}
	@Override
	public int getLegalKeys() {return WHITESPACE;}
	@Override
	public void initParams() {
		_whiteSpace = 'c';
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		if (!p.isToken("null")) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		} else {
			String s = p.getParsedBufferPartFrom(pos);
			p.isSpaces();
			p.replaceParsedBufferFrom(pos0, s);
			checkPatterns(p);
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_NULL;}
}
