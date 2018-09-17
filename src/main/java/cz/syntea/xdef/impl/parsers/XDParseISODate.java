package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.sys.StringParser;

/** Parser of X-Script "date" type.
 * @author Vaclav Trojan
 */
public class XDParseISODate extends XDParseDateYMDhms {
	private static final String ROOTBASENAME = "ISOdate";

	public XDParseISODate() {
		super();
	}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isISO8601Date();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DATETIME;}
}
