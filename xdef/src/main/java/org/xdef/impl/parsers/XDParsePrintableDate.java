package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;

/** Parser printable date and time format.
 * @author Vaclav Trojan
 */
public class XDParsePrintableDate extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "printableDate";

	public XDParsePrintableDate() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isPrintableDatetime();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DATETIME;}
}