package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefContainer;
import org.xdef.proc.XXNode;

/** Parser of Schema "IDREFS" type.
 * @author Vaclav Trojan
 */
public class XDParseCHKIDS extends XSParseENTITIES {
	private static final String ROOTBASENAME = "IDREFS";

	public XDParseCHKIDS() {
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
		DefContainer val = (DefContainer) result.getParsedValue();
		for (int i = 0; i < val.getXDItemsNumber(); i++) {
			XDValue id = val.getXDItem(i);
			tab.getParsedItems()[0].setParsedObject(id);
			if (!tab.hasId()) {
				result.error(XDEF.XDEF522, id + "&{xpath}" + xnode.getXPos()
					+ "&{xdpos}" + xnode.getXDPosition());
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}