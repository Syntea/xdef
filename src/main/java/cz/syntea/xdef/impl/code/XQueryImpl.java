/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XQueryImpl.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.xml.KXqueryExpr;
import cz.syntea.xdef.XDContainer;
import cz.syntea.xdef.proc.XXNode;
import org.w3c.dom.Node;

/**
 *
 * @author Vaclav Trojan
 */
public interface XQueryImpl {

	/** Execute XPath expression and return result.
	 * @param x XQuery expression object.
	 * @param node node or <tt>null</tt>.
	 * @param xNode node model or <tt>null</tt>.
	 * @return The string representation of value of the object.
	 */
	public XDContainer exec(KXqueryExpr x, Node node, XXNode xNode);

}