/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDDate.java, created 2011-09-07.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */

package cz.syntea.xdef;

//import cz.syntea.xd.impl.DefContainer;
import cz.syntea.xdef.proc.XXNode;
import org.w3c.dom.Node;

/** Datetime in x-script.
 * @author Vaclav Trojan
 */
public interface XDXQueryExpr extends XDValue {

	/** Execute XQuery expression and return result.
	 * @param node node or <tt>null</tt>.
	 * @param xNode node model or <tt>null</tt>.
	 * @return The string representation of value of the object.
	 */
	public XDContainer exec(Node node, XXNode xNode);

}
