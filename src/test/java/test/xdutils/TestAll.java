/*
 * File: TestAll.java
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

import cz.syntea.xdef.sys.STester;
import java.io.FileOutputStream;
import java.io.PrintStream;

/** Run all available tests for package cz.syntea.xd.
 * @author Vaclav Trojan
 */
public class TestAll {

	private TestAll() {  }

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static int runTests(String[] args) {
		PrintStream log = null;
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
			tests, "package xdutils",
/*#if DEBUG*#/
			true, args);
/*#else*/
			false, args);
/*#end*/
		if (log!= null) {
			log.close();
		}
		return result;
	}

	/** Run all available tests in this package
	 * @param args list of of arguments
	 */
	public static void main(String... args) {
		System.exit(runTests(args));
	}
}
