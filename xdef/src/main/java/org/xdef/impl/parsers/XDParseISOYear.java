package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of X-Script "date" type.
 * @author Vaclav Trojan
 */
public class XDParseISOYear extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "ISOyear";

	public XDParseISOYear() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLYear();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}