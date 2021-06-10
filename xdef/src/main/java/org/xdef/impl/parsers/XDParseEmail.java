package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefEmail;
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
		String s = p.getUnparsedBufferPart().trim();
		if (chkEmail(p, s, ROOTBASENAME)) {
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
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName);
		} else {
			try {
				p.setParsedValue(new DefEmail(s));
				return true;
			} catch (Exception ex) {}
			//Incorrect value of &{0}&{1}&{: }
			p.errorWithString(XDEF.XDEF809, parserName);
		}
		return false;
	}

	@Override
	public short parsedType() {return XD_EMAIL;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}