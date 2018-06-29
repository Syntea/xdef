/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseDateYMDhms.java
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

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.SDatetime;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefDate;

/** Parser of Schema "date" type.
 * @author Vaclav Trojan
 */
public class XDParseDateYMDhms extends XSParseDatetime {
	private static final String ROOTBASENAME = "dateYMDhms";

	public XDParseDateYMDhms() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parse(parser)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}
		SDatetime d = parser.getParsedSDatetime();
		p.setParsedValue(new DefDate(d));
		p.addReports((ArrayReporter) parser.getReportWriter());//datetime errors
		p.setBufIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		if (!d.chkDatetime()) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		} else if (xnode != null && !xnode.getXDDocument().isLegalDate(d)) {
			//Range of values of year of date must be from &{0} to &{1}'
			p.error(XDEF.XDEF818, xnode.getXDDocument().getMinYear(),
				xnode.getXDDocument().getMaxYear());
			return;
		}
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	boolean parse(final StringParser parser) {
		return parser.isDatetime("yyyyMMddHHmmss");
	}
}