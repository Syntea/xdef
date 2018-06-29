/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: XXData.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.proc;

/** Control data of selectors (choice, sequence, mixed).
 * @author Vaclav Trojan
 */
public interface XXSelector extends XXNode {

	/** Return flag if selector may be empty sequence.
	 * @return the value of empty flag.
	 */
	public boolean isEmptyFlag();

	/** Get index where selector begins.
	 * @return index of beginning of the group.
	 */
	public int getBegIndex();

	/** Get index where selector ends.
	 * @return the index of beginning of the group.
	 */
	public int getEndIndex();

}
