package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of Schema "short" type.
 * @author Vaclav Trojan
 */
public class XSParseShort extends XSParseLong {
	private static final String ROOTBASENAME = "short";

	public XSParseShort() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.matches()) {
			long val =  p.getParsedValue().longValue();
			if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) {
				//Value of '&{0}' is out of range&{1}{: }
				p.error(XDEF.XDEF806, parserName(), val);
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}


	@Override
	public void checkValue(final XDValue x) {
		long val =  x.longValue();
		if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}