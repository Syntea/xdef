package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of Schema "gYear" type.
 * @author Vaclav Trojan
 */
public class XSParseGYear extends XSParseDate {
	private static final String ROOTBASENAME = "gYear";

	public XSParseGYear() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLYear();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}