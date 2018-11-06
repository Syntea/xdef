package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.SRuntimeException;

/** Parser of Schema "int" type.
 * @author Vaclav Trojan
 */
public class XSParseInt extends XSParseLong {
	private static final String ROOTBASENAME = "int";
	public XSParseInt() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.matches()) {
			long val =  p.getParsedValue().longValue();
			if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
				//Value of '&{0}' is out of range
				p.error(XDEF.XDEF806, parserName());
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		if (x.longValue() < Integer.MIN_VALUE
			|| x.longValue() > Integer.MAX_VALUE) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}