package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.proc.XXNode;
import org.xdef.sys.Report;

/** Parser of Schema "ID" type.
 * @author Vaclav Trojan
 */
public class XSParseID extends XSParseQName {
	private static final String ROOTBASENAME = "ID";

	public XSParseID() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		CodeUniqueset tab = ((ChkNode) xnode).getIdRefTable();
		tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
		Report rep = tab.setId();
		if (rep != null) {
			result.error(rep.getMsgID(), rep.getText(), rep.getModification());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}