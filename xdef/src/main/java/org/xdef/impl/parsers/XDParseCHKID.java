package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDParseResult;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.proc.XXNode;
import org.xdef.sys.Report;
import org.xdef.sys.SReporter;

/** Parser of Schema "IDREF" type.
 * @author Vaclav Trojan
 */
public class XDParseCHKID extends XSParseQName {
	private final static String ROOTBASENAME = "CHKID";

	public XDParseCHKID() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode in XDParseCHKID.finalCheck(parser, xnode);");
			return;
		}
		CodeUniqueset tab = ((ChkNode) xnode).getIdRefTable();
		tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
		ArrayReporter a = tab.chkId();
		if (a != null) {
			SReporter reporter = xnode.getReporter();
			result.error(XDEF.XDEF522, result.getParsedString()
				+"&{xpath}"+xnode.getXPos()
				+ "&{xdpos}" + xnode.getXDPosition());
			Report rep;
			while((rep = a.getReport()) != null) {
				reporter.putReport(rep);
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}