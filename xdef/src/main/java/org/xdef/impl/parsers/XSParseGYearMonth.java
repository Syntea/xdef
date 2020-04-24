package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "gYearMonth" type.
 * @author Vaclav Trojan
 */
public class XSParseGYearMonth extends XSParseDate {
	private static final String ROOTBASENAME = "gYearMonth";

	public XSParseGYearMonth() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLYearMonth();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}