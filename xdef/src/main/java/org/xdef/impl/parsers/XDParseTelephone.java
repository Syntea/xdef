package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_TELEPHONE;
import org.xdef.impl.code.DefTelephone;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-Script "telephone" type.
 * @author Vaclav Trojan
 */
public class XDParseTelephone extends XDParserAbstract {
	private static final String ROOTBASENAME = "telephone";
	public XDParseTelephone() {super();}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int i1 = p.getIndex();
		p.isSpaces();
		String parsedValue = DefTelephone.parseTelephone(p);
		p.setParsedValue(new DefTelephone(parsedValue));
		if (parsedValue == null) {
			int i2 = p.getIndex();
			p.setIndex(i1);
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809,parserName(),p.getBufferPart(i1,i2));
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_TELEPHONE;}
}