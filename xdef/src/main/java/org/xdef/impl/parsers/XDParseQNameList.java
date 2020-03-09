package org.xdef.impl.parsers;

import org.xdef.sys.StringParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;

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