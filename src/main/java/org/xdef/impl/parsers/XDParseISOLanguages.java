package org.xdef.impl.parsers;

import org.xdef.sys.SParser;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;

/** Parser of X-Script "ISOLanguages" type.
 * @author Vaclav Trojan
 */
public class XDParseISOLanguages extends XDParseNCNameList {
	private static final String ROOTBASENAME = "ISOlanguages";

	public XDParseISOLanguages() {
		super();
	}
	@Override
	XDValue parse(final XXNode xnode, final StringParser parser) {
		int pos = parser.getIndex();
		while(!parser.eos() && parser.isLetter() != SParser.NOCHAR) {}
		try {
			String s = parser.getParsedBufferPartFrom(pos);
			SUtils.getISO3Language(s);
			return new DefString(s);
		} catch (Exception ex) {}
		return null;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}