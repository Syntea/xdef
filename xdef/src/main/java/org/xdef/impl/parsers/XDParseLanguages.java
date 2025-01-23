package org.xdef.impl.parsers;

import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;

/** Parser of Xscript "languages" type items (RFC 3066 or IETF BCP 47).
 * @author Vaclav Trojan
 */
public class XDParseLanguages extends XDParseNCNameList {
	private static final String ROOTBASENAME = "languages";

	public XDParseLanguages() {super();}
	@Override
	XDValue parse(final XXNode xnode, final StringParser p) {
		int pos = p.getIndex();
		while(p.isLetter() != 0) {}
		try {
			String s = p.getParsedBufferPartFrom(pos);
			SUtils.getISO3Language(s);
			return new DefString(s);
		} catch (Exception ex) {}
		return null;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}