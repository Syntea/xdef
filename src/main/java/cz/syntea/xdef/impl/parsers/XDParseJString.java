/*
 * Copyright 2016 Syntea software group a.s. All rights reserved.
 *
 * File: JString.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.XDParseResult;

/** Parser of X-Script "js:string" type.
 * @author Vaclav Trojan
 */
public class XDParseJString extends XDParseAn {
	private static final String ROOTBASENAME = "jstring";
	public XDParseJString() {
		super();
	}
	@Override
	boolean parse(final XDParseResult p) {
		p.isSpaces();
		if (p.isChar('"')) { // quoted string
			StringBuilder sb = new StringBuilder();
			for (;;) {
				if (p.eos()) {
					return false;
				}
				if (p.isToken("\"\"")) {
					sb.append('"');
				} else if (p.isChar('"')) {
					p.setParsedValue(sb.toString());
					return true;
				} else {
					sb.append(p.peekChar());
				}
			}
		} else if (!p.eos()) {//not quoed string
			int pos = p.getIndex();
			char ch;
			while (!p.eos() && (ch = p.getCurrentChar()) != ' '
				&& ch != '\t' && ch != '\r' && ch != '\n') {
//				sb.append(ch = p.peekChar());
				ch = p.peekChar();
			}
			p.setParsedValue(p.getBufferPart(pos, p.getIndex()));
			return true;
		}
		return false;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}