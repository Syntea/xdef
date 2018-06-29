/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseDate.java
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
public class XSParseDate extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "date";
	public XSParseDate() {super();}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed to collapse
			MAXINCLUSIVE +
			MAXEXCLUSIVE +
			MININCLUSIVE +
			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
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
		p.setBufIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		SDatetime d = parser.getParsedSDatetime();
//		if (d.getTZ() == null && (d.getYear() >= 0 || d.getMonth() >= 0
//			|| d.getDay() >= 0)) {
//			d.setTZ(TimeZone.getTimeZone("Z"));
//		}
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(new DefDate(d));
		p.addReports((ArrayReporter) parser.getReportWriter());//datetime errors
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
	boolean parse(final StringParser parser) {
		return parser.isXMLDate();
	}
	@Override
	public short parsedType() {return XD_DATETIME;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}