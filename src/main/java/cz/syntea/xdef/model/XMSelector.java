/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XMSelector.java
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

/** Interface of model of XML selector (groups).
 *
 * @author Vaclav Trojan
 */
public interface XMSelector extends XMNode {

	/** Get index where selector begins in child nodes list.
	 * @return index of beginning of the group.
	 */
	public int getBegIndex();

	/** Get index where selector ends in child nodes list.
	 * @return the index of beginning of the group.
	 */
	public int getEndIndex();

}