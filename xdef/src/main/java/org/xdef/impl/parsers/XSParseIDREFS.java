package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDParseResult;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;

/** Parser of XML Schema (XSD) "IDREFS" type.
 * @author Vaclav Trojan
 */
public class XSParseIDREFS extends XSParseENTITIES {
	private static final String ROOTBASENAME = "IDREFS";

	public XSParseIDREFS() {super();}

	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			//The validation method &{0} can be called only from the X-script of attribute or text node
			result.error(XDEF.XDEF574, ROOTBASENAME);
			return;
		}
		CodeUniqueset tab = ((ChkNode) xnode).getIdRefTable();
		DefContainer val = (DefContainer) result.getParsedValue();
		for (int i = 0; i < val.getXDItemsNumber(); i++) {
			tab.getParsedItems()[0].setParsedObject(val.getXDItem(i));
			ArrayReporter a = tab.chkId();
			if (a != null) {
				a.error(XDEF.XDEF522, result.getParsedValue()); //Unique value "&{0}" was not set
			}
		}
	}

	@Override
	public short parsedType() {return XD_CONTAINER;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}