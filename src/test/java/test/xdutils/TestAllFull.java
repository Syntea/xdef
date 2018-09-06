/*
 * File: TestAllFull.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test.xdutils;

import java.io.FileOutputStream;
import java.io.PrintStream;
import test.utils.XDTester;
import test.utils.STester;

/** Run all available tests for package cz.syntea.xdef.util  with all features
 * of the tester.
 * @author Vaclav Trojan
 */
public class TestAllFull {

	private TestAllFull() {  }

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static int runTests(String[] args) {
		XDTester.setFulltestMode(true);
		PrintStream log;
		try {
			log = new PrintStream(new FileOutputStream("testUtils.log"));
		} catch (Exception ex) {
			log = null;
		}
		STester[] tests = new STester[]{
			new TestValidate(),
			new TestDTDToXdef(),
			new TestGenCollection(),
			new TestXd2XsdConv(),
			new TestXsd2XdConv(),
			new TestPrettyXdef(),
			new TestXDefUtils(),
			new TestGenDTD(),
		};
		int result = STester.runTests(System.out, System.err, log,
			tests, "package xdutils", XDTester.getFulltestMode(), args);
		if (log!= null) {
			log.close();
		}
		return result;
	}

	/** Run all available tests in this package
	 * @param args list of of arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		System.exit(runTests(args));
	}
}
