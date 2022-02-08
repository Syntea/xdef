package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_TELEPHONE;
import org.xdef.impl.code.DefIPAddr;
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
	public void parseObject(XXNode xnode, XDParseResult p) {
		int pos = p.getIndex();
		p.isSpaces();
		int pos1 = p.getIndex();
		int pos2 = pos1;
		if (p.isChar('+')) {
			if (p.isInteger()) {
				if (p.eos() || !p.isSpace()) {
					pos2 = p.getIndex();
					p.setParsedValue(new DefTelephone()); //null DefTelephone
					p.setIndex(pos);
					//Incorrect value of '&{0}'&{1}{: }
					p.errorWithString(XDEF.XDEF809,
						parserName(), p.getBufferPart(pos1, pos2));
				}
			}
		}
		while (p.isInteger()) {
			if (p.eos()) {
				pos2 = p.getIndex();
				p.setParsedValue(new DefTelephone(
					p.getBufferPart(pos1, pos2)));
				return;
			}
			if (!p.isSpace()) {
				break;
			}
		}
		p.setIndex(pos);
		p.setParsedValue(new DefIPAddr()); //null IPAddr
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,
			parserName(), p.getBufferPart(pos1, pos2));
	}
	@Override
	public String parserName() {return ROOTBASENAME;}	
	@Override
	public short parsedType() {return XD_TELEPHONE;}
}
