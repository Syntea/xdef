package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of Schema "positiveInteger" type.
 * @author Vaclav Trojan
 */
public class XSParsePositiveInteger extends XSParseInteger {
	private static final String ROOTBASENAME = "positiveInteger";

	public XSParsePositiveInteger() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.matches()) {
			if (p.getParsedValue().decimalValue().signum() <= 0) {
				//Value of '&{0}' is out of range&{1}{: }
				p.error(XDEF.XDEF806, parserName(),
					p.getParsedValue().decimalValue());
			}
		}
	}
	@Override
	/** Get name of value.
	 * @return The name.
	 */
	public String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		if (x.decimalValue().signum() <= 0) {
			//Incorrect range specification of &{0}
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME);
		}
	}
}