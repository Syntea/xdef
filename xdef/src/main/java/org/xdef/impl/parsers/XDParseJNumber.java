package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefLong;

/** Parser of X-script "jnumber" type.
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
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		int pos = p.getIndex();
		if (p.isSignedFloat()) {
			p.setParsedValue(new DefDecimal(p.getBufferPart(pos,p.getIndex())));
		} else if (p.isSignedInteger()) {
			try {
				p.setParsedValue(new DefLong(p.getBufferPart(pos,p.getIndex())));
			} catch (Exception ex) {
				p.setParsedValue(new DefBigInteger(p.getBufferPart(pos,p.getIndex())));
			}
		} else {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
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