package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import java.net.URI;

/** Parse URI.
 * @author Vaclav Trojan
 */
public class XDParseUri extends XDParserAbstract {

	private static final String ROOTBASENAME = "uri";

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		p.isSpaces();
		if (chkUri(p, p.getUnparsedBufferPart().trim(), ROOTBASENAME)) {
			p.setEos();
		}
	}

	/** Check if the argument contains correct URI.
	 * @param p XDParseResult where to set en error information.
	 * @param s string with URI.
	 * @return true if the string contains correct URI.
	 */
	final static boolean chkUri(final XDParseResult p,
		final String s,
		final String paserName) {
		try {
			if (!s.isEmpty()) {
				new URI(s);
				return true;
			}
		} catch (Exception ex) {}
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809, ROOTBASENAME);
		return false;
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
}