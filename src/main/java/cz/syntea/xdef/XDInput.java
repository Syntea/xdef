/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDInput.java, created 2011-09-10.
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

import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.ReportReader;

/** XD input stream/report reader in x-script.
 * @author Vaclav Trojan
 */
public interface XDInput extends XDValue {

	/** Reset input stream.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void reset() throws SRuntimeException;

	public Report getReport();

	public String readString();

	public String readStream();

	public void close();

	public boolean isOpened();

	/** Get reader.
	 * @return report reader.
	 */
	public ReportReader getReader();

}
