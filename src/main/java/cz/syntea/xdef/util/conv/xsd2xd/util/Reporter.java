/*
 * File: Reporter.java
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
package cz.syntea.xdef.util.conv.xsd2xd.util;

import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SReporter;

/** Message reporter.
 *
 * @author Alexandrov
 */
public class Reporter extends SReporter {

	/** Debug mode switch. */
	private boolean _debugMode = false;

	/** Create new instance.
	 * @param repWriterInt Report writer.
	 * @param debugMode true if debug mode.
	 */
	public Reporter(ReportWriter repWriterInt, boolean debugMode) {
		super(repWriterInt);
		_debugMode = debugMode;
	}

	/** Debug mode switch setter.
	 * @param debugMode debug mode switch.
	 */
	public void setDebugMode(boolean debugMode) {_debugMode = debugMode;}

	/** Put warning message.
	 * @param id message id.
	 * @param msg message string.
	 */
	public void warning(String id, String msg) {
		if (_debugMode) {
			super.warning(id, msg);
		}
	}
}
