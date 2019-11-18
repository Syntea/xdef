package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;

/** Parse email address.
 * @author Vaclav Trojan
 */
public class XDParseEmail extends XDParserAbstract {

	private static final String ROOTBASENAME = "email";

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		p.isSpaces();
		if (chkEmail(p, p.getUnparsedBufferPart().trim(), ROOTBASENAME)) {
			p.setEos();
		}
	}

	/** Check if the argument contains correct email address.
	 * @param p XDParseResult where to set en error information.
	 * @param s string with email address.
	 * @param parserName name of parser.
	 * @return true if the string contains correct email address.
	 */
	final static boolean chkEmail(final XDParseResult p,
		final String s,
		final String parserName) {
		if (s == null || s.isEmpty()) {
			p.error(XDEF.XDEF809, parserName); //Incorrect value of &{0}
		} else {
			String emailregex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*"
				+ "@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
			if (s.matches(emailregex)) {
				return true;
			}
			p.error(XDEF.XDEF809, parserName); //Incorrect value of &{0}
		}
		return false;
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
}