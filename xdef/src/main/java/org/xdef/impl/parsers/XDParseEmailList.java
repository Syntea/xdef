package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefContainer;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import java.util.StringTokenizer;

/** Parse list of email address (separator white space, ',' or ';')..
 * @author Vaclav Trojan
 */
public class XDParseEmailList extends XDParserAbstract {

	private static final String ROOTBASENAME = "emailList";

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		StringTokenizer st = new StringTokenizer(
			p.getUnparsedBufferPart().trim(), ";,");
		if (!st.hasMoreTokens()) {
			//Incorrect value of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		XDContainer val = new DefContainer();
		String t = null;
		do {
			String x = st.nextToken().trim();
			if (!x.isEmpty() ) {
				if (!XDParseUri.chkUri(p, x, parserName())) {
					return;
				}
				if (t == null) {
					t = x;
				} else {
					t += ' ' + x;
				}
				val.addXDItem(x);
			}
		} while (st.hasMoreTokens());
		p.setParsedValue(val);
		p.setEos();
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
}