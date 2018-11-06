package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefString;

/** Parser of X-Script "NCNameList" type.
 * @author Vaclav Trojan
 */
public class XDParseQNameList extends XDParseNCNameList {
	private static final String ROOTBASENAME = "QNameList";
	public XDParseQNameList() {
		super();
	}
	@Override
	XDValue parse(final XXNode xnode, final StringParser p) {
		if (!p.isXMLName((byte) 10)) {
			return null;
		}
		return new DefString(p.getParsedString());
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
