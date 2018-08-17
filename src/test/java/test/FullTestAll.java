/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: FullTestAll.java, created 2010-03-27.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test;

import test.util.XDefTester;

/** Execute all tests verbose.
 * @author Vaclav Trojan
 */
public class FullTestAll {
	/** @param args the command line arguments. */
	public static void main(String... args) {
		XDefTester.setFulltestMode(true);
		test.common.TestAll.runTests(args);
		test.xdef.TestAll.runTests(args);
		test.xdutils.TestAll.runTests(args);
	}
}
