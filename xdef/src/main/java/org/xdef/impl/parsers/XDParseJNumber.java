package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefLong;

/** Parser of X-Script "jnumber" type.
 * @author Vaclav Trojan
 */
public class XDParseJNumber extends XSParseDouble {
	private static final String ROOTBASENAME = "jnumber";

	public XDParseJNumber() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos0 = p.getIndex();
		p.isSpaces();
		if (p.getCurrentChar() == '+') {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		int pos = p.getIndex();
		if (p.isSignedFloat()) {
			p.setParsedValue(new DefDecimal(p.getBufferPart(pos,p.getIndex())));
		} else if (p.isSignedInteger()) {
			try {
				p.setParsedValue(
					new DefLong(p.getBufferPart(pos,p.getIndex())));
			} catch (Exception ex) {
				p.setParsedValue(
					new DefBigInteger(p.getBufferPart(pos,p.getIndex())));
			}
		} else {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public short parsedType() {return XD_NUMBER;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}