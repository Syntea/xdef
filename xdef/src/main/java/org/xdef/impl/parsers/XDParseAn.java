package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import static org.xdef.XDParserAbstract.checkCharset;
import org.xdef.impl.code.DefParseResult;
import org.xdef.proc.XXNode;
import org.xdef.xon.XonTools;

/** Parser of "an" type.
 * @author Vaclav Trojan
 */
public class XDParseAn extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "an";

	public XDParseAn() {super();}

	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		XDParseResult q = xn != null && xn.getXonMode() > 0 && p.isChar('"')
			? new DefParseResult(XonTools.readJString(p)) : p;
		if (q.isLetterOrDigit() == 0) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		while(q.isLetterOrDigit() != 0){}
		if (q.eos()) {
			checkCharset(xn, p);
			checkItem(q);
			if (p != q) {
				p.setEos();
			}
			return;
		}
		p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
}