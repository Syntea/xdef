package org.xdef.impl.parsers;

import java.util.Locale;
import org.xdef.XDParseResult;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SParser;

/** Parser of XML Schema (XSD) "language" type (RFC 3066 or IETF BCP 47).
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
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
				return;
			}
		}
		if (count == 0) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		} else {
			while (fits && p.isChar('-')) {
				count = 0;
				while (p.isInInterval('a', 'z') != SParser.NOCHAR
					|| p.isInInterval('A', 'Z') != SParser.NOCHAR
					|| p.isInInterval('0', '9') != SParser.NOCHAR){
					if (++count > 8) {
						p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
						return;
					}
				}
				if (count == 0) {
					p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
					return;
				}
			}
		}
		String s = p.getParsedBufferPartFrom(pos);
		String t;
		try {
			Locale locale = Locale.forLanguageTag(s);
			t = locale == null ? "" : locale.getISO3Language();
			if (t.length() > 0) {
				if (s.length() == 2) {
					t = locale.getLanguage();
				}
			}
		} catch (Exception ex) {
			t = null;
		}
		if (t == null || t.isEmpty()) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
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