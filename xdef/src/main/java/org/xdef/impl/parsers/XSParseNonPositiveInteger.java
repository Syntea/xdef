package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of XML Schema (XSD) "nonPositiveInteger" type.
 * @author Vaclav Trojan
 */
public class XSParseNonPositiveInteger extends XSParseInteger {
	private static final String ROOTBASENAME = "nonPositiveInteger";

	public XSParseNonPositiveInteger() {super();}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		super.parseObject(xnode, p);
		if(p.matches()) {
			if (p.getParsedValue().decimalValue().signum() > 0) {
				//Value of '&{0}' is out of range&{1}{: }
				p.error(XDEF.XDEF806, parserName(), p.getParsedValue().decimalValue());
			}
		}
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public void checkValue(final XDValue x) {
		if (x.decimalValue().signum() > 0) {
			throw new SRuntimeException(XDEF.XDEF821, ROOTBASENAME); //Incorrect range specification of &{0}
		}
	}
}