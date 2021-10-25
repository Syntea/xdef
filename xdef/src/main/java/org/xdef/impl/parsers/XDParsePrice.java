package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefPrice;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.Price;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;

/** Parse price (with currency code).
 * @author Vaclav Trojan
 */
public class XDParsePrice extends XDParserAbstract {
	private static final String ROOTBASENAME = "price";

	public XDParsePrice() {super();}

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		p.isSpaces();
		int pos = p.getIndex();
		boolean xon;
		if (xon = p.isToken("p(")) {
			p.isSpaces();
		}
		int pos1 = p.getIndex();
		if (p.isFloat() || p.isInteger()) {
			double d = Double.parseDouble(p.getBufferPart(pos1, p.getIndex()));
			char ch;
			if (p.isChar(' ') && ((ch=p.getCurrentChar())>='A' && ch<='Z')) {
				String code = String.valueOf(ch);
				int i = 0;
				for (;;) {
					p.nextChar();
					if (++i < 3
						&& ((ch=p.getCurrentChar())>='A' && ch<='Z')) {
						code += ch;
					} else {
						break;
					}
				}
				if (!xon || ((p.isSpaces()||true) && p.isChar(')'))) {
					try {
						p.setParsedValue(new DefPrice(new Price(d, code)));
						return;
					} catch (SRuntimeException ex) { // currency error
						Report r = ex.getReport();
						p.error(r.getMsgID(), r.getText(), r.getModification());
					}
				}
			}
		}
		p.setParsedValue(new DefPrice()); //null price
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,
			parserName(), p.getBufferPart(pos, p.getIndex()));
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_PRICE;}
}