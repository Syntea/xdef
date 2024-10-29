package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "date" type.
 * @author Vaclav Trojan
 */
public class XDParseDateYMDhms extends XSParseDatetime {
	private static final String ROOTBASENAME = "dateYMDhms";
	public XDParseDateYMDhms() {super();}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	boolean parse(final StringParser parser) {return parser.isDatetime("yyyyMMddHHmmss");}
}