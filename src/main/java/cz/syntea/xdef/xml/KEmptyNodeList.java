/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: KEmptyNodeList.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Implemetation of empty object org.w3c.dom.Nodelist.
 *
 * @author Vaclav Trojan
 */

public final class KEmptyNodeList implements NodeList {
	/** Creates a new instance of KEmptyNodeList */
	public KEmptyNodeList() {}
	@Override
	public final int getLength() {return 0;}
	@Override
	public final Node item(int index) {return null;}
}