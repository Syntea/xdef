package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefGPSPosition;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SParser;
import org.xdef.sys.SRuntimeException;

/** Parse GPS value.
 * @author Vaclav Trojan
 */
public class XDParseGPS extends XDParserAbstract {
	private static final String ROOTBASENAME = "gps";

	public XDParseGPS() {super();}

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		p.isSpaces();
		int pos = p.getIndex();
		try {
			if (p.isChar('(') && (p.isSignedFloat() || p.isSignedInteger())) {
				double latitude =
					Double.parseDouble(p.getParsedString().substring(1));
				String name = null;
				if (p.isChar(',')) {
					int pos1 = p.getIndex();
					if ((p.isSignedFloat() || p.isSignedInteger())) {
						double longitude = Double.parseDouble(
							p.getBufferPart(pos1, p.getIndex()));
						double altitude = Double.MIN_VALUE;
						if (p.isChar(',')) {
							pos1 = p.getIndex();
							if ((p.isSignedFloat() || p.isSignedInteger())){
								altitude = Double.parseDouble(
									p.getBufferPart(pos1, p.getIndex()));
								if (p.isChar(',')) {
									name = readGPSName(p);
								}
							} else {
								name = readGPSName(p);
							}
						} else {
							if (p.isChar(',')) {
								name = readGPSName(p);
							}
						}
						if (p.isChar(')')) {
							p.setParsedValue(new DefGPSPosition(new GPSPosition(
								latitude, longitude, altitude, name)));
							return;
						}
					}
				}
			}
		} catch (SRuntimeException ex) {
			p.putReport(ex.getReport());
		}
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809, parserName(),
			p.getBufferPart(pos, p.getIndex()));
	}

	/** Read name of position. */
	private String readGPSName(XDParseResult p) {
		StringBuilder sb = new StringBuilder();
		char ch;
		p.isSpaces();
		while ((ch = p.getCurrentChar()) != SParser.NOCHAR && ch != ')') {
			sb.append(p.peekChar());
		}
		String result = sb.toString().trim();
		if (result.isEmpty()) {
			// Incorrect GPosition &{0}{: }
			throw new SRuntimeException(XDEF.XDEF222, "name: " + result);
		}
		return result;
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_GPSPOSITION;}
}