package org.xdef.impl.parsers;

import java.io.File;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import static org.xdef.XDValueID.XD_FILE;
import org.xdef.impl.code.DefFile;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parse filename.
 * @author Vaclav Trojan
 */
public class XDParseFile extends XDParserAbstract {

	private static final String ROOTBASENAME = "file";

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		p.isSpaces();
		String s = p.getUnparsedBufferPart().trim();
		if (chkFile(p, s, ROOTBASENAME)) {
			p.setEos();
		}
	}

	/** Check if the argument contains correct filename.
	 * @param p XDParseResult where to set en error information.
	 * @param s string with filename.
	 * @param parserName name of parser.
	 * @return true if the string contains correct filename.
	 */
	final static boolean chkFile(final XDParseResult p,
		final String s,
		final String parserName) {
		if (!s.isEmpty()) {
			try {
				p.setParsedValue(new DefFile(new File(s)));
				return true;
			} catch (Exception ex) {}
		}
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809, ROOTBASENAME);
		return false;
	}

	@Override
	public short parsedType() {return XD_FILE;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}