package org.xdef.impl.parsers;

import org.xdef.XDValue;
import org.xdef.impl.code.DefString;
import org.xdef.proc.XXNode;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/**
 *
 * @author Vaclav Trojan
 */
public class XDParseCountries  extends XDParseNCNameList {
	private static final String ROOTBASENAME = "countries";

	public XDParseCountries() {super();}
	@Override
	XDValue parse(final XXNode xnode, final StringParser p) {
		p.isSpaces();
		int pos = p.getIndex();
		while(p.getCurrentChar() > ' ') {p.nextChar();}
		try {
			String s = p.getParsedBufferPartFrom(pos);
			SUtils.getISO3Country(s);
			return new DefString(s);
		} catch (Exception ex) {}
		return null;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}