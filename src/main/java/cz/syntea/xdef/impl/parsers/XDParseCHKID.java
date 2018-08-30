/*
 * Copyright 2016 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseCHKID.java
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
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.impl.ChkNode;
import cz.syntea.xdef.impl.code.CodeUniqueset;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SReporter;

/** Parser of Schema "IDREF" type.
 * @author Vaclav Trojan
 */
public class XDParseCHKID extends XSParseQName {
	private final static String ROOTBASENAME = "CHKID";

	public XDParseCHKID() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode in XDParseCHKID.finalCheck(parser, xnode);");
			return;
		}
		CodeUniqueset tab = ((ChkNode) xnode).getIdRefTable();
		tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
		ArrayReporter a = tab.chkId();
		if (a != null) {
			SReporter reporter = xnode.getReporter();
			result.error(XDEF.XDEF522, result.getParsedString()
				+"&{xpath}"+xnode.getXPos()
				+ "&{xdpos}" + xnode.getXDPosition());
			Report rep;
			while((rep = a.getReport()) != null) {
				reporter.putReport(rep);
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
