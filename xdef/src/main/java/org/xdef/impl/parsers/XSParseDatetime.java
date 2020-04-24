package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "dateTime" type.
 * @author Vaclav Trojan
 */
public class XSParseDatetime extends XSParseDate {
	private static final String ROOTBASENAME = "dateTime";

	public XSParseDatetime() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLDatetime();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DATETIME;}
}