package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import java.net.URL;

/** Parse email address.
 * @author Vaclav Trojan
 */
public class XDParseUrl extends XDParserAbstract {

	private static final String ROOTBASENAME = "url";

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		p.isSpaces();
		if (chkUrl(p, p.getUnparsedBufferPart().trim(), ROOTBASENAME)) {
			p.setEos();
		}
	}

	/** Check if the argument contains correct URL.
	 * @param p XDParseResult where to set en error information.
	 * @param s string with URL.
	 * @return true if the string contains correct URL.
	 */
	final static boolean chkUrl(final XDParseResult p,
		final String s,
		final String paserName) {
		try {
			if (!s.isEmpty()) {
				new URL(s);
				return true;
			}
		} catch (Exception ex) {}
		p.error(XDEF.XDEF809, ROOTBASENAME); //Incorrect value of &{0}
		return false;
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
}