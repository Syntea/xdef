package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "gMonthDay" type.
 * @author Vaclav Trojan
 */
public class XSParseGMonthDay extends XSParseDate {
	private static final String ROOTBASENAME = "gMonthDay";

	public XSParseGMonthDay() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLMonthDay();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

}