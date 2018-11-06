package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDParseResult;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.proc.XXNode;

/** Parser of Schema "IDREF" type.
 * @author Vaclav Trojan
 */
public class XSParseIDREF extends XSParseQName {
	private final static String ROOTBASENAME = "IDREF";

	public XSParseIDREF() {super();}

	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		CodeUniqueset tab = ((ChkNode)xnode).getIdRefTable();
		tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
		ArrayReporter a = tab.chkId();
		if (a != null) {
			//Unique value "&{0}" was not set
			a.error(XDEF.XDEF522, result.getParsedValue());
		}
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
}