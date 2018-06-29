/*
 * File: PatternRestricted.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.util.conv.type.domain.restr;

import java.util.Set;

/** Represents patterns restricted type.
 * @author Ilia Alexandrov
 */
public interface PatternRestricted {

	/** Adds pattern.
	 *
	 * @param patern pattern.
	 */
	public void addPattern(String patern);

	/** Gets set of patterns.
	 *
	 * @return patterns set.
	 */
	public Set getPatterns();
}