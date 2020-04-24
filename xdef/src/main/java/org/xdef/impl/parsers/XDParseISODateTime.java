package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "date" type.
 * @author Vaclav Trojan
 */
public class XDParseISODateTime extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "ISOdateTime";

	public XDParseISODateTime() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isISO8601DateAndTime();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

}