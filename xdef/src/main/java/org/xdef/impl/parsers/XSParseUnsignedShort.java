package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of XML Schema (XSD) "unsignedShort" type.
 * @author Vaclav Trojan
 */
public class XSParseUnsignedShort extends XSParseLong {
	private static final String ROOTBASENAME = "unsignedShort";

	public XSParseUnsignedShort() {super();}

	@Override
	public final void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.errors()) {
			return;
		}
		long val =  p.getParsedValue().longValue();
		if (val < 0 || val > 655355) {
			p.error(XDEF.XDEF806, parserName(), val); //Value of '&{0}' is out of range&{1}{: }
		}
	}

	@Override
	public final String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		long val = x.longValue();
		if (val < 0 || val > 655355) {
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);//Incorrect range specification of &{0}
		}
	}

	@Override
	public short parsedType() {return XD_INT;}
}