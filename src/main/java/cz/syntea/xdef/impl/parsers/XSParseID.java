/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseID.java
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
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.impl.ChkNode;
import cz.syntea.xdef.impl.code.CodeUniqueset;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.Report;

/** Parser of Schema "ID" type.
 * @author Vaclav Trojan
 */
public class XSParseID extends XSParseQName {
	private static final String ROOTBASENAME = "ID";

	public XSParseID() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		CodeUniqueset tab = ((ChkNode) xnode).getIdRefTable();
		tab.getParsedItems()[0].setParsedObject(result.getParsedValue());
		Report rep = tab.setId();
		if (rep != null) {
			result.error(rep.getMsgID(), rep.getText(), rep.getModification());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
