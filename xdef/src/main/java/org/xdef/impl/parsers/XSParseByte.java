package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of Schema "byte" type.
 * @author Vaclav Trojan
 */
public class XSParseByte extends XSParseLong {
	private static final String ROOTBASENAME = "byte";

	public XSParseByte() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		super.parseObject(xnode, p);
		if (p.matches()) {
			long parsed = p.getParsedValue().longValue();
			if (parsed < -128 || parsed > 127) {
				//Value of '&{0}' is out of range&{1}{: }
				p.error(XDEF.XDEF806, parserName(), parsed);
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