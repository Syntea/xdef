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
		if (p.isFloat() || p.isInteger()) {
			double d = Double.parseDouble(p.getParsedString());
			char ch;
			if (p.isChar(' ') && ((ch=p.getCurrentChar())>='A' && ch<='Z')){
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
				if (i == 3) {
					try {
						p.setParsedValue(new DefPrice(
							new Price(d, code)));
						return;
					} catch (SRuntimeException ex) {
						Report r = ex.getReport();
						p.error(r.getMsgID(), //currency error ?
							r.getText(), r.getModification());
						return;
					}
				}
			}
		}
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809, parserName(),
			p.getBufferPart(pos, p.getIndex()));
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_PRICE;}
}