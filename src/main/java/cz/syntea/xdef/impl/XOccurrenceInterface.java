/*
 * File: XOccurrenceInterface.java.
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.model.XMOccurrence;

/** Extended occurrence interface (for internal usage).
 *  deprecated - will be not public in future versions
 * @author Vaclav Trojan
 */
public interface XOccurrenceInterface extends XMOccurrence {
	/** Object is undefined. */
	static final int UNDEFINED = -3;
	/** Object is illegal. */
	static final int ILLEGAL = -2;
	/** Object is accepted but ignored. */
	static final int IGNORE = -1;

	/** Set occurrence from other occurrence object.
	 * @param occ other occurrence.
	 */
	public void setOccurrence(XMOccurrence occ);

	/** Set occurrence from parameters.
	 * @param min minimum.
	 * @param max maximum.
	 */
	public void setOccurrence(int min, int max);

	/** Set min occurrence.
	 * @param min value of minimal occurrence.
	 */
	public void setMinOccur(int min);

	/** Set max occurrence.
	 * @param max value of maximal occurrence.
	 */
	public void setMaxOccur(int max);

	/** Set value of occurrence as illegal. */
	public void setIllegal();

	/** Set value of occurrence as ignored. */
	public void setIgnore();

	/** Set value of occurrence as fixed. */
	public void setFixed();

	/** Set value of occurrence as required. */
	public void setRequired();

	/** Set value of occurrence as optional. */
	public void setOptional();

	/** Set value of occurrence as unspecified. */
	public void setUnspecified();

	/** Set value of occurrence as unbounded. */
	public void setUnbounded();

}