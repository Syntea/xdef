package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.SRuntimeException;

/** Parser of Schema "byte" type.
 * @author Vaclav Trojan
 */
public class XSParseByte extends XSParseLong {
	private static final String ROOTBASENAME = "byte";

	public XSParseByte() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if (p.matches()) {
			long parsed = p.getParsedValue().longValue();
			if (parsed < -128 || parsed > 127) {
				//Value of '&{0}' is out of range
				p.error(XDEF.XDEF806, parserName());
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		if (x.longValue() < -128 || x.longValue() > 127) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}