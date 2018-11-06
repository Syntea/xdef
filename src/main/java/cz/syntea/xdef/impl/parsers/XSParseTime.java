package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.sys.StringParser;

/** Parser of Schema "time" type.
 * @author Vaclav Trojan
 */
public class XSParseTime extends XSParseDate {
	private static final String ROOTBASENAME = "time";
	public XSParseTime() {super();}
	@Override
	boolean parse(final StringParser parser) {return parser.isXMLTime();}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
