/*
 * File: LengthRestricted.java
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

/** Represents item length restricted type.
 * @author Ilia Alexandrov
 */
public interface LengthRestricted {

	/** Gets length count restriction.
	 *
	 * @return length count or <code>null</code>.
	 */
	public Integer getLength();

	/** Gets minimal length count restriction.
	 *
	 * @return minimal length count or <code>null</code>.
	 */
	public Integer getMinLength();

	/** Gets maximal length count restriction.
	 *
	 * @return maximal length count or <code>null</code>.
	 */
	public Integer getMaxLength();

	/** Sets length count restriction.
	 *
	 * @param length length count.
	 * @throws IllegalArgumentException if length count is negative.
	 */
	public void setLength(int length);

	/** Sets minimal length count restriction.
	 *
	 * @param minLength minimal length count.
	 * @throws IllegalArgumentException if minimal length count is negative.
	 */
	public void setMinLength(int minLength);

	/** Sets maximal length count restriction.
	 *
	 * @param maxLength miximal length count.
	 * @throws IllegalArgumentException if maximal length count is negative.
	 */
	public void setMaxLength(int maxLength);
}