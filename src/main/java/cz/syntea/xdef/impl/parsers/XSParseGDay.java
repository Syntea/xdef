package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.sys.StringParser;

/** Parser of Schema "gDay" type.
 * @author Vaclav Trojan
 */
public class XSParseGDay extends XSParseDate {
	private static final String ROOTBASENAME = "gDay";

	public XSParseGDay() {
		super();
	}

	@Override
	boolean parse(final StringParser parser) {
		return parser.isXMLDay();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
