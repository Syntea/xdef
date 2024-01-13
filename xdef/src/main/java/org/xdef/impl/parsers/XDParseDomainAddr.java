package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.BNFGrammar;
import org.xdef.xon.XonTools;

/** Parse domain address.
 * @author Vaclav Trojan
 */
public class XDParseDomainAddr extends XDParserAbstract {
	private static final String ROOTBASENAME = "domainAddr";

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		int pos = p.getIndex();
		p.isSpaces();
		int pos1 = p.getIndex();
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		String s = quoted ? XonTools.readJString(p) : p.getUnparsedBufferPart();
		s = s.trim();
		BNFGrammar g = BNFGrammar.compile(
"atom ::= [-0-9a-zA-Z_]+\n"+
"domain ::= atom ('.' atom)*");
		if (g.parse(s,"domain")) {
			p.setIndex(pos1 + s.length());
			return;
		}
		p.setIndex(pos);
		p.setParsedValue(new DefIPAddr()); //null IPAddr
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,parserName(),s);
	}
	/** Check if the argument contains correct email address.
	 * @param p XDParseResult where to set en error information.
	 * @param s string with domain address.
	 * @param parserName name of parser.
	 * @return true if the string contains correct email address.
	 */
	final static boolean chkDomain(final XDParseResult p,
		final String s,
		final String parserName) {
		if (s == null || s.isEmpty()) {
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName);
		} else {
			try {
				p.setParsedValue(new DefEmailAddr(s));
				return true;
			} catch (Exception ex) {}
			p.setParsedValue(new DefEmailAddr()); // null email
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