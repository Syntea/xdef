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
import cz.syntea.xdef.proc.XXNode;
import java.util.Map;

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
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		String id = result.getSourceBuffer();
		Map<Object, ArrayReporter> tab = xnode.getIdRefTable();
		ArrayReporter a = tab.get(id);
		if ((a == null || a.size() > 0)) {
			//Missing an element with identifier '&{0}'
			result.error(XDEF.XDEF522, id+"&{xpath}"+xnode.getXPos()
			+ "&{xdpos}" + xnode.getXDPosition());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
