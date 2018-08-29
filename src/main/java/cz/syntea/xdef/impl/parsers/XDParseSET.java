/*
 * Copyright 2016 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseSET.java, created 2016-10-30.
 * Package: cz.syntea.xd.impl.parsers
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.impl.code.CodeUniqueset;
import cz.syntea.xdef.proc.XXNode;

/** Parser of Schema "SET" type.
 * @author Vaclav Trojan
 */
public class XDParseSET extends XSParseQName {
	private static final String ROOTBASENAME = "SET";

	public XDParseSET() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		CodeUniqueset tab = (CodeUniqueset) xnode.getIdRefTable();
		tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
		tab.setId();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}