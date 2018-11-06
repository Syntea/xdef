package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefBytes;
import java.io.ByteArrayOutputStream;

/** Parser of Schema "hexBinary" type.
 * @author Vaclav Trojan
 */
public class XSParseHexBinary extends XSParseBase64Binary {
	private static final String ROOTBASENAME = "hexBinary";

	public XSParseHexBinary() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i;
		while ((i="0123456789ABCDEFabcdef".indexOf(p.getCurrentChar()))>=0){
			i = i >= 16 ? (i - 6) << 4 : i << 4;
			int j;
			p.nextChar();
			if (((j="0123456789ABCDEFabcdef".indexOf(p.getCurrentChar())) < 0)) {
				//Incorrect value of &{0}
				p.error(XDEF.XDEF809,
					"hexBinary: (must be multiple of 2 chars)");
				return;
			}
			baos.write(j >= 16 ? i + j - 6 : i + j);
			p.nextChar();
		}
		String s = p.getParsedBufferPartFrom(pos);
		if (s == null) {
			p.error(XDEF.XDEF809, "hexBinary");//Incorrect value of &{0}
			return;
		}
		p.setParsedValue(new DefBytes(baos.toByteArray()));
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		check(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}