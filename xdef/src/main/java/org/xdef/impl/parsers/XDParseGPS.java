package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.impl.code.DefGPSPosition;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parse GPS value
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
			if (p.isToken("gps(")) {
				int pos1 = p.getIndex();
				if (p.isChar(')')) {
					p.setParsedValue(new DefGPSPosition());
					return; // undefined position
				} else if ((p.isSignedFloat() || p.isSignedInteger())) {
					double latitude =
						Double.parseDouble(p.getBufferPart(pos1, p.getIndex()));
					if (p.isChar(',')) {
						pos1 = p.getIndex();
						if ((p.isSignedFloat() || p.isSignedInteger())) {
							double longitude = Double.parseDouble(
								p.getBufferPart(pos1, p.getIndex()));
							if (p.isChar(',')) {
								pos1 = p.getIndex();
								if ((p.isSignedFloat() || p.isSignedInteger())){
									double altitude = Double.parseDouble(
										p.getBufferPart(pos1, p.getIndex()));
									p.setParsedValue(new DefGPSPosition(
										latitude, longitude, altitude));
								}
							} else {
								p.setParsedValue(
									new DefGPSPosition(latitude, longitude));
							}
							if (p.isChar(')')) {
								return;
							}
						}
					}
				}
			}
		} catch (Exception ex) {}
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809, parserName(),
			p.getBufferPart(pos, p.getIndex()));
	}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short parsedType() {return XD_GPSPOSITION;}
}