package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.impl.ChkNode;
import cz.syntea.xdef.impl.code.CodeUniqueset;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefContainer;

/** Parser of Schema "IDREFS" type.
 * @author Vaclav Trojan
 */
public class XSParseIDREFS extends XSParseENTITIES {
	private static final String ROOTBASENAME = "IDREFS";

	public XSParseIDREFS() {
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
			tab.getParsedItems()[0].setParsedObject(val.getXDItem(i));
			ArrayReporter a = tab.chkId();
			if (a != null) {
				//Unique value "&{0}" was not set
				a.error(XDEF.XDEF522, result.getParsedValue());
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
