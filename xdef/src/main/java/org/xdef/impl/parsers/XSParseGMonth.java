package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser of XML Schema (XSD) "gMonth" type.
 * @author Vaclav Trojan
 */
public class XSParseGMonth extends XSParseDate {
	private static final String ROOTBASENAME = "gMonth";

	public XSParseGMonth() {super();}

	@Override
	boolean parse(final StringParser parser) {return parser.isXMLMonth();}
	@Override
	public String parserName() {return ROOTBASENAME;}
}