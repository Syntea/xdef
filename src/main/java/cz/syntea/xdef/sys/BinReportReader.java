/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: BinReportReader.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.sys;

import java.io.InputStream;
import java.io.PrintStream;

/** Reader of binary form of report data.
 *
 * @author Vaclav Trojan
 */
public class BinReportReader implements ReportReader {

	final private SObjectReader _in;

	public BinReportReader(final InputStream in) {_in = new SObjectReader(in);}

	@Override
	public final Report getReport() {
		try {
			return Report.readObj(_in);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return null;
		}
	}


	@Override
	public final void close() {
		try {
			_in.getStream().close();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	@Override
	public final void printReports(final PrintStream out) {
	}

	@Override
	public final void printReports(final PrintStream out,
		final String language) {
	}

	@Override
	public final String printToString() {
		return null;
	}

	@Override
	public final String printToString(final String language) {
		return null;
	}

}