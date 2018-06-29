/*
 * File: EnumerationRestricted.java
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

/** Represents enumeration restricted type.
 * @author Ilia Alexandrov
 */
public interface EnumerationRestricted {

	/** Adds enumeration.
	 *
	 * @param enumeration enumeration to add.
	 */
	public void addEnumeration(String enumeration);

	/** Gets set of enumerations.
	 *
	 * @return enumeration set.
	 */
	public Set getEnumerations();
}