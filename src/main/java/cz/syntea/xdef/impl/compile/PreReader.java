/*
 * File: PreReader.java
 *
 * Copyright 2018 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import java.io.InputStream;

/** Interface for readers of source X-definition (SML, JSON).
 * @author Vaclav Trojan
 */
public interface PreReader {

	/** Parse source input stream.
	 * @param in input stream with source data.
	 * @param sysId system ID of source data.
	 * @throws Exception if an error occurs.
	 */
	public void doParse(final InputStream in, final String sysId)
		throws Exception;
}