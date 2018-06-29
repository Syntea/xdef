/*
 * Copyright 2012 Syntea software group a.s. All rights reserved.
 *
 * File: XMDocument.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.model;

/** Interface of model of XML Document.
 *
 * @author Vaclav Trojan
 */
public interface XMDocument extends XMNode {

	/** Get array of child nodes of this document.
	 * @return array of child nodes of this document.
	 */
	public XMNode[] getChildNodeModels();

}