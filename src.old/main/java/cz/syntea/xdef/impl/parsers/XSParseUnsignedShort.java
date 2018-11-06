package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.SRuntimeException;

/** Parser of Schema "unsignedShort" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedShort extends XSParseLong {
	public XSParseUnsignedShort() {super();}
	private static final String ROOTBASENAME = "unsignedShort";
	@Override
	public final void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.errors()) {
			return;
		}
		long val =  p.getParsedValue().longValue();
		if (val < 0 || val > 655355) {
			//Value of '&{0}' is out of range
			p.error(XDEF.XDEF806, parserName());
		}
	}
	@Override
	public final short parsedType() {return XD_INT;}
	@Override
	public final String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		long val = x.longValue();
		if (val < 0 || val > 655355) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}