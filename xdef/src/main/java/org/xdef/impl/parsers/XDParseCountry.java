package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SUtils;

/** Parser of ISO country code or country name.
 * @author Vaclav Trojan
 */
public class XDParseCountry extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "country";

	public XDParseCountry() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		while(p.getCurrentChar() > ' '){p.nextChar();}
		String s = p.getParsedString();
		p.isSpaces();
		try {
			SUtils.getISO3Country(s);
		} catch (Exception ex) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		p.setParsedValue(s);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkItem(p);
	}
	@Override
	public final String parserName() {return ROOTBASENAME;}
}