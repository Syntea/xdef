package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.impl.code.DefParseResult;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.BNFGrammar;
import org.xdef.xon.XonTools;

/** Parse domain address.
 * @author Vaclav Trojan
 */
public class XDParseDomainAddr extends XDParserAbstract {
	private static final String ROOTBASENAME = "domainAddr";
	private static final BNFGrammar G =	BNFGrammar.compile("atom::=[-0-9a-zA-Z_]+\ndomain::=atom('.' atom)*");

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p) {
		int pos = p.getIndex();
		p.isSpaces();
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		String s = q.getUnparsedBufferPart().trim();
		if (G.parse(s, "domain")) {
			return;
		}
		p.setIndex(pos);
		p.setParsedValue(new DefIPAddr()); //null IPAddr
		p.errorWithString(XDEF.XDEF809, parserName(), s); //Incorrect value of '&{0}'&{1}{: }
	}

	@Override
	public short parsedType() {return XD_EMAIL;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}