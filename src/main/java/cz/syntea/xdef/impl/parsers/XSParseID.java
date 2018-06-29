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
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import java.util.Map;

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
		String id = result.getSourceBuffer();
		Map<Object, ArrayReporter> tab = xnode.getIdRefTable();
		ArrayReporter a = tab.get(id);
		if (a == null) {// new item
			tab.put(id, new ArrayReporter());  // set item to table
		} else {
			if (a.isEmpty()) {
				//value must be unique&{0}{: }
				result.error(XDEF.XDEF819, parserName() + ": " + id);
			} else {
				a.clear(); //clear unresolved references
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
