package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefParseResult;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.xon.XonTools;

/** Parse emailAddr.
 * @author Vaclav Trojan
 */
public class XDParseEmailAddr extends XDParserAbstract {
	private static final String ROOTBASENAME = "emailAddr";

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		p.isSpaces();
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		String s = q.getUnparsedBufferPart();
		if (chkEmail(p, s, ROOTBASENAME)) {
			p.setEos();
			checkCharset(xn, p);
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
			p.errorWithString(XDEF.XDEF809, parserName); //Incorrect value of &{0}&{1}{: }
		} else {
			try {
				p.setParsedValue(new DefEmailAddr(s));
				return true;
			} catch (Exception ex) {}
			p.setParsedValue(new DefEmailAddr()); // null email
			p.errorWithString(XDEF.XDEF809, parserName, s); //Incorrect value of &{0}&{1}&{: }
		}
		return false;
	}

	@Override
	public short parsedType() {return XD_EMAIL;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}