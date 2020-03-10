package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of X-Script "date" type.
 * @author Vaclav Trojan
 */
public class XDParseISOYearMonth extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "ISOyearMonth";

	public XDParseISOYearMonth() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLYearMonth();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}