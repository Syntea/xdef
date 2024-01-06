package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;

/** Parser of X-Script "letters" type.
 * @author Vaclav Trojan
 */
public class XDParseLetters extends XDParseAn {
	private static final String ROOTBASENAME = "letters";

	public XDParseLetters() {super();}
	@Override
	boolean parse(final XXNode xn, final XDParseResult p) {
		int pos = p.getIndex();
		if (p.isLetter() == 0) {
			return false;
		}
		while(p.isLetter() != 0){}
		p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}