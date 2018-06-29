/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDConstructor.java
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

import cz.syntea.xdef.proc.XXNode;

/** Constructs XDValue from an iterated object fro XDResultSet. This interface
 * is used to construct x-script objects from XDResultSet.
 *
 * @author Vaclav Trojan
 */
public interface XDConstructor {

	/** Construct XDValue from an object.
	 * @param resultSet object from which result will be created (may be null).
	 * @param xNode XXnode from which this method was called.
	 * @return created XDObject or <tt>null</tt>.
	 */
	public XDValue construct(XDResultSet resultSet, XXNode xNode);

}