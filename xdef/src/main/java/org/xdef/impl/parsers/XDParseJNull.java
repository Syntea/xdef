package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.impl.code.DefJNull;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonReader;

/** Parser of X-Script "jnull" type.
 * @author Vaclav Trojan
 */
public class XDParseJNull  extends XSAbstractParser {
	private static final String ROOTBASENAME = "jnull";

	public XDParseJNull() {
		super();
		_whiteSpace = WS_PRESERVE;
	}
	@Override
	public int getLegalKeys() {return WHITESPACE;}
	@Override
	public void initParams() {_whiteSpace = WS_PRESERVE;}
	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		if (!p.isToken("null")) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		} else {
			String s = p.getParsedBufferPartFrom(pos);
			p.isSpaces();
			p.replaceParsedBufferFrom(pos0, s);
			p.setParsedValue(new DefJNull(XonReader.JNULL));
			checkPatterns(p);
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_NULL;}
}