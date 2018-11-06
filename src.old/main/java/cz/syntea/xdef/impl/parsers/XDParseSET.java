package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.impl.ChkNode;
import cz.syntea.xdef.impl.code.CodeUniqueset;
import cz.syntea.xdef.proc.XXNode;

/** Parser of X-script "SET" type.
 * @author Vaclav Trojan
 */
public class XDParseSET extends XSParseQName {
	private static final String ROOTBASENAME = "SET";

	public XDParseSET() {
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
		tab.setId();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}