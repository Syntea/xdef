package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "date" type.
 * @author Vaclav Trojan
 */
public class XDParseEmailDate extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "emailDate";

	public XDParseEmailDate() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isRFC822Datetime();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DATETIME;}

}