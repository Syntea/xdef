package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.SRuntimeException;

/** Parser of Schema "unsignedInt" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedInt extends XSParseLong {
	private static final String ROOTBASENAME = "unsignedInt";

	public XSParseUnsignedInt() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.errors()) {
			return;
		}
		long val = p.getParsedValue().longValue();
		if (val > 4294967295L || val < 0) {
			//Value of '&{0}' is out of range
			p.error(XDEF.XDEF806, parserName());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		long val =  x.longValue();
		if (val > 4294967295L || val < 0) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}