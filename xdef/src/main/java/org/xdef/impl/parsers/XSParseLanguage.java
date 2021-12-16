package org.xdef.impl.parsers;

import java.util.Locale;
import org.xdef.XDParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SParser;

/** Parser of Schema "language" type (RFC 3066 or IETF BCP 47).
 * @author Vaclav Trojan
 */
public class XSParseLanguage extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "language";

	public XSParseLanguage() {super();}
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
		String t;
		try {
			Locale lo = Locale.forLanguageTag(s);
			t = lo == null
				? "" : s.length()==2 ? lo.getLanguage() : lo.getISO3Language();
		} catch (Exception ex) {
			t = null;
		}
		if (t == null || t.isEmpty()) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		p.setParsedValue(t);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkItem(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}