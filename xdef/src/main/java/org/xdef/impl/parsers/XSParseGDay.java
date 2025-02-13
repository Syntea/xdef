package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of XML Schema (XSD) "gDay" type.
 * @author Vaclav Trojan
 */
public class XSParseGDay extends XSParseDate {
	private static final String ROOTBASENAME = "gDay";

	public XSParseGDay() {super();}
	@Override
	boolean parse(final StringParser parser) {return parser.isXMLDay();}
	@Override
	public String parserName() {return ROOTBASENAME;}
}