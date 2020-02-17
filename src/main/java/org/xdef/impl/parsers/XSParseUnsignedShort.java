package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

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
			//Value of '&{0}' is out of range&{1}{: }
			p.error(XDEF.XDEF806, parserName(), val);
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