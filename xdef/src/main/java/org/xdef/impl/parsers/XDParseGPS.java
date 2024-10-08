package org.xdef.impl.parsers;

import org.xdef.XDGPSPosition;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Report;
import org.xdef.sys.SParser;
import org.xdef.sys.SRuntimeException;

/** Parse GPS value.
 * @author Vaclav Trojan
 */
public class XDParseGPS extends XDParserAbstract {
	private static final String ROOTBASENAME = "gps";

	public XDParseGPS() {super();}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		p.isSpaces();
		int pos = p.getIndex();
		boolean xon;
		if (xon = p.isToken("g(")) {
			p.isSpaces();
		}
		try {
			int pos1 = p.getIndex();
			if ((p.isSignedFloat() || p.isSignedInteger())) {
				double latitude =
					Double.parseDouble(p.getBufferPart(pos1, p.getIndex()));
				String name = null;
				if (p.isChar(',') && (p.isChar(' ') || true)) {
					pos1 = p.getIndex();
					if ((p.isSignedFloat() || p.isSignedInteger())) {
						double longitude = Double.parseDouble(
							p.getBufferPart(pos1, p.getIndex()));
						double altitude = Double.MIN_VALUE;
						if (p.isChar(',') && (p.isChar(' ') || true)) {
							pos1 = p.getIndex();
							if ((p.isSignedFloat() || p.isSignedInteger())) {
								altitude = Double.parseDouble(
									p.getBufferPart(pos1, p.getIndex()));
								if (p.isChar(',') && (p.isChar(' ') || true)) {
									name = readGPSName(p);
								}
							} else {
								name = readGPSName(p);
							}
						} else  if (p.isChar(',')&&(p.isChar(' ')||true)) {
							name = readGPSName(p);
						}
						if (!xon || ((p.isSpaces()||true) && p.isChar(')'))) {
							GPSPosition gpos = new GPSPosition(
								latitude, longitude, altitude, name);
							p.setParsedValue(new XDGPSPosition(gpos));
							return;
						}
					}
				}
			}
		} catch (SRuntimeException ex) {
			Report r = ex.getReport();
			p.error(r.getMsgID(), r.getText(), r.getModification());
		}
		p.setParsedValue(new XDGPSPosition()); //null GPS
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809,
			parserName(), p.getBufferPart(pos, p.getIndex()));
	}
	/** Read name of position. */
	private String readGPSName(final XDParseResult p) {
		StringBuilder sb = new StringBuilder();
		char ch;
		if (p.isChar('"')) {
			for (;;) {
				if ((ch = p.peekChar()) == SParser.NOCHAR) {
					break;
				}
				if (ch == '"') {
					if (!p.isChar('"')) {
						if (sb.length() > 0) {
							return sb.toString();
						}
						break;
					}
				}
				sb.append(ch);
			}
		} else if ((ch=p.isLetter()) != SParser.NOCHAR) {
			sb.append(ch);
			while ((ch=p.getCurrentChar()) > ' '
				&& (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-')) {
				sb.append(p.peekChar());
			}
			return sb.toString();
		}
		//Incorrect GPS position &amp;{0}{: }
		throw new SRuntimeException(XDEF.XDEF222, "name error");
	}

	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_GPSPOSITION;}
}